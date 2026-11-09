-- 批次 3：按课时聚合学习时长（完成度判定）时避免回表扫描。
-- 若索引已存在会报 Duplicate key name，可先执行 SHOW INDEX FROM study_session 确认。
ALTER TABLE study_session
  ADD INDEX idx_session_user_lesson (user_id, lesson_id);
