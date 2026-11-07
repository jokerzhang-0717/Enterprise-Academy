<template>
  <div class="home-page">

    <!-- ============ 1. 顶部欢迎横幅 ============ -->
    <section class="welcome-banner">
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

    <!-- ============ 2. 快捷入口（5 个卡片） ============ -->
    <section class="quick-actions">

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

    <!-- ============ 3. 主体分栏 ============ -->
    <div class="main-grid">

      <!-- ======== 左栏（8 列） ======== -->
      <div class="main-left">

        <!-- 正在学习的课程 -->
        <section class="section-block">
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
          <div v-else class="course-grid">
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

        <!-- 新员工入职手册 -->
        <section class="section-block">
          <div class="section-header section-header-wrap">
            <div class="section-header-left">
              <span class="section-bar"></span>
              <h2 class="section-title">新员工入职手册</h2>
            </div>
            <p class="guide-subtitle">从熟悉平台开始，轻松开启新旅程</p>
          </div>

          <div class="guide-grid">
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

      <!-- ======== 右栏（4 列） ======== -->
      <div class="main-right">

        <!-- 企业培训公告 -->
        <section class="side-panel">
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

        <section class="cat-corner" aria-label="小猫陪伴角">
          <span class="cat-sparkle cat-sparkle-left" aria-hidden="true">✦</span>
          <span class="cat-sparkle cat-sparkle-right" aria-hidden="true">✧</span>
          <svg class="study-cat" viewBox="0 0 180 170" role="img" aria-label="一只可爱的小猫">
            <ellipse cx="92" cy="151" rx="48" ry="8" fill="#dbeafe" />
            <path d="M56 119c-17 1-27-8-27-20 0-9 7-16 17-17" fill="none" stroke="#d99a72" stroke-width="9" stroke-linecap="round" />
            <ellipse cx="91" cy="123" rx="43" ry="34" fill="#f3bd91" />
            <ellipse cx="91" cy="127" rx="23" ry="25" fill="#fff1df" />
            <path d="m48 64-5-39 34 23m24 0 34-23-5 39" fill="#f3bd91" stroke="#bd805e" stroke-width="4" stroke-linejoin="round" />
            <path d="m52 55-2-20 19 14m43 0 19-14-2 20" fill="#f4a8ae" />
            <path d="M49 58c10-15 24-22 42-22s32 7 42 22c8 11 11 25 7 39-6 22-25 34-49 34s-43-12-49-34c-4-14-1-28 7-39Z" fill="#ffe0bd" stroke="#bd805e" stroke-width="4" />
            <path d="M70 77c4-5 10-5 14 0m14 0c4-5 10-5 14 0" fill="none" stroke="#604a45" stroke-width="4" stroke-linecap="round" />
            <path d="m86 91 5 4 5-4c0 6-2 8-5 8s-5-2-5-8Z" fill="#dd8290" />
            <path d="M91 99c-1 7-7 9-12 5m12-5c1 7 7 9 12 5" fill="none" stroke="#604a45" stroke-width="2.5" stroke-linecap="round" />
            <circle cx="69" cy="94" r="6" fill="#f4a8ae" opacity=".65" />
            <circle cx="113" cy="94" r="6" fill="#f4a8ae" opacity=".65" />
            <path d="M48 91 27 87m22 13-22 2m105-11 21-4m-21 13 22 2" fill="none" stroke="#bd805e" stroke-width="2" stroke-linecap="round" />
            <path d="M69 145c2 6 10 8 17 4m19 0c7 4 15 2 17-4" fill="none" stroke="#bd805e" stroke-width="3" stroke-linecap="round" />
          </svg>
          <p class="cat-caption">喵～学习也要记得休息哦</p>
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
import { computed, nextTick, onMounted, ref, watch } from 'vue';
import { learnerStore } from '../learnerStore.js';
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

/* ---------- 快捷入口：副标题与目标页同一份数据 ---------- */
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

