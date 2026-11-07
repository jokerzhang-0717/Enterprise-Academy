<template>
  <div class="analytics-page">

    <!-- 页面标题 -->
    <div class="page-header">
      <div class="breadcrumb">
        <span>企业赋能平台</span>
        <span class="breadcrumb-sep">></span>
        <span class="breadcrumb-current">个人学习看板</span>
      </div>
      <h1 class="page-title">{{ user.name }} · 个人研学效能全景</h1>
    </div>

    <!-- 四个核心指标卡片 -->
    <div class="metrics-grid">

      <!-- 指标 1：累计有效学时 -->
      <div class="metric-card">
        <div class="metric-top">
          <span class="metric-label">累计有效学时</span>
          <div class="metric-icon metric-icon--blue">
            <Clock class="metric-icon-svg" aria-hidden="true" />
          </div>
        </div>
        <div class="metric-bottom">
          <span class="metric-number">{{ learningHours }}</span>
          <span class="metric-unit">小时</span>
        </div>
      </div>

      <!-- 指标 2：已完成专修课程 -->
      <div class="metric-card">
        <div class="metric-top">
          <span class="metric-label">已完成专修课程</span>
          <div class="metric-icon metric-icon--blue">
            <Reading class="metric-icon-svg" aria-hidden="true" />
          </div>
        </div>
        <div class="metric-bottom">
          <span class="metric-number">{{ completedCourses }}</span>
          <span class="metric-unit">门</span>
        </div>
      </div>

      <!-- 指标 3：考评平均分 -->
      <div class="metric-card">
        <div class="metric-top">
          <span class="metric-label">考评平均分</span>
          <div class="metric-icon metric-icon--purple">
            <Medal class="metric-icon-svg" aria-hidden="true" />
          </div>
        </div>
        <div class="metric-bottom">
          <span class="metric-number metric-number--purple">{{ averageScore }}</span>
          <span class="metric-unit">分</span>
        </div>
      </div>

    </div>

    <!-- 每月学习时长柱状图 -->
    <div class="chart-section">
      <div class="chart-card">

        <!-- 图表标题 -->
        <div class="chart-header">
          <h3 class="chart-title">每月学习时长</h3>
          <div class="chart-legend">
            <span class="legend-dot"></span>
            <span class="legend-text">学时 (h)</span>
          </div>
        </div>
        <p class="chart-desc">展示近六个月累计学习时长（小时），高亮月份为学习时长峰值</p>

        <div v-if="loading" class="chart-message">正在加载学习数据...</div>
        <div v-else-if="loadError" class="chart-message chart-message--error" role="alert">
          {{ loadError }}
          <button class="retry-button" type="button" @click="loadDashboard">重试</button>
        </div>
        <div v-else-if="!hasLearningData" class="chart-message">
          <span>近六个月暂无学习记录，完成课程课时后会自动累计</span>
          <button class="retry-button" type="button" @click="emit('navigate', 'courses')">去课程中心学习</button>
        </div>

        <!-- 柱状图 -->
        <div v-else class="chart-body">
          <svg class="chart-svg" viewBox="0 0 700 250" role="img" aria-label="近六个月每月学习时长柱状图">
            <g v-for="tick in chartTicks" :key="tick.value">
              <line
                x1="56"
                :y1="tick.y"
                x2="680"
                :y2="tick.y"
                :stroke="tick.value === 0 ? 'var(--app-border-strong)' : 'var(--app-blue-70)'"
                :stroke-width="tick.value === 0 ? 1.5 : 1"
              />
              <text
                v-if="tick.value > 0"
                x="46"
                :y="tick.y + 4"
                text-anchor="end"
                fill="var(--app-text-5)"
                font-size="11"
              >{{ formatHours(tick.value) }}h</text>
            </g>

            <g v-for="month in monthlyChart" :key="month.key" class="bar-group">
              <rect
                :x="month.x - 20"
                :y="month.y"
                width="40"
                :height="month.height"
                rx="5"
                :fill="month.isPeak ? 'var(--app-accent)' : 'var(--app-accent-soft)'"
                class="bar-rect"
              />
              <g class="bar-tooltip">
                <rect :x="month.x - 28" :y="month.tooltipY" width="56" height="20" rx="10" fill="var(--app-accent)" />
                <text
                  :x="month.x"
                  :y="month.tooltipY + 14"
                  text-anchor="middle"
                  fill="var(--app-on-fill)"
                  font-size="10"
                  font-weight="bold"
                >{{ formatHours(month.hours) }}h</text>
              </g>
              <text
                :x="month.x"
                y="222"
                text-anchor="middle"
                :fill="month.isPeak ? 'var(--app-violet)' : 'var(--app-text-4)'"
                font-size="12"
                :font-weight="month.isPeak ? 'bold' : 'normal'"
              >{{ month.label }}</text>
            </g>
          </svg>
        </div>

      </div>
    </div>

  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { Clock, Reading, Medal } from '@element-plus/icons-vue';
import { fetchDashboard } from '../api/index.js';

const props = defineProps({
  user: {
    type: Object,
    default: () => ({})
  }
});

const emit = defineEmits(['navigate', 'open-certificate-modal']);

const dashboard = ref(null);
const loading = ref(false);
const loadError = ref('');

