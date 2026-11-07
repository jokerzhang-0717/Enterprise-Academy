<template>
  <div class="admin-leave admin-fit-page">

    <!-- 页头 -->
    <div class="page-head">
      <div>
        <h1 class="page-title">请假审批</h1>
        <p class="page-sub">员工培训请假申请统一审批 · 线上审批闭环流转</p>
      </div>
    </div>

    <!-- 统计条 -->
    <div class="overview-strip">
      <div class="overview-item">
        <span class="overview-value overview-value--gold">{{ pendingCount }}</span>
        <span class="overview-label">待审批</span>
      </div>
      <div class="overview-item">
        <span class="overview-value overview-value--green">{{ approvedCount }}</span>
        <span class="overview-label">已通过</span>
      </div>
      <div class="overview-item">
        <span class="overview-value overview-value--red">{{ rejectedCount }}</span>
        <span class="overview-label">已驳回</span>
      </div>
      <div class="overview-item">
        <span class="overview-value">{{ weekCount }}</span>
        <span class="overview-label">本周请假人次</span>
      </div>
    </div>

    <div class="filter-toolbar">
      <select v-model="statusFilter" class="filter-select" aria-label="按审批状态筛选">
        <option value="">全部状态</option>
        <option value="pending">待审批</option>
        <option value="approved">已通过</option>
        <option value="rejected">已驳回</option>
      </select>
      <select v-model="departmentFilter" class="filter-select" aria-label="按部门筛选">
        <option value="">全部部门</option>
        <option v-for="department in departments" :key="department" :value="department">
          {{ department }}
        </option>
      </select>
      <label class="filter-search">
        <span class="search-icon" aria-hidden="true"></span>
        <input
          v-model.trim="keyword"
          class="filter-search-input"
          type="text"
          placeholder="搜索姓名 / 工号 / 请假类型 / 事由..."
          aria-label="搜索申请人、工号、请假类型或事由"
        />
        <button
          v-if="keyword"
          type="button"
          class="filter-clear"
          aria-label="清除搜索内容"
          title="清除搜索内容"
          @click="keyword = ''"
        >
          <Close />
        </button>
      </label>
      <span class="filter-count">{{ filteredRequests.length }} 条申请</span>
    </div>

    <!-- 请假审批表 -->
    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>申请人</th>
            <th>请假类型</th>
            <th>时间</th>
            <th class="num">天数</th>
            <th>事由</th>
            <th>提交时间</th>
            <th>状态</th>
            <th class="ops">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="item in pagedRequests"
            :key="item.id"
            @click="openReason(item)"
          >
            <td>
              <div class="cell-user">
                <span class="cell-avatar">
                  <img
                    v-if="item.avatarUrl"
                    :src="item.avatarUrl"
                    :alt="`${item.applicant}头像`"
                    @error="$event.target.style.display = 'none'"
                  />
                  {{ item.applicant.charAt(0) }}
                </span>
                <div>
                  <div class="cell-name">{{ item.applicant }}</div>
                  <div class="cell-meta">{{ item.department }} · {{ item.employeeId }}</div>
                </div>
              </div>
            </td>
            <td><span class="type-chip">{{ item.type }}</span></td>
            <td class="cell-muted">{{ item.timeRange }}</td>
            <td class="num">{{ item.days }} 天</td>
            <td class="reason-cell">
              <div class="reason-text">{{ item.reason }}</div>
            </td>
            <td class="cell-muted">{{ item.submittedTime }}</td>
            <td>
              <span class="status-pill" :class="`status-pill--${item.status}`">
                {{ statusLabel(item.status) }}
              </span>
            </td>
            <td class="ops">
              <div class="row-actions" v-if="item.status === 'pending'" @click.stop>
                <button class="text-btn text-btn--ok" @click="decide(item, 'approved')">通过</button>
                <button class="text-btn text-btn--reject" @click="decide(item, 'rejected')">驳回</button>
              </div>
              <span v-else class="cell-muted">已处理</span>
            </td>
          </tr>
          <tr v-if="!filteredRequests.length">
            <td colspan="8" class="empty-cell">暂无相关请假申请</td>
          </tr>
        </tbody>
      </table>
    </div>

    <AdminPager
      v-model:page="page"
      :total="filteredRequests.length"
      :total-pages="totalPages"
    />

    <!-- 审批确认 -->
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

    <!-- 请假事由详情 -->
    <Teleport to="body">
      <div v-if="reasonDetail" class="reason-modal" @click.self="closeReason">
        <div class="reason-dialog" role="dialog" aria-modal="true" aria-labelledby="reason-dialog-title">
          <div class="reason-dialog-head">
            <div>
              <h3 id="reason-dialog-title" class="reason-dialog-title">请假事由</h3>
              <p class="reason-dialog-sub">
                {{ reasonDetail.applicant }} · {{ reasonDetail.department }} · {{ reasonDetail.employeeId }}
              </p>
            </div>
            <button class="reason-dialog-close" aria-label="关闭" @click="closeReason">✕</button>
          </div>
          <div class="reason-dialog-body">
            <div class="reason-dialog-meta">
              <span class="type-chip">{{ reasonDetail.type }}</span>
              <span class="reason-dialog-time">{{ reasonDetail.timeRange }} · {{ reasonDetail.days }} 天</span>
              <span class="status-pill" :class="`status-pill--${reasonDetail.status}`">
                {{ statusLabel(reasonDetail.status) }}
              </span>
            </div>
            <p class="reason-dialog-text">{{ reasonDetail.reason }}</p>
            <p class="reason-dialog-foot">提交时间：{{ reasonDetail.submittedTime }}</p>
          </div>
          <div class="reason-dialog-actions">
            <button type="button" class="reason-dialog-btn" @click="closeReason">关闭</button>
          </div>
        </div>
      </div>
    </Teleport>

  </div>
