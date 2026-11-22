-- 已有库迁移：移除题库的历史难度分类字段（题目难度分类已下线）。
-- 新库无需执行：schema.sql 不再包含 difficulty 字段。
-- 可重复执行：仅当字段存在时才删除。
SET NAMES utf8mb4;
SET @drop_difficulty_sql = (
  SELECT IF(COUNT(*) > 0,
           'ALTER TABLE exam_question DROP COLUMN difficulty',
           'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'exam_question'
    AND COLUMN_NAME = 'difficulty'
);
PREPARE drop_difficulty_stmt FROM @drop_difficulty_sql;
EXECUTE drop_difficulty_stmt;
DEALLOCATE PREPARE drop_difficulty_stmt;