const guideDetails = {
  platform: {
    step: '新员工手册 · 01',
    title: '认识云舒艺境',
    intro: '这是公司的学习与发展平台，入职后的课程、考试和学习记录都可以在这里管理。',
    sections: [
      { title: '课程学习', description: '进入“课程学习中心”浏览推荐课程和岗位必修内容，课程进度会自动记录。' },
      { title: '考试与测评', description: '在“考试与测评中心”查看待参加考试、考试时间和历史成绩，请留意考试开放与截止时间。' },
      { title: '通知与个人档案', description: '通过“消息与站内信”查看公司通知；在个人中心核对个人资料并查看学习档案。' },
    ],
    tip: '建议先完善个人资料，再查看岗位必修课程和最新通知。',
  },
  checklist: {
    step: '新员工手册 · 02',
    title: '完成入职必办事项',
    intro: '花几分钟完成基础设置，后续学习、考勤和部门通知才能准确关联到你的账号。',
    sections: [
      { title: '核对个人资料', description: '检查姓名、工号、岗位、所属部门等信息；如有错误，请联系管理员更新。' },
      { title: '熟悉考勤流程', description: '了解每日签到入口和考勤要求；如需培训假或请假，通过考勤与培训假页面提交申请。' },
      { title: '关注部门信息', description: '确认账号所属部门正确，以便接收对应的学习安排和部门通知。' },
    ],
    tip: '账号信息如无法自行修改，请联系管理员，不要使用他人账号代办。',
  },
  growth: {
    step: '新员工手册 · 03',
    title: '开启学习与成长',
    intro: '按照岗位要求循序渐进完成学习任务，让学习记录和能力成长清晰可见。',
    sections: [
      { title: '从岗位必修开始', description: '优先完成岗位要求的课程；可以从首页“正在学习的课程”或课程中心进入学习。' },
      { title: '按时参加考核', description: '查看考试详情中的开放时间、截止时间和考试要求，在规定时间内完成测评。' },
      { title: '积累学习成果', description: '课程学时、考试记录和已获得的专业认证会沉淀在个人学习档案中，可随时查看。' },
    ],
    tip: '按自己的节奏持续学习，遇到课程或考试问题可以先查看通知，或联系培训管理员。',
  },
};

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
  /* 全名会挤掉百分比，这里用同类目页也展示的短标签 */
  const label = course.categoryLabel || course.tags?.[0] || '在修课程';
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
  const [examsResult, certificatesResult] = await Promise.allSettled([
    fetchExams(),
    fetchMyCertificates(),
  ]);
  if (examsResult.status === 'fulfilled') {
    examAssignments.value = examsResult.value.assignments || [];
  } else {
    showToast(examsResult.reason?.message || '考核数据加载失败。', 'error');
  }
  if (certificatesResult.status === 'fulfilled') {
    certificateCount.value = certificatesResult.value.length;
  } else {
    showToast(certificatesResult.reason?.message || '证书数据加载失败。', 'error');
  }
});

const unreadCount = computed(() => messages.value.filter(m => !m.isRead).length);
const messageSub = computed(() =>
  unreadCount.value ? `${unreadCount.value} 条未读消息` : '暂无未读消息');

/* ---------- 企业培训公告：与消息中心的公告数据同源 ---------- */
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

/* ---------- 键盘无障碍操作 ---------- */
const openCourseFromKeyboard = (event) => {
  if (event.target === event.currentTarget) {
    event.currentTarget.click();
  }
};

/* ---------- 搜索定位滚动（修复死循环 / 爆栈风险） ---------- */
let lastScrolledId = '';

