# -*- coding: utf-8 -*-
"""
批量上传 D:\video 课程视频到「云舒艺境」平台（v2，支持边传边删 + 断点续传）。

manifest（upload_manifest.json）按「课程名 -> 课时列表」存储每条视频的
标题/章节/时长/URL/源文件路径/是否已删，因此即使源文件被删除也能重建数据库。

流程：
  1. 管理员登录；
  2. 封面走 /files/upload，视频走 /files/presign-upload + PUT（sha256 去重）；
  3. 解析文件名 "<课程>-P<n> <标题>.mp4"，生成 course/chapter/lesson 记录；
  4. 全部上传后一次性写库（幂等覆盖）。

用法：
  python upload_videos.py                          # 预览
  python upload_videos.py --limit 1 --apply        # 先传 1 个课程验证
  python upload_videos.py --apply --delete-after   # 全量上传，传完即删源文件
"""
import argparse
import hashlib
import json
import os
import re
import struct
import subprocess
import sys
import time

import requests

try:
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")
except Exception:
    pass

# ---------------- 配置 ----------------
DEFAULT_ROOT = r"D:\video"
BASE_URL = os.environ.get("EMP_API_BASE", "http://127.0.0.1:8080/api")
ADMIN_NO = os.environ.get("EMP_ADMIN_NO", "admin")
ADMIN_PWD = os.environ.get("EMP_ADMIN_PWD", "123456")
DB_NAME = "enterprise_academy"
DB_USER = "root"
DB_PWD = os.environ.get("MYSQL_PASSWORD", "123456")
MANIFEST = os.path.join(os.path.dirname(os.path.abspath(__file__)), "upload_manifest_v2.json")
COURSE_CODES_FILE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "course_codes.json")

VIDEO_EXTS = {".mp4"}
IMAGE_EXTS = {".png", ".jpg", ".jpeg", ".webp", ".bmp", ".gif"}
TEMP_MARK = ".bilibili-video-downloader"

COURSE_ID_START = 103      # 从 103 开始（100-102 是已有课程）
CHAPTER_ID_START = 4000
LESSON_ID_START = 40000
CATEGORY_ID = 2
CATEGORY_LABEL = "编程开发"

session = requests.Session()


def log(msg):
    print(msg, flush=True)


def sha256_file(path, chunk=4 * 1024 * 1024):
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for block in iter(lambda: f.read(chunk), b""):
            h.update(block)
    return h.hexdigest()


def dur(path):
    try:
        with open(path, "rb") as f:
            f.seek(0, 2); n = f.tell(); f.seek(0)
            while f.tell() < n - 8:
                p = f.tell(); h = f.read(8)
                if len(h) < 8: break
                s = struct.unpack(">I", h[:4])[0]; t = h[4:8]
                if s == 1: s = struct.unpack(">Q", f.read(8))[0]
                if s == 0: s = n - p
                if s < 8: break
                if t == b"moov":
                    end = p + s
                    while f.tell() < end - 8:
                        p2 = f.tell(); h2 = f.read(8)
                        if len(h2) < 8: break
                        s2 = struct.unpack(">I", h2[:4])[0]; t2 = h2[4:8]
                        if s2 == 1: s2 = struct.unpack(">Q", f.read(8))[0]
                        if s2 < 8: break
                        if t2 == b"mvhd":
                            v = f.read(1)[0]; f.read(3)
                            if v == 1:
                                f.read(16); ts = struct.unpack(">I", f.read(4))[0]
                                d = struct.unpack(">Q", f.read(8))[0]
                            else:
                                f.read(8); ts = struct.unpack(">I", f.read(4))[0]
                                d = struct.unpack(">I", f.read(4))[0]
                            return int(d / ts) if ts else 0
                        f.seek(p2 + s2)
                    return 0
                f.seek(p + s)
    except Exception:
        return 0
    return 0


def esc(s):
    return str(s).replace("\\", "\\\\").replace("'", "\\'")


def login():
    r = session.post(BASE_URL + "/auth/login",
                     json={"employeeNo": ADMIN_NO, "password": ADMIN_PWD}, timeout=30)
    r.raise_for_status()
    data = r.json()
    if data.get("code") != 0:
        raise RuntimeError("登录失败: " + str(data.get("message")))
    session.headers.update({"Authorization": "Bearer " + data["data"]["token"]})
    log("登录成功: " + str(data["data"]["user"].get("employeeNo")))


def upload_small(path, folder):
    with open(path, "rb") as f:
        r = session.post(BASE_URL + "/files/upload",
                         files={"file": (os.path.basename(path), f)},
                         data={"folder": folder}, timeout=120)
    r.raise_for_status()
    data = r.json()
    if data.get("code") != 0:
        raise RuntimeError("封面上传失败: " + str(data.get("message")))
    return data["data"]["url"]


