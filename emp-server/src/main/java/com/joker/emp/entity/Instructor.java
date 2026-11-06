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
 * instructor
 */
@Data
@TableName("instructor")
public class Instructor {

  @TableId(type = IdType.AUTO)
  private Long id;

  /** 讲师姓名或机构名 */
  private String name;

  /** 讲师头衔，如 资深架构师 */
  private String title;

  /** 1 机构讲师（如 集团安全部）0 个人 */
  private Boolean isOrg;

  private String avatarUrl;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createdAt;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updatedAt;
}
