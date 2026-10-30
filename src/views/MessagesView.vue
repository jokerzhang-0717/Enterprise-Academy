<template>
  <div class="space-y-6 pb-12 animate-in fade-in duration-200">
    <!-- 1. Top Title & Global Controls -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div v-if="!detailMode">
        <div class="flex items-center gap-2">
          <h1 class="text-2xl font-extrabold text-[#1E293B] tracking-tight">
            消息与站内信中心
          </h1>
          <span class="px-2 py-0.5 rounded-full bg-[#D9F0FF] text-[#1E293B] text-xs font-semibold">
            实时推送已开启
          </span>
        </div>
        <p class="text-xs text-[#64748B] mt-0.5">
          汇集全企培训通告、学习进度督促、认证考评安排与调休考勤审批结果
        </p>
      </div>

      <div v-if="!detailMode" class="flex items-center gap-2">
        <button
          v-if="selectedIds.length"
          @click="handleDeleteSelected"
          class="px-3.5 py-1.5 bg-rose-50 border border-rose-200 hover:bg-rose-100 text-xs font-semibold text-rose-700 rounded-xl flex items-center gap-1.5 transition-colors"
        >
          <Trash2 class="w-3.5 h-3.5" />
          <span>删除所选 ({{ selectedIds.length }})</span>
        </button>
        <button
          @click="handleMarkAllRead"
          class="px-3.5 py-1.5 bg-white border border-[#A3D5FF] hover:bg-[#D9F0FF] text-xs font-semibold text-[#1E293B] rounded-xl flex items-center gap-1.5 transition-colors"
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
        class="fixed left-1/2 top-20 z-[70] flex w-[min(90vw,380px)] -translate-x-1/2 items-start gap-3 rounded-xl border border-[#E2E8F0] bg-white px-4 py-3.5 shadow-lg shadow-slate-900/10"
      >
        <span class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-emerald-50 text-emerald-600">
          <CheckCheck class="h-4 w-4" />
        </span>
        <div class="min-w-0 flex-1">
          <p class="text-sm font-semibold text-[#1E293B]">{{ deleteNotice }}</p>
          <button v-if="deletedMessages.length" @click="undoDelete" class="mt-1 text-xs font-semibold text-[#005576] hover:underline">撤销</button>
        </div>
        <button aria-label="关闭提示" @click="clearDeleteNotice" class="rounded p-1 text-[#94A3B8] hover:bg-slate-100 hover:text-[#475569]">
          <X class="h-4 w-4" />
        </button>
      </div>
    </Transition>

    <!-- Message detail -->
    <div v-if="detailMode && selectedMessage" class="space-y-3">
      <button
        @click="goBackToList"
        class="inline-flex items-center gap-1.5 rounded-lg px-2 py-1.5 text-xs font-medium text-[#64748B] transition-colors hover:bg-white hover:text-[#005576]"
      >
        <ChevronLeft class="h-3.5 w-3.5" />
        <span>返回消息列表</span>
      </button>

      <article class="overflow-hidden rounded-2xl border border-[#D9F0FF] bg-white shadow-sm">
        <div class="border-b border-[#E8EEF5] bg-gradient-to-r from-[#F4FAFF] to-white px-6 py-5 sm:px-8">
          <div class="flex flex-wrap items-center gap-2 text-xs">
            <span class="rounded-full bg-[#D9F0FF] px-2.5 py-1 font-semibold text-[#005576]">{{ selectedMessage.typeLabel }}</span>
            <span class="text-[#64748B]">{{ selectedMessage.sender }}</span>
            <span class="ml-auto text-[#94A3B8]">{{ selectedMessage.time }}</span>
          </div>
          <h2 class="mt-4 text-xl font-bold leading-relaxed text-[#1E293B]">{{ selectedMessage.title }}</h2>
        </div>

        <div class="space-y-6 px-6 py-6 sm:px-8">
          <div class="whitespace-pre-line text-sm leading-7 text-[#475569]">
            {{ selectedMessage.details?.fullContent || selectedMessage.preview }}
          </div>

          <section v-if="selectedMessage.details?.bulletPoints?.length" class="rounded-xl bg-[#F8FAFC] p-4">
            <h3 class="mb-3 text-sm font-semibold text-[#1E293B]">注意事项</h3>
            <ul class="space-y-2.5">
              <li v-for="(point, index) in selectedMessage.details.bulletPoints" :key="index" class="flex gap-2.5 text-sm leading-6 text-[#475569]">
                <span class="mt-2 h-1.5 w-1.5 shrink-0 rounded-full bg-[#1683A8]" />
                <span>{{ point }}</span>
              </li>
            </ul>
          </section>

          <section v-if="selectedMessage.details?.attachments?.length" class="border-t border-[#E8EEF5] pt-5">
            <h3 class="mb-3 text-sm font-semibold text-[#1E293B]">附件（{{ selectedMessage.details.attachments.length }}）</h3>
            <div class="grid gap-3 sm:grid-cols-2">
              <div v-for="attachment in selectedMessage.details.attachments" :key="attachment.name" class="flex items-start gap-3 rounded-xl border border-[#E2E8F0] p-3">
                <span class="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-sky-50 text-[#0879A5]"><FileText class="h-4 w-4" /></span>
                <div class="min-w-0">
                  <p class="break-all text-xs font-semibold text-[#334155]">{{ attachment.name }}</p>
                  <p class="mt-1 text-[11px] text-[#94A3B8]">{{ attachment.size }}<span v-if="attachment.source"> · {{ attachment.source }}</span></p>
                </div>
              </div>
            </div>
          </section>

          <section v-if="selectedMessage.details?.officialNoticeId || selectedMessage.details?.examDuration || selectedMessage.details?.passingScore" class="grid gap-3 border-t border-[#E8EEF5] pt-5 sm:grid-cols-3">
            <div v-if="selectedMessage.details.officialNoticeId" class="rounded-lg bg-[#F8FAFC] p-3">
              <p class="text-[11px] text-[#94A3B8]">通知编号</p>
              <p class="mt-1 text-xs font-semibold text-[#334155]">{{ selectedMessage.details.officialNoticeId }}</p>
            </div>
            <div v-if="selectedMessage.details.examDuration" class="rounded-lg bg-[#F8FAFC] p-3">
              <p class="text-[11px] text-[#94A3B8]">考试时长</p>
              <p class="mt-1 text-xs font-semibold text-[#334155]">{{ selectedMessage.details.examDuration }}</p>
            </div>
            <div v-if="selectedMessage.details.passingScore" class="rounded-lg bg-[#F8FAFC] p-3">
              <p class="text-[11px] text-[#94A3B8]">合格标准</p>
              <p class="mt-1 text-xs font-semibold text-[#334155]">{{ selectedMessage.details.passingScore }}</p>
            </div>
          </section>
        </div>
      </article>
    </div>
    <div v-else-if="detailMode" class="rounded-xl border border-[#E2E8F0] bg-white p-8 text-center text-sm text-[#64748B]">
      未找到这条消息，请返回消息列表重新选择。
    </div>

    <div v-else class="grid grid-cols-1 gap-5">
      <div class="space-y-3">
        <div class="flex items-center justify-between px-1 text-xs text-[#64748B]">
          <label class="flex items-center gap-2 cursor-pointer">
            <input
              type="checkbox"
              :checked="paginatedMessages.length > 0 && paginatedMessages.every(message => selectedIds.includes(message.id))"
              @change="toggleSelectAll"
              class="rounded border-[#A3D5FF] accent-[#9381FF]"
            />
            <span>全选</span>
          </label>
          <span>共 {{ messages.length }} 条站内信，{{ unreadCount }} 条未读</span>
        </div>

        <div class="min-h-[423px] space-y-3">
          <div
            v-for="msg in paginatedMessages"
            :key="msg.id"
            :id="`message-${msg.id}`"
            @click="emit('open-message', msg.id)"
            class="relative cursor-pointer rounded-2xl border border-[#D9F0FF] bg-white p-4 shadow-xs transition-all hover:border-[#A3D5FF] hover:bg-[#D9F0FF]"
          >
            <div class="flex items-start gap-3">
              <input
                type="checkbox"
                :checked="selectedIds.includes(msg.id)"
                @click.stop="toggleSelect(msg.id)"
                class="mt-1 rounded border-[#A3D5FF] accent-[#9381FF]"
              />

              <div class="flex-1 space-y-1.5">
                <div class="flex items-center justify-between text-xs">
                  <div class="flex items-center gap-2">
                    <span 
                      class="w-2 h-2 rounded-full"
                      :class="msg.isRead ? 'bg-slate-300' : 'bg-[#DC2626]'"
                    />
                    <span class="font-semibold text-[#1E293B] text-[11px]">
                      {{ msg.typeLabel }}
                    </span>
                    <span 
                      v-if="!msg.isRead"
                      class="px-1.5 py-0.2 rounded bg-[#FEE2E2] text-[#DC2626] text-[10px] font-bold"
                    >
                      未读
                    </span>
                    <span class="text-[#64748B] text-[11px] truncate">{{ msg.sender }}</span>
                  </div>
                  <span class="text-[#94A3B8] font-mono text-[11px]">{{ msg.time }}</span>
                </div>

                <h3 class="text-sm font-bold text-[#1E293B]">
                  {{ msg.title }}
                </h3>

                <p class="text-xs text-[#64748B] line-clamp-2 leading-relaxed">
                  {{ msg.preview }}
                </p>

                <div class="pt-1 flex items-center justify-between text-xs">
                  <span v-if="msg.attachmentsCount" class="text-[#64748B] text-[11px] flex items-center gap-1">
                    <FileText class="w-3.5 h-3.5" />
                    {{ msg.attachmentsCount }} 个附件
                  </span>
                  <span v-else class="text-[11px] text-[#94A3B8]">已自动存档</span>

                  <span class="text-xs font-semibold text-[#1E293B] flex items-center gap-0.5">
                    <span>查看详情</span>
                    <ChevronRight class="w-3 h-3" />
                  </span>
                </div>
              </div>
            </div>
          </div>
          <div v-if="!messages.length" class="rounded-2xl border border-dashed border-[#CBD5E1] bg-white py-12 text-center text-sm text-[#94A3B8]">
            暂无消息通知
          </div>
        </div>

        <div v-if="messages.length" class="flex items-center justify-between text-xs text-[#64748B] pt-3 border-t border-[#D9F0FF]">
          <span>显示 {{ displayedRange.start }} - {{ displayedRange.end }} 条，共 {{ messages.length }} 条</span>
          <div v-if="totalPages > 1" class="flex items-center gap-1">
            <button
              type="button"
              :disabled="currentPage === 1"
              @click="currentPage--"
              class="p-1 rounded border border-[#E2E8F0] hover:bg-white text-[#475569] disabled:cursor-not-allowed disabled:opacity-40"
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
              class="px-2.5 py-0.5 rounded border border-[#A3D5FF] hover:bg-[#D9F0FF]"
              :class="currentPage === page ? 'bg-[#83C9F4] text-[#1E293B] font-bold' : 'bg-white'"
            >
              {{ page }}
            </button>
            <button
              type="button"
              :disabled="currentPage === totalPages"
              @click="currentPage++"
              class="p-1 rounded border border-[#E2E8F0] hover:bg-white text-[#475569] disabled:cursor-not-allowed disabled:opacity-40"
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
import { computed, nextTick, ref, watch } from 'vue';
import { 
  CheckCheck, 
  FileText, 
  ChevronRight, 
  ChevronLeft,
  Trash2,
  X
} from 'lucide-vue-next';
import { MESSAGES_DATA } from '../mockData.js';

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

