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
 * learning_track
 */
@Data
@TableName("learning_track")
public class LearningTrack {

  @TableId(type = IdType.AUTO)
  private Long id;

  /** mock 原始 id，如 trk-1 */
  private String extId;

  /** security / tech / leadership */
  private String category;

  private String categoryLabel;

  private String title;

  private String thumbnailUrl;

  private String summary;

  private String description;

  private Integer lectureCount;

  /** mock 的 "1,420" 去千分位后入库 */
  private Integer studentCount;

  /** 约 3 小时 */
  private String durationLabel;

  /** 1 入门 2 进阶 3 高级 */
  private Integer level;

  /** 适合人群 */
  private String audience;

  /** 1 草稿 2 已上架 3 已下架 */
  private Integer status;

  private Integer sortNo;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createdAt;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updatedAt;

  @TableLogic(value = "null", delval = "now()")
  private LocalDateTime deletedAt;
}
