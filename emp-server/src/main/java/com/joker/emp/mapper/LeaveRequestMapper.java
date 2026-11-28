package com.joker.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.joker.emp.entity.LeaveRequest;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LeaveRequestMapper extends BaseMapper<LeaveRequest> {

  IPage<Map<String, Object>> selectAdminPage(IPage<?> page,
      @Param("status") Integer status, @Param("keyword") String keyword);

  List<Map<String, Object>> selectMyList(@Param("userId") Long userId,
      @Param("status") Integer status);

  /** 当天最新的请假流水号，包含已逻辑删除的记录；flow_no 有唯一约束。 */
  String selectLatestFlowNo(@Param("date") String date);
}
