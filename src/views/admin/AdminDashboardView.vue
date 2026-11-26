<template>
  <div class="admin-dashboard">

    <div class="page-head" v-reveal>
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

    <section class="welcome-card" v-reveal="{ delay: 30 }">
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
        <div class="clock-time" :aria-label="clockTime">
          <span
            v-for="(ch, i) in clockChars"
            :key="i"
            class="clock-digit-slot"
            :class="{ 'clock-digit-slot--sep': ch === ':' }"
          >
            <Transition v-if="ch !== ':'" name="digit-roll">
              <span :key="ch" class="clock-digit">{{ ch }}</span>
            </Transition>
            <span v-else class="clock-digit">:</span>
          </span>
        </div>
        <div class="clock-date">{{ clockDate }}</div>
      </div>
    </section>

    <section class="stat-grid" v-reveal-rows="{ rowDelay: 90, itemDelay: 50 }">
      <div
        v-for="card in statCards"
        :key="card.label"
        class="stat-card"
        role="button"
        tabindex="0"
        :aria-label="`${card.label}，点击查看详情`"
        @click="$emit('navigate', card.view)"
        @keydown.enter.prevent="$emit('navigate', card.view)"
        @keydown.space.prevent="$emit('navigate', card.view)"
      >
        <div class="stat-card-top">
          <div class="stat-icon" :style="{ background: card.tint, color: card.color }">
            <component :is="card.icon" />
          </div>
          <span v-if="card.trend" class="stat-trend" :class="card.trendClass">{{ card.trend }}</span>
        </div>
        <div class="stat-value"><span v-count-up="card.value"></span></div>
        <div class="stat-label">{{ card.label }}</div>
        <div class="stat-foot">{{ card.foot }}</div>
      </div>
    </section>

    <section class="dash-row">

      <article class="panel panel--chart" v-reveal="{ delay: 30 }">
        <div class="panel-head">
          <div>
            <h2 class="panel-title">各部门累计学时分布</h2>
            <p class="panel-sub">按学员学习记录统计 · 单位:小时</p>
          </div>
        </div>
        <div class="chart-body" v-reveal-rows="{ rowDelay: 90, itemDelay: 50 }">
          <div v-for="dept in deptStats" :key="dept.department" class="chart-row">
            <span class="chart-label">{{ dept.department }}</span>
            <div class="chart-track">
              <div
                class="chart-bar"
                :style="{ width: barsReady ? barWidth(dept.hours) : '0%' }"
              ></div>
            </div>
            <span class="chart-value">{{ dept.hours }}h</span>
            <span class="chart-learners">{{ dept.learners }}人</span>
          </div>
          <div v-if="deptStats.length === 0" class="chart-empty">暂无部门学员数据</div>
        </div>
      </article>

      <article class="panel panel--todo" v-reveal="{ delay: 60 }">
        <div class="panel-head">
          <div>
            <h2 class="panel-title">待办事项</h2>
            <p class="panel-sub">需要管理员处理</p>
          </div>
          <span class="todo-count">{{ todos.length }}</span>
        </div>
        <div class="todo-list" v-reveal-rows="{ rowDelay: 90, itemDelay: 50 }">
          <div
            v-for="todo in todos"
            :key="todo.id"
            class="todo-item"
            role="button"
            tabindex="0"
            :aria-label="`${todo.title}，${todo.action}`"
            @click="$emit('navigate', todo.view)"
            @keydown.enter.prevent="$emit('navigate', todo.view)"
            @keydown.space.prevent="$emit('navigate', todo.view)"
          >
            <span class="todo-dot" :class="`todo-dot--${todo.level}`"></span>
            <div class="todo-text">
              <div class="todo-title">{{ todo.title }}</div>
              <div class="todo-meta">{{ todo.meta }}</div>
            </div>
            <button
              class="todo-action"
              @click.stop="$emit('navigate', todo.view)"
            >{{ todo.action }}</button>
          </div>
        </div>
      </article>
    </section>

    <section class="panel" v-reveal="{ delay: 45 }">
      <div class="panel-head">
        <div>
          <h2 class="panel-title">最近管理动态</h2>
          <p class="panel-sub">平台操作审计日志</p>
        </div>
      </div>
      <div class="activity-list" v-reveal-rows="{ rowDelay: 90, itemDelay: 50 }">
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
import { adminStore } from '../../adminStore.js';
import { fetchAdminCheckins } from '../../api/index.js';
import { relativeTime } from '../../api/adapters.js';
import { useToast } from '../../composables/useToast.js';

const props = defineProps({
  user: { type: Object, default: () => ({ name: '王智远' }) }
});

defineEmits(['navigate']);

const todayText = new Date().toLocaleDateString('zh-CN', {
  year: 'numeric', month: '2-digit', day: '2-digit'
});

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

