<template>
  <div class="admin-checkin">

    <!-- 页头 -->
    <div class="page-head">
      <div>
        <h1 class="page-title">签到记录</h1>
        <p class="page-sub">线上签到统一入口 · 今日数据更新于 16:30</p>
      </div>
    </div>

    <!-- 统计条 -->
    <div class="overview-strip">
      <div class="overview-item">
        <span class="overview-value overview-value--green">{{ normalCount }}</span>
        <span class="overview-label">正常签到</span>
      </div>
      <div class="overview-item">
        <span class="overview-value overview-value--red">{{ missingCount }}</span>
        <span class="overview-label">未签到</span>
      </div>
      <div class="overview-item">
        <span class="overview-value">328</span>
        <span class="overview-label">应签到总人数</span>
      </div>
    </div>

    <!-- 签到明细表 -->
    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>员工</th>
            <th>部门</th>
            <th>签到时间</th>
            <th>签退时间</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="record in pagedRecords" :key="record.id" @click="openDetail(record)">
            <td>
              <div class="cell-user">
                <span class="cell-avatar">{{ record.name.charAt(0) }}</span>
                <div>
                  <div class="cell-name">{{ record.name }}</div>
                  <div class="cell-meta">{{ record.employeeId }}</div>
                </div>
              </div>
            </td>
            <td class="cell-muted">{{ record.department }}</td>
            <td class="mono-cell">{{ record.checkInTime }}</td>
            <td class="mono-cell">{{ record.checkOutTime }}</td>
            <td>
              <span class="status-pill" :class="`status-pill--${record.status}`">
                {{ statusLabel(record.status) }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <AdminPager
      v-model:page="page"
      :total="checkinRecords.length"
      :total-pages="totalPages"
    />

    <!-- 签到详情 -->
    <AdminDetailModal
      v-if="detailRecord"
      title="签到详情"
      :subtitle="`${detailRecord.name} · ${detailRecord.employeeId}`"
      :tags="[statusLabel(detailRecord.status)]"
      :rows="[
        { label: '所属部门', value: detailRecord.department },
        { label: '签到时间', value: detailRecord.checkInTime || '—' },
        { label: '签退时间', value: detailRecord.checkOutTime || '—' },
        { label: '签到状态', value: statusLabel(detailRecord.status) }
      ]"
      @close="detailRecord = null"
    />

  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import { ADMIN_CHECKIN_RECORDS } from '../../mockData.js';
import { usePager } from '../../composables/usePager.js';
import AdminPager from '../../components/AdminPager.vue';
import AdminDetailModal from '../../components/AdminDetailModal.vue';

const checkinRecords = ref(ADMIN_CHECKIN_RECORDS.filter(c => c.status !== 'late').map(c => ({ ...c })));

const { page, totalPages, paged: pagedRecords } = usePager(checkinRecords, 6);

const normalCount = computed(() => checkinRecords.value.filter(c => c.status === 'normal').length);
const missingCount = computed(() => checkinRecords.value.filter(c => c.status === 'missing').length);

const statusLabelMap = {
  normal: '正常',
  missing: '未签到'
};
const statusLabel = (status) => statusLabelMap[status] || status;

/* ---------- 签到详情 ---------- */
const detailRecord = ref(null);
const openDetail = (record) => { detailRecord.value = record; };
</script>

<style scoped>
/* ============================================================
   AdminCheckinRecords · 签到记录 · 纯原生 CSS
   ============================================================ */

* { margin: 0; padding: 0; box-sizing: border-box; }

.admin-checkin {
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
  grid-template-columns: repeat(3, minmax(0, 1fr));
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
}

.data-table tbody tr:last-child td { border-bottom: none; }
.data-table tbody tr { cursor: pointer; }
.data-table tbody td { transition: background 0.15s, color 0.15s, box-shadow 0.15s; }
.data-table tbody tr:hover td { background: var(--app-accent-soft); }
.data-table tbody tr:hover td:first-child { box-shadow: inset 3px 0 0 var(--app-accent); }
.data-table tbody tr:hover td,
.data-table tbody tr:hover .cell-name,
.data-table tbody tr:hover .cell-meta,
.data-table tbody tr:hover .cell-muted,
.data-table tbody tr:hover .mono-cell {
  color: var(--app-accent-deep);
}

.cell-muted { color: var(--app-text-4); }

.mono-cell {
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  font-size: 12px;
  font-variant-numeric: tabular-nums;
}

/* ========== 员工单元格 ========== */
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
  color: var(--app-accent-deep);
  background: var(--app-accent-soft);
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
  color: var(--app-text-5);
  white-space: nowrap;
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
.status-pill--normal  { color: var(--app-emerald-fg); background: var(--app-emerald-bg); }
.status-pill--late    { color: var(--app-amber-fg); background: var(--app-amber-bg); }
.status-pill--missing { color: var(--app-rose-fg); background: var(--app-rose-bg); }

@media (max-width: 640px) {
  .overview-strip { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
</style>
