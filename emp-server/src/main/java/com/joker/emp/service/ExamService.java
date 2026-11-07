package com.joker.emp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.joker.emp.common.BizException;
import com.joker.emp.common.Numbers;
import com.joker.emp.common.PageResult;
import com.joker.emp.entity.AttemptAnswer;
import com.joker.emp.entity.Exam;
import com.joker.emp.entity.ExamAssignment;
import com.joker.emp.entity.ExamAttempt;
import com.joker.emp.entity.ExamQuestion;
import com.joker.emp.entity.QuestionOption;
import com.joker.emp.entity.SysUser;
import com.joker.emp.mapper.AttemptAnswerMapper;
import com.joker.emp.mapper.ExamAssignmentMapper;
import com.joker.emp.mapper.ExamAttemptMapper;
import com.joker.emp.mapper.ExamMapper;
import com.joker.emp.mapper.SysUserMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExamService {

  private final ExamMapper examMapper;
  private final ExamAssignmentMapper assignmentMapper;
  private final ExamAttemptMapper attemptMapper;
  private final AttemptAnswerMapper answerMapper;
  private final SysUserMapper userMapper;
  private final QuestionService questionService;
  private final OperationLogService logService;

  public ExamService(ExamMapper examMapper, ExamAssignmentMapper assignmentMapper,
      ExamAttemptMapper attemptMapper, AttemptAnswerMapper answerMapper,
      SysUserMapper userMapper, QuestionService questionService,
      OperationLogService logService) {
    this.examMapper = examMapper;
    this.assignmentMapper = assignmentMapper;
    this.attemptMapper = attemptMapper;
    this.answerMapper = answerMapper;
    this.userMapper = userMapper;
    this.questionService = questionService;
    this.logService = logService;
  }

  // ------------------------------------------------------------------ 管理端

  public PageResult<Map<String, Object>> page(long page, long size, String keyword,
      Integer status) {
    IPage<Map<String, Object>> result = examMapper.selectOverviewPage(new Page<>(page, size),
        keyword, status);
    List<Map<String, Object>> records = new ArrayList<>();
    for (Map<String, Object> row : result.getRecords()) {
      Map<String, Object> item = new LinkedHashMap<>(row);
      Long id = (long) Numbers.toInt(row.get("id"), 0);
      Exam exam = examMapper.selectById(id);
      if (exam != null) {
        item.put("extId", exam.getExtId());
        item.put("organizer", exam.getOrganizer());
        item.put("examType", exam.getExamType());
        item.put("location", exam.getLocation());
        item.put("startAt", exam.getStartAt());
        item.put("endAt", exam.getEndAt());
        item.put("durationMinutes", exam.getDurationMinutes());
        item.put("totalScore", exam.getTotalScore());
        item.put("isDoubleCamera", exam.getIsDoubleCamera());
        item.put("relatedCourseId", exam.getRelatedCourseId());
        item.put("relatedCourseText", exam.getRelatedCourseText());
        item.put("remainingChanceCached", exam.getRemainingChanceCached());
      }
      item.put("statusCode", row.get("status"));
      item.put("status", statusText(row.get("status")));
      records.add(item);
    }
    return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), records);
  }

  public Map<String, Object> detail(Long id) {
    Exam exam = requireExam(id);
    Map<String, Object> item = toExamItem(exam);
    item.put("questions", questionService.list(id, null, null, false));
    item.put("assignments", assignmentMapper.selectAllByExam(id).size());
    long attempts = attemptMapper.selectCount(Wrappers.<ExamAttempt>lambdaQuery()
        .eq(ExamAttempt::getExamId, id));
    long graded = attemptMapper.selectCount(Wrappers.<ExamAttempt>lambdaQuery()
        .eq(ExamAttempt::getExamId, id)
        .in(ExamAttempt::getStatus, 3, 4));
    item.put("attemptCount", attempts);
    item.put("gradedCount", graded);
    return item;
  }

  @Transactional
  public Map<String, Object> create(ExamSaveRequest request) {
    validate(request);
    if (request.status() != null && request.status() != 1) {
      throw new BizException("新建考试只能保存为待发布状态");
    }
    Exam exam = new Exam();
    apply(exam, request);
    exam.setStatus(request.status() == null ? 1 : request.status());
    exam.setDurationMinutes(request.durationMinutes() == null ? 60 : request.durationMinutes());
    exam.setTotalScore(request.totalScore() == null ? BigDecimal.valueOf(100) : request.totalScore());
    exam.setPassScore(request.passScore() == null ? BigDecimal.valueOf(60) : request.passScore());
    exam.setQuestionCountCached(0);
    exam.setParticipantCountCached(0);
    exam.setCreatedBy(com.joker.emp.security.AuthContext.userId());
    examMapper.insert(exam);
    if (request.questionIds() != null) {
      updateQuestionSet(exam, request.questionIds());
    }
    logService.record("exam", "新增考试 " + exam.getTitle(), "exam", exam.getId());
    return detail(exam.getId());
  }

  @Transactional
  public Map<String, Object> update(Long id, ExamSaveRequest request) {
    Exam exam = requireExam(id);
    if (request.questionIds() != null) {
      questionService.replaceExamQuestions(id, request.questionIds());
    }
    apply(exam, request);
    if (request.status() != null) exam.setStatus(request.status());
    if (request.durationMinutes() != null) exam.setDurationMinutes(request.durationMinutes());
    if (request.totalScore() != null) exam.setTotalScore(request.totalScore());
    if (request.passScore() != null) exam.setPassScore(request.passScore());
    if (request.questionIds() != null) {
      exam.setTotalScore(totalQuestionScore(id));
      exam.setQuestionCountCached((int) questionService.countByExam(id));
      if (exam.getPassScore() != null && exam.getPassScore().compareTo(exam.getTotalScore()) > 0) {
        throw new BizException("及格分不能超过试卷总分");
      }
    }
    if (request.status() != null) {
      validateStatus(id, request.status());
    }
    examMapper.updateById(exam);
    if (request.status() != null && isPublished(request.status())) {
      assignToActiveLearners(id);
      refreshParticipantCount(id);
    }
    logService.record("exam", "更新考试 " + exam.getTitle(), "exam", id);
    return detail(id);
  }

  private void updateQuestionSet(Exam exam, List<Long> questionIds) {
    questionService.replaceExamQuestions(exam.getId(), questionIds);
    exam.setTotalScore(totalQuestionScore(exam.getId()));
    if (exam.getPassScore() != null && exam.getPassScore().compareTo(exam.getTotalScore()) > 0) {
      throw new BizException("及格分不能超过试卷总分");
    }
    exam.setQuestionCountCached((int) questionService.countByExam(exam.getId()));
    examMapper.updateById(exam);
  }

  private BigDecimal totalQuestionScore(Long examId) {
    return questionService.questionsOf(examId).stream()
        .map(question -> question.getScore() == null ? BigDecimal.ZERO : question.getScore())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  @Transactional
  public void remove(Long id) {
    Exam exam = requireExam(id);
    examMapper.deleteById(id);
    logService.record("exam", "删除考试 " + exam.getTitle(), "exam", id);
  }

  @Transactional
  public void updateStatus(Long id, Integer status) {
    validateStatus(id, status);
    Exam exam = requireExam(id);
    Exam update = new Exam();
    update.setId(id);
    update.setStatus(status);
    update.setQuestionCountCached((int) questionService.countByExam(id));
    examMapper.updateById(update);
    if (isPublished(status)) {
      assignToActiveLearners(id);
      refreshParticipantCount(id);
    }
    logService.record("exam", statusText(status) + "考试 " + exam.getTitle(), "exam", id);
  }

  private boolean isPublished(Integer status) {
    return status != null && (status == 2 || status == 3);
  }

  private void assignToActiveLearners(Long examId) {
    List<SysUser> learners = userMapper.selectList(Wrappers.<SysUser>lambdaQuery()
        .eq(SysUser::getRole, 1)
        .eq(SysUser::getAccountStatus, 1));
    if (learners.isEmpty()) {
      return;
    }

    List<Long> assignedUserIds = assignmentMapper.selectList(
        Wrappers.<ExamAssignment>lambdaQuery().eq(ExamAssignment::getExamId, examId))
        .stream().map(ExamAssignment::getUserId).toList();
    java.util.Set<Long> assigned = new java.util.HashSet<>(assignedUserIds);
    for (SysUser learner : learners) {
      if (assigned.add(learner.getId())) {
        ExamAssignment assignment = new ExamAssignment();
        assignment.setExamId(examId);
        assignment.setUserId(learner.getId());
        assignment.setStatus(1);
        assignment.setAttemptsAllowed(1);
        assignment.setAttemptsUsed(0);
        assignmentMapper.insert(assignment);
      }
    }
  }

  private void validateStatus(Long id, Integer status) {
    if (status == null || status < 1 || status > 4) {
      throw new BizException("考试状态不合法");
    }
    long questionCount = questionService.countByExam(id);
    if ((status == 2 || status == 3) && questionCount == 0) {
      throw new BizException("试卷还没有题目，无法发布");
    }
  }

  public PageResult<Map<String, Object>> assignments(Long examId, long page, long size,
      String keyword) {
    requireExam(examId);
    IPage<Map<String, Object>> result = assignmentMapper.selectByExam(new Page<>(page, size),
        examId, keyword);
    for (Map<String, Object> row : result.getRecords()) {
      row.put("statusLabel", assignmentStatusText(row.get("status")));
    }
    return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(),
        result.getRecords());
  }

  @Transactional
  public int assign(Long examId, AssignRequest request) {
    requireExam(examId);
    List<Long> userIds = request.userIds();
    if (userIds == null || userIds.isEmpty()) {
      // 未指定人员时，指派给全部启用学员
      userIds = userMapper.selectList(Wrappers.<SysUser>lambdaQuery()
              .eq(SysUser::getRole, 1)
              .eq(SysUser::getAccountStatus, 1))
          .stream().map(SysUser::getId).toList();
    }
    int created = 0;
    for (Long userId : userIds) {
      ExamAssignment exists = assignmentMapper.selectOne(Wrappers.<ExamAssignment>lambdaQuery()
          .eq(ExamAssignment::getExamId, examId)
          .eq(ExamAssignment::getUserId, userId)
          .last("LIMIT 1"));
      if (exists != null) {
        exists.setAttemptsAllowed(request.attemptsAllowed() == null
            ? exists.getAttemptsAllowed() : request.attemptsAllowed());
        exists.setDeadlineAt(request.deadlineAt() == null
            ? exists.getDeadlineAt() : request.deadlineAt());
        exists.setStatus(1);
        assignmentMapper.updateById(exists);
      } else {
        ExamAssignment assignment = new ExamAssignment();
        assignment.setExamId(examId);
        assignment.setUserId(userId);
        assignment.setStatus(1);
        assignment.setAttemptsAllowed(request.attemptsAllowed() == null
            ? 1 : request.attemptsAllowed());
        assignment.setAttemptsUsed(0);
        assignment.setDeadlineAt(request.deadlineAt());
        assignmentMapper.insert(assignment);
        created++;
      }
    }
    refreshParticipantCount(examId);
    logService.record("exam", "指派考试，新增 " + created + " 人", "exam", examId);
    return created;
  }

  @Transactional
  public void unassign(Long examId, Long assignmentId) {
    ExamAssignment assignment = assignmentMapper.selectById(assignmentId);
    if (assignment == null || !examId.equals(assignment.getExamId())) {
      throw BizException.notFound("指派记录不存在");
    }
    assignmentMapper.deleteById(assignmentId);
    refreshParticipantCount(examId);
  }

  public PageResult<Map<String, Object>> attempts(long page, long size, Long examId,
      Integer status, String keyword) {
    IPage<Map<String, Object>> result = attemptMapper.selectAdminPage(new Page<>(page, size),
        examId, status, keyword);
    for (Map<String, Object> row : result.getRecords()) {
      row.put("statusLabel", attemptStatusText(row.get("status")));
    }
    return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(),
        result.getRecords());
  }

  public Map<String, Object> attemptDetail(Long attemptId, Long userId, boolean withAnswer) {
    ExamAttempt attempt = attemptMapper.selectById(attemptId);
    if (attempt == null) {
      throw BizException.notFound("答卷不存在");
    }
    if (userId != null && !userId.equals(attempt.getUserId())) {
      throw BizException.forbidden("无权查看该答卷");
    }
    Exam exam = requireExam(attempt.getExamId());
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("id", attempt.getId());
    item.put("examId", attempt.getExamId());
    item.put("examTitle", exam.getTitle());
    item.put("code", exam.getCode());
    item.put("userId", attempt.getUserId());
    SysUser user = userMapper.selectById(attempt.getUserId());
    item.put("userName", user == null ? null : user.getName());
    item.put("employeeNo", user == null ? null : user.getEmployeeNo());
    item.put("attemptNo", attempt.getAttemptNo());
    item.put("status", attempt.getStatus());
    item.put("statusLabel", attemptStatusText(attempt.getStatus()));
    item.put("startedAt", attempt.getStartedAt());
    item.put("submittedAt", attempt.getSubmittedAt());
    item.put("remainingSeconds", attempt.getRemainingSeconds());
    item.put("objectiveScore", attempt.getObjectiveScore());
    item.put("subjectiveScore", attempt.getSubjectiveScore());
    item.put("totalScore", attempt.getTotalScore());
    item.put("passScore", attempt.getPassScore());
    item.put("isPassed", attempt.getIsPassed());
    item.put("wrongCount", attempt.getWrongCount());
    item.put("switchCount", attempt.getSwitchCount());
    item.put("gradedAt", attempt.getGradedAt());

    List<Map<String, Object>> answers = answerMapper.selectDetail(attemptId);
    if (!withAnswer) {
      for (Map<String, Object> answer : answers) {
        answer.remove("correctAnswer");
        answer.remove("referenceAnswer");
        answer.remove("analysis");
      }
    }
    item.put("answers", answers);
    return item;
  }

  @Transactional
  public Map<String, Object> grade(Long attemptId, GradeRequest request) {
    ExamAttempt attempt = attemptMapper.selectById(attemptId);
    if (attempt == null) {
      throw BizException.notFound("答卷不存在");
    }
    if (attempt.getStatus() == null || attempt.getStatus() == 1) {
      throw new BizException("该答卷尚未交卷");
    }
    BigDecimal subjective = BigDecimal.ZERO;
    if (request.answers() != null) {
      for (AnswerGrade grade : request.answers()) {
        AttemptAnswer answer = answerMapper.selectById(grade.answerId());
        if (answer == null || !attemptId.equals(answer.getAttemptId())) {
          continue;
        }
        BigDecimal score = grade.scoreGained() == null ? BigDecimal.ZERO : grade.scoreGained();
        answer.setScoreGained(score);
        answer.setIsCorrect(score.compareTo(BigDecimal.ZERO) > 0);
        answerMapper.updateById(answer);
        subjective = subjective.add(score);
      }
    } else if (request.subjectiveScore() != null) {
      subjective = request.subjectiveScore();
    }
    BigDecimal objective = attempt.getObjectiveScore() == null
        ? BigDecimal.ZERO : attempt.getObjectiveScore();
    BigDecimal total = objective.add(subjective);
    BigDecimal passScore = attempt.getPassScore() == null
        ? BigDecimal.valueOf(60) : attempt.getPassScore();

    attempt.setSubjectiveScore(subjective);
    attempt.setTotalScore(total);
    attempt.setIsPassed(total.compareTo(passScore) >= 0);
    attempt.setStatus(3);
    attempt.setGradedAt(LocalDateTime.now());
    attemptMapper.updateById(attempt);

    ExamAssignment assignment = assignmentMapper.selectOne(
        Wrappers.<ExamAssignment>lambdaQuery()
            .eq(ExamAssignment::getExamId, attempt.getExamId())
            .eq(ExamAssignment::getUserId, attempt.getUserId())
            .last("LIMIT 1"));
    if (assignment != null) {
      assignment.setStatus(2);
      assignmentMapper.updateById(assignment);
    }
    logService.record("exam", "评分答卷 #" + attemptId + "，总分 " + total, "attempt", attemptId);
    return attemptDetail(attemptId, null, true);
  }

  // ------------------------------------------------------------------ 学员端

  public Map<String, Object> myExams() {
    Long userId = com.joker.emp.security.AuthContext.userId();
    List<Map<String, Object>> assignments = examMapper.selectAssignments(userId);
    for (Map<String, Object> item : assignments) {
      // 注意：先取出原始值再覆盖 status，避免把已转成文案的字符串再强转 Integer
      Integer examStatus = Numbers.toInteger(item.get("status"));
      Integer assignmentStatus = Numbers.toInteger(item.get("assignmentStatus"));
      item.put("statusCode", examStatus);
      item.put("examStatusCode", examStatus);
      item.put("examStatus", statusText(examStatus));
      item.put("assignmentStatusCode", assignmentStatus);
      item.put("status", assignmentStatusText(assignmentStatus));
      item.put("statusLabel", assignmentStatusText(assignmentStatus));
      item.put("timeWindow", timeWindow(item.get("startAt"), item.get("endAt")));
      item.put("isDoubleCameraProctored", item.get("isDoubleCamera"));
      item.put("category", item.get("organizer"));
    }
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("assignments", assignments);
    result.put("metrics", metrics(userId));
    result.put("past", pastAttempts(userId));
    return result;
  }

  public Map<String, Object> metrics(Long userId) {
    Map<String, Object> metrics = examMapper.selectMetrics(userId);
    if (metrics == null) {
      metrics = new LinkedHashMap<>();
      metrics.put("pendingCount", 0);
      metrics.put("inProgressCount", 0);
      metrics.put("finishedCount", 0);
      metrics.put("avgPastScore", null);
      metrics.put("passRate", null);
    }
    return metrics;
  }

  public List<Map<String, Object>> pastAttempts(Long userId) {
    List<Map<String, Object>> rows = examMapper.selectPastAttempts(userId);
    for (Map<String, Object> row : rows) {
      row.put("statusLabel", attemptStatusText(row.get("status")));
      row.put("completionDate", row.get("submittedAt"));
      row.put("score", row.get("totalScore"));
    }
    return rows;
  }

  public Map<String, Object> paper(Long examId) {
    Long userId = com.joker.emp.security.AuthContext.userId();
    Exam exam = requireExam(examId);
    ExamAssignment assignment = assignmentMapper.selectOne(Wrappers.<ExamAssignment>lambdaQuery()
        .eq(ExamAssignment::getExamId, examId)
        .eq(ExamAssignment::getUserId, userId)
        .last("LIMIT 1"));
    if (assignment == null) {
      throw BizException.forbidden("你没有被指派参加该考试");
    }
    ExamAttempt inProgress = attemptMapper.selectOne(Wrappers.<ExamAttempt>lambdaQuery()
        .eq(ExamAttempt::getExamId, examId)
        .eq(ExamAttempt::getUserId, userId)
        .eq(ExamAttempt::getStatus, 1)
        .orderByDesc(ExamAttempt::getId)
        .last("LIMIT 1"));
    Map<String, Object> item = toExamItem(exam);
    item.put("assignment", assignment);
    item.put("questionCount", questionService.countByExam(examId));
    item.put("questions", questionService.learnerQuestions(examId));
    item.put("attempt", inProgress);
    return item;
  }

  @Transactional
  public Map<String, Object> start(Long examId) {
    Long userId = com.joker.emp.security.AuthContext.userId();
    Exam exam = requireExam(examId);
    if (!Integer.valueOf(3).equals(exam.getStatus())) {
      throw new BizException("该考试当前未开放");
    }
    ExamAssignment assignment = assignmentMapper.selectOne(Wrappers.<ExamAssignment>lambdaQuery()
        .eq(ExamAssignment::getExamId, examId)
        .eq(ExamAssignment::getUserId, userId)
        .last("LIMIT 1"));
    if (assignment == null) {
      throw BizException.forbidden("你没有被指派参加该考试");
    }
    ExamAttempt inProgress = attemptMapper.selectOne(Wrappers.<ExamAttempt>lambdaQuery()
        .eq(ExamAttempt::getExamId, examId)
        .eq(ExamAttempt::getUserId, userId)
        .eq(ExamAttempt::getStatus, 1)
        .orderByDesc(ExamAttempt::getId)
        .last("LIMIT 1"));
    if (inProgress != null) {
      return attemptPayload(inProgress, exam);
    }
    int allowed = assignment.getAttemptsAllowed() == null ? 1 : assignment.getAttemptsAllowed();
    int used = assignment.getAttemptsUsed() == null ? 0 : assignment.getAttemptsUsed();
    if (used >= allowed) {
      throw new BizException("已达到允许的考试次数");
    }
    ExamAttempt attempt = new ExamAttempt();
    attempt.setExamId(examId);
    attempt.setUserId(userId);
    attempt.setAttemptNo(used + 1);
    attempt.setStatus(1);
    attempt.setStartedAt(LocalDateTime.now());
    attempt.setRemainingSeconds(exam.getDurationMinutes() == null
        ? 3600 : exam.getDurationMinutes() * 60);
    attempt.setPassScore(exam.getPassScore());
    attempt.setWrongCount(0);
    attempt.setSwitchCount(0);
    attemptMapper.insert(attempt);
    return attemptPayload(attempt, exam);
  }

  @Transactional
  public Map<String, Object> saveAnswer(Long attemptId, AnswerSaveRequest request) {
    Long userId = com.joker.emp.security.AuthContext.userId();
    ExamAttempt attempt = requireOwnAttempt(attemptId, userId);
    if (!Integer.valueOf(1).equals(attempt.getStatus())) {
      throw new BizException("答卷已提交，无法继续作答");
    }
    ExamQuestion question = questionService.list(attempt.getExamId(), null, null, false).stream()
        .map(q -> (Long) q.get("id"))
        .filter(id -> id.equals(request.questionId()))
        .findFirst().map(id -> {
          ExamQuestion q = new ExamQuestion();
          q.setId(id);
          return q;
        }).orElse(null);
    if (question == null) {
      throw BizException.notFound("题目不属于该考试");
    }
    AttemptAnswer answer = answerMapper.selectOne(Wrappers.<AttemptAnswer>lambdaQuery()
        .eq(AttemptAnswer::getAttemptId, attemptId)
        .eq(AttemptAnswer::getQuestionId, request.questionId())
        .last("LIMIT 1"));
    String selected = request.selectedKeys() == null ? null
        : normalizeKeys(request.selectedKeys());
    if (answer == null) {
      answer = new AttemptAnswer();
      answer.setAttemptId(attemptId);
      answer.setQuestionId(request.questionId());
      answer.setSelectedKeys(selected);
      answer.setAnswerText(request.answerText());
      answer.setIsFlagged(Boolean.TRUE.equals(request.flagged()));
      answer.setAnsweredAt(LocalDateTime.now());
      answerMapper.insert(answer);
    } else {
      answer.setSelectedKeys(selected);
      answer.setAnswerText(request.answerText());
      if (request.flagged() != null) {
        answer.setIsFlagged(request.flagged());
      }
      answer.setAnsweredAt(LocalDateTime.now());
      answerMapper.updateById(answer);
    }
    if (request.remainingSeconds() != null) {
      attempt.setRemainingSeconds(request.remainingSeconds());
      attemptMapper.updateById(attempt);
    }
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("answerId", answer.getId());
    result.put("questionId", answer.getQuestionId());
    result.put("selectedKeys", answer.getSelectedKeys());
    result.put("answerText", answer.getAnswerText());
    result.put("remainingSeconds", attempt.getRemainingSeconds());
    return result;
  }

  @Transactional
  public Map<String, Object> switchScreen(Long attemptId) {
    Long userId = com.joker.emp.security.AuthContext.userId();
    ExamAttempt attempt = requireOwnAttempt(attemptId, userId);
    if (!Integer.valueOf(1).equals(attempt.getStatus())) {
      return Map.of("switchCount", attempt.getSwitchCount());
    }
    attempt.setSwitchCount((attempt.getSwitchCount() == null ? 0 : attempt.getSwitchCount()) + 1);
    attemptMapper.updateById(attempt);
    return Map.of("switchCount", attempt.getSwitchCount());
  }

  @Transactional
  public Map<String, Object> submit(Long attemptId) {
    Long userId = com.joker.emp.security.AuthContext.userId();
    ExamAttempt attempt = requireOwnAttempt(attemptId, userId);
    if (!Integer.valueOf(1).equals(attempt.getStatus())) {
      throw new BizException("该答卷已提交");
    }
    List<ExamQuestion> questions = questionService.questionsOf(attempt.getExamId());
    Map<Long, AttemptAnswer> answers = answerMapper.selectList(
            Wrappers.<AttemptAnswer>lambdaQuery()
                .eq(AttemptAnswer::getAttemptId, attemptId))
        .stream().collect(Collectors.toMap(AttemptAnswer::getQuestionId, Function.identity()));

    BigDecimal objective = BigDecimal.ZERO;
    BigDecimal subjectiveExisting = BigDecimal.ZERO;
    int wrong = 0;
    boolean hasSubjective = false;
    for (ExamQuestion question : questions) {
      AttemptAnswer answer = answers.get(question.getId());
      if (question.getType() != null && question.getType() == 4) {
        hasSubjective = true;
        if (answer != null && answer.getScoreGained() != null) {
          subjectiveExisting = subjectiveExisting.add(answer.getScoreGained());
        }
        continue;
      }
      if (answer == null) {
        wrong++;
        continue;
      }
      boolean correct = answer.getSelectedKeys() != null
          && question.getCorrectAnswer() != null
          && normalizeKeys(answer.getSelectedKeys())
              .equals(normalizeKeys(question.getCorrectAnswer()));
      answer.setIsCorrect(correct);
      answer.setScoreGained(correct
          ? (question.getScore() == null ? BigDecimal.ZERO : question.getScore())
          : BigDecimal.ZERO);
      if (answer.getAnsweredAt() == null) {
        answer.setAnsweredAt(LocalDateTime.now());
      }
      answerMapper.updateById(answer);
      if (correct) {
        objective = objective.add(answer.getScoreGained());
      } else {
        wrong++;
      }
    }

    BigDecimal passScore = attempt.getPassScore() == null
        || attempt.getPassScore().compareTo(BigDecimal.ZERO) == 0
        ? (examMapper.selectById(attempt.getExamId()).getPassScore() == null
        ? BigDecimal.valueOf(60) : examMapper.selectById(attempt.getExamId()).getPassScore())
        : attempt.getPassScore();
    attempt.setObjectiveScore(objective);
    attempt.setSubjectiveScore(subjectiveExisting);
    attempt.setTotalScore(objective.add(subjectiveExisting));
    attempt.setWrongCount(wrong);
    attempt.setSubmittedAt(LocalDateTime.now());
    attempt.setRemainingSeconds(0);
    attempt.setStatus(hasSubjective ? 2 : 3);
    if (!hasSubjective) {
      attempt.setIsPassed(attempt.getTotalScore().compareTo(passScore) >= 0);
      attempt.setGradedAt(LocalDateTime.now());
    }
    attemptMapper.updateById(attempt);

    ExamAssignment assignment = assignmentMapper.selectOne(Wrappers.<ExamAssignment>lambdaQuery()
        .eq(ExamAssignment::getExamId, attempt.getExamId())
        .eq(ExamAssignment::getUserId, userId)
        .last("LIMIT 1"));
    if (assignment != null) {
      assignment.setAttemptsUsed((assignment.getAttemptsUsed() == null
          ? 0 : assignment.getAttemptsUsed()) + 1);
      assignment.setStatus(2);
      assignmentMapper.updateById(assignment);
    }
    refreshParticipantCount(attempt.getExamId());
    return attemptDetail(attemptId, userId, true);
  }

  public Map<String, Object> myAttempt(Long attemptId) {
    Long userId = com.joker.emp.security.AuthContext.userId();
    ExamAttempt attempt = requireOwnAttempt(attemptId, userId);
    return attemptDetail(attemptId, userId, !Integer.valueOf(1).equals(attempt.getStatus()));
  }

  // ------------------------------------------------------------------ 内部

  private Map<String, Object> attemptPayload(ExamAttempt attempt, Exam exam) {
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("attemptId", attempt.getId());
    item.put("examId", attempt.getExamId());
    item.put("attemptNo", attempt.getAttemptNo());
    item.put("status", attempt.getStatus());
    item.put("startedAt", attempt.getStartedAt());
    item.put("remainingSeconds", attempt.getRemainingSeconds());
    item.put("durationMinutes", exam.getDurationMinutes());
    item.put("totalScore", exam.getTotalScore());
    item.put("passScore", exam.getPassScore());
    item.put("questions", questionService.learnerQuestions(attempt.getExamId()));
    item.put("answers", answerMapper.selectDetail(attempt.getId()).stream()
        .peek(a -> {
          a.remove("correctAnswer");
          a.remove("referenceAnswer");
          a.remove("analysis");
        }).toList());
    return item;
  }

  private ExamAttempt requireOwnAttempt(Long attemptId, Long userId) {
    ExamAttempt attempt = attemptMapper.selectById(attemptId);
    if (attempt == null) {
      throw BizException.notFound("答卷不存在");
    }
    if (!userId.equals(attempt.getUserId())) {
      throw BizException.forbidden("无权操作该答卷");
    }
    return attempt;
  }

  private Exam requireExam(Long id) {
    Exam exam = examMapper.selectById(id);
    if (exam == null) {
      throw BizException.notFound("考试不存在");
    }
    return exam;
  }

  private void validate(ExamSaveRequest request) {
    if (request.title() == null || request.title().isBlank()) {
      throw new BizException("考试标题不能为空");
    }
    if (request.code() == null || request.code().isBlank()) {
      throw new BizException("考试编号不能为空");
    }
    long duplicate = examMapper.selectCount(Wrappers.<Exam>lambdaQuery()
        .eq(Exam::getCode, request.code().trim()));
    if (duplicate > 0) {
      throw new BizException(409, "考试编号已存在");
    }
  }

  private void apply(Exam exam, ExamSaveRequest request) {
    if (request.code() != null) exam.setCode(request.code().trim());
    if (request.title() != null) exam.setTitle(request.title());
    if (request.organizer() != null) exam.setOrganizer(request.organizer());
    if (request.examType() != null) exam.setExamType(request.examType());
    if (request.location() != null) exam.setLocation(request.location());
    if (request.startAt() != null) exam.setStartAt(request.startAt());
    if (request.endAt() != null) exam.setEndAt(request.endAt());
    if (request.isDoubleCamera() != null) exam.setIsDoubleCamera(request.isDoubleCamera());
    if (request.relatedCourseId() != null) exam.setRelatedCourseId(request.relatedCourseId());
    if (request.relatedCourseText() != null) exam.setRelatedCourseText(request.relatedCourseText());
    if (request.remainingChance() != null) exam.setRemainingChanceCached(request.remainingChance());
  }

  private void refreshParticipantCount(Long examId) {
    long count = assignmentMapper.selectCount(Wrappers.<ExamAssignment>lambdaQuery()
        .eq(ExamAssignment::getExamId, examId));
    Exam update = new Exam();
    update.setId(examId);
    update.setParticipantCountCached((int) count);
    update.setQuestionCountCached((int) questionService.countByExam(examId));
    examMapper.updateById(update);
  }

  private Map<String, Object> toExamItem(Exam exam) {
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("id", exam.getId());
    item.put("extId", exam.getExtId());
    item.put("code", exam.getCode());
    item.put("title", exam.getTitle());
    item.put("organizer", exam.getOrganizer());
    item.put("category", exam.getOrganizer());
    item.put("examType", exam.getExamType());
    item.put("statusCode", exam.getStatus());
    item.put("status", statusText(exam.getStatus()));
    item.put("location", exam.getLocation());
    item.put("startAt", exam.getStartAt());
    item.put("endAt", exam.getEndAt());
    item.put("timeWindow", timeWindow(exam.getStartAt(), exam.getEndAt()));
    item.put("durationMinutes", exam.getDurationMinutes());
    item.put("totalScore", exam.getTotalScore());
    item.put("passScore", exam.getPassScore());
    item.put("isDoubleCamera", exam.getIsDoubleCamera());
    item.put("isDoubleCameraProctored", exam.getIsDoubleCamera());
    item.put("relatedCourseId", exam.getRelatedCourseId());
    item.put("relatedCourseText", exam.getRelatedCourseText());
    item.put("remainingChanceCached", exam.getRemainingChanceCached());
    item.put("questionCountCached", exam.getQuestionCountCached());
    item.put("participantCountCached", exam.getParticipantCountCached());
    item.put("createdBy", exam.getCreatedBy());
    item.put("createdAt", exam.getCreatedAt());
    item.put("updatedAt", exam.getUpdatedAt());
    return item;
  }

  private String timeWindow(Object start, Object end) {
    if (start == null && end == null) {
      return null;
    }
    String s = start == null ? "" : start.toString().replace('T', ' ');
    String e = end == null ? "" : end.toString().replace('T', ' ');
    return (s + (e.isBlank() ? "" : " ~ " + e)).trim();
  }

  private String normalizeKeys(String value) {
    if (value == null) {
      return "";
    }
    return value.replaceAll("[^A-Za-z0-9]", "").toUpperCase().chars()
        .sorted()
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
  }

  private String statusText(Object rawStatus) {
    Integer status = Numbers.toInteger(rawStatus);
    if (status == null) {
      return "草稿";
    }
    return switch (status) {
      case 2 -> "未开始";
      case 3 -> "进行中";
      case 4 -> "已结束";
      case 5 -> "未开始";
      default -> "草稿";
    };
  }

  private String assignmentStatusText(Object rawStatus) {
    Integer status = Numbers.toInteger(rawStatus);
    if (status == null) {
      return "";
    }
    return switch (status) {
      case 2 -> "已完成";
      case 3 -> "已逾期";
      default -> "待参加";
    };
  }

  private String attemptStatusText(Object rawStatus) {
    Integer status = Numbers.toInteger(rawStatus);
    if (status == null) {
      return "";
    }
    return switch (status) {
      case 1 -> "作答中";
      case 2 -> "待评分";
      case 3 -> "已评分";
      case 4 -> "系统强制交卷";
      default -> "未知";
    };
  }

  public record ExamSaveRequest(String code, String title, String organizer, String examType,
      Integer status, String location, LocalDateTime startAt, LocalDateTime endAt,
      Integer durationMinutes, BigDecimal totalScore, BigDecimal passScore,
      Boolean isDoubleCamera, Long relatedCourseId, String relatedCourseText,
      Integer remainingChance, List<Long> questionIds) {
  }

  public record AssignRequest(List<Long> userIds, Integer attemptsAllowed,
      LocalDateTime deadlineAt) {
  }

  public record GradeRequest(BigDecimal subjectiveScore, List<AnswerGrade> answers) {
  }

  public record AnswerGrade(Long answerId, BigDecimal scoreGained) {
  }

  public record AnswerSaveRequest(Long questionId, String selectedKeys, String answerText,
      Boolean flagged, Integer remainingSeconds) {
  }
}
