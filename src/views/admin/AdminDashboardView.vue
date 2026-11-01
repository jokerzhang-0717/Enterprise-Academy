<template>
  <div class="admin-dashboard">

    <!-- 页头 -->
    <div class="page-head">
      <div class="head-copy">
        <h1 class="page-title">管理总览</h1>
        <p class="page-sub">平台运营数据全景 · 数据更新于 {{ todayText }}</p>
      </div>
      <div class="head-actions">
        <button class="btn btn--ghost" @click="$emit('navigate', 'admin-announcements')">
          <Promotion class="btn-icon" /> 发布公告
        </button>
      </div>
    </div>

    <!-- 欢迎卡 -->
    <section class="welcome-card">
      <div class="welcome-copy">
        <div class="welcome-title">
          {{ greeting }}，{{ userName }}
          <span class="welcome-wave">👋</span>
        </div>
        <p class="welcome-desc">
          平台数据已更新，愿今天的每项工作都推进顺利。
        </p>
      </div>
      <div class="welcome-clock">
        <div class="clock-time">{{ clockTime }}</div>
        <div class="clock-date">{{ clockDate }}</div>
      </div>
    </section>

    <!-- 统计卡片 -->
    <section class="stat-grid">
      <div v-for="card in statCards" :key="card.label" class="stat-card">
        <div class="stat-card-top">
          <div class="stat-icon" :style="{ background: card.tint, color: card.color }">
            <component :is="card.icon" />
          </div>
          <span class="stat-trend" :class="card.trendClass">{{ card.trend }}</span>
        </div>
        <div class="stat-value">{{ card.value }}</div>
        <div class="stat-label">{{ card.label }}</div>
        <div class="stat-foot">{{ card.foot }}</div>
      </div>
    </section>

    <!-- 图表 + 待办 -->
    <section class="dash-row">
      <!-- 部门学时柱状图 -->
      <article class="panel panel--chart">
        <div class="panel-head">
          <div>
            <h2 class="panel-title">各部门累计学时分布</h2>
            <p class="panel-sub">本季度 · 单位:小时</p>
          </div>
        </div>
        <div class="chart-body">
          <div v-for="dept in deptStats" :key="dept.department" class="chart-row">
            <span class="chart-label">{{ dept.department }}</span>
            <div class="chart-track">
              <div
                class="chart-bar"
                :style="{ width: barWidth(dept.hours) }"
              ></div>
            </div>
            <span class="chart-value">{{ dept.hours }}h</span>
            <span class="chart-learners">{{ dept.learners }}人</span>
          </div>
        </div>
      </article>

      <!-- 待办事项 -->
      <article class="panel panel--todo">
        <div class="panel-head">
          <div>
            <h2 class="panel-title">待办事项</h2>
            <p class="panel-sub">需要管理员处理</p>
          </div>
          <span class="todo-count">{{ todos.length }}</span>
        </div>
        <div class="todo-list">
          <div v-for="todo in todos" :key="todo.id" class="todo-item">
            <span class="todo-dot" :class="`todo-dot--${todo.level}`"></span>
            <div class="todo-text">
              <div class="todo-title">{{ todo.title }}</div>
              <div class="todo-meta">{{ todo.meta }}</div>
            </div>
            <button
              class="todo-action"
              @click="$emit('navigate', todo.view)"
            >{{ todo.action }}</button>
          </div>
        </div>
      </article>
    </section>

    <!-- 最近动态 -->
    <section class="panel">
      <div class="panel-head">
        <div>
          <h2 class="panel-title">最近管理动态</h2>
          <p class="panel-sub">平台操作审计日志</p>
        </div>
      </div>
      <div class="activity-list">
        <div v-for="act in activities" :key="act.id" class="activity-item">
          <span class="activity-icon" :class="`activity-icon--${act.type}`">
            <component :is="activityIcon(act.type)" />
          </span>
          <div class="activity-main">
            <div class="activity-text">{{ act.text }}</div>
            <div class="activity-meta">{{ act.operator }} · {{ act.time }}</div>
          </div>
        </div>
      </div>
    </section>

  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue';
