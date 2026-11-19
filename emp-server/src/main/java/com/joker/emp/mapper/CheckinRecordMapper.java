package com.joker.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.joker.emp.entity.CheckinRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CheckinRecordMapper extends BaseMapper<CheckinRecord> {

  @Select("""
      <script>
      SELECT cr.id, cr.user_id AS userId, u.name AS userName, u.employee_no AS employeeNo,
             u.avatar_url AS avatarUrl,
             d.name AS department,
             cr.checkin_date AS checkinDate, cr.check_in_time AS checkInTime,
             cr.check_out_time AS checkOutTime, cr.status
      FROM checkin_record cr
      JOIN sys_user u ON u.id = cr.user_id
      LEFT JOIN department d ON d.id = u.department_id
      WHERE 1 = 1
      <if test="date != null">AND cr.checkin_date = #{date}</if>
      <if test="status != null">AND cr.status = #{status}</if>
      <if test="keyword != null and keyword != ''">
        AND (u.name LIKE CONCAT('%', #{keyword}, '%')
          OR u.employee_no LIKE CONCAT('%', #{keyword}, '%')
          OR d.name LIKE CONCAT('%', #{keyword}, '%'))
      </if>
      ORDER BY cr.checkin_date DESC, cr.check_in_time DESC, cr.id DESC
      </script>
      """)
  IPage<Map<String, Object>> selectAdminPage(IPage<?> page,
      @Param("date") LocalDate date,
      @Param("status") Integer status,
      @Param("keyword") String keyword);

  @Select("""
      <script>
      SELECT cr.id, cr.checkin_date AS checkinDate, cr.check_in_time AS checkInTime,
             cr.check_out_time AS checkOutTime, cr.status
      FROM checkin_record cr
      WHERE cr.user_id = #{userId}
      <if test="from != null">AND cr.checkin_date &gt;= #{from}</if>
      <if test="to != null">AND cr.checkin_date &lt;= #{to}</if>
      ORDER BY cr.checkin_date DESC
      </script>
      """)
  List<Map<String, Object>> selectMyList(@Param("userId") Long userId,
      @Param("from") LocalDate from, @Param("to") LocalDate to);
}
