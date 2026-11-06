package com.joker.emp.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * course_category
 */
@Data
@TableName("course_category")
public class CourseCategory {

  @TableId(type = IdType.AUTO)
  private Long id;

  /** core / tech / product / leadership / compliance */
  private String code;

  /** 通识素养 / 技术研发 ... */
  private String label;

  private Integer sortNo;
}
