<template>
  <ToastStack />
  <Transition name="app-view">
  <!-- ============ 1. 登录全屏视图 ============ -->
  <div v-if="currentView === 'login'" class="app-login">
    <LoginView @login-success="handleLoginSuccess" />
  </div>

  <!-- ============ 2. 主应用布局 ============ -->
  <div
    v-else
    class="app-shell"
    :class="'theme-' + theme"
  >
    <div class="app-body" :class="{ 'app-body--user': !isAdmin }">

      <!-- 侧边导航:管理员 / 学员 -->
      <AdminSidebar
        v-if="isAdmin"
        :current-view="currentView"
        :user="currentUser"
        @select-view="handleNavigate"
        @logout="handleLogout"
      />
      <Sidebar
        v-else
        :current-view="currentView"
        :theme="theme"
        :user="currentUser"
        @select-view="handleNavigate"
        @change-theme="theme = $event"
        @logout="handleLogout"
      />

      <!-- 主内容工作区 -->
      <div class="app-workspace">
        <!-- 顶部栏:管理端只保留头像 -->
        <Header
          :user="currentUser"
          :is-admin="isAdmin"
          :is-checked-in="isCheckedIn"
          :unread-count="unreadMessageCount"
          :search-results="searchResults"
          @navigate="handleNavigate"
          @cycle-theme="cycleTheme"
          @search="globalSearchQuery = $event"
          @select-search-result="handleSelectSearchResult"
          @open-mockup-viewer="handleOpenMockupForCurrentView"
        />

        <!-- 视图画布 -->
        <main
          ref="mainContent"
          class="app-viewport"
          :class="{
            'app-viewport--admin': isAdmin && currentView !== 'admin-profile',
            'app-viewport--user': !isAdmin
          }"
        >
          <Transition name="view" mode="out-in">
            <div
              :key="currentView"
              class="view-transition"
              :class="{ 'view-transition--home': currentView === 'home' }"
            >
              <AdminDashboardView
                v-if="currentView === 'admin-dashboard'"
                :user="currentUser"
                @navigate="handleNavigate"
              />

              <AdminStudentsView
                v-else-if="currentView === 'admin-students'"
              />

              <AdminDepartmentsView
                v-else-if="currentView === 'admin-departments'"
              />

              <AdminCoursesView
                v-else-if="currentView === 'admin-courses'"
              />

              <AdminExamsView
                v-else-if="currentView === 'admin-exams'"
                @navigate="handleNavigate"
              />

              <AdminQuestionBankView
                v-else-if="currentView === 'admin-question-bank'"
              />

              <AdminLeaveApprovalView
                v-else-if="currentView === 'admin-leave'"
              />

              <AdminCheckinRecordsView
                v-else-if="currentView === 'admin-checkin'"
              />

              <AdminCertificatesView
                v-else-if="currentView === 'admin-certificates'"
              />

              <AdminAnnouncementsView
                v-else-if="currentView === 'admin-announcements'"
              />

              <ProfileView
                v-else-if="currentView === 'admin-profile'"
                :user="currentUser"
                @update-user="handleUpdateUser"
              />

              <HomeView
                v-else-if="currentView === 'home'"
                :search-target-id="searchTargetId"
                :course-id="selectedCourseId"
                @navigate="handleNavigate"
                @open-course="handleOpenCourse"
              />

              <CoursesView
                v-else-if="currentView === 'courses'"
                :search-target-id="searchTargetId"
                @open-course="handleOpenCourse"
                @open-certificate-modal="handleOpenCertificate"
              />

              <CoursePlayerView
                v-else-if="currentView === 'course-player'"
                :course-id="selectedCourseId"
                @back="handleNavigate('courses')"
                @navigate="handleNavigate"
              />

              <RecommendedTrackDetailView
                v-else-if="currentView === 'track-detail' && selectedTrack"
                :track="selectedTrack"
                @back="handleNavigate('home')"
              />

              <ExamsView
                v-else-if="currentView === 'exams'"
                @start-exam="handleStartExam"
              />

              <ExamTakingView
                v-else-if="currentView === 'exam-taking'"
                :exam-id="selectedExamId"
                :user="currentUser"
                @back="handleNavigate('exams')"
                @submit-exam="handleSubmitExam"
              />

              <AnalyticsView
                v-else-if="currentView === 'analytics'"
                :user="currentUser"
                @navigate="handleNavigate"
                @open-certificate-modal="handleOpenCertificate"
              />

              <ProfileView
                v-else-if="currentView === 'profile'"
                :user="currentUser"
                @update-user="handleUpdateUser"
              />

              <AttendanceView
                v-else-if="currentView === 'attendance'"
                :employee-id="currentUser.employeeId"
              />

              <MessagesView
                v-else-if="currentView === 'messages'"
                :search-target-id="searchTargetId"
                :selected-message-id="selectedMessageId"
                :detail-mode="false"
                @navigate="handleNavigate"
                @open-message="handleOpenMessage"
              />

              <MessagesView
                v-else-if="currentView === 'messages-detail'"
                :selected-message-id="selectedMessageId"
                :detail-mode="true"
                @navigate="handleNavigate"
              />

              <CertificatesView
                v-else-if="currentView === 'certificates'"
                :search-target-id="searchTargetId"
                @open-certificate-modal="handleOpenCertificate"
              />
            </div>
          </Transition>
        </main>
      </div>
    </div>

    <!-- ============ 3. 弹窗 / 对话框 ============ -->

    <!-- 1) 原型图查看器 -->
    <MockupViewerModal
      :is-open="isMockupViewerOpen"
      :initial-key="mockupInitialKey"
      @close="isMockupViewerOpen = false"
    />

    <!-- 2) 证书查看器 -->
    <CertificateModal
      :certificate="selectedCert"
      :recipient-name="currentUser.name"
      @close="selectedCert = null"
    />

    <!-- 3) 考试成绩弹窗 -->
    <ExamResultModal
      :is-open="isExamResultOpen"
      :score="examResult.score"
      :pass-score="examResult.passScore"
      :total-score="examResult.totalScore"
      :answered-count="examResult.answeredCount"
      :total-questions="examResult.totalQuestions"
      :exam-title="examResult.examTitle"
      :is-passed="examResult.isPassed"
      :pending-review="examResult.pendingReview"
      @close="handleCloseExamResult"
      @view-certificate="handleViewCertFromExam"
      @retake="handleRetakeExam"
    />
  </div>
  </Transition>
</template>

<script setup>
import { computed, onMounted, ref, watch, watchEffect } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  COURSES_DATA,
  EXAMS_DATA,
  PAST_EXAMS,
  CERTIFICATES_DATA,
  MESSAGES_DATA,
  RECOMMENDED_TRACKS,
} from './mockData.js';

// Shared Components
import Sidebar from './components/Sidebar.vue';
import AdminSidebar from './components/AdminSidebar.vue';
import Header from './components/Header.vue';
import MockupViewerModal from './components/MockupViewerModal.vue';
import ToastStack from './components/ToastStack.vue';
import CertificateModal from './components/CertificateModal.vue';
import ExamResultModal from './components/ExamResultModal.vue';

