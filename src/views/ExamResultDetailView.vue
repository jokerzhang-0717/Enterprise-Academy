<template>
  <div class="detail-page">

    <div class="detail-page__topbar">
      <button type="button" class="back-link" @click="goBack">
        <ArrowLeft class="back-link__icon" />
        <span>返回成绩详情</span>
      </button>
      <span class="detail-page__crumb">在线考核 / 成绩详情 / 答题明细</span>
    </div>

    <div v-if="loading" class="detail-state">答题明细加载中…</div>

    <div v-else-if="loadError" class="detail-state detail-state--error">
      <span>{{ loadError }}</span>
      <button type="button" class="btn btn-secondary" @click="goBack">返回成绩详情</button>
    </div>

    <template v-else>

      <div class="detail-summary">
        <div class="detail-summary__main">
          <div class="detail-label">ANSWER REVIEW · 逐题作答明细</div>
          <h1 class="detail-title">{{ examTitle }}</h1>
          <p v-if="submittedAt" class="detail-meta">交卷时间 {{ submittedAt }}</p>
        </div>
        <div class="detail-summary__counts">
          <span class="count-chip count-chip--all">共 {{ answers.length }} 题</span>
          <span v-if="counts.correct" class="count-chip count-chip--correct">答对 {{ counts.correct }}</span>
          <span v-if="counts.wrong" class="count-chip count-chip--wrong">答错 {{ counts.wrong }}</span>
          <span v-if="counts.partial" class="count-chip count-chip--partial">部分得分 {{ counts.partial }}</span>
          <span v-if="counts.pending" class="count-chip count-chip--pending">待评分 {{ counts.pending }}</span>
        </div>
      </div>

      <div v-if="answers.length" class="filter-bar">
        <button
          v-for="tab in filterTabs"
          :key="tab.key"
          type="button"
          class="filter-btn"
          :class="{ 'filter-btn--active': filter === tab.key }"
          @click="filter = tab.key"
        >
          {{ tab.label }}
          <span class="filter-btn__num">{{ tab.count }}</span>
        </button>
      </div>

      <div v-if="filteredAnswers.length" class="answer-list">
        <article
          v-for="(entry, index) in filteredAnswers"
          :key="entry.answer.questionId ?? entry.index"
          class="answer-card"
        >
          <div class="answer-card__head">
            <span class="answer-card__no">{{ entry.answer.questionNo || entry.index + 1 }}</span>
            <span class="answer-card__title">{{ entry.answer.title || `第 ${entry.index + 1} 题` }}</span>
            <span :class="['answer-state', `answer-state--${answerState(entry.answer)}`]">
              {{ answerStateLabel(entry.answer) }}
            </span>
            <span class="answer-card__score">{{ answerScoreText(entry.answer) }}</span>
          </div>
          <div class="answer-card__body">
            <div class="answer-line">
              <span class="answer-line__label">我的作答</span>
              <span class="answer-line__value">{{ answerDisplay(entry.answer) }}</span>
            </div>
            <div v-if="!pendingReview && (entry.answer.correctAnswer || entry.answer.referenceAnswer)" class="answer-line">
              <span class="answer-line__label">{{ referenceLabel(entry.answer) }}</span>
              <span class="answer-line__value answer-line__value--ref">
                {{ entry.answer.correctAnswer || entry.answer.referenceAnswer }}
              </span>
            </div>
            <div v-if="!pendingReview && entry.answer.analysis" class="answer-line answer-line--analysis">
              <span class="answer-line__label">解析</span>
              <span class="answer-line__value">{{ entry.answer.analysis }}</span>
            </div>
          </div>
        </article>
      </div>

      <div v-else-if="answers.length" class="detail-empty">该分类下暂无题目</div>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ArrowLeft } from 'lucide-vue-next';
import { fetchAttempt } from '../api/index.js';
import { formatDateTime } from '../api/adapters.js';

const route = useRoute();
const router = useRouter();

const attemptId = String(route.params.attemptId || '');
const loading = ref(true);
const loadError = ref('');
const filter = ref('all');

