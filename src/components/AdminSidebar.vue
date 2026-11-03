<template>
  <aside class="admin-sidebar">

    <!-- 品牌头部 -->
    <div class="brand-header" @click="$emit('select-view', 'admin-dashboard')">
      <div class="brand-logo">
        <School class="sidebar-icon sidebar-brand-icon" />
        <span class="brand-logo-shield"></span>
      </div>
      <div class="brand-text">
        <div class="brand-name">云舒艺境</div>
        <div class="brand-sub">ADMIN CONSOLE · 管理端</div>
      </div>
    </div>

    <!-- 导航菜单 -->
    <nav class="nav-menu">
      <div class="nav-section-label">平台管理</div>

      <button
        v-for="item in navItems"
        :key="item.id"
        :class="['nav-item', isItemActive(item.id) ? 'nav-item--active' : '']"
        :title="item.label"
        @click="$emit('select-view', item.id)"
      >
        <div class="nav-item-left">
          <component :is="item.icon" class="sidebar-icon" />
          <span class="nav-label">{{ item.label }}</span>
        </div>
      </button>

    </nav>

    <!-- 底部管理员卡片 -->
    <div class="sidebar-footer">
      <div
        class="admin-card"
        @click="$emit('select-view', 'admin-profile')"
      >
        <div class="admin-card-avatar">
          <img v-if="user?.avatarUrl" :src="user.avatarUrl" alt="" class="admin-card-img" />
          <User v-else class="sidebar-icon sidebar-user-icon" />
          <span class="admin-card-dot"></span>
        </div>
        <div class="admin-card-text">
          <div class="admin-card-name">{{ user?.name || '王智远' }}</div>
          <div class="admin-card-title">{{ user?.title || '超级管理员' }}</div>
        </div>
        <button class="logout-btn" @click.stop="$emit('logout')" title="退出登录">
          <SwitchButton class="sidebar-icon sidebar-logout-icon" />
        </button>
      </div>
    </div>

  </aside>
</template>

<script setup>
import {
  School,
  Odometer,
  UserFilled,
  Collection,
  Tickets,
  Promotion,
  Calendar,
  Medal,
  OfficeBuilding,
  Clock,
  Memo,
  User,
  SwitchButton
} from '@element-plus/icons-vue';

const props = defineProps({
  currentView:    { type: String, default: 'admin-dashboard' },
  draftNoticeCount: { type: Number, default: 0 },
  activeExamCount:  { type: Number, default: 0 },
  pendingLeaveCount: { type: Number, default: 0 },
  pendingCertCount:  { type: Number, default: 0 },
  user: {
    type: Object,
    default: () => ({
      name: '王智远',
      title: '超级管理员',
      department: '平台运营部'
    })
  }
});

defineEmits(['select-view', 'logout']);

const navItems = [
  { id: 'admin-dashboard',      label: '管理总览',   icon: Odometer },
  { id: 'admin-students',       label: '学员管理',   icon: UserFilled },
  { id: 'admin-departments',    label: '部门管理',   icon: OfficeBuilding },
  { id: 'admin-courses',        label: '课程管理',   icon: Collection },
  { id: 'admin-exams',          label: '考试管理',   icon: Tickets },
  { id: 'admin-question-bank',  label: '题库管理',   icon: Memo },
  { id: 'admin-leave',          label: '请假审批',   icon: Calendar },
  { id: 'admin-checkin',        label: '签到记录',   icon: Clock },
  { id: 'admin-certificates',   label: '证书管理',   icon: Medal },
  { id: 'admin-announcements',  label: '公告管理',   icon: Promotion },
  { id: 'admin-profile',        label: '个人信息',   icon: User }
];

function isItemActive(id) {
  return props.currentView === id;
}
</script>

<style scoped>
/* ============================================================
   AdminSidebar · 管理员导航栏 · 纯原生 CSS
   ------------------------------------------------------------
   配色全部走 App.vue 的主题 token,随「换主题」按钮整体切换
     导航面   --app-nav-1 / -2 / -3 (深色渐变) --app-nav-ink
     身份色   --app-brand / -deep / -soft / -glow / -a12 / -a14
     文字     --app-nav-text / -text-hi / -text-active / -muted
   ============================================================ */

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

button {
  font-family: inherit;
  cursor: pointer;
}

