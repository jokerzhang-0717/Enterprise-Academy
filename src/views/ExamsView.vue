<template>
  <div class="exm-page">
    <!-- 1. Overview Metrics -->
    <div class="exm-metrics">
      <!-- 待参加考核 -->
      <div class="exm-metric">
        <div class="exm-metric__body">
          <div class="exm-metric__label">待参加考核</div>
          <div class="exm-metric__value">
            3 <span class="exm-metric__note exm-metric__note--blue">1场正在进行</span>
          </div>
          <div class="exm-metric__foot">本季度必考安排</div>
        </div>
        <div class="exm-metric__icon">
          <FileCheck2 class="exm-icon" />
        </div>
      </div>

      <!-- 已通过认证科目 -->
      <div class="exm-metric">
        <div class="exm-metric__body">
          <div class="exm-metric__label">已通过认证科目</div>
          <div class="exm-metric__value">
            8 <span class="exm-metric__note exm-metric__note--green">获8份电子徽章</span>
          </div>
          <div class="exm-metric__foot">认证率 100% 达标</div>
        </div>
        <div class="exm-metric__icon">
          <Award class="exm-icon" />
        </div>
      </div>

      <!-- 综合考核平均分 -->
      <div class="exm-metric">
        <div class="exm-metric__body">
          <div class="exm-metric__label">综合考核平均分</div>
          <div class="exm-metric__value exm-metric__value--purple">
            88.5 <span class="exm-metric__note">通过率 100%</span>
          </div>
          <div class="exm-metric__foot">超过 91% 同步考生</div>
        </div>
        <div class="exm-metric__icon exm-metric__icon--purple">
          <Layers class="exm-icon" />
        </div>
      </div>
    </div>

    <!-- 2. Upcoming Exam Arrangements -->
    <section class="exm-section">
      <!-- 标题 + 筛选 -->
      <div class="exm-section__head">
        <div class="exm-section__title">
          <span class="exm-section__bar" />
          <h2>待参加考试与测评安排</h2>
          <span class="exm-section__hint">点击对应课程考卷卡片即可即时解锁考位并加载在线作答套题</span>
        </div>

        <div class="exm-tabs">
          <span class="exm-tabs__label">筛选状态:</span>
          <button
            type="button"
            class="exm-tab"
            :class="filterTab === 'all' ? 'is-active' : ''"
            @click="setFilterTab('all')"
          >
            全部安排 (3)
          </button>
          <button
            type="button"
            class="exm-tab"
            :class="filterTab === 'in_progress' ? 'is-active' : ''"
            @click="setFilterTab('in_progress')"
          >
            进行中 (1)
          </button>
          <button
            type="button"
            class="exm-tab"
            :class="filterTab === 'not_started' ? 'is-active' : ''"
            @click="setFilterTab('not_started')"
          >
            未开始 (2)
          </button>
        </div>
      </div>

      <!-- 考试卡片列表 -->
      <div class="exm-cards">
        <article
          v-for="exam in filteredExams"
          :id="`exam-${exam.id}`"
          :key="exam.id"
          class="exm-card"
        >
          <!-- 左侧信息 -->
          <div class="exm-card__info">
            <div class="exm-card__tags">
              <span v-if="exam.status === 'in_progress'" class="exm-badge exm-badge--live">
                可随时进入 · 随学随考
              </span>
              <span v-else-if="exam.status === 'not_started'" class="exm-badge exm-badge--wait">
                未开始 · 请稍后
              </span>
              <span v-else class="exm-badge exm-badge--anytime">
                可随时进入 · 随学随考
              </span>
            </div>

            <h3 class="exm-card__title">{{ exam.title }}</h3>

            <div class="exm-card__meta">
              <span>时间: <strong>{{ exam.timeWindow }}</strong></span>
              <span>时长: <strong>{{ exam.durationMinutes }} 分钟 · 满分 {{ exam.totalScore }}</strong></span>
            </div>
          </div>

          <!-- 右侧操作 -->
          <div class="exm-card__actions">
            <!-- 进行中 -->
            <button
              v-if="exam.status === 'in_progress'"
              type="button"
              class="exm-btn exm-btn--primary"
              @click="$emit('navigate', 'exam-taking')"
            >
              <FileCheck2 class="exm-icon exm-icon--sm" />
              <span>开始考试</span>
            </button>

            <!-- 未开始 -->
            <button
              v-else-if="exam.status === 'not_started'"
              type="button"
              class="exm-btn exm-btn--disabled"
              disabled
            >
              <Clock class="exm-icon exm-icon--sm" />
              <span>暂未开放</span>
            </button>

            <!-- 随学随考 -->
            <button
              v-else-if="exam.status === 'anytime'"
              type="button"
              class="exm-btn exm-btn--soft"
              @click="$emit('navigate', 'exam-taking')"
            >
              <Play class="exm-icon exm-icon--sm exm-icon--fill" />
              <span>开始考试</span>
            </button>
          </div>
        </article>
      </div>
    </section>

    <!-- 3. Past Exam Records -->
    <section class="exm-panel">
      <!-- 面板头部 -->
      <div class="exm-panel__head">
        <div>
          <h3 class="exm-panel__title">往期测评档案与错题深度回顾</h3>
          <p class="exm-panel__desc">支持调取知识盲区雷达图及错题解析报告</p>
        </div>

        <div class="exm-panel__stats">
          <span>总参测: <strong class="exm-stat exm-stat--dark">8 场</strong></span>
          <span>平均分: <strong class="exm-stat exm-stat--blue">88.5</strong></span>
          <span>通过率: <strong class="exm-stat exm-stat--green">100%</strong></span>
        </div>
      </div>

      <!-- 表格 -->
      <div class="exm-table-wrap">
        <table class="exm-table">
          <thead>
            <tr>
              <th>考试科目</th>
              <th>完成时间</th>
              <th>得分</th>
              <th>考核状态</th>
              <th class="exm-table__right">错题分析</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in PAST_EXAMS" :id="`past-exam-${row.id}`" :key="row.id">
              <td>
                <div class="exm-table__subject">{{ row.title }}</div>
              </td>
              <td class="exm-table__mono">{{ row.completionDate }}</td>
              <td class="exm-table__score">{{ row.score }}</td>
              <td>
                <span class="exm-pass">
                  <span class="exm-pass__dot" />
                  {{ row.status === 'passed_perfect' ? '满分通过' : row.status === 'passed_excellent' ? '优秀通过' : '合格通过' }}
                </span>
              </td>
              <td class="exm-table__right">
                <button
                  v-if="row.wrongCount > 0"
                  type="button"
                  class="exm-link"
                  @click="wrongAnalysisModal = row.title"
                >
                  <FileText class="exm-icon exm-icon--xs" />
                  <span>错题分析 ({{ row.wrongCount }})</span>
                </button>
                <span v-else class="exm-table__empty">无错题记录</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <!-- 4. Wrong Questions Modal -->
    <div v-if="wrongAnalysisModal" class="exm-modal" role="dialog" aria-modal="true">
      <div class="exm-modal__box">
        <h3 class="exm-modal__title">错题复盘解析报告</h3>
        <div class="exm-modal__subject">{{ wrongAnalysisModal }}</div>

        <div class="exm-modal__list">
          <div class="exm-wrong">
            <div class="exm-wrong__q">第 14 题: 分布式两阶段提交 (2PC) 的阻塞性缺点</div>
            <div class="exm-wrong__a">
              你的选项: B (网络分区下自动选主) | 正确答案: C (协调者单点故障导致参与者资源一直锁定锁定)
            </div>
          </div>

          <div class="exm-wrong">
            <div class="exm-wrong__q">第 21 题: Raft 算法中的 Term 周期选举安全性</div>
            <div class="exm-wrong__a">
              你的选项: A | 正确答案: D (日志包含最新已提交索引的候选人才能获得投票)
            </div>
          </div>
        </div>

        <button type="button" class="exm-btn exm-btn--close" @click="wrongAnalysisModal = null">
          已掌握关闭
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, watch } from 'vue';
import {
  FileCheck2,
  Clock,
  Award,
  FileText,
  Play,
  Layers
} from 'lucide-vue-next';
import { EXAMS_DATA, PAST_EXAMS } from '../mockData.js';

