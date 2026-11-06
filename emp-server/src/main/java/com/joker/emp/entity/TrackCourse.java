package com.joker.emp.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * track_course（复合主键 track_id + course_id，不走 BaseMapper 的单主键能力）
 */
@Data
@TableName("track_course")
public class TrackCourse {

  private Long trackId;

  private Long courseId;

  private Integer sortNo;
}
