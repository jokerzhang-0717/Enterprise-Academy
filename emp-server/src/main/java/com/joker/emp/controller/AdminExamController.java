package com.joker.emp.controller;

import com.joker.emp.common.ApiResponse;
import com.joker.emp.common.PageResult;
import com.joker.emp.security.AdminOnly;
import com.joker.emp.service.ExamService;
import com.joker.emp.service.ExamService.AnswerGrade;
import com.joker.emp.service.ExamService.AssignRequest;
import com.joker.emp.service.ExamService.ExamSaveRequest;
import com.joker.emp.service.ExamService.GradeRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
public class AdminExamController {

  private final ExamService examService;

  public AdminExamController(ExamService examService) {
    this.examService = examService;
  }

  @GetMapping("/exams")
  public ApiResponse<PageResult<Map<String, Object>>> page(
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "10") long size,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer status) {
    return ApiResponse.ok(examService.page(page, size, keyword, status));
  }

  @GetMapping("/exams/{id}")
  public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
    return ApiResponse.ok(examService.detail(id));
  }

  @PostMapping("/exams")
  public ApiResponse<Map<String, Object>> create(@Valid @RequestBody ExamSaveRequest request) {
    return ApiResponse.ok(examService.create(request));
  }

  @PutMapping("/exams/{id}")
  public ApiResponse<Map<String, Object>> update(@PathVariable Long id,
      @Valid @RequestBody ExamSaveRequest request) {
    return ApiResponse.ok(examService.update(id, request));
  }

  @DeleteMapping("/exams/{id}")
  public ApiResponse<Void> remove(@PathVariable Long id) {
    examService.remove(id);
    return ApiResponse.ok();
  }

  @PutMapping("/exams/{id}/status")
  public ApiResponse<Void> updateStatus(@PathVariable Long id,
      @Valid @RequestBody StatusRequest request) {
    examService.updateStatus(id, request.status());
    return ApiResponse.ok();
  }

  @GetMapping("/exams/{id}/assignments")
  public ApiResponse<PageResult<Map<String, Object>>> assignments(@PathVariable Long id,
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "10") long size,
      @RequestParam(required = false) String keyword) {
    return ApiResponse.ok(examService.assignments(id, page, size, keyword));
  }

  @PostMapping("/exams/{id}/assignments")
  public ApiResponse<Map<String, Object>> assign(@PathVariable Long id,
      @Valid @RequestBody AssignRequest request) {
    int created = examService.assign(id, request);
    return ApiResponse.ok(Map.of("created", created));
  }

  @DeleteMapping("/exams/{id}/assignments/{assignmentId}")
  public ApiResponse<Void> unassign(@PathVariable Long id, @PathVariable Long assignmentId) {
    examService.unassign(id, assignmentId);
    return ApiResponse.ok();
  }

  @GetMapping("/exam-attempts")
  public ApiResponse<PageResult<Map<String, Object>>> attempts(
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "10") long size,
      @RequestParam(required = false) Long examId,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) String keyword) {
    return ApiResponse.ok(examService.attempts(page, size, examId, status, keyword));
  }

  @GetMapping("/exam-attempts/{id}")
  public ApiResponse<Map<String, Object>> attemptDetail(@PathVariable Long id) {
    return ApiResponse.ok(examService.attemptDetail(id, null, true));
  }

  @PostMapping("/exam-attempts/{id}/grade")
  public ApiResponse<Map<String, Object>> grade(@PathVariable Long id,
      @Valid @RequestBody GradeRequest request) {
    return ApiResponse.ok(examService.grade(id, request));
  }

  public record StatusRequest(@NotNull(message = "请选择考试状态") Integer status) {
  }

  public record SingleGrade(Long answerId, java.math.BigDecimal scoreGained) {
  }
}
