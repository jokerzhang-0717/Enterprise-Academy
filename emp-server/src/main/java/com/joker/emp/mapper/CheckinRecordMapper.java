package com.joker.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.joker.emp.entity.CheckinRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CheckinRecordMapper extends BaseMapper<CheckinRecord> {

  IPage<Map<String, Object>> selectAdminPage(IPage<?> page,
      @Param("date") LocalDate date,
      @Param("status") Integer status,
      @Param("keyword") String keyword);

  List<Map<String, Object>> selectMyList(@Param("userId") Long userId,
      @Param("from") LocalDate from, @Param("to") LocalDate to);
}
