<template>
  <div class="admin-students">

    <!-- 页头 -->
    <div class="page-head">
      <div>
        <h1 class="page-title">学员管理</h1>
        <p class="page-sub">共 {{ learners.length }} 名学员 · {{ activeCount }} 名正常学习</p>
      </div>
      <button class="btn btn--primary" @click="openCreate">
        <Plus class="btn-icon" /> 新增学员
      </button>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="search-box">
        <span class="search-icon"></span>
        <input
          v-model.trim="keyword"
          type="text"
          class="search-input"
          placeholder="搜索姓名 / 工号 / 职级..."
        />
      </div>
      <select v-model="deptFilter" class="select">
        <option value="">全部部门</option>
        <option v-for="dept in departments" :key="dept" :value="dept">{{ dept }}</option>
      </select>
      <select v-model="statusFilter" class="select">
        <option value="">全部状态</option>
        <option value="active">正常学习</option>
        <option value="inactive">已停用</option>
      </select>
      <span class="toolbar-result">{{ filteredLearners.length }} 条结果</span>
    </div>

    <!-- 学员表格 -->
    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>学员</th>
            <th>工号</th>
            <th>部门</th>
            <th>职级</th>
            <th class="num">学时</th>
            <th class="num">完成课程</th>
            <th class="num">证书</th>
            <th>状态</th>
            <th>最近学习</th>
            <th class="ops">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="learner in filteredLearners" :key="learner.id">
            <td>
              <div class="cell-user">
                <span class="cell-avatar">
                  <img v-if="learner.avatarUrl" :src="learner.avatarUrl" alt="" class="cell-avatar-img" />
                  <template v-else>{{ learner.name.charAt(0) }}</template>
                </span>
                <span class="cell-name">{{ learner.name }}</span>
              </div>
            </td>
            <td class="mono">{{ learner.employeeId }}</td>
            <td>{{ learner.department }}</td>
            <td class="cell-muted">{{ learner.title }}</td>
            <td class="num">{{ learner.learningHours }}h</td>
            <td class="num">{{ learner.completedCourses }}</td>
            <td class="num">{{ learner.certificateCount }}</td>
            <td>
              <span class="status-pill" :class="learner.status === 'active' ? 'status-pill--on' : 'status-pill--off'">
                {{ learner.status === 'active' ? '正常学习' : '已停用' }}
              </span>
            </td>
            <td class="cell-muted">{{ learner.lastActive }}</td>
            <td class="ops">
              <div class="row-actions">
                <button class="icon-btn" title="编辑" @click="openEdit(learner)">
                  <EditPen />
                </button>
                <button
                  class="icon-btn"
                  :class="learner.status === 'active' ? 'icon-btn--warn' : 'icon-btn--ok'"
                  :title="learner.status === 'active' ? '停用账号' : '启用账号'"
                  @click="toggleStatus(learner)"
                >
                  <CircleClose v-if="learner.status === 'active'" />
                  <CircleCheck v-else />
                </button>
                <button class="icon-btn icon-btn--danger" title="删除" @click="removeLearner(learner)">
                  <Delete />
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="!filteredLearners.length">
            <td colspan="10" class="empty-cell">没有匹配的学员</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 新增 / 编辑弹窗 -->
    <div v-if="modalOpen" class="modal-backdrop" @click.self="closeModal">
      <div class="modal">
        <div class="modal-head">
          <h2 class="modal-title">{{ editingId ? '编辑学员' : '新增学员' }}</h2>
          <button class="modal-close" @click="closeModal"><Close /></button>
        </div>
        <form class="modal-body" @submit.prevent="saveLearner">
          <label class="field">
            <span class="field-label">姓名</span>
            <input v-model.trim="form.name" type="text" class="field-input" placeholder="请输入姓名" />
          </label>
          <label class="field">
            <span class="field-label">企业工号</span>
            <input v-model.trim="form.employeeId" type="text" class="field-input mono" placeholder="如 EMP-2025001" />
          </label>
          <div class="field-row">
            <label class="field">
              <span class="field-label">部门</span>
              <select v-model="form.department" class="field-input">
                <option v-for="dept in departments" :key="dept" :value="dept">{{ dept }}</option>
              </select>
            </label>
            <label class="field">
              <span class="field-label">职级</span>
              <input v-model.trim="form.title" type="text" class="field-input" placeholder="如 高级工程师" />
            </label>
          </div>
          <label class="field">
            <span class="field-label">账号状态</span>
            <select v-model="form.status" class="field-input">
              <option value="active">正常学习</option>
              <option value="inactive">停用</option>
            </select>
          </label>
          <label class="field">
            <span class="field-label">头像链接</span>
            <div class="avatar-input-row">
              <input v-model.trim="form.avatarUrl" type="text" class="field-input" placeholder="粘贴图片 URL,留空则用姓名首字母头像" />
              <span class="avatar-preview">
                <img v-if="form.avatarUrl" :src="form.avatarUrl" alt="" class="avatar-preview-img" @error="onAvatarError" />
                <span v-else class="avatar-preview-fallback">{{ form.name ? form.name.charAt(0) : '?' }}</span>
              </span>
            </div>
          </label>
          <label class="field">
            <span class="field-label">登录密码</span>
            <input
              v-model="form.password"
              type="text"
              class="field-input"
              :placeholder="editingId ? '留空则不修改密码' : '默认 123456,可自定义'"
            />
          </label>
          <p v-if="formError" class="form-error">{{ formError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn btn--ghost" @click="closeModal">取消</button>
            <button type="submit" class="btn btn--primary">{{ editingId ? '保存修改' : '创建学员' }}</button>
          </div>
        </form>
      </div>
    </div>

  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import { Plus, EditPen, Delete, Close, CircleClose, CircleCheck, Search } from '@element-plus/icons-vue';
import { ADMIN_LEARNERS_DATA } from '../../mockData.js';

const learners = ref(ADMIN_LEARNERS_DATA.map(l => ({ ...l })));

const keyword = ref('');
const deptFilter = ref('');
const statusFilter = ref('');

const departments = computed(() => [...new Set(learners.value.map(l => l.department))]);

const activeCount = computed(() => learners.value.filter(l => l.status === 'active').length);

const filteredLearners = computed(() => {
  const kw = keyword.value.trim().toLowerCase();
  return learners.value.filter(l => {
    const hitKw = !kw ||
      [l.name, l.employeeId, l.title, l.department].some(v => v.toLowerCase().includes(kw));
    const hitDept = !deptFilter.value || l.department === deptFilter.value;
    const hitStatus = !statusFilter.value || l.status === statusFilter.value;
    return hitKw && hitDept && hitStatus;
  });
});

/* ---------- 弹窗表单 ---------- */
const modalOpen = ref(false);
const editingId = ref(null);
const formError = ref('');
const form = ref({ name: '', employeeId: '', department: '研发部', title: '', status: 'active', avatarUrl: '', password: '' });

const openCreate = () => {
  editingId.value = null;
  formError.value = '';
  form.value = { name: '', employeeId: '', department: departments.value[0] || '研发部', title: '', status: 'active', avatarUrl: '', password: '123456' };
  modalOpen.value = true;
};

const openEdit = (learner) => {
  editingId.value = learner.id;
  formError.value = '';
  form.value = {
    name: learner.name,
    employeeId: learner.employeeId,
    department: learner.department,
    title: learner.title,
    status: learner.status,
    avatarUrl: learner.avatarUrl || '',
    password: ''
  };
  modalOpen.value = true;
};

const onAvatarError = (e) => {
  e.target.style.display = 'none';
};

const closeModal = () => {
  modalOpen.value = false;
  formError.value = '';
};

const saveLearner = () => {
  if (!form.value.name) { formError.value = '请输入学员姓名。'; return; }
  if (!form.value.employeeId) { formError.value = '请输入企业工号。'; return; }
  const dup = learners.value.some(
    l => l.employeeId === form.value.employeeId && l.id !== editingId.value
  );
  if (dup) { formError.value = '该工号已存在,请勿重复创建。'; return; }

  if (editingId.value) {
    const target = learners.value.find(l => l.id === editingId.value);
    const { password, ...rest } = form.value;
    Object.assign(target, { ...rest });
    if (password) target.password = password;
  } else {
    learners.value.unshift({
      id: `u-${Date.now()}`,
      ...form.value,
      password: form.value.password || '123456',
      learningHours: 0,
      completedCourses: 0,
      certificateCount: 0,
      lastActive: '—'
    });
  }
  closeModal();
};

const toggleStatus = (learner) => {
  learner.status = learner.status === 'active' ? 'inactive' : 'active';
};

const removeLearner = (learner) => {
  if (!window.confirm(`确认删除学员「${learner.name} (${learner.employeeId})」?此操作不可恢复。`)) return;
  learners.value = learners.value.filter(l => l.id !== learner.id);
};
</script>

<style scoped>
/* ============================================================
   AdminStudents · 学员管理 · 纯原生 CSS
   ============================================================ */

* { margin: 0; padding: 0; box-sizing: border-box; }
button, input, select { font-family: inherit; }
button { cursor: pointer; }

.admin-students {
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

/* ========== 工具栏 ========== */
.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  flex-wrap: wrap;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 200px;
  padding: 0 12px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.search-box:focus-within {
  border-color: #2E8BD6;
  box-shadow: 0 0 0 3px rgba(46, 139, 214, 0.12);
}

.search-icon {
  position: relative;
  width: 14px;
  height: 14px;
  flex-shrink: 0;
}
.search-icon::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 9px;
  height: 9px;
  border: 2px solid #64748B;
  border-radius: 50%;
}
.search-icon::after {
  content: '';
  position: absolute;
  bottom: 0;
  right: 0;
  width: 6px;
  height: 2px;
  background: #64748B;
  border-radius: 1px;
  transform: rotate(45deg);
  transform-origin: right center;
}

.search-input {
  flex: 1;
  min-width: 0;
  padding: 9px 0;
  font-size: 13px;
  color: #1E293B;
  background: transparent;
  border: none;
  outline: none;
}

.select {
  padding: 9px 12px;
  font-size: 13px;
  color: #334155;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  outline: none;
  transition: border-color 0.15s;
}
.select:focus { border-color: #2E8BD6; }

.toolbar-result {
  margin-left: auto;
  font-size: 12px;
  color: #94A3B8;
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
  white-space: nowrap;
}

.data-table tbody tr:last-child td { border-bottom: none; }
.data-table tbody tr:hover { background: #F8FAFC; }

th.num, td.num { text-align: right; }
th.ops, td.ops { text-align: right; }

.mono { font-family: 'JetBrains Mono', ui-monospace, monospace; font-size: 12px; }
.cell-muted { color: #64748B; }
.empty-cell { text-align: center; color: #94A3B8; padding: 28px 0 !important; }

.cell-user { display: flex; align-items: center; gap: 8px; }

.cell-avatar {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: #005576;
  background: #D9F0FF;
  border-radius: 50%;
  flex-shrink: 0;
  overflow: hidden;
}

.cell-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cell-name { font-weight: 600; }

/* ========== 状态徽标 ========== */
.status-pill {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  font-size: 11px;
  font-weight: 700;
  border-radius: 9999px;
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
  max-width: 460px;
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

.form-error {
  font-size: 12px;
  color: #BE123C;
}

/* 头像链接行 */
.avatar-input-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar-preview {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #D9F0FF;
  border: 1px solid #A3D5FF;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.avatar-preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-preview-fallback {
  font-size: 16px;
  font-weight: 700;
  color: #005576;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 4px;
}

@media (max-width: 640px) {
  .field-row { grid-template-columns: 1fr; }
}
</style>
