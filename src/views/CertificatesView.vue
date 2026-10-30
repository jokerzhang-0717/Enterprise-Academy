<template>
  <div class="certificate-page">
    <!-- 页面标题 -->
    <header class="page-header">
      <div class="page-heading">
        <div class="update-badge">
          <span>人才资产认证体系</span>
          <span>·</span>
          <span class="update-time">更新时间: 今日 09:30</span>
        </div>
        <h1 class="page-title">荣誉证书</h1>
        <p class="page-description">查看企业技术资质认证资产与合规证书。</p>
      </div>
    </header>

    <div class="certificate-content">
      <!-- 统计卡片 -->
      <section class="metrics-grid" aria-label="证书统计">
        <article class="metric-card">
          <div class="metric-copy">
            <div class="metric-label">已获得官方认证</div>
            <div class="metric-value">
              4 <span class="metric-unit">张证书</span>
            </div>
            <div class="metric-note metric-note--blue">
              <span aria-hidden="true">↑</span> 领先同序列 88% 同仁
            </div>
          </div>
          <div class="metric-icon metric-icon--blue">
            <ShieldCheck class="icon icon--large" aria-hidden="true" />
          </div>
        </article>

        <article class="metric-card">
          <div class="metric-copy">
            <div class="metric-label">技术雷达点亮技能</div>
            <div class="metric-value metric-value--purple">
              12 <span class="metric-unit">项能力</span>
            </div>
            <div class="metric-note metric-note--purple">
              <span aria-hidden="true">★</span> 包含 3 项云原生核心项
            </div>
          </div>
          <div class="metric-icon metric-icon--blue metric-icon--purple">
            <Cpu class="icon icon--large" aria-hidden="true" />
          </div>
        </article>

        <article class="metric-card">
          <div class="metric-copy">
            <div class="metric-label">企业合规有效状态</div>
            <div class="metric-value metric-value--green">
              100% <span class="metric-unit">生效中</span>
            </div>
            <div class="metric-note metric-note--green">
              <span class="status-dot" aria-hidden="true"></span>
              <span>全部证书在合规保护期</span>
            </div>
          </div>
          <div class="metric-icon metric-icon--green">
            <ShieldCheck class="icon icon--large" aria-hidden="true" />
          </div>
        </article>
      </section>

      <!-- 证书分类筛选 -->
      <nav class="filter-list" aria-label="证书分类">
          <button
            type="button"
            @click="selectedFilter = 'all'"
            class="filter-button"
            :class="{ 'filter-button--active': selectedFilter === 'all' }"
            :aria-pressed="selectedFilter === 'all'"
          >
            全部证书 (4)
          </button>
          <button
            type="button"
            @click="selectedFilter = 'architecture'"
            class="filter-button"
            :class="{ 'filter-button--active': selectedFilter === 'architecture' }"
            :aria-pressed="selectedFilter === 'architecture'"
          >
            架构实战类 (2)
          </button>
          <button
            type="button"
            @click="selectedFilter = 'compliance'"
            class="filter-button"
            :class="{ 'filter-button--active': selectedFilter === 'compliance' }"
            :aria-pressed="selectedFilter === 'compliance'"
          >
            合规风控类 (1)
          </button>
          <button
            type="button"
            @click="selectedFilter = 'devops'"
            class="filter-button"
            :class="{ 'filter-button--active': selectedFilter === 'devops' }"
            :aria-pressed="selectedFilter === 'devops'"
          >
            运维与容灾 (1)
          </button>
      </nav>

      <!-- 证书卡片列表 -->
      <section class="certificate-grid" aria-label="证书列表">
        <div
          v-for="cert in filteredCerts"
          :key="cert.id"
          :id="`certificate-${cert.id}`"
          class="certificate-card"
        >
          <!-- 卡片背景装饰 -->
          <div class="certificate-watermark" aria-hidden="true">
            <ShieldCheck class="watermark-icon" />
          </div>

          <div class="certificate-main">
            <!-- 等级与有效期 -->
            <div class="certificate-top">
              <div class="certificate-level">
                <div class="award-icon">
                  <Award class="icon icon--small" aria-hidden="true" />
                </div>
                <span class="level-name">{{ cert.level }}</span>
              </div>

              <span class="validity-badge">
                {{ cert.validity }}
              </span>
            </div>

            <h3 class="certificate-title">{{ cert.title }}</h3>

            <p class="certificate-description">
              {{ cert.description }}
            </p>

            <!-- 证书信息 -->
            <div class="certificate-details">
              <div class="detail-item">
                <span class="detail-label">防伪编号</span>
                <span class="detail-value detail-value--mono">{{ cert.serialNumber }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">颁发机构</span>
                <span class="detail-value">{{ cert.issueOrg }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">发放日期</span>
                <span class="detail-value detail-value--mono">{{ cert.issueDate }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">考核评级</span>
                <span class="detail-value detail-value--purple">{{ cert.rankingScore || '合格' }}</span>
              </div>
            </div>
          </div>

          <!-- 卡片底部操作 -->
          <div class="certificate-footer">
            <div class="certificate-privilege">
              <ShieldCheck class="icon icon--tiny" aria-hidden="true" />
              <span>{{ cert.privilege }}</span>
            </div>

            <button
              type="button"
              @click="emit('open-certificate-modal', cert.id)"
              class="preview-button"
            >
              <Eye class="icon icon--tiny" aria-hidden="true" />
              <span>在线预览</span>
            </button>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, watch } from 'vue';
import { 
  Award, 
  ShieldCheck, 
  Cpu, 
  Eye
} from 'lucide-vue-next';
import { CERTIFICATES_DATA } from '../mockData.js';

const emit = defineEmits(['open-certificate-modal']);
const props = defineProps({
  searchTargetId: {
    type: String,
    default: '',
  },
});

const selectedFilter = ref('all');

const filteredCerts = computed(() => {
  return CERTIFICATES_DATA.filter(c => {
    if (selectedFilter.value === 'all') return true;
    return c.category === selectedFilter.value;
  });
});

watch(
  () => props.searchTargetId,
  targetId => {
    if (!targetId) return;
    selectedFilter.value = 'all';
    nextTick(() => document.getElementById(targetId)?.scrollIntoView({ behavior: 'smooth', block: 'center' }));
  },
  { immediate: true },
);
</script>

<style scoped>
/* 页面整体间距和字体 */
.certificate-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding-bottom: 48px;
  color: #1e293b;
}

.certificate-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 页面标题 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.page-heading {
  min-width: 0;
}

.update-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
  padding: 3px 10px;
  border-radius: 999px;
  background: #d9f0ff;
  color: #005576;
  font-size: 10px;
  font-weight: 700;
}

