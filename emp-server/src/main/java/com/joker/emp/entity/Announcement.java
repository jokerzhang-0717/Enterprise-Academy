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
 * announcement
 */
@Data
@TableName("announcement")
public class Announcement {

  @TableId(type = IdType.AUTO)
  private Long id;

  /** mock 原始 id，如 ann-1 */
  private String extId;

  private String title;

  private String content;

  private Boolean isTop;

  /** 1 草稿 2 已发布 */
  private Integer status;

  private Long authorId;

  /** 冗余展示名，支持「系统自动」 */
  private String authorName;

  private LocalDateTime publishAt;

  /** 缓存：阅读量（学员端展示） */
  private Integer viewCountCached;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createdAt;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updatedAt;

  @TableLogic(value = "null", delval = "now()")
  private LocalDateTime deletedAt;
}
