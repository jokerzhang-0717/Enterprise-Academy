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
 * department
 */
@Data
@TableName("department")
public class Department {

  @TableId(type = IdType.AUTO)
  private Long id;

  /** 部门名称 */
  private String name;

  /** 部门负责人姓名 */
  private String managerName;

  /** 部门职责说明 */
  private String description;

  /** 1 启用 2 停用 */
  private Integer status;

  private Integer sortNo;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createdAt;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updatedAt;

  @TableLogic(value = "null", delval = "now()")
  private LocalDateTime deletedAt;
}