/* ========== 侧边栏布局 ========== */
.admin-sidebar {
  width: 240px;
  height: 100vh;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  background: linear-gradient(180deg, var(--app-nav-ink) 0%, var(--app-nav-2) 60%, var(--app-nav-3) 100%);
  color: var(--app-nav-text-hi);
  user-select: none;
  z-index: 20;
  overflow: hidden;
}

/* ========== 品牌头部 ========== */
.brand-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px;
  border-bottom: 1px solid var(--app-brand-a14);
  cursor: pointer;
  transition: background 0.15s;
}

.brand-header:hover {
  background: rgba(255, 255, 255, 0.03);
}

.brand-logo {
  position: relative;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--app-brand) 0%, var(--app-brand-deep) 100%);
  color: var(--app-nav-ink);
  border-radius: 12px;
  box-shadow: 0 2px 8px var(--app-brand-glow);
  flex-shrink: 0;
}

.brand-logo-shield {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 12px;
  height: 12px;
  background: #2FBF8F;
  border: 2px solid var(--app-nav-ink);
  border-radius: 50%;
}

.sidebar-icon {
  display: block;
  width: 16px;
  height: 16px;
  flex: 0 0 auto;
}

.sidebar-brand-icon {
  width: 22px;
  height: 22px;
  color: var(--app-nav-ink);
}

.sidebar-user-icon {
  width: 20px;
  height: 20px;
  color: var(--app-brand);
}

.sidebar-logout-icon {
  width: 18px;
  height: 18px;
}

.brand-text {
  min-width: 0;
}

.brand-name {
  font-size: 16px;
  font-weight: 700;
  color: #F4F7FB;
  letter-spacing: -0.025em;
  line-height: 1.375;
}

.brand-sub {
  font-size: 9px;
  font-weight: 600;
  color: var(--app-brand);
  letter-spacing: 0.12em;
  text-transform: uppercase;
  white-space: nowrap;
}

/* ========== 导航菜单 ========== */
.nav-menu {
  flex: 1;
  padding: 16px 12px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-section-label {
  padding: 0 14px 8px;
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.18em;
  color: var(--app-nav-muted);
  text-transform: uppercase;
}

.nav-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 10px 14px;
  font-size: 14px;
  font-weight: 500;
  color: var(--app-nav-text);
  background: transparent;
  border: none;
  border-left: 3px solid transparent;
  border-radius: 8px;
  transition: background 0.15s, color 0.15s, border-color 0.15s;
  text-align: left;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.06);
  color: var(--app-nav-text-hi);
}

.nav-item--active {
  background: var(--app-brand-a12);
  border-left-color: var(--app-brand);
  color: var(--app-nav-text-active);
  font-weight: 600;
}

.nav-item--active:hover {
  background: var(--app-brand-a14);
  color: var(--app-nav-text-active);
}

.nav-item-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.nav-label {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ========== 徽章 ========== */
/* ========== 底部管理员卡片 ========== */
.sidebar-footer {
  margin-top: auto;
  padding: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.admin-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.09);
  border-radius: 16px;
  transition: background 0.15s;
}

.admin-card:hover {
  background: rgba(255, 255, 255, 0.08);
}

.admin-card-avatar {
  position: relative;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: visible;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--app-brand), var(--app-brand-deep));
  color: var(--app-nav-ink);
  flex-shrink: 0;
}

.admin-card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.admin-card-dot {
  position: absolute;
  bottom: -1px;
  right: -1px;
  width: 10px;
  height: 10px;
  background: #2FBF8F;
  border: 2px solid var(--app-nav-2);
  border-radius: 50%;
}

.admin-card-text {
  flex: 1;
  min-width: 0;
  margin-left: 10px;
}

.admin-card-name {
  font-size: 14px;
  font-weight: 700;
  color: #F4F7FB;
  line-height: 1.25;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.admin-card-title {
  margin-top: 2px;
  font-size: 11px;
  color: var(--app-nav-text);
  line-height: 1.25;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 退出按钮 */
.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 6px;
  color: var(--app-nav-text);
  background: transparent;
  border: none;
  border-radius: 8px;
  transition: color 0.15s, background 0.15s;
  flex-shrink: 0;
  margin-left: 4px;
}

.logout-btn:hover {
  color: #E85D75;
  background: rgba(255, 255, 255, 0.08);
}
</style>
