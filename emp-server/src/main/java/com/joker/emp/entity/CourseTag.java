package com.joker.emp.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * course_tag（复合主键 course_id + tag_id，不走 BaseMapper 的单主键能力）
 */
@Data
@TableName("course_tag")
public class CourseTag {

  private Long courseId;

  private Long tagId;

  private Integer sortNo;
}
