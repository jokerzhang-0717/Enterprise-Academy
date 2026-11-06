package com.joker.emp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joker.emp.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

  private final JwtService jwtService;
  private final ObjectMapper objectMapper;

  public AuthInterceptor(JwtService jwtService, ObjectMapper objectMapper) {
    this.jwtService = jwtService;
    this.objectMapper = objectMapper;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    if (HttpMethod.OPTIONS.matches(request.getMethod())) {
      return true;
    }

    String token = resolveToken(request);
    if (token == null || token.isBlank()) {
      writeError(response, 401, "请先登录");
      return false;
    }

    AuthUser user;
    try {
      user = jwtService.parse(token);
    } catch (IllegalArgumentException ex) {
      writeError(response, 401, ex.getMessage());
      return false;
    }
    AuthContext.set(user);

    if (handler instanceof HandlerMethod method) {
      boolean adminOnly = method.hasMethodAnnotation(AdminOnly.class)
          || method.getBeanType().isAnnotationPresent(AdminOnly.class);
      if (adminOnly && !user.isAdmin()) {
        writeError(response, 403, "需要管理员权限");
        return false;
      }
    }
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
      Exception ex) {
    AuthContext.clear();
  }

  private String resolveToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      return header.substring(7).trim();
    }
    if (header != null && !header.isBlank()) {
      return header.trim();
    }
    String alias = request.getHeader("X-Token");
    if (alias != null && !alias.isBlank()) {
      return alias.trim();
    }
    return request.getParameter("token");
  }

  private void writeError(HttpServletResponse response, int code, String message) throws Exception {
    response.setStatus(code);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(code, message)));
  }
}
