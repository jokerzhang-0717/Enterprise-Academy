import { ref } from 'vue';
import {
  fetchAnnouncement,
  fetchAnnouncementMessages,
  markAllAnnouncementsRead,
} from '../api/index.js';

const messages = ref([]);
let loaded = false;
let loadingPromise = null;

export const loadMessages = async (force = false) => {
  if (loaded && !force) return messages.value;
  if (loadingPromise) return loadingPromise;
  loadingPromise = fetchAnnouncementMessages({ page: 1, size: 100 })
    .then((page) => {
      messages.value = page.records || [];
      loaded = true;
      return messages.value;
    })
    .finally(() => {
      loadingPromise = null;
    });
  return loadingPromise;
};

export const resetMessages = () => {
  messages.value = [];
  loaded = false;
  loadingPromise = null;
};

export const markMessageRead = async (id, isRead = true) => {
  const target = messages.value.find((item) => item.id === id);
  if (!target || target.isRead === isRead) return;
  if (isRead) {

    await fetchAnnouncement(target.numericId ?? target.id);
    target.isRead = true;
  } else {
    target.isRead = false;
  }
};

export const markAllMessagesRead = async () => {
  if (!messages.value.some((item) => !item.isRead)) return 0;
  const count = messages.value.filter((item) => !item.isRead).length;
  await markAllAnnouncementsRead();
  messages.value.forEach((item) => {
    item.isRead = true;
  });
  return count;
};

export const removeMessages = async (ids) => {
  messages.value = messages.value.filter((item) => !ids.includes(item.id));
};

export const useMessages = () => messages;
