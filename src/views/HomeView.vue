<template>
  <div class="home-page">

    <section class="welcome-banner" v-reveal>
      <div class="welcome-content">
        <div class="welcome-tag">
          <span class="welcome-tag-dot"></span>
          <span>持续学习 · 稳步成长</span>
        </div>
        <h1 class="welcome-title">欢迎来到云舒艺境，{{ user?.name || '学员' }}</h1>
        <p class="welcome-desc">
          今天是 {{ todayLabel }}，永远相信美好的事情即将发生！
        </p>
      </div>
    </section>

    <section class="quick-actions" v-reveal-rows>

      <button class="quick-card" @click="currentCourse ? $emit('open-course', currentCourse) : $emit('navigate', 'courses')">
        <div class="quick-card-icon icon-blue">
          <Play class="quick-icon" />
        </div>
        <div class="quick-card-text">
          <div class="quick-card-title">{{ courseTitle }}</div>
          <div class="quick-card-sub">{{ courseSub }}</div>
        </div>
      </button>

      <button class="quick-card" @click="$emit('navigate', 'exams')">
        <div class="quick-card-icon icon-blue">
          <FileCheck2 class="quick-icon" />
        </div>
        <div class="quick-card-text">
          <div class="quick-card-title">在线考核测评</div>
          <div class="quick-card-sub">{{ examSub }}</div>
        </div>
      </button>

      <button class="quick-card" @click="$emit('navigate', 'attendance')">
        <div class="quick-card-icon icon-green">
          <CalendarClock class="quick-icon" />
        </div>
        <div class="quick-card-text">
          <div class="quick-card-title">考勤与培训假</div>
          <div class="quick-card-sub">{{ attendanceSub }}</div>
        </div>
      </button>

      <button class="quick-card" @click="$emit('navigate', 'certificates')">
        <div class="quick-card-icon icon-purple">
          <Award class="quick-icon" />
        </div>
        <div class="quick-card-text">
          <div class="quick-card-title">荣誉资质证书</div>
          <div class="quick-card-sub">{{ certificateSub }}</div>
        </div>
      </button>

      <button class="quick-card quick-card-last" @click="$emit('navigate', 'messages')">
        <div class="quick-card-icon icon-red">
          <Bell class="quick-icon" />
        </div>
        <div class="quick-card-text">
          <div class="quick-card-title">通知与待办</div>
          <div class="quick-card-sub">{{ messageSub }}</div>
        </div>
      </button>

    </section>

    <div class="main-grid">

      <div class="main-left">

        <section class="section-block" v-reveal>
          <div class="section-header">
            <div class="section-header-left">
              <span class="section-bar"></span>
              <h2 class="section-title">正在学习的课程</h2>
            </div>
            <button class="section-link" @click="$emit('navigate', 'courses')">全部学习档案 &gt;</button>
          </div>

          <div v-if="learnerStore.loading && !learnerStore.loaded" class="course-empty">
            正在加载你的学习记录…
          </div>
          <div v-else-if="!personalCourses.length" class="course-empty">
            <p>你还没有开始学习课程，开始学习后这里会显示你的课程记录。</p>
            <button type="button" class="course-empty-link" @click="$emit('navigate', 'courses')">
              去课程中心
            </button>
          </div>
          <div v-else class="course-grid" v-reveal-rows>
            <article
              v-for="course in personalCourses"
              :key="course.id"
              class="course-card"
              role="button"
              tabindex="0"
              :aria-label="`打开${course.title}课程`"
              @click="$emit('open-course', course)"
              @keydown.enter="openCourseFromKeyboard"
              @keydown.space.prevent="openCourseFromKeyboard"
            >
              <div class="course-cover">
                <img
                  v-if="course.thumbnail"
                  :src="course.thumbnail"
                  :alt="`${course.title}课程封面`"
                  class="course-cover-img"
                />
                <BookOpenText v-else class="course-cover-placeholder" aria-hidden="true" />
                <div class="course-cover-shade"></div>
                <div class="course-badge" :class="course.status === 'completed' ? 'badge-purple' : 'badge-dark'">
                  {{ course.statusLabel || (course.status === 'completed' ? '已完成' : '学习中') }}
                </div>
              </div>
              <div class="course-body">
                <div class="course-teacher">
                  <span class="course-avatar avatar-blue">
                    {{ course.instructor.name?.charAt(0) || '课' }}
                  </span>
                  <div class="course-teacher-info">
                    <p class="course-teacher-label">授课讲师</p>
                    <p class="course-teacher-name">
                      {{ course.instructor.name || '暂未设置' }}
                      <template v-if="course.instructor.title"> · {{ course.instructor.title }}</template>
                    </p>
                  </div>
                </div>
                <h3 class="course-name">{{ course.title }}</h3>
                <p class="course-progress">
                  {{ course.status === 'completed' ? '已完成学习' : `学习进度 ${course.progress || 0}%` }}
                  <span v-if="course.lastStudied"> · {{ course.lastStudied }}</span>
                </p>
              </div>
            </article>
          </div>
        </section>

        <section class="section-block" v-reveal>
          <div class="section-header section-header-wrap">
            <div class="section-header-left">
              <span class="section-bar"></span>
              <h2 class="section-title">新员工入职手册</h2>
            </div>
            <p class="guide-subtitle">从熟悉平台开始，轻松开启新旅程</p>
          </div>

          <div class="guide-grid" v-reveal-rows>
            <article
              class="guide-card"
              role="button"
              tabindex="0"
              aria-haspopup="dialog"
              @click="openGuide('platform', $event)"
              @keydown.enter="openGuideFromKeyboard($event, 'platform')"
              @keydown.space.prevent="openGuideFromKeyboard($event, 'platform')"
            >
              <div class="guide-visual guide-visual-blue">
                <span class="guide-step">01 · 认识平台</span>
                <div class="guide-illustration guide-illustration-platform" aria-hidden="true">
                  <div class="guide-window">
                    <span></span><span></span><span></span>
                    <div class="guide-window-content">
                      <LayoutDashboard class="guide-art-icon" />
                      <i></i><i></i><i></i>
                    </div>
                  </div>
                  <BookOpenText class="guide-floating-icon" />
                </div>
              </div>
              <div class="guide-card-body">
                <h3 class="guide-title">认识云舒艺境</h3>
                <p class="guide-description">课程学习、在线考试、通知公告和个人档案，都可以在平台中轻松找到。</p>
                <div class="guide-tags"><span>课程中心</span><span>学习档案</span><span>消息通知</span></div>
                <span class="guide-more">点击查看详情 <ArrowRight /></span>
              </div>
            </article>

            <article
              class="guide-card"
              role="button"
              tabindex="0"
              aria-haspopup="dialog"
              @click="openGuide('checklist', $event)"
              @keydown.enter="openGuideFromKeyboard($event, 'checklist')"
              @keydown.space.prevent="openGuideFromKeyboard($event, 'checklist')"
            >
              <div class="guide-visual guide-visual-green">
                <span class="guide-step">02 · 入职必办</span>
                <div class="guide-illustration guide-illustration-checklist" aria-hidden="true">
                  <div class="guide-checklist">
                    <div><CircleCheck /><span></span></div>
                    <div><CircleCheck /><span></span></div>
                    <div><CircleCheck /><span></span></div>
                  </div>
                  <ClipboardCheck class="guide-floating-icon" />
                </div>
              </div>
              <div class="guide-card-body">
                <h3 class="guide-title">完成入职必办事项</h3>
                <p class="guide-description">检查个人资料和部门信息，了解考勤、培训假等日常制度。</p>
                <div class="guide-tags"><span>个人资料</span><span>考勤规则</span><span>部门信息</span></div>
                <span class="guide-more">点击查看详情 <ArrowRight /></span>
              </div>
            </article>

            <article
              class="guide-card"
              role="button"
              tabindex="0"
              aria-haspopup="dialog"
              @click="openGuide('growth', $event)"
              @keydown.enter="openGuideFromKeyboard($event, 'growth')"
              @keydown.space.prevent="openGuideFromKeyboard($event, 'growth')"
            >
              <div class="guide-visual guide-visual-violet">
                <span class="guide-step">03 · 学习成长</span>
                <div class="guide-illustration guide-illustration-growth" aria-hidden="true">
                  <div class="guide-growth-path"><i></i><i></i><i></i></div>
                  <GraduationCap class="guide-art-icon" />
                  <Sparkles class="guide-floating-icon" />
                </div>
              </div>
              <div class="guide-card-body">
                <h3 class="guide-title">开启学习与成长</h3>
                <p class="guide-description">从岗位必修课程开始，按计划参加考核，逐步积累学习成果与专业认证。</p>
                <div class="guide-tags"><span>岗位必修</span><span>在线考核</span><span>专业认证</span></div>
                <span class="guide-more">点击查看详情 <ArrowRight /></span>
              </div>
            </article>
          </div>
        </section>

      </div>

      <div class="main-right">

        <section class="side-panel" v-reveal="{ delay: 80, direction: 'right' }">
          <div class="side-panel-header">
            <div class="side-panel-title-row">
              <Bell class="side-panel-icon" />
              <h2 class="side-panel-title">企业培训公告</h2>
            </div>
            <button class="section-link" @click="$emit('navigate', 'messages')">查看更多</button>
          </div>

          <div class="announcement-list">

            <div
              v-for="(item, index) in homeAnnouncements"
              :key="item.id"
              class="announcement-item"
              :class="{ 'announcement-item-last': index === homeAnnouncements.length - 1 }"
              @click="$emit('open-message', item.id)"
            >
              <div class="announcement-meta">
                <span
                  class="announcement-tag"
                  :class="item.isTop ? 'tag-amber-bg' : 'tag-blue-bg'"
                >{{ item.typeLabel }}</span>
                <span class="announcement-date">{{ announcementDate(item) }}</span>
              </div>
              <div class="announcement-title">
                <span v-if="!item.isRead" class="announcement-unread" aria-label="未读"></span>{{ item.title }}
              </div>
            </div>

            <p v-if="!homeAnnouncements.length" class="announcement-empty">
              暂无已发布的培训公告
            </p>

          </div>
        </section>

        <section class="focus-corner" v-reveal="{ delay: 140 }" aria-label="学习陪伴角">

          <span class="float-deco float-deco--1">✦</span>
          <span class="float-deco float-deco--2">✧</span>
          <span class="float-deco float-deco--3">✦</span>

          <transition name="badge-pop">
            <span v-if="orbTaps > 0" :key="orbTaps" class="tap-badge">已互动 {{ orbTaps }} 次</span>
          </transition>

          <div
            class="mascot-stage"
            @mouseenter="handleCatEnter($event)"
            @mousemove="handleCatTilt($event)"
            @mouseleave="handleCatTiltReset($event)"
          >

            <span
              v-for="p in particles"
              :key="p.id"
              class="spark-particle"
              :style="{ '--px': p.x + 'px', '--py': p.y + 'px', '--dx': p.dx + 'px', '--dy': p.dy + 'px', '--pc': p.color }"
            >{{ p.icon }}</span>

            <button
              class="mascot-btn"
              :class="{ 'is-pulse': orbPulse, 'is-happy': mascotHappy }"
              type="button"
              :aria-label="`点击和守护兽互动，当前第 ${quoteIndex + 1} 句励志语`"
              @click="changeQuote"
            >

              <span class="mascot-halo"></span>
              <img
                class="mascot-img"
                src="/assets/fox-mascot.png"
                alt="戴皇冠的白色守护兽玩偶"
                loading="lazy"
              />

              <span v-if="blinking" class="mascot-blink"></span>
            </button>
          </div>

          <div class="chat-bubble-wrap">
            <transition name="bubble-pop" mode="out-in">
              <div :key="quoteIndex" class="chat-bubble">
                <span class="bubble-tail"></span>
                <div class="bubble-avatar">
                  <img class="bubble-avatar-img" src="/assets/fox-mascot.png" alt="守护兽狐小d" />
                </div>
                <div class="bubble-body">
                  <span class="bubble-name">狐小d</span>
                  <p class="bubble-text">{{ typedText }}<span v-if="isTyping" class="typing-caret"></span></p>
                </div>
              </div>
            </transition>
          </div>
          <p class="focus-hint">点一下守护兽 · 听它说句悄悄话</p>
        </section>

      </div>
    </div>

    <div v-if="selectedGuide" class="guide-modal-backdrop" @click.self="closeGuide">
      <section
        class="guide-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="guide-modal-title"
        @keydown.esc="closeGuide"
      >
        <button ref="guideModalCloseButton" class="guide-modal-close" aria-label="关闭详情" @click="closeGuide">
          <X />
        </button>
        <span class="guide-modal-step">{{ selectedGuide.step }}</span>
        <h2 id="guide-modal-title" class="guide-modal-title">{{ selectedGuide.title }}</h2>
        <p class="guide-modal-intro">{{ selectedGuide.intro }}</p>
        <div class="guide-modal-sections">
          <article v-for="section in selectedGuide.sections" :key="section.title" class="guide-detail">
            <h3>{{ section.title }}</h3>
            <p>{{ section.description }}</p>
          </article>
        </div>
        <div class="guide-modal-tip">
          <Lightbulb />
          <p>{{ selectedGuide.tip }}</p>
        </div>
        <button class="guide-modal-done" @click="closeGuide">我知道了</button>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue';
