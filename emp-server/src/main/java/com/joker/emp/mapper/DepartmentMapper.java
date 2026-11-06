package com.joker.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.joker.emp.entity.Department;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {

  @Select("""
      SELECT d.id AS departmentId, d.name, d.manager_name AS managerName, d.status,
             (SELECT COUNT(*)
                FROM sys_user u
               WHERE u.department_id = d.id AND u.role = 1 AND u.deleted_at IS NULL
             ) AS learnerCount,
             (SELECT COALESCE(SUM(e.study_hours), 0)
                FROM sys_user u
                JOIN enrollment e ON e.user_id = u.id
               WHERE u.department_id = d.id AND u.role = 1 AND u.deleted_at IS NULL
             ) AS totalStudyHours,
             d.learner_count_cached AS learnerCountCached,
             d.study_hours_cached AS studyHoursCached
        FROM department d
       WHERE d.deleted_at IS NULL
       ORDER BY d.id
      """)
  List<Map<String, Object>> selectStats();
}
