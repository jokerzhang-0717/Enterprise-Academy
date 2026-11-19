package com.joker.emp.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * sys_user
 */
@Data
@TableName("sys_user")
public class SysUser {

  @TableId(type = IdType.AUTO)
  private Long id;

  /** 工号，登录账号 */
  private String employeeNo;

  /** 口令散列；演示种子为空串（空串时默认口令为 123456） */
  @JsonIgnore
  private String passwordHash;

  private String name;

  private Long departmentId;

  /** 职位，如 高级研发工程师 */
  private String jobTitle;

  private String avatarUrl;

  /** 性别：男 / 女 */
  private String gender;

  private Integer age;

  private String phone;

  private String email;

  /** 1 学员 2 管理员 */
  private Integer role;

  /** 1 正常 2 禁用 */
  private Integer accountStatus;

  /** 最近活跃时间 */
  private LocalDateTime lastActiveAt;

  /** 新员工入职手册阅读完成时间；NULL 表示尚未完成 */
  private LocalDateTime onboardingCompletedAt;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createdAt;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updatedAt;

  @TableLogic(value = "null", delval = "now()")
  private LocalDateTime deletedAt;
}
