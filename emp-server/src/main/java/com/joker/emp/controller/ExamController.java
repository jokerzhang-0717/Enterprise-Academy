package com.joker.emp.controller;

import com.joker.emp.common.ApiResponse;
import com.joker.emp.service.ExamService;
import com.joker.emp.service.ExamService.AnswerSaveRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exams")
public class ExamController {

  private final ExamService examService;

  public ExamController(ExamService examService) {
    this.examService = examService;
  }

  @GetMapping
  public ApiResponse<Map<String, Object>> myExams() {
    return ApiResponse.ok(examService.myExams());
  }

  @GetMapping("/metrics")
  public ApiResponse<Map<String, Object>> metrics() {
    return ApiResponse.ok(examService.metrics(com.joker.emp.security.AuthContext.userId()));
  }

  @GetMapping("/past")
  public ApiResponse<java.util.List<Map<String, Object>>> past() {
    return ApiResponse.ok(examService.pastAttempts(
        com.joker.emp.security.AuthContext.userId()));
  }

  @GetMapping("/{id}/paper")
  public ApiResponse<Map<String, Object>> paper(@PathVariable Long id) {
    return ApiResponse.ok(examService.paper(id));
  }

  @PostMapping("/{id}/start")
  public ApiResponse<Map<String, Object>> start(@PathVariable Long id) {
    return ApiResponse.ok(examService.start(id));
  }

  @GetMapping("/attempts/{attemptId}")
  public ApiResponse<Map<String, Object>> attempt(@PathVariable Long attemptId) {
    return ApiResponse.ok(examService.myAttempt(attemptId));
  }

  @PostMapping("/attempts/{attemptId}/answers")
  public ApiResponse<Map<String, Object>> answer(@PathVariable Long attemptId,
      @RequestBody AnswerSaveRequest request) {
    return ApiResponse.ok(examService.saveAnswer(attemptId, request));
  }

  @PostMapping("/attempts/{attemptId}/switch")
  public ApiResponse<Map<String, Object>> switchScreen(@PathVariable Long attemptId) {
    return ApiResponse.ok(examService.switchScreen(attemptId));
  }

  @PostMapping("/attempts/{attemptId}/submit")
  public ApiResponse<Map<String, Object>> submit(@PathVariable Long attemptId) {
    return ApiResponse.ok(examService.submit(attemptId));
  }
}
