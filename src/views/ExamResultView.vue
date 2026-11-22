<template>
  <!-- 考试成绩：以大窗口（弹窗）形式呈现，不再是独立页面；底层仍是考评中心 -->
  <div class="result-overlay" @click.self="close">
    <div class="result-modal" role="dialog" aria-modal="true" aria-label="考试成绩详情">
      <!-- 窗口头部：标题 + 关闭 -->
      <div class="result-modal__head">
        <span class="result-modal__crumb">在线考核 / 成绩详情</span>
        <button type="button" class="result-modal__close" aria-label="关闭" @click="close">
          <X class="result-modal__close-icon" />
        </button>
      </div>

      <div v-if="loading" class="result-state">成绩详情加载中…</div>

      <div v-else-if="loadError" class="result-state result-state--error">
        <span>{{ loadError }}</span>
        <button type="button" class="btn btn-secondary" @click="close">关闭</button>
      </div>

      <div v-else class="result-card">
        <!-- 顶部结果图标：待评分=时钟 / 通过=奖杯 / 未通过=叉号 -->
        <div class="result-card__head">
          <div :class="['result-icon', `result-icon--${resultTheme}`]">
            <span class="result-icon__glyph">{{ resultGlyph }}</span>
          </div>
          <div class="result-card__heading">
            <div class="result-label">{{ resultLabel }}</div>
            <h1 class="result-title">{{ data.examTitle }}</h1>
            <p v-if="data.submittedAt" class="result-meta">
              第 {{ data.attemptNo || 1 }} 次作答 · 交卷时间 {{ data.submittedAt }}
            </p>
          </div>
        </div>

        <!-- 分数展示 -->
        <div class="score-panel" :class="{ 'score-panel--pending': data.pendingReview }">
          <div class="score-caption">
            {{ data.pendingReview ? '客观题机审分（最终成绩待人工评分）' : '最终成绩' }}
          </div>
          <div class="score-display">
            <span :class="['score-number', `score-number--${resultTheme}`]">{{ data.score }}</span>
            <span class="score-total">/ {{ data.totalScore }} 分</span>
          </div>
          <div class="score-status">
            <span :class="['pass-badge', `pass-badge--${resultTheme}`]">{{ statusBadge }}</span>
            <span class="pass-line">合格基准线 {{ data.passScore }} 分</span>
          </div>
        </div>

        <!-- 仅保留作答完成度；机审得分率、考位状态不再展示 -->
        <div class="stats-grid">
          <div class="stats-card">
            <span class="stats-label">作答完成度</span>
            <span class="stats-value">{{ data.answeredCount }} / {{ data.totalQuestions }} 题</span>
          </div>
        </div>

        <!-- 答题明细：跳转到独立的答题明细页面 -->
        <button
          v-if="data.answers.length"
          type="button"
          class="detail-toggle"
          @click="goDetails"
        >
          <span class="detail-toggle__left">
            <ClipboardList class="detail-toggle__icon" />
            <span class="detail-toggle__title">答题明细</span>
            <span class="detail-toggle__summary">{{ detailSummary }}</span>
          </span>
          <span class="detail-toggle__right">
            查看逐题明细
            <ArrowUpRight class="detail-toggle__arrow" />
          </span>
        </button>

        <!-- 操作按钮 -->
        <div class="action-bar">
          <!-- 还有考试机会 → 直接开始下一次作答（待评分期间也可以开始） -->
          <button
            v-if="canRetry"
            class="btn btn-retry"
            @click="retryExam"
          >
            <span class="icon-retry">↺</span>
            <span>{{ retryLabel }}</span>
          </button>

          <button class="btn btn-secondary" @click="close">
            <span>返回考评中心</span>
            <span class="icon-arrow">→</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ArrowUpRight, ClipboardList, X } from 'lucide-vue-next';
import { fetchAttempt, fetchExams } from '../api/index.js';
import { formatDateTime } from '../api/adapters.js';

const route = useRoute();
const router = useRouter();

const attemptId = String(route.params.attemptId || '');
const loading = ref(true);
const loadError = ref('');
/* null 表示尚未取得/取得失败，此时按原逻辑兜底 */
const remainingAttempts = ref(null);