const props = defineProps({
  searchTargetId: {
    type: String,
    default: '',
  },
});

defineEmits(['navigate', 'open-cert']);

const filterTab = ref('all');
const wrongAnalysisModal = ref(null);

/* ---------- 筛选（纯函数，无副作用） ---------- */
const filteredExams = computed(() => {
  return EXAMS_DATA.filter(exam => {
    if (filterTab.value === 'in_progress') return exam.status === 'in_progress';
    if (filterTab.value === 'not_started') return exam.status !== 'in_progress';
    return true;
  });
});

/* ---------- 筛选切换：统一入口，避免直接改 ref 造成隐式闭环 ---------- */
const setFilterTab = (tab) => {
  if (filterTab.value === tab) return; // 同值短路，防止无意义的重复触发
  filterTab.value = tab;
};

/* ---------- 搜索定位滚动（修复死循环 / 爆栈风险） ---------- */
let lastScrolledId = '';

const scrollToTarget = (targetId) => {
  if (!targetId) return;
  // 同一个目标不重复滚动，打断 watch <-> 状态 闭环
  if (lastScrolledId === targetId) return;
  lastScrolledId = targetId;

  nextTick(() => {
    requestAnimationFrame(() => {
      const el = document.getElementById(targetId);
      if (el && typeof el.scrollIntoView === 'function') {
        el.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
    });
  });
};

watch(
  () => props.searchTargetId,
  (targetId) => {
    if (!targetId) return;

    // 只在当前筛选不包含目标时才重置，且重置后值不变就不再赋值
    if (filterTab.value !== 'all') {
      filterTab.value = 'all';
    }

    scrollToTarget(targetId);
  },
  { immediate: true },
);
</script>

<style scoped>
/* ============ Design tokens ============ */
.exm-page {
  --c-text: #1e293b;
  --c-text-sub: #334155;
  --c-text-soft: #475569;
  --c-muted: #64748b;
  --c-faint: #94a3b8;
  --c-border: #e2e8f0;
  --c-border-soft: #f1f5f9;
  --c-bg-soft: #f8fafc;
  --c-blue: #005576;
  --c-blue-bg: #d9f0ff;
  --c-sky: #83c9f4;
  --c-sky-hover: #6fb9e8;
  --c-purple: #6555b0;
  --c-green: #059669;

  display: flex;
  flex-direction: column;
  gap: 24px;
  padding-bottom: 48px;
  font-family: 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', sans-serif;
}

/* ============ Icons (lucide) ============ */
.exm-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}
.exm-icon--sm { width: 16px; height: 16px; }
.exm-icon--xs { width: 14px; height: 14px; }
.exm-icon--fill { fill: currentColor; }

