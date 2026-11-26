import { reactive } from 'vue';

const state = reactive({ items: [] });
let seq = 0;

export function useToast() {
  const show = (message, type = 'info', duration = 3200) => {
    const text = String(message ?? '').trim();
    if (!text) return;

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
