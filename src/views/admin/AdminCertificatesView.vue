<template>
  <div class="admin-certificates">

    <!-- 页头 -->
    <div class="page-head">
      <div>
        <h1 class="page-title">证书管理</h1>
        <p class="page-sub">证书发放台账 · 区块链存证已启用</p>
      </div>
      <button class="btn btn--primary" @click="openIssue">
        <Plus class="btn-icon" /> 发放证书
      </button>
    </div>

    <!-- 统计条 -->
    <div class="overview-strip">
      <div class="overview-item">
        <span class="overview-value">{{ issuedCount }}</span>
        <span class="overview-label">已发放</span>
      </div>
      <div class="overview-item">
        <span class="overview-value overview-value--gold">{{ pendingCount }}</span>
        <span class="overview-label">待发放</span>
      </div>
      <div class="overview-item">
        <span class="overview-value overview-value--red">{{ revokedCount }}</span>
        <span class="overview-label">已撤销</span>
      </div>
      <div class="overview-item">
        <span class="overview-value">8</span>
        <span class="overview-label">本月新增</span>
      </div>
    </div>

    <!-- 证书表格 -->
    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>持证人</th>
            <th>证书名称</th>
            <th>证书编号</th>
            <th>颁发机构</th>
            <th>发放日期</th>
            <th>有效期</th>
            <th>状态</th>
            <th class="ops">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="cert in pagedCertificates" :key="cert.id">
            <td>
              <div class="cell-user">
                <span class="cell-avatar">{{ cert.holder.charAt(0) }}</span>
                <div>
                  <div class="cell-name">{{ cert.holder }}</div>
                  <div class="cell-meta">{{ cert.employeeId }}</div>
                </div>
              </div>
            </td>
            <td class="cert-title-cell">{{ cert.title }}</td>
            <td class="mono">{{ cert.serialNumber }}</td>
            <td class="cell-muted">{{ cert.issueOrg }}</td>
            <td class="cell-muted">{{ cert.issueDate }}</td>
            <td class="cell-muted">{{ cert.validity }}</td>
            <td>
              <span class="status-pill" :class="`status-pill--${cert.status}`">
                {{ statusLabel(cert.status) }}
              </span>
            </td>
            <td class="ops">
              <div class="row-actions">
                <button
                  v-if="cert.status === 'pending'"
                  class="text-btn text-btn--primary"
                  @click="issueCert(cert)"
                >发放</button>
                <button
                  v-if="cert.status === 'issued'"
                  class="text-btn text-btn--warn"
                  @click="revokeCert(cert)"
                >撤销</button>
                <button class="icon-btn icon-btn--danger" title="删除" @click="removeCert(cert)">
                  <Delete />
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="!certificates.length">
            <td colspan="8" class="empty-cell">暂无证书记录</td>
          </tr>
        </tbody>
      </table>
    </div>

    <AdminPager
      v-model:page="page"
      :total="certificates.length"
      :total-pages="totalPages"
    />

    <!-- 发放证书弹窗 -->
    <div v-if="modalOpen" class="modal-backdrop" @click.self="closeModal">
      <div class="modal">
        <div class="modal-head">
          <h2 class="modal-title">发放证书</h2>
          <button class="modal-close" @click="closeModal"><Close /></button>
        </div>
        <form class="modal-body" @submit.prevent="saveIssue">
          <label class="field">
            <span class="field-label">持证人</span>
            <select v-model="form.holder" class="field-input">
              <option value="" disabled>请选择学员</option>
              <option v-for="learner in learners" :key="learner.id" :value="learner.name">
                {{ learner.name }} ({{ learner.employeeId }} · {{ learner.department }})
              </option>
            </select>
          </label>
          <label class="field">
            <span class="field-label">证书名称</span>
            <input v-model.trim="form.title" type="text" class="field-input" placeholder="如 Kubernetes 云原生容器编排认证" />
          </label>
          <div class="field-row">
            <label class="field">
              <span class="field-label">颁发机构</span>
              <input v-model.trim="form.issueOrg" type="text" class="field-input" placeholder="如 企业技术委员会" />
            </label>
            <label class="field">
              <span class="field-label">有效期</span>
              <input v-model.trim="form.validity" type="text" class="field-input" placeholder="如 长期有效 / 24 个月" />
            </label>
          </div>
          <p class="form-hint">证书防伪编号将自动生成:{{ autoSerial }}</p>
          <p v-if="formError" class="form-error">{{ formError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn btn--ghost" @click="closeModal">取消</button>
            <button type="submit" class="btn btn--primary">确认发放</button>
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
import { Plus, Delete, Close } from '@element-plus/icons-vue';
import { ADMIN_CERTIFICATE_ISSUES, ADMIN_LEARNERS_DATA } from '../../mockData.js';
import { usePager } from '../../composables/usePager.js';
import { useConfirm } from '../../composables/useConfirm.js';
import AdminPager from '../../components/AdminPager.vue';
import ConfirmDialog from '../../components/ConfirmDialog.vue';

const certificates = ref(ADMIN_CERTIFICATE_ISSUES.map(c => ({ ...c })));

const { confirmState, askConfirm, cancelConfirm, acceptConfirm } = useConfirm();
const learners = ADMIN_LEARNERS_DATA;

const { page, totalPages, paged: pagedCertificates } = usePager(certificates, 6);

const issuedCount = computed(() => certificates.value.filter(c => c.status === 'issued').length);
const pendingCount = computed(() => certificates.value.filter(c => c.status === 'pending').length);
const revokedCount = computed(() => certificates.value.filter(c => c.status === 'revoked').length);

const statusLabelMap = {
  issued: '已发放',
  pending: '待发放',
  revoked: '已撤销'
};
const statusLabel = (status) => statusLabelMap[status] || status;

const todayText = () => {
  const d = new Date();
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
};

/* ---------- 发放弹窗 ---------- */
const modalOpen = ref(false);
const formError = ref('');
const autoSerial = computed(() => {
  const seq = String(certificates.value.length + 1).padStart(3, '0');
  return `ZX-${new Date().getFullYear()}-AUTO-${seq}`;
});
const form = ref({
  holder: '', title: '', issueOrg: '', validity: '长期有效'
});

const openIssue = () => {
  formError.value = '';
  form.value = {
    holder: '', title: '', issueOrg: '企业大学教培中心', validity: '长期有效'
  };
  modalOpen.value = true;
};

const closeModal = () => {
  modalOpen.value = false;
  formError.value = '';
};

const saveIssue = () => {
  if (!form.value.holder) { formError.value = '请选择持证学员。'; return; }
  if (!form.value.title) { formError.value = '请输入证书名称。'; return; }

  const learner = learners.find(l => l.name === form.value.holder);
  certificates.value.unshift({
    id: `aci-${Date.now()}`,
    holder: form.value.holder,
    employeeId: learner?.employeeId || '—',
    title: form.value.title,
    serialNumber: autoSerial.value,
    level: '企业认证',
    issueOrg: form.value.issueOrg,
    issueDate: todayText(),
    validity: form.value.validity || '长期有效',
    status: 'issued'
  });
  closeModal();
};

const issueCert = (cert) => {
  cert.status = 'issued';
  cert.issueDate = todayText();
};

const revokeCert = (cert) => {
  askConfirm({
    tone: 'primary',
    title: '撤销该证书？',
    subtitle: `${cert.holder} · ${cert.title}`,
    rows: [
      { label: '证书编号', value: cert.serialNumber },
      { label: '颁发机构', value: cert.issueOrg },
      { label: '颁发日期', value: cert.issueDate }
    ],
    tip: '撤销后该证书将失效，学员端不再展示，可用于纠错重新发放。',
    confirmText: '确认撤销',
    action: () => { cert.status = 'revoked'; }
  });
};

const removeCert = (cert) => {
  askConfirm({
    tone: 'danger',
    title: '删除该证书记录？',
    subtitle: `${cert.holder} · ${cert.title}`,
    rows: [
      { label: '证书编号', value: cert.serialNumber },
      { label: '颁发日期', value: cert.issueDate }
    ],
    tip: '删除后该发放记录将从列表中移除，此操作不可恢复。',
    confirmText: '确认删除',
    action: () => {
      certificates.value = certificates.value.filter(c => c.id !== cert.id);
    }
  });
};
</script>

<style scoped>
/* ============================================================
   AdminCertificates · 证书管理 · 纯原生 CSS
   ============================================================ */

* { margin: 0; padding: 0; box-sizing: border-box; }
button, input, select { font-family: inherit; }
button { cursor: pointer; }

.admin-certificates {
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
.overview-value--gold { color: #B45309; }
.overview-value--red  { color: #BE123C; }

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

th.ops, td.ops { text-align: right; white-space: nowrap; }

/* 表头默认 left 优先级更高,这里提权让操作列的表头与其内容对齐 */
.data-table thead th.ops { text-align: right; }

.mono { font-family: 'JetBrains Mono', ui-monospace, monospace; font-size: 12px; }
.cell-muted { color: #64748B; }
.empty-cell { text-align: center; color: #94A3B8; padding: 28px 0 !important; }

.cert-title-cell {
  max-width: 260px;
  font-weight: 600;
  white-space: normal;
  line-height: 1.5;
}

/* ========== 持证人单元格 ========== */
.cell-user {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 120px;
}

.cell-avatar {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  color: #B45309;
  background: #FEF3C7;
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
.status-pill--issued  { color: #047857; background: #D1FAE5; }
.status-pill--pending { color: #B45309; background: #FEF3C7; }
.status-pill--revoked { color: #64748B; background: #E2E8F0; }

/* ========== 行操作 ========== */
.row-actions {
  display: inline-flex;
  align-items: center;
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
.text-btn--primary { color: #005576; background: #D9F0FF; }
.text-btn--primary:hover { background: #C2E4FA; }
.text-btn--warn { color: #8A6414; background: #FEF3C7; }
.text-btn--warn:hover { background: #FDE68A; }

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
  max-width: 540px;
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

.form-hint {
  margin-top: -8px;
  font-size: 11px;
  color: #94A3B8;
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