const messages = ref([...MESSAGES_DATA]);
const currentPage = ref(1);
const pageSize = 3;
const selectedIds = ref([]);
const selectedMessage = computed(() => messages.value.find(message => message.id === props.selectedMessageId));
const unreadCount = computed(() => messages.value.filter(message => !message.isRead).length);
const totalPages = computed(() => Math.ceil(messages.value.length / pageSize));
const paginatedMessages = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return messages.value.slice(start, start + pageSize);
});
const displayedRange = computed(() => ({
  start: messages.value.length ? (currentPage.value - 1) * pageSize + 1 : 0,
  end: Math.min(currentPage.value * pageSize, messages.value.length),
}));
const deleteNotice = ref('');
const deletedMessages = ref([]);
let deleteNoticeTimeout;

watch(totalPages, pageCount => {
  if (currentPage.value > pageCount) currentPage.value = Math.max(pageCount, 1);
});

watch(
  () => props.searchTargetId,
  targetId => {
    if (!targetId) return;
    nextTick(() => document.getElementById(targetId)?.scrollIntoView({ behavior: 'smooth', block: 'center' }));
  },
  { immediate: true },
);

const goBackToList = () => {
  emit('navigate', 'messages');
};

const handleMarkAllRead = () => {
  const unreadBeforeUpdate = unreadCount.value;
  if (!unreadBeforeUpdate) {
    showNotice('所有消息已是已读状态');
    return;
  }
  messages.value = messages.value.map(m => ({ ...m, isRead: true }));
  showNotice(`已将 ${unreadBeforeUpdate} 条未读消息标记为已读`);
};

const handleDeleteSelected = () => {
  if (!selectedIds.value.length) return;
  const count = selectedIds.value.length;
  const idsToDelete = new Set(selectedIds.value);
  deletedMessages.value = messages.value.filter(message => idsToDelete.has(message.id));
  messages.value = messages.value.filter(message => !idsToDelete.has(message.id));
  selectedIds.value = [];
  deleteNotice.value = `已删除 ${count} 条消息`;
  clearTimeout(deleteNoticeTimeout);
  deleteNoticeTimeout = setTimeout(clearDeleteNotice, 5000);
};

const undoDelete = () => {
  messages.value = [...messages.value, ...deletedMessages.value]
    .sort((left, right) => MESSAGES_DATA.findIndex(message => message.id === left.id) - MESSAGES_DATA.findIndex(message => message.id === right.id));
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
