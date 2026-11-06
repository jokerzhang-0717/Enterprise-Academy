package com.joker.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.joker.emp.entity.AttemptAnswer;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AttemptAnswerMapper extends BaseMapper<AttemptAnswer> {

  @Select("""
      SELECT aa.id, aa.attempt_id AS attemptId, aa.question_id AS questionId,
             aa.selected_keys AS selectedKeys, aa.answer_text AS answerText,
             aa.is_flagged AS isFlagged, aa.is_correct AS isCorrect,
             aa.score_gained AS scoreGained, aa.answered_at AS answeredAt,
             q.question_no AS questionNo, q.type, q.score, q.title,
             q.correct_answer AS correctAnswer, q.reference_answer AS referenceAnswer,
             q.analysis
      FROM attempt_answer aa
      JOIN exam_question q ON q.id = aa.question_id
      WHERE aa.attempt_id = #{attemptId}
      ORDER BY q.question_no, q.id
      """)
  List<Map<String, Object>> selectDetail(@Param("attemptId") Long attemptId);
}
