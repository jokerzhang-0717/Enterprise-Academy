-- 已有库迁移：为 study_session(user_id, lesson_id) 补索引，
-- 避免按课时聚合学习时长（完成度判定）时回表扫描。
-- 新库无需执行：schema.sql 已包含该索引。
-- 可重复执行：仅当索引不存在时才添加。
SET NAMES utf8mb4;
SET @add_index_sql = (
  SELECT IF(COUNT(*) > 0,
           'SELECT 1',
           'ALTER TABLE study_session ADD INDEX idx_session_user_lesson (user_id, lesson_id)')
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'study_session'
    AND INDEX_NAME = 'idx_session_user_lesson'
);
PREPARE add_index_stmt FROM @add_index_sql;
EXECUTE add_index_stmt;
DEALLOCATE PREPARE add_index_stmt;
