package com.joker.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.joker.emp.entity.ExamAssignment;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ExamAssignmentMapper extends BaseMapper<ExamAssignment> {

  IPage<Map<String, Object>> selectByExam(IPage<?> page, @Param("examId") Long examId,
      @Param("keyword") String keyword);

  List<ExamAssignment> selectAllByExam(@Param("examId") Long examId);
}
