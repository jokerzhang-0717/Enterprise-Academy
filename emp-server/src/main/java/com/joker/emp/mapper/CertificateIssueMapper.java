package com.joker.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.joker.emp.entity.CertificateIssue;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CertificateIssueMapper extends BaseMapper<CertificateIssue> {

  String ISSUE_SELECT = """
      SELECT ci.id, ci.serial_number AS serialNumber, ci.status,
             ci.issue_date AS issueDate, ci.expire_date AS expireDate,
             ci.validity_label AS validityLabel, ci.ranking_score AS rankingScore,
             ci.blockchain_hash AS blockchainHash, ci.template_id AS templateId,
             ct.title AS templateTitle, ct.level_label AS levelLabel,
             ct.category, ct.issue_org AS issueOrg, ct.validity_months AS validityMonths,
             ct.badge_type AS badgeType, ct.privilege_text AS privilegeText,
             ci.user_id AS userId, u.name AS userName, u.employee_no AS employeeNo,
             d.name AS department
      FROM certificate_issue ci
      JOIN certificate_template ct ON ct.id = ci.template_id
      JOIN sys_user u ON u.id = ci.user_id
      LEFT JOIN department d ON d.id = u.department_id
      WHERE ci.deleted_at IS NULL
      """;

  @Select("""
      <script>
      SELECT ci.id, ci.serial_number AS serialNumber, ci.status,
             ci.issue_date AS issueDate, ci.expire_date AS expireDate,
             ci.validity_label AS validityLabel, ci.ranking_score AS rankingScore,
             ci.blockchain_hash AS blockchainHash, ci.template_id AS templateId,
             ct.title AS templateTitle, ct.level_label AS levelLabel,
             ct.category, ct.issue_org AS issueOrg, ct.validity_months AS validityMonths,
             ct.badge_type AS badgeType, ct.privilege_text AS privilegeText,
             ci.user_id AS userId, u.name AS userName, u.employee_no AS employeeNo,
             d.name AS department
      FROM certificate_issue ci
      JOIN certificate_template ct ON ct.id = ci.template_id
      JOIN sys_user u ON u.id = ci.user_id
      LEFT JOIN department d ON d.id = u.department_id
      WHERE ci.deleted_at IS NULL
      <if test="status != null">AND ci.status = #{status}</if>
      <if test="keyword != null and keyword != ''">
        AND (u.name LIKE CONCAT('%', #{keyword}, '%')
          OR ci.serial_number LIKE CONCAT('%', #{keyword}, '%')
          OR ct.title LIKE CONCAT('%', #{keyword}, '%'))
      </if>
      ORDER BY ci.id DESC
      </script>
      """)
  IPage<Map<String, Object>> selectAdminPage(IPage<?> page,
      @Param("status") Integer status, @Param("keyword") String keyword);

  @Select("""
      SELECT ci.id, ci.serial_number AS serialNumber, ci.status,
             ci.issue_date AS issueDate, ci.expire_date AS expireDate,
             ci.validity_label AS validityLabel, ci.ranking_score AS rankingScore,
             ci.blockchain_hash AS blockchainHash,
             ct.id AS templateId, ct.title AS templateTitle, ct.level_label AS levelLabel,
             ct.category, ct.issue_org AS issueOrg, ct.validity_months AS validityMonths,
             ct.description, ct.badge_type AS badgeType, ct.privilege_text AS privilegeText,
             u.name AS userName, u.employee_no AS employeeNo, d.name AS department
      FROM certificate_issue ci
      JOIN certificate_template ct ON ct.id = ci.template_id
      JOIN sys_user u ON u.id = ci.user_id
      LEFT JOIN department d ON d.id = u.department_id
      WHERE ci.user_id = #{userId} AND ci.status = 2 AND ci.deleted_at IS NULL
      ORDER BY ci.issue_date DESC, ci.id DESC
      """)
  java.util.List<Map<String, Object>> selectMyCertificates(@Param("userId") Long userId);

  @Select("""
      SELECT ci.id, ci.serial_number AS serialNumber, ci.status,
             ci.issue_date AS issueDate, ci.expire_date AS expireDate,
             ci.validity_label AS validityLabel, ci.ranking_score AS rankingScore,
             ci.blockchain_hash AS blockchainHash,
             ct.title AS templateTitle, ct.level_label AS levelLabel,
             ct.category, ct.issue_org AS issueOrg, ct.validity_months AS validityMonths,
             ct.badge_type AS badgeType, ct.privilege_text AS privilegeText,
             u.name AS userName, u.employee_no AS employeeNo, d.name AS department
      FROM certificate_issue ci
      JOIN certificate_template ct ON ct.id = ci.template_id
      JOIN sys_user u ON u.id = ci.user_id
      LEFT JOIN department d ON d.id = u.department_id
      WHERE ci.serial_number = #{serial}
      LIMIT 1
      """)
  Map<String, Object> selectBySerial(@Param("serial") String serial);
}
