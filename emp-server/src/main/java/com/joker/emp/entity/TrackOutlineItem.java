package com.joker.emp.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * track_outline_item
 */
@Data
@TableName("track_outline_item")
public class TrackOutlineItem {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long trackId;

  private String title;

  private Integer sortNo;
}
