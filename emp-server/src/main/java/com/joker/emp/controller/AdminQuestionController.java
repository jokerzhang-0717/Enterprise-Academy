package com.joker.emp.controller;

import com.joker.emp.common.ApiResponse;
import com.joker.emp.security.AdminOnly;
import com.joker.emp.service.QuestionService;
import com.joker.emp.service.QuestionService.QuestionSaveRequest;
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
@RequestMapping("/admin/questions")
public class AdminQuestionController {

  private final QuestionService questionService;

  public AdminQuestionController(QuestionService questionService) {
    this.questionService = questionService;
  }

  @GetMapping
  public ApiResponse<List<Map<String, Object>>> list(
      @RequestParam(required = false) Long examId,
      @RequestParam(required = false) Integer type,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String knowledgePoint,
      @RequestParam(required = false) Integer difficulty,
      @RequestParam(defaultValue = "false") boolean standalone) {
    return ApiResponse.ok(
        questionService.list(examId, type, keyword, standalone, knowledgePoint, difficulty));
  }

  @GetMapping("/{id}")
  public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
    return ApiResponse.ok(questionService.detail(id));
  }

  @PostMapping
  public ApiResponse<Map<String, Object>> create(@RequestBody QuestionSaveRequest request) {
    return ApiResponse.ok(questionService.create(request));
  }

  @PutMapping("/{id}")
  public ApiResponse<Map<String, Object>> update(@PathVariable Long id,
      @RequestBody QuestionSaveRequest request) {
    return ApiResponse.ok(questionService.update(id, request));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> remove(@PathVariable Long id) {
    questionService.remove(id);
    return ApiResponse.ok();
  }
}
