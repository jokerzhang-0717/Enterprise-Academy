package com.joker.emp.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Data;

/**
 * checkin_record
 */
@Data
@TableName("checkin_record")
public class CheckinRecord {

  @TableId(type = IdType.AUTO)
  private Long id;

  /** mock 原始 id，如 ck-1 */
  private String extId;

  private Long userId;

  /** 线下培训场次对应的课程，日常签到为空 */
  private Long courseId;

  private LocalDate checkinDate;

  /** 缺勤为 NULL */
  private LocalTime checkInTime;

  private LocalTime checkOutTime;

  /** 1 人脸识别 2 工牌刷卡 3 移动端定位 */
  private Integer method;

  /** 1 正常 2 迟到 3 缺勤 4 早退 */
  private Integer status;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createdAt;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updatedAt;
}
