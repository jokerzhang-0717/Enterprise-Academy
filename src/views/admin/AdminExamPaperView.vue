<template>
  <div class="paper-page admin-fit-page">

    <div class="paper-head">
      <button type="button" class="back-btn" @click="goBack">
        <ArrowLeft class="back-icon" /> 返回考试管理
      </button>
      <div class="paper-head-main">
        <div>
          <h1 class="page-title">试卷组卷</h1>
          <p class="page-sub">{{ exam?.title || '加载中…' }}</p>
        </div>
        <span v-if="exam" class="status-pill" :class="`status-pill--${exam.state}`">
          {{ stateLabel }}
        </span>
      </div>
    </div>

    <div v-if="locked && exam" class="lock-banner">
      该考试已发布或已有作答记录，试卷已锁定，此处仅可查看，不再支持调整。
    </div>

    <div class="paper-layout">

      <section class="pool-panel">
        <div class="panel-head">
          <div>
            <h2 class="panel-title">题库选题</h2>
            <p class="panel-sub">点击题目加入右侧试卷，也可按当前筛选随机抽取</p>
          </div>
          <span class="pool-count">{{ filteredPool.length }} / {{ questions.length }} 道</span>
        </div>

        <div class="pool-tools">
          <div class="type-tabs" role="tablist" aria-label="按题型筛选">
            <button
              v-for="type in questionTypes"
              :key="type.value"
              type="button"
              class="type-tab"
              :class="{ 'type-tab--active': typeFilter === type.value }"
              @click="typeFilter = type.value"
            >
              {{ type.label }}
            </button>
          </div>
          <label class="search-box">
            <Search class="search-icon" />
            <input
              v-model.trim="keyword"
              type="search"
              class="search-input"
              placeholder="搜索题干关键词…"
              aria-label="搜索题干关键词"
            />
          </label>
          <div class="random-box">
            <input
              v-model.number="randomCount"
              type="number"
              min="1"
              class="random-input"
              aria-label="随机抽题数量"
            />
            <button
              type="button"
              class="btn btn--ghost btn--sm"
              :disabled="locked || remaining <= 0 || !filteredPool.length"
              @click="addRandom"
            >
              <Refresh class="btn-icon" /> 随机加入
            </button>
          </div>
        </div>

        <div class="pool-list">
          <article
            v-for="question in pagedPool"
            :key="question.numericId"
            class="pool-item"
            :class="{ 'pool-item--added': isAdded(question) }"
          >
            <div class="pool-item-tags">
              <span class="type-chip" :class="`type-chip--${question.type}`">{{ question.typeLabel }}</span>
              <span class="score-chip">{{ question.score }} 分</span>
            </div>
            <div class="pool-item-body">
              <p class="question-title">{{ question.title }}</p>
              <p v-if="optionPreview(question)" class="option-preview">{{ optionPreview(question) }}</p>
            </div>
            <button
              type="button"
              class="add-btn"
              :class="{ 'add-btn--added': isAdded(question) }"
              :disabled="locked || (!isAdded(question) && question.score > remaining)"
              :title="!isAdded(question) && question.score > remaining ? '剩余分值不足' : ''"
              @click="toggleQuestion(question)"
            >
              <Check v-if="isAdded(question)" class="btn-icon" />
              <Plus v-else class="btn-icon" />
              {{ isAdded(question) ? '已加入' : '加入' }}
            </button>
          </article>
          <p v-if="!filteredPool.length" class="empty-tip">没有匹配的题目，请调整题型或关键词。</p>
        </div>

        <AdminPager
          v-model:page="page"
          :total="filteredPool.length"
          :total-pages="totalPages"
        />
      </section>

      <aside class="selected-panel">
        <div class="panel-head selected-head">
          <div>
            <h2 class="panel-title">已选试卷</h2>
            <p class="panel-sub">按加入顺序排列 · 满分 100 分</p>
          </div>
          <button
            v-if="!locked && selectedIds.length"
            type="button"
            class="clear-btn"
            @click="clearAll"
          >清空</button>
        </div>

        <div class="score-card">
          <div class="score-line">
            <span class="score-current">{{ selectedTotal }}</span>
            <span class="score-total">/ 100 分</span>
            <span class="score-remain">剩余 {{ remaining }} 分</span>
          </div>
          <div class="score-track">
            <div
              class="score-fill"
              :class="{ 'score-fill--done': selectedTotal === 100 }"
              :style="{ width: `${Math.min(100, selectedTotal)}%` }"
            ></div>
          </div>
          <p class="score-tip">及格分固定 60 分，试卷满分需恰好 100 分才能保存</p>
        </div>

        <div class="selected-list">
          <div v-for="(question, index) in selectedQuestions" :key="question.numericId" class="selected-item">
            <span class="selected-index">{{ index + 1 }}</span>
            <div class="selected-body">
              <div class="selected-meta">
                <span class="type-chip" :class="`type-chip--${question.type}`">{{ question.typeLabel }}</span>
                <span class="score-chip">{{ question.score }} 分</span>
              </div>
              <p class="selected-title">{{ question.title }}</p>
            </div>
            <button
              type="button"
              class="remove-btn"
              :disabled="locked"
              title="移除该题"
              @click="removeQuestion(question)"
            ><Close class="btn-icon" /></button>
          </div>
          <div v-if="!selectedQuestions.length" class="selected-empty">
            <span class="selected-empty-icon">＋</span>
            <p>还没有选择题目</p>
            <span>从左侧题库点击「加入」开始组卷</span>
          </div>
        </div>

        <div class="selected-footer">
          <p v-if="validationMsg" class="form-error">{{ validationMsg }}</p>
          <button
            type="button"
            class="btn btn--primary btn--block"
            :disabled="locked || saving || selectedTotal !== 100"
            @click="savePaper"
          >
            {{ saving ? '保存中…' : (locked ? '试卷已锁定' : '保存组卷') }}
          </button>
          <button type="button" class="btn btn--ghost btn--block" @click="goBack">返回考试管理</button>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ArrowLeft, Search, Refresh, Close, Check, Plus } from '@element-plus/icons-vue';
