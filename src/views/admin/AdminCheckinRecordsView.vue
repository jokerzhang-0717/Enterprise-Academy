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
        <span class="overview-value overview-value--gold">{{ lateCount }}</span>
        <span class="overview-label">迟到</span>
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
          <tr v-for="record in pagedRecords" :key="record.id">
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

  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import { ADMIN_CHECKIN_RECORDS } from '../../mockData.js';
import { usePager } from '../../composables/usePager.js';
import AdminPager from '../../components/AdminPager.vue';

const checkinRecords = ref(ADMIN_CHECKIN_RECORDS.map(c => ({ ...c })));

const { page, totalPages, paged: pagedRecords } = usePager(checkinRecords, 6);

const normalCount = computed(() => checkinRecords.value.filter(c => c.status === 'normal').length);
const lateCount = computed(() => checkinRecords.value.filter(c => c.status === 'late').length);
const missingCount = computed(() => checkinRecords.value.filter(c => c.status === 'missing').length);

const statusLabelMap = {
  normal: '正常',
  late: '迟到',
  missing: '未签到'
};
const statusLabel = (status) => statusLabelMap[status] || status;
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
}

.data-table tbody tr:last-child td { border-bottom: none; }
.data-table tbody tr:hover { background: #F8FAFC; }

.cell-muted { color: #64748B; }

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
.status-pill--normal  { color: #047857; background: #D1FAE5; }
.status-pill--late    { color: #B45309; background: #FEF3C7; }
.status-pill--missing { color: #BE123C; background: #FFE4E6; }

@media (max-width: 640px) {
  .overview-strip { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
</style>
