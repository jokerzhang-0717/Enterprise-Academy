package com.joker.emp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.joker.emp.common.BizException;
import com.joker.emp.common.Numbers;
import com.joker.emp.common.PageResult;
import com.joker.emp.entity.CertificateIssue;
import com.joker.emp.entity.CertificateTemplate;
import com.joker.emp.entity.SysUser;
import com.joker.emp.mapper.CertificateIssueMapper;
import com.joker.emp.mapper.CertificateTemplateMapper;
import com.joker.emp.mapper.SysUserMapper;
import com.joker.emp.security.AuthContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CertificateService {

  private final CertificateIssueMapper issueMapper;
  private final CertificateTemplateMapper templateMapper;
  private final SysUserMapper userMapper;
  private final OperationLogService logService;

  public CertificateService(CertificateIssueMapper issueMapper,
      CertificateTemplateMapper templateMapper, SysUserMapper userMapper,
      OperationLogService logService) {
    this.issueMapper = issueMapper;
    this.templateMapper = templateMapper;
    this.userMapper = userMapper;
    this.logService = logService;
  }

  public PageResult<Map<String, Object>> page(long page, long size, Integer status,
      String keyword) {
    IPage<Map<String, Object>> result = issueMapper.selectAdminPage(new Page<>(page, size),
        status, keyword);
    for (Map<String, Object> row : result.getRecords()) {
      row.put("statusLabel", statusText(row.get("status")));
    }
    return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(),
        result.getRecords());
  }

  public List<Map<String, Object>> myCertificates() {
    return issueMapper.selectMyCertificates(AuthContext.userId()).stream()
        .peek(row -> row.put("statusLabel", "已发放"))
        .toList();
  }

  public Map<String, Object> detail(Long id) {
    CertificateIssue issue = requireIssue(id);
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("id", issue.getId());
    item.put("extId", issue.getExtId());
    item.put("templateId", issue.getTemplateId());
    item.put("userId", issue.getUserId());
    item.put("serialNumber", issue.getSerialNumber());
    item.put("issueDate", issue.getIssueDate());
    item.put("expireDate", issue.getExpireDate());
    item.put("validityLabel", issue.getValidityLabel());
    item.put("rankingScore", issue.getRankingScore());
    item.put("status", issue.getStatus());
    item.put("statusLabel", statusText(issue.getStatus()));
    item.put("blockchainHash", issue.getBlockchainHash());
    item.put("issuedBy", issue.getIssuedBy());
    CertificateTemplate template = templateMapper.selectById(issue.getTemplateId());
    item.put("template", template == null ? null : toTemplateItem(template));
    SysUser user = userMapper.selectById(issue.getUserId());
    if (user != null) {
      item.put("userName", user.getName());
      item.put("employeeNo", user.getEmployeeNo());
    }
    return item;
  }

  @Transactional
  public Map<String, Object> createIssue(IssueSaveRequest request) {
    if (request.templateId() == null || request.userId() == null) {
      throw new BizException("请选择证书模板与持证人");
    }
    CertificateTemplate template = templateMapper.selectById(request.templateId());
    if (template == null) {
      throw BizException.notFound("证书模板不存在");
    }
    if (userMapper.selectById(request.userId()) == null) {
      throw BizException.notFound("持证人不存在");
    }
    String serial = request.serialNumber() == null || request.serialNumber().isBlank()
        ? generateSerial(template) : request.serialNumber().trim();
    long duplicate = issueMapper.selectCount(Wrappers.<CertificateIssue>lambdaQuery()
        .eq(CertificateIssue::getSerialNumber, serial));
    if (duplicate > 0) {
      throw new BizException(409, "证书编号已存在");
    }
    CertificateIssue issue = new CertificateIssue();
    issue.setTemplateId(request.templateId());
    issue.setUserId(request.userId());
    issue.setSerialNumber(serial);
    issue.setStatus(request.status() == null ? 1 : request.status());
    issue.setIssueDate(request.issueDate() == null ? LocalDate.now() : request.issueDate());
    issue.setExpireDate(request.expireDate() == null
        ? expireDate(issue.getIssueDate(), template.getValidityMonths()) : request.expireDate());
    issue.setValidityLabel(request.validityLabel() == null
        ? validityLabel(issue.getIssueDate(), template.getValidityMonths()) : request.validityLabel());
    issue.setRankingScore(request.rankingScore());
    issue.setBlockchainHash(request.blockchainHash() == null
        ? randomHash() : request.blockchainHash());
    issue.setIssuedBy(AuthContext.userId());
    issueMapper.insert(issue);
    logService.record("user", "签发证书 " + serial, "certificate", issue.getId());
    return detail(issue.getId());
  }

  @Transactional
  public Map<String, Object> updateIssue(Long id, IssueSaveRequest request) {
    CertificateIssue issue = requireIssue(id);
    if (request.templateId() != null) issue.setTemplateId(request.templateId());
    if (request.userId() != null) issue.setUserId(request.userId());
    if (request.serialNumber() != null && !request.serialNumber().isBlank()) {
      issue.setSerialNumber(request.serialNumber().trim());
    }
    if (request.issueDate() != null) issue.setIssueDate(request.issueDate());
    if (request.expireDate() != null) issue.setExpireDate(request.expireDate());
    if (request.validityLabel() != null) issue.setValidityLabel(request.validityLabel());
    if (request.rankingScore() != null) issue.setRankingScore(request.rankingScore());
    if (request.status() != null) issue.setStatus(request.status());
    if (request.blockchainHash() != null) issue.setBlockchainHash(request.blockchainHash());
    issueMapper.updateById(issue);
    logService.record("user", "更新证书 " + issue.getSerialNumber(), "certificate", id);
    return detail(id);
  }

  @Transactional
  public Map<String, Object> updateStatus(Long id, Integer status) {
    if (status == null || status < 1 || status > 3) {
      throw new BizException("证书状态只能是 1 待发放 / 2 已发放 / 3 已撤销");
    }
    CertificateIssue issue = requireIssue(id);
    if (status == 2) {
      CertificateTemplate template = templateMapper.selectById(issue.getTemplateId());
      if (issue.getIssueDate() == null) {
        issue.setIssueDate(LocalDate.now());
      }
      if (issue.getExpireDate() == null && template != null) {
        issue.setExpireDate(expireDate(issue.getIssueDate(), template.getValidityMonths()));
        issue.setValidityLabel(validityLabel(issue.getIssueDate(), template.getValidityMonths()));
      }
      issue.setIssuedBy(AuthContext.userId());
      if (issue.getBlockchainHash() == null) {
        issue.setBlockchainHash(randomHash());
      }
    }
    issue.setStatus(status);
    issueMapper.updateById(issue);
    logService.record("user", statusText(status) + "证书 " + issue.getSerialNumber(),
        "certificate", id);
    return detail(id);
  }

  @Transactional
  public void remove(Long id) {
    CertificateIssue issue = requireIssue(id);
    issueMapper.deleteById(id);
    logService.record("user", "删除证书 " + issue.getSerialNumber(), "certificate", id);
  }

  public Map<String, Object> verify(String serial) {
    Map<String, Object> certificate = issueMapper.selectBySerial(serial);
    boolean found = certificate != null;
    boolean issued = found && Integer.valueOf(2).equals(certificate.get("status"));
    LocalDate expireDate = found ? toLocalDate(certificate.get("expireDate")) : null;
    boolean expired = issued && expireDate != null && expireDate.isBefore(LocalDate.now());
    boolean valid = issued && !expired;

    String message;
    if (!found) {
      message = "未查询到该证书";
    } else if (Integer.valueOf(3).equals(certificate.get("status"))) {
      message = "证书已撤销";
    } else if (!issued) {
      message = "证书尚未发放";
    } else if (expired) {
      message = "证书已过期（有效期至 " + expireDate + "）";
    } else {
      message = "证书有效";
    }

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("valid", valid);
    result.put("issued", issued);
    result.put("expired", expired);
    result.put("message", message);
    result.put("certificate", certificate);
    return result;
  }

  // ------------------------------------------------------------- 模板

  public List<Map<String, Object>> templates() {
    return templateMapper.selectList(Wrappers.<CertificateTemplate>lambdaQuery()
            .orderByAsc(CertificateTemplate::getId)).stream()
        .map(this::toTemplateItem).toList();
  }

  @Transactional
  public Map<String, Object> createTemplate(CertificateTemplate request) {
    if (request.getTitle() == null || request.getTitle().isBlank()) {
      throw new BizException("证书名称不能为空");
    }
    request.setId(null);
    templateMapper.insert(request);
    return toTemplateItem(request);
  }

  @Transactional
  public Map<String, Object> updateTemplate(Long id, CertificateTemplate request) {
    CertificateTemplate template = templateMapper.selectById(id);
    if (template == null) {
      throw BizException.notFound("证书模板不存在");
    }
    if (request.getTitle() != null) template.setTitle(request.getTitle());
    if (request.getLevelLabel() != null) template.setLevelLabel(request.getLevelLabel());
    if (request.getCategory() != null) template.setCategory(request.getCategory());
    if (request.getIssueOrg() != null) template.setIssueOrg(request.getIssueOrg());
    if (request.getValidityMonths() != null) template.setValidityMonths(request.getValidityMonths());
    if (request.getDescription() != null) template.setDescription(request.getDescription());
    if (request.getBadgeType() != null) template.setBadgeType(request.getBadgeType());
    if (request.getPrivilegeText() != null) template.setPrivilegeText(request.getPrivilegeText());
    templateMapper.updateById(template);
    return toTemplateItem(template);
  }

  @Transactional
  public void removeTemplate(Long id) {
    long used = issueMapper.selectCount(Wrappers.<CertificateIssue>lambdaQuery()
        .eq(CertificateIssue::getTemplateId, id));
    if (used > 0) {
      throw new BizException("该模板已签发 " + used + " 张证书，无法删除");
    }
    templateMapper.deleteById(id);
  }

  private CertificateIssue requireIssue(Long id) {
    CertificateIssue issue = issueMapper.selectById(id);
    if (issue == null) {
      throw BizException.notFound("证书记录不存在");
    }
    return issue;
  }

  private String generateSerial(CertificateTemplate template) {
    String category = template.getCategory() == null ? "CERT"
        : template.getCategory().replaceAll("[^A-Za-z]", "").toUpperCase();
    String year = String.valueOf(LocalDate.now().getYear());
    String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 4).toUpperCase();
    return "ZX-" + year + "-" + (category.isBlank() ? "CERT" : category) + "-" + suffix;
  }

  private String randomHash() {
    return "0x" + UUID.randomUUID().toString().replace("-", "")
        + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
  }

  private LocalDate expireDate(LocalDate issueDate, Integer months) {
    if (months == null || months <= 0) {
      return null;
    }
    return issueDate.plusMonths(months);
  }

  private LocalDate toLocalDate(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof LocalDate localDate) {
      return localDate;
    }
    if (value instanceof java.sql.Date sqlDate) {
      return sqlDate.toLocalDate();
    }
    if (value instanceof java.util.Date date) {
      return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }
    String text = String.valueOf(value);
    return LocalDate.parse(text.length() >= 10 ? text.substring(0, 10) : text);
  }

  private String validityLabel(LocalDate issueDate, Integer months) {
    if (months == null || months <= 0) {
      return "长期有效";
    }
    return months + " 个月 (" + issueDate.plusMonths(months).getYear() + "年到期)";
  }

  private String statusText(Object rawStatus) {
    Integer status = Numbers.toInteger(rawStatus);
    if (status == null) {
      return "";
    }
    return switch (status) {
      case 2 -> "已发放";
      case 3 -> "已撤销";
      default -> "待发放";
    };
  }

  private Map<String, Object> toTemplateItem(CertificateTemplate template) {
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("id", template.getId());
    item.put("extId", template.getExtId());
    item.put("title", template.getTitle());
    item.put("levelLabel", template.getLevelLabel());
    item.put("category", template.getCategory());
    item.put("issueOrg", template.getIssueOrg());
    item.put("validityMonths", template.getValidityMonths());
    item.put("description", template.getDescription());
    item.put("badgeType", template.getBadgeType());
    item.put("privilegeText", template.getPrivilegeText());
    return item;
  }

  public record IssueSaveRequest(Long templateId, Long userId, String serialNumber,
      LocalDate issueDate, LocalDate expireDate, String validityLabel, String rankingScore,
      Integer status, String blockchainHash) {
  }
}
