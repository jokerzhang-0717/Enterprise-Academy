package com.joker.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.joker.emp.entity.Enrollment;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EnrollmentMapper extends BaseMapper<Enrollment> {

  @Select("""
      SELECT course_id AS courseId, title, total_hours AS totalHours,
             total_lesson_count AS totalLessonCount, status,
             progress_percent AS progressPercent,
             completed_lesson_count AS completedLessonCount,
             study_hours AS studyHours, score, last_studied_at AS lastStudiedAt,
             badge_text AS badgeText
      FROM v_course_progress
      WHERE user_id = #{userId}
      ORDER BY last_studied_at DESC
      """)
  List<Map<String, Object>> selectProgress(@Param("userId") Long userId);

  @Select("SELECT COALESCE(SUM(study_hours), 0) FROM enrollment WHERE user_id = #{userId}")
  BigDecimal sumStudyHours(@Param("userId") Long userId);
}
