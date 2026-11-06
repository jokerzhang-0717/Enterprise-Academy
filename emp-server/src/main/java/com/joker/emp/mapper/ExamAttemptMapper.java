package com.joker.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.joker.emp.entity.ExamAttempt;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ExamAttemptMapper extends BaseMapper<ExamAttempt> {

  @Select("""
      <script>
      SELECT a.id, a.exam_id AS examId, x.title AS examTitle, x.code,
             a.user_id AS userId, u.name AS userName, u.employee_no AS employeeNo,
             d.name AS department, a.attempt_no AS attemptNo, a.status,
             a.started_at AS startedAt, a.submitted_at AS submittedAt,
             a.objective_score AS objectiveScore, a.subjective_score AS subjectiveScore,
             a.total_score AS totalScore, a.pass_score AS passScore, a.is_passed AS isPassed,
             a.wrong_count AS wrongCount, a.switch_count AS switchCount, a.graded_at AS gradedAt
      FROM exam_attempt a
      JOIN exam x ON x.id = a.exam_id
      JOIN sys_user u ON u.id = a.user_id
      LEFT JOIN department d ON d.id = u.department_id
      WHERE 1 = 1
      <if test="examId != null">AND a.exam_id = #{examId}</if>
      <if test="status != null">AND a.status = #{status}</if>
      <if test="keyword != null and keyword != ''">
        AND (u.name LIKE CONCAT('%', #{keyword}, '%')
          OR u.employee_no LIKE CONCAT('%', #{keyword}, '%')
          OR x.title LIKE CONCAT('%', #{keyword}, '%'))
      </if>
      ORDER BY a.id DESC
      </script>
      """)
  IPage<Map<String, Object>> selectAdminPage(IPage<?> page, @Param("examId") Long examId,
      @Param("status") Integer status, @Param("keyword") String keyword);
}
