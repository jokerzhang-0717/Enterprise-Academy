package com.joker.emp.security;

import com.joker.emp.common.BizException;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 基于 Redis 的登录 / 注册限流，防止无限次撞库。
 * 只统计失败登录；登录成功后清空该工号的失败计数。
 */
@Component
public class LoginRateLimiter {

  private static final Logger log = LoggerFactory.getLogger(LoginRateLimiter.class);
  private static final String LOGIN_ACCOUNT_PREFIX = "auth:limit:login:account:";
  private static final String LOGIN_IP_PREFIX = "auth:limit:login:ip:";
  private static final String REGISTER_IP_PREFIX = "auth:limit:register:ip:";

  private final StringRedisTemplate redis;
  private final AuthProperties properties;

  public LoginRateLimiter(StringRedisTemplate redis, AuthProperties properties) {
    this.redis = redis;
    this.properties = properties;
  }

  public void checkLoginAllowed(String employeeNo, String ip) {
    long accountAttempts = count(LOGIN_ACCOUNT_PREFIX + normalize(employeeNo));
    long ipAttempts = count(LOGIN_IP_PREFIX + normalize(ip));
    if (accountAttempts >= properties.getMaxLoginAttempts()
        || ipAttempts >= properties.getMaxLoginIpAttempts()) {
      throw BizException.tooManyRequests("登录失败次数过多，请稍后再试");
    }
  }

  public void recordLoginFailure(String employeeNo, String ip) {
    increment(LOGIN_ACCOUNT_PREFIX + normalize(employeeNo),
        Duration.ofMinutes(properties.getLoginWindowMinutes()));
    increment(LOGIN_IP_PREFIX + normalize(ip),
        Duration.ofMinutes(properties.getLoginWindowMinutes()));
  }

  public void resetLoginFailures(String employeeNo) {
    try {
      redis.delete(LOGIN_ACCOUNT_PREFIX + normalize(employeeNo));
    } catch (DataAccessException exception) {
      log.warn("Redis login failure counter reset failed", exception);
    }
  }

  public void checkRegisterAllowed(String ip) {
    String key = REGISTER_IP_PREFIX + normalize(ip);
    long attempts = increment(key, Duration.ofMinutes(properties.getRegisterWindowMinutes()));
    if (attempts > properties.getMaxRegisterAttempts()) {
      throw BizException.tooManyRequests("注册过于频繁，请稍后再试");
    }
  }

  private long count(String key) {
    try {
      String value = redis.opsForValue().get(key);
      return value == null ? 0 : Long.parseLong(value);
    } catch (DataAccessException | NumberFormatException exception) {
      log.warn("Redis rate limit read failed", exception);
      return 0;
    }
  }

  private long increment(String key, Duration window) {
    try {
      Long value = redis.opsForValue().increment(key);
      if (value != null && value == 1L) {
        redis.expire(key, window);
      }
      return value == null ? 0 : value;
    } catch (DataAccessException exception) {
      log.warn("Redis rate limit write failed", exception);
      return 0;
    }
  }

  private String normalize(String value) {
    return value == null || value.isBlank() ? "unknown" : value.trim().toLowerCase();
  }
}
