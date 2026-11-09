package com.joker.emp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joker.emp.common.BizException;
import com.joker.emp.common.PageResult;
import com.joker.emp.entity.Notification;
import com.joker.emp.entity.NotificationAttachment;
import com.joker.emp.entity.SysUser;
import com.joker.emp.mapper.NotificationAttachmentMapper;
import com.joker.emp.mapper.NotificationMapper;
import com.joker.emp.mapper.SysUserMapper;
import com.joker.emp.security.AuthContext;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

  private final NotificationMapper notificationMapper;
  private final NotificationAttachmentMapper attachmentMapper;
  private final SysUserMapper userMapper;
  private final ObjectMapper objectMapper;
  private final OperationLogService logService;

  public NotificationService(NotificationMapper notificationMapper,
      NotificationAttachmentMapper attachmentMapper, SysUserMapper userMapper,
      ObjectMapper objectMapper, OperationLogService logService) {
    this.notificationMapper = notificationMapper;
    this.attachmentMapper = attachmentMapper;
    this.userMapper = userMapper;
    this.objectMapper = objectMapper;
    this.logService = logService;
  }

  public PageResult<Map<String, Object>> page(long page, long size, Integer type, Boolean isRead) {
    IPage<Notification> result = notificationMapper.selectPage(new Page<>(page, size),
        Wrappers.<Notification>lambdaQuery()
            .eq(Notification::getUserId, AuthContext.userId())
            .eq(type != null, Notification::getType, type)
            .eq(isRead != null, Notification::getIsRead, isRead)
            .orderByAsc(Notification::getIsRead)
            .orderByDesc(Notification::getCreatedAt)
            .orderByDesc(Notification::getId));
    List<Map<String, Object>> records = result.getRecords().stream()
        .map(n -> toItem(n, false)).toList();
    return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), records);
  }

  public List<Map<String, Object>> recent(int limit) {
    return notificationMapper.selectRecent(AuthContext.userId(), Math.min(limit, 50));
  }

  public long unreadCount() {
    return notificationMapper.countUnread(AuthContext.userId());
  }

  public Map<String, Object> detail(Long id) {
    Notification notification = requireOwn(id);
    if (!Boolean.TRUE.equals(notification.getIsRead())) {
      notification.setIsRead(true);
      notification.setReadAt(LocalDateTime.now());
      notificationMapper.updateById(notification);
    }
    return toItem(notification, true);
  }

  @Transactional
  public void markRead(Long id, Boolean read) {
    Notification notification = requireOwn(id);
    notification.setIsRead(!Boolean.FALSE.equals(read));
    notification.setReadAt(notification.getIsRead() ? LocalDateTime.now() : null);
    notificationMapper.updateById(notification);
  }

  @Transactional
  public void markAllRead() {
    List<Notification> unread = notificationMapper.selectList(Wrappers.<Notification>lambdaQuery()
        .eq(Notification::getUserId, AuthContext.userId())
        .eq(Notification::getIsRead, false));
    for (Notification notification : unread) {
      notification.setIsRead(true);
      notification.setReadAt(LocalDateTime.now());
      notificationMapper.updateById(notification);
    }
  }

  @Transactional
  public void remove(Long id) {
    requireOwn(id);
    notificationMapper.deleteById(id);
  }

  /** 管理端发送站内信；userIds 为空时发给全部启用学员。 */
  @Transactional
  public int send(SendRequest request) {
    if (request.title() == null || request.title().isBlank()) {
      throw new BizException("通知标题不能为空");
    }
    List<Long> userIds = request.userIds();
    if (userIds == null || userIds.isEmpty()) {
      userIds = userMapper.selectList(Wrappers.<SysUser>lambdaQuery()
              .eq(SysUser::getAccountStatus, 1))
          .stream().map(SysUser::getId).toList();
    }
    String bulletJson = null;
    if (request.bulletPoints() != null && !request.bulletPoints().isEmpty()) {
      try {
        bulletJson = objectMapper.writeValueAsString(request.bulletPoints());
      } catch (Exception ignored) {
        bulletJson = null;
      }
    }
    int count = 0;
    for (Long userId : userIds) {
      Notification notification = new Notification();
      notification.setUserId(userId);
      notification.setType(request.type() == null ? 4 : request.type());
      notification.setTypeLabel(request.typeLabel());
      notification.setPriority(request.priority() == null ? 1 : request.priority());
      notification.setTitle(request.title());
      notification.setSummary(request.summary());
      notification.setContent(request.content());
      notification.setBulletPoints(bulletJson);
      notification.setSenderName(request.senderName() == null ? "系统自动" : request.senderName());
      notification.setOfficialRefNo(request.officialRefNo());
      notification.setRefType(request.refType());
      notification.setRefId(request.refId());
      notification.setDurationLabel(request.durationLabel());
      notification.setPassingScoreLabel(request.passingScoreLabel());
      notification.setIsRead(false);
      notificationMapper.insert(notification);
      count++;
    }
    logService.record("notice", "发送站内信 " + request.title(), "notification", null);
    return count;
  }

  private Notification requireOwn(Long id) {
    Notification notification = notificationMapper.selectById(id);
    if (notification == null) {
      throw BizException.notFound("通知不存在");
    }
    if (!AuthContext.userId().equals(notification.getUserId())) {
      throw BizException.forbidden("无权查看该通知");
    }
    return notification;
  }

  private Map<String, Object> toItem(Notification notification, boolean withDetail) {
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("id", notification.getId());
    item.put("type", notification.getType());
    item.put("typeLabel", notification.getTypeLabel());
    item.put("priority", notification.getPriority());
    item.put("title", notification.getTitle());
    item.put("summary", notification.getSummary());
    item.put("senderName", notification.getSenderName());
    item.put("officialRefNo", notification.getOfficialRefNo());
    item.put("refType", notification.getRefType());
    item.put("refId", notification.getRefId());
    item.put("durationLabel", notification.getDurationLabel());
    item.put("passingScoreLabel", notification.getPassingScoreLabel());
    item.put("isRead", notification.getIsRead());
    item.put("readAt", notification.getReadAt());
    item.put("createdAt", notification.getCreatedAt());
    if (withDetail) {
      item.put("content", notification.getContent());
      item.put("bulletPoints", parseBullets(notification.getBulletPoints()));
      item.put("attachments", attachmentMapper.selectList(
              Wrappers.<NotificationAttachment>lambdaQuery()
                  .eq(NotificationAttachment::getNotificationId, notification.getId())
                  .orderByAsc(NotificationAttachment::getSortNo)
                  .orderByAsc(NotificationAttachment::getId)).stream()
          .map(a -> {
            Map<String, Object> ai = new LinkedHashMap<>();
            ai.put("id", a.getId());
            ai.put("fileName", a.getFileName());
            ai.put("fileSizeLabel", a.getFileSizeLabel());
            ai.put("fileUrl", a.getFileUrl());
            ai.put("sourceNote", a.getSourceNote());
            return ai;
          }).toList());
    }
    return item;
  }

  private Object parseBullets(String json) {
    if (json == null || json.isBlank()) {
      return List.of();
    }
    try {
      return objectMapper.readValue(json, new TypeReference<List<String>>() {
      });
    } catch (Exception ex) {
      return List.of(json);
    }
  }

  public record SendRequest(List<Long> userIds, Integer type, String typeLabel, Integer priority,
      @NotBlank(message = "通知标题不能为空") String title, String summary, String content,
      List<String> bulletPoints, String senderName, String officialRefNo, String refType, Long refId,
      String durationLabel, String passingScoreLabel) {
  }
}
