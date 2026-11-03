<template>
  <div class="admin-announcements admin-fit-page">

    <!-- 页头 -->
    <div class="page-head">
      <div>
        <h1 class="page-title">公告管理</h1>
        <p class="page-sub">共 {{ announcements.length }} 篇公告 · {{ publishedCount }} 篇已发布 · {{ draftCount }} 篇草稿</p>
      </div>
      <button class="btn btn--primary" @click="openCompose">
        <Plus class="btn-icon" /> 撰写公告
      </button>
    </div>

    <!-- 公告列表 -->
    <div class="notice-list">
      <article
        v-for="notice in pagedAnnouncements"
        :key="notice.id"
        class="notice-card"
        :class="[`notice-card--${notice.status}`, notice.isTop ? 'notice-card--top' : '']"
        @click="openDetail(notice)"
      >
        <div class="notice-main">
          <div class="notice-title-row">
            <span v-if="notice.isTop" class="top-chip">置顶</span>
            <h2 class="notice-title">{{ notice.title }}</h2>
          </div>
          <p class="notice-content">{{ notice.content }}</p>
          <div class="notice-meta">
            <span>{{ notice.author }}</span>
            <span class="meta-sep">·</span>
            <span>{{ notice.publishTime }}</span>
          </div>
        </div>

        <div class="notice-side">
          <span class="status-pill" :class="notice.status === 'published' ? 'status-pill--on' : 'status-pill--draft'">
            {{ notice.status === 'published' ? '已发布' : '草稿' }}
          </span>
          <div class="row-actions" @click.stop>
            <button
              v-if="notice.status === 'draft'"
              class="text-btn text-btn--primary"
              @click="publishNotice(notice)"
            >发布</button>
            <button
              v-else
              class="text-btn text-btn--ghost"
              @click="toggleTop(notice)"
            >{{ notice.isTop ? '取消置顶' : '置顶' }}</button>
            <button class="icon-btn" title="编辑" @click="openEdit(notice)">
              <EditPen />
            </button>
            <button class="icon-btn icon-btn--danger" title="删除" @click="removeNotice(notice)">
              <Delete />
            </button>
          </div>
        </div>
      </article>

      <p v-if="!announcements.length" class="empty-tip">暂无公告,点击右上角「撰写公告」创建。</p>
    </div>

    <AdminPager
      v-model:page="page"
      :total="announcements.length"
      :total-pages="totalPages"
    />

    <!-- 公告详情 -->
    <AdminDetailModal
      v-if="detailNotice"
      title="公告详情"
      :subtitle="detailNotice.title"
      :tags="detailTags"
      :rows="[
        { label: '发布人', value: detailNotice.author },
        { label: '发布时间', value: detailNotice.publishTime }
      ]"
      :sections="[{ label: '公告内容', text: detailNotice.content }]"
      @close="detailNotice = null"
    />

    <!-- 撰写 / 编辑公告弹窗 -->
    <div v-if="modalOpen" class="modal-backdrop" @click.self="closeModal">
      <div class="modal">
        <div class="modal-head">
          <h2 class="modal-title">{{ editingId ? '编辑公告' : '撰写公告' }}</h2>
          <button class="modal-close" @click="closeModal"><Close /></button>
        </div>
        <form class="modal-body" @submit.prevent="saveDraft(false)">
          <label class="field">
            <span class="field-label">公告标题</span>
            <input v-model.trim="form.title" type="text" class="field-input" placeholder="请输入公告标题" />
          </label>
          <label class="field">
            <span class="field-label">公告内容</span>
            <textarea
              v-model.trim="form.content"
              rows="5"
              class="field-input field-textarea"
              placeholder="请输入公告正文..."
            ></textarea>
          </label>
          <p v-if="formError" class="form-error">{{ formError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn btn--ghost" @click="closeModal">取消</button>
            <button v-if="editingId" type="submit" class="btn btn--primary">保存修改</button>
            <template v-else>
              <button type="button" class="btn btn--ghost" @click="saveDraft(false)">存为草稿</button>
              <button type="button" class="btn btn--primary" @click="saveDraft(true)">立即发布</button>
            </template>
          </div>
        </form>
      </div>
    </div>

    <ConfirmDialog
      v-if="confirmState"
      :title="confirmState.title"
      :subtitle="confirmState.subtitle"
      :rows="confirmState.rows"
      :tip="confirmState.tip"
      :tone="confirmState.tone"
      :confirm-text="confirmState.confirmText"
      @cancel="cancelConfirm"
      @confirm="acceptConfirm"
    />

  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import { Plus, EditPen, Delete, Close } from '@element-plus/icons-vue';
import { ADMIN_ANNOUNCEMENTS_DATA } from '../../mockData.js';
import { usePager } from '../../composables/usePager.js';
import { useConfirm } from '../../composables/useConfirm.js';
import AdminPager from '../../components/AdminPager.vue';
import AdminDetailModal from '../../components/AdminDetailModal.vue';
import ConfirmDialog from '../../components/ConfirmDialog.vue';

const announcements = ref(ADMIN_ANNOUNCEMENTS_DATA.map(a => ({ ...a })));

const { confirmState, askConfirm, cancelConfirm, acceptConfirm } = useConfirm();

const { page, totalPages, paged: pagedAnnouncements } = usePager(announcements, 5);

const publishedCount = computed(() => announcements.value.filter(a => a.status === 'published').length);
const draftCount = computed(() => announcements.value.filter(a => a.status === 'draft').length);

/* ---------- 公告详情 ---------- */
const detailNotice = ref(null);
const openDetail = (notice) => { detailNotice.value = notice; };
const detailTags = computed(() => {
  if (!detailNotice.value) return [];
  const tags = [detailNotice.value.status === 'published' ? '已发布' : '草稿'];
  if (detailNotice.value.isTop) tags.push('置顶');
  return tags;
});

/* ---------- 弹窗表单 ---------- */
const modalOpen = ref(false);
const editingId = ref(null);
const formError = ref('');
const form = ref({ title: '', content: '' });

const openCompose = () => {
  editingId.value = null;
  formError.value = '';
  form.value = { title: '', content: '' };
  modalOpen.value = true;
};

const openEdit = (notice) => {
  editingId.value = notice.id;
  formError.value = '';
  form.value = { title: notice.title, content: notice.content };
  modalOpen.value = true;
};

const closeModal = () => {
  modalOpen.value = false;
  editingId.value = null;
  formError.value = '';
};

const nowText = () => {
  const d = new Date();
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
};

const saveDraft = (publishNow = false) => {
  if (!form.value.title) { formError.value = '请输入公告标题。'; return; }
  if (!form.value.content) { formError.value = '请输入公告内容。'; return; }

  if (editingId.value) {
    const notice = announcements.value.find(a => a.id === editingId.value);
    if (notice) {
      notice.title = form.value.title;
      notice.content = form.value.content;
    }
    closeModal();
    return;
  }

  announcements.value.unshift({
    id: `ann-${Date.now()}`,
    title: form.value.title,
    content: form.value.content,
    isTop: false,
    status: publishNow ? 'published' : 'draft',
    author: '王智远',
    publishTime: publishNow ? nowText() : `${nowText()} (草稿)`,
    views: 0
  });
  closeModal();
};

const publishNotice = (notice) => {
  notice.status = 'published';
  notice.publishTime = nowText();
};

const toggleTop = (notice) => {
  notice.isTop = !notice.isTop;
};

const removeNotice = (notice) => {
  askConfirm({
    tone: 'danger',
    title: '删除该公告？',
    subtitle: notice.title,
    rows: [
      { label: '公告状态', value: notice.status === 'published' ? '已发布' : '草稿' },
      { label: '发布时间', value: notice.publishTime }
    ],
    tip: '删除后公告将从前台和列表中移除，此操作不可恢复。',
    confirmText: '确认删除',
    action: () => {
      announcements.value = announcements.value.filter(a => a.id !== notice.id);
    }
  });
};
</script>

<style scoped>
/* ============================================================
   AdminAnnouncements · 公告管理 · 纯原生 CSS
   ============================================================ */

* { margin: 0; padding: 0; box-sizing: border-box; }
button, input, textarea { font-family: inherit; }
button { cursor: pointer; }

.admin-announcements {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 100%;
}

/* ========== 页头 ========== */
.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--app-heading);
  letter-spacing: -0.02em;
}

