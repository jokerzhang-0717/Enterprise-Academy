<template>
  <div class="modal-backdrop" @click.self="$emit('close')">
    <div class="modal grading-modal">
      <div class="modal-head">
        <h2 class="modal-title">考试阅卷</h2>
        <button type="button" class="modal-close" @click="$emit('close')"><Close /></button>
      </div>

      <div class="modal-body">
        <p class="grading-subtitle">{{ exam.title }}</p>

        <div v-if="loading" class="grading-state">加载中…</div>
        <div v-else-if="error" class="grading-state grading-state--error">{{ error }}</div>

        <div v-else class="grading-layout">
          <aside class="grading-list">
            <div class="grading-list__head">待评卷 {{ attempts.length }} 份</div>
            <button
              v-for="item in attempts"
              :key="item.id"
              type="button"
              class="grading-list__item"
              :class="{ 'is-active': selected && selected.id === item.id }"
              @click="selectAttempt(item)"
            >
              <strong>{{ item.userName || `用户 #${item.userId}` }}</strong>
              <span>{{ item.employeeNo || '' }} · 第 {{ item.attemptNo }} 次</span>
              <span>交卷 {{ item.submittedAt || '—' }}</span>
              <span>客观题 {{ item.objectiveScore ?? 0 }} 分 · 当前 {{ item.totalScore ?? 0 }} 分</span>
            </button>
            <div v-if="!attempts.length" class="grading-list__empty">暂无待评卷记录</div>
          </aside>

          <section class="grading-detail">
            <template v-if="selected">
              <div class="grading-detail__head">
                <strong>{{ selected.userName || `用户 #${selected.userId}` }}</strong>
                <span>第 {{ selected.attemptNo }} 次作答</span>
              </div>

              <div v-if="detailLoading" class="grading-state">答卷加载中…</div>
              <template v-else>
                <div v-if="!subjectiveRows.length" class="grading-state">
                  本卷没有主观题，无需人工评分
                </div>

                <div
                  v-for="answer in subjectiveRows"
                  :key="rowKey(answer)"
                  class="grading-answer"
                >
                  <div class="grading-answer__title">
                    <span>{{ answer.questionNo }}. {{ answer.title }}</span>
                    <span>满分 {{ answer.score }} 分</span>
                  </div>
                  <div class="grading-answer__ref">
                    <span>参考答案</span>
                    <p>{{ answer.referenceAnswer || '—' }}</p>
                  </div>
                  <div class="grading-answer__text">
                    <span>学员作答</span>
                    <p>{{ answer.answerText || '（未作答）' }}</p>
                  </div>
                  <label class="grading-answer__score">
                    <span>本题得分</span>
                    <input
                      v-model.number="scores[rowKey(answer)]"
                      type="number"
                      min="0"
                      :max="answer.score"
                      step="0.5"
                    />
                    <span>/ {{ answer.score }}</span>
                  </label>
                </div>

                <div class="grading-summary">
                  <span>客观题 {{ objectiveScore }} 分</span>
                  <span>主观题 {{ subjectiveTotal }} 分</span>
                  <strong>合计 {{ grandTotal }} 分</strong>
                </div>

                <p v-if="gradeError" class="grading-error">{{ gradeError }}</p>

                <div class="grading-actions">
                  <button type="button" class="btn btn--ghost" @click="fillFull">主观题全满分</button>
                  <button type="button" class="btn btn--primary" :disabled="saving" @click="submitGrade">
                    {{ saving ? '提交中…' : '提交评分' }}
                  </button>
                </div>
              </template>
            </template>

            <div v-else class="grading-state">请选择左侧考生查看答卷</div>
          </section>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { Close } from '@element-plus/icons-vue';
import {
  fetchAdminExamAttempt,
  fetchAdminExamAttempts,
  fetchAdminQuestions,
  gradeAdminExamAttempt,
} from '../api/index.js';
import { useToast } from '../composables/useToast.js';

const props = defineProps({
  exam: { type: Object, required: true },
});
defineEmits(['close']);

const { show: showToast } = useToast();

const attempts = ref([]);
const loading = ref(true);
const error = ref('');
const selected = ref(null);
const detail = ref(null);
const detailLoading = ref(false);
const scores = ref({});
const saving = ref(false);
const gradeError = ref('');

const examId = computed(() => props.exam.numericId ?? props.exam.id);

// 题目列表接口返回 type 标签字符串（application），答卷明细返回数字 4
const isSubjective = (type) => Number(type) === 4 || type === 'application' || type === 'essay';

