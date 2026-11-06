<template>
  <!-- 原型图查看弹窗 -->
  <div v-if="isOpen" class="modal-overlay">

    <div class="modal-container">

      <!-- 顶部标题栏 -->
      <div class="modal-header">
        <div class="header-left">
          <div class="header-icon">
            <!-- 图片图标（CSS 画的） -->
            <span class="icon-image"></span>
          </div>
          <div class="header-text">
            <div class="header-title">
              <span>原型设计对照图谱 (共 10 屏)</span>
              <span class="page-counter">{{ currentIndex + 1 }} / {{ mockupKeys.length }}</span>
            </div>
            <div class="header-subtitle">{{ currentMockup.title }}</div>
          </div>
        </div>

        <div class="header-actions">
          <a
            class="btn-link"
            :href="currentMockup.path"
            target="_blank"
            rel="noreferrer"
          >
            <!-- 外链图标（CSS 画的） -->
            <span class="icon-external"></span>
            <span>新标签页打开原图</span>
          </a>
          <button class="btn-close" @click="$emit('close')">
            <span class="icon-x"></span>
          </button>
        </div>
      </div>

      <!-- 缩略图切换栏 -->
      <div class="thumb-bar">
        <button
          v-for="key in mockupKeys"
          :key="key"
          :class="['thumb-btn', selectedKey === key ? 'thumb-btn--active' : '']"
          @click="selectedKey = key"
        >
          {{ SCREEN_MOCKUPS[key].title.split(' ')[0] }}
        </button>
      </div>

      <!-- 图片展示区 -->
      <div class="viewer-stage">

        <!-- 上一张 -->
        <button class="nav-arrow nav-arrow--prev" @click="prevMockup" title="上一张">
          <span class="icon-chevron-left"></span>
        </button>

        <!-- 图片主体 -->
        <div class="viewer-content">
          <img
            :src="currentMockup.path"
            :alt="currentMockup.title"
            class="viewer-image"
          />
          <p class="viewer-desc">{{ currentMockup.desc }}</p>
        </div>

        <!-- 下一张 -->
        <button class="nav-arrow nav-arrow--next" @click="nextMockup" title="下一张">
          <span class="icon-chevron-right"></span>
        </button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { SCREEN_MOCKUPS } from '../mockData.js';

const props = defineProps({
  isOpen:    { type: Boolean, default: false },
  initialKey:{ type: String,  default: 'home' }
});

defineEmits(['close']);

const mockupKeys  = Object.keys(SCREEN_MOCKUPS);
const selectedKey = ref(props.initialKey || 'home');

// 监听外部传入的 initialKey 变化
watch(() => props.initialKey, (newVal) => {
  if (newVal && SCREEN_MOCKUPS[newVal]) {
    selectedKey.value = newVal;
  }
});

// 当前展示的原型图
const currentMockup = computed(() => SCREEN_MOCKUPS[selectedKey.value] || SCREEN_MOCKUPS.home);

// 当前索引
const currentIndex = computed(() => mockupKeys.indexOf(selectedKey.value));

// 上一张
function prevMockup() {
  const idx = (currentIndex.value - 1 + mockupKeys.length) % mockupKeys.length;
  selectedKey.value = mockupKeys[idx];
}

// 下一张
function nextMockup() {
  const idx = (currentIndex.value + 1) % mockupKeys.length;
  selectedKey.value = mockupKeys[idx];
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

a {
  text-decoration: none;
  font-family: inherit;
}

/* ========== 弹窗遮罩 ========== */
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: rgba(15, 23, 42, 0.7);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to   { opacity: 1; }
}

/* ========== 弹窗主体 ========== */
.modal-container {
  width: 100%;
  max-width: 1152px;
  max-height: 92vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 16px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

/* ========== 顶部标题栏 ========== */
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: var(--app-surface-2);
  border-bottom: 1px solid var(--app-border);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 标题图标 */
.header-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(131, 201, 244, 0.2);
  color: var(--app-accent-deep);
  border-radius: 8px;
  flex-shrink: 0;
}

/* --- 图片图标（CSS 画的） --- */
.icon-image {
  position: relative;
  width: 16px;
  height: 16px;
  border: 1.5px solid var(--app-accent-deep);
  border-radius: 3px;
}

.icon-image::before {
  content: '';
  position: absolute;
  top: 2px;
  left: 2px;
  width: 4px;
  height: 4px;
  background: var(--app-accent-deep);
  border-radius: 50%;
}