const data = ref({
  examId: '',
  examTitle: '',
  score: 0,
  passScore: 60,
  totalScore: 100,
  answeredCount: 0,
  totalQuestions: 0,
  isPassed: false,
  pendingReview: false,
  attemptNo: 0,
  submittedAt: '',
  answers: [],
});

/* App.vue 在跳转前把组装好的成绩写入 sessionStorage，这里读取作为准确的满分等上下文；
   刷新或直接访问 URL 时用 fetchAttempt 兜底。 */
const storeKey = `emp_exam_result_${attemptId}`;
const readStored = () => {
  try {
    return JSON.parse(window.sessionStorage.getItem(storeKey) || 'null');
  } catch {
    return null;
  }
};

const apply = (payload) => {
  data.value = { ...data.value, ...(payload || {}) };
};

const close = () => router.push({ name: 'exams' });
/* 答题明细独立成页：整页展示逐题作答 */
const goDetails = () => router.push({ name: 'exam-result-details', params: { attemptId } });

/* 还有机会就直接开新一次作答；待评分期间同样允许（交卷已扣次数，后端 start 会放行） */
const canRetry = computed(() => {
  if (remainingAttempts.value !== null) return remainingAttempts.value > 0;
  return !data.value.pendingReview && !data.value.isPassed;
});
const retryLabel = computed(() =>
  remainingAttempts.value !== null
    ? `再考一次（剩余 ${remainingAttempts.value} 次机会）`
    : '重新作答'
);
const retryExam = () => {
  if (!data.value.examId) {
    router.push({ name: 'exams' });
    return;
  }
  router.push({ name: 'exam-taking-by-id', params: { examId: data.value.examId } });
};

/* 关闭窗口时清理滚动锁与 Esc 监听 */
const onKeydown = (event) => {
  if (event.key === 'Escape') close();
};
onMounted(async () => {
  document.addEventListener('keydown', onKeydown);
  document.body.style.overflow = 'hidden';

  const stored = readStored();
  if (stored) apply(stored);

  try {
    const detail = await fetchAttempt(attemptId);
    const answers = detail?.answers || [];
    const status = Number(detail?.status ?? 0);
    const merged = {
      examId: String(detail?.examId ?? stored?.examId ?? ''),
      examTitle: detail?.examTitle || stored?.examTitle || '',
      attemptNo: Number(detail?.attemptNo ?? stored?.attemptNo ?? 1),
      submittedAt: stored?.submittedAt || formatDateTime(detail?.submittedAt),
      status,
      // 以服务端最新状态为准：待评分被人工评分后刷新即可看到最终成绩
      pendingReview: status === 2,
      isPassed: Boolean(detail?.isPassed),
      score: Number(detail?.totalScore ?? stored?.score ?? 0),
      passScore: Number(detail?.passScore ?? stored?.passScore ?? 60),
      answers,
    };
    // 满分/题数：优先用跳转时带入的试卷上下文，缺失时按答案累加/计数兜底
    merged.totalScore = Number(stored?.totalScore || 0)
      || answers.reduce((sum, a) => sum + Number(a.score || 0), 0)
      || 100;
    merged.totalQuestions = Number(stored?.totalQuestions || 0) || answers.length;
    merged.answeredCount = Number(stored?.answeredCount ?? answers.length);
    apply(merged);
  } catch (error) {
    if (!stored) loadError.value = error?.message || '成绩详情加载失败，请稍后重试';
  } finally {
    loading.value = false;
  }

  /* 读取剩余考试机会：有待评分记录也要能直接开始下一次作答 */
  if (data.value.examId) {
    try {
      const examsData = await fetchExams();
      const assignment = (examsData?.assignments || []).find(
        (item) => String(item.examId) === String(data.value.examId)
      );
      if (assignment) remainingAttempts.value = Number(assignment.remainingAttempts ?? 0);
    } catch {
      /* 机会数获取失败时退回原逻辑，不影响成绩展示 */
    }
  }
});
onUnmounted(() => {
  document.removeEventListener('keydown', onKeydown);
  document.body.style.overflow = '';
});

const isSubjectiveType = (type) =>
  Number(type) === 4 || type === 'application' || type === 'essay';

