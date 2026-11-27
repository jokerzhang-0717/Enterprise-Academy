package com.joker.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.joker.emp.entity.Enrollment;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EnrollmentMapper extends BaseMapper<Enrollment> {

  @Select("""
      SELECT course_id AS courseId, title, status,
             progress_percent AS progressPercent,
             completed_lesson_count AS completedLessonCount,
             study_hours AS studyHours, last_studied_at AS lastStudiedAt,
             badge_text AS badgeText
      FROM v_course_progress
      WHERE user_id = #{userId}
      ORDER BY last_studied_at DESC
      """)
  List<Map<String, Object>> selectProgress(@Param("userId") Long userId);

  /** 幂等创建选课记录，避免并发请求同时 check-then-insert 触发 uk_enrollment 唯一键冲突。 */
  @Insert("""
      INSERT INTO enrollment (user_id, course_id, status, progress_percent,
                              completed_lesson_count, study_hours, started_at, last_studied_at)
      VALUES (#{userId}, #{courseId}, 2, 0, 0, 0, NOW(), NOW())
      ON DUPLICATE KEY UPDATE id = LAST_INSERT_ID(id)
      """)
  int insertOrIgnore(@Param("userId") Long userId, @Param("courseId") Long courseId);

  @Select("SELECT COALESCE(SUM(study_hours), 0) FROM enrollment WHERE user_id = #{userId}")
  BigDecimal sumStudyHours(@Param("userId") Long userId);
}
