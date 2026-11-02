<template>
  <div class="pager">
    <span class="pager-info">
      共 {{ total }} 条 · 第 {{ currentPage }} / {{ totalPages }} 页
    </span>
    <div class="pager-controls">
      <button class="pager-btn" :disabled="currentPage === 1" @click="go(currentPage - 1)">上一页</button>
      <button
        v-for="p in pageNumbers"
        :key="p"
        class="pager-btn pager-btn--num"
        :class="{ 'pager-btn--active': p === currentPage }"
        @click="go(p)"
      >
        {{ p }}
      </button>
      <button class="pager-btn" :disabled="currentPage === totalPages" @click="go(currentPage + 1)">下一页</button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  total: { type: Number, default: 0 },
  page: { type: Number, default: 1 },
  totalPages: { type: Number, default: 1 }
});

const emit = defineEmits(['update:page']);

const currentPage = computed(() => {
  const max = Math.max(1, props.totalPages);
  return Math.min(Math.max(1, props.page), max);
});

const pageNumbers = computed(() => {
  const total = Math.max(1, props.totalPages);
  const max = 5;
  let start = Math.max(1, currentPage.value - 2);
  const end = Math.min(total, start + max - 1);
  start = Math.max(1, end - max + 1);
  return Array.from({ length: end - start + 1 }, (_, i) => start + i);
});

const go = (p) => {
  const next = Math.min(Math.max(1, p), Math.max(1, props.totalPages));
  if (next !== props.page) emit('update:page', next);
};
</script>

<style scoped>
.pager {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: auto;
  flex-shrink: 0;
}

.pager-info {
  font-size: 12px;
  color: var(--app-text-5);
}

.pager-controls {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.pager-btn {
  min-width: 32px;
  height: 32px;
  padding: 0 12px;
  font-family: inherit;
  font-size: 12px;
  font-weight: 600;
  color: var(--app-text-3);
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s, color 0.15s, border-color 0.15s;
}

.pager-btn:hover:not(:disabled) {
  background: var(--app-brand-wash);
  color: var(--app-brand-ink);
  border-color: var(--app-brand);
}

.pager-btn:disabled {
  color: var(--app-text-6);
  background: var(--app-surface-2);
  cursor: not-allowed;
}

.pager-btn--num { padding: 0; }

.pager-btn--active,
.pager-btn--active:hover:not(:disabled) {
  color: #fff;
  background: linear-gradient(135deg, var(--app-brand), var(--app-brand-deep));
  border-color: var(--app-brand-deep);
}
</style>