import { learnerStore } from '../learnerStore.js';
import { guideDetails } from '../onboardingGuide.js';
import { fetchExams, fetchMyCertificates } from '../api/index.js';
import { useMessages } from '../composables/useMessages.js';
import { useAttendance } from '../composables/useAttendance.js';
import { useLeaveRequests } from '../composables/useLeaveRequests.js';
import { useToast } from '../composables/useToast.js';
import {
  Play,
  FileCheck2,
  CalendarClock,
  Award,
  Bell,
  BookOpenText,
  CircleCheck,
  ClipboardCheck,
  GraduationCap,
  LayoutDashboard,
  Sparkles,
  ArrowRight,
  Lightbulb,
  X,
} from 'lucide-vue-next';

const props = defineProps({
  searchTargetId: {
    type: String,
    default: '',
  },
  courseId: {
    type: String,
    default: '',
  },
  user: {
    type: Object,
    default: null,
  },
});

defineEmits(['navigate', 'open-course', 'open-message']);

const messages = useMessages();
const leaveRecords = useLeaveRequests();
const { isCheckedIn, checkedInAt } = useAttendance();
const { show: showToast } = useToast();
const examAssignments = ref(null);
const certificateCount = ref(null);
const todayLabel = new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  weekday: 'long',
}).format(new Date());
const selectedGuide = ref(null);
const guideModalCloseButton = ref(null);
const guideTrigger = ref(null);

