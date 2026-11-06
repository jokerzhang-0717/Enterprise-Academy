package com.joker.emp.controller;

import com.joker.emp.common.ApiResponse;
import com.joker.emp.common.BizException;
import com.joker.emp.common.PageResult;
import com.joker.emp.entity.CourseLesson;
import com.joker.emp.mapper.CourseLessonMapper;
import com.joker.emp.service.CourseService;
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

  public CourseController(CourseService courseService, CourseLessonMapper lessonMapper) {
    this.courseService = courseService;
    this.lessonMapper = lessonMapper;
  }

  @GetMapping("/courses")
  public ApiResponse<PageResult<Map<String, Object>>> catalog(
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "12") long size,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) Boolean required,
      @RequestParam(required = false) String status) {
    return ApiResponse.ok(courseService.catalog(page, size, keyword, category, required, status));
  }

  @GetMapping("/courses/my-learning")
  public ApiResponse<List<Map<String, Object>>> myLearning() {
    return ApiResponse.ok(courseService.myLearning());
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
      @PathVariable Long lessonId, @RequestBody ProgressRequest request) {
    return ApiResponse.ok(courseService.updateLessonProgress(id, lessonId,
        request.watchedSeconds(), request.completed()));
  }

  @GetMapping("/courses/{id}/lessons/{lessonId}")
  public ApiResponse<Map<String, Object>> player(@PathVariable Long id,
      @PathVariable Long lessonId) {
    CourseLesson lesson = lessonMapper.selectById(lessonId);
    if (lesson == null) {
      throw BizException.notFound("课时不存在");
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

  @GetMapping("/course-tags")
  public ApiResponse<List<Map<String, Object>>> tags() {
    return ApiResponse.ok(courseService.tags());
  }

  @GetMapping("/instructors")
  public ApiResponse<List<Map<String, Object>>> instructors() {
    return ApiResponse.ok(courseService.instructors());
  }

  public record ProgressRequest(Integer watchedSeconds, Boolean completed) {
  }
}