const examTitle = ref('');
const submittedAt = ref('');
const pendingReview = ref(false);
const answers = ref([]);

onMounted(async () => {
  try {
    const detail = await fetchAttempt(attemptId);
    answers.value = detail?.answers || [];
    examTitle.value = detail?.examTitle || '';
    submittedAt.value = formatDateTime(detail?.submittedAt);
    pendingReview.value = Number(detail?.status) === 2;
  } catch (error) {
    loadError.value = error?.message || '答题明细加载失败，请稍后重试';
  } finally {
    loading.value = false;
  }
});

const goBack = () => router.push({ name: 'exam-result', params: { attemptId } });

const isSubjectiveType = (type) =>
  Number(type) === 4 || type === 'application' || type === 'essay';

const answerState = (answer) => {
  if (pendingReview.value && isSubjectiveType(answer.type)) return 'pending';
  const gained = Number(answer.scoreGained || 0);
  const total = Number(answer.score || 0);
  if (!total) return 'neutral';
  if (gained >= total) return 'correct';
  if (gained <= 0) return 'wrong';
  return 'partial';
};
const answerStateLabel = (answer) => ({
  pending: '待评分',
  correct: '✓ 答对',
  wrong: '✕ 答错',
  partial: '部分得分',
  neutral: '—'
}[answerState(answer)]);

const answerDisplay = (answer) => {
  const selected = String(answer.selectedKeys || '').trim();
  if (selected) {
    const letters = selected.replace(/[^A-Za-z]/g, '').split('').join('、');
    return letters || selected;
  }
  return answer.answerText || '（未作答）';
};
const referenceLabel = (answer) =>
  isSubjectiveType(answer.type) ? '参考答案' : '正确答案';
const answerScoreText = (answer) => {
  const gained = Number(answer.scoreGained || 0);
  const total = Number(answer.score || 0);
  if (pendingReview.value && isSubjectiveType(answer.type)) return '待评分';
  return `${gained} / ${total} 分`;
};

const counts = computed(() => {
  const result = { correct: 0, wrong: 0, partial: 0, pending: 0, neutral: 0 };
  for (const answer of answers.value) {
    const state = answerState(answer);
    if (result[state] !== undefined) result[state] += 1;
  }
  return result;
});
const filterTabs = computed(() => [
  { key: 'all', label: '全部', count: answers.value.length },
  { key: 'wrong', label: '错题', count: counts.value.wrong + counts.value.partial },
  { key: 'correct', label: '答对', count: counts.value.correct },
  ...(counts.value.pending ? [{ key: 'pending', label: '待评分', count: counts.value.pending }] : []),
]);
const filteredAnswers = computed(() =>
  answers.value
    .map((answer, index) => ({ answer, index }))
    .filter(({ answer }) => {
      if (filter.value === 'all') return true;
      if (filter.value === 'wrong') return ['wrong', 'partial'].includes(answerState(answer));
      return answerState(answer) === filter.value;
    })
);
</script>

<style scoped>
.detail-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
  width: 100%;
  max-width: 1040px;
  margin: 0 auto;
  padding-bottom: 48px;
}

.detail-page__topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}
.back-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border: 1px solid var(--app-border);
  border-radius: 10px;
  background: var(--app-surface);
  color: var(--app-text-2);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
}
.back-link:hover {
  background: var(--app-accent-soft);
  border-color: var(--app-accent-border);
  color: var(--app-accent-deep);
}
.back-link__icon { width: 16px; height: 16px; }
.detail-page__crumb { font-size: 12px; color: var(--app-text-5); }

.detail-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 80px 20px;
  border: 1px solid var(--app-border);
  border-radius: 16px;
  background: var(--app-surface);
  color: var(--app-text-3);
  font-size: 14px;
}
.detail-state--error { color: var(--app-rose-fg); }
.detail-empty {
  padding: 40px 20px;
  border: 1px dashed var(--app-border);
  border-radius: 12px;
  text-align: center;
  color: var(--app-text-4);
  font-size: 13px;
}

