package com.joker.emp.security;

import com.joker.emp.common.BizException;

public final class AuthContext {

  private static final ThreadLocal<AuthUser> HOLDER = new ThreadLocal<>();

  private AuthContext() {
  }

  public static void set(AuthUser user) {
    HOLDER.set(user);
  }

  public static AuthUser get() {
    return HOLDER.get();
  }

  public static AuthUser require() {
    AuthUser user = HOLDER.get();
    if (user == null) {
      throw BizException.unauthorized("请先登录");
    }
    return user;
  }

  public static Long userId() {
    return require().id();
  }

  public static void clear() {
    HOLDER.remove();
  }
}
