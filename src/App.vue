<template>
  <ToastStack />
  <Transition name="app-view">

  <div v-if="currentView === 'login'" class="app-login">
    <LoginView @login-success="handleLoginSuccess" />
  </div>

  <div
    v-else
    class="app-shell"
    :class="'theme-' + theme"
  >
    <div class="app-body" :class="{ 'app-body--user': !isAdmin }">

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

      <div class="app-workspace">

        <Header
          :user="currentUser"
          :is-admin="isAdmin"
          :is-checked-in="isCheckedIn"
          :unread-count="unreadMessageCount"
          :search-results="searchResults"
          @navigate="handleNavigate"
          @logout="handleLogout"
          @cycle-theme="cycleTheme"
          @search="globalSearchQuery = $event"
          @select-search-result="handleSelectSearchResult"
        />

        <main
          ref="mainContent"
          class="app-viewport"
          :class="{
            'app-viewport--admin': isAdmin && currentView !== 'admin-profile',
            'app-viewport--user': !isAdmin
          }"
        >
          <Transition name="view" mode="out-in" @before-enter="onBeforeViewEnter" @after-enter="onViewEntered">
            <div
              :key="viewTransitionKey"
              class="view-transition"
              :class="{
                'view-transition--home': currentView === 'home',
                'view-transition--instant': skipViewTransition,
              }"
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

              <AdminExamPaperView v-else-if="currentView === 'admin-exam-paper'" />

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
                @password-changed="handlePasswordChanged"
              />

              <HomeView
                v-else-if="currentView === 'home'"
                :search-target-id="searchTargetId"
                :course-id="selectedCourseId"
                :user="currentUser"
                @navigate="handleNavigate"
                @open-course="handleOpenCourse"
                @open-message="handleOpenMessage"
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
                :user="currentUser"
                @back="handleNavigate('courses')"
                @navigate="handleNavigate"
              />

              <ExamsView
                v-else-if="currentView === 'exams' || currentView === 'exam-result'"
                :search-target-id="searchTargetId"
                @start-exam="handleStartExam"
                @view-result="handleViewResult"
                @view-exam="handleViewExam"
              />

              <ExamDetailView
                v-else-if="currentView === 'exam-detail'"
                :exam-id="selectedExamId"
                :assignment="detailAssignment"
                :past-record="detailPastRecord"
                @back="handleNavigate('exams')"
                @start-exam="handleStartExam"
                @view-result="handleViewResult"
              />

              <ExamTakingView
                v-else-if="currentView === 'exam-taking'"
                :exam-id="selectedExamId"
                :user="currentUser"
                @back="handleNavigate('exams')"
                @submit-exam="handleSubmitExam"
              />

              <ExamResultDetailView v-else-if="currentView === 'exam-result-details'" />

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
                @password-changed="handlePasswordChanged"
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

    <CertificateModal
      :certificate="selectedCert"
      :recipient-name="currentUser?.name || ''"
      @close="selectedCert = null"
    />

    <ExamResultView v-if="currentView === 'exam-result'" />

    <OnboardingGate
      v-if="showOnboardingGate"
      :user-name="currentUser?.name || ''"
      @completed="handleOnboardingCompleted"
    />
  </div>
  </Transition>
</template>

<script setup>
import { computed, onMounted, ref, watch, watchEffect } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import Sidebar from './components/Sidebar.vue';
import AdminSidebar from './components/AdminSidebar.vue';
import Header from './components/Header.vue';
import ToastStack from './components/ToastStack.vue';
import CertificateModal from './components/CertificateModal.vue';
import OnboardingGate from './components/OnboardingGate.vue';

