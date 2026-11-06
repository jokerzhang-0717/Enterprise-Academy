package com.joker.emp.security;

/**
 * 当前登录用户（从 JWT 还原，请求结束后清理）。
 */
public record AuthUser(Long id, String employeeNo, String name, Integer role, Long departmentId) {

  public boolean isAdmin() {
    return role != null && role == 2;
  }
}
