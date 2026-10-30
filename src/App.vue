<template>
  <Transition name="app-view">
  <!-- ============ 1. 登录全屏视图 ============ -->
  <div v-if="currentView === 'login'" class="app-login">
    <LoginView @login-success="currentView = 'home'" />
  </div>

  <!-- ============ 2. 主应用布局 ============ -->
  <div
    v-else
    class="app-shell"
    :class="{
      'theme-purple': theme === 'purple',
      'theme-navy': theme === 'navy',
      'theme-cyan': theme === 'cyan'
    }"
  >
    <div class="app-body">

      <!-- 侧边导航 -->
      <Sidebar
        :current-view="currentView"
        :theme="theme"
        :user="currentUser"
        :unread-messages-count="3"
        :active-exams-count="1"
        @select-view="handleNavigate"
        @change-theme="theme = $event"
        @logout="currentView = 'login'"
        @switch-user="handleSwitchUser"
      />

      <!-- 主内容工作区 -->
      <div class="app-workspace">
        <!-- 顶部栏 -->
        <Header
          :user="currentUser"
          :is-checked-in="isCheckedIn"
          :unread-count="3"
          :search-results="searchResults"
          @switch-user="handleSwitchUser"
          @navigate="handleNavigate"
          @search="globalSearchQuery = $event"
          @select-search-result="handleSelectSearchResult"
          @open-mockup-viewer="handleOpenMockupForCurrentView"
          @toggle-check-in="isCheckedIn = !isCheckedIn"
        />

        <!-- 视图画布 -->
        <main ref="mainContent" class="app-viewport">
          <Transition name="view">
            <div
              :key="currentView"
              class="view-transition"
              :class="{ 'view-transition--home': currentView === 'home' }"
            >
              <HomeView
                v-if="currentView === 'home'"
                :search-target-id="searchTargetId"
                @navigate="handleNavigate"
                @open-track="handleOpenTrack"
              />

              <CoursesView
                v-else-if="currentView === 'courses'"
                :search-target-id="searchTargetId"
                @navigate="handleNavigate"
                @open-certificate-modal="handleOpenCertificate"
              />

              <CoursePlayerView
                v-else-if="currentView === 'course-player'"
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
import { computed, ref } from 'vue';
import {
  currentUserZhang,
  currentUserLi,
  CERTIFICATES_DATA,
  COURSES_DATA,
  EXAMS_DATA,
  PAST_EXAMS,
  MESSAGES_DATA,
  RECOMMENDED_TRACKS,
} from './mockData.js';

// Shared Components
import Sidebar from './components/Sidebar.vue';
import Header from './components/Header.vue';
import MockupViewerModal from './components/MockupViewerModal.vue';
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

// Navigation State
const currentView = ref('home');
const mainContent = ref(null);
const currentUser = ref(currentUserLi);
const theme = ref('cyan');
const isCheckedIn = ref(true);
const selectedTrack = ref(null);
const selectedMessageId = ref('msg-1');
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

const handleOpenMessage = (messageId) => {
  selectedMessageId.value = messageId;
  handleNavigate('messages-detail');
};

const handleSwitchUser = () => {
  if (currentUser.value.id === 'zhanghr') {
    currentUser.value = currentUserLi;
  } else {
    currentUser.value = currentUserZhang;
  }
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
.app-shell {
  --app-accent: #83c9f4;
  --app-accent-deep: #005576;
  --app-accent-soft: #d9f0ff;
  --app-bg: #f4f9fd;
}
.theme-cyan {
  --app-accent: #83c9f4;
  --app-accent-deep: #005576;
  --app-accent-soft: #d9f0ff;
  --app-bg: #f4f9fd;
}
.theme-purple {
  --app-accent: #9381ff;
  --app-accent-deep: #6555b0;
  --app-accent-soft: #ede9fe;
  --app-bg: #f6f4fd;
}
.theme-navy {
  --app-accent: #64748b;
  --app-accent-deep: #1e293b;
  --app-accent-soft: #e2e8f0;
  --app-bg: #f1f5f9;
}

/* ==================== 03 登录视图 ==================== */
.app-login {
  position: relative;
  min-height: 100vh;
  background: var(--app-bg, #f4f9fd);
}

/* ==================== 04 应用外壳 ==================== */
.app-shell {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background: var(--app-bg);
  color: #1e293b;
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