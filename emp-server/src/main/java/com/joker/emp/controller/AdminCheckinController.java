package com.joker.emp.controller;

import com.joker.emp.common.ApiResponse;
import com.joker.emp.common.PageResult;
import com.joker.emp.security.AdminOnly;
import com.joker.emp.service.CheckinService;
import com.joker.emp.service.CheckinService.CheckinSaveRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/admin/checkins")
public class AdminCheckinController {

  private final CheckinService checkinService;

  public AdminCheckinController(CheckinService checkinService) {
    this.checkinService = checkinService;
  }

  @GetMapping
  public ApiResponse<PageResult<Map<String, Object>>> page(
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "10") long size,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) String keyword) {
    return ApiResponse.ok(checkinService.page(page, size, date, status, keyword));
  }

  @GetMapping("/stats")
  public ApiResponse<List<Map<String, Object>>> stats(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    return ApiResponse.ok(checkinService.stats(from, to));
  }

  @PostMapping
  public ApiResponse<Map<String, Object>> create(@RequestBody CheckinSaveRequest request) {
    return ApiResponse.ok(checkinService.create(request));
  }

  @PutMapping("/{id}")
  public ApiResponse<Map<String, Object>> update(@PathVariable Long id,
      @RequestBody CheckinSaveRequest request) {
    return ApiResponse.ok(checkinService.update(id, request));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> remove(@PathVariable Long id) {
    checkinService.remove(id);
    return ApiResponse.ok();
  }
}
