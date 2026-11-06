package com.joker.emp.common;

/**
 * 统一响应体。前端所有接口都按 { code, message, data } 解析。
 */
public record ApiResponse<T>(int code, String message, T data) {

  public static <T> ApiResponse<T> ok(T data) {
    return new ApiResponse<>(0, "ok", data);
  }

  public static ApiResponse<Void> ok() {
    return new ApiResponse<>(0, "ok", null);
  }

  public static <T> ApiResponse<T> fail(int code, String message) {
    return new ApiResponse<>(code, message, null);
  }
}