// Views
import HomeView from './views/HomeView.vue';
import CoursesView from './views/CoursesView.vue';
import CoursePlayerView from './views/CoursePlayerView.vue';
import RecommendedTrackDetailView from './views/CourseDetailView.vue';
import ExamsView from './views/ExamsView.vue';
import ExamTakingView from './views/ExamTakingView.vue';
import AnalyticsView from './views/AnalyticsView.vue';
import ProfileView from './views/ProfileView.vue';
import AttendanceView from './views/AttendanceView.vue';
import MessagesView from './views/MessagesView.vue';
import CertificatesView from './views/CertificatesView.vue';
import LoginView from './views/LoginView.vue';
import AdminDashboardView from './views/admin/AdminDashboardView.vue';
import AdminStudentsView from './views/admin/AdminStudentsView.vue';
import AdminDepartmentsView from './views/admin/AdminDepartmentsView.vue';
import AdminCoursesView from './views/admin/AdminCoursesView.vue';
import AdminExamsView from './views/admin/AdminExamsView.vue';
import AdminQuestionBankView from './views/admin/AdminQuestionBankView.vue';
import AdminLeaveApprovalView from './views/admin/AdminLeaveApprovalView.vue';
import AdminCheckinRecordsView from './views/admin/AdminCheckinRecordsView.vue';
import AdminCertificatesView from './views/admin/AdminCertificatesView.vue';
import AdminAnnouncementsView from './views/admin/AdminAnnouncementsView.vue';

import { useToast } from './composables/useToast.js';
import { useMessages, resetMessages, loadMessages, markMessageRead } from './composables/useMessages.js';
import { useAttendance, resetAttendance, loadAttendance } from './composables/useAttendance.js';
import { resetLeaves, loadLeaves } from './composables/useLeaveRequests.js';
import { adminStore, loadAdminStore, resetAdminStore } from './adminStore.js';
import { loadLearnerCourses, resetLearnerStore } from './learnerStore.js';
import { locationForView } from './router/index.js';
import { logoutApi } from './api/index.js';
import {
  clearAuth,
  getStoredUser,
  getToken,
  setStoredUser,
  setToken,
  setUnauthorizedHandler,
} from './api/client.js';

// 登录态
const storedUser = getStoredUser();
const route = useRoute();
const router = useRouter();
/* 登录态落地页由角色决定 */
const landingView = (user) => (user && user.role === 'admin' ? 'admin-dashboard' : 'home');
const currentUser = ref(storedUser || null);
const currentView = ref(route.name === 'exam-taking-by-id' ? 'exam-taking' : route.name || 'login');

// Navigation State
const mainContent = ref(null);
const theme = ref('cyan');
const { isCheckedIn } = useAttendance();
const selectedTrack = ref(null);
const selectedMessageId = ref('');
const messages = useMessages();
const unreadMessageCount = computed(() => messages.value.filter(message => !message.isRead).length);
const selectedCourseId = ref('');
const selectedExamId = ref('');
const globalSearchQuery = ref('');
const searchTargetId = ref('');

watch(
  () => route.fullPath,
  () => {
    const routeName = route.name === 'exam-taking-by-id' ? 'exam-taking' : route.name;
    currentView.value = routeName || 'home';
    selectedCourseId.value = String(route.params.courseId || '');
    selectedTrack.value = route.params.trackId
      ? RECOMMENDED_TRACKS.find((track) => track.id === route.params.trackId) || null
      : null;
    selectedMessageId.value = String(route.params.messageId || '');
    selectedExamId.value = String(route.params.examId || route.query.examId || '');
    searchTargetId.value = '';
    mainContent.value?.scrollTo({ top: 0, behavior: 'auto' });
  },
  { immediate: true },
);

// Modals State
const isMockupViewerOpen = ref(false);
const mockupInitialKey = ref('home');
const selectedCert = ref(null);

const isExamResultOpen = ref(false);
const examResult = ref({
  score: 0,
  passScore: 60,
  totalScore: 100,
  answeredCount: 0,
  totalQuestions: 0,
  examTitle: '',
  isPassed: false,
  pendingReview: false,
});

const searchableRecords = computed(() => [
  ...COURSES_DATA.map(course => ({
    id: course.id,
    type: '课程',
    title: course.title,
    description: `${course.instructor.name} · ${course.summary}`,
    searchText: [
      course.title,
      course.summary,
      course.categoryLabel,
      course.level,
      course.instructor.name,
      course.instructor.title,
      ...course.tags,
    ].join(' '),
    view: 'courses',
    targetId: `course-card-${course.id}`,
  })),
  ...RECOMMENDED_TRACKS.map(track => ({
    id: track.id,
    type: '研修通道',
    title: track.title,
    description: track.summary,
    searchText: [track.title, track.summary, track.description, track.audience, ...track.outline].join(' '),
    view: 'track-detail',
    targetId: track.id,
  })),
  ...EXAMS_DATA.map(exam => ({
    id: exam.id,
    type: '考试',
    title: exam.title,
    description: `${exam.category} · ${exam.timeWindow}`,
    searchText: JSON.stringify(exam),
    view: 'exams',
    targetId: `exam-${exam.id}`,
  })),
  ...PAST_EXAMS.map(exam => ({
    id: exam.id,
    type: '历史考试',
    title: exam.title,
    description: `${exam.type} · ${exam.completionDate}`,
    searchText: JSON.stringify(exam),
    view: 'exams',
    targetId: `past-exam-${exam.id}`,
  })),
  ...CERTIFICATES_DATA.map(certificate => ({
    id: certificate.id,
    type: '证书',
    title: certificate.title,
    description: `${certificate.level} · ${certificate.issueOrg}`,
    searchText: JSON.stringify(certificate),
    view: 'certificates',
    targetId: `certificate-${certificate.id}`,
  })),
  ...MESSAGES_DATA.map(message => ({
    id: message.id,
    type: '通知',
    title: message.title,
    description: `${message.sender} · ${message.preview}`,
    searchText: JSON.stringify(message),
    view: 'messages',
    targetId: `message-${message.id}`,
  })),
  {
    id: 'home-announcement-1',
    type: '企业公告',
    title: '关于发布《2025年第二季度技术序列通用学分核算与晋升认定申报通知》',
    description: '全员通告 · 2025-05-13',
    searchText: '2025 第二季度 技术序列 通用学分 核算 晋升认定 申报 通知 全员通告',
    view: 'home',
    targetId: 'home-announcement-1',
  },
  {
    id: 'home-announcement-2',
    type: '企业公告',
    title: '2025「云舒内训师」金牌讲师团第二期招募启动',
    description: '讲师招募 · 2025-05-11',
    searchText: '云舒 内训师 金牌讲师团 第二期 招募 课程研发津贴',
    view: 'home',
    targetId: 'home-announcement-2',
  },
  {
    id: 'analytics-page',
    type: '学习看板',
    title: '个人学习看板',
    description: '学习时长趋势、能力发展与晋升通道',
    searchText: '个人学习看板 研学效能 学习时长 课时趋势 能力发展 六维雷达 学习任务 晋升通道 研学活动 里程碑',
    view: 'analytics',
    targetId: '',
  },
  {
    id: 'attendance-page',
    type: '培训考勤',
    title: '培训考勤与请假中心',
    description: '培训签到、签退与请假申请',
    searchText: '培训考勤 签到 签退 请假申请 培训假 研讨活动 考勤记录',
    view: 'attendance',
    targetId: '',
  },
  {
    id: 'course-player-page',
    type: '课程学习',
    title: 'Spring Boot 3 企业级微服务实战与高并发架构',
    description: '课程视频与章节目录',
    searchText: 'Spring Boot 企业级微服务 Redis 集群 分布式锁 缓存击穿 Lua 实操 课程目录',
    view: 'course-player',
    targetId: '',
  },
]);

