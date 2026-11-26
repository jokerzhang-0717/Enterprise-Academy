
import { ApiError, getRefreshToken, http } from './client.js';
import {
  adaptAdminAnnouncement,
  adaptAdminCertificate,
  adaptAdminCheckin,
  adaptAdminCourse,
  adaptAdminDepartment,
  adaptAdminExam,
  adaptAnnouncementMessage,
  adaptCertificate,
  adaptCourse,
  adaptExamAssignment,
  adaptExamMetrics,
  adaptExamPaper,
  adaptExamQuestion,
  adaptExamSession,
  adaptLeave,
  adaptPastExam,
  adaptUser,
} from './adapters.js';

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

export const logoutApi = () =>
  http.post('/auth/logout', { refreshToken: getRefreshToken() });

export const fetchProfile = () => http.get('/profile').then(adaptUser);

export const updateProfile = (payload) => http.put('/profile', payload).then(adaptUser);

export const fetchDashboard = () => http.get('/dashboard');

export const fetchCourses = (params = {}) =>
  http.get('/courses', { page: 1, size: 100, ...params }).then((page) => ({
    ...page,
    records: (page?.records || []).map(adaptCourse),
  }));

export const fetchCourse = (id) => http.get(`/courses/${id}`).then(adaptCourse);

export const saveLessonProgress = (courseId, lessonId, payload) =>
  http.post(`/courses/${courseId}/lessons/${lessonId}/progress`, payload);

export const startStudySession = (courseId, lessonId, sessionId) =>
  http.post(`/courses/${courseId}/lessons/${lessonId}/study-sessions`, { sessionId });

export const heartbeatStudySession = (sessionId) =>
  http.post(`/study-sessions/${sessionId}/heartbeat`);

export const stopStudySession = (sessionId) =>
  http.post(`/study-sessions/${sessionId}/stop`);

export const fetchExams = () =>
  http.get('/exams').then((data) => ({
    ...(data || {}),
    assignments: (data?.assignments || []).map(adaptExamAssignment),
    metrics: adaptExamMetrics(data?.metrics),
    past: (data?.past || []).map(adaptPastExam),
  }));

export const fetchExamPaper = (id) => http.get(`/exams/${id}/paper`).then(adaptExamPaper);

export const startExam = (id) => http.post(`/exams/${id}/start`).then(adaptExamSession);

export const fetchAttempt = (attemptId) =>
  http.get(`/exams/attempts/${attemptId}`).then(adaptExamSession);

export const saveExamAnswer = (attemptId, payload) =>
  http.post(`/exams/attempts/${attemptId}/answers`, payload);

export const submitExam = (attemptId) =>
  http.post(`/exams/attempts/${attemptId}/submit`).then(adaptExamSession);

export const fetchLeaves = () => http.get('/leaves').then((list) => (list || []).map(adaptLeave));

export const createLeave = (payload) => http.post('/leaves', payload);

export const fetchTodayCheckin = () => http.get('/checkins/today');

export const checkIn = () => http.post('/checkins/check-in');

export const checkOut = () => http.post('/checkins/check-out');

export const fetchMyCertificates = () =>
  http.get('/certificates/my').then((list) => (list || []).map(adaptCertificate));

export const fetchAnnouncement = (id) => http.get(`/announcements/${id}`);

export const markAllAnnouncementsRead = () => http.put('/announcements/read-all', {});

export const fetchAnnouncementMessages = (params = { page: 1, size: 100 }) =>
  http.get('/announcements', params).then((page) => ({
    ...page,
    records: (page?.records || []).map(adaptAnnouncementMessage),
  }));

export const fetchAdminDashboard = () => http.get('/admin/dashboard');

export const fetchAdminUsers = (params = {}) =>
  http.get('/admin/users', { page: 1, size: 200, ...params }).then((page) => ({
    ...page,
    records: (page?.records || []).map(adaptUser),
  }));

export const createAdminUser = (payload) => http.post('/admin/users', payload);

