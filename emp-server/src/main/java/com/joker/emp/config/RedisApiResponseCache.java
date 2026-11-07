package com.joker.emp.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joker.emp.common.ApiResponse;
import com.joker.emp.security.AuthContext;
import com.joker.emp.security.AuthUser;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

@Component
@ControllerAdvice
public class RedisApiResponseCache implements HandlerInterceptor, ResponseBodyAdvice<Object> {

  private static final Logger log = LoggerFactory.getLogger(RedisApiResponseCache.class);
  private static final String CACHE_GENERATION_KEY = "api:response-cache:generation";
  private static final String CACHE_KEY_ATTRIBUTE = RedisApiResponseCache.class.getName() + ".key";

  private final StringRedisTemplate redis;
  private final ObjectMapper objectMapper;
  private final Duration ttl;

  public RedisApiResponseCache(StringRedisTemplate redis, ObjectMapper objectMapper,
      @Value("${emp.cache.api-response-ttl:1s}") Duration ttl) {
    this.redis = redis;
    this.objectMapper = objectMapper;
    this.ttl = ttl;
  }

  @Override
  public boolean preHandle(jakarta.servlet.http.HttpServletRequest request,
      jakarta.servlet.http.HttpServletResponse response, Object handler) throws Exception {
    if ("GET".equalsIgnoreCase(request.getMethod())) {
      AuthUser user = AuthContext.get();
      if (user == null || !isCacheablePath(request.getRequestURI())) {
        return true;
      }

      String generation;
      try {
        generation = redis.opsForValue().get(CACHE_GENERATION_KEY);
        if (generation == null) {
          generation = "0";
        }
        String key = cacheKey(request, user.id(), generation);
        String cached = redis.opsForValue().get(key);
        if (cached != null) {
          response.setStatus(200);
          response.setCharacterEncoding(StandardCharsets.UTF_8.name());
          response.setContentType(MediaType.APPLICATION_JSON_VALUE);
          response.getWriter().write(cached);
          return false;
        }
        request.setAttribute(CACHE_KEY_ATTRIBUTE, key);
      } catch (DataAccessException exception) {
        log.warn("Redis API response cache read failed; continuing without cache", exception);
      }
      return true;
    }

    return true;
  }

  @Override
  public void afterCompletion(jakarta.servlet.http.HttpServletRequest request,
      jakarta.servlet.http.HttpServletResponse response, Object handler, Exception exception) {
    if (!"GET".equalsIgnoreCase(request.getMethod())
        && response.getStatus() >= 200 && response.getStatus() < 300) {
      try {
        redis.opsForValue().increment(CACHE_GENERATION_KEY);
      } catch (DataAccessException redisException) {
        log.warn("Redis API response cache invalidation failed", redisException);
      }
    }
  }

  @Override
  public boolean supports(MethodParameter returnType,
      Class<? extends HttpMessageConverter<?>> converterType) {
    return true;
  }

  @Override
  public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
      ServerHttpResponse response) {
    if (!(body instanceof ApiResponse<?> apiResponse) || apiResponse.code() != 0
        || !(request instanceof org.springframework.http.server.ServletServerHttpRequest servletRequest)) {
      return body;
    }

    Object key = servletRequest.getServletRequest().getAttribute(CACHE_KEY_ATTRIBUTE);
    if (key == null) {
      return body;
    }

    try {
      redis.opsForValue().set(String.valueOf(key), objectMapper.writeValueAsString(body), ttl);
    } catch (DataAccessException | JsonProcessingException exception) {
      log.warn("Redis API response cache write failed", exception);
    }
    return body;
  }

  private boolean isCacheablePath(String requestUri) {
    return !requestUri.contains("/files/") && !requestUri.contains("/study-sessions/");
  }

  private String cacheKey(jakarta.servlet.http.HttpServletRequest request, Long userId,
      String generation) {
    String signature = String.join("\n",
        String.valueOf(userId),
        request.getRequestURI(),
        request.getQueryString() == null ? "" : request.getQueryString(),
        request.getHeader("Accept-Language") == null ? "" : request.getHeader("Accept-Language"));
    return "api:response:v1:" + generation + ":" + sha256(signature);
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