.update-time {
  font-family: 'JetBrains Mono', Consolas, monospace;
  font-weight: 500;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  letter-spacing: -0.025em;
}

.page-description {
  margin: 2px 0 0;
  color: #64748b;
  font-size: 12px;
}

/* 顶部统计卡片 */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.metric-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-width: 0;
  padding: 20px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 1px 2px rgb(15 23 42 / 5%);
}

.metric-copy {
  min-width: 0;
}

.metric-label {
  color: #64748b;
  font-size: 12px;
}

.metric-value {
  margin: 4px 0;
  color: #1e293b;
  font-family: 'JetBrains Mono', Consolas, monospace;
  font-size: 30px;
  font-weight: 800;
  line-height: 1.2;
}

.metric-value--purple,
.metric-note--purple {
  color: #6555b0;
}

.metric-value--green,
.metric-note--green {
  color: #059669;
}

.metric-unit {
  color: #64748b;
  font-family: inherit;
  font-size: 12px;
  font-weight: 400;
}

.metric-note {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
}

.metric-note--blue {
  color: #005576;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #10b981;
}

.metric-icon,
.award-icon {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  color: #005576;
}

.metric-icon {
  width: 48px;
  height: 48px;
  border-radius: 16px;
}

.metric-icon--blue,
.award-icon {
  background: #d9f0ff;
}

.metric-icon--purple {
  color: #6555b0;
}

