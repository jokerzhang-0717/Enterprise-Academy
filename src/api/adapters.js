
const pad = (n) => String(n).padStart(2, '0');

const asDate = (value) => {
  if (!value) return null;
  if (value instanceof Date) return value;
  const text = String(value).replace(' ', 'T');
  const d = new Date(text);
  return Number.isNaN(d.getTime()) ? null : d;
};

export const formatDate = (value) => {
  if (!value) return '';
  const text = String(value);
  if (/^\d{4}-\d{2}-\d{2}/.test(text)) return text.slice(0, 10);
  const d = asDate(value);
  if (!d) return text;
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
};

export const formatDateTime = (value) => {
  if (!value) return '';
  const d = asDate(value);
  if (!d) return String(value);
  return `${formatDate(d)} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
};

export const formatTime = (value) => {
  if (!value) return '';
  const d = asDate(value);
  if (!d) return '';
  return `${pad(d.getHours())}:${pad(d.getMinutes())}`;
};

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

export const durationText = (seconds) => {
  const total = Number(seconds);
  if (!Number.isFinite(total) || total <= 0) return '';
  if (total < 60) return `${Math.round(total)}秒`;
  const min = Math.floor(total / 60);
  const sec = Math.round(total % 60);
  return sec ? `${min}分${sec}秒` : `${min}分`;
};

export const toId = (value) => (value === undefined || value === null ? '' : String(value));

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
    certificateCount: Number(user.certificateCount || 0),
    lastActive: relativeTime(user.lastActiveAt),
    lastActiveAt: user.lastActiveAt || '',
  };
};

export const adaptLesson = (lesson) => ({
  ...lesson,
  id: toId(lesson.id),
  numericId: lesson.id,
  name: lesson.title || lesson.name || '',
  duration: durationText(lesson.durationSeconds),
  url: lesson.videoUrl || lesson.url || '',
  completed: Boolean(lesson.completed),
  watchedSeconds: Number(lesson.watchedSeconds || 0),
  lastStudiedAt: lesson.lastStudiedAt || null,
});

export const adaptChapter = (chapter) => ({
  ...chapter,
  id: toId(chapter.id),
  numericId: chapter.id,
  name: chapter.title || chapter.name || '',
  lessons: (chapter.lessons || []).map(adaptLesson),
});

// 旧数据的“二级标签”直接提升为唯一分类字段；不再在界面里区分一级/二级
const categoryLabelOf = (course) =>
  (course?.subcategoryLabel || course?.categoryLabel || '').trim() || '视频课程';

export const adaptCourse = (course) => {
  if (!course) return null;
  const completedLessons = course.completedLessons ?? course.completedLessonCount ?? 0;
  return {
    ...course,
    categoryLabel: categoryLabelOf(course),
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
    completedLessons: Number(completedLessons),
    completedLessonCount: Number(completedLessons),
    progress: Number(course.progress ?? course.progressPercent ?? 0),
    progressPercent: Number(course.progressPercent ?? course.progress ?? 0),

    published: course.statusCode === 2,
    syllabus: (course.chapters || []).map(adaptChapter),
  };
};

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

const assignmentStatusToUi = (assignment) => {
  const examStatus = Number(assignment.examStatusCode);
  const allowed = Number(assignment.attemptsAllowed ?? 1);
  const used = Number(assignment.attemptsUsed ?? 0);
  const remaining = Math.max(0, allowed - used);

  if (examStatus === 2 || examStatus === 5) return 'not_started';
  if (examStatus === 4) return 'completed';
  if (examStatus === 3) {

    if (assignment.inProgressAttemptId != null) return 'in_progress';

    if (remaining > 0) return 'in_progress';
    return 'completed';
  }
  return 'not_started';
};

export const adaptExamAssignment = (assignment) => {
  if (!assignment) return null;
  const status = assignmentStatusToUi(assignment);
  const attemptsAllowed = Number(assignment.attemptsAllowed ?? 1);
  const attemptsUsed = Number(assignment.attemptsUsed ?? 0);
  return {
    ...assignment,
    id: toId(assignment.examId),
    examId: assignment.examId,
    code: assignment.code || '',
    organizer: assignment.organizer || '',
    status,
    statusLabel: assignment.statusLabel || '',
    location: assignment.location || '',
    timeWindow: assignment.timeWindow || '',
    durationMinutes: assignment.durationMinutes || 0,
    totalScore: assignment.totalScore || 100,
    passScore: assignment.passScore || 60,
    questionCount: assignment.questionCount || 0,
    attemptsAllowed,
    attemptsUsed,
    remainingAttempts: Math.max(0, attemptsAllowed - attemptsUsed),
    inProgressAttemptId: assignment.inProgressAttemptId ?? null,
  };
};

export const adaptPastExam = (past) => {
  if (!past) return null;
  const pendingReview = Boolean(past.pendingReview) || Number(past.status) === 2;
  const passed = Boolean(past.isPassed);
  const fullScore = Number(past.examTotalScore || past.totalScore || 100);
  let status = 'failed';
  if (pendingReview) status = 'pending_review';
  else if (passed) {
    if (Number(past.score) >= fullScore) status = 'passed_perfect';
    else if (Number(past.score) >= Number(past.passScore || 60) + 10) status = 'passed_excellent';
    else status = 'passed_qualified';
  }
  return {
    ...past,
    id: toId(past.attemptId ?? past.examId),
    examId: past.examId,
    attemptId: past.attemptId,
    attemptNo: Number(past.attemptNo || 0),
    type: past.type || '',
    completionDate: formatDateTime(past.completionDate || past.submittedAt),
    score: Number(past.score ?? past.totalScore ?? 0),
    objectiveScore: Number(past.objectiveScore || 0),
    subjectiveScore: Number(past.subjectiveScore || 0),
    attemptTotalScore: Number(past.totalScore || 0),
    examTotalScore: Number(past.examTotalScore || past.totalScore || 100),
    totalScore: Number(past.examTotalScore || past.totalScore || 100),
    passScore: Number(past.passScore || 60),
    pendingReview,
    status,
    statusLabel: past.statusLabel || (pendingReview ? '待人工评分' : ''),
    wrongCount: Number(past.wrongCount || 0),
    answeredCount: Number(past.answeredCount || 0),
    questionCount: Number(past.questionCount || 0),
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

    examId: question.examId == null ? '' : toId(question.examId),
    questionNumber: question.questionNo,
    questionNo: question.questionNo,
    type: question.type,
    typeLabel: question.typeLabel,
    score: Number(question.score || 0),
    title: question.title,
    options,
    answer,
    correctAnswer: answer,
    referenceAnswer: question.referenceAnswer || '',
    analysis: question.analysis || '',
  };
};

export const adaptCertificate = (cert) => {
  if (!cert) return null;
  return {
    ...cert,
    id: toId(cert.id),
    numericId: cert.id,
    title: cert.templateTitle || cert.title || '',
    level: cert.levelLabel || cert.level || '',
    validity: cert.validityLabel || cert.validity || '',
    privilege: cert.privilegeText || cert.privilege || '',
    description: cert.description || '',
    issueDate: formatDate(cert.issueDate),
    category: cert.category || '',
    status: cert.status,
    statusLabel: cert.statusLabel || '',
    serialNumber: cert.serialNumber || '',
    issueOrg: cert.issueOrg || '',
  };
};

export const adaptAnnouncementMessage = (announcement) => {
  if (!announcement) return null;
  const content = announcement.content || '';
  const plain = content.replace(/\s+/g, ' ').trim();
  return {
    ...announcement,
    id: toId(announcement.id),
    numericId: announcement.id,
    type: 'announcement',
    typeLabel: announcement.isTop ? '置顶公告' : '企业公告',
    sender: announcement.authorName || '企业培训中心',
    time: formatMessageTime(announcement.publishAt || announcement.createdAt),
    preview: plain.length > 120 ? `${plain.slice(0, 120)}…` : plain,
    priority: announcement.isTop ? 'urgent' : 'normal',
    isRead: Boolean(announcement.isRead),
    attachmentsCount: 0,
    details: {
      fullContent: content,
      bulletPoints: [],
      examDuration: '',
      passingScore: '',
      attachments: [],
    },
  };
};

export const adaptLeave = (leave) => {
  if (!leave) return null;
  const statusMap = { 1: 'pending', 2: 'approved', 3: 'rejected' };
  return {
    ...leave,
    id: toId(leave.id),
    numericId: leave.id,
    type: leave.leaveType || '',
    flowNumber: leave.flowNo || '',
    timeRange: leave.startDate === leave.endDate
      ? formatDate(leave.startDate)
      : `${formatDate(leave.startDate)} ~ ${formatDate(leave.endDate)}`,
    days: String(leave.leaveDays ?? ''),
    reason: leave.reason || '',
    status: statusMap[leave.status] || 'pending',
    statusLabel: leave.statusLabel || '',
    submittedTime: formatDateTime(leave.submittedAt),
    applicant: leave.userName || leave.applicant || '',
    userName: leave.userName || leave.applicant || '',
    avatarUrl: leave.avatarUrl || '',
    employeeId: leave.employeeNo || leave.employeeId || '',
    employeeNo: leave.employeeNo || leave.employeeId || '',
    department: leave.department || '',
    submittedAt: leave.submittedAt,
  };
};

export const adaptAdminDepartment = (dept) => {
  if (!dept) return null;
  return {
    ...dept,
    id: toId(dept.id),
    numericId: dept.id,
    manager: dept.managerName || dept.manager || '',
    managerName: dept.managerName || dept.manager || '',
    desc: dept.description || dept.desc || '',
    description: dept.description || dept.desc || '',
    status: dept.status === 2 ? 'inactive' : 'active',
    statusCode: dept.status,
    learners: Number(dept.learnerCount ?? 0),
    learnerCount: Number(dept.learnerCount ?? 0),
    hours: Number(dept.totalStudyHours ?? 0),
    totalStudyHours: Number(dept.totalStudyHours ?? 0),
    sortNo: dept.sortNo ?? 0,
  };
};

const examStateFromCode = (code) => {
  const map = { 1: 'draft', 2: 'scheduled', 3: 'running', 4: 'ended' };
  return map[code] || 'draft';
};

export const adaptAdminExam = (exam) => {
  if (!exam) return null;
  return {
    ...exam,
    id: toId(exam.id),
    numericId: exam.id,
    code: exam.code || '',
    organizer: exam.organizer || '',
    state: examStateFromCode(exam.statusCode),
    statusCode: exam.statusCode,
    status: exam.status || '',
    timeWindow: exam.timeWindow || (exam.startAt
      ? `${formatDateTime(exam.startAt)} ~ ${formatDateTime(exam.endAt)}`
      : ''),
    durationMinutes: exam.durationMinutes || 0,
    totalScore: exam.totalScore || 100,
    passScore: exam.passScore || 60,
    questionCount: exam.questionCount ?? 0,
    attemptCount: exam.attemptCount ?? 0,
  };
};

export const adaptAdminCertificate = (cert) => {
  const base = adaptCertificate(cert);
  if (!base) return null;
  return {
    ...base,
    templateId: cert.templateId,
    userId: cert.userId,

    holder: cert.userName || cert.holder || '',
    userName: cert.userName || cert.holder || '',
    avatarUrl: cert.avatarUrl || '',
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
  };
};

export const adaptAdminCheckin = (checkin) => {
  if (!checkin) return null;
  const statusMap = { 1: 'normal', 2: 'late', 3: 'missing', 4: 'early' };
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
  };
};

export const adaptAttemptAnswer = (answer) => {
  if (!answer) return null;
  return {
    ...answer,
    questionId: answer.questionId,
    questionNo: Number(answer.questionNo || 0),
    type: Number(answer.type || 0),
    title: answer.title || '',
    score: Number(answer.score || 0),
    scoreGained: Number(answer.scoreGained || 0),
    referenceAnswer: answer.referenceAnswer || '',
    analysis: answer.analysis || '',
    selectedKeys: answer.selectedKeys || '',
    selectedList: String(answer.selectedKeys || '').replace(/[^A-Za-z]/g, '').split(''),
    answerText: answer.answerText || '',
    isFlagged: Boolean(answer.isFlagged),
  };
};

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
