<template>
  <header class="app-header">

    <!-- ========== 搜索栏区域 ========== -->
    <div class="search-zone">

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

      <!-- 消息铃铛 -->
      <button
        class="bell-btn"
        title="消息与站内信"
        @click="$emit('navigate', 'messages')"
      >
        <!-- 铃铛图标（CSS 画的） -->
        <span class="icon-bell"></span>
        <!-- 未读红点 -->
        <span v-if="unreadCount > 0" class="unread-dot"></span>
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
              <div class="dropdown-user-id">{{ user.employeeId }} · {{ user.role }}</div>
            </div>
            <button
              class="dropdown-action dropdown-action--switch"
              @click="$emit('switch-user'); showUserDropdown = false"
            >
              <span class="icon-switch">⇄</span>
              <span>{{ isAdmin ? '返回学员端' : '切换视角 (张浩然 / 李明)' }}</span>
            </button>
            <button
              class="dropdown-action dropdown-action--logout"
              @click="$emit('navigate', 'login'); showUserDropdown = false"
            >
              <span>重新登录</span>
            </button>
          </div>
        </template>
      </div>

    </div>
  </header>
</template>

<script setup>
import { ref } from 'vue';

const props = defineProps({
  user:          { type: Object,  required: true },
  isAdmin:       { type: Boolean, default: false },
  isCheckedIn:   { type: Boolean, default: true },
  unreadCount:   { type: Number,  default: 3 },
  searchResults: { type: Array,   default: () => [] }
});

const emit = defineEmits([
  'navigate',
  'switch-user',
  'open-mockup',
  'toggle-checkin',
  'search',
  'select-search-result'
]);

const searchValue     = ref('');
const showSearchModal = ref(false);
const showUserDropdown = ref(false);

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
  background: #fff;
  border-bottom: 1px solid #E2E8F0;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
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
  background: #fff;
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
  color: #1E293B;
  background: transparent;
  border: none;
  outline: none;
}

.search-input::placeholder {
  color: #64748B;
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
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
  font-size: 12px;
}

.dropdown-title {
  margin-bottom: 8px;
  padding: 0 8px;
  font-size: 11px;
  font-weight: 600;
  color: #64748B;
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
  color: #1E293B;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-desc {
  margin-top: 4px;
  padding-left: 4px;
  font-size: 10px;
  color: #94A3B8;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-action {
  margin-left: 12px;
  font-size: 10px;
  color: #64748B;
  flex-shrink: 0;
}

.dropdown-empty {
  padding: 12px 8px;
  font-size: 11px;
  color: #94A3B8;
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
  color: #64748B;
  background: transparent;
  border: none;
  border-radius: 50%;
  transition: color 0.15s, background 0.15s;
}

.bell-btn:hover {
  color: #1E293B;
  background: #F1F5F9;
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
  background: #F8FAFC;
  border-color: #E2E8F0;
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
  color: #1E293B;
  line-height: 1.25;
}

.user-meta {
  margin-top: 2px;
  font-size: 10px;
  color: #64748B;
  line-height: 1;
}

/* --- 小箭头（CSS 画） --- */
.icon-arrow-down {
  width: 0;
  height: 0;
  border-left: 3.5px solid transparent;
  border-right: 3.5px solid transparent;
  border-top: 4px solid #94A3B8;
}

/* ========== 用户下拉菜单 ========== */
.user-dropdown {
  position: absolute;
  right: 0;
  top: 48px;
  z-index: 50;
  width: 224px;
  padding: 8px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  font-size: 12px;
}

.dropdown-user-info {
  padding: 8px 12px;
  margin-bottom: 4px;
  border-bottom: 1px solid #F1F5F9;
}

.dropdown-user-name {
  font-weight: 600;
  color: #1E293B;
}

.dropdown-user-id {
  margin-top: 2px;
  font-size: 11px;
  color: #64748B;
}

/* 下拉菜单按钮 */
.dropdown-action {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 8px 12px;
  font-size: 12px;
  background: transparent;
  border: none;
  border-radius: 8px;
  text-align: left;
  transition: background 0.15s;
}

/* 切换视角 */
.dropdown-action--switch {
  color: #005576;
  font-weight: 500;
}

.dropdown-action--switch:hover {
  background: #F4F9FD;
}

.icon-switch {
  font-size: 13px;
}

/* 重新登录 */
.dropdown-action--logout {
  margin-top: 4px;
  color: #DC2626;
}

/* 管理端标签 */
.admin-tag {
  display: inline-flex;
  align-items: center;
  margin-left: 6px;
  padding: 1px 7px;
  font-size: 10px;
  font-weight: 700;
  color: #8A6414;
  background: #FEF3C7;
  border: 1px solid #FDE68A;
  border-radius: 9999px;
  vertical-align: 1px;
}
</style>