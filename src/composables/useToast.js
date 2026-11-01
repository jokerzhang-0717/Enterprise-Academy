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
    const id = ++seq;
    state.items.push({ id, message, type });
    window.setTimeout(() => {
      const idx = state.items.findIndex(item => item.id === id);
      if (idx > -1) state.items.splice(idx, 1);
    }, duration);
  };

  return { toasts: state.items, show };
}