import HomeView from './views/HomeView.vue';
import CoursesView from './views/CoursesView.vue';
import CoursePlayerView from './views/CoursePlayerView.vue';
import ExamsView from './views/ExamsView.vue';
import ExamDetailView from './views/ExamDetailView.vue';
import ExamTakingView from './views/ExamTakingView.vue';
import ExamResultView from './views/ExamResultView.vue';
import ExamResultDetailView from './views/ExamResultDetailView.vue';
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
import AdminExamPaperView from './views/admin/AdminExamPaperView.vue';
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
import { learnerStore, loadLearnerCourses, resetLearnerStore } from './learnerStore.js';
import { locationForView } from './router/index.js';
import {
  fetchAttempt,
  fetchExams,
  fetchMyCertificates,
  logoutApi,
} from './api/index.js';
import {
  clearAuth,
  getStoredUser,
  getToken,
  setRefreshToken,
  setStoredUser,
  setToken,
  setUnauthorizedHandler,
} from './api/client.js';

const storedUser = getStoredUser();
const route = useRoute();
const router = useRouter();
const { show: showToast } = useToast();
const landingView = (user) => (user && user.role === 'admin' ? 'admin-dashboard' : 'home');
const currentUser = ref(storedUser || null);
const currentView = ref(route.name === 'exam-taking-by-id' ? 'exam-taking' : route.name || 'login');
const viewTransitionKey = computed(() => (
  currentView.value === 'exam-result' ? 'exams' : currentView.value
));
const skipViewTransition = ref(false);

const scrollPositions = new Map();
const pendingScrollTop = ref(0);
const applyPendingScroll = () => {
  if (route.query.searchTargetId) return;
  mainContent.value?.scrollTo({ top: pendingScrollTop.value, behavior: 'auto' });
};
const onBeforeViewEnter = applyPendingScroll;
const onViewEntered = applyPendingScroll;

const mainContent = ref(null);
const theme = ref('cyan');
const { isCheckedIn } = useAttendance();
const selectedMessageId = ref('');
const messages = useMessages();
const unreadMessageCount = computed(() => messages.value.filter(message => !message.isRead).length);
const selectedCourseId = ref('');
const selectedExamId = ref('');
const globalSearchQuery = ref('');
const searchTargetId = ref('');
const searchAssignments = ref([]);
const searchPastExams = ref([]);
const searchCertificates = ref([]);

watch(
  () => route.fullPath,
  () => {
    const routeName = route.name === 'exam-taking-by-id' ? 'exam-taking' : route.name;
    const nextView = routeName || 'home';
    const viewGroup = (name) => (name === 'exam-result' ? 'exams' : name);
    const leavingView = currentView.value;
    const leavingGroup = viewGroup(leavingView);
    if (leavingView && mainContent.value) {
      scrollPositions.set(leavingGroup, mainContent.value.scrollTop || 0);
    }
    const viewChanged = viewGroup(nextView) !== leavingGroup;
    currentView.value = nextView;
    selectedCourseId.value = String(route.params.courseId || '');
    selectedMessageId.value = String(route.params.messageId || '');
    selectedExamId.value = String(route.params.examId || route.query.examId || '');
    searchTargetId.value = String(route.query.searchTargetId || '');
    if (viewChanged && !searchTargetId.value) {
      const detailToParent = {
        'course-player': 'courses',
        'exam-detail': 'exams',
        'exam-result-details': 'exams',
        'messages-detail': 'messages',
      };
      const returningFromDetail = detailToParent[leavingView] === nextView;
      const target = returningFromDetail ? (scrollPositions.get(viewGroup(nextView)) || 0) : 0;
      pendingScrollTop.value = target;
      skipViewTransition.value = returningFromDetail;
      if (target === 0 && !returningFromDetail) {
        mainContent.value?.scrollTo({ top: 0, behavior: 'auto' });
      }
    } else {
      pendingScrollTop.value = mainContent.value?.scrollTop || 0;
    }
  },
  { immediate: true },
);

const selectedCert = ref(null);

