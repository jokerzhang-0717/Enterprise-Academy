package com.joker.emp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.joker.emp.common.BizException;
import com.joker.emp.common.Numbers;
import com.joker.emp.common.PageResult;
import com.joker.emp.entity.Department;
import com.joker.emp.entity.SysUser;
import com.joker.emp.mapper.DepartmentMapper;
import com.joker.emp.mapper.SysUserMapper;
import com.joker.emp.security.AuthContext;
import com.joker.emp.security.AuthProperties;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private final SysUserMapper userMapper;
  private final DepartmentMapper departmentMapper;
  private final PasswordEncoder passwordEncoder;
  private final OperationLogService logService;
  private final AuthProperties authProperties;

  public UserService(SysUserMapper userMapper, DepartmentMapper departmentMapper,
      PasswordEncoder passwordEncoder, OperationLogService logService,
      AuthProperties authProperties) {
    this.userMapper = userMapper;
    this.departmentMapper = departmentMapper;
    this.passwordEncoder = passwordEncoder;
    this.logService = logService;
    this.authProperties = authProperties;
  }

  public PageResult<Map<String, Object>> page(long page, long size, String keyword,
      Long departmentId, Integer status, Integer role) {
    IPage<SysUser> result = userMapper.selectPage(new Page<>(page, size),
        Wrappers.<SysUser>lambdaQuery()
            .eq(role != null, SysUser::getRole, role)
            .eq(departmentId != null, SysUser::getDepartmentId, departmentId)
            .eq(status != null, SysUser::getAccountStatus, status)
            .and(keyword != null && !keyword.isBlank(), w -> w
                .like(SysUser::getName, keyword)
                .or().like(SysUser::getEmployeeNo, keyword)
                .or().like(SysUser::getPhone, keyword)
                .or().like(SysUser::getEmail, keyword))
            .orderByAsc(SysUser::getId));

    List<SysUser> users = result.getRecords();
    Map<Long, Department> departments = departmentMapper.selectList(null).stream()
        .collect(Collectors.toMap(Department::getId, Function.identity()));
    Map<Long, Map<String, Object>> profiles = new HashMap<>();
    if (!users.isEmpty()) {
      profiles = userMapper.selectLearnerProfiles(
              users.stream().map(SysUser::getId).toList()).stream()
          .collect(Collectors.toMap(m -> Numbers.toLong(m.get("userId")), m -> m));
    }

    List<Map<String, Object>> records = new ArrayList<>();
    for (SysUser user : users) {
      Map<String, Object> item = new LinkedHashMap<>();
      item.put("id", user.getId());
      item.put("extId", user.getExtId());
      item.put("employeeNo", user.getEmployeeNo());
      item.put("name", user.getName());
      item.put("departmentId", user.getDepartmentId());
      Department dept = user.getDepartmentId() == null ? null : departments.get(user.getDepartmentId());
      item.put("department", dept == null ? null : dept.getName());
      item.put("jobTitle", user.getJobTitle());
      item.put("grade", user.getGrade());
      item.put("specialty", user.getSpecialty());
      item.put("avatarUrl", user.getAvatarUrl());
      item.put("phone", user.getPhone());
      item.put("email", user.getEmail());
      item.put("role", user.getRole());
      item.put("accountStatus", user.getAccountStatus());
      item.put("lastActiveAt", user.getLastActiveAt());
      item.put("createdAt", user.getCreatedAt());

      Map<String, Object> profile = profiles.get(user.getId());
      item.put("learningHours", profile == null ? 0 : profile.get("learningHours"));
      item.put("completedCourses", profile == null ? 0 : profile.get("completedCourses"));
      item.put("studyingCourses", profile == null ? 0 : profile.get("studyingCourses"));
      item.put("certificateCount", profile == null ? 0 : profile.get("certificateCount"));
      item.put("unreadNotifications", profile == null ? 0 : profile.get("unreadNotifications"));
      records.add(item);
    }
    return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), records);
  }

  public Map<String, Object> detail(Long id) {
    SysUser user = userMapper.selectById(id);
    if (user == null) {
      throw BizException.notFound("用户不存在");
    }
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("id", user.getId());
    item.put("employeeNo", user.getEmployeeNo());
    item.put("name", user.getName());
    item.put("departmentId", user.getDepartmentId());
    Department dept = user.getDepartmentId() == null ? null : departmentMapper.selectById(user.getDepartmentId());
    item.put("department", dept == null ? null : dept.getName());
    item.put("jobTitle", user.getJobTitle());
    item.put("grade", user.getGrade());
    item.put("specialty", user.getSpecialty());
    item.put("avatarUrl", user.getAvatarUrl());
    item.put("gender", user.getGender());
    item.put("age", user.getAge());
    item.put("phone", user.getPhone());
    item.put("email", user.getEmail());
    item.put("role", user.getRole());
    item.put("accountStatus", user.getAccountStatus());
    item.put("lastActiveAt", user.getLastActiveAt());
    item.put("createdAt", user.getCreatedAt());
    Map<String, Object> profile = userMapper.selectLearnerProfile(id);
    if (profile != null) {
      profile.forEach((k, v) -> {
        if (!"userId".equals(k)) {
          item.put(k, v);
        }
      });
    }
    return item;
  }

  @Transactional
  public Map<String, Object> create(SysUser request, String rawPassword) {
    if (request.getEmployeeNo() == null || request.getEmployeeNo().isBlank()) {
      throw new BizException("工号不能为空");
    }
    long exists = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
        .eq(SysUser::getEmployeeNo, request.getEmployeeNo().trim()));
    if (exists > 0) {
      throw new BizException(409, "该工号已存在");
    }
    request.setId(null);
    request.setEmployeeNo(request.getEmployeeNo().trim());
    String initialPassword = resolvePassword(rawPassword, "请设置初始密码");
    request.setPasswordHash(passwordEncoder.encode(initialPassword));
    request.setRole(request.getRole() == null ? 1 : request.getRole());
    request.setAccountStatus(request.getAccountStatus() == null ? 1 : request.getAccountStatus());
    if (request.getLastActiveAt() == null) {
      request.setLastActiveAt(LocalDateTime.now());
    }
    userMapper.insert(request);
    logService.record("user", "新增用户 " + request.getName(), "user", request.getId());
    return detail(request.getId());
  }

  @Transactional
  public Map<String, Object> update(Long id, SysUser request) {
    SysUser exists = userMapper.selectById(id);
    if (exists == null) {
      throw BizException.notFound("用户不存在");
    }
    if (request.getEmployeeNo() != null && !request.getEmployeeNo().isBlank()
        && !request.getEmployeeNo().equals(exists.getEmployeeNo())) {
      long count = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
          .eq(SysUser::getEmployeeNo, request.getEmployeeNo().trim())
          .ne(SysUser::getId, id));
      if (count > 0) {
        throw new BizException(409, "该工号已被占用");
      }
      exists.setEmployeeNo(request.getEmployeeNo().trim());
    }
    if (request.getName() != null) exists.setName(request.getName());
    if (request.getDepartmentId() != null) exists.setDepartmentId(request.getDepartmentId());
    if (request.getJobTitle() != null) exists.setJobTitle(request.getJobTitle());
    if (request.getGrade() != null) exists.setGrade(request.getGrade());
    if (request.getSpecialty() != null) exists.setSpecialty(request.getSpecialty());
    if (request.getAvatarUrl() != null) exists.setAvatarUrl(request.getAvatarUrl());
    if (request.getPhone() != null) exists.setPhone(request.getPhone());
    if (request.getEmail() != null) exists.setEmail(request.getEmail());
    if (request.getRole() != null) exists.setRole(request.getRole());
    if (request.getAccountStatus() != null) exists.setAccountStatus(request.getAccountStatus());
    userMapper.updateById(exists);
    logService.record("user", "更新用户 " + exists.getName(), "user", id);
    return detail(id);
  }

  @Transactional
  public void remove(Long id) {
    SysUser exists = userMapper.selectById(id);
    if (exists == null) {
      throw BizException.notFound("用户不存在");
    }
    if (AuthContext.userId().equals(id)) {
      throw new BizException("不能删除当前登录账号");
    }
    userMapper.deleteById(id);
    logService.record("user", "删除用户 " + exists.getName(), "user", id);
  }

  @Transactional
  public void updateStatus(Long id, Integer accountStatus) {
    SysUser exists = userMapper.selectById(id);
    if (exists == null) {
      throw BizException.notFound("用户不存在");
    }
    if (accountStatus == null || (accountStatus != 1 && accountStatus != 2)) {
      throw new BizException("账号状态只能是 1 正常 / 2 停用");
    }
    SysUser update = new SysUser();
    update.setId(id);
    update.setAccountStatus(accountStatus);
    userMapper.updateById(update);
    logService.record("user", (accountStatus == 1 ? "启用" : "停用") + "用户 " + exists.getName(),
        "user", id);
  }

  @Transactional
  public void resetPassword(Long id, String newPassword) {
    SysUser exists = userMapper.selectById(id);
    if (exists == null) {
      throw BizException.notFound("用户不存在");
    }
    String raw = resolvePassword(newPassword, "请设置新密码");
    if (raw.length() < 6) {
      throw new BizException("密码至少需要 6 位");
    }
    SysUser update = new SysUser();
    update.setId(id);
    update.setPasswordHash(passwordEncoder.encode(raw));
    userMapper.updateById(update);
    logService.record("user", "重置用户 " + exists.getName() + " 的密码", "user", id);
  }

  /** 演示模式下未填写密码时沿用 123456；生产环境必须显式传入密码。 */
  private String resolvePassword(String rawPassword, String missingMessage) {
    if (rawPassword != null && !rawPassword.isBlank()) {
      return rawPassword;
    }
    if (authProperties.isDemoMode()) {
      return AuthService.DEMO_PASSWORD;
    }
    throw new BizException(missingMessage);
  }

  public List<Map<String, Object>> options(String keyword) {
    List<SysUser> users = userMapper.selectList(Wrappers.<SysUser>lambdaQuery()
        .eq(SysUser::getRole, 1)
        .eq(SysUser::getAccountStatus, 1)
        .and(keyword != null && !keyword.isBlank(), w -> w
            .like(SysUser::getName, keyword)
            .or().like(SysUser::getEmployeeNo, keyword))
        .orderByAsc(SysUser::getId)
        .last("LIMIT 200"));
    Map<Long, Department> departments = departmentMapper.selectList(null).stream()
        .collect(Collectors.toMap(Department::getId, Function.identity()));
    List<Map<String, Object>> result = new ArrayList<>();
    for (SysUser user : users) {
      Map<String, Object> item = new LinkedHashMap<>();
      item.put("id", user.getId());
      item.put("name", user.getName());
      item.put("employeeNo", user.getEmployeeNo());
      Department dept = user.getDepartmentId() == null ? null : departments.get(user.getDepartmentId());
      item.put("department", dept == null ? null : dept.getName());
      result.add(item);
    }
    return result;
  }

  @Transactional
  public Map<String, Object> updateProfile(String name, String gender, Integer age,
      String phone, String email, String jobTitle, String specialty, String grade,
      String avatarUrl) {
    Long userId = AuthContext.userId();
    SysUser user = userMapper.selectById(userId);
    if (user == null) {
      throw BizException.notFound("用户不存在");
    }
    userMapper.update(null, Wrappers.<SysUser>lambdaUpdate()
        .eq(SysUser::getId, userId)
        .set(name != null && !name.isBlank(), SysUser::getName, name)
        .set(SysUser::getGender, gender == null || gender.isBlank() ? null : gender)
        .set(SysUser::getAge, age)
        .set(SysUser::getPhone, phone)
        .set(SysUser::getEmail, email)
        .set(jobTitle != null, SysUser::getJobTitle, jobTitle)
        .set(specialty != null, SysUser::getSpecialty, specialty)
        .set(grade != null, SysUser::getGrade, grade)
        .set(avatarUrl != null, SysUser::getAvatarUrl, avatarUrl));
    return detail(userId);
  }
}
