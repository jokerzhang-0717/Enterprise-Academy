<template>
  <div class="admin-exams">

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
          <tr v-for="exam in exams" :key="exam.id">
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
              <div class="row-actions">
                <button
                  class="text-btn text-btn--bank"
                  title="在题库管理中查看该考试题目"
                  @click="$emit('navigate', 'admin-question-bank')"
                >题库</button>
                <button
                  v-if="exam.state === 'draft'"
                  class="text-btn text-btn--primary"
                  @click="publishExam(exam)"
                >发布</button>
                <button
                  v-else-if="exam.state === 'scheduled' || exam.state === 'open'"
                  class="text-btn text-btn--warn"
                  @click="revokeExam(exam)"
                >撤回</button>
                <button class="icon-btn icon-btn--danger" title="删除" @click="removeExam(exam)">
                  <Delete />
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="!exams.length">
            <td colspan="6" class="empty-cell">暂无考试数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 新建考试弹窗 -->
    <div v-if="modalOpen" class="modal-backdrop" @click.self="closeModal">
      <div class="modal">
        <div class="modal-head">
          <h2 class="modal-title">新建考试</h2>
          <button class="modal-close" @click="closeModal"><Close /></button>
        </div>
        <form class="modal-body" @submit.prevent="saveExam">
          <label class="field">
            <span class="field-label">考试名称</span>
            <input v-model.trim="form.title" type="text" class="field-input" placeholder="请输入考试名称" />
          </label>
          <label class="field">
            <span class="field-label">组织方</span>
            <input v-model.trim="form.category" type="text" class="field-input" placeholder="如 技术合规部" />
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
            <button type="submit" class="btn btn--primary">保存为待发布</button>
          </div>
        </form>
      </div>
    </div>

  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import { Plus, Delete, Close } from '@element-plus/icons-vue';
import { adminStore, questionCount } from '../../adminStore.js';

defineEmits(['navigate']);

const exams = adminStore.exams;

const runningCount = computed(() => exams.filter(e => e.state === 'running').length);
const scheduledCount = computed(() => exams.filter(e => e.state === 'scheduled').length);
const endedCount = computed(() => exams.filter(e => e.state === 'ended').length);
const draftCount = computed(() => exams.filter(e => e.state === 'draft').length);

const stateLabelMap = {
  running: '进行中',
  scheduled: '待开始',
  open: '报名中',
  ended: '已结束',
  draft: '待发布'
};
const stateLabel = (state) => stateLabelMap[state] || state;

/* ---------- 新建考试表单 ---------- */
const modalOpen = ref(false);
const formError = ref('');

const defaultDate = () => {
  const d = new Date(Date.now() + 3 * 24 * 60 * 60 * 1000);
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
};

const form = ref({
  title: '',
  category: '',
  examDate: defaultDate(),
  startTime: '14:00',
  endTime: '15:30',
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
  formError.value = '';
  form.value = {
    title: '',
    category: '',
    examDate: defaultDate(),
    startTime: '14:00',
    endTime: '15:30',
    passScore: 80
  };
  modalOpen.value = true;
};

const closeModal = () => {
  modalOpen.value = false;
  formError.value = '';
};

const saveExam = () => {
  if (!form.value.title) { formError.value = '请输入考试名称。'; return; }
  if (!form.value.category) { formError.value = '请输入组织方。'; return; }
  if (!form.value.examDate) { formError.value = '请选择考试日期。'; return; }
  if (toMinutes(form.value.endTime) <= toMinutes(form.value.startTime)) {
    formError.value = '结束时间需要晚于开始时间。';
    return;
  }

  exams.unshift({
    id: `exam-${Date.now()}`,
    code: '',
    title: form.value.title,
    category: form.value.category,
    state: 'draft',
    timeWindow: timeWindowPreview.value,
    durationMinutes: durationMinutes.value,
    totalScore: 100,
    passScore: form.value.passScore,
    questionCount: 0,
    participants: 0
  });
  closeModal();
};

const publishExam = (exam) => {
  if (!questionCount(exam.id)) {
    window.alert('该考试暂无题目,请先到「题库管理」为该考试添加题目后再发布。');
    return;
  }
  exam.state = 'scheduled';
};

const revokeExam = (exam) => {
  if (!window.confirm(`确认撤回考试「${exam.title}」?撤回后学员端不再展示。`)) return;
  exam.state = 'draft';
};

const removeExam = (exam) => {
  if (!window.confirm(`确认删除考试「${exam.title}」?此操作不可恢复。`)) return;
  const idx = exams.findIndex(e => e.id === exam.id);
  if (idx > -1) exams.splice(idx, 1);
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
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
}

.overview-value {
  font-size: 22px;
  font-weight: 800;
  color: #0F1E33;
  font-variant-numeric: tabular-nums;
}
.overview-value--gold { color: #B45309; }

.overview-label {
  font-size: 12px;
  color: #64748B;
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

.cell-muted { color: #64748B; }
.empty-cell { text-align: center; color: #94A3B8; padding: 28px 0 !important; }

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
.exam-dot--ended     { background: #94A3B8; }
.exam-dot--draft     { background: #E9B949; }

.cell-exam-title {
  font-weight: 600;
  color: #1E293B;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cell-exam-meta {
  margin-top: 2px;
  font-size: 11px;
  color: #94A3B8;
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
.status-pill--running   { color: #047857; background: #D1FAE5; }
.status-pill--scheduled { color: #005576; background: #D9F0FF; }
.status-pill--open      { color: #6555B0; background: #EDE9FE; }
.status-pill--ended     { color: #64748B; background: #E2E8F0; }
.status-pill--draft     { color: #8A6414; background: #FEF3C7; }

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
.text-btn--primary { color: #005576; background: #D9F0FF; }
.text-btn--primary:hover { background: #C2E4FA; }
.text-btn--warn { color: #8A6414; background: #FEF3C7; }
.text-btn--warn:hover { background: #FDE68A; }
.text-btn--bank { color: #6555B0; background: #EDE9FE; }
.text-btn--bank:hover { background: #DDD6FE; }

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
  background: #fff;
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

/* ========== 时间窗预览 ========== */
.window-preview {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: #F6FBFF;
  border: 1px dashed #A3D5FF;
  border-radius: 10px;
  flex-wrap: wrap;
}

.window-preview-label {
  font-size: 12px;
  font-weight: 700;
  color: #005576;
}

.window-preview-value {
  font-size: 13px;
  font-weight: 600;
  color: #0F1E33;
  font-variant-numeric: tabular-nums;
}

.window-preview-duration {
  margin-left: auto;
  font-size: 11px;
  color: #64748B;
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

@media (max-width: 640px) {
  .overview-strip { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .field-row { grid-template-columns: 1fr; }
}
</style>