const searchableRecords = computed(() => {
  const admin = isAdmin.value;
  const courses = admin ? adminStore.courses : learnerStore.courses;
  const records = courses.map((course) => ({
    id: course.id,
    type: '课程',
    title: course.title,
    description: `${course.instructor?.name || ''} · ${course.summary || ''}`,
    searchText: [
      course.title,
      course.summary,
      course.categoryLabel,
      course.category,
      course.instructor?.name,
      course.instructor?.title,
    ].filter(Boolean).join(' '),
    view: admin ? 'admin-courses' : 'courses',
    targetId: admin ? '' : `course-card-${course.id}`,
  }));

  if (admin) {
    records.push(
      ...adminStore.exams.map((exam) => ({
        id: exam.id,
        type: '考试',
        title: exam.title,
        description: exam.organizer || exam.statusLabel || '',
        searchText: [exam.title, exam.organizer, exam.statusLabel].filter(Boolean).join(' '),
        view: 'admin-exams',
        targetId: '',
      })),
      ...adminStore.certificates.map((certificate) => ({
        id: certificate.id,
        type: '证书',
        title: certificate.title,
        description: `${certificate.holder || ''} · ${certificate.issueOrg || ''}`,
        searchText: [certificate.title, certificate.holder, certificate.issueOrg].filter(Boolean).join(' '),
        view: 'admin-certificates',
        targetId: '',
      })),
      ...adminStore.announcements.map((announcement) => ({
        id: announcement.id,
        type: '公告',
        title: announcement.title,
        description: announcement.statusLabel || '',
        searchText: [announcement.title, announcement.content, announcement.statusLabel].filter(Boolean).join(' '),
        view: 'admin-announcements',
        targetId: '',
      })),
    );
  } else {
    records.push(
      ...searchAssignments.value.map((exam) => ({
        id: exam.id,
        type: '考试',
        title: exam.title,
        description: `${exam.organizer || ''} · ${exam.timeWindow || ''}`,
        searchText: [exam.title, exam.organizer, exam.timeWindow].filter(Boolean).join(' '),
        view: 'exams',
        targetId: `exam-${exam.id}`,
      })),
      ...searchPastExams.value.map((exam) => ({
        id: exam.id,
        type: '历史考试',
        title: exam.title,
        description: `${exam.type || ''} · ${exam.completionDate || ''}`,
        searchText: [exam.title, exam.type, exam.completionDate].filter(Boolean).join(' '),
        view: 'exams',
        targetId: `past-exam-${exam.id}`,
      })),
      ...searchCertificates.value.map((certificate) => ({
        id: certificate.id,
        type: '证书',
        title: certificate.title,
        description: `${certificate.level || ''} · ${certificate.issueOrg || ''}`,
        searchText: [certificate.title, certificate.level, certificate.issueOrg, certificate.serialNumber]
          .filter(Boolean).join(' '),
        view: 'certificates',
        targetId: `certificate-${certificate.id}`,
      })),
    );
  }

  records.push(
    ...messages.value.map((message) => ({
      id: message.id,
      type: '公告',
      title: message.title,
      description: `${message.sender || ''} · ${message.preview || ''}`,
      searchText: [message.title, message.preview, message.sender].filter(Boolean).join(' '),
      view: admin ? 'admin-announcements' : 'messages',
      targetId: admin ? '' : `message-${message.id}`,
    })),
    {
      id: 'analytics-page',
      type: '学习看板',
      title: '个人学习看板',
      description: '学习时长趋势与学习记录',
      searchText: '个人学习看板 学习时长 学习记录 能力发展',
      view: 'analytics',
      targetId: '',
    },
    {
      id: 'attendance-page',
      type: '培训考勤',
      title: '培训考勤与请假中心',
      description: '培训签到、签退与请假申请',
      searchText: '培训考勤 签到 签退 请假申请 培训假 考勤记录',
      view: 'attendance',
      targetId: '',
    },
    {
      id: 'course-player-page',
      type: '课程学习',
      title: '课程播放器',
      description: '课程视频与章节目录',
      searchText: '课程视频 播放器 学习 课程目录',
      view: 'courses',
      targetId: '',
    },
  );
  return records;
});

const searchResults = computed(() => {
  const query = globalSearchQuery.value.trim().toLocaleLowerCase();
  if (!query) return [];
  return searchableRecords.value
    .filter(record => record.searchText.toLocaleLowerCase().includes(query))
    .slice(0, 10);
});

const detailAssignment = computed(() =>
  searchAssignments.value.find(item => String(item.examId) === String(selectedExamId.value)) || null);
const detailPastRecord = computed(() =>
  searchPastExams.value.find(item => String(item.examId) === String(selectedExamId.value)) || null);

