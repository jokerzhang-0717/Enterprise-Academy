<template>
  <div class="admin-checkin admin-fit-page">

    <!-- 页头 -->
    <div class="page-head">
      <div>
        <h1 class="page-title">签到记录</h1>
        <p class="page-sub">线上签到统一入口 · 共 {{ checkinRecords.length }} 条记录</p>
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
        <span class="overview-value">{{ learnerCount }}</span>
        <span class="overview-label">应签到总人数</span>
      </div>
    </div>

    <div class="filter-toolbar">
      <select v-model="departmentFilter" class="filter-select" aria-label="按部门筛选签到记录">
        <option value="">全部部门</option>
        <option v-for="department in departments" :key="department" :value="department">
          {{ department }}
        </option>
      </select>
      <select v-model="statusFilter" class="filter-select" aria-label="按签到状态筛选">
        <option value="">全部状态</option>
        <option value="normal">正常</option>
        <option value="missing">未签到</option>
      </select>
      <label class="filter-search">
        <span class="search-icon" aria-hidden="true"></span>
        <input
          v-model.trim="keyword"
          class="filter-search-input"
          type="text"
          placeholder="搜索姓名 / 工号 / 部门..."
          aria-label="搜索姓名、工号或部门"
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
      <span class="filter-count">{{ filteredRecords.length }} 条记录</span>
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
          <tr v-if="!filteredRecords.length">
            <td colspan="5" class="empty-cell">没有匹配的签到记录</td>
          </tr>
        </tbody>
      </table>
    </div>

    <AdminPager
      v-model:page="page"
      :total="filteredRecords.length"
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
import { computed, ref, watch } from 'vue';
import { Close } from '@element-plus/icons-vue';
import { adminStore } from '../../adminStore.js';
import { usePager } from '../../composables/usePager.js';
import AdminPager from '../../components/AdminPager.vue';
import AdminDetailModal from '../../components/AdminDetailModal.vue';

/* 线上签到页只统计正常与未签到，迟到由请假流程单独处理 */
const checkinRecords = computed(() => adminStore.checkins.filter(c => c.status !== 'late'));
const learnerCount = computed(() => adminStore.users.length);

const departmentFilter = ref('');
const statusFilter = ref('');
const keyword = ref('');
const departments = computed(() => [...new Set(checkinRecords.value.map(r => r.department).filter(Boolean))].sort());

const filteredRecords = computed(() => {
  const search = keyword.value.toLowerCase();
  return checkinRecords.value.filter((record) => {
    const matchesDepartment = !departmentFilter.value || record.department === departmentFilter.value;
    const matchesStatus = !statusFilter.value || record.status === statusFilter.value;
    const matchesKeyword = !search ||
      `${record.name} ${record.employeeId} ${record.department}`.toLowerCase().includes(search);
    return matchesDepartment && matchesStatus && matchesKeyword;
  });
});

const { page, totalPages, paged: pagedRecords, resetPage } = usePager(filteredRecords, 7);
watch([departmentFilter, statusFilter, keyword], resetPage);

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

/* ========== 筛选工具栏 ========== */
.filter-toolbar {
  display: grid;
  grid-template-columns: minmax(140px, 180px) minmax(130px, 160px) minmax(220px, 1fr) auto;
  align-items: center;
  gap: 12px;
  min-height: 64px;
  padding: 12px 16px;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 12px;
}
.filter-select, .filter-search {
  width: 100%; min-width: 0; height: 40px; color: var(--app-text-2);
  background: var(--app-surface); border: 1px solid var(--app-border); border-radius: 10px;
}
.filter-select { padding: 0 34px 0 12px; font-size: 13px; outline: none; cursor: pointer; }
.filter-select:focus, .filter-search:focus-within {
  border-color: var(--app-accent-strong); box-shadow: 0 0 0 3px var(--app-accent-a12);
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
.empty-cell { padding: 28px 0; color: var(--app-text-5); text-align: center; }

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
  justify-content: center;
  min-width: 56px;
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

@media (max-width: 900px) {
  .filter-toolbar { grid-template-columns: minmax(0, 1fr) minmax(0, 1fr); }
  .filter-search, .filter-count { grid-column: 1 / -1; }
  .filter-count { justify-self: start; }
}

@media (max-width: 420px) {
  .filter-toolbar { grid-template-columns: minmax(0, 1fr); }
  .filter-select, .filter-search, .filter-count { grid-column: 1; }
}
</style>
