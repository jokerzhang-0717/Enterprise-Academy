<template>
  <Teleport to="body">
    <div class="toast-stack" role="status" aria-live="polite">
      <TransitionGroup name="toast">
        <div
          v-for="item in toasts"
          :key="item.id"
          class="toast"
          :class="`toast--${item.type}`"
        >
          <span class="toast-icon">
            <component :is="iconOf(item.type)" />
          </span>
          <span class="toast-text">{{ item.message }}</span>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<script setup>
import { WarningFilled, CircleCheckFilled, InfoFilled } from '@element-plus/icons-vue';
import { useToast } from '../composables/useToast.js';

const { toasts } = useToast();

const iconMap = {
  error:   WarningFilled,
  success: CircleCheckFilled,
  info:    InfoFilled
};
const iconOf = (type) => iconMap[type] || InfoFilled;
</script>

<style scoped>
* { box-sizing: border-box; }

.toast-stack {
  position: fixed;
  top: 24px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 300;
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: center;
  pointer-events: none;
}

.toast {
  display: flex;
  align-items: center;
  gap: 10px;
  max-width: 440px;
  padding: 12px 16px;
  background: var(--app-surface);
  border: 1px solid var(--app-overlay-border);
  border-radius: 12px;
  box-shadow: 0 14px 34px rgba(15, 30, 51, 0.16);
}

.toast-icon {
  display: inline-flex;
  flex-shrink: 0;
}
.toast-icon svg { width: 18px; height: 18px; }

.toast-text {
  font-size: 13px;
  line-height: 1.6;
  color: var(--app-text-2);
}

.toast--error .toast-icon   { color: #BE123C; }
.toast--success .toast-icon { color: #047857; }
.toast--info .toast-icon    { color: #B45309; }

.toast-enter-active,
.toast-leave-active { transition: opacity 0.2s ease, transform 0.2s ease; }
.toast-enter-from,
.toast-leave-to { opacity: 0; transform: translateY(-8px); }
</style>
