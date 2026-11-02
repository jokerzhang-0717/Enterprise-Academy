<template>
  <header class="app-header" :class="{ 'app-header--admin': isAdmin }">

    <!-- ========== 搜索栏区域（管理端隐藏） ========== -->
    <div v-if="!isAdmin" class="search-zone">

      <div class="search-box">
        <!-- 放大镜图标（CSS 画的） -->
        <span class="icon-search"></span>
        <input
          type="text"
          class="search-input"
          v-model="searchValue"
          @focus="showSearchModal = true"
          @input="emit('search', searchValue)"
          @keydown.enter.prevent="selectFirstResult"
          @keydown.esc="showSearchModal = false"
          placeholder="搜索课程、考试、证书、通知..."
        />
      </div>

      <!-- 搜索结果下拉框 -->
      <template v-if="showSearchModal">
        <div class="backdrop" @click="showSearchModal = false"></div>
        <div class="search-dropdown">
          <div class="dropdown-title">
            {{ searchValue.trim() ? `搜索结果 (${searchResults.length})` : '输入关键词搜索项目内容' }}
          </div>

          <!-- 搜索结果列表 -->
          <div v-if="searchResults.length" class="dropdown-list">
            <button
              v-for="item in searchResults"
              :key="`${item.type}-${item.id}`"
              class="dropdown-item"
              @click="handleSelectSuggestion(item)"
            >
              <div class="item-info">
                <div class="item-row">
                  <span class="item-tag">{{ item.type }}</span>
                  <span class="item-title">{{ item.title }}</span>
                </div>
                <p class="item-desc">{{ item.description }}</p>
              </div>
              <span class="item-action">定位</span>
            </button>
          </div>

          <!-- 无结果 -->
          <p v-else-if="searchValue.trim()" class="dropdown-empty">
            没有找到匹配内容
          </p>
        </div>
      </template>
    </div>

    <!-- ========== 右侧操作区 ========== -->
    <div class="header-actions">

      <!-- 签到按钮 -->
      <button
        v-if="!isAdmin"
        :class="['checkin-btn', isCheckedIn ? 'checkin-btn--done' : 'checkin-btn--active']"
        @click="$emit('toggle-checkin')"
      >
        <!-- 打勾图标 -->
        <span :class="['icon-check', isCheckedIn ? 'icon-check--blue' : 'icon-check--white']">✓</span>
        <span>{{ isCheckedIn ? '今日培训已签到' : '立即打卡签到' }}</span>
      </button>

      <!-- 消息铃铛（管理端隐藏） -->
      <button
        v-if="!isAdmin"
        class="bell-btn"
        title="消息与站内信"
        @click="$emit('navigate', 'messages')"
      >
        <!-- 铃铛图标（CSS 画的） -->
        <span class="icon-bell"></span>
        <!-- 未读红点 -->
        <span v-if="unreadCount > 0" class="unread-dot"></span>
      </button>

      <!-- 管理端工具:全屏 -->
      <button
        v-if="isAdmin"
        class="tool-btn"
        :title="isFullscreen ? '退出全屏' : '进入全屏'"
        @click="toggleFullscreen"
      >
        <Minimize v-if="isFullscreen" class="tool-icon" />
        <Maximize v-else class="tool-icon" />
      </button>

      <!-- 管理端工具:换主题 -->
      <button
        v-if="isAdmin"
        class="tool-btn tool-btn--theme"
        title="换主题"
        @click="$emit('cycle-theme')"
      >
        <Palette class="tool-icon" />
      </button>

      <!-- 用户头像 + 下拉菜单 -->
      <div class="user-zone">
        <button class="user-btn" @click="showUserDropdown = !showUserDropdown">
          <!-- 头像 -->
          <div class="avatar">
            <img v-if="user.avatarUrl" :src="user.avatarUrl" alt="" class="avatar-img" />
            <span v-else>{{ user.name.charAt(0) }}</span>
          </div>
          <!-- 用户信息 -->
          <div class="user-info">
            <div class="user-name">
              {{ user.name }}
              <span v-if="isAdmin" class="admin-tag">管理端</span>
              <!-- 小箭头（CSS 画的） -->
              <span class="icon-arrow-down"></span>
            </div>
            <div class="user-meta">{{ user.department }} · {{ user.title }}</div>
          </div>
        </button>

        <!-- 下拉菜单 -->
        <template v-if="showUserDropdown">
          <div class="backdrop" @click="showUserDropdown = false"></div>
          <div class="user-dropdown">
            <div class="dropdown-user-info">
              <div class="dropdown-user-name">{{ user.name }}</div>
              <div v-if="!isAdmin" class="dropdown-user-id">{{ user.employeeId }} · {{ user.role }}</div>
            </div>
            <button
              class="dropdown-action"
              @click="$emit('navigate', isAdmin ? 'admin-profile' : 'profile'); showUserDropdown = false"
            >
              <span class="icon-switch">✎</span>
              <span>修改个人信息</span>
            </button>
            <div class="dropdown-divider"></div>
            <button
              class="dropdown-action dropdown-action--logout"
              @click="$emit('navigate', 'login'); showUserDropdown = false"
            >
              <span class="icon-switch">↩</span>
              <span>退出登录</span>
            </button>
          </div>
        </template>
      </div>

    </div>
  </header>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { Maximize, Minimize, Palette } from 'lucide-vue-next';

