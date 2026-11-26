"""
D:\video 封面去重脚本
遍历根目录（含所有子目录），每个目录里若有多张图片（封面），
按自然排序保留第一张（通常是第 1 集封面），删除其余图片。
默认只预览不删除；加 --apply 才真正删除。
用法：
    python dedupe_covers.py            # 预览
    python dedupe_covers.py --apply    # 真正删除
"""

import argparse
import os
import re

DEFAULT_ROOT = r"D:\video"
IMAGE_EXTS = {".png", ".jpg", ".jpeg", ".webp", ".bmp", ".gif"}


def natural_key(name):
    """自然排序：P2 排在 P10 前面，而不是按字符串字典序。"""
    return [int(t) if t.isdigit() else t.lower()
            for t in re.split(r"(\d+)", name)]


def main():
    parser = argparse.ArgumentParser(description="每个文件夹只保留一张封面")
    parser.add_argument("--root", default=DEFAULT_ROOT, help="要处理的根目录")
    parser.add_argument("--apply", action="store_true", help="真正删除；默认仅预览")
    args = parser.parse_args()

    if not os.path.isdir(args.root):
        print(f"目录不存在: {args.root}")
        return

    total_dirs = 0
    total_removed = 0
    total_bytes = 0

    for dirpath, _dirnames, filenames in os.walk(args.root):
        images = [f for f in filenames
                  if os.path.splitext(f)[1].lower() in IMAGE_EXTS]
        if len(images) <= 1:
            continue

        images.sort(key=natural_key)
        keep = images[0]
        total_dirs += 1

        print(f"\n[目录] {dirpath}")
        print(f"  共 {len(images)} 张图片，保留: {keep}")

        for name in images[1:]:
            path = os.path.join(dirpath, name)
            try:
                size = os.path.getsize(path)
            except OSError:
                size = 0
            total_bytes += size

            if args.apply:
                try:
                    os.remove(path)
                    print(f"  已删除: {name} ({size} bytes)")
                except OSError as e:
                    print(f"  删除失败: {name} -> {e}")
                    continue
            else:
                print(f"  [预览] 将删除: {name} ({size} bytes)")
            total_removed += 1

    mode = "执行" if args.apply else "预览"
    print(f"\n=== {mode}完成：{total_dirs} 个目录，"
          f"{total_removed} 张图片，"
          f"释放空间约 {total_bytes / 1024 / 1024:.2f} MB ===")
    if not args.apply:
        print("确认无误后执行: python dedupe_covers.py --apply")


if __name__ == "__main__":
    main()