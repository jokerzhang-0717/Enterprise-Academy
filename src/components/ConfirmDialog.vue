<template>
  <Teleport to="body">
    <Transition name="cf">
      <div v-if="open" class="cf-backdrop" @click.self="emit('cancel')">
        <div class="cf-modal" role="dialog" aria-modal="true" :aria-label="title">

          <div class="cf-head">
            <span class="cf-icon" :class="`cf-icon--${tone}`">
              <component :is="iconComponent" />
            </span>
            <div class="cf-heading">
              <h2 class="cf-title">{{ title }}</h2>
              <p v-if="subtitle" class="cf-subtitle">{{ subtitle }}</p>
            </div>
          </div>

          <div v-if="rows.length" class="cf-body">
            <div v-for="row in rows" :key="row.label" class="cf-row">
              <span class="cf-key">{{ row.label }}</span>
              <span class="cf-val">{{ row.value }}</span>
            </div>
          </div>

          <div v-if="list.length" class="cf-body cf-body--list">
            <div v-for="(text, i) in list" :key="i" class="cf-list-row">
              <span class="cf-list-idx">{{ String(i + 1).padStart(2, '0') }}</span>
              <span class="cf-list-text">{{ text }}</span>
            </div>
          </div>

          <p v-if="tip" class="cf-tip" :class="`cf-tip--${tone}`">{{ tip }}</p>

          <div class="cf-actions">
            <button v-if="!hideCancel" class="cf-btn cf-btn--ghost" @click="emit('cancel')">
              {{ cancelText }}
            </button>
            <button ref="confirmBtn" class="cf-btn" :class="`cf-btn--${tone}`" @click="emit('confirm')">
              {{ confirmText }}
            </button>
          </div>

        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { WarningFilled, CircleCheckFilled, InfoFilled } from '@element-plus/icons-vue';

const props = defineProps({
  open:        { type: Boolean, default: true },
  tone:        { type: String,  default: 'danger' },   // danger | success | primary
  title:       { type: String,  required: true },
  subtitle:    { type: String,  default: '' },
  rows:        { type: Array,   default: () => [] },   // [{ label, value }]
  list:        { type: Array,   default: () => [] },   // ['规则一', '规则二']
  tip:         { type: String,  default: '' },
  confirmText: { type: String,  default: '确认' },
  cancelText:  { type: String,  default: '取消' },
  hideCancel:  { type: Boolean, default: false }
});

const emit = defineEmits(['confirm', 'cancel']);

const iconComponent = computed(() => {
  if (props.tone === 'success') return CircleCheckFilled;
  if (props.tone === 'primary') return InfoFilled;
  return WarningFilled;
});

const onKeydown = (e) => {
  if (e.key === 'Escape') {
    emit('cancel');
    return;
  }
  if (e.key === 'Enter') {
    e.preventDefault();
    emit('confirm');
  }
};

const confirmBtn = ref(null);

onMounted(() => {
  window.addEventListener('keydown', onKeydown);
  confirmBtn.value?.focus();
});
onUnmounted(() => window.removeEventListener('keydown', onKeydown));
</script>

<style scoped>
* { margin: 0; padding: 0; box-sizing: border-box; }

.cf-backdrop {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(15, 30, 51, 0.42);
  backdrop-filter: blur(3px);
}

.cf-modal {
  width: 100%;
  max-width: 420px;
  background: var(--app-surface);
  border-radius: 16px;
  box-shadow: 0 24px 60px rgba(15, 30, 51, 0.28);
  overflow: hidden;
}

/* ========== 头部 ========== */
.cf-head {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 24px 24px 0;
}

.cf-icon {
  flex-shrink: 0;
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}
.cf-icon svg { width: 22px; height: 22px; }

