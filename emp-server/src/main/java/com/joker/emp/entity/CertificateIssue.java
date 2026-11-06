package com.joker.emp.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * certificate_issue
 */
@Data
@TableName("certificate_issue")
public class CertificateIssue {

  @TableId(type = IdType.AUTO)
  private Long id;

  /** mock 原始 id，如 aci-3 */
  private String extId;

  private Long templateId;

  /** 持证人 */
  private Long userId;

  /** 证书编号 */
  private String serialNumber;

  /** NULL = 尚未签发 */
  private LocalDate issueDate;

  /** NULL = 长期有效 */
  private LocalDate expireDate;

  /** 展示文案，如 24 个月 (2027年到期) */
  private String validityLabel;

  /** TOP 3% (卓越) / 98 / 100 / Platinum 徽章 */
  private String rankingScore;

  /** 1 待发放 2 已发放 3 已撤销 */
  private Integer status;

  private String blockchainHash;

  private Long issuedBy;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createdAt;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updatedAt;

  @TableLogic(value = "null", delval = "now()")
  private LocalDateTime deletedAt;
}