const openGuide = (id, event) => {
  guideTrigger.value = event?.currentTarget || null;
  selectedGuide.value = guideDetails[id];
  nextTick(() => guideModalCloseButton.value?.focus());
};

const closeGuide = () => {
  selectedGuide.value = null;
  nextTick(() => guideTrigger.value?.focus());
};

const openGuideFromKeyboard = (event, id) => {
  if (event.target === event.currentTarget) openGuide(id, event);
};

const CAT_MAX_TILT = 16;
const handleCatEnter = (event) => {
  const el = event.currentTarget;
  el.__catRect = el.getBoundingClientRect();
};
const handleCatTilt = (event) => {
  if (window.matchMedia?.('(prefers-reduced-motion: reduce)').matches) return;
  const el = event.currentTarget;
  const rect = el.__catRect || el.getBoundingClientRect();
  const px = (event.clientX - rect.left) / rect.width;
  const py = (event.clientY - rect.top) / rect.height;
  el.style.setProperty('--cat-tilt-y', `${((px - 0.5) * 2 * CAT_MAX_TILT).toFixed(2)}deg`);
  el.style.setProperty('--cat-tilt-x', `${((0.5 - py) * 2 * CAT_MAX_TILT).toFixed(2)}deg`);
};
const handleCatTiltReset = (event) => {
  const el = event.currentTarget;
  el.style.setProperty('--cat-tilt-x', '0deg');
  el.style.setProperty('--cat-tilt-y', '0deg');
  el.__catRect = null;
};

const quotes = [
  '每一次专注，都在为未来蓄力',
  '今天多学一点，明天从容一些',
  '进步不必惊天动地，持续就好',
  '种一棵树最好的时间是现在',
  '点亮一个想法，胜过空想百次',
  '专注当下，就是最好的准备',
];
const quoteIndex = ref(0);
const orbPulse = ref(false);
const mascotHappy = ref(false);
const orbTaps = ref(0);
const blinking = ref(false);
const typedText = ref('');
const isTyping = ref(false);
const particles = ref([]);
const PARTICLE_ICONS = ['✦', '✧', '★', '💫', '✨', '⭐'];
const PARTICLE_COLORS = ['#f5b642', '#7ec8f2', '#ff9eb5', '#a78bfa', '#34d399'];
let orbPulseTimer = null;
let happyTimer = null;
let blinkTimer = null;
let typeTimer = null;
let particleId = 0;

const scheduleBlink = () => {
  const delay = 2600 + Math.random() * 3200;
  blinkTimer = setTimeout(() => {
    blinking.value = true;
    setTimeout(() => { blinking.value = false; }, 180);
    scheduleBlink();
  }, delay);
};

const burstParticles = () => {
  const created = Array.from({ length: 10 }, () => {
    const angle = Math.random() * Math.PI * 2;
    const dist = 60 + Math.random() * 90;
    return {
      id: ++particleId,
      x: 90 + (Math.random() - 0.5) * 60,
      y: 120 + (Math.random() - 0.5) * 60,
      dx: Math.cos(angle) * dist,
      dy: Math.sin(angle) * dist - 40,
      icon: PARTICLE_ICONS[Math.floor(Math.random() * PARTICLE_ICONS.length)],
      color: PARTICLE_COLORS[Math.floor(Math.random() * PARTICLE_COLORS.length)],
    };
  });
  particles.value.push(...created);
  setTimeout(() => {
    const ids = new Set(created.map((p) => p.id));
    particles.value = particles.value.filter((p) => !ids.has(p.id));
  }, 900);
};

const typeQuote = (text) => {
  if (typeTimer) clearInterval(typeTimer);

  if (window.matchMedia?.('(prefers-reduced-motion: reduce)').matches) {
    typedText.value = text;
    isTyping.value = false;
    return;
  }
  typedText.value = '';
  isTyping.value = true;
  let i = 0;
  typeTimer = setInterval(() => {
    typedText.value = text.slice(0, ++i);
    if (i >= text.length) {
      clearInterval(typeTimer);
      isTyping.value = false;
    }
  }, 70);
};

const changeQuote = () => {

  let next = quoteIndex.value;
  while (next === quoteIndex.value && quotes.length > 1) {
    next = Math.floor(Math.random() * quotes.length);
  }
  quoteIndex.value = next;
  orbTaps.value += 1;
  typeQuote(quotes[next]);

  orbPulse.value = false;
  requestAnimationFrame(() => {
    orbPulse.value = true;
    if (orbPulseTimer) clearTimeout(orbPulseTimer);
    orbPulseTimer = setTimeout(() => { orbPulse.value = false; }, 600);
  });

  mascotHappy.value = false;
  requestAnimationFrame(() => {
    mascotHappy.value = true;
    if (happyTimer) clearTimeout(happyTimer);
    happyTimer = setTimeout(() => { mascotHappy.value = false; }, 700);
  });
  burstParticles();
};