const searchResults = computed(() => {
  const query = globalSearchQuery.value.trim().toLocaleLowerCase();
  if (!query) return [];
  return searchableRecords.value
    .filter(record => record.searchText.toLocaleLowerCase().includes(query))
    .slice(0, 10);
});

const screenOptions = [
  { id: 'home', label: '1. 首页工作台 (早报/考勤)' },
  { id: 'courses', label: '2. 课程中心' },
  { id: 'course-player', label: '3. 视频学习与Lua实操' },
  { id: 'exams', label: '4. 考评中心 (测评矩阵)' },
  { id: 'exam-taking', label: '5. 在线闭卷作答大屏' },
  { id: 'analytics', label: '6. 个人研学看板 (效能全景)' },
  { id: 'attendance', label: '7. 考勤打卡与请假中心' },
  { id: 'certificates', label: '8. 荣誉证书与问答' },
  { id: 'messages', label: '9. 站内信与考评通告' },
  { id: 'messages-detail', label: '9.1 消息详情' },
  { id: 'login', label: '10. 企业登录门户' },
];

const handleNavigate = (view) => {
  searchTargetId.value = '';
  const courseId = selectedCourseId.value || COURSES_DATA[0]?.id;
  router.push(locationForView(view, {
    courseId,
    trackId: selectedTrack.value?.id,
    messageId: selectedMessageId.value,
    examId: selectedExamId.value,
  }));
};

const { show: showToast } = useToast();
const THEME_LABELS = { cyan: '青蓝', black: '曜黑', navy: '石墨' };

/* 石墨只给管理端;学员端在青蓝/曜黑之间切 */
const cycleTheme = () => {
  const keys = isAdmin.value ? Object.keys(THEME_LABELS) : ['cyan', 'black'];
  const next = keys[(keys.indexOf(theme.value) + 1) % keys.length];
  theme.value = next;
  showToast(`已切换主题：${THEME_LABELS[next]}`, 'success');
};

const handleSelectSearchResult = (result) => {
  selectedTrack.value = result.type === '研修通道'
    ? RECOMMENDED_TRACKS.find(track => track.id === result.id)
    : null;
  handleNavigate(result.view);
  searchTargetId.value = result.targetId;
  globalSearchQuery.value = '';
};

const handleOpenTrack = (track) => {
  selectedTrack.value = track;
  handleNavigate('track-detail');
};

const handleOpenCourse = (course) => {
  selectedCourseId.value = course.id;
  handleNavigate('course-player');
};

const handleOpenMessage = (messageId) => {
  selectedMessageId.value = messageId;
  handleNavigate('messages-detail');
  markMessageRead(messageId);
};

const isAdmin = computed(() => currentUser.value?.role === 'admin');

/* 壳层与共享数据：学员端消息/考勤/请假/课程 + 管理端基础字典 */
const bootstrapData = async () => {
  const tasks = [loadMessages(true), loadAttendance(), loadLeaves(true)];
  if (isAdmin.value) tasks.push(loadAdminStore());
  else tasks.push(loadLearnerCourses(true));
  await Promise.allSettled(tasks);
};

// ToastStack 通过 Teleport 挂在 body 下,取不到 .app-shell 的覆盖变量,
// 所以把当前主题类同步一份到 body,让浮层跟随主题。
watchEffect(() => {
  document.body.classList.remove('theme-black', 'theme-navy');
  if (currentView.value !== 'login' && theme.value !== 'cyan') {
    document.body.classList.add('theme-' + theme.value);
  }
});

const handleLoginSuccess = (payload) => {
  const user = payload?.user || null;
  if (!user) return;
  setToken(payload.token);
  setStoredUser(user);
  currentUser.value = user;
  const requestedPath = route.query.redirect;
  const destination = typeof requestedPath === 'string'
    && requestedPath.startsWith('/')
    && !requestedPath.startsWith('//')
    ? requestedPath
    : locationForView(landingView(user));
  router.replace(destination);
  bootstrapData();
};

const handleLogout = async () => {
  try {
    await logoutApi();
  } catch {
    /* 后端登出是幂等空操作，失败不应阻塞本地清理 */
  }
  clearAuth();
  resetAdminStore();
  resetLearnerStore();
  resetMessages();
  resetAttendance();
  resetLeaves();
  currentUser.value = null;
  theme.value = 'cyan';
  router.replace({ name: 'login' });
};

onMounted(async () => {
  setUnauthorizedHandler(() => {
    clearAuth();
    resetAdminStore();
    resetLearnerStore();
    resetMessages();
    resetAttendance();
    resetLeaves();
    currentUser.value = null;
    router.replace({ name: 'login' });
  });

  if (getToken()) await bootstrapData();
});

const handleUpdateUser = (updatedUser) => {
  currentUser.value = { ...currentUser.value, ...updatedUser };
};

const handleOpenCertificate = (certId) => {
  const found = CERTIFICATES_DATA.find(c => c.id === certId) || CERTIFICATES_DATA[0];
  selectedCert.value = found;
};

const handleOpenMockupForCurrentView = () => {
  let key = 'home';
  if (currentView.value === 'courses') key = 'courses';
  else if (currentView.value === 'course-player') key = 'coursePlayer';
  else if (currentView.value === 'exams') key = 'exams';
  else if (currentView.value === 'exam-taking') key = 'examTaking';
  else if (currentView.value === 'analytics') key = 'analytics';
  else if (currentView.value === 'attendance') key = 'attendance';
  else if (currentView.value === 'certificates') key = 'certificates';
  else if (currentView.value === 'messages') key = 'messages';
  else if (currentView.value === 'login') key = 'login';

  mockupInitialKey.value = key;
  isMockupViewerOpen.value = true;
};

const handleStartExam = (exam) => {
  selectedExamId.value = exam?.examId ?? exam?.id ?? '';
  handleNavigate('exam-taking');
};

const handleSubmitExam = (payload) => {
  examResult.value = { ...examResult.value, ...(payload || {}) };
  isExamResultOpen.value = true;
};

const handleCloseExamResult = () => {
  isExamResultOpen.value = false;
  handleNavigate('exams');
};

const handleViewCertFromExam = () => {
  isExamResultOpen.value = false;
  handleOpenCertificate('cert-1');
};

const handleRetakeExam = () => {
  isExamResultOpen.value = false;
  selectedExamId.value = '';
  handleNavigate('exams');
};
</script>

<style>
/* ============================================================
   App.vue 根布局 · 纯手写 CSS
   ------------------------------------------------------------
   目录
     01  全局重置与变量
     02  主题配色变体
     03  登录视图
     04  应用外壳 (app-shell / app-body)
     05  主工作区与视图画布
     06  无障碍：减少动效
   ============================================================ */

/* ==================== 01 全局重置与变量 ==================== */
*,
*::before,
*::after {
  box-sizing: border-box;
}

html,
body,
#app {
  height: 100%;
}

