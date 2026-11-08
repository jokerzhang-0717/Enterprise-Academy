package com.joker.emp.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 登录会话与限流配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "emp.auth")
public class AuthProperties {

  /** 刷新令牌有效期（天）。 */
  private long refreshTokenDays = 7;

  /** 同一工号在窗口期内允许的连续登录失败次数。 */
  private int maxLoginAttempts = 5;

  /** 同一 IP 在窗口期内允许的连续登录失败次数。 */
  private int maxLoginIpAttempts = 30;

  /** 登录失败计数窗口（分钟）。 */
  private long loginWindowMinutes = 15;

  /** 同一 IP 在窗口期内允许的注册次数。 */
  private int maxRegisterAttempts = 5;

  /** 注册限流窗口（分钟）。 */
  private long registerWindowMinutes = 60;
}
