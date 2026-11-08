<template>
  <div class="exam-page">
    <!-- 1. Top Navigation Bar -->
    <header class="exam-topbar">
      <!-- 左侧：考场信息 -->
      <div class="exam-topbar__brand">
        <div class="exam-brand__logo">考</div>
        <div>
          <div class="exam-brand__title-row">
            <span class="exam-brand__title">{{ exam ? exam.title : '在线考核' }} · 统一考场</span>
            <span class="exam-live-badge">作弊监测运行中</span>
          </div>
          <div class="exam-brand__meta">
            考生: {{ user.name }} (工号: {{ user.employeeId }}) · {{ user.department }}
          </div>
        </div>
      </div>

      <!-- 右侧：倒计时 + 操作 -->
      <div class="exam-topbar__tools">
        <div class="exam-timer">
          <div class="exam-timer__icon">
            <Clock class="exam-icon exam-icon--sm" />
          </div>
          <div>
            <div class="exam-timer__label">剩余答题时间</div>
            <div class="exam-timer__value">{{ formattedTimer }}</div>
          </div>
        </div>

        <button type="button" class="exam-btn exam-btn--ghost" @click="showRulesHint">
          <HelpCircle class="exam-icon exam-icon--xs" />
          <span>考规帮助</span>
        </button>

        <button type="button" class="exam-btn exam-btn--sky" @click="openSubmitConfirm">
          <Award class="exam-icon exam-icon--xs" />
          <span>提前交卷</span>
        </button>
      </div>
    </header>

    <!-- 2. Main Split: Question (left) + Answer Sheet (right) -->
    <div class="exam-layout">
      <!-- ===== Left: Question Canvas ===== -->
      <div class="exam-main">
        <div v-if="pageError" class="exam-panel exam-state">
          <h3 class="exam-state__title">试卷加载失败</h3>
          <p class="exam-state__text">{{ pageError }}</p>
          <div class="exam-state__actions">
            <button type="button" class="exam-btn exam-btn--sky" @click="loadSession">重新加载</button>
            <button type="button" class="exam-btn exam-btn--prev" @click="$emit('back')">
              返回考试中心
            </button>
          </div>
        </div>

        <div v-else-if="loading || !currentQuestion" class="exam-panel exam-state">
          <p class="exam-state__text">正在加载试卷与断点续答进度…</p>
        </div>

        <div v-else class="exam-panel">
          <!-- 题目元信息栏 -->
          <div class="exam-qmeta">
            <div class="exam-qmeta__left">
              <span class="exam-chip">第 {{ currentQuestionIdx }} 题</span>
              <span class="exam-qmeta__muted">共 {{ totalQuestions }} 题</span>
              <span>·</span>
              <span class="exam-chip exam-chip--type">{{ currentQuestion.typeLabel }}</span>
              <span>·</span>
              <span class="exam-qmeta__muted">
                本题分值: <strong>{{ currentQuestion.score }} 分</strong>
              </span>
            </div>

            <!-- 标记复查 -->
            <button
              type="button"
              class="exam-flag-btn"
              :class="isFlagged ? 'is-flagged' : ''"
              @click="toggleFlag"
            >
              <Flag class="exam-icon exam-icon--xs" />
              <span>{{ isFlagged ? '已标记复查' : '标记复查' }}</span>
            </button>
          </div>

          <!-- 题干 -->
          <h3 class="exam-question__title">
            Q{{ currentQuestionIdx }}. {{ currentQuestion.title }}
          </h3>

          <!-- 应用题：文本作答 -->
          <div v-if="currentQuestion.type === 'application'" class="exam-answer">
            <label :for="`answer-${currentQuestionIdx}`" class="exam-answer__label">
              请在下方填写作答内容
            </label>
            <textarea
              :id="`answer-${currentQuestionIdx}`"
              :value="currentAnswer || ''"
              rows="6"
              maxlength="2000"
              placeholder="请结合题目说明你的分析与解决方案"
              class="exam-textarea"
              @input="handleApplicationInput($event.target.value)"
            />
            <p class="exam-answer__count">{{ (currentAnswer || '').length }} / 2000</p>
          </div>

          <!-- 选择题：选项列表 -->
          <div v-else class="exam-options">
            <div
              v-for="opt in currentQuestion.options"
              :key="opt.key"
              class="exam-option"
              :class="selectedOptions.includes(opt.key) ? 'is-selected' : ''"
              @click="handleSelectOption(opt.key)"
            >
              <!-- 选择指示器（单选圆 / 多选方） -->
              <div
                class="exam-option__indicator"
                :class="[
                  currentQuestion.type === 'multiple' ? 'is-square' : 'is-circle',
                  selectedOptions.includes(opt.key) ? 'is-checked' : ''
                ]"
              >
                <span
                  v-if="selectedOptions.includes(opt.key)"
                  class="exam-option__dot"
                  :class="currentQuestion.type === 'multiple' ? 'is-square' : 'is-circle'"
                />
              </div>

              <div class="exam-option__body">
                <div class="exam-option__text">
                  <strong class="exam-option__key">{{ opt.key }}.</strong>
                  <span>{{ opt.text }}</span>
                </div>
                <div
                  v-if="opt.explanation"
                  class="exam-option__explanation"
                  :class="selectedOptions.includes(opt.key) ? 'is-highlight' : ''"
                >
                  {{ opt.explanation }}
                </div>
              </div>
            </div>
          </div>

          <!-- 底部导航按钮 -->
          <div class="exam-nav">
            <button
              type="button"
              class="exam-btn exam-btn--prev"
              :disabled="currentQuestionIdx === 1"
              @click="handlePrevQuestion"
            >
              <ArrowLeft class="exam-icon exam-icon--xs" />
              <span>上一题 (Q{{ Math.max(1, currentQuestionIdx - 1) }})</span>
            </button>

            <button type="button" class="exam-btn exam-btn--clear" @click="clearCurrentSelection">
              清除本题选择
            </button>

            <button
              type="button"
              class="exam-btn exam-btn--next"
              :disabled="submitting"
              @click="currentQuestionIdx === totalQuestions ? openSubmitConfirm() : handleNextQuestion()"
            >
              <template v-if="currentQuestionIdx === totalQuestions">
                <span>提交试卷</span>
                <Award class="exam-icon exam-icon--xs" />
              </template>
              <template v-else>
                <span>保存并前往下一题 (Q{{ currentQuestionIdx + 1 }})</span>
                <ArrowRight class="exam-icon exam-icon--xs" />
              </template>
            </button>
          </div>
        </div>

        <div v-if="saveError" class="exam-save-error">{{ saveError }}</div>

        <!-- 安全节点提示 -->
        <div class="exam-secure">
          <div class="exam-secure__left">
            <span class="exam-secure__dot" />
            <span>当前会话采用企业硬件密钥隔离保护，试题内容实时加密同步，网络瞬断具备断点续答能力</span>
          </div>
          <span class="exam-secure__node">NODE: AP-SH-POD-09</span>
        </div>
      </div>

      <!-- ===== Right Sidebar ===== -->
      <aside v-if="!pageError" class="exam-side">
        <!-- 答题进度概览 -->
        <div class="exam-panel exam-side__panel">
          <div class="exam-progress__head">
            <h3 class="exam-side__title">答题进度概览</h3>
            <div class="exam-progress__percent">
              <span class="exam-progress__num">{{ progressPercent }}%</span>
              <span class="exam-progress__label">已完成进度</span>
            </div>
          </div>

          <!-- 进度条 -->
          <div class="exam-progress__track">
            <div class="exam-progress__bar" :style="{ width: `${progressPercent}%` }" />
          </div>

          <!-- 统计三宫格 -->
          <div class="exam-progress__stats">
            <div class="exam-stat-box exam-stat-box--answered">
              <span class="exam-stat-box__num">{{ answeredCount }}</span>
              <span class="exam-stat-box__label">已作答</span>
            </div>
            <div class="exam-stat-box exam-stat-box--blank">
              <span class="exam-stat-box__num">{{ totalQuestions - answeredCount }}</span>
              <span class="exam-stat-box__label">未作答</span>
            </div>
            <div class="exam-stat-box exam-stat-box--flagged">
              <span class="exam-stat-box__num">{{ flaggedCount }}</span>
              <span class="exam-stat-box__label">标记复查</span>
            </div>
          </div>

          <!-- 图例 -->
          <div class="exam-legend">
            <span class="exam-legend__item">
              <span class="exam-legend__swatch exam-legend__swatch--answered" />
              已答
            </span>
            <span class="exam-legend__item">
              <span class="exam-legend__swatch exam-legend__swatch--blank" />
              未答
            </span>
            <span class="exam-legend__item">
              <span class="exam-legend__swatch exam-legend__swatch--current" />
              当前题
            </span>
            <span class="exam-legend__item">
              <span class="exam-legend__swatch exam-legend__swatch--flag" />
              已标记
            </span>
          </div>
        </div>

        <!-- 全卷答题卡 -->
        <div class="exam-panel exam-side__panel">
          <div class="exam-sheet__top">
            <h3 class="exam-side__title">全卷答题卡</h3>
            <span class="exam-sheet__hint">{{ totalQuestions }} 题 · 点击题号跳转</span>
          </div>
          <div v-for="section in sheetSections" :key="section.type" class="exam-sheet__section">
            <div class="exam-sheet__head">
              <span>{{ section.label }}</span>
              <span class="exam-sheet__range">第 {{ section.start }}–{{ section.end }} 题</span>
            </div>
            <div class="exam-sheet__grid">
              <button
                v-for="qNum in section.numbers"
                :key="qNum"
                type="button"
                class="exam-sheet__cell"
                :class="getGridButtonClass(qNum)"
                :aria-label="`跳转到第 ${qNum} 题`"
                :aria-current="qNum === currentQuestionIdx ? 'step' : undefined"
                @click="jumpToQuestion(qNum)"
              >
                {{ qNum }}
                <span v-if="flags[qNum]" class="exam-sheet__flag" />
              </button>
            </div>
          </div>

          <!-- 提交试卷 -->
          <button
            type="button"
            class="exam-btn exam-btn--submit"
            :disabled="submitting"
            @click="openSubmitConfirm"
          >
            <Award class="exam-icon exam-icon--sm" />
            <span>提交试卷</span>
          </button>
        </div>
      </aside>
    </div>

    <!-- 交卷确认 -->
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

    <!-- 考场规则 -->
    <ConfirmDialog
      v-if="rulesOpen"
      tone="primary"
      title="考场规则提醒"
      subtitle="请遵守以下规则，违规将影响成绩认定"
      :list="examRules"
      tip="如需再次查看，可点击右上角「考规帮助」。"
      confirm-text="我已知晓"
      hide-cancel
      @cancel="rulesOpen = false"
      @confirm="rulesOpen = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import {
  Clock,
  HelpCircle,
  Flag,
  ArrowLeft,
  ArrowRight,
  Award
} from 'lucide-vue-next';
import {
  fetchAttempt,
  fetchExamPaper,
  reportExamSwitch,
  saveExamAnswer,
  startExam,
  submitExam,
} from '../api/index.js';
import { useConfirm } from '../composables/useConfirm.js';
import ConfirmDialog from '../components/ConfirmDialog.vue';

