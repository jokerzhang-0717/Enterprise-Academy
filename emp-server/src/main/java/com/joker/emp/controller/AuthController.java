package com.joker.emp.controller;

import com.joker.emp.common.ApiResponse;
import com.joker.emp.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public ApiResponse<Map<String, Object>> login(@RequestBody LoginRequest request,
      HttpServletRequest servletRequest) {
    return ApiResponse.ok(authService.login(request.employeeNo(), request.password(),
        clientIp(servletRequest)));
  }

  @PostMapping("/register")
  public ApiResponse<Map<String, Object>> register(@RequestBody RegisterRequest request,
      HttpServletRequest servletRequest) {
    return ApiResponse.ok(authService.register(request.name(), request.employeeNo(),
        request.password(), request.departmentId(), request.jobTitle(), request.email(),
        request.phone(), clientIp(servletRequest)));
  }

  /** 无需携带访问令牌；使用一次性刷新令牌换发新会话。 */
  @PostMapping("/refresh")
  public ApiResponse<Map<String, Object>> refresh(@RequestBody RefreshTokenRequest request) {
    return ApiResponse.ok(authService.refresh(request.refreshToken()));
  }

  @GetMapping("/me")
  public ApiResponse<Map<String, Object>> me() {
    return ApiResponse.ok(authService.me());
  }

  @PutMapping("/password")
  public ApiResponse<Void> changePassword(@RequestBody ChangePasswordRequest request) {
    authService.changePassword(request.oldPassword(), request.newPassword());
    return ApiResponse.ok();
  }

  /** 无需访问令牌即可调用；同时吊销访问令牌与刷新令牌。 */
  @PostMapping("/logout")
  public ApiResponse<Void> logout(HttpServletRequest servletRequest,
      @RequestBody(required = false) LogoutRequest request) {
    authService.logout(bearerToken(servletRequest),
        request == null ? null : request.refreshToken());
    return ApiResponse.ok();
  }

  private String bearerToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      return header.substring(7).trim();
    }
    return null;
  }

  private String clientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    String realIp = request.getHeader("X-Real-IP");
    if (realIp != null && !realIp.isBlank()) {
      return realIp.trim();
    }
    return request.getRemoteAddr();
  }

  public record LoginRequest(@NotBlank(message = "请输入企业工号") String employeeNo,
                             @NotBlank(message = "请输入密码") String password) {
  }

  public record RegisterRequest(@NotBlank(message = "请输入姓名") String name,
                                @NotBlank(message = "请输入企业工号") String employeeNo,
                                @NotBlank(message = "请输入密码") String password,
                                Long departmentId,
                                String jobTitle,
                                String email,
                                String phone) {
  }

  public record RefreshTokenRequest(@NotBlank(message = "缺少刷新令牌") String refreshToken) {
  }

  public record LogoutRequest(String refreshToken) {
  }

  public record ChangePasswordRequest(@NotBlank String oldPassword,
                                      @NotBlank String newPassword) {
  }
}
