package com.joker.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.joker.emp.entity.AnnouncementRead;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AnnouncementReadMapper extends BaseMapper<AnnouncementRead> {

  /** 一次写入多条已读记录，避免“全部已读”逐条插入。 */
  @Insert("""
      <script>
      INSERT INTO announcement_read (announcement_id, user_id, read_at) VALUES
      <foreach collection="items" item="item" separator=",">
        (#{item.announcementId}, #{item.userId}, #{item.readAt})
      </foreach>
      </script>
      """)
  int insertBatch(@Param("items") List<AnnouncementRead> items);
}