import { adminStore, loadExams, loadQuestions } from '../../adminStore.js';
import { fetchAdminExam, updateAdminExam } from '../../api/index.js';
import { usePager } from '../../composables/usePager.js';
import { useToast } from '../../composables/useToast.js';
import AdminPager from '../../components/AdminPager.vue';

const route = useRoute();
const router = useRouter();
const { show: showToast } = useToast();

const examId = computed(() => String(route.params.examId || ''));
const exam = ref(null);
const saving = ref(false);
const selectedIds = ref([]);
const typeFilter = ref('');
const keyword = ref('');
const randomCount = ref(5);
const validationMsg = ref('');

const questions = adminStore.questions;
const questionTypes = [
  { value: '', label: '全部' },
  { value: 'single', label: '单选' },
  { value: 'multiple', label: '多选' },
  { value: 'judgment', label: '判断' },
  { value: 'application', label: '问答' },
];

const stateLabelMap = { 1: '待发布', 2: '待开始', 3: '进行中', 4: '已结束' };
const stateLabel = computed(() => stateLabelMap[exam.value?.statusCode] || '—');

const locked = computed(() =>
  !exam.value || exam.value.statusCode !== 1 || Number(exam.value.attemptCount || 0) > 0,
);

const filteredPool = computed(() => questions.filter((question) => {
  const matchesType = !typeFilter.value || question.type === typeFilter.value;
  const matchesKeyword = !keyword.value || String(question.title || '').includes(keyword.value);
  return matchesType && matchesKeyword;
}));
const { page, totalPages, paged: pagedPool, resetPage } = usePager(filteredPool, 12);
watch([typeFilter, keyword], resetPage);

const questionById = computed(() => new Map(questions.map((question) => [question.numericId, question])));
const selectedQuestions = computed(() =>
  selectedIds.value.map((id) => questionById.value.get(id)).filter(Boolean),
);
const selectedTotal = computed(() => Number(
  selectedQuestions.value.reduce((sum, question) => sum + Number(question.score || 0), 0).toFixed(2),
));
const remaining = computed(() => Number((100 - selectedTotal.value).toFixed(2)));

const isAdded = (question) => selectedIds.value.includes(question.numericId);
const optionPreview = (question) =>
  (question.options || []).slice(0, 4).map((option) => `${option.key}. ${option.text}`).join('   ');

