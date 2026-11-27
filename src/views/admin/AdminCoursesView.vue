<template>
  <div class="admin-courses admin-fit-page">

    <div class="page-head">
      <div>
        <h1 class="page-title">课程管理</h1>
        <p class="page-sub">共 {{ courses.length }} 门课程 · {{ onlineCount }} 门在架</p>
      </div>
      <button class="btn btn--primary" @click="openCreate">
        <Plus class="btn-icon" /> 新增课程
      </button>
    </div>

    <div class="toolbar">
      <div class="search-box">
        <span class="search-icon"></span>
        <input
          v-model.trim="keyword"
          type="text"
          class="search-input"
          placeholder="搜索课程名称 / 讲师 / 标签..."
        />
        <button
          v-if="keyword"
          type="button"
          class="search-clear"
          aria-label="清除搜索内容"
          title="清除搜索内容"
          @click="keyword = ''"
        >
          <Close />
        </button>
      </div>
      <select v-model="categoryFilter" class="select">
        <option value="">全部分类</option>
        <option v-for="label in categoryLabelOptions" :key="label" :value="label">{{ label }}</option>
      </select>
      <select v-model="publishFilter" class="select">
        <option value="">全部状态</option>
        <option value="online">在架</option>
        <option value="offline">已下架</option>
      </select>
      <span class="toolbar-result">{{ filteredCourses.length }} 条结果</span>
    </div>

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
          <tr v-for="course in pagedCourses" :key="course.id" @click="openEdit(course)">
            <td>
              <div class="cell-course">
                <img v-if="course.thumbnail" :src="course.thumbnail" alt="" class="cell-thumb" />
                <span v-else class="cell-thumb cell-thumb--empty">无</span>
                <div class="cell-course-text">
                  <div class="cell-course-title">{{ course.title }}</div>
                  <div class="cell-course-meta">
                    {{ course.instructor?.name || '—' }}
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
                @click.stop="togglePublish(course)"
              >
                <span class="switch-knob"></span>
              </button>
            </td>
            <td class="ops">
              <div class="row-actions" @click.stop>
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

    <AdminPager
      v-model:page="page"
      :total="filteredCourses.length"
      :total-pages="totalPages"
    />

    <div v-if="modalOpen" class="modal-backdrop" @click.self="closeModal">
      <div class="modal">
        <div class="modal-head">
          <h2 class="modal-title">{{ editingId ? '编辑课程' : '新增课程' }}</h2>
          <button type="button" class="modal-close" @click="closeModal"><Close /></button>
        </div>
        <form class="modal-body" @submit.prevent="saveCourse">

          <div class="section-block">
            <div class="section-header">
              <span class="section-dot"></span>
              <span class="section-title">基础信息</span>
            </div>
            <div class="section-fields">
              <label class="field">
                <span class="field-label">课程名称</span>
                <input v-model.trim="form.title" type="text" class="field-input" placeholder="请输入课程名称" />
              </label>
              <label class="field">
                <span class="field-label">课程代号（存储目录名，如 java2026）</span>
                <input v-model.trim="form.courseCode" type="text" class="field-input" placeholder="英文或数字，如 java2026" />
              </label>
              <div class="field-row">
                <label class="field">
                  <span class="field-label">所属分类</span>
                  <input
                    v-model.trim="form.categoryLabel"
                    list="course-category-options"
                    type="text"
                    class="field-input"
                    placeholder="选择或输入分类，如 Java 开发 / AI 开发"
                  />
                  <datalist id="course-category-options">
                    <option v-for="label in categoryLabelOptions" :key="label" :value="label"></option>
                  </datalist>
                </label>
                <label class="field">
                  <span class="field-label">主讲讲师</span>
                  <input v-model.trim="form.instructorName" type="text" class="field-input" placeholder="讲师姓名" />
                </label>
              </div>
              <label class="field">
                <span class="field-label">课程简介</span>
                <textarea v-model.trim="form.summary" rows="3" class="field-input field-textarea" placeholder="一句话描述课程内容，让学员快速了解学习收获"></textarea>
              </label>
            </div>
          </div>

          <div class="section-block">
            <div class="section-header">
              <span class="section-dot section-dot--blue"></span>
              <span class="section-title">课程封面</span>
            </div>
            <div class="cover-section">
              <label
                class="cover-hero"
                :class="{ 'cover-hero--empty': !form.thumbnail }"
                :title="form.thumbnail ? '点击更换封面' : '点击上传封面'"
              >
                <input type="file" accept="image/*" class="file-input-hidden" @change="handleCoverUpload" />
                <img v-if="form.thumbnail" :src="form.thumbnail" alt="课程封面" class="cover-hero-img" />
                <span v-if="form.thumbnail" class="cover-hero-overlay">{{ uploadingCover ? '上传中…' : '更换封面' }}</span>
                <span v-else class="cover-hero-empty">
                  <span class="cover-empty-icon">🖼</span>
                  <span class="cover-empty-title">{{ uploadingCover ? '封面上传中…' : '点击上传封面' }}</span>
                  <span class="cover-empty-hint">建议 16:9 尺寸 · JPG / PNG · 不超过 5MB</span>
                </span>
              </label>
            </div>
          </div>

          <div
            class="section-block syllabus-dropzone"
            :class="{ 'is-dragover': dragOver }"
            @dragenter.prevent="dragOver = true"
            @dragover.prevent="dragOver = true"
            @dragleave.prevent="onDragLeave"
            @drop.prevent="handleDrop"
          >
            <div class="section-header">
              <span class="section-dot section-dot--green"></span>
              <span class="section-title">分集列表</span>
              <span class="section-badge">{{ lessonCount }} 集</span>
            </div>

            <p v-if="loadingDetail" class="video-empty">正在读取分集列表…</p>
            <div v-else-if="flatLessons.length" class="syl-editor">
              <div v-for="item in flatLessons" :key="item.lesson.id" class="syl-episode">
                <span class="syl-lesson-no">P{{ item.no }}</span>
                <input
                  v-model.trim="item.lesson.name"
                  type="text"
                  class="field-input syl-lesson-name"
                  placeholder="分集标题，如：第1章-入门篇-01-初始Java"
                />
                <span v-if="item.lesson.duration" class="syl-episode-dur">{{ item.lesson.duration }}</span>
                <label class="upload-btn upload-btn--video upload-btn--sm">
                  <input type="file" accept="video/*" multiple class="file-input-hidden" @change="handleLessonFile(item.lesson, $event)" />
                  <span>↑</span> {{ isLessonUploading(item.lesson) ? '上传中…' : (item.lesson.url ? '替换视频' : '上传视频') }}
                </label>
                <button type="button" class="del-lesson-btn" title="删除这一集" @click="removeEpisode(item)">
                  <Delete class="del-lesson-icon" /> 删除
                </button>
              </div>
            </div>
            <p v-else class="video-empty">还没有分集，点「批量上传视频」多选文件，或直接把视频拖到这里。</p>

            <div class="syl-actions">
              <label class="btn btn--primary btn--small batch-upload-btn" :class="{ 'is-busy': batchUploading }">
                <input type="file" accept="video/*" multiple class="file-input-hidden" @change="handleBatchFiles" />
                <span>⇪</span> {{ batchUploading ? '批量上传中…' : '批量上传视频（可多选）' }}
              </label>
              <button type="button" class="btn btn--ghost btn--small" @click="addEpisode">＋ 添加分集</button>
              <span class="syl-hint">也可把多个视频直接拖进此区域；按文件名排序生成 P1 / P2 / P3…</span>
            </div>
            <div v-if="dragOver" class="syl-drop-hint">松开即可添加并上传多个视频</div>
          </div>

          <label v-if="!editingId" class="check-row">
            <input v-model="form.published" type="checkbox" class="checkbox" />
            <span>创建后立即上架</span>
          </label>
          <p v-else class="form-hint">上架与下架请用列表中的开关切换，保存不会改动课程状态。</p>
          <p v-if="formError" class="form-error">{{ formError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn btn--ghost" @click="closeModal">取消</button>
            <button type="submit" class="btn btn--primary" :disabled="saving">{{ saving ? '保存中…' : (editingId ? '保存修改' : '创建课程') }}</button>
          </div>
        </form>
      </div>
    </div>

    <ConfirmDialog
      v-if="confirmState"
      :title="confirmState.title"
      :subtitle="confirmState.subtitle"
      :rows="confirmState.rows"
      :tip="confirmState.tip"
      :tone="confirmState.tone"
      :confirm-text="confirmState.confirmText"
      @cancel="cancelConfirm"
      @confirm="acceptConfirm"
    />

  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { Plus, EditPen, Delete, Close } from '@element-plus/icons-vue';
import { adminStore, loadCourses } from '../../adminStore.js';
import {
  createAdminChapter,
  createAdminCourse,
  createAdminInstructor,
  createAdminLesson,
  deleteAdminChapter,
  deleteAdminCourse,
  deleteAdminLesson,
  fetchAdminCourse,
  createAdminCourseCategory,
  fetchAdminCourseCategories,
  fetchAdminInstructors,
  updateAdminChapter,
  updateAdminCourse,
  updateAdminCourseStatus,
  updateAdminLesson,
  uploadFile,
  presignUploadFile,
  normalizeFileUrl,
} from '../../api/index.js';
import { usePager } from '../../composables/usePager.js';
import { useConfirm } from '../../composables/useConfirm.js';
import { useToast } from '../../composables/useToast.js';
import AdminPager from '../../components/AdminPager.vue';
import ConfirmDialog from '../../components/ConfirmDialog.vue';

const { show: showToast } = useToast();

const courses = adminStore.courses;

const { confirmState, askConfirm, cancelConfirm, acceptConfirm } = useConfirm();

const keyword = ref('');
const categoryFilter = ref('');
const publishFilter = ref('');

const categories = ref([]);
const instructors = ref([]);

// 唯一分类字段：候选项 = 已有 course_category + 课程当前显示的分类
const isUsableLabel = (label) => {
  const text = (label || '').trim();
  return text && text !== '视频课程' && /[\u4e00-\u9fa5a-zA-Z]/.test(text);
};
const categoryLabelOptions = computed(() => {
  const labels = new Set();
  for (const cat of categories.value) {
    if (isUsableLabel(cat.label)) labels.add(cat.label.trim());
  }
  for (const course of courses) {
    if (isUsableLabel(course.categoryLabel)) labels.add(course.categoryLabel.trim());
  }
  return [...labels].sort((a, b) => a.localeCompare(b, 'zh-CN'));
});

onMounted(async () => {
  const [cats, insts] = await Promise.all([
    fetchAdminCourseCategories().catch(() => []),
    fetchAdminInstructors().catch(() => []),
  ]);
  categories.value = cats || [];
  instructors.value = insts || [];
});

const onlineCount = computed(() => courses.filter(c => c.published).length);

const resolveInstructorId = async (name) => {
  const found = instructors.value.find(i => i.name === name);
  if (found) return found.id;
  const created = await createAdminInstructor({ name });
  if (created?.id) instructors.value = [...instructors.value, created];
  return created?.id ?? null;
};

const filteredCourses = computed(() => {
  const kw = keyword.value.trim().toLowerCase();
  return courses.filter(c => {
    const hitKw = !kw || [c.title, c.summary, c.instructor?.name, c.categoryLabel]
      .filter(Boolean)
      .some(v => v.toLowerCase().includes(kw));
    const hitCat = !categoryFilter.value || c.categoryLabel === categoryFilter.value;
    const hitPub = !publishFilter.value ||
      (publishFilter.value === 'online' ? c.published : !c.published);
    return hitKw && hitCat && hitPub;
  });
});

const { page, totalPages, paged: pagedCourses, resetPage } = usePager(filteredCourses, 8);
watch([keyword, categoryFilter, publishFilter], resetPage);

const modalOpen = ref(false);
const editingId = ref(null);
const formError = ref('');
const saving = ref(false);
const loadingDetail = ref(false);
const uploadingCover = ref(false);

const uploadingLessons = ref(new Set());
const isLessonUploading = (lesson) => uploadingLessons.value.has(lesson);
const batchUploading = ref(false);
const form = ref({
  title: '', courseCode: '', categoryLabel: '', instructorName: '', summary: '', published: true,
  thumbnail: '', syllabus: []
});

let savedSyllabus = [];

const lessonCount = computed(() =>
  form.value.syllabus.reduce((sum, ch) => sum + (ch.lessons ? ch.lessons.length : 0), 0)
);

// 管理端按 B 站方式平铺展示分集；章节仅作为存储的隐藏容器，不暴露给操作界面
const flatLessons = computed(() => {
  const list = [];
  let no = 0;
  for (const chapter of form.value.syllabus) {
    for (const lesson of (chapter.lessons || [])) {
      no += 1;
      list.push({ lesson, chapter, no });
    }
  }
  return list;
});

const resolveCategoryId = async (label) => {
  const name = (label || '').trim();
  if (!name) return null;
  const hit = categories.value.find(c => c.label === name || c.code === name);
  if (hit) return Number(hit.id);
  const created = await createAdminCourseCategory(name);
  if (created?.id) {
    categories.value = [...categories.value, created];
    return Number(created.id);
  }
  return null;
};

const toEditorChapters = (chapters = []) => chapters.map(ch => ({
  ...ch,
  lessons: (ch.lessons || []).map(ls => ({ ...ls }))
}));

const openCreate = () => {
  editingId.value = null;
  formError.value = '';
  savedSyllabus = [];
  form.value = {
    title: '', courseCode: '', categoryLabel: '技术研发', instructorName: '', summary: '', published: true,
    thumbnail: '', syllabus: []
  };
  modalOpen.value = true;
};

const openEdit = async (course) => {
  editingId.value = course.id;
  formError.value = '';
  savedSyllabus = [];
  form.value = {
    title: course.title,
    courseCode: course.courseCode || '',
    categoryLabel: course.categoryLabel || '',
    instructorName: course.instructor?.name || '',
    summary: course.summary || '',
    published: course.published,
    thumbnail: course.thumbnail || '',
    syllabus: []
  };
  modalOpen.value = true;

  loadingDetail.value = true;
  try {
    const detail = await fetchAdminCourse(course.numericId ?? course.id);
    const chapters = toEditorChapters(detail?.syllabus);
    form.value.syllabus = chapters;
    savedSyllabus = toEditorChapters(detail?.syllabus);
  } catch (error) {
    formError.value = error?.message || '课程目录加载失败，请稍后重试。';
  } finally {
    loadingDetail.value = false;
  }
};

const closeModal = () => {
  modalOpen.value = false;
  formError.value = '';
};

const syncSyllabus = async (courseId, chapters) => {
  const keptChapterIds = new Set();
  for (let ci = 0; ci < chapters.length; ci++) {
    const chapter = chapters[ci];
    const title = (chapter.title || '').trim() || `第 ${ci + 1} 章`;
    let chapterId = chapter.numericId;
    if (chapterId) {
      await updateAdminChapter(courseId, chapterId, { title, sortNo: ci + 1 });
    } else {
      const created = await createAdminChapter(courseId, { title, sortNo: ci + 1 });
      chapterId = created?.id ?? null;
      if (!chapterId) continue;
      chapter.numericId = chapterId;
    }
    keptChapterIds.add(chapterId);

    const lessons = chapter.lessons || [];
    const keptLessonIds = new Set();
    for (let li = 0; li < lessons.length; li++) {
      const lesson = lessons[li];
      const payload = {
        title: (lesson.name || '').trim() || '未命名分集',
        durationSeconds: Number(lesson.durationSeconds || 0),
        videoUrl: lesson.url || null,
        sortNo: li + 1
      };
      if (lesson.numericId) {
        await updateAdminLesson(courseId, chapterId, lesson.numericId, payload);
        keptLessonIds.add(lesson.numericId);
      } else {
        const created = await createAdminLesson(courseId, chapterId, payload);
        if (created?.id) {
          lesson.numericId = created.id;
          keptLessonIds.add(created.id);
        }
      }
    }

    const originChapter = savedSyllabus.find(ch => ch.numericId === chapterId);
    for (const old of originChapter?.lessons || []) {
      if (old.numericId && !keptLessonIds.has(old.numericId)) {
        await deleteAdminLesson(courseId, chapterId, old.numericId);
      }
    }
  }

  for (const old of savedSyllabus) {
    if (old.numericId && !keptChapterIds.has(old.numericId)) {
      await deleteAdminChapter(courseId, old.numericId);
    }
  }
};

const saveCourse = async () => {
  if (!form.value.title) { formError.value = '请输入课程名称。'; return; }
  if (!form.value.instructorName) { formError.value = '请输入讲师姓名。'; return; }
  if (!form.value.categoryLabel) { formError.value = '请选择或输入课程分类。'; return; }

  saving.value = true;
  formError.value = '';
  try {
    const categoryId = await resolveCategoryId(form.value.categoryLabel);
    if (categoryId == null) {
      throw new Error('课程分类保存失败，请重试。');
    }
    const instructorId = await resolveInstructorId(form.value.instructorName);
    const payload = {
      title: form.value.title,
      courseCode: form.value.courseCode || null,
      categoryId,
      instructorId,
      thumbnailUrl: form.value.thumbnail || null,
      summary: form.value.summary || null
    };

    let courseId;
    if (editingId.value) {
      const saved = await updateAdminCourse(editingId.value, payload);
      courseId = saved?.id ?? editingId.value;
      showToast(`已更新课程「${payload.title}」`, 'success');
    } else {
      const saved = await createAdminCourse({ ...payload, status: form.value.published ? 2 : 1 });
      courseId = saved?.id ?? null;
      showToast(`已创建课程「${payload.title}」`, 'success');
    }

    if (courseId) await syncSyllabus(courseId, form.value.syllabus);
    await loadCourses();
    closeModal();
  } catch (error) {
    formError.value = error?.message || '保存失败，请稍后重试。';
  } finally {
    saving.value = false;
  }
};

const togglePublish = async (course) => {

  const nextCode = course.published ? 3 : 2;
  try {
    await updateAdminCourseStatus(course.numericId ?? course.id, nextCode);
    await loadCourses();
    showToast(`「${course.title}」已${nextCode === 2 ? '上架' : '下架'}`, 'success');
  } catch (error) {
    showToast(error?.message || '状态更新失败', 'error');
  }
};

// 新增分集统一追加到最后一个隐藏章节，保证全局集数（P1/P2…）始终排在末尾
const chapterForNewEpisode = () => {
  if (!form.value.syllabus.length) {
    form.value.syllabus.push({
      id: `ch-default-${Date.now()}`,
      title: '课程视频',
      lessons: []
    });
  }
  return form.value.syllabus[form.value.syllabus.length - 1];
};

let localLessonSeq = 0;
const nextLessonId = () => `ls-${Date.now()}-${++localLessonSeq}`;

const addEpisode = () => {
  const chapter = chapterForNewEpisode();
  const lessons = chapter.lessons || (chapter.lessons = []);
  lessons.push({ id: nextLessonId(), name: '', duration: '', url: '' });
};

const isMeaninglessName = (name) =>
  !/[\u4e00-\u9fa5]/.test(name) && /^[0-9a-f]{12,}$/i.test(name.replace(/[\s_-]/g, ''));

const formatDuration = (seconds) => {
  if (!Number.isFinite(seconds) || seconds <= 0) return '';
  const total = Math.round(seconds);
  if (total < 60) return `${total}秒`;
  const min = Math.floor(total / 60);
  const sec = total % 60;
  return sec ? `${min}分${sec}秒` : `${min}分`;
};

const probeLessonDuration = (lesson, file) => {
  const probeUrl = URL.createObjectURL(file);
  const probe = document.createElement('video');
  probe.preload = 'metadata';
  probe.onloadedmetadata = () => {
    const seconds = Math.round(probe.duration);
    if (Number.isFinite(seconds) && seconds > 0) {
      lesson.durationSeconds = seconds;
      if (!lesson.duration) lesson.duration = formatDuration(seconds);
    }
    probe.src = '';
    URL.revokeObjectURL(probeUrl);
  };
  probe.onerror = () => {
    probe.src = '';
    URL.revokeObjectURL(probeUrl);
  };
  probe.src = probeUrl;
};

// 上传单个分集视频；silent 用于批量上传时避免逐条弹提示
const uploadLessonVideo = async (lesson, file, { silent = false } = {}) => {
  // 按课程内所有分集的顺序计算全局集数（从 1 开始）
  let episode = 0;
  let counter = 0;
  outer: for (const ch of form.value.syllabus) {
    for (const ls of (ch.lessons || [])) {
      counter += 1;
      if (ls === lesson) { episode = counter; break outer; }
    }
  }
  uploadingLessons.value.add(lesson);
  let ok = false;
  try {
    const code = form.value.courseCode?.trim();
    // 有代号+集数走 presign，落到 video/import/<code>/P<n>；否则退回普通上传
    const result = (code && episode)
      ? await presignUploadFile(file, 'video', code, episode)
      : await uploadFile(file, 'video');
    lesson.url = normalizeFileUrl(result?.url || result?.directUrl);
    ok = true;
    if (!silent) showToast(`已上传视频：${lesson.name}`, 'success');
  } catch (error) {
    if (!silent) showToast(error?.message || '视频上传失败，请重试或换一个小文件。', 'error');
  } finally {
    uploadingLessons.value.delete(lesson);
  }
  if (ok) probeLessonDuration(lesson, file);
  return ok;
};

const MAX_DIRECT_BYTES = 2 * 1024 * 1024 * 1024; // presign 直传 MinIO，不受后端 multipart 限制
const MAX_MULTIPART_BYTES = 50 * 1024 * 1024; // 无课程代号时走普通上传，对齐后端 50MB 上限

// 统一入口：可一次传入多个视频。firstLesson 存在时第一个文件填入该行，其余追加为新分集
const addAndUploadFiles = async (picked, firstLesson = null) => {
  const files = picked.filter((file) => {
    const useDirect = Boolean(form.value.courseCode?.trim());
    const limit = useDirect ? MAX_DIRECT_BYTES : MAX_MULTIPART_BYTES;
    if (file.size > limit) {
      showToast(`「${file.name}」超过 ${useDirect ? '2GB' : '50MB'}，已跳过。`, 'error');
      return false;
    }
    return true;
  });
  if (!files.length || batchUploading.value) return;

  files.sort((a, b) => a.name.localeCompare(b.name, undefined, { numeric: true, sensitivity: 'base' }));

  const needAppend = files.length > (firstLesson ? 1 : 0);
  let targetLessons = null;
  if (needAppend) {
    const chapter = chapterForNewEpisode();
    if (!Array.isArray(chapter.lessons)) chapter.lessons = [];
    targetLessons = chapter.lessons;
  }

  const items = files.map((file, i) => {
    const stem = file.name.replace(/\.[^.]+$/, '').trim();
    const lesson = i === 0 && firstLesson ? firstLesson : {
      id: nextLessonId(), name: '', duration: '', url: ''
    };
    if (!lesson.name || lesson.name === '未命名分集' || isMeaninglessName(lesson.name)) {
      lesson.name = isMeaninglessName(stem) ? '未命名分集' : stem;
    }
    if (lesson !== firstLesson) targetLessons.push(lesson);
    return { lesson, file };
  });

  const silent = items.length > 1;
  batchUploading.value = true;
  let succeeded = 0;
  try {
    for (const { lesson, file } of items) {
      if (await uploadLessonVideo(lesson, file, { silent })) succeeded += 1;
    }
  } finally {
    batchUploading.value = false;
  }
  if (silent && succeeded) showToast(`已添加并上传 ${succeeded} 集视频`, 'success');
};

// 每一行的上传框也支持多选：第一个文件填当前行，其余自动追加
const handleLessonFile = (lesson, e) => {
  const input = e.target;
  const picked = [...(input.files || [])];
  input.value = '';
  if (!picked.length || !lesson) return;
  addAndUploadFiles(picked, lesson);
};

// 批量按钮：全部作为新分集追加，按文件名自然排序生成 P1 / P2 / P10…
const handleBatchFiles = (e) => {
  const input = e.target;
  const picked = [...(input.files || [])];
  input.value = '';
  if (!picked.length) return;
  addAndUploadFiles(picked);
};

const dragOver = ref(false);
const isVideoFile = (file) =>
  (file?.type || '').startsWith('video/') || /\.(mp4|webm|mov|m4v|avi|mkv|mpe?g)$/i.test(file?.name || '');

const onDragLeave = (e) => {
  if (!e.currentTarget.contains(e.relatedTarget)) dragOver.value = false;
};

// 直接把多个视频拖进分集列表即可批量上传
const handleDrop = (e) => {
  dragOver.value = false;
  const files = [...(e.dataTransfer?.files || [])].filter(isVideoFile);
  if (files.length) addAndUploadFiles(files);
};

const removeEpisode = (item) => {
  if (!item?.lesson || !item?.chapter) return;
  item.chapter.lessons = item.chapter.lessons.filter(ls => ls.id !== item.lesson.id);
};

const handleCoverUpload = async (e) => {
  const input = e.target;
  const file = input.files && input.files[0];
  input.value = '';
  if (!file) return;
  if (file.size > 5 * 1024 * 1024) {
    showToast('封面图片超过 5MB，请压缩后重试。', 'error');
    return;
  }
  uploadingCover.value = true;
  try {
    // 有课程代号时走 presign，封面落到 video/import/<code>/cover；否则退回普通上传
    const code = form.value.courseCode?.trim();
    const result = code
      ? await presignUploadFile(file, 'cover', code)
      : await uploadFile(file, 'cover');
    // 兼容旧后端返回的绝对地址，统一存相对路径，避免局域网访问指向本机 127.0.0.1。
    const url = normalizeFileUrl(result?.url || result?.directUrl);
    // 同名 URL 会被浏览器缓存；带上内容版本让预览和列表立即显示新封面。
    form.value.thumbnail = url && result?.sha256
      ? `${url.split('?')[0]}?v=${result.sha256.slice(0, 16)}`
      : url;
    showToast('封面已上传，保存后生效', 'success');
  } catch (error) {
    showToast(error?.message || '封面上传失败', 'error');
  } finally {
    uploadingCover.value = false;
  }
};

const removeCourse = (course) => {
  askConfirm({
    tone: 'danger',
    title: '删除该课程？',
    subtitle: course.title,
    rows: [
      { label: '课程分类', value: course.categoryLabel }
    ],
    tip: '删除后课程内容、章节与学员学习进度将一并移除，此操作不可恢复。',
    confirmText: '确认删除',
    action: async () => {
      try {
        await deleteAdminCourse(course.numericId ?? course.id);
        await loadCourses();
        showToast(`已删除课程「${course.title}」`, 'success');
      } catch (error) {
        showToast(error?.message || '删除失败', 'error');
      }
    }
  });
};
</script>

<style scoped>

* { margin: 0; padding: 0; box-sizing: border-box; }
button, input, select, textarea { font-family: inherit; }
button { cursor: pointer; }

.admin-courses {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 100%;
}

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
  color: var(--app-heading);
  letter-spacing: -0.02em;
}

.page-sub {
  margin-top: 4px;
  font-size: 13px;
  color: var(--app-text-4);
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
  background: linear-gradient(135deg, var(--app-brand), var(--app-brand-deep));
  color: var(--app-brand-on);
  border-color: var(--app-brand-deep);
  box-shadow: 0 2px 6px var(--app-brand-glow);
}
.btn--primary:hover { background: linear-gradient(135deg, var(--app-brand-hi), var(--app-brand-deep)); }

.btn--ghost {
  background: var(--app-surface);
  color: var(--app-text-2);
  border-color: var(--app-text-6);
}
.btn--ghost:hover { border-color: var(--app-brand); color: var(--app-brand-ink); }

.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
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
  background: var(--app-surface-2);
  border: 1px solid var(--app-border);
  border-radius: 10px;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.search-box:focus-within {
  border-color: var(--app-accent-strong);
  box-shadow: 0 0 0 3px var(--app-accent-a12);
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
  border: 2px solid var(--app-text-4);
  border-radius: 50%;
}
.search-icon::after {
  content: '';
  position: absolute;
  bottom: 0;
  right: 0;
  width: 6px;
  height: 2px;
  background: var(--app-text-4);
  border-radius: 1px;
  transform: rotate(45deg);
  transform-origin: right center;
}

.search-input {
  flex: 1;
  min-width: 0;
  padding: 9px 0;
  font-size: 13px;
  color: var(--app-text);
  background: transparent;
  border: none;
  outline: none;
}

.search-clear {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 24px;
  width: 24px;
  height: 24px;
  padding: 0;
  color: var(--app-text-4);
  background: transparent;
  border: 0;
  border-radius: 50%;
  cursor: pointer;
}
.search-clear:hover { color: var(--app-text-2); background: var(--app-chip); }
.search-clear svg { width: 14px; height: 14px; }

.select {
  padding: 9px 12px;
  font-size: 13px;
  color: var(--app-text-2);
  background: var(--app-surface-2);
  border: 1px solid var(--app-border);
  border-radius: 10px;
  outline: none;
  transition: border-color 0.15s;
}
.select:focus { border-color: var(--app-accent-strong); }

.toolbar-result {
  margin-left: auto;
  font-size: 12px;
  color: var(--app-text-5);
}

.table-wrap {
  background: var(--app-surface);
  border: 1px solid var(--app-border);
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
  color: var(--app-text-4);
  text-align: left;
  background: var(--app-surface-2);
  border-bottom: 1px solid var(--app-border);
  white-space: nowrap;
}

.data-table tbody td {
  padding: 12px 14px;
  color: var(--app-text);
  border-bottom: 1px solid var(--app-border-soft);
  white-space: nowrap;
}

.data-table tbody tr:last-child td { border-bottom: none; }
.data-table tbody tr { cursor: pointer; }
.data-table tbody td { transition: background 0.15s, color 0.15s, box-shadow 0.15s; }
.data-table tbody tr:hover td { background: var(--app-accent-soft); }
.data-table tbody tr:hover td:first-child { box-shadow: inset 3px 0 0 var(--app-accent); }
.data-table tbody tr:hover td,
.data-table tbody tr:hover .cell-course-title,
.data-table tbody tr:hover .cell-course-meta {
  color: var(--app-accent-deep);
}

th.num, td.num { text-align: right; }
th.ops, td.ops { text-align: right; }

.data-table thead th.num,
.data-table thead th.ops { text-align: right; }

.mono { font-family: 'JetBrains Mono', ui-monospace, monospace; }
.cell-muted { color: var(--app-text-4); }
.empty-cell { text-align: center; color: var(--app-text-5); padding: 28px 0 !important; }

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
.cell-thumb--empty {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  color: var(--app-text-5);
  background: var(--app-surface-3);
  border: 1px dashed var(--app-border-strong);
}

.cell-course-text { min-width: 0; }

.cell-course-title {
  font-weight: 600;
  color: var(--app-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cell-course-meta {
  margin-top: 2px;
  font-size: 11px;
  color: var(--app-text-5);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cat-chip {
  display: inline-flex;
  padding: 3px 10px;
  font-size: 11px;
  font-weight: 600;
  color: var(--app-accent-deep);
  background: var(--app-accent-soft);
  border-radius: 9999px;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  padding: 3px 10px;
  font-size: 11px;
  font-weight: 700;
  border-radius: 9999px;
}
.status-pill--on  { color: var(--app-emerald-fg); background: var(--app-emerald-bg); }
.status-pill--off { color: var(--app-text-4); background: var(--app-chip); }

.switch {
  position: relative;
  width: 40px;
  height: 22px;
  background: var(--app-surface-3);
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
  background: var(--app-surface);
  border-radius: 50%;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  transition: left 0.2s;
}
.switch--on .switch-knob { left: 21px; }

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
  color: var(--app-text-3);
  background: var(--app-surface-2);
  border: 1px solid var(--app-border);
  border-radius: 8px;
  transition: background 0.15s, color 0.15s, border-color 0.15s;
}
.icon-btn svg { width: 14px; height: 14px; }
.icon-btn:hover { background: var(--app-accent-soft); color: var(--app-accent-deep); border-color: var(--app-accent-border); }
.icon-btn--danger:hover { background: var(--app-rose-bg); color: var(--app-rose-fg); border-color: var(--app-rose-line); }

.modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: var(--app-scrim);
  backdrop-filter: blur(4px);
}

.modal {
  width: 100%;
  max-width: 560px;
  max-height: 92vh;
  overflow-y: auto;
  background: var(--app-surface);
  border-radius: 18px;
  box-shadow: 0 32px 64px rgba(15, 30, 51, 0.28);
  animation: modal-in 0.22s ease-out;
  scrollbar-width: thin;
  scrollbar-color: var(--app-text-6) transparent;
}
.modal::-webkit-scrollbar { width: 5px; }
.modal::-webkit-scrollbar-thumb { background: var(--app-chip); border-radius: 9999px; }
.modal::-webkit-scrollbar-track { background: transparent; }

@keyframes modal-in {
  from { opacity: 0; transform: translateY(12px) scale(0.97); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}

.modal-head {
  position: sticky;
  top: 0;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 22px 16px;
  background: var(--app-surface);
  border-bottom: 1px solid var(--app-border-soft);
  border-radius: 18px 18px 0 0;
}

.modal-title {
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.01em;
  color: var(--app-heading);
  display: flex;
  align-items: center;
  gap: 9px;
}
.modal-title::before {
  content: '';
  display: inline-block;
  width: 3px;
  height: 16px;
  background: linear-gradient(180deg, var(--app-accent), var(--app-brand));
  border-radius: 9999px;
}

.modal-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  color: var(--app-text-4);
  background: transparent;
  border: 1px solid transparent;
  border-radius: 8px;
  transition: background 0.15s, color 0.15s, border-color 0.15s;
}
.modal-close svg { width: 16px; height: 16px; }
.modal-close:hover {
  background: var(--app-surface-3);
  border-color: var(--app-border);
  color: var(--app-heading);
}

.modal-body {
  display: flex;
  flex-direction: column;
  gap: 0;
  padding: 0;
}

.section-block {
  padding: 18px 22px;
  border-bottom: 1px solid var(--app-border-soft);
}
.section-block:last-of-type { border-bottom: none; }

.syllabus-dropzone {
  position: relative;
  transition: background 0.15s, box-shadow 0.15s;
}
.syllabus-dropzone.is-dragover {
  background: var(--app-accent-a10);
  box-shadow: inset 0 0 0 2px var(--app-accent-border);
}
.syl-drop-hint {
  position: absolute;
  inset: 8px;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  color: var(--app-accent-deep);
  background: var(--app-surface-2);
  border: 2px dashed var(--app-accent);
  border-radius: 10px;
  pointer-events: none;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}

.section-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--app-brand);
  flex-shrink: 0;
}
.section-dot--blue  { background: #2E8BD6; }
.section-dot--green { background: #10B981; }

.section-title {
  font-size: 13px;
  font-weight: 700;
  color: var(--app-text);
  letter-spacing: 0.02em;
}

.section-badge {
  margin-left: auto;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 600;
  color: #10B981;
  background: var(--app-emerald-bg);
  border-radius: 9999px;
}

.section-fields {
  display: flex;
  flex-direction: column;
  gap: 12px;
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
  color: var(--app-text-3);
}

.field-input {
  width: 100%;
  padding: 10px 12px;
  font-size: 13px;
  color: var(--app-text);
  background: var(--app-surface-2);
  border: 1px solid var(--app-border);
  border-radius: 10px;
  outline: none;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.field-input:focus {
  border-color: var(--app-accent-strong);
  box-shadow: 0 0 0 3px var(--app-accent-a12);
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
  padding: 14px 22px;
  font-size: 13px;
  color: var(--app-text-2);
  cursor: pointer;
  user-select: none;
  border-bottom: 1px solid var(--app-border-soft);
}

.checkbox {
  width: 16px;
  height: 16px;
  accent-color: var(--app-brand-deep);
  cursor: pointer;
}

.form-error {
  padding: 0 22px;
  font-size: 12px;
  color: var(--app-rose-fg);
}

.form-hint {
  padding: 0 22px;
  font-size: 12px;
  color: var(--app-text-4);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 22px 20px;
}

.cover-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cover-hero {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  background: var(--app-surface-2);
  border: 1px solid var(--app-border);
  border-radius: 14px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: border-color 0.18s, background 0.18s, box-shadow 0.18s;
}
.cover-hero:hover {
  border-color: var(--app-accent-border);
  background: var(--app-surface-3);
  box-shadow: 0 2px 10px rgb(15 30 51 / 5%);
}

.cover-hero--empty {
  border: 1.5px dashed var(--app-border-strong);
}
.cover-hero--empty:hover {
  border-color: var(--app-accent);
  background: var(--app-surface-2);
}

.cover-hero-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.cover-hero-overlay {
  position: absolute;
  inset: 0;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.02em;
  color: #fff;
  background: rgb(15 30 51 / 28%);
  opacity: 0;
  transition: opacity 0.18s;
  pointer-events: none;
}
.cover-hero:hover .cover-hero-overlay,
.cover-hero:focus-within .cover-hero-overlay {
  opacity: 1;
}

.cover-hero-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 12px;
  text-align: center;
  color: var(--app-text-4);
  transition: color 0.18s;
}
.cover-hero:hover .cover-hero-empty { color: var(--app-accent-deep); }

.cover-empty-icon { font-size: 28px; opacity: 0.55; }
.cover-empty-title { font-size: 13px; font-weight: 600; }
.cover-empty-hint { font-size: 11px; color: var(--app-text-5); }

.upload-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 9px 14px;
  font-size: 12px;
  font-weight: 600;
  color: var(--app-accent-deep);
  background: var(--app-accent-soft);
  border: 1px solid var(--app-accent-border);
  border-radius: 10px;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.15s, box-shadow 0.15s;
}
.upload-btn:hover {
  background: var(--app-accent-hover);
  box-shadow: 0 2px 6px var(--app-accent-a20);
}

.upload-btn--video {
  padding: 6px 11px;
  font-size: 11px;
  border-radius: 8px;
}

.file-input-hidden {
  display: none;
}

.syl-editor {
  display: flex;
  flex-direction: column;
  margin-bottom: 12px;
  border: 1px solid var(--app-border);
  border-radius: 12px;
  overflow: hidden;
  background: var(--app-surface);
}

.syl-episode {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 12px;
  background: var(--app-surface);
  transition: background 0.12s;
}
.syl-episode:hover { background: var(--app-surface-2); }
.syl-episode + .syl-episode {
  border-top: 1px solid var(--app-border-soft);
}

.syl-lesson-no {
  min-width: 36px;
  padding: 3px 6px;
  font-size: 11px;
  font-weight: 700;
  color: var(--app-accent-deep);
  background: var(--app-surface-2);
  border: 1px solid var(--app-border);
  border-radius: 6px;
  text-align: center;
  font-variant-numeric: tabular-nums;
  flex-shrink: 0;
}

.syl-lesson-name {
  flex: 1;
  min-width: 0;
  padding: 7px 10px;
  font-size: 12px;
}

.syl-episode-dur {
  font-size: 11px;
  color: var(--app-text-5);
  white-space: nowrap;
  flex-shrink: 0;
}

.upload-btn--sm {
  padding: 5px 9px;
  font-size: 11px;
  border-radius: 7px;
  flex-shrink: 0;
}

.del-lesson-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 9px;
  font-size: 11px;
  font-weight: 600;
  color: var(--app-text-4);
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 7px;
  flex-shrink: 0;
  cursor: pointer;
  transition: color 0.15s, background 0.15s, border-color 0.15s;
}
.del-lesson-btn:hover {
  color: var(--app-rose-fg);
  background: var(--app-rose-bg);
  border-color: var(--app-rose-line);
}
.del-lesson-icon { width: 12px; height: 12px; }

.syl-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.batch-upload-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
}
.batch-upload-btn.is-busy {
  opacity: 0.6;
  pointer-events: none;
}

.syl-hint {
  font-size: 11px;
  color: var(--app-text-5);
}

.video-empty {
  padding: 16px;
  text-align: center;
  font-size: 12px;
  color: var(--app-text-5);
  background: var(--app-surface-2);
  border: 1px dashed var(--app-border);
  border-radius: 10px;
  margin-bottom: 10px;
}

.btn--small {
  padding: 7px 13px;
  font-size: 12px;
}

@media (max-width: 640px) {
  .field-row { grid-template-columns: 1fr; }
  .syl-episode { flex-wrap: wrap; }
  .syl-lesson-name { flex-basis: 100%; }
}
</style>