</template>

<script setup>
import { computed, ref, watch, onMounted, onBeforeUnmount } from 'vue';
import { Close } from '@element-plus/icons-vue';
import { adminStore, loadLeaves } from '../../adminStore.js';
import { approveAdminLeave } from '../../api/index.js';
import { usePager } from '../../composables/usePager.js';
import { useConfirm } from '../../composables/useConfirm.js';
import { useToast } from '../../composables/useToast.js';
import AdminPager from '../../components/AdminPager.vue';
import ConfirmDialog from '../../components/ConfirmDialog.vue';

const { show: showToast } = useToast();

const requests = computed(() => adminStore.leaves);
const statusFilter = ref('pending');
const departmentFilter = ref('');
const keyword = ref('');

const { confirmState, askConfirm, cancelConfirm, acceptConfirm } = useConfirm();

const pendingCount = computed(() => requests.value.filter(r => r.status === 'pending').length);
const approvedCount = computed(() => requests.value.filter(r => r.status === 'approved').length);
const rejectedCount = computed(() => requests.value.filter(r => r.status === 'rejected').length);
const departments = computed(() => [...new Set(requests.value.map(r => r.department).filter(Boolean))].sort());

/* 接口只给按状态的统计，本周人次按提交时间在本地算 */
const weekCount = computed(() => {
  const now = new Date();
  const day = (now.getDay() + 6) % 7;
  const monday = new Date(now.getFullYear(), now.getMonth(), now.getDate() - day);
  return requests.value.filter(r => {
    const at = r.submittedAt ? new Date(r.submittedAt) : null;
    return at && at >= monday;
  }).length;
});

const filteredRequests = computed(() => {
  const search = keyword.value.toLowerCase();
  return requests.value.filter((request) => {
    const matchesStatus = !statusFilter.value || request.status === statusFilter.value;
    const matchesDepartment = !departmentFilter.value || request.department === departmentFilter.value;
    const matchesKeyword = !search ||
      `${request.applicant} ${request.employeeId} ${request.department} ${request.type} ${request.reason}`.toLowerCase().includes(search);
    return matchesStatus && matchesDepartment && matchesKeyword;
  });
});

const { page, totalPages, paged: pagedRequests, resetPage } = usePager(filteredRequests, 6);
watch([statusFilter, departmentFilter, keyword], resetPage);

