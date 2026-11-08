package com.joker.emp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joker.emp.common.ApiResponse;
import com.joker.emp.entity.SysUser;
import com.joker.emp.mapper.SysUserMapper;
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
  private final AuthTokenStore tokenStore;
  private final SysUserMapper userMapper;
  private final ObjectMapper objectMapper;

  public AuthInterceptor(JwtService jwtService, AuthTokenStore tokenStore,
      SysUserMapper userMapper, ObjectMapper objectMapper) {
    this.jwtService = jwtService;
    this.tokenStore = tokenStore;
    this.userMapper = userMapper;
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

    JwtService.ParsedAccessToken parsed;
    try {
      parsed = jwtService.parseAccessToken(token);
    } catch (IllegalArgumentException ex) {
      writeError(response, 401, ex.getMessage());
      return false;
    }

    if (tokenStore.isAccessTokenRevoked(parsed.jti())) {
      writeError(response, 401, "登录状态已失效，请重新登录");
      return false;
    }

    // 每次请求都以数据库为准，账号停用 / 角色调整立即生效。
    SysUser account = userMapper.selectById(parsed.user().id());
    if (account == null) {
      writeError(response, 401, "账号不存在，请重新登录");
      return false;
    }
    if (account.getAccountStatus() != null && account.getAccountStatus() == 2) {
      writeError(response, 401, "账号已被停用，请联系管理员");
      return false;
    }
    String fingerprint = CredentialFingerprint.of(account.getId(), account.getPasswordHash());
    if (!fingerprint.equals(parsed.credentialFingerprint())) {
      writeError(response, 401, "登录状态已失效，请重新登录");
      return false;
    }

    AuthUser user = new AuthUser(account.getId(), account.getEmployeeNo(), account.getName(),
        account.getRole(), account.getDepartmentId());
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

  /** 只接受 Authorization: Bearer <token>，不再兼容裸 header、X-Token 或查询参数。 */
  private String resolveToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      return header.substring(7).trim();
    }
    return null;
  }

  private void writeError(HttpServletResponse response, int code, String message) throws Exception {
    response.setStatus(code);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(code, message)));
  }
}
