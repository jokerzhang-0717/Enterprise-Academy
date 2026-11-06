package com.joker.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.joker.emp.entity.Exam;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ExamMapper extends BaseMapper<Exam> {

  @Select("""
      <script>
      SELECT id, code, title, status, pass_score AS passScore,
             question_count AS questionCount, attempt_count AS attemptCount,
             passed_count AS passedCount, avg_score AS avgScore, pass_rate AS passRate,
             participant_count_cached AS participantCountCached
      FROM v_exam_overview
      WHERE 1 = 1
      <if test="keyword != null and keyword != ''">
        AND (title LIKE CONCAT('%', #{keyword}, '%') OR code LIKE CONCAT('%', #{keyword}, '%'))
      </if>
      <if test="status != null">AND status = #{status}</if>
      ORDER BY id DESC
      </script>
      """)
  IPage<Map<String, Object>> selectOverviewPage(IPage<?> page,
      @Param("keyword") String keyword, @Param("status") Integer status);

  @Select("""
      SELECT e.id AS examId, e.code, e.title, e.organizer, e.status, e.location,
             e.start_at AS startAt, e.end_at AS endAt,
             e.duration_minutes AS durationMinutes, e.total_score AS totalScore,
             e.pass_score AS passScore, e.is_double_camera AS isDoubleCamera,
             e.related_course_id AS relatedCourseId, e.related_course_text AS relatedCourseText,
             e.remaining_chance_cached AS remainingChance,
             ast.status AS assignmentStatus, ast.attempts_allowed AS attemptsAllowed,
             ast.attempts_used AS attemptsUsed, ast.deadline_at AS deadlineAt,
             (SELECT COUNT(*) FROM exam_question q
               WHERE q.exam_id = e.id AND q.deleted_at IS NULL) AS questionCount,
             (SELECT a.id FROM exam_attempt a
               WHERE a.exam_id = e.id AND a.user_id = #{userId} AND a.status = 1
               ORDER BY a.id DESC LIMIT 1) AS inProgressAttemptId
      FROM exam_assignment ast
      JOIN exam e ON e.id = ast.exam_id
      WHERE ast.user_id = #{userId} AND e.deleted_at IS NULL
      ORDER BY e.start_at, e.id
      """)
  List<Map<String, Object>> selectAssignments(@Param("userId") Long userId);

  @Select("""
      SELECT a.id AS attemptId, a.exam_id AS examId, e.title, e.code, e.exam_type AS examType,
             a.status, a.started_at AS startedAt, a.submitted_at AS submittedAt,
             a.total_score AS totalScore, a.pass_score AS passScore, a.is_passed AS isPassed,
             a.wrong_count AS wrongCount, a.switch_count AS switchCount
      FROM exam_attempt a
      JOIN exam e ON e.id = a.exam_id
      WHERE a.user_id = #{userId} AND a.status IN (3, 4)
      ORDER BY a.submitted_at DESC
      """)
  List<Map<String, Object>> selectPastAttempts(@Param("userId") Long userId);

  @Select("""
      SELECT pending_count AS pendingCount, in_progress_count AS inProgressCount,
             finished_count AS finishedCount, avg_past_score AS avgPastScore,
             pass_rate AS passRate
      FROM v_exam_metrics
      WHERE user_id = #{userId}
      """)
  Map<String, Object> selectMetrics(@Param("userId") Long userId);
}