body {
  margin: 0;
  font-family: 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', sans-serif;
  background: var(--app-page-bg);
  color: var(--app-text);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

button,
input,
select,
textarea {
  font-family: inherit;
}

/* ==================== 02 主题配色变体 ==================== */
/* :root 基线 = 青蓝主题;theme-black / theme-navy 覆盖同名变量。
   基线同时挂 :root,是因为 ToastStack 用 Teleport 传送到 body,
   不在 .app-shell 内,需要能从根节点拿到同一套 token。
   三组 token 一起换:内容强调色 accent、管理端身份色 brand、深色导航面 nav。 */
:root,
.app-shell {
  /* 内容强调色 */
  --app-accent: #83c9f4;
  --app-accent-strong: #2e8bd6;
  --app-accent-deep: #005576;
  /* 强调色实底上的文字:浅色主题沿用 accent-deep,曜黑下强调色变亮,文字要转成近黑 */
  --app-on-accent-solid: var(--app-accent-deep);
  --app-on-accent-muted: var(--app-text-4);
  --app-accent-soft: #d9f0ff;
  --app-accent-line: #b9d7f0;
  --app-accent-border: #a3d5ff;
  --app-accent-hover: #c2e4fa;
  --app-accent-focus: #38a6d4;
  --app-accent-dim: rgba(131, 201, 244, 0.18);
  /* 实底强调色按钮上的文字 / 悬停填充 */
  --app-on-accent-fill: #005576;
  --app-accent-fill-hi: #a3d5ff;
  --app-accent-a10: rgba(46, 139, 214, 0.1);
  --app-accent-a12: rgba(46, 139, 214, 0.12);
  --app-accent-a20: rgba(46, 139, 214, 0.2);
  /* 管理端身份色(原金色) */
  --app-brand: #e9b949;
  --app-brand-deep: #c89a2e;
  --app-brand-soft: #f7d98c;
  --app-brand-ink: #8a6414;
  --app-brand-tint: #fef3c7;
  --app-brand-tint-line: #fde68a;
  --app-brand-hi: #f0c45e;
  --app-brand-on: #241a04;
  --app-brand-wash: #fdf7e7;
  --app-brand-glow: rgba(233, 185, 73, 0.35);
  --app-brand-a12: rgba(233, 185, 73, 0.12);
  --app-brand-a14: rgba(233, 185, 73, 0.14);
  --app-brand-a45: rgba(233, 185, 73, 0.45);
  /* 深色导航面 */
  --app-nav-1: #0e1b2e;
  --app-nav-2: #122035;
  --app-nav-3: #0d1929;
  --app-nav-ink: #0e1b2e;
  --app-nav-text: #9fb2c9;
  --app-nav-text-hi: #e7eef7;
  --app-nav-text-active: #f7d98c;
  --app-nav-muted: #5c7290;
  /* 内容面 · 卡片/填充/描边/文字 */
  --app-surface: #ffffff;
  --app-surface-2: #f8fafc;
  --app-surface-3: #f1f5f9;
  --app-chip: #e2e8f0;
  --app-chip-soft: rgba(226, 232, 240, 0.4);
  /* 压在彩色/图片面上的浅色磨砂块:深色主题要转成低透明度白纱 */
  --app-wash-strong: rgba(255, 255, 255, 0.8);
  --app-wash-hover: rgba(255, 255, 255, 0.7);
  --app-border: #e2e8f0;
  --app-border-soft: #eef2f6;
  --app-border-strong: #cbd5e1;
  --app-overlay-border: #e8edf3;
  /* 语义色:状态/分类用色,按色相分组,每组 底色/描边/前景 三档 */
  --app-emerald-bg: #d1fae5;
  --app-emerald-line: #a7f3d0;
  --app-emerald-fg: #047857;
  --app-rose-bg: #ffe4e6;
  --app-rose-line: #fecdd3;
  --app-rose-fg: #be123c;
  --app-amber-bg: #fef3c7;
  --app-amber-line: #fde68a;
  --app-amber-fg: #b45309;
  --app-orange-bg: #ffedd5;
  --app-orange-line: #fed7aa;
  --app-orange-fg: #c2410c;
  --app-violet-bg: #ede9fe;
  --app-violet-line: #ddd6fe;
  --app-violet-fg: #6555b0;
  --app-blue-bg: #dbeafe;
  --app-blue-line: #bfdbfe;
  --app-blue-fg: #1d4ed8;
  /* 横幅玻璃层:压在渐变上的半透明块与其上的文字 */
  --app-glass-bg: rgb(255 255 255 / 45%);
  --app-glass-bg-strong: rgb(255 255 255 / 50%);
  --app-glass-line: rgb(255 255 255 / 70%);
  --app-hero-divide: rgb(131 201 244 / 60%);
  --app-on-hero: rgb(0 85 118 / 85%);
  --app-on-hero-soft: rgb(0 85 118 / 75%);
  --app-heading: #0f1e33;
  --app-text: #1e293b;
  --app-text-2: #334155;
  --app-text-3: #475569;
  --app-text-4: #64748b;
  --app-text-5: #94a3b8;
  --app-text-6: #cbd5e1;
  /* 学员端扩展色板:学员页面原本逐处写死,统一收进 token 后
     才能跟着主题走。基线值与原字面量完全一致,青蓝态零变化。 */
  /* 实底填充上的前景(按钮/徽章文字) */
  --app-on-fill: #ffffff;
  /* 深色蒙版上的前景(视频控件、图片角标):各主题恒为浅色,不随 on-fill 反转 */
  --app-on-scrim: #ffffff;
  /* 中性墨色,由深到浅 */
  --app-ink-pure: #000000;
  --app-ink-900: #0f172a;
  --app-ink-850: #111c2d;
  --app-ink-800: #172b4d;
  --app-ink-solid: #1e293b;
  --app-ink-700: #173b56;
  --app-ink-night: #0b132b;
  --app-slate-500: #58758a;
  --app-slate-400: #7b8798;
  --app-slate-300: #a0aab8;
  /* 蓝系浅底 */
  --app-blue-25: #f4faff;
  --app-blue-25b: #fbfdff;
  --app-blue-50: #f0f9ff;
  --app-blue-70: #e8eef5;
  --app-blue-75: #eaf4fb;
  --app-blue-80: #ebf3fa;
  --app-blue-85: #eef4fb;
  --app-blue-90: #e5eff9;
  --app-blue-100: #e0f2fe;
  --app-blue-line-100: #dbe5ed;
  --app-blue-line-110: #e2eaf4;
  --app-blue-line-120: #d3e4f3;
  --app-blue-line-200: #bae6fd;
  /* 品牌蓝的中间档 */
  --app-accent-hover-strong: #6fb9e8;
  --app-accent-sky-400: #63b8e8;
  --app-accent-ink: #0879a5;
  --app-accent-ink-2: #1683a8;
  /* 紫 */
  --app-violet: #9381ff;
  --app-violet-strong: #7866e0;
  --app-violet-deep: #7c3aed;
  --app-violet-50: #f5f3ff;
  --app-violet-line-100: #d8d5f2;
  /* 绿 */
  --app-emerald-strong: #059669;
  --app-emerald-solid: #10b981;
  --app-emerald-deep: #065f46;
  --app-emerald-soft: #ecfdf5;
  --app-green-600: #16a34a;
  --app-green-300: #86efac;
  --app-green-50: #f0fdf4;
  /* 红/玫红 */
  --app-red-800: #991b1b;
  --app-red-700: #b91c1c;
  --app-red-600: #dc2626;
  --app-red-500: #ef4444;
  --app-red-400: #f87171;
  --app-red-300: #fca5a5;
  --app-red-100: #fee2e2;
  --app-red-50: #fef2f2;
  --app-rose-600: #e11d48;
  --app-rose-500: #f43f5e;
  --app-rose-100: #fff1f2;
  /* 琥珀 */
  --app-amber-strong: #d97706;
  --app-amber-solid: #fbbf24;
  --app-amber-deep: #92400e;
  --app-amber-50: #fffbeb;
  /* 个人中心横幅:整块横幅(渐变底/墨色/玻璃块)一起换,曜黑下转成深蓝面板 */
  --app-hero-1: var(--app-accent);
  --app-hero-2: var(--app-accent-border);
  --app-hero-3: var(--app-accent-soft);
  --app-hero-ink: var(--app-accent-deep);
  --app-hero-ink-75: rgb(0 85 118 / 75%);
  --app-hero-ink-80: rgb(0 85 118 / 80%);
  --app-hero-ink-85: rgb(0 85 118 / 85%);
  --app-hero-glass: rgb(255 255 255 / 45%);
  --app-hero-glass-strong: rgb(255 255 255 / 50%);
  --app-hero-glass-line: rgb(255 255 255 / 70%);
  --app-hero-orb-line: rgb(255 255 255 / 38%);
  --app-hero-orb-ring-a: rgb(255 255 255 / 12%);
  --app-hero-orb-ring-b: rgb(255 255 255 / 8%);
  --app-hero-violet-wash: rgb(147 129 255 / 12%);
  --app-hero-stat-div: rgb(131 201 244 / 60%);
  /* 提示小块:浅蓝底 → 曜黑转深 */
  --app-note-wash: rgb(240 249 255 / 70%);
  /* 焦点环 */
  --app-ring-accent: rgb(0 85 118 / 25%);
  /* 主按钮 hover 底:浅色主题用浅蓝描边色,曜黑要保持在强调色一侧 */
  --app-btn-primary-hover: var(--app-accent-border);
  /* 页面底 */
  --app-page-bg: #f4f9fd;
}
.theme-black {
  /* 内容面:深色卡片层,页面底比卡片更暗 */
  --app-surface: #16171c;
  --app-surface-2: #1b1d23;
  --app-surface-3: #23262e;
  --app-chip: #2a2f38;
  --app-chip-soft: rgba(42, 47, 56, 0.4);
  --app-wash-strong: rgba(255, 255, 255, 0.08);
  --app-wash-hover: rgba(255, 255, 255, 0.07);
  --app-border: #2a2c33;
  --app-border-soft: #23252b;
  --app-border-strong: #3a4150;
  --app-overlay-border: #333945;
  /* 语义色反转:浅底→深底,深前景→亮前景,色相含义不变 */
  --app-emerald-bg: #12291f;
  --app-emerald-line: #1e4a38;
  --app-emerald-fg: #6ee7b7;
  --app-rose-bg: #2b161c;
  --app-rose-line: #4e222c;
  --app-rose-fg: #fda4af;
  --app-amber-bg: #2b2314;
  --app-amber-line: #4e3d1c;
  --app-amber-fg: #fcd34d;
  --app-orange-bg: #2b1d14;
  --app-orange-line: #4e341e;
  --app-orange-fg: #fdba74;
  --app-violet-bg: #1e1b2c;
  --app-violet-line: #383250;
  --app-violet-fg: #c4b5fd;
  --app-blue-bg: #0d2431;
  --app-blue-line: #155e75;
  --app-blue-fg: #7dd3fc;
  /* 曜黑下横幅是深银渐变,玻璃块要从"透白"变"透微白",文字翻亮 */
  --app-glass-bg: rgb(255 255 255 / 7%);
  --app-glass-bg-strong: rgb(255 255 255 / 10%);
  --app-glass-line: rgb(255 255 255 / 16%);
  --app-hero-divide: rgb(255 255 255 / 14%);
  --app-on-hero: #eef1f5;
  --app-on-hero-soft: #c3cad4;
  --app-heading: #f4f5f7;
  --app-text: #e6e8ec;
  --app-text-2: #d3d7de;
  --app-text-3: #b6bcc8;
  --app-text-4: #9aa1ad;
  --app-text-5: #787f8b;
  --app-text-6: #5c626e;
  --app-page-bg: #0e0f12;
  /* 单一亮色:电光青。交互/选中/身份标识全部走它,
     语义色(成功/警告/危险)仍保留各自色相,只有状态标签带颜色。 */
  --app-accent: #38bdf8;
  --app-accent-strong: #0ea5e9;
  --app-accent-deep: #7dd3fc;
  --app-on-accent-solid: #041520;
  --app-on-accent-muted: rgba(4, 21, 32, 0.72);
  --app-accent-soft: #10293a;
  --app-accent-line: #1b435c;
  --app-accent-border: #155e75;
  --app-accent-hover: #12303f;
  --app-accent-focus: #38bdf8;
  --app-accent-dim: rgba(56, 189, 248, 0.2);
  --app-on-accent-fill: #041520;
  --app-accent-fill-hi: #7dd3fc;
  --app-accent-a10: rgba(56, 189, 248, 0.1);
  --app-accent-a12: rgba(56, 189, 248, 0.16);
  --app-accent-a20: rgba(56, 189, 248, 0.24);
  /* 身份色:与强调色同一支,不再另发一种颜色 */
  --app-brand: #38bdf8;
  --app-brand-deep: #0284c7;
  --app-brand-soft: #7dd3fc;
  --app-brand-ink: #7dd3fc;
  --app-brand-tint: #0d2431;
  --app-brand-tint-line: #155e75;
  --app-brand-hi: #7dd3fc;
  --app-brand-on: #041520;
  --app-brand-wash: #0b1e29;
  --app-brand-glow: rgba(56, 189, 248, 0.32);
  --app-brand-a12: rgba(56, 189, 248, 0.13);
  --app-brand-a14: rgba(56, 189, 248, 0.18);
  --app-brand-a45: rgba(56, 189, 248, 0.42);
  /* 导航面 */
  --app-nav-1: #0a0b0d;
  --app-nav-2: #101215;
  --app-nav-3: #08090a;
  --app-nav-ink: #0a0b0d;
  --app-nav-text: #9aa3b0;
  --app-nav-text-hi: #eef2f6;
  --app-nav-text-active: #38bdf8;
  --app-nav-muted: #626a75;
  /* 学员端扩展色板的深色对应值 */
  --app-on-fill: #041520;
  --app-ink-pure: #e6e8ec;
  --app-ink-900: #f1f5f9;
  --app-ink-850: #eef2f6;
  --app-ink-800: #c3cad4;
  --app-ink-solid: #0b0d10;
  --app-ink-700: #9fb2c9;
  --app-ink-night: #0a0b0d;
  --app-slate-500: #9aa3b0;
  --app-slate-400: #a2a9b4;
  --app-slate-300: #787f8b;
  --app-blue-25: #1b1d23;
  --app-blue-25b: #16171c;
  --app-blue-50: #0d2431;
  --app-blue-70: #1b1d23;
  --app-blue-75: #102027;
  --app-blue-80: #102027;
  --app-blue-85: #12232c;
  --app-blue-90: #14262f;
  --app-blue-100: #10293a;
  --app-blue-line-100: #2a2f38;
  --app-blue-line-110: #2a2f38;
  --app-blue-line-120: #1b435c;
  --app-blue-line-200: #155e75;
  --app-accent-hover-strong: #0ea5e9;
  --app-accent-sky-400: #38bdf8;
  --app-accent-ink: #7dd3fc;
  --app-accent-ink-2: #67e8f9;
  --app-violet: #a78bfa;
  --app-violet-strong: #8b5cf6;
  --app-violet-deep: #c4b5fd;
  --app-violet-50: #1e1b2c;
  --app-violet-line-100: #383250;
  --app-emerald-strong: #34d399;
  --app-emerald-solid: #059669;
  --app-emerald-deep: #6ee7b7;
  --app-emerald-soft: #0f2a20;
  --app-green-600: #4ade80;
  --app-green-300: #22c55e;
  --app-green-50: #12291f;
  --app-red-800: #fecaca;
  --app-red-700: #fca5a5;
  --app-red-600: #f87171;
  --app-red-500: #dc2626;
  --app-red-400: #b91c1c;
  --app-red-300: #7f1d1d;
  --app-red-100: #2b161c;
  --app-red-50: #2a1418;
  --app-rose-600: #fb7185;
  --app-rose-500: #fda4af;
  --app-rose-100: #2b161c;
  --app-amber-strong: #fbbf24;
  --app-amber-solid: #d97706;
  --app-amber-deep: #fcd34d;
  --app-amber-50: #2b2314;
  /* 个人中心横幅:曜黑下改成深蓝面板,文字转亮 */
  --app-hero-1: #12303f;
  --app-hero-2: #0f2833;
  --app-hero-3: #10293a;
  --app-hero-ink: #7dd3fc;
  --app-hero-ink-75: rgb(125 211 252 / 75%);
  --app-hero-ink-80: rgb(125 211 252 / 80%);
  --app-hero-ink-85: rgb(125 211 252 / 85%);
  --app-hero-glass: rgba(255, 255, 255, 0.06);
  --app-hero-glass-strong: rgba(255, 255, 255, 0.09);
  --app-hero-glass-line: rgba(255, 255, 255, 0.16);
  --app-hero-orb-line: rgba(125, 211, 252, 0.16);
  --app-hero-orb-ring-a: rgba(125, 211, 252, 0.05);
  --app-hero-orb-ring-b: rgba(125, 211, 252, 0.03);
  --app-hero-violet-wash: rgba(167, 139, 250, 0.16);
  --app-hero-stat-div: rgba(255, 255, 255, 0.14);
  --app-note-wash: rgba(13, 36, 49, 0.7);
  --app-ring-accent: rgba(125, 211, 252, 0.35);
  --app-btn-primary-hover: #0ea5e9;
}
.theme-navy {
  --app-accent: #7b8ea8;
  --app-accent-strong: #4f6a89;
  --app-accent-deep: #24344d;
  --app-accent-soft: #dde5ef;
  --app-accent-line: #b6c4d6;
  --app-accent-border: #b6c4d6;
  --app-accent-hover: #c2d2e4;
  --app-accent-focus: #5f7893;
  --app-accent-dim: rgba(123, 142, 168, 0.18);
  --app-on-accent-fill: #1c2a3f;
  --app-accent-fill-hi: #c6d4e4;
  --app-accent-a10: rgba(79, 106, 137, 0.1);
  --app-accent-a12: rgba(79, 106, 137, 0.12);
  --app-accent-a20: rgba(79, 106, 137, 0.2);
  --app-brand: #9fb6d1;
  --app-brand-deep: #6e86a3;
  --app-brand-soft: #cfe0f2;
  --app-brand-ink: #26364a;
  --app-brand-tint: #e5edf6;
  --app-brand-tint-line: #c6d8ea;
  --app-brand-hi: #b3c8de;
  --app-brand-on: #16212e;
  --app-brand-wash: #f0f4f9;
  --app-brand-glow: rgba(159, 182, 209, 0.35);
  --app-brand-a12: rgba(159, 182, 209, 0.14);
  --app-brand-a14: rgba(159, 182, 209, 0.16);
  --app-brand-a45: rgba(159, 182, 209, 0.45);
  --app-nav-1: #141b24;
  --app-nav-2: #19222d;
  --app-nav-3: #101620;
  --app-nav-ink: #141b24;
  --app-nav-text: #a7b6c8;
  --app-nav-text-hi: #e9f0f8;
  --app-nav-text-active: #cfe0f2;
  --app-nav-muted: #6b7d92;
  /* 石墨:语义色降饱和,保持浅底深字 */
  --app-emerald-bg: #e6efe9;
  --app-emerald-line: #c9dccf;
  --app-emerald-fg: #2f6b52;
  --app-rose-bg: #f2e7e9;
  --app-rose-line: #dcc6cb;
  --app-rose-fg: #98394f;
  --app-amber-bg: #f1ece0;
  --app-amber-line: #dbd0b6;
  --app-amber-fg: #7c5f24;
  --app-orange-bg: #f1e9e2;
  --app-orange-line: #dbcabd;
  --app-orange-fg: #864f2f;
  --app-violet-bg: #eae9f1;
  --app-violet-line: #ccc8dd;
  --app-violet-fg: #4e4a7e;
  --app-blue-bg: #e6ebf2;
  --app-blue-line: #c8d2e0;
  --app-blue-fg: #35547f;
  --app-on-hero: rgb(36 52 77 / 88%);
  --app-on-hero-soft: rgb(36 52 77 / 76%);
  --app-page-bg: #eef2f7;
}

/* ==================== 03 登录视图 ==================== */
.app-login {
  position: relative;
  min-height: 100vh;
  background: var(--app-page-bg, #f4f9fd);
}

/* ==================== 04 应用外壳 ==================== */
.app-shell {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background: var(--app-page-bg);
  color: var(--app-text);
  -webkit-font-smoothing: antialiased;
}

.app-body {
  display: flex;
  flex: 1;
  width: 100%;
  min-height: 0;
  overflow: hidden;
}

/* ==================== 05 主工作区与视图画布 ==================== */
.app-workspace {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
}

.app-viewport {
  flex: 1;
  width: 100%;
  max-width: 80rem;
  min-height: 0;
  margin-left: auto;
  margin-right: auto;
  padding: 1.5rem;
  overflow-y: auto;
}

/* 管理端:允许整页上下滚动,内容按自然高度铺开 */
#app .app-viewport--admin {
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  padding-top: 10px;
  /* 分页条自然排布,页面底部留白统一控制 */
  padding-bottom: 0;
  /* 始终预留滚动条位置，页面切换时内容不会左右抖动 */
  scrollbar-gutter: stable;
  scrollbar-width: thin;
  scrollbar-color: var(--app-border-strong, #cbd5e1) transparent;
}
#app .app-viewport--admin::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}
#app .app-viewport--admin::-webkit-scrollbar-track {
  background: transparent;
}
#app .app-viewport--admin::-webkit-scrollbar-thumb {
  background: var(--app-border-strong, #cbd5e1);
  background-clip: content-box;
  border: 2px solid transparent;
  border-radius: 999px;
}
#app .app-viewport--admin::-webkit-scrollbar-thumb:hover {
  background: var(--app-text-5, #94a3b8);
  background-clip: content-box;
}

/* 管理端视图至少占满可用高度,内容超出时由整个画布自然滚动 */
#app .app-viewport--admin > .view-transition {
  display: flex;
  flex: 1 0 auto;
  flex-direction: column;
  height: auto;
  min-height: 100%;
}
#app .app-viewport--admin .view-transition > * {
  min-height: 0;
  height: auto;
}
#app .app-viewport--admin .admin-fit-page {
  flex: 1 0 auto;
  gap: 12px;
}

