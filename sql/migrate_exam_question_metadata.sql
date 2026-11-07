-- Existing databases: run this once after backing up the database.
ALTER TABLE exam_question
  ADD COLUMN knowledge_point VARCHAR(128) NULL COMMENT '知识点' AFTER title,
  ADD COLUMN difficulty TINYINT NOT NULL DEFAULT 2 COMMENT '难度：1 简单 2 中等 3 困难' AFTER knowledge_point;