const handleNavigate = (view, { searchTargetId: targetId = '', courseId: nextCourseId } = {}) => {
  searchTargetId.value = '';
  const courseId = nextCourseId || selectedCourseId.value || learnerStore.courses[0]?.id;
  if (view === 'course-player' && !courseId) {
    return router.push(locationForView('courses'));
  }
  const location = locationForView(view, {
    courseId,
    messageId: selectedMessageId.value,
    examId: selectedExamId.value,
  });
  if (targetId) {
    location.query = { ...location.query, searchTargetId: targetId };
  }
  return router.push(location);
};

const THEME_LABELS = { cyan: '青蓝', black: '曜黑', navy: '石墨' };

const cycleTheme = () => {
  const keys = isAdmin.value ? Object.keys(THEME_LABELS) : ['cyan', 'black'];
  const next = keys[(keys.indexOf(theme.value) + 1) % keys.length];
  theme.value = next;
  showToast(`已切换主题：${THEME_LABELS[next]}`, 'success');
};

const handleSelectSearchResult = (result) => {
  handleNavigate(result.view, { searchTargetId: result.targetId });
  globalSearchQuery.value = '';
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

const showOnboardingGate = computed(() =>
  currentUser.value?.role === 'learner' && currentUser.value?.onboardingCompleted !== true
);

const loadSearchData = async () => {
  if (isAdmin.value) return;
  const results = await Promise.allSettled([
    fetchExams(),
    fetchMyCertificates(),
  ]);
  if (results[0].status === 'fulfilled') {
    searchAssignments.value = results[0].value.assignments || [];
    searchPastExams.value = results[0].value.past || [];
  }
  if (results[1].status === 'fulfilled') {
    searchCertificates.value = results[1].value;
  }
  if (results.some((result) => result.status === 'rejected' && result.reason?.status !== 401)) {
    showToast('部分搜索数据加载失败，相关内容暂不可搜索。', 'error');
  }
};

const resetSearchData = () => {
  searchAssignments.value = [];
  searchPastExams.value = [];
  searchCertificates.value = [];
};

const bootstrapData = async () => {
  const tasks = [loadMessages(true), loadAttendance(), loadLeaves(true)];
  if (isAdmin.value) tasks.push(loadAdminStore());
  else tasks.push(loadLearnerCourses(true), loadSearchData());
  await Promise.allSettled(tasks);
};

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
  setRefreshToken(payload.refreshToken);
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

const clearLocalSession = () => {
  clearAuth();
  resetAdminStore();
  resetLearnerStore();
  resetMessages();
  resetAttendance();
  resetLeaves();
  resetSearchData();
  currentUser.value = null;
  currentView.value = 'login';
  theme.value = 'cyan';
};

const handleLogout = () => {
  const logoutRequest = logoutApi();
  clearLocalSession();
  router.replace({ name: 'login' });
  logoutRequest.catch(() => {
  });
};

const handlePasswordChanged = () => {
  handleLogout();
  showToast('密码已修改，请使用新密码重新登录。', 'success');
};

onMounted(async () => {
  setUnauthorizedHandler(() => {
    clearLocalSession();
    showToast('登录状态已失效，请重新登录', 'error');
    router.replace({ name: 'login' });
  });

  if (getToken()) await bootstrapData();
});

const handleUpdateUser = (updatedUser) => {
  currentUser.value = { ...currentUser.value, ...updatedUser };
  setStoredUser(currentUser.value);
};

const handleOnboardingCompleted = (patch) => {
  currentUser.value = { ...currentUser.value, ...(patch || {}), onboardingCompleted: true };
  setStoredUser(currentUser.value);
  showToast('入职手册已阅读完成，欢迎开始学习！', 'success');
};

const handleOpenCertificate = (certId) => {
  const found = searchCertificates.value.find((certificate) => certificate.id === String(certId));
  if (!found) {
    showToast('没有找到这张已发放的证书。', 'error');
    return;
  }
  selectedCert.value = found;
};

const handleViewExam = (exam) => {
  selectedExamId.value = exam?.examId ?? exam?.id ?? '';
  handleNavigate('exam-detail');
};

const handleStartExam = (exam) => {
  selectedExamId.value = exam?.examId ?? exam?.id ?? '';
  handleNavigate('exam-taking');
};

const openExamResult = (result) => {
  const attemptId = result?.attemptId;
  if (attemptId == null) return;
  try {
    window.sessionStorage.setItem(`emp_exam_result_${attemptId}`, JSON.stringify(result));
  } catch {

  }
  router.push({ name: 'exam-result', params: { attemptId: String(attemptId) } });
};

const handleSubmitExam = (payload) => {
  openExamResult({ ...(payload || {}), examId: selectedExamId.value });
};

const handleViewResult = async (record) => {
  if (!record?.attemptId) return;
  try {
    const detail = await fetchAttempt(record.attemptId);
    const answers = detail?.answers || [];
    openExamResult({
      attemptId: record.attemptId,
      examId: record.examId,
      score: Number(record.score ?? detail.totalScore ?? 0),
      passScore: Number(detail.passScore ?? record.passScore ?? 60),
      totalScore: Number(record.totalScore ?? detail.totalScore ?? 100),
      answeredCount: Number(record.answeredCount ?? answers.length),
      totalQuestions: Number(record.questionCount ?? answers.length),
      examTitle: detail?.examTitle || record.title || '',
      isPassed: Boolean(detail?.isPassed),
      pendingReview: Boolean(record.pendingReview) || Number(detail?.status) === 2,
      attemptNo: Number(record.attemptNo ?? detail?.attemptNo ?? 0),
      submittedAt: record.completionDate || detail?.submittedAt || '',
      answers,
    });
  } catch (error) {
    showToast(error?.message || '成绩详情加载失败，请稍后重试', 'error');
  }
};
</script>

<style>
#app [data-scroll-reveal] {
  animation: none !important;
}
#app [data-scroll-reveal='hidden'],
#app [data-scroll-reveal='visible'] {
  --reveal-duration: 520ms;
  --reveal-stagger: 1;
  --reveal-ease: cubic-bezier(0.25, 0.7, 0.3, 1);
  opacity: 0;
  transform: translate3d(var(--reveal-x, 0), var(--reveal-y, var(--reveal-distance, 22px)), 0);
  transition:
    opacity var(--reveal-duration) var(--reveal-ease),
    transform var(--reveal-duration) var(--reveal-ease);
  transition-delay: calc(var(--reveal-delay, 0ms) * var(--reveal-stagger));
}
#app [data-scroll-reveal='visible'] {
  opacity: 1;
  transform: none;
}
#app [data-scroll-reveal-dir='left'] { --reveal-x: -28px; --reveal-y: 0; }
#app [data-scroll-reveal-dir='right'] { --reveal-x: 28px; --reveal-y: 0; }
#app [data-scroll-reveal-dir='down'] { --reveal-x: 0; --reveal-y: -18px; }
#app [data-scroll-reveal-dir='none'] { --reveal-x: 0; --reveal-y: 0; }
@media (prefers-reduced-motion: reduce) {
  #app [data-scroll-reveal='hidden'],
  #app [data-scroll-reveal='visible'] {
    opacity: 1 !important;
    transform: none !important;
    transition: none !important;
  }
}
@media print {
  #app [data-scroll-reveal='hidden'],
  #app [data-scroll-reveal='visible'] {
    opacity: 1 !important;
    transform: none !important;
  }
}

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

