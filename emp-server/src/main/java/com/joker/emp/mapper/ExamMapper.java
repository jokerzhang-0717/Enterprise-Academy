package com.joker.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.joker.emp.entity.Exam;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ExamMapper extends BaseMapper<Exam> {

  /** 历史答卷可能指向已归档（软删除）的考试，读取详情时需绕过 @TableLogic */
  Exam selectByIdIncludingDeleted(@Param("id") Long id);

  /** 生成考试编号时需统计含软删除记录，否则会撞上仍存在的唯一索引 */
  long countCodesIncludingDeleted(@Param("prefix") String prefix);

  IPage<Map<String, Object>> selectOverviewPage(IPage<?> page,
      @Param("keyword") String keyword, @Param("status") Integer status);

  List<Map<String, Object>> selectAssignments(@Param("userId") Long userId);

  List<Map<String, Object>> selectPastAttempts(@Param("userId") Long userId);

  Map<String, Object> selectMetrics(@Param("userId") Long userId);
}