const props = defineProps({
  user:          { type: Object,  required: true },
  isAdmin:       { type: Boolean, default: false },
  isCheckedIn:   { type: Boolean, default: true },
  unreadCount:   { type: Number,  default: 3 },
  searchResults: { type: Array,   default: () => [] }
});

const emit = defineEmits([
  'navigate',
  'cycle-theme',
  'open-mockup',
  'toggle-checkin',
  'search',
  'select-search-result'
]);

const searchValue     = ref('');
const showSearchModal = ref(false);
const showUserDropdown = ref(false);

const isFullscreen = ref(false);
const syncFullscreen = () => { isFullscreen.value = !!document.fullscreenElement; };

const toggleFullscreen = async () => {
  try {
    if (document.fullscreenElement) await document.exitFullscreen();
    else await document.documentElement.requestFullscreen();
  } catch {
    /* 内嵌预览环境可能禁用全屏 API，静默忽略 */
  }
};

onMounted(() => document.addEventListener('fullscreenchange', syncFullscreen));
onBeforeUnmount(() => document.removeEventListener('fullscreenchange', syncFullscreen));

// 选中搜索结果
function handleSelectSuggestion(item) {
  emit('select-search-result', item);
  showSearchModal.value = false;
  searchValue.value = '';
  emit('search', '');
}

