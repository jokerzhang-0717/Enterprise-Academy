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
 * exam_question
 */
@Data
@TableName("exam_question")
public class ExamQuestion {

  @TableId(type = IdType.AUTO)
  private Long id;

  /** mock 原始 id，如 q-2 */
  private String extId;

  /** 为空 = 未组卷的备用题 */
  private Long examId;

  /** 题号 */
  private Integer questionNo;

  /** 1 单选 2 多选 3 判断 4 应用/简答 */
  private Integer type;

  private BigDecimal score;

  private String title;

  /** 题干附带的代码片段元信息 */
  private String codeFilename;

  private String codeRuntime;

  private String codeContent;

  /** 客观题答案：单选/判断 "A"，多选 "ABD"；主观题为空 */
  private String correctAnswer;

  /** 应用题参考答案/评分要点 */
  private String referenceAnswer;

  /** 整题解析 */
  private String analysis;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createdAt;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updatedAt;

  @TableLogic(value = "null", delval = "now()")
  private LocalDateTime deletedAt;
}