/* 统一主题：待评分 / 通过 / 未通过 */
const resultTheme = computed(() => {
  if (data.value.pendingReview) return 'pending';
  return data.value.isPassed ? 'pass' : 'fail';
});
const resultGlyph = computed(() => {
  if (data.value.pendingReview) return '⏳';
  return data.value.isPassed ? '🏆' : '✕';
});
const resultLabel = computed(() =>
  data.value.pendingReview
    ? 'EXAM RESULT · 待人工评分'
    : 'EXAM RESULT · 即时测评成绩单'
);
const statusBadge = computed(() => {
  if (data.value.pendingReview) return '客观题已机审 · 主观题待评分';
  return data.value.isPassed ? '★ 考核优秀通过' : '未达到合格基准线';
});
/* 每题的判定状态（仅用于明细入口的对错概要统计） */
const answerState = (answer) => {
  if (data.value.pendingReview && isSubjectiveType(answer.type)) return 'pending';
  const gained = Number(answer.scoreGained || 0);
  const total = Number(answer.score || 0);
  if (!total) return 'neutral';
  if (gained >= total) return 'correct';
  if (gained <= 0) return 'wrong';
  return 'partial';
};

/* 只给一个概要，具体逐题数据跳转到独立的答题明细页查看 */
const detailSummary = computed(() => {
  const counts = { correct: 0, wrong: 0, partial: 0, pending: 0 };
  for (const answer of data.value.answers) {
    const state = answerState(answer);
    if (counts[state] !== undefined) counts[state] += 1;
  }
  const parts = [];
  if (counts.correct) parts.push(`答对 ${counts.correct}`);
  if (counts.wrong) parts.push(`答错 ${counts.wrong}`);
  if (counts.partial) parts.push(`部分得分 ${counts.partial}`);
  if (counts.pending) parts.push(`待评分 ${counts.pending}`);
  return parts.length ? parts.join(' · ') : `共 ${data.value.answers.length} 题`;
});
</script>

<style scoped>
/* ============ 大窗口（弹窗）外壳 ============ */
.result-overlay {
  position: fixed;
  inset: 0;
  z-index: 250;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: clamp(16px, 4vh, 48px) 16px;
  overflow-y: auto;
  background: rgba(15, 23, 42, 0.58);
  backdrop-filter: blur(3px);
  animation: overlayIn 0.18s ease both;
}
@keyframes overlayIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
.result-modal {
  width: min(100%, 1000px);
  margin: auto;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 18px;
  box-shadow: 0 32px 80px -24px rgba(15, 23, 42, 0.55);
  overflow: hidden;
  animation: modalIn 0.24s cubic-bezier(0.22, 1, 0.36, 1) both;
}
@keyframes modalIn {
  from { opacity: 0; transform: translateY(16px) scale(0.985); }
  to { opacity: 1; transform: none; }
}
.result-modal__head {
  position: sticky;
  top: 0;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 22px;
  border-bottom: 1px solid var(--app-border);
  background: var(--app-surface);
}
.result-modal__crumb { font-size: 12px; color: var(--app-text-4); }
.result-modal__close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  background: var(--app-surface-2);
  color: var(--app-text-3);
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}
.result-modal__close:hover {
  background: var(--app-rose-bg);
  color: var(--app-rose-fg);
  border-color: var(--app-rose-line);
}
.result-modal__close-icon { width: 18px; height: 18px; }

/* ============ 加载 / 错误态 ============ */
.result-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 80px 20px;
  color: var(--app-text-3);
  font-size: 14px;
}
.result-state--error { color: var(--app-rose-fg); }

/* ============ 成绩卡片 ============ */
.result-card {
  padding: 32px clamp(20px, 5vw, 48px) 36px;
  animation: cardIn 0.28s ease both;
}
@keyframes cardIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: none; }
}
.result-card__head {
  display: flex;
  align-items: center;
  gap: 20px;
  text-align: left;
}
.result-card__heading { min-width: 0; flex: 1; }

/* 顶部结果图标 */
.result-icon {
  width: 80px;
  height: 80px;
  flex-shrink: 0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.result-icon__glyph { font-size: 38px; line-height: 1; }
.result-icon--pass { background: var(--app-emerald-bg); color: var(--app-emerald-strong); }
.result-icon--fail { background: var(--app-amber-bg); color: var(--app-amber-strong); }
.result-icon--pending { background: var(--app-accent-soft); color: var(--app-accent-deep); }

.result-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--app-accent-deep);
  letter-spacing: 0.05em;
  margin-bottom: 6px;
}
.result-title {
  font-size: clamp(20px, 3vw, 28px);
  font-weight: 800;
  color: var(--app-text);
  margin-bottom: 8px;
  overflow-wrap: anywhere;
}
.result-meta { font-size: 12px; color: var(--app-text-4); }

