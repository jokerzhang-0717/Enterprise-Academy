-- 新员工入职手册阅读状态：NULL = 尚未阅读，非 NULL = 首次完成阅读时间。
-- Existing databases: run this once after backing up the database.
ALTER TABLE sys_user
  ADD COLUMN onboarding_completed_at DATETIME NULL COMMENT '入职手册阅读完成时间，NULL = 未完成' AFTER last_active_at;