onUnmounted(() => {
  clearTimeout(orbPulseTimer);
  clearTimeout(happyTimer);
  clearTimeout(blinkTimer);
  clearInterval(typeTimer);
});

const personalCourses = computed(() => learnerStore.courses
  .filter((course) => course.status === 'studying'
    || course.status === 'completed'
    || Number(course.progress) > 0
    || Boolean(course.lastStudiedAt))
  .slice()
  .sort((a, b) => String(b.lastStudiedAt || '').localeCompare(String(a.lastStudiedAt || '')))
  .slice(0, 2));

const currentCourse = computed(() =>
  personalCourses.value.find((course) => course.id === props.courseId)
    || personalCourses.value[0]
);

const hasStarted = computed(() => (Number(currentCourse.value?.progress) || 0) > 0);
const courseTitle = computed(() => hasStarted.value ? '继续上次学习' : '开始学习');

const courseSub = computed(() => {
  const course = currentCourse.value;
  if (!course) return '暂无可选课程';

  const label = course.categoryLabel || '在修课程';
  return hasStarted.value ? `${label} · ${course.progress}%` : `${label} · 未开始`;
});

const examSub = computed(() => {
  if (!examAssignments.value) return '考核数据加载中…';
  const runningCount = examAssignments.value.filter((exam) => exam.status === 'in_progress').length;
  const pendingCount = examAssignments.value.filter((exam) => exam.status !== 'completed').length;
  if (runningCount) return `${runningCount} 场进行中考核`;
  return pendingCount ? `${pendingCount} 场考核待完成` : '暂无待完成考核';
});

const pendingLeaveCount = computed(() => leaveRecords.value.filter(r => r.status === 'pending').length);
const attendanceSub = computed(() => {
  if (isCheckedIn.value) {
    return `已签到 ${checkedInAt.value}` + (pendingLeaveCount.value ? ` · ${pendingLeaveCount.value} 条审批中` : '');
  }
  return pendingLeaveCount.value ? `未签到 · ${pendingLeaveCount.value} 条审批中` : '未签到 · 无待审批请假';
});

const certificateSub = computed(() =>
  certificateCount.value === null ? '证书数据加载中…' : `已获 ${certificateCount.value} 项专业认证`);

onMounted(async () => {
  scheduleBlink();
  typeQuote(quotes[0]);
  const [examsResult, certificatesResult] = await Promise.allSettled([
    fetchExams(),
    fetchMyCertificates(),
  ]);
  if (examsResult.status === 'fulfilled') {
    examAssignments.value = examsResult.value.assignments || [];
  } else if (examsResult.reason?.status !== 401) {
    showToast(examsResult.reason?.message || '考核数据加载失败。', 'error');
  }
  if (certificatesResult.status === 'fulfilled') {
    certificateCount.value = certificatesResult.value.length;
  } else if (certificatesResult.reason?.status !== 401) {
    showToast(certificatesResult.reason?.message || '证书数据加载失败。', 'error');
  }
});

const unreadCount = computed(() => messages.value.filter(m => !m.isRead).length);
const messageSub = computed(() =>
  unreadCount.value ? `${unreadCount.value} 条未读消息` : '暂无未读消息');

const announcementDate = (item) => {
  const value = item?.publishAt || item?.createdAt;
  if (!value) return '';
  const text = String(value);
  if (/^\d{4}-\d{2}-\d{2}/.test(text)) return text.slice(0, 10);
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return text;
  const pad = (n) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
};

const announcementTime = (item) => {
  const value = item?.publishAt || item?.createdAt;
  const time = value ? new Date(value).getTime() : 0;
  return Number.isNaN(time) ? 0 : time;
};

const homeAnnouncements = computed(() =>
  [...messages.value]
    .sort((a, b) => {
      if (Boolean(a.isTop) !== Boolean(b.isTop)) return a.isTop ? -1 : 1;
      return announcementTime(b) - announcementTime(a);
    })
    .slice(0, 2));

const openCourseFromKeyboard = (event) => {
  if (event.target === event.currentTarget) {
    event.currentTarget.click();
  }
};

let lastScrolledId = '';

const scrollToTarget = (targetId) => {
  if (!targetId) return;

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
  targetId => {
    if (!targetId) return;
    scrollToTarget(targetId);
  },
  { immediate: true },
);
</script>

<style scoped>

.home-page {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  padding-bottom: 3rem;
}

.welcome-banner {
  border-radius: 1rem;
  border: 1px solid var(--app-accent-soft);
  background: linear-gradient(135deg, var(--app-accent-soft) 0%, var(--app-page-bg) 50%, var(--app-surface) 100%);
  padding: 2rem 1.25rem;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}
@media (min-width: 768px) {
  .welcome-banner { padding: 2.5rem 2.5rem; }
}
.welcome-content {
  max-width: 48rem;
  margin-left: auto;
  margin-right: auto;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  text-align: center;
}

.welcome-tag {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.25rem 0.75rem;
  border-radius: 999px;
  background: var(--app-wash-strong);
  border: 1px solid rgba(131, 201, 244, 0.4);
  color: var(--app-accent-deep);
  font-size: 0.75rem;
  font-weight: 600;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  align-self: center;
  animation: welcome-text-in 360ms ease-out both;
}
.welcome-tag-dot {
  width: 0.5rem;
  height: 0.5rem;
  border-radius: 50%;
  background: var(--app-accent);
  flex-shrink: 0;
}
.welcome-title {
  font-size: 1.5rem;
  font-weight: 800;
  color: var(--app-text);
  letter-spacing: -0.025em;
  line-height: 1.25;
  animation: welcome-text-in 380ms cubic-bezier(0.2, 0.7, 0.2, 1) both;
}
@media (min-width: 640px) {
  .welcome-title { font-size: 1.875rem; }
}
@media (min-width: 768px) {
  .welcome-title { font-size: 2.25rem; }
}
.welcome-desc {
  max-width: 42rem;
  margin-left: auto;
  margin-right: auto;
  font-size: 0.75rem;
  color: var(--app-text-4);
  line-height: 1.625;
  animation: welcome-text-in 380ms ease-out both;
}
@media (min-width: 640px) {
  .welcome-desc { font-size: 0.875rem; }
}

