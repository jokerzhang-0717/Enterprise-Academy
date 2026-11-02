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
    icon: UserFilled, color: '--app-accent-deep', tint: '--app-accent-soft'
  },
  {
    label: '在架课程', value: COURSES_DATA.length,
    foot: '含 3 门必修红线课', trend: '+1', trendClass: 'up',
    icon: Collection, color: '--app-violet-fg', tint: '--app-violet-bg'
  },
  {
    label: '进行中考试', value: EXAMS_DATA.length,
    foot: '累计参考 1,286 人次', trend: '+1', trendClass: 'up',
    icon: Tickets, color: '--app-orange-fg', tint: '--app-orange-bg'
  },
  {
    label: '累计发证', value: issuedCerts.toLocaleString(),
    foot: '区块链存证已启用', trend: '链上', trendClass: 'flat',
    icon: Medal, color: '--app-amber-fg', tint: '--app-amber-bg'
  },
  {
    label: '今日签到率', value: '96.4%',
    foot: '应到 328 · 实到 316', trend: '+0.8%', trendClass: 'up',
    icon: Calendar, color: '--app-emerald-fg', tint: '--app-emerald-bg'
  },
  {
    label: '待审公告', value: draftNotices,
    foot: '含 1 篇停机维护通知', trend: '待发布', trendClass: 'warn',
    icon: Promotion, color: '--app-rose-fg', tint: '--app-rose-bg'
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
  color: var(--app-heading);
  letter-spacing: -0.02em;
  line-height: 1.3;
}

.page-sub {
  margin-top: 4px;
  font-size: 13px;
  color: var(--app-text-4);
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
  background: var(--app-surface);
  border: 1px solid var(--app-border-soft);
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
  color: var(--app-heading);
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
  color: var(--app-text-4);
}

.welcome-clock {
  flex-shrink: 0;
  padding-left: 26px;
  border-left: 1px solid var(--app-border-soft);
  text-align: right;
}

.clock-time {
  font-size: 34px;
  font-weight: 700;
  line-height: 1.1;
  color: var(--app-heading);
  letter-spacing: -0.02em;
  font-variant-numeric: tabular-nums;
}

.clock-date {
  margin-top: 5px;
  font-size: 12.5px;
  color: var(--app-text-5);
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
  background: linear-gradient(135deg, var(--app-brand), var(--app-brand-deep));
  color: var(--app-brand-on);
  border-color: var(--app-brand-deep);
  box-shadow: 0 2px 6px var(--app-brand-glow);
}
.btn--primary:hover { background: linear-gradient(135deg, var(--app-brand-hi), var(--app-brand-deep)); }

.btn--ghost {
  background: var(--app-surface);
  color: var(--app-text-2);
  border-color: var(--app-text-6);
}
.btn--ghost:hover { border-color: var(--app-brand); color: var(--app-brand-ink); }

/* ========== 统计卡片 ========== */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 14px;
}

.stat-card {
  padding: 16px;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
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
.stat-trend.up   { color: var(--app-emerald-fg); background: var(--app-emerald-bg); }
.stat-trend.flat { color: var(--app-accent-deep); background: var(--app-accent-soft); }
.stat-trend.warn { color: var(--app-rose-fg); background: var(--app-rose-bg); }

.stat-value {
  margin-top: 12px;
  font-size: 26px;
  font-weight: 800;
  color: var(--app-heading);
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.02em;
}

.stat-label {
  margin-top: 2px;
  font-size: 13px;
  font-weight: 600;
  color: var(--app-text-3);
}

.stat-foot {
  margin-top: 8px;
  font-size: 11px;
  color: var(--app-text-5);
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
  background: var(--app-surface);
  border: 1px solid var(--app-border);
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
  color: var(--app-heading);
}

.panel-sub {
  margin-top: 3px;
  font-size: 12px;
  color: var(--app-text-5);
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
  color: var(--app-text-3);
  flex-shrink: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chart-track {
  flex: 1;
  height: 14px;
  background: var(--app-surface-3);
  border-radius: 9999px;
  overflow: hidden;
}

.chart-bar {
  height: 100%;
  background: linear-gradient(90deg, var(--app-accent-strong), var(--app-accent));
  border-radius: 9999px;
  transition: width 0.5s ease-out;
}

.chart-value {
  width: 52px;
  font-size: 12px;
  font-weight: 700;
  color: var(--app-heading);
  font-variant-numeric: tabular-nums;
  text-align: right;
  flex-shrink: 0;
}

.chart-learners {
  width: 40px;
  font-size: 11px;
  color: var(--app-text-5);
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
  color: var(--app-rose-fg);
  background: var(--app-rose-bg);
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
  border-top: 1px solid var(--app-border-soft);
}
.todo-item:first-child { border-top: none; }

.todo-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.todo-dot--high { background: #E85D75; }
.todo-dot--mid  { background: var(--app-brand); }
.todo-dot--low  { background: #2FBF8F; }

.todo-text { flex: 1; min-width: 0; }

.todo-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--app-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.todo-meta {
  margin-top: 2px;
  font-size: 11px;
  color: var(--app-text-5);
}

.todo-action {
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 600;
  color: var(--app-accent-deep);
  background: var(--app-accent-soft);
  border: none;
  border-radius: 9999px;
  flex-shrink: 0;
  transition: background 0.15s;
}
.todo-action:hover { background: var(--app-accent-hover); }

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
  border-top: 1px solid var(--app-border-soft);
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

.activity-icon--course  { background: var(--app-accent-soft); color: var(--app-accent-deep); }
.activity-icon--user    { background: var(--app-violet-bg); color: var(--app-violet-fg); }
.activity-icon--exam    { background: var(--app-orange-bg); color: var(--app-orange-fg); }
.activity-icon--notice  { background: var(--app-amber-bg); color: var(--app-amber-fg); }
.activity-icon--approve { background: var(--app-emerald-bg); color: var(--app-emerald-fg); }

.activity-main { flex: 1; min-width: 0; }

.activity-text {
  font-size: 13px;
  font-weight: 500;
  color: var(--app-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.activity-meta {
  margin-top: 2px;
  font-size: 11px;
  color: var(--app-text-5);
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
    border-top: 1px solid var(--app-border-soft);
    text-align: left;
  }
}
</style>
