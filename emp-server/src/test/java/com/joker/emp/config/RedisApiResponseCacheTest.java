package com.joker.emp.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joker.emp.security.AuthContext;
import com.joker.emp.security.AuthUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RedisApiResponseCacheTest {

  private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
  private final ValueOperations<String, String> values = mock(ValueOperations.class);
  private final RedisApiResponseCache cache =
      new RedisApiResponseCache(redis, new ObjectMapper(), java.time.Duration.ofSeconds(1));

  @AfterEach
  void clearAuthContext() {
    AuthContext.clear();
  }

  @Test
  void servesCachedResponseForAuthenticatedRead() throws Exception {
    AuthContext.set(new AuthUser(7L, "EMP-7", "学员", 1, 1L));
    when(redis.opsForValue()).thenReturn(values);
    when(values.get(anyString())).thenAnswer(invocation ->
        "api:response-cache:generation".equals(invocation.getArgument(0))
            ? "4" : "{\"code\":0,\"message\":\"ok\",\"data\":[]}");

    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/courses");
    MockHttpServletResponse response = new MockHttpServletResponse();

    assertFalse(cache.preHandle(request, response, new Object()));
    assertEquals(200, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"data\":[]"));
  }

  @Test
  void skipsCacheWhenRedisIsUnavailable() throws Exception {
    AuthContext.set(new AuthUser(7L, "EMP-7", "学员", 1, 1L));
    when(redis.opsForValue()).thenThrow(
        new org.springframework.data.redis.RedisConnectionFailureException("offline"));

    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/courses");
    MockHttpServletResponse response = new MockHttpServletResponse();

    assertTrue(cache.preHandle(request, response, new Object()));
  }
}
