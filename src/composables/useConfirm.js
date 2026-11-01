import { ref } from 'vue';

/**
 * 管理端统一的确认弹窗状态
 *
 * const { confirmState, askConfirm, cancelConfirm, acceptConfirm } = useConfirm();
 * askConfirm({ title, subtitle, rows, tip, tone, confirmText, action });
 */
export function useConfirm() {
  const confirmState = ref(null);

  const askConfirm = (options) => {
    confirmState.value = options;
  };

  const cancelConfirm = () => {
    confirmState.value = null;
  };

  const acceptConfirm = () => {
    const state = confirmState.value;
    confirmState.value = null;
    state?.action?.();
  };

  return { confirmState, askConfirm, cancelConfirm, acceptConfirm };
}
