package com.joker.emp.controller;

import com.joker.emp.common.ApiResponse;
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

@RestController
@RequestMapping("/leaves")
public class LeaveController {

  private final LeaveService leaveService;

  public LeaveController(LeaveService leaveService) {
    this.leaveService = leaveService;
  }

  @GetMapping
  public ApiResponse<List<Map<String, Object>>> myList(
      @RequestParam(required = false) Integer status) {
    return ApiResponse.ok(leaveService.myList(status));
  }

  @GetMapping("/{id}")
  public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
    return ApiResponse.ok(leaveService.detail(id));
  }

  @PostMapping
  public ApiResponse<Map<String, Object>> submit(@RequestBody LeaveSaveRequest request) {
    return ApiResponse.ok(leaveService.submit(request));
  }

  @PutMapping("/{id}")
  public ApiResponse<Map<String, Object>> update(@PathVariable Long id,
      @RequestBody LeaveSaveRequest request) {
    return ApiResponse.ok(leaveService.update(id, request));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> cancel(@PathVariable Long id) {
    leaveService.cancel(id);
    return ApiResponse.ok();
  }
}
