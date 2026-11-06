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
 * exam_assignment
 */
@Data
@TableName("exam_assignment")
public class ExamAssignment {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long examId;

  private Long userId;

  /** 1 待参加 2 已完成 3 已逾期 */
  private Integer status;

  private Integer attemptsAllowed;

  private Integer attemptsUsed;

  private LocalDateTime deadlineAt;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createdAt;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updatedAt;
}