def upload_large(path, code=None, episode=None, retries=6):
    size = os.path.getsize(path)
    digest = sha256_file(path)
    name = os.path.basename(path)
    last_err = None
    for attempt in range(1, retries + 1):
        try:
            payload = {"fileName": name, "contentType": "video/mp4", "folder": "video",
                       "sha256": digest, "size": size}
            if code and episode:
                payload["courseCode"] = code
                payload["episode"] = episode
            r = session.post(BASE_URL + "/files/presign-upload", json=payload, timeout=60)
            r.raise_for_status()
            data = r.json()
            if data.get("code") != 0:
                raise RuntimeError("预签名失败: " + str(data.get("message")))
            info = data["data"]
            if info.get("deduplicated"):
                return info["url"], True
            with open(path, "rb") as f:
                body = f.read()
            put = requests.put(info["uploadUrl"], data=body,
                               headers={"Content-Type": "video/mp4"}, timeout=1800)
            if put.status_code not in (200, 204):
                raise RuntimeError("PUT HTTP %d: %s" %
                                   (put.status_code, put.text[:120]))
            return info["url"], False
        except Exception as e:
            last_err = e
            if attempt < retries:
                wait = 3 * attempt
                log("  !! 重试 %d/%d (%s) 等 %ds: %s" %
                    (attempt, retries, name[:22], wait, str(e)[:80]))
                time.sleep(wait)
    raise last_err


def parse_lesson(filename):
    stem = os.path.splitext(filename)[0]
    m = re.match(r".*-P(\d+)\s*(.*)$", stem)
    if not m:
        return 0, stem
    return int(m.group(1)), (m.group(2) or stem).strip()


def chapter_of(title):
    m = re.search(r"第\s*(\d+)\s*章", title)
    return ("第%d章" % int(m.group(1))) if m else "第1章"


def load_manifest():
    if os.path.exists(MANIFEST):
        try:
            with open(MANIFEST, "r", encoding="utf-8") as f:
                return json.load(f)
        except Exception:
            return {}
    return {}


def save_manifest(m):
    # 原子写：先写临时文件再替换，避免中断导致 manifest 半截损坏
    tmp = MANIFEST + ".tmp"
    with open(tmp, "w", encoding="utf-8") as f:
        json.dump(m, f, ensure_ascii=False, indent=2)
        f.flush()
        os.fsync(f.fileno())
    os.replace(tmp, MANIFEST)


