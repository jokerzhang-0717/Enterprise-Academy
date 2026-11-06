/**
 * 后端 DTO → 前端视图模型 适配层
 * 视图模板尽量保持原样，只在这里做字段名/枚举/展示文本的转换。
 */

const pad = (n) => String(n).padStart(2, '0');

const asDate = (value) => {
  if (!value) return null;
  if (value instanceof Date) return value;
  const text = String(value).replace(' ', 'T');
  const d = new Date(text);
  return Number.isNaN(d.getTime()) ? null : d;
};

/** 2025-05-13T09:30:00 → 2025-05-13 */
export const formatDate = (value) => {
  if (!value) return '';
  const text = String(value);
  if (/^\d{4}-\d{2}-\d{2}/.test(text)) return text.slice(0, 10);
  const d = asDate(value);
  if (!d) return text;
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
};

/** 2025-05-13T09:30:00 → 2025-05-13 09:30 */
export const formatDateTime = (value) => {
  if (!value) return '';
  const d = asDate(value);
  if (!d) return String(value);
  return `${formatDate(d)} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
};

/** 2025-05-13T09:30:00 → 09:30 */
export const formatTime = (value) => {
  if (!value) return '';
  const d = asDate(value);
  if (!d) return '';
  return `${pad(d.getHours())}:${pad(d.getMinutes())}`;
};

/** 消息列表里的相对时间展示 */
export const formatMessageTime = (value) => {
  if (!value) return '';
  const d = asDate(value);
  if (!d) return '';
  const now = new Date();
  const sameDay = d.toDateString() === now.toDateString();
  if (sameDay) return formatTime(d);
  const yesterday = new Date(now.getTime() - 86400000);
  if (d.toDateString() === yesterday.toDateString()) return `昨日 ${formatTime(d)}`;
  const sameYear = d.getFullYear() === now.getFullYear();
  return sameYear
    ? `${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
    : `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
};

/** 最近活跃时间展示 */
export const relativeTime = (value) => {
  if (!value) return '';
  const d = asDate(value);
  if (!d) return '';
  const diff = Date.now() - d.getTime();
  if (diff < 60 * 1000) return '刚刚';
  if (diff < 60 * 60 * 1000) return `${Math.floor(diff / 60000)} 分钟前`;
  if (diff < 24 * 60 * 60 * 1000) return `${Math.floor(diff / 3600000)} 小时前`;
  if (diff < 3 * 24 * 60 * 60 * 1000) return `${Math.floor(diff / 86400000)} 天前`;
  return formatDate(d);
};

/** 秒 → 12分30秒 */
export const durationText = (seconds) => {
  const total = Number(seconds);
  if (!Number.isFinite(total) || total <= 0) return '';
  if (total < 60) return `${Math.round(total)}秒`;
  const min = Math.floor(total / 60);
  const sec = Math.round(total % 60);
  return sec ? `${min}分${sec}秒` : `${min}分`;
};

export const toId = (value) => (value === undefined || value === null ? '' : String(value));

/* ============================== 用户 ============================== */

export const adaptUser = (user) => {
  if (!user) return null;
  return {
    ...user,
    id: toId(user.id),
    numericId: user.id,
    employeeId: user.employeeNo || user.employeeId || '',
    employeeNo: user.employeeNo || user.employeeId || '',
    title: user.jobTitle || user.title || '',
    jobTitle: user.jobTitle || user.title || '',
    role: user.role === 2 ? 'admin' : 'learner',
    roleCode: user.role,
    status: user.accountStatus === 2 ? 'inactive' : 'active',
    accountStatus: user.accountStatus,
    avatarUrl: user.avatarUrl || '',
    phone: user.phone || '',
    email: user.email || '',
    learningHours: Number(user.learningHours || 0),
    completedCourses: Number(user.completedCourses || 0),
    studyingCourses: Number(user.studyingCourses || 0),
    certificateCount: Number(user.certificateCount || 0),
    unreadNotifications: Number(user.unreadNotifications || 0),
    lastActive: relativeTime(user.lastActiveAt),
    lastActiveAt: user.lastActiveAt || '',
  };
};

/* ============================== 课程 ============================== */

export const adaptLesson = (lesson) => ({
  ...lesson,
  id: toId(lesson.id),
  numericId: lesson.id,
  name: lesson.title || lesson.name || '',
  duration: durationText(lesson.durationSeconds),
  url: lesson.videoUrl || lesson.url || '',
  completed: Boolean(lesson.completed),
  watchedSeconds: Number(lesson.watchedSeconds || 0),
});

export const adaptChapter = (chapter) => ({
  ...chapter,
  id: toId(chapter.id),
  numericId: chapter.id,
  name: chapter.title || chapter.name || '',
  lessons: (chapter.lessons || []).map(adaptLesson),
});

export const adaptCourse = (course) => {
  if (!course) return null;
  const totalLessons = course.totalLessons ?? course.totalLessonCount ?? 0;
  const completedLessons = course.completedLessons ?? course.completedLessonCount ?? 0;
  return {
    ...course,
    id: toId(course.id),
    numericId: course.id,
    courseId: toId(course.courseId ?? course.id),
    instructor: {
      ...(course.instructor || {}),
      name: course.instructor?.name || '',
      title: course.instructor?.title || '',
    },
    thumbnail: course.thumbnailUrl || course.thumbnail || '',
    thumbnailUrl: course.thumbnailUrl || course.thumbnail || '',
    hours: Number(course.hours ?? course.totalHours ?? 0),
    totalHours: Number(course.totalHours ?? course.hours ?? 0),
    students: Number(course.students ?? course.enrollCount ?? 0),
    totalLessons: Number(totalLessons),
    totalLessonCount: Number(totalLessons),
    completedLessons: Number(completedLessons),
    completedLessonCount: Number(completedLessons),
    progress: Number(course.progress ?? course.progressPercent ?? 0),
    progressPercent: Number(course.progressPercent ?? course.progress ?? 0),
    // 管理端 statusCode: 1 草稿 / 2 已上架 / 3 已下架
    published: course.statusCode === 2,
    syllabus: (course.chapters || []).map(adaptChapter),
    tags: course.tags || [],
  };
};

/** 管理端课程：沿用学员端字段，再补上管理状态与目录兜底 */
export const adaptAdminCourse = (course) => {
  const base = adaptCourse(course);
  if (!base) return null;
  const statusCode = course.statusCode ?? (course.published ? 2 : 1);
  return {
    ...base,
    statusCode,
    statusLabel: course.status || (statusCode === 2 ? '已上架' : statusCode === 3 ? '已下架' : '草稿'),
    categoryId: course.categoryId ?? course.category,
    badge: statusCode === 2 ? '在架' : statusCode === 3 ? '已下架' : '草稿',
    badgeType: statusCode === 2 ? 'primary' : 'default',
  };
};

export const adaptTrack = (track) => {
  if (!track) return null;
  return {
    ...track,
    id: track.extId || toId(track.id),
    numericId: track.id,
    thumbnail: track.thumbnailUrl || track.thumbnail || '',
    studentCount: track.studentCount ?? 0,
    outline: track.outline || [],
  };
};

/* ============================== 考试 ============================== */

const assignmentStatusToUi = (assignment) => {
  if (assignment.assignmentStatus === 2) return 'completed';
  if (assignment.examStatusCode === 3) return 'in_progress';
  if (assignment.examStatusCode === 5) return 'not_started';
  if (assignment.examStatusCode === 4) return 'completed';
  return 'not_started';
};

export const adaptExamAssignment = (assignment) => {
  if (!assignment) return null;
  const status = assignmentStatusToUi(assignment);
  return {
    ...assignment,
    id: toId(assignment.examId),
    examId: assignment.examId,
    code: assignment.code || '',
    category: assignment.organizer || assignment.category || '',
    status,
    statusLabel: assignment.statusLabel || '',
    location: assignment.location || '',
    timeWindow: assignment.timeWindow || '',
    durationMinutes: assignment.durationMinutes || 0,
    totalScore: assignment.totalScore || 100,
    passScore: assignment.passScore || 60,
    questionCount: assignment.questionCount || 0,
    isDoubleCameraProctored: Boolean(assignment.isDoubleCameraProctored),
    relatedCourse: assignment.relatedCourseText || '',
    remainingTimeText: assignment.remainingChance != null
      ? `剩余考评机会: ${assignment.remainingChance}次`
      : (status === 'in_progress' ? '进行中' : ''),
  };
};

export const adaptPastExam = (past) => {
  if (!past) return null;
  const passed = Boolean(past.isPassed);
  let status = 'failed';
  if (passed) {
    if (Number(past.score) >= Number(past.totalScore || 100)) status = 'passed_perfect';
    else if (Number(past.score) >= Number(past.passScore || 60) + 10) status = 'passed_excellent';
    else status = 'passed_qualified';
  }
  return {
    ...past,
    id: toId(past.attemptId ?? past.examId),
    examId: past.examId,
    attemptId: past.attemptId,
    category: past.category || '',
    type: past.type || '',
    completionDate: formatDateTime(past.completionDate || past.submittedAt),
    score: Number(past.score || 0),
    totalScore: Number(past.totalScore || 100),
    passScore: Number(past.passScore || 60),
    status,
    statusLabel: past.statusLabel || '',
    wrongCount: Number(past.wrongCount || 0),
    relatedCourse: past.relatedCourseText || '',
  };
};

export const adaptExamMetrics = (metrics) => ({
  ...(metrics || {}),
  inProgressCount: Number(metrics?.inProgressCount || 0),
  finishedCount: Number(metrics?.finishedCount || 0),
  avgPastScore: Number(metrics?.avgPastScore || 0),
  pendingCount: Number(metrics?.pendingCount || 0),
  passRate: Number(metrics?.passRate || 0),
});

export const adaptExamQuestion = (question) => {
  if (!question) return null;
  const options = (question.options || []).map((option) => ({
    ...option,
    key: option.key,
    text: option.text || option.content || '',
    explanation: option.explanation || '',
  }));
  let answer = question.correctAnswer ?? question.answer;
  if (Array.isArray(answer)) answer = answer.join(',');
  return {
    ...question,
    id: toId(question.id),
    numericId: question.id,
    // 考试在管理端统一用字符串 id，题目要按所属考试筛选时必须同类型
    examId: question.examId == null ? '' : toId(question.examId),
    questionNumber: question.questionNo,
    questionNo: question.questionNo,
    type: question.type,
    typeLabel: question.typeLabel,
    score: Number(question.score || 0),
    title: question.title,
    codeSnippet: question.codeContent
      ? {
          filename: question.codeFilename || '',
          runtime: question.codeRuntime || '',
          code: question.codeContent,
        }
      : null,
    options,
    answer,
    correctAnswer: answer,
    referenceAnswer: question.referenceAnswer || '',
    analysis: question.analysis || '',
  };
};

/* ============================== 证书 ============================== */

export const adaptCertificate = (cert) => {
  if (!cert) return null;
  return {
    ...cert,
    id: toId(cert.id),
    numericId: cert.id,
    title: cert.templateTitle || cert.title || '',
    level: cert.levelLabel || cert.level || '',
    validity: cert.validityLabel || cert.validity || '',
    rankingScore: cert.rankingScore || '',
    privilege: cert.privilegeText || cert.privilege || '',
    description: cert.description || '',
    issueDate: formatDate(cert.issueDate),
    category: cert.category || 'architecture',
    status: cert.status,
    statusLabel: cert.statusLabel || '',
    serialNumber: cert.serialNumber || '',
    issueOrg: cert.issueOrg || '',
    badgeType: cert.badgeType || 'blockchain',
  };
};

/* ============================== 消息 / 通知 ============================== */

const notificationType = (notice) => {
  if (notice.refType === 'exam') return 'exam';
  if (notice.refType === 'course') return 'course';
  if (notice.refType === 'leave' || notice.refType === 'attendance') return 'attendance';
  if (notice.refType === 'announcement') return 'announcement';
  const map = { 1: 'exam', 2: 'course', 3: 'attendance', 4: 'announcement', 5: 'attendance' };
  return map[notice.type] || 'announcement';
};

export const adaptNotification = (notice) => {
  if (!notice) return null;
  return {
    ...notice,
    id: toId(notice.id),
    numericId: notice.id,
    type: notificationType(notice),
    typeLabel: notice.typeLabel || '系统通知',
    sender: notice.senderName || '系统',
    time: formatMessageTime(notice.createdAt),
    preview: notice.summary || '',
    priority: Number(notice.priority) >= 2 ? 'urgent' : 'normal',
    isRead: Boolean(notice.isRead),
    attachmentsCount: 0,
    details: {
      fullContent: notice.summary || '',
      bulletPoints: [],
      officialNoticeId: notice.officialRefNo || '',
      examDuration: notice.durationLabel || '',
      passingScore: notice.passingScoreLabel || '',
      attachments: [],
    },
  };
};

/* ============================== 考勤 / 请假 ============================== */

export const adaptLeave = (leave) => {
  if (!leave) return null;
  const statusMap = { 1: 'pending', 2: 'approved', 3: 'rejected' };
  return {
    ...leave,
    id: toId(leave.id),
    numericId: leave.id,
    type: leave.leaveType || '',
    flowNumber: leave.flowNo || '',
    title: leave.trainingTitle || '',
    timeRange: leave.startDate === leave.endDate
      ? formatDate(leave.startDate)
      : `${formatDate(leave.startDate)} ~ ${formatDate(leave.endDate)}`,
    days: String(leave.leaveDays ?? ''),
    reason: leave.reason || '',
    status: statusMap[leave.status] || 'pending',
    statusLabel: leave.statusLabel || '',
    submittedTime: formatDateTime(leave.submittedAt),
    // 管理端审批列表字段
    applicant: leave.userName || leave.applicant || '',
    userName: leave.userName || leave.applicant || '',
    employeeId: leave.employeeNo || leave.employeeId || '',
    employeeNo: leave.employeeNo || leave.employeeId || '',
    department: leave.department || '',
    submittedAt: leave.submittedAt,
  };
};

/* ============================== 管理端 ============================== */

export const adaptAdminDepartment = (dept) => {
  if (!dept) return null;
  return {
    ...dept,
    id: toId(dept.id),
    numericId: dept.id,
    extId: dept.extId,
    manager: dept.managerName || dept.manager || '',
    managerName: dept.managerName || dept.manager || '',
    desc: dept.description || dept.desc || '',
    description: dept.description || dept.desc || '',
    status: dept.status === 2 ? 'inactive' : 'active',
    statusCode: dept.status,
    learners: Number(dept.learnerCount ?? dept.learnerCountCached ?? 0),
    learnerCount: Number(dept.learnerCount ?? 0),
    hours: Number(dept.totalStudyHours ?? dept.studyHoursCached ?? 0),
    totalStudyHours: Number(dept.totalStudyHours ?? 0),
    sortNo: dept.sortNo ?? 0,
  };
};

const examStateFromCode = (code) => {
  const map = { 1: 'draft', 2: 'scheduled', 3: 'running', 4: 'ended', 5: 'scheduled' };
  return map[code] || 'draft';
};

export const adaptAdminExam = (exam) => {
  if (!exam) return null;
  return {
    ...exam,
    id: toId(exam.id ?? exam.extId),
    numericId: exam.id,
    extId: exam.extId,
    code: exam.code || '',
    category: exam.organizer || exam.category || '',
    organizer: exam.organizer || exam.category || '',
    state: examStateFromCode(exam.statusCode),
    statusCode: exam.statusCode,
    status: exam.status || '',
    timeWindow: exam.timeWindow || (exam.startAt
      ? `${formatDateTime(exam.startAt)} ~ ${formatDateTime(exam.endAt)}`
      : ''),
    durationMinutes: exam.durationMinutes || 0,
    totalScore: exam.totalScore || 100,
    passScore: exam.passScore || 60,
    questionCount: exam.questionCount ?? exam.questionCountCached ?? 0,
    participants: exam.attemptCount ?? exam.participantCountCached ?? 0,
    attemptCount: exam.attemptCount ?? 0,
    passRate: exam.passRate ?? 0,
    avgScore: exam.avgScore ?? 0,
    relatedCourseText: exam.relatedCourseText || '',
    isDoubleCameraProctored: Boolean(exam.isDoubleCameraProctored ?? exam.isDoubleCamera),
  };
};

export const adaptAdminCertificate = (cert) => {
  const base = adaptCertificate(cert);
  if (!base) return null;
  return {
    ...base,
    templateId: cert.templateId,
    userId: cert.userId,
    // 管理端列表/弹窗沿用 holder 命名
    holder: cert.userName || cert.holder || '',
    userName: cert.userName || cert.holder || '',
    employeeNo: cert.employeeNo || '',
    employeeId: cert.employeeNo || cert.employeeId || '',
    department: cert.department || '',
    issueDate: formatDate(cert.issueDate),
    expireDate: cert.expireDate ? formatDate(cert.expireDate) : '',
    blockchainHash: cert.blockchainHash || '',
    statusCode: cert.status,
    status: cert.status === 2 ? 'issued' : cert.status === 3 ? 'revoked' : 'pending',
  };
};

export const adaptAdminAnnouncement = (notice) => {
  if (!notice) return null;
  return {
    ...notice,
    id: toId(notice.id),
    numericId: notice.id,
    subtitle: notice.content || '',
    status: notice.statusCode === 2 ? 'published' : 'draft',
    statusCode: notice.statusCode,
    author: notice.authorName || '系统',
    authorName: notice.authorName || '系统',
    publishTime: formatDateTime(notice.publishAt),
    publishAt: notice.publishAt,
    views: notice.viewCount || 0,
    viewCount: notice.viewCount || 0,
  };
};

export const adaptAdminCheckin = (checkin) => {
  if (!checkin) return null;
  const statusMap = { 1: 'normal', 2: 'late', 3: 'missing' };
  return {
    ...checkin,
    id: toId(checkin.id),
    numericId: checkin.id,
    employeeId: checkin.employeeNo || '',
    date: formatDate(checkin.checkinDate),
    checkinDate: formatDate(checkin.checkinDate),
    name: checkin.userName || checkin.name || '',
    userName: checkin.userName || checkin.name || '',
    employeeNo: checkin.employeeNo || '',
    department: checkin.department || '',
    checkIn: checkin.checkInTime || '--',
    checkOut: checkin.checkOutTime || '--',
    status: statusMap[checkin.status] || 'missing',
    statusLabel: checkin.statusLabel || '',
    method: checkin.method === 2 ? '线上' : '线下',
  };
};

/* ============================== 考试作答 ============================== */

export const adaptAttemptAnswer = (answer) => {
  if (!answer) return null;
  return {
    ...answer,
    questionId: answer.questionId,
    selectedKeys: answer.selectedKeys || '',
    selectedList: String(answer.selectedKeys || '').replace(/[^A-Za-z]/g, '').split(''),
    answerText: answer.answerText || '',
    isFlagged: Boolean(answer.isFlagged),
  };
};

/** 进行中的作答会话：POST /exams/{id}/start 与 GET /exams/attempts/{id} 共用 */
export const adaptExamSession = (session) => {
  if (!session) return null;
  const attemptId = session.attemptId ?? session.id;
  return {
    ...session,
    attemptId,
    numericAttemptId: attemptId,
    remainingSeconds: Number(session.remainingSeconds || 0),
    questions: (session.questions || []).map(adaptExamQuestion),
    answers: (session.answers || []).map(adaptAttemptAnswer),
  };
};

export const adaptExamPaper = (paper) => {
  if (!paper) return null;
  return {
    ...paper,
    id: toId(paper.id),
    numericId: paper.id,
    totalScore: Number(paper.totalScore || 100),
    passScore: Number(paper.passScore || 0),
    questions: (paper.questions || []).map(adaptExamQuestion),
    attempt: paper.attempt ? adaptExamSession(paper.attempt) : null,
  };
};
