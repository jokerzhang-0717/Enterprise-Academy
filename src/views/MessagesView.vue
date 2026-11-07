<template>
  <div class="messages-page space-y-6 animate-in fade-in duration-200">
    <!-- 1. Top Title & Global Controls -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div v-if="!detailMode">
        <div class="flex items-center gap-2">
          <h1 class="text-2xl font-extrabold text-[color:var(--app-text)] tracking-tight">
            消息与站内信中心
          </h1>
          <span class="px-2 py-0.5 rounded-full bg-[color:var(--app-accent-soft)] text-[color:var(--app-text)] text-xs font-semibold">
            实时推送已开启
          </span>
        </div>
        <p class="text-xs text-[color:var(--app-text-4)] mt-0.5">
          汇集全企培训公告与通知，与管理员在「公告管理」中发布的已发布公告实时同步
        </p>
      </div>

      <div v-if="!detailMode" class="flex items-center gap-2">
        <button
          v-if="selectedIds.length"
          @click="handleDeleteSelected"
          class="px-3.5 py-1.5 bg-[color:var(--app-rose-100)] border border-[color:var(--app-rose-line)] hover:bg-[color:var(--app-rose-bg)] text-xs font-semibold text-[color:var(--app-rose-fg)] rounded-xl flex items-center gap-1.5 transition-colors"
        >
          <Trash2 class="w-3.5 h-3.5" />
          <span>删除所选 ({{ selectedIds.length }})</span>
        </button>
        <button
          @click="handleMarkAllRead"
          class="px-3.5 py-1.5 bg-[color:var(--app-surface)] border border-[color:var(--app-accent-border)] hover:bg-[color:var(--app-accent-soft)] text-xs font-semibold text-[color:var(--app-text)] rounded-xl flex items-center gap-1.5 transition-colors"
        >
          <CheckCheck class="w-3.5 h-3.5" />
          <span>全部已读</span>
        </button>
      </div>
    </div>

    <Transition name="toast">
      <div
        v-if="deleteNotice"
        role="status"
        class="fixed left-1/2 top-20 z-[70] flex w-[min(90vw,380px)] -translate-x-1/2 items-start gap-3 rounded-xl border border-[color:var(--app-border)] bg-[color:var(--app-surface)] px-4 py-3.5 shadow-lg shadow-slate-900/10"
      >
        <span class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-[color:var(--app-emerald-soft)] text-[color:var(--app-emerald-strong)]">
          <CheckCheck class="h-4 w-4" />
        </span>
        <div class="min-w-0 flex-1">
          <p class="text-sm font-semibold text-[color:var(--app-text)]">{{ deleteNotice }}</p>
          <button v-if="deletedMessages.length" @click="undoDelete" class="mt-1 text-xs font-semibold text-[color:var(--app-accent-deep)] hover:underline">撤销</button>
        </div>
        <button aria-label="关闭提示" @click="clearDeleteNotice" class="rounded p-1 text-[color:var(--app-text-5)] hover:bg-[color:var(--app-surface-3)] hover:text-[color:var(--app-text-3)]">
          <X class="h-4 w-4" />
        </button>
      </div>
    </Transition>

    <!-- Message detail -->
    <div v-if="detailMode && selectedMessage" class="space-y-3">
      <button
        @click="goBackToList"
        class="inline-flex items-center gap-1.5 rounded-lg px-2 py-1.5 text-xs font-medium text-[color:var(--app-text-4)] transition-colors hover:bg-[color:var(--app-surface)] hover:text-[color:var(--app-accent-deep)]"
      >
        <ChevronLeft class="h-3.5 w-3.5" />
        <span>返回消息列表</span>
      </button>

      <article class="overflow-hidden rounded-2xl border border-[color:var(--app-accent-soft)] bg-[color:var(--app-surface)] shadow-sm">
        <div class="border-b border-[color:var(--app-blue-70)] bg-gradient-to-r from-[color:var(--app-blue-25)] to-[color:var(--app-surface)] px-6 py-5 sm:px-8">
          <div class="flex flex-wrap items-center gap-2 text-xs">
            <span class="rounded-full bg-[color:var(--app-accent-soft)] px-2.5 py-1 font-semibold text-[color:var(--app-accent-deep)]">{{ selectedMessage.typeLabel }}</span>
            <span class="text-[color:var(--app-text-4)]">{{ selectedMessage.sender }}</span>
            <span class="ml-auto text-[color:var(--app-text-5)]">{{ selectedMessage.time }}</span>
          </div>
          <h2 class="mt-4 text-xl font-bold leading-relaxed text-[color:var(--app-text)]">{{ selectedMessage.title }}</h2>
        </div>

        <div class="space-y-6 px-6 py-6 sm:px-8">
          <div class="whitespace-pre-line text-sm leading-7 text-[color:var(--app-text-3)]">
            {{ selectedMessage.details?.fullContent || selectedMessage.preview }}
          </div>

          <section v-if="selectedMessage.details?.bulletPoints?.length" class="rounded-xl bg-[color:var(--app-surface-2)] p-4">
            <h3 class="mb-3 text-sm font-semibold text-[color:var(--app-text)]">注意事项</h3>
            <ul class="space-y-2.5">
              <li v-for="(point, index) in selectedMessage.details.bulletPoints" :key="index" class="flex gap-2.5 text-sm leading-6 text-[color:var(--app-text-3)]">
                <span class="mt-2 h-1.5 w-1.5 shrink-0 rounded-full bg-[color:var(--app-accent-ink-2)]" />
                <span>{{ point }}</span>
              </li>
            </ul>
          </section>

          <section v-if="selectedMessage.details?.attachments?.length" class="border-t border-[color:var(--app-blue-70)] pt-5">
            <h3 class="mb-3 text-sm font-semibold text-[color:var(--app-text)]">附件（{{ selectedMessage.details.attachments.length }}）</h3>
            <div class="grid gap-3 sm:grid-cols-2">
              <div v-for="attachment in selectedMessage.details.attachments" :key="attachment.name" class="flex items-start gap-3 rounded-xl border border-[color:var(--app-border)] p-3">
                <span class="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-[color:var(--app-blue-50)] text-[color:var(--app-accent-ink)]"><FileText class="h-4 w-4" /></span>
                <div class="min-w-0">
                  <p class="break-all text-xs font-semibold text-[color:var(--app-text-2)]">{{ attachment.name }}</p>
                  <p class="mt-1 text-[11px] text-[color:var(--app-text-5)]">{{ attachment.size }}<span v-if="attachment.source"> · {{ attachment.source }}</span></p>
                </div>
              </div>
            </div>
          </section>

          <section v-if="selectedMessage.details?.officialNoticeId || selectedMessage.details?.examDuration || selectedMessage.details?.passingScore" class="grid gap-3 border-t border-[color:var(--app-blue-70)] pt-5 sm:grid-cols-3">
            <div v-if="selectedMessage.details.officialNoticeId" class="rounded-lg bg-[color:var(--app-surface-2)] p-3">
              <p class="text-[11px] text-[color:var(--app-text-5)]">通知编号</p>
              <p class="mt-1 text-xs font-semibold text-[color:var(--app-text-2)]">{{ selectedMessage.details.officialNoticeId }}</p>
            </div>
            <div v-if="selectedMessage.details.examDuration" class="rounded-lg bg-[color:var(--app-surface-2)] p-3">
              <p class="text-[11px] text-[color:var(--app-text-5)]">考试时长</p>
              <p class="mt-1 text-xs font-semibold text-[color:var(--app-text-2)]">{{ selectedMessage.details.examDuration }}</p>
            </div>
            <div v-if="selectedMessage.details.passingScore" class="rounded-lg bg-[color:var(--app-surface-2)] p-3">
              <p class="text-[11px] text-[color:var(--app-text-5)]">合格标准</p>
              <p class="mt-1 text-xs font-semibold text-[color:var(--app-text-2)]">{{ selectedMessage.details.passingScore }}</p>
            </div>
          </section>
        </div>
      </article>
    </div>
    <div v-else-if="detailMode" class="rounded-xl border border-[color:var(--app-border)] bg-[color:var(--app-surface)] p-8 text-center text-sm text-[color:var(--app-text-4)]">
      未找到这条消息，请返回消息列表重新选择。
    </div>

    <div v-else class="messages-list-page grid grid-cols-1 gap-5">
      <div class="messages-list-column space-y-3">
        <div class="flex items-center justify-between px-1 text-xs text-[color:var(--app-text-4)]">
          <label class="flex items-center gap-2 cursor-pointer">
            <input
              type="checkbox"
              :checked="paginatedMessages.length > 0 && paginatedMessages.every(message => selectedIds.includes(message.id))"
              @change="toggleSelectAll"
              class="rounded border-[color:var(--app-accent-border)] accent-[color:var(--app-violet)]"
            />
            <span>全选</span>
          </label>
          <span>共 {{ messages.length }} 条公告，{{ unreadCount }} 条未读</span>
        </div>

        <div class="messages-cards min-h-[423px] space-y-3">
          <div
            v-for="msg in paginatedMessages"
            :key="msg.id"
            :id="`message-${msg.id}`"
            :class="{ 'search-result-target': searchTargetId === `message-${msg.id}` }"
            @click="emit('open-message', msg.id)"
            class="relative cursor-pointer rounded-2xl border border-[color:var(--app-accent-soft)] bg-[color:var(--app-surface)] p-4 shadow-xs transition-all hover:border-[color:var(--app-accent-border)] hover:bg-[color:var(--app-accent-soft)]"
          >
            <div class="flex items-center gap-3">
              <input
                type="checkbox"
                :checked="selectedIds.includes(msg.id)"
                @click.stop="toggleSelect(msg.id)"
                class="shrink-0 rounded border-[color:var(--app-accent-border)] accent-[color:var(--app-violet)]"
              />

              <div class="min-w-0 flex-1 space-y-1.5">
                <div class="flex items-center justify-between text-xs">
                  <div class="flex items-center gap-2">
                    <span class="font-semibold text-[color:var(--app-text)] text-[11px]">
                      {{ msg.typeLabel }}
                    </span>
                    <span 
                      v-if="!msg.isRead"
                      class="px-1.5 py-0.2 rounded bg-[color:var(--app-red-100)] text-[color:var(--app-red-600)] text-[10px] font-bold"
                    >
                      未读
                    </span>
                    <span class="text-[color:var(--app-text-4)] text-[11px] truncate">{{ msg.sender }}</span>
                  </div>
                  <span class="text-[color:var(--app-text-5)] font-mono text-[11px]">{{ msg.time }}</span>
                </div>

                <h3 class="text-sm font-bold text-[color:var(--app-text)]">
                  {{ msg.title }}
                </h3>

                <p class="text-xs text-[color:var(--app-text-4)] line-clamp-2 leading-relaxed">
                  {{ msg.preview }}
                </p>
              </div>

              <span class="shrink-0 text-xs font-semibold text-[color:var(--app-text)] flex items-center gap-0.5">
                <span>查看详情</span>
                <ChevronRight class="w-3 h-3" />
              </span>
            </div>
          </div>
          <div v-if="!messages.length" class="rounded-2xl border border-dashed border-[color:var(--app-border-strong)] bg-[color:var(--app-surface)] py-12 text-center text-sm text-[color:var(--app-text-5)]">
            暂无公告
          </div>
        </div>

        <div v-if="messages.length" class="messages-pagination flex items-center justify-between text-xs text-[color:var(--app-text-4)] pt-3 border-t border-[color:var(--app-accent-soft)]">
          <span>显示 {{ displayedRange.start }} - {{ displayedRange.end }} 条，共 {{ messages.length }} 条</span>
          <div v-if="totalPages > 1" class="flex items-center gap-1">
            <button
              type="button"
              :disabled="currentPage === 1"
              @click="currentPage--"
              class="p-1 rounded border border-[color:var(--app-border)] hover:bg-[color:var(--app-surface)] text-[color:var(--app-text-3)] disabled:cursor-not-allowed disabled:opacity-40"
              aria-label="上一页"
            >
              <ChevronLeft class="w-3.5 h-3.5" />
            </button>
            <button
              v-for="page in totalPages"
              :key="page"
              type="button"
              @click="currentPage = page"
              :aria-current="currentPage === page ? 'page' : undefined"
              class="px-2.5 py-0.5 rounded border border-[color:var(--app-accent-border)] hover:bg-[color:var(--app-accent-soft)]"
              :class="currentPage === page ? 'bg-[color:var(--app-accent)] text-[color:var(--app-ink-solid)] font-bold' : 'bg-[color:var(--app-surface)]'"
            >
              {{ page }}
            </button>
            <button
              type="button"
              :disabled="currentPage === totalPages"
              @click="currentPage++"
              class="p-1 rounded border border-[color:var(--app-border)] hover:bg-[color:var(--app-surface)] text-[color:var(--app-text-3)] disabled:cursor-not-allowed disabled:opacity-40"
              aria-label="下一页"
            >
              <ChevronRight class="w-3.5 h-3.5" />
            </button>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { 
  CheckCheck, 
  FileText, 
  ChevronRight, 
  ChevronLeft,
  Trash2,
  X
} from 'lucide-vue-next';
import { useMessages, markAllMessagesRead, loadMessages } from '../composables/useMessages.js';

