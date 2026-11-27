package com.joker.emp.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * study_session
 */
@Data
@TableName("study_session")
public class StudySession {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long userId;

  private Long courseId;

  private Long lessonId;

  private LocalDateTime startedAt;

  private BigDecimal durationMinutes;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createdAt;
}
