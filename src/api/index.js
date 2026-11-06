/**
 * 后端接口封装（与 emp-server/docs/openapi.yaml 一一对应）
 */
import { http } from './client.js';
import {
  adaptAdminAnnouncement,
  adaptAdminCertificate,
  adaptAdminCheckin,
  adaptAdminCourse,
  adaptAdminDepartment,
  adaptAdminExam,
  adaptCertificate,
  adaptCourse,
  adaptExamAssignment,
  adaptExamMetrics,
  adaptExamPaper,
  adaptExamQuestion,
  adaptExamSession,
  adaptLeave,
  adaptNotification,
  adaptPastExam,
  adaptTrack,
  adaptUser,
} from './adapters.js';

/* ============================== 认证 ============================== */

export const login = async (employeeNo, password) => {
  const data = await http.post('/auth/login', { employeeNo, password }, { auth: false });
  return {
    ...data,
    user: adaptUser(data?.user),
  };
};

export const register = (payload) =>
  http.post('/auth/register', payload, { auth: false }).then(adaptUser);

export const fetchMe = () => http.get('/auth/me').then(adaptUser);

export const changePassword = (oldPassword, newPassword) =>
  http.put('/auth/password', { oldPassword, newPassword });

export const logoutApi = () => http.post('/auth/logout');

/* ============================== 个人资料 ============================== */

export const fetchProfile = () => http.get('/profile').then(adaptUser);

export const updateProfile = (payload) => http.put('/profile', payload).then(adaptUser);

/* ============================== 学员端 ============================== */

export const fetchDashboard = () => http.get('/dashboard');

export const fetchCourses = (params = {}) =>
  http.get('/courses', { page: 1, size: 100, ...params }).then((page) => ({
    ...page,
    records: (page?.records || []).map(adaptCourse),
  }));

export const fetchCourse = (id) => http.get(`/courses/${id}`).then(adaptCourse);

export const enrollCourse = (id) => http.post(`/courses/${id}/enroll`);

export const fetchLesson = (courseId, lessonId) =>
  http.get(`/courses/${courseId}/lessons/${lessonId}`);

export const saveLessonProgress = (courseId, lessonId, payload) =>
  http.post(`/courses/${courseId}/lessons/${lessonId}/progress`, payload);

export const fetchMyLearning = () => http.get('/courses/my-learning');

export const fetchCourseCategories = () => http.get('/course-categories');

export const fetchCourseTags = () => http.get('/course-tags');

export const fetchInstructors = () => http.get('/instructors');

export const fetchTracks = () => http.get('/tracks');

export const fetchAllTracks = () => http.get('/tracks/all').then((list) => (list || []).map(adaptTrack));

export const fetchTrack = (id) => http.get(`/tracks/${id}`);

export const fetchExams = () =>
  http.get('/exams').then((data) => ({
    ...(data || {}),
    assignments: (data?.assignments || []).map(adaptExamAssignment),
    metrics: adaptExamMetrics(data?.metrics),
    past: (data?.past || []).map(adaptPastExam),
  }));

export const fetchExamMetrics = () => http.get('/exams/metrics').then(adaptExamMetrics);

export const fetchPastExams = () => http.get('/exams/past').then((list) => (list || []).map(adaptPastExam));

export const fetchExamPaper = (id) => http.get(`/exams/${id}/paper`).then(adaptExamPaper);

export const startExam = (id) => http.post(`/exams/${id}/start`).then(adaptExamSession);

export const fetchAttempt = (attemptId) =>
  http.get(`/exams/attempts/${attemptId}`).then(adaptExamSession);

export const saveExamAnswer = (attemptId, payload) =>
  http.post(`/exams/attempts/${attemptId}/answers`, payload);

export const reportExamSwitch = (attemptId) =>
  http.post(`/exams/attempts/${attemptId}/switch`);

export const submitExam = (attemptId) =>
  http.post(`/exams/attempts/${attemptId}/submit`).then(adaptExamSession);

/* ---------- 请假 / 考勤 ---------- */

