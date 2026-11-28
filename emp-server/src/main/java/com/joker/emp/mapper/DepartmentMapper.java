package com.joker.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.joker.emp.entity.Department;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {

  List<Map<String, Object>> selectStats();
}