const learningHours = computed(() =>
  Number(dashboard.value?.stats?.learningHours ?? props.user?.learningHours ?? 0)
);
const completedCourses = computed(() =>
  Number(dashboard.value?.stats?.completedCourses ?? props.user?.completedCourses ?? 0)
);
const averageScore = computed(() => {
  const score = dashboard.value?.examMetrics?.avgPastScore;
  if (score === null || score === undefined || score === '') return '--';
  const numericScore = Number(score);
  return Number.isFinite(numericScore) ? numericScore : '--';
});

const formatHours = (value) => Number(value.toFixed(1)).toString();

const monthlyChart = computed(() => {
  const records = new Map(
    (dashboard.value?.monthlyStudyHours || []).map((row) => [
      String(row.ym || '').slice(0, 7),
      Number(row.studyHours) || 0,
    ])
  );
  const now = new Date();
  const months = Array.from({ length: 6 }, (_, index) => {
    const date = new Date(now.getFullYear(), now.getMonth() - 5 + index, 1);
    const key = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
    return {
      key,
      label: `${date.getMonth() + 1}月`,
      hours: Math.max(0, records.get(key) || 0),
      x: 108 + index * 110,
    };
  });
  const maxHours = Math.max(...months.map((month) => month.hours));
  const axisMax = Math.max(10, Math.ceil(maxHours / 10) * 10);
  const peak = maxHours > 0 ? maxHours : null;

  return months.map((month) => {
    const height = month.hours / axisMax * 160;
    const y = 195 - height;
    return {
      ...month,
      height,
      y,
      tooltipY: Math.max(4, y - 26),
      isPeak: peak !== null && month.hours === peak,
    };
  });
});

const chartTicks = computed(() => {
  const maxHours = Math.max(...monthlyChart.value.map((month) => month.hours));
  const axisMax = Math.max(10, Math.ceil(maxHours / 10) * 10);
  return Array.from({ length: 5 }, (_, index) => {
    const value = axisMax * (4 - index) / 4;
    return { value, y: 35 + index * 40 };
  });
});

const hasLearningData = computed(() => monthlyChart.value.some((month) => month.hours > 0));

const loadDashboard = async () => {
  loading.value = true;
  loadError.value = '';
  try {
    dashboard.value = await fetchDashboard();
  } catch (error) {
    loadError.value = error?.message || '学习数据加载失败，请稍后重试。';
  } finally {
    loading.value = false;
  }
};

onMounted(loadDashboard);
</script>

<style scoped>
/* ========== 全局重置 ========== */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

/* ========== 页面布局 ========== */
.analytics-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding-bottom: 48px;
}

/* ========== 页面标题 ========== */
.page-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--app-text-4);
  margin-bottom: 4px;
}

.breadcrumb-sep {
  opacity: 0.5;
}

.breadcrumb-current {
  color: var(--app-accent-deep);
  font-weight: 600;
}

.page-title {
  font-size: 28px;
  font-weight: 800;
  color: var(--app-text);
  letter-spacing: -0.025em;
}

/* ========== 指标卡片网格 ========== */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.metric-card {
  min-height: 148px;
  padding: 20px;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  transition: box-shadow 0.2s;
}

.metric-card:hover {
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}

.metric-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: var(--app-text-4);
}

.metric-label {
  font-size: 12px;
  color: var(--app-text-4);
}

/* 图标背景 */
.metric-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
}

.metric-icon-svg {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.metric-icon--blue {
  background: var(--app-accent-soft);
  color: var(--app-accent-deep);
}

.metric-icon--purple {
  background: var(--app-accent-soft);
  color: var(--app-violet-fg);
}

/* 数字 */
.metric-bottom {
  margin-top: 24px;
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.metric-number {
  font-family: "SFMono-Regular", Consolas, Menlo, monospace;
  font-size: 30px;
  font-weight: 800;
  color: var(--app-text);
  letter-spacing: -0.025em;
}

.metric-number--purple {
  color: var(--app-violet-fg);
}

.metric-unit {
  font-size: 12px;
  color: var(--app-text-4);
}

/* ========== 图表区域 ========== */
.chart-section {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 20px;
}

.chart-card {
  grid-column: span 12;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 24px;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

/* 图表标题 */
.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.chart-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--app-text);
}

.chart-legend {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 4px;
  background: var(--app-accent);
}

.legend-text {
  color: var(--app-text-4);
}

.chart-desc {
  font-size: 12px;
  color: var(--app-text-4);
}

.chart-message {
  min-height: 260px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--app-text-4);
  font-size: 14px;
}

.chart-message--error {
  color: var(--app-danger, #c45656);
}

.retry-button {
  padding: 6px 12px;
  border: 1px solid var(--app-border);
  border-radius: 6px;
  background: var(--app-surface);
  color: var(--app-accent-deep);
  cursor: pointer;
}

/* 图表主体 */
.chart-body {
  padding: 12px 0 4px;
}

.chart-svg {
  display: block;
  width: 100%;
  height: 260px;
}

/* 柱状图交互 */
.bar-group {
  cursor: pointer;
}

.bar-rect {
  transition: fill 0.2s;
}

.bar-group:hover .bar-rect {
  fill: var(--app-accent-sky-400);
}

/* 提示气泡（默认隐藏，hover 显示） */
.bar-tooltip {
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s;
}

.bar-group:hover .bar-tooltip {
  opacity: 1;
}

/* ========== 响应式 ========== */
@media (max-width: 1024px) {
  .metrics-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .chart-card {
    grid-column: span 12;
  }
}

@media (max-width: 640px) {
  .metrics-grid {
    grid-template-columns: 1fr;
  }

  .page-title {
    font-size: 22px;
  }
}
</style>