<template>
  <Teleport to="body">
    <div class="overlay" @click.self="$emit('close')">
      <div class="modal" role="dialog" aria-modal="true" :aria-label="title">
        <div class="modal-head">
          <div>
            <h3 class="modal-title">{{ title }}</h3>
            <p v-if="subtitle" class="modal-subtitle">{{ subtitle }}</p>
          </div>
          <button class="close-icon" aria-label="关闭" @click="$emit('close')">✕</button>
        </div>

        <div class="modal-body">
          <div v-if="tags && tags.length" class="tags">
            <span v-for="(tag, i) in tags" :key="i" class="tag">{{ tag }}</span>
          </div>

          <div v-if="rows && rows.length" class="grid">
            <div
              v-for="(row, i) in rows"
              :key="i"
              class="item"
              :class="{ 'item-wide': row.wide }"
            >
              <span class="label">{{ row.label }}</span>
              <span class="value">{{ row.value }}</span>
            </div>
          </div>

          <div v-for="(section, i) in sections" :key="`sec-${i}`" class="section">
            <span class="label">{{ section.label }}</span>
            <p class="text">{{ section.text }}</p>
          </div>
        </div>

        <div class="modal-actions">
          <button type="button" class="close-btn" @click="$emit('close')">关闭</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { onMounted, onBeforeUnmount } from 'vue';

defineProps({
  title: { type: String, default: '详情' },
  subtitle: { type: String, default: '' },
  tags: { type: Array, default: () => [] },
  rows: { type: Array, default: () => [] },
  sections: { type: Array, default: () => [] }
});

const emit = defineEmits(['close']);

const onKeydown = (e) => { if (e.key === 'Escape') emit('close'); };
onMounted(() => window.addEventListener('keydown', onKeydown));
onBeforeUnmount(() => window.removeEventListener('keydown', onKeydown));
</script>

<style scoped>
/* ===== 遮罩 ===== */
.overlay {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(15, 30, 51, 0.5);
  backdrop-filter: blur(4px);
  animation: fade-in 0.16s ease-out;
}

/* ===== 弹窗 ===== */
.modal {
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: 560px;
  max-height: 86vh;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 16px;
  box-shadow: 0 28px 60px rgba(15, 30, 51, 0.28);
  animation: modal-in 0.2s ease-out;
}

.modal-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 20px 14px;
  border-bottom: 1px solid var(--app-border-soft);
}

.modal-title {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--app-heading);
}

.modal-subtitle {
  margin: 4px 0 0;
  font-size: 12px;
  line-height: 1.6;
  color: var(--app-text-4);
  word-break: break-word;
}

.close-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  flex-shrink: 0;
  border: none;
  border-radius: 7px;
  background: transparent;
  color: var(--app-text-4);
  font-size: 13px;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}
.close-icon:hover {
  background: var(--app-surface-3);
  color: var(--app-heading);
}

/* ===== 内容 ===== */
.modal-body {
  padding: 16px 20px 18px;
  overflow-y: auto;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.tag {
  display: inline-flex;
  padding: 3px 10px;
  border: 1px solid var(--app-blue-line);
  border-radius: 9999px;
  background: var(--app-blue-bg);
  color: var(--app-blue-fg);
  font-size: 11px;
  font-weight: 600;
}

.grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 18px;
}

.item {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}
.item-wide {
  grid-column: 1 / -1;
}

.label {
  font-size: 11px;
  font-weight: 600;
  color: var(--app-text-4);
  letter-spacing: 0.02em;
}

.value {
  font-size: 13px;
  line-height: 1.6;
  color: var(--app-text);
  word-break: break-word;
}

.section {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px dashed var(--app-border-soft);
}

.text {
  margin-top: 6px;
  padding: 12px 14px;
  border: 1px solid var(--app-border);
  border-radius: 10px;
  background: var(--app-surface-2);
  font-size: 13px;
  line-height: 1.75;
  color: var(--app-text);
  white-space: pre-wrap;
  word-break: break-word;
}

/* ===== 底部操作 ===== */
.modal-actions {
  display: flex;
  justify-content: flex-end;
  padding: 12px 20px 16px;
  border-top: 1px solid var(--app-border-soft);
}

.close-btn {
  padding: 7px 16px;
  border: 1px solid var(--app-border);
  border-radius: 9px;
  background: var(--app-surface-2);
  color: var(--app-text-3);
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}
.close-btn:hover {
  background: var(--app-surface-3);
  color: var(--app-heading);
}

/* ===== 动画 ===== */
@keyframes fade-in {
  from { opacity: 0; }
  to   { opacity: 1; }
}
@keyframes modal-in {
  from { opacity: 0; transform: translateY(10px) scale(0.98); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}
</style>