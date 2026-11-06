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
 * attempt_answer
 */
@Data
@TableName("attempt_answer")
public class AttemptAnswer {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long attemptId;

  private Long questionId;

  /** 作答选项，多选按字母序拼接；主观题为空 */
  private String selectedKeys;

  /** 主观题作答内容 */
  private String answerText;

  /** 标记待复查 */
  private Boolean isFlagged;

  private Boolean isCorrect;

  private BigDecimal scoreGained;

  private LocalDateTime answeredAt;
}
