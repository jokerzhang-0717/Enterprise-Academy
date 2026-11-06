package com.joker.emp.controller;

import com.joker.emp.common.ApiResponse;
import com.joker.emp.service.AuthService;
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
  public ApiResponse<Map<String, Object>> login(@RequestBody LoginRequest request) {
    return ApiResponse.ok(authService.login(request.employeeNo(), request.password()));
  }

  @PostMapping("/register")
  public ApiResponse<Map<String, Object>> register(@RequestBody RegisterRequest request) {
    return ApiResponse.ok(authService.register(request.name(), request.employeeNo(),
        request.password(), request.departmentId(), request.jobTitle(), request.email(),
        request.phone()));
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

  @PostMapping("/logout")
  public ApiResponse<Void> logout() {
    // 无状态 JWT，客户端丢弃 token 即可。
    return ApiResponse.ok();
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

  public record ChangePasswordRequest(@NotBlank String oldPassword,
                                      @NotBlank String newPassword) {
  }
}
