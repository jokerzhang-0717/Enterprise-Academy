package com.joker.emp.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.joker.emp.common.BizException;
import com.joker.emp.entity.Exam;
import com.joker.emp.entity.ExamAttempt;
import com.joker.emp.entity.ExamQuestion;
import com.joker.emp.entity.QuestionOption;
import com.joker.emp.mapper.ExamAttemptMapper;
import com.joker.emp.mapper.ExamMapper;
import com.joker.emp.mapper.ExamQuestionMapper;
import com.joker.emp.mapper.QuestionOptionMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestionService {

  private final ExamQuestionMapper questionMapper;
  private final QuestionOptionMapper optionMapper;
  private final ExamMapper examMapper;
  private final ExamAttemptMapper attemptMapper;
  private final OperationLogService logService;

  public QuestionService(ExamQuestionMapper questionMapper, QuestionOptionMapper optionMapper,
      ExamMapper examMapper, ExamAttemptMapper attemptMapper, OperationLogService logService) {
    this.questionMapper = questionMapper;
    this.optionMapper = optionMapper;
    this.examMapper = examMapper;
    this.attemptMapper = attemptMapper;
    this.logService = logService;
  }

  public List<Map<String, Object>> list(Long examId, Integer type, String keyword,
      boolean standalone) {
    String keywordFilter = keyword == null ? null : keyword.trim();
    List<ExamQuestion> questions = questionMapper.selectList(Wrappers.<ExamQuestion>lambdaQuery()
        .eq(standalone, ExamQuestion::getExamId, null)
        .eq(!standalone && examId != null, ExamQuestion::getExamId, examId)
        .eq(type != null, ExamQuestion::getType, type)
        .and(keywordFilter != null && !keywordFilter.isBlank(), w -> w
            .like(ExamQuestion::getTitle, keywordFilter))
        .orderByAsc(ExamQuestion::getQuestionNo)
        .orderByAsc(ExamQuestion::getId));
    Map<Long, List<QuestionOption>> options = optionsOf(
        questions.stream().map(ExamQuestion::getId).toList());
    return questions.stream()
        .map(q -> toItem(q, options.getOrDefault(q.getId(), List.of()), true))
        .toList();
  }

  public Map<String, Object> detail(Long id) {
    ExamQuestion question = requireQuestion(id);
    return toItem(question, optionsOf(List.of(id)).getOrDefault(id, List.of()), true);
  }

  /** 轻量校验题目是否属于指定考试，避免每次保存答案都加载整套题目与选项。 */
  public boolean existsInExam(Long examId, Long questionId) {
    if (examId == null || questionId == null) {
      return false;
    }
    Long count = questionMapper.selectCount(Wrappers.<ExamQuestion>lambdaQuery()
        .eq(ExamQuestion::getId, questionId)
        .eq(ExamQuestion::getExamId, examId));
    return count != null && count > 0;
  }

  @Transactional
  public Map<String, Object> create(QuestionSaveRequest request) {
    validate(request);
    if (request.examId() != null && request.examId() > 0) {
      assertExamEditable(request.examId());
    }
    ExamQuestion question = new ExamQuestion();
    apply(question, request);
    if (question.getQuestionNo() == null || question.getQuestionNo() == 0) {
      question.setQuestionNo(nextQuestionNo(question.getExamId()));
    }
    questionMapper.insert(question);
    saveOptions(question.getId(), request.options());
    refreshExamSnapshot(question.getExamId());
    logService.record("exam", "新增题目：" + truncate(question.getTitle()), "question", question.getId());
    return detail(question.getId());
  }

  @Transactional
  public Map<String, Object> update(Long id, QuestionSaveRequest request) {
    ExamQuestion question = requireQuestion(id);
    Long previousExamId = question.getExamId();
    Long requestedExamId = request.examId() == null ? previousExamId
        : (request.examId() == 0 ? null : request.examId());
    boolean examChanged = !Objects.equals(previousExamId, requestedExamId);
    // 已发布或已有作答的试卷只锁定“换卷”；题干、选项、答案和分值仍可修改，
    // 修改会直接写回该试卷的题目记录，管理端试卷与学员端会同步生效。
    if (examChanged) {
      if (previousExamId != null) {
        assertExamEditable(previousExamId);
      }
      if (requestedExamId != null) {
        assertExamEditable(requestedExamId);
      }
    }
    apply(question, request);
    questionMapper.updateById(question);
    if (request.options() != null) {
      saveOptions(id, request.options());
    }
    if (examChanged) {
      refreshExamSnapshot(previousExamId);
    }
    refreshExamSnapshot(question.getExamId());
    logService.record("exam", "更新题目：" + truncate(question.getTitle()), "question", id);
    return detail(id);
  }

  @Transactional
  public void remove(Long id) {
    ExamQuestion question = requireQuestion(id);
    Long examId = question.getExamId();
    if (examId != null) {
      assertExamEditable(examId);
    }
    optionMapper.delete(Wrappers.<QuestionOption>lambdaQuery()
        .eq(QuestionOption::getQuestionId, id));
    questionMapper.deleteById(id);
    refreshExamSnapshot(examId);
    logService.record("exam", "删除题目：" + truncate(question.getTitle()), "question", id);
  }

  @Transactional
  public void replaceExamQuestions(Long examId, List<Long> questionIds) {
    assertExamEditable(examId);
    List<Long> selectedIds = new ArrayList<>(new LinkedHashSet<>(questionIds));
    if (selectedIds.contains(null)) {
      throw new BizException("题目列表包含无效题目");
    }

    List<ExamQuestion> selected = selectedIds.isEmpty()
        ? List.of() : questionMapper.selectBatchIds(selectedIds);
    if (selected.size() != selectedIds.size()) {
      throw new BizException("部分题目不存在，请刷新题库后重试");
    }
    Map<Long, ExamQuestion> selectedById = selected.stream()
        .collect(Collectors.toMap(ExamQuestion::getId, Function.identity()));
    BigDecimal selectedTotalScore = selected.stream()
        .map(question -> question.getScore() == null ? BigDecimal.ZERO : question.getScore())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    if (selectedTotalScore.compareTo(BigDecimal.valueOf(100)) > 0) {
      throw new BizException("试卷总分不能超过 100 分");
    }

    List<ExamQuestion> current = questionsOf(examId);
    Set<Long> retainedIds = new LinkedHashSet<>();
    int questionNo = 1;
    for (Long selectedId : selectedIds) {
      ExamQuestion source = selectedById.get(selectedId);
      if (Objects.equals(source.getExamId(), examId)) {
        source.setQuestionNo(questionNo++);
        questionMapper.updateById(source);
        retainedIds.add(source.getId());
      } else {
        ExamQuestion copy = new ExamQuestion();
        copy.setExamId(examId);
        copy.setQuestionNo(questionNo++);
        copy.setType(source.getType());
        copy.setScore(source.getScore());
        copy.setTitle(source.getTitle());
        copy.setCorrectAnswer(source.getCorrectAnswer());
        copy.setReferenceAnswer(source.getReferenceAnswer());
        copy.setAnalysis(source.getAnalysis());
        questionMapper.insert(copy);
        List<QuestionOption> options = optionsOf(List.of(source.getId()))
            .getOrDefault(source.getId(), List.of());
        saveOptions(copy.getId(), options.stream().map(option -> new OptionSaveRequest(
            option.getOptionKey(), option.getContent(), option.getExplanation(),
            option.getSortNo())).toList());
      }
    }

    for (ExamQuestion oldQuestion : current) {
      if (!retainedIds.contains(oldQuestion.getId())) {
        optionMapper.delete(Wrappers.<QuestionOption>lambdaQuery()
            .eq(QuestionOption::getQuestionId, oldQuestion.getId()));
        questionMapper.deleteById(oldQuestion.getId());
      }
    }
  }

  private void assertExamEditable(Long examId) {
    Exam exam = examMapper.selectById(examId);
    if (exam == null) {
      throw BizException.notFound("所属考试不存在");
    }
    long attemptCount = attemptMapper.selectCount(Wrappers.<ExamAttempt>lambdaQuery()
        .eq(ExamAttempt::getExamId, examId));
    if (!Objects.equals(exam.getStatus(), 1) || attemptCount > 0) {
      throw new BizException("考试已发布或已有作答记录，试卷题目已锁定");
    }
  }

  /** 题目变更后同步试卷缓存字段，保证试卷总分、题量与题库修改保持一致。 */
  private void refreshExamSnapshot(Long examId) {
    if (examId == null) {
      return;
    }
    Exam exam = examMapper.selectById(examId);
    if (exam == null) {
      return;
    }
    List<ExamQuestion> questions = questionsOf(examId);
    BigDecimal totalScore = questions.stream()
        .map(question -> question.getScore() == null ? BigDecimal.ZERO : question.getScore())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    Exam update = new Exam();
    update.setId(examId);
    update.setTotalScore(totalScore);
    examMapper.updateById(update);
  }

  /** 学员作答视图：不返回正确答案与解析。 */
  public List<Map<String, Object>> learnerQuestions(Long examId) {
    List<ExamQuestion> questions = questionMapper.selectList(Wrappers.<ExamQuestion>lambdaQuery()
        .eq(ExamQuestion::getExamId, examId)
        .orderByAsc(ExamQuestion::getQuestionNo)
        .orderByAsc(ExamQuestion::getId));
    Map<Long, List<QuestionOption>> options = optionsOf(
        questions.stream().map(ExamQuestion::getId).toList());
    return questions.stream()
        .map(q -> toItem(q, options.getOrDefault(q.getId(), List.of()), false))
        .toList();
  }

  public List<ExamQuestion> questionsOf(Long examId) {
    return questionMapper.selectList(Wrappers.<ExamQuestion>lambdaQuery()
        .eq(ExamQuestion::getExamId, examId)
        .orderByAsc(ExamQuestion::getQuestionNo)
        .orderByAsc(ExamQuestion::getId));
  }

  public Map<Long, List<QuestionOption>> optionsOf(List<Long> questionIds) {
    if (questionIds.isEmpty()) {
      return Map.of();
    }
    return optionMapper.selectList(Wrappers.<QuestionOption>lambdaQuery()
            .in(QuestionOption::getQuestionId, questionIds)
            .orderByAsc(QuestionOption::getSortNo)
            .orderByAsc(QuestionOption::getId))
        .stream().collect(Collectors.groupingBy(QuestionOption::getQuestionId));
  }

  public long countByExam(Long examId) {
    return questionMapper.selectCount(Wrappers.<ExamQuestion>lambdaQuery()
        .eq(ExamQuestion::getExamId, examId));
  }

  private void saveOptions(Long questionId, List<OptionSaveRequest> options) {
    optionMapper.delete(Wrappers.<QuestionOption>lambdaQuery()
        .eq(QuestionOption::getQuestionId, questionId));
    if (options == null) {
      return;
    }
    int sort = 0;
    for (OptionSaveRequest option : options) {
      if (option == null || option.key() == null || option.key().isBlank()) {
        continue;
      }
      QuestionOption entity = new QuestionOption();
      entity.setQuestionId(questionId);
      entity.setOptionKey(option.key().trim().toUpperCase());
      entity.setContent(option.content() == null ? "" : option.content());
      entity.setExplanation(option.explanation());
      entity.setSortNo(option.sortNo() == null ? sort : option.sortNo());
      optionMapper.insert(entity);
      sort++;
    }
  }

  private ExamQuestion requireQuestion(Long id) {
    ExamQuestion question = questionMapper.selectById(id);
    if (question == null) {
      throw BizException.notFound("题目不存在");
    }
    return question;
  }

  private void validate(QuestionSaveRequest request) {
    if (request.title() == null || request.title().isBlank()) {
      throw new BizException("题干不能为空");
    }
    if (request.type() == null || request.type() < 1 || request.type() > 4) {
      throw new BizException("题型只能是 1 单选 / 2 多选 / 3 判断 / 4 应用简答");
    }
    if (request.type() <= 3
        && (request.correctAnswer() == null || request.correctAnswer().isBlank())) {
      throw new BizException("客观题必须设置正确答案");
    }
    if (request.type() <= 3 && (request.options() == null || request.options().isEmpty())) {
      throw new BizException("客观题至少需要一个选项");
    }
  }

  private void apply(ExamQuestion question, QuestionSaveRequest request) {
    if (request.examId() != null) {
      question.setExamId(request.examId() == 0 ? null : request.examId());
    }
    if (request.questionNo() != null) question.setQuestionNo(request.questionNo());
    if (request.type() != null) question.setType(request.type());
    if (request.score() != null) question.setScore(request.score());
    if (request.title() != null) question.setTitle(request.title());
    if (request.correctAnswer() != null) {
      question.setCorrectAnswer(request.correctAnswer().isBlank()
          ? null : request.correctAnswer().trim().toUpperCase());
    }
    if (request.referenceAnswer() != null) question.setReferenceAnswer(request.referenceAnswer());
    if (request.analysis() != null) question.setAnalysis(request.analysis());
    if (question.getScore() == null) question.setScore(defaultScore(question.getType()));
    if (question.getQuestionNo() == null) question.setQuestionNo(0);
  }

  /**
   * 分值规则：单选 2 分 / 多选 4 分 / 判断 2 分 / 应用题 8 分。
   * 管理端与批量导入未显式给分时按此兜底，保证新建题目与题库口径一致。
   */
  private BigDecimal defaultScore(Integer type) {
    return switch (type == null ? 1 : type) {
      case 2 -> BigDecimal.valueOf(4);
      case 4 -> BigDecimal.valueOf(8);
      default -> BigDecimal.valueOf(2);
    };
  }

  private int nextQuestionNo(Long examId) {
    if (examId == null) {
      return 0;
    }
    List<ExamQuestion> questions = questionsOf(examId);
    return questions.stream().map(ExamQuestion::getQuestionNo)
        .filter(java.util.Objects::nonNull).max(Integer::compareTo).orElse(0) + 1;
  }

  private Map<String, Object> toItem(ExamQuestion question, List<QuestionOption> options,
      boolean includeAnswer) {
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("id", question.getId());
    item.put("examId", question.getExamId());
    item.put("questionNo", question.getQuestionNo());
    item.put("type", typeCode(question.getType()));
    item.put("typeCode", question.getType());
    item.put("typeLabel", typeLabel(question.getType()));
    item.put("score", question.getScore());
    item.put("title", question.getTitle());
    List<Map<String, Object>> optionItems = new ArrayList<>();
    for (QuestionOption option : options) {
      Map<String, Object> oi = new LinkedHashMap<>();
      oi.put("id", option.getId());
      oi.put("key", option.getOptionKey());
      oi.put("text", option.getContent());
      oi.put("content", option.getContent());
      if (includeAnswer) {
        oi.put("explanation", option.getExplanation());
      }
      optionItems.add(oi);
    }
    item.put("options", optionItems);
    if (includeAnswer) {
      item.put("answer", question.getCorrectAnswer());
      item.put("correctAnswer", question.getCorrectAnswer());
      item.put("referenceAnswer", question.getReferenceAnswer());
      item.put("analysis", question.getAnalysis());
    }
    return item;
  }

  private String typeCode(Integer type) {
    return switch (type == null ? 1 : type) {
      case 2 -> "multiple";
      case 3 -> "judgment";
      case 4 -> "application";
      default -> "single";
    };
  }

  private String typeLabel(Integer type) {
    return switch (type == null ? 1 : type) {
      case 2 -> "多选题";
      case 3 -> "判断题";
      case 4 -> "应用/简答";
      default -> "单选题";
    };
  }

  private String truncate(String title) {
    if (title == null) {
      return "";
    }
    return title.length() <= 40 ? title : title.substring(0, 40) + "…";
  }

  public record QuestionSaveRequest(Long examId, Integer questionNo, Integer type,
      BigDecimal score, String title, String correctAnswer, String referenceAnswer,
      String analysis, List<OptionSaveRequest> options) {
  }

  public record OptionSaveRequest(String key, String content, String explanation,
      Integer sortNo) {
  }
}
