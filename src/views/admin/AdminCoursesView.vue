<template>
  <div class="admin-courses">

    <!-- 页头 -->
    <div class="page-head">
      <div>
        <h1 class="page-title">课程管理</h1>
        <p class="page-sub">共 {{ courses.length }} 门课程 · {{ onlineCount }} 门在架</p>
      </div>
      <button class="btn btn--primary" @click="openCreate">
        <Plus class="btn-icon" /> 新增课程
      </button>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="search-box">
        <span class="search-icon"></span>
        <input
          v-model.trim="keyword"
          type="text"
          class="search-input"
          placeholder="搜索课程名称 / 讲师 / 标签..."
        />
      </div>
      <select v-model="categoryFilter" class="select">
        <option value="">全部分类</option>
        <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.label }}</option>
      </select>
      <select v-model="publishFilter" class="select">
        <option value="">全部状态</option>
        <option value="online">在架</option>
        <option value="offline">已下架</option>
      </select>
      <span class="toolbar-result">{{ filteredCourses.length }} 条结果</span>
    </div>

    <!-- 课程表格 -->
    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>课程</th>
            <th>分类</th>
            <th>状态</th>
            <th>上架开关</th>
            <th class="ops">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="course in filteredCourses" :key="course.id">
            <td>
              <div class="cell-course">
                <img :src="course.thumbnail" alt="" class="cell-thumb" />
                <div class="cell-course-text">
                  <div class="cell-course-title">{{ course.title }}</div>
                  <div class="cell-course-meta">
                    {{ course.instructor?.name || '—' }} · {{ course.tags?.join(' / ') }}
                  </div>
                </div>
              </div>
            </td>
            <td>
              <span class="cat-chip">{{ course.categoryLabel }}</span>
            </td>
            <td>
              <span class="status-pill" :class="course.published ? 'status-pill--on' : 'status-pill--off'">
                {{ course.published ? '在架' : '已下架' }}
              </span>
            </td>
            <td>
              <button
                class="switch"
                :class="course.published ? 'switch--on' : ''"
                role="switch"
                :aria-checked="course.published"
                @click="togglePublish(course)"
              >
                <span class="switch-knob"></span>
              </button>
            </td>
            <td class="ops">
              <div class="row-actions">
                <button class="icon-btn" title="编辑" @click="openEdit(course)">
                  <EditPen />
                </button>
                <button class="icon-btn icon-btn--danger" title="删除" @click="removeCourse(course)">
                  <Delete />
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="!filteredCourses.length">
            <td colspan="5" class="empty-cell">没有匹配的课程</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 新增 / 编辑弹窗 -->
    <div v-if="modalOpen" class="modal-backdrop" @click.self="closeModal">
      <div class="modal">
        <div class="modal-head">
          <h2 class="modal-title">{{ editingId ? '编辑课程' : '新增课程' }}</h2>
          <button class="modal-close" @click="closeModal"><Close /></button>
        </div>
        <form class="modal-body" @submit.prevent="saveCourse">
          <label class="field">
            <span class="field-label">课程名称</span>
            <input v-model.trim="form.title" type="text" class="field-input" placeholder="请输入课程名称" />
          </label>
          <div class="field-row">
            <label class="field">
              <span class="field-label">分类</span>
              <select v-model="form.category" class="field-input" @change="syncCategoryLabel">
                <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.label }}</option>
              </select>
            </label>
            <label class="field">
              <span class="field-label">讲师</span>
              <input v-model.trim="form.instructorName" type="text" class="field-input" placeholder="讲师姓名" />
            </label>
          </div>
          <label class="field">
            <span class="field-label">课程简介</span>
            <textarea v-model.trim="form.summary" rows="3" class="field-input field-textarea" placeholder="一句话描述课程内容"></textarea>
          </label>

          <!-- 课程封面 -->
          <div class="field">
            <span class="field-label">课程封面</span>
            <div class="cover-section">
              <div class="cover-preview">
                <img v-if="form.thumbnail" :src="form.thumbnail" alt="" class="cover-preview-img" />
                <span v-else class="cover-preview-empty">暂无封面</span>
              </div>
              <div class="cover-controls">
                <div class="cover-grid">
                  <button
                    v-for="(cover, i) in presetCovers"
                    :key="i"
                    type="button"
                    class="cover-option"
                    :class="form.thumbnail === cover ? 'cover-option--active' : ''"
                    :title="`预设封面 ${i + 1}`"
                    @click="form.thumbnail = cover"
                  >
                    <img :src="cover" alt="" class="cover-option-img" />
                  </button>
                </div>
                <div class="cover-url-row">
                  <input
                    v-model.trim="form.thumbnail"
                    type="text"
                    class="field-input"
                    placeholder="或粘贴封面图片 URL"
                  />
                  <label class="upload-btn">
                    <input type="file" accept="image/*" class="file-input-hidden" @change="handleCoverUpload" />
                    上传图片
                  </label>
                </div>
              </div>
            </div>
          </div>

          <!-- 课程目录(章节 → 课时视频) -->
          <div class="field">
            <span class="field-label">课程目录({{ form.syllabus.length }} 章 · {{ lessonCount }} 节)</span>

            <!-- 视频预览 -->
            <div v-if="previewLesson" class="syl-preview">
              <video :src="previewLesson.url" controls preload="metadata" class="video-player"></video>
              <div class="syl-preview-info">
                <div class="video-name">{{ previewLesson.name }}</div>
                <button type="button" class="text-btn text-btn--danger" @click="previewLesson = null">关闭预览</button>
              </div>
            </div>

            <div v-if="form.syllabus.length" class="syl-editor">
              <div v-for="(chapter, ci) in form.syllabus" :key="chapter.id" class="syl-chapter">
                <div class="syl-chapter-head">
                  <span class="syl-chapter-index">第 {{ ci + 1 }} 章</span>
                  <input
                    v-model.trim="chapter.title"
                    type="text"
                    class="field-input syl-chapter-title"
                    placeholder="章节名称,如 微服务架构选型与升级"
                  />
                  <label class="upload-btn upload-btn--small">
                    <input type="file" accept="video/*" class="file-input-hidden" @change="handleLessonUpload(chapter)" />
                    上传视频到本章
                  </label>
                  <button type="button" class="text-btn text-btn--danger" @click="removeChapter(chapter)">删除章节</button>
                </div>

                <div v-if="chapter.lessons.length" class="syl-lessons">
                  <div v-for="(lesson, li) in chapter.lessons" :key="lesson.id" class="syl-lesson">
                    <span class="syl-lesson-no">{{ ci + 1 }}.{{ li + 1 }}</span>
                    <input v-model.trim="lesson.name" type="text" class="field-input syl-lesson-name" placeholder="课时名称" />
                    <input v-model.trim="lesson.duration" type="text" class="field-input syl-lesson-dur" placeholder="时长,如 22分" />
                    <button
                      v-if="lesson.url"
                      type="button"
                      class="icon-btn"
                      title="预览视频"
                      @click="previewLesson = lesson"
                    >▶</button>
                    <span v-else class="syl-no-video">未上传</span>
                    <button type="button" class="icon-btn icon-btn--danger" title="删除课时" @click="removeLesson(chapter, lesson)">×</button>
                  </div>
                </div>
                <p v-else class="syl-lessons-empty">本章暂无课时,点「上传视频到本章」添加。</p>
              </div>
            </div>
            <p v-else class="video-empty">暂无章节,点击下方「添加章节」创建课程目录。</p>

            <div class="syl-actions">
              <button type="button" class="btn btn--ghost btn--small" @click="addChapter">添加章节</button>
              <span class="video-upload-tip">课时 = 一个视频,上传后自动以文件名命名,支持 mp4,≤30MB</span>
            </div>
          </div>

          <label class="check-row">
            <input v-model="form.published" type="checkbox" class="checkbox" />
            <span>创建后立即上架</span>
          </label>
          <p v-if="formError" class="form-error">{{ formError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn btn--ghost" @click="closeModal">取消</button>
            <button type="submit" class="btn btn--primary">{{ editingId ? '保存修改' : '创建课程' }}</button>
          </div>
        </form>
      </div>
    </div>

  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import { Plus, EditPen, Delete, Close } from '@element-plus/icons-vue';
import { COURSE_CATEGORIES } from '../../mockData.js';
import { adminStore, PRESET_COVERS } from '../../adminStore.js';

const courses = adminStore.courses;
const presetCovers = PRESET_COVERS;

const keyword = ref('');
const categoryFilter = ref('');
const publishFilter = ref('');

const categories = COURSE_CATEGORIES;

const onlineCount = computed(() => courses.filter(c => c.published).length);

const filteredCourses = computed(() => {
  const kw = keyword.value.trim().toLowerCase();
  return courses.filter(c => {
    const hitKw = !kw || [c.title, c.summary, c.instructor?.name, c.categoryLabel, ...(c.tags || [])]
      .filter(Boolean)
      .some(v => v.toLowerCase().includes(kw));
    const hitCat = !categoryFilter.value || c.category === categoryFilter.value;
    const hitPub = !publishFilter.value ||
      (publishFilter.value === 'online' ? c.published : !c.published);
    return hitKw && hitCat && hitPub;
  });
});

/* ---------- 弹窗表单 ---------- */
const modalOpen = ref(false);
const editingId = ref(null);
const formError = ref('');
const form = ref({
  title: '', category: 'tech', categoryLabel: '', instructorName: '', summary: '', published: true,
  thumbnail: PRESET_COVERS[0], syllabus: []
});

const previewLesson = ref(null);

const lessonCount = computed(() =>
  form.value.syllabus.reduce((sum, ch) => sum + (ch.lessons ? ch.lessons.length : 0), 0)
);

const syncCategoryLabel = () => {
  form.value.categoryLabel = categories.find(c => c.id === form.value.category)?.label || '';
};

const openCreate = () => {
  editingId.value = null;
  formError.value = '';
  form.value = {
    title: '', category: 'tech', categoryLabel: '技术研发', instructorName: '', summary: '', published: true,
    thumbnail: PRESET_COVERS[0], syllabus: []
  };
  modalOpen.value = true;
};

const openEdit = (course) => {
  editingId.value = course.id;
  formError.value = '';
  previewLesson.value = null;
  form.value = {
    title: course.title,
    category: course.category,
    categoryLabel: course.categoryLabel,
    instructorName: course.instructor?.name || '',
    summary: course.summary || '',
    published: course.published,
    thumbnail: course.thumbnail || PRESET_COVERS[0],
    syllabus: (course.syllabus || []).map(ch => ({
      ...ch,
      lessons: (ch.lessons || []).map(ls => ({ ...ls }))
    }))
  };
  modalOpen.value = true;
};

const closeModal = () => {
  modalOpen.value = false;
  formError.value = '';
};

const saveCourse = () => {
  if (!form.value.title) { formError.value = '请输入课程名称。'; return; }
  if (!form.value.instructorName) { formError.value = '请输入讲师姓名。'; return; }

  const payload = {
    title: form.value.title,
    category: form.value.category,
    categoryLabel: form.value.categoryLabel,
    instructor: { name: form.value.instructorName, title: '企业认证讲师' },
    summary: form.value.summary,
    published: form.value.published,
    thumbnail: form.value.thumbnail || PRESET_COVERS[0],
    syllabus: form.value.syllabus
  };

  if (editingId.value) {
    const target = courses.find(c => c.id === editingId.value);
    Object.assign(target, payload);
  } else {
    courses.unshift({
      id: `c-${Date.now()}`,
      ...payload,
      students: 0,
      progress: 0,
      totalLessons: 12,
      completedLessons: 0,
      tags: [payload.categoryLabel],
      status: payload.published ? 'not_started' : 'offline',
      badge: payload.published ? '新课程' : '已下架',
      badgeType: 'primary'
    });
  }
  closeModal();
};

const togglePublish = (course) => {
  course.published = !course.published;
};

/* ---------- 课程目录(章节 → 课时) ---------- */
const addChapter = () => {
  form.value.syllabus.push({
    id: `ch-${Date.now()}`,
    title: `第 ${form.value.syllabus.length + 1} 章: 新章节`,
    lessons: []
  });
};

const removeChapter = (chapter) => {
  if (!window.confirm(`确认删除章节「${chapter.title}」及其所有课时?`)) return;
  form.value.syllabus = form.value.syllabus.filter(ch => ch.id !== chapter.id);
};

const handleLessonUpload = (chapter) => (e) => {
  const file = e.target.files && e.target.files[0];
  if (!file) return;
  if (file.size > 30 * 1024 * 1024) {
    window.alert('视频超过 30MB,本演示环境仅支持小文件上传。');
    return;
  }
  const reader = new FileReader();
  reader.onload = () => {
    const lessons = chapter.lessons || (chapter.lessons = []);
    lessons.push({
      id: `ls-${Date.now()}`,
      name: file.name.replace(/\.[^.]+$/, ''),
      duration: '',
      url: String(reader.result || '')
    });
  };
  reader.readAsDataURL(file);
  e.target.value = '';
};

const removeLesson = (chapter, lesson) => {
  chapter.lessons = chapter.lessons.filter(ls => ls.id !== lesson.id);
};

/* ---------- 封面 ---------- */
const handleCoverUpload = (e) => {
  const file = e.target.files && e.target.files[0];
  if (!file) return;
  if (file.size > 5 * 1024 * 1024) {
    window.alert('封面图片超过 5MB,请压缩后重试。');
    return;
  }
  const reader = new FileReader();
  reader.onload = () => {
    form.value.thumbnail = String(reader.result || '');
  };
  reader.readAsDataURL(file);
};

const removeCourse = (course) => {
  if (!window.confirm(`确认删除课程「${course.title}」?此操作不可恢复。`)) return;
  const idx = courses.findIndex(c => c.id === course.id);
  if (idx > -1) courses.splice(idx, 1);
};
</script>

<style scoped>
/* ============================================================
   AdminCourses · 课程管理 · 纯原生 CSS
   ============================================================ */

* { margin: 0; padding: 0; box-sizing: border-box; }
button, input, select, textarea { font-family: inherit; }
button { cursor: pointer; }

.admin-courses {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ========== 页头 ========== */
.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #0F1E33;
  letter-spacing: -0.02em;
}

.page-sub {
  margin-top: 4px;
  font-size: 13px;
  color: #64748B;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 16px;
  font-size: 13px;
  font-weight: 600;
  border-radius: 10px;
  border: 1px solid transparent;
  transition: background 0.15s, border-color 0.15s, transform 0.15s;
}
.btn:active { transform: translateY(1px); }
.btn-icon { width: 15px; height: 15px; }

.btn--primary {
  background: linear-gradient(135deg, #E9B949, #D19A2B);
  color: #241A04;
  border-color: #D19A2B;
  box-shadow: 0 2px 6px rgba(233, 185, 73, 0.35);
}
.btn--primary:hover { background: linear-gradient(135deg, #F0C45E, #D19A2B); }

.btn--ghost {
  background: #fff;
  color: #334155;
  border-color: #E2E8F0;
}
.btn--ghost:hover { border-color: #E9B949; color: #8A6414; }

/* ========== 工具栏 ========== */
.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  flex-wrap: wrap;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 200px;
  padding: 0 12px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.search-box:focus-within {
  border-color: #2E8BD6;
  box-shadow: 0 0 0 3px rgba(46, 139, 214, 0.12);
}

.search-icon {
  position: relative;
  width: 14px;
  height: 14px;
  flex-shrink: 0;
}
.search-icon::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 9px;
  height: 9px;
  border: 2px solid #64748B;
  border-radius: 50%;
}
.search-icon::after {
  content: '';
  position: absolute;
  bottom: 0;
  right: 0;
  width: 6px;
  height: 2px;
  background: #64748B;
  border-radius: 1px;
  transform: rotate(45deg);
  transform-origin: right center;
}

.search-input {
  flex: 1;
  min-width: 0;
  padding: 9px 0;
  font-size: 13px;
  color: #1E293B;
  background: transparent;
  border: none;
  outline: none;
}

.select {
  padding: 9px 12px;
  font-size: 13px;
  color: #334155;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  outline: none;
  transition: border-color 0.15s;
}
.select:focus { border-color: #2E8BD6; }

.toolbar-result {
  margin-left: auto;
  font-size: 12px;
  color: #94A3B8;
}

/* ========== 表格 ========== */
.table-wrap {
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.data-table thead th {
  padding: 12px 14px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.06em;
  color: #64748B;
  text-align: left;
  background: #F8FAFC;
  border-bottom: 1px solid #E2E8F0;
  white-space: nowrap;
}

.data-table tbody td {
  padding: 12px 14px;
  color: #1E293B;
  border-bottom: 1px solid #F1F5F9;
  white-space: nowrap;
}

.data-table tbody tr:last-child td { border-bottom: none; }
.data-table tbody tr:hover { background: #F8FAFC; }

th.num, td.num { text-align: right; }
th.ops, td.ops { text-align: right; }

.mono { font-family: 'JetBrains Mono', ui-monospace, monospace; }
.cell-muted { color: #64748B; }
.empty-cell { text-align: center; color: #94A3B8; padding: 28px 0 !important; }

.cell-course {
  display: flex;
  align-items: center;
  gap: 10px;
  max-width: 340px;
}

.cell-thumb {
  width: 56px;
  height: 38px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
}

.cell-course-text { min-width: 0; }

.cell-course-title {
  font-weight: 600;
  color: #1E293B;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cell-course-meta {
  margin-top: 2px;
  font-size: 11px;
  color: #94A3B8;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ========== 分类与必修徽标 ========== */
.cat-chip {
  display: inline-flex;
  padding: 3px 10px;
  font-size: 11px;
  font-weight: 600;
  color: #005576;
  background: #D9F0FF;
  border-radius: 9999px;
}

/* ========== 状态徽标 ========== */
.status-pill {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  font-size: 11px;
  font-weight: 700;
  border-radius: 9999px;
}
.status-pill--on  { color: #047857; background: #D1FAE5; }
.status-pill--off { color: #64748B; background: #E2E8F0; }

/* ========== 上架开关 ========== */
.switch {
  position: relative;
  width: 40px;
  height: 22px;
  background: #CBD5E1;
  border: none;
  border-radius: 9999px;
  transition: background 0.2s;
}
.switch--on { background: #2FBF8F; }

.switch-knob {
  position: absolute;
  top: 3px;
  left: 3px;
  width: 16px;
  height: 16px;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  transition: left 0.2s;
}
.switch--on .switch-knob { left: 21px; }

/* ========== 行操作 ========== */
.row-actions {
  display: inline-flex;
  gap: 6px;
}

.icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  color: #475569;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  transition: background 0.15s, color 0.15s, border-color 0.15s;
}
.icon-btn svg { width: 14px; height: 14px; }
.icon-btn:hover { background: #D9F0FF; color: #005576; border-color: #A3D5FF; }
.icon-btn--danger:hover { background: #FFE4E6; color: #BE123C; border-color: #FECDD3; }

/* ========== 弹窗 ========== */
.modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(15, 30, 51, 0.45);
  backdrop-filter: blur(2px);
}

.modal {
  width: 100%;
  max-width: 520px;
  max-height: 90vh;
  overflow-y: auto;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 24px 48px rgba(15, 30, 51, 0.25);
  animation: modal-in 0.22s ease-out;
}

@keyframes modal-in {
  from { opacity: 0; transform: translateY(10px) scale(0.98); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}

.modal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #F1F5F9;
}

.modal-title {
  font-size: 16px;
  font-weight: 700;
  color: #0F1E33;
}

.modal-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  color: #64748B;
  background: transparent;
  border: none;
  border-radius: 8px;
  transition: background 0.15s, color 0.15s;
}
.modal-close svg { width: 16px; height: 16px; }
.modal-close:hover { background: #F1F5F9; color: #0F1E33; }

.modal-body {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 20px;
}

.field-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field-label {
  font-size: 12px;
  font-weight: 600;
  color: #475569;
}

.field-input {
  width: 100%;
  padding: 10px 12px;
  font-size: 13px;
  color: #1E293B;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  outline: none;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.field-input:focus {
  border-color: #2E8BD6;
  box-shadow: 0 0 0 3px rgba(46, 139, 214, 0.12);
}

.field-textarea {
  resize: vertical;
  min-height: 64px;
  line-height: 1.6;
}

.check-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #334155;
  cursor: pointer;
  user-select: none;
}

.checkbox {
  width: 16px;
  height: 16px;
  accent-color: #D19A2B;
  cursor: pointer;
}

.form-error {
  font-size: 12px;
  color: #BE123C;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 4px;
}

/* ========== 封面管理 ========== */
.cover-section {
  display: flex;
  gap: 14px;
  align-items: flex-start;
}

.cover-preview {
  width: 150px;
  height: 92px;
  background: #F1F5F9;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  overflow: hidden;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-preview-empty {
  font-size: 12px;
  color: #94A3B8;
}

.cover-controls {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.cover-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 8px;
}

.cover-option {
  position: relative;
  padding: 0;
  aspect-ratio: 16 / 10;
  border: 2px solid #E2E8F0;
  border-radius: 8px;
  overflow: hidden;
  background: #F1F5F9;
  transition: border-color 0.15s, box-shadow 0.15s;
}

.cover-option:hover {
  border-color: #A3D5FF;
}

.cover-option--active {
  border-color: #2E8BD6;
  box-shadow: 0 0 0 3px rgba(46, 139, 214, 0.2);
}

.cover-option-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.cover-url-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.upload-btn {
  display: inline-flex;
  align-items: center;
  padding: 9px 14px;
  font-size: 12px;
  font-weight: 600;
  color: #005576;
  background: #D9F0FF;
  border: 1px solid #A3D5FF;
  border-radius: 10px;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.15s;
}

.upload-btn:hover {
  background: #C2E4FA;
}

.file-input-hidden {
  display: none;
}

/* ========== 课程目录(章节 → 课时) ========== */
.syl-editor {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.syl-chapter {
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  overflow: hidden;
}

.syl-chapter-head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: #F6FBFF;
  border-bottom: 1px solid #E9F2FA;
  flex-wrap: wrap;
}

.syl-chapter-index {
  padding: 3px 9px;
  font-size: 11px;
  font-weight: 700;
  color: #005576;
  background: #D9F0FF;
  border-radius: 9999px;
  white-space: nowrap;
  flex-shrink: 0;
}

.syl-chapter-title {
  flex: 1;
  min-width: 180px;
  font-weight: 600;
}

/* ========== 课时 ========== */
.syl-lessons {
  display: flex;
  flex-direction: column;
}

.syl-lesson {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #fff;
}
.syl-lesson + .syl-lesson {
  border-top: 1px solid #F1F5F9;
}

.syl-lesson-no {
  width: 34px;
  font-size: 11px;
  font-weight: 700;
  color: #64748B;
  font-variant-numeric: tabular-nums;
  flex-shrink: 0;
}

.syl-lesson-name {
  flex: 1;
  min-width: 0;
  padding: 7px 10px;
  font-size: 12px;
}

.syl-lesson-dur {
  width: 96px;
  padding: 7px 10px;
  font-size: 12px;
  flex-shrink: 0;
}

.syl-no-video {
  width: 44px;
  font-size: 11px;
  color: #94A3B8;
  text-align: center;
  flex-shrink: 0;
}

.syl-lessons-empty {
  padding: 12px;
  font-size: 12px;
  color: #94A3B8;
}

.syl-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

/* ========== 视频预览 ========== */
.syl-preview {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 10px;
  background: #0F1E33;
  border-radius: 12px;
}

.syl-preview .video-player {
  width: 200px;
  height: 112px;
}

.syl-preview-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}

.syl-preview-info .video-name {
  color: #E7EEF7;
}

.video-player {
  width: 180px;
  height: 100px;
  object-fit: cover;
  background: #0F1E33;
  border-radius: 8px;
  flex-shrink: 0;
}

.video-name {
  font-size: 13px;
  font-weight: 600;
  color: #1E293B;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.video-empty {
  padding: 14px;
  text-align: center;
  font-size: 12px;
  color: #94A3B8;
  background: #F8FAFC;
  border: 1px dashed #E2E8F0;
  border-radius: 10px;
}

.video-upload-tip {
  font-size: 11px;
  color: #94A3B8;
  line-height: 1.5;
}

.upload-btn--small {
  padding: 6px 12px;
  font-size: 11px;
}

.btn--small {
  padding: 7px 13px;
  font-size: 12px;
}

.text-btn {
  padding: 5px 12px;
  font-size: 12px;
  font-weight: 700;
  border: 1px solid transparent;
  border-radius: 9999px;
  transition: background 0.15s;
}

.text-btn--danger {
  color: #BE123C;
  background: #FFE4E6;
  flex-shrink: 0;
}
.text-btn--danger:hover { background: #FECDD3; }
@media (max-width: 640px) {
  .field-row { grid-template-columns: 1fr; }
  .cover-section { flex-direction: column; }
  .cover-grid { grid-template-columns: repeat(3, 1fr); }
  .syl-chapter-head { flex-direction: column; align-items: stretch; }
  .syl-preview { flex-direction: column; align-items: stretch; }
  .syl-preview .video-player { width: 100%; }
  .syl-lesson { flex-wrap: wrap; }
  .syl-lesson-dur { width: 100%; }
}
</style>
