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
 * notification
 */
@Data
@TableName("notification")
public class Notification {

  @TableId(type = IdType.AUTO)
  private Long id;

  /** mock 原始 id，如 msg-1 */
  private String extId;

  /** 收件人 */
  private Long userId;

  /** 1 考试 2 课程 3 考勤 4 公告 5 培训考勤 */
  private Integer type;

  /** 展示用分类名 */
  private String typeLabel;

  /** 1 普通 2 加急 */
  private Integer priority;

  private String title;

  /** 列表预览 */
  private String summary;

  /** 详情正文 */
  private String content;

  /** 详情要点列表 */
  private String bulletPoints;

  private String senderName;

  /** 关联公文号，如 SEC-2025-Q2-0892 */
  private String officialRefNo;

  /** exam / course / attendance / announcement */
  private String refType;

  /** 指向对应业务表主键 */
  private Long refId;

  /** 考试时长 60 分钟 */
  private String durationLabel;

  /** 85分及格 */
  private String passingScoreLabel;

  private Boolean isRead;

  private LocalDateTime readAt;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createdAt;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updatedAt;

  @TableLogic(value = "null", delval = "now()")
  private LocalDateTime deletedAt;
}