/* 列表按内容自然展开,纵向滚动交给整个管理端画布 */
#app .app-viewport--admin .table-wrap,
#app .app-viewport--admin .notice-list,
#app .app-viewport--admin .question-list {
  flex: 0 0 auto;
  min-height: 0;
}

/* 分页页至少铺满画布,数据较多时由画布滚动,不再裁切或单独滚动列表 */
#app .app-viewport--admin > .view-transition > .admin-fit-page {
  height: auto;
  min-height: 100%;
  overflow: visible;
}
#app .app-viewport--admin .admin-fit-page > :not(.table-wrap):not(.question-list):not(.notice-list) {
  flex: 0 0 auto;
}
#app .app-viewport--admin .admin-fit-page .table-wrap,
#app .app-viewport--admin .admin-fit-page .question-list,
#app .app-viewport--admin .admin-fit-page .notice-list {
  flex: 0 0 auto;
  min-height: 0;
}
#app .app-viewport--admin .admin-fit-page .table-wrap {
  overflow-y: hidden;
}
#app .app-viewport--admin .admin-fit-page .table-wrap .data-table.data-table--wide {
  min-width: 900px;
}

/* 分页条在列表后自然排布,随页面内容滚动 */
#app .app-viewport--admin .pager {
  position: static;
  padding-bottom: 1.25rem;
}

