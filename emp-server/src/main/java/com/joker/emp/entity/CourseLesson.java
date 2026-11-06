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
 * course_lesson
 */
@Data
@TableName("course_lesson")
public class CourseLesson {

  @TableId(type = IdType.AUTO)
  private Long id;

  /** mock 原始 id，如 ls-1 */
  private String extId;

  private Long chapterId;

  private String title;

  /** 课时时长（秒），mock 的「22分」折算为 1320 */
  private Integer durationSeconds;

  /** 为空时前端走占位控制条 */
  private String videoUrl;

  private Integer sortNo;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createdAt;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updatedAt;
}
