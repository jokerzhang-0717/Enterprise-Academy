package com.joker.emp.service;

import com.joker.emp.entity.OperationLog;
import com.joker.emp.mapper.OperationLogMapper;
import com.joker.emp.security.AuthContext;
import com.joker.emp.security.AuthUser;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class OperationLogService {

  private final OperationLogMapper logMapper;

  public OperationLogService(OperationLogMapper logMapper) {
    this.logMapper = logMapper;
  }

  public void record(String logType, String content, String refType, Long refId) {
    AuthUser current = AuthContext.get();
    OperationLog log = new OperationLog();
    log.setLogType(logType);
    log.setContent(content);
    log.setRefType(refType);
    log.setRefId(refId);
    log.setOccurredAt(LocalDateTime.now());
    if (current != null) {
      log.setOperatorId(current.id());
      log.setOperatorName(current.name());
    } else {
      log.setOperatorName("系统自动");
    }
    logMapper.insert(log);
  }
}
