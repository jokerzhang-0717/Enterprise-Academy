package com.joker.emp.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.joker.emp.common.BizException;
import com.joker.emp.common.Numbers;
import com.joker.emp.common.PageResult;
import com.joker.emp.entity.Department;
import com.joker.emp.entity.SysUser;
import com.joker.emp.mapper.DepartmentMapper;
import com.joker.emp.mapper.SysUserMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepartmentService {

  private final DepartmentMapper departmentMapper;
  private final SysUserMapper userMapper;
  private final OperationLogService logService;

  public DepartmentService(DepartmentMapper departmentMapper, SysUserMapper userMapper,
      OperationLogService logService) {
    this.departmentMapper = departmentMapper;
    this.userMapper = userMapper;
    this.logService = logService;
  }

  public PageResult<Map<String, Object>> page(long page, long size, String keyword, Integer status) {
    List<Department> departments = departmentMapper.selectList(Wrappers.<Department>lambdaQuery()
        .orderByAsc(Department::getSortNo)
        .orderByAsc(Department::getId));
    Map<Long, Map<String, Object>> stats = departmentMapper.selectStats().stream()
        .collect(Collectors.toMap(m -> Numbers.toLong(m.get("departmentId")), m -> m));

    List<Map<String, Object>> items = new ArrayList<>();
    for (Department department : departments) {
      if (status != null && !status.equals(department.getStatus())) {
        continue;
      }
      if (keyword != null && !keyword.isBlank()) {
        String kw = keyword.trim().toLowerCase();
        boolean hit = (department.getName() != null && department.getName().toLowerCase().contains(kw))
            || (department.getManagerName() != null && department.getManagerName().toLowerCase().contains(kw));
        if (!hit) {
          continue;
        }
      }
      items.add(toItem(department, stats.get(department.getId())));
    }

    long total = items.size();
    int from = (int) Math.min(total, Math.max(0, (page - 1) * size));
    int to = (int) Math.min(total, from + size);
    return PageResult.of(total, page, size, items.subList(from, to));
  }

  public List<Map<String, Object>> options() {
    return departmentMapper.selectList(Wrappers.<Department>lambdaQuery()
            .orderByAsc(Department::getSortNo)
            .orderByAsc(Department::getId)).stream()
        .map(d -> {
          Map<String, Object> item = new LinkedHashMap<>();
          item.put("id", d.getId());
          item.put("name", d.getName());
          item.put("status", d.getStatus());
          return item;
        }).toList();
  }

  public Map<String, Object> detail(Long id) {
    Department department = requireDepartment(id);
    Map<Long, Map<String, Object>> stats = departmentMapper.selectStats().stream()
        .collect(Collectors.toMap(m -> Numbers.toLong(m.get("departmentId")), m -> m));
    return toItem(department, stats.get(id));
  }

  @Transactional
  public Map<String, Object> create(Department request) {
    validateName(request.getName(), null);
    request.setId(null);
    request.setStatus(request.getStatus() == null ? 1 : request.getStatus());
    if (request.getSortNo() == null) {
      long max = departmentMapper.selectCount(null);
      request.setSortNo((int) max + 1);
    }
    departmentMapper.insert(request);
    logService.record("user", "新增部门 " + request.getName(), "department", request.getId());
    return detail(request.getId());
  }

  @Transactional
  public Map<String, Object> update(Long id, Department request) {
    Department exists = requireDepartment(id);
    if (request.getName() != null && !request.getName().isBlank()) {
      validateName(request.getName(), id);
      exists.setName(request.getName());
    }
    if (request.getManagerName() != null) exists.setManagerName(request.getManagerName());
    if (request.getDescription() != null) exists.setDescription(request.getDescription());
    if (request.getStatus() != null) exists.setStatus(request.getStatus());
    if (request.getSortNo() != null) exists.setSortNo(request.getSortNo());
    departmentMapper.updateById(exists);
    logService.record("user", "更新部门 " + exists.getName(), "department", id);
    return detail(id);
  }

  @Transactional
  public void remove(Long id) {
    Department exists = requireDepartment(id);
    long users = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
        .eq(SysUser::getDepartmentId, id));
    if (users > 0) {
      throw new BizException("该部门下还有 " + users + " 名成员，无法删除");
    }
    departmentMapper.deleteById(id);
    logService.record("user", "删除部门 " + exists.getName(), "department", id);
  }

  @Transactional
  public void updateStatus(Long id, Integer status) {
    Department exists = requireDepartment(id);
    if (status == null || (status != 1 && status != 2)) {
      throw new BizException("部门状态只能是 1 启用 / 2 停用");
    }
    Department update = new Department();
    update.setId(id);
    update.setStatus(status);
    departmentMapper.updateById(update);
    logService.record("user", (status == 1 ? "启用" : "停用") + "部门 " + exists.getName(),
        "department", id);
  }

  private Department requireDepartment(Long id) {
    Department department = departmentMapper.selectById(id);
    if (department == null) {
      throw BizException.notFound("部门不存在");
    }
    return department;
  }

  private void validateName(String name, Long excludeId) {
    if (name == null || name.isBlank()) {
      throw new BizException("部门名称不能为空");
    }
    long count = departmentMapper.selectCount(Wrappers.<Department>lambdaQuery()
        .eq(Department::getName, name.trim())
        .ne(excludeId != null, Department::getId, excludeId));
    if (count > 0) {
      throw new BizException(409, "部门名称已存在");
    }
  }

  private Map<String, Object> toItem(Department department, Map<String, Object> stat) {
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("id", department.getId());
    item.put("extId", department.getExtId());
    item.put("name", department.getName());
    item.put("managerName", department.getManagerName());
    item.put("description", department.getDescription());
    item.put("status", department.getStatus());
    item.put("sortNo", department.getSortNo());
    item.put("createdAt", department.getCreatedAt());
    item.put("learnerCount", stat == null ? 0 : stat.get("learnerCount"));
    item.put("totalStudyHours", stat == null ? 0 : stat.get("totalStudyHours"));
    item.put("learnerCountCached", department.getLearnerCountCached());
    item.put("studyHoursCached", department.getStudyHoursCached());
    return item;
  }
}
