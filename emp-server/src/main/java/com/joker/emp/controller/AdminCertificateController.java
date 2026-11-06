package com.joker.emp.controller;

import com.joker.emp.common.ApiResponse;
import com.joker.emp.common.PageResult;
import com.joker.emp.entity.CertificateTemplate;
import com.joker.emp.security.AdminOnly;
import com.joker.emp.service.CertificateService;
import com.joker.emp.service.CertificateService.IssueSaveRequest;
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
@RequestMapping("/admin/certificates")
public class AdminCertificateController {

  private final CertificateService certificateService;

  public AdminCertificateController(CertificateService certificateService) {
    this.certificateService = certificateService;
  }

  @GetMapping
  public ApiResponse<PageResult<Map<String, Object>>> page(
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "10") long size,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) String keyword) {
    return ApiResponse.ok(certificateService.page(page, size, status, keyword));
  }

  @GetMapping("/{id}")
  public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
    return ApiResponse.ok(certificateService.detail(id));
  }

  @PostMapping
  public ApiResponse<Map<String, Object>> create(@RequestBody IssueSaveRequest request) {
    return ApiResponse.ok(certificateService.createIssue(request));
  }

  @PutMapping("/{id}")
  public ApiResponse<Map<String, Object>> update(@PathVariable Long id,
      @RequestBody IssueSaveRequest request) {
    return ApiResponse.ok(certificateService.updateIssue(id, request));
  }

  @PutMapping("/{id}/status")
  public ApiResponse<Map<String, Object>> updateStatus(@PathVariable Long id,
      @RequestBody StatusRequest request) {
    return ApiResponse.ok(certificateService.updateStatus(id, request.status()));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> remove(@PathVariable Long id) {
    certificateService.remove(id);
    return ApiResponse.ok();
  }

  @GetMapping("/templates")
  public ApiResponse<List<Map<String, Object>>> templates() {
    return ApiResponse.ok(certificateService.templates());
  }

  @PostMapping("/templates")
  public ApiResponse<Map<String, Object>> createTemplate(@RequestBody CertificateTemplate request) {
    return ApiResponse.ok(certificateService.createTemplate(request));
  }

  @PutMapping("/templates/{id}")
  public ApiResponse<Map<String, Object>> updateTemplate(@PathVariable Long id,
      @RequestBody CertificateTemplate request) {
    return ApiResponse.ok(certificateService.updateTemplate(id, request));
  }

  @DeleteMapping("/templates/{id}")
  public ApiResponse<Void> removeTemplate(@PathVariable Long id) {
    certificateService.removeTemplate(id);
    return ApiResponse.ok();
  }

  public record StatusRequest(Integer status) {
  }
}
