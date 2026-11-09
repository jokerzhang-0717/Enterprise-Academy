package com.joker.emp.controller;

import com.joker.emp.common.ApiResponse;
import com.joker.emp.service.AuthService;
import com.joker.emp.service.UserService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profile")
public class ProfileController {

  private final UserService userService;
  private final AuthService authService;

  public ProfileController(UserService userService, AuthService authService) {
    this.userService = userService;
    this.authService = authService;
  }

  @GetMapping
  public ApiResponse<Map<String, Object>> profile() {
    return ApiResponse.ok(authService.me());
  }

  @PutMapping
  public ApiResponse<Map<String, Object>> update(@Valid @RequestBody ProfileRequest request) {
    return ApiResponse.ok(userService.updateProfile(request.name(), request.gender(),
        request.age(), request.phone(), request.email(), request.jobTitle(),
        request.specialty(), request.grade(), request.avatarUrl()));
  }

  public record ProfileRequest(String name, String gender, Integer age, String phone,
                               String email, String jobTitle, String specialty,
                               String grade, String avatarUrl) {
  }
}