const clockChars = computed(() => clockTime.value.split(''));
const clockDate = computed(() => {
  const d = now.value;
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日星期${'日一二三四五六'[d.getDay()]}`;
});

let clockTimer = null;
const todayKey = (() => {
  const d = new Date();
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
})();
const todayCheckins = ref([]);
const { show: showToast } = useToast();

const refreshTodayCheckins = async () => {
  try {
    const page = await fetchAdminCheckins({ date: todayKey });
    todayCheckins.value = page?.records || [];
  } catch (error) {
    showToast(error?.message || '今日签到数据加载失败，请稍后重试。', 'error');
  }
};

onMounted(() => {
  clockTimer = setInterval(() => { now.value = new Date(); }, 1000);
  refreshTodayCheckins();

  requestAnimationFrame(() => requestAnimationFrame(() => { barsReady.value = true; }));
});
onUnmounted(() => {
  if (clockTimer) clearInterval(clockTimer);
});

const dash = computed(() => adminStore.dashboard || {});
const stats = computed(() => dash.value.stats || {});
const pending = computed(() => dash.value.pending || {});

const learners = computed(() => adminStore.users);
const activeLearners = computed(() => learners.value.filter(u => u.status === 'active').length);
const totalCourses = computed(() => adminStore.courses.length);
const attemptTotal = computed(() =>
  (dash.value.exams || []).reduce((sum, e) => sum + Number(e.attemptCount || 0), 0));

const attendedToday = computed(() =>
  todayCheckins.value.filter(c => Boolean(c.checkInTime)).length);
const todayRate = computed(() => {
  const total = activeLearners.value;
  if (!total) return '0.0%';
  return `${((attendedToday.value / total) * 100).toFixed(1)}%`;
});

const statCards = computed(() => [
  {
    label: '注册学员', value: activeLearners.value, view: 'admin-students',
    foot: `共 ${learners.value.length} 个账号`, trend: '', trendClass: 'flat',
    icon: UserFilled, color: '--app-accent-deep', tint: '--app-accent-soft'
  },
  {
    label: '在架课程', value: stats.value.publishedCourseCount ?? 0, view: 'admin-courses',
    foot: `共 ${totalCourses.value} 门课程`, trend: '', trendClass: 'flat',
    icon: Collection, color: '--app-violet-fg', tint: '--app-violet-bg'
  },
  {
    label: '进行中考试', value: pending.value.activeExamCount ?? 0, view: 'admin-exams',
    foot: `累计参考 ${attemptTotal.value} 人次`, trend: '', trendClass: 'flat',
    icon: Tickets, color: '--app-orange-fg', tint: '--app-orange-bg'
  },
  {
    label: '累计发证', value: adminStore.certificates.length, view: 'admin-certificates',
    foot: '区块链存证已启用', trend: '链上', trendClass: 'flat',
    icon: Medal, color: '--app-amber-fg', tint: '--app-amber-bg'
  },
  {
    label: '今日签到率', value: todayRate.value, view: 'admin-checkin',
    foot: `应到 ${activeLearners.value} · 实到 ${attendedToday.value}`, trend: '', trendClass: 'flat',
    icon: Calendar, color: '--app-emerald-fg', tint: '--app-emerald-bg'
  },
  {
    label: '待审公告', value: pending.value.draftNoticeCount ?? 0, view: 'admin-announcements',
    foot: `共 ${adminStore.announcements.length} 篇公告`, trend: '待发布', trendClass: 'warn',
    icon: Promotion, color: '--app-rose-fg', tint: '--app-rose-bg'
  }
]);

const deptStats = computed(() => adminStore.departments
  .filter(d => d.learners > 0)
  .map(d => ({
    department: d.name, hours: d.hours, learners: d.learners
  })));
const maxHours = computed(() => Math.max(1, ...deptStats.value.map(d => d.hours)));
const barWidth = (hours) => `${Math.max((hours / maxHours.value) * 100, 4)}%`;

const barsReady = ref(false);

const todos = computed(() => {
  const list = [];
  const pendingLeaves = adminStore.leaves.filter(l => l.status === 'pending');
  if (pendingLeaves.length) {
    list.push({
      id: 'todo-leave', level: 'high',
      title: `${pendingLeaves.length} 条请假申请待审批`,
      meta: pendingLeaves.slice(0, 3).map(l => l.applicant).join('、'),
      view: 'admin-leave', action: '去审批'
    });
  }
  const running = adminStore.exams.filter(e => e.state === 'running');
  if (running.length) {
    list.push({
      id: 'todo-exam', level: 'high',
      title: `${running.length} 场考试正在进行`,
      meta: running.slice(0, 2).map(e => e.title.slice(0, 18)).join('、'),
      view: 'admin-exams', action: '去查看'
    });
  }
  if (pending.value.pendingCertCount) {
    const holders = adminStore.certificates.filter(c => c.status === 'pending').map(c => c.holder);
    list.push({
      id: 'todo-cert', level: 'mid',
      title: `${pending.value.pendingCertCount} 张证书待发放`,
      meta: holders.slice(0, 3).join('、') || '结课认证',
      view: 'admin-certificates', action: '去发放'
    });
  }
  if (pending.value.draftNoticeCount) {
    const titles = adminStore.announcements.filter(a => a.status === 'draft').map(a => a.title);
    list.push({
      id: 'todo-notice', level: 'mid',
      title: `${pending.value.draftNoticeCount} 篇公告待发布`,
      meta: titles[0] || '草稿公告',
      view: 'admin-announcements', action: '去发布'
    });
  }
  return list;
});

const activities = computed(() => (dash.value.recentActivities || []).map(act => ({
  id: act.id,
  type: act.logType,
  text: act.content,
  operator: act.operatorName,
  time: relativeTime(act.occurredAt)
})));

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

:global(#app .admin-dashboard [data-scroll-reveal]) {
  --reveal-duration: 340ms;
  --reveal-distance: 12px;
}

* { margin: 0; padding: 0; box-sizing: border-box; }
button { font-family: inherit; cursor: pointer; }

.admin-dashboard {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 100%;
}

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
  display: inline-flex;
  align-items: center;
  font-size: 34px;
  font-weight: 700;
  line-height: 1.1;
  color: var(--app-heading);
  letter-spacing: -0.02em;
  font-variant-numeric: tabular-nums;
}

.clock-digit-slot {
  position: relative;
  display: inline-block;
  width: 0.62em;
  height: 1.1em;
  line-height: 1.1;
  overflow: hidden;
  text-align: center;
}

.clock-digit-slot--sep {
  width: 0.34em;
}

.clock-digit {
  display: block;
  width: 100%;
}

.digit-roll-enter-active,
.digit-roll-leave-active {
  transition: transform 0.28s cubic-bezier(0.22, 1, 0.36, 1), opacity 0.28s ease;
}

.digit-roll-enter-from {
  transform: translateY(100%);
  opacity: 0;
}

.digit-roll-leave-to {
  transform: translateY(-100%);
  opacity: 0;
}

.digit-roll-leave-active {
  position: absolute;
  left: 0;
  top: 0;
}

@media (prefers-reduced-motion: reduce) {
  .digit-roll-enter-active,
  .digit-roll-leave-active {
    transition: none;
  }
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

.stat-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 14px;
}

.stat-card {
  position: relative;
  padding: 16px;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 14px;
  box-shadow: 0 1px 2px rgba(15, 30, 51, 0.04);
  cursor: pointer;
  outline: none;
  transform: translateY(0);
  transition: transform 0.18s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.25s, border-color 0.25s;
}

.stat-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 18px 36px rgba(15, 30, 51, 0.16), 0 6px 12px rgba(15, 30, 51, 0.08);
  border-color: var(--app-accent);
}

.stat-card:focus-visible {
  border-color: var(--app-accent);
  box-shadow: 0 0 0 3px var(--app-accent-soft);
}

.stat-card:active {
  transform: translateY(-2px) scale(0.99);
  transition-duration: 0.1s;
}

@media (prefers-reduced-motion: reduce) {
  .stat-card,
  .stat-card:hover,
  .stat-card:active {
    transform: none !important;
    transition: none !important;
  }
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

.dash-row {
  display: grid;
  grid-template-columns: 3fr 2fr;
  gap: 14px;
  align-items: stretch;
}

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

.chart-empty {
  padding: 24px 0;
  color: var(--app-text-5);
  font-size: 13px;
  text-align: center;
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
  transition: width 0.7s cubic-bezier(0.22, 1, 0.36, 1);
}

@media (prefers-reduced-motion: reduce) {
  .chart-bar { transition: none; }
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
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  margin: 0 -12px;
  border-top: 1px solid var(--app-border-soft);
  border-radius: 8px;
  cursor: pointer;
  outline: none;
  transition: background 0.2s ease, transform 0.2s cubic-bezier(0.22, 1, 0.36, 1);
}

.todo-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  width: 3px;
  height: 0;
  border-radius: 9999px;
  background: var(--app-accent);
  transform: translateY(-50%);
  transition: height 0.25s cubic-bezier(0.22, 1, 0.36, 1);
}

.todo-item:first-child { border-top: none; }
.todo-item:hover,
.todo-item:focus-visible {
  background: var(--app-accent-soft);
  transform: translateX(3px);
}
.todo-item:hover::before,
.todo-item:focus-visible::before {
  height: 60%;
}
.todo-item:hover .todo-title,
.todo-item:focus-visible .todo-title { color: var(--app-accent-deep); }
.todo-item:focus-visible {
  box-shadow: 0 0 0 2px var(--app-ring-accent);
}

@media (prefers-reduced-motion: reduce) {
  .todo-item,
  .todo-item::before { transition: none; }
  .todo-item:hover,
  .todo-item:focus-visible { transform: none; }
}

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
