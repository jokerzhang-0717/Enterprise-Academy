package com.joker.emp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.joker.emp.common.BizException;
import com.joker.emp.common.Numbers;
import com.joker.emp.common.PageResult;
import com.joker.emp.entity.CheckinRecord;
import com.joker.emp.entity.Course;
import com.joker.emp.entity.SysUser;
import com.joker.emp.mapper.CheckinRecordMapper;
import com.joker.emp.mapper.CourseMapper;
import com.joker.emp.mapper.SysUserMapper;
import com.joker.emp.security.AuthContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckinService {

  private final CheckinRecordMapper checkinMapper;
  private final CourseMapper courseMapper;
  private final SysUserMapper userMapper;
  private final OperationLogService logService;

  public CheckinService(CheckinRecordMapper checkinMapper, CourseMapper courseMapper,
      SysUserMapper userMapper, OperationLogService logService) {
    this.checkinMapper = checkinMapper;
    this.courseMapper = courseMapper;
    this.userMapper = userMapper;
    this.logService = logService;
  }

  public PageResult<Map<String, Object>> page(long page, long size, LocalDate date,
      Integer status, String keyword) {
    IPage<Map<String, Object>> result = checkinMapper.selectAdminPage(new Page<>(page, size),
        date, status, keyword);
    for (Map<String, Object> row : result.getRecords()) {
      row.put("statusLabel", statusText(row.get("status")));
    }
    return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(),
        result.getRecords());
  }

  public List<Map<String, Object>> myList(LocalDate from, LocalDate to) {
    List<Map<String, Object>> rows = checkinMapper.selectMyList(AuthContext.userId(), from, to);
    for (Map<String, Object> row : rows) {
      row.put("statusLabel", statusText(row.get("status")));
    }
    return rows;
  }

  public Map<String, Object> today() {
    LocalDate today = LocalDate.now();
    CheckinRecord record = checkinMapper.selectOne(Wrappers.<CheckinRecord>lambdaQuery()
        .eq(CheckinRecord::getUserId, AuthContext.userId())
        .eq(CheckinRecord::getCheckinDate, today)
        .last("LIMIT 1"));
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("date", today);
    result.put("checkedIn", record != null && record.getCheckInTime() != null);
    result.put("checkedOut", record != null && record.getCheckOutTime() != null);
    if (record != null) {
      result.put("record", toItem(record));
    }
    return result;
  }

  @Transactional
  public Map<String, Object> checkIn(String location, Integer method) {
    Long userId = AuthContext.userId();
    LocalDate today = LocalDate.now();
    CheckinRecord exists = checkinMapper.selectOne(Wrappers.<CheckinRecord>lambdaQuery()
        .eq(CheckinRecord::getUserId, userId)
        .eq(CheckinRecord::getCheckinDate, today)
        .last("LIMIT 1"));
    if (exists != null && exists.getCheckInTime() != null) {
      throw new BizException("今天已经签到过了");
    }
    LocalTime now = LocalTime.now();
    if (exists == null) {
      exists = new CheckinRecord();
      exists.setUserId(userId);
      exists.setCheckinDate(today);
      exists.setCheckInTime(now);
      exists.setMethod(method == null ? 3 : method);
      exists.setStatus(now.isAfter(LocalTime.of(9, 0)) ? 2 : 1);
      exists.setLocation(location == null || location.isBlank() ? "企业总部·移动端定位" : location);
      checkinMapper.insert(exists);
    } else {
      exists.setCheckInTime(now);
      exists.setMethod(method == null ? 3 : method);
      exists.setStatus(now.isAfter(LocalTime.of(9, 0)) ? 2 : 1);
      if (location != null) {
        exists.setLocation(location);
      }
      checkinMapper.updateById(exists);
    }
    logService.record("user", "完成签到", "checkin", exists.getId());
    return toItem(exists);
  }

  @Transactional
  public Map<String, Object> checkOut() {
    LocalDate today = LocalDate.now();
    CheckinRecord record = checkinMapper.selectOne(Wrappers.<CheckinRecord>lambdaQuery()
        .eq(CheckinRecord::getUserId, AuthContext.userId())
        .eq(CheckinRecord::getCheckinDate, today)
        .last("LIMIT 1"));
    if (record == null || record.getCheckInTime() == null) {
      throw new BizException("请先签到");
    }
    if (record.getCheckOutTime() != null) {
      throw new BizException("今天已经签退过了");
    }
    record.setCheckOutTime(LocalTime.now());
    if (LocalTime.now().isBefore(LocalTime.of(18, 0))) {
      record.setStatus(4);
    }
    checkinMapper.updateById(record);
    logService.record("user", "完成签退", "checkin", record.getId());
    return toItem(record);
  }

  @Transactional
  public Map<String, Object> create(CheckinSaveRequest request) {
    if (request.userId() == null || request.checkinDate() == null) {
      throw new BizException("请选择学员与签到日期");
    }
    if (userMapper.selectById(request.userId()) == null) {
      throw BizException.notFound("学员不存在");
    }
    CheckinRecord exists = checkinMapper.selectOne(Wrappers.<CheckinRecord>lambdaQuery()
        .eq(CheckinRecord::getUserId, request.userId())
        .eq(CheckinRecord::getCheckinDate, request.checkinDate())
        .last("LIMIT 1"));
    if (exists != null) {
      throw new BizException(409, "该学员当天已有签到记录");
    }
    CheckinRecord record = new CheckinRecord();
    record.setUserId(request.userId());
    record.setCourseId(request.courseId());
    record.setCheckinDate(request.checkinDate());
    record.setCheckInTime(request.checkInTime());
    record.setCheckOutTime(request.checkOutTime());
    record.setMethod(request.method() == null ? 1 : request.method());
    record.setStatus(request.status() == null
        ? (request.checkInTime() == null ? 3 : 1) : request.status());
    record.setLocation(request.location());
    checkinMapper.insert(record);
    logService.record("user", "补录签到记录", "checkin", record.getId());
    return toItem(record);
  }

  @Transactional
  public Map<String, Object> update(Long id, CheckinSaveRequest request) {
    CheckinRecord record = requireRecord(id);
    if (request.courseId() != null) record.setCourseId(request.courseId());
    if (request.checkinDate() != null) record.setCheckinDate(request.checkinDate());
    if (request.checkInTime() != null) record.setCheckInTime(request.checkInTime());
    if (request.checkOutTime() != null) record.setCheckOutTime(request.checkOutTime());
    if (request.method() != null) record.setMethod(request.method());
    if (request.status() != null) record.setStatus(request.status());
    if (request.location() != null) record.setLocation(request.location());
    checkinMapper.updateById(record);
    logService.record("user", "更新签到记录", "checkin", id);
    return toItem(record);
  }

  @Transactional
  public void remove(Long id) {
    requireRecord(id);
    checkinMapper.deleteById(id);
    logService.record("user", "删除签到记录", "checkin", id);
  }

  public List<Map<String, Object>> stats(LocalDate from, LocalDate to) {
    LocalDate start = from == null ? LocalDate.now().withDayOfMonth(1) : from;
    LocalDate end = to == null ? LocalDate.now() : to;
    return checkinMapper.selectStatusStats(start, end).stream().map(row -> {
      Map<String, Object> item = new LinkedHashMap<>(row);
      item.put("statusLabel", statusText(row.get("status")));
      return item;
    }).toList();
  }

  private CheckinRecord requireRecord(Long id) {
    CheckinRecord record = checkinMapper.selectById(id);
    if (record == null) {
      throw BizException.notFound("签到记录不存在");
    }
    return record;
  }

  private Map<String, Object> toItem(CheckinRecord record) {
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("id", record.getId());
    item.put("userId", record.getUserId());
    item.put("courseId", record.getCourseId());
    item.put("checkinDate", record.getCheckinDate());
    item.put("checkInTime", record.getCheckInTime());
    item.put("checkOutTime", record.getCheckOutTime());
    item.put("method", record.getMethod());
    item.put("methodLabel", methodText(record.getMethod()));
    item.put("status", record.getStatus());
    item.put("statusLabel", statusText(record.getStatus()));
    item.put("location", record.getLocation());
    if (record.getUserId() != null) {
      SysUser user = userMapper.selectById(record.getUserId());
      item.put("userName", user == null ? null : user.getName());
      item.put("employeeNo", user == null ? null : user.getEmployeeNo());
    }
    if (record.getCourseId() != null) {
      Course course = courseMapper.selectById(record.getCourseId());
      item.put("courseTitle", course == null ? null : course.getTitle());
    }
    return item;
  }

  private String methodText(Integer method) {
    if (method == null) {
      return "";
    }
    return switch (method) {
      case 2 -> "工牌刷卡";
      case 3 -> "移动端定位";
      default -> "人脸识别";
    };
  }

  private String statusText(Object rawStatus) {
    Integer status = Numbers.toInteger(rawStatus);
    if (status == null) {
      return "";
    }
    return switch (status) {
      case 2 -> "迟到";
      case 3 -> "缺勤";
      case 4 -> "早退";
      default -> "正常";
    };
  }

  public record CheckinSaveRequest(Long userId, Long courseId, LocalDate checkinDate,
      LocalTime checkInTime, LocalTime checkOutTime, Integer method, Integer status,
      String location) {
  }
}