:root,
.app-shell {

  --app-accent: #83c9f4;
  --app-accent-strong: #2e8bd6;
  --app-accent-deep: #005576;

  --app-on-accent-solid: var(--app-accent-deep);
  --app-on-accent-muted: var(--app-text-4);
  --app-accent-soft: #d9f0ff;
  --app-accent-line: #b9d7f0;
  --app-accent-border: #a3d5ff;
  --app-accent-hover: #c2e4fa;
  --app-accent-focus: #38a6d4;
  --app-accent-dim: rgba(131, 201, 244, 0.18);

  --app-on-accent-fill: #005576;
  --app-accent-fill-hi: #a3d5ff;
  --app-accent-a10: rgba(46, 139, 214, 0.1);
  --app-accent-a12: rgba(46, 139, 214, 0.12);
  --app-accent-a20: rgba(46, 139, 214, 0.2);

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

  --app-nav-1: #0e1b2e;
  --app-nav-2: #122035;
  --app-nav-3: #0d1929;
  --app-nav-ink: #0e1b2e;
  --app-nav-text: #9fb2c9;
  --app-nav-text-hi: #e7eef7;
  --app-nav-text-active: #f7d98c;
  --app-nav-muted: #5c7290;

  --app-surface: #ffffff;
  --app-surface-2: #f8fafc;
  --app-surface-3: #f1f5f9;
  --app-chip: #e2e8f0;
  --app-chip-soft: rgba(226, 232, 240, 0.4);

  --app-wash-strong: rgba(255, 255, 255, 0.8);
  --app-wash-hover: rgba(255, 255, 255, 0.7);
  --app-border: #e2e8f0;
  --app-border-soft: #eef2f6;
  --app-border-strong: #cbd5e1;
  --app-overlay-border: #e8edf3;

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

  --app-on-fill: #ffffff;

  --app-on-scrim: #ffffff;

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

  --app-accent-hover-strong: #6fb9e8;
  --app-accent-sky-400: #63b8e8;
  --app-accent-ink: #0879a5;
  --app-accent-ink-2: #1683a8;

  --app-violet: #9381ff;
  --app-violet-strong: #7866e0;
  --app-violet-deep: #7c3aed;
  --app-violet-50: #f5f3ff;
  --app-violet-line-100: #d8d5f2;

  --app-emerald-strong: #059669;
  --app-emerald-solid: #10b981;
  --app-emerald-deep: #065f46;
  --app-emerald-soft: #ecfdf5;
  --app-green-600: #16a34a;
  --app-green-300: #86efac;
  --app-green-50: #f0fdf4;

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

  --app-amber-strong: #d97706;
  --app-amber-solid: #fbbf24;
  --app-amber-deep: #92400e;
  --app-amber-50: #fffbeb;

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

  --app-note-wash: rgb(240 249 255 / 70%);

  --app-ring-accent: rgb(0 85 118 / 25%);

  --app-btn-primary-hover: var(--app-accent-border);

  --app-page-bg: #f4f9fd;

  --app-scrim: rgba(15, 30, 51, 0.45);
}
.theme-black {

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

  --app-scrim: rgba(0, 0, 0, 0.62);

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

  --app-nav-1: #0a0b0d;
  --app-nav-2: #101215;
  --app-nav-3: #08090a;
  --app-nav-ink: #0a0b0d;
  --app-nav-text: #9aa3b0;
  --app-nav-text-hi: #eef2f6;
  --app-nav-text-active: #38bdf8;
  --app-nav-muted: #626a75;

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

.app-login {
  position: relative;
  min-height: 100vh;
  background: var(--app-page-bg, #f4f9fd);
}

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

#app .app-viewport--admin {
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  padding-top: 10px;

  padding-bottom: 0;

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

#app .app-viewport--admin .table-wrap,
#app .app-viewport--admin .notice-list,
#app .app-viewport--admin .question-list {
  flex: 0 0 auto;
  min-height: 0;
}

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

#app .app-viewport--admin .pager {
  position: static;
  padding-bottom: 1.25rem;
}

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

.view-transition--instant.view-enter-active,
.view-transition--instant.view-leave-active,
.view-transition--instant.view-enter-from,
.view-transition--instant.view-leave-to {
  transition-duration: 0s !important;
  animation-duration: 0s !important;
}
.view-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

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

.app-shell.theme-black .pager-btn--active { color: var(--app-brand-on); }

@media (max-width: 1280px) {
  #app .app-viewport--admin {
    padding-left: 1.25rem;
    padding-right: 1.25rem;
  }
  #app .app-viewport--admin .overview-strip {
    grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  }
}

@media (max-width: 1080px) {
  #app .app-viewport--admin {
    padding-left: 1rem;
    padding-right: 1rem;
  }

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
