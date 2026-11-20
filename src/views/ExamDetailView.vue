<template>
  <div class="exd-page">
    <button type="button" class="exd-back" @click="$emit('back')">
      <ArrowLeft class="exd-icon exd-icon--sm" />
      <span>返回考试中心</span>
    </button>

    <div v-if="loading" class="exd-state">正在加载考核详情…</div>

    <div v-else-if="loadError" class="exd-state exd-state--error">
      <p>{{ loadError }}</p>
      <button type="button" class="exd-btn exd-btn--primary" @click="loadDetail">重新加载</button>
    </div>

    <template v-else>
      <!-- 头部：标题与状态 -->
      <section class="exd-hero">
        <span class="exd-badge" :class="statusClass">{{ statusText }}</span>
        <h1 class="exd-hero__title">{{ exam.title }}</h1>
        <p class="exd-hero__sub">
          <template v-if="exam.code">考试编号 {{ exam.code }}</template>
          <template v-if="exam.code && exam.organizer"> · </template>
          <template v-if="exam.organizer">主办部门 {{ exam.organizer }}</template>
        </p>
      </section>

      <!-- 考过试：直接展示成绩；没考过：显示考试入口 -->
      <section
        v-if="hasAttempt"
        class="exd-result"
        role="button"
        tabindex="0"
        @click="$emit('view-result', pastRecord)"
        @keydown.enter.prevent="$emit('view-result', pastRecord)"
      >
        <div class="exd-result__head">
          <span class="exd-result__label">我的考试成绩</span>
          <span class="exd-result__status" :class="resultStatusClass">{{ resultLabel }}</span>
        </div>
        <div class="exd-result__score">
          <strong>{{ resultScore }}</strong>
          <span v-if="pastRecord.pendingReview">
            满分 {{ pastRecord.totalScore || exam.totalScore }} 分 · 主观题复核中
          </span>
          <span v-else>/ {{ pastRecord.totalScore || exam.totalScore }} 分</span>
        </div>
        <div class="exd-result__foot">
          <span>第 {{ pastRecord.attemptNo || 1 }} 次 · {{ pastRecord.completionDate }}</span>
          <span class="exd-result__link">点击查看成绩详情</span>
        </div>
      </section>

      <section v-else class="exd-noattempt">
        <div class="exd-noattempt__text">
          <div class="exd-noattempt__title">{{ canStart ? '尚未参加本场考核' : '本场考核暂未开放' }}</div>
          <div class="exd-noattempt__sub">
            {{ canStart ? '点击下方按钮即可进入在线作答' : '请在考试开放时间内进入作答' }}
          </div>
        </div>
        <button
          v-if="canStart"
          type="button"
          class="exd-btn exd-btn--primary"
          @click="$emit('start-exam', exam)"
        >
          <FileCheck2 class="exd-icon exd-icon--sm" />
          <span>开始考试</span>
        </button>
        <button v-else type="button" class="exd-btn exd-btn--disabled" disabled>
          <Clock class="exd-icon exd-icon--sm" />
          <span>暂未开放</span>
        </button>
      </section>

      <div class="exd-grid">
        <!-- 基本信息 -->
        <section class="exd-card">
          <h2 class="exd-card__title">考核信息</h2>
          <dl class="exd-info">
            <div class="exd-info__row">
              <dt>考试时间</dt>
              <dd>{{ exam.timeWindow || '另行通知' }}</dd>
            </div>
            <div class="exd-info__row">
              <dt>考试时长</dt>
              <dd>{{ exam.durationMinutes }} 分钟</dd>
            </div>
            <div class="exd-info__row">
              <dt>试卷满分</dt>
              <dd>{{ exam.totalScore }} 分</dd>
            </div>
            <div class="exd-info__row">
              <dt>及格分数</dt>
              <dd>{{ exam.passScore || '—' }} 分</dd>
            </div>
            <div class="exd-info__row">
              <dt>题目数量</dt>
              <dd>{{ exam.questionCount }} 题</dd>
            </div>
            <div v-if="exam.location" class="exd-info__row">
              <dt>考试地点</dt>
              <dd>{{ exam.location }}</dd>
            </div>
          </dl>
        </section>

        <!-- 题型分布 -->
        <section class="exd-card">
          <h2 class="exd-card__title">题型分布</h2>
          <div v-if="typeGroups.length" class="exd-types">
            <div v-for="group in typeGroups" :key="group.label" class="exd-types__row">
              <span>{{ group.label }}</span>
              <strong>{{ group.count }} 题 · {{ group.score }} 分</strong>
            </div>
          </div>
          <p v-else class="exd-empty">本场考核暂未编排题目</p>
        </section>

        <!-- 考试须知 -->
        <section class="exd-card exd-card--wide">
          <h2 class="exd-card__title">考试须知</h2>
          <ol class="exd-rules">
            <li>请在规定时间内进入作答，考试开始后系统按试卷时长倒计时。</li>
            <li>答题过程中会自动保存作答进度，可标记题目稍后复查。</li>
            <li>客观题交卷后自动判分；应用题需人工复核，复核完成后更新最终成绩。</li>
            <li>成绩达到及格分数视为通过，可在历史成绩中查看答题明细。</li>
          </ol>
        </section>
      </div>

      <!-- 已考过仍可重考时，保留次要入口；首次考试入口在上方 -->
      <div v-if="hasAttempt && canStart" class="exd-actions">
        <button
          type="button"
          class="exd-btn exd-btn--ghost"
          @click="$emit('start-exam', exam)"
        >
          <FileCheck2 class="exd-icon exd-icon--sm" />
          <span>{{ startLabel }}</span>
        </button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { ArrowLeft, FileCheck2, Clock } from 'lucide-vue-next';
