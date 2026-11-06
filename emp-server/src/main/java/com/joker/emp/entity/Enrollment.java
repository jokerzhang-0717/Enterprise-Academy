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
 * enrollment
 */
@Data
@TableName("enrollment")
public class Enrollment {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long userId;

  private Long courseId;

  /** 1 未开始 2 学习中 3 已完成 */
  private Integer status;

  /** 缓存：0-100，由后端按完成课时推进 */
  private Integer progressPercent;

  private Integer completedLessonCount;

  /** 缓存：该门课已投入学时 */
  private BigDecimal studyHours;

  /** 结课成绩 */
  private BigDecimal score;

  private LocalDateTime startedAt;

  /** 断点续学时间 */
  private LocalDateTime lastStudiedAt;

  private LocalDateTime completedAt;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createdAt;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updatedAt;
}
