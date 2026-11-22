-- Existing databases: run this once after backing up the database.
-- Removes the legacy difficulty classification from the question bank.
-- Safe to re-run: the column is only dropped when it still exists.
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