/* 管理总览没有分页条，单独补底部留白 */
#app .app-viewport--admin .admin-dashboard {
  padding-bottom: 1.25rem;
}
.app-view-enter-active {
  transition: opacity 300ms ease-out, transform 300ms ease-out;
}
.app-view-enter-from {
  opacity: 0;
  transform: translateY(6px);
}
.view-enter-active {
  transition: opacity 420ms cubic-bezier(0.22, 1, 0.36, 1),
    transform 420ms cubic-bezier(0.22, 1, 0.36, 1);
}
.view-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

/* 管理端页面进入时只做淡入，不做位移：
   位移会让整个视图短暂超出滚动容器底部，触发滚动条闪现 */
#app .app-viewport--admin .view-enter-from {
  transform: none;
}
#app .app-viewport--admin .view-enter-active {
  transition: opacity 300ms ease-out;
}
.view-enter-active:not(.view-transition--home) :is(
  h1,
  h2,
  h3,
  p,
  article,
  .metric-card,
  .course-card,
  .side-panel,
  .profile-card,
  .att-panel,
  .certificate-card,
  .crs-card,
  .exm-card
) {
  animation: view-content-in 480ms ease-out both;
}
.view-enter-active:not(.view-transition--home) :is(
  h1,
  h2,
  h3,
  p,
  article,
  .metric-card,
  .course-card,
  .side-panel,
  .profile-card,
  .att-panel,
  .certificate-card,
  .crs-card,
  .exm-card
):nth-child(2) {
  animation-delay: 45ms;
}
.view-enter-active:not(.view-transition--home) :is(
  h1,
  h2,
  h3,
  p,
  article,
  .metric-card,
  .course-card,
  .side-panel,
  .profile-card,
  .att-panel,
  .certificate-card,
  .crs-card,
  .exm-card
):nth-child(3) {
  animation-delay: 90ms;
}
.view-enter-active:not(.view-transition--home) :is(
  h1,
  h2,
  h3,
  p,
  article,
  .metric-card,
  .course-card,
  .side-panel,
  .profile-card,
  .att-panel,
  .certificate-card,
  .crs-card,
  .exm-card
):nth-child(4) {
  animation-delay: 135ms;
}
@keyframes view-content-in {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
@media (min-width: 768px) {
  .app-viewport { padding: 2rem; }
}

/* ==================== 05b 曜黑:共享组件内的浅底语义块 ====================
   ConfirmDialog 学员端也在用,组件里保留原色;
   只在管理端深色主题下用更高优先级覆盖,把浅底深字翻成深底亮字。 */
.app-shell.theme-black .cf-icon--danger,
.app-shell.theme-black .cf-tip--danger {
  background: var(--app-rose-bg);
  color: var(--app-rose-fg);
}
.app-shell.theme-black .cf-tip--danger { border-color: var(--app-rose-line); }
.app-shell.theme-black .cf-icon--success,
.app-shell.theme-black .cf-tip--success {
  background: var(--app-emerald-bg);
  color: var(--app-emerald-fg);
}
.app-shell.theme-black .cf-tip--success { border-color: var(--app-emerald-line); }
.app-shell.theme-black .cf-icon--primary,
.app-shell.theme-black .cf-tip--primary,
.app-shell.theme-black .cf-tip--warning {
  background: var(--app-amber-bg);
  color: var(--app-amber-fg);
}
.app-shell.theme-black .cf-tip--primary,
.app-shell.theme-black .cf-tip--warning { border-color: var(--app-amber-line); }
.app-shell.theme-black .cf-backdrop { background: rgba(0, 0, 0, 0.62); }
/* 分页当前页压在品牌渐变上,浅色主题是金字深底,曜黑下青底必须换深墨 */
.app-shell.theme-black .pager-btn--active { color: var(--app-brand-on); }

/* ==================== 05c 管理端响应式布局 ====================
   屏幕变窄时:统计条自动换行、工具栏纵向排布、表格横向滚动、
   侧边栏收成图标栏,保证管理端各页在小屏下仍然整齐不挤。 */

/* --- 中等屏:收紧内边距,统计卡自动换行 --- */
@media (max-width: 1280px) {
  #app .app-viewport--admin {
    padding-left: 1.25rem;
    padding-right: 1.25rem;
  }
  #app .app-viewport--admin .overview-strip {
    grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  }
}