.detail-summary {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
  padding: 26px 28px;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 16px;
  box-shadow: 0 20px 48px -24px rgba(15, 23, 42, 0.24);
  animation: cardIn 0.28s ease both;
}
@keyframes cardIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: none; }
}
.detail-summary__main { min-width: 0; flex: 1 1 320px; }
.detail-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--app-accent-deep);
  letter-spacing: 0.05em;
  margin-bottom: 6px;
}
.detail-title {
  font-size: clamp(18px, 2.6vw, 24px);
  font-weight: 800;
  color: var(--app-text);
  margin-bottom: 6px;
  overflow-wrap: anywhere;
}
.detail-meta { font-size: 12px; color: var(--app-text-4); }
.detail-summary__counts { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.count-chip {
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 700;
  border-radius: 9999px;
  border: 1px solid var(--app-border);
  color: var(--app-text-2);
  background: var(--app-surface-2);
}
.count-chip--correct { color: var(--app-emerald-fg); background: var(--app-emerald-bg); border-color: var(--app-emerald-line); }
.count-chip--wrong { color: var(--app-rose-fg); background: var(--app-rose-bg); border-color: var(--app-rose-line); }
.count-chip--partial { color: var(--app-amber-fg); background: var(--app-amber-bg); border-color: var(--app-amber-line); }
.count-chip--pending { color: var(--app-accent-deep); background: var(--app-accent-soft); border-color: var(--app-accent-line); }

.filter-bar { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.filter-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  border: 1px solid var(--app-border);
  border-radius: 9999px;
  background: var(--app-surface);
  color: var(--app-text-3);
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s, color 0.15s, border-color 0.15s;
}
.filter-btn:hover { border-color: var(--app-accent-border); color: var(--app-accent-deep); }
.filter-btn--active { background: var(--app-accent); color: var(--app-on-fill); border-color: transparent; }
.filter-btn--active:hover { color: var(--app-on-fill); }
.filter-btn__num { opacity: 0.75; font-variant-numeric: tabular-nums; }

.answer-list { display: flex; flex-direction: column; gap: 12px; }
.answer-card {
  padding: 18px 20px;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 12px;
  text-align: left;
  animation: cardIn 0.28s ease both;
}
.answer-card__head { display: flex; align-items: baseline; gap: 10px; flex-wrap: wrap; }
.answer-card__no {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--app-accent-soft);
  color: var(--app-accent-deep);
  font-size: 12px;
  font-weight: 700;
}
.answer-card__title {
  flex: 1;
  min-width: 0;
  color: var(--app-text);
  font-size: 14px;
  font-weight: 600;
  overflow-wrap: anywhere;
}
.answer-card__score {
  flex-shrink: 0;
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 13px;
  color: var(--app-accent-deep);
}
.answer-state { flex-shrink: 0; font-size: 12px; font-weight: 700; white-space: nowrap; }
.answer-state--correct { color: var(--app-emerald-strong); }
.answer-state--wrong { color: var(--app-amber-strong); }
.answer-state--partial { color: #d97706; }
.answer-state--pending { color: var(--app-accent-deep); }
.answer-state--neutral { color: var(--app-text-5); }

.answer-card__body { margin-top: 12px; display: flex; flex-direction: column; gap: 8px; }
.answer-line { display: flex; align-items: baseline; gap: 10px; font-size: 13px; line-height: 1.6; }
.answer-line__label { flex-shrink: 0; color: var(--app-text-4); min-width: 64px; }
.answer-line__value { color: var(--app-text-2); word-break: break-word; }
.answer-line__value--ref { color: var(--app-emerald-strong); font-weight: 600; }
.answer-line--analysis { padding-top: 8px; border-top: 1px dashed var(--app-border); }

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 18px;
  font-size: 13px;
  font-weight: 600;
  border: 1px solid var(--app-border);
  border-radius: 12px;
  background: var(--app-surface);
  color: var(--app-text-3);
  cursor: pointer;
}
.btn:hover { background: var(--app-surface-2); }

@media (max-width: 640px) {
  .detail-summary { padding: 20px 16px; }
  .answer-card { padding: 14px 16px; }
  .answer-line { flex-direction: column; gap: 2px; }
}
</style>
