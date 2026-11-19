package com.joker.emp.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * leave_request
 */
@Data
@TableName("leave_request")
public class LeaveRequest {

  @TableId(type = IdType.AUTO)
  private Long id;

  /** 审批流水号 LV-20250518-004 */
  private String flowNo;

  private Long userId;

  /** 项目紧急发版 / 病假 / 因公外出 / 个人事务 */
  private String leaveType;

  private LocalDate startDate;

  /** 单日请假可等于 start_date */
  private LocalDate endDate;

  private BigDecimal leaveDays;

  private String reason;

  /** 1 待审批 2 已通过 3 已驳回 */
  private Integer status;

  private LocalDateTime submittedAt;

  private Long approverId;

  private LocalDateTime approvedAt;

  private String approveRemark;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createdAt;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updatedAt;

  @TableLogic(value = "null", delval = "now()")
  private LocalDateTime deletedAt;
}