const load = async () => {
  if (!examId.value) return;
  validationMsg.value = '';
  try {
    const [, , detail] = await Promise.all([
      loadExams(),
      loadQuestions(),
      fetchAdminExam(examId.value),
    ]);
    exam.value = detail;
    selectedIds.value = questions
      .filter((question) => String(question.examId) === examId.value)
      .map((question) => question.numericId);
  } catch (error) {
    showToast(error?.message || '加载试卷失败，请稍后重试。', 'error');
  }
};
onMounted(load);
watch(examId, load);

const toggleQuestion = (question) => {
  if (locked.value) return;
  if (isAdded(question)) {
    selectedIds.value = selectedIds.value.filter((id) => id !== question.numericId);
    validationMsg.value = '';
    return;
  }
  if (question.score > remaining.value) {
    showToast('剩余分值不足，请先移除部分题目。', 'info');
    return;
  }
  selectedIds.value = [...selectedIds.value, question.numericId];
  validationMsg.value = '';
};

const removeQuestion = (question) => {
  if (locked.value) return;
  selectedIds.value = selectedIds.value.filter((id) => id !== question.numericId);
  validationMsg.value = '';
};

const clearAll = () => {
  if (locked.value) return;
  selectedIds.value = [];
  validationMsg.value = '';
};

const addRandom = () => {
  if (locked.value) return;
  const already = new Set(selectedIds.value);
  const available = filteredPool.value.filter(
    (question) => !already.has(question.numericId) && question.score <= remaining.value,
  );
  const shuffled = [...available];
  for (let index = shuffled.length - 1; index > 0; index -= 1) {
    const swapIndex = Math.floor(Math.random() * (index + 1));
    [shuffled[index], shuffled[swapIndex]] = [shuffled[swapIndex], shuffled[index]];
  }
  const count = Math.max(0, Math.floor(Number(randomCount.value) || 0));
  let left = remaining.value;
  const picked = [];
  for (const question of shuffled) {
    if (picked.length >= count) break;
    const score = Number(question.score || 0);
    if (score <= left) {
      picked.push(question);
      left = Number((left - score).toFixed(2));
    }
  }
  if (!picked.length) {
    showToast(remaining.value <= 0 ? '试卷已满 100 分。' : '当前筛选下没有符合剩余分值的题目。', 'info');
    return;
  }
  selectedIds.value = [...selectedIds.value, ...picked.map((question) => question.numericId)];
  validationMsg.value = '';
  if (picked.length < count) {
    showToast(`已随机加入 ${picked.length} 题，剩余分值不足以再加入更多题目。`, 'info');
  }
};

const savePaper = async () => {
  if (locked.value || saving.value) return;
  if (selectedTotal.value !== 100) {
    validationMsg.value = `当前总分 ${selectedTotal.value} 分，需恰好 100 分才能保存。`;
    return;
  }
  saving.value = true;
  validationMsg.value = '';
  try {
    await updateAdminExam(examId.value, { questionIds: selectedIds.value, totalScore: 100 });
    await Promise.all([loadExams(), loadQuestions()]);
    showToast('试卷组卷已保存', 'success');
    router.push({ name: 'admin-exams' });
  } catch (error) {
    validationMsg.value = error?.message || '保存失败，请稍后重试。';
  } finally {
    saving.value = false;
  }
};

const goBack = () => router.push({ name: 'admin-exams' });
</script>

<style scoped>
* { margin: 0; padding: 0; box-sizing: border-box; }
button, input { font-family: inherit; }
button { cursor: pointer; }

.paper-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 100%;
}

.paper-head { display: flex; flex-direction: column; gap: 12px; }
.back-btn {
  align-self: flex-start;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  background: var(--app-surface-2);
  color: var(--app-text-3);
  font-size: 13px;
  transition: all .18s ease;
}
.back-btn:hover { color: var(--app-accent); border-color: var(--app-accent); }
.back-icon { width: 15px; height: 15px; }
.paper-head-main { display: flex; align-items: flex-end; justify-content: space-between; gap: 16px; }
.page-title { font-size: 24px; font-weight: 700; color: var(--app-heading); letter-spacing: -.02em; }
.page-sub { margin-top: 4px; font-size: 13px; color: var(--app-text-4); }
.status-pill { padding: 5px 12px; border-radius: 999px; font-size: 12px; font-weight: 600; white-space: nowrap; }
.status-pill--draft { color: var(--app-amber-fg); background: var(--app-amber-bg); }
.status-pill--scheduled { color: var(--app-accent-deep); background: var(--app-accent-soft); }
.status-pill--running { color: var(--app-emerald-fg); background: var(--app-emerald-bg); }
.status-pill--ended { color: var(--app-text-3); background: var(--app-surface-2); }

