<template>
  <div class="admin-announcements">

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
        v-for="notice in announcements"
        :key="notice.id"
        class="notice-card"
        :class="[`notice-card--${notice.status}`, notice.isTop ? 'notice-card--top' : '']"
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
            <span v-if="notice.status === 'published'" class="meta-sep">·</span>
            <span v-if="notice.status === 'published'">{{ notice.views.toLocaleString() }} 次阅读</span>
          </div>
        </div>

        <div class="notice-side">
          <span class="status-pill" :class="notice.status === 'published' ? 'status-pill--on' : 'status-pill--draft'">
            {{ notice.status === 'published' ? '已发布' : '草稿' }}
          </span>
          <div class="row-actions">
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
            <button class="icon-btn icon-btn--danger" title="删除" @click="removeNotice(notice)">
              <Delete />
            </button>
          </div>
        </div>
      </article>

      <p v-if="!announcements.length" class="empty-tip">暂无公告,点击右上角「撰写公告」创建。</p>
    </div>

    <!-- 撰写公告弹窗 -->
    <div v-if="modalOpen" class="modal-backdrop" @click.self="closeModal">
      <div class="modal">
        <div class="modal-head">
          <h2 class="modal-title">撰写公告</h2>
          <button class="modal-close" @click="closeModal"><Close /></button>
        </div>
        <form class="modal-body" @submit.prevent="saveDraft">
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
            <button type="submit" class="btn btn--ghost">存为草稿</button>
            <button type="button" class="btn btn--primary" @click="saveDraft(true)">立即发布</button>
          </div>
        </form>
      </div>
    </div>

  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import { Plus, Delete, Close } from '@element-plus/icons-vue';
import { ADMIN_ANNOUNCEMENTS_DATA } from '../../mockData.js';

const announcements = ref(ADMIN_ANNOUNCEMENTS_DATA.map(a => ({ ...a })));

const publishedCount = computed(() => announcements.value.filter(a => a.status === 'published').length);
const draftCount = computed(() => announcements.value.filter(a => a.status === 'draft').length);

/* ---------- 弹窗表单 ---------- */
const modalOpen = ref(false);
const formError = ref('');
const form = ref({ title: '', content: '' });

const openCompose = () => {
  formError.value = '';
  form.value = { title: '', content: '' };
  modalOpen.value = true;
};

const closeModal = () => {
  modalOpen.value = false;
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
  if (!window.confirm(`确认删除公告「${notice.title}」?此操作不可恢复。`)) return;
  announcements.value = announcements.value.filter(a => a.id !== notice.id);
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
  color: #0F1E33;
  letter-spacing: -0.02em;
}

.page-sub {
  margin-top: 4px;
  font-size: 13px;
  color: #64748B;
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
  background: linear-gradient(135deg, #E9B949, #D19A2B);
  color: #241A04;
  border-color: #D19A2B;
  box-shadow: 0 2px 6px rgba(233, 185, 73, 0.35);
}
.btn--primary:hover { background: linear-gradient(135deg, #F0C45E, #D19A2B); }

.btn--ghost {
  background: #fff;
  color: #334155;
  border-color: #E2E8F0;
}
.btn--ghost:hover { border-color: #E9B949; color: #8A6414; }

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
  background: #fff;
  border: 1px solid #E2E8F0;
  border-left: 4px solid #CBD5E1;
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(15, 30, 51, 0.04);
  transition: box-shadow 0.15s, border-color 0.15s;
}

.notice-card:hover {
  box-shadow: 0 6px 16px rgba(15, 30, 51, 0.08);
}

.notice-card--published { border-left-color: #2FBF8F; }
.notice-card--top { border-left-color: #E9B949; }
.notice-card--draft { background: #FDFDFC; }

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
  color: #8A6414;
  background: #FEF3C7;
  border-radius: 9999px;
  flex-shrink: 0;
}

.notice-title {
  font-size: 15px;
  font-weight: 700;
  color: #0F1E33;
  line-height: 1.4;
}

.notice-content {
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.7;
  color: #475569;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notice-meta {
  margin-top: 10px;
  font-size: 11px;
  color: #94A3B8;
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
.status-pill--on    { color: #047857; background: #D1FAE5; }
.status-pill--draft { color: #8A6414; background: #FEF3C7; }

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
.text-btn--primary { color: #005576; background: #D9F0FF; }
.text-btn--primary:hover { background: #C2E4FA; }
.text-btn--ghost { color: #64748B; background: #F1F5F9; }
.text-btn--ghost:hover { background: #E2E8F0; }

.icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  color: #475569;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  transition: background 0.15s, color 0.15s, border-color 0.15s;
}
.icon-btn svg { width: 14px; height: 14px; }
.icon-btn--danger:hover { background: #FFE4E6; color: #BE123C; border-color: #FECDD3; }

.empty-tip {
  padding: 40px 0;
  text-align: center;
  font-size: 13px;
  color: #94A3B8;
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
  background: #fff;
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
  border-bottom: 1px solid #F1F5F9;
}

.modal-title {
  font-size: 16px;
  font-weight: 700;
  color: #0F1E33;
}

.modal-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  color: #64748B;
  background: transparent;
  border: none;
  border-radius: 8px;
  transition: background 0.15s, color 0.15s;
}
.modal-close svg { width: 16px; height: 16px; }
.modal-close:hover { background: #F1F5F9; color: #0F1E33; }

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
  color: #475569;
}

.field-input {
  width: 100%;
  padding: 10px 12px;
  font-size: 13px;
  color: #1E293B;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  outline: none;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.field-input:focus {
  border-color: #2E8BD6;
  box-shadow: 0 0 0 3px rgba(46, 139, 214, 0.12);
}

.field-textarea {
  resize: vertical;
  min-height: 110px;
  line-height: 1.6;
}

.form-error {
  font-size: 12px;
  color: #BE123C;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 4px;
}
</style>
