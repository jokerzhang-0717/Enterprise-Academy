package com.joker.emp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.joker.emp.entity.Announcement;
import com.joker.emp.entity.CheckinRecord;
import com.joker.emp.entity.Course;
import com.joker.emp.entity.Department;
import com.joker.emp.entity.Enrollment;
import com.joker.emp.entity.Exam;
import com.joker.emp.entity.OperationLog;
import com.joker.emp.entity.SysUser;
import com.joker.emp.mapper.AnnouncementMapper;
import com.joker.emp.mapper.CheckinRecordMapper;
import com.joker.emp.mapper.CourseMapper;
import com.joker.emp.mapper.DepartmentMapper;
import com.joker.emp.mapper.EnrollmentMapper;
import com.joker.emp.mapper.ExamMapper;
import com.joker.emp.mapper.LearningTrackMapper;
import com.joker.emp.mapper.NotificationMapper;
import com.joker.emp.mapper.OperationLogMapper;
import com.joker.emp.mapper.SysUserMapper;
import com.joker.emp.security.AuthContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

  private final SysUserMapper userMapper;
  private final DepartmentMapper departmentMapper;
  private final CourseMapper courseMapper;
  private final EnrollmentMapper enrollmentMapper;
  private final ExamMapper examMapper;
  private final AnnouncementMapper announcementMapper;
  private final NotificationMapper notificationMapper;
  private final CheckinRecordMapper checkinMapper;
  private final OperationLogMapper operationLogMapper;
  private final LearningTrackMapper trackMapper;
  private final AuthService authService;

  public DashboardService(SysUserMapper userMapper, DepartmentMapper departmentMapper,
      CourseMapper courseMapper, EnrollmentMapper enrollmentMapper, ExamMapper examMapper,
      AnnouncementMapper announcementMapper, NotificationMapper notificationMapper,
      CheckinRecordMapper checkinMapper, OperationLogMapper operationLogMapper,
      LearningTrackMapper trackMapper, AuthService authService) {
    this.userMapper = userMapper;
    this.departmentMapper = departmentMapper;
    this.courseMapper = courseMapper;
    this.enrollmentMapper = enrollmentMapper;
    this.examMapper = examMapper;
    this.announcementMapper = announcementMapper;
    this.notificationMapper = notificationMapper;
    this.checkinMapper = checkinMapper;
    this.operationLogMapper = operationLogMapper;
    this.trackMapper = trackMapper;
    this.authService = authService;
  }

  public Map<String, Object> learnerDashboard() {
    Long userId = AuthContext.userId();
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("user", authService.me());

    Map<String, Object> profile = userMapper.selectLearnerProfile(userId);
    Map<String, Object> stats = new LinkedHashMap<>();
    stats.put("learningHours", profile == null ? 0 : profile.get("learningHours"));
    stats.put("completedCourses", profile == null ? 0 : profile.get("completedCourses"));
    stats.put("studyingCourses", profile == null ? 0 : profile.get("studyingCourses"));
    stats.put("enrolledCourses", profile == null ? 0 : profile.get("enrolledCourses"));
    stats.put("certificateCount", profile == null ? 0 : profile.get("certificateCount"));
    stats.put("unreadNotifications", profile == null ? 0 : profile.get("unreadNotifications"));
    result.put("stats", stats);

    result.put("monthlyStudyHours", userMapper.selectMonthlyStudyHours(userId));
    result.put("courses", enrollmentMapper.selectProgress(userId));
    result.put("upcomingExams", examMapper.selectAssignments(userId));
    result.put("examMetrics", examMapper.selectMetrics(userId));
    result.put("recentNotifications", notificationMapper.selectRecent(userId, 5));
    result.put("announcements", announcementMapper.selectList(
            Wrappers.<Announcement>lambdaQuery()
                .eq(Announcement::getStatus, 2)
                .orderByDesc(Announcement::getIsTop)
                .orderByDesc(Announcement::getPublishAt)
                .last("LIMIT 5")).stream()
        .map(a -> {
          Map<String, Object> item = new LinkedHashMap<>();
          item.put("id", a.getId());
          item.put("title", a.getTitle());
          item.put("publishAt", a.getPublishAt());
          item.put("isTop", a.getIsTop());
          item.put("authorName", a.getAuthorName());
          return item;
        }).toList());
    result.put("todayCheckin", todayCheckin(userId));
    result.put("weekly", weeklyStudy(userId));
    return result;
  }

  public Map<String, Object> adminDashboard() {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("pending", userMapper.selectAdminPending());

    Map<String, Object> stats = new LinkedHashMap<>();
    stats.put("learnerCount", userMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
        .eq(SysUser::getRole, 1)));
    stats.put("departmentCount", departmentMapper.selectCount(null));
    stats.put("courseCount", courseMapper.selectCount(null));
    stats.put("publishedCourseCount", courseMapper.selectCount(Wrappers.<Course>lambdaQuery()
        .eq(Course::getStatus, 2)));
    stats.put("examCount", examMapper.selectCount(null));
    stats.put("trackCount", trackMapper.selectCount(null));
    stats.put("announcementCount", announcementMapper.selectCount(null));
    result.put("stats", stats);

    result.put("departments", departmentMapper.selectStats());
    result.put("recentActivities", operationLogMapper.selectPage(new Page<>(1, 10),
            Wrappers.<OperationLog>lambdaQuery()
                .orderByDesc(OperationLog::getOccurredAt)
                .orderByDesc(OperationLog::getId))
        .getRecords());
    result.put("exams", examMapper.selectOverviewPage(new Page<>(1, 6), null, null)
        .getRecords());
    result.put("enrollmentStats", enrollmentStats());
    return result;
  }

  public List<Map<String, Object>> recentActivities(long page, long size) {
    IPage<OperationLog> result = operationLogMapper.selectPage(new Page<>(page, size),
        Wrappers.<OperationLog>lambdaQuery()
            .orderByDesc(OperationLog::getOccurredAt)
            .orderByDesc(OperationLog::getId));
    return result.getRecords().stream().map(log -> {
      Map<String, Object> item = new LinkedHashMap<>();
      item.put("id", log.getId());
      item.put("logType", log.getLogType());
      item.put("content", log.getContent());
      item.put("operatorId", log.getOperatorId());
      item.put("operatorName", log.getOperatorName());
      item.put("refType", log.getRefType());
      item.put("refId", log.getRefId());
      item.put("occurredAt", log.getOccurredAt());
      return item;
    }).toList();
  }

  private Map<String, Object> todayCheckin(Long userId) {
    CheckinRecord record = checkinMapper.selectOne(Wrappers.<CheckinRecord>lambdaQuery()
        .eq(CheckinRecord::getUserId, userId)
        .eq(CheckinRecord::getCheckinDate, LocalDate.now())
        .last("LIMIT 1"));
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("checkedIn", record != null && record.getCheckInTime() != null);
    item.put("checkInTime", record == null ? null : record.getCheckInTime());
    item.put("checkOutTime", record == null ? null : record.getCheckOutTime());
    item.put("status", record == null ? null : record.getStatus());
    return item;
  }

  private Map<String, Object> weeklyStudy(Long userId) {
    LocalDate monday = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
    LocalDateTime start = monday.atStartOfDay();
    List<Map<String, Object>> months = userMapper.selectMonthlyStudyHours(userId);
    BigDecimal monthHours = BigDecimal.ZERO;
    String currentYm = LocalDate.now().toString().substring(0, 7);
    for (Map<String, Object> row : months) {
      if (currentYm.equals(String.valueOf(row.get("ym")))) {
        Object value = row.get("studyHours");
        if (value instanceof Number number) {
          monthHours = BigDecimal.valueOf(number.doubleValue());
        }
      }
    }
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("weekStart", monday);
    item.put("weekEnd", monday.plusDays(6));
    item.put("targetHours", 10);
    item.put("monthHours", monthHours);
    item.put("monthStart", start.toLocalDate());
    item.put("today", LocalDate.now());
    item.put("now", LocalTime.now());
    return item;
  }

  private Map<String, Object> enrollmentStats() {
    Map<String, Object> stats = new LinkedHashMap<>();
    stats.put("notStarted", enrollmentMapper.selectCount(Wrappers.<Enrollment>lambdaQuery()
        .eq(Enrollment::getStatus, 1)));
    stats.put("studying", enrollmentMapper.selectCount(Wrappers.<Enrollment>lambdaQuery()
        .eq(Enrollment::getStatus, 2)));
    stats.put("completed", enrollmentMapper.selectCount(Wrappers.<Enrollment>lambdaQuery()
        .eq(Enrollment::getStatus, 3)));
    BigDecimal totalHours = enrollmentMapper.selectList(null).stream()
        .map(Enrollment::getStudyHours)
        .filter(java.util.Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    stats.put("totalStudyHours", totalHours);
    return stats;
  }
}