export const fetchLeaves = () => http.get('/leaves').then((list) => (list || []).map(adaptLeave));

export const createLeave = (payload) => http.post('/leaves', payload);

export const deleteLeave = (id) => http.delete(`/leaves/${id}`);

export const fetchCheckins = (params = {}) => http.get('/checkins', params);

export const fetchTodayCheckin = () => http.get('/checkins/today');

export const checkIn = () => http.post('/checkins/check-in');

export const checkOut = () => http.post('/checkins/check-out');

export const fetchCheckinStats = () => http.get('/checkins/stats');

/* ---------- 证书 ---------- */

export const fetchMyCertificates = () =>
  http.get('/certificates/my').then((list) => (list || []).map(adaptCertificate));

export const fetchCertificates = (params = {}) => http.get('/certificates', params);

export const verifyCertificate = (serial) => http.get(`/certificates/verify/${serial}`);

/* ---------- 公告 / 通知 ---------- */

export const fetchAnnouncements = (params = {}) => http.get('/announcements', params);

export const fetchAnnouncement = (id) => http.get(`/announcements/${id}`);

export const fetchUnreadAnnouncementCount = () => http.get('/announcements/unread-count');

export const fetchNotifications = (params = { page: 1, size: 100 }) =>
  http.get('/notifications', params).then((page) => ({
    ...page,
    records: (page?.records || []).map(adaptNotification),
  }));

export const fetchRecentNotifications = () =>
  http.get('/notifications/recent').then((list) => (list || []).map(adaptNotification));

export const fetchUnreadNotificationCount = () => http.get('/notifications/unread-count');

export const markNotificationRead = (id, isRead = true) =>
  http.put(`/notifications/${id}/read`, { isRead });

export const markAllNotificationsRead = () => http.put('/notifications/read-all', {});

export const deleteNotification = (id) => http.delete(`/notifications/${id}`);

/* ============================== 管理端 ============================== */

export const fetchAdminDashboard = () => http.get('/admin/dashboard');

export const fetchAdminActivities = (params = { page: 1, size: 20 }) =>
  http.get('/admin/activities', params);

export const fetchAdminLogs = (params = { page: 1, size: 20 }) =>
  http.get('/admin/logs', params);

/* ---------- 用户 ---------- */

export const fetchAdminUsers = (params = {}) =>
  http.get('/admin/users', { page: 1, size: 200, ...params }).then((page) => ({
    ...page,
    records: (page?.records || []).map(adaptUser),
  }));

export const fetchAdminUserOptions = () => http.get('/admin/users/options');

export const fetchAdminUser = (id) => http.get(`/admin/users/${id}`).then(adaptUser);

export const createAdminUser = (payload) => http.post('/admin/users', payload);

export const updateAdminUser = (id, payload) => http.put(`/admin/users/${id}`, payload);

export const deleteAdminUser = (id) => http.delete(`/admin/users/${id}`);

export const updateAdminUserStatus = (id, accountStatus) =>
  http.put(`/admin/users/${id}/status`, { accountStatus });

export const resetAdminUserPassword = (id, password = '123456') =>
  http.post(`/admin/users/${id}/reset-password`, { newPassword: password });

/* ---------- 部门 ---------- */

export const fetchAdminDepartments = (params = {}) =>
  http.get('/admin/departments', { page: 1, size: 200, ...params }).then((page) => ({
    ...page,
    records: (page?.records || []).map(adaptAdminDepartment),
  }));

export const fetchDepartmentOptions = () => http.get('/departments/options');

export const fetchAdminDepartmentOptions = () => http.get('/admin/departments/options');

export const createAdminDepartment = (payload) => http.post('/admin/departments', payload);

export const updateAdminDepartment = (id, payload) => http.put(`/admin/departments/${id}`, payload);

export const deleteAdminDepartment = (id) => http.delete(`/admin/departments/${id}`);

export const updateAdminDepartmentStatus = (id, status) =>
  http.put(`/admin/departments/${id}/status`, { status });

