package com.joker.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.joker.emp.entity.StudySession;
import java.math.BigDecimal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StudySessionMapper extends BaseMapper<StudySession> {

  BigDecimal selectStudyMinutes(@Param("userId") Long userId,
      @Param("courseId") Long courseId);
}
