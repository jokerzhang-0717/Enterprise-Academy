package com.joker.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.joker.emp.entity.AttemptAnswer;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AttemptAnswerMapper extends BaseMapper<AttemptAnswer> {

  List<Map<String, Object>> selectDetail(@Param("attemptId") Long attemptId);
}