.page-sub {
  margin-top: 4px;
  font-size: 13px;
  color: var(--app-text-4);
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 16px;
  font-size: 13px;
  font-weight: 600;
  border-radius: 10px;
  border: 1px solid transparent;
  transition: background 0.15s, border-color 0.15s, transform 0.15s;
}
.btn:active { transform: translateY(1px); }
.btn-icon { width: 15px; height: 15px; }

.btn--primary {
  background: linear-gradient(135deg, var(--app-brand), var(--app-brand-deep));
  color: var(--app-brand-on);
  border-color: var(--app-brand-deep);
  box-shadow: 0 2px 6px var(--app-brand-glow);
}
.btn--primary:hover { background: linear-gradient(135deg, var(--app-brand-hi), var(--app-brand-deep)); }

.btn--ghost {
  background: var(--app-surface);
  color: var(--app-text-2);
  border-color: var(--app-text-6);
}
.btn--ghost:hover { border-color: var(--app-brand); color: var(--app-brand-ink); }

/* ========== 公告卡片 ========== */
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notice-card {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 18px;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-left: 4px solid var(--app-border-strong);
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(15, 30, 51, 0.04);
  cursor: pointer;
  transition: background 0.15s, box-shadow 0.15s, border-color 0.15s;
}

.notice-card--published { border-left-color: #2FBF8F; }
.notice-card--top { border-left-color: var(--app-brand); }
.notice-card--draft { background: var(--app-surface); }

.notice-card:hover {
  background: var(--app-accent-soft);
  /* 只翻三边,左 4px 条留给「已发布/置顶」的状态色,hover 不再把它刷成品牌青 */
  border-top-color: var(--app-accent-border);
  border-right-color: var(--app-accent-border);
  border-bottom-color: var(--app-accent-border);
  box-shadow: 0 6px 18px var(--app-accent-a20);
}
.notice-card:hover .notice-title { color: var(--app-accent-deep); }
/* 卡片底变成青底后,同为青底的操作按钮会糊进去,改用实底卡片色 */
.notice-card:hover .text-btn--primary { background: var(--app-surface); }

.notice-main { flex: 1; min-width: 0; }

.notice-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.top-chip {
  padding: 2px 8px;
  font-size: 10px;
  font-weight: 700;
  color: var(--app-brand-ink);
  background: var(--app-brand-tint);
  border-radius: 9999px;
  flex-shrink: 0;
}

.notice-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--app-heading);
  line-height: 1.4;
}

