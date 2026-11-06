<template>
  <div class="admin-students admin-fit-page">

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
        <button
          v-if="keyword"
          type="button"
          class="search-clear"
          aria-label="清除搜索内容"
          title="清除搜索内容"
          @click="keyword = ''"
        >
          <Close />
        </button>
      </div>
      <select v-model="deptFilter" class="select">
        <option value="">全部部门</option>
        <option v-for="dept in allDeptNames" :key="dept" :value="dept">{{ dept }}</option>
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
            <th>头像</th>
            <th>学员</th>
            <th>工号</th>
            <th>部门</th>
            <th>职级</th>
            <th class="center">学时</th>
            <th class="center">证书</th>
            <th class="center">状态</th>
            <th class="ops">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="learner in pagedLearners" :key="learner.id" @click="openEdit(learner)">
            <td>
              <span class="cell-avatar">
                <img v-if="learner.avatarUrl" :src="learner.avatarUrl" alt="" class="cell-avatar-img" />
                <template v-else>{{ learner.name.charAt(0) }}</template>
              </span>
            </td>
            <td>
              <span class="cell-name">{{ learner.name }}</span>
            </td>
            <td class="mono">{{ learner.employeeId }}</td>
            <td>{{ learner.department }}</td>
            <td class="cell-muted">{{ learner.title }}</td>
            <td class="center">{{ learner.learningHours }}h</td>
            <td class="center">{{ learner.certificateCount }}</td>
            <td class="center">
              <span class="status-pill" :class="learner.status === 'active' ? 'status-pill--on' : 'status-pill--off'">
                {{ learner.status === 'active' ? '正常学习' : '已停用' }}
              </span>
            </td>
            <td class="ops">
              <div class="row-actions" @click.stop>
                <button
                  class="status-toggle-btn"
                  :class="learner.status === 'active' ? 'status-toggle-btn--off' : 'status-toggle-btn--on'"
                  :title="learner.status === 'active' ? '停用此学员账号' : '启用此学员账号'"
                  @click="toggleStatus(learner)"
                >
                  <span class="toggle-dot"></span>
                  <span>{{ learner.status === 'active' ? '停用' : '启用' }}</span>
                </button>
                <button class="icon-btn icon-btn--key" title="重置密码为 123456" @click="resetPassword(learner)">
                  <Key />
                </button>
                <button class="icon-btn" title="编辑" @click="openEdit(learner)">
                  <EditPen />
                </button>
                <button class="icon-btn icon-btn--danger" title="删除" @click="removeLearner(learner)">
                  <Delete />
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="!filteredLearners.length">
            <td colspan="9" class="empty-cell">没有匹配的学员</td>
          </tr>
        </tbody>
      </table>
    </div>

    <AdminPager
      v-model:page="page"
      :total="filteredLearners.length"
      :total-pages="totalPages"
    />

    <!-- 新增 / 编辑弹窗 -->
    <div v-if="modalOpen" class="modal-backdrop" @click.self="closeModal">
      <div class="modal">
        <div class="modal-head">
          <h2 class="modal-title">{{ editingId ? '编辑学员' : '新增学员' }}</h2>
          <button type="button" class="modal-close" @click="closeModal"><Close /></button>
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
                <option v-if="!assignableDepts.length" value="">暂无可分配部门</option>
                <option v-for="dept in assignableDepts" :key="dept" :value="dept">{{ dept }}</option>
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
          <div class="field">
            <span class="field-label">学员头像</span>
            <input
              ref="fileInputRef"
              type="file"
              accept="image/*"
              style="display: none"
              @change="handleAvatarFile"
            />
            <div class="avatar-upload-box" @click="triggerFileInput">
              <div class="avatar-upload-preview">
                <img v-if="form.avatarUrl" :src="form.avatarUrl" alt="头像预览" class="avatar-upload-img" />
                <span v-else class="avatar-upload-letter">{{ form.name ? form.name.charAt(0) : '?' }}</span>
              </div>
              <div class="avatar-upload-info">
                <div class="avatar-upload-btn">
                  <Plus class="upload-btn-icon" />
                  <span>{{ uploadingAvatar ? '上传中…' : (form.avatarUrl ? '点击更换头像' : '点击上传头像') }}</span>
                </div>
                <p class="avatar-upload-hint">支持 JPG、PNG、WebP 图片，点击即可选择本地文件</p>
              </div>
              <button
                v-if="form.avatarUrl"
                type="button"
                class="avatar-clear-btn"
                title="清除自定义头像"
                @click.stop="form.avatarUrl = ''"
              >
                <Close />
              </button>
            </div>
          </div>
          <p v-if="formError" class="form-error">{{ formError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn btn--ghost" @click="closeModal">取消</button>
            <button type="submit" class="btn btn--primary" :disabled="saving">{{ saving ? '保存中…' : (editingId ? '保存修改' : '创建学员') }}</button>
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
import { Plus, EditPen, Delete, Close, CircleClose, CircleCheck, Key } from '@element-plus/icons-vue';
import { adminStore, loadUsers } from '../../adminStore.js';
import {
  createAdminUser,
  deleteAdminUser,
  resetAdminUserPassword,
  updateAdminUser,
  updateAdminUserStatus,
  uploadFile,
} from '../../api/index.js';
import { useConfirm } from '../../composables/useConfirm.js';
import { usePager } from '../../composables/usePager.js';
import { useToast } from '../../composables/useToast.js';
import AdminPager from '../../components/AdminPager.vue';
import ConfirmDialog from '../../components/ConfirmDialog.vue';

const { show: showToast } = useToast();

/* 学员列表取自共享 store（视图只读，写操作一律走接口后重新加载） */
const learners = computed(() => adminStore.users);

const { confirmState, askConfirm, cancelConfirm, acceptConfirm } = useConfirm();

const keyword = ref('');
const deptFilter = ref('');
const statusFilter = ref('');

/* 部门数据统一取自「部门管理」的共享 store */
const allDeptNames = computed(() => adminStore.departments.map(d => d.name));

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

/* ---------- 分页 ---------- */
const { page, totalPages, paged: pagedLearners, resetPage } = usePager(filteredLearners);

/* 筛选条件变化时回到第一页 */
watch([keyword, deptFilter, statusFilter], resetPage);

/* ---------- 弹窗表单 ---------- */
const modalOpen = ref(false);
const editingId = ref(null);
const formError = ref('');
const saving = ref(false);
const uploadingAvatar = ref(false);
const fileInputRef = ref(null);
const form = ref({ name: '', employeeId: '', department: '', title: '', status: 'active', avatarUrl: '' });

const accountStatusToCode = (status) => (status === 'inactive' ? 2 : 1);

/* 表单按部门名称选择，接口需要 departmentId */
const departmentIdOf = (name) =>
  adminStore.departments.find(d => d.name === name)?.numericId ?? null;

/* 只允许分配到启用中的部门；编辑时保留学员原部门，避免下拉回落把部门静默改掉 */
const assignableDepts = computed(() => {
  const active = adminStore.departments
    .filter(d => d.status === 'active')
    .map(d => d.name);
  const current = form.value.department;
  return current && !active.includes(current) ? [...active, current] : active;
});

const triggerFileInput = () => {
  fileInputRef.value?.click();
};

const handleAvatarFile = async (e) => {
  const file = e.target.files?.[0];
  e.target.value = '';
  if (!file) return;
  if (!file.type.startsWith('image/')) {
    showToast('请上传有效的图片文件', 'error');
    return;
  }
  uploadingAvatar.value = true;
  try {
    const result = await uploadFile(file, 'avatar');
    form.value.avatarUrl = result?.url || result?.directUrl || '';
    showToast('头像已上传，保存后生效', 'success');
  } catch (error) {
    showToast(error?.message || '头像上传失败', 'error');
  } finally {
    uploadingAvatar.value = false;
  }
};

const openCreate = () => {
  editingId.value = null;
  formError.value = '';
  form.value = { name: '', employeeId: '', department: assignableDepts.value[0] || '', title: '', status: 'active', avatarUrl: '' };
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
    avatarUrl: learner.avatarUrl || ''
  };
  modalOpen.value = true;
};

const closeModal = () => {
  modalOpen.value = false;
  formError.value = '';
};

const resetPassword = (learner) => {
  askConfirm({
    tone: 'warn',
    title: '重置学员登录密码？',
    subtitle: `${learner.name} · 工号: ${learner.employeeId}`,
    rows: [
      { label: '所属部门', value: learner.department },
      { label: '学员职级', value: learner.title },
      { label: '重置后密码', value: '123456' }
    ],
    tip: '重置后该学员的登录密码将统一恢复为 123456。',
    confirmText: '确认重置为 123456',
    action: async () => {
      try {
        await resetAdminUserPassword(learner.numericId ?? learner.id);
        showToast(`已成功将【${learner.name}】的密码重置为 123456`, 'success');
      } catch (error) {
        showToast(error?.message || '密码重置失败', 'error');
      }
    }
  });
};

const saveLearner = async () => {
  if (!form.value.name) { formError.value = '请输入学员姓名。'; return; }
  if (!form.value.employeeId) { formError.value = '请输入企业工号。'; return; }
  if (!form.value.department) {
    formError.value = assignableDepts.value.length
      ? '请选择所属部门。'
      : '暂无启用中的部门，请先在「部门管理」中创建或启用部门。';
    return;
  }
  const dup = learners.value.some(
    l => l.employeeId === form.value.employeeId && l.id !== editingId.value
  );
  if (dup) { formError.value = '该工号已存在,请勿重复创建。'; return; }

  const payload = {
    name: form.value.name,
    employeeNo: form.value.employeeId,
    departmentId: departmentIdOf(form.value.department),
    jobTitle: form.value.title,
    avatarUrl: form.value.avatarUrl || '',
    role: 1,
    accountStatus: accountStatusToCode(form.value.status)
  };

  saving.value = true;
  try {
    if (editingId.value) {
      await updateAdminUser(editingId.value, payload);
      showToast(`已更新学员【${form.value.name}】信息`, 'success');
    } else {
      await createAdminUser({ ...payload, password: '123456' });
      showToast(`成功新增学员【${form.value.name}】，初始密码为 123456`, 'success');
    }
    await loadUsers();
    closeModal();
  } catch (error) {
    formError.value = error?.message || '保存失败，请稍后重试。';
  } finally {
    saving.value = false;
  }
};

const toggleStatus = async (learner) => {
  const next = learner.status === 'active' ? 'inactive' : 'active';
  try {
    await updateAdminUserStatus(learner.numericId ?? learner.id, accountStatusToCode(next));
    await loadUsers();
    showToast(`【${learner.name}】已${next === 'active' ? '启用' : '停用'}`, 'success');
  } catch (error) {
    showToast(error?.message || '状态更新失败', 'error');
  }
};

const removeLearner = (learner) => {
  askConfirm({
    tone: 'danger',
    title: '删除该学员？',
    subtitle: `${learner.name} · ${learner.department}`,
    rows: [
      { label: '工号', value: learner.employeeId },
      { label: '职级', value: learner.title },
      { label: '累计学时', value: `${learner.learningHours}h` }
    ],
    tip: '删除后该学员的账号与学习记录将一并移除，此操作不可恢复。',
    confirmText: '确认删除',
    action: async () => {
      try {
        await deleteAdminUser(learner.numericId ?? learner.id);
        await loadUsers();
        showToast(`已删除学员【${learner.name}】`, 'success');
      } catch (error) {
        showToast(error?.message || '删除失败', 'error');
      }
    }
  });
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

/* ========== 工具栏 ========== */
.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
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
  background: var(--app-surface-2);
  border: 1px solid var(--app-border);
  border-radius: 10px;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.search-box:focus-within {
  border-color: var(--app-accent-strong);
  box-shadow: 0 0 0 3px var(--app-accent-a12);
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
  border: 2px solid var(--app-text-4);
  border-radius: 50%;
}
.search-icon::after {
  content: '';
  position: absolute;
  bottom: 0;
  right: 0;
  width: 6px;
  height: 2px;
  background: var(--app-text-4);
  border-radius: 1px;
  transform: rotate(45deg);
  transform-origin: right center;
}

.search-input {
  flex: 1;
  min-width: 0;
  padding: 9px 0;
  font-size: 13px;
  color: var(--app-text);
  background: transparent;
  border: none;
  outline: none;
}

.search-clear {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 24px;
  width: 24px;
  height: 24px;
  padding: 0;
  color: var(--app-text-4);
  background: transparent;
  border: 0;
  border-radius: 50%;
  cursor: pointer;
}
.search-clear:hover { color: var(--app-text-2); background: var(--app-chip); }
.search-clear svg { width: 14px; height: 14px; }

.select {
  padding: 9px 12px;
  font-size: 13px;
  color: var(--app-text-2);
  background: var(--app-surface-2);
  border: 1px solid var(--app-border);
  border-radius: 10px;
  outline: none;
  transition: border-color 0.15s;
}
.select:focus { border-color: var(--app-accent-strong); }

.toolbar-result {
  margin-left: auto;
  font-size: 12px;
  color: var(--app-text-5);
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
  white-space: nowrap;
}

.data-table tbody tr:last-child td { border-bottom: none; }
.data-table tbody tr { cursor: pointer; }
.data-table tbody td { transition: background 0.15s, color 0.15s, box-shadow 0.15s; }
.data-table tbody tr:hover td { background: var(--app-accent-soft); }
.data-table tbody tr:hover td:first-child { box-shadow: inset 3px 0 0 var(--app-accent); }
.data-table tbody tr:hover td,
.data-table tbody tr:hover .cell-name,
.data-table tbody tr:hover .cell-meta,
.data-table tbody tr:hover .cell-muted {
  color: var(--app-accent-deep);
}

th.num, td.num { text-align: right; }
th.ops, td.ops { text-align: right; }

/* 表头默认 left 优先级更高,这里提权让数字列/操作列的表头与其内容对齐 */
.data-table thead th.num,
.data-table thead th.ops { text-align: right; }

/* 学时/证书/状态 统一居中 */
.data-table th.center,
.data-table td.center { text-align: center; }

.mono { font-family: 'JetBrains Mono', ui-monospace, monospace; font-size: 12px; }
.cell-muted { color: var(--app-text-4); }
.empty-cell { text-align: center; color: var(--app-text-5); padding: 28px 0 !important; }

.cell-user { display: flex; align-items: center; gap: 8px; }

.cell-avatar {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: var(--app-accent-deep);
  background: var(--app-accent-soft);
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
  justify-content: center;
  min-width: 64px;
  padding: 3px 10px;
  font-size: 11px;
  font-weight: 700;
  border-radius: 9999px;
}
.status-pill--on  { color: var(--app-emerald-fg); background: var(--app-emerald-bg); }
.status-pill--off { color: var(--app-text-4); background: var(--app-chip); }

/* ========== 行操作 ========== */
.row-actions {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

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
/* 正常学习中 -> 按钮显示停用操作 */
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
.icon-btn--key:hover { background: var(--app-amber-bg); color: var(--app-amber-fg); border-color: var(--app-amber-line); }
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
  max-width: 460px;
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

.form-error {
  font-size: 12px;
  color: var(--app-rose-fg);
}

/* 本地头像点击上传区域 */
.avatar-upload-box {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 14px;
  background: var(--app-surface-2);
  border: 1.5px dashed var(--app-border-strong);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}
.avatar-upload-box:hover {
  background: var(--app-surface-3);
  border-color: var(--app-accent-strong);
}

.avatar-upload-preview {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--app-accent-soft);
  border: 2px solid var(--app-blue-line);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
}

.avatar-upload-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-upload-letter {
  font-size: 18px;
  font-weight: 700;
  color: var(--app-accent-deep);
}

.avatar-upload-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.avatar-upload-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--app-blue-fg);
}

.upload-btn-icon {
  width: 14px;
  height: 14px;
}

.avatar-upload-hint {
  font-size: 11px;
  color: var(--app-text-5);
}

.avatar-clear-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  border: 1px solid var(--app-border);
  background: var(--app-surface);
  color: var(--app-text-4);
  cursor: pointer;
  transition: all 0.15s;
}
.avatar-clear-btn:hover {
  background: var(--app-rose-bg);
  color: var(--app-rose-fg);
  border-color: var(--app-rose-line);
}
.avatar-clear-btn svg {
  width: 13px;
  height: 13px;
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
