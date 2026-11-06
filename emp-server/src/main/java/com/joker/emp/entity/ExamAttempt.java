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
 * exam_attempt
 */
@Data
@TableName("exam_attempt")
public class ExamAttempt {

  @TableId(type = IdType.AUTO)
  private Long id;

  /** mock 原始 id，如 past-1 */
  private String extId;

  private Long examId;

  private Long userId;

  private Integer attemptNo;

  /** 1 作答中 2 已交卷待评分 3 已评分 4 系统强制交卷 */
  private Integer status;

  private LocalDateTime startedAt;

  private LocalDateTime submittedAt;

  /** 作答中用于断点续考 */
  private Integer remainingSeconds;

  private BigDecimal objectiveScore;

  private BigDecimal subjectiveScore;

  private BigDecimal totalScore;

  private BigDecimal passScore;

  private Boolean isPassed;

  private Integer wrongCount;

  /** 切屏次数（防作弊） */
  private Integer switchCount;

  private String deviceInfo;

  private LocalDateTime gradedAt;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createdAt;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updatedAt;
}
