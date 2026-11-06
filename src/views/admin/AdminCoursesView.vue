<template>
  <div class="admin-courses admin-fit-page">

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
          <tr v-for="course in pagedCourses" :key="course.id" @click="openEdit(course)">
            <td>
              <div class="cell-course">
                <img v-if="course.thumbnail" :src="course.thumbnail" alt="" class="cell-thumb" />
                <span v-else class="cell-thumb cell-thumb--empty">无</span>
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

    <!-- 新增 / 编辑弹窗 -->
    <div v-if="modalOpen" class="modal-backdrop" @click.self="closeModal">
      <div class="modal">
        <div class="modal-head">
          <h2 class="modal-title">{{ editingId ? '编辑课程' : '新增课程' }}</h2>
          <button type="button" class="modal-close" @click="closeModal"><Close /></button>
        </div>
        <form class="modal-body" @submit.prevent="saveCourse">

          <!-- ── 基础信息 ── -->
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
              <div class="field-row">
                <label class="field">
                  <span class="field-label">所属分类</span>
                  <select v-model="form.category" class="field-input" @change="syncCategoryLabel">
                    <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.label }}</option>
                  </select>
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

          <!-- ── 课程封面 ── -->
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

          <!-- ── 课程目录 ── -->
          <div class="section-block">
            <div class="section-header">
              <span class="section-dot section-dot--green"></span>
              <span class="section-title">课程目录</span>
              <span class="section-badge">{{ form.syllabus.length }} 章 · {{ lessonCount }} 节</span>
            </div>

            <p v-if="loadingDetail" class="video-empty">正在读取课程目录…</p>
            <div v-else-if="form.syllabus.length" class="syl-editor">
              <div v-for="(chapter, ci) in form.syllabus" :key="chapter.id" class="syl-chapter">
                <div class="syl-chapter-head">
                  <span class="syl-chapter-index">第 {{ ci + 1 }} 章</span>
                  <input
                    v-model.trim="chapter.title"
                    type="text"
                    class="field-input syl-chapter-title"
                    placeholder="章节名称，如：微服务架构选型与升级"
                  />
                  <div class="chapter-head-actions">
                    <button type="button" class="del-chapter-btn" title="删除此章节" @click="removeChapter(chapter)">
                      <span class="del-icon">✕</span> 删除章节
                    </button>
                  </div>
                </div>

                <div v-if="chapter.lessons.length" class="syl-lessons">
                  <div v-for="(lesson, li) in chapter.lessons" :key="lesson.id" class="syl-lesson">
                    <span class="syl-lesson-no">{{ ci + 1 }}.{{ li + 1 }}</span>
                    <input v-model.trim="lesson.name" type="text" class="field-input syl-lesson-name" placeholder="小节名称" />
                    <label class="upload-btn upload-btn--video upload-btn--sm">
                      <input type="file" accept="video/*" class="file-input-hidden" @change="handleLessonFile(lesson, $event)" />
                      <span>↑</span> {{ uploadingLesson ? '上传中…' : '上传视频' }}
                    </label>
                    <button type="button" class="del-lesson-btn" title="删除小节" @click="removeLesson(chapter, lesson)">
                      <Delete class="del-lesson-icon" /> 删除
                    </button>
                  </div>
                </div>
                <p v-else class="syl-lessons-empty">本章暂无小节，点「＋ 添加小节」先填写小节名称，再在小节行上传视频。</p>

                <button type="button" class="add-lesson-btn" @click="addLesson(chapter)">＋ 添加小节</button>
              </div>
            </div>
            <p v-else class="video-empty">暂无章节，点击下方「添加章节」创建课程目录。</p>

            <div class="syl-actions">
              <button type="button" class="btn btn--ghost btn--small" @click="addChapter">＋ 添加章节</button>
            </div>
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
  fetchAdminCourseCategories,
  fetchAdminInstructors,
  updateAdminChapter,
  updateAdminCourse,
  updateAdminCourseStatus,
  updateAdminLesson,
  uploadFile,
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

