<template>

  <div v-if="certificate" class="modal-overlay" @click.self="closeModal">

    <div class="modal-container">

      <div class="modal-header">
        <div class="status-group">
          <span class="status-dot"></span>
          <span class="status-text">已发放证书</span>
        </div>
        <button class="close-btn" @click="closeModal">
          <span class="close-icon"></span>
        </button>
      </div>

      <div class="certificate-stage">
        <div class="certificate-paper">

          <div class="watermark">🛡</div>

          <div class="certificate-header">
            <div class="header-title">ENTERPRISE TECHNICAL CERTIFICATION</div>
            <div v-if="certificate.issueOrg" class="header-org">{{ certificate.issueOrg }}</div>
          </div>

          <div v-if="certificate.level" class="level-badge">
            <span>🛡</span>
            <span>{{ certificate.level }}</span>
          </div>

          <h2 class="certificate-title">{{ certificate.title }}</h2>

          <p class="recipient-intro">特此证明</p>
          <div v-if="recipientName" class="recipient-name">{{ recipientName }}</div>

          <p class="certificate-desc">{{ certificate.description }}</p>

          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">颁发机构</span>
              <span class="info-value">{{ certificate.issueOrg || '—' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">签发时间</span>
              <span class="info-value">{{ certificate.issueDate || '—' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">资质有效期</span>
              <span class="info-value">{{ certificate.validity || '—' }}</span>
            </div>
          </div>

          <div class="certificate-footer">
            <div v-if="certificate.blockchainHash || certificate.serialNumber" class="qr-block">
              <div class="qr-text">
                <div>{{ certificate.blockchainHash ? '区块链存证摘要' : '证书编号' }}</div>
                <div class="qr-code">{{ certificate.blockchainHash || certificate.serialNumber }}</div>
              </div>
            </div>
          </div>

        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  certificate: { type: Object, default: null },
  recipientName: { type: String, default: '' }
});

const emit = defineEmits(['close']);

function closeModal() {
  emit('close');
}
</script>

<style scoped>

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

button {
  font-family: inherit;
  cursor: pointer;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: rgba(15, 23, 42, 0.7);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to   { opacity: 1; }
}

.modal-container {
  width: 100%;
  max-width: 768px;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 16px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  border-bottom: 1px solid var(--app-border);
  background: var(--app-surface-2);
}

.status-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--app-emerald-solid);
}

.status-text {
  font-size: 12px;
  font-weight: 600;
  color: var(--app-text);
}

.close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 6px;
  color: var(--app-text-4);
  background: transparent;
  border: none;
  border-radius: 8px;
  transition: color 0.15s, background 0.15s;
}

.close-btn:hover {
  color: var(--app-text);
  background: var(--app-border);
}

.close-icon {
  position: relative;
  width: 20px;
  height: 20px;
  display: inline-block;
}

.close-icon::before,
.close-icon::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 16px;
  height: 2px;
  background: currentColor;
  border-radius: 1px;
}

.close-icon::before {
  transform: translate(-50%, -50%) rotate(45deg);
}

.close-icon::after {
  transform: translate(-50%, -50%) rotate(-45deg);
}

.certificate-stage {
  display: flex;
  justify-content: center;
  padding: 32px;
  background: linear-gradient(to bottom right, var(--app-surface-2), var(--app-page-bg), var(--app-blue-80));
}

.certificate-paper {
  position: relative;
  width: 100%;
  max-width: 672px;
  padding: 32px;
  background: var(--app-surface);
  border: 8px double rgba(131, 201, 244, 0.4);
  border-radius: 12px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1),
              0 8px 10px -6px rgba(0, 0, 0, 0.1);
  text-align: center;
  overflow: hidden;
}

.watermark {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 300px;
  opacity: 0.03;
  pointer-events: none;
  user-select: none;
}

.certificate-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--app-border);
  text-align: center;
}

.header-title {
  font-size: 12px;
  font-weight: 700;
  color: var(--app-accent-deep);
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.header-org {
  margin-top: 4px;
  font-size: 14px;
  font-weight: 600;
  color: var(--app-text);
}

.level-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 16px;
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 700;
  color: var(--app-accent-deep);
  background: var(--app-accent-soft);
  border: 1px solid rgba(131, 201, 244, 0.5);
  border-radius: 9999px;
}

.certificate-title {
  margin-bottom: 16px;
  font-size: 24px;
  font-weight: 800;
  color: var(--app-text);
  letter-spacing: -0.025em;
}

.recipient-intro {
  margin-bottom: 8px;
  font-size: 12px;
  color: var(--app-text-4);
}

.recipient-name {
  display: inline-block;
  margin-bottom: 16px;
  padding: 0 32px 4px;
  font-size: 20px;
  font-weight: 700;
  color: var(--app-accent-deep);
  border-bottom: 2px solid var(--app-accent);
}

.certificate-desc {
  max-width: 512px;
  margin: 0 auto 24px;
  font-size: 12px;
  line-height: 1.625;
  color: var(--app-text-3);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 24px;
  padding: 14px;
  background: var(--app-surface-2);
  border: 1px solid var(--app-border);
  border-radius: 8px;
  text-align: left;
  font-size: 12px;
}

.info-label {
  display: block;
  font-size: 10px;
  color: var(--app-text-4);
}

.info-value {
  font-weight: 600;
  color: var(--app-text);
}

.certificate-footer {
  display: flex;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid var(--app-border);
}

.qr-block {
  display: flex;
  align-items: center;
  gap: 8px;
  text-align: left;
}

.qr-text {
  font-size: 10px;
  color: var(--app-text-4);
  line-height: 1.5;
  overflow-wrap: anywhere;
}

.qr-code {
  font-family: "SFMono-Regular", Consolas, Menlo, monospace;
  color: var(--app-accent-deep);
}
</style>
