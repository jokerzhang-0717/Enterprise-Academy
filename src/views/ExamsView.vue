<template>
  <div class="exm-page">
    <!-- 1. Overview Metrics -->
    <div class="exm-metrics">
      <!-- 待参加考核 -->
      <div class="exm-metric">
        <div class="exm-metric__body">
          <div class="exm-metric__label">待参加考核</div>
          <div class="exm-metric__value">
            {{ metrics.pendingCount }} <span class="exm-metric__note exm-metric__note--blue">{{ metrics.inProgressCount }}场正在进行</span>
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
            {{ certificateCount }} <span class="exm-metric__note exm-metric__note--green">获{{ certificateCount }}份电子徽章</span>
          </div>
          <div class="exm-metric__foot">认证率 {{ metrics.passRate }}% 达标</div>
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
            {{ metrics.avgPastScore }} <span class="exm-metric__note">通过率 {{ metrics.passRate }}%</span>
          </div>
          <div class="exm-metric__foot">本季度综合考核</div>
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
          <h2>待参加考试安排</h2>
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
            全部安排 ({{ examsInTab('all').length }})
          </button>
          <button
            type="button"
            class="exm-tab"
            :class="filterTab === 'in_progress' ? 'is-active' : ''"
            @click="setFilterTab('in_progress')"
          >
            进行中 ({{ examsInTab('in_progress').length }})
          </button>
          <button
            type="button"
            class="exm-tab"
            :class="filterTab === 'not_started' ? 'is-active' : ''"
            @click="setFilterTab('not_started')"
          >
            未开始 ({{ examsInTab('not_started').length }})
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
          :class="{ 'exm-card--search-target': searchTargetId === `exam-${exam.id}` }"
        >
          <!-- 左侧信息 -->
          <div class="exm-card__info">
            <div class="exm-card__tags">
              <span v-if="exam.status === 'in_progress'" class="exm-badge exm-badge--live">
                考试进行中 · 可进入作答
              </span>
              <span v-else-if="exam.status === 'not_started'" class="exm-badge exm-badge--wait">
                未开始 · 请稍后
              </span>
              <span v-else-if="exam.status === 'completed'" class="exm-badge exm-badge--wait">
                本场考核已完成
              </span>
              <span v-else class="exm-badge exm-badge--wait">
                考试状态待确认
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
              @click="$emit('start-exam', exam)"
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

            <!-- 已完成 -->
            <button
              v-else-if="exam.status === 'completed'"
              type="button"
              class="exm-btn exm-btn--disabled"
              disabled
            >
              <Award class="exm-icon exm-icon--sm" />
              <span>已完成</span>
            </button>

            <button
              v-else
              type="button"
              class="exm-btn exm-btn--disabled"
              disabled
            >
              <Clock class="exm-icon exm-icon--sm" />
              <span>暂不可考</span>
            </button>
          </div>
        </article>
      </div>
    </section>

    <!-- 3. Past Exam Records (已移除) -->
    <!-- 4. Wrong Questions Modal (已移除) -->
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, watch } from 'vue';
import {
  FileCheck2,
  Clock,
  Award,
  FileText,
  Layers
} from 'lucide-vue-next';
import { fetchExams, fetchMyCertificates } from '../api/index.js';

const props = defineProps({
  searchTargetId: {
    type: String,
    default: '',
  },
});

defineEmits(['start-exam']);

const exams = ref([]);
const metrics = ref({ pendingCount: 0, inProgressCount: 0, finishedCount: 0, avgPastScore: 0, passRate: 0 });
const certificateCount = ref(0);

const filterTab = ref('all');

onMounted(async () => {
  const [examsData, certificates] = await Promise.all([
    fetchExams().catch(() => null),
    fetchMyCertificates().catch(() => []),
  ]);
  if (examsData) {
    exams.value = examsData.assignments || [];
    metrics.value = examsData.metrics || metrics.value;
  }
  certificateCount.value = (certificates || []).length;
});

/* ---------- 筛选（纯函数，无副作用） ----------
   可进入作答的状态归入「进行中」，其余归入「未开始」 */
const examsInTab = (tab) => exams.value.filter(exam => {
  if (tab === 'in_progress') return exam.status === 'in_progress';
  if (tab === 'not_started') return exam.status === 'not_started' || exam.status === 'completed';
  return true;
});

const filteredExams = computed(() => examsInTab(filterTab.value));

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
  [() => props.searchTargetId, exams],
  ([targetId, records]) => {
    if (!targetId) return;

    // 只在当前筛选不包含目标时才重置，且重置后值不变就不再赋值
    if (filterTab.value !== 'all') {
      filterTab.value = 'all';
    }

    if (records.length) scrollToTarget(targetId);
  },
  { immediate: true, deep: true },
);
</script>

<style scoped>
/* ============ Design tokens ============ */
.exm-page {
  --c-text: var(--app-text);
  --c-text-sub: var(--app-text-2);
  --c-text-soft: var(--app-text-3);
  --c-muted: var(--app-text-4);
  --c-faint: var(--app-text-5);
  --c-border: var(--app-border);
  --c-border-soft: var(--app-surface-3);
  --c-bg-soft: var(--app-surface-2);
  --c-blue: var(--app-accent-deep);
  --c-blue-bg: var(--app-accent-soft);
  --c-sky: var(--app-accent);
  --c-sky-hover: var(--app-accent-hover-strong);
  --c-purple: var(--app-violet-fg);
  --c-green: var(--app-emerald-strong);

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
  background: var(--app-surface);
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
  background: var(--app-surface);
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
  color: var(--app-on-accent-solid);
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
  background: var(--app-surface);
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
.exm-card--search-target {
  border-color: var(--c-sky);
  box-shadow: 0 0 0 3px var(--app-accent-soft);
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
  color: var(--app-on-fill);
  box-shadow: 0 6px 14px rgba(15, 23, 42, 0.12);
}
.exm-btn--primary:hover {
  background: var(--c-sky-hover);
  transform: scale(1.02);
}
.exm-btn--soft {
  padding: 10px 20px;
  background: var(--c-sky);
  color: var(--app-on-fill);
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
  color: var(--app-on-accent-solid);
  font-weight: 600;
}
.exm-btn--close:hover { background: var(--c-sky-hover); }

/* ============ Panel (已移除) ============ */
</style>