/* 分类与讲师取自接口；/admin/course-categories 的 id 是 code，写入接口要的是数字主键 */
const categories = ref([]);
const instructors = ref([]);

onMounted(async () => {
  const [cats, insts] = await Promise.all([
    fetchAdminCourseCategories().catch(() => []),
    fetchAdminInstructors().catch(() => []),
  ]);
  categories.value = cats || [];
  instructors.value = insts || [];
});

const onlineCount = computed(() => courses.filter(c => c.published).length);

/* 分类 code → course_category.id：从已有课程的 categoryId/categoryCode 对照中取 */
const categoryIdOf = (code) => {
  const hit = courses.find(c => (c.categoryCode || c.category) === code && c.categoryId != null);
  return hit ? hit.categoryId : null;
};

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
    const hitKw = !kw || [c.title, c.summary, c.instructor?.name, c.categoryLabel, ...(c.tags || [])]
      .filter(Boolean)
      .some(v => v.toLowerCase().includes(kw));
    const hitCat = !categoryFilter.value || c.category === categoryFilter.value;
    const hitPub = !publishFilter.value ||
      (publishFilter.value === 'online' ? c.published : !c.published);
    return hitKw && hitCat && hitPub;
  });
});

const { page, totalPages, paged: pagedCourses, resetPage } = usePager(filteredCourses, 8);
watch([keyword, categoryFilter, publishFilter], resetPage);

/* ---------- 弹窗表单 ---------- */
const modalOpen = ref(false);
const editingId = ref(null);
const formError = ref('');
const saving = ref(false);
const loadingDetail = ref(false);
const uploadingCover = ref(false);
const uploadingLesson = ref(false);
const form = ref({
  title: '', category: 'tech', categoryLabel: '', instructorName: '', summary: '', published: true,
  thumbnail: '', syllabus: []
});

/* 弹窗打开时的目录快照：保存时据此找出被删掉的章节与小节 */
let savedSyllabus = [];

const lessonCount = computed(() =>
  form.value.syllabus.reduce((sum, ch) => sum + (ch.lessons ? ch.lessons.length : 0), 0)
);

const syncCategoryLabel = () => {
  form.value.categoryLabel = categories.value.find(c => c.id === form.value.category)?.label || '';
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
    title: '', category: 'tech', categoryLabel: '技术研发', instructorName: '', summary: '', published: true,
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
    category: course.category,
    categoryLabel: course.categoryLabel,
    instructorName: course.instructor?.name || '',
    summary: course.summary || '',
    published: course.published,
    thumbnail: course.thumbnail || '',
    syllabus: []
  };
  modalOpen.value = true;
  /* 列表接口不返回章节目录，编辑时必须按课程详情取回 */
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

