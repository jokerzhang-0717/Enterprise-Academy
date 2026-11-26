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
 * course
 */
@Data
@TableName("course")
public class Course {

  @TableId(type = IdType.AUTO)
  private Long id;

  private String title;

  private Long categoryId;

  /** 卡片二级标签，如 研发架构 */
  private String subcategoryLabel;

  private Long instructorId;

  private String thumbnailUrl;

  /** 一句话简介 */
  private String summary;

  /** 1 草稿 2 已上架 3 已下架 */
  private Integer status;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createdAt;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updatedAt;

  @TableLogic(value = "null", delval = "now()")
  private LocalDateTime deletedAt;
}