.notice-content {
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.7;
  color: var(--app-text-3);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notice-meta {
  margin-top: 10px;
  font-size: 11px;
  color: var(--app-text-5);
}

.meta-sep { margin: 0 6px; }

/* ========== 右侧操作区 ========== */
.notice-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
  flex-shrink: 0;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  font-size: 11px;
  font-weight: 700;
  border-radius: 9999px;
}
.status-pill--on    { color: var(--app-emerald-fg); background: var(--app-emerald-bg); }
.status-pill--draft { color: var(--app-amber-fg); background: var(--app-amber-bg); }

.row-actions {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.text-btn {
  padding: 5px 12px;
  font-size: 12px;
  font-weight: 700;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 9999px;
  transition: background 0.15s;
}
.text-btn--primary { color: var(--app-accent-deep); background: var(--app-accent-soft); }
.text-btn--primary:hover { background: var(--app-accent-hover); }
.text-btn--ghost { color: var(--app-text-4); background: var(--app-surface-3); }
.text-btn--ghost:hover { background: var(--app-chip); }

.icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  color: var(--app-text-3);
  background: var(--app-surface-2);
  border: 1px solid var(--app-border);
  border-radius: 8px;
  transition: background 0.15s, color 0.15s, border-color 0.15s;
}
.icon-btn svg { width: 14px; height: 14px; }
.icon-btn--danger:hover { background: var(--app-rose-bg); color: var(--app-rose-fg); border-color: var(--app-rose-line); }

.empty-tip {
  padding: 40px 0;
  text-align: center;
  font-size: 13px;
  color: var(--app-text-5);
}

/* ========== 弹窗 ========== */
.modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(15, 30, 51, 0.45);
  backdrop-filter: blur(2px);
}

.modal {
  width: 100%;
  max-width: 560px;
  background: var(--app-surface);
  border-radius: 16px;
  box-shadow: 0 24px 48px rgba(15, 30, 51, 0.25);
  overflow: hidden;
  animation: modal-in 0.22s ease-out;
}

@keyframes modal-in {
  from { opacity: 0; transform: translateY(10px) scale(0.98); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}

.modal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--app-border-soft);
}

.modal-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--app-heading);
}

.modal-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  color: var(--app-text-4);
  background: transparent;
  border: none;
  border-radius: 8px;
  transition: background 0.15s, color 0.15s;
}
.modal-close svg { width: 16px; height: 16px; }
.modal-close:hover { background: var(--app-surface-3); color: var(--app-heading); }

.modal-body {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 20px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--app-text-3);
}

.field-input {
  width: 100%;
  padding: 10px 12px;
  font-size: 13px;
  color: var(--app-text);
  background: var(--app-surface-2);
  border: 1px solid var(--app-border);
  border-radius: 10px;
  outline: none;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.field-input:focus {
  border-color: var(--app-accent-strong);
  box-shadow: 0 0 0 3px var(--app-accent-a12);
}

.field-textarea {
  resize: vertical;
  min-height: 110px;
  line-height: 1.6;
}

.form-error {
  font-size: 12px;
  color: var(--app-rose-fg);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 4px;
}
</style>