/* ---------- 课程 ---------- */

export const fetchAdminCourses = (params = {}) =>
  http.get('/admin/courses', { page: 1, size: 200, ...params }).then((page) => ({
    ...page,
    records: (page?.records || []).map(adaptAdminCourse),
  }));

export const fetchAdminCourseOptions = () => http.get('/admin/courses/options');

export const fetchAdminCourseCategories = () => http.get('/admin/course-categories');

export const fetchAdminCourseTags = () => http.get('/admin/course-tags');

export const fetchAdminInstructors = () => http.get('/admin/instructors');

export const createAdminInstructor = (payload) => http.post('/admin/instructors', payload);

export const fetchAdminCourse = (id) => http.get(`/admin/courses/${id}`).then(adaptAdminCourse);

export const createAdminCourse = (payload) => http.post('/admin/courses', payload);

export const updateAdminCourse = (id, payload) => http.put(`/admin/courses/${id}`, payload);

export const deleteAdminCourse = (id) => http.delete(`/admin/courses/${id}`);

export const updateAdminCourseStatus = (id, status) =>
  http.put(`/admin/courses/${id}/status`, { status });

export const createAdminChapter = (courseId, payload) =>
  http.post(`/admin/courses/${courseId}/chapters`, payload);

export const updateAdminChapter = (courseId, chapterId, payload) =>
  http.put(`/admin/courses/${courseId}/chapters/${chapterId}`, payload);

export const deleteAdminChapter = (courseId, chapterId) =>
  http.delete(`/admin/courses/${courseId}/chapters/${chapterId}`);

export const createAdminLesson = (courseId, chapterId, payload) =>
  http.post(`/admin/courses/${courseId}/chapters/${chapterId}/lessons`, payload);

export const updateAdminLesson = (courseId, chapterId, lessonId, payload) =>
  http.put(`/admin/courses/${courseId}/chapters/${chapterId}/lessons/${lessonId}`, payload);

export const deleteAdminLesson = (courseId, chapterId, lessonId) =>
  http.delete(`/admin/courses/${courseId}/chapters/${chapterId}/lessons/${lessonId}`);

/* ---------- 研修通道 ---------- */

export const fetchAdminTracks = (params = { page: 1, size: 100 }) =>
  http.get('/admin/tracks', params);

export const createAdminTrack = (payload) => http.post('/admin/tracks', payload);

export const updateAdminTrack = (id, payload) => http.put(`/admin/tracks/${id}`, payload);

export const deleteAdminTrack = (id) => http.delete(`/admin/tracks/${id}`);

export const updateAdminTrackStatus = (id, status) =>
  http.put(`/admin/tracks/${id}/status`, { status });

/* ---------- 考试 ---------- */

export const fetchAdminExams = (params = {}) =>
  http.get('/admin/exams', { page: 1, size: 200, ...params }).then((page) => ({
    ...page,
    records: (page?.records || []).map(adaptAdminExam),
  }));

export const fetchAdminExam = (id) => http.get(`/admin/exams/${id}`).then(adaptAdminExam);

export const createAdminExam = (payload) => http.post('/admin/exams', payload);

export const updateAdminExam = (id, payload) => http.put(`/admin/exams/${id}`, payload);

export const deleteAdminExam = (id) => http.delete(`/admin/exams/${id}`);

export const updateAdminExamStatus = (id, status) =>
  http.put(`/admin/exams/${id}/status`, { status });

export const fetchAdminExamAssignments = (examId, params = { page: 1, size: 100 }) =>
  http.get(`/admin/exams/${examId}/assignments`, params);

export const fetchAdminExamAttempts = (params = { page: 1, size: 100 }) =>
  http.get('/admin/exam-attempts', params);

export const gradeAdminExamAttempt = (id, payload) =>
  http.put(`/admin/exam-attempts/${id}/grade`, payload);

/* ---------- 题库 ---------- */

