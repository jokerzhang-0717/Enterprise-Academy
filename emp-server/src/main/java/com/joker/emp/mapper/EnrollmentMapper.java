package com.joker.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.joker.emp.entity.Enrollment;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EnrollmentMapper extends BaseMapper<Enrollment> {

  List<Map<String, Object>> selectProgress(@Param("userId") Long userId);

  /** 幂等创建选课记录，SQL 见 resources/mapper/EnrollmentMapper.xml。 */
  int insertOrIgnore(@Param("userId") Long userId, @Param("courseId") Long courseId);

  BigDecimal sumStudyHours(@Param("userId") Long userId);
}
