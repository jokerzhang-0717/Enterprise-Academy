package com.joker.emp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.joker.emp.common.BizException;
import com.joker.emp.common.Numbers;
import com.joker.emp.common.PageResult;
import com.joker.emp.entity.LeaveRequest;
import com.joker.emp.mapper.LeaveRequestMapper;
import com.joker.emp.security.AuthContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    leave.setStartDate(request.startDate());
    leave.setEndDate(request.endDate() == null ? request.startDate() : request.endDate());
    if (request.leaveDays() == null) {
      long days = java.time.temporal.ChronoUnit.DAYS.between(
          leave.getStartDate(), leave.getEndDate()) + 1;
      leave.setLeaveDays(java.math.BigDecimal.valueOf(Math.max(0.5, days)));
    } else {
      leave.setLeaveDays(request.leaveDays());
    }
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
    if (request.startDate() != null) leave.setStartDate(request.startDate());
    if (request.endDate() != null) leave.setEndDate(request.endDate());
    if (request.leaveDays() != null) leave.setLeaveDays(request.leaveDays());
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

  private String nextFlowNo() {
    String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    long todayCount = leaveMapper.selectCount(Wrappers.<LeaveRequest>lambdaQuery()
        .likeRight(LeaveRequest::getFlowNo, "LV-" + date));
    return "LV-" + date + "-" + String.format("%03d", todayCount + 1);
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