/* --- 窄屏:侧边栏收成图标栏,给内容让出宽度 --- */
@media (max-width: 1080px) {
  #app .app-viewport--admin {
    padding-left: 1rem;
    padding-right: 1rem;
  }
  /* 窄屏下压缩表格内边距，尽量让常见列数在可视区内放下 */
  #app .app-viewport--admin .data-table {
    font-size: 12.5px;
  }
  #app .app-viewport--admin .data-table thead th,
  #app .app-viewport--admin .data-table tbody td {
    padding-left: 10px;
    padding-right: 10px;
    padding-top: 8px;
    padding-bottom: 8px;
  }
  /* 表格横向滚动条做细一点，不抢内容 */
  #app .app-viewport--admin .table-wrap {
    scrollbar-width: thin;
  }
  #app .app-viewport--admin .table-wrap::-webkit-scrollbar {
    height: 8px;
  }
  #app .app-viewport--admin .table-wrap::-webkit-scrollbar-thumb {
    background: var(--app-border-strong, #cbd5e1);
    border-radius: 999px;
  }
  #app .admin-sidebar {
    width: 76px;
  }
  #app .admin-sidebar .brand-header {
    justify-content: center;
    padding: 16px 8px;
  }
  #app .admin-sidebar .brand-text,
  #app .admin-sidebar .nav-label,
  #app .admin-sidebar .nav-section-label,
  #app .admin-sidebar .admin-card-text {
    display: none;
  }
  #app .admin-sidebar .nav-menu {
    padding: 12px 8px;
  }
  #app .admin-sidebar .nav-item {
    justify-content: center;
    padding: 11px 0;
    border-left-width: 0;
  }
  #app .admin-sidebar .nav-item-left {
    gap: 0;
  }
  #app .admin-sidebar .sidebar-footer {
    padding: 8px;
  }
  #app .admin-sidebar .admin-card {
    flex-direction: column;
    gap: 6px;
    padding: 8px 6px;
  }
  #app .admin-sidebar .logout-btn {
    margin-left: 0;
  }
}

