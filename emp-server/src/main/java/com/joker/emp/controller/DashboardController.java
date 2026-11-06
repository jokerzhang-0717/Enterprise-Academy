package com.joker.emp.controller;

import com.joker.emp.common.ApiResponse;
import com.joker.emp.service.DashboardService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

  private final DashboardService dashboardService;

  public DashboardController(DashboardService dashboardService) {
    this.dashboardService = dashboardService;
  }

  @GetMapping
  public ApiResponse<Map<String, Object>> learnerDashboard() {
    return ApiResponse.ok(dashboardService.learnerDashboard());
  }
}
