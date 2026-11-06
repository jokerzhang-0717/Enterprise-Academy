package com.joker.emp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

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

  public String createToken(AuthUser user) {
    Instant now = Instant.now();
    Instant expiresAt = now.plusSeconds(properties.getExpireMinutes() * 60);
    return Jwts.builder()
        .issuer(properties.getIssuer())
        .subject(String.valueOf(user.id()))
        .claim("uid", user.id())
        .claim("employeeNo", user.employeeNo())
        .claim("name", user.name())
        .claim("role", user.role())
        .claim("departmentId", user.departmentId())
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiresAt))
        .signWith(key)
        .compact();
  }

  public AuthUser parse(String token) {
    try {
      Claims claims = Jwts.parser()
          .verifyWith(key)
          .requireIssuer(properties.getIssuer())
          .build()
          .parseSignedClaims(token)
          .getPayload();
      return new AuthUser(
          claims.get("uid", Number.class).longValue(),
          claims.get("employeeNo", String.class),
          claims.get("name", String.class),
          claims.get("role", Number.class).intValue(),
          claims.get("departmentId") == null ? null : claims.get("departmentId", Number.class).longValue()
      );
    } catch (JwtException | IllegalArgumentException ex) {
      throw new IllegalArgumentException("登录状态已失效，请重新登录");
    }
  }

  public long getExpireSeconds() {
    return properties.getExpireMinutes() * 60;
  }
}
