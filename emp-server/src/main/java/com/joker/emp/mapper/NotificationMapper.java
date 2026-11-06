package com.joker.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.joker.emp.entity.Notification;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

  @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = 0 AND deleted_at IS NULL")
  long countUnread(@Param("userId") Long userId);

  @Select("""
      SELECT id, type, type_label AS typeLabel, priority, title, summary,
             sender_name AS senderName, is_read AS isRead, created_at AS createdAt
      FROM notification
      WHERE user_id = #{userId} AND deleted_at IS NULL
      ORDER BY is_read ASC, created_at DESC
      LIMIT #{limit}
      """)
  List<Map<String, Object>> selectRecent(@Param("userId") Long userId, @Param("limit") int limit);

  @Select("""
      SELECT id, notification_id AS notificationId, file_name AS fileName,
             file_size_label AS fileSizeLabel, file_url AS fileUrl,
             source_note AS sourceNote, sort_no AS sortNo
      FROM notification_attachment
      WHERE notification_id = #{notificationId}
      ORDER BY sort_no, id
      """)
  List<Map<String, Object>> selectAttachments(@Param("notificationId") Long notificationId);
}
