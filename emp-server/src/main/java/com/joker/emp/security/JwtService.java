package com.joker.emp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

  private static final String TOKEN_TYPE = "access";

  private final JwtProperties properties;
  private final SecretKey key;

  public JwtService(JwtProperties properties) {
    this.properties = properties;
    byte[] bytes = properties.getSecret().getBytes(StandardCharsets.UTF_8);
    if (bytes.length < 32) {
      throw new IllegalStateException("emp.jwt.secret 至少需要 32 字节");
    }
    this.key = Keys.hmacShaKeyFor(bytes);
  }

  /** 新签发的访问令牌。 */
  public record IssuedAccessToken(String token, String jti, long expiresInSeconds) {
  }

  /** 解析后的访问令牌；凭证指纹用于在每次请求时校验账号状态。 */
  public record ParsedAccessToken(AuthUser user, String jti, String credentialFingerprint,
                                  Instant expiresAt) {
  }

  public IssuedAccessToken createAccessToken(AuthUser user, String credentialFingerprint) {
    Instant now = Instant.now();
    Instant expiresAt = now.plusSeconds(getExpireSeconds());
    String jti = UUID.randomUUID().toString();
    String token = Jwts.builder()
        .issuer(properties.getIssuer())
        .subject(String.valueOf(user.id()))
        .id(jti)
        .claim("typ", TOKEN_TYPE)
        .claim("cv", credentialFingerprint)
        .claim("uid", user.id())
        .claim("employeeNo", user.employeeNo())
        .claim("name", user.name())
        .claim("role", user.role())
        .claim("departmentId", user.departmentId())
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiresAt))
        .signWith(key)
        .compact();
    return new IssuedAccessToken(token, jti, getExpireSeconds());
  }

  /**
   * 仅接受带 jti / typ=access / 凭证指纹的访问令牌。
   * 旧版本 12 小时令牌缺少这些声明，会直接失效并要求重新登录。
   */
  public ParsedAccessToken parseAccessToken(String token) {
    try {
      Claims claims = Jwts.parser()
          .verifyWith(key)
          .requireIssuer(properties.getIssuer())
          .build()
          .parseSignedClaims(token)
          .getPayload();
      String jti = claims.getId();
      String type = claims.get("typ", String.class);
      String fingerprint = claims.get("cv", String.class);
      if (jti == null || jti.isBlank() || !TOKEN_TYPE.equals(type)
          || fingerprint == null || fingerprint.isBlank()) {
        throw new IllegalArgumentException("登录状态已失效，请重新登录");
      }
      AuthUser user = new AuthUser(
          claims.get("uid", Number.class).longValue(),
          claims.get("employeeNo", String.class),
          claims.get("name", String.class),
          claims.get("role", Number.class).intValue(),
          claims.get("departmentId") == null ? null : claims.get("departmentId", Number.class).longValue()
      );
      return new ParsedAccessToken(user, jti, fingerprint, claims.getExpiration().toInstant());
    } catch (JwtException | IllegalArgumentException | NullPointerException ex) {
      throw new IllegalArgumentException("登录状态已失效，请重新登录");
    }
  }

  public long getExpireSeconds() {
    return properties.getExpireMinutes() * 60;
  }
}