.quick-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
}
@media (min-width: 640px) {
  .quick-actions { grid-template-columns: repeat(5, 1fr); }
}
.quick-card {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.875rem;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 0.75rem;
  cursor: pointer;
  text-align: left;
  transition: transform 0.25s cubic-bezier(0.22, 1, 0.36, 1), border-color 0.25s, box-shadow 0.25s;
}
.quick-card:hover {
  transform: translateY(-6px);
  border-color: var(--app-accent);
  box-shadow: 0 12px 24px rgba(0, 82, 118, 0.16), 0 4px 8px rgba(0, 0, 0, 0.06);
}
.quick-card:active {
  transform: translateY(-2px);
  transition-duration: 0.1s;
}
.quick-card-last {
  grid-column: span 2;
}
@media (min-width: 640px) {
  .quick-card-last { grid-column: span 1; }
}
@media (max-width: 420px) {
  .quick-actions { grid-template-columns: minmax(0, 1fr); }
  .quick-card-last { grid-column: auto; }
}

.quick-card-icon {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 0.75rem;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: transform 0.2s;
}
.quick-card:hover .quick-card-icon {
  transform: scale(1.12) translateY(-1px);
}
.quick-icon {
  width: 1.25rem;
  height: 1.25rem;
}
.icon-blue   { background: var(--app-accent-soft); color: var(--app-accent-deep); }
.icon-green  { background: var(--app-green-50); color: var(--app-green-600); }
.icon-purple { background: var(--app-accent-soft); color: var(--app-violet-fg); }
.icon-red    { background: var(--app-rose-100); color: var(--app-rose-600); }

.quick-card-text { min-width: 0; }
.quick-card-title {
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--app-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: color 0.2s;
}
.quick-card:hover .quick-card-title {
  color: var(--app-accent-deep);
}
.quick-card-sub {
  font-size: 0.625rem;
  color: var(--app-text-4);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.main-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 1.5rem;
}
@media (min-width: 1024px) {
  .main-grid {
    grid-template-columns: repeat(12, minmax(0, 1fr));
  }
  .main-left  { grid-column: span 8; }
  .main-right { grid-column: span 4; }
}
.main-left {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}
.main-right {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.section-block {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.section-header-wrap {
  flex-wrap: wrap;
  gap: 0.5rem;
}
@media (min-width: 640px) {
  .section-header-wrap { flex-wrap: nowrap; }
}
.section-header-left {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.section-bar {
  width: 0.25rem;
  height: 1rem;
  border-radius: 999px;
  background: var(--app-accent);
  flex-shrink: 0;
}
.section-title {
  font-size: 1rem;
  font-weight: 700;
  color: var(--app-text);
}
.section-link {
  font-size: 0.75rem;
  color: var(--app-accent-deep);
  font-weight: 500;
  cursor: pointer;
  background: none;
  border: none;
}
.section-link:hover {
  text-decoration: underline;
}

.course-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 1rem;
}
.course-empty {
  display: flex;
  min-height: 10rem;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 1.5rem;
  border: 1px dashed var(--app-border);
  border-radius: 0.75rem;
  background: var(--app-surface);
  color: var(--app-text-4);
  font-size: 0.875rem;
  text-align: center;
}
.course-empty p { margin: 0; }
.course-empty-link {
  padding: 0.45rem 0.85rem;
  border: 0;
  border-radius: 0.5rem;
  background: var(--app-accent);
  color: var(--app-on-accent-solid);
  cursor: pointer;
  font: inherit;
}
@media (min-width: 768px) {
  .course-grid { grid-template-columns: 1fr 1fr; }
}
.course-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: 0.75rem;
  border: 1px solid var(--app-border);
  background: var(--app-surface);
  cursor: pointer;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s, border-color 0.2s, box-shadow 0.2s;
  animation: card-reveal 360ms ease-out both;
}
.course-card:hover {
  transform: translateY(-2px);
  border-color: var(--app-accent);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
.course-card:focus-visible {
  outline: 2px solid var(--app-accent);
  outline-offset: 2px;
}

.course-cover {
  position: relative;
  height: 10rem;
  overflow: hidden;
  background: var(--app-ink-solid);
  flex-shrink: 0;
}
.course-cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0.9;
  transition: transform 0.5s;
}
.course-cover-placeholder {
  position: absolute;
  inset: 0;
  width: 3rem;
  height: 3rem;
  margin: auto;
  color: var(--app-accent-soft);
}
.course-card:hover .course-cover-img {
  transform: scale(1.05);
}
.course-cover-shade {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.25), transparent 60%);
}
.course-badge {
  position: absolute;
  left: 0.75rem;
  top: 0.75rem;
  padding: 0.25rem 0.625rem;
  border-radius: 0.375rem;
  font-size: 0.625rem;
  font-weight: 600;
  color: var(--app-on-scrim);
}
.badge-dark {
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
}
.badge-purple {
  background: var(--app-violet);
}

