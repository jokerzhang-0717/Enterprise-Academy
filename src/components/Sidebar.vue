<template>
  <aside class="sidebar">

    <!-- 品牌头部 -->
    <div class="brand-header" @click="$emit('select-view', 'home')">
      <div class="brand-logo">
        <School class="sidebar-icon sidebar-brand-icon" />
      </div>
      <div class="brand-text">
        <div class="brand-name">云舒艺境</div>
        <div class="brand-sub">ENTERPRISE ACADEMY</div>
      </div>
    </div>

    <!-- 导航菜单 -->
    <nav class="nav-menu">
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

    <!-- 底部用户卡片 -->
    <div class="sidebar-footer">
      <div class="user-card">
        <div class="user-card-info">
          <div class="user-card-avatar">
            <img v-if="user?.avatarUrl" :src="user.avatarUrl" alt="" class="user-card-img" />
            <User v-else class="sidebar-icon sidebar-user-icon" />
          </div>
          <div class="user-card-text">
            <div class="user-card-name">{{ user?.name || '李明' }}</div>
            <div class="user-card-title">{{ user?.title || '高级研发工程师' }}</div>
          </div>
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
  House,
  Reading,
  DocumentChecked,
  DataAnalysis,
  Calendar,
  Bell,
  Medal,
  User,
  School,
  SwitchButton
} from '@element-plus/icons-vue';

const props = defineProps({
  currentView:         { type: String,  default: 'home' },
  theme:               { type: String,  default: 'cyan' },
  user: {
    type: Object,
    default: () => ({
      name: '李明',
      title: '高级研发工程师',
      department: '研发部'
    })
  }
});

defineEmits(['select-view', 'change-theme', 'logout']);

const navItems = [
  { id: 'home',         label: '首页',           icon: House },
  { id: 'courses',      label: '课程学习中心',     icon: Reading },
  { id: 'exams',        label: '考试与测评中心',   icon: DocumentChecked },
  { id: 'analytics',    label: '个人学习看板',     icon: DataAnalysis },
  { id: 'attendance',   label: '培训考勤与请假',   icon: Calendar },
  { id: 'messages',     label: '消息与站内信',     icon: Bell },
  { id: 'certificates', label: '荣誉证书与问答',   icon: Medal },
  { id: 'profile',      label: '个人中心',         icon: User }
];

function isItemActive(id) {
  return props.currentView === id ||
    (id === 'courses' && props.currentView === 'course-player') ||
    (id === 'exams' && props.currentView === 'exam-taking');
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

/* ========== 侧边栏布局 ========== */
.sidebar {
  width: 240px;
  height: 100vh;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  background: var(--app-surface);
  border-right: 1px solid var(--app-border);
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
  border-bottom: 1px solid var(--app-surface-3);
  cursor: pointer;
  transition: opacity 0.15s;
}

.brand-header:hover {
  opacity: 0.9;
}

.brand-logo {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--app-accent);
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  flex-shrink: 0;
}

.sidebar-icon {
  display: block;
  width: 16px;
  height: 16px;
  flex: 0 0 auto;
}

.sidebar-brand-icon {
  width: 24px;
  height: 24px;
  color: var(--app-on-fill);
}

.sidebar-user-icon {
  width: 20px;
  height: 20px;
  color: var(--app-accent-deep);
}

.sidebar-logout-icon {
  width: 18px;
  height: 18px;
}

.brand-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--app-text);
  letter-spacing: -0.025em;
  line-height: 1.375;
}

.brand-sub {
  font-size: 10px;
  font-weight: 600;
  color: var(--app-text-4);
  letter-spacing: 0.05em;
  text-transform: uppercase;
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

.nav-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 10px 14px;
  font-size: 14px;
  font-weight: 500;
  color: var(--app-text-3);
  background: transparent;
  border: none;
  border-radius: 8px;
  transition: background 0.15s, color 0.15s;
  text-align: left;
}

.nav-item:hover {
  background: var(--app-page-bg);
  color: var(--app-text);
}

.nav-item--active {
  background: var(--app-accent);
  color: var(--app-on-accent-solid);
  font-weight: 600;
}

.nav-item--active:hover {
  background: var(--app-accent);
  color: var(--app-on-accent-solid);
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
.nav-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  font-size: 11px;
  font-weight: 700;
  border-radius: 9999px;
  flex-shrink: 0;
}

.nav-badge--purple {
  background: var(--app-violet);
  color: var(--app-ink-solid);
}

.nav-badge--red {
  background: var(--app-red-600);
  color: var(--app-on-fill);
}

/* ========== 底部用户卡片 ========== */
.sidebar-footer {
  margin-top: auto;
  padding: 12px;
  background: var(--app-blue-25b);
  border-top: 1px solid var(--app-border);
}

.user-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: var(--app-blue-85);
  border: 1px solid var(--app-blue-line-110);
  border-radius: 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  transition: background 0.15s;
}

.user-card:hover {
  background: var(--app-blue-90);
}

.user-card-info {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  flex: 1;
}

.user-card-avatar {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 50%;
  background: var(--app-accent);
  color: var(--app-on-accent-solid);
  flex-shrink: 0;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.user-card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-card-text {
  min-width: 0;
}

.user-card-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--app-ink-850);
  line-height: 1.25;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-card-title {
  margin-top: 2px;
  font-size: 12px;
  color: var(--app-text-4);
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
  color: var(--app-text-2);
  background: transparent;
  border: none;
  border-radius: 8px;
  transition: color 0.15s, background 0.15s;
  flex-shrink: 0;
  margin-left: 4px;
}

.logout-btn:hover {
  color: var(--app-red-600);
  background: var(--app-wash-hover);
}

@media (max-width: 820px) {
  .sidebar {
    width: 64px;
    height: 100%;
  }
  .brand-header {
    justify-content: center;
    padding: 14px 6px;
  }
  .brand-text,
  .nav-label,
  .user-card-text { display: none; }
  .nav-menu {
    flex-direction: column;
    gap: 4px;
    padding: 10px 6px;
  }
  .nav-item {
    justify-content: center;
    padding: 12px 0;
  }
  .nav-item-left { gap: 0; }
  .sidebar-footer { padding: 6px; }
  .user-card {
    flex-direction: column;
    gap: 6px;
    padding: 8px 4px;
  }
  .logout-btn { margin-left: 0; }
}
</style>