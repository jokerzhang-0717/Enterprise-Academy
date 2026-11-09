package com.joker.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.joker.emp.entity.LeaveRequest;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LeaveRequestMapper extends BaseMapper<LeaveRequest> {

  @Select("""
      <script>
      SELECT lr.id, lr.flow_no AS flowNo, lr.leave_type AS leaveType,
             lr.training_title AS trainingTitle, lr.course_id AS courseId,
             lr.start_date AS startDate, lr.end_date AS endDate, lr.leave_days AS leaveDays,
             lr.reason, lr.status, lr.attachment_url AS attachmentUrl,
             lr.submitted_at AS submittedAt, lr.approver_id AS approverId,
             lr.approved_at AS approvedAt, lr.approve_remark AS approveRemark,
             lr.user_id AS userId, u.name AS userName, u.employee_no AS employeeNo,
             u.avatar_url AS avatarUrl,
             d.name AS department
      FROM leave_request lr
      JOIN sys_user u ON u.id = lr.user_id
      LEFT JOIN department d ON d.id = u.department_id
      WHERE lr.deleted_at IS NULL
      <if test="status != null">AND lr.status = #{status}</if>
      <if test="keyword != null and keyword != ''">
        AND (u.name LIKE CONCAT('%', #{keyword}, '%')
          OR lr.leave_type LIKE CONCAT('%', #{keyword}, '%')
          OR lr.flow_no LIKE CONCAT('%', #{keyword}, '%'))
      </if>
      ORDER BY lr.submitted_at DESC, lr.id DESC
      </script>
      """)
  IPage<Map<String, Object>> selectAdminPage(IPage<?> page,
      @Param("status") Integer status, @Param("keyword") String keyword);

  @Select("""
      <script>
      SELECT lr.id, lr.flow_no AS flowNo, lr.leave_type AS leaveType,
             lr.training_title AS trainingTitle, lr.course_id AS courseId,
             lr.start_date AS startDate, lr.end_date AS endDate, lr.leave_days AS leaveDays,
             lr.reason, lr.status, lr.attachment_url AS attachmentUrl,
             lr.submitted_at AS submittedAt, lr.approver_id AS approverId,
             lr.approved_at AS approvedAt, lr.approve_remark AS approveRemark
      FROM leave_request lr
      WHERE lr.user_id = #{userId} AND lr.deleted_at IS NULL
      <if test="status != null">AND lr.status = #{status}</if>
      ORDER BY lr.submitted_at DESC, lr.id DESC
      </script>
      """)
  List<Map<String, Object>> selectMyList(@Param("userId") Long userId, @Param("status") Integer status);

  @Select("""
      SELECT status, COUNT(*) AS total
      FROM leave_request
      WHERE deleted_at IS NULL
      GROUP BY status
      """)
  List<Map<String, Object>> selectStatusStats();

  /**
   * 当天最新的请假流水号，包含已逻辑删除的记录。
   * flow_no 有唯一约束，若只统计未删除记录会导致撤销后再次提交时流水号冲突。
   */
  @Select("""
      SELECT flow_no
      FROM leave_request
      WHERE flow_no LIKE CONCAT('LV-', #{date}, '-%')
      ORDER BY flow_no DESC
      LIMIT 1
      """)
  String selectLatestFlowNo(@Param("date") String date);
}
