package com.joker.emp.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * exam
 */
@Data
@TableName("exam")
public class Exam {

  @TableId(type = IdType.AUTO)
  private Long id;

  /** 考试编号 ZK-... / CER-... */
  private String code;

  private String title;

  /** 主办方，如 技术合规部 · 2025Q2 */
  private String organizer;

  /** 1 草稿 2 未开始 3 进行中 4 已结束 */
  private Integer status;

  /** mock timeWindow 前半段 */
  private LocalDateTime startAt;

  /** mock timeWindow 后半段 */
  private LocalDateTime endAt;

  private Integer durationMinutes;

  private BigDecimal totalScore;

  private BigDecimal passScore;

  private Long createdBy;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createdAt;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updatedAt;

  @TableLogic(value = "null", delval = "now()")
  private LocalDateTime deletedAt;
}