import { fetchExamPaper } from '../api/index.js';

const props = defineProps({
  examId: { type: [String, Number], default: '' },
  assignment: { type: Object, default: null },
  pastRecord: { type: Object, default: null },
});

defineEmits(['back', 'start-exam', 'view-result']);

const paper = ref(null);
const loading = ref(true);
const loadError = ref('');

const loadDetail = async () => {
  loading.value = true;
  loadError.value = '';
  try {
    paper.value = await fetchExamPaper(props.examId);
  } catch (error) {
    loadError.value = error?.message || '考核详情加载失败，请稍后重试';
  } finally {
    loading.value = false;
  }
};

onMounted(loadDetail);

const exam = computed(() => {
  const p = paper.value || {};
  const a = props.assignment || {};
  const questions = p.questions || [];
  return {
    ...a,
    ...p,
    examId: props.examId,
    title: p.title || a.title || '在线考核',
    code: p.code || a.code || '',
    organizer: p.organizer || a.organizer || '',
    timeWindow: p.timeWindow || a.timeWindow || '',
    location: a.location || '',
    durationMinutes: Number(p.durationMinutes ?? a.durationMinutes ?? 0),
    totalScore: Number(p.totalScore ?? a.totalScore ?? 100),
    passScore: Number(p.passScore ?? a.passScore ?? 0),
    questionCount: Number(p.questionCount ?? a.questionCount ?? questions.length ?? 0),
    status: a.status || '',
    attemptsUsed: Number(a.attemptsUsed ?? 0),
  };
});

const statusText = computed(() => {
  const map = {
    in_progress: '进行中 · 可作答',
    not_started: '未开始',
    completed: '已完成',
  };
  return map[exam.value.status] || paper.value?.status || '考核详情';
});

const statusClass = computed(() => {
  const status = exam.value.status;
  if (status === 'in_progress') return 'exd-badge--live';
  if (status === 'completed') return 'exd-badge--done';
  return 'exd-badge--wait';
});

const canStart = computed(() => {
  const status = exam.value.status;
  if (status === 'not_started' || status === 'completed') return false;
  return true;
});

const startLabel = computed(() =>
  Number(exam.value.attemptsUsed) > 0 ? '重新作答' : '开始考试');

/* 有成绩就展示成绩，没考过才展示考试入口 */
const hasAttempt = computed(() => props.pastRecord?.attemptId != null);
const resultScore = computed(() =>
  props.pastRecord?.pendingReview ? '待评分' : Number(props.pastRecord?.score ?? 0));
const resultLabel = computed(() => {
  const status = String(props.pastRecord?.status || '');
  if (status === 'pending_review') return '待人工评分';
  if (status.startsWith('passed')) return '已通过';
  if (status === 'failed') return '未通过';
  return props.pastRecord?.statusLabel || '已参加';
});
const resultStatusClass = computed(() => {
  const status = String(props.pastRecord?.status || '');
  if (status === 'pending_review') return 'exd-result__status--pending';
  if (status.startsWith('passed')) return 'exd-result__status--passed';
  if (status === 'failed') return 'exd-result__status--failed';
  return '';
});

const typeGroups = computed(() => {
  const groups = new Map();
  for (const question of paper.value?.questions || []) {
    const key = question.type || 'other';
    const group = groups.get(key) || { label: question.typeLabel || '其他', count: 0, score: 0 };
    group.count += 1;
    group.score += Number(question.score || 0);
    groups.set(key, group);
  }
  return Array.from(groups.values());
});
</script>

<style scoped>
.exd-page {
  --c-text: var(--app-text);
  --c-sub: var(--app-text-2);
  --c-muted: var(--app-text-3);
  --c-faint: var(--app-text-4);
  --c-border: var(--app-border);
  --c-blue: var(--app-accent-deep);
  --c-blue-bg: var(--app-accent-soft);
  --c-sky: var(--app-accent);
  --c-sky-hover: var(--app-accent-hover-strong);

  display: flex;
  flex-direction: column;
  gap: 20px;
  padding-bottom: 48px;
  font-family: 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', sans-serif;
}
.exd-icon { width: 20px; height: 20px; flex-shrink: 0; }
.exd-icon--sm { width: 16px; height: 16px; }

.exd-back {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  align-self: flex-start;
  padding: 6px 12px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
  background: var(--app-surface);
  color: var(--c-sub);
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}
.exd-back:hover { border-color: var(--c-sky); color: var(--c-blue); }

