<template>
  <div class="admin-checkin admin-fit-page">

    <!-- 页头 -->
    <div class="page-head">
      <div>
        <h1 class="page-title">签到记录</h1>
        <p class="page-sub">
          {{ dateFilter }} 签到情况 ·
          {{ recordsLoadError ? '加载失败' : loadingRecords ? '加载中…' : `应到 ${learners.length} 人 · ${checkinRecords.length} 人` }}
        </p>
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
      <input v-model="dateFilter" class="filter-select filter-date" type="date" aria-label="选择签到日期" />
      <select v-model="departmentFilter" class="filter-select" aria-label="按部门筛选签到记录">
        <option value="">全部部门</option>
        <option v-for="department in departments" :key="department" :value="department">
          {{ department }}
        </option>
      </select>
      <select v-model="statusFilter" class="filter-select" aria-label="按签到状态筛选">
        <option value="">全部状态</option>
        <option value="normal">正常</option>
        <option value="late">迟到</option>
        <option value="early">早退</option>
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
            <th>日期</th>
            <th>部门</th>
            <th>签到时间</th>
            <th>签退时间</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="record in pagedRecords" :key="record.id" @click="openDetail(record)">
            <td>
              <div class="cell-user">
                <span class="cell-avatar">
                  <img
                    v-if="record.avatarUrl"
                    :src="record.avatarUrl"
                    :alt="`${record.name}头像`"
                    @error="$event.target.style.display = 'none'"
                  />
                  {{ record.name.charAt(0) }}
                </span>
                <div>
                  <div class="cell-name">{{ record.name }}</div>
                  <div class="cell-meta">{{ record.employeeId }}</div>
                </div>
              </div>
            </td>
            <td class="mono-cell">{{ record.checkinDate || dateFilter }}</td>
            <td class="cell-muted">{{ record.department }}</td>
            <td class="mono-cell">{{ record.checkInTime }}</td>
            <td class="mono-cell">{{ record.checkOutTime }}</td>
            <td>
              <span class="status-pill" :class="`status-pill--${record.status}`">
                {{ statusLabel(record.status) }}
              </span>
            </td>
            <td class="row-action-cell" @click.stop>
              <button
                type="button"
                class="edit-record-btn"
                :disabled="savingRecord"
                @click="openEdit(record)"
              >
                <EditPen />
                修改
              </button>
            </td>
          </tr>
          <tr v-if="!filteredRecords.length">
            <td colspan="7" class="empty-cell">
              {{ recordsLoadError || (loadingRecords ? '正在加载签到数据…' : '没有匹配的签到记录') }}
            </td>
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
        { label: '签到日期', value: detailRecord.checkinDate || dateFilter },
        { label: '签到时间', value: detailRecord.checkInTime || '—' },
        { label: '签退时间', value: detailRecord.checkOutTime || '—' },
        { label: '签到状态', value: statusLabel(detailRecord.status) }
      ]"
      @close="detailRecord = null"
    />

    <div v-if="editingRecord" class="edit-modal-backdrop" @click.self="closeEdit">
      <section
        class="edit-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="edit-checkin-title"
      >
        <header class="edit-modal__head">
          <div>
            <h2 id="edit-checkin-title" class="edit-modal__title">修改签到记录</h2>
            <p class="edit-modal__subtitle">{{ editingRecord.name }} · {{ editingRecord.employeeId }}</p>
          </div>
          <button
            type="button"
            class="edit-modal__close"
            aria-label="关闭"
            :disabled="savingRecord"
            @click="closeEdit"
          >
            <Close />
          </button>
        </header>

        <form class="edit-form" @submit.prevent="saveRecord">
          <label class="edit-field">
            <span>签到日期</span>
            <input v-model="editForm.checkinDate" type="date" required />
          </label>
          <label class="edit-field">
            <span>签到状态</span>
            <select v-model="editForm.status" required>
              <option value="1">正常</option>
              <option value="2">迟到</option>
              <option value="3">未签到</option>
              <option value="4">早退</option>
            </select>
          </label>
          <label class="edit-field">
            <span>签到时间</span>
            <input v-model="editForm.checkInTime" type="time" step="1" />
          </label>
          <label class="edit-field">
            <span>签退时间</span>
            <input v-model="editForm.checkOutTime" type="time" step="1" />
          </label>
          <p class="edit-form__hint">签到或签退时间留空时，将清除对应时间。</p>
          <footer class="edit-modal__actions">
            <button type="button" class="edit-btn edit-btn--secondary" :disabled="savingRecord" @click="closeEdit">
              取消
            </button>
            <button type="submit" class="edit-btn edit-btn--primary" :disabled="savingRecord">
              {{ savingRecord ? '正在保存…' : '保存修改' }}
            </button>
          </footer>
        </form>
      </section>
    </div>

  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue';