export const fetchAdminQuestions = (params = {}) => {
  const query = typeof params === 'number' ? { examId: params } : params;
  return http.get('/admin/questions', query).then((data) => {
    const list = Array.isArray(data) ? data : (data?.records || []);
    return list.map(adaptExamQuestion);
  });
};

export const createAdminQuestion = (payload) => http.post('/admin/questions', payload);

export const updateAdminQuestion = (id, payload) => http.put(`/admin/questions/${id}`, payload);

export const deleteAdminQuestion = (id) => http.delete(`/admin/questions/${id}`);

/* ---------- 请假 ---------- */

export const fetchAdminLeaves = (params = {}) =>
  http.get('/admin/leaves', { page: 1, size: 200, ...params }).then((page) => ({
    ...page,
    records: (page?.records || []).map(adaptLeave),
  }));

export const fetchAdminLeaveStats = () => http.get('/admin/leaves/stats');

const LEAVE_STATUS_CODES = { pending: 1, approved: 2, rejected: 3 };

/** status 既接受视图里的 'approved'/'rejected'，也接受后端原始码 2/3 */
export const approveAdminLeave = (id, status, remark = '') =>
  http.put(`/admin/leaves/${id}/approve`, {
    status: LEAVE_STATUS_CODES[status] ?? status,
    remark,
  });

export const deleteAdminLeave = (id) => http.delete(`/admin/leaves/${id}`);

/* ---------- 签到 ---------- */

export const fetchAdminCheckins = (params = {}) =>
  http.get('/admin/checkins', { page: 1, size: 200, ...params }).then((page) => ({
    ...page,
    records: (page?.records || []).map(adaptAdminCheckin),
  }));

export const fetchAdminCheckinStats = () => http.get('/admin/checkins/stats');

export const createAdminCheckin = (payload) => http.post('/admin/checkins', payload);

export const updateAdminCheckin = (id, payload) => http.put(`/admin/checkins/${id}`, payload);

export const deleteAdminCheckin = (id) => http.delete(`/admin/checkins/${id}`);

/* ---------- 证书 ---------- */

export const fetchAdminCertificates = (params = {}) =>
  http.get('/admin/certificates', { page: 1, size: 200, ...params }).then((page) => ({
    ...page,
    records: (page?.records || []).map(adaptAdminCertificate),
  }));

export const fetchAdminCertificateTemplates = () =>
  http.get('/admin/certificates/templates');

export const createAdminCertificateTemplate = (payload) =>
  http.post('/admin/certificates/templates', payload);

export const updateAdminCertificateTemplate = (id, payload) =>
  http.put(`/admin/certificates/templates/${id}`, payload);

export const issueAdminCertificate = (payload) => http.post('/admin/certificates', payload);

export const updateAdminCertificate = (id, payload) =>
  http.put(`/admin/certificates/${id}`, payload);

export const deleteAdminCertificate = (id) => http.delete(`/admin/certificates/${id}`);

export const updateAdminCertificateStatus = (id, status) =>
  http.put(`/admin/certificates/${id}/status`, { status });

/* ---------- 公告 ---------- */

export const fetchAdminAnnouncements = (params = {}) =>
  http.get('/admin/announcements', { page: 1, size: 200, ...params }).then((page) => ({
    ...page,
    records: (page?.records || []).map(adaptAdminAnnouncement),
  }));

export const createAdminAnnouncement = (payload) => http.post('/admin/announcements', payload);

export const updateAdminAnnouncement = (id, payload) =>
  http.put(`/admin/announcements/${id}`, payload);

export const deleteAdminAnnouncement = (id) => http.delete(`/admin/announcements/${id}`);

export const updateAdminAnnouncementStatus = (id, status) =>
  http.put(`/admin/announcements/${id}/status`, { status });

export const toggleAdminAnnouncementTop = (id, isTop) =>
  http.put(`/admin/announcements/${id}/top`, { isTop });

/* ---------- 文件 ---------- */

export const uploadFile = async (file, folder = 'common') => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('folder', folder);
  return http.upload('/files/upload', formData);
};
