package com.joker.emp.common;

/** 把 MyBatis Map 结果里的数字/字符串安全地转成 Integer，避免 JDBC 驱动返回类型不一致导致的 ClassCastException。 */
public final class Numbers {

  private Numbers() {
  }

  /** 返回 Integer，无法解析时返回 null。 */
  public static Integer toInteger(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Number number) {
      return number.intValue();
    }
    String text = value.toString().trim();
    if (text.isEmpty()) {
      return null;
    }
    try {
      return Integer.valueOf(text);
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  /** 返回 int，无法解析时返回 fallback。 */
  public static int toInt(Object value, int fallback) {
    Integer result = toInteger(value);
    return result == null ? fallback : result;
  }

  /** 返回 Long，无法解析时返回 null。 */
  public static Long toLong(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Number number) {
      return number.longValue();
    }
    String text = value.toString().trim();
    if (text.isEmpty()) {
      return null;
    }
    try {
      return Long.valueOf(text);
    } catch (NumberFormatException ex) {
      return null;
    }
  }
}
