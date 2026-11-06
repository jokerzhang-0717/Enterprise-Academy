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
 * lesson_progress
 */
@Data
@TableName("lesson_progress")
public class LessonProgress {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long userId;

  private Long lessonId;

  private Boolean isCompleted;

  /** 断点续播位置 */
  private Integer watchedSeconds;

  private LocalDateTime completedAt;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updatedAt;
}
