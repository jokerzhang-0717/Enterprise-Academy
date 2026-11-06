package com.joker.emp.controller;

import com.joker.emp.common.ApiResponse;
import com.joker.emp.common.PageResult;
import com.joker.emp.entity.Department;
import com.joker.emp.security.AdminOnly;
import com.joker.emp.service.DepartmentService;
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
@RequestMapping("/admin/departments")
public class AdminDepartmentController {

  private final DepartmentService departmentService;

  public AdminDepartmentController(DepartmentService departmentService) {
    this.departmentService = departmentService;
  }

  @GetMapping
  public ApiResponse<PageResult<Map<String, Object>>> page(
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "10") long size,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer status) {
    return ApiResponse.ok(departmentService.page(page, size, keyword, status));
  }

  @GetMapping("/options")
  public ApiResponse<List<Map<String, Object>>> options() {
    return ApiResponse.ok(departmentService.options());
  }

  @GetMapping("/{id}")
  public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
    return ApiResponse.ok(departmentService.detail(id));
  }

  @PostMapping
  public ApiResponse<Map<String, Object>> create(@RequestBody Department request) {
    return ApiResponse.ok(departmentService.create(request));
  }

  @PutMapping("/{id}")
  public ApiResponse<Map<String, Object>> update(@PathVariable Long id,
      @RequestBody Department request) {
    return ApiResponse.ok(departmentService.update(id, request));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> remove(@PathVariable Long id) {
    departmentService.remove(id);
    return ApiResponse.ok();
  }

  @PutMapping("/{id}/status")
  public ApiResponse<Void> updateStatus(@PathVariable Long id,
      @RequestBody StatusRequest request) {
    departmentService.updateStatus(id, request.status());
    return ApiResponse.ok();
  }

  public record StatusRequest(Integer status) {
  }
}
