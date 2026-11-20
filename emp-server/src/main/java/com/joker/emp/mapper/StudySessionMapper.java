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
}
