package com.joker.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.joker.emp.entity.ExamAssignment;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ExamAssignmentMapper extends BaseMapper<ExamAssignment> {

  @Select("""
      <script>
      SELECT ast.id, ast.exam_id AS examId, ast.user_id AS userId,
             u.name AS userName, u.employee_no AS employeeNo, d.name AS department,
             ast.status, ast.attempts_allowed AS attemptsAllowed,
             ast.attempts_used AS attemptsUsed, ast.deadline_at AS deadlineAt,
             ast.created_at AS createdAt
      FROM exam_assignment ast
      JOIN sys_user u ON u.id = ast.user_id
      LEFT JOIN department d ON d.id = u.department_id
      WHERE ast.exam_id = #{examId}
      <if test="keyword != null and keyword != ''">
        AND (u.name LIKE CONCAT('%', #{keyword}, '%')
          OR u.employee_no LIKE CONCAT('%', #{keyword}, '%'))
      </if>
      ORDER BY ast.id
      </script>
      """)
  IPage<Map<String, Object>> selectByExam(IPage<?> page, @Param("examId") Long examId,
      @Param("keyword") String keyword);

  @Select("SELECT * FROM exam_assignment WHERE exam_id = #{examId}")
  List<ExamAssignment> selectAllByExam(@Param("examId") Long examId);
}