import {
  UserFilled, Collection, Tickets, Medal, Calendar, Promotion,
  RefreshRight, Warning, Check, EditPen, OfficeBuilding
} from '@element-plus/icons-vue';
import {
  ADMIN_LEARNERS_DATA,
  ADMIN_DEPT_STATS,
  ADMIN_ACTIVITIES_DATA,
  ADMIN_ANNOUNCEMENTS_DATA,
  COURSES_DATA,
  EXAMS_DATA,
  CERTIFICATES_DATA
} from '../../mockData.js';

const props = defineProps({
  user: { type: Object, default: () => ({ name: '王智远' }) }
});

defineEmits(['navigate']);

const todayText = new Date().toLocaleDateString('zh-CN', {
  year: 'numeric', month: '2-digit', day: '2-digit'
});

/* ===== 欢迎卡 ===== */
const now = ref(new Date());
const userName = computed(() => props.user?.name || '管理员');

const greeting = computed(() => {
  const h = now.value.getHours();
  if (h < 6) return '夜深了';
  if (h < 9) return '早上好';
  if (h < 12) return '上午好';
  if (h < 14) return '中午好';
  if (h < 18) return '下午好';
  return '晚上好';
});

const pad = (n) => String(n).padStart(2, '0');
const clockTime = computed(
  () => `${pad(now.value.getHours())}:${pad(now.value.getMinutes())}:${pad(now.value.getSeconds())}`
);
const clockDate = computed(() => {
  const d = now.value;
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日星期${'日一二三四五六'[d.getDay()]}`;
});

let clockTimer = null;
onMounted(() => {
  clockTimer = setInterval(() => { now.value = new Date(); }, 1000);
});
onUnmounted(() => {
  if (clockTimer) clearInterval(clockTimer);
});

const activeLearners = ADMIN_LEARNERS_DATA.filter(l => l.status === 'active').length;
const draftNotices = ADMIN_ANNOUNCEMENTS_DATA.filter(a => a.status === 'draft').length;
const issuedCerts = CERTIFICATES_DATA.length * 100 + 386;

const statCards = computed(() => [
  {
    label: '注册学员', value: activeLearners,
    foot: `共 ${ADMIN_LEARNERS_DATA.length} 个账号`, trend: '+3.2%', trendClass: 'up',
    icon: UserFilled, color: '#005576', tint: '#D9F0FF'
  },
  {
    label: '在架课程', value: COURSES_DATA.length,
    foot: '含 3 门必修红线课', trend: '+1', trendClass: 'up',
    icon: Collection, color: '#6555B0', tint: '#EDE9FE'
  },
  {
    label: '进行中考试', value: EXAMS_DATA.length,
    foot: '累计参考 1,286 人次', trend: '+1', trendClass: 'up',
    icon: Tickets, color: '#9A3412', tint: '#FFEDD5'
  },
  {
    label: '累计发证', value: issuedCerts.toLocaleString(),
    foot: '区块链存证已启用', trend: '链上', trendClass: 'flat',
    icon: Medal, color: '#B45309', tint: '#FEF3C7'
  },
  {
    label: '今日签到率', value: '96.4%',
    foot: '应到 328 · 实到 316', trend: '+0.8%', trendClass: 'up',
    icon: Calendar, color: '#047857', tint: '#D1FAE5'
  },
  {
    label: '待审公告', value: draftNotices,
    foot: '含 1 篇停机维护通知', trend: '待发布', trendClass: 'warn',
    icon: Promotion, color: '#BE123C', tint: '#FFE4E6'
  }
]);

const deptStats = ADMIN_DEPT_STATS;
const maxHours = Math.max(...deptStats.map(d => d.hours));
const barWidth = (hours) => `${Math.max((hours / maxHours) * 100, 4)}%`;

const todos = [
  { id: 't-1', level: 'high', title: '3 条请假申请待审批', meta: '张浩然、刘子墨、陈嘉怡', view: 'admin-leave', action: '去审批' },
  { id: 't-2', level: 'high', title: '《2025数据安全与合规》考核今日 18:00 截止', meta: '仍有 87 人未作答', view: 'admin-exams', action: '去催办' },
  { id: 't-3', level: 'mid', title: '2 张证书待发放', meta: '陈嘉怡、王若琳 · 结课认证', view: 'admin-certificates', action: '去发放' },
  { id: 't-4', level: 'mid', title: '1 篇公告待发布', meta: '《平台 3.8.0 停机维护通知》草稿', view: 'admin-announcements', action: '去发布' }
];

const activities = ADMIN_ACTIVITIES_DATA;

const activityIconMap = {
  course:  Collection,
  user:    UserFilled,
  exam:    Tickets,
  notice:  Promotion,
  approve: Check,
  default: RefreshRight
};
const activityIcon = (type) => activityIconMap[type] || activityIconMap.default;
</script>

<style scoped>

* { margin: 0; padding: 0; box-sizing: border-box; }
button { font-family: inherit; cursor: pointer; }

.admin-dashboard {
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: 100%;
  min-height: 100%;
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.admin-dashboard::-webkit-scrollbar {
  width: 0;
  height: 0;
  display: none;
}

/* ========== 页头 ========== */
.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  padding-bottom: 4px;
}

.head-copy {
  flex: 1 1 auto;
  min-width: 0;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #0F1E33;
  letter-spacing: -0.02em;
  line-height: 1.3;
}

.page-sub {
  margin-top: 4px;
  font-size: 13px;
  color: #64748B;
}

.head-actions {
  display: flex;
  gap: 10px;
}

/* ========== 欢迎卡 ========== */
.welcome-card {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 22px 26px;
  background: #fff;
  border: 1px solid #E8EDF3;
  border-radius: 16px;
}

.welcome-copy {
  flex: 1 1 auto;
  min-width: 0;
}

.welcome-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 21px;
  font-weight: 700;
  color: #0F1E33;
  letter-spacing: -0.015em;
  line-height: 1.35;
}

.welcome-wave {
  font-size: 19px;
  line-height: 1;
}

.welcome-desc {
  margin-top: 6px;
  font-size: 14px;
  color: #64748B;
}

.welcome-clock {
  flex-shrink: 0;
  padding-left: 26px;
  border-left: 1px solid #EEF2F7;
  text-align: right;
}

.clock-time {
  font-size: 34px;
  font-weight: 700;
  line-height: 1.1;
  color: #0F1E33;
  letter-spacing: -0.02em;
  font-variant-numeric: tabular-nums;
}

.clock-date {
  margin-top: 5px;
  font-size: 12.5px;
  color: #94A3B8;
  font-variant-numeric: tabular-nums;
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
  transition: background 0.15s, border-color 0.15s, color 0.15s, transform 0.15s;
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

/* ========== 统计卡片 ========== */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 14px;
}

.stat-card {
  padding: 16px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  box-shadow: 0 1px 2px rgba(15, 30, 51, 0.04);
  transition: transform 0.15s, box-shadow 0.15s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(15, 30, 51, 0.08);
}

.stat-card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
}
.stat-icon :deep(svg) { width: 18px; height: 18px; }

.stat-trend {
  font-size: 11px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 9999px;
}
.stat-trend.up   { color: #047857; background: #D1FAE5; }
.stat-trend.flat { color: #005576; background: #D9F0FF; }
.stat-trend.warn { color: #BE123C; background: #FFE4E6; }

.stat-value {
  margin-top: 12px;
  font-size: 26px;
  font-weight: 800;
  color: #0F1E33;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.02em;
}

.stat-label {
  margin-top: 2px;
  font-size: 13px;
  font-weight: 600;
  color: #475569;
}

.stat-foot {
  margin-top: 8px;
  font-size: 11px;
  color: #94A3B8;
}

/* ========== 行布局 ========== */
.dash-row {
  display: grid;
  grid-template-columns: 3fr 2fr;
  gap: 14px;
  align-items: stretch;
}

/* ========== 面板 ========== */
.panel {
  padding: 18px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  box-shadow: 0 1px 2px rgba(15, 30, 51, 0.04);
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.panel-title {
  font-size: 15px;
  font-weight: 700;
  color: #0F1E33;
}

.panel-sub {
  margin-top: 3px;
  font-size: 12px;
  color: #94A3B8;
}

/* ========== 柱状图 ========== */
.chart-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chart-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.chart-label {
  width: 76px;
  font-size: 12px;
  font-weight: 600;
  color: #475569;
  flex-shrink: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chart-track {
  flex: 1;
  height: 14px;
  background: #F1F5F9;
  border-radius: 9999px;
  overflow: hidden;
}

.chart-bar {
  height: 100%;
  background: linear-gradient(90deg, #2E8BD6, #83C9F4);
  border-radius: 9999px;
  transition: width 0.5s ease-out;
}

.chart-value {
  width: 52px;
  font-size: 12px;
  font-weight: 700;
  color: #0F1E33;
  font-variant-numeric: tabular-nums;
  text-align: right;
  flex-shrink: 0;
}

.chart-learners {
  width: 40px;
  font-size: 11px;
  color: #94A3B8;
  text-align: right;
  flex-shrink: 0;
}

/* ========== 待办 ========== */
.todo-count {
  min-width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8px;
  font-size: 12px;
  font-weight: 700;
  color: #BE123C;
  background: #FFE4E6;
  border-radius: 9999px;
}

.todo-list {
  display: flex;
  flex-direction: column;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-top: 1px solid #F1F5F9;
}
.todo-item:first-child { border-top: none; }

.todo-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.todo-dot--high { background: #E85D75; }
.todo-dot--mid  { background: #E9B949; }
.todo-dot--low  { background: #2FBF8F; }

.todo-text { flex: 1; min-width: 0; }

.todo-title {
  font-size: 13px;
  font-weight: 600;
  color: #1E293B;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.todo-meta {
  margin-top: 2px;
  font-size: 11px;
  color: #94A3B8;
}

.todo-action {
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 600;
  color: #005576;
  background: #D9F0FF;
  border: none;
  border-radius: 9999px;
  flex-shrink: 0;
  transition: background 0.15s;
}
.todo-action:hover { background: #C2E4FA; }

/* ========== 最近动态 ========== */
.activity-list {
  display: flex;
  flex-direction: column;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-top: 1px solid #F1F5F9;
}
.activity-item:first-child { border-top: none; }

.activity-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 9px;
  flex-shrink: 0;
}
.activity-icon :deep(svg) { width: 15px; height: 15px; }

.activity-icon--course  { background: #D9F0FF; color: #005576; }
.activity-icon--user    { background: #EDE9FE; color: #6555B0; }
.activity-icon--exam    { background: #FFEDD5; color: #9A3412; }
.activity-icon--notice  { background: #FEF3C7; color: #B45309; }
.activity-icon--approve { background: #D1FAE5; color: #047857; }

.activity-main { flex: 1; min-width: 0; }

.activity-text {
  font-size: 13px;
  font-weight: 500;
  color: #1E293B;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.activity-meta {
  margin-top: 2px;
  font-size: 11px;
  color: #94A3B8;
}

/* ========== 响应式 ========== */
@media (max-width: 1200px) {
  .stat-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
}

@media (max-width: 900px) {
  .dash-row { grid-template-columns: 1fr; }
}

@media (max-width: 640px) {
  .stat-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .welcome-card { flex-wrap: wrap; }
  .welcome-clock {
    width: 100%;
    padding-left: 0;
    padding-top: 10px;
    border-left: 0;
    border-top: 1px solid #EEF2F7;
    text-align: left;
  }
}
</style>
