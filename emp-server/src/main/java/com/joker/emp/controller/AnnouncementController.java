package com.joker.emp.controller;

import com.joker.emp.common.ApiResponse;
import com.joker.emp.common.PageResult;
import com.joker.emp.service.AnnouncementService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/announcements")
public class AnnouncementController {

  private final AnnouncementService announcementService;

  public AnnouncementController(AnnouncementService announcementService) {
    this.announcementService = announcementService;
  }

  @GetMapping
  public ApiResponse<PageResult<Map<String, Object>>> page(
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "10") long size,
      @RequestParam(required = false) String keyword) {
    return ApiResponse.ok(announcementService.publishedPage(page, size, keyword));
  }

  @GetMapping("/unread-count")
  public ApiResponse<Map<String, Object>> unreadCount() {
    return ApiResponse.ok(Map.of("count", announcementService.unreadCount()));
  }

  @PutMapping("/read-all")
  public ApiResponse<Map<String, Object>> markAllRead() {
    return ApiResponse.ok(Map.of("updated", announcementService.markAllRead()));
  }

  @GetMapping("/{id}")
  public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
    return ApiResponse.ok(announcementService.detail(id, true));
  }
}
