package com.joker.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.joker.emp.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {

  @Select("""
      SELECT COUNT(*)
      FROM announcement a
      WHERE a.status = 2 AND a.deleted_at IS NULL
        AND NOT EXISTS (
          SELECT 1 FROM announcement_read r
          WHERE r.announcement_id = a.id AND r.user_id = #{userId}
        )
      """)
  long countUnread(@Param("userId") Long userId);
}
