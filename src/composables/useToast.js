import { reactive } from 'vue';

/**
 * 轻量全局提示（替代原生 alert）
 *
 * const { show } = useToast();
 * show('视频超过 30MB，本演示环境仅支持小文件上传。', 'error');
 */
const state = reactive({ items: [] });
let seq = 0;

export function useToast() {
  const show = (message, type = 'info', duration = 3200) => {
    const text = String(message ?? '').trim();
    if (!text) return;
    // 同一时间只保留一条相同提示：并发请求同时失败时不再堆叠重复气泡。
    if (state.items.some(item => item.message === text && item.type === type)) return;
    const id = ++seq;
    state.items.push({ id, message: text, type });
    window.setTimeout(() => {
      const idx = state.items.findIndex(item => item.id === id);
      if (idx > -1) state.items.splice(idx, 1);
    }, duration);
  };

  return { toasts: state.items, show };
}
