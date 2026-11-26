import { computed, ref, watch } from 'vue';

export const ADMIN_PAGE_SIZE = 10;

export function usePager(source, pageSize = ADMIN_PAGE_SIZE) {
  const page = ref(1);

  const getList = () => {
    const value = typeof source === 'function' ? source() : source;
    if (value && typeof value === 'object' && 'value' in value) return value.value || [];
    return value || [];
  };

  const total = computed(() => getList().length);

  const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)));

  const paged = computed(() => {
    const list = getList();
    const start = (page.value - 1) * pageSize;
    return list.slice(start, start + pageSize);
  });

  const resetPage = () => { page.value = 1; };

  watch(totalPages, (n) => {
    if (page.value > n) page.value = n;
  });

  return { page, total, totalPages, paged, resetPage };
}
