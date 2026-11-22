package com.joker.emp.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.joker.emp.common.ApiResponse;
import com.joker.emp.common.BizException;
import com.joker.emp.common.PageResult;
import com.joker.emp.entity.Course;
import com.joker.emp.entity.CourseChapter;
import com.joker.emp.entity.CourseLesson;
import com.joker.emp.entity.Enrollment;
import com.joker.emp.mapper.CourseChapterMapper;
import com.joker.emp.mapper.CourseMapper;
import com.joker.emp.mapper.CourseLessonMapper;
import com.joker.emp.mapper.EnrollmentMapper;
import com.joker.emp.security.AuthContext;
import com.joker.emp.service.CourseService;
import com.joker.emp.service.StudySessionTrackingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class CourseController {

  private final CourseService courseService;
  private final CourseLessonMapper lessonMapper;
  private final CourseMapper courseMapper;
  private final CourseChapterMapper chapterMapper;
  private final EnrollmentMapper enrollmentMapper;
  private final StudySessionTrackingService studySessionTrackingService;

  public CourseController(CourseService courseService, CourseLessonMapper lessonMapper,
      CourseMapper courseMapper, CourseChapterMapper chapterMapper,
      EnrollmentMapper enrollmentMapper,
      StudySessionTrackingService studySessionTrackingService) {
    this.courseService = courseService;
    this.lessonMapper = lessonMapper;
    this.courseMapper = courseMapper;
    this.chapterMapper = chapterMapper;
    this.enrollmentMapper = enrollmentMapper;
    this.studySessionTrackingService = studySessionTrackingService;
  }

  @GetMapping("/courses")
  public ApiResponse<PageResult<Map<String, Object>>> catalog(
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "12") long size,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String status) {
    return ApiResponse.ok(courseService.catalog(page, size, keyword, category, status));
  }

  @GetMapping("/courses/{id}")
  public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
    return ApiResponse.ok(courseService.catalogDetail(id));
  }

  @PostMapping("/courses/{id}/enroll")
  public ApiResponse<Map<String, Object>> enroll(@PathVariable Long id) {
    return ApiResponse.ok(courseService.enroll(id));
  }

  @PostMapping("/courses/{id}/lessons/{lessonId}/progress")
  public ApiResponse<Map<String, Object>> updateProgress(@PathVariable Long id,
      @PathVariable Long lessonId, @Valid @RequestBody ProgressRequest request) {
    return ApiResponse.ok(courseService.updateLessonProgress(id, lessonId,
        request.watchedSeconds(), request.completed()));
  }

  @PostMapping("/courses/{id}/lessons/{lessonId}/study-sessions")
  public ApiResponse<Map<String, Object>> startStudySession(@PathVariable Long id,
      @PathVariable Long lessonId, @Valid @RequestBody StudySessionRequest request) {
    return ApiResponse.ok(studySessionTrackingService.start(id, lessonId, request.sessionId()));
  }

  @PostMapping("/study-sessions/{sessionId}/heartbeat")
  public ApiResponse<Map<String, Object>> heartbeatStudySession(@PathVariable String sessionId) {
    return ApiResponse.ok(studySessionTrackingService.heartbeat(sessionId));
  }

  @PostMapping("/study-sessions/{sessionId}/stop")
  public ApiResponse<Map<String, Object>> stopStudySession(@PathVariable String sessionId) {
    return ApiResponse.ok(studySessionTrackingService.stop(sessionId));
  }

  @GetMapping("/courses/{id}/lessons/{lessonId}")
  public ApiResponse<Map<String, Object>> player(@PathVariable Long id,
      @PathVariable Long lessonId) {
    CourseLesson lesson = lessonMapper.selectById(lessonId);
    if (lesson == null) {
      throw BizException.notFound("课时不存在");
    }
    CourseChapter chapter = chapterMapper.selectById(lesson.getChapterId());
    Course course = courseMapper.selectById(id);
    if (chapter == null || !id.equals(chapter.getCourseId()) || course == null
        || !Integer.valueOf(2).equals(course.getStatus())) {
      throw BizException.notFound("课时不存在或课程未上架");
    }
    Enrollment enrollment = enrollmentMapper.selectOne(Wrappers.<Enrollment>lambdaQuery()
        .eq(Enrollment::getUserId, AuthContext.userId())
        .eq(Enrollment::getCourseId, id)
        .last("LIMIT 1"));
    if (enrollment == null) {
      throw BizException.notFound("请先报名该课程");
    }
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("id", lesson.getId());
    item.put("courseId", id);
    item.put("chapterId", lesson.getChapterId());
    item.put("title", lesson.getTitle());
    item.put("durationSeconds", lesson.getDurationSeconds());
    item.put("videoUrl", lesson.getVideoUrl());
    return ApiResponse.ok(item);
  }

  @GetMapping("/course-categories")
  public ApiResponse<List<Map<String, Object>>> categories() {
    return ApiResponse.ok(courseService.categories());
  }

  @GetMapping("/instructors")
  public ApiResponse<List<Map<String, Object>>> instructors() {
    return ApiResponse.ok(courseService.instructors());
  }

  public record ProgressRequest(@Min(value = 0, message = "观看时长不能为负") Integer watchedSeconds,
                                Boolean completed) {
  }

  public record StudySessionRequest(@NotBlank(message = "学习会话ID无效") String sessionId) {
  }
}
