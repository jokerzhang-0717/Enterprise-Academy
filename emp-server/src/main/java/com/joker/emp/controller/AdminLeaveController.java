package com.joker.emp.controller;

import com.joker.emp.common.ApiResponse;
import com.joker.emp.common.PageResult;
import com.joker.emp.security.AdminOnly;
import com.joker.emp.service.LeaveService;
import com.joker.emp.service.LeaveService.LeaveSaveRequest;
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
@RequestMapping("/admin/leaves")
public class AdminLeaveController {

  private final LeaveService leaveService;

  public AdminLeaveController(LeaveService leaveService) {
    this.leaveService = leaveService;
  }

  @GetMapping
  public ApiResponse<PageResult<Map<String, Object>>> page(
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "10") long size,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) String keyword) {
    return ApiResponse.ok(leaveService.page(page, size, status, keyword));
  }

  @GetMapping("/stats")
  public ApiResponse<List<Map<String, Object>>> stats() {
    return ApiResponse.ok(leaveService.stats());
  }

  @GetMapping("/{id}")
  public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
    return ApiResponse.ok(leaveService.detail(id));
  }

  @PutMapping("/{id}/approve")
  public ApiResponse<Map<String, Object>> approve(@PathVariable Long id,
      @RequestBody ApproveRequest request) {
    return ApiResponse.ok(leaveService.approve(id, request.status(), request.remark()));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> remove(@PathVariable Long id) {
    leaveService.remove(id);
    return ApiResponse.ok();
  }

  public record ApproveRequest(Integer status, String remark) {
  }
}