const subjectiveRows = ref([]);
const rowKey = (row) => (row.id != null ? `a-${row.id}` : `q-${row.questionId}`);
const objectiveScore = computed(() => Number(selected.value?.objectiveScore || 0));
const subjectiveTotal = computed(() =>
  subjectiveRows.value.reduce((sum, row) => sum + Number(scores.value[rowKey(row)] || 0), 0),
);
const grandTotal = computed(() => Number((objectiveScore.value + subjectiveTotal.value).toFixed(1)));

const loadAttempts = async () => {
  loading.value = true;
  error.value = '';
  try {
    const data = await fetchAdminExamAttempts({ examId: examId.value, status: 2 });
    attempts.value = Array.isArray(data) ? data : (data?.records || []);
  } catch (loadError) {
    error.value = loadError?.message || '待评卷列表加载失败';
  } finally {
    loading.value = false;
  }
};

const selectAttempt = async (item) => {
  selected.value = item;
  detail.value = null;
  subjectiveRows.value = [];
  gradeError.value = '';
  detailLoading.value = true;
  try {
    const [data, questions] = await Promise.all([
      fetchAdminExamAttempt(item.id),
      fetchAdminQuestions(examId.value),
    ]);
    detail.value = data;

    // 未作答的主观题在 attempt_answer 中没有行，需按试卷题目补出来才能评分
    const answerByQuestion = new Map();
    for (const answer of data?.answers || []) {
      answerByQuestion.set(String(answer.questionId), answer);
    }
    subjectiveRows.value = (questions || [])
      .filter((question) => isSubjective(question.type))
      .map((question) => {
        const answer = answerByQuestion.get(String(question.numericId ?? question.id));
        return {
          id: answer?.id ?? null,
          questionId: question.numericId ?? question.id,
          questionNo: answer?.questionNo ?? question.questionNo ?? '',
          title: question.title || answer?.title || '',
          score: Number(question.score || 0),
          referenceAnswer: question.referenceAnswer || answer?.referenceAnswer || '',
          answerText: answer?.answerText || '',
          scoreGained: Number(answer?.scoreGained || 0),
        };
      });

    const initial = {};
    for (const row of subjectiveRows.value) {
      initial[rowKey(row)] = row.scoreGained;
    }
    scores.value = initial;
  } catch (loadError) {
    gradeError.value = loadError?.message || '答卷加载失败';
  } finally {
    detailLoading.value = false;
  }
};

const fillFull = () => {
  for (const row of subjectiveRows.value) {
    scores.value = { ...scores.value, [rowKey(row)]: Number(row.score || 0) };
  }
};

const submitGrade = async () => {
  if (!selected.value || saving.value) return;
  saving.value = true;
  gradeError.value = '';
  try {
    await gradeAdminExamAttempt(selected.value.id, {
      answers: subjectiveRows.value.map((row) => ({
        ...(row.id != null ? { answerId: row.id } : { questionId: row.questionId }),
        scoreGained: Number(scores.value[rowKey(row)] || 0),
      })),
    });
    showToast('评分已提交，学员可查看最终成绩', 'success');
    selected.value = null;
    detail.value = null;
    await loadAttempts();
  } catch (submitError) {
    gradeError.value = submitError?.message || '评分提交失败，请稍后重试';
  } finally {
    saving.value = false;
  }
};

onMounted(loadAttempts);
</script>

