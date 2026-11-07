<template>
  <div class="att-page">
    <!-- Toast -->
    <Transition name="attendance-toast">
      <div v-if="notice" role="status" class="att-toast">
        <span class="att-toast__icon">
          <CircleCheck class="att-icon" aria-hidden="true" />
        </span>
        <div class="att-toast__body">
          <p class="att-toast__title">{{ notice.title }}</p>
          <p v-if="notice.message" class="att-toast__msg">{{ notice.message }}</p>
        </div>
        <button aria-label="关闭提示" class="att-toast__close" @click="clearNotice">
          <X class="att-icon" aria-hidden="true" />
        </button>
      </div>
    </Transition>

    <!-- 1. Header -->
    <header class="att-header">
      <div class="att-header__meta">
        <span class="att-tag">HYBRID LMS ATTENDANCE</span>
        <span v-if="employeeId" class="att-header__emp">企业工号: {{ employeeId }}</span>
      </div>
      <h1 class="att-header__title">培训考勤打卡与请假联动中心</h1>
      <p class="att-header__desc">
        记录今日签到与签退状态，管理培训相关请假申请及审批进度。
      </p>
    </header>

    <!-- 2. Quick Punch Box -->
    <section class="att-panel att-punch">
      <div class="att-punch__head">
        <div class="att-punch__label">
          <LockKeyhole class="att-icon att-icon--blue" aria-hidden="true" />
          <span>快速打卡通道</span>
        </div>
        <div class="att-punch__clock">{{ currentTime }}</div>
      </div>

      <div class="att-punch__grid">
        <!-- 签到状态 -->
        <div v-if="isCheckedIn" class="att-punch__card att-punch__card--done">
          <span class="att-punch__badge att-punch__badge--done">
            <CircleCheck class="att-icon" aria-hidden="true" />
          </span>
          <div>
            <p class="att-punch__state">今日已签到</p>
            <p class="att-punch__time">
              {{ checkedInAt }} <span class="att-punch__time-note">签到成功</span>
            </p>
          </div>
        </div>
        <button
          v-else
          type="button"
          class="att-punch__card att-punch__btn is-active"
          @click="handlePunchIn"
        >
          <span class="att-punch__btn-main">
            <span class="att-punch__badge is-active">
              <CircleCheck class="att-icon" aria-hidden="true" />
            </span>
            <span class="att-punch__btn-text">
              <span class="att-punch__state">今日尚未签到</span>
              <span class="att-punch__time">点击完成签到</span>
            </span>
          </span>
          <ChevronRight class="att-icon att-punch__arrow" aria-hidden="true" />
        </button>

        <!-- 签退 -->
        <div v-if="isCheckedOut" class="att-punch__card att-punch__card--done">
          <span class="att-punch__badge att-punch__badge--done">
            <CircleCheck class="att-icon" aria-hidden="true" />
          </span>
          <div>
            <p class="att-punch__state">今日已签退</p>
            <p class="att-punch__time">
              {{ checkedOutAt || '—' }}
              <span class="att-punch__time-note">签退成功</span>
            </p>
          </div>
        </div>
        <button
          v-else
          type="button"
          class="att-punch__card att-punch__btn is-active"
          @click="handlePunchOut"
        >
          <span class="att-punch__btn-main">
            <span class="att-punch__badge is-active">
              <LogOut class="att-icon" aria-hidden="true" />
            </span>
            <span class="att-punch__btn-text">
              <span class="att-punch__state">完成今日培训</span>
              <span class="att-punch__time">点击签退</span>
            </span>
          </span>
          <ChevronRight class="att-icon att-punch__arrow" aria-hidden="true" />
        </button>
      </div>
    </section>

    <!-- 3. Leave Form & Records -->
    <section class="att-columns">
      <!-- Left: 请假申请 -->
      <div class="att-panel att-col">
        <div class="att-panel__head">
          <div class="att-panel__title">
            <CalendarDays class="att-icon att-icon--purple" aria-hidden="true" />
            <h3>发起培训请假申请</h3>
          </div>
          <span class="att-chip">线上闭环审批</span>
        </div>

        <form novalidate class="att-form" @submit.prevent="handleSubmitLeave">
          <div class="att-form__row">
            <div class="att-field">
              <label class="att-field__label" for="leave-type">请假类型</label>
              <input
                id="leave-type"
                v-model="leaveType"
                type="text"
                maxlength="50"
                placeholder="请填写请假类型"
                class="att-input"
                :class="leaveErrors.type ? 'is-error' : ''"
                :aria-invalid="Boolean(leaveErrors.type)"
                aria-describedby="leave-type-error"
                @input="leaveErrors.type = ''"
              />
              <p v-if="leaveErrors.type" id="leave-type-error" class="att-field__error">{{ leaveErrors.type }}</p>
            </div>

            <div class="att-field">
              <label class="att-field__label" for="leave-time">请假时间</label>
              <div class="att-input-wrap">
                <input
                  id="leave-time"
                  ref="leaveTimeInput"
                  v-model="leaveTime"
                  type="date"
                  class="att-input att-date-input"
                  :class="leaveErrors.time ? 'is-error' : ''"
                  :aria-invalid="Boolean(leaveErrors.time)"
                  aria-describedby="leave-time-error"
                  @change="leaveErrors.time = ''"
                />
                <button type="button" aria-label="选择请假日期" class="att-input-suffix" @click="openLeaveDatePicker">
                  <CalendarDays class="att-icon" aria-hidden="true" />
                </button>
              </div>
              <p v-if="leaveErrors.time" id="leave-time-error" class="att-field__error">{{ leaveErrors.time }}</p>
            </div>

            <div class="att-field">
              <label class="att-field__label" for="leave-days">请假天数</label>
              <input
                id="leave-days"
                v-model.trim="leaveDays"
                type="text"
                inputmode="decimal"
                maxlength="10"
                pattern="[0-9]+([.][0-9]+)?"
                title="请填写大于 0 的数字，可使用小数"
                placeholder="请输入天数"
                class="att-input"
                :class="leaveErrors.days ? 'is-error' : ''"
                :aria-invalid="Boolean(leaveErrors.days)"
                aria-describedby="leave-days-error"
                @input="leaveErrors.days = ''"
              />
              <p v-if="leaveErrors.days" id="leave-days-error" class="att-field__error">{{ leaveErrors.days }}</p>
            </div>
          </div>

          <div class="att-field">
            <label class="att-field__label" for="leave-reason">请假原因</label>
            <textarea
              id="leave-reason"
              v-model="leaveReason"
              rows="4"
              placeholder="请详细说明请假原因..."
              class="att-textarea"
              :class="leaveErrors.reason ? 'is-error' : ''"
              :aria-invalid="Boolean(leaveErrors.reason)"
              aria-describedby="leave-reason-error"
              @input="leaveErrors.reason = ''"
            />
            <p v-if="leaveErrors.reason" id="leave-reason-error" class="att-field__error">{{ leaveErrors.reason }}</p>
          </div>

          <button type="submit" class="att-btn-primary">
            <Send class="att-icon att-icon--sm" aria-hidden="true" />
            <span>提交请假申请</span>
          </button>
        </form>
      </div>

      <!-- Right: 审批记录 -->
      <div class="att-panel att-col">
        <div class="att-panel__head">
          <div class="att-panel__title">
            <FileText class="att-icon att-icon--blue" aria-hidden="true" />
            <h3>请假审批记录</h3>
            <span class="att-count">{{ leaveRecords.length }}</span>
          </div>
        </div>

        <div class="att-records">
          <article v-for="req in paginatedLeaveRecords" :key="req.id" class="att-record">
            <div class="att-record__top">
              <div class="att-record__tags">
                <span class="att-record__type">{{ req.type }}</span>
              </div>
              <span
                class="att-record__status"
                :class="{
                  'is-pending': req.status === 'pending',
                  'is-passed': req.status === 'approved',
                  'is-rejected': req.status === 'rejected'
                }"
              >
                {{
                  req.status === 'pending'
                    ? '● 审批中'
                    : req.status === 'rejected'
                      ? '✕ 已驳回'
                      : '✓ 已通过'
                }}
              </span>
            </div>

            <h4 class="att-record__title">{{ req.title }}</h4>

            <div class="att-record__time">
              <Clock class="att-icon att-icon--xs" aria-hidden="true" />
              <span>请假时间: {{ req.timeRange }}</span>
            </div>
            <div v-if="req.days" class="att-record__days">
              请假天数: <strong>{{ req.days }} 天</strong>
            </div>

            <p class="att-record__reason">请假原因: {{ req.reason }}</p>
          </article>
        </div>

        <div v-if="leaveRecords.length" class="att-pagination">
          <span class="att-pagination__info">
            显示 {{ displayedRange.start }} - {{ displayedRange.end }} 条，共 {{ leaveRecords.length }} 条
          </span>
          <div v-if="totalPages > 1" class="att-pagination__btns">
            <button
              type="button"
              class="att-page-arrow"
              aria-label="上一页"
              :disabled="currentPage === 1"
              @click="currentPage--"
            >
              <ChevronLeft class="att-icon att-icon--sm" aria-hidden="true" />
            </button>
            <button
              v-for="page in totalPages"
              :key="page"
              type="button"
              class="att-page-num"
              :class="currentPage === page ? 'is-current' : ''"
              :aria-current="currentPage === page ? 'page' : undefined"
              @click="currentPage = page"
            >
              {{ page }}
            </button>
            <button
              type="button"
              class="att-page-arrow"
              aria-label="下一页"
              :disabled="currentPage === totalPages"
              @click="currentPage++"
            >
              <ChevronRight class="att-icon att-icon--sm" aria-hidden="true" />
            </button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue';
