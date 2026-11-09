package com.joker.emp.controller;

import com.joker.emp.common.ApiResponse;
import com.joker.emp.common.PageResult;
import com.joker.emp.entity.SysUser;
import com.joker.emp.security.AdminOnly;
import com.joker.emp.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
@RequestMapping("/admin/users")
public class AdminUserController {

  private final UserService userService;

  public AdminUserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public ApiResponse<PageResult<Map<String, Object>>> page(
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "10") long size,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Long departmentId,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false, defaultValue = "1") Integer role) {
    return ApiResponse.ok(userService.page(page, size, keyword, departmentId, status, role));
  }

  @GetMapping("/options")
  public ApiResponse<List<Map<String, Object>>> options(
      @RequestParam(required = false) String keyword) {
    return ApiResponse.ok(userService.options(keyword));
  }

  @GetMapping("/{id}")
  public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
    return ApiResponse.ok(userService.detail(id));
  }

  @PostMapping
  public ApiResponse<Map<String, Object>> create(@Valid @RequestBody UserSaveRequest request) {
    return ApiResponse.ok(userService.create(request.toEntity(), request.password()));
  }

  @PutMapping("/{id}")
  public ApiResponse<Map<String, Object>> update(@PathVariable Long id,
      @Valid @RequestBody UserSaveRequest request) {
    return ApiResponse.ok(userService.update(id, request.toEntity()));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> remove(@PathVariable Long id) {
    userService.remove(id);
    return ApiResponse.ok();
  }

  @PutMapping("/{id}/status")
  public ApiResponse<Void> updateStatus(@PathVariable Long id,
      @Valid @RequestBody StatusRequest request) {
    userService.updateStatus(id, request.accountStatus());
    return ApiResponse.ok();
  }

  @PostMapping("/{id}/reset-password")
  public ApiResponse<Void> resetPassword(@PathVariable Long id,
      @Valid @RequestBody(required = false) ResetPasswordRequest request) {
    userService.resetPassword(id, request == null ? null : request.newPassword());
    return ApiResponse.ok();
  }

  public record UserSaveRequest(
      @NotBlank(message = "请输入姓名") String name,
      @NotBlank(message = "请输入工号") String employeeNo,
      String password,
      Long departmentId,
      String jobTitle,
      String grade,
      String specialty,
      String avatarUrl,
      String phone,
      String email,
      Integer role,
      Integer accountStatus) {

    public SysUser toEntity() {
      SysUser user = new SysUser();
      user.setName(name);
      user.setEmployeeNo(employeeNo);
      user.setDepartmentId(departmentId);
      user.setJobTitle(jobTitle);
      user.setGrade(grade);
      user.setSpecialty(specialty);
      user.setAvatarUrl(avatarUrl);
      user.setPhone(phone);
      user.setEmail(email);
      user.setRole(role);
      user.setAccountStatus(accountStatus);
      return user;
    }
  }

  public record StatusRequest(@NotNull(message = "请选择账号状态") Integer accountStatus) {
  }

  public record ResetPasswordRequest(String newPassword) {
  }
}
