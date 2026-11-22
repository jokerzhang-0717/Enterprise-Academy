-- 已有库迁移：为 sys_user 增加入职手册阅读完成时间。
-- NULL = 尚未阅读；非 NULL = 首次完成阅读时间。
-- 新库无需执行：schema.sql 已包含该字段。
-- 可重复执行：仅当字段不存在时才添加。
SET NAMES utf8mb4;
SET @add_column_sql = (
  SELECT IF(COUNT(*) > 0,
           'SELECT 1',
           'ALTER TABLE sys_user ADD COLUMN onboarding_completed_at DATETIME NULL COMMENT ''入职手册阅读完成时间，NULL = 未完成'' AFTER last_active_at')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sys_user'
    AND COLUMN_NAME = 'onboarding_completed_at'
);
PREPARE add_column_stmt FROM @add_column_sql;
EXECUTE add_column_stmt;
DEALLOCATE PREPARE add_column_stmt;