/* 章节与小节各自走子资源接口：新增/改名/排序逐条提交，再删除快照里已不存在的行 */
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
        title: (lesson.name || '').trim() || '未命名小节',
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

  const categoryId = categoryIdOf(form.value.category);
  if (categoryId == null) {
    formError.value = '未找到该分类的课程编号，请先在该分类下建课或由后端补全分类接口。';
    return;
  }

  saving.value = true;
  formError.value = '';
  try {
    const instructorId = await resolveInstructorId(form.value.instructorName);
    const payload = {
      title: form.value.title,
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
  /* 1 草稿 / 2 已上架 / 3 已下架 */
  const nextCode = course.published ? 3 : 2;
  try {
    await updateAdminCourseStatus(course.numericId ?? course.id, nextCode);
    await loadCourses();
    showToast(`「${course.title}」已${nextCode === 2 ? '上架' : '下架'}`, 'success');
  } catch (error) {
    showToast(error?.message || '状态更新失败', 'error');
  }
};

/* ---------- 课程目录(章节 → 小节) ---------- */
const addChapter = () => {
  form.value.syllabus.push({
    id: `ch-${Date.now()}`,
    title: `第 ${form.value.syllabus.length + 1} 章: 新章节`,
    lessons: []
  });
};

const addLesson = (chapter) => {
  const lessons = chapter.lessons || (chapter.lessons = []);
  lessons.push({ id: `ls-${Date.now()}`, name: '', duration: '', url: '' });
};

/* 导出/转码后的视频文件名常是哈希串，直接照抄会变成一长串乱码 */
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

const handleLessonFile = async (lesson, e) => {
  const input = e.target;
  const file = input.files && input.files[0];
  input.value = '';
  if (!file || !lesson) return;
  if (file.size > 30 * 1024 * 1024) {
    showToast('视频超过 30MB，本演示环境仅支持小文件上传。', 'error');
    return;
  }
  const stem = file.name.replace(/\.[^.]+$/, '').trim();
  if (!lesson.name || lesson.name === '未命名小节' || isMeaninglessName(lesson.name)) {
    if (!isMeaninglessName(stem)) {
      lesson.name = stem;
    } else if (!lesson.name || lesson.name === '未命名小节') {
      lesson.name = '未命名小节';
    }
  }

  uploadingLesson.value = true;
  try {
    const result = await uploadFile(file, 'video');
    lesson.url = result?.url || result?.directUrl || '';
    showToast(`已上传视频：${lesson.name}`, 'success');
  } catch (error) {
    showToast(error?.message || '视频上传失败，请重试或换一个小文件。', 'error');
    return;
  } finally {
    uploadingLesson.value = false;
  }

  /* 时长用本地文件探测，不必为了读元数据再把视频下载一遍 */
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

const removeChapter = (chapter) => {
  askConfirm({
    tone: 'danger',
    title: '删除该章节？',
    subtitle: chapter.title,
    rows: [
      { label: '本章小节', value: `${chapter.lessons?.length || 0} 个` }
    ],
    tip: '保存后本章及其所有小节将一并移除，请确认内容已备份。',
    confirmText: '确认删除',
    action: () => {
      form.value.syllabus = form.value.syllabus.filter(ch => ch.id !== chapter.id);
    }
  });
};

const removeLesson = (chapter, lesson) => {
  chapter.lessons = chapter.lessons.filter(ls => ls.id !== lesson.id);
};

/* ---------- 封面 ---------- */
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
    const result = await uploadFile(file, 'cover');
    form.value.thumbnail = result?.url || result?.directUrl || '';
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
      { label: '课程分类', value: course.categoryLabel },
      { label: '课时数', value: `${course.totalLessons} 课时` },
      { label: '在学人数', value: `${(course.students || 0).toLocaleString()} 人` }
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
  min-height: 100%;
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

/* ========== 工具栏 ========== */
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

/* ========== 表格 ========== */
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

/* 表头默认 left 优先级更高,这里提权让数字列/操作列的表头与其内容对齐 */
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

/* ========== 分类与必修徽标 ========== */
.cat-chip {
  display: inline-flex;
  padding: 3px 10px;
  font-size: 11px;
  font-weight: 600;
  color: var(--app-accent-deep);
  background: var(--app-accent-soft);
  border-radius: 9999px;
}

/* ========== 状态徽标 ========== */
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

/* ========== 上架开关 ========== */
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
  color: var(--app-text-3);
  background: var(--app-surface-2);
  border: 1px solid var(--app-border);
  border-radius: 8px;
  transition: background 0.15s, color 0.15s, border-color 0.15s;
}
.icon-btn svg { width: 14px; height: 14px; }
.icon-btn:hover { background: var(--app-accent-soft); color: var(--app-accent-deep); border-color: var(--app-accent-border); }
.icon-btn--danger:hover { background: var(--app-rose-bg); color: var(--app-rose-fg); border-color: var(--app-rose-line); }

/* ========== 弹窗 ========== */
.modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(15, 30, 51, 0.50);
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

/* ========== 表单分区 ========== */
.section-block {
  padding: 18px 22px;
  border-bottom: 1px solid var(--app-border-soft);
}
.section-block:last-of-type { border-bottom: none; }

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

/* ========== 封面管理 ========== */
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

/* 已有封面时，悬停居中提示更换 */
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

/* ========== 上传按钮 ========== */
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

/* ========== 课程目录(章节 → 课时) ========== */
.syl-editor {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 12px;
}

.syl-chapter {
  position: relative;
  border: 1px solid var(--app-border);
  border-radius: 12px;
  overflow: hidden;
  background: var(--app-surface);
  transition: border-color 0.18s, box-shadow 0.18s;
}
.syl-chapter:hover {
  border-color: var(--app-border-strong);
  box-shadow: 0 2px 10px rgb(15 30 51 / 5%);
}
.syl-chapter:focus-within {
  border-color: var(--app-accent-border);
  box-shadow: 0 0 0 3px var(--app-accent-a10);
}

.syl-chapter-head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 14px;
  background: linear-gradient(90deg, var(--app-surface) 0%, var(--app-surface-2) 100%);
  border-bottom: 1px solid var(--app-border);
  flex-wrap: wrap;
}
.syl-chapter-head::before {
  content: "";
  position: absolute;
  left: 0;
  top: 9px;
  bottom: 9px;
  width: 3px;
  border-radius: 0 3px 3px 0;
  background: var(--app-accent);
  opacity: 0.75;
}

