import { reactive } from 'vue';
import {
  fetchAdminAnnouncements,
  fetchAdminCertificates,
  fetchAdminCheckins,
  fetchAdminCourses,
  fetchAdminDashboard,
  fetchAdminDepartments,
  fetchAdminExams,
  fetchAdminLeaves,
  fetchAdminQuestions,
  fetchAdminUsers,
} from './api/index.js';

/**
 * 管理端共享响应式状态
 * 课程管理页 → 课程导入页、题库页 → 考试页 之间通过该模块同步数据。
 * 数据源为后端接口；所有列表都通过 splice 原地替换，保证视图里
 * `const courses = adminStore.courses` 这类引用始终是同一数组。
 */

const replaceAll = (target, list) => {
  target.splice(0, target.length, ...(list || []));
};

export const adminStore = reactive({
  dashboard: null,
  departments: [],
  courses: [],
  exams: [],
  questions: [],
  users: [],
  leaves: [],
  checkins: [],
  certificates: [],
  announcements: [],
  loaded: false,
  loading: false,
});

/* ============================== 单个列表加载 ============================== */

export const loadDepartments = async () => {
  const page = await fetchAdminDepartments();
  replaceAll(adminStore.departments, page?.records || []);
  return adminStore.departments;
};

export const loadCourses = async () => {
  const page = await fetchAdminCourses();
  replaceAll(adminStore.courses, page?.records || []);
  return adminStore.courses;
};

export const loadExams = async () => {
  const page = await fetchAdminExams();
  replaceAll(adminStore.exams, page?.records || []);
  return adminStore.exams;
};

export const loadQuestions = async () => {
  const list = await fetchAdminQuestions();
  replaceAll(adminStore.questions, list || []);
  return adminStore.questions;
};

export const loadUsers = async () => {
  const page = await fetchAdminUsers();
  replaceAll(adminStore.users, page?.records || []);
  return adminStore.users;
};

export const loadLeaves = async () => {
  const page = await fetchAdminLeaves();
  replaceAll(adminStore.leaves, page?.records || []);
  return adminStore.leaves;
};

export const loadCheckins = async () => {
  const page = await fetchAdminCheckins();
  replaceAll(adminStore.checkins, page?.records || []);
  return adminStore.checkins;
};

export const loadCertificates = async () => {
  const page = await fetchAdminCertificates();
  replaceAll(adminStore.certificates, page?.records || []);
  return adminStore.certificates;
};

export const loadAnnouncements = async () => {
  const page = await fetchAdminAnnouncements();
  replaceAll(adminStore.announcements, page?.records || []);
  return adminStore.announcements;
};

export const loadDashboard = async () => {
  adminStore.dashboard = await fetchAdminDashboard();
  return adminStore.dashboard;
};

/** 一次加载管理端全部基础数据（失败的列表保持原值，不阻塞其它页面） */
export const loadAdminStore = async () => {
  if (adminStore.loading) return adminStore;
  adminStore.loading = true;
  const tasks = [
    loadDashboard,
    loadDepartments,
    loadCourses,
    loadExams,
    loadQuestions,
    loadUsers,
    loadLeaves,
    loadCheckins,
    loadCertificates,
    loadAnnouncements,
  ];
  await Promise.allSettled(tasks.map((task) => task()));
  adminStore.loading = false;
  adminStore.loaded = true;
  return adminStore;
};

/** 退出登录时清空管理端缓存 */
export const resetAdminStore = () => {
  adminStore.dashboard = null;
  adminStore.departments.splice(0);
  adminStore.courses.splice(0);
  adminStore.exams.splice(0);
  adminStore.questions.splice(0);
  adminStore.users.splice(0);
  adminStore.leaves.splice(0);
  adminStore.checkins.splice(0);
  adminStore.certificates.splice(0);
  adminStore.announcements.splice(0);
  adminStore.loaded = false;
};

/** 统计某场考试的题目数 */
export const questionCount = (examId) =>
  adminStore.questions.filter(q => String(q.examId) === String(examId)).length;