const props = defineProps({
  examId: {
    type: [String, Number],
    default: '',
  },
  user: {
    type: Object,
    required: true,
  },
});

const emit = defineEmits(['back', 'submit-exam']);

const exam = ref(null);
const questions = ref([]);
const attemptId = ref(null);
/* 答案与标记都以题目主键为 key，避免与后端 questionId 混淆 */
const answers = ref({});
const flags = ref({});
const loading = ref(true);
const pageError = ref('');
const saveError = ref('');
const submitting = ref(false);
const switchCount = ref(0);
const secondsLeft = ref(0);
const currentQuestionIdx = ref(1);

const questionTypeOrder = {
  single: 0,
  multiple: 1,
  judgment: 2,
  application: 3,
};

let timer = null;
let textSaveTimer = null;

const idxByQuestionId = computed(() => {
  const map = {};
  questions.value.forEach((question, i) => {
    map[question.numericId] = i + 1;
  });
  return map;
});

const applySession = (paper, session) => {
  attemptId.value = session.attemptId;
  questions.value = [...(paper.questions || [])].sort((a, b) =>
    (questionTypeOrder[a.type] ?? Number.MAX_SAFE_INTEGER)
      - (questionTypeOrder[b.type] ?? Number.MAX_SAFE_INTEGER)
  );
  // 倒计时以服务端剩余时间为准；服务端明确返回 0 时立即自动交卷，不再回退到试卷时长
  const sessionRemaining = session.remainingSeconds == null ? null : Number(session.remainingSeconds);
  const examDurationSeconds = Number(paper.durationMinutes || 60) * 60;
  secondsLeft.value = sessionRemaining == null
    ? examDurationSeconds
    : Math.max(0, sessionRemaining);
  switchCount.value = Number(session.switchCount || 0);

  const nextAnswers = {};
  const nextFlags = {};
  (session.answers || []).forEach((answer) => {
    const idx = idxByQuestionId.value[answer.questionId];
    if (!idx) return;
    nextAnswers[idx] = { selected: answer.selectedList, text: answer.answerText };
    nextFlags[idx] = answer.isFlagged;
  });
  answers.value = nextAnswers;
  flags.value = nextFlags;
};