/* ============ 分数面板 ============ */
.score-panel {
  margin: 28px 0 24px;
  padding: 28px;
  background: var(--app-page-bg);
  border: 1px solid var(--app-border);
  border-radius: 14px;
  text-align: center;
}
.score-panel--pending {
  background: var(--app-accent-soft);
  border-color: var(--app-accent-line);
}
.score-caption { font-size: 12px; font-weight: 600; color: var(--app-text-4); margin-bottom: 8px; }
.score-panel--pending .score-caption { color: var(--app-accent-deep); }
.score-display {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 6px;
  font-family: "SFMono-Regular", Consolas, Menlo, monospace;
}
.score-number { font-size: 72px; font-weight: 900; line-height: 1; }
.score-number--pass { color: var(--app-emerald-strong); }
.score-number--fail { color: var(--app-amber-strong); }
.score-number--pending { color: var(--app-accent-deep); }
.score-total { font-size: 22px; color: var(--app-text-4); }
.score-status {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
}
.pass-badge { padding: 3px 12px; font-size: 13px; font-weight: 700; border-radius: 9999px; }
.pass-badge--pass { color: var(--app-emerald-fg); background: var(--app-emerald-soft); border: 1px solid var(--app-emerald-line); }
.pass-badge--fail { color: var(--app-amber-fg); background: var(--app-amber-50); }
.pass-badge--pending { color: var(--app-accent-deep); background: var(--app-surface); border: 1px solid var(--app-accent-line); }
.pass-line { font-size: 12px; color: var(--app-text-4); }

/* ============ 数据统计 ============ */
.stats-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
  margin-bottom: 24px;
}
.stats-card {
  padding: 14px 16px;
  background: var(--app-surface-2);
  border: 1px solid var(--app-border);
  border-radius: 10px;
  text-align: left;
}
.stats-label { display: block; font-size: 11px; color: var(--app-text-4); margin-bottom: 4px; }
.stats-value { font-size: 14px; font-weight: 700; color: var(--app-text); }
.stats-value--amber { color: var(--app-amber-strong); }

/* ============ 答题明细入口 ============ */
.detail-toggle {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
  margin-bottom: 20px;
  padding: 14px 16px;
  border: 1px solid var(--app-border);
  border-radius: 10px;
  background: var(--app-surface-2);
  color: var(--app-text);
  text-align: left;
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s;
}
.detail-toggle:hover { border-color: var(--app-accent-border); background: var(--app-accent-soft); }
.detail-toggle__left { display: flex; align-items: center; gap: 10px; min-width: 0; flex-wrap: wrap; }
.detail-toggle__icon { width: 18px; height: 18px; flex-shrink: 0; color: var(--app-accent-deep); }
.detail-toggle__title { font-size: 14px; font-weight: 700; }
.detail-toggle__summary { font-size: 12px; color: var(--app-text-4); }
.detail-toggle__right {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
  font-size: 12px;
  font-weight: 600;
  color: var(--app-accent-deep);
}
.detail-toggle__arrow { width: 16px; height: 16px; }

/* ============ 操作按钮 ============ */
.action-bar { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 22px;
  font-size: 13px;
  font-weight: 600;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}
.btn-retry { flex: 1; background: var(--app-accent); color: var(--app-on-accent-solid); }
.btn-retry:hover { background: var(--app-accent-hover-strong); }
.btn-secondary { background: var(--app-surface); color: var(--app-text-3); border: 1px solid var(--app-border); }
.btn-secondary:hover { background: var(--app-surface-2); }
.icon-retry, .icon-arrow { font-size: 15px; line-height: 1; }

@media (max-width: 640px) {
  .result-card { padding: 24px 16px; }
  .result-card__head { flex-direction: column; align-items: flex-start; gap: 12px; }
  .stats-grid { grid-template-columns: 1fr; }
  .score-number { font-size: 56px; }
}
</style>