/* --- 小屏:页头/工具栏/统计条纵向或两列排布,表格横向滚动 --- */
@media (max-width: 820px) {
  #app .app-viewport--admin {
    padding: 10px 0.75rem 0;
  }
  #app .app-viewport--admin .page-head {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  #app .app-viewport--admin .page-head > * {
    width: 100%;
  }
  #app .app-viewport--admin .page-title {
    font-size: 20px;
  }
  #app .app-viewport--admin .page-sub {
    font-size: 12px;
  }
  #app .app-viewport--admin .overview-strip,
  #app .app-viewport--admin .stat-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 10px;
  }
  #app .app-viewport--admin .overview-item {
    padding: 10px;
  }
  #app .app-viewport--admin .overview-value {
    font-size: 18px;
  }
  #app .app-viewport--admin .toolbar {
    gap: 8px;
    padding: 10px;
  }
  #app .app-viewport--admin .toolbar .search-box {
    flex: 1 1 100%;
    min-width: 0;
  }
  #app .app-viewport--admin .toolbar-result {
    margin-left: 0;
  }
  #app .app-viewport--admin .filter-tabs {
    gap: 6px;
  }
  #app .app-viewport--admin .filter-tab {
    padding: 6px 10px;
    font-size: 12px;
  }
  #app .app-viewport--admin .table-wrap {
    border-radius: 10px;
    overflow-x: auto;
  }
  #app .app-viewport--admin .data-table {
    min-width: 720px;
  }
  #app .app-viewport--admin .data-table thead th,
  #app .app-viewport--admin .data-table tbody td {
    padding: 7px 10px;
  }
  #app .app-viewport--admin .pager {
    gap: 8px;
  }
}

/* --- 超窄屏:留白再收紧,分页按钮允许换行 --- */
@media (max-width: 560px) {
  #app .app-viewport--admin {
    padding-left: 0.625rem;
    padding-right: 0.625rem;
  }
  #app .app-viewport--admin .pager-controls {
    flex-wrap: wrap;
  }
  #app .app-header--admin {
    padding: 0 14px;
  }
  .app-header--admin .user-meta,
  .app-header--admin .admin-tag {
    display: none;
  }
}

/* --- 用户端:桌面使用完整宽度,窄屏导航改为可横向浏览的菜单栏 --- */
#app .app-viewport--user {
  width: 100%;
  max-width: none;
  padding: clamp(12px, 2vw, 32px);
  overflow-x: hidden;
}

#app .app-viewport--user > .view-transition {
  display: flex;
  flex: 1 0 auto;
  flex-direction: column;
  min-height: 100%;
}

/* --- 用户端中等窄屏:侧栏收成与管理端相同的图标栏 --- */
@media (max-width: 1080px) and (min-width: 821px) {
  #app .app-body--user .sidebar {
    width: 76px;
  }
  #app .app-body--user .brand-header {
    justify-content: center;
    padding: 16px 8px;
  }
  #app .app-body--user .brand-text,
  #app .app-body--user .nav-label,
  #app .app-body--user .user-card-text {
    display: none;
  }
  #app .app-body--user .nav-menu {
    padding: 12px 8px;
  }
  #app .app-body--user .nav-item {
    justify-content: center;
    padding: 11px 0;
  }
  #app .app-body--user .nav-item-left {
    gap: 0;
  }
  #app .app-body--user .sidebar-footer {
    padding: 8px;
  }
  #app .app-body--user .user-card {
    flex-direction: column;
    gap: 6px;
    padding: 8px 6px;
  }
  #app .app-body--user .logout-btn {
    margin-left: 0;
  }
}

@media (max-width: 820px) {
  #app .app-body--user .sidebar {
    width: 64px;
    height: 100%;
  }
  #app .app-body--user .brand-header {
    justify-content: center;
    padding: 14px 6px;
  }
  #app .app-body--user .brand-text,
  #app .app-body--user .nav-label,
  #app .app-body--user .user-card-text {
    display: none;
  }
  #app .app-body--user .nav-menu {
    flex-direction: column;
    gap: 4px;
    padding: 10px 6px;
  }
  #app .app-body--user .nav-item {
    justify-content: center;
    padding: 12px 0;
  }
  #app .app-body--user .nav-item-left { gap: 0; }
  #app .app-body--user .sidebar-footer { padding: 6px; }
  #app .app-body--user .user-card {
    flex-direction: column;
    gap: 6px;
    padding: 8px 4px;
  }
  #app .app-body--user .logout-btn { margin-left: 0; }
}

@media (max-width: 560px) {
  #app .app-viewport--user {
    padding: 12px;
  }
}

/* --- 矮屏:适度收紧纵向间距,减少首屏滚动距离 --- */
@media (max-height: 800px) {
  #app .app-viewport--admin .view-transition > * {
    gap: 10px;
  }
  #app .app-viewport--admin .admin-fit-page {
    gap: 8px;
  }
  #app .app-viewport--admin .page-title {
    font-size: 21px;
  }
  #app .app-viewport--admin .page-sub {
    margin-top: 2px;
  }
  #app .app-viewport--admin .overview-strip,
  #app .app-viewport--admin .stat-strip {
    gap: 10px;
  }
  #app .app-viewport--admin .overview-item,
  #app .app-viewport--admin .stat-item {
    padding: 10px 12px;
  }
  #app .app-viewport--admin .overview-value {
    font-size: 19px;
  }
  #app .app-viewport--admin .data-table thead th {
    padding-top: 6px;
    padding-bottom: 6px;
  }
  #app .app-viewport--admin .data-table tbody td {
    padding-top: 6px;
    padding-bottom: 6px;
  }
  #app .app-viewport--admin .question-item {
    gap: 8px;
    padding: 11px 12px;
  }
  #app .app-viewport--admin .notice-card {
    gap: 10px;
    padding: 9px 14px;
  }
  #app .app-viewport--admin .notice-list {
    gap: 6px;
  }
  #app .app-viewport--admin .toolbar {
    padding: 9px 12px;
  }
  #app .app-viewport--admin .pager {
    padding-top: 0;
    padding-bottom: 10px;
  }
}

/* --- 弹窗:矮屏/窄屏下顶部对齐并可滚动,内容不会溢出屏幕 --- */
@media (max-width: 640px), (max-height: 720px) {
  html body .modal-backdrop.modal-backdrop {
    align-items: flex-start;
    overflow-y: auto;
    padding: 14px;
  }
  html body .modal.modal {
    max-height: calc(100vh - 28px);
    overflow-y: auto;
  }
}

/* ==================== 06 无障碍：减少动效 ==================== */
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
    scroll-behavior: auto !important;
  }
}
</style>