const startCountdown = () => {
  if (timer) clearInterval(timer);
  timer = setInterval(() => {
    if (secondsLeft.value > 1) {
      secondsLeft.value -= 1;
      return;
    }
    secondsLeft.value = 0;
    clearInterval(timer);
    timer = null;
    finishExam();
  }, 1000);
};

const loadSession = async () => {
  loading.value = true;
  pageError.value = '';
  try {
    const paper = await fetchExamPaper(props.examId);
    exam.value = paper;
    const resumable = paper.attempt && Number(paper.attempt.status) === 1;
    const session = resumable
      ? await fetchAttempt(paper.attempt.attemptId)
      : await startExam(props.examId);
    applySession(paper, session);
    startCountdown();
  } catch (error) {
    pageError.value = error?.message || '试卷加载失败，请稍后重试';
  } finally {
    loading.value = false;
  }
};

const reportSwitch = () => {
  if (document.visibilityState === 'visible' || !attemptId.value || loading.value) return;
  reportExamSwitch(attemptId.value)
    .then((result) => {
      switchCount.value = Number(result?.switchCount ?? switchCount.value);
    })
    .catch(() => {
      /* 切屏计数上报失败不影响作答 */
    });
};

onMounted(() => {
  loadSession();
  document.addEventListener('visibilitychange', reportSwitch);
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
  if (textSaveTimer) clearTimeout(textSaveTimer);
  document.removeEventListener('visibilitychange', reportSwitch);
});