const props = defineProps({
  detailMode: {
    type: Boolean,
    default: false,
  },
  selectedMessageId: {
    type: String,
    default: 'msg-1',
  },
  searchTargetId: {
    type: String,
    default: '',
  },
});

const emit = defineEmits(['navigate', 'open-message']);

const messages = useMessages();
const currentPage = ref(1);
const isFullscreen = ref(false);
const pageSize = computed(() => isFullscreen.value ? 5 : 4);
const selectedIds = ref([]);
const selectedMessage = computed(() => messages.value.find(message => message.id === props.selectedMessageId));
const unreadCount = computed(() => messages.value.filter(message => !message.isRead).length);
const totalPages = computed(() => Math.ceil(messages.value.length / pageSize.value));
const paginatedMessages = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return messages.value.slice(start, start + pageSize.value);
});
const displayedRange = computed(() => ({
  start: messages.value.length ? (currentPage.value - 1) * pageSize.value + 1 : 0,
  end: Math.min(currentPage.value * pageSize.value, messages.value.length),
}));
const deleteNotice = ref('');
const deletedMessages = ref([]);
let deleteNoticeTimeout;

const syncFullscreen = () => {
  isFullscreen.value = Boolean(document.fullscreenElement);
};

onMounted(() => {
  syncFullscreen();
  document.addEventListener('fullscreenchange', syncFullscreen);
  // 进入消息中心时同步管理端最新已发布公告
  loadMessages(true).catch(() => {});
});

