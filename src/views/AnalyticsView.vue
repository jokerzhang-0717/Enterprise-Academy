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
          <span class="metric-number">{{ user.learningHours }}</span>
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
          <span class="metric-number">{{ user.completedCourses }}</span>
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
          <span class="metric-number metric-number--purple">92.5</span>
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
        <p class="chart-desc">展示每月累计学习时长（小时），高亮月份为学习时长峰值</p>

        <!-- 柱状图 -->
        <div class="chart-body">
          <svg class="chart-svg" viewBox="0 0 700 250" role="img" aria-label="10月至3月每月学习时长柱状图">

            <!-- 横向网格线 -->
            <line x1="56" y1="35"  x2="680" y2="35"  stroke="#E8EEF5" stroke-width="1" />
            <line x1="56" y1="75"  x2="680" y2="75"  stroke="#E8EEF5" stroke-width="1" />
            <line x1="56" y1="115" x2="680" y2="115" stroke="#E8EEF5" stroke-width="1" />
            <line x1="56" y1="155" x2="680" y2="155" stroke="#E8EEF5" stroke-width="1" />
            <line x1="56" y1="195" x2="680" y2="195" stroke="#CBD5E1" stroke-width="1.5" />

            <!-- 纵轴刻度 -->
            <text x="46" y="39"  text-anchor="end" fill="#94A3B8" font-size="11">40h</text>
            <text x="46" y="79"  text-anchor="end" fill="#94A3B8" font-size="11">30h</text>
            <text x="46" y="119" text-anchor="end" fill="#94A3B8" font-size="11">20h</text>
            <text x="46" y="159" text-anchor="end" fill="#94A3B8" font-size="11">10h</text>

            <!-- 10月 -->
            <g class="bar-group">
              <rect x="88" y="129" width="40" height="66" rx="5" fill="#D9F0FF" class="bar-rect" />
              <g class="bar-tooltip">
                <rect x="82" y="103" width="52" height="20" rx="10" fill="#83C9F4" />
                <text x="108" y="117" text-anchor="middle" fill="#FFFFFF" font-size="10" font-weight="bold">16.5h</text>
              </g>
            </g>

            <!-- 11月 -->
            <g class="bar-group">
              <rect x="198" y="96" width="40" height="99" rx="5" fill="#D9F0FF" class="bar-rect" />
              <g class="bar-tooltip">
                <rect x="192" y="70" width="52" height="20" rx="10" fill="#83C9F4" />
                <text x="218" y="84" text-anchor="middle" fill="#FFFFFF" font-size="10" font-weight="bold">24.7h</text>
              </g>
            </g>

            <!-- 12月 -->
            <g class="bar-group">
              <rect x="308" y="119" width="40" height="76" rx="5" fill="#D9F0FF" class="bar-rect" />
              <g class="bar-tooltip">
                <rect x="302" y="93" width="52" height="20" rx="10" fill="#83C9F4" />
                <text x="328" y="107" text-anchor="middle" fill="#FFFFFF" font-size="10" font-weight="bold">18.9h</text>
              </g>
            </g>

            <!-- 1月 -->
            <g class="bar-group">
              <rect x="418" y="82" width="40" height="113" rx="5" fill="#D9F0FF" class="bar-rect" />
              <g class="bar-tooltip">
                <rect x="412" y="56" width="52" height="20" rx="10" fill="#83C9F4" />
                <text x="438" y="70" text-anchor="middle" fill="#FFFFFF" font-size="10" font-weight="bold">28.3h</text>
              </g>
            </g>

            <!-- 2月（峰值高亮） -->
            <g class="bar-group">
              <rect x="528" y="42" width="40" height="153" rx="5" fill="#D9F0FF" class="bar-rect" />
              <g class="bar-tooltip">
                <rect x="522" y="16" width="52" height="20" rx="10" fill="#83C9F4" />
                <text x="548" y="30" text-anchor="middle" fill="#FFFFFF" font-size="10" font-weight="bold">38.2h</text>
              </g>
            </g>

            <!-- 3月 -->
            <g class="bar-group">
              <rect x="638" y="112" width="40" height="83" rx="5" fill="#D9F0FF" class="bar-rect" />
              <g class="bar-tooltip">
                <rect x="632" y="86" width="52" height="20" rx="10" fill="#83C9F4" />
                <text x="658" y="100" text-anchor="middle" fill="#FFFFFF" font-size="10" font-weight="bold">20.8h</text>
              </g>
            </g>

            <!-- 横轴月份 -->
            <text x="108" y="222" text-anchor="middle" fill="#64748B" font-size="12">10月</text>
            <text x="218" y="222" text-anchor="middle" fill="#64748B" font-size="12">11月</text>
            <text x="328" y="222" text-anchor="middle" fill="#64748B" font-size="12">12月</text>
            <text x="438" y="222" text-anchor="middle" fill="#64748B" font-size="12">1月</text>
            <text x="548" y="222" text-anchor="middle" fill="#9381FF" font-size="12" font-weight="bold">2月</text>
            <text x="658" y="222" text-anchor="middle" fill="#64748B" font-size="12">3月</text>
          </svg>
        </div>

      </div>
    </div>

  </div>
</template>

<script setup>
import { Clock, Reading, Medal } from '@element-plus/icons-vue';
import { CURRENT_USER } from '../mockData.js';

const props = defineProps({
  user: {
    type: Object,
    default: () => CURRENT_USER
  }
});

const emit = defineEmits(['navigate', 'open-certificate-modal']);
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
  color: #64748B;
  margin-bottom: 4px;
}

.breadcrumb-sep {
  opacity: 0.5;
}

.breadcrumb-current {
  color: #005576;
  font-weight: 600;
}

.page-title {
  font-size: 28px;
  font-weight: 800;
  color: #1E293B;
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
  background: #fff;
  border: 1px solid #E2E8F0;
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
  color: #64748B;
}

.metric-label {
  font-size: 12px;
  color: #64748B;
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
  background: #D9F0FF;
  color: #005576;
}

.metric-icon--purple {
  background: #D9F0FF;
  color: #6555B0;
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
  color: #1E293B;
  letter-spacing: -0.025em;
}

.metric-number--purple {
  color: #6555B0;
}

.metric-unit {
  font-size: 12px;
  color: #64748B;
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
  background: #fff;
  border: 1px solid #E2E8F0;
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
  color: #1E293B;
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
  background: #83C9F4;
}

.legend-text {
  color: #64748B;
}

.chart-desc {
  font-size: 12px;
  color: #64748B;
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
  fill: #63B8E8;
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