const statusLabelMap = {
  pending: '待审批',
  approved: '已通过',
  rejected: '已驳回'
};
const statusLabel = (status) => statusLabelMap[status] || status;

/* ---------- 事由详情 ---------- */
const reasonDetail = ref(null);
const openReason = (item) => { reasonDetail.value = item; };
const closeReason = () => { reasonDetail.value = null; };
const onKeydown = (e) => { if (e.key === 'Escape') closeReason(); };
onMounted(() => window.addEventListener('keydown', onKeydown));
onBeforeUnmount(() => window.removeEventListener('keydown', onKeydown));

const decide = (item, status) => {
  const approve = status === 'approved';
  askConfirm({
    tone: approve ? 'success' : 'danger',
    title: approve ? '通过这条请假申请？' : '驳回这条请假申请？',
    subtitle: `${item.applicant} · ${item.department} · ${item.employeeId}`,
    rows: [
      { label: '请假类型', value: item.type },
      { label: '请假时间', value: `${item.timeRange} · ${item.days} 天` },
      { label: '提交时间', value: item.submittedTime }
    ],
    tip: approve
      ? '通过后，该学员培训考勤将按请假记录，不计入缺勤。'
      : '驳回后，该学员将收到驳回通知，需正常参加培训。',
    confirmText: approve ? '确认通过' : '确认驳回',
    action: async () => {
      try {
        await approveAdminLeave(item.numericId ?? item.id, status);
        await loadLeaves();
        showToast(approve ? '已通过该请假申请' : '已驳回该请假申请', 'success');
      } catch (err) {
        showToast(err?.message || '审批失败，请重试', 'error');
      }
    }
  });
};
</script>

<style scoped>

* { margin: 0; padding: 0; box-sizing: border-box; }
button { font-family: inherit; cursor: pointer; }

.admin-leave {
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

/* ========== 概览条 ========== */
.overview-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.overview-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 14px;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 12px;
}

.overview-value {
  font-size: 22px;
  font-weight: 800;
  color: var(--app-heading);
  font-variant-numeric: tabular-nums;
}
.overview-value--green { color: var(--app-emerald-fg); }
.overview-value--gold  { color: var(--app-amber-fg); }
.overview-value--red   { color: var(--app-rose-fg); }

.overview-label {
  font-size: 12px;
  color: var(--app-text-4);
}

/* ========== 筛选工具栏 ========== */
.filter-toolbar {
  display: grid;
  grid-template-columns: minmax(140px, 180px) minmax(140px, 180px) minmax(220px, 1fr) auto;
  align-items: center;
  gap: 12px;
  min-height: 64px;
  padding: 12px 16px;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 12px;
}
.filter-select,
.filter-search {
  width: 100%;
  min-width: 0;
  height: 40px;
  color: var(--app-text-2);
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 10px;
}
.filter-select {
  padding: 0 34px 0 12px;
  font-size: 13px;
  outline: none;
  cursor: pointer;
}
.filter-select:focus,
.filter-search:focus-within {
  border-color: var(--app-accent-strong);
  box-shadow: 0 0 0 3px var(--app-accent-a12);
}
.filter-search { display: flex; align-items: center; gap: 8px; padding: 0 12px; }
.search-icon { position: relative; width: 14px; height: 14px; flex: 0 0 14px; }
.search-icon::before {
  position: absolute; top: 0; left: 0; width: 9px; height: 9px;
  border: 2px solid var(--app-text-4); border-radius: 50%; content: '';
}
.search-icon::after {
  position: absolute; right: 0; bottom: 0; width: 6px; height: 2px;
  background: var(--app-text-4); border-radius: 1px; content: '';
  transform: rotate(45deg); transform-origin: right center;
}
.filter-search-input {
  flex: 1; min-width: 0; height: 100%; font-size: 13px; color: var(--app-text);
  background: transparent; border: 0; outline: 0;
}
.filter-search-input::placeholder { color: var(--app-text-5); }
.filter-clear {
  display: inline-flex; flex: 0 0 24px; align-items: center; justify-content: center;
  width: 24px; height: 24px; padding: 0; color: var(--app-text-4);
  background: transparent; border: 0; border-radius: 50%;
}
.filter-clear:hover { color: var(--app-text-2); background: var(--app-chip); }
.filter-clear svg { width: 14px; height: 14px; }
.filter-count {
  justify-self: end; padding: 7px 11px; font-size: 12px; font-weight: 600;
  color: var(--app-accent-deep); background: var(--app-accent-soft);
  border-radius: 999px; white-space: nowrap;
}