.cf-icon--danger  { color: #BE123C; background: #FFE4E6; }
.cf-icon--success { color: #047857; background: #D1FAE5; }
.cf-icon--primary { color: #B45309; background: #FEF3C7; }
.cf-icon--warn,
.cf-icon--warning { color: #D97706; background: var(--app-brand-tint); }

.cf-heading { min-width: 0; padding-top: 1px; }

.cf-title {
  font-size: 16px;
  font-weight: 700;
  line-height: 1.45;
  color: var(--app-heading);
  letter-spacing: -0.01em;
}

.cf-subtitle {
  margin-top: 5px;
  font-size: 12.5px;
  line-height: 1.5;
  color: var(--app-text-5);
}

/* ========== 信息行 ========== */
.cf-body {
  margin: 18px 24px 0;
  padding: 2px 14px;
  background: var(--app-surface-2);
  border: 1px solid var(--app-border-soft);
  border-radius: 12px;
}

.cf-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  padding: 10px 0;
  font-size: 13px;
  line-height: 1.5;
  border-bottom: 1px dashed var(--app-border-soft);
}
.cf-row:last-child { border-bottom: none; }

/* ========== 规则列表 ========== */
.cf-body--list { padding: 8px 0; }

.cf-list-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 7px 6px;
  font-size: 12.5px;
  line-height: 1.65;
}

.cf-list-idx {
  flex-shrink: 0;
  margin-top: 1px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.04em;
  color: #B45309;
  background: var(--app-brand-tint);
  border-radius: 5px;
  padding: 1px 5px;
}

.cf-list-text { color: var(--app-text-2); }

.cf-key {
  flex-shrink: 0;
  color: var(--app-text-5);
}

.cf-val {
  color: var(--app-text-2);
  font-weight: 600;
  text-align: right;
  word-break: break-word;
}

/* ========== 提示 ========== */
.cf-tip {
  margin: 14px 24px 0;
  padding: 10px 12px;
  font-size: 12px;
  line-height: 1.65;
  border-radius: 10px;
}
.cf-tip--danger  { color: #BE123C; background: #FFF1F2; border: 1px solid #FECDD3; }
.cf-tip--success { color: #047857; background: #F0FDF4; border: 1px solid #D1FAE5; }
.cf-tip--primary { color: #B45309; background: #FFFBEB; border: 1px solid #FDE68A; }
.cf-tip--warn,
.cf-tip--warning { color: #B45309; background: #FFFBEB; border: 1px solid #FDE68A; }

/* ========== 底部按钮 ========== */
.cf-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 20px 24px 22px;
}

.cf-btn {
  min-width: 96px;
  padding: 9px 20px;
  font-size: 13px;
  font-weight: 600;
  font-family: inherit;
  border: 1px solid transparent;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s, color 0.15s, transform 0.15s, box-shadow 0.15s;
}
.cf-btn:active { transform: translateY(1px); }
.cf-btn:focus-visible { outline: 2px solid rgba(148, 163, 184, 0.7); outline-offset: 2px; }

/* 默认兜底确认按钮样式，确保无论任何 tone 都高亮显眼 */
.cf-btn:not(.cf-btn--ghost) {
  color: #fff;
  background: linear-gradient(135deg, var(--app-brand), var(--app-brand-deep));
  box-shadow: 0 2px 8px var(--app-brand-a45);
}

.cf-btn--ghost {
  color: var(--app-text-3);
  background: var(--app-surface-2);
  border-color: var(--app-text-6);
}
.cf-btn--ghost:hover { background: var(--app-surface-2); border-color: var(--app-text-6); color: var(--app-text); }

.cf-btn--danger  { color: #fff; background: #BE123C; box-shadow: 0 2px 8px rgba(190, 18, 60, 0.35); }
.cf-btn--danger:hover  { background: #9F1239; box-shadow: 0 4px 12px rgba(190, 18, 60, 0.45); }

.cf-btn--success { color: #fff; background: #047857; box-shadow: 0 2px 8px rgba(4, 120, 87, 0.35); }
.cf-btn--success:hover { background: #036B4E; box-shadow: 0 4px 12px rgba(4, 120, 87, 0.45); }

.cf-btn--primary { color: var(--app-brand-on); background: linear-gradient(135deg, var(--app-brand), var(--app-brand-deep)); box-shadow: 0 2px 8px var(--app-brand-glow); }
.cf-btn--primary:hover { background: linear-gradient(135deg, var(--app-brand-hi), var(--app-brand-deep)); box-shadow: 0 4px 12px var(--app-brand-a45); }

.cf-btn--warn,
.cf-btn--warning {
  color: #fff !important;
  background: linear-gradient(135deg, #F59E0B, #D97706) !important;
  border-color: #D97706 !important;
  box-shadow: 0 3px 10px rgba(217, 119, 6, 0.4) !important;
}
.cf-btn--warn:hover,
.cf-btn--warning:hover {
  background: linear-gradient(135deg, #FBBF24, #D97706) !important;
  box-shadow: 0 5px 14px rgba(217, 119, 6, 0.5) !important;
}

/* ========== 动画 ========== */
.cf-enter-active,
.cf-leave-active { transition: opacity 0.18s ease; }

.cf-enter-active .cf-modal { animation: cf-in 0.22s cubic-bezier(0.2, 0.7, 0.2, 1); }
.cf-leave-active .cf-modal { transition: transform 0.16s ease, opacity 0.16s ease; }

.cf-enter-from,
.cf-leave-to { opacity: 0; }
.cf-leave-to .cf-modal { opacity: 0; transform: scale(0.97); }

@keyframes cf-in {
  from { opacity: 0; transform: translateY(8px) scale(0.97); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}
</style>
