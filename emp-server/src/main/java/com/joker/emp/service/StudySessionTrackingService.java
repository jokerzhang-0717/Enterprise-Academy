package com.joker.emp.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.joker.emp.common.BizException;
import com.joker.emp.entity.CourseChapter;
import com.joker.emp.entity.CourseLesson;
import com.joker.emp.entity.Enrollment;
import com.joker.emp.entity.StudySession;
import com.joker.emp.mapper.CourseChapterMapper;
import com.joker.emp.mapper.CourseLessonMapper;
import com.joker.emp.mapper.EnrollmentMapper;
import com.joker.emp.mapper.StudySessionMapper;
import com.joker.emp.security.AuthContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudySessionTrackingService {

  private static final Duration SESSION_TTL = Duration.ofMinutes(10);
  private static final long MAX_HEARTBEAT_SECONDS = 30;

  private final StringRedisTemplate redis;
  private final CourseLessonMapper lessonMapper;
  private final CourseChapterMapper chapterMapper;
  private final EnrollmentMapper enrollmentMapper;
  private final StudySessionMapper studySessionMapper;

  public StudySessionTrackingService(StringRedisTemplate redis, CourseLessonMapper lessonMapper,
      CourseChapterMapper chapterMapper, EnrollmentMapper enrollmentMapper,
      StudySessionMapper studySessionMapper) {
    this.redis = redis;
    this.lessonMapper = lessonMapper;
    this.chapterMapper = chapterMapper;
    this.enrollmentMapper = enrollmentMapper;
    this.studySessionMapper = studySessionMapper;
  }

  @Transactional
  public Map<String, Object> start(Long courseId, Long lessonId, String sessionId) {
    validateSessionId(sessionId);
    Long userId = AuthContext.userId();
    validateLesson(courseId, lessonId);
    ensureEnrollment(userId, courseId);

    String key = key(userId, sessionId);
    if (Boolean.TRUE.equals(redis.hasKey(key))) {
      throw new BizException("学习会话已存在");
    }

    LocalDateTime now = LocalDateTime.now();
    StudySession session = new StudySession();
    session.setUserId(userId);
    session.setCourseId(courseId);
    session.setLessonId(lessonId);
    session.setStartedAt(now);
    session.setDurationMinutes(BigDecimal.ZERO);
    session.setSource(1);
    studySessionMapper.insert(session);

    long nowMillis = System.currentTimeMillis();
    redis.opsForHash().putAll(key, Map.of(
        "studySessionId", String.valueOf(session.getId()),
        "courseId", String.valueOf(courseId),
        "lessonId", String.valueOf(lessonId),
        "lastHeartbeat", String.valueOf(nowMillis),
        "elapsedSeconds", "0"));
    redis.expire(key, SESSION_TTL);
    return Map.of("sessionId", sessionId, "startedAt", now.toString());
  }

  @Transactional
  public Map<String, Object> heartbeat(String sessionId) {
    Long userId = AuthContext.userId();
    String key = key(userId, sessionId);
    Map<Object, Object> state = redis.opsForHash().entries(key);
    if (state.isEmpty()) {
      throw new BizException("学习会话已过期，请重新开始学习");
    }

    long nowMillis = System.currentTimeMillis();
    long lastHeartbeat = value(state, "lastHeartbeat");
    long elapsed = value(state, "elapsedSeconds");
    long delta = Math.min(Math.max(0, (nowMillis - lastHeartbeat) / 1000),
        MAX_HEARTBEAT_SECONDS);
    long totalSeconds = elapsed + delta;
    redis.opsForHash().putAll(key, Map.of(
        "lastHeartbeat", String.valueOf(nowMillis),
        "elapsedSeconds", String.valueOf(totalSeconds)));
    redis.expire(key, SESSION_TTL);

    StudySession session = requireSession(state);
    persistDuration(session, totalSeconds);
    return Map.of("elapsedSeconds", totalSeconds);
  }

  @Transactional
  public Map<String, Object> stop(String sessionId) {
    Long userId = AuthContext.userId();
    String key = key(userId, sessionId);
    Map<Object, Object> state = redis.opsForHash().entries(key);
    if (state.isEmpty()) {
      return Map.of("elapsedSeconds", 0);
    }

    long nowMillis = System.currentTimeMillis();
    long lastHeartbeat = value(state, "lastHeartbeat");
    long elapsed = value(state, "elapsedSeconds");
    long delta = Math.min(Math.max(0, (nowMillis - lastHeartbeat) / 1000),
        MAX_HEARTBEAT_SECONDS);
    long totalSeconds = elapsed + delta;

    StudySession session = requireSession(state);
    persistDuration(session, totalSeconds);
    redis.delete(key);
    return Map.of("elapsedSeconds", totalSeconds);
  }

  private void validateLesson(Long courseId, Long lessonId) {
    CourseLesson lesson = lessonMapper.selectById(lessonId);
    if (lesson == null) {
      throw BizException.notFound("课时不存在");
    }
    CourseChapter chapter = chapterMapper.selectById(lesson.getChapterId());
    if (chapter == null || !courseId.equals(chapter.getCourseId())) {
      throw new BizException("课时不属于该课程");
    }
  }

  private void ensureEnrollment(Long userId, Long courseId) {
    Enrollment enrollment = enrollmentMapper.selectOne(Wrappers.<Enrollment>lambdaQuery()
        .eq(Enrollment::getUserId, userId)
        .eq(Enrollment::getCourseId, courseId)
        .last("LIMIT 1"));
    if (enrollment != null) {
      return;
    }

    Enrollment created = new Enrollment();
    created.setUserId(userId);
    created.setCourseId(courseId);
    created.setStatus(2);
    created.setProgressPercent(0);
    created.setCompletedLessonCount(0);
    created.setStudyHours(BigDecimal.ZERO);
    created.setStartedAt(LocalDateTime.now());
    enrollmentMapper.insert(created);
  }

  private void persistDuration(StudySession session, long elapsedSeconds) {
    BigDecimal minutes = BigDecimal.valueOf(elapsedSeconds)
        .divide(BigDecimal.valueOf(60), 1, RoundingMode.HALF_UP);
    session.setDurationMinutes(minutes);
    studySessionMapper.updateById(session);

    Enrollment enrollment = enrollmentMapper.selectOne(Wrappers.<Enrollment>lambdaQuery()
        .eq(Enrollment::getUserId, session.getUserId())
        .eq(Enrollment::getCourseId, session.getCourseId())
        .last("LIMIT 1"));
    if (enrollment != null) {
      BigDecimal totalMinutes = studySessionMapper.selectStudyMinutes(
          session.getUserId(), session.getCourseId());
      enrollment.setStudyHours(totalMinutes.divide(BigDecimal.valueOf(60), 2,
          RoundingMode.HALF_UP));
      enrollment.setLastStudiedAt(LocalDateTime.now());
      enrollmentMapper.updateById(enrollment);
    }
  }

  private StudySession requireSession(Map<Object, Object> state) {
    Long id = value(state, "studySessionId");
    StudySession session = studySessionMapper.selectById(id);
    if (session == null) {
      throw new BizException("学习记录不存在，请重新开始学习");
    }
    return session;
  }

  private long value(Map<Object, Object> state, String field) {
    Object raw = state.get(field);
    if (raw == null) {
      throw new BizException("学习会话数据无效，请重新开始学习");
    }
    return Long.parseLong(String.valueOf(raw));
  }

  private String key(Long userId, String sessionId) {
    validateSessionId(sessionId);
    return "study:session:" + userId + ":" + sessionId;
  }

  private void validateSessionId(String sessionId) {
    if (sessionId == null || sessionId.isBlank()) {
      throw new BizException("学习会话ID无效");
    }
    try {
      UUID.fromString(sessionId);
    } catch (IllegalArgumentException exception) {
      throw new BizException("学习会话ID无效");
    }
  }
}
