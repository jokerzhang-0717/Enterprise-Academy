import { ref } from 'vue';
import {
  deleteNotification,
  fetchNotifications,
  markAllNotificationsRead,
  markNotificationRead,
} from '../api/index.js';

const messages = ref([]);
let loaded = false;
let loadingPromise = null;

/** 拉取当前用户的站内信 / 通知 */
export const loadMessages = async (force = false) => {
  if (loaded && !force) return messages.value;
  if (loadingPromise) return loadingPromise;
  loadingPromise = fetchNotifications({ page: 1, size: 100 })
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
  await markNotificationRead(id, isRead);
  target.isRead = isRead;
};

export const markAllMessagesRead = async () => {
  if (!messages.value.some((item) => !item.isRead)) return 0;
  const count = messages.value.filter((item) => !item.isRead).length;
  await markAllNotificationsRead();
  messages.value.forEach((item) => {
    item.isRead = true;
  });
  return count;
};

export const removeMessages = async (ids) => {
  const numericIds = ids
    .map((id) => messages.value.find((item) => item.id === id))
    .filter(Boolean)
    .map((item) => item.numericId ?? item.id);
  await Promise.all(numericIds.map((id) => deleteNotification(id)));
  messages.value = messages.value.filter((item) => !ids.includes(item.id));
};

export const useMessages = () => messages;