import { onMounted } from 'vue';
import { Close, EditPen } from '@element-plus/icons-vue';
import { adminStore, loadCheckins } from '../../adminStore.js';
import { createAdminCheckin, fetchAdminCheckins, updateAdminCheckin } from '../../api/index.js';
import { usePager } from '../../composables/usePager.js';
import { useToast } from '../../composables/useToast.js';
import AdminPager from '../../components/AdminPager.vue';
import AdminDetailModal from '../../components/AdminDetailModal.vue';

const localDate = () => {
  const date = new Date();
  const pad = (value) => String(value).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
};
const dateFilter = ref(localDate());
const dayRecords = ref([]);
const loadingRecords = ref(false);
const recordsLoadError = ref('');
const learners = computed(() =>
  adminStore.users.filter(user => user.role === 'learner' && user.status === 'active'));

const checkinRecords = computed(() => {
  if (recordsLoadError.value || loadingRecords.value) return [];
  const recordsByUser = new Map(dayRecords.value.map(record => [String(record.userId), record]));
  const learnerIds = new Set(learners.value.map(user => String(user.numericId ?? user.id)));
  const roster = learners.value.map((user) => {
    const userId = String(user.numericId ?? user.id);
    return recordsByUser.get(userId) || {
      id: `missing-${userId}-${dateFilter.value}`,
      numericId: null,
      userId: user.numericId ?? Number(user.id),
      name: user.name || '',
      userName: user.name || '',
      employeeId: user.employeeId || '',
      employeeNo: user.employeeNo || user.employeeId || '',
      avatarUrl: user.avatarUrl || '',
      department: user.department || '',
      checkinDate: dateFilter.value,
      checkInTime: '',
      checkOutTime: '',
      status: 'missing',
      isVirtualMissing: true,
    };
  });
  const otherRecords = dayRecords.value.filter(record => !learnerIds.has(String(record.userId)));
  return [...roster, ...otherRecords];
});
const learnerCount = computed(() => learners.value.length);

const refreshDayRecords = async () => {
  loadingRecords.value = true;
  recordsLoadError.value = '';
  try {
    const page = await fetchAdminCheckins({ date: dateFilter.value });
    dayRecords.value = page?.records || [];
  } catch (error) {
    dayRecords.value = [];
    recordsLoadError.value = error?.message || '签到数据加载失败，请稍后重试。';
  } finally {
    loadingRecords.value = false;
  }
};

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
watch(dateFilter, (value) => {
  if (!value) {
    dateFilter.value = localDate();
    return;
  }
  resetPage();
  refreshDayRecords();
});
onMounted(refreshDayRecords);

const normalCount = computed(() => checkinRecords.value.filter(c => c.status === 'normal').length);
const missingCount = computed(() => checkinRecords.value.filter(c => c.status === 'missing').length);

const statusLabelMap = {
  normal: '正常',
  late: '迟到',
  early: '早退',
  missing: '未签到'
};
const statusLabel = (status) => statusLabelMap[status] || status;

/* ---------- 签到详情 ---------- */
const detailRecord = ref(null);
const openDetail = (record) => { detailRecord.value = record; };

const editingRecord = ref(null);
const savingRecord = ref(false);
const editForm = reactive({
  checkinDate: '',
  checkInTime: '',
  checkOutTime: '',
  status: '1',
});
const statusCodeMap = { normal: 1, late: 2, missing: 3, early: 4 };
const { show: showToast } = useToast();
const timeInputValue = (value) => {
  if (!value) return '';
  if (Array.isArray(value)) {
    const [hour, minute, second = 0] = value;
    return [hour, minute, second].map(part => String(part).padStart(2, '0')).join(':');
  }
  if (typeof value === 'object') {
    const { hour, minute, second = 0 } = value;
    if (hour !== undefined && minute !== undefined) {
      return [hour, minute, second].map(part => String(part).padStart(2, '0')).join(':');
    }
  }
  const match = String(value).trim().match(/(?:^|T| )(\d{1,2}):(\d{2})(?::(\d{2}))?/);
  if (!match) return '';
  return [match[1], match[2], match[3] || '00']
    .map(part => String(part).padStart(2, '0'))
    .join(':');
};

const openEdit = (record) => {
  detailRecord.value = null;
  editingRecord.value = record;
  editForm.checkinDate = record.checkinDate || '';
  editForm.checkInTime = timeInputValue(record.checkInTime);
  editForm.checkOutTime = timeInputValue(record.checkOutTime);
  editForm.status = String(statusCodeMap[record.status] || (record.isVirtualMissing ? 3 : 1));
};

const closeEdit = () => {
  if (!savingRecord.value) editingRecord.value = null;
};