import {
  CalendarDays,
  ChevronLeft,
  ChevronRight,
  CircleCheck,
  Clock,
  FileText,
  LockKeyhole,
  LogOut,
  Send,
  X,
} from 'lucide-vue-next';
import { useAttendance } from '../composables/useAttendance.js';
import { useLeaveRequests, submitLeave } from '../composables/useLeaveRequests.js';

const props = defineProps({
  employeeId: {
    type: String,
    default: '',
  }
});

const formatClock = (date) => {
  const pad = (value) => String(value).padStart(2, '0');
  return `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
};
const currentTime = ref(formatClock(new Date()));
const {
  isCheckedIn,
  isCheckedOut,
  checkedInAt,
  checkedOutAt,
  checkIn,
  checkOut,
} = useAttendance();
const leaveRecords = useLeaveRequests();
const currentPage = ref(1);
const pageSize = 3;
const totalPages = computed(() => Math.ceil(leaveRecords.value.length / pageSize));
const paginatedLeaveRecords = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return leaveRecords.value.slice(start, start + pageSize);
});
const displayedRange = computed(() => ({
  start: leaveRecords.value.length ? (currentPage.value - 1) * pageSize + 1 : 0,
  end: Math.min(currentPage.value * pageSize, leaveRecords.value.length),
}));

const leaveType = ref('');
const leaveTime = ref('');
const leaveTimeInput = ref(null);
const leaveDays = ref('');
const leaveErrors = ref({ type: '', time: '', days: '', reason: '' });
const leaveReason = ref('');
const notice = ref(null);

let timer = null;
let noticeTimer = null;
onMounted(() => {
  timer = setInterval(() => {
    currentTime.value = formatClock(new Date());
  }, 1000);
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
  if (noticeTimer) clearTimeout(noticeTimer);
});

const punching = ref(false);

const handlePunchOut = async () => {
  if (isCheckedOut.value || punching.value) return;
  punching.value = true;
  try {
    await checkOut();
    showNotice('签退成功', checkedOutAt.value ? `签退时间：${checkedOutAt.value}` : '');
  } catch (err) {
    showNotice('签退失败', err?.message || '请先完成签到');
  } finally {
    punching.value = false;
  }
};

const handlePunchIn = async () => {
  if (isCheckedIn.value || punching.value) return;
  punching.value = true;
  try {
    await checkIn();
    showNotice('签到成功', checkedInAt.value ? `签到时间：${checkedInAt.value}` : '');
  } catch (err) {
    showNotice('签到失败', err?.message || '请稍后重试');
  } finally {
    punching.value = false;
  }
};

const leaveSubmitting = ref(false);

const handleSubmitLeave = async () => {
  if (leaveSubmitting.value) return;
  const days = Number(leaveDays.value);
  leaveErrors.value = {
    type: leaveType.value.trim() ? '' : '请填写请假类型。',
    time: leaveTime.value ? '' : '请选择请假时间。',
    days: Number.isFinite(days) && days > 0 ? '' : '请填写大于 0 的有效天数。',
    reason: leaveReason.value.trim() ? '' : '请填写请假原因。',
  };
  if (Object.values(leaveErrors.value).some(Boolean)) return;

  leaveSubmitting.value = true;
  try {
    await submitLeave({
      leaveType: leaveType.value.trim(),
      startDate: leaveTime.value,
      leaveDays: days,
      reason: leaveReason.value.trim(),
    });
    currentPage.value = 1;
    leaveType.value = '';
    leaveTime.value = '';
    leaveDays.value = '';
    leaveReason.value = '';
    showNotice('请假申请已提交', '可在下方请假记录中查看审批进度。');
  } catch (err) {
    showNotice('请假申请提交失败', err?.message || '请稍后重试');
  } finally {
    leaveSubmitting.value = false;
  }
};

const openLeaveDatePicker = () => {
  const input = leaveTimeInput.value;
  if (input && typeof input.showPicker === 'function') input.showPicker();
};

const showNotice = (title, message = '') => {
  notice.value = { title, message };
  if (noticeTimer) clearTimeout(noticeTimer);
  noticeTimer = setTimeout(clearNotice, 4000);
};

const clearNotice = () => {
  if (noticeTimer) clearTimeout(noticeTimer);
  notice.value = null;
};
</script>

<style scoped>
/* ============ Design tokens ============ */
.att-page {
  --c-text: var(--app-text);
  --c-text-sub: var(--app-text-3);
  --c-muted: var(--app-text-4);
  --c-faint: var(--app-text-5);
  --c-border: var(--app-border);
  --c-border-strong: var(--app-border-strong);
  --c-bg-soft: var(--app-surface-2);
  --c-blue: var(--app-accent-deep);
  --c-blue-bg: var(--app-accent-soft);
  --c-sky: var(--app-accent);
  --c-sky-hover: var(--app-accent-hover-strong);
  --c-purple: var(--app-violet);
  --c-purple-hover: var(--app-violet-strong);
  --c-purple-icon: var(--app-violet-fg);

  display: flex;
  flex-direction: column;
  gap: 24px;
  padding-bottom: 48px;
  font-family: 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', sans-serif;
}

/* 所有图标使用统一尺寸和线条粗细。 */
.att-icon {
  display: block;
  width: 18px;
  height: 18px;
  flex-shrink: 0;
  stroke-width: 1.8;
}
.att-icon--sm { width: 14px; height: 14px; }
.att-icon--xs { width: 12px; height: 12px; }
.att-icon--blue { color: var(--c-blue); }
.att-icon--purple { color: var(--c-purple-icon); }

/* ============ Toast ============ */
.att-toast {
  position: fixed;
  left: 50%;
  top: 80px;
  /* 入场动画的 translate(-50%,-8px) 只在 enter-from 帧生效,
     基础态必须自己带 -50%,否则动画结束后整块会向右偏移自身宽度的一半 */
  transform: translateX(-50%);
  z-index: 70;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  width: min(92vw, 520px);
  padding: 16px 20px;
  border: 1px solid var(--app-emerald-line);
  border-radius: 16px;
  background: var(--app-surface);
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.2), 0 0 0 1px var(--app-emerald-bg);
}
.att-toast__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  flex-shrink: 0;
  border-radius: 50%;
  background: var(--app-emerald-bg);
  color: var(--app-emerald-fg);
}
.att-toast__body { min-width: 0; flex: 1; }
.att-toast__title {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--c-text);
}
.att-toast__msg {
  margin: 6px 0 0;
  font-size: 13px;
  line-height: 20px;
  color: var(--c-text-sub);
}
.att-toast__close {
  padding: 6px;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: var(--c-muted);
  cursor: pointer;
}
.att-toast__close:hover {
  background: var(--app-surface-3);
  color: var(--c-text);
}

/* ============ Header ============ */
.att-header__meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.att-tag {
  padding: 2px 8px;
  border-radius: 4px;
  background: var(--c-blue-bg);
  color: var(--c-blue);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.04em;
}
.att-header__emp {
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 12px;
  color: var(--c-muted);
}
.att-header__title {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--c-text);
}
.att-header__desc {
  margin: 2px 0 0;
  font-size: 12px;
  color: var(--c-muted);
}

/* ============ Panels ============ */
.att-panel {
  background: var(--app-surface);
  border: 1px solid var(--c-border);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}
.att-panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 12px;
  margin-bottom: 16px;
  border-bottom: 1px solid var(--c-border);
}
.att-panel__title {
  display: flex;
  align-items: center;
  gap: 8px;
}
.att-panel__title h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--c-text);
}
.att-chip {
  padding: 2px 10px;
  border-radius: 999px;
  background: var(--c-bg-soft);
  font-size: 10px;
  color: var(--c-muted);
}
.att-count {
  min-width: 22px;
  padding: 1px 8px;
  border-radius: 999px;
  background: var(--c-blue-bg);
  color: var(--c-blue);
  font-size: 12px;
  font-weight: 700;
  text-align: center;
}

/* ============ Quick punch ============ */
.att-punch__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.att-punch__label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 700;
  color: var(--c-text);
}
.att-punch__clock {
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.08em;
  color: var(--c-blue);
}
.att-punch__grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
  padding: 8px 0;
}
@media (min-width: 640px) {
  .att-punch__grid { grid-template-columns: 1fr 1fr; }
}

.att-punch__card {
  display: flex;
  align-items: center;
  gap: 16px;
  min-height: 80px;
  padding: 16px 20px;
  border-radius: 16px;
  text-align: left;
}
.att-punch__card--done {
  border: 1px solid var(--app-emerald-line);
  background: linear-gradient(135deg, var(--app-emerald-soft) 0%, var(--app-surface) 100%);
}

.att-punch__badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  flex-shrink: 0;
  border-radius: 50%;
}
.att-punch__badge--done {
  background: var(--app-emerald-bg);
  color: var(--app-emerald-strong);
}
.att-punch__badge.is-done {
  background: var(--app-blue-100);
  color: var(--c-blue);
}
.att-punch__badge.is-active {
  background: rgba(255, 255, 255, 0.15);
  color: var(--app-on-scrim);
}

.att-punch__state {
  display: block;
  margin: 0;
  font-size: 12px;
  font-weight: 600;
  color: var(--app-emerald-deep);
}
.att-punch__time {
  display: block;
  margin: 4px 0 0;
  font-size: 14px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  color: var(--app-emerald-fg);
}
.att-punch__time-note {
  font-size: 11px;
  font-weight: 500;
}

/* 签退按钮 */
.att-punch__btn {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  width: 100%;
  border: 1px solid transparent;
  cursor: pointer;
  transition: background-color 0.2s ease;
}
.att-punch__btn-main {
  display: flex;
  align-items: center;
  gap: 16px;
}
.att-punch__btn-text { display: block; }
.att-punch__btn .att-punch__state { color: inherit; }
.att-punch__btn .att-punch__time { color: inherit; }

.att-punch__btn.is-active {
  border-color: var(--c-sky);
  background: var(--c-sky);
  color: var(--app-on-accent-solid);
}
.att-punch__btn.is-active:hover { background: var(--c-sky-hover); }
.att-punch__btn.is-active .att-punch__state,
.att-punch__btn.is-active .att-punch__time { color: var(--app-on-accent-solid); }

.att-punch__btn.is-done {
  border-color: var(--app-blue-line-200);
  background: var(--app-blue-50);
  color: var(--c-blue);
  cursor: default;
}
.att-punch__btn.is-done .att-punch__state,
.att-punch__btn.is-done .att-punch__time { color: var(--c-blue); }

.att-punch__btn:focus-visible {
  outline: none;
  box-shadow: 0 0 0 4px var(--app-blue-100);
}
.att-punch__arrow {
  color: rgba(255, 255, 255, 0.7);
  transition: transform 0.2s ease;
}
.att-punch__btn:hover .att-punch__arrow { transform: translateX(2px); }

/* ============ Two columns ============ */
.att-columns {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
  align-items: start;
}
@media (min-width: 1024px) {
  .att-columns { grid-template-columns: 1fr 1fr; }
}

/* ============ Form ============ */
.att-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
  font-size: 12px;
}
.att-form__row {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}
@media (min-width: 640px) {
  .att-form__row { grid-template-columns: repeat(3, 1fr); }
}

.att-field__label {
  display: block;
  margin-bottom: 4px;
  color: var(--c-muted);
  font-weight: 500;
}
.att-field__error {
  margin: 4px 0 0;
  font-size: 11px;
  color: var(--app-red-600);
}

.att-input,
.att-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--c-border-strong);
  border-radius: 12px;
  background: var(--c-bg-soft);
  font-size: 12px;
  color: var(--c-text);
  outline: none;
  transition: border-color 0.2s ease;
  box-sizing: border-box;
}
.att-textarea {
  padding: 12px;
  line-height: 1.625;
  resize: vertical;
  font-family: inherit;
}
.att-input:focus,
.att-textarea:focus { border-color: var(--c-sky); }
.att-input.is-error,
.att-textarea.is-error { border-color: var(--app-red-400); }
.att-input::placeholder,
.att-textarea::placeholder { color: var(--c-faint); }

.att-input-wrap { position: relative; }
.att-date-input {
  padding-right: 40px;
  cursor: pointer;
  appearance: none;
  -webkit-appearance: none;
}
.att-date-input::-webkit-calendar-picker-indicator {
  width: 0;
  opacity: 0;
}
.att-input-suffix {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  padding: 6px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: var(--c-muted);
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease;
}
.att-input-suffix:hover {
  background: var(--app-blue-75);
  color: var(--c-blue);
}

.att-btn-primary {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 100%;
  padding: 10px 0;
  border: 0;
  border-radius: 12px;
  background: var(--c-purple);
  color: var(--app-on-fill);
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.05);
  transition: background-color 0.2s ease;
}
.att-btn-primary:hover { background: var(--c-purple-hover); }

/* ============ Records ============ */
.att-records {
  height: 520px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.att-record {
  padding: 14px;
  border: 1px solid var(--c-border);
  border-radius: 12px;
  background: var(--c-bg-soft);
  font-size: 12px;
  transition: background-color 0.2s ease;
}
.att-record:hover { background: var(--app-surface); }

.att-record__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.att-record__tags {
  display: flex;
  align-items: center;
  gap: 8px;
}
.att-record__type {
  padding: 2px 8px;
  border: 1px solid var(--c-border-strong);
  border-radius: 4px;
  background: var(--app-surface);
  font-size: 10px;
  font-weight: 600;
  color: var(--c-text);
}
.att-record__status {
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 10px;
  font-weight: 700;
  white-space: nowrap;
}
.att-record__status.is-pending {
  background: var(--app-amber-bg);
  color: var(--app-amber-deep);
}
.att-record__status.is-passed {
  background: var(--app-emerald-bg);
  color: var(--app-emerald-deep);
}
.att-record__status.is-rejected {
  background: var(--app-red-100);
  color: var(--app-red-700);
}

.att-record__title {
  margin: 10px 0 6px;
  font-size: 12px;
  font-weight: 700;
  color: var(--c-text);
}
.att-record__time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 11px;
  color: var(--c-muted);
}
.att-record__time .att-icon { color: var(--c-faint); }
.att-record__days {
  margin-top: 6px;
  font-size: 11px;
  color: var(--c-muted);
}
.att-record__days strong {
  color: var(--app-text-2);
}
.att-record__reason {
  margin: 8px 0 0;
  padding: 8px;
  border: 1px solid var(--app-surface-3);
  border-radius: 8px;
  background: var(--app-surface);
  font-size: 11px;
  line-height: 1.625;
  color: var(--c-text-sub);
}

/* ============ Pagination ============ */
.att-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--c-border);
  font-size: 12px;
  color: var(--c-muted);
}
.att-pagination__btns {
  display: flex;
  align-items: center;
  gap: 4px;
}
.att-page-arrow,
.att-page-num {
  border-radius: 4px;
  background: transparent;
  color: var(--c-text-sub);
  cursor: pointer;
  transition: background-color 0.2s ease;
}
.att-page-arrow {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4px;
  border: 1px solid var(--c-border);
}
.att-page-arrow:hover:not(:disabled) { background: var(--c-bg-soft); }
.att-page-arrow:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
.att-page-num {
  padding: 2px 10px;
  border: 1px solid var(--c-border-strong);
}
.att-page-num:hover { background: var(--c-bg-soft); }
.att-page-num.is-current {
  border-color: var(--c-sky);
  background: var(--c-sky);
  color: var(--app-ink-solid);
  font-weight: 700;
}

/* ============ Toast transition ============ */
.attendance-toast-enter-active,
.attendance-toast-leave-active {
  transition: opacity 180ms ease, transform 180ms ease;
}
.attendance-toast-enter-from,
.attendance-toast-leave-to {
  opacity: 0;
  transform: translate(-50%, -8px);
}
</style>