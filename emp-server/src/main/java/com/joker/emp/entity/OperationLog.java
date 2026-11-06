package com.joker.emp.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * operation_log
 */
@Data
@TableName("operation_log")
public class OperationLog {

  @TableId(type = IdType.AUTO)
  private Long id;

  /** mock 原始 id，如 act-1 */
  private String extId;

  /** course / user / exam / notice / approve */
  private String logType;

  private String content;

  private Long operatorId;

  /** 冗余展示名，支持「系统自动」 */
  private String operatorName;

  private String refType;

  private Long refId;

  private LocalDateTime occurredAt;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createdAt;
}