export const updateAdminUser = (id, payload) => http.put(`/admin/users/${id}`, payload);

export const deleteAdminUser = (id) => http.delete(`/admin/users/${id}`);

export const updateAdminUserStatus = (id, accountStatus) =>
  http.put(`/admin/users/${id}/status`, { accountStatus });

export const resetAdminUserPassword = (id, password = '123456') =>
  http.post(`/admin/users/${id}/reset-password`, { newPassword: password });

export const fetchAdminDepartments = (params = {}) =>
  http.get('/admin/departments', { page: 1, size: 200, ...params }).then((page) => ({
    ...page,
    records: (page?.records || []).map(adaptAdminDepartment),
  }));

export const createAdminDepartment = (payload) => http.post('/admin/departments', payload);

export const updateAdminDepartment = (id, payload) => http.put(`/admin/departments/${id}`, payload);

export const deleteAdminDepartment = (id) => http.delete(`/admin/departments/${id}`);

export const updateAdminDepartmentStatus = (id, status) =>
  http.put(`/admin/departments/${id}/status`, { status });

export const fetchAdminCourses = (params = {}) =>
  http.get('/admin/courses', { page: 1, size: 200, ...params }).then((page) => ({
    ...page,
    records: (page?.records || []).map(adaptAdminCourse),
  }));

export const fetchAdminCourseCategories = () => http.get('/admin/course-categories');

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

export const fetchAdminExamAttempts = (params = {}) =>
  http.get('/admin/exam-attempts', { page: 1, size: 200, ...params });

export const fetchAdminExamAttempt = (id) => http.get(`/admin/exam-attempts/${id}`);

export const gradeAdminExamAttempt = (id, payload) =>
  http.post(`/admin/exam-attempts/${id}/grade`, payload);

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

export const fetchAdminLeaves = (params = {}) =>
  http.get('/admin/leaves', { page: 1, size: 200, ...params }).then((page) => ({
    ...page,
    records: (page?.records || []).map(adaptLeave),
  }));

const LEAVE_STATUS_CODES = { pending: 1, approved: 2, rejected: 3 };

export const approveAdminLeave = (id, status, remark = '') =>
  http.put(`/admin/leaves/${id}/approve`, {
    status: LEAVE_STATUS_CODES[status] ?? status,
    remark,
  });

export const fetchAdminCheckins = (params = {}) =>
  http.get('/admin/checkins', { page: 1, size: 200, ...params }).then((page) => ({
    ...page,
    records: (page?.records || []).map(adaptAdminCheckin),
  }));

export const createAdminCheckin = (payload) => http.post('/admin/checkins', payload);

export const updateAdminCheckin = (id, payload) => http.put(`/admin/checkins/${id}`, payload);

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

export const uploadFile = async (file, folder = 'common') => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('folder', folder);
  return http.upload('/files/upload', formData);
};

export const sha256Hex = async (file) => {
  if (!window.crypto?.subtle) {
    throw new Error('当前浏览器不支持 Web Crypto，无法计算文件摘要');
  }
  const buffer = await file.arrayBuffer();
  const digest = await window.crypto.subtle.digest('SHA-256', buffer);
  return Array.from(new Uint8Array(digest), (byte) =>
    byte.toString(16).padStart(2, '0')).join('');
};

export const presignUploadFile = async (file, folder = 'common') => {
  const sha256 = await sha256Hex(file);
  const result = await http.post('/files/presign-upload', {
    fileName: file.name,
    contentType: file.type || 'application/octet-stream',
    folder,
    sha256,
    size: file.size,
  });
  if (result.deduplicated) return result;

  const response = await fetch(result.uploadUrl, {
    method: 'PUT',
    headers: { 'Content-Type': result.contentType || file.type || 'application/octet-stream' },
    body: file,
  });
  if (!response.ok) {
    throw new ApiError('文件直传失败，请稍后重试', -1, response.status, null);
  }
  return result;
};
