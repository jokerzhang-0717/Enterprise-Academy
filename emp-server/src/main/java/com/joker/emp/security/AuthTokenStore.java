package com.joker.emp.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

/**
 * Redis 侧会话状态：访问令牌黑名单 + 一次性刷新令牌。
 */
@Component
public class AuthTokenStore {

  private static final Logger log = LoggerFactory.getLogger(AuthTokenStore.class);
  private static final String BLACKLIST_PREFIX = "auth:blacklist:access:";
  private static final String REFRESH_PREFIX = "auth:refresh:";
  private static final SecureRandom RANDOM = new SecureRandom();

  /** Redis < 6.2 没有 GETDEL，用 Lua 保证 GET+DEL 原子执行。 */
  private static final DefaultRedisScript<String> CONSUME_SCRIPT = new DefaultRedisScript<>(
      "local v = redis.call('GET', KEYS[1]); if v then redis.call('DEL', KEYS[1]) end; return v;",
      String.class);

  private final StringRedisTemplate redis;
  private final AuthProperties properties;

  public AuthTokenStore(StringRedisTemplate redis, AuthProperties properties) {
    this.redis = redis;
    this.properties = properties;
  }

  /** 刷新令牌对应的服务端会话。 */
  public record RefreshSession(Long userId, String credentialFingerprint) {
  }

  public String createRefreshToken(Long userId, String credentialFingerprint) {
    byte[] bytes = new byte[32];
    RANDOM.nextBytes(bytes);
    String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    String value = userId + "\n" + credentialFingerprint;
    redis.opsForValue().set(refreshKey(token), value, Duration.ofDays(properties.getRefreshTokenDays()));
    return token;
  }

  /**
   * 读取并删除刷新令牌（一次性使用）。不存在或 Redis 不可用时返回 null。
   */
  public RefreshSession consumeRefreshToken(String token) {
    if (token == null || token.isBlank()) {
      return null;
    }
    String key = refreshKey(token);
    try {
      // Lua 脚本原子消费：并发重放同一刷新令牌时只有一个请求能拿到值。
      String value = redis.execute(CONSUME_SCRIPT, List.of(key));
      if (value == null) {
        return null;
      }
      String[] parts = value.split("\n", 2);
      if (parts.length != 2) {
        return null;
      }
      return new RefreshSession(Long.parseLong(parts[0]), parts[1]);
    } catch (DataAccessException | NumberFormatException exception) {
      log.warn("Redis refresh token lookup failed", exception);
      return null;
    }
  }

  public void revokeRefreshToken(String token) {
    if (token == null || token.isBlank()) {
      return;
    }
    try {
      redis.delete(refreshKey(token));
    } catch (DataAccessException exception) {
      log.warn("Redis refresh token revoke failed", exception);
    }
  }

  public void blacklistAccessToken(String jti, long ttlSeconds) {
    if (jti == null || jti.isBlank()) {
      return;
    }
    try {
      redis.opsForValue().set(BLACKLIST_PREFIX + jti, "1",
          Duration.ofSeconds(Math.max(1, ttlSeconds)));
    } catch (DataAccessException exception) {
      log.warn("Redis access token blacklist write failed", exception);
    }
  }

  /**
   * Redis 不可用时放行，避免缓存故障导致全站无法登录；令牌本身仍会过期。
   */
  public boolean isAccessTokenRevoked(String jti) {
    if (jti == null || jti.isBlank()) {
      return false;
    }
    try {
      return Boolean.TRUE.equals(redis.hasKey(BLACKLIST_PREFIX + jti));
    } catch (DataAccessException exception) {
      log.warn("Redis access token blacklist read failed", exception);
      return false;
    }
  }

  public long getRefreshExpireSeconds() {
    return Duration.ofDays(properties.getRefreshTokenDays()).toSeconds();
  }

  private String refreshKey(String token) {
    return REFRESH_PREFIX + sha256(token);
  }

  private String sha256(String value) {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256")
          .digest(value.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable", exception);
    }
  }
}