/* ========== 表格 ========== */
.table-wrap {
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 12px;
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.data-table thead th {
  padding: 12px 14px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.06em;
  color: var(--app-text-4);
  text-align: left;
  background: var(--app-surface-2);
  border-bottom: 1px solid var(--app-border);
  white-space: nowrap;
}

.data-table tbody td {
  padding: 12px 14px;
  color: var(--app-text);
  border-bottom: 1px solid var(--app-border-soft);
  vertical-align: top;
}

.data-table tbody tr:last-child td { border-bottom: none; }
.data-table tbody tr { cursor: pointer; }
.data-table tbody td { transition: background 0.15s, color 0.15s, box-shadow 0.15s; }
.data-table tbody tr:hover td { background: var(--app-accent-soft); }
.data-table tbody tr:hover td:first-child { box-shadow: inset 3px 0 0 var(--app-accent); }

/* 悬停整行时，整行文字统一变为主色 */
.data-table tbody tr:hover td,
.data-table tbody tr:hover .cell-name,
.data-table tbody tr:hover .cell-meta,
.data-table tbody tr:hover .cell-muted,
.data-table tbody tr:hover .reason-text {
  color: var(--app-accent-deep);
}

th.num, td.num { text-align: right; white-space: nowrap; }
th.ops, td.ops { text-align: right; white-space: nowrap; }

/* 表头默认 left 优先级更高,这里提权让数字列/操作列的表头与其内容对齐 */
.data-table thead th.num,
.data-table thead th.ops { text-align: right; }

.cell-muted { color: var(--app-text-4); white-space: nowrap; }
.empty-cell { text-align: center; color: var(--app-text-5); padding: 28px 0 !important; }

/* ========== 申请人单元格 ========== */
.cell-user {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 150px;
}

.cell-avatar {
  position: relative;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  color: var(--app-accent-deep);
  background: var(--app-accent-soft);
  border-radius: 50%;
  flex-shrink: 0;
  overflow: hidden;
}

.cell-avatar img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  border-radius: inherit;
  object-fit: cover;
}

.cell-name {
  font-weight: 600;
  white-space: nowrap;
}

.cell-meta {
  margin-top: 2px;
  font-size: 11px;
  color: var(--app-text-5);
  white-space: nowrap;
}

/* ========== 类型与事由 ========== */
.type-chip {
  display: inline-flex;
  padding: 3px 10px;
  font-size: 11px;
  font-weight: 600;
  color: var(--app-violet-fg);
  background: var(--app-violet-bg);
  border-radius: 9999px;
  white-space: nowrap;
}

.reason-cell {
  width: 320px;
  min-width: 260px;
  color: var(--app-text-3);
  cursor: pointer;
}

/* 事由最多显示两行，超出省略；点击单元格查看完整内容 */
.reason-text {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-size: 12.5px;
  line-height: 1.6;
  white-space: normal;
  word-break: break-word;
  transition: color 0.15s;
}

/* ========== 状态徽标 ========== */
.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 56px;
  padding: 3px 10px;
  font-size: 11px;
  font-weight: 700;
  border-radius: 9999px;
  white-space: nowrap;
}
.status-pill--pending  { color: var(--app-amber-fg); background: var(--app-amber-bg); }
.status-pill--approved { color: var(--app-emerald-fg); background: var(--app-emerald-bg); }
.status-pill--rejected { color: var(--app-rose-fg); background: var(--app-rose-bg); }

/* ========== 行操作 ========== */
.row-actions {
  display: inline-flex;
  gap: 6px;
}