onBeforeUnmount(() => {
  document.removeEventListener('fullscreenchange', syncFullscreen);
});

watch(totalPages, pageCount => {
  if (currentPage.value > pageCount) currentPage.value = Math.max(pageCount, 1);
});

watch(
  [() => props.searchTargetId, messages],
  ([targetId, records]) => {
    if (!targetId) return;
    const messageIndex = records.findIndex(message => `message-${message.id}` === targetId);
    if (messageIndex < 0) return;
    currentPage.value = Math.floor(messageIndex / pageSize.value) + 1;
    nextTick(() => document.getElementById(targetId)?.scrollIntoView({ behavior: 'smooth', block: 'center' }));
  },
  { immediate: true, deep: true },
);

const goBackToList = () => {
  emit('navigate', 'messages');
};

const handleMarkAllRead = async () => {
  const unreadBeforeUpdate = unreadCount.value;
  if (!unreadBeforeUpdate) {
    showNotice('所有消息已是已读状态');
    return;
  }
  try {
    await markAllMessagesRead();
    showNotice(`已将 ${unreadBeforeUpdate} 条未读消息标记为已读`);
  } catch {
    showNotice('标记已读失败，请稍后重试');
  }
};

const handleDeleteSelected = () => {
  if (!selectedIds.value.length) return;
  const idsToDelete = new Set(selectedIds.value);
  const removed = [];
  messages.value.forEach((message, index) => {
    if (idsToDelete.has(message.id)) removed.push({ index, message });
  });
  deletedMessages.value = removed;
  messages.value = messages.value.filter(message => !idsToDelete.has(message.id));
  selectedIds.value = [];
  deleteNotice.value = `已从当前列表移除 ${removed.length} 条公告`;
  clearTimeout(deleteNoticeTimeout);
  deleteNoticeTimeout = setTimeout(clearDeleteNotice, 5000);
};

