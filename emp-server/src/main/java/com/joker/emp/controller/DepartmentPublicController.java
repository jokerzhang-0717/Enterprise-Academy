package com.joker.emp.controller;

import com.joker.emp.common.ApiResponse;
import com.joker.emp.service.DepartmentService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 注册页等公开场景使用的部门下拉数据（无需登录）。
 */
@RestController
@RequestMapping("/departments")
public class DepartmentPublicController {

  private final DepartmentService departmentService;

  public DepartmentPublicController(DepartmentService departmentService) {
    this.departmentService = departmentService;
  }

  @GetMapping("/options")
  public ApiResponse<List<Map<String, Object>>> options() {
    return ApiResponse.ok(departmentService.options());
  }
}
