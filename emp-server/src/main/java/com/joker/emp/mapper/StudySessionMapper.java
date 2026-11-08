package com.joker.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.joker.emp.entity.StudySession;
import java.math.BigDecimal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StudySessionMapper extends BaseMapper<StudySession> {

  @Select("""
      SELECT COALESCE(SUM(duration_minutes), 0)
      FROM study_session
      WHERE user_id = #{userId} AND course_id = #{courseId}
      """)
  BigDecimal selectStudyMinutes(@Param("userId") Long userId,
      @Param("courseId") Long courseId);

  /** 课时累计学习秒数，仅用于服务端完成度判定，不信任客户端上报值。 */
  @Select("""
      SELECT COALESCE(SUM(duration_minutes), 0) * 60
      FROM study_session
      WHERE user_id = #{userId} AND lesson_id = #{lessonId}
      """)
  BigDecimal selectStudySecondsByLesson(@Param("userId") Long userId,
      @Param("lessonId") Long lessonId);
}