.icon-image::after {
  content: '';
  position: absolute;
  bottom: 2px;
  left: 1px;
  width: 10px;
  height: 6px;
  background: var(--app-accent-deep);
  clip-path: polygon(0 100%, 40% 20%, 65% 55%, 85% 35%, 100% 100%);
}

.header-text {
  min-width: 0;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 700;
  color: var(--app-text);
}

.page-counter {
  padding: 2px 8px;
  font-size: 12px;
  font-family: "SFMono-Regular", Consolas, Menlo, monospace;
  color: var(--app-accent-deep);
  background: var(--app-accent-soft);
  border-radius: 9999px;
}

.header-subtitle {
  margin-top: 2px;
  font-size: 12px;
  color: var(--app-text-4);
}

/* ========== 右侧操作按钮 ========== */
.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-link {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 500;
  color: var(--app-text-3);
  background: transparent;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  transition: color 0.15s, background 0.15s;
}

.btn-link:hover {
  color: var(--app-accent-deep);
  background: var(--app-surface);
}

.btn-close {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 6px;
  color: var(--app-text-4);
  background: transparent;
  border: none;
  border-radius: 8px;
  transition: color 0.15s, background 0.15s;
}

.btn-close:hover {
  color: var(--app-text);
  background: var(--app-border);
}

/* --- 外链图标（CSS 画的） --- */
.icon-external {
  position: relative;
  width: 14px;
  height: 14px;
  display: inline-block;
}

.icon-external::before {
  content: '';
  position: absolute;
  top: 3px;
  right: 0;
  width: 7px;
  height: 7px;
  border-top: 1.5px solid currentColor;
  border-right: 1.5px solid currentColor;
}

.icon-external::after {
  content: '';
  position: absolute;
  bottom: 2px;
  left: 1px;
  width: 10px;
  height: 1.5px;
  background: currentColor;
  transform: rotate(-45deg);
  transform-origin: left center;
}

/* --- 叉号 ×（CSS 画的） --- */
.icon-x {
  position: relative;
  width: 20px;
  height: 20px;
  display: inline-block;
}

.icon-x::before,
.icon-x::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 16px;
  height: 2px;
  background: currentColor;
  border-radius: 1px;
}

.icon-x::before {
  transform: translate(-50%, -50%) rotate(45deg);
}

.icon-x::after {
  transform: translate(-50%, -50%) rotate(-45deg);
}

/* ========== 缩略图切换栏 ========== */
.thumb-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 24px;
  background: var(--app-surface-3);
  border-bottom: 1px solid var(--app-border);
  overflow-x: auto;
  flex-shrink: 0;
}

.thumb-btn {
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 500;
  color: var(--app-text-3);
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 8px;
  white-space: nowrap;
  transition: background 0.15s, color 0.15s, border-color 0.15s;
}

.thumb-btn:hover {
  background: var(--app-border);
}

.thumb-btn--active {
  color: var(--app-accent-deep);
  background: var(--app-accent);
  border-color: var(--app-accent);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

/* ========== 图片展示区 ========== */
.viewer-stage {
  position: relative;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: auto;
  padding: 24px;
  background: rgba(11, 19, 43, 0.05);
}

.viewer-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  max-width: 100%;
  max-height: 100%;
}

.viewer-image {
  max-height: 68vh;
  object-fit: contain;
  border-radius: 12px;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--app-border-strong);
}

.viewer-desc {
  margin-top: 12px;
  max-width: 672px;
  font-size: 12px;
  color: var(--app-text-4);
  text-align: center;
}

/* ========== 左右翻页按钮 ========== */
.nav-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid var(--app-border);
  border-radius: 50%;
  color: var(--app-text);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  transition: background 0.15s, transform 0.15s;
  z-index: 10;
}

.nav-arrow:hover {
  background: var(--app-surface);
  transform: translateY(-50%) scale(1.05);
}

.nav-arrow--prev {
  left: 16px;
}

.nav-arrow--next {
  right: 16px;
}

/* --- 左箭头 <（CSS 画的） --- */
.icon-chevron-left {
  width: 0;
  height: 0;
  border-top: 6px solid transparent;
  border-bottom: 6px solid transparent;
  border-right: 8px solid var(--app-text);
  margin-right: 2px;
}

/* --- 右箭头 >（CSS 画的） --- */
.icon-chevron-right {
  width: 0;
  height: 0;
  border-top: 6px solid transparent;
  border-bottom: 6px solid transparent;
  border-left: 8px solid var(--app-text);
  margin-left: 2px;
}
</style>