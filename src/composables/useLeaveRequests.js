import { ref } from 'vue';
import { createLeave, fetchLeaves } from '../api/index.js';

const leaveRequests = ref([]);
let loaded = false;
let loadingPromise = null;

export const loadLeaves = async (force = false) => {
  if (loaded && !force) return leaveRequests.value;
  if (loadingPromise) return loadingPromise;
  loadingPromise = fetchLeaves()
    .then((list) => {
      leaveRequests.value = list || [];
      loaded = true;
      return leaveRequests.value;
    })
    .finally(() => {
      loadingPromise = null;
    });
  return loadingPromise;
};

export const resetLeaves = () => {
  leaveRequests.value = [];
  loaded = false;
  loadingPromise = null;
};

export const submitLeave = async (payload) => {
  const created = await createLeave(payload);
  await loadLeaves(true);
  return created;
};

export const useLeaveRequests = () => leaveRequests;