const saveRecord = async () => {
  if (!editingRecord.value || savingRecord.value) return;
  savingRecord.value = true;
  try {
    const payload = {
      checkinDate: editForm.checkinDate,
      checkInTime: editForm.checkInTime || null,
      checkOutTime: editForm.checkOutTime || null,
      status: Number(editForm.status),
    };
    if (editingRecord.value.isVirtualMissing) {
      await createAdminCheckin({
        ...payload,
        userId: editingRecord.value.userId,
        method: 1,
      });
    } else {
      await updateAdminCheckin(editingRecord.value.numericId ?? editingRecord.value.id, payload);
    }
    editingRecord.value = null;
    showToast('签到记录已保存。', 'success');
    try {
      await refreshDayRecords();
      await loadCheckins();
    } catch (error) {
      showToast(error?.message || '记录已保存，但列表刷新失败，请重新进入页面查看。', 'error');
    }
  } catch (error) {
    showToast(error?.message || '签到记录更新失败，请稍后重试。', 'error');
  } finally {
    savingRecord.value = false;
  }
};
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
  grid-template-columns: minmax(160px, 190px) minmax(140px, 180px) minmax(130px, 160px) minmax(180px, 1fr) auto;
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
.filter-date { padding-right: 10px; }
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
.status-pill--early   { color: var(--app-amber-fg); background: var(--app-amber-bg); }
.status-pill--missing { color: var(--app-rose-fg); background: var(--app-rose-bg); }

.row-action-cell { white-space: nowrap; }
.edit-record-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  padding: 6px 10px;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  color: var(--app-accent-deep);
  background: var(--app-surface);
  font: inherit;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}
.edit-record-btn:hover { background: var(--app-accent-soft); border-color: var(--app-accent); }
.edit-record-btn:disabled { cursor: wait; opacity: 0.6; }
.edit-record-btn svg { width: 14px; height: 14px; }

.edit-modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgb(15 30 51 / 48%);
  backdrop-filter: blur(3px);
}
.edit-modal {
  width: min(100%, 520px);
  max-height: min(90vh, 720px);
  overflow: auto;
  border: 1px solid var(--app-border);
  border-radius: 16px;
  background: var(--app-surface);
  box-shadow: 0 24px 60px rgb(15 30 51 / 24%);
}
.edit-modal__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border-bottom: 1px solid var(--app-border-soft);
}
.edit-modal__title {
  margin: 0;
  color: var(--app-heading);
  font-size: 16px;
  font-weight: 700;
}
.edit-modal__subtitle {
  margin: 5px 0 0;
  color: var(--app-text-4);
  font-size: 12px;
}
.edit-modal__close {
  display: grid;
  width: 30px;
  height: 30px;
  place-items: center;
  border: 0;
  border-radius: 8px;
  color: var(--app-text-4);
  background: transparent;
  cursor: pointer;
}
.edit-modal__close:hover { color: var(--app-heading); background: var(--app-surface-2); }
.edit-modal__close:disabled { cursor: wait; opacity: 0.6; }
.edit-modal__close svg { width: 16px; height: 16px; }
.edit-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  padding: 20px;
}
.edit-field {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 6px;
}
.edit-field > span {
  color: var(--app-text-3);
  font-size: 12px;
  font-weight: 600;
}
.edit-field input,
.edit-field select {
  width: 100%;
  min-height: 40px;
  padding: 8px 10px;
  border: 1px solid var(--app-border);
  border-radius: 9px;
  color: var(--app-text);
  background: var(--app-surface-2);
  font: inherit;
  font-size: 13px;
}
.edit-field input:focus,
.edit-field select:focus {
  outline: 0;
  border-color: var(--app-accent-strong);
  box-shadow: 0 0 0 3px var(--app-accent-a12);
}
.edit-form__hint,
.edit-modal__actions { grid-column: 1 / -1; }
.edit-form__hint {
  margin: -4px 0 0;
  color: var(--app-text-5);
  font-size: 11px;
}
.edit-modal__actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 14px;
  border-top: 1px solid var(--app-border-soft);
}
.edit-btn {
  min-height: 38px;
  padding: 8px 16px;
  border: 1px solid transparent;
  border-radius: 9px;
  font: inherit;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}
.edit-btn:disabled { cursor: wait; opacity: 0.6; }
.edit-btn--secondary {
  border-color: var(--app-border);
  color: var(--app-text-3);
  background: var(--app-surface);
}
.edit-btn--primary { color: var(--app-on-accent-solid); background: var(--app-accent); }
.edit-btn--primary:hover { background: var(--app-btn-primary-hover); }

@media (max-width: 640px) {
  .overview-strip { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .edit-form { grid-template-columns: 1fr; }
  .edit-form__hint, .edit-modal__actions { grid-column: 1; }
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
