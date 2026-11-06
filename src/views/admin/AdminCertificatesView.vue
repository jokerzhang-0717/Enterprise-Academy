<template>
  <div class="admin-certificates admin-fit-page">

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

    <div class="filter-toolbar">
      <select v-model="statusFilter" class="filter-select" aria-label="按证书状态筛选">
        <option value="">全部状态</option>
        <option value="issued">已发放</option>
        <option value="pending">待发放</option>
        <option value="revoked">已撤销</option>
      </select>
      <select v-model="issuerFilter" class="filter-select" aria-label="按颁发机构筛选">
        <option value="">全部机构</option>
        <option v-for="issuer in issuers" :key="issuer" :value="issuer">{{ issuer }}</option>
      </select>
      <label class="filter-search">
        <span class="search-icon" aria-hidden="true"></span>
        <input
          v-model.trim="keyword"
          class="filter-search-input"
          type="text"
          placeholder="搜索持证人 / 证书名称 / 编号..."
          aria-label="搜索持证人、证书名称或编号"
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
      <span class="filter-count">{{ filteredCertificates.length }} 张证书</span>
    </div>

    <!-- 证书表格 -->
    <div class="table-wrap">
      <table class="data-table data-table--wide">
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
          <tr v-for="cert in pagedCertificates" :key="cert.id" @click="openDetail(cert)">
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
              <div class="row-actions" @click.stop>
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
                <button class="icon-btn" title="编辑" @click="openEdit(cert)">
                  <EditPen />
                </button>
                <button class="icon-btn icon-btn--danger" title="删除" @click="removeCert(cert)">
                  <Delete />
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="!filteredCertificates.length">
            <td colspan="8" class="empty-cell">
              {{ certificates.length ? '没有匹配的证书' : '暂无证书记录' }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <AdminPager
      v-model:page="page"
      :total="filteredCertificates.length"
      :total-pages="totalPages"
    />

    <!-- 证书详情 -->
    <AdminDetailModal
      v-if="detailCert"
      title="证书详情"
      :subtitle="`${detailCert.holder} · ${detailCert.employeeId}`"
      :tags="[statusLabel(detailCert.status)]"
      :rows="[
        { label: '证书名称', value: detailCert.title, wide: true },
        { label: '证书编号', value: detailCert.serialNumber, wide: true },
        { label: '颁发机构', value: detailCert.issueOrg },
        { label: '颁发日期', value: detailCert.issueDate },
        { label: '有效期', value: detailCert.validity },
        { label: '当前状态', value: statusLabel(detailCert.status) }
      ]"
      @close="detailCert = null"
    />

    <!-- 发放 / 编辑证书弹窗 -->
    <div v-if="modalOpen" class="modal-backdrop" @click.self="closeModal">
      <div class="modal">
        <div class="modal-head">
          <h2 class="modal-title">{{ editingId ? '编辑证书' : '发放证书' }}</h2>
          <button type="button" class="modal-close" @click="closeModal"><Close /></button>
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
          <p v-if="editingId" class="form-hint">证书编号保持不变:{{ form.serialNumber }}</p>
          <p v-else class="form-hint">证书防伪编号由服务端在发放时自动生成</p>
          <p v-if="formError" class="form-error">{{ formError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn btn--ghost" @click="closeModal">取消</button>
            <button type="submit" class="btn btn--primary" :disabled="saving">{{ saving ? '提交中…' : (editingId ? '保存修改' : '确认发放') }}</button>
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
import { computed, onMounted, ref, watch } from 'vue';
import { Plus, EditPen, Delete, Close } from '@element-plus/icons-vue';
import { adminStore, loadCertificates } from '../../adminStore.js';
import {
  createAdminCertificateTemplate,
  deleteAdminCertificate,
  fetchAdminCertificateTemplates,
  issueAdminCertificate,
  updateAdminCertificate,
  updateAdminCertificateStatus,
  updateAdminCertificateTemplate
} from '../../api/index.js';
import { usePager } from '../../composables/usePager.js';
import { useConfirm } from '../../composables/useConfirm.js';
import { useToast } from '../../composables/useToast.js';
import AdminPager from '../../components/AdminPager.vue';
import AdminDetailModal from '../../components/AdminDetailModal.vue';
import ConfirmDialog from '../../components/ConfirmDialog.vue';

const { show: showToast } = useToast();

const certificates = computed(() => adminStore.certificates);

const { confirmState, askConfirm, cancelConfirm, acceptConfirm } = useConfirm();
const learners = computed(() => adminStore.users);

/* 证书名称与颁发机构属于模板，接口按 templateId 关联，名称不在模板内时新建一条模板 */
const templates = ref([]);
onMounted(async () => {
  templates.value = await fetchAdminCertificateTemplates().catch(() => []) || [];
});

const resolveTemplate = async (title, issueOrg) => {
  const hit = templates.value.find(t => t.title === title);
  if (!hit) {
    const created = await createAdminCertificateTemplate({
      title,
      issueOrg: issueOrg || '企业大学教培中心',
      levelLabel: '企业认证'
    });
    if (created?.id) templates.value = [...templates.value, created];
    return created || null;
  }
  if ((hit.issueOrg || '') !== (issueOrg || '')) {
    const saved = await updateAdminCertificateTemplate(hit.id, { ...hit, issueOrg });
    templates.value = templates.value.map(t => t.id === hit.id ? { ...t, issueOrg } : t);
    return saved || { ...hit, issueOrg };
  }
  return hit;
};

const statusFilter = ref('');
const issuerFilter = ref('');
const keyword = ref('');
const issuers = computed(() => [...new Set(certificates.value.map(c => c.issueOrg).filter(Boolean))].sort());

const filteredCertificates = computed(() => {
  const search = keyword.value.toLowerCase();
  return certificates.value.filter((cert) => {
    const matchesStatus = !statusFilter.value || cert.status === statusFilter.value;
    const matchesIssuer = !issuerFilter.value || cert.issueOrg === issuerFilter.value;
    const matchesKeyword = !search ||
      `${cert.holder} ${cert.employeeId} ${cert.title} ${cert.serialNumber} ${cert.issueOrg}`.toLowerCase().includes(search);
    return matchesStatus && matchesIssuer && matchesKeyword;
  });
});

const { page, totalPages, paged: pagedCertificates, resetPage } = usePager(filteredCertificates, 7);
watch([statusFilter, issuerFilter, keyword], resetPage);

const issuedCount = computed(() => certificates.value.filter(c => c.status === 'issued').length);
const pendingCount = computed(() => certificates.value.filter(c => c.status === 'pending').length);
const revokedCount = computed(() => certificates.value.filter(c => c.status === 'revoked').length);

const statusLabelMap = {
  issued: '已发放',
  pending: '待发放',
  revoked: '已撤销'
};
const statusLabel = (status) => statusLabelMap[status] || status;

/* ---------- 证书详情 ---------- */
const detailCert = ref(null);
const openDetail = (cert) => { detailCert.value = cert; };

/* ---------- 发放 / 编辑弹窗 ---------- */
const modalOpen = ref(false);
const editingId = ref(null);
const formError = ref('');
const saving = ref(false);
const form = ref({
  holder: '', title: '', issueOrg: '', validity: '长期有效', serialNumber: ''
});

const openIssue = () => {
  editingId.value = null;
  formError.value = '';
  form.value = {
    holder: '', title: '', issueOrg: '企业大学教培中心', validity: '长期有效', serialNumber: ''
  };
  modalOpen.value = true;
};

const openEdit = (cert) => {
  editingId.value = cert.numericId ?? cert.id;
  formError.value = '';
  form.value = {
    holder: cert.holder,
    title: cert.title,
    issueOrg: cert.issueOrg,
    validity: cert.validity,
    serialNumber: cert.serialNumber
  };
  modalOpen.value = true;
};

const closeModal = () => {
  modalOpen.value = false;
  editingId.value = null;
  formError.value = '';
};

const saveIssue = async () => {
  if (!form.value.holder) { formError.value = '请选择持证学员。'; return; }
  if (!form.value.title) { formError.value = '请输入证书名称。'; return; }

  const learner = learners.value.find(l => l.name === form.value.holder);
  if (!learner) { formError.value = '未找到该学员，请重新选择持证人。'; return; }

  saving.value = true;
  const isEdit = !!editingId.value;
  try {
    const template = await resolveTemplate(form.value.title, form.value.issueOrg);
    if (!template?.id) { throw new Error('证书模板准备失败，请重试。'); }

    const payload = {
      templateId: template.id,
      userId: learner.numericId ?? learner.id,
      validityLabel: form.value.validity || '长期有效'
    };
    if (form.value.serialNumber) payload.serialNumber = form.value.serialNumber;

    if (isEdit) {
      await updateAdminCertificate(editingId.value, payload);
    } else {
      payload.status = 2;
      await issueAdminCertificate(payload);
    }
    await loadCertificates();
    closeModal();
    showToast(isEdit ? '证书记录已更新' : `已向 ${form.value.holder} 发放证书`, 'success');
  } catch (err) {
    formError.value = err?.message || '保存证书失败，请重试。';
  } finally {
    saving.value = false;
  }
};

const issueCert = async (cert) => {
  try {
    await updateAdminCertificateStatus(cert.numericId ?? cert.id, 2);
    await loadCertificates();
    showToast(`已发放证书 ${cert.serialNumber}`, 'success');
  } catch (err) {
    showToast(err?.message || '发放证书失败', 'error');
  }
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
    action: async () => {
      try {
        await updateAdminCertificateStatus(cert.numericId ?? cert.id, 3);
        await loadCertificates();
        showToast('证书已撤销', 'success');
      } catch (err) {
        showToast(err?.message || '撤销证书失败', 'error');
      }
    }
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
    tip: '删除后该发放记录将从列表和学员端移除，此操作不可恢复。',
    confirmText: '确认删除',
    action: async () => {
      try {
        await deleteAdminCertificate(cert.numericId ?? cert.id);
        await loadCertificates();
        showToast('证书记录已删除', 'success');
      } catch (err) {
        showToast(err?.message || '删除证书记录失败', 'error');
      }
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
  grid-template-columns: repeat(4, minmax(0, 1fr));
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
.overview-value--gold { color: var(--app-amber-fg); }
.overview-value--red  { color: var(--app-rose-fg); }

.overview-label {
  font-size: 12px;
  color: var(--app-text-4);
}

/* ========== 筛选工具栏 ========== */
.filter-toolbar {
  display: grid;
  grid-template-columns: minmax(140px, 180px) minmax(150px, 210px) minmax(220px, 1fr) auto;
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
  vertical-align: top;
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
.data-table tbody tr:hover .mono,
.data-table tbody tr:hover .cert-title-cell {
  color: var(--app-accent-deep);
}

th.ops, td.ops { text-align: right; white-space: nowrap; }

/* 表头默认 left 优先级更高,这里提权让操作列的表头与其内容对齐 */
.data-table thead th.ops { text-align: right; }

.mono { font-family: 'JetBrains Mono', ui-monospace, monospace; font-size: 12px; }
.cell-muted { color: var(--app-text-4); }
.empty-cell { text-align: center; color: var(--app-text-5); padding: 28px 0 !important; }

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
  color: var(--app-amber-fg);
  background: var(--app-brand-tint);
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
.status-pill--issued  { color: var(--app-emerald-fg); background: var(--app-emerald-bg); }
.status-pill--pending { color: var(--app-amber-fg); background: var(--app-amber-bg); }
.status-pill--revoked { color: var(--app-text-4); background: var(--app-chip); }

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
.text-btn--primary { color: var(--app-accent-deep); background: var(--app-accent-soft); }
.text-btn--primary:hover { background: var(--app-accent-hover); }
.text-btn--warn { color: var(--app-brand-ink); background: var(--app-brand-tint); }
.text-btn--warn:hover { background: var(--app-brand-tint-line); }

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
  max-width: 540px;
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

.form-hint {
  margin-top: -8px;
  font-size: 11px;
  color: var(--app-text-5);
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
  .field-row { grid-template-columns: 1fr; }
}
</style>
