import { createRouter, createWebHistory } from 'vue-router';
import { fetchMe } from '../api/index.js';
import { clearAuth, getStoredUser, getToken, setStoredUser } from '../api/client.js';

/**
 * 云舒艺境 · 前端路由
 *
 * 说明:本项目的视图渲染仍由 App.vue 内的 v-if 分支负责(以 route.name 驱动),
 * 这里只负责 URL ↔ 视图名(路由名沿用原 currentView 的 view id)的映射与守卫,
 * 因此路由记录无需绑定真实组件。
 */

const Empty = { name: 'RouteView', render: () => null };

const landingPath = (user) => (user && user.role === 'admin' ? '/admin/dashboard' : '/home');
let sessionInitialized = false;

const routes = [
  { path: '/', name: 'root', component: Empty },

  /* ---------- 登录 ---------- */
  { path: '/login', name: 'login', component: Empty, meta: { guestOnly: true } },

  /* ---------- 学员端 ---------- */
  { path: '/home', name: 'home', component: Empty, meta: { learnerOnly: true } },
  { path: '/courses', name: 'courses', component: Empty, meta: { learnerOnly: true } },
  { path: '/courses/:courseId/player', name: 'course-player', component: Empty, meta: { learnerOnly: true } },
  { path: '/tracks/:trackId', name: 'track-detail', component: Empty, meta: { learnerOnly: true } },
  { path: '/exams', name: 'exams', component: Empty, meta: { learnerOnly: true } },
  { path: '/exams/taking', name: 'exam-taking', component: Empty, meta: { learnerOnly: true } },
  { path: '/exams/:examId/taking', name: 'exam-taking-by-id', component: Empty, meta: { learnerOnly: true } },
  { path: '/analytics', name: 'analytics', component: Empty, meta: { learnerOnly: true } },
  { path: '/profile', name: 'profile', component: Empty, meta: { learnerOnly: true } },
  { path: '/attendance', name: 'attendance', component: Empty, meta: { learnerOnly: true } },
  { path: '/messages', name: 'messages', component: Empty, meta: { learnerOnly: true } },
  { path: '/messages/:messageId', name: 'messages-detail', component: Empty, meta: { learnerOnly: true } },
  { path: '/certificates', name: 'certificates', component: Empty, meta: { learnerOnly: true } },

  /* ---------- 管理端 ---------- */
  { path: '/admin', redirect: '/admin/dashboard' },
  { path: '/admin/dashboard', name: 'admin-dashboard', component: Empty, meta: { adminOnly: true } },
  { path: '/admin/students', name: 'admin-students', component: Empty, meta: { adminOnly: true } },
  { path: '/admin/departments', name: 'admin-departments', component: Empty, meta: { adminOnly: true } },
  { path: '/admin/courses', name: 'admin-courses', component: Empty, meta: { adminOnly: true } },
  { path: '/admin/exams', name: 'admin-exams', component: Empty, meta: { adminOnly: true } },
  { path: '/admin/question-bank', name: 'admin-question-bank', component: Empty, meta: { adminOnly: true } },
  { path: '/admin/leave', name: 'admin-leave', component: Empty, meta: { adminOnly: true } },
  { path: '/admin/checkin', name: 'admin-checkin', component: Empty, meta: { adminOnly: true } },
  { path: '/admin/certificates', name: 'admin-certificates', component: Empty, meta: { adminOnly: true } },
  { path: '/admin/announcements', name: 'admin-announcements', component: Empty, meta: { adminOnly: true } },
  { path: '/admin/profile', name: 'admin-profile', component: Empty, meta: { adminOnly: true } },

  /* ---------- 兜底 ---------- */
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

  // 已登录访问登录页 → 回角色落地页
  if (to.meta?.guestOnly) {
    return token ? landingPath(user) : true;
  }

  // 未登录访问受保护页面 → 登录页(带返回地址)
  if (!token) {
    return { name: 'login', query: to.fullPath && to.fullPath !== '/' ? { redirect: to.fullPath } : {} };
  }

  // 学员访问管理端 → 回学员首页
  if (to.meta?.adminOnly && (!user || user.role !== 'admin')) {
    return { name: 'home' };
  }

  // 管理员访问学员页面时回到管理首页，避免导航与页面角色不匹配
  if (to.meta?.learnerOnly && user?.role === 'admin') {
    return { name: 'admin-dashboard' };
  }

  return true;
});

export { landingPath };

export const locationForView = (view, { courseId, trackId, messageId, examId } = {}) => {
  switch (view) {
    case 'course-player':
      return courseId
        ? { name: 'course-player', params: { courseId } }
        : { name: 'courses' };
    case 'track-detail':
      return trackId
        ? { name: 'track-detail', params: { trackId } }
        : { name: 'home' };
    case 'messages-detail':
      return messageId
        ? { name: 'messages-detail', params: { messageId } }
        : { name: 'messages' };
    case 'exam-taking':
      return examId
        ? { name: 'exam-taking-by-id', params: { examId } }
        : { name: 'exam-taking' };
    default:
      return router.hasRoute(view) ? { name: view } : { name: 'home' };
  }
};

export default router;
