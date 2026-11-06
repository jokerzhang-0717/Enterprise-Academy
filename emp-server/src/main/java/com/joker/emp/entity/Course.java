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
 * course
 */
@Data
@TableName("course")
public class Course {

  @TableId(type = IdType.AUTO)
  private Long id;

  /** mock 原始 id，如 c1 */
  private String extId;

  private String title;

  private Long categoryId;

  /** 卡片二级标签，如 研发架构 */
  private String subcategoryLabel;

  private Long instructorId;

  /** 1 必修 0 选修 */
  private Boolean isRequired;

  /** 难度/性质文案，如 必修课程 / 进阶技术 */
  private String level;

  /** 课程总学时 */
  private BigDecimal totalHours;

  /** 总课时数（缓存，随章节编辑刷新） */
  private Integer totalLessonCount;

  /** 缓存：报名人数（展示用，非 enrollment 行数） */
  private Integer enrollCountCached;

  private String thumbnailUrl;

  /** 一句话简介 */
  private String summary;

  /** 富文本详情 */
  private String description;

  /** 1 草稿 2 已上架 3 已下架 */
  private Integer status;

  private Long createdBy;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createdAt;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updatedAt;

  @TableLogic(value = "null", delval = "now()")
  private LocalDateTime deletedAt;
}
