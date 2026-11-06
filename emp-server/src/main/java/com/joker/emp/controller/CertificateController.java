package com.joker.emp.controller;

import com.joker.emp.common.ApiResponse;
import com.joker.emp.service.CertificateService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/certificates")
public class CertificateController {

  private final CertificateService certificateService;

  public CertificateController(CertificateService certificateService) {
    this.certificateService = certificateService;
  }

  @GetMapping
  public ApiResponse<List<Map<String, Object>>> myCertificates() {
    return ApiResponse.ok(certificateService.myCertificates());
  }

  @GetMapping("/my")
  public ApiResponse<List<Map<String, Object>>> my() {
    return ApiResponse.ok(certificateService.myCertificates());
  }

  /** 证书验真，公开访问（无需登录）。 */
  @GetMapping("/verify/{serial}")
  public ApiResponse<Map<String, Object>> verify(@PathVariable String serial) {
    return ApiResponse.ok(certificateService.verify(serial));
  }
}
