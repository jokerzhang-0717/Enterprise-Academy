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
    :class="isAdmin ? 'theme-' + theme : 'theme-cyan'"
  >
    <div class="app-body">

      <!-- 侧边导航:管理员 / 学员 -->
      <AdminSidebar
        v-if="isAdmin"
        :current-view="currentView"
        :user="currentUser"
        :draft-notice-count="draftNoticeCount"
        :active-exam-count="activeExamCount"
        :pending-leave-count="pendingLeaveCount"
        :pending-cert-count="pendingCertCount"
        @select-view="handleNavigate"
        @logout="handleLogout"
      />
      <Sidebar
        v-else
        :current-view="currentView"
        :theme="theme"
        :user="currentUser"
        :unread-messages-count="3"
        :active-exams-count="1"
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
          :unread-count="3"
          :search-results="searchResults"
          @navigate="handleNavigate"
          @cycle-theme="cycleTheme"
          @search="globalSearchQuery = $event"
          @select-search-result="handleSelectSearchResult"
          @open-mockup-viewer="handleOpenMockupForCurrentView"
          @toggle-check-in="isCheckedIn = !isCheckedIn"
        />

        <!-- 视图画布 -->
        <main
          ref="mainContent"
          class="app-viewport"
          :class="{ 'app-viewport--admin': isAdmin && currentView !== 'admin-profile' }"
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
                @navigate="handleNavigate"
                @open-track="handleOpenTrack"
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
                :search-target-id="searchTargetId"
                @navigate="handleNavigate"
                @open-certificate-modal="handleOpenCertificate"
              />

              <ExamTakingView
                v-else-if="currentView === 'exam-taking'"
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
      :score="examScore"
      :pass-score="80"
      :total-score="100"
      :answered-count="24"
      :total-questions="25"
      exam-title="2025Q2 全栈开发规范与安全编码认证考试"
      @close="handleCloseExamResult"
      @view-certificate="handleViewCertFromExam"
      @retake="handleRetakeExam"
    />
  </div>
  </Transition>
</template>

<script setup>
import { computed, ref, watchEffect } from 'vue';
import {
  currentUserZhang,
  currentUserLi,
  currentUserAdmin,
  ADMIN_ANNOUNCEMENTS_DATA,
  ADMIN_LEAVE_REQUESTS,
  ADMIN_CERTIFICATE_ISSUES,
  CERTIFICATES_DATA,
  COURSES_DATA,
  EXAMS_DATA,
  PAST_EXAMS,
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
import { useAdminTableFit } from './composables/useAdminTableFit.js';

// Navigation State
const currentView = ref('home');
const mainContent = ref(null);
const currentUser = ref(currentUserLi);
const theme = ref('cyan');
const isCheckedIn = ref(true);
const selectedTrack = ref(null);
const selectedMessageId = ref('msg-1');
const selectedCourseId = ref('');
const globalSearchQuery = ref('');
const searchTargetId = ref('');

// Modals State
const isMockupViewerOpen = ref(false);
const mockupInitialKey = ref('home');
const selectedCert = ref(null);

const isExamResultOpen = ref(false);
const examScore = ref(95);

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
    id: 'home-schedule-1',
    type: '待办日程',
    title: '《2025数据安全与合规》全员考核系统关闭',
    description: '5月17日 18:00 · 考核截止',
    searchText: '2025数据安全与合规 全员考核 系统关闭 5月17日 考核截止 线上机考 必修',
    view: 'home',
    targetId: 'home-schedule-1',
  },
  {
    id: 'home-schedule-2',
    type: '待办日程',
    title: '云原生全链路可观测性落地工作坊',
    description: '5月19日 14:30 · 线上直播研讨',
    searchText: '云原生 全链路 可观测性 工作坊 5月19日 线上直播研讨 腾讯会议 多媒体教室',
    view: 'home',
    targetId: 'home-schedule-2',
  },
  {
    id: 'home-schedule-3',
    type: '待办日程',
    title: 'PMP & 敏捷管理内训资质证书年度换发',
    description: '5月21日 10:00 · 证书复核',
    searchText: 'PMP 敏捷管理 内训 资质证书 年度换发 5月21日 证书复核 继续教育学时证明',
    view: 'home',
    targetId: 'home-schedule-3',
  },
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
    title: '2025「智训内训师」金牌讲师团第二期招募启动',
    description: '讲师招募 · 2025-05-11',
    searchText: '智训 内训师 金牌讲师团 第二期 招募 课程研发津贴',
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
  currentView.value = view;
  mainContent.value?.scrollTo({ top: 0, behavior: 'auto' });
};

const { show: showToast } = useToast();
const THEME_LABELS = { cyan: '青蓝', black: '曜黑', navy: '石墨' };

const cycleTheme = () => {
  const keys = Object.keys(THEME_LABELS);
  theme.value = keys[(keys.indexOf(theme.value) + 1) % keys.length];
  showToast(`已切换主题：${THEME_LABELS[theme.value]}`, 'success');
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
};

const isAdmin = computed(() => currentUser.value.role === 'admin');

useAdminTableFit(mainContent, currentView);

