package com.joker.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.joker.emp.entity.SysUser;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

  Map<String, Object> selectLearnerProfile(@Param("userId") Long userId);

  List<Map<String, Object>> selectLearnerProfiles(@Param("ids") Collection<Long> ids);

  Map<String, Object> selectAdminPending();

  List<Map<String, Object>> selectMonthlyStudyHours(@Param("userId") Long userId);
}