// 按回车选第一个结果
function selectFirstResult() {
  if (props.searchResults.length) {
    handleSelectSuggestion(props.searchResults[0]);
  }
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

input {
  font-family: inherit;
}

/* ========== 顶部导航栏 ========== */
.app-header {
  position: sticky;
  top: 0;
  z-index: 30;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 24px;
  background: var(--app-surface);
  border-bottom: 1px solid var(--app-border);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

/* 管理端只有头像,靠右显示 */
.app-header--admin {
  justify-content: flex-end;
}

/* 管理端底边:强调色 → 身份色 的渐变条,整条随主题走 */
.app-header--admin::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 2px;
  background: linear-gradient(90deg, var(--app-accent) 0%, var(--app-brand) 100%);
}

/* ========== 搜索栏区域 ========== */
.search-zone {
  position: relative;
  flex: 1;
  max-width: 576px;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 14px;
  background: var(--app-surface);
  border: 1px solid #A3D5FF;
  border-radius: 9999px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  transition: border-color 0.2s, box-shadow 0.2s;
}

.search-box:focus-within {
  border-color: #83C9F4;
  box-shadow: 0 0 0 4px rgba(131, 201, 244, 0.2);
}

.search-input {
  flex: 1;
  min-width: 0;
  padding: 10px 0;
  font-size: 14px;
  color: var(--app-text);
  background: transparent;
  border: none;
  outline: none;
}

.search-input::placeholder {
  color: var(--app-text-4);
}

/* --- 放大镜图标（CSS 画） --- */
.icon-search {
  position: relative;
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.icon-search::before {
  content: '';
  position: absolute;
  top: 1px;
  left: 1px;
  width: 10px;
  height: 10px;
  border: 2px solid #005576;
  border-radius: 50%;
}

.icon-search::after {
  content: '';
  position: absolute;
  bottom: 1px;
  right: 1px;
  width: 6px;
  height: 2px;
  background: #005576;
  border-radius: 1px;
  transform: rotate(45deg);
  transform-origin: right center;
}

/* ========== 搜索下拉框 ========== */
.backdrop {
  position: fixed;
  inset: 0;
  z-index: 40;
}

.search-dropdown {
  position: absolute;
  top: 48px;
  left: 0;
  right: 0;
  z-index: 50;
  padding: 12px;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 12px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
  font-size: 12px;
}

.dropdown-title {
  margin-bottom: 8px;
  padding: 0 8px;
  font-size: 11px;
  font-weight: 600;
  color: var(--app-text-4);
}

.dropdown-list {
  max-height: 320px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.dropdown-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 8px;
  background: transparent;
  border: none;
  border-radius: 8px;
  text-align: left;
  transition: background 0.15s;
}

.dropdown-item:hover {
  background: #F4F9FD;
}

.item-info {
  min-width: 0;
}

.item-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.item-tag {
  padding: 2px 6px;
  font-size: 10px;
  font-weight: 500;
  color: #005576;
  background: #D9F0FF;
  border-radius: 4px;
  flex-shrink: 0;
}

.item-title {
  color: var(--app-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-desc {
  margin-top: 4px;
  padding-left: 4px;
  font-size: 10px;
  color: var(--app-text-5);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-action {
  margin-left: 12px;
  font-size: 10px;
  color: var(--app-text-4);
  flex-shrink: 0;
}

.dropdown-empty {
  padding: 12px 8px;
  font-size: 11px;
  color: var(--app-text-5);
}

/* ========== 右侧操作区 ========== */
.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

/* ========== 签到按钮 ========== */
.checkin-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  font-size: 12px;
  font-weight: 500;
  border-radius: 9999px;
  border: 1px solid transparent;
  transition: background 0.15s, border-color 0.15s;
}

/* 已签到状态 */
.checkin-btn--done {
  color: #005576;
  background: #D9F0FF;
  border-color: rgba(131, 201, 244, 0.5);
}

.checkin-btn--done:hover {
  background: #D9F0FF;
}

/* 未签到状态 */
.checkin-btn--active {
  color: #005576;
  background: #83C9F4;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.checkin-btn--active:hover {
  background: #6FB9E8;
}

/* 打勾图标 */
.icon-check {
  font-size: 13px;
  font-weight: 700;
  line-height: 1;
}

.icon-check--blue {
  color: #005576;
}

.icon-check--white {
  color: #fff;
}

/* ========== 铃铛按钮 ========== */
.bell-btn {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
  color: var(--app-text-4);
  background: transparent;
  border: none;
  border-radius: 50%;
  transition: color 0.15s, background 0.15s;
}

.bell-btn:hover {
  color: var(--app-text);
  background: var(--app-surface-3);
}

/* --- 管理端工具按钮（全屏 / 换主题） --- */
.tool-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  color: var(--app-text-4);
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 10px;
  transition: color 0.15s, border-color 0.15s, background 0.15s;
}

.tool-btn:hover {
  color: var(--app-accent-deep);
  border-color: var(--app-accent);
  background: var(--app-accent-soft);
}

.tool-btn--theme {
  color: var(--app-accent-deep);
  border-color: var(--app-accent);
  background: var(--app-accent-soft);
}

.tool-icon {
  width: 16px;
  height: 16px;
}

.app-header--admin .avatar {
  background: var(--app-brand);
  color: var(--app-brand-on);
}

/* --- 铃铛图标（CSS 画） --- */
.icon-bell {
  position: relative;
  width: 16px;
  height: 16px;
}

/* 铃铛主体（半圆） */
.icon-bell::before {
  content: '';
  position: absolute;
  top: 1px;
  left: 2px;
  width: 12px;
  height: 12px;
  border: 1.5px solid currentColor;
  border-bottom: none;
  border-radius: 6px 6px 0 0;
}

/* 铃铛底部横线 + 小圆 */
.icon-bell::after {
  content: '';
  position: absolute;
  bottom: 1px;
  left: 3px;
  width: 10px;
  height: 1.5px;
  background: currentColor;
  border-radius: 1px;
  box-shadow: 0 3px 0 0 currentColor;
}

/* 未读红点 */
.unread-dot {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 8px;
  height: 8px;
  background: #DC2626;
  border-radius: 50%;
}

/* ========== 用户头像区域 ========== */
.user-zone {
  position: relative;
}

.user-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 4px 4px 8px;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 9999px;
  transition: background 0.15s, border-color 0.15s;
}

.user-btn:hover {
  background: var(--app-surface-2);
  border-color: var(--app-text-6);
}

/* 头像 */
.avatar {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 50%;
  background: #83C9F4;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  border: 2px solid #fff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  flex-shrink: 0;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 用户文字信息 */
.user-info {
  text-align: left;
}

.user-name {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 700;
  color: var(--app-text);
  line-height: 1.25;
}

.user-meta {
  margin-top: 2px;
  font-size: 10px;
  color: var(--app-text-4);
  line-height: 1;
}

/* --- 小箭头（CSS 画） --- */
.icon-arrow-down {
  width: 0;
  height: 0;
  border-left: 3.5px solid transparent;
  border-right: 3.5px solid transparent;
  border-top: 4px solid var(--app-text-5);
}

/* ========== 用户下拉菜单 ========== */
.user-dropdown {
  position: absolute;
  right: 0;
  top: 48px;
  z-index: 50;
  width: 232px;
  padding: 6px;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 14px;
  box-shadow: 0 4px 6px -2px rgba(15, 23, 42, 0.05), 0 12px 28px -6px rgba(15, 23, 42, 0.16);
  font-size: 13px;
  animation: dropdown-in 0.14s ease-out;
}

@keyframes dropdown-in {
  from { opacity: 0; transform: translateY(-4px); }
  to   { opacity: 1; transform: translateY(0); }
}

.dropdown-user-info {
  padding: 9px 10px 10px;
}

.dropdown-user-name {
  font-size: 13px;
  font-weight: 600;
  line-height: 1.3;
  color: #0F172A;
}

.dropdown-user-id {
  margin-top: 3px;
  font-size: 11px;
  line-height: 1.3;
  color: var(--app-text-4);
}

.dropdown-divider {
  height: 1px;
  margin: 5px 8px;
  background: var(--app-surface-2);
}

/* 下拉菜单按钮 */
.dropdown-action {
  display: flex;
  align-items: center;
  gap: 9px;
  width: 100%;
  padding: 9px 10px;
  font-size: 13px;
  color: var(--app-text-2);
  background: transparent;
  border: none;
  border-radius: 10px;
  text-align: left;
  transition: background 0.15s, color 0.15s;
}

.dropdown-action:hover {
  background: var(--app-surface-3);
  color: #0F172A;
}

.dropdown-action:focus-visible {
  outline: 2px solid #005576;
  outline-offset: -2px;
}

.icon-switch {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  flex: none;
  font-size: 13px;
  line-height: 1;
  color: var(--app-text-5);
}

/* 退出登录 */
.dropdown-action--logout {
  color: #DC2626;
}

.dropdown-action--logout:hover {
  background: #FEF2F2;
  color: #B91C1C;
}

.dropdown-action--logout .icon-switch {
  color: #FCA5A5;
}

.dropdown-action--logout:hover .icon-switch {
  color: #DC2626;
}

/* 管理端:下拉浮层跟随主题,深色主题下不出现浅底深字;学员端沿用上面的原色 */
.app-header--admin .dropdown-user-name {
  color: var(--app-heading);
}

.app-header--admin .dropdown-action:hover {
  color: var(--app-heading);
}

.app-header--admin .dropdown-action:focus-visible {
  outline-color: var(--app-accent-focus);
}

.app-header--admin .dropdown-action--logout {
  color: var(--app-rose-fg);
}

.app-header--admin .dropdown-action--logout:hover {
  background: var(--app-rose-bg);
  color: var(--app-rose-fg);
}

.app-header--admin .dropdown-action--logout .icon-switch {
  color: var(--app-rose-line);
}

.app-header--admin .dropdown-action--logout:hover .icon-switch {
  color: var(--app-rose-fg);
}

/* 管理端标签 */
.admin-tag {
  display: inline-flex;
  align-items: center;
  margin-left: 6px;
  padding: 1px 7px;
  font-size: 10px;
  font-weight: 700;
  color: var(--app-brand-ink);
  background: var(--app-brand-tint);
  border: 1px solid var(--app-brand-tint-line);
  border-radius: 9999px;
  vertical-align: 1px;
}
</style>