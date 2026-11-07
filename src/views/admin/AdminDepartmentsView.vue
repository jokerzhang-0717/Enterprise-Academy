<template>
  <div class="admin-depts admin-fit-page">

    <!-- 页头 -->
    <div class="page-head">
      <div>
        <h1 class="page-title">部门管理</h1>
        <p class="page-sub">共 {{ departments.length }} 个部门 · {{ activeCount }} 个启用中</p>
      </div>
      <button class="btn btn--primary" @click="openCreate">
        <Plus class="btn-icon" /> 新增部门
      </button>
    </div>

    <!-- 统计条 -->
    <div class="overview-strip">
      <div class="overview-item">
        <span class="overview-value">{{ departments.length }}</span>
        <span class="overview-label">部门总数</span>
      </div>
      <div class="overview-item">
        <span class="overview-value">{{ totalLearners }}</span>
        <span class="overview-label">部门总人数</span>
      </div>
      <div class="overview-item">
        <span class="overview-value">{{ avgLearners }}</span>
        <span class="overview-label">平均人数/部门</span>
      </div>
    </div>

    <div class="filter-toolbar">
      <select v-model="statusFilter" class="filter-select" aria-label="按部门状态筛选">
        <option value="">全部状态</option>
        <option value="active">启用</option>
        <option value="inactive">已停用</option>
      </select>
      <label class="filter-search">
        <span class="search-icon" aria-hidden="true"></span>
        <input
          v-model.trim="keyword"
          class="filter-search-input"
          type="text"
          placeholder="搜索部门名称 / 负责人 / 简介..."
          aria-label="搜索部门名称、负责人或简介"
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
      <span class="filter-count">{{ filteredDepartments.length }} 个部门</span>
    </div>

    <!-- 部门表格 -->
    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>部门</th>
            <th>负责人</th>
            <th class="num">人数</th>
            <th>简介</th>
            <th>状态</th>
            <th class="ops">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="dept in pagedDepartments"
            :key="dept.id"
            class="clickable-row"
            title="点击查看部门详细数据"
            @click="openDetail(dept)"
          >
            <td>
              <div class="cell-user">
                <span class="cell-avatar">{{ dept.name.charAt(0) }}</span>
                <span class="cell-name">{{ dept.name }}</span>
              </div>
            </td>
            <td class="cell-muted">{{ dept.manager }}</td>
            <td class="num">{{ dept.learners }}</td>
            <td class="desc-cell" :title="dept.desc">{{ dept.desc }}</td>
            <td>
              <span class="status-pill" :class="dept.status === 'active' ? 'status-pill--on' : 'status-pill--off'">
                {{ dept.status === 'active' ? '启用' : '已停用' }}
              </span>
            </td>
            <td class="ops">
              <div class="row-actions">
                <button
                  class="status-toggle-btn"
                  :class="dept.status === 'active' ? 'status-toggle-btn--off' : 'status-toggle-btn--on'"
                  :title="dept.status === 'active' ? '停用此部门' : '启用此部门'"
                  @click.stop="toggleStatus(dept)"
                >
                  <span class="toggle-dot"></span>
                  <span>{{ dept.status === 'active' ? '停用' : '启用' }}</span>
                </button>
                <button class="icon-btn" title="编辑" @click.stop="openEdit(dept)">
                  <EditPen />
                </button>
                <button class="icon-btn icon-btn--danger" title="删除" @click.stop="removeDept(dept)">
                  <Delete />
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="!filteredDepartments.length">
            <td colspan="6" class="empty-cell">
              {{ departments.length ? '没有匹配的部门' : '暂无部门数据' }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <AdminPager
      v-model:page="page"
      :total="filteredDepartments.length"
      :total-pages="totalPages"
    />

    <!-- 部门详细数据弹窗 -->
    <div v-if="detailModalOpen" class="modal-backdrop" @click.self="detailModalOpen = false">
      <div class="modal modal--detail">
        <div class="modal-head">
          <h2 class="modal-title">部门详细资料</h2>
          <button type="button" class="modal-close" @click="detailModalOpen = false"><Close /></button>
        </div>
        <div v-if="selectedDept" class="modal-body detail-body">
          <div class="detail-header-card">
            <div class="detail-avatar">{{ selectedDept.name.charAt(0) }}</div>
            <div class="detail-main">
              <div class="detail-name-row">
                <h3 class="detail-name">{{ selectedDept.name }}</h3>
                <span class="status-pill" :class="selectedDept.status === 'active' ? 'status-pill--on' : 'status-pill--off'">
                  {{ selectedDept.status === 'active' ? '正常启用' : '已停用' }}
                </span>
              </div>
              <p class="detail-meta">部门编号：{{ selectedDept.id }} · 负责人：{{ selectedDept.manager }}</p>
            </div>
          </div>

          <div class="detail-grid">
            <div class="detail-stat-card">
              <span class="detail-stat-label">在编人数</span>
              <span class="detail-stat-val">{{ selectedDept.learners }} 人</span>
            </div>
            <div class="detail-stat-card">
              <span class="detail-stat-label">部门负责人</span>
              <span class="detail-stat-val">{{ selectedDept.manager }}</span>
            </div>
            <div class="detail-stat-card">
              <span class="detail-stat-label">运行状态</span>
              <span class="detail-stat-val" :class="selectedDept.status === 'active' ? 'text-green' : 'text-slate'">
                {{ selectedDept.status === 'active' ? '正常运营中' : '已暂停运营' }}
              </span>
            </div>
          </div>

          <div class="detail-section">
            <div class="detail-section-label">部门简介与职责范围</div>
            <div class="detail-desc-box">{{ selectedDept.desc || '该部门暂无详细职责简介说明。' }}</div>
          </div>

          <div class="modal-actions">
            <button type="button" class="btn btn--ghost" @click="detailModalOpen = false">关闭</button>
            <button type="button" class="btn btn--primary" @click="openEditFromDetail">编辑此部门</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增 / 编辑弹窗 -->
    <div v-if="modalOpen" class="modal-backdrop" @click.self="closeModal">
      <div class="modal">
        <div class="modal-head">
          <h2 class="modal-title">{{ editingId ? '编辑部门' : '新增部门' }}</h2>
          <button type="button" class="modal-close" @click="closeModal"><Close /></button>
        </div>
        <form class="modal-body" @submit.prevent="saveDept">
          <label class="field">
            <span class="field-label">部门名称</span>
            <input v-model.trim="form.name" type="text" class="field-input" placeholder="如 数据智能部" />
          </label>
          <div class="field-row">
            <label class="field">
              <span class="field-label">负责人</span>
              <input v-model.trim="form.manager" type="text" class="field-input" placeholder="负责人姓名" />
            </label>
            <label class="field">
              <span class="field-label">状态</span>
              <select v-model="form.status" class="field-input">
                <option value="active">启用</option>
                <option value="inactive">停用</option>
              </select>
            </label>
          </div>
          <label class="field">
            <span class="field-label">部门简介</span>
            <textarea v-model.trim="form.desc" rows="3" class="field-input field-textarea" placeholder="一句话描述部门职责"></textarea>
          </label>
          <p v-if="formError" class="form-error">{{ formError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn btn--ghost" @click="closeModal">取消</button>
            <button type="submit" class="btn btn--primary" :disabled="saving">{{ saving ? '保存中…' : (editingId ? '保存修改' : '创建部门') }}</button>
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
import { computed, ref, watch } from 'vue';
import { Plus, EditPen, Delete, Close, CircleClose, CircleCheck } from '@element-plus/icons-vue';
import { adminStore, loadDepartments } from '../../adminStore.js';
import {
  createAdminDepartment,
  deleteAdminDepartment,
  updateAdminDepartment,
  updateAdminDepartmentStatus,
} from '../../api/index.js';
import { usePager } from '../../composables/usePager.js';
import { useConfirm } from '../../composables/useConfirm.js';
import { useToast } from '../../composables/useToast.js';
import AdminPager from '../../components/AdminPager.vue';
import ConfirmDialog from '../../components/ConfirmDialog.vue';

const departments = adminStore.departments;

const { show: showToast } = useToast();

const { confirmState, askConfirm, cancelConfirm, acceptConfirm } = useConfirm();

const keyword = ref('');
const statusFilter = ref('');

const filteredDepartments = computed(() => {
  const search = keyword.value.toLowerCase();
  return departments.filter((dept) => {
    const matchesStatus = !statusFilter.value || dept.status === statusFilter.value;
    const matchesKeyword = !search ||
      `${dept.name} ${dept.manager} ${dept.desc} ${dept.id}`.toLowerCase().includes(search);
    return matchesStatus && matchesKeyword;
  });
});

const { page, totalPages, paged: pagedDepartments, resetPage } = usePager(filteredDepartments, 8);
watch([keyword, statusFilter], resetPage);

const activeCount = computed(() => departments.filter(d => d.status === 'active').length);
const totalLearners = computed(() => departments.reduce((s, d) => s + d.learners, 0));
const avgLearners = computed(() =>
  departments.length
    ? Math.round(totalLearners.value / departments.length)
    : 0
);

/* ---------- 详情弹窗 ---------- */
const detailModalOpen = ref(false);
const selectedDept = ref(null);

const openDetail = (dept) => {
  selectedDept.value = dept;
  detailModalOpen.value = true;
};

const openEditFromDetail = () => {
  const target = selectedDept.value;
  detailModalOpen.value = false;
  if (target) openEdit(target);
};

/* ---------- 弹窗表单 ---------- */
const modalOpen = ref(false);
const editingId = ref(null);
const formError = ref('');
const saving = ref(false);
const form = ref({ name: '', manager: '', desc: '', status: 'active' });

const openCreate = () => {
  editingId.value = null;
  formError.value = '';
  form.value = { name: '', manager: '', desc: '', status: 'active' };
  modalOpen.value = true;
};

const openEdit = (dept) => {
  editingId.value = dept.id;
  formError.value = '';
  form.value = {
    name: dept.name,
    manager: dept.manager,
    desc: dept.desc,
    status: dept.status
  };
  modalOpen.value = true;
};

const closeModal = () => {
  modalOpen.value = false;
  formError.value = '';
};

const departmentStatusToCode = (status) => (status === 'inactive' ? 2 : 1);

const saveDept = async () => {
  if (!form.value.name) { formError.value = '请输入部门名称。'; return; }
  if (!form.value.manager) { formError.value = '请输入负责人姓名。'; return; }
  const dup = departments.some(
    d => d.name === form.value.name && d.id !== editingId.value
  );
  if (dup) { formError.value = '该部门已存在,请勿重复创建。'; return; }

  const payload = {
    name: form.value.name.trim(),
    managerName: form.value.manager.trim(),
    description: form.value.desc.trim(),
    status: departmentStatusToCode(form.value.status),
  };

  saving.value = true;
  try {
    if (editingId.value) {
      await updateAdminDepartment(editingId.value, payload);
      showToast(`已更新部门「${payload.name}」`, 'success');
    } else {
      await createAdminDepartment(payload);
      showToast(`已创建部门「${payload.name}」`, 'success');
    }
    await loadDepartments();
    closeModal();
  } catch (error) {
    formError.value = error?.message || '保存失败，请稍后重试。';
  } finally {
    saving.value = false;
  }
};

const toggleStatus = async (dept) => {
  const next = dept.status === 'active' ? 'inactive' : 'active';
  try {
    await updateAdminDepartmentStatus(dept.numericId ?? dept.id, departmentStatusToCode(next));
    await loadDepartments();
    showToast(`「${dept.name}」已${next === 'active' ? '启用' : '停用'}`, 'success');
  } catch (error) {
    showToast(error?.message || '状态更新失败', 'error');
  }
};

const removeDept = (dept) => {
  askConfirm({
    tone: 'danger',
    title: '删除该部门？',
    subtitle: `${dept.name} · 负责人 ${dept.manager}`,
    rows: [
      { label: '部门人数', value: `${dept.learners} 人` }
    ],
    tip: dept.learners > 0
      ? `该部门下仍有 ${dept.learners} 名成员，需先转移成员后才能删除。`
      : '删除后不可恢复。',
    confirmText: '确认删除',
    action: async () => {
      try {
        await deleteAdminDepartment(dept.numericId ?? dept.id);
        await loadDepartments();
        showToast(`已删除部门「${dept.name}」`, 'success');
      } catch (error) {
        showToast(error?.message || '删除失败', 'error');
      }
    }
  });
};
</script>

<style scoped>
/* ============================================================
   AdminDepartments · 部门管理 · 纯原生 CSS
   ============================================================ */

* { margin: 0; padding: 0; box-sizing: border-box; }
button, input, select, textarea { font-family: inherit; }
button { cursor: pointer; }

.admin-depts {
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

.overview-label {
  font-size: 12px;
  color: var(--app-text-4);
}

/* ========== 筛选工具栏 ========== */
.filter-toolbar {
  display: grid;
  grid-template-columns: minmax(140px, 190px) minmax(220px, 1fr) auto;
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
.filter-search {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 12px;
}
.search-icon {
  position: relative;
  width: 14px;
  height: 14px;
  flex: 0 0 14px;
}
.search-icon::before {
  position: absolute;
  top: 0;
  left: 0;
  width: 9px;
  height: 9px;
  border: 2px solid var(--app-text-4);
  border-radius: 50%;
  content: '';
}
.search-icon::after {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 6px;
  height: 2px;
  background: var(--app-text-4);
  border-radius: 1px;
  content: '';
  transform: rotate(45deg);
  transform-origin: right center;
}
.filter-search-input {
  flex: 1;
  min-width: 0;
  height: 100%;
  font-size: 13px;
  color: var(--app-text);
  background: transparent;
  border: 0;
  outline: 0;
}
.filter-search-input::placeholder { color: var(--app-text-5); }
.filter-clear {
  display: inline-flex;
  flex: 0 0 24px;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  padding: 0;
  color: var(--app-text-4);
  background: transparent;
  border: 0;
  border-radius: 50%;
}
.filter-clear:hover { color: var(--app-text-2); background: var(--app-chip); }
.filter-clear svg { width: 14px; height: 14px; }
.filter-count {
  justify-self: end;
  padding: 7px 11px;
  font-size: 12px;
  font-weight: 600;
  color: var(--app-accent-deep);
  background: var(--app-accent-soft);
  border-radius: 999px;
  white-space: nowrap;
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
  transition: background 0.15s, color 0.15s, box-shadow 0.15s;
}

.data-table tbody tr:last-child td { border-bottom: none; }
.data-table tbody tr:hover td { background: var(--app-accent-soft); }
.data-table tbody tr:hover td:first-child { box-shadow: inset 3px 0 0 var(--app-accent); }
.data-table tbody tr:hover td,
.data-table tbody tr:hover .cell-name,
.data-table tbody tr:hover .cell-muted,
.data-table tbody tr:hover .desc-cell {
  color: var(--app-accent-deep);
}

.clickable-row {
  cursor: pointer;
}

th.num, td.num { text-align: right; white-space: nowrap; }
th.ops, td.ops { text-align: right; white-space: nowrap; }

/* 表头默认 left 优先级更高,这里提权让数字列/操作列的表头与其内容对齐 */
.data-table thead th.num,
.data-table thead th.ops { text-align: right; }

.cell-muted { color: var(--app-text-4); }
.empty-cell { text-align: center; color: var(--app-text-5); padding: 28px 0 !important; }

/* 部门简介仅显示一行，超出省略号 */
.desc-cell {
  max-width: 240px;
  color: var(--app-text-3);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.6;
}

.cell-user {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 110px;
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
  border-radius: 8px;
  flex-shrink: 0;
}

.cell-name {
  font-weight: 600;
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
.status-pill--on  { color: var(--app-emerald-fg); background: var(--app-emerald-bg); }
.status-pill--off { color: var(--app-text-4); background: var(--app-chip); }

/* ========== 现代化状态切换按钮（替代原先难看的圆圈按钮） ========== */
.status-toggle-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 9px;
  font-size: 11px;
  font-weight: 600;
  border-radius: 6px;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.15s ease;
}
.toggle-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}
/* 启用中 -> 按钮显示停用操作 */
.status-toggle-btn--off {
  background: var(--app-orange-bg);
  color: var(--app-orange-fg);
  border-color: var(--app-orange-line);
}
.status-toggle-btn--off .toggle-dot {
  background: #EA580C;
}
.status-toggle-btn--off:hover {
  background: var(--app-rose-bg);
  color: #DC2626;
  border-color: var(--app-rose-line);
}
.status-toggle-btn--off:hover .toggle-dot {
  background: #DC2626;
}
/* 已停用 -> 按钮显示启用操作 */
.status-toggle-btn--on {
  background: var(--app-emerald-bg);
  color: var(--app-emerald-fg);
  border-color: var(--app-emerald-line);
}
.status-toggle-btn--on .toggle-dot {
  background: #16A34A;
}
.status-toggle-btn--on:hover {
  background: var(--app-emerald-bg);
  color: var(--app-emerald-fg);
  border-color: var(--app-emerald-line);
}

/* ========== 行操作 ========== */
.row-actions {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

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
.icon-btn:hover { background: var(--app-accent-soft); color: var(--app-accent-deep); border-color: var(--app-accent-border); }
.icon-btn--warn:hover { background: var(--app-orange-bg); color: var(--app-orange-fg); border-color: var(--app-orange-line); }
.icon-btn--ok:hover   { background: var(--app-emerald-bg); color: var(--app-emerald-fg); border-color: var(--app-emerald-line); }
.icon-btn--danger:hover { background: var(--app-rose-bg); color: var(--app-rose-fg); border-color: var(--app-rose-line); }

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
  max-width: 480px;
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

.field-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
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
  min-height: 64px;
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

/* ========== 部门详情弹窗定制样式 ========== */
.modal--detail {
  max-width: 520px;
}

.detail-body {
  gap: 16px;
}

.detail-header-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  background: var(--app-surface-2);
  border: 1px solid var(--app-border);
  border-radius: 12px;
}

.detail-avatar {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 800;
  color: var(--app-accent-deep);
  background: var(--app-accent-soft);
  border-radius: 10px;
  flex-shrink: 0;
}

.detail-main {
  flex: 1;
  min-width: 0;
}

.detail-name-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.detail-name {
  font-size: 17px;
  font-weight: 700;
  color: var(--app-heading);
}

.detail-meta {
  margin-top: 3px;
  font-size: 12px;
  color: var(--app-text-4);
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.detail-stat-card {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 10px 12px;
  background: var(--app-surface-2);
  border: 1px solid var(--app-border);
  border-radius: 10px;
  text-align: center;
}

.detail-stat-label {
  font-size: 11px;
  color: var(--app-text-4);
}

.detail-stat-val {
  font-size: 14px;
  font-weight: 700;
  color: var(--app-heading);
}

.text-green { color: var(--app-emerald-fg) !important; }
.text-slate { color: var(--app-text-4) !important; }

.detail-section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-section-label {
  font-size: 12px;
  font-weight: 700;
  color: var(--app-text-2);
}

.detail-desc-box {
  padding: 12px 14px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--app-text-2);
  background: var(--app-surface-2);
  border: 1px solid var(--app-border);
  border-radius: 10px;
  white-space: pre-wrap;
  word-break: break-word;
}

@media (max-width: 640px) {
  .overview-strip { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .field-row { grid-template-columns: 1fr; }
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
