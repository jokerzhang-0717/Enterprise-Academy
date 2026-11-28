package com.joker.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.joker.emp.entity.CourseLesson;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CourseLessonMapper extends BaseMapper<CourseLesson> {

  long countByCourse(@Param("courseId") Long courseId);
}
