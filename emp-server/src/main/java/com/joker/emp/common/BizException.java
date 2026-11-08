package com.joker.emp.common;

/**
 * 业务异常：由全局异常处理器转换成统一响应。
 */
public class BizException extends RuntimeException {

  private final int code;

  public BizException(String message) {
    this(400, message);
  }

  public BizException(int code, String message) {
    super(message);
    this.code = code;
  }

  public int getCode() {
    return code;
  }

  public static BizException unauthorized(String message) {
    return new BizException(401, message);
  }

  public static BizException forbidden(String message) {
    return new BizException(403, message);
  }

  public static BizException notFound(String message) {
    return new BizException(404, message);
  }

  public static BizException tooManyRequests(String message) {
    return new BizException(429, message);
  }
}
