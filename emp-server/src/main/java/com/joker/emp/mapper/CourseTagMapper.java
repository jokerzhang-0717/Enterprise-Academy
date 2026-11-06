package com.joker.emp.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** course_tag 复合主键关联表（course_id, tag_id），只走显式 SQL。 */
@Mapper
public interface CourseTagMapper {

  @Select("SELECT tag_id FROM course_tag WHERE course_id = #{courseId} ORDER BY sort_no, tag_id")
  List<Long> selectLinkedIds(@Param("courseId") Long courseId);

  @Insert("INSERT IGNORE INTO course_tag (course_id, tag_id, sort_no) VALUES (#{courseId}, #{tagId}, #{sortNo})")
  int insert(@Param("courseId") Long courseId, @Param("tagId") Long tagId, @Param("sortNo") int sortNo);

  @Delete("DELETE FROM course_tag WHERE course_id = #{courseId}")
  int deleteByOwner(@Param("courseId") Long courseId);

  @Select("""
      SELECT t.id, t.name
      FROM course_tag ct
      JOIN tag t ON t.id = ct.tag_id
      WHERE ct.course_id = #{courseId}
      ORDER BY ct.sort_no, t.id
      """)
  List<java.util.Map<String, Object>> selectTags(@Param("courseId") Long courseId);
}
