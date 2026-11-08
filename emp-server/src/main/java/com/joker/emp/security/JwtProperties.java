package com.joker.emp.security;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "emp.jwt")
public class JwtProperties {

  /** HS256 密钥，至少 32 字节。 */
  private String secret = "emp-academy-dev-secret-key-change-me-in-production-2025";

  /** 访问 Token 有效期（分钟），默认 15 分钟。 */
  private long expireMinutes = 15;

  private String issuer = "emp-server";

  /** 允许跨域的前端地址；为空表示全部放行（开发环境）。 */
  private List<String> allowedOrigins;
}
