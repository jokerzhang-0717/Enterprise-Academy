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
        @click="$emit('select-view', item.id)"
      >
        <div class="nav-item-left">
          <component :is="item.icon" class="sidebar-icon" />
          <span class="nav-label">{{ item.label }}</span>
        </div>
        <span
          v-if="item.badge !== undefined"
          :class="['nav-badge', item.badgeClass]"
        >
          {{ item.badge }}
        </span>
      </button>
    </nav>

    <!-- 底部用户卡片 -->
    <div class="sidebar-footer">
      <div class="user-card">
        <div class="user-card-info" @click="$emit('switch-user')" title="点击切换演示账号">
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
  unreadMessagesCount: { type: Number,  default: 3 },
  activeExamsCount:    { type: Number,  default: 1 },
  user: {
    type: Object,
    default: () => ({
      name: '李明',
      title: '高级研发工程师',
      department: '研发部'
    })
  }
});

defineEmits(['select-view', 'change-theme', 'logout', 'switch-user']);

const navItems = [
  { id: 'home',         label: '首页',           icon: House },
  { id: 'courses',      label: '课程学习中心',     icon: Reading },
  {
    id: 'exams',
    label: '考试与测评中心',
    icon: DocumentChecked,
    badge: props.activeExamsCount > 0 ? props.activeExamsCount : undefined,
    badgeClass: 'nav-badge--purple'
  },
  { id: 'analytics',    label: '个人学习看板',     icon: DataAnalysis },
  { id: 'attendance',   label: '培训考勤与请假',   icon: Calendar },
  {
    id: 'messages',
    label: '消息与站内信',
    icon: Bell,
    badge: props.unreadMessagesCount > 0 ? props.unreadMessagesCount : undefined,
    badgeClass: 'nav-badge--red'
  },
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
  background: #fff;
  border-right: 1px solid #E2E8F0;
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
  border-bottom: 1px solid #F1F5F9;
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
  background: #83C9F4;
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
  color: #fff;
}

.sidebar-user-icon {
  width: 20px;
  height: 20px;
  color: #005576;
}

.sidebar-logout-icon {
  width: 18px;
  height: 18px;
}

.brand-name {
  font-size: 16px;
  font-weight: 700;
  color: #1E293B;
  letter-spacing: -0.025em;
  line-height: 1.375;
}

.brand-sub {
  font-size: 10px;
  font-weight: 600;
  color: #64748B;
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
  color: #475569;
  background: transparent;
  border: none;
  border-radius: 8px;
  transition: background 0.15s, color 0.15s;
  text-align: left;
}

.nav-item:hover {
  background: #F4F9FD;
  color: #1E293B;
}

.nav-item--active {
  background: #83C9F4;
  color: #005576;
  font-weight: 600;
}

.nav-item--active:hover {
  background: #83C9F4;
  color: #005576;
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
  background: #9381FF;
  color: #1E293B;
}

.nav-badge--red {
  background: #DC2626;
  color: #fff;
}

/* ========== 底部用户卡片 ========== */
.sidebar-footer {
  margin-top: auto;
  padding: 12px;
  background: #FBFDFF;
  border-top: 1px solid #E2E8F0;
}

.user-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: #EEF4FB;
  border: 1px solid #E2EAF4;
  border-radius: 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  transition: background 0.15s;
}

.user-card:hover {
  background: #E5EFF9;
}

.user-card-info {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  flex: 1;
  cursor: pointer;
}

.user-card-avatar {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 50%;
  background: #83C9F4;
  color: #005576;
  flex-shrink: 0;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  transition: transform 0.15s;
}

.user-card-info:hover .user-card-avatar {
  transform: scale(1.05);
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
  color: #111C2D;
  line-height: 1.25;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-card-title {
  margin-top: 2px;
  font-size: 12px;
  color: #64748B;
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
  color: #334155;
  background: transparent;
  border: none;
  border-radius: 8px;
  transition: color 0.15s, background 0.15s;
  flex-shrink: 0;
  margin-left: 4px;
}

.logout-btn:hover {
  color: #DC2626;
  background: rgba(255, 255, 255, 0.7);
}
</style>