.syl-chapter-index {
  padding: 2px 7px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.03em;
  color: var(--app-accent-deep);
  background: var(--app-surface);
  border: 1px solid var(--app-accent-border);
  border-radius: 6px;
  white-space: nowrap;
  flex-shrink: 0;
}

.syl-chapter-title {
  flex: 1;
  min-width: 160px;
  padding: 5px 8px;
  font-weight: 600;
  font-size: 13px;
  color: var(--app-heading);
  background: transparent;
  border-color: transparent;
  border-radius: 7px;
}
.syl-chapter-title:hover { background: var(--app-surface-3); }
.syl-chapter-title:focus {
  background: var(--app-surface);
  border-color: var(--app-accent-border);
  box-shadow: 0 0 0 3px var(--app-accent-a10);
}

.chapter-head-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

/* 删除章节按钮 — 静默样式，悬停才显出危险色 */
.del-chapter-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  font-size: 11px;
  font-weight: 500;
  color: var(--app-text-4);
  background: transparent;
  border: 1px solid transparent;
  border-radius: 7px;
  cursor: pointer;
  transition: color 0.15s, border-color 0.15s, background 0.15s;
}
.del-chapter-btn:hover {
  color: var(--app-rose-fg);
  border-color: var(--app-rose-line);
  background: var(--app-rose-bg);
}
.del-icon { font-size: 10px; }

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
  background: var(--app-surface);
  transition: background 0.12s;
}
.syl-lesson:hover { background: var(--app-surface); }
.syl-lesson + .syl-lesson {
  border-top: 1px solid var(--app-border-soft);
}

.syl-lesson-no {
  width: 32px;
  font-size: 11px;
  font-weight: 700;
  color: var(--app-text-5);
  font-variant-numeric: tabular-nums;
  flex-shrink: 0;
}

.syl-lesson-name {
  flex: 1;
  min-width: 0;
  padding: 7px 10px;
  font-size: 12px;
}

.upload-btn--sm {
  padding: 5px 9px;
  font-size: 11px;
  border-radius: 7px;
  flex-shrink: 0;
}

/* 删除课时按钮 */
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

.syl-lessons-empty {
  padding: 12px 14px;
  font-size: 12px;
  color: var(--app-text-5);
  background: var(--app-surface);
}

.add-lesson-btn {
  width: 100%;
  padding: 9px 12px;
  font-size: 12px;
  color: var(--app-accent-deep);
  background: var(--app-surface);
  border: 1px dashed var(--app-accent-border);
  border-top: none;
  border-radius: 0 0 10px 10px;
  transition: background 0.15s, border-color 0.15s;
}
.add-lesson-btn:hover { background: var(--app-page-bg); border-color: var(--app-accent); }

.syl-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
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
  .syl-chapter-head { flex-direction: column; align-items: stretch; }
  .chapter-head-actions { justify-content: flex-end; }
  .syl-lesson { flex-wrap: wrap; }
}
</style>
