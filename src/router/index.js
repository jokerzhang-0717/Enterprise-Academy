import { createRouter, createWebHistory } from 'vue-router';
import { fetchMe } from '../api/index.js';
import { clearAuth, getStoredUser, getToken, setStoredUser } from '../api/client.js';

const Empty = { name: 'RouteView', render: () => null };

const landingPath = (user) => (user && user.role === 'admin' ? '/admin/dashboard' : '/home');
let sessionInitialized = false;

const routes = [
  { path: '/', name: 'root', component: Empty },

  { path: '/login', name: 'login', component: Empty, meta: { guestOnly: true } },

  { path: '/home', name: 'home', component: Empty, meta: { learnerOnly: true } },
  { path: '/courses', name: 'courses', component: Empty, meta: { learnerOnly: true } },
  { path: '/courses/:courseId/player', name: 'course-player', component: Empty, meta: { learnerOnly: true } },
  { path: '/exams', name: 'exams', component: Empty, meta: { learnerOnly: true } },
  { path: '/exams/:examId/detail', name: 'exam-detail', component: Empty, meta: { learnerOnly: true } },
  { path: '/exams/result/:attemptId/details', name: 'exam-result-details', component: Empty, meta: { learnerOnly: true } },
  { path: '/exams/result/:attemptId', name: 'exam-result', component: Empty, meta: { learnerOnly: true } },
  { path: '/exams/taking', name: 'exam-taking', component: Empty, meta: { learnerOnly: true } },
  { path: '/exams/:examId/taking', name: 'exam-taking-by-id', component: Empty, meta: { learnerOnly: true } },
  { path: '/analytics', name: 'analytics', component: Empty, meta: { learnerOnly: true } },
  { path: '/profile', name: 'profile', component: Empty, meta: { learnerOnly: true } },
  { path: '/attendance', name: 'attendance', component: Empty, meta: { learnerOnly: true } },
  { path: '/messages', name: 'messages', component: Empty, meta: { learnerOnly: true } },
  { path: '/messages/:messageId', name: 'messages-detail', component: Empty, meta: { learnerOnly: true } },
  { path: '/certificates', name: 'certificates', component: Empty, meta: { learnerOnly: true } },

  { path: '/admin', redirect: '/admin/dashboard' },
  { path: '/admin/dashboard', name: 'admin-dashboard', component: Empty, meta: { adminOnly: true } },
  { path: '/admin/students', name: 'admin-students', component: Empty, meta: { adminOnly: true } },
  { path: '/admin/departments', name: 'admin-departments', component: Empty, meta: { adminOnly: true } },
  { path: '/admin/courses', name: 'admin-courses', component: Empty, meta: { adminOnly: true } },
  { path: '/admin/exams', name: 'admin-exams', component: Empty, meta: { adminOnly: true } },
  { path: '/admin/exams/:examId/paper', name: 'admin-exam-paper', component: Empty, meta: { adminOnly: true } },
  { path: '/admin/question-bank', name: 'admin-question-bank', component: Empty, meta: { adminOnly: true } },
  { path: '/admin/leave', name: 'admin-leave', component: Empty, meta: { adminOnly: true } },
  { path: '/admin/checkin', name: 'admin-checkin', component: Empty, meta: { adminOnly: true } },
  { path: '/admin/certificates', name: 'admin-certificates', component: Empty, meta: { adminOnly: true } },
  { path: '/admin/announcements', name: 'admin-announcements', component: Empty, meta: { adminOnly: true } },
  { path: '/admin/profile', name: 'admin-profile', component: Empty, meta: { adminOnly: true } },

  { path: '/:pathMatch(.*)*', redirect: '/' },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to) => {
  let token = getToken();
  let user = getStoredUser();

  if (token && !sessionInitialized) {
    sessionInitialized = true;
    try {
      user = await fetchMe();
      setStoredUser(user);
    } catch (error) {
      if (error?.status === 401) {
        clearAuth();
        token = '';
        user = null;
      } else if (!user) {
        return { name: 'login', query: { redirect: to.fullPath } };
      }
    }
  }

  if (to.name === 'root') {
    return token ? landingPath(user) : { name: 'login' };
  }

  if (to.meta?.guestOnly) {
    return token ? landingPath(user) : true;
  }

  if (!token) {
    return { name: 'login', query: to.fullPath && to.fullPath !== '/' ? { redirect: to.fullPath } : {} };
  }

  if (to.meta?.adminOnly && (!user || user.role !== 'admin')) {
    return { name: 'home' };
  }

  if (to.meta?.learnerOnly && user?.role === 'admin') {
    return { name: 'admin-dashboard' };
  }

  return true;
});

export { landingPath };

export const locationForView = (view, { courseId, messageId, examId } = {}) => {
  switch (view) {
    case 'course-player':
      return courseId
        ? { name: 'course-player', params: { courseId } }
        : { name: 'courses' };
    case 'messages-detail':
      return messageId
        ? { name: 'messages-detail', params: { messageId } }
        : { name: 'messages' };
    case 'exam-detail':
      return examId
        ? { name: 'exam-detail', params: { examId } }
        : { name: 'exams' };
    case 'exam-taking':
      return examId
        ? { name: 'exam-taking-by-id', params: { examId } }
        : { name: 'exam-taking' };
    default:
      return router.hasRoute(view) ? { name: view } : { name: 'home' };
  }
};

export default router;
