package com.joker.emp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.joker.emp.common.BizException;
import com.joker.emp.common.PageResult;
import com.joker.emp.entity.Course;
import com.joker.emp.entity.CourseCategory;
import com.joker.emp.entity.CourseChapter;
import com.joker.emp.entity.CourseLesson;
import com.joker.emp.entity.Enrollment;
import com.joker.emp.entity.Instructor;
import com.joker.emp.entity.LessonProgress;
import com.joker.emp.mapper.CourseCategoryMapper;
import com.joker.emp.mapper.CourseChapterMapper;
import com.joker.emp.mapper.CourseLessonMapper;
import com.joker.emp.mapper.CourseMapper;
import com.joker.emp.mapper.EnrollmentMapper;
import com.joker.emp.mapper.InstructorMapper;
import com.joker.emp.mapper.LessonProgressMapper;
import com.joker.emp.security.AuthContext;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseService {

  private final CourseMapper courseMapper;
  private final CourseCategoryMapper categoryMapper;
  private final CourseChapterMapper chapterMapper;
  private final CourseLessonMapper lessonMapper;
  private final InstructorMapper instructorMapper;
  private final EnrollmentMapper enrollmentMapper;
  private final LessonProgressMapper lessonProgressMapper;
  private final OperationLogService logService;

  public CourseService(CourseMapper courseMapper, CourseCategoryMapper categoryMapper,
      CourseChapterMapper chapterMapper, CourseLessonMapper lessonMapper,
      InstructorMapper instructorMapper, EnrollmentMapper enrollmentMapper,
      LessonProgressMapper lessonProgressMapper, OperationLogService logService) {
    this.courseMapper = courseMapper;
    this.categoryMapper = categoryMapper;
    this.chapterMapper = chapterMapper;
    this.lessonMapper = lessonMapper;
    this.instructorMapper = instructorMapper;
    this.enrollmentMapper = enrollmentMapper;
    this.lessonProgressMapper = lessonProgressMapper;
    this.logService = logService;
  }

  // ------------------------------------------------------------------ 管理端

  public PageResult<Map<String, Object>> page(long page, long size, String keyword,
      Long categoryId, Integer status) {
    IPage<Course> result = courseMapper.selectPage(new Page<>(page, size),
        Wrappers.<Course>lambdaQuery()
            .eq(categoryId != null, Course::getCategoryId, categoryId)
            .eq(status != null, Course::getStatus, status)
            .and(keyword != null && !keyword.isBlank(), w -> w
                .like(Course::getTitle, keyword)
                .or().like(Course::getSummary, keyword)
                .or().like(Course::getSubcategoryLabel, keyword))
            .orderByDesc(Course::getUpdatedAt)
            .orderByDesc(Course::getId));

    Map<Long, CourseCategory> categories = categoryMapper.selectList(null).stream()
        .collect(Collectors.toMap(CourseCategory::getId, Function.identity()));
    Map<Long, Instructor> instructors = instructorMapper.selectList(null).stream()
        .collect(Collectors.toMap(Instructor::getId, Function.identity()));

    List<Map<String, Object>> records = new ArrayList<>();
    for (Course course : result.getRecords()) {
      records.add(toAdminItem(course, categories.get(course.getCategoryId()),
          course.getInstructorId() == null ? null : instructors.get(course.getInstructorId())));
    }
    return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), records);
  }

  public Map<String, Object> detail(Long id, Long userId) {
    Course course = requireCourse(id);
    Map<String, Object> item = toAdminItem(course,
        course.getCategoryId() == null ? null : categoryMapper.selectById(course.getCategoryId()),
        course.getInstructorId() == null ? null : instructorMapper.selectById(course.getInstructorId()));

    List<CourseChapter> chapters = chapterMapper.selectList(Wrappers.<CourseChapter>lambdaQuery()
        .eq(CourseChapter::getCourseId, id)
        .orderByAsc(CourseChapter::getSortNo)
        .orderByAsc(CourseChapter::getId));
    List<CourseLesson> lessons = chapters.isEmpty() ? List.of()
        : lessonMapper.selectList(Wrappers.<CourseLesson>lambdaQuery()
            .in(CourseLesson::getChapterId,
                chapters.stream().map(CourseChapter::getId).toList())
            .orderByAsc(CourseLesson::getChapterId)
            .orderByAsc(CourseLesson::getSortNo)
            .orderByAsc(CourseLesson::getId));
    Map<Long, LessonProgress> progress = new LinkedHashMap<>();
    Enrollment enrollment = null;
    if (userId != null) {
      enrollment = enrollmentMapper.selectOne(Wrappers.<Enrollment>lambdaQuery()
          .eq(Enrollment::getUserId, userId)
          .eq(Enrollment::getCourseId, id)
          .last("LIMIT 1"));
      if (!lessons.isEmpty()) {
        Map<Long, LessonProgress> map = lessonProgressMapper.selectList(
                Wrappers.<LessonProgress>lambdaQuery()
                    .eq(LessonProgress::getUserId, userId)
                    .in(LessonProgress::getLessonId,
                        lessons.stream().map(CourseLesson::getId).toList()))
            .stream().collect(Collectors.toMap(LessonProgress::getLessonId, Function.identity()));
        progress.putAll(map);
      }
    }

    List<Map<String, Object>> chapterItems = new ArrayList<>();
    for (CourseChapter chapter : chapters) {
      Map<String, Object> ci = new LinkedHashMap<>();
      ci.put("id", chapter.getId());
      ci.put("courseId", chapter.getCourseId());
      ci.put("title", chapter.getTitle());
      ci.put("sortNo", chapter.getSortNo());
      List<Map<String, Object>> lessonItems = new ArrayList<>();
      for (CourseLesson lesson : lessons) {
        if (!chapter.getId().equals(lesson.getChapterId())) {
          continue;
        }
        LessonProgress lp = progress.get(lesson.getId());
        Map<String, Object> li = new LinkedHashMap<>();
        li.put("id", lesson.getId());
        li.put("chapterId", lesson.getChapterId());
        li.put("title", lesson.getTitle());
        li.put("durationSeconds", lesson.getDurationSeconds());
        li.put("videoUrl", lesson.getVideoUrl());
        li.put("sortNo", lesson.getSortNo());
        li.put("completed", lp != null && Boolean.TRUE.equals(lp.getIsCompleted()));
        li.put("watchedSeconds", lp == null ? 0 : lp.getWatchedSeconds());
        li.put("lastStudiedAt", lp == null ? null : lp.getUpdatedAt());
        lessonItems.add(li);
      }
      ci.put("lessons", lessonItems);
      chapterItems.add(ci);
    }
    item.put("chapters", chapterItems);
    item.put("progressPercent", enrollment == null ? 0 : enrollment.getProgressPercent());
    item.put("enrollmentStatus", enrollment == null ? null : enrollment.getStatus());
    item.put("lastStudiedAt", enrollment == null ? null : enrollment.getLastStudiedAt());
    return item;
  }

  @Transactional
  public Map<String, Object> create(CourseSaveRequest request) {
    validateCourse(request);
    Course course = new Course();
    apply(course, request);
    course.setStatus(request.status() == null ? 1 : request.status());
    courseMapper.insert(course);
    logService.record("course", "新增课程 " + course.getTitle(), "course", course.getId());
    return detail(course.getId(), null);
  }

  @Transactional
  public Map<String, Object> update(Long id, CourseSaveRequest request) {
    Course course = requireCourse(id);
    if (request.title() != null && request.title().isBlank()) {
      throw new BizException("课程标题不能为空");
    }
    apply(course, request);
    if (request.status() != null) {
      course.setStatus(request.status());
    }
    courseMapper.updateById(course);
    logService.record("course", "更新课程 " + course.getTitle(), "course", id);
    return detail(id, null);
  }

  @Transactional
  public void remove(Long id) {
    Course course = requireCourse(id);
    courseMapper.deleteById(id);
    logService.record("course", "删除课程 " + course.getTitle(), "course", id);
  }

  @Transactional
  public void updateStatus(Long id, Integer status) {
    if (status == null || status < 1 || status > 3) {
      throw new BizException("课程状态只能是 1 草稿 / 2 已上架 / 3 已下架");
    }
    Course course = requireCourse(id);
    Course update = new Course();
    update.setId(id);
    update.setStatus(status);
    courseMapper.updateById(update);
    String label = status == 1 ? "草稿" : status == 2 ? "上架" : "下架";
    logService.record("course", label + "课程 " + course.getTitle(), "course", id);
  }

  // ------------------------------------------------------------- 章节 / 课时

  @Transactional
  public Map<String, Object> saveChapter(Long courseId, Long chapterId, ChapterSaveRequest request) {
    requireCourse(courseId);
    CourseChapter chapter;
    if (chapterId == null) {
      chapter = new CourseChapter();
      chapter.setCourseId(courseId);
    } else {
      chapter = chapterMapper.selectById(chapterId);
      if (chapter == null || !courseId.equals(chapter.getCourseId())) {
        throw BizException.notFound("章节不存在");
      }
    }
    if (request.title() == null || request.title().isBlank()) {
      throw new BizException("章节标题不能为空");
    }
    chapter.setTitle(request.title());
    chapter.setSortNo(request.sortNo() == null ? nextChapterSort(courseId) : request.sortNo());
    if (chapterId == null) {
      chapterMapper.insert(chapter);
    } else {
      chapterMapper.updateById(chapter);
    }
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("id", chapter.getId());
    item.put("courseId", courseId);
    item.put("title", chapter.getTitle());
    item.put("sortNo", chapter.getSortNo());
    return item;
  }

  @Transactional
  public void removeChapter(Long courseId, Long chapterId) {
    CourseChapter chapter = chapterMapper.selectById(chapterId);
    if (chapter == null || !courseId.equals(chapter.getCourseId())) {
      throw BizException.notFound("章节不存在");
    }
    List<CourseLesson> lessons = lessonMapper.selectList(Wrappers.<CourseLesson>lambdaQuery()
        .eq(CourseLesson::getChapterId, chapterId));
    List<Long> lessonIds = lessons.stream().map(CourseLesson::getId).toList();
    if (!lessonIds.isEmpty()) {
      lessonProgressMapper.delete(Wrappers.<LessonProgress>lambdaQuery()
          .in(LessonProgress::getLessonId, lessonIds));
    }
    lessonMapper.delete(Wrappers.<CourseLesson>lambdaQuery()
        .eq(CourseLesson::getChapterId, chapterId));
    chapterMapper.deleteById(chapterId);
  }

  @Transactional
  public Map<String, Object> saveLesson(Long courseId, Long chapterId, Long lessonId,
      LessonSaveRequest request) {
    CourseChapter chapter = chapterMapper.selectById(chapterId);
    if (chapter == null || !courseId.equals(chapter.getCourseId())) {
      throw BizException.notFound("章节不存在");
    }
    CourseLesson lesson;
    if (lessonId == null) {
      lesson = new CourseLesson();
      lesson.setChapterId(chapterId);
    } else {
      lesson = lessonMapper.selectById(lessonId);
      if (lesson == null || !chapterId.equals(lesson.getChapterId())) {
        throw BizException.notFound("课时不存在");
      }
    }
    if (request.title() == null || request.title().isBlank()) {
      throw new BizException("课时标题不能为空");
    }
    lesson.setTitle(request.title());
    lesson.setDurationSeconds(request.durationSeconds() == null ? 0 : request.durationSeconds());
    lesson.setVideoUrl(request.videoUrl());
    lesson.setSortNo(request.sortNo() == null ? nextLessonSort(chapterId) : request.sortNo());
    if (lessonId == null) {
      lessonMapper.insert(lesson);
    } else {
      lessonMapper.updateById(lesson);
    }
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("id", lesson.getId());
    item.put("chapterId", chapterId);
    item.put("title", lesson.getTitle());
    item.put("durationSeconds", lesson.getDurationSeconds());
    item.put("videoUrl", lesson.getVideoUrl());
    item.put("sortNo", lesson.getSortNo());
    return item;
  }

  @Transactional
  public void removeLesson(Long courseId, Long chapterId, Long lessonId) {
    CourseLesson lesson = lessonMapper.selectById(lessonId);
    if (lesson == null || !chapterId.equals(lesson.getChapterId())) {
      throw BizException.notFound("课时不存在");
    }
    lessonProgressMapper.delete(Wrappers.<LessonProgress>lambdaQuery()
        .eq(LessonProgress::getLessonId, lessonId));
    lessonMapper.deleteById(lessonId);
  }

  // ------------------------------------------------------------- 学员端

  public PageResult<Map<String, Object>> catalog(long page, long size, String keyword,
      String categoryCode, String status) {
    Long userId = AuthContext.userId();
    Long categoryId = null;
    if (categoryCode != null && !categoryCode.isBlank()) {
      CourseCategory category = categoryMapper.selectOne(Wrappers.<CourseCategory>lambdaQuery()
          .eq(CourseCategory::getCode, categoryCode)
          .last("LIMIT 1"));
      if (category == null) {
        return PageResult.of(0, page, size, List.of());
      }
      categoryId = category.getId();
    }

    IPage<Course> result = courseMapper.selectPage(new Page<>(page, size),
        Wrappers.<Course>lambdaQuery()
            .eq(Course::getStatus, 2)
            .eq(categoryId != null, Course::getCategoryId, categoryId)
            .and(keyword != null && !keyword.isBlank(), w -> w
                .like(Course::getTitle, keyword)
                .or().like(Course::getSummary, keyword))
            .orderByDesc(Course::getUpdatedAt)
            .orderByDesc(Course::getId));

    Map<Long, Enrollment> enrollments = enrollmentsOf(userId,
        result.getRecords().stream().map(Course::getId).toList());
    Map<Long, CourseCategory> categories = categoryMapper.selectList(null).stream()
        .collect(Collectors.toMap(CourseCategory::getId, Function.identity()));
    Map<Long, Instructor> instructors = instructorMapper.selectList(null).stream()
        .collect(Collectors.toMap(Instructor::getId, Function.identity()));

    List<Map<String, Object>> records = new ArrayList<>();
    for (Course course : result.getRecords()) {
      Map<String, Object> item = toLearnerItem(course, categories.get(course.getCategoryId()),
          course.getInstructorId() == null ? null : instructors.get(course.getInstructorId()));
      applyEnrollment(item, enrollments.get(course.getId()));
      if (status != null && !status.isBlank()
          && !status.equals(item.get("status"))) {
        continue;
      }
      records.add(item);
    }
    return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), records);
  }

  public Map<String, Object> catalogDetail(Long id) {
    Course course = requireCourse(id);
    if (!Integer.valueOf(2).equals(course.getStatus())) {
      throw BizException.notFound("课程不存在或未上架");
    }
    Map<String, Object> item = detail(id, AuthContext.userId());
    Enrollment enrollment = enrollmentMapper.selectOne(Wrappers.<Enrollment>lambdaQuery()
        .eq(Enrollment::getUserId, AuthContext.userId())
        .eq(Enrollment::getCourseId, id)
        .last("LIMIT 1"));
    applyEnrollment(item, enrollment);
    return item;
  }

  @Transactional
  public Map<String, Object> enroll(Long courseId) {
    Long userId = AuthContext.userId();
    Course course = requireCourse(courseId);
    if (!Integer.valueOf(2).equals(course.getStatus())) {
      throw new BizException("该课程尚未上架");
    }
    Enrollment enrollment = enrollmentMapper.selectOne(Wrappers.<Enrollment>lambdaQuery()
        .eq(Enrollment::getUserId, userId)
        .eq(Enrollment::getCourseId, courseId)
        .last("LIMIT 1"));
    if (enrollment == null) {
      enrollment = new Enrollment();
      enrollment.setUserId(userId);
      enrollment.setCourseId(courseId);
      enrollment.setStatus(2);
      enrollment.setProgressPercent(0);
      enrollment.setCompletedLessonCount(0);
      enrollment.setStudyHours(BigDecimal.ZERO);
      enrollment.setStartedAt(LocalDateTime.now());
      enrollment.setLastStudiedAt(LocalDateTime.now());
      enrollmentMapper.insert(enrollment);
    }
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("enrollmentId", enrollment.getId());
    result.put("courseId", courseId);
    result.put("status", enrollment.getStatus());
    result.put("progressPercent", enrollment.getProgressPercent());
    return result;
  }

  @Transactional
  public Map<String, Object> updateLessonProgress(Long courseId, Long lessonId,
      Integer watchedSeconds, Boolean completed) {
    Long userId = AuthContext.userId();
    CourseLesson lesson = lessonMapper.selectById(lessonId);
    if (lesson == null) {
      throw BizException.notFound("课时不存在");
    }
    CourseChapter chapter = chapterMapper.selectById(lesson.getChapterId());
    if (chapter == null || !courseId.equals(chapter.getCourseId())) {
      throw new BizException("课时不属于该课程");
    }
    Enrollment enrollment = enrollmentMapper.selectOne(Wrappers.<Enrollment>lambdaQuery()
        .eq(Enrollment::getUserId, userId)
        .eq(Enrollment::getCourseId, courseId)
        .last("LIMIT 1"));
    if (enrollment == null) {
      enrollment = new Enrollment();
      enrollment.setUserId(userId);
      enrollment.setCourseId(courseId);
      enrollment.setStatus(2);
      enrollment.setProgressPercent(0);
      enrollment.setCompletedLessonCount(0);
      enrollment.setStudyHours(BigDecimal.ZERO);
      enrollment.setStartedAt(LocalDateTime.now());
      enrollmentMapper.insert(enrollment);
    }

    LessonProgress progress = lessonProgressMapper.selectOne(Wrappers.<LessonProgress>lambdaQuery()
        .eq(LessonProgress::getUserId, userId)
        .eq(LessonProgress::getLessonId, lessonId)
        .last("LIMIT 1"));

    /* 不做最低学习时长校验：学员看多少就记录多少，看完即可标记完成 */
    int safeWatchedSeconds = clampWatchedSeconds(watchedSeconds, lesson);

    if (progress == null) {
      progress = new LessonProgress();
      progress.setUserId(userId);
      progress.setLessonId(lessonId);
      progress.setWatchedSeconds(safeWatchedSeconds);
      progress.setIsCompleted(Boolean.TRUE.equals(completed));
      lessonProgressMapper.insert(progress);
    } else {
      progress.setWatchedSeconds(Math.max(
          progress.getWatchedSeconds() == null ? 0 : progress.getWatchedSeconds(),
          safeWatchedSeconds));
      if (Boolean.TRUE.equals(completed)) {
        progress.setIsCompleted(true);
      }
      lessonProgressMapper.updateById(progress);
    }

    long total = lessonMapper.countByCourse(courseId);
    long done = lessonProgressMapper.selectCount(Wrappers.<LessonProgress>lambdaQuery()
        .eq(LessonProgress::getUserId, userId)
        .eq(LessonProgress::getIsCompleted, true)
        .inSql(LessonProgress::getLessonId,
            "SELECT l.id FROM course_lesson l JOIN course_chapter c ON c.id = l.chapter_id WHERE c.course_id = "
                + courseId));

    enrollment.setCompletedLessonCount((int) done);
    int percent = total == 0 ? 0 : (int) Math.round(done * 100.0 / total);
    enrollment.setProgressPercent(Math.min(100, percent));
    enrollment.setLastStudiedAt(LocalDateTime.now());
    if (percent >= 100) {
      enrollment.setStatus(3);
    } else if (percent > 0) {
      enrollment.setStatus(2);
    }
    enrollmentMapper.updateById(enrollment);

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("courseId", courseId);
    result.put("lessonId", lessonId);
    result.put("completed", progress.getIsCompleted());
    result.put("watchedSeconds", progress.getWatchedSeconds());
    result.put("progressPercent", enrollment.getProgressPercent());
    result.put("completedLessonCount", enrollment.getCompletedLessonCount());
    result.put("totalLessonCount", total);
    result.put("status", enrollment.getStatus());
    return result;
  }

  // ------------------------------------------------------------- 字典

  public List<Map<String, Object>> categories() {
    return categoryMapper.selectList(Wrappers.<CourseCategory>lambdaQuery()
            .orderByAsc(CourseCategory::getSortNo)
            .orderByAsc(CourseCategory::getId)).stream()
        .map(c -> {
          Map<String, Object> item = new LinkedHashMap<>();
          // id 返回分类主键，code 保留业务编码；管理端新建课程不再依赖反查已有课程
          item.put("id", c.getId());
          item.put("code", c.getCode());
          item.put("label", c.getLabel());
          item.put("sortNo", c.getSortNo());
          return item;
        }).toList();
  }

  /** 管理端新建课程时按名称创建分类（已存在则直接返回），返回可提交的分类信息。 */
  @Transactional
  public Map<String, Object> createCategory(String label) {
    String name = label == null ? "" : label.trim();
    if (name.isEmpty()) {
      throw new BizException("分类名称不能为空");
    }
    if (name.length() > 64) {
      throw new BizException("分类名称不能超过 64 个字符");
    }
    CourseCategory existing = categoryMapper.selectOne(
        Wrappers.<CourseCategory>lambdaQuery().eq(CourseCategory::getLabel, name).last("limit 1"));
    if (existing != null) {
      return categoryItem(existing);
    }
    // code 用拼音无关的通用前缀 + 时间戳，保证唯一且不与内置 code 冲突
    String code = "custom-" + System.currentTimeMillis();
    int maxSort = categoryMapper.selectList(Wrappers.<CourseCategory>lambdaQuery())
        .stream().map(CourseCategory::getSortNo).filter(java.util.Objects::nonNull)
        .mapToInt(Integer::intValue).max().orElse(0);
    CourseCategory created = new CourseCategory();
    created.setCode(code);
    created.setLabel(name);
    created.setSortNo(maxSort + 1);
    categoryMapper.insert(created);
    logService.record("course", "创建课程分类：" + name, "course_category", created.getId());
    return categoryItem(created);
  }

  private Map<String, Object> categoryItem(CourseCategory c) {
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("id", c.getId());
    item.put("code", c.getCode());
    item.put("label", c.getLabel());
    item.put("sortNo", c.getSortNo());
    return item;
  }

  public List<Map<String, Object>> instructors() {
    return instructorMapper.selectList(Wrappers.<Instructor>lambdaQuery()
            .orderByAsc(Instructor::getId)).stream()
        .map(this::toInstructorItem).toList();
  }

  @Transactional
  public Map<String, Object> createInstructor(Instructor request) {
    if (request.getName() == null || request.getName().isBlank()) {
      throw new BizException("讲师姓名不能为空");
    }
    request.setId(null);
    instructorMapper.insert(request);
    return toInstructorItem(request);
  }

  @Transactional
  public Map<String, Object> updateInstructor(Long id, Instructor request) {
    Instructor exists = instructorMapper.selectById(id);
    if (exists == null) {
      throw BizException.notFound("讲师不存在");
    }
    if (request.getName() != null) exists.setName(request.getName());
    if (request.getTitle() != null) exists.setTitle(request.getTitle());
    instructorMapper.updateById(exists);
    return toInstructorItem(exists);
  }

  @Transactional
  public void removeInstructor(Long id) {
    Instructor exists = instructorMapper.selectById(id);
    if (exists == null) {
      throw BizException.notFound("讲师不存在");
    }
    long used = courseMapper.selectCount(Wrappers.<Course>lambdaQuery()
        .eq(Course::getInstructorId, id));
    if (used > 0) {
      throw new BizException("该讲师仍有 " + used + " 门课程，无法删除");
    }
    instructorMapper.deleteById(id);
  }

  // ------------------------------------------------------------- 内部

  private Course requireCourse(Long id) {
    Course course = courseMapper.selectById(id);
    if (course == null) {
      throw BizException.notFound("课程不存在");
    }
    return course;
  }

  private void validateCourse(CourseSaveRequest request) {
    if (request.title() == null || request.title().isBlank()) {
      throw new BizException("课程标题不能为空");
    }
    if (request.categoryId() == null) {
      throw new BizException("请选择课程分类");
    }
    if (categoryMapper.selectById(request.categoryId()) == null) {
      throw new BizException("课程分类不存在");
    }
  }

  private void apply(Course course, CourseSaveRequest request) {
    if (request.title() != null) course.setTitle(request.title());
    if (request.categoryId() != null) course.setCategoryId(request.categoryId());
    if (request.subcategoryLabel() != null) course.setSubcategoryLabel(request.subcategoryLabel());
    if (request.instructorId() != null) course.setInstructorId(request.instructorId());
    if (request.thumbnailUrl() != null) course.setThumbnailUrl(request.thumbnailUrl());
    if (request.summary() != null) course.setSummary(request.summary());
  }

  private int nextChapterSort(Long courseId) {
    List<CourseChapter> list = chapterMapper.selectList(Wrappers.<CourseChapter>lambdaQuery()
        .eq(CourseChapter::getCourseId, courseId));
    return list.stream().map(CourseChapter::getSortNo).filter(java.util.Objects::nonNull)
        .max(Comparator.naturalOrder()).orElse(0) + 1;
  }

  private int nextLessonSort(Long chapterId) {
    List<CourseLesson> list = lessonMapper.selectList(Wrappers.<CourseLesson>lambdaQuery()
        .eq(CourseLesson::getChapterId, chapterId));
    return list.stream().map(CourseLesson::getSortNo).filter(java.util.Objects::nonNull)
        .max(Comparator.naturalOrder()).orElse(0) + 1;
  }

  private Map<Long, Enrollment> enrollmentsOf(Long userId, List<Long> courseIds) {
    if (courseIds.isEmpty()) {
      return Map.of();
    }
    return enrollmentMapper.selectList(Wrappers.<Enrollment>lambdaQuery()
            .eq(Enrollment::getUserId, userId)
            .in(Enrollment::getCourseId, courseIds))
        .stream().collect(Collectors.toMap(Enrollment::getCourseId, Function.identity()));
  }

  private void applyEnrollment(Map<String, Object> item, Enrollment enrollment) {
    int progress = enrollment == null || enrollment.getProgressPercent() == null
        ? 0 : enrollment.getProgressPercent();
    Integer enrollStatus = enrollment == null ? null : enrollment.getStatus();
    item.put("progress", progress);
    item.put("progressPercent", progress);
    item.put("completedLessons", enrollment == null || enrollment.getCompletedLessonCount() == null
        ? 0 : enrollment.getCompletedLessonCount());
    item.put("lastStudied", enrollment == null || enrollment.getLastStudiedAt() == null
        ? null : relativeTime(enrollment.getLastStudiedAt()));
    item.put("lastStudiedAt", enrollment == null ? null : enrollment.getLastStudiedAt());
    item.put("studyHours", enrollment == null ? 0 : enrollment.getStudyHours());
    String status = enrollStatus == null || enrollStatus == 1 ? "not_started"
        : enrollStatus == 2 ? "studying" : "completed";
    item.put("status", status);
    item.put("statusLabel", statusLabel(enrollStatus));
    item.put("badge", badgeText(enrollment));
    item.put("badgeType", enrollStatus == null || enrollStatus == 1 ? "urgent"
        : enrollStatus == 2 ? "primary" : "success");
  }

  private String statusLabel(Object status) {
    int value = status instanceof Number number ? number.intValue() : 0;
    return switch (value) {
      case 2 -> "学习中";
      case 3 -> "已完成";
      default -> "未开始";
    };
  }

  private String badgeText(Enrollment enrollment) {
    if (enrollment == null) {
      return "未开始";
    }
    if (Integer.valueOf(3).equals(enrollment.getStatus())) {
      return "已结课";
    }
    if (Integer.valueOf(2).equals(enrollment.getStatus())) {
      return "学习中 · " + enrollment.getProgressPercent() + "%";
    }
    return "未开始";
  }

  /** 观看进度直接记录学员实际上报的秒数，仅在无时长上限时做基本非负处理。 */
  private int clampWatchedSeconds(Integer watchedSeconds, CourseLesson lesson) {
    int value = watchedSeconds == null ? 0 : Math.max(0, watchedSeconds);
    Integer duration = lesson.getDurationSeconds();
    int upperBound = duration == null || duration <= 0 ? 86_400 : duration;
    return Math.min(value, upperBound);
  }

  private String relativeTime(LocalDateTime time) {
    Duration duration = Duration.between(time, LocalDateTime.now());
    long minutes = Math.max(0, duration.toMinutes());
    if (minutes < 60) {
      return minutes + "分钟前";
    }
    if (minutes < 60 * 24) {
      return (minutes / 60) + "小时前";
    }
    return (minutes / (60 * 24)) + "天前";
  }

  private Map<String, Object> toAdminItem(Course course, CourseCategory category,
      Instructor instructor) {
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("id", course.getId());
    item.put("title", course.getTitle());
    item.put("categoryId", course.getCategoryId());
    item.put("category", category == null ? null : category.getCode());
    item.put("categoryCode", category == null ? null : category.getCode());
    item.put("categoryLabel", category == null ? null : category.getLabel());
    item.put("subcategoryLabel", course.getSubcategoryLabel());
    item.put("instructorId", course.getInstructorId());
    item.put("instructor", instructor == null ? null : toInstructorItem(instructor));
    item.put("thumbnail", course.getThumbnailUrl());
    item.put("thumbnailUrl", course.getThumbnailUrl());
    item.put("summary", course.getSummary());
    item.put("statusCode", course.getStatus());
    item.put("status", statusText(course.getStatus()));
    item.put("createdAt", course.getCreatedAt());
    item.put("updatedAt", course.getUpdatedAt());
    return item;
  }

  private Map<String, Object> toLearnerItem(Course course, CourseCategory category,
      Instructor instructor) {
    Map<String, Object> item = toAdminItem(course, category, instructor);
    item.put("instructor", instructor == null ? null : Map.of(
        "name", instructor.getName(),
        "title", instructor.getTitle() == null ? "" : instructor.getTitle()));
    item.put("status", "not_started");
    return item;
  }

  private String statusText(Integer status) {
    if (status == null) {
      return "草稿";
    }
    return switch (status) {
      case 2 -> "已上架";
      case 3 -> "已下架";
      default -> "草稿";
    };
  }

  private Map<String, Object> toInstructorItem(Instructor instructor) {
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("id", instructor.getId());
    item.put("name", instructor.getName());
    item.put("title", instructor.getTitle());
    return item;
  }

  public record CourseSaveRequest(String title, Long categoryId, String subcategoryLabel,
      Long instructorId, String thumbnailUrl, String summary,
      Integer status) {
  }

  public record ChapterSaveRequest(String title, Integer sortNo) {
  }

  public record LessonSaveRequest(String title, Integer durationSeconds, String videoUrl,
      Integer sortNo) {
  }
}
