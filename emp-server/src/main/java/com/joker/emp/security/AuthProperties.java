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

  /** 是否信任 X-Forwarded-For / X-Real-IP；仅当应用部署在可信反向代理之后才开启。 */
  private boolean trustForwardedHeaders = false;

  /** 演示模式：允许 password_hash 为空的种子账号使用演示口令登录，生产环境应关闭。 */
  private boolean demoMode = true;
}
