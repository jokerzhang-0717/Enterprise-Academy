package com.joker.emp.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.joker.emp.common.BizException;
import com.joker.emp.entity.Department;
import com.joker.emp.entity.SysUser;
import com.joker.emp.mapper.DepartmentMapper;
import com.joker.emp.mapper.SysUserMapper;
import com.joker.emp.security.AuthContext;
import com.joker.emp.security.AuthTokenStore;
import com.joker.emp.security.AuthUser;
import com.joker.emp.security.CredentialFingerprint;
import com.joker.emp.security.JwtService;
import com.joker.emp.security.LoginRateLimiter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  /** 种子账号 password_hash 为空串时使用演示口令。 */
  public static final String DEMO_PASSWORD = "123456";

  private final SysUserMapper userMapper;
  private final DepartmentMapper departmentMapper;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthTokenStore tokenStore;
  private final LoginRateLimiter rateLimiter;

  public AuthService(SysUserMapper userMapper, DepartmentMapper departmentMapper,
      PasswordEncoder passwordEncoder, JwtService jwtService, AuthTokenStore tokenStore,
      LoginRateLimiter rateLimiter) {
    this.userMapper = userMapper;
    this.departmentMapper = departmentMapper;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.tokenStore = tokenStore;
    this.rateLimiter = rateLimiter;
  }

  public Map<String, Object> login(String employeeNo, String password, String ip) {
    rateLimiter.checkLoginAllowed(employeeNo, ip);

    SysUser user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
        .eq(SysUser::getEmployeeNo, employeeNo)
        .last("LIMIT 1"));
    if (user == null || !matches(user, password)) {
      rateLimiter.recordLoginFailure(employeeNo, ip);
      throw BizException.unauthorized("工号或密码错误");
    }
    if (user.getAccountStatus() != null && user.getAccountStatus() == 2) {
      throw BizException.forbidden("账号已被停用，请联系管理员");
    }

    rateLimiter.resetLoginFailures(employeeNo);
    touch(user);
    return issueSession(user);
  }

  /**
   * 用一次性刷新令牌换发新的访问令牌与刷新令牌。
   * 账号停用、密码变更后，旧刷新令牌立即失效。
   */
  public Map<String, Object> refresh(String refreshToken) {
    AuthTokenStore.RefreshSession session = tokenStore.consumeRefreshToken(refreshToken);
    if (session == null) {
      throw BizException.unauthorized("登录状态已失效，请重新登录");
    }
    SysUser user = userMapper.selectById(session.userId());
    if (user == null) {
      throw BizException.unauthorized("账号不存在，请重新登录");
    }
    if (user.getAccountStatus() != null && user.getAccountStatus() == 2) {
      throw BizException.unauthorized("账号已被停用，请联系管理员");
    }
    String fingerprint = CredentialFingerprint.of(user.getId(), user.getPasswordHash());
    if (!fingerprint.equals(session.credentialFingerprint())) {
      throw BizException.unauthorized("登录状态已失效，请重新登录");
    }

    touch(user);
    return issueSession(user);
  }

  /** 登出：吊销刷新令牌，并把当前访问令牌写入黑名单直到自然过期。 */
  public void logout(String accessToken, String refreshToken) {
    tokenStore.revokeRefreshToken(refreshToken);
    if (accessToken == null || accessToken.isBlank()) {
      return;
    }
    try {
      JwtService.ParsedAccessToken parsed = jwtService.parseAccessToken(accessToken);
      long remaining = parsed.expiresAt().getEpochSecond() - Instant.now().getEpochSecond();
      tokenStore.blacklistAccessToken(parsed.jti(), Math.max(1, remaining));
    } catch (IllegalArgumentException ignored) {
      // 令牌已过期或无效时无需再拉黑；刷新令牌仍会被吊销。
    }
  }

  @Transactional
  public Map<String, Object> register(String name, String employeeNo, String password,
      Long departmentId, String jobTitle, String email, String phone, String ip) {
    rateLimiter.checkRegisterAllowed(ip);
    if (employeeNo == null || employeeNo.isBlank()) {
      throw new BizException("请输入企业工号");
    }
    Long exists = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
        .eq(SysUser::getEmployeeNo, employeeNo.trim()));
    if (exists != null && exists > 0) {
      throw new BizException(409, "该工号已注册");
    }
    if (password == null || password.length() < 6) {
      throw new BizException("密码至少需要 6 位");
    }
    SysUser user = new SysUser();
    user.setName(name);
    user.setEmployeeNo(employeeNo.trim());
    user.setPasswordHash(passwordEncoder.encode(password));
    user.setDepartmentId(departmentId);
    user.setJobTitle(jobTitle);
    user.setEmail(email);
    user.setPhone(phone);
    user.setRole(1);
    user.setAccountStatus(1);
    user.setLastActiveAt(LocalDateTime.now());
    userMapper.insert(user);
    return toSafeUser(user);
  }

  public Map<String, Object> me() {
    AuthUser current = AuthContext.require();
    SysUser user = userMapper.selectById(current.id());
    if (user == null) {
      throw BizException.unauthorized("账号不存在");
    }
    Map<String, Object> result = new LinkedHashMap<>(toSafeUser(user));
    Map<String, Object> profile = userMapper.selectLearnerProfile(user.getId());
    if (profile != null) {
      result.put("learningHours", profile.get("learningHours"));
      result.put("completedCourses", profile.get("completedCourses"));
      result.put("studyingCourses", profile.get("studyingCourses"));
      result.put("certificateCount", profile.get("certificateCount"));
      result.put("unreadNotifications", profile.get("unreadNotifications"));
    }
    return result;
  }

  @Transactional
  public void changePassword(String oldPassword, String newPassword) {
    AuthUser current = AuthContext.require();
    SysUser user = userMapper.selectById(current.id());
    if (user == null) {
      throw BizException.unauthorized("账号不存在");
    }
    if (!matches(user, oldPassword)) {
      throw new BizException("原密码不正确");
    }
    if (newPassword == null || newPassword.length() < 8) {
      throw new BizException("新密码至少需要 8 位");
    }
    SysUser update = new SysUser();
    update.setId(user.getId());
    update.setPasswordHash(passwordEncoder.encode(newPassword));
    userMapper.updateById(update);
  }

  public boolean matches(SysUser user, String rawPassword) {
    if (rawPassword == null) {
      return false;
    }
    String hash = user.getPasswordHash();
    if (hash == null || hash.isBlank()) {
      return DEMO_PASSWORD.equals(rawPassword);
    }
    return passwordEncoder.matches(rawPassword, hash);
  }

  public Map<String, Object> toSafeUser(SysUser user) {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("id", user.getId());
    result.put("employeeNo", user.getEmployeeNo());
    result.put("name", user.getName());
    result.put("departmentId", user.getDepartmentId());
    if (user.getDepartmentId() != null) {
      Department department = departmentMapper.selectById(user.getDepartmentId());
      result.put("department", department == null ? null : department.getName());
    } else {
      result.put("department", null);
    }
    result.put("jobTitle", user.getJobTitle());
    result.put("grade", user.getGrade());
    result.put("specialty", user.getSpecialty());
    result.put("avatarUrl", user.getAvatarUrl());
    result.put("gender", user.getGender());
    result.put("age", user.getAge());
    result.put("phone", user.getPhone());
    result.put("email", user.getEmail());
    result.put("role", user.getRole());
    result.put("accountStatus", user.getAccountStatus());
    result.put("lastActiveAt", user.getLastActiveAt());
    return result;
  }

  private Map<String, Object> issueSession(SysUser user) {
    AuthUser authUser = new AuthUser(user.getId(), user.getEmployeeNo(), user.getName(),
        user.getRole(), user.getDepartmentId());
    String fingerprint = CredentialFingerprint.of(user.getId(), user.getPasswordHash());
    JwtService.IssuedAccessToken access = jwtService.createAccessToken(authUser, fingerprint);
    String refreshToken = tokenStore.createRefreshToken(user.getId(), fingerprint);

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("token", access.token());
    result.put("tokenType", "Bearer");
    result.put("expiresIn", access.expiresInSeconds());
    result.put("refreshToken", refreshToken);
    result.put("refreshExpiresIn", tokenStore.getRefreshExpireSeconds());
    result.put("user", toSafeUser(user));
    return result;
  }

  private void touch(SysUser user) {
    user.setLastActiveAt(LocalDateTime.now());
    userMapper.updateById(user);
  }
}
