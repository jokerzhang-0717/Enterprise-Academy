package com.joker.emp.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * question_option
 */
@Data
@TableName("question_option")
public class QuestionOption {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long questionId;

  /** A/B/C/D */
  private String optionKey;

  private String content;

  /** 选项级解析（错题回顾用） */
  private String explanation;

  private Integer sortNo;
}
