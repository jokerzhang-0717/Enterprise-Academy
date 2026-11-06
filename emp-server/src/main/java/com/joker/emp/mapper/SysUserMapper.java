package com.joker.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.joker.emp.entity.SysUser;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

  String LEARNER_PROFILE_COLUMNS = """
      user_id AS userId, employee_no AS employeeNo, name, department,
      job_title AS jobTitle, grade, specialty, avatar_url AS avatarUrl,
      role, account_status AS accountStatus, last_active_at AS lastActiveAt,
      learning_hours AS learningHours, completed_courses AS completedCourses,
      studying_courses AS studyingCourses, enrolled_courses AS enrolledCourses,
      certificate_count AS certificateCount, unread_notifications AS unreadNotifications
      """;

  @Select("SELECT " + LEARNER_PROFILE_COLUMNS + " FROM v_learner_profile WHERE user_id = #{userId}")
  Map<String, Object> selectLearnerProfile(@Param("userId") Long userId);

  @Select("""
      <script>
      SELECT user_id AS userId, employee_no AS employeeNo, name, department,
             job_title AS jobTitle, grade, specialty, avatar_url AS avatarUrl,
             role, account_status AS accountStatus, last_active_at AS lastActiveAt,
             learning_hours AS learningHours, completed_courses AS completedCourses,
             studying_courses AS studyingCourses, enrolled_courses AS enrolledCourses,
             certificate_count AS certificateCount, unread_notifications AS unreadNotifications
      FROM v_learner_profile
      WHERE user_id IN
      <foreach collection="ids" item="id" open="(" separator="," close=")">#{id}</foreach>
      </script>
      """)
  List<Map<String, Object>> selectLearnerProfiles(@Param("ids") Collection<Long> ids);

  @Select("""
      SELECT pending_leave_count AS pendingLeaveCount,
             pending_cert_count AS pendingCertCount,
             draft_notice_count AS draftNoticeCount,
             active_exam_count AS activeExamCount
      FROM v_admin_pending
      """)
  Map<String, Object> selectAdminPending();

  @Select("""
      SELECT ym, study_hours AS studyHours
      FROM v_monthly_study_hours
      WHERE user_id = #{userId}
      ORDER BY ym
      """)
  List<Map<String, Object>> selectMonthlyStudyHours(@Param("userId") Long userId);
}
