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
 * exam
 */
@Data
@TableName("exam")
public class Exam {

  @TableId(type = IdType.AUTO)
  private Long id;

  /** mock 原始 id，如 exam-1 / past-3 */
  private String extId;

  /** 考试编号 ZK-... / CER-... */
  private String code;

  private String title;

  /** 主办方，如 技术合规部 · 2025Q2 */
  private String organizer;

  /** 往期档案的类型，如 技术晋升认证 */
  private String examType;

  /** 1 草稿 2 未开始 3 进行中 4 已结束 */
  private Integer status;

  private String location;

  /** mock timeWindow 前半段 */
  private LocalDateTime startAt;

  /** mock timeWindow 后半段 */
  private LocalDateTime endAt;

  private Integer durationMinutes;

  private BigDecimal totalScore;

  private BigDecimal passScore;

  /** 双机位监考 */
  private Boolean isDoubleCamera;

  private Long relatedCourseId;

  /** mock 原文（含进度后缀），迁移期保留 */
  private String relatedCourseText;

  /** 缓存：剩余考评机会 */
  private Integer remainingChanceCached;

  /** 缓存：试卷题数，发布时刷新 */
  private Integer questionCountCached;

  /** 缓存：参与人次 */
  private Integer participantCountCached;

  private Long createdBy;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createdAt;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updatedAt;

  @TableLogic(value = "null", delval = "now()")
  private LocalDateTime deletedAt;
}
