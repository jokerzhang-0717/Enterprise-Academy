<template>
  <div class="admin-bank">

    <!-- 页头 -->
    <div class="page-head">
      <div>
        <h1 class="page-title">题库管理</h1>
        <p class="page-sub">全题库统一维护 · 题目发布后即被对应考试引用</p>
      </div>
      <button class="btn btn--primary" @click="openAdd">
        <Plus class="btn-icon" /> 新增题目
      </button>
    </div>

    <!-- 题型统计 -->
    <div class="stat-strip">
      <div class="stat-item">
        <span class="stat-value">{{ questions.length }}</span>
        <span class="stat-label">总题数</span>
      </div>
      <div class="stat-item">
        <span class="stat-value stat-value--blue">{{ typeCount('single') }}</span>
        <span class="stat-label">单选题</span>
      </div>
      <div class="stat-item">
        <span class="stat-value stat-value--purple">{{ typeCount('multiple') }}</span>
        <span class="stat-label">多选题</span>
      </div>
      <div class="stat-item">
        <span class="stat-value stat-value--gold">{{ typeCount('judgment') }}</span>
        <span class="stat-label">判断题</span>
      </div>
      <div class="stat-item">
        <span class="stat-value stat-value--green">{{ typeCount('application') }}</span>
        <span class="stat-label">问答题</span>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="toolbar">
      <select v-model="examFilter" class="select">
        <option value="">全部考试</option>
        <option v-for="exam in exams" :key="exam.id" :value="exam.id">
          {{ exam.title.slice(0, 24) }}{{ exam.title.length > 24 ? '…' : '' }}
        </option>
      </select>
      <select v-model="typeFilter" class="select">
        <option value="">全部题型</option>
        <option v-for="t in qTypeOptions" :key="t.id" :value="t.id">{{ t.label }}</option>
      </select>
      <div class="search-box">
        <span class="search-icon"></span>
        <input v-model.trim="keyword" type="text" class="search-input" placeholder="搜索题干关键词..." />
      </div>
      <span class="toolbar-result">{{ filteredQuestions.length }} 道题</span>
    </div>

    <!-- 题目列表 -->
    <div class="question-list">
      <article v-for="(q, idx) in pagedQuestions" :key="q.id" class="question-item">
        <div class="q-index">{{ (page - 1) * 4 + idx + 1 }}</div>
        <div class="q-main">
          <div class="q-top">
            <span class="q-type-chip" :class="`q-type-chip--${q.type}`">{{ q.typeLabel }}</span>
            <span class="q-score">{{ q.score }} 分</span>
            <span class="q-exam">{{ examTitle(q.examId) }}</span>
            <span v-if="q.answer" class="q-answer mono">答案:{{ q.answer }}</span>
          </div>
          <p class="q-title">{{ q.title }}</p>
        </div>
        <div class="q-actions">
          <button class="icon-btn" title="编辑" @click="openEdit(q)"><EditPen /></button>
          <button class="icon-btn icon-btn--danger" title="删除" @click="removeQ(q)"><Delete /></button>
        </div>
      </article>
      <p v-if="!filteredQuestions.length" class="empty-tip">没有匹配的题目,点击右上角「新增题目」添加。</p>
    </div>

    <AdminPager
      v-model:page="page"
      :total="filteredQuestions.length"
      :total-pages="totalPages"
    />

    <!-- 新增 / 编辑题目弹窗 -->
    <div v-if="modalOpen" class="modal-backdrop" @click.self="closeModal">
      <div class="modal">
        <div class="modal-head">
          <h2 class="modal-title">{{ editingId ? '编辑题目' : '新增题目' }}</h2>
          <button class="modal-close" @click="closeModal"><Close /></button>
        </div>
        <form class="modal-body" @submit.prevent="saveQ">
          <div class="field-row">
            <label class="field">
              <span class="field-label">所属考试</span>
              <select v-model="form.examId" class="field-input">
                <option value="" disabled>请选择考试</option>
                <option v-for="exam in exams" :key="exam.id" :value="exam.id">
                  {{ exam.title.slice(0, 28) }}{{ exam.title.length > 28 ? '…' : '' }}
                </option>
              </select>
            </label>
            <label class="field">
              <span class="field-label">题型</span>
              <select v-model="form.type" class="field-input" @change="switchType">
                <option v-for="t in qTypeOptions" :key="t.id" :value="t.id">{{ t.label }}</option>
              </select>
            </label>
          </div>
          <div class="field-row">
            <label class="field">
              <span class="field-label">分值</span>
              <input v-model.number="form.score" type="number" min="1" class="field-input" />
            </label>
          </div>
          <label class="field">
            <span class="field-label">题干</span>
            <textarea v-model.trim="form.title" rows="3" class="field-input field-textarea" placeholder="请输入题目内容"></textarea>
          </label>
          <template v-if="needOptions">
            <div v-for="opt in form.options" :key="opt.key" class="field">
              <span class="field-label">选项 {{ opt.key }}</span>
              <input
                v-model.trim="opt.text"
                type="text"
                class="field-input"
                :disabled="form.type === 'judgment'"
                :placeholder="`选项 ${opt.key} 内容`"
              />
            </div>
            <label class="field">
              <span class="field-label">答案</span>
              <input
                v-if="form.type === 'multiple'"
                v-model.trim="form.answer"
                type="text"
                class="field-input mono"
                placeholder="多选题答案,如 ABD"
              />
              <select v-else v-model="form.answer" class="field-input">
                <option value="" disabled>请选择正确答案</option>
                <option v-for="opt in form.options" :key="opt.key" :value="opt.key">{{ opt.key }} · {{ opt.text }}</option>
              </select>
            </label>
          </template>
          <p v-if="formError" class="form-error">{{ formError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn btn--ghost" @click="closeModal">取消</button>
            <button type="submit" class="btn btn--primary">{{ editingId ? '保存修改' : '添加题目' }}</button>
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
import { adminStore } from '../../adminStore.js';
import { usePager } from '../../composables/usePager.js';
import { useConfirm } from '../../composables/useConfirm.js';
import AdminPager from '../../components/AdminPager.vue';
import ConfirmDialog from '../../components/ConfirmDialog.vue';

const questions = adminStore.questions;

const { confirmState, askConfirm, cancelConfirm, acceptConfirm } = useConfirm();
const exams = adminStore.exams;

const examFilter = ref('');
const typeFilter = ref('');
const keyword = ref('');

const qTypeOptions = [
  { id: 'single',      label: '单选题' },
  { id: 'multiple',    label: '多选题' },
  { id: 'judgment',    label: '判断题' },
  { id: 'application', label: '问答题' }
];

const typeCount = (type) => questions.filter(q => q.type === type).length;
const examTitle = (examId) => {
  const exam = exams.find(e => e.id === examId);
  return exam ? `所属:${exam.title.slice(0, 14)}${exam.title.length > 14 ? '…' : ''}` : '未关联考试';
};

const filteredQuestions = computed(() => {
  const kw = keyword.value.trim().toLowerCase();
  return questions.filter(q => {
    const hitExam = !examFilter.value || q.examId === examFilter.value;
    const hitType = !typeFilter.value || q.type === typeFilter.value;
    const hitKw = !kw || q.title.toLowerCase().includes(kw);
    return hitExam && hitType && hitKw;
  });
});

const { page, totalPages, paged: pagedQuestions, resetPage } = usePager(filteredQuestions, 4);
watch([examFilter, typeFilter, keyword], resetPage);

/* ---------- 弹窗表单 ---------- */
const modalOpen = ref(false);
const editingId = ref(null);
const formError = ref('');

const emptyForm = () => ({
  examId: '',
  type: 'single',
  score: 5,
  title: '',
  options: [
    { key: 'A', text: '' }, { key: 'B', text: '' },
    { key: 'C', text: '' }, { key: 'D', text: '' }
  ],
  answer: ''
});
const form = ref(emptyForm());

const needOptions = computed(() => form.value.type !== 'application');

const openAdd = () => {
  editingId.value = null;
  formError.value = '';
  form.value = emptyForm();
  modalOpen.value = true;
};

const openEdit = (q) => {
  editingId.value = q.id;
  formError.value = '';
  form.value = {
    examId: q.examId,
    type: q.type,
    score: q.score,
    title: q.title,
    options: (q.options && q.options.length) ? q.options.map(o => ({ ...o })) : emptyForm().options,
    answer: q.answer || ''
  };
  modalOpen.value = true;
};

const closeModal = () => {
  modalOpen.value = false;
  formError.value = '';
};

const switchType = () => {
  if (form.value.type === 'judgment') {
    form.value.options = [{ key: 'A', text: '正确' }, { key: 'B', text: '错误' }];
  } else if (form.value.type === 'application') {
    form.value.answer = '';
  } else if (!form.value.options.length) {
    form.value.options = emptyForm().options;
  }
};

const typeLabelMap = { single: '单选题', multiple: '多选题', judgment: '判断题', application: '问答题' };

const saveQ = () => {
  if (!form.value.examId) { formError.value = '请选择所属考试。'; return; }
  if (!form.value.title.trim()) { formError.value = '请输入题干。'; return; }
  if (needOptions.value) {
    const hasOption = form.value.options.some(o => o.text.trim());
    if (!hasOption) { formError.value = '请填写选项内容。'; return; }
    if (!form.value.answer.trim()) {
      formError.value = form.value.type === 'multiple' ? '请填写答案(如 ABD)。' : '请选择答案。';
      return;
    }
  }

  if (editingId.value) {
    const target = questions.find(q => q.id === editingId.value);
    Object.assign(target, { ...form.value, typeLabel: typeLabelMap[form.value.type] });
  } else {
    questions.unshift({
      id: `q-${Date.now()}`,
      ...form.value,
      typeLabel: typeLabelMap[form.value.type]
    });
  }
  closeModal();
};

const removeQ = (q) => {
  const exam = adminStore.exams.find(e => e.id === q.examId);
  askConfirm({
    tone: 'danger',
    title: '删除该题目？',
    subtitle: `${q.typeLabel} · ${q.score} 分`,
    rows: [
      { label: '所属考试', value: exam ? exam.title : q.examId },
      { label: '正确答案', value: q.answer }
    ],
    tip: '删除后该题目将从题库及相关考试中移除，此操作不可恢复。',
    confirmText: '确认删除',
    action: () => {
      const idx = questions.findIndex(item => item.id === q.id);
      if (idx > -1) questions.splice(idx, 1);
    }
  });
};
</script>

<style scoped>
/* ============================================================
   AdminQuestionBank · 题库管理 · 纯原生 CSS
   ============================================================ */

* { margin: 0; padding: 0; box-sizing: border-box; }
button, input, select, textarea { font-family: inherit; }
button { cursor: pointer; }

.admin-bank {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 100%;
  height: 100%;
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

/* ========== 题型统计 ========== */
.stat-strip {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 14px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
}

.stat-value {
  font-size: 22px;
  font-weight: 800;
  color: #0F1E33;
  font-variant-numeric: tabular-nums;
}
.stat-value--blue   { color: #005576; }
.stat-value--purple { color: #6555B0; }
.stat-value--gold   { color: #B45309; }
.stat-value--green  { color: #047857; }

.stat-label {
  font-size: 12px;
  color: #64748B;
}

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

.select {
  padding: 9px 12px;
  font-size: 13px;
  color: #334155;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  outline: none;
  max-width: 260px;
  transition: border-color 0.15s;
}
.select:focus { border-color: #2E8BD6; }

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 180px;
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

.toolbar-result {
  margin-left: auto;
  font-size: 12px;
  color: #94A3B8;
}

/* ========== 题目列表 ========== */
.question-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.question-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  transition: box-shadow 0.15s;
}
.question-item:hover { box-shadow: 0 4px 12px rgba(15, 30, 51, 0.07); }

.q-index {
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 800;
  color: #005576;
  background: #D9F0FF;
  border-radius: 8px;
  flex-shrink: 0;
}

.q-main {
  flex: 1;
  min-width: 0;
}

.q-top {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.q-type-chip {
  padding: 2px 8px;
  font-size: 10px;
  font-weight: 700;
  border-radius: 9999px;
}
.q-type-chip--single      { color: #005576; background: #D9F0FF; }
.q-type-chip--multiple    { color: #6555B0; background: #EDE9FE; }
.q-type-chip--judgment    { color: #B45309; background: #FEF3C7; }
.q-type-chip--application { color: #047857; background: #D1FAE5; }

.q-score {
  font-size: 11px;
  font-weight: 600;
  color: #64748B;
}

.q-exam {
  font-size: 11px;
  color: #94A3B8;
}

.q-answer {
  font-size: 11px;
  font-weight: 700;
  color: #047857;
}

.q-title {
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.6;
  color: #1E293B;
  word-break: break-all;
}

.q-actions {
  display: inline-flex;
  gap: 6px;
  flex-shrink: 0;
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

.empty-tip {
  padding: 40px 0;
  text-align: center;
  font-size: 13px;
  color: #94A3B8;
}

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
  max-width: 560px;
  max-height: 88vh;
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
  gap: 12px;
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

.mono { font-family: 'JetBrains Mono', ui-monospace, monospace; }

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
  .stat-strip { grid-template-columns: repeat(3, minmax(0, 1fr)); }
  .field-row { grid-template-columns: 1fr; }
}
</style>