def import_sql(sql_text):
    p = subprocess.run(["mysql", "-u" + DB_USER, "-p" + DB_PWD, DB_NAME],
                       input=("SET NAMES utf8mb4;\n" + sql_text).encode("utf-8"),
                       stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    if p.returncode != 0:
        raise RuntimeError("SQL 导入失败: " + p.stderr.decode("utf-8", "replace")[:500])
    return p.stdout.decode("utf-8", "replace").strip()


def main():
    ap = argparse.ArgumentParser(description="批量上传课程视频到云舒艺境 v2")
    ap.add_argument("--root", default=DEFAULT_ROOT, help="视频根目录")
    ap.add_argument("--apply", action="store_true", help="真正上传并写库")
    ap.add_argument("--limit", type=int, default=0, help="只处理前 N 个课程（0=全部）")
    ap.add_argument("--skip-db", action="store_true", help="只上传文件，不写数据库")
    ap.add_argument("--delete-after", action="store_true", help="上传成功后删除源视频")
    args = ap.parse_args()

    if not os.path.isdir(args.root):
        log("目录不存在: " + args.root); return

    manifest = load_manifest()
    course_codes = {}
    if os.path.exists(COURSE_CODES_FILE):
        with open(COURSE_CODES_FILE, "r", encoding="utf-8") as f:
            course_codes = json.load(f)
    courses = sorted([d for d in os.listdir(args.root)
                      if os.path.isdir(os.path.join(args.root, d))])
    # 合并 manifest 里已删除源文件但已上传的课程（保持固定顺序）
    # 用小写做键去重，避免目录名大小写与 manifest 不一致导致重复课程
    merged = {}
    for c in courses:
        merged.setdefault(c.lower(), c)
    for c in manifest.keys():
        merged.setdefault(c.lower(), c)
    all_courses = sorted(merged.values(), key=lambda x: x.lower())
    # manifest 的 key（可能小写）统一映射回 all_courses 里的规范课程名
    canon = {c.lower(): c for c in all_courses}
    for old in list(manifest.keys()):
        target = canon.get(old.lower())
        if target and target != old:
            manifest[target] = manifest.pop(old)
    if args.limit:
        all_courses = all_courses[:args.limit]

    total = sum(len(manifest.get(c, {}).get("lessons", [])) for c in all_courses)
    log("发现 %d 个课程（manifest 已记录 %d 条课时），模式=%s" %
        (len(all_courses), total, "执行" if args.apply else "预览"))

    if not args.apply:
        for c in all_courses:
            rec = manifest.get(c, {})
            n = len(rec.get("lessons", []))
            src = "manifest" if c not in courses else "目录"
            log("  [%s] %s (%d 课时)" % (src, c[:44], n))
        log("\n确认后执行: python upload_videos.py --apply --delete-after")
        return

    login()
    new_uploads = 0
    reused = 0

    for cname in all_courses:
        rec = manifest.get(cname)
        if rec is None:
            rec = {"cover": "", "lessons": []}
            manifest[cname] = rec
        cdir = os.path.join(args.root, cname)
        log("\n=== 课程: %s (%d 课时) ===" % (cname[:40], len(rec["lessons"])))

        # 封面
        if not rec.get("cover") and os.path.isdir(cdir):
            covers = []
            for dp, _dn, fn in os.walk(cdir):
                for f in fn:
                    if os.path.splitext(f)[1].lower() in IMAGE_EXTS:
                        covers.append(os.path.join(dp, f))
            if covers:
                covers.sort()
                rec["cover"] = upload_small(covers[0], "cover")
                save_manifest(manifest)

        # 扫描源目录中尚未记录的视频
        if os.path.isdir(cdir):
            existing = {l["file"] for l in rec["lessons"]}
            found = []
            for dp, _dn, fn in os.walk(cdir):
                for f in fn:
                    ext = os.path.splitext(f)[1].lower()
                    if ext in VIDEO_EXTS and TEMP_MARK not in f:
                        found.append(os.path.join(dp, f))
            found.sort(key=lambda p: parse_lesson(os.path.basename(p))[0] or 0)
            for v in found:
                key = v.replace("\\", "/").lower()
                if key in existing:
                    continue
                # 源文件存在但 manifest 无记录，需要上传
                idx, title = parse_lesson(os.path.basename(v))
                code = course_codes.get(cname, {}).get("code")
                url, dedup = upload_large(v, code=code, episode=idx)
                if dedup:
                    reused += 1
                else:
                    new_uploads += 1
                rec["lessons"].append({
                    "file": key, "idx": idx, "title": title,
                    "chapter": chapter_of(title), "duration": dur(v),
                    "url": url, "deleted": False,
                })
                save_manifest(manifest)
                if args.delete_after:
                    try:
                        os.remove(v)
                        rec["lessons"][-1]["deleted"] = True
                        save_manifest(manifest)
                    except OSError:
                        pass

    save_manifest(manifest)
    log("\n上传阶段完成：新传 %d，去重复用 %d" % (new_uploads, reused))

    if not args.skip_db:
        # 按固定顺序分配 ID
        sql = []
        cid = COURSE_ID_START
        chid = CHAPTER_ID_START
        lid = LESSON_ID_START
        for cname in all_courses:
            rec = manifest[cname]
            lessons = rec.get("lessons", [])
            if not lessons:
                continue
            sql.append(
                "INSERT INTO course (id,title,category_id,subcategory_label,thumbnail_url,summary,status,created_at,updated_at) "
                "VALUES (%d,'%s',%d,'%s','%s','%s',2,NOW(),NOW());" %
                (cid, esc(cname), CATEGORY_ID, esc(CATEGORY_LABEL),
                 esc(rec.get("cover", "")), esc("共 %d 节课程视频" % len(lessons))))
            chapters = {}
            for l in lessons:
                chapters.setdefault(l["chapter"], []).append(l)
            for ci, (ch_name, ls) in enumerate(chapters.items()):
                sql.append(
                    "INSERT INTO course_chapter (id,course_id,title,sort_no,created_at,updated_at) "
                    "VALUES (%d,%d,'%s',%d,NOW(),NOW());" %
                    (chid, cid, esc(ch_name), ci + 1))
                for l in ls:
                    lid += 1
                    sql.append(
                        "INSERT INTO course_lesson (id,chapter_id,title,duration_seconds,video_url,sort_no,created_at,updated_at) "
                        "VALUES (%d,%d,'%s',%d,'%s',%d,NOW(),NOW());" %
                        (lid, chid, esc(l["title"]), l.get("duration", 0),
                         esc(l["url"]), l.get("idx", 1) or 1))
                chid += 1
            cid += 1
        cleanup = (
            "DELETE FROM course_lesson WHERE chapter_id IN "
            "(SELECT id FROM course_chapter WHERE course_id BETWEEN %d AND %d);\n"
            "DELETE FROM course_chapter WHERE course_id BETWEEN %d AND %d;\n"
            "DELETE FROM course WHERE id BETWEEN %d AND %d;\n" %
            (COURSE_ID_START, cid - 1, COURSE_ID_START, cid - 1, COURSE_ID_START, cid - 1))
        out = import_sql(cleanup + "\n".join(sql))
        log("数据库导入完成: " + (out or "ok"))
    log("全部完成。")


if __name__ == "__main__":
    main()