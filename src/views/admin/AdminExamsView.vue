<template>
  <div class="admin-exams admin-fit-page">

    <!-- 页头 -->
    <div class="page-head">
      <div>
        <h1 class="page-title">考试管理</h1>
        <p class="page-sub">共 {{ exams.length }} 场考试 · {{ runningCount }} 场进行中 · {{ draftCount }} 场待发布</p>
      </div>
      <button class="btn btn--primary" @click="openCreate">
        <Plus class="btn-icon" /> 新建考试
      </button>
    </div>

    <!-- 概览条 -->
    <div class="overview-strip">
      <div class="overview-item">
        <span class="overview-value">{{ runningCount }}</span>
        <span class="overview-label">进行中</span>
      </div>
      <div class="overview-item">
        <span class="overview-value">{{ scheduledCount }}</span>
        <span class="overview-label">待开始</span>
      </div>
      <div class="overview-item">
        <span class="overview-value">{{ endedCount }}</span>
        <span class="overview-label">已结束</span>
      </div>
      <div class="overview-item">
        <span class="overview-value overview-value--gold">{{ draftCount }}</span>
        <span class="overview-label">待发布</span>
      </div>
    </div>

    <div class="toolbar exam-filter-bar">
      <select v-model="categoryFilter" class="exam-filter-select" aria-label="按分类筛选考试">
        <option value="">全部分类</option>
        <option v-for="category in categories" :key="category" :value="category">
          {{ category }}
        </option>
      </select>
      <select v-model="stateFilter" class="exam-filter-select" aria-label="按状态筛选考试">
        <option value="">全部状态</option>
        <option v-for="state in examStates" :key="state.value" :value="state.value">
          {{ state.label }}
        </option>
      </select>
      <label class="exam-search">
        <span class="search-icon" aria-hidden="true"></span>
        <input
          v-model.trim="keyword"
          type="search"
          class="exam-search-input"
          placeholder="搜索考试名称 / 组织方..."
          aria-label="搜索考试名称或组织方"
        />
      </label>
      <span class="exam-filter-result">{{ filteredExams.length }} 场考试</span>
    </div>

    <!-- 考试表格 -->
    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>考试</th>
            <th>时间窗</th>
            <th class="num">时长</th>
            <th class="num">题目数</th>
            <th>状态</th>
            <th class="ops">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="exam in pagedExams" :key="exam.id" @click="openDetail(exam)">
            <td>
              <div class="cell-exam">
                <span class="exam-dot" :class="`exam-dot--${exam.state}`"></span>
                <div>
                  <div class="cell-exam-title">{{ exam.title }}</div>
                  <div class="cell-exam-meta">{{ exam.category }}</div>
                </div>
              </div>
            </td>
            <td class="cell-muted">{{ exam.timeWindow }}</td>
            <td class="num">{{ exam.durationMinutes }}分</td>
            <td class="num">{{ questionCount(exam.id) }}</td>
            <td>
              <span class="status-pill" :class="`status-pill--${exam.state}`">
                {{ stateLabel(exam.state) }}
              </span>
            </td>
            <td class="ops">
              <div class="row-actions" @click.stop>
                <button
                  v-if="exam.state === 'draft'"
                  class="text-btn text-btn--primary"
                  @click="publishExam(exam)"
                >发布</button>
                <button
                  v-else-if="exam.state === 'scheduled'"
                  class="text-btn text-btn--warn"
                  @click="revokeExam(exam)"
                >撤回</button>
                <button class="text-btn text-btn--primary" @click="openQuickStatus(exam)">
                  快捷改状态
                </button>
                <button class="icon-btn" title="编辑" @click="openEdit(exam)">
                  <EditPen />
                </button>
                <button class="icon-btn icon-btn--danger" title="删除" @click="removeExam(exam)">
                  <Delete />
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="!filteredExams.length">
            <td colspan="6" class="empty-cell">
              {{ exams.length ? '没有匹配的考试' : '暂无考试数据' }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <AdminPager
      v-model:page="page"
      :total="filteredExams.length"
      :total-pages="totalPages"
    />

    <div v-if="quickStatusExam" class="modal-backdrop" @click.self="closeQuickStatus">
      <div class="modal modal--quick-status">
        <div class="modal-head">
          <h2 class="modal-title">快捷修改状态</h2>
          <button type="button" class="modal-close" @click="closeQuickStatus"><Close /></button>
        </div>
        <form class="modal-body" @submit.prevent="saveQuickStatus">
          <p class="quick-status-title">{{ quickStatusExam.title }}</p>
          <label class="field">
            <span class="field-label">考试状态</span>
            <select v-model.number="quickStatus" class="field-input">
              <option v-for="state in examStatusOptions" :key="state.value" :value="state.value">
                {{ state.label }}
              </option>
            </select>
          </label>
          <p v-if="quickStatusError" class="form-error">{{ quickStatusError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn btn--ghost" @click="closeQuickStatus">取消</button>
            <button type="submit" class="btn btn--primary" :disabled="quickStatusSaving">
              {{ quickStatusSaving ? '保存中…' : '保存状态' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 考试详情 -->
    <AdminDetailModal
      v-if="detailExam"
      title="考试详情"
      :subtitle="detailExam.title"
      :tags="[stateLabel(detailExam.state), `${questionCount(detailExam.id)} 道题`]"
      :rows="[
        { label: '组织方', value: detailExam.category },
        { label: '考试时间', value: detailExam.timeWindow, wide: true },
        { label: '考试时长', value: `${detailExam.durationMinutes} 分钟` },
        { label: '题目数量', value: `${questionCount(detailExam.id)} 道` },
        { label: '总分 / 及格分', value: `${detailExam.totalScore} / ${detailExam.passScore}` },
        { label: '当前状态', value: stateLabel(detailExam.state) }
      ]"
      @close="detailExam = null"
    />

    <!-- 新建 / 编辑考试弹窗 -->
    <div v-if="modalOpen" class="modal-backdrop" @click.self="closeModal">
      <div class="modal">
        <div class="modal-head">
          <h2 class="modal-title">{{ editingId ? '编辑考试' : '新建考试' }}</h2>
          <button type="button" class="modal-close" @click="closeModal"><Close /></button>
        </div>
        <form class="modal-body" @submit.prevent="saveExam">
          <label class="field">
            <span class="field-label">考试编号</span>
            <input v-model.trim="form.code" type="text" class="field-input" placeholder="如 EXAM-2026-001" />
          </label>
          <label class="field">
            <span class="field-label">考试名称</span>
            <input v-model.trim="form.title" type="text" class="field-input" placeholder="请输入考试名称" />
          </label>
          <label class="field">
            <span class="field-label">组织方</span>
            <input v-model.trim="form.category" type="text" class="field-input" placeholder="如 技术合规部" />
          </label>
          <label v-if="editingId" class="field">
            <span class="field-label">考试状态</span>
            <select v-model.number="form.status" class="field-input">
              <option v-for="state in examStatusOptions" :key="state.value" :value="state.value">
                {{ state.label }}
              </option>
            </select>
          </label>

          <!-- 时间窗便捷录入 -->
          <div class="field">
            <span class="field-label">考试日期</span>
            <input v-model="form.examDate" type="date" class="field-input" />
          </div>
          <div class="field-row">
            <label class="field">
              <span class="field-label">开始时间</span>
              <input v-model="form.startTime" type="time" class="field-input" />
            </label>
            <label class="field">
              <span class="field-label">结束时间</span>
              <input v-model="form.endTime" type="time" class="field-input" />
            </label>
          </div>
          <div class="window-preview">
            <span class="window-preview-label">时间窗</span>
            <span class="window-preview-value">{{ timeWindowPreview }}</span>
            <span class="window-preview-duration">时长 {{ durationMinutes }} 分钟</span>
          </div>

          <label class="field">
            <span class="field-label">及格分</span>
            <input v-model.number="form.passScore" type="number" min="0" max="100" class="field-input" />
          </label>
          <p v-if="formError" class="form-error">{{ formError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn btn--ghost" @click="closeModal">取消</button>
            <button type="submit" class="btn btn--primary" :disabled="saving">{{ saving ? '保存中…' : (editingId ? '保存修改' : '保存为待发布') }}</button>
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
import { computed, ref, watch } from 'vue';
import { Plus, EditPen, Delete, Close } from '@element-plus/icons-vue';
import { adminStore, loadExams, loadQuestions, questionCount } from '../../adminStore.js';
import {
  createAdminExam,
  deleteAdminExam,
  updateAdminExam,
  updateAdminExamStatus,
} from '../../api/index.js';
import { usePager } from '../../composables/usePager.js';
import { useConfirm } from '../../composables/useConfirm.js';
import { useToast } from '../../composables/useToast.js';
import AdminPager from '../../components/AdminPager.vue';
import AdminDetailModal from '../../components/AdminDetailModal.vue';
import ConfirmDialog from '../../components/ConfirmDialog.vue';

const { show: showToast } = useToast();

defineEmits(['navigate']);

const exams = adminStore.exams;

const { confirmState, askConfirm, cancelConfirm, acceptConfirm } = useConfirm();

const categoryFilter = ref('');
const stateFilter = ref('');
const keyword = ref('');

const categories = computed(() => [...new Set(exams.map(exam => exam.category).filter(Boolean))].sort());

const runningCount = computed(() => exams.filter(e => e.state === 'running').length);
const scheduledCount = computed(() => exams.filter(e => e.state === 'scheduled').length);
const endedCount = computed(() => exams.filter(e => e.state === 'ended').length);
const draftCount = computed(() => exams.filter(e => e.state === 'draft').length);

const filteredExams = computed(() => {
  const search = keyword.value.toLowerCase();
  return exams.filter((exam) => {
    const matchesCategory = !categoryFilter.value || exam.category === categoryFilter.value;
    const matchesState = !stateFilter.value || exam.state === stateFilter.value;
    const matchesKeyword = !search ||
      `${exam.title} ${exam.category}`.toLowerCase().includes(search);
    return matchesCategory && matchesState && matchesKeyword;
  });
});

const { page, totalPages, paged: pagedExams, resetPage } = usePager(filteredExams, 7);
watch([categoryFilter, stateFilter, keyword], resetPage);

const examStates = [
  { value: 'running', label: '进行中' },
  { value: 'scheduled', label: '待开始' },
  { value: 'ended', label: '已结束' },
  { value: 'draft', label: '待发布' },
];

const examStatusOptions = [
  { value: 1, state: 'draft', label: '待发布' },
  { value: 2, state: 'scheduled', label: '待开始' },
  { value: 3, state: 'running', label: '进行中' },
  { value: 4, state: 'ended', label: '已结束' },
];

const stateLabelMap = {
  running: '进行中',
  scheduled: '待开始',
  ended: '已结束',
  draft: '待发布',
};
const stateLabel = (state) => stateLabelMap[state] || state;

/* ---------- 考试详情 ---------- */
const detailExam = ref(null);
const openDetail = (exam) => { detailExam.value = exam; };

const quickStatusExam = ref(null);
const quickStatus = ref(1);
const quickStatusError = ref('');
const quickStatusSaving = ref(false);

const openQuickStatus = (exam) => {
  quickStatusExam.value = exam;
  quickStatus.value = examStatusOptions.some((state) => state.value === exam.statusCode)
    ? exam.statusCode
    : examStatusOptions.find((state) => state.state === exam.state)?.value ?? 2;
  quickStatusError.value = '';
};

const closeQuickStatus = () => {
  if (quickStatusSaving.value) return;
  quickStatusExam.value = null;
  quickStatusError.value = '';
};

const saveQuickStatus = async () => {
  if (!quickStatusExam.value) return;
  quickStatusSaving.value = true;
  quickStatusError.value = '';
  try {
    await updateAdminExamStatus(
      quickStatusExam.value.numericId ?? quickStatusExam.value.id,
      quickStatus.value,
    );
    await loadExams();
    showToast(`已更新考试「${quickStatusExam.value.title}」状态`, 'success');
    quickStatusExam.value = null;
  } catch (error) {
    quickStatusError.value = error?.message || '状态更新失败，请稍后重试。';
  } finally {
    quickStatusSaving.value = false;
  }
};

/* ---------- 新建 / 编辑考试表单 ---------- */
const modalOpen = ref(false);
const editingId = ref(null);
const formError = ref('');
const saving = ref(false);

const defaultDate = () => {
  const d = new Date(Date.now() + 3 * 24 * 60 * 60 * 1000);
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
};

const form = ref({
  code: '',
  title: '',
  category: '',
  examDate: defaultDate(),
  startTime: '14:00',
  endTime: '15:30',
  status: 1,
  passScore: 80
});

const toMinutes = (hhmm) => {
  const [h, m] = String(hhmm || '').split(':').map(Number);
  return (h || 0) * 60 + (m || 0);
};

const durationMinutes = computed(() => {
  const diff = toMinutes(form.value.endTime) - toMinutes(form.value.startTime);
  return diff > 0 ? diff : 60;
});

const timeWindowPreview = computed(() =>
  `${form.value.examDate} ${form.value.startTime} ~ ${form.value.endTime}`
);

const openCreate = () => {
  editingId.value = null;
  formError.value = '';
  form.value = {
    code: '',
    title: '',
    category: '',
    examDate: defaultDate(),
    startTime: '14:00',
    endTime: '15:30',
    status: 1,
    passScore: 80
  };
  modalOpen.value = true;
};

/* 接口返回 ISO 时间则直接取，种子数据只有展示用时间窗时尽力解析 */
const fromIso = (value) => {
  const m = /^(\d{4}-\d{2}-\d{2})[T ](\d{2}:\d{2})/.exec(value || '');
  return m ? { date: m[1], time: m[2] } : null;
};

const parseWindow = (text) => {
  const date = /\d{4}-\d{2}-\d{2}/.exec(text || '')?.[0];
  const times = text?.match(/\d{2}:\d{2}/g) || [];
  return { examDate: date || defaultDate(), startTime: times[0] || '14:00', endTime: times[1] || '15:30' };
};

const openEdit = (exam) => {
  const start = fromIso(exam.startAt);
  const end = fromIso(exam.endAt);
  const win = start && end
    ? { examDate: start.date, startTime: start.time, endTime: end.time }
    : parseWindow(exam.timeWindow);
  editingId.value = exam.id;
  formError.value = '';
  form.value = {
    code: exam.code || '',
    title: exam.title,
    category: exam.category,
    examDate: win.examDate,
    startTime: win.startTime,
    endTime: win.endTime,
    status: examStatusOptions.some((state) => state.value === exam.statusCode)
      ? exam.statusCode
      : examStatusOptions.find((state) => state.state === exam.state)?.value ?? 2,
    passScore: exam.passScore
  };
  modalOpen.value = true;
};

const closeModal = () => {
  modalOpen.value = false;
  formError.value = '';
};

const saveExam = async () => {
  if (!form.value.code) { formError.value = '请输入考试编号。'; return; }
  if (!form.value.title) { formError.value = '请输入考试名称。'; return; }
  if (!form.value.category) { formError.value = '请输入组织方。'; return; }
  if (!form.value.examDate) { formError.value = '请选择考试日期。'; return; }
  if (toMinutes(form.value.endTime) <= toMinutes(form.value.startTime)) {
    formError.value = '结束时间需要晚于开始时间。';
    return;
  }
  const dup = exams.some(e => e.code === form.value.code && e.id !== editingId.value);
  if (dup) { formError.value = '考试编号已存在，请换一个。'; return; }

  const payload = {
    code: form.value.code,
    title: form.value.title,
    organizer: form.value.category,
    startAt: `${form.value.examDate}T${form.value.startTime}:00`,
    endAt: `${form.value.examDate}T${form.value.endTime}:00`,
    durationMinutes: durationMinutes.value,
    passScore: form.value.passScore,
    ...(editingId.value ? { status: form.value.status } : {}),
  };

  saving.value = true;
  try {
    if (editingId.value) {
      await updateAdminExam(editingId.value, payload);
      showToast(`已更新考试「${payload.title}」`, 'success');
    } else {
      await createAdminExam(payload);
      showToast(`已创建考试「${payload.title}」，当前为待发布`, 'success');
    }
    await loadExams();
    closeModal();
  } catch (error) {
    formError.value = error?.message || '保存失败，请稍后重试。';
  } finally {
    saving.value = false;
  }
};

const publishExam = async (exam) => {
  if (!questionCount(exam.id)) {
    showToast('该考试暂无题目，请先到「题库管理」为该考试添加题目后再发布。', 'info');
    return;
  }
  try {
    await updateAdminExamStatus(exam.numericId ?? exam.id, 2);
    await loadExams();
    showToast(`已发布考试「${exam.title}」`, 'success');
  } catch (error) {
    showToast(error?.message || '发布失败', 'error');
  }
};

const revokeExam = (exam) => {
  askConfirm({
    tone: 'primary',
    title: '撤回该考试？',
    subtitle: exam.title,
    rows: [
      { label: '当前状态', value: stateLabel(exam.state) },
      { label: '时间窗', value: exam.timeWindow },
      { label: '题目数', value: `${questionCount(exam.id)} 题` }
    ],
    tip: '撤回后考试将回到「待发布」状态，学员端不再展示该考试。',
    confirmText: '确认撤回',
    action: async () => {
      try {
        await updateAdminExamStatus(exam.numericId ?? exam.id, 1);
        await loadExams();
        showToast(`已撤回考试「${exam.title}」`, 'success');
      } catch (error) {
        showToast(error?.message || '撤回失败', 'error');
      }
    }
  });
};

const removeExam = (exam) => {
  askConfirm({
    tone: 'danger',
    title: '删除该考试？',
    subtitle: exam.title,
    rows: [
      { label: '考试状态', value: stateLabel(exam.state) },
      { label: '时间窗', value: exam.timeWindow },
      { label: '参考人数', value: `${(exam.participants || 0).toLocaleString()} 人` }
    ],
    tip: '删除后考试信息、题目关联与成绩记录将一并移除，此操作不可恢复。',
    confirmText: '确认删除',
    action: async () => {
      try {
        await deleteAdminExam(exam.numericId ?? exam.id);
        await Promise.all([loadExams(), loadQuestions()]);
        showToast(`已删除考试「${exam.title}」`, 'success');
      } catch (error) {
        showToast(error?.message || '删除失败', 'error');
      }
    }
  });
};
</script>

<style scoped>
/* ============================================================
   AdminExams · 考试管理 · 纯原生 CSS
   ============================================================ */

* { margin: 0; padding: 0; box-sizing: border-box; }
button, input { font-family: inherit; }
button { cursor: pointer; }

.admin-exams {
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

/* ========== 概览条 ========== */
.overview-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.overview-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 14px;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 12px;
}

.overview-value {
  font-size: 22px;
  font-weight: 800;
  color: var(--app-heading);
  font-variant-numeric: tabular-nums;
}
.overview-value--gold { color: var(--app-amber-fg); }

.overview-label {
  font-size: 12px;
  color: var(--app-text-4);
}

/* ========== 筛选工具栏 ========== */
.exam-filter-bar {
  display: grid;
  grid-template-columns: minmax(150px, 190px) minmax(130px, 160px) minmax(220px, 1fr) auto;
  align-items: center;
  gap: 12px;
  min-height: 64px;
  padding: 12px 16px;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 12px;
}

.exam-filter-select {
  width: 100%;
  min-width: 0;
  height: 40px;
  padding: 0 34px 0 12px;
  font-size: 13px;
  color: var(--app-text-2);
  background-color: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 10px;
  outline: none;
  cursor: pointer;
}
.exam-filter-select:focus {
  border-color: var(--app-accent-strong);
  box-shadow: 0 0 0 3px var(--app-accent-a12);
}

.exam-search {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  height: 40px;
  padding: 0 12px;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 10px;
}
.exam-search:focus-within {
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
  position: absolute;
  top: 0;
  left: 0;
  width: 9px;
  height: 9px;
  border: 2px solid var(--app-text-4);
  border-radius: 50%;
  content: '';
}
.search-icon::after {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 6px;
  height: 2px;
  background: var(--app-text-4);
  border-radius: 1px;
  content: '';
  transform: rotate(45deg);
  transform-origin: right center;
}

.exam-search-input {
  flex: 1;
  min-width: 0;
  height: 100%;
  font-size: 13px;
  color: var(--app-text);
  background: transparent;
  border: 0;
  outline: 0;
}

.exam-search-input::placeholder { color: var(--app-text-5); }
.exam-search-input::-webkit-search-cancel-button { cursor: pointer; }

.exam-filter-result {
  justify-self: end;
  padding: 7px 11px;
  font-size: 12px;
  font-weight: 600;
  color: var(--app-accent-deep);
  background: var(--app-accent-soft);
  border-radius: 999px;
  white-space: nowrap;
}

@media (max-width: 900px) {
  .exam-filter-bar {
    grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  }
  .exam-search,
  .exam-filter-result {
    grid-column: 1 / -1;
  }
  .exam-filter-result { justify-self: start; }
}

@media (max-width: 420px) {
  .exam-filter-bar { grid-template-columns: minmax(0, 1fr); }
  .exam-filter-select,
  .exam-search,
  .exam-filter-result { grid-column: 1; }
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
.data-table tbody tr:hover .cell-exam-title,
.data-table tbody tr:hover .cell-exam-meta,
.data-table tbody tr:hover .cell-muted {
  color: var(--app-accent-deep);
}

th.num, td.num { text-align: right; }
th.ops, td.ops { text-align: right; }

/* 表头默认 left 优先级更高,这里提权让数字列/操作列的表头与其内容对齐 */
.data-table thead th.num,
.data-table thead th.ops { text-align: right; }

.cell-muted { color: var(--app-text-4); }
.empty-cell { text-align: center; color: var(--app-text-5); padding: 28px 0 !important; }

.cell-exam {
  display: flex;
  align-items: center;
  gap: 10px;
  max-width: 400px;
}

.exam-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.exam-dot--running   { background: #2FBF8F; }
.exam-dot--scheduled { background: #2E8BD6; }
.exam-dot--open      { background: #9381FF; }
.exam-dot--ended     { background: var(--app-text-5); }
.exam-dot--draft     { background: #E9B949; }

.cell-exam-title {
  font-weight: 600;
  color: var(--app-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cell-exam-meta {
  margin-top: 2px;
  font-size: 11px;
  color: var(--app-text-5);
}

/* ========== 状态徽标 ========== */
.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 56px;
  padding: 3px 10px;
  font-size: 11px;
  font-weight: 700;
  border-radius: 9999px;
}
.status-pill--running   { color: var(--app-emerald-fg); background: var(--app-emerald-bg); }
.status-pill--scheduled { color: var(--app-accent-deep); background: var(--app-accent-soft); }
.status-pill--open      { color: var(--app-violet-fg); background: var(--app-violet-bg); }
.status-pill--ended     { color: var(--app-text-4); background: var(--app-chip); }
.status-pill--draft     { color: var(--app-amber-fg); background: var(--app-amber-bg); }

/* ========== 行操作 ========== */
.row-actions {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.text-btn {
  padding: 5px 12px;
  font-size: 12px;
  font-weight: 700;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 9999px;
  transition: background 0.15s;
}
.text-btn--primary { color: var(--app-accent-deep); background: var(--app-accent-soft); }
.text-btn--primary:hover { background: var(--app-accent-hover); }
.text-btn--warn { color: var(--app-brand-ink); background: var(--app-brand-tint); }
.text-btn--warn:hover { background: var(--app-brand-tint-line); }
.text-btn--bank { color: var(--app-violet-fg); background: var(--app-violet-bg); }
.text-btn--bank:hover { background: var(--app-violet-line); }

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
  background: rgba(15, 30, 51, 0.45);
  backdrop-filter: blur(2px);
}

.modal {
  width: 100%;
  max-width: 520px;
  background: var(--app-surface);
  border-radius: 16px;
  box-shadow: 0 24px 48px rgba(15, 30, 51, 0.25);
  overflow: hidden;
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
  border-bottom: 1px solid var(--app-border-soft);
}

.modal-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--app-heading);
}

.modal-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  color: var(--app-text-4);
  background: transparent;
  border: none;
  border-radius: 8px;
  transition: background 0.15s, color 0.15s;
}
.modal-close svg { width: 16px; height: 16px; }
.modal-close:hover { background: var(--app-surface-3); color: var(--app-heading); }

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

/* ========== 时间窗预览 ========== */
.window-preview {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: var(--app-surface);
  border: 1px dashed var(--app-accent-border);
  border-radius: 10px;
  flex-wrap: wrap;
}

.window-preview-label {
  font-size: 12px;
  font-weight: 700;
  color: var(--app-accent-deep);
}

.window-preview-value {
  font-size: 13px;
  font-weight: 600;
  color: var(--app-heading);
  font-variant-numeric: tabular-nums;
}

.window-preview-duration {
  margin-left: auto;
  font-size: 11px;
  color: var(--app-text-4);
}

.form-error {
  font-size: 12px;
  color: var(--app-rose-fg);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 4px;
}

@media (max-width: 640px) {
  .overview-strip { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .field-row { grid-template-columns: 1fr; }
}
</style>