const formattedTimer = computed(() => {
  const total = secondsLeft.value;
  const hrs = Math.floor(total / 3600);
  const mins = Math.floor((total % 3600) / 60);
  const secs = total % 60;
  return `${String(hrs).padStart(2, '0')}:${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
});

const totalQuestions = computed(() => questions.value.length);
const currentQuestion = computed(() => questions.value[currentQuestionIdx.value - 1] || null);
const sheetSections = computed(() => {
  const sections = [];
  questions.value.forEach((question, index) => {
    const type = question.type;
    let section = sections[sections.length - 1];
    if (!section || section.type !== type) {
      section = {
        type,
        label: {
          single: '单选题',
          multiple: '多选题',
          judgment: '判断题',
          application: '应用题',
        }[type] || question.typeLabel || '其他题型',
        start: index + 1,
        end: index + 1,
        numbers: [],
      };
      sections.push(section);
    }
    section.end = index + 1;
    section.numbers.push(index + 1);
  });
  return sections;
});
const currentAnswer = computed(() => answers.value[currentQuestionIdx.value]?.text || '');
const selectedOptions = computed(() => answers.value[currentQuestionIdx.value]?.selected || []);
const answeredCount = computed(() =>
  Object.values(answers.value).filter(
    answer => (answer.selected && answer.selected.length > 0) || String(answer.text || '').trim().length > 0
  ).length
);
const flaggedCount = computed(() => Object.values(flags.value).filter(Boolean).length);
const isFlagged = computed(() => Boolean(flags.value[currentQuestionIdx.value]));
const progressPercent = computed(() =>
  totalQuestions.value ? Math.round((answeredCount.value / totalQuestions.value) * 100) : 0
);

/* 同一题连续修改（多选连点）必须串行提交，否则并发插入撞 uk_attempt_answer 唯一键 */
let saveChain = Promise.resolve();

const sendAnswer = async (qNum) => {
  const question = questions.value[qNum - 1];
  if (!attemptId.value || !question) return;
  const state = answers.value[qNum] || { selected: [], text: '' };
  try {
    const result = await saveExamAnswer(attemptId.value, {
      questionId: question.numericId,
      selectedKeys: state.selected.join(','),
      answerText: state.text || null,
      flagged: Boolean(flags.value[qNum]),
      remainingSeconds: secondsLeft.value,
    });
    if (result?.remainingSeconds != null) secondsLeft.value = Number(result.remainingSeconds);
    saveError.value = '';
  } catch (error) {
    saveError.value = error?.message || '答案保存失败，请检查网络后重试';
  }
};

const persist = (qNum, patch) => {
  const state = answers.value[qNum] || { selected: [], text: '' };
  answers.value = { ...answers.value, [qNum]: { ...state, ...patch } };
  saveChain = saveChain.then(() => sendAnswer(qNum));
  return saveChain;
};

const toggleFlag = () => {
  const qNum = currentQuestionIdx.value;
  const next = { ...flags.value, [qNum]: !flags.value[qNum] };
  flags.value = next;
  persist(qNum, {});
};

const handleSelectOption = (key) => {
  const qNum = currentQuestionIdx.value;
  if (currentQuestion.value?.type === 'multiple') {
    const selected = selectedOptions.value;
    persist(qNum, {
      selected: selected.includes(key)
        ? selected.filter(option => option !== key)
        : [...selected, key]
    });
    return;
  }
  persist(qNum, { selected: [key] });
};

const handleApplicationInput = (value) => {
  const qNum = currentQuestionIdx.value;
  answers.value = { ...answers.value, [qNum]: { selected: [], text: value } };
  if (textSaveTimer) clearTimeout(textSaveTimer);
  textSaveTimer = setTimeout(() => persist(qNum, { text: value }), 800);
};

const clearCurrentSelection = () => {
  if (textSaveTimer) clearTimeout(textSaveTimer);
  persist(currentQuestionIdx.value, { selected: [], text: '' });
};

const jumpToQuestion = (qNum) => {
  if (qNum < 1 || qNum > totalQuestions.value) return;
  currentQuestionIdx.value = qNum;
};

const handleNextQuestion = () => {
  jumpToQuestion(currentQuestionIdx.value + 1);
};

const handlePrevQuestion = () => {
  jumpToQuestion(currentQuestionIdx.value - 1);
};

/* 返回语义化状态类（不再返回 Tailwind 工具类） */
const getGridButtonClass = (qNum) => {
  if (qNum === currentQuestionIdx.value) return 'is-current';
  if (flags.value[qNum]) return 'is-flagged';
  const answer = answers.value[qNum];
  return answer && ((answer.selected && answer.selected.length) || String(answer.text || '').trim())
    ? 'is-answered'
    : 'is-blank';
};

const { confirmState, askConfirm, cancelConfirm, acceptConfirm } = useConfirm();

const rulesOpen = ref(false);

const examRules = [
  '请保持全屏专注，切屏超过 3 次将触发系统强行交卷。',
  '每题修改将实时保存至云端，中断后可继续作答。',
  '遇到网络波动，答题进度已于本地安全存储。'
];

const openSubmitConfirm = () => {
  const left = totalQuestions.value - answeredCount.value;
  askConfirm({
    tone: 'primary',
    title: '确认交卷？',
    subtitle: '交卷后将立即机审评分，无法再修改答案。',
    rows: [
      { label: '已作答', value: `${answeredCount.value} / ${totalQuestions.value} 题` },
      { label: '已标记', value: `${flaggedCount.value} 题` },
      { label: '剩余时间', value: formattedTimer.value }
    ],
    tip: left > 0
      ? `尚有 ${left} 道题未作答，未作答题目将按 0 分计入总分。`
      : '全部题目均已作答，可以放心交卷。',
    confirmText: '确认交卷',
    action: () => finishExam()
  });
};

const finishExam = async () => {
  if (!attemptId.value || submitting.value) return;
  submitting.value = true;
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
  if (textSaveTimer) {
    clearTimeout(textSaveTimer);
    textSaveTimer = null;
  }
  try {
    await saveChain;
    const result = await submitExam(attemptId.value);
    emit('submit-exam', {
      attemptId: result.attemptId,
      examTitle: result.examTitle || exam.value?.title || '在线考核',
      score: Number(result.totalScore || 0),
      passScore: Number(result.passScore ?? exam.value?.passScore ?? 60),
      totalScore: Number(exam.value?.totalScore ?? result.totalScore ?? 100),
      answeredCount: answeredCount.value,
      totalQuestions: totalQuestions.value,
      wrongCount: Number(result.wrongCount || 0),
      switchCount: Number(result.switchCount ?? switchCount.value),
      isPassed: Boolean(result.isPassed),
      pendingReview: Number(result.status) === 2
    });
  } catch (error) {
    saveError.value = error?.message || '交卷失败，请重试';
    startCountdown();
  } finally {
    submitting.value = false;
  }
};

const showRulesHint = () => {
  rulesOpen.value = true;
};
</script>

<style scoped>
/* ============ Design tokens ============ */
.exam-page {
  --c-text: var(--app-text);
  --c-text-sub: var(--app-text-2);
  --c-text-soft: var(--app-text-3);
  --c-muted: var(--app-text-4);
  --c-faint: var(--app-text-5);
  --c-border: var(--app-border);
  --c-border-strong: var(--app-border-strong);
  --c-border-soft: var(--app-surface-3);
  --c-bg-soft: var(--app-surface-2);
  --c-blue: var(--app-accent-deep);
  --c-blue-bg: var(--app-accent-soft);
  --c-sky: var(--app-accent);
  --c-sky-hover: var(--app-accent-hover-strong);
  --c-sky-soft: var(--app-page-bg);
  --c-purple: var(--app-violet);
  --c-purple-hover: var(--app-violet-strong);
  --c-purple-deep: var(--app-violet-fg);
  --c-green: var(--app-emerald-solid);

  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-bottom: 48px;
  font-family: 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', sans-serif;
}

/* ============ Icons (lucide) ============ */
.exam-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}
.exam-icon--sm { width: 16px; height: 16px; }
.exam-icon--xs { width: 14px; height: 14px; }

/* ============ Top bar ============ */
.exam-topbar {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  border: 1px solid var(--c-border);
  border-radius: 16px;
  background: var(--app-surface);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}
@media (min-width: 768px) {
  .exam-topbar {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }
}

.exam-topbar__brand {
  display: flex;
  align-items: center;
  gap: 12px;
}
.exam-brand__logo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  border-radius: 12px;
  background: var(--c-sky);
  color: var(--app-on-accent-solid);
  font-size: 15px;
  font-weight: 700;
}
.exam-brand__title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.exam-brand__title {
  font-size: 14px;
  font-weight: 700;
  color: var(--c-text);
}
.exam-live-badge {
  padding: 2px 8px;
  border: 1px solid var(--app-emerald-line);
  border-radius: 999px;
  background: var(--app-emerald-soft);
  color: var(--app-emerald-fg);
  font-size: 10px;
  font-weight: 600;
}
.exam-brand__meta {
  margin-top: 2px;
  font-size: 11px;
  color: var(--c-muted);
}

.exam-topbar__tools {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}
@media (min-width: 768px) {
  .exam-topbar__tools { margin-left: auto; }
}

/* ---- Timer ---- */
.exam-timer {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  border: 1px solid rgba(131, 201, 244, 0.4);
  border-radius: 12px;
  background: var(--c-blue-bg);
}
.exam-timer__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: rgba(131, 201, 244, 0.3);
  color: var(--c-blue);
}
.exam-timer__label {
  font-size: 10px;
  color: var(--c-muted);
}
.exam-timer__value {
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.08em;
  color: var(--c-blue);
}

/* ============ Common buttons ============ */
.exam-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: 0;
  border-radius: 12px;
  font-size: 12px;
  cursor: pointer;
  transition: background-color 0.2s ease, transform 0.2s ease, color 0.2s ease;
}
.exam-btn--ghost {
  padding: 8px 12px;
  border: 1px solid var(--c-border-strong);
  background: var(--c-bg-soft);
  color: var(--c-text-soft);
  font-weight: 600;
}
.exam-btn--ghost:hover { background: var(--c-border-soft); }
.exam-btn--sky {
  padding: 8px 16px;
  background: var(--c-sky);
  color: var(--app-on-fill);
  font-weight: 700;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.05);
}
.exam-btn--sky:hover { background: var(--c-sky-hover); }

/* ============ Layout ============ */
.exam-layout {
  display: grid;
  width: 100%;
  min-width: 0;
  grid-template-columns: 1fr;
  gap: 20px;
}
@media (min-width: 1200px) {
  .exam-layout { grid-template-columns: minmax(0, 2fr) minmax(300px, 1fr); }
}
.exam-main {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 16px;
}
.exam-side {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 16px;
}

/* ============ Panel ============ */
.exam-panel {
  min-width: 0;
  padding: 24px;
  border: 1px solid var(--c-border);
  border-radius: 16px;
  background: var(--app-surface);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.exam-side__panel {
  padding: 20px;
  gap: 16px;
}
.exam-side__title {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: var(--c-text);
}

/* ============ 加载 / 错误态 ============ */
.exam-state {
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 48px 24px;
  text-align: center;
}
.exam-state__title {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--c-text);
}
.exam-state__text {
  margin: 0;
  font-size: 13px;
  color: var(--c-text-soft);
}
.exam-state__actions {
  display: flex;
  gap: 10px;
  margin-top: 4px;
}
.exam-save-error {
  padding: 10px 14px;
  border: 1px solid var(--app-rose-line);
  border-radius: 12px;
  background: var(--app-rose-bg);
  color: var(--app-rose-fg);
  font-size: 12px;
  font-weight: 600;
}

/* ============ Question meta ============ */
.exam-qmeta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--c-border);
  font-size: 12px;
}
.exam-qmeta__left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.exam-qmeta__muted { color: var(--c-muted); }
.exam-qmeta__muted strong {
  font-family: 'SFMono-Regular', Consolas, monospace;
  color: var(--c-text);
}
.exam-chip {
  padding: 2px 10px;
  border-radius: 8px;
  background: var(--c-sky);
  color: var(--app-on-accent-solid);
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 12px;
  font-weight: 700;
}
.exam-chip--type {
  background: var(--c-blue-bg);
  color: var(--app-accent-deep);
  font-family: inherit;
  font-size: 11px;
  font-weight: 600;
}

.exam-flag-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border: 1px solid transparent;
  border-radius: 8px;
  background: transparent;
  color: var(--c-muted);
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease;
}
.exam-flag-btn:hover { background: var(--c-border-soft); }
.exam-flag-btn.is-flagged {
  border-color: rgba(147, 129, 255, 0.4);
  background: var(--c-blue-bg);
  color: var(--c-purple-deep);
  font-weight: 700;
}

/* ============ Question title ============ */
.exam-question__title {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  line-height: 1.625;
  color: var(--c-text);
}
@media (min-width: 768px) {
  .exam-question__title { font-size: 16px; }
}

/* ============ Application answer ============ */
.exam-answer {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.exam-answer__label {
  font-size: 12px;
  font-weight: 600;
  color: var(--c-text-soft);
}
.exam-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid var(--c-border-strong);
  border-radius: 12px;
  background: var(--c-bg-soft);
  font-size: 14px;
  line-height: 24px;
  color: var(--c-text);
  font-family: inherit;
  resize: vertical;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
  box-sizing: border-box;
}
.exam-textarea:focus {
  border-color: var(--c-sky);
  box-shadow: 0 0 0 3px rgba(131, 201, 244, 0.3);
}
.exam-answer__count {
  margin: 0;
  text-align: right;
  font-size: 11px;
  color: var(--c-faint);
}

/* ============ Options ============ */
.exam-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.exam-option {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 16px;
  border: 2px solid var(--c-border);
  border-radius: 12px;
  cursor: pointer;
  transition: border-color 0.2s ease, background-color 0.2s ease, box-shadow 0.2s ease;
}
.exam-option:hover {
  border-color: var(--c-sky);
  background: var(--c-bg-soft);
}
.exam-option.is-selected {
  border-color: var(--c-sky);
  background: var(--c-blue-bg);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.05);
}

.exam-option__indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  margin-top: 2px;
  flex-shrink: 0;
  border: 2px solid var(--c-border-strong);
  background: var(--app-surface);
  transition: border-color 0.2s ease, background-color 0.2s ease;
}
.exam-option__indicator.is-circle { border-radius: 50%; }
.exam-option__indicator.is-square { border-radius: 6px; }
.exam-option__indicator.is-checked {
  border-color: var(--c-sky);
  background: var(--c-sky);
}
.exam-option__dot {
  width: 8px;
  height: 8px;
  background: var(--app-surface);
}
.exam-option__dot.is-circle { border-radius: 50%; }
.exam-option__dot.is-square { border-radius: 2px; }

.exam-option__body {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.exam-option__text {
  font-size: 12px;
  line-height: 1.625;
  color: var(--c-text);
}
.exam-option__key {
  margin-right: 8px;
  font-family: 'SFMono-Regular', Consolas, monospace;
}
.exam-option__explanation {
  font-size: 11px;
  color: var(--c-muted);
}
.exam-option__explanation.is-highlight {
  color: var(--c-blue);
  font-weight: 600;
}

/* ============ Nav buttons ============ */
.exam-nav {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid var(--c-border);
}
.exam-btn--prev {
  padding: 8px 16px;
  border: 1px solid var(--c-border-strong);
  background: var(--app-surface);
  color: var(--c-text-soft);
  font-weight: 600;
}
.exam-btn--prev:hover:not(:disabled) { background: var(--c-bg-soft); }
.exam-btn--prev:disabled { opacity: 0.4; cursor: not-allowed; }

.exam-btn--clear {
  padding: 8px 12px;
  background: transparent;
  color: var(--c-muted);
  font-weight: 500;
}
.exam-btn--clear:hover { color: var(--app-red-600); }

.exam-btn--next {
  padding: 8px 20px;
  background: var(--c-sky);
  color: var(--app-on-fill);
  font-weight: 700;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.05);
}
.exam-btn--next:hover {
  background: var(--c-sky-hover);
  transform: scale(1.01);
}

/* ============ Secure notice ============ */
.exam-secure {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  border: 1px solid var(--c-border);
  border-radius: 12px;
  background: var(--c-bg-soft);
  font-size: 11px;
  color: var(--c-muted);
}
@media (min-width: 640px) {
  .exam-secure {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }
}
.exam-secure__left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.exam-secure__dot {
  width: 8px;
  height: 8px;
  flex-shrink: 0;
  border-radius: 50%;
  background: var(--c-green);
}
.exam-secure__node {
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 10px;
  color: var(--c-faint);
}

/* ============ Progress overview ============ */
.exam-progress__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.exam-progress__percent { text-align: right; }
.exam-progress__num {
  display: block;
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 18px;
  font-weight: 700;
  color: var(--c-blue);
}
.exam-progress__label {
  display: block;
  font-size: 10px;
  color: var(--c-muted);
}
.exam-progress__track {
  width: 100%;
  height: 8px;
  overflow: hidden;
  border-radius: 999px;
  background: var(--c-border);
}
.exam-progress__bar {
  height: 100%;
  border-radius: 999px;
  background: var(--c-sky);
  transition: width 0.3s ease;
}
.exam-progress__stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}
.exam-stat-box {
  padding: 8px;
  border-radius: 8px;
  text-align: center;
}
.exam-stat-box--answered { background: var(--c-sky); }
.exam-stat-box--blank {
  border: 1px solid var(--c-border-strong);
  background: var(--app-surface);
}
.exam-stat-box--flagged { background: var(--c-purple); }
.exam-stat-box__num {
  display: block;
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 16px;
  font-weight: 700;
}
.exam-stat-box--answered .exam-stat-box__num { color: var(--app-on-accent-solid); }
.exam-stat-box--answered .exam-stat-box__label { color: var(--app-on-accent-muted); }
.exam-stat-box--blank .exam-stat-box__num { color: var(--c-faint); }
.exam-stat-box--flagged .exam-stat-box__num { color: var(--app-on-fill); }
.exam-stat-box__label {
  display: block;
  font-size: 10px;
  color: var(--c-muted);
}
.exam-stat-box--flagged .exam-stat-box__label { color: var(--app-on-fill); }

/* ---- Legend ---- */
.exam-legend {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding-top: 4px;
  font-size: 10px;
  color: var(--c-muted);
}
.exam-legend__item {
  display: flex;
  align-items: center;
  gap: 4px;
}
.exam-legend__swatch {
  width: 10px;
  height: 10px;
  border-radius: 4px;
}
.exam-legend__swatch--answered { background: var(--c-sky); }
.exam-legend__swatch--current { background: var(--app-green-300); }
.exam-legend__swatch--blank {
  border: 1px solid var(--c-border-strong);
  background: var(--app-surface);
}
.exam-legend__swatch--flag { background: var(--c-purple); }

/* ============ Answer sheet ============ */
.exam-sheet__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.exam-sheet__hint {
  font-size: 10px;
  color: var(--c-muted);
}
.exam-sheet__section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.exam-sheet__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  font-weight: 600;
  color: var(--c-text);
}
.exam-sheet__range {
  font-size: 10px;
  font-weight: 400;
  color: var(--c-muted);
}
.exam-sheet__grid {
  display: grid;
  min-width: 0;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 8px;
}
.exam-sheet__cell {
  position: relative;
  min-width: 0;
  width: 100%;
  height: 36px;
  border: 1px solid transparent;
  border-radius: 8px;
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease, box-shadow 0.2s ease;
}
.exam-sheet__cell.is-current {
  background: var(--app-emerald-bg);
  color: var(--app-emerald-fg);
  box-shadow: 0 0 0 2px var(--app-green-300), 0 0 0 3px var(--app-surface);
}
.exam-sheet__cell.is-flagged {
  background: var(--c-purple);
  color: var(--app-ink-solid);
}
.exam-sheet__cell.is-answered {
  background: var(--c-sky);
  color: var(--app-on-accent-solid);
}
.exam-sheet__cell.is-blank {
  border-color: var(--c-border-strong);
  background: var(--app-surface);
  color: var(--c-muted);
}
.exam-sheet__cell.is-blank:hover { background: var(--c-border-soft); }
.exam-sheet__flag {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--app-amber-solid);
}

/* ---- Final submit ---- */
.exam-btn--submit {
  width: 100%;
  padding: 12px 0;
  background: var(--c-purple);
  color: var(--app-on-fill);
  font-weight: 700;
  box-shadow: 0 6px 14px rgba(15, 23, 42, 0.12);
}
.exam-btn--submit:hover {
  background: var(--c-purple-hover);
  transform: scale(1.01);
}
</style>