.lock-banner {
  padding: 12px 16px;
  border: 1px solid var(--app-amber-bg);
  border-left: 3px solid var(--app-amber-fg);
  border-radius: 8px;
  background: var(--app-amber-bg);
  color: var(--app-amber-fg);
  font-size: 13px;
}

.paper-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 370px;
  gap: 16px;
  align-items: start;
}
.pool-panel, .selected-panel {
  display: flex;
  flex-direction: column;
  min-width: 0;
  border: 1px solid var(--app-border);
  border-radius: 12px;
  background: var(--app-surface);
  overflow: hidden;
}
.selected-panel { position: sticky; top: 16px; max-height: calc(100vh - 32px); }

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 18px;
  border-bottom: 1px solid var(--app-border);
}
.panel-title { font-size: 16px; font-weight: 700; color: var(--app-heading); }
.panel-sub { margin-top: 3px; font-size: 12px; color: var(--app-text-4); }
.pool-count { font-size: 12px; color: var(--app-text-4); white-space: nowrap; }
.selected-head { align-items: center; }
.clear-btn { border: none; background: none; color: var(--app-text-4); font-size: 12px; }
.clear-btn:hover { color: var(--app-danger, #ef4444); }

.pool-tools {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  padding: 14px 18px;
  border-bottom: 1px solid var(--app-border);
}
.type-tabs { display: flex; gap: 4px; padding: 3px; border-radius: 8px; background: var(--app-surface-2); }
.type-tab {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: var(--app-text-3);
  font-size: 12px;
  transition: all .18s ease;
}
.type-tab:hover { color: var(--app-heading); }
.type-tab--active { color: var(--app-accent-deep); background: var(--app-surface); font-weight: 600; box-shadow: 0 1px 4px rgba(0,0,0,.06); }
.search-box { display: flex; align-items: center; gap: 8px; flex: 1; min-width: 180px; padding: 0 12px; border: 1px solid var(--app-border); border-radius: 8px; background: var(--app-surface-2); }
.search-icon { width: 15px; height: 15px; color: var(--app-text-4); flex: none; }
.search-input { width: 100%; height: 34px; border: none; outline: none; background: transparent; color: var(--app-text); font-size: 13px; }
.random-box { display: flex; align-items: center; gap: 8px; }
.random-input { width: 62px; height: 34px; padding: 0 8px; border: 1px solid var(--app-border); border-radius: 8px; background: var(--app-surface-2); color: var(--app-text); font-size: 13px; text-align: center; }
.btn--sm { padding: 7px 12px; font-size: 12px; }

.pool-list { display: flex; flex-direction: column; gap: 8px; padding: 14px 18px; }
.pool-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 14px;
  border: 1px solid var(--app-border);
  border-radius: 10px;
  background: var(--app-surface-2);
  transition: all .18s ease;
}
.pool-item:hover { border-color: var(--app-accent); transform: translateY(-1px); }
.pool-item--added { border-color: var(--app-emerald-fg); background: var(--app-emerald-bg); }
.pool-item-tags { display: flex; flex-direction: column; align-items: flex-start; gap: 6px; flex: none; width: 68px; }
.type-chip { display: inline-block; padding: 3px 8px; border-radius: 6px; font-size: 11px; font-weight: 600; white-space: nowrap; }
.type-chip--single { color: var(--app-accent-deep); background: var(--app-accent-soft); }
.type-chip--multiple { color: var(--app-violet-fg); background: var(--app-violet-bg); }
.type-chip--judgment { color: var(--app-amber-fg); background: var(--app-amber-bg); }
.type-chip--application { color: var(--app-emerald-fg); background: var(--app-emerald-bg); }
.score-chip { font-size: 11px; color: var(--app-text-4); }
.pool-item-body { flex: 1; min-width: 0; }
.question-title { font-size: 13px; line-height: 1.5; color: var(--app-text); display: -webkit-box; -webkit-box-orient: vertical; -webkit-line-clamp: 2; overflow: hidden; }
.option-preview { margin-top: 4px; font-size: 11px; color: var(--app-text-4); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.add-btn {
  display: inline-flex; align-items: center; gap: 4px; flex: none;
  padding: 7px 12px; border: 1px solid var(--app-accent); border-radius: 8px;
  background: var(--app-accent-soft); color: var(--app-accent-deep); font-size: 12px; font-weight: 600;
  transition: all .18s ease;
}
.add-btn:hover:not(:disabled) { background: var(--app-accent); color: #fff; }
.add-btn:disabled { opacity: .45; cursor: not-allowed; }
.add-btn--added { border-color: var(--app-emerald-fg); background: var(--app-emerald-bg); color: var(--app-emerald-fg); }
.btn-icon { width: 14px; height: 14px; }
.empty-tip { padding: 30px 0; text-align: center; color: var(--app-text-4); font-size: 13px; }

.score-card { padding: 16px 18px; border-bottom: 1px solid var(--app-border); background: var(--app-surface-2); }
.score-line { display: flex; align-items: baseline; gap: 6px; }
.score-current { font-size: 30px; font-weight: 700; color: var(--app-accent-deep); }
.score-total { font-size: 14px; color: var(--app-text-3); }
.score-remain { margin-left: auto; font-size: 12px; color: var(--app-text-4); }
.score-track { height: 8px; margin-top: 10px; border-radius: 99px; background: var(--app-border); overflow: hidden; }
.score-fill { height: 100%; border-radius: 99px; background: linear-gradient(90deg, var(--app-accent), var(--app-accent-deep)); transition: width .25s ease; }
.score-fill--done { background: linear-gradient(90deg, var(--app-emerald-fg), #34d399); }
.score-tip { margin-top: 8px; font-size: 11px; color: var(--app-text-4); }

.selected-list { display: flex; flex-direction: column; gap: 8px; padding: 14px 18px; overflow-y: auto; flex: 1; min-height: 160px; }
.selected-item { display: flex; align-items: flex-start; gap: 10px; padding: 10px 12px; border: 1px solid var(--app-border); border-radius: 8px; background: var(--app-surface-2); }
.selected-index { flex: none; width: 22px; height: 22px; display: grid; place-items: center; border-radius: 6px; background: var(--app-accent-soft); color: var(--app-accent-deep); font-size: 11px; font-weight: 700; }
.selected-body { flex: 1; min-width: 0; }
.selected-meta { display: flex; align-items: center; gap: 8px; }
.selected-title { margin-top: 5px; font-size: 12px; line-height: 1.5; color: var(--app-text); display: -webkit-box; -webkit-box-orient: vertical; -webkit-line-clamp: 2; overflow: hidden; }
.remove-btn { flex: none; width: 26px; height: 26px; display: grid; place-items: center; border: none; border-radius: 6px; background: transparent; color: var(--app-text-4); }
.remove-btn:hover:not(:disabled) { background: var(--app-danger-bg, #fee2e2); color: var(--app-danger, #ef4444); }
.remove-btn:disabled { opacity: .4; cursor: not-allowed; }
.selected-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 4px; padding: 30px 0; color: var(--app-text-4); }
.selected-empty-icon { width: 38px; height: 38px; display: grid; place-items: center; border-radius: 50%; background: var(--app-surface-2); font-size: 20px; }
.selected-empty p { font-size: 13px; color: var(--app-text-3); }
.selected-empty span { font-size: 11px; }

.selected-footer { display: flex; flex-direction: column; gap: 8px; padding: 14px 18px 18px; border-top: 1px solid var(--app-border); }
.btn { display: inline-flex; align-items: center; justify-content: center; gap: 6px; padding: 9px 16px; border-radius: 8px; font-size: 13px; font-weight: 600; border: 1px solid transparent; }
.btn--block { width: 100%; }
.btn--primary { background: var(--app-accent); color: #fff; }
.btn--primary:hover:not(:disabled) { background: var(--app-accent-deep); }
.btn--primary:disabled { opacity: .5; cursor: not-allowed; }
.btn--ghost { border-color: var(--app-border); background: transparent; color: var(--app-text-3); }
.btn--ghost:hover:not(:disabled) { border-color: var(--app-accent); color: var(--app-accent); }
.btn--ghost:disabled { opacity: .5; cursor: not-allowed; }
.form-error { color: var(--app-danger, #ef4444); font-size: 12px; line-height: 1.5; }

@media (max-width: 1100px) {
  .paper-layout { grid-template-columns: 1fr; }
  .selected-panel { position: static; max-height: none; }
}
</style>
