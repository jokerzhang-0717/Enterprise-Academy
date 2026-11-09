package com.joker.emp.controller;

import com.joker.emp.common.ApiResponse;
import com.joker.emp.common.PageResult;
import com.joker.emp.entity.Instructor;
import com.joker.emp.security.AdminOnly;
import com.joker.emp.service.CourseService;
import com.joker.emp.service.CourseService.ChapterSaveRequest;
import com.joker.emp.service.CourseService.CourseSaveRequest;
import com.joker.emp.service.CourseService.LessonSaveRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AdminOnly
@RestController
@RequestMapping("/admin")
public class AdminCourseController {

  private final CourseService courseService;

  public AdminCourseController(CourseService courseService) {
    this.courseService = courseService;
  }

  @GetMapping("/courses")
  public ApiResponse<PageResult<Map<String, Object>>> page(
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "10") long size,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) Boolean required) {
    return ApiResponse.ok(courseService.page(page, size, keyword, categoryId, status, required));
  }

  @GetMapping("/courses/options")
  public ApiResponse<List<Map<String, Object>>> options() {
    return ApiResponse.ok(courseService.page(1, 200, null, null, 2, null).records().stream()
        .map(c -> Map.<String, Object>of(
            "id", c.get("id"),
            "title", c.get("title"),
            "categoryLabel", c.get("categoryLabel") == null ? "" : c.get("categoryLabel"),
            "hours", c.get("hours") == null ? 0 : c.get("hours")))
        .toList());
  }

  @GetMapping("/courses/{id}")
  public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
    return ApiResponse.ok(courseService.detail(id, null));
  }

  @PostMapping("/courses")
  public ApiResponse<Map<String, Object>> create(@Valid @RequestBody CourseSaveRequest request) {
    return ApiResponse.ok(courseService.create(request));
  }

  @PutMapping("/courses/{id}")
  public ApiResponse<Map<String, Object>> update(@PathVariable Long id,
      @Valid @RequestBody CourseSaveRequest request) {
    return ApiResponse.ok(courseService.update(id, request));
  }

  @DeleteMapping("/courses/{id}")
  public ApiResponse<Void> remove(@PathVariable Long id) {
    courseService.remove(id);
    return ApiResponse.ok();
  }

  @PutMapping("/courses/{id}/status")
  public ApiResponse<Void> updateStatus(@PathVariable Long id,
      @Valid @RequestBody StatusRequest request) {
    courseService.updateStatus(id, request.status());
    return ApiResponse.ok();
  }

  @PostMapping("/courses/{id}/chapters")
  public ApiResponse<Map<String, Object>> createChapter(@PathVariable Long id,
      @Valid @RequestBody ChapterSaveRequest request) {
    return ApiResponse.ok(courseService.saveChapter(id, null, request));
  }

  @PutMapping("/courses/{id}/chapters/{chapterId}")
  public ApiResponse<Map<String, Object>> updateChapter(@PathVariable Long id,
      @PathVariable Long chapterId, @Valid @RequestBody ChapterSaveRequest request) {
    return ApiResponse.ok(courseService.saveChapter(id, chapterId, request));
  }

  @DeleteMapping("/courses/{id}/chapters/{chapterId}")
  public ApiResponse<Void> removeChapter(@PathVariable Long id, @PathVariable Long chapterId) {
    courseService.removeChapter(id, chapterId);
    return ApiResponse.ok();
  }

  @PostMapping("/courses/{id}/chapters/{chapterId}/lessons")
  public ApiResponse<Map<String, Object>> createLesson(@PathVariable Long id,
      @PathVariable Long chapterId, @Valid @RequestBody LessonSaveRequest request) {
    return ApiResponse.ok(courseService.saveLesson(id, chapterId, null, request));
  }

  @PutMapping("/courses/{id}/chapters/{chapterId}/lessons/{lessonId}")
  public ApiResponse<Map<String, Object>> updateLesson(@PathVariable Long id,
      @PathVariable Long chapterId, @PathVariable Long lessonId,
      @Valid @RequestBody LessonSaveRequest request) {
    return ApiResponse.ok(courseService.saveLesson(id, chapterId, lessonId, request));
  }

  @DeleteMapping("/courses/{id}/chapters/{chapterId}/lessons/{lessonId}")
  public ApiResponse<Void> removeLesson(@PathVariable Long id, @PathVariable Long chapterId,
      @PathVariable Long lessonId) {
    courseService.removeLesson(id, chapterId, lessonId);
    return ApiResponse.ok();
  }

  @GetMapping("/course-categories")
  public ApiResponse<List<Map<String, Object>>> categories() {
    return ApiResponse.ok(courseService.categories());
  }

  @GetMapping("/course-tags")
  public ApiResponse<List<Map<String, Object>>> tags() {
    return ApiResponse.ok(courseService.tags());
  }

  @PostMapping("/course-tags")
  public ApiResponse<Map<String, Object>> createTag(@Valid @RequestBody NameRequest request) {
    return ApiResponse.ok(courseService.createTag(request.name()));
  }

  @GetMapping("/instructors")
  public ApiResponse<List<Map<String, Object>>> instructors() {
    return ApiResponse.ok(courseService.instructors());
  }

  @PostMapping("/instructors")
  public ApiResponse<Map<String, Object>> createInstructor(@Valid @RequestBody Instructor request) {
    return ApiResponse.ok(courseService.createInstructor(request));
  }

  @PutMapping("/instructors/{id}")
  public ApiResponse<Map<String, Object>> updateInstructor(@PathVariable Long id,
      @Valid @RequestBody Instructor request) {
    return ApiResponse.ok(courseService.updateInstructor(id, request));
  }

  @DeleteMapping("/instructors/{id}")
  public ApiResponse<Void> removeInstructor(@PathVariable Long id) {
    courseService.removeInstructor(id);
    return ApiResponse.ok();
  }

  public record StatusRequest(@NotNull(message = "请选择课程状态") Integer status) {
  }

  public record NameRequest(@NotBlank(message = "请输入标签名") String name) {
  }
}