<style scoped>
/* 组件内部的 modal 结构样式必须自带：父组件的 scoped 样式只作用在子组件根节点上 */
.grading-modal {
  display: flex;
  flex-direction: column;
  width: min(1160px, calc(100vw - 40px));
  max-height: calc(100vh - 40px);
  overflow: hidden;
  background: var(--app-surface, #fff);
  border-radius: 16px;
  box-shadow: 0 24px 48px rgba(15, 30, 51, 0.25);
}
.modal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
  padding: 18px 24px;
  border-bottom: 1px solid var(--app-border-soft, #ebeef5);
}
.modal-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--app-heading, #1f2937);
}
.modal-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  padding: 0;
  color: var(--app-text-4, #8a94a6);
  background: transparent;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
.modal-close:hover {
  color: var(--app-heading, #1f2937);
  background: var(--app-surface-3, #f2f5f9);
}
.modal-close svg { width: 18px; height: 18px; }
.modal-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 20px 24px 24px;
}

.grading-subtitle {
  margin: 0 0 18px;
  font-size: 14px;
  font-weight: 600;
  color: var(--app-text-2, #475569);
}

.grading-state {
  padding: 56px 16px;
  text-align: center;
  font-size: 14px;
  color: var(--app-text-4, #8a94a6);
}
.grading-state--error { color: #e05252; }

.grading-layout {
  display: grid;
  grid-template-columns: minmax(240px, 300px) minmax(0, 1fr);
  gap: 20px;
  align-items: start;
}
@media (max-width: 860px) {
  .grading-layout { grid-template-columns: 1fr; }
}

.grading-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.grading-list__head {
  font-size: 13px;
  font-weight: 700;
  color: var(--app-heading, #1f2937);
}
.grading-list__item {
  display: flex;
  flex-direction: column;
  gap: 5px;
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--app-border, #e2e8f0);
  border-radius: 10px;
  background: var(--app-surface, #fff);
  text-align: left;
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s;
}
.grading-list__item:hover { border-color: var(--app-accent, #3b82f6); }
.grading-list__item.is-active {
  border-color: var(--app-accent, #3b82f6);
  background: var(--app-accent-soft, #eff6ff);
  box-shadow: inset 3px 0 0 var(--app-accent, #3b82f6);
}
.grading-list__item strong { font-size: 14px; color: var(--app-heading, #1f2937); }
.grading-list__item span { font-size: 12px; color: var(--app-text-3, #64748b); }
.grading-list__empty {
  padding: 32px 16px;
  text-align: center;
  font-size: 13px;
  color: var(--app-text-4, #8a94a6);
  border: 1px dashed var(--app-border, #e2e8f0);
  border-radius: 10px;
}

.grading-detail {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-width: 0;
}
.grading-detail__head {
  display: flex;
  gap: 12px;
  align-items: baseline;
  padding: 0 2px 12px;
  border-bottom: 1px solid var(--app-border-soft, #ebeef5);
}
.grading-detail__head strong { font-size: 16px; color: var(--app-heading, #1f2937); }
.grading-detail__head span { font-size: 13px; color: var(--app-text-3, #64748b); }

.grading-answer {
  padding: 18px;
  border: 1px solid var(--app-border, #e2e8f0);
  border-radius: 12px;
  background: var(--app-surface, #fff);
}
.grading-answer__title {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  font-size: 15px;
  line-height: 1.6;
  font-weight: 700;
  color: var(--app-heading, #1f2937);
}
.grading-answer__title span:last-child {
  flex-shrink: 0;
  font-size: 13px;
  color: var(--app-text-3, #64748b);
  font-weight: 600;
}
.grading-answer__ref,
.grading-answer__text {
  margin-top: 12px;
  padding: 12px 14px;
  border-radius: 10px;
  font-size: 14px;
  line-height: 1.8;
  color: var(--app-text-2, #334155);
}
.grading-answer__ref {
  background: #f4f8ff;
  border-left: 3px solid #6aa5f5;
}
.grading-answer__text {
  background: #f6fbf7;
  border-left: 3px solid #52b788;
}
.grading-answer__ref span,
.grading-answer__text span {
  display: block;
  margin-bottom: 4px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.02em;
  color: var(--app-text-3, #64748b);
}
.grading-answer__ref p,
.grading-answer__text p {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}
.grading-answer__score {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 14px;
  font-size: 14px;
  font-weight: 600;
  color: var(--app-text-2, #334155);
}
.grading-answer__score input {
  width: 104px;
  height: 38px;
  padding: 6px 10px;
  font-size: 15px;
  font-weight: 700;
  color: var(--app-heading, #1f2937);
  border: 1px solid var(--app-border, #cbd5e1);
  border-radius: 8px;
  background: var(--app-surface, #fff);
}
.grading-answer__score input:focus {
  outline: none;
  border-color: var(--app-accent, #3b82f6);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
}

.grading-summary {
  position: sticky;
  bottom: 0;
  z-index: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 8px 24px;
  padding: 14px 18px;
  border: 1px solid var(--app-border, #e2e8f0);
  border-radius: 12px;
  background: var(--app-surface-2, #f5f7fa);
  font-size: 14px;
  color: var(--app-text-2, #334155);
}
.grading-summary strong {
  color: var(--app-accent-deep, #2563eb);
  font-size: 17px;
}

.grading-error {
  margin: 0;
  font-size: 13px;
  color: #e05252;
}

.grading-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 2px;
}
.grading-actions .btn {
  min-height: 38px;
  padding: 8px 18px;
  font-size: 14px;
  border-radius: 8px;
}
</style>
