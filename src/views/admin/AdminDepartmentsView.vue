<template>
  <div class="admin-depts">

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
          <tr v-for="dept in pagedDepartments" :key="dept.id">
            <td>
              <div class="cell-user">
                <span class="cell-avatar">{{ dept.name.charAt(0) }}</span>
                <span class="cell-name">{{ dept.name }}</span>
              </div>
            </td>
            <td class="cell-muted">{{ dept.manager }}</td>
            <td class="num">{{ dept.learners }}</td>
            <td class="desc-cell">{{ dept.desc }}</td>
            <td>
              <span class="status-pill" :class="dept.status === 'active' ? 'status-pill--on' : 'status-pill--off'">
                {{ dept.status === 'active' ? '启用' : '已停用' }}
              </span>
            </td>
            <td class="ops">
              <div class="row-actions">
                <button class="icon-btn" title="编辑" @click="openEdit(dept)">
                  <EditPen />
                </button>
                <button
                  class="icon-btn"
                  :class="dept.status === 'active' ? 'icon-btn--warn' : 'icon-btn--ok'"
                  :title="dept.status === 'active' ? '停用部门' : '启用部门'"
                  @click="toggleStatus(dept)"
                >
                  <CircleClose v-if="dept.status === 'active'" />
                  <CircleCheck v-else />
                </button>
                <button class="icon-btn icon-btn--danger" title="删除" @click="removeDept(dept)">
                  <Delete />
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="!departments.length">
            <td colspan="6" class="empty-cell">暂无部门数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <AdminPager
      v-model:page="page"
      :total="departments.length"
      :total-pages="totalPages"
    />

    <!-- 新增 / 编辑弹窗 -->
    <div v-if="modalOpen" class="modal-backdrop" @click.self="closeModal">
      <div class="modal">
        <div class="modal-head">
          <h2 class="modal-title">{{ editingId ? '编辑部门' : '新增部门' }}</h2>
          <button class="modal-close" @click="closeModal"><Close /></button>
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
            <button type="submit" class="btn btn--primary">{{ editingId ? '保存修改' : '创建部门' }}</button>
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
import { Plus, EditPen, Delete, Close, CircleClose, CircleCheck } from '@element-plus/icons-vue';
import { ADMIN_DEPARTMENTS } from '../../mockData.js';
import { usePager } from '../../composables/usePager.js';
import { useConfirm } from '../../composables/useConfirm.js';
import AdminPager from '../../components/AdminPager.vue';
import ConfirmDialog from '../../components/ConfirmDialog.vue';

const departments = ref(ADMIN_DEPARTMENTS.map(d => ({ ...d })));

const { confirmState, askConfirm, cancelConfirm, acceptConfirm } = useConfirm();

const { page, totalPages, paged: pagedDepartments } = usePager(departments, 6);

const activeCount = computed(() => departments.value.filter(d => d.status === 'active').length);
const totalLearners = computed(() => departments.value.reduce((s, d) => s + d.learners, 0));
const avgLearners = computed(() =>
  departments.value.length
    ? Math.round(totalLearners.value / departments.value.length)
    : 0
);

/* ---------- 弹窗表单 ---------- */
const modalOpen = ref(false);
const editingId = ref(null);
const formError = ref('');
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

const saveDept = () => {
  if (!form.value.name) { formError.value = '请输入部门名称。'; return; }
  if (!form.value.manager) { formError.value = '请输入负责人姓名。'; return; }
  const dup = departments.value.some(
    d => d.name === form.value.name && d.id !== editingId.value
  );
  if (dup) { formError.value = '该部门已存在,请勿重复创建。'; return; }

  if (editingId.value) {
    const target = departments.value.find(d => d.id === editingId.value);
    Object.assign(target, { ...form.value });
  } else {
    departments.value.push({
      id: `d-${Date.now()}`,
      ...form.value,
      learners: 0,
      hours: 0
    });
  }
  closeModal();
};

const toggleStatus = (dept) => {
  dept.status = dept.status === 'active' ? 'inactive' : 'active';
};

const removeDept = (dept) => {
  askConfirm({
    tone: 'danger',
    title: '删除该部门？',
    subtitle: `${dept.name} · 负责人 ${dept.manager}`,
    rows: [
      { label: '部门人数', value: `${dept.learners} 人` }
    ],
    tip: `删除后，该部门下 ${dept.learners} 名学员将变为「无部门」状态，此操作不可恢复。`,
    confirmText: '确认删除',
    action: () => {
      departments.value = departments.value.filter(d => d.id !== dept.id);
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

.desc-cell {
  max-width: 240px;
  color: #475569;
  white-space: normal;
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
  color: #005576;
  background: #D9F0FF;
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
.status-pill--on  { color: #047857; background: #D1FAE5; }
.status-pill--off { color: #64748B; background: #E2E8F0; }

/* ========== 行操作 ========== */
.row-actions {
  display: inline-flex;
  gap: 6px;
}

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
.icon-btn:hover { background: #D9F0FF; color: #005576; border-color: #A3D5FF; }
.icon-btn--warn:hover { background: #FFEDD5; color: #9A3412; border-color: #FED7AA; }
.icon-btn--ok:hover   { background: #D1FAE5; color: #047857; border-color: #A7F3D0; }
.icon-btn--danger:hover { background: #FFE4E6; color: #BE123C; border-color: #FECDD3; }

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
  min-height: 64px;
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

@media (max-width: 640px) {
  .overview-strip { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .field-row { grid-template-columns: 1fr; }
}
</style>
