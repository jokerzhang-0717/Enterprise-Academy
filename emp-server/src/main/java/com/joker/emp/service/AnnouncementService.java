package com.joker.emp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.joker.emp.common.BizException;
import com.joker.emp.common.PageResult;
import com.joker.emp.entity.Announcement;
import com.joker.emp.entity.AnnouncementRead;
import com.joker.emp.entity.SysUser;
import com.joker.emp.mapper.AnnouncementMapper;
import com.joker.emp.mapper.AnnouncementReadMapper;
import com.joker.emp.mapper.SysUserMapper;
import com.joker.emp.security.AuthContext;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnnouncementService {

  private final AnnouncementMapper announcementMapper;
  private final AnnouncementReadMapper readMapper;
  private final SysUserMapper userMapper;
  private final OperationLogService logService;

  public AnnouncementService(AnnouncementMapper announcementMapper,
      AnnouncementReadMapper readMapper, SysUserMapper userMapper,
      OperationLogService logService) {
    this.announcementMapper = announcementMapper;
    this.readMapper = readMapper;
    this.userMapper = userMapper;
    this.logService = logService;
  }

  public PageResult<Map<String, Object>> page(long page, long size, String keyword,
      Integer status) {
    IPage<Announcement> result = announcementMapper.selectPage(new Page<>(page, size),
        Wrappers.<Announcement>lambdaQuery()
            .eq(status != null, Announcement::getStatus, status)
            .and(keyword != null && !keyword.isBlank(), w -> w
                .like(Announcement::getTitle, keyword)
                .or().like(Announcement::getContent, keyword))
            .orderByDesc(Announcement::getIsTop)
            .orderByDesc(Announcement::getPublishAt)
            .orderByDesc(Announcement::getId));
    List<Map<String, Object>> records = result.getRecords().stream()
        .map(a -> toItem(a, null)).toList();
    return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), records);
  }

  public Map<String, Object> detail(Long id, boolean markRead) {
    Announcement announcement = requireAnnouncement(id);
    if (markRead) {
      Long userId = AuthContext.userId();
      AnnouncementRead read = readMapper.selectOne(Wrappers.<AnnouncementRead>lambdaQuery()
          .eq(AnnouncementRead::getAnnouncementId, id)
          .eq(AnnouncementRead::getUserId, userId)
          .last("LIMIT 1"));
      if (read == null) {
        read = new AnnouncementRead();
        read.setAnnouncementId(id);
        read.setUserId(userId);
        read.setReadAt(LocalDateTime.now());
        readMapper.insert(read);
        Announcement update = new Announcement();
        update.setId(id);
        update.setViewCountCached((announcement.getViewCountCached() == null
            ? 0 : announcement.getViewCountCached()) + 1);
        announcementMapper.updateById(update);
        announcement.setViewCountCached((announcement.getViewCountCached() == null
            ? 0 : announcement.getViewCountCached()) + 1);
      }
    }
    return toItem(announcement, markRead ? AuthContext.userId() : null);
  }

  public PageResult<Map<String, Object>> publishedPage(long page, long size, String keyword) {
    IPage<Announcement> result = announcementMapper.selectPage(new Page<>(page, size),
        Wrappers.<Announcement>lambdaQuery()
            .eq(Announcement::getStatus, 2)
            .and(keyword != null && !keyword.isBlank(), w -> w
                .like(Announcement::getTitle, keyword)
                .or().like(Announcement::getContent, keyword))
            .orderByDesc(Announcement::getIsTop)
            .orderByDesc(Announcement::getPublishAt)
            .orderByDesc(Announcement::getId));
    Long userId = AuthContext.userId();
    List<Map<String, Object>> records = result.getRecords().stream()
        .map(a -> toItem(a, userId)).toList();
    return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), records);
  }

  public long unreadCount() {
    return announcementMapper.countUnread(AuthContext.userId());
  }

  /** 将当前用户所有已发布公告标记为已读。 */
  @Transactional
  public int markAllRead() {
    Long userId = AuthContext.userId();
    List<Announcement> published = announcementMapper.selectList(
        Wrappers.<Announcement>lambdaQuery().eq(Announcement::getStatus, 2));
    int count = 0;
    for (Announcement announcement : published) {
      long read = readMapper.selectCount(Wrappers.<AnnouncementRead>lambdaQuery()
          .eq(AnnouncementRead::getAnnouncementId, announcement.getId())
          .eq(AnnouncementRead::getUserId, userId));
      if (read > 0) {
        continue;
      }
      AnnouncementRead record = new AnnouncementRead();
      record.setAnnouncementId(announcement.getId());
      record.setUserId(userId);
      record.setReadAt(LocalDateTime.now());
      readMapper.insert(record);
      count++;
    }
    return count;
  }

  @Transactional
  public Map<String, Object> create(AnnouncementSaveRequest request) {
    validate(request);
    Announcement announcement = new Announcement();
    apply(announcement, request);
    SysUser current = userMapper.selectById(AuthContext.userId());
    announcement.setAuthorId(AuthContext.userId());
    announcement.setAuthorName(current == null ? "系统自动" : current.getName());
    announcement.setStatus(request.status() == null ? 1 : request.status());
    announcement.setIsTop(Boolean.TRUE.equals(request.isTop()));
    announcement.setViewCountCached(0);
    if (Integer.valueOf(2).equals(announcement.getStatus())) {
      announcement.setPublishAt(request.publishAt() == null
          ? LocalDateTime.now() : request.publishAt());
    } else {
      announcement.setPublishAt(request.publishAt());
    }
    announcementMapper.insert(announcement);
    logService.record("notice", "发布公告 " + announcement.getTitle(), "announcement",
        announcement.getId());
    return detail(announcement.getId(), false);
  }

  @Transactional
  public Map<String, Object> update(Long id, AnnouncementSaveRequest request) {
    Announcement announcement = requireAnnouncement(id);
    apply(announcement, request);
    if (request.status() != null) {
      announcement.setStatus(request.status());
      if (request.status() == 2 && announcement.getPublishAt() == null) {
        announcement.setPublishAt(LocalDateTime.now());
      }
    }
    if (request.isTop() != null) {
      announcement.setIsTop(request.isTop());
    }
    if (request.publishAt() != null) {
      announcement.setPublishAt(request.publishAt());
    }
    announcementMapper.updateById(announcement);
    logService.record("notice", "更新公告 " + announcement.getTitle(), "announcement", id);
    return detail(id, false);
  }

  @Transactional
  public Map<String, Object> updateStatus(Long id, Integer status) {
    if (status == null || (status != 1 && status != 2)) {
      throw new BizException("公告状态只能是 1 草稿 / 2 已发布");
    }
    Announcement announcement = requireAnnouncement(id);
    announcement.setStatus(status);
    if (status == 2 && announcement.getPublishAt() == null) {
      announcement.setPublishAt(LocalDateTime.now());
    }
    announcementMapper.updateById(announcement);
    logService.record("notice", (status == 2 ? "发布" : "撤回") + "公告 " + announcement.getTitle(),
        "announcement", id);
    return detail(id, false);
  }

  @Transactional
  public Map<String, Object> toggleTop(Long id, Boolean isTop) {
    Announcement announcement = requireAnnouncement(id);
    announcement.setIsTop(Boolean.TRUE.equals(isTop));
    announcementMapper.updateById(announcement);
    return detail(id, false);
  }

  @Transactional
  public void remove(Long id) {
    Announcement announcement = requireAnnouncement(id);
    announcementMapper.deleteById(id);
    logService.record("notice", "删除公告 " + announcement.getTitle(), "announcement", id);
  }

  private Announcement requireAnnouncement(Long id) {
    Announcement announcement = announcementMapper.selectById(id);
    if (announcement == null) {
      throw BizException.notFound("公告不存在");
    }
    return announcement;
  }

  private void validate(AnnouncementSaveRequest request) {
    if (request.title() == null || request.title().isBlank()) {
      throw new BizException("公告标题不能为空");
    }
    if (request.content() == null || request.content().isBlank()) {
      throw new BizException("公告内容不能为空");
    }
  }

  private void apply(Announcement announcement, AnnouncementSaveRequest request) {
    if (request.title() != null) announcement.setTitle(request.title());
    if (request.content() != null) announcement.setContent(request.content());
  }

  private Map<String, Object> toItem(Announcement announcement, Long userId) {
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("id", announcement.getId());
    item.put("extId", announcement.getExtId());
    item.put("title", announcement.getTitle());
    item.put("content", announcement.getContent());
    item.put("isTop", announcement.getIsTop());
    item.put("statusCode", announcement.getStatus());
    item.put("status", announcement.getStatus() != null && announcement.getStatus() == 2
        ? "已发布" : "草稿");
    item.put("authorId", announcement.getAuthorId());
    item.put("authorName", announcement.getAuthorName());
    item.put("publishAt", announcement.getPublishAt());
    item.put("viewCount", announcement.getViewCountCached());
    item.put("createdAt", announcement.getCreatedAt());
    item.put("updatedAt", announcement.getUpdatedAt());
    if (userId != null) {
      long read = readMapper.selectCount(Wrappers.<AnnouncementRead>lambdaQuery()
          .eq(AnnouncementRead::getAnnouncementId, announcement.getId())
          .eq(AnnouncementRead::getUserId, userId));
      item.put("isRead", read > 0);
    }
    return item;
  }

  public record AnnouncementSaveRequest(String title, String content, Boolean isTop,
      Integer status, LocalDateTime publishAt) {
  }
}
