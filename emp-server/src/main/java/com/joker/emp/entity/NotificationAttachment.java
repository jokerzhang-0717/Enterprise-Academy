package com.joker.emp.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * notification_attachment
 */
@Data
@TableName("notification_attachment")
public class NotificationAttachment {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long notificationId;

  private String fileName;

  /** 4.8 MB；正式实现建议另存 byte 数值列 */
  private String fileSizeLabel;

  private String fileUrl;

  /** 集团法务合规部编印 */
  private String sourceNote;

  private Integer sortNo;
}
