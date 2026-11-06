package com.joker.emp.controller;

import com.joker.emp.common.ApiResponse;
import com.joker.emp.common.PageResult;
import com.joker.emp.security.AdminOnly;
import com.joker.emp.service.AnnouncementService;
import com.joker.emp.service.AnnouncementService.AnnouncementSaveRequest;
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
@RequestMapping("/admin/announcements")
public class AdminAnnouncementController {

  private final AnnouncementService announcementService;

  public AdminAnnouncementController(AnnouncementService announcementService) {
    this.announcementService = announcementService;
  }

  @GetMapping
  public ApiResponse<PageResult<Map<String, Object>>> page(
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "10") long size,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer status) {
    return ApiResponse.ok(announcementService.page(page, size, keyword, status));
  }

  @GetMapping("/{id}")
  public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
    return ApiResponse.ok(announcementService.detail(id, false));
  }

  @PostMapping
  public ApiResponse<Map<String, Object>> create(@RequestBody AnnouncementSaveRequest request) {
    return ApiResponse.ok(announcementService.create(request));
  }

  @PutMapping("/{id}")
  public ApiResponse<Map<String, Object>> update(@PathVariable Long id,
      @RequestBody AnnouncementSaveRequest request) {
    return ApiResponse.ok(announcementService.update(id, request));
  }

  @PutMapping("/{id}/status")
  public ApiResponse<Map<String, Object>> updateStatus(@PathVariable Long id,
      @RequestBody StatusRequest request) {
    return ApiResponse.ok(announcementService.updateStatus(id, request.status()));
  }

  @PutMapping("/{id}/top")
  public ApiResponse<Map<String, Object>> toggleTop(@PathVariable Long id,
      @RequestBody TopRequest request) {
    return ApiResponse.ok(announcementService.toggleTop(id, request.isTop()));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> remove(@PathVariable Long id) {
    announcementService.remove(id);
    return ApiResponse.ok();
  }

  public record StatusRequest(Integer status) {
  }

  public record TopRequest(Boolean isTop) {
  }
}
