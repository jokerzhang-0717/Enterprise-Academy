package com.joker.emp.controller;

import com.joker.emp.common.ApiResponse;
import com.joker.emp.security.AdminOnly;
import com.joker.emp.service.DashboardService;
import com.joker.emp.service.OperationLogService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AdminOnly
@RestController
@RequestMapping("/admin")
public class AdminDashboardController {

  private final DashboardService dashboardService;
  private final OperationLogService operationLogService;

  public AdminDashboardController(DashboardService dashboardService,
      OperationLogService operationLogService) {
    this.dashboardService = dashboardService;
    this.operationLogService = operationLogService;
  }

  @GetMapping("/dashboard")
  public ApiResponse<Map<String, Object>> dashboard() {
    return ApiResponse.ok(dashboardService.adminDashboard());
  }

  @GetMapping("/activities")
  public ApiResponse<List<Map<String, Object>>> activities(
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "10") long size) {
    return ApiResponse.ok(dashboardService.recentActivities(page, size));
  }

  @GetMapping("/logs")
  public ApiResponse<Map<String, Object>> logs(
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "10") long size,
      @RequestParam(required = false) String logType) {
    var result = operationLogService.page(page, size, logType);
    return ApiResponse.ok(Map.of(
        "total", result.getTotal(),
        "page", result.getCurrent(),
        "size", result.getSize(),
        "records", result.getRecords()));
  }
}
