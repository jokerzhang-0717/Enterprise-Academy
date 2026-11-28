package com.joker.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.joker.emp.entity.ExamAttempt;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ExamAttemptMapper extends BaseMapper<ExamAttempt> {

  IPage<Map<String, Object>> selectAdminPage(IPage<?> page, @Param("examId") Long examId,
      @Param("status") Integer status, @Param("keyword") String keyword);
}
