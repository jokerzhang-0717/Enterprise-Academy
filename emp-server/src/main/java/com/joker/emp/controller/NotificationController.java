package com.joker.emp.controller;

import com.joker.emp.common.ApiResponse;
import com.joker.emp.common.PageResult;
import com.joker.emp.security.AdminOnly;
import com.joker.emp.service.NotificationService;
import com.joker.emp.service.NotificationService.SendRequest;
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
@RequestMapping("/notifications")
public class NotificationController {

  private final NotificationService notificationService;

  public NotificationController(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @GetMapping
  public ApiResponse<PageResult<Map<String, Object>>> page(
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "10") long size,
      @RequestParam(required = false) Integer type,
      @RequestParam(required = false) Boolean isRead) {
    return ApiResponse.ok(notificationService.page(page, size, type, isRead));
  }

  @GetMapping("/recent")
  public ApiResponse<List<Map<String, Object>>> recent(
      @RequestParam(defaultValue = "10") int limit) {
    return ApiResponse.ok(notificationService.recent(limit));
  }

  @GetMapping("/unread-count")
  public ApiResponse<Map<String, Object>> unreadCount() {
    return ApiResponse.ok(Map.of("count", notificationService.unreadCount()));
  }

  @GetMapping("/{id}")
  public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
    return ApiResponse.ok(notificationService.detail(id));
  }

  @PutMapping("/{id}/read")
  public ApiResponse<Void> markRead(@PathVariable Long id,
      @RequestBody(required = false) ReadRequest request) {
    notificationService.markRead(id, request == null ? true : request.isRead());
    return ApiResponse.ok();
  }

  @PutMapping("/read-all")
  public ApiResponse<Void> markAllRead() {
    notificationService.markAllRead();
    return ApiResponse.ok();
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> remove(@PathVariable Long id) {
    notificationService.remove(id);
    return ApiResponse.ok();
  }

  @AdminOnly
  @PostMapping("/send")
  public ApiResponse<Map<String, Object>> send(@RequestBody SendRequest request) {
    return ApiResponse.ok(Map.of("sent", notificationService.send(request)));
  }

  public record ReadRequest(Boolean isRead) {
  }
}
