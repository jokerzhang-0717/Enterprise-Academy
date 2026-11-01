<template>
  <div class="admin-leave">

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
        <span class="overview-value">12</span>
        <span class="overview-label">本周请假人次</span>
      </div>
    </div>

    <!-- 筛选 Tab -->
    <div class="filter-tabs">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        :class="['filter-tab', statusFilter === tab.key ? 'filter-tab--active' : '']"
        @click="statusFilter = tab.key"
      >
        {{ tab.label }}
        <span class="filter-tab-count">{{ tabCount(tab.key) }}</span>
      </button>
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
          <tr v-for="item in pagedRequests" :key="item.id">
            <td>
              <div class="cell-user">
                <span class="cell-avatar">{{ item.applicant.charAt(0) }}</span>
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
              <div class="row-actions" v-if="item.status === 'pending'">
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

  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import { ADMIN_LEAVE_REQUESTS } from '../../mockData.js';
import { usePager } from '../../composables/usePager.js';
import { useConfirm } from '../../composables/useConfirm.js';
import AdminPager from '../../components/AdminPager.vue';
import ConfirmDialog from '../../components/ConfirmDialog.vue';

const requests = ref(ADMIN_LEAVE_REQUESTS.map(r => ({ ...r })));
const statusFilter = ref('pending');

const { confirmState, askConfirm, cancelConfirm, acceptConfirm } = useConfirm();

const tabs = [
  { key: '',        label: '全部' },
  { key: 'pending', label: '待审批' },
  { key: 'approved', label: '已通过' },
  { key: 'rejected', label: '已驳回' }
];

const pendingCount = computed(() => requests.value.filter(r => r.status === 'pending').length);
const approvedCount = computed(() => requests.value.filter(r => r.status === 'approved').length);
const rejectedCount = computed(() => requests.value.filter(r => r.status === 'rejected').length);

const filteredRequests = computed(() => {
  if (!statusFilter.value) return requests.value;
  return requests.value.filter(r => r.status === statusFilter.value);
});

const { page, totalPages, paged: pagedRequests, resetPage } = usePager(filteredRequests, 4);
watch(statusFilter, resetPage);

const tabCount = (key) => {
  if (!key) return requests.value.length;
  return requests.value.filter(r => r.status === key).length;
};

const statusLabelMap = {
  pending: '待审批',
  approved: '已通过',
  rejected: '已驳回'
};
const statusLabel = (status) => statusLabelMap[status] || status;

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
    action: () => { item.status = status; }
  });
};
</script>

<style scoped>
/* ============================================================
   AdminLeaveApproval · 请假审批 · 纯原生 CSS
   ============================================================ */

* { margin: 0; padding: 0; box-sizing: border-box; }
button { font-family: inherit; cursor: pointer; }

.admin-leave {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 100%;
  height: 100%;
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
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
}

.overview-value {
  font-size: 22px;
  font-weight: 800;
  color: #0F1E33;
  font-variant-numeric: tabular-nums;
}
.overview-value--green { color: #047857; }
.overview-value--gold  { color: #B45309; }
.overview-value--red   { color: #BE123C; }

.overview-label {
  font-size: 12px;
  color: #64748B;
}

/* ========== 筛选 Tab ========== */
.filter-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.filter-tab {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 9999px;
  transition: background 0.15s, color 0.15s, border-color 0.15s;
}

.filter-tab:hover {
  border-color: #E9B949;
  color: #8A6414;
}

.filter-tab--active {
  background: linear-gradient(135deg, #E9B949, #D19A2B);
  border-color: #D19A2B;
  color: #241A04;
}

.filter-tab-count {
  min-width: 18px;
  height: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 5px;
  font-size: 11px;
  font-weight: 700;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 9999px;
}

/* ========== 表格 ========== */
.table-wrap {
  background: #fff;
  border: 1px solid #E2E8F0;
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
  color: #64748B;
  text-align: left;
  background: #F8FAFC;
  border-bottom: 1px solid #E2E8F0;
  white-space: nowrap;
}

.data-table tbody td {
  padding: 12px 14px;
  color: #1E293B;
  border-bottom: 1px solid #F1F5F9;
  vertical-align: top;
}

.data-table tbody tr:last-child td { border-bottom: none; }
.data-table tbody tr:hover { background: #F8FAFC; }

th.num, td.num { text-align: right; white-space: nowrap; }
th.ops, td.ops { text-align: right; white-space: nowrap; }

/* 表头默认 left 优先级更高,这里提权让数字列/操作列的表头与其内容对齐 */
.data-table thead th.num,
.data-table thead th.ops { text-align: right; }

.cell-muted { color: #64748B; }
.empty-cell { text-align: center; color: #94A3B8; padding: 28px 0 !important; }

/* ========== 申请人单元格 ========== */
.cell-user {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 150px;
}

.cell-avatar {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  color: #005576;
  background: #D9F0FF;
  border-radius: 50%;
  flex-shrink: 0;
}

.cell-name {
  font-weight: 600;
  white-space: nowrap;
}

.cell-meta {
  margin-top: 2px;
  font-size: 11px;
  color: #94A3B8;
  white-space: nowrap;
}

/* ========== 类型与事由 ========== */
.type-chip {
  display: inline-flex;
  padding: 3px 10px;
  font-size: 11px;
  font-weight: 600;
  color: #6555B0;
  background: #EDE9FE;
  border-radius: 9999px;
  white-space: nowrap;
}

.reason-cell {
  width: 320px;
  min-width: 260px;
  color: #475569;
}

/* 事由完整展示:由单元格自然换行,不再截断 */
.reason-text {
  font-size: 12.5px;
  line-height: 1.6;
  white-space: normal;
  word-break: break-word;
}

/* ========== 状态徽标 ========== */
.status-pill {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  font-size: 11px;
  font-weight: 700;
  border-radius: 9999px;
  white-space: nowrap;
}
.status-pill--pending  { color: #B45309; background: #FEF3C7; }
.status-pill--approved { color: #047857; background: #D1FAE5; }
.status-pill--rejected { color: #BE123C; background: #FFE4E6; }

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
.text-btn--ok { color: #047857; background: #D1FAE5; }
.text-btn--ok:hover { background: #A7F3D0; }
.text-btn--reject { color: #BE123C; background: #FFE4E6; }
.text-btn--reject:hover { background: #FECDD3; }

@media (max-width: 640px) {
  .overview-strip { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}

/* 窄屏下收窄事由列,避免表格横向溢出 */
@media (max-width: 1280px) {
  .reason-cell { width: 240px; min-width: 200px; }
  .reason-text { font-size: 12px; }
}
</style>