.text-btn {
  padding: 5px 12px;
  font-size: 12px;
  font-weight: 700;
  border: 1px solid transparent;
  border-radius: 9999px;
  transition: background 0.15s;
}
.text-btn--ok { color: var(--app-emerald-fg); background: var(--app-emerald-bg); }
.text-btn--ok:hover { background: var(--app-emerald-line); }
.text-btn--reject { color: var(--app-rose-fg); background: var(--app-rose-bg); }
.text-btn--reject:hover { background: var(--app-rose-line); }

/* ========== 事由详情弹窗 ========== */
.reason-modal {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgb(15 30 51 / 50%);
  backdrop-filter: blur(4px);
  animation: reason-fade 0.16s ease-out;
}

.reason-dialog {
  width: 100%;
  max-width: 520px;
  max-height: 86vh;
  display: flex;
  flex-direction: column;
  background: var(--app-surface);
  border-radius: 16px;
  box-shadow: 0 28px 60px rgb(15 30 51 / 28%);
  animation: reason-in 0.2s ease-out;
}

.reason-dialog-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 20px 14px;
  border-bottom: 1px solid var(--app-border-soft);
}

.reason-dialog-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--app-heading);
}

.reason-dialog-sub {
  margin-top: 4px;
  font-size: 12px;
  color: var(--app-text-4);
}

.reason-dialog-close {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  color: var(--app-text-4);
  background: transparent;
  border: none;
  border-radius: 7px;
  flex-shrink: 0;
  transition: background 0.15s, color 0.15s;
}
.reason-dialog-close:hover { background: var(--app-surface-3); color: var(--app-heading); }

.reason-dialog-body {
  padding: 16px 20px 18px;
  overflow-y: auto;
}

.reason-dialog-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.reason-dialog-time {
  font-size: 12px;
  color: var(--app-text-4);
}

.reason-dialog-text {
  margin-top: 12px;
  padding: 12px 14px;
  font-size: 13px;
  line-height: 1.75;
  color: var(--app-text-2);
  background: var(--app-surface-2);
  border: 1px solid var(--app-border-soft);
  border-radius: 10px;
  white-space: pre-wrap;
  word-break: break-word;
}

.reason-dialog-foot {
  margin-top: 10px;
  font-size: 11px;
  color: var(--app-text-5);
}

.reason-dialog-actions {
  display: flex;
  justify-content: flex-end;
  padding: 12px 20px 16px;
  border-top: 1px solid var(--app-border-soft);
}

.reason-dialog-btn {
  padding: 7px 16px;
  font-size: 12px;
  font-weight: 600;
  color: var(--app-text-3);
  background: var(--app-surface-2);
  border: 1px solid var(--app-border);
  border-radius: 9px;
  transition: background 0.15s, color 0.15s;
}
.reason-dialog-btn:hover { background: var(--app-surface-3); color: var(--app-heading); }

@keyframes reason-fade {
  from { opacity: 0; }
  to   { opacity: 1; }
}
@keyframes reason-in {
  from { opacity: 0; transform: translateY(10px) scale(0.98); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}

@media (max-width: 640px) {
  .overview-strip { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}

@media (max-width: 900px) {
  .filter-toolbar { grid-template-columns: minmax(0, 1fr) minmax(0, 1fr); }
  .filter-search, .filter-count { grid-column: 1 / -1; }
  .filter-count { justify-self: start; }
}

@media (max-width: 420px) {
  .filter-toolbar { grid-template-columns: minmax(0, 1fr); }
  .filter-select, .filter-search, .filter-count { grid-column: 1; }
}

/* 1366 / 1440 这类窄屏:收窄左右内边距,
   否则 8 列的最小宽度会超过容器,挤出横向滚动条并连带压出纵向滚动条 */
@media (max-width: 1440px) {
  .data-table thead th,
  .data-table tbody td {
    padding-left: 8px;
    padding-right: 8px;
  }
  .reason-cell { width: 280px; min-width: 220px; }
}

/* 窄屏下收窄事由列,避免表格横向溢出 */
@media (max-width: 1280px) {
  .reason-cell { width: 240px; min-width: 200px; }
  .reason-text { font-size: 12px; }
}
</style>
