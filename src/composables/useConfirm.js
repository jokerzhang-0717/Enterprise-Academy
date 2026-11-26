import { ref } from 'vue';

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
