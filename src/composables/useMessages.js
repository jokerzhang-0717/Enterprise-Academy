import { ref } from 'vue';
import {
  fetchAnnouncement,
  fetchAnnouncementMessages,
  markAllAnnouncementsRead,
} from '../api/index.js';

/**
 * 学员端消息中心 = 管理端「公告管理」中已发布的公告。
 * 草稿、撤回的公告不会出现在这里；已读状态由后端 announcement_read 维护。
 */
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
    // 公告详情接口会记录当前用户的已读状态
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

/** 公告为全员广播，学员端删除仅从当前页面移除，刷新后仍会保留。 */
export const removeMessages = async (ids) => {
  messages.value = messages.value.filter((item) => !ids.includes(item.id));
};

export const useMessages = () => messages;
