package com.joker.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.joker.emp.entity.CourseLesson;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CourseLessonMapper extends BaseMapper<CourseLesson> {

  @Select("""
      SELECT COUNT(*)
      FROM course_lesson l
      JOIN course_chapter c ON c.id = l.chapter_id
      WHERE c.course_id = #{courseId}
      """)
  long countByCourse(@Param("courseId") Long courseId);
}
