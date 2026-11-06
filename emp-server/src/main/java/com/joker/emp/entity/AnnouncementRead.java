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
 * announcement_read
 */
@Data
@TableName("announcement_read")
public class AnnouncementRead {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long announcementId;

  private Long userId;

  private LocalDateTime readAt;
}
