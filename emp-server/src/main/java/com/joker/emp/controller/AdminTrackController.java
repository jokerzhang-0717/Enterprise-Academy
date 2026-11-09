package com.joker.emp.controller;

import com.joker.emp.common.ApiResponse;
import com.joker.emp.common.PageResult;
import com.joker.emp.security.AdminOnly;
import com.joker.emp.service.TrackService;
import com.joker.emp.service.TrackService.TrackSaveRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
@RequestMapping("/admin/tracks")
public class AdminTrackController {

  private final TrackService trackService;

  public AdminTrackController(TrackService trackService) {
    this.trackService = trackService;
  }

  @GetMapping
  public ApiResponse<PageResult<Map<String, Object>>> page(
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "10") long size,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) Integer status) {
    return ApiResponse.ok(trackService.page(page, size, keyword, category, status, false));
  }

  @GetMapping("/{id}")
  public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
    return ApiResponse.ok(trackService.detail(id, false));
  }

  @PostMapping
  public ApiResponse<Map<String, Object>> create(@Valid @RequestBody TrackSaveRequest request) {
    return ApiResponse.ok(trackService.create(request));
  }

  @PutMapping("/{id}")
  public ApiResponse<Map<String, Object>> update(@PathVariable Long id,
      @Valid @RequestBody TrackSaveRequest request) {
    return ApiResponse.ok(trackService.update(id, request));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> remove(@PathVariable Long id) {
    trackService.remove(id);
    return ApiResponse.ok();
  }

  @PutMapping("/{id}/status")
  public ApiResponse<Void> updateStatus(@PathVariable Long id,
      @Valid @RequestBody StatusRequest request) {
    trackService.updateStatus(id, request.status());
    return ApiResponse.ok();
  }

  public record StatusRequest(@NotNull(message = "请选择通道状态") Integer status) {
  }
}
