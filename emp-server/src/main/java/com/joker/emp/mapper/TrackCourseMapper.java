package com.joker.emp.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** track_course 复合主键关联表（track_id, course_id），只走显式 SQL。 */
@Mapper
public interface TrackCourseMapper {

  @Select("SELECT course_id FROM track_course WHERE track_id = #{trackId} ORDER BY sort_no, course_id")
  List<Long> selectLinkedIds(@Param("trackId") Long trackId);

  @Insert("INSERT IGNORE INTO track_course (track_id, course_id, sort_no) VALUES (#{trackId}, #{courseId}, #{sortNo})")
  int insert(@Param("trackId") Long trackId, @Param("courseId") Long courseId,
      @Param("sortNo") int sortNo);

  @Delete("DELETE FROM track_course WHERE track_id = #{trackId}")
  int deleteByOwner(@Param("trackId") Long trackId);
}
