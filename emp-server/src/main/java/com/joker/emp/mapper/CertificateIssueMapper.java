package com.joker.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.joker.emp.entity.CertificateIssue;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CertificateIssueMapper extends BaseMapper<CertificateIssue> {

  IPage<Map<String, Object>> selectAdminPage(IPage<?> page,
      @Param("status") Integer status, @Param("keyword") String keyword);

  List<Map<String, Object>> selectMyCertificates(@Param("userId") Long userId);

  Map<String, Object> selectBySerial(@Param("serial") String serial);
}
