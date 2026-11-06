package com.joker.emp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.joker.emp.common.BizException;
import com.joker.emp.common.PageResult;
import com.joker.emp.entity.Course;
import com.joker.emp.entity.LearningTrack;
import com.joker.emp.entity.TrackCourse;
import com.joker.emp.entity.TrackOutlineItem;
import com.joker.emp.mapper.CourseMapper;
import com.joker.emp.mapper.LearningTrackMapper;
import com.joker.emp.mapper.TrackCourseMapper;
import com.joker.emp.mapper.TrackOutlineItemMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrackService {

  private final LearningTrackMapper trackMapper;
  private final TrackOutlineItemMapper outlineMapper;
  private final TrackCourseMapper trackCourseMapper;
  private final CourseMapper courseMapper;
  private final OperationLogService logService;

  public TrackService(LearningTrackMapper trackMapper, TrackOutlineItemMapper outlineMapper,
      TrackCourseMapper trackCourseMapper, CourseMapper courseMapper,
      OperationLogService logService) {
    this.trackMapper = trackMapper;
    this.outlineMapper = outlineMapper;
    this.trackCourseMapper = trackCourseMapper;
    this.courseMapper = courseMapper;
    this.logService = logService;
  }

  public PageResult<Map<String, Object>> page(long page, long size, String keyword,
      String category, Integer status, boolean onlyPublished) {
    IPage<LearningTrack> result = trackMapper.selectPage(new Page<>(page, size),
        Wrappers.<LearningTrack>lambdaQuery()
            .eq(onlyPublished, LearningTrack::getStatus, 2)
            .eq(!onlyPublished && status != null, LearningTrack::getStatus, status)
            .eq(category != null && !category.isBlank(), LearningTrack::getCategory, category)
            .and(keyword != null && !keyword.isBlank(), w -> w
                .like(LearningTrack::getTitle, keyword)
                .or().like(LearningTrack::getSummary, keyword)
                .or().like(LearningTrack::getCategoryLabel, keyword))
            .orderByAsc(LearningTrack::getSortNo)
            .orderByAsc(LearningTrack::getId));
    List<Map<String, Object>> records = result.getRecords().stream()
        .map(this::toListItem).toList();
    return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), records);
  }

  public List<Map<String, Object>> allPublished() {
    return trackMapper.selectList(Wrappers.<LearningTrack>lambdaQuery()
            .eq(LearningTrack::getStatus, 2)
            .orderByAsc(LearningTrack::getSortNo)
            .orderByAsc(LearningTrack::getId)).stream()
        .map(this::toListItem).toList();
  }

  public Map<String, Object> detail(Long id, boolean onlyPublished) {
    LearningTrack track = requireTrack(id);
    if (onlyPublished && !Integer.valueOf(2).equals(track.getStatus())) {
      throw BizException.notFound("研修通道不存在或未上架");
    }
    Map<String, Object> item = toListItem(track);

    List<TrackOutlineItem> outlineItems = outlineMapper.selectList(
        Wrappers.<TrackOutlineItem>lambdaQuery()
            .eq(TrackOutlineItem::getTrackId, id)
            .orderByAsc(TrackOutlineItem::getSortNo)
            .orderByAsc(TrackOutlineItem::getId));
    List<Map<String, Object>> outline = outlineItems.stream().map(o -> {
      Map<String, Object> entry = new LinkedHashMap<>();
      entry.put("id", o.getId());
      entry.put("title", o.getTitle());
      entry.put("sortNo", o.getSortNo());
      return entry;
    }).toList();
    item.put("outline", outline.stream().map(o -> o.get("title")).toList());
    item.put("outlineItems", outline);

    List<Long> courseIds = trackCourseMapper.selectLinkedIds(id);
    if (!courseIds.isEmpty()) {
      Map<Long, Course> courses = courseMapper.selectBatchIds(courseIds).stream()
          .collect(Collectors.toMap(Course::getId, Function.identity()));
      List<Map<String, Object>> courseItems = new ArrayList<>();
      for (Long courseId : courseIds) {
        Course course = courses.get(courseId);
        if (course == null) {
          continue;
        }
        Map<String, Object> ci = new LinkedHashMap<>();
        ci.put("id", course.getId());
        ci.put("title", course.getTitle());
        ci.put("summary", course.getSummary());
        ci.put("thumbnailUrl", course.getThumbnailUrl());
        ci.put("totalHours", course.getTotalHours());
        ci.put("totalLessonCount", course.getTotalLessonCount());
        ci.put("level", course.getLevel());
        courseItems.add(ci);
      }
      item.put("courses", courseItems);
    } else {
      item.put("courses", List.of());
    }
    return item;
  }

  @Transactional
  public Map<String, Object> create(TrackSaveRequest request) {
    validate(request);
    LearningTrack track = new LearningTrack();
    apply(track, request);
    track.setStatus(request.status() == null ? 1 : request.status());
    track.setSortNo(request.sortNo() == null ? 0 : request.sortNo());
    track.setLevel(request.level() == null ? 2 : request.level());
    track.setLectureCount(request.lectureCount() == null ? 0 : request.lectureCount());
    track.setStudentCount(request.studentCount() == null ? 0 : request.studentCount());
    trackMapper.insert(track);
    saveRelations(track.getId(), request);
    logService.record("course", "新增研修通道 " + track.getTitle(), "track", track.getId());
    return detail(track.getId(), false);
  }

  @Transactional
  public Map<String, Object> update(Long id, TrackSaveRequest request) {
    LearningTrack track = requireTrack(id);
    apply(track, request);
    if (request.status() != null) track.setStatus(request.status());
    if (request.sortNo() != null) track.setSortNo(request.sortNo());
    if (request.level() != null) track.setLevel(request.level());
    if (request.lectureCount() != null) track.setLectureCount(request.lectureCount());
    if (request.studentCount() != null) track.setStudentCount(request.studentCount());
    trackMapper.updateById(track);
    if (request.outline() != null || request.courseIds() != null) {
      saveRelations(id, request);
    }
    logService.record("course", "更新研修通道 " + track.getTitle(), "track", id);
    return detail(id, false);
  }

  @Transactional
  public void remove(Long id) {
    LearningTrack track = requireTrack(id);
    trackMapper.deleteById(id);
    logService.record("course", "删除研修通道 " + track.getTitle(), "track", id);
  }

  @Transactional
  public void updateStatus(Long id, Integer status) {
    if (status == null || status < 1 || status > 3) {
      throw new BizException("通道状态只能是 1 草稿 / 2 已上架 / 3 已下架");
    }
    requireTrack(id);
    LearningTrack update = new LearningTrack();
    update.setId(id);
    update.setStatus(status);
    trackMapper.updateById(update);
  }

  private void saveRelations(Long trackId, TrackSaveRequest request) {
    if (request.outline() != null) {
      outlineMapper.delete(Wrappers.<TrackOutlineItem>lambdaQuery()
          .eq(TrackOutlineItem::getTrackId, trackId));
      int sort = 0;
      for (String title : request.outline()) {
        if (title == null || title.isBlank()) {
          continue;
        }
        TrackOutlineItem item = new TrackOutlineItem();
        item.setTrackId(trackId);
        item.setTitle(title.trim());
        item.setSortNo(sort++);
        outlineMapper.insert(item);
      }
    }
    if (request.courseIds() != null) {
      trackCourseMapper.deleteByOwner(trackId);
      int sort = 0;
      for (Long courseId : request.courseIds()) {
        if (courseId != null) {
          trackCourseMapper.insert(trackId, courseId, sort++);
        }
      }
    }
  }

  private LearningTrack requireTrack(Long id) {
    LearningTrack track = trackMapper.selectById(id);
    if (track == null) {
      throw BizException.notFound("研修通道不存在");
    }
    return track;
  }

  private void validate(TrackSaveRequest request) {
    if (request.title() == null || request.title().isBlank()) {
      throw new BizException("通道标题不能为空");
    }
  }

  private void apply(LearningTrack track, TrackSaveRequest request) {
    if (request.title() != null) track.setTitle(request.title());
    if (request.category() != null) track.setCategory(request.category());
    if (request.categoryLabel() != null) track.setCategoryLabel(request.categoryLabel());
    if (request.thumbnailUrl() != null) track.setThumbnailUrl(request.thumbnailUrl());
    if (request.summary() != null) track.setSummary(request.summary());
    if (request.description() != null) track.setDescription(request.description());
    if (request.durationLabel() != null) track.setDurationLabel(request.durationLabel());
    if (request.audience() != null) track.setAudience(request.audience());
  }

  private Map<String, Object> toListItem(LearningTrack track) {
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("id", track.getId());
    item.put("extId", track.getExtId());
    item.put("category", track.getCategory());
    item.put("categoryLabel", track.getCategoryLabel());
    item.put("title", track.getTitle());
    item.put("thumbnail", track.getThumbnailUrl());
    item.put("thumbnailUrl", track.getThumbnailUrl());
    item.put("summary", track.getSummary());
    item.put("description", track.getDescription());
    item.put("lectureCount", track.getLectureCount());
    item.put("studentCount", track.getStudentCount());
    item.put("duration", track.getDurationLabel());
    item.put("durationLabel", track.getDurationLabel());
    item.put("level", levelText(track.getLevel()));
    item.put("levelCode", track.getLevel());
    item.put("audience", track.getAudience());
    item.put("statusCode", track.getStatus());
    item.put("status", track.getStatus() != null && track.getStatus() == 2 ? "已上架" : "草稿");
    item.put("sortNo", track.getSortNo());
    item.put("createdAt", track.getCreatedAt());
    item.put("updatedAt", track.getUpdatedAt());
    return item;
  }

  private String levelText(Integer level) {
    if (level == null) {
      return "进阶";
    }
    return switch (level) {
      case 1 -> "入门";
      case 3 -> "高级";
      default -> "进阶";
    };
  }

  public record TrackSaveRequest(String category, String categoryLabel, String title,
      String thumbnailUrl, String summary, String description, Integer lectureCount,
      Integer studentCount, String durationLabel, Integer level, String audience,
      Integer status, Integer sortNo, List<String> outline, List<Long> courseIds) {
  }
}
