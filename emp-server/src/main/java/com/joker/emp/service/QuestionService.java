package com.joker.emp.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.joker.emp.common.BizException;
import com.joker.emp.entity.ExamQuestion;
import com.joker.emp.entity.QuestionOption;
import com.joker.emp.mapper.ExamQuestionMapper;
import com.joker.emp.mapper.QuestionOptionMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestionService {

  private final ExamQuestionMapper questionMapper;
  private final QuestionOptionMapper optionMapper;
  private final OperationLogService logService;

  public QuestionService(ExamQuestionMapper questionMapper, QuestionOptionMapper optionMapper,
      OperationLogService logService) {
    this.questionMapper = questionMapper;
    this.optionMapper = optionMapper;
    this.logService = logService;
  }

  public List<Map<String, Object>> list(Long examId, Integer type, String keyword,
      boolean standalone) {
    List<ExamQuestion> questions = questionMapper.selectList(Wrappers.<ExamQuestion>lambdaQuery()
        .eq(standalone, ExamQuestion::getExamId, null)
        .eq(!standalone && examId != null, ExamQuestion::getExamId, examId)
        .eq(type != null, ExamQuestion::getType, type)
        .and(keyword != null && !keyword.isBlank(), w -> w
            .like(ExamQuestion::getTitle, keyword)
            .or().like(ExamQuestion::getCorrectAnswer, keyword))
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

  @Transactional
  public Map<String, Object> create(QuestionSaveRequest request) {
    validate(request);
    ExamQuestion question = new ExamQuestion();
    apply(question, request);
    if (question.getQuestionNo() == null || question.getQuestionNo() == 0) {
      question.setQuestionNo(nextQuestionNo(request.examId()));
    }
    questionMapper.insert(question);
    saveOptions(question.getId(), request.options());
    logService.record("exam", "新增题目：" + truncate(question.getTitle()), "question", question.getId());
    return detail(question.getId());
  }

  @Transactional
  public Map<String, Object> update(Long id, QuestionSaveRequest request) {
    ExamQuestion question = requireQuestion(id);
    apply(question, request);
    questionMapper.updateById(question);
    if (request.options() != null) {
      saveOptions(id, request.options());
    }
    logService.record("exam", "更新题目：" + truncate(question.getTitle()), "question", id);
    return detail(id);
  }

  @Transactional
  public void remove(Long id) {
    ExamQuestion question = requireQuestion(id);
    optionMapper.delete(Wrappers.<QuestionOption>lambdaQuery()
        .eq(QuestionOption::getQuestionId, id));
    questionMapper.deleteById(id);
    logService.record("exam", "删除题目：" + truncate(question.getTitle()), "question", id);
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
    if (request.examId() != null) question.setExamId(request.examId());
    if (request.questionNo() != null) question.setQuestionNo(request.questionNo());
    if (request.type() != null) question.setType(request.type());
    if (request.score() != null) question.setScore(request.score());
    if (request.title() != null) question.setTitle(request.title());
    if (request.codeFilename() != null) question.setCodeFilename(request.codeFilename());
    if (request.codeRuntime() != null) question.setCodeRuntime(request.codeRuntime());
    if (request.codeContent() != null) question.setCodeContent(request.codeContent());
    if (request.correctAnswer() != null) {
      question.setCorrectAnswer(request.correctAnswer().isBlank()
          ? null : request.correctAnswer().trim().toUpperCase());
    }
    if (request.referenceAnswer() != null) question.setReferenceAnswer(request.referenceAnswer());
    if (request.analysis() != null) question.setAnalysis(request.analysis());
    if (question.getScore() == null) question.setScore(BigDecimal.valueOf(5));
    if (question.getQuestionNo() == null) question.setQuestionNo(0);
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
    item.put("extId", question.getExtId());
    item.put("examId", question.getExamId());
    item.put("questionNo", question.getQuestionNo());
    item.put("type", typeCode(question.getType()));
    item.put("typeCode", question.getType());
    item.put("typeLabel", typeLabel(question.getType()));
    item.put("score", question.getScore());
    item.put("title", question.getTitle());
    item.put("codeFilename", question.getCodeFilename());
    item.put("codeRuntime", question.getCodeRuntime());
    item.put("codeContent", question.getCodeContent());
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
      BigDecimal score, String title, String codeFilename, String codeRuntime,
      String codeContent, String correctAnswer, String referenceAnswer, String analysis,
      List<OptionSaveRequest> options) {
  }

  public record OptionSaveRequest(String key, String content, String explanation,
      Integer sortNo) {
  }
}
