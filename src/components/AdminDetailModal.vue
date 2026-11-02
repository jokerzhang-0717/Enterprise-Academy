<template>
  <Teleport to="body">
    <div class="detail-backdrop" @click.self="$emit('close')">
      <div class="detail-card" role="dialog" aria-modal="true" :aria-label="title">
        <div class="detail-head">
          <div class="detail-head-text">
            <h3 class="detail-title">{{ title }}</h3>
            <p v-if="subtitle" class="detail-subtitle">{{ subtitle }}</p>
          </div>
          <button class="detail-close" aria-label="关闭" @click="$emit('close')">✕</button>
        </div>

        <div class="detail-body">
          <div v-if="tags && tags.length" class="detail-tags">
            <span v-for="(tag, i) in tags" :key="i" class="detail-tag">{{ tag }}</span>
          </div>

          <div v-if="rows && rows.length" class="detail-grid">
            <div
              v-for="(row, i) in rows"
              :key="i"
              class="detail-item"
              :class="{ 'detail-item--wide': row.wide }"
            >
              <span class="detail-label">{{ row.label }}</span>
              <span class="detail-value">{{ row.value }}</span>
            </div>
          </div>

          <div v-for="(section, i) in sections" :key="`sec-${i}`" class="detail-section">
            <span class="detail-label">{{ section.label }}</span>
            <p class="detail-text">{{ section.text }}</p>
          </div>
        </div>

        <div class="detail-actions">
          <button type="button" class="detail-btn" @click="$emit('close')">关闭</button>
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
.detail-backdrop {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgb(15 30 51 / 50%);
  backdrop-filter: blur(4px);
  animation: detail-fade 0.16s ease-out;
}

.detail-card {
  width: 100%;
  max-width: 560px;
  max-height: 86vh;
  display: flex;
  flex-direction: column;
  background: var(--app-surface);
  border-radius: 16px;
  box-shadow: 0 28px 60px rgb(15 30 51 / 28%);
  animation: detail-in 0.2s ease-out;
}

.detail-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 20px 14px;
  border-bottom: 1px solid var(--app-border-soft);
}

.detail-head-text { min-width: 0; }

.detail-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--app-heading);
}

.detail-subtitle {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.6;
  color: var(--app-text-4);
  word-break: break-word;
}

.detail-close {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  color: var(--app-text-4);
  background: transparent;
  border: none;
  border-radius: 7px;
  flex-shrink: 0;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}
.detail-close:hover { background: var(--app-surface-3); color: var(--app-heading); }

.detail-body {
  padding: 16px 20px 18px;
  overflow-y: auto;
}

.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.detail-tag {
  display: inline-flex;
  padding: 3px 10px;
  font-size: 11px;
  font-weight: 600;
  color: var(--app-accent-deep);
  background: var(--app-accent-soft);
  border: 1px solid var(--app-accent-border);
  border-radius: 9999px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 18px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}
.detail-item--wide { grid-column: 1 / -1; }

.detail-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--app-text-5);
  letter-spacing: 0.02em;
}

.detail-value {
  font-size: 13px;
  line-height: 1.6;
  color: var(--app-text-2);
  word-break: break-word;
}

.detail-section {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px dashed var(--app-border);
}

.detail-text {
  margin-top: 6px;
  padding: 12px 14px;
  font-size: 13px;
  line-height: 1.75;
  color: var(--app-text-2);
  background: var(--app-surface-2);
  border: 1px solid var(--app-border-soft);
  border-radius: 10px;
  white-space: pre-wrap;
  word-break: break-word;
}

.detail-actions {
  display: flex;
  justify-content: flex-end;
  padding: 12px 20px 16px;
  border-top: 1px solid var(--app-border-soft);
}

.detail-btn {
  padding: 7px 16px;
  font-size: 12px;
  font-weight: 600;
  color: var(--app-text-3);
  background: var(--app-surface-2);
  border: 1px solid var(--app-border);
  border-radius: 9px;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}
.detail-btn:hover { background: var(--app-surface-3); color: var(--app-heading); }

@keyframes detail-fade {
  from { opacity: 0; }
  to   { opacity: 1; }
}
@keyframes detail-in {
  from { opacity: 0; transform: translateY(10px) scale(0.98); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}
</style>
