import { computed, ref, watch } from 'vue';

/**
 * 列表分页
 * @param {import('vue').ComputedRef<Array>|import('vue').Ref<Array>} source 数据源
 * @param {number} pageSize 每页条数
 */
export function usePager(source, pageSize = 8) {
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

  /* 数据减少导致页码越界时自动收拢 */
  watch(totalPages, (n) => {
    if (page.value > n) page.value = n;
  });

  return { page, total, totalPages, paged, resetPage };
}