/* ============ Metrics ============ */
.exm-metrics {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
}
@media (min-width: 640px) {
  .exm-metrics { grid-template-columns: repeat(2, 1fr); }
}
@media (min-width: 1024px) {
  .exm-metrics { grid-template-columns: repeat(3, 1fr); }
}

.exm-metric {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border: 1px solid var(--c-border);
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}
.exm-metric__label {
  font-size: 12px;
  color: var(--c-muted);
}
.exm-metric__value {
  margin: 4px 0;
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 24px;
  font-weight: 700;
  color: var(--c-text);
}
.exm-metric__value--purple { color: var(--c-purple); }
.exm-metric__note {
  font-family: inherit;
  font-size: 12px;
  font-weight: 400;
  color: var(--c-muted);
}
.exm-metric__note--blue { color: var(--c-blue); }
.exm-metric__note--green { color: var(--c-green); }
.exm-metric__foot {
  font-size: 10px;
  color: var(--c-faint);
}
.exm-metric__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  border-radius: 12px;
  background: var(--c-blue-bg);
  color: var(--c-blue);
}
.exm-metric__icon--purple { color: var(--c-purple); }

/* ============ Section head ============ */
.exm-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.exm-section__head {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
@media (min-width: 640px) {
  .exm-section__head {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }
}
.exm-section__title {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.exm-section__bar {
  width: 6px;
  height: 16px;
  border-radius: 999px;
  background: var(--c-sky);
}
.exm-section__title h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--c-text);
}
.exm-section__hint {
  font-size: 12px;
  color: var(--c-muted);
}

/* ---- Tabs ---- */
.exm-tabs {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
  background: #fff;
  font-size: 12px;
}
.exm-tabs__label {
  padding: 0 8px;
  color: var(--c-muted);
}
.exm-tab {
  padding: 4px 12px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: var(--c-muted);
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease;
}
.exm-tab:hover { color: var(--c-text); }
.exm-tab.is-active {
  background: var(--c-sky);
  color: var(--c-blue);
}

/* ============ Exam cards ============ */
.exm-cards {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.exm-card {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 16px;
  padding: 20px;
  border: 1px solid var(--c-border);
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
  transition: border-color 0.25s ease, background-color 0.25s ease;
}
@media (min-width: 768px) {
  .exm-card {
    flex-direction: row;
    align-items: center;
  }
}
.exm-card:hover {
  border-color: var(--c-sky);
  background: var(--c-blue-bg);
}

.exm-card__info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-width: 672px;
}
.exm-card__tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}
.exm-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 600;
}
.exm-badge--live {
  background: var(--c-blue-bg);
  color: var(--c-blue);
  font-weight: 700;
}
.exm-badge--wait {
  background: var(--c-border-soft);
  color: var(--c-muted);
}
.exm-badge--anytime {
  background: var(--c-blue-bg);
  color: var(--c-blue);
}
@keyframes exm-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.55; }
}