.course-body {
  display: flex;
  flex-direction: column;
  flex: 1;
  padding: 1rem;
}
.course-teacher {
  display: flex;
  align-items: center;
  gap: 0.625rem;
  margin-bottom: 0.75rem;
}
.course-avatar {
  width: 2rem;
  height: 2rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 700;
  flex-shrink: 0;
}
.avatar-blue   { background: var(--app-accent-soft); color: var(--app-accent-deep); }
.avatar-purple { background: var(--app-accent-soft); color: var(--app-violet-fg); }
.course-teacher-info { min-width: 0; }
.course-teacher-label {
  font-size: 0.625rem;
  line-height: 1;
  color: var(--app-text-5);
}
.course-teacher-name {
  font-size: 0.75rem;
  font-weight: 500;
  color: var(--app-text-3);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.course-name {
  font-size: 0.875rem;
  font-weight: 700;
  line-height: 1.25;
  color: var(--app-text);
  min-height: 2.75rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.course-progress {
  margin: 0.5rem 0 0;
  color: var(--app-text-4);
  font-size: 0.6875rem;
}

.guide-subtitle {
  color: var(--app-text-4);
  font-size: 0.75rem;
}
.guide-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 0.75rem;
}
@media (min-width: 768px) {
  .guide-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
}
.guide-card {
  min-width: 0;
  overflow: hidden;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 0.75rem;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: transform 0.28s cubic-bezier(0.22, 1, 0.36, 1), border-color 0.28s, box-shadow 0.28s;
}
.guide-card:hover {
  transform: translateY(-8px);
  border-color: var(--app-accent);
  box-shadow: 0 18px 40px rgba(37, 99, 145, 0.18), 0 6px 14px rgba(37, 99, 145, 0.08);
}
.guide-card:active {
  transform: translateY(-3px);
  transition-duration: 0.1s;
}
.guide-card:focus-visible {
  outline: 2px solid var(--app-accent);
  outline-offset: 2px;
}
@media (prefers-reduced-motion: reduce) {
  .guide-card { transition: none; }
  .guide-card:hover,
  .guide-card:active { transform: none; }
}

.guide-visual {
  position: relative;
  height: 7.5rem;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.35s cubic-bezier(0.22, 1, 0.36, 1);
}
.guide-visual-blue {
  background: linear-gradient(135deg, #d9efff, #eef8ff);
}
.guide-visual-green {
  background: linear-gradient(135deg, #dcfce9, #f1fff5);
}
.guide-visual-violet {
  background: linear-gradient(135deg, #e9e4ff, #f7f4ff);
}
.guide-step {
  position: absolute;
  top: 0.75rem;
  left: 0.875rem;
  font-size: 0.625rem;
  font-weight: 700;
  color: var(--app-text-3);
}
.guide-illustration {
  position: relative;
  width: 9rem;
  height: 5.25rem;
  display: flex;
  align-items: center;
  justify-content: center;
}
.guide-window {
  width: 6rem;
  height: 4.1rem;
  padding: 0.35rem;
  border: 2px solid #6ab3df;
  border-radius: 0.5rem;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 6px 12px rgba(65, 135, 174, 0.12);
}
.guide-window > span {
  display: inline-block;
  width: 0.3rem;
  height: 0.3rem;
  margin-right: 0.2rem;
  border-radius: 50%;
  background: #8bc9ed;
}
.guide-window-content {
  height: 2.8rem;
  margin-top: 0.2rem;
  display: flex;
  align-items: center;
  gap: 0.2rem;
  padding: 0.25rem;
  border-radius: 0.25rem;
  background: #eef8ff;
}
.guide-art-icon {
  width: 1.65rem;
  height: 1.65rem;
  color: #3196cb;
  flex-shrink: 0;
}
.guide-window-content i {
  height: 0.3rem;
  flex: 1;
  border-radius: 999px;
  background: #b7def4;
  box-shadow: 0 0.55rem #d3ebf8, 0 1.1rem #d3ebf8;
}
.guide-floating-icon {
  position: absolute;
  right: 0.1rem;
  bottom: 0.1rem;
  width: 2rem;
  height: 2rem;
  padding: 0.35rem;
  color: #318fc1;
  border: 1px solid rgba(255, 255, 255, 0.9);
  border-radius: 0.65rem;
  background: #fff;
  box-shadow: 0 4px 10px rgba(31, 82, 115, 0.12);
}
.guide-illustration-checklist {
  width: 8rem;
}
.guide-checklist {
  width: 5.5rem;
  padding: 0.45rem 0.6rem;
  border: 2px solid #70c795;
  border-radius: 0.5rem;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 6px 12px rgba(67, 144, 98, 0.12);
}
.guide-checklist > div {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  height: 0.75rem;
}
.guide-checklist > div + div {
  margin-top: 0.2rem;
}
.guide-checklist svg {
  width: 0.65rem;
  height: 0.65rem;
  color: #36a968;
}
.guide-checklist span {
  width: 2.8rem;
  height: 0.25rem;
  border-radius: 999px;
  background: #c9ecd6;
}
.guide-illustration-checklist .guide-floating-icon {
  color: #36a968;
}
.guide-illustration-growth {
  width: 8.5rem;
}
.guide-illustration-growth .guide-art-icon {
  width: 3.25rem;
  height: 3.25rem;
  color: #7965c8;
  z-index: 1;
}
.guide-illustration-growth .guide-floating-icon {
  color: #7965c8;
}
.guide-growth-path {
  position: absolute;
  left: 0.35rem;
  right: 0.35rem;
  bottom: 0.45rem;
  height: 1.5rem;
  border-bottom: 3px solid #aa9be7;
  border-radius: 50%;
  transform: rotate(-12deg);
}
.guide-growth-path i {
  position: absolute;
  bottom: -0.3rem;
  width: 0.55rem;
  height: 0.55rem;
  border: 2px solid #fff;
  border-radius: 50%;
  background: #9682dc;
}
.guide-growth-path i:nth-child(1) { left: 12%; }
.guide-growth-path i:nth-child(2) { left: 50%; bottom: 0.05rem; }
.guide-growth-path i:nth-child(3) { right: 8%; bottom: 0.55rem; }
.guide-card-body {
  padding: 0.875rem;
  transition: transform 0.35s cubic-bezier(0.22, 1, 0.36, 1);
}
.guide-card:hover .guide-visual {
  transform: scale(1.04);
}
.guide-card:hover .guide-card-body {
  transform: translateY(-2px);
}
@media (prefers-reduced-motion: reduce) {
  .guide-visual,
  .guide-card-body { transition: none; }
  .guide-card:hover .guide-visual,
  .guide-card:hover .guide-card-body { transform: none; }
}
.guide-title {
  margin-bottom: 0.35rem;
  color: var(--app-text);
  font-size: 0.8125rem;
  font-weight: 700;
}
.guide-description {
  min-height: 2.5rem;
  color: var(--app-text-4);
  font-size: 0.6875rem;
  line-height: 1.6;
}
.guide-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.3rem;
  margin-top: 0.6rem;
}
.guide-tags span {
  padding: 0.18rem 0.4rem;
  border-radius: 999px;
  background: var(--app-surface-3);
  color: var(--app-text-3);
  font-size: 0.6rem;
}
.guide-more {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  margin-top: 0.7rem;
  color: var(--app-accent-deep);
  font-size: 0.6875rem;
  font-weight: 700;
}
.guide-more svg {
  width: 0.8rem;
  height: 0.8rem;
  transition: transform 0.2s;
}
.guide-card:hover .guide-more svg {
  transform: translateX(3px);
}
.guide-modal-backdrop {
  position: fixed;
  z-index: 1000;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 1rem;
  background: rgba(15, 23, 42, 0.48);
  backdrop-filter: blur(3px);
}
.guide-modal {
  position: relative;
  width: min(100%, 34rem);
  max-height: min(90vh, 46rem);
  overflow-y: auto;
  padding: 1.75rem;
  border: 1px solid var(--app-border);
  border-radius: 1rem;
  background: var(--app-surface);
  box-shadow: 0 20px 60px rgba(15, 23, 42, 0.2);
  animation: card-reveal 180ms ease-out both;
}
.guide-modal-close {
  position: absolute;
  top: 1rem;
  right: 1rem;
  display: grid;
  place-items: center;
  width: 2rem;
  height: 2rem;
  border: 0;
  border-radius: 0.5rem;
  background: var(--app-surface-3);
  color: var(--app-text-3);
  cursor: pointer;
}
.guide-modal-close:hover {
  background: var(--app-accent-soft);
  color: var(--app-accent-deep);
}
.guide-modal-close svg {
  width: 1rem;
  height: 1rem;
}
.guide-modal-step {
  color: var(--app-accent-deep);
  font-size: 0.7rem;
  font-weight: 700;
}
.guide-modal-title {
  margin-top: 0.45rem;
  padding-right: 2rem;
  color: var(--app-heading);
  font-size: 1.35rem;
  font-weight: 800;
}
.guide-modal-intro {
  margin-top: 0.55rem;
  color: var(--app-text-3);
  font-size: 0.875rem;
  line-height: 1.7;
}
.guide-modal-sections {
  display: grid;
  gap: 0.7rem;
  margin-top: 1.25rem;
}
.guide-detail {
  padding: 0.9rem 1rem;
  border: 1px solid var(--app-border);
  border-radius: 0.75rem;
  background: var(--app-surface-2);
}
.guide-detail h3 {
  color: var(--app-text);
  font-size: 0.8125rem;
  font-weight: 700;
}
.guide-detail p {
  margin-top: 0.3rem;
  color: var(--app-text-4);
  font-size: 0.75rem;
  line-height: 1.65;
}
.guide-modal-tip {
  display: flex;
  align-items: flex-start;
  gap: 0.55rem;
  margin-top: 1rem;
  padding: 0.75rem;
  border-radius: 0.65rem;
  background: var(--app-accent-soft);
  color: var(--app-accent-deep);
}
.guide-modal-tip svg {
  width: 1rem;
  height: 1rem;
  margin-top: 0.1rem;
  flex-shrink: 0;
}
.guide-modal-tip p {
  font-size: 0.75rem;
  line-height: 1.6;
}
.guide-modal-done {
  display: block;
  min-width: 6rem;
  margin: 1.25rem 0 0 auto;
  padding: 0.6rem 1rem;
  border: 0;
  border-radius: 0.55rem;
  background: var(--app-accent-deep);
  color: #fff;
  font-size: 0.8rem;
  font-weight: 700;
  cursor: pointer;
}
.guide-modal-done:hover {
  filter: brightness(1.08);
}
@media (max-width: 520px) {
  .guide-modal {
    padding: 1.25rem;
  }
}

.side-panel {
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 0.75rem;
  padding: 1.25rem;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  animation: card-reveal 360ms ease-out both;
}

.focus-corner {
  position: relative;
  min-height: 340px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border: 1px solid var(--app-border);
  border-radius: 0.75rem;
  padding: 1.25rem 1rem;
  background:
    radial-gradient(ellipse at 50% 55%, var(--app-accent-dim), transparent 62%),
    linear-gradient(145deg, var(--app-surface), var(--app-surface-2));
}

.float-deco {
  position: absolute;
  color: var(--app-accent);
  opacity: 0.45;
  pointer-events: none;
  font-size: 1rem;
  animation: float-deco 5s ease-in-out infinite;
}
.float-deco--1 { top: 18%; left: 14%; font-size: 1.1rem; animation-delay: 0s; }
.float-deco--2 { top: 32%; right: 16%; color: #f5b642; animation-delay: 1.4s; }
.float-deco--3 { bottom: 30%; left: 20%; font-size: 0.8rem; animation-delay: 2.6s; }
@keyframes float-deco {
  0%, 100% { transform: translateY(0) rotate(0deg); opacity: 0.3; }
  50% { transform: translateY(-14px) rotate(45deg); opacity: 0.7; }
}

.tap-badge {
  position: absolute;
  top: 0.75rem;
  right: 0.85rem;
  padding: 0.25rem 0.6rem;
  border-radius: 999px;
  background: var(--app-accent);
  color: var(--app-on-accent-fill);
  font-size: 0.68rem;
  font-weight: 600;
  box-shadow: 0 2px 8px var(--app-accent-a20);
}
.badge-pop-enter-active { animation: badge-pop 0.4s cubic-bezier(0.34, 1.56, 0.64, 1); }
@keyframes badge-pop {
  0% { transform: scale(0.4); opacity: 0; }
  60% { transform: scale(1.12); }
  100% { transform: scale(1); opacity: 1; }
}

.mascot-stage {
  position: relative;
  perspective: 620px;
  --cat-tilt-x: 0deg;
  --cat-tilt-y: 0deg;
}
.mascot-btn {
  position: relative;
  width: 180px;
  height: 240px;
  padding: 0;
  border: 0;
  background: transparent;
  cursor: pointer;
  transform-style: preserve-3d;
  transform: rotateX(var(--cat-tilt-x)) rotateY(var(--cat-tilt-y));
  transition: transform 0.2s cubic-bezier(0.22, 1, 0.36, 1);
  animation: mascot-bob 3.4s ease-in-out infinite;
}
.mascot-btn:focus-visible { outline: 2px solid var(--app-accent); outline-offset: 6px; }
.mascot-img {
  position: relative;
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
  transform: translateZ(22px);
  z-index: 1;
}

.mascot-btn.is-pulse { animation: mascot-tap 0.55s ease-out; }

.mascot-btn.is-happy .mascot-img { animation: mascot-wiggle 0.7s ease-in-out; }
@keyframes mascot-bob {
  0%, 100% { transform: translateY(0) rotateX(var(--cat-tilt-x)) rotateY(var(--cat-tilt-y)); }
  50% { transform: translateY(-7px) rotateX(var(--cat-tilt-x)) rotateY(var(--cat-tilt-y)); }
}
@keyframes mascot-tap {
  0% { transform: scale(1) rotateX(var(--cat-tilt-x)) rotateY(var(--cat-tilt-y)); }
  35% { transform: scale(0.88) translateY(6px) rotateX(var(--cat-tilt-x)) rotateY(var(--cat-tilt-y)); }
  70% { transform: scale(1.06) translateY(-4px) rotateX(var(--cat-tilt-x)) rotateY(var(--cat-tilt-y)); }
  100% { transform: scale(1) rotateX(var(--cat-tilt-x)) rotateY(var(--cat-tilt-y)); }
}
@keyframes mascot-wiggle {
  0%, 100% { transform: translateZ(22px) rotate(0deg); }
  25% { transform: translateZ(22px) rotate(-8deg); }
  75% { transform: translateZ(22px) rotate(8deg); }
}

.mascot-halo {
  position: absolute;
  left: 50%;
  top: 52%;
  width: 200px;
  height: 200px;
  margin: -100px 0 0 -100px;
  border-radius: 50%;
  background: radial-gradient(circle, var(--app-accent-a20), transparent 68%);
  animation: halo-pulse 3.4s ease-in-out infinite;
  pointer-events: none;
}
@keyframes halo-pulse {
  0%, 100% { transform: scale(0.9); opacity: 0.5; }
  50% { transform: scale(1.1); opacity: 0.9; }
}

.mascot-blink {
  position: absolute;
  left: 50%;
  top: 46%;
  width: 96px;
  height: 8px;
  margin-left: -48px;
  z-index: 2;
  pointer-events: none;
}
.mascot-blink::before,
.mascot-blink::after {
  content: '';
  position: absolute;
  top: 0;
  width: 26px;
  height: 5px;
  border-radius: 3px;
  background: #f2c9a5;
}
.mascot-blink::before { left: 14px; }
.mascot-blink::after { right: 14px; }

.spark-particle {
  position: absolute;
  left: var(--px);
  top: var(--py);
  z-index: 3;
  font-size: 1rem;
  color: var(--pc);
  pointer-events: none;
  animation: spark-fly 0.9s cubic-bezier(0.22, 1, 0.36, 1) forwards;
}
@keyframes spark-fly {
  0% { transform: translate(0, 0) scale(0.4) rotate(0deg); opacity: 1; }
  100% { transform: translate(var(--dx), var(--dy)) scale(1.3) rotate(180deg); opacity: 0; }
}

.chat-bubble-wrap {
  width: 100%;
  display: flex;
  justify-content: center;
  padding: 0.5rem 0.25rem 0.2rem;
  perspective: 600px;
}
.chat-bubble {
  position: relative;
  display: flex;
  align-items: center;
  gap: 0.8rem;
  max-width: 100%;
  min-height: 62px;
  padding: 0.7rem 1.1rem 0.7rem 0.7rem;
  background: linear-gradient(135deg, #0b3a5c 0%, #005576 100%);
  border-radius: 1.15rem;
  box-shadow:
    0 12px 28px rgba(0, 85, 118, 0.28),
    inset 0 1px 0 rgba(255, 255, 255, 0.18);
}

.bubble-tail {
  position: absolute;
  top: -6px;
  left: 50%;
  width: 0;
  height: 0;
  border-left: 9px solid transparent;
  border-right: 9px solid transparent;
  border-bottom: 8px solid #0b3a5c;
  transform: translateX(-50%) rotate(12deg);
}

.bubble-avatar {
  position: relative;
  flex-shrink: 0;
  width: 46px;
  height: 46px;
  border-radius: 50%;
  background: var(--app-surface);
  padding: 2px;
  box-shadow: 0 4px 12px var(--app-accent-a20);
}
.bubble-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center 40%;
  border-radius: 50%;
}
.bubble-body { min-width: 0; flex: 0 1 auto; }
.bubble-name {
  display: block;
  font-size: 0.66rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.82);
  letter-spacing: 0.06em;
  margin-bottom: 0.15rem;
}
.bubble-text {
  margin: 0;
  font-size: 0.9rem;
  font-weight: 600;
  line-height: 1.45;
  color: #ffffff;
  min-height: 1.45em;
  min-width: 13.5em;
  letter-spacing: 0.01em;
}
.typing-caret {
  display: inline-block;
  width: 2px;
  height: 1em;
  margin-left: 2px;
  vertical-align: -0.12em;
  border-radius: 2px;
  background: var(--app-accent);
  animation: caret-blink 0.7s steps(1) infinite;
}
@keyframes caret-blink { 0%, 50% { opacity: 1; } 51%, 100% { opacity: 0; } }
.bubble-pop-enter-active { animation: bubble-in 0.4s cubic-bezier(0.34, 1.56, 0.64, 1); }
.bubble-pop-leave-active { transition: opacity 0.15s ease, transform 0.15s ease; }
.bubble-pop-enter-from { opacity: 0; transform: translateY(12px) scale(0.92); }
.bubble-pop-leave-to { opacity: 0; transform: translateY(-4px) scale(0.97); }
@keyframes bubble-in {
  0% { opacity: 0; transform: translateY(14px) scale(0.9); }
  65% { transform: translateY(-2px) scale(1.02); }
  100% { opacity: 1; transform: translateY(0) scale(1); }
}
.focus-hint {
  margin: 0.75rem 0 0;
  font-size: 0.7rem;
  color: var(--app-text-4);
}

@media (prefers-reduced-motion: reduce) {
  .mascot-btn, .mascot-img, .mascot-halo, .float-deco { animation: none !important; transition: none !important; }
  .mascot-btn.is-pulse, .mascot-btn.is-happy .mascot-img { animation: none !important; }
  .spark-particle { display: none; }
  .typing-caret { display: none; }
}
.side-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}
.side-panel-title-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.side-panel-icon {
  width: 1rem;
  height: 1rem;
  color: var(--app-accent-deep);
}
.side-panel-title {
  font-size: 0.875rem;
  font-weight: 700;
  color: var(--app-text);
}
.side-panel-count {
  font-size: 0.625rem;
  padding: 0.125rem 0.5rem;
  border-radius: 999px;
  background: var(--app-surface-3);
  color: var(--app-text-4);
  font-family: 'JetBrains Mono', monospace;
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.announcement-item {
  padding: 0.625rem;
  border-radius: 0.5rem;
  cursor: pointer;
  border-bottom: 1px solid var(--app-surface-3);
  transition: background 0.2s;
  animation: card-reveal 340ms ease-out both;
}
.announcement-item:hover {
  background: var(--app-surface-2);
}
.announcement-item-last {
  border-bottom: none;
}
.announcement-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 0.625rem;
  color: var(--app-text-5);
  margin-bottom: 0.25rem;
}
.announcement-title {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--app-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.announcement-unread {
  flex: none;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--app-rose-500, #ef4444);
}

.announcement-empty {
  padding: 1.25rem 0.5rem;
  text-align: center;
  font-size: 0.75rem;
  color: var(--app-text-5);
}

.announcement-tag {
  padding: 0.05rem 0.375rem;
  border-radius: 0.25rem;
  font-size: 0.625rem;
  font-weight: 500;
  white-space: nowrap;
  line-height: 1.4;
}
.tag-blue-bg  { background: var(--app-accent-soft); color: var(--app-accent-deep); }
.tag-amber-bg { background: var(--app-amber-bg); color: var(--app-amber-strong); }

.announcement-date {
  font-size: 0.625rem;
  color: var(--app-text-5);
}

@keyframes welcome-text-in {
  from {
    opacity: 0.65;
    transform: translateY(3px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
@keyframes card-reveal {
  from {
    opacity: 0.65;
    transform: translateY(3px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