const scrollToTarget = (targetId) => {
  if (!targetId) return;
  // 同一目标不重复滚动，打断 watch <-> 状态 闭环
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
/* ============================================================
   云舒艺境 · 首页仪表盘样式
   ------------------------------------------------------------
   目录
     01  页面基础布局          08  研修通道卡片
     02  顶部欢迎横幅          09  右栏通用面板
     03  快捷入口卡片          10  企业公告
     04  主体分栏网格          11  企业公告
     05  分栏标题              12  标签配色变体
     06  课程卡片              13  入场动画
     07  课程封面与角标        14  无障碍
   ------------------------------------------------------------
   色板速查
     品牌蓝   #005576 · #83C9F4 · #D9F0FF
     品牌紫   #6555B0 · #9381FF
     标题深色 #1E293B · #334155 · #475569
     正文灰   #64748B · #94A3B8
     成功绿   #16A34A · #F0FDF4
     警告红   #DC2626 · #E11D48 · #FEE2E2 · #FFF1F2
     警示琥珀 #D97706 · #FEF3C7
     边框浅灰 #E2E8F0 · #F1F5F9 · #F8FAFC · #FFFFFF
   ============================================================ */

/* ==================== 01 页面基础布局 ==================== */
.home-page {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;              /* 24px */
  padding-bottom: 3rem;     /* 48px */
}

/* ==================== 02 顶部欢迎横幅 ==================== */
.welcome-banner {
  border-radius: 1rem;      /* 16px */
  border: 1px solid var(--app-accent-soft);
  background: linear-gradient(135deg, var(--app-accent-soft) 0%, var(--app-page-bg) 50%, var(--app-surface) 100%);
  padding: 2rem 1.25rem;    /* 32px 20px */
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}
@media (min-width: 768px) {
  .welcome-banner { padding: 2.5rem 2.5rem; }
}
.welcome-content {
  max-width: 48rem;         /* 768px */
  margin-left: auto;
  margin-right: auto;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;             /* 12px */
  text-align: center;
}

/* 标题上方小标签 */
.welcome-tag {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;            /* 6px */
  padding: 0.25rem 0.75rem;
  border-radius: 999px;
  background: var(--app-wash-strong);
  border: 1px solid rgba(131, 201, 244, 0.4);
  color: var(--app-accent-deep);
  font-size: 0.75rem;       /* 12px */
  font-weight: 600;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  align-self: center;
  animation: welcome-text-in 360ms ease-out both;
}
.welcome-tag-dot {
  width: 0.5rem;            /* 8px */
  height: 0.5rem;
  border-radius: 50%;
  background: var(--app-accent);
  flex-shrink: 0;
}
.welcome-title {
  font-size: 1.5rem;        /* 24px */
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

/* ==================== 03 快捷入口卡片 ==================== */
.quick-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;             /* 12px */
}
@media (min-width: 640px) {
  .quick-actions { grid-template-columns: repeat(5, 1fr); }
}
.quick-card {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.875rem;        /* 14px */
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 0.75rem;   /* 12px */
  cursor: pointer;
  text-align: left;
  transition: transform 0.2s, border-color 0.2s, box-shadow 0.2s;
  animation: card-reveal 360ms ease-out both;
}
.quick-card:hover {
  transform: translateY(-2px);
  border-color: var(--app-accent);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
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

/* 图标容器 + 变体 */
.quick-card-icon {
  width: 2.5rem;            /* 40px */
  height: 2.5rem;
  border-radius: 0.75rem;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: transform 0.2s;
}
.quick-card:hover .quick-card-icon {
  transform: scale(1.05);
}
.quick-icon {
  width: 1.25rem;
  height: 1.25rem;
}
.icon-blue   { background: var(--app-accent-soft); color: var(--app-accent-deep); }
.icon-green  { background: var(--app-green-50); color: var(--app-green-600); }
.icon-purple { background: var(--app-accent-soft); color: var(--app-violet-fg); }
.icon-red    { background: var(--app-rose-100); color: var(--app-rose-600); }

/* 卡片文字 */
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
  font-size: 0.625rem;      /* 10px */
  color: var(--app-text-4);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ==================== 04 主体分栏网格 ==================== */
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

/* ==================== 05 分栏标题 ==================== */
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
  width: 0.25rem;           /* 4px */
  height: 1rem;             /* 16px */
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

/* ==================== 06 课程卡片 ==================== */
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

/* ==================== 07 课程封面与角标 ==================== */
.course-cover {
  position: relative;
  height: 10rem;            /* 160px */
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

/* 课程正文 */
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
  width: 2rem;              /* 32px */
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

/* ==================== 08 新员工入职手册 ==================== */
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
  animation: card-reveal 360ms ease-out both;
  transition: transform 0.2s, border-color 0.2s, box-shadow 0.2s;
}
.guide-card:hover {
  transform: translateY(-2px);
  border-color: var(--app-accent);
  box-shadow: 0 4px 14px rgba(37, 99, 145, 0.1);
}
.guide-card:focus-visible {
  outline: 2px solid var(--app-accent);
  outline-offset: 2px;
}
.guide-visual {
  position: relative;
  height: 7.5rem;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
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

/* ==================== 09 右栏通用面板 ==================== */
.side-panel {
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 0.75rem;
  padding: 1.25rem;         /* 20px */
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  animation: card-reveal 360ms ease-out both;
}
.cat-corner {
  position: relative;
  min-height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border: 1px solid var(--app-border);
  border-radius: 0.75rem;
  background:
    radial-gradient(ellipse at 50% 100%, rgba(191, 226, 250, 0.42), transparent 58%),
    linear-gradient(145deg, #f8fcff, #edf7ff);
}
.study-cat {
  width: min(150px, 44%);
  overflow: visible;
  animation: cat-bob 3s ease-in-out infinite;
}
.cat-sparkle {
  position: absolute;
  color: #f0bf67;
  font-size: 1.25rem;
}
.cat-sparkle-left {
  top: 28%;
  left: 28%;
}
.cat-sparkle-right {
  top: 38%;
  right: 27%;
  color: #8bc9ed;
}
.cat-caption {
  margin-top: 0.5rem;
  color: var(--app-text-3);
  font-size: 0.8125rem;
  font-weight: 600;
}
@keyframes cat-bob {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
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

/* ==================== 10 企业公告 ==================== */
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

/* 未读公告红点，与消息中心已读状态同步 */
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

/* ==================== 11 标签配色变体 ==================== */
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

/* ==================== 12 入场动画 ==================== */
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
/* ==================== 14 无障碍：减少动效 ==================== */
@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>