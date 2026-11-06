<template>
  <!-- 考试结果弹窗 -->
  <div v-if="isOpen" class="modal-overlay" @click.self="closeModal">

    <div class="modal-container">

      <!-- 顶部图标（通过=奖杯 / 未通过=圆圈勾） -->
      <div :class="['result-icon', isPassed ? 'result-icon--pass' : 'result-icon--fail']">
        <span v-if="isPassed" class="icon-trophy">🏆</span>
        <span v-else class="icon-check">✓</span>
      </div>

      <!-- 标题区域 -->
      <div class="result-label">EXAM SUBMISSION RESULT · 即时测评成绩单</div>
      <h3 class="result-title">{{ examTitle }}</h3>

      <!-- 分数展示 -->
      <div class="score-panel">
        <div class="score-display">
          <span :class="['score-number', isPassed ? 'score-number--pass' : 'score-number--fail']">
            {{ score }}
          </span>
          <span class="score-total">/ {{ totalScore }} 分</span>
        </div>

        <div class="score-status">
          <span :class="['pass-badge', isPassed ? 'pass-badge--pass' : 'pass-badge--fail']">
            {{ pendingReview ? '客观题已机审 · 主观题待评分' : (isPassed ? '★ 考核优秀通过' : '未达到合格基准线') }}
          </span>
          <span class="pass-line">合格基准线: {{ passScore }} 分</span>
        </div>
      </div>

      <!-- 数据统计 -->
      <div class="stats-grid">
        <div class="stats-card">
          <span class="stats-label">作答完成度</span>
          <span class="stats-value">{{ answeredCount }} / {{ totalQuestions }} 题</span>
        </div>
        <div class="stats-card">
          <span class="stats-label">得分率</span>
          <span class="stats-value stats-value--green">{{ accuracyPercent }}%</span>
        </div>
        <div class="stats-card">
          <span class="stats-label">考位状态</span>
          <span class="stats-value stats-value--blue">{{ pendingReview ? '待人工复核' : '已安全交卷' }}</span>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-bar">

        <!-- 通过 → 查看证书 -->
        <button
          v-if="isPassed"
          class="btn btn-primary"
          @click="$emit('view-certificate')"
        >
          <span class="icon-award">🏆</span>
          <span>查看授予证书</span>
        </button>

        <!-- 未通过 → 重新作答 -->
        <button
          v-else-if="!pendingReview"
          class="btn btn-retry"
          @click="$emit('retake')"
        >
          <span class="icon-retry">↺</span>
          <span>重新作答</span>
        </button>

        <!-- 返回 -->
        <button
          class="btn btn-secondary"
          @click="closeModal"
        >
          <span>返回考评中心</span>
          <span class="icon-arrow">→</span>
        </button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  isOpen:        { type: Boolean, default: false },
  examTitle:     { type: String,  default: '' },
  score:         { type: Number,  default: 0 },
  passScore:     { type: Number,  default: 60 },
  totalScore:    { type: Number,  default: 100 },
  answeredCount: { type: Number,  default: 0 },
  totalQuestions:{ type: Number,  default: 0 },
  isPassed:      { type: Boolean, default: false },
  pendingReview: { type: Boolean, default: false }
});

const emit = defineEmits(['close', 'view-certificate', 'retake']);

// 是否通过
const isPassed = computed(() => props.isPassed);
const accuracyPercent = computed(() =>
  props.totalScore ? Math.round((props.score / props.totalScore) * 100) : 0
);

// 关闭弹窗
function closeModal() {
  emit('close');
}
</script>

<style scoped>
/* ========== 全局重置 ========== */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

button {
  font-family: inherit;
  cursor: pointer;
}

/* ========== 弹窗遮罩 ========== */
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: rgba(15, 23, 42, 0.7);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to   { opacity: 1; }
}

/* ========== 弹窗主体 ========== */
.modal-container {
  width: 100%;
  max-width: 512px;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 16px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  overflow: hidden;
  padding: 24px;
  text-align: center;
}

/* ========== 顶部结果图标 ========== */
.result-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
}

/* 通过：绿色背景 */
.result-icon--pass {
  background: var(--app-emerald-bg);
  color: var(--app-emerald-strong);
}

/* 未通过：琥珀色背景 */
.result-icon--fail {
  background: var(--app-amber-bg);
  color: var(--app-amber-strong);
}

.icon-check {
  font-size: 28px;
  font-weight: 700;
}

/* ========== 标题区域 ========== */
.result-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--app-accent-deep);
  letter-spacing: 0.05em;
  text-transform: uppercase;
  margin-bottom: 4px;
}

.result-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--app-text);
  margin-bottom: 8px;
}

/* ========== 分数展示面板 ========== */
.score-panel {
  margin: 24px 0;
  padding: 20px;
  background: var(--app-page-bg);
  border: 1px solid var(--app-border);
  border-radius: 12px;
}

.score-display {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;
  font-family: "SFMono-Regular", Consolas, Menlo, monospace;
}

.score-number {
  font-size: 48px;
  font-weight: 900;
}

.score-number--pass {
  color: var(--app-emerald-strong);
}

.score-number--fail {
  color: var(--app-amber-strong);
}

.score-total {
  font-size: 20px;
  color: var(--app-text-4);
}

/* 合格状态标签 */
.score-status {
  margin-top: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.pass-badge {
  padding: 2px 10px;
  font-size: 12px;
  font-weight: 700;
  border-radius: 9999px;
}

.pass-badge--pass {
  color: var(--app-emerald-fg);
  background: var(--app-emerald-soft);
  border: 1px solid var(--app-emerald-line);
}

.pass-badge--fail {
  color: var(--app-amber-fg);
  background: var(--app-amber-50);
}

.pass-line {
  font-size: 12px;
  color: var(--app-text-4);
}

/* ========== 数据统计 ========== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 24px;
  text-align: left;
}

.stats-card {
  padding: 10px;
  background: var(--app-surface-2);
  border: 1px solid var(--app-border);
  border-radius: 8px;
}

.stats-label {
  display: block;
  font-size: 10px;
  color: var(--app-text-4);
}

.stats-value {
  font-size: 12px;
  font-weight: 700;
  color: var(--app-text);
}

.stats-value--green {
  color: var(--app-emerald-strong);
}

.stats-value--blue {
  color: var(--app-accent-deep);
}

/* ========== 操作按钮 ========== */
.action-bar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 16px;
  font-size: 12px;
  font-weight: 600;
  border: none;
  border-radius: 12px;
  transition: background 0.15s, color 0.15s;
}

/* 主按钮：查看证书 */
.btn-primary {
  flex: 1;
  background: var(--app-accent);
  color: var(--app-on-fill);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.btn-primary:hover {
  background: var(--app-accent-hover-strong);
}

/* 重试按钮 */
.btn-retry {
  flex: 1;
  background: var(--app-accent);
  color: var(--app-on-accent-solid);
}

.btn-retry:hover {
  background: var(--app-accent-hover-strong);
}

/* 次要按钮：返回 */
.btn-secondary {
  background: var(--app-surface);
  color: var(--app-text-3);
  border: 1px solid var(--app-border);
}

.btn-secondary:hover {
  background: var(--app-surface-2);
}

/* 按钮内小图标 */
.icon-award,
.icon-retry,
.icon-arrow {
  font-size: 14px;
  line-height: 1;
}
</style>