.exd-state {
  padding: 48px 24px;
  border: 1px dashed var(--c-border);
  border-radius: 14px;
  background: var(--app-surface);
  color: var(--c-muted);
  font-size: 13px;
  text-align: center;
}
.exd-state--error { display: flex; flex-direction: column; align-items: center; gap: 16px; }

.exd-hero {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
  padding: 24px;
  border: 1px solid var(--c-border);
  border-radius: 16px;
  background: var(--app-surface);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}
.exd-badge {
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
}
.exd-badge--live { background: var(--c-blue-bg); color: var(--c-blue); }
.exd-badge--wait { background: var(--app-surface-3); color: var(--c-muted); }
.exd-badge--done { background: var(--app-emerald-soft); color: var(--app-emerald-fg); }

.exd-hero__title {
  margin: 0;
  font-size: 22px;
  line-height: 1.4;
  font-weight: 700;
  color: var(--c-text);
}
.exd-hero__sub {
  margin: 0;
  font-size: 12px;
  color: var(--c-muted);
}

/* 考过试：成绩概览（整块可点击查看明细） */
.exd-result {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px 24px;
  border: 1px solid var(--c-border);
  border-left: 4px solid var(--c-sky);
  border-radius: 14px;
  background: var(--app-surface);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
  cursor: pointer;
}
.exd-result:hover { border-color: var(--c-sky); background: var(--c-blue-bg); }
.exd-result:focus-visible { outline: 2px solid var(--c-sky); outline-offset: 2px; }
.exd-result__head { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.exd-result__label { font-size: 12px; font-weight: 600; color: var(--c-muted); }
.exd-result__status {
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  background: var(--app-surface-3);
  color: var(--c-muted);
}
.exd-result__status--passed { background: var(--app-emerald-soft); color: var(--app-emerald-fg); }
.exd-result__status--failed,
.exd-result__status--pending { background: var(--app-amber-50, var(--app-surface-3)); color: var(--app-amber-strong, var(--c-muted)); }
.exd-result__score { display: flex; align-items: baseline; gap: 6px; font-family: 'SFMono-Regular', Consolas, monospace; }
.exd-result__score strong { font-size: 32px; color: var(--c-text); }
.exd-result__score span { font-size: 13px; color: var(--c-muted); }
.exd-result__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-size: 11px;
  color: var(--c-faint);
}
.exd-result__link { color: var(--c-blue); font-weight: 600; }

/* 没考过试：考试入口 */
.exd-noattempt {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 24px;
  border: 1px solid var(--c-border);
  border-radius: 14px;
  background: var(--app-surface);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}
@media (min-width: 768px) {
  .exd-noattempt { flex-direction: row; align-items: center; }
}
.exd-noattempt__title { font-size: 15px; font-weight: 700; color: var(--c-text); }
.exd-noattempt__sub { margin-top: 4px; font-size: 12px; color: var(--c-muted); }

.exd-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
}
@media (min-width: 900px) {
  .exd-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .exd-card--wide { grid-column: 1 / -1; }
}
.exd-card {
  padding: 20px 24px;
  border: 1px solid var(--c-border);
  border-radius: 14px;
  background: var(--app-surface);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}
.exd-card__title {
  margin: 0 0 16px;
  font-size: 14px;
  font-weight: 700;
  color: var(--c-text);
}
.exd-info { margin: 0; display: flex; flex-direction: column; gap: 12px; }
.exd-info__row { display: flex; align-items: baseline; justify-content: space-between; gap: 16px; }
.exd-info__row dt { font-size: 12px; color: var(--c-muted); flex-shrink: 0; }
.exd-info__row dd {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--c-sub);
  text-align: right;
}
.exd-types { display: flex; flex-direction: column; gap: 12px; }
.exd-types__row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 16px;
  font-size: 13px;
  color: var(--c-sub);
}
.exd-types__row strong {
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 12px;
  color: var(--c-blue);
}
.exd-rules {
  margin: 0;
  padding-left: 20px;
  color: var(--c-sub);
  font-size: 12px;
  line-height: 1.9;
}
.exd-empty { margin: 0; font-size: 12px; color: var(--c-faint); }

.exd-actions { display: flex; flex-wrap: wrap; gap: 12px; }
.exd-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 24px;
  border: 0;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}
.exd-btn--primary {
  background: var(--c-sky);
  color: var(--app-on-fill);
  box-shadow: 0 6px 14px rgba(15, 23, 42, 0.12);
}
.exd-btn--primary:hover { background: var(--c-sky-hover); }
.exd-btn--ghost {
  border: 1px solid var(--c-border);
  background: var(--app-surface);
  color: var(--c-blue);
}
.exd-btn--ghost:hover { border-color: var(--c-sky); background: var(--c-blue-bg); }
.exd-btn--disabled {
  padding: 10px 20px;
  border: 1px solid var(--c-border);
  background: var(--app-surface-3);
  color: var(--c-muted);
  cursor: not-allowed;
}
</style>
