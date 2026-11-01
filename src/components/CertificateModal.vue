<template>
  <!-- 证书弹窗（有证书数据才显示） -->
  <div v-if="certificate" class="modal-overlay" @click.self="closeModal">

    <div class="modal-container">

      <!-- 顶部信息栏 -->
      <div class="modal-header">
        <div class="status-group">
          <span class="status-dot"></span>
          <span class="status-text">企业级官方资质认证 · 证书防伪验证通过</span>
        </div>
        <button class="close-btn" @click="closeModal">
          <span class="close-icon"></span>
        </button>
      </div>

      <!-- 证书画布 -->
      <div class="certificate-stage">
        <div class="certificate-paper">

          <!-- 防伪水印 -->
          <div class="watermark">🛡</div>

          <!-- 页眉 -->
          <div class="certificate-header">
            <div class="header-title">ENTERPRISE TECHNICAL CERTIFICATION</div>
            <div class="header-org">云舒艺境 · 企业数字化人才培养委员会</div>
          </div>

          <!-- 等级徽章 -->
          <div class="level-badge">
            <span>🛡</span>
            <span>{{ certificate.level }}</span>
          </div>

          <!-- 证书标题 -->
          <h2 class="certificate-title">{{ certificate.title }}</h2>

          <!-- 获奖人 -->
          <p class="recipient-intro">特此证明</p>
          <div class="recipient-name">{{ recipientName }}</div>

          <!-- 证书说明 -->
          <p class="certificate-desc">{{ certificate.description }}</p>

          <!-- 信息栏 -->
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">颁发机构</span>
              <span class="info-value">{{ certificate.issueOrg }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">签发时间</span>
              <span class="info-value">{{ certificate.issueDate }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">资质有效期</span>
              <span class="info-value">{{ certificate.validity }}</span>
            </div>
          </div>

          <!-- 底部 -->
          <div class="certificate-footer">
            <div class="qr-block">
              <div class="qr-icon"></div>
              <div class="qr-text">
                <div>扫码校验链上存证</div>
                <div class="qr-code">Block: #09281-ZK</div>
              </div>
            </div>

            <!-- 红色印章 -->
            <div class="seal">
              <div class="seal-org">企业技术委员会</div>
              <div class="seal-main">★ 认证验讫 ★</div>
              <div class="seal-sub">LMS VERIFIED</div>
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
  recipientName: { type: String, default: '张浩然' }
});

const emit = defineEmits(['close']);

function closeModal() {
  emit('close');
}
</script>

<style scoped>
/* ========== 全局重置 ========== */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

button {
  font-family: inherit;
  cursor: pointer;
}

/* ========== 弹窗遮罩 ========== */
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

/* ========== 弹窗主体 ========== */
.modal-container {
  width: 100%;
  max-width: 768px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 16px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* ========== 顶部信息栏 ========== */
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  border-bottom: 1px solid #E2E8F0;
  background: #F8FAFC;
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
  background: #10b981;
}

.status-text {
  font-size: 12px;
  font-weight: 600;
  color: #1E293B;
}

.close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 6px;
  color: #64748B;
  background: transparent;
  border: none;
  border-radius: 8px;
  transition: color 0.15s, background 0.15s;
}

.close-btn:hover {
  color: #1E293B;
  background: #E2E8F0;
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

/* ========== 证书画布 ========== */
.certificate-stage {
  display: flex;
  justify-content: center;
  padding: 32px;
  background: linear-gradient(to bottom right, #F8FAFC, #F4F9FD, #EBF3FA);
}

/* ========== 证书纸张 ========== */
.certificate-paper {
  position: relative;
  width: 100%;
  max-width: 672px;
  padding: 32px;
  background: #fff;
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

/* ========== 页眉 ========== */
.certificate-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #E2E8F0;
  text-align: center;
}

.header-title {
  font-size: 12px;
  font-weight: 700;
  color: #005576;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.header-org {
  margin-top: 4px;
  font-size: 14px;
  font-weight: 600;
  color: #1E293B;
}

/* ========== 等级徽章 ========== */
.level-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 16px;
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 700;
  color: #005576;
  background: #D9F0FF;
  border: 1px solid rgba(131, 201, 244, 0.5);
  border-radius: 9999px;
}

/* ========== 证书标题 ========== */
.certificate-title {
  margin-bottom: 16px;
  font-size: 24px;
  font-weight: 800;
  color: #1E293B;
  letter-spacing: -0.025em;
}

/* ========== 获奖人 ========== */
.recipient-intro {
  margin-bottom: 8px;
  font-size: 12px;
  color: #64748B;
}

.recipient-name {
  display: inline-block;
  margin-bottom: 16px;
  padding: 0 32px 4px;
  font-size: 20px;
  font-weight: 700;
  color: #005576;
  border-bottom: 2px solid #83C9F4;
}

/* ========== 证书说明 ========== */
.certificate-desc {
  max-width: 512px;
  margin: 0 auto 24px;
  font-size: 12px;
  line-height: 1.625;
  color: #475569;
}

/* ========== 信息栏 ========== */
.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 24px;
  padding: 14px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  text-align: left;
  font-size: 12px;
}

.info-label {
  display: block;
  font-size: 10px;
  color: #64748B;
}

.info-value {
  font-weight: 600;
  color: #1E293B;
}

/* ========== 底部 ========== */
.certificate-footer {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  padding-top: 16px;
  border-top: 1px solid #E2E8F0;
}

.qr-block {
  display: flex;
  align-items: center;
  gap: 8px;
  text-align: left;
}

.qr-icon {
  width: 40px;
  height: 40px;
  border: 1px solid #CBD5E1;
  border-radius: 4px;
  padding: 4px;
  background:
    linear-gradient(#334155, #334155) 4px 4px / 10px 10px no-repeat,
    linear-gradient(#334155, #334155) 26px 4px / 10px 10px no-repeat,
    linear-gradient(#334155, #334155) 4px 26px / 10px 10px no-repeat,
    linear-gradient(#334155, #334155) 20px 16px / 4px 4px no-repeat,
    linear-gradient(#334155, #334155) 28px 20px / 4px 4px no-repeat,
    linear-gradient(#334155, #334155) 16px 28px / 4px 4px no-repeat,
    linear-gradient(#334155, #334155) 24px 28px / 4px 4px no-repeat,
    linear-gradient(#334155, #334155) 32px 32px / 4px 4px no-repeat,
    linear-gradient(#334155, #334155) 20px 20px / 4px 4px no-repeat;
  background-color: #fff;
  flex-shrink: 0;
}

.qr-text {
  font-size: 10px;
  color: #64748B;
  line-height: 1.5;
}

.qr-code {
  font-family: "SFMono-Regular", Consolas, Menlo, monospace;
  color: #005576;
}

/* 红色印章 */
.seal {
  width: 80px;
  height: 80px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4px;
  color: #DC2626;
  border: 2px dashed #DC2626;
  border-radius: 50%;
  transform: rotate(-12deg);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  opacity: 0.85;
  user-select: none;
  text-align: center;
}

.seal-org {
  font-size: 8px;
  font-weight: 700;
  letter-spacing: -0.05em;
}

.seal-main {
  margin: 2px 0;
  font-size: 11px;
  font-weight: 700;
}

.seal-sub {
  font-size: 7px;
  font-weight: 700;
}
</style>