.metric-icon--green {
  background: #ecfdf5;
  color: #059669;
}

/* 图标尺寸 */
.icon {
  display: block;
  flex: 0 0 auto;
}

.icon--large {
  width: 24px;
  height: 24px;
}

.icon--small {
  width: 16px;
  height: 16px;
}

.icon--tiny {
  width: 14px;
  height: 14px;
}

/* 分类筛选按钮 */
.filter-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-button {
  padding: 6px 16px;
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  background: #fff;
  color: #475569;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: background-color 150ms ease, color 150ms ease, border-color 150ms ease;
}

.filter-button:hover {
  background: #f1f5f9;
}

.filter-button--active {
  border-color: #83c9f4;
  background: #83c9f4;
  color: #005576;
  box-shadow: 0 1px 2px rgb(15 23 42 / 8%);
}

.filter-button--active:hover {
  background: #83c9f4;
}

.filter-button:focus-visible,
.preview-button:focus-visible {
  outline: 3px solid rgb(0 85 118 / 25%);
  outline-offset: 2px;
}

/* 证书卡片 */
.certificate-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
}

.certificate-card {
  position: relative;
  display: flex;
  min-width: 0;
  flex-direction: column;
  justify-content: space-between;
  gap: 16px;
  overflow: hidden;
  padding: 24px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 1px 2px rgb(15 23 42 / 5%);
  transition: border-color 150ms ease;
}

.certificate-card:hover {
  border-color: #83c9f4;
}

.certificate-main {
  position: relative;
  z-index: 1;
}

.certificate-watermark {
  position: absolute;
  right: -32px;
  bottom: -32px;
  color: #005576;
  opacity: 0.035;
  pointer-events: none;
}

.watermark-icon {
  display: block;
  width: 224px;
  height: 224px;
}

.certificate-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.certificate-level {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 8px;
}

.award-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
}

.level-name {
  color: #005576;
  font-size: 11px;
  font-weight: 700;
}

.validity-badge {
  flex: 0 0 auto;
  padding: 3px 10px;
  border-radius: 999px;
  background: #d9f0ff;
  color: #005576;
  font-size: 10px;
  font-weight: 700;
}

.certificate-title {
  margin: 0 0 8px;
  color: #1e293b;
  font-size: 16px;
  font-weight: 700;
}

.certificate-description {
  margin: 0 0 16px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.6;
}

/* 证书编号、机构、日期和评级 */
.certificate-details {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
  padding: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #f8fafc;
}

.detail-item {
  min-width: 0;
}

.detail-label {
  display: block;
  margin-bottom: 2px;
  color: #94a3b8;
  font-size: 10px;
}

.detail-value {
  display: block;
  overflow: hidden;
  color: #1e293b;
  font-size: 11px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-value--mono {
  font-family: 'JetBrains Mono', Consolas, monospace;
  font-weight: 500;
}

.detail-value--purple {
  color: #6555b0;
  font-weight: 700;
}

/* 卡片底部说明和预览按钮 */
.certificate-footer {
  position: relative;
  z-index: 1;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
}

.certificate-privilege {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 4px;
  color: #005576;
  font-size: 11px;
  font-weight: 500;
}

.preview-button {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  background: #f8fafc;
  color: #475569;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 150ms ease;
}

.preview-button:hover {
  background: #f1f5f9;
}

/* 平板和手机布局 */
@media (max-width: 900px) {
  .metrics-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .certificate-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 600px) {
  .certificate-page,
  .certificate-content {
    gap: 18px;
  }

  .metrics-grid {
    grid-template-columns: 1fr;
  }

  .metric-card,
  .certificate-card {
    padding: 18px;
  }

  .certificate-details {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .certificate-top {
    align-items: flex-start;
  }

  .validity-badge {
    max-width: 48%;
    text-align: center;
    white-space: normal;
  }
}

@media (prefers-reduced-motion: reduce) {
  .certificate-page {
    animation: none;
  }

  .filter-button,
  .preview-button,
  .certificate-card {
    transition: none;
  }
}
</style>