const undoDelete = () => {
  const restored = [...messages.value];
  deletedMessages.value.forEach(({ index, message }) => {
    restored.splice(Math.min(index, restored.length), 0, message);
  });
  messages.value = restored;
  deletedMessages.value = [];
  selectedIds.value = [];
  clearDeleteNotice();
};

const clearDeleteNotice = () => {
  clearTimeout(deleteNoticeTimeout);
  deleteNotice.value = '';
  deletedMessages.value = [];
};

const showNotice = (message) => {
  deleteNotice.value = message;
  deletedMessages.value = [];
  clearTimeout(deleteNoticeTimeout);
  deleteNoticeTimeout = setTimeout(clearDeleteNotice, 4000);
};

const toggleSelect = (id) => {
  if (selectedIds.value.includes(id)) {
    selectedIds.value = selectedIds.value.filter(i => i !== id);
  } else {
    selectedIds.value.push(id);
  }
};

const toggleSelectAll = () => {
  const currentPageIds = paginatedMessages.value.map(message => message.id);
  const allCurrentPageSelected = currentPageIds.every(id => selectedIds.value.includes(id));
  if (allCurrentPageSelected) {
    selectedIds.value = selectedIds.value.filter(id => !currentPageIds.includes(id));
  } else {
    selectedIds.value = [...new Set([...selectedIds.value, ...currentPageIds])];
  }
};
</script>

<style scoped>
.messages-page {
  display: flex;
  flex: 1 0 auto;
  flex-direction: column;
}

.messages-list-page,
.messages-list-column {
  display: flex;
  flex: 1 0 auto;
  flex-direction: column;
}

.messages-cards {
  flex: 1 0 auto;
}

.search-result-target {
  border-color: var(--app-accent) !important;
  box-shadow: 0 0 0 3px var(--app-accent-soft);
}

.messages-pagination {
  margin-top: auto;
}
</style>