// ToastStack 通过 Teleport 挂在 body 下,取不到 .app-shell 的覆盖变量,
// 所以把当前主题类同步一份到 body,让浮层跟随管理端主题。
watchEffect(() => {
  document.body.classList.remove('theme-black', 'theme-navy');
  if (isAdmin.value && currentView.value !== 'login' && theme.value !== 'cyan') {
    document.body.classList.add('theme-' + theme.value);
  }
});

const draftNoticeCount = computed(
  () => ADMIN_ANNOUNCEMENTS_DATA.filter(a => a.status === 'draft').length
);

const activeExamCount = computed(() => EXAMS_DATA.length);

const pendingLeaveCount = computed(
  () => ADMIN_LEAVE_REQUESTS.filter(r => r.status === 'pending').length
);

const pendingCertCount = computed(
  () => ADMIN_CERTIFICATE_ISSUES.filter(c => c.status === 'pending').length
);
const handleLoginSuccess = (payload) => {
  const employeeId = payload?.employeeId || '';
  if (employeeId === currentUserAdmin.employeeId) {
    currentUser.value = currentUserAdmin;
    currentView.value = 'admin-dashboard';
  } else {
    currentUser.value = employeeId === currentUserZhang.employeeId ? currentUserZhang : currentUserLi;
    currentView.value = 'home';
  }
};

const handleLogout = () => {
  currentUser.value = currentUserLi;
  currentView.value = 'login';
};

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

const handleSubmitExam = (score) => {
  examScore.value = score;
  isExamResultOpen.value = true;
};

const handleCloseExamResult = () => {
  isExamResultOpen.value = false;
  currentView.value = 'exams';
};

const handleViewCertFromExam = () => {
  isExamResultOpen.value = false;
  handleOpenCertificate('cert-1');
};

const handleRetakeExam = () => {
  isExamResultOpen.value = false;
  currentView.value = 'exam-taking';
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
  background: #f4f9fd;
  color: #1e293b;
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
  /* 页面底 */
  --app-page-bg: #f4f9fd;
}
.theme-black {
  /* 内容面:深色卡片层,页面底比卡片更暗 */
  --app-surface: #16171c;
  --app-surface-2: #1b1d23;
  --app-surface-3: #23262e;
  --app-chip: #2a2f38;
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
  overflow-y: auto;
  padding-top: calc(0.75rem + 10px);
  /* 底部留白交给吸底分页条自己处理，保证分页条吸底位置与自然位置一致 */
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

/* 管理端:视图容器给一个确定高度,页面根节点才能用 min-height:100%
   撑满视口,保证分页条始终贴在内容底部、不随数据条数上下跳 */
#app .app-viewport--admin > .view-transition {
  height: 100%;
}
#app .app-viewport--admin .view-transition > * {
  min-height: 0;
  height: auto;
}

/* 列表区域不再内部滚动,跟随整页一起滚动 */
#app .app-viewport--admin .table-wrap,
#app .app-viewport--admin .notice-list,
#app .app-viewport--admin .question-list {
  flex: 0 0 auto;
  min-height: 0;
}

/* 分页页:整页锁在一屏内,列表卡片吃掉剩余高度把分页条顶到屏幕底部,
   行内边距由 useAdminTableFit 按比例压缩,保证当前页数据一屏看全 */
#app .app-viewport--admin > .view-transition > .admin-fit-page {
  height: 100%;
  overflow: hidden;
}
#app .app-viewport--admin .admin-fit-page > :not(.table-wrap):not(.question-list):not(.notice-list) {
  flex: 0 0 auto;
}
#app .app-viewport--admin .admin-fit-page .table-wrap,
#app .app-viewport--admin .admin-fit-page .question-list,
#app .app-viewport--admin .admin-fit-page .notice-list {
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
}
#app .app-viewport--admin .admin-fit-page .data-table thead th,
#app .app-viewport--admin .admin-fit-page .data-table tbody td {
  padding-top: var(--row-pad, 12px);
  padding-bottom: var(--row-pad, 12px);
}
#app .app-viewport--admin .admin-fit-page .question-item {
  padding-top: var(--row-pad, 14px);
  padding-bottom: var(--row-pad, 14px);
}
#app .app-viewport--admin .admin-fit-page .notice-card {
  padding-top: var(--row-pad, 18px);
  padding-bottom: var(--row-pad, 18px);
}

/* 管理端分页条：紧跟在列表后面，页面不留大片空白；
   内容超高时吸底，滚动中也能随时翻页 */
#app .app-viewport--admin .pager {
  position: sticky;
  bottom: 0;
  z-index: 6;
  margin-top: 0;
  padding-top: 0;
  padding-bottom: 1.25rem;
  background: var(--app-page-bg, #f4f9fd);
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
    padding: calc(0.75rem + 10px) 0.75rem 0;
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
    padding: 10px 12px;
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
  #app .app-viewport--admin .data-table {
    min-width: 640px;
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

/* --- 矮屏:收紧纵向留白,让 6 行列表也能一屏放下,避免整页出现滚动条 ---
   仅在窗口高度 ≤800px 时生效,大屏保持原有宽松间距 --- */
@media (max-height: 800px) {
  #app .app-viewport--admin .view-transition > * {
    gap: 12px;
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
    padding-top: 8px;
    padding-bottom: 8px;
  }
  #app .app-viewport--admin .data-table tbody td {
    padding-top: 7px;
    padding-bottom: 7px;
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