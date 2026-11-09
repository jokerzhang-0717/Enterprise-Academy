package com.joker.emp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.joker.emp.common.BizException;
import com.joker.emp.common.Numbers;
import com.joker.emp.common.PageResult;
import com.joker.emp.entity.LeaveRequest;
import com.joker.emp.mapper.LeaveRequestMapper;
import com.joker.emp.security.AuthContext;
import com.joker.emp.security.AuthUser;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeaveService {

  private final LeaveRequestMapper leaveMapper;
  private final OperationLogService logService;

  public LeaveService(LeaveRequestMapper leaveMapper, OperationLogService logService) {
    this.leaveMapper = leaveMapper;
    this.logService = logService;
  }

  public PageResult<Map<String, Object>> page(long page, long size, Integer status,
      String keyword) {
    IPage<Map<String, Object>> result = leaveMapper.selectAdminPage(new Page<>(page, size),
        status, keyword);
    for (Map<String, Object> row : result.getRecords()) {
      row.put("statusLabel", statusText(row.get("status")));
    }
    return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(),
        result.getRecords());
  }

  public List<Map<String, Object>> myList(Integer status) {
    List<Map<String, Object>> rows = leaveMapper.selectMyList(AuthContext.userId(), status);
    for (Map<String, Object> row : rows) {
      row.put("statusLabel", statusText(row.get("status")));
    }
    return rows;
  }

  public Map<String, Object> detail(Long id) {
    LeaveRequest leave = requireLeave(id);
    AuthUser current = AuthContext.require();
    if (!current.isAdmin() && !current.id().equals(leave.getUserId())) {
      throw BizException.forbidden("无权查看该请假申请");
    }
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("id", leave.getId());
    item.put("flowNo", leave.getFlowNo());
    item.put("userId", leave.getUserId());
    item.put("leaveType", leave.getLeaveType());
    item.put("trainingTitle", leave.getTrainingTitle());
    item.put("courseId", leave.getCourseId());
    item.put("startDate", leave.getStartDate());
    item.put("endDate", leave.getEndDate());
    item.put("leaveDays", leave.getLeaveDays());
    item.put("reason", leave.getReason());
    item.put("status", leave.getStatus());
    item.put("statusLabel", statusText(leave.getStatus()));
    item.put("attachmentUrl", leave.getAttachmentUrl());
    item.put("submittedAt", leave.getSubmittedAt());
    item.put("approverId", leave.getApproverId());
    item.put("approvedAt", leave.getApprovedAt());
    item.put("approveRemark", leave.getApproveRemark());
    return item;
  }

  @Transactional
  public Map<String, Object> submit(LeaveSaveRequest request) {
    if (request.leaveType() == null || request.leaveType().isBlank()) {
      throw new BizException("请选择请假类型");
    }
    if (request.startDate() == null) {
      throw new BizException("请选择请假开始日期");
    }
    if (request.reason() == null || request.reason().isBlank()) {
      throw new BizException("请填写请假事由");
    }
    LeaveRequest leave = new LeaveRequest();
    leave.setUserId(AuthContext.userId());
    leave.setFlowNo(nextFlowNo());
    leave.setLeaveType(request.leaveType());
    leave.setTrainingTitle(request.trainingTitle());
    leave.setCourseId(request.courseId());
    LocalDate start = request.startDate();
    LocalDate end = request.endDate() == null ? start : request.endDate();
    leave.setStartDate(start);
    leave.setEndDate(end);
    leave.setLeaveDays(resolveLeaveDays(start, end, request.leaveDays()));
    leave.setReason(request.reason());
    leave.setStatus(1);
    leave.setAttachmentUrl(request.attachmentUrl());
    leave.setSubmittedAt(LocalDateTime.now());
    leaveMapper.insert(leave);
    logService.record("approve", "提交请假申请 " + leave.getFlowNo(), "leave", leave.getId());
    return detail(leave.getId());
  }

  @Transactional
  public Map<String, Object> update(Long id, LeaveSaveRequest request) {
    LeaveRequest leave = requireLeave(id);
    if (!AuthContext.userId().equals(leave.getUserId())) {
      throw BizException.forbidden("只能修改自己的请假申请");
    }
    if (!Integer.valueOf(1).equals(leave.getStatus())) {
      throw new BizException("已审批的申请不能修改");
    }
    if (request.leaveType() != null) leave.setLeaveType(request.leaveType());
    if (request.trainingTitle() != null) leave.setTrainingTitle(request.trainingTitle());
    if (request.courseId() != null) leave.setCourseId(request.courseId());
    LocalDate start = request.startDate() != null ? request.startDate() : leave.getStartDate();
    LocalDate end = request.endDate() != null ? request.endDate() : leave.getEndDate();
    boolean datesChanged = request.startDate() != null || request.endDate() != null;
    BigDecimal leaveDays;
    if (request.leaveDays() != null || datesChanged) {
      // 日期变化但未显式传入天数时，按新的日期范围重新计算，避免保留过期天数。
      leaveDays = resolveLeaveDays(start, end, request.leaveDays());
    } else {
      // 仅修改其它字段时保留原天数，但仍校验既有区间合法。
      leaveDays = resolveLeaveDays(start, end, leave.getLeaveDays());
    }
    leave.setStartDate(start);
    leave.setEndDate(end);
    leave.setLeaveDays(leaveDays);
    if (request.reason() != null) leave.setReason(request.reason());
    if (request.attachmentUrl() != null) leave.setAttachmentUrl(request.attachmentUrl());
    leaveMapper.updateById(leave);
    return detail(id);
  }

  @Transactional
  public void cancel(Long id) {
    LeaveRequest leave = requireLeave(id);
    if (!AuthContext.userId().equals(leave.getUserId())) {
      throw BizException.forbidden("只能撤销自己的请假申请");
    }
    if (!Integer.valueOf(1).equals(leave.getStatus())) {
      throw new BizException("已审批的申请不能撤销");
    }
    leaveMapper.deleteById(id);
    logService.record("approve", "撤销请假申请 " + leave.getFlowNo(), "leave", id);
  }

  @Transactional
  public Map<String, Object> approve(Long id, Integer status, String remark) {
    if (status == null || (status != 2 && status != 3)) {
      throw new BizException("审批结果只能是 2 通过 / 3 驳回");
    }
    LeaveRequest leave = requireLeave(id);
    if (!Integer.valueOf(1).equals(leave.getStatus())) {
      throw new BizException("该申请已处理");
    }
    leave.setStatus(status);
    leave.setApproverId(AuthContext.userId());
    leave.setApprovedAt(LocalDateTime.now());
    leave.setApproveRemark(remark);
    leaveMapper.updateById(leave);
    logService.record("approve", (status == 2 ? "通过" : "驳回") + "请假申请 " + leave.getFlowNo(),
        "leave", id);
    return detail(id);
  }

  @Transactional
  public void remove(Long id) {
    LeaveRequest leave = requireLeave(id);
    leaveMapper.deleteById(id);
    logService.record("approve", "删除请假申请 " + leave.getFlowNo(), "leave", id);
  }

  public List<Map<String, Object>> stats() {
    return leaveMapper.selectStatusStats().stream().map(row -> {
      Map<String, Object> item = new LinkedHashMap<>(row);
      item.put("statusLabel", statusText(row.get("status")));
      return item;
    }).toList();
  }

  private LeaveRequest requireLeave(Long id) {
    LeaveRequest leave = leaveMapper.selectById(id);
    if (leave == null) {
      throw BizException.notFound("请假申请不存在");
    }
    return leave;
  }

  /**
   * 校验请假区间并按区间兜底计算天数：结束日期不得早于开始日期，
   * 天数必须大于 0 且不能超过日期跨度，避免客户端传入负数 / 超大值。
   */
  private BigDecimal resolveLeaveDays(LocalDate start, LocalDate end, BigDecimal requested) {
    if (start == null) {
      throw new BizException("请选择请假开始日期");
    }
    LocalDate resolvedEnd = end == null ? start : end;
    if (resolvedEnd.isBefore(start)) {
      throw new BizException("请假结束日期不能早于开始日期");
    }
    long span = ChronoUnit.DAYS.between(start, resolvedEnd) + 1;
    if (span > 365) {
      throw new BizException("单次请假不能超过 365 天");
    }
    BigDecimal days = requested == null ? BigDecimal.valueOf(Math.max(0.5, span)) : requested;
    if (days.compareTo(BigDecimal.ZERO) <= 0) {
      throw new BizException("请假天数必须大于 0");
    }
    if (days.compareTo(BigDecimal.valueOf(span)) > 0) {
      throw new BizException("请假天数不能超过所选日期范围");
    }
    return days;
  }

  private String nextFlowNo() {
    String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    String latest = leaveMapper.selectLatestFlowNo(date);
    int next = 1;
    if (latest != null) {
      int dash = latest.lastIndexOf('-');
      if (dash > -1) {
        try {
          next = Integer.parseInt(latest.substring(dash + 1)) + 1;
        } catch (NumberFormatException ignored) {
          next = 1;
        }
      }
    }
    return "LV-" + date + "-" + String.format("%03d", next);
  }

  private String statusText(Object rawStatus) {
    Integer status = Numbers.toInteger(rawStatus);
    if (status == null) {
      return "";
    }
    return switch (status) {
      case 2 -> "已通过";
      case 3 -> "已驳回";
      default -> "待审批";
    };
  }

  public record LeaveSaveRequest(String leaveType, String trainingTitle, Long courseId,
      LocalDate startDate, LocalDate endDate, java.math.BigDecimal leaveDays, String reason,
      String attachmentUrl) {
  }
}
