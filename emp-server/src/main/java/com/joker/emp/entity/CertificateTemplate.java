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
 * certificate_template
 */
@Data
@TableName("certificate_template")
public class CertificateTemplate {

  @TableId(type = IdType.AUTO)
  private Long id;

  /** mock 原始 id，如 cert-1 */
  private String extId;

  private String title;

  /** LEVEL 4 · 专家级认证 */
  private String levelLabel;

  /** architecture / compliance / devops / ai */
  private String category;

  private String issueOrg;

  /** 有效期月数，NULL = 长期有效 */
  private Integer validityMonths;

  private String description;

  /** blockchain / security / cloud / ai */
  private String badgeType;

  /** 持证权益 */
  private String privilegeText;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createdAt;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updatedAt;

  @TableLogic(value = "null", delval = "now()")
  private LocalDateTime deletedAt;
}