.exm-card__title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--c-text);
}
.exm-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 20px;
  font-size: 12px;
  color: var(--c-muted);
}
.exm-card__meta strong {
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-weight: 700;
  color: var(--c-text-sub);
}

/* ---- Card actions ---- */
.exm-card__actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
}
@media (min-width: 640px) {
  .exm-card__actions { align-items: flex-end; }
}

.exm-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 24px;
  border: 0;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: background-color 0.2s ease, transform 0.2s ease;
}
.exm-btn--primary {
  background: var(--c-sky);
  color: #fff;
  box-shadow: 0 6px 14px rgba(15, 23, 42, 0.12);
}
.exm-btn--primary:hover {
  background: var(--c-sky-hover);
  transform: scale(1.02);
}
.exm-btn--soft {
  padding: 10px 20px;
  background: var(--c-sky);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.05);
}
.exm-btn--soft:hover { background: var(--c-sky-hover); }
.exm-btn--disabled {
  padding: 10px 20px;
  border: 1px solid var(--c-border);
  background: var(--c-border-soft);
  color: var(--c-muted);
  font-weight: 600;
  cursor: not-allowed;
}
.exm-btn--close {
  width: 100%;
  margin-top: 20px;
  padding: 8px 0;
  background: var(--c-sky);
  color: var(--c-blue);
  font-weight: 600;
}
.exm-btn--close:hover { background: var(--c-sky-hover); }

/* ============ Panel (past records) ============ */
.exm-panel {
  padding: 20px;
  border: 1px solid var(--c-border);
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.exm-panel__head {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--c-border);
}
@media (min-width: 640px) {
  .exm-panel__head {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }
}
.exm-panel__title {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: var(--c-text);
}
.exm-panel__desc {
  margin: 2px 0 0;
  font-size: 12px;
  color: var(--c-muted);
}
.exm-panel__stats {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: var(--c-muted);
}
.exm-stat {
  font-family: 'SFMono-Regular', Consolas, monospace;
}
.exm-stat--dark { color: var(--c-text); }
.exm-stat--blue { color: var(--c-blue); }
.exm-stat--green { color: var(--c-green); }

/* ============ Table ============ */
.exm-table-wrap {
  overflow-x: auto;
}
.exm-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;
  text-align: left;
}
.exm-table thead tr {
  border-bottom: 1px solid var(--c-border);
}
.exm-table th {
  padding: 10px 12px;
  font-weight: 600;
  color: var(--c-muted);
}
.exm-table tbody tr {
  border-bottom: 1px solid var(--c-border-soft);
  transition: background-color 0.2s ease;
}
.exm-table tbody tr:last-child { border-bottom: 0; }
.exm-table tbody tr:hover { background: var(--c-bg-soft); }
.exm-table td {
  padding: 12px;
}
.exm-table__right { text-align: right; }
.exm-table__subject {
  font-weight: 700;
  color: var(--c-text);
}
.exm-table__mono {
  font-family: 'SFMono-Regular', Consolas, monospace;
  color: var(--c-muted);
}
.exm-table__score {
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 16px;
  font-weight: 700;
  color: var(--c-blue);
}
.exm-table__empty {
  font-size: 11px;
  color: var(--c-faint);
}

.exm-pass {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  border-radius: 999px;
  background: #ecfdf5;
  color: #047857;
  font-size: 10px;
  font-weight: 600;
}
.exm-pass__dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #10b981;
}

.exm-link {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--c-blue);
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}
.exm-link:hover { text-decoration: underline; }

/* ============ Modal ============ */
.exm-modal {
  position: fixed;
  inset: 0;
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(4px);
}
.exm-modal__box {
  width: 100%;
  max-width: 512px;
  padding: 24px;
  border: 1px solid var(--c-border);
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 25px 50px rgba(15, 23, 42, 0.25);
}
.exm-modal__title {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 700;
  color: var(--c-text);
}
.exm-modal__subject {
  margin-bottom: 16px;
  font-size: 12px;
  font-weight: 600;
  color: var(--c-blue);
}
.exm-modal__list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  font-size: 12px;
}
.exm-wrong {
  padding: 12px;
  border: 1px solid #fee2e2;
  border-radius: 12px;
  background: #fef2f2;
}
.exm-wrong__q {
  margin-bottom: 4px;
  font-weight: 700;
  color: #991b1b;
}
.exm-wrong__a {
  font-size: 11px;
  color: #b91c1c;
}
</style>