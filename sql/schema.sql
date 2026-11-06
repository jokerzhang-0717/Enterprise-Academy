-- =============================================================================
-- 云舒艺境 ENTERPRISE ACADEMY · 企业数字化智能人才培养平台
-- 数据库结构 (MySQL 8.0+ / InnoDB / utf8mb4)
--
-- 建模口径：正规后端建模
--   1. 主键统一 BIGINT UNSIGNED AUTO_INCREMENT；业务编码单独成列并加唯一索引。
--   2. ext_id 保留前端 mock 的原始 id（c1 / exam-1 / aci-3 ...），便于数据迁移与对拍，
--      迁移完成后可删除该列。
--   3. 状态类字段一律 TINYINT + 注释枚举（见下方「枚举字典」），不使用 MySQL ENUM。
--   4. 可派生的统计（学时、完成门数、证书数、题目数、部门汇总）不写回业务表，
--      由文件末尾的视图给出；确需缓存的计数器列名统一以 _cached 结尾，由后端维护。
--   5. 业务表带 created_at / updated_at / deleted_at（软删除，NULL = 未删除）。
--
-- 枚举字典
--   sys_user.role              1 学员  2 管理员
--   sys_user.account_status    1 正常  2 禁用
--   department.status          1 启用  2 停用
--   course.status              1 草稿  2 已上架  3 已下架
--   course_chapter/lesson      无状态，靠 sort_no 排序
--   enrollment.status          1 未开始  2 学习中  3 已完成
--   exam.status                1 草稿  2 未开始  3 进行中  4 已结束
--   exam_question.type         1 单选  2 多选  3 判断  4 应用/简答
--   exam_assignment.status     1 待参加  2 已完成  3 已逾期
--   exam_attempt.status        1 作答中  2 已交卷待评分  3 已评分  4 系统强制交卷
--   certificate_issue.status   1 待发放  2 已发放  3 已撤销
--   announcement.status        1 草稿  2 已发布
--   notification.type          1 考试与测评  2 课程学习  3 考勤与请假  4 系统公告  5 培训考勤
--   notification.priority      1 普通  2 加急
--   leave_request.status       1 待审批  2 已通过  3 已驳回
--   checkin_record.status      1 正常  2 迟到  3 缺勤  4 早退
--   checkin_record.method      1 人脸识别  2 工牌刷卡  3 移动端定位
--   learning_track.level       1 入门  2 进阶  3 高级
--
-- 时间锚点：种子数据以 2025-05-14 12:00:00 为「今天」（首页横幅显示的日期），
--          mock 里的「3小时前 / 今天 09:42 / 昨天 17:30」均按该锚点折算成绝对时间。
-- =============================================================================

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS enterprise_academy
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE enterprise_academy;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS operation_log;
DROP TABLE IF EXISTS checkin_record;
DROP TABLE IF EXISTS leave_request;
DROP TABLE IF EXISTS notification_attachment;
DROP TABLE IF EXISTS notification;
DROP TABLE IF EXISTS announcement_read;
DROP TABLE IF EXISTS announcement;
DROP TABLE IF EXISTS certificate_issue;
DROP TABLE IF EXISTS certificate_template;
DROP TABLE IF EXISTS attempt_answer;
DROP TABLE IF EXISTS exam_attempt;
DROP TABLE IF EXISTS exam_assignment;
DROP TABLE IF EXISTS question_option;
DROP TABLE IF EXISTS exam_question;
DROP TABLE IF EXISTS exam;
DROP TABLE IF EXISTS track_course;
DROP TABLE IF EXISTS track_outline_item;
DROP TABLE IF EXISTS learning_track;
DROP TABLE IF EXISTS study_session;
DROP TABLE IF EXISTS lesson_progress;
DROP TABLE IF EXISTS enrollment;
DROP TABLE IF EXISTS course_lesson;
DROP TABLE IF EXISTS course_chapter;
DROP TABLE IF EXISTS course_tag;
DROP TABLE IF EXISTS tag;
DROP TABLE IF EXISTS course;
DROP TABLE IF EXISTS course_category;
DROP TABLE IF EXISTS instructor;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS department;

-- -----------------------------------------------------------------------------
-- 1. 组织与账号
-- -----------------------------------------------------------------------------

CREATE TABLE department (
  id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  ext_id              VARCHAR(32)     NULL COMMENT 'mock 原始 id，如 d-1',
  name                VARCHAR(64)     NOT NULL COMMENT '部门名称',
  manager_name        VARCHAR(64)     NULL COMMENT '部门负责人姓名',
  description         VARCHAR(255)    NULL COMMENT '部门职责说明',
  status              TINYINT         NOT NULL DEFAULT 1 COMMENT '1 启用 2 停用',
  learner_count_cached INT UNSIGNED   NOT NULL DEFAULT 0 COMMENT '缓存：部门学员数（全公司口径，非本表行数）',
  study_hours_cached  DECIMAL(10,1)   NOT NULL DEFAULT 0 COMMENT '缓存：部门累计学时',
  sort_no             INT             NOT NULL DEFAULT 0,
  created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at          DATETIME        NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_department_ext (ext_id),
  UNIQUE KEY uk_department_name (name),
  KEY idx_department_status (status, deleted_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '部门';

CREATE TABLE sys_user (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  ext_id          VARCHAR(32)     NULL COMMENT 'mock 原始 id，如 u-1 / admin',
  employee_no     VARCHAR(32)     NOT NULL COMMENT '工号，登录账号',
  password_hash   VARCHAR(128)    NOT NULL DEFAULT '' COMMENT '口令散列；演示种子为空串',
  name            VARCHAR(64)     NOT NULL,
  department_id   BIGINT UNSIGNED NULL,
  job_title       VARCHAR(64)     NULL COMMENT '职位，如 高级研发工程师',
  grade           VARCHAR(64)     NULL COMMENT '职级/专班，如 P6 高级研发',
  specialty       VARCHAR(128)    NULL COMMENT '专长方向，如 云原生与微服务核心骨干',
  avatar_url      VARCHAR(512)    NULL,
  phone           VARCHAR(32)     NULL,
  email           VARCHAR(128)    NULL,
  role            TINYINT         NOT NULL DEFAULT 1 COMMENT '1 学员 2 管理员',
  account_status  TINYINT         NOT NULL DEFAULT 1 COMMENT '1 正常 2 禁用',
  last_active_at  DATETIME        NULL COMMENT '最近活跃时间',
  created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at      DATETIME        NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_ext (ext_id),
  UNIQUE KEY uk_user_employee_no (employee_no),
  KEY idx_user_department (department_id),
  KEY idx_user_list (role, account_status, deleted_at),
  CONSTRAINT fk_user_department FOREIGN KEY (department_id) REFERENCES department (id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '平台用户（学员 + 管理员）';

CREATE TABLE instructor (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name        VARCHAR(64)     NOT NULL COMMENT '讲师姓名或机构名',
  title       VARCHAR(64)     NULL COMMENT '讲师头衔，如 资深架构师',
  is_org      TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '1 机构讲师（如 集团安全部）0 个人',
  avatar_url  VARCHAR(512)    NULL,
  created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_instructor_name_title (name, title)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '讲师档案';

-- -----------------------------------------------------------------------------
-- 2. 课程目录
-- -----------------------------------------------------------------------------

CREATE TABLE course_category (
  id       BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code     VARCHAR(32)     NOT NULL COMMENT 'core / tech / product / leadership / compliance',
  label    VARCHAR(64)     NOT NULL COMMENT '通识素养 / 技术研发 ...',
  sort_no  INT             NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_course_category_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '课程一级分类';

CREATE TABLE course (
  id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  ext_id              VARCHAR(32)     NULL COMMENT 'mock 原始 id，如 c1',
  title               VARCHAR(200)    NOT NULL,
  category_id         BIGINT UNSIGNED NOT NULL,
  subcategory_label   VARCHAR(64)     NULL COMMENT '卡片二级标签，如 研发架构',
  instructor_id       BIGINT UNSIGNED NULL,
  is_required         TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '1 必修 0 选修',
  level               VARCHAR(64)     NULL COMMENT '难度/性质文案，如 必修课程 / 进阶技术',
  total_hours         DECIMAL(6,1)    NOT NULL DEFAULT 0 COMMENT '课程总学时',
  total_lesson_count  INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '总课时数（缓存，随章节编辑刷新）',
  enroll_count_cached INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '缓存：报名人数（展示用，非 enrollment 行数）',
  thumbnail_url       VARCHAR(512)    NULL,
  summary             VARCHAR(500)    NULL COMMENT '一句话简介',
  description         TEXT            NULL COMMENT '富文本详情',
  status              TINYINT         NOT NULL DEFAULT 1 COMMENT '1 草稿 2 已上架 3 已下架',
  created_by          BIGINT UNSIGNED NULL,
  created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at          DATETIME        NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_course_ext (ext_id),
  KEY idx_course_category (category_id, status),
  KEY idx_course_list (status, deleted_at, updated_at),
  CONSTRAINT fk_course_category FOREIGN KEY (category_id) REFERENCES course_category (id),
  CONSTRAINT fk_course_instructor FOREIGN KEY (instructor_id) REFERENCES instructor (id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '课程';

CREATE TABLE tag (
  id    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name  VARCHAR(64)     NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_tag_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '课程标签字典';

CREATE TABLE course_tag (
  course_id BIGINT UNSIGNED NOT NULL,
  tag_id    BIGINT UNSIGNED NOT NULL,
  sort_no   INT             NOT NULL DEFAULT 0,
  PRIMARY KEY (course_id, tag_id),
  CONSTRAINT fk_course_tag_course FOREIGN KEY (course_id) REFERENCES course (id) ON DELETE CASCADE,
  CONSTRAINT fk_course_tag_tag FOREIGN KEY (tag_id) REFERENCES tag (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '课程-标签';

CREATE TABLE course_chapter (
  id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  ext_id     VARCHAR(32)     NULL COMMENT 'mock 原始 id，如 ch-1',
  course_id  BIGINT UNSIGNED NOT NULL,
  title      VARCHAR(200)    NOT NULL,
  sort_no    INT             NOT NULL DEFAULT 0,
  created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_chapter_ext (ext_id),
  KEY idx_chapter_course (course_id, sort_no),
  CONSTRAINT fk_chapter_course FOREIGN KEY (course_id) REFERENCES course (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '课程章节';

CREATE TABLE course_lesson (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  ext_id            VARCHAR(32)     NULL COMMENT 'mock 原始 id，如 ls-1',
  chapter_id        BIGINT UNSIGNED NOT NULL,
  title             VARCHAR(200)    NOT NULL,
  duration_seconds  INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '课时时长（秒），mock 的「22分」折算为 1320',
  video_url         VARCHAR(512)    NULL COMMENT '为空时前端走占位控制条',
  sort_no           INT             NOT NULL DEFAULT 0,
  created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_lesson_ext (ext_id),
  KEY idx_lesson_chapter (chapter_id, sort_no),
  CONSTRAINT fk_lesson_chapter FOREIGN KEY (chapter_id) REFERENCES course_chapter (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '课时（章节下的小节）';

-- -----------------------------------------------------------------------------
-- 3. 学习过程
-- -----------------------------------------------------------------------------

CREATE TABLE enrollment (
  id                     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id                BIGINT UNSIGNED NOT NULL,
  course_id              BIGINT UNSIGNED NOT NULL,
  status                 TINYINT         NOT NULL DEFAULT 1 COMMENT '1 未开始 2 学习中 3 已完成',
  progress_percent       TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '缓存：0-100，由后端按完成课时推进',
  completed_lesson_count INT UNSIGNED    NOT NULL DEFAULT 0,
  study_hours            DECIMAL(6,2)    NOT NULL DEFAULT 0 COMMENT '缓存：该门课已投入学时',
  score                  DECIMAL(5,1)    NULL COMMENT '结课成绩',
  started_at             DATETIME        NULL,
  last_studied_at        DATETIME        NULL COMMENT '断点续学时间',
  completed_at           DATETIME        NULL,
  created_at             DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at             DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_enrollment (user_id, course_id),
  KEY idx_enrollment_course (course_id, status),
  KEY idx_enrollment_recent (user_id, last_studied_at),
  CONSTRAINT fk_enrollment_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE,
  CONSTRAINT fk_enrollment_course FOREIGN KEY (course_id) REFERENCES course (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '选课与学习进度';

CREATE TABLE lesson_progress (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id          BIGINT UNSIGNED NOT NULL,
  lesson_id        BIGINT UNSIGNED NOT NULL,
  is_completed     TINYINT(1)      NOT NULL DEFAULT 0,
  watched_seconds  INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '断点续播位置',
  completed_at     DATETIME        NULL,
  updated_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_lesson_progress (user_id, lesson_id),
  CONSTRAINT fk_lp_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE,
  CONSTRAINT fk_lp_lesson FOREIGN KEY (lesson_id) REFERENCES course_lesson (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '课时级完成明细';

CREATE TABLE study_session (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id          BIGINT UNSIGNED NOT NULL,
  course_id        BIGINT UNSIGNED NULL,
  lesson_id        BIGINT UNSIGNED NULL,
  started_at       DATETIME        NOT NULL,
  duration_minutes DECIMAL(6,1)    NOT NULL DEFAULT 0,
  source           TINYINT         NOT NULL DEFAULT 1 COMMENT '1 视频 2 线下培训 3 考试复盘',
  created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_session_user_time (user_id, started_at),
  CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE,
  CONSTRAINT fk_session_course FOREIGN KEY (course_id) REFERENCES course (id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '学时流水（个人看板月度趋势的数据源）';

-- -----------------------------------------------------------------------------
-- 4. 研修通道
-- -----------------------------------------------------------------------------

CREATE TABLE learning_track (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  ext_id           VARCHAR(32)     NULL COMMENT 'mock 原始 id，如 trk-1',
  category         VARCHAR(32)     NULL COMMENT 'security / tech / leadership',
  category_label   VARCHAR(64)     NULL,
  title            VARCHAR(200)    NOT NULL,
  thumbnail_url    VARCHAR(512)    NULL,
  summary          VARCHAR(500)    NULL,
  description      TEXT            NULL,
  lecture_count    INT UNSIGNED    NOT NULL DEFAULT 0,
  student_count    INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT 'mock 的 "1,420" 去千分位后入库',
  duration_label   VARCHAR(32)     NULL COMMENT '约 3 小时',
  level            TINYINT         NOT NULL DEFAULT 2 COMMENT '1 入门 2 进阶 3 高级',
  audience         VARCHAR(255)    NULL COMMENT '适合人群',
  status           TINYINT         NOT NULL DEFAULT 2 COMMENT '1 草稿 2 已上架 3 已下架',
  sort_no          INT             NOT NULL DEFAULT 0,
  created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at       DATETIME        NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_track_ext (ext_id),
  KEY idx_track_list (status, sort_no)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '企业研修通道';

CREATE TABLE track_outline_item (
  id        BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  track_id  BIGINT UNSIGNED NOT NULL,
  title     VARCHAR(200)    NOT NULL,
  sort_no   INT             NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_outline_track (track_id, sort_no),
  CONSTRAINT fk_outline_track FOREIGN KEY (track_id) REFERENCES learning_track (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '通道大纲条目';

CREATE TABLE track_course (
  track_id  BIGINT UNSIGNED NOT NULL,
  course_id BIGINT UNSIGNED NOT NULL,
  sort_no   INT             NOT NULL DEFAULT 0,
  PRIMARY KEY (track_id, course_id),
  CONSTRAINT fk_tc_track FOREIGN KEY (track_id) REFERENCES learning_track (id) ON DELETE CASCADE,
  CONSTRAINT fk_tc_course FOREIGN KEY (course_id) REFERENCES course (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '通道-课程';

-- -----------------------------------------------------------------------------
-- 5. 考试与题库
-- -----------------------------------------------------------------------------

CREATE TABLE exam (
  id                     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  ext_id                 VARCHAR(32)     NULL COMMENT 'mock 原始 id，如 exam-1 / past-3',
  code                   VARCHAR(64)     NOT NULL COMMENT '考试编号 ZK-... / CER-...',
  title                  VARCHAR(200)    NOT NULL,
  organizer              VARCHAR(128)    NULL COMMENT '主办方，如 技术合规部 · 2025Q2',
  exam_type              VARCHAR(64)     NULL COMMENT '往期档案的类型，如 技术晋升认证',
  status                 TINYINT         NOT NULL DEFAULT 1 COMMENT '1 草稿 2 未开始 3 进行中 4 已结束',
  location               VARCHAR(128)    NULL,
  start_at               DATETIME        NULL COMMENT 'mock timeWindow 前半段',
  end_at                 DATETIME        NULL COMMENT 'mock timeWindow 后半段',
  duration_minutes       SMALLINT UNSIGNED NOT NULL DEFAULT 60,
  total_score            DECIMAL(5,1)    NOT NULL DEFAULT 100,
  pass_score             DECIMAL(5,1)    NOT NULL DEFAULT 60,
  is_double_camera       TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '双机位监考',
  related_course_id      BIGINT UNSIGNED NULL,
  related_course_text    VARCHAR(200)    NULL COMMENT 'mock 原文（含进度后缀），迁移期保留',
  remaining_chance_cached INT UNSIGNED   NULL COMMENT '缓存：剩余考评机会',
  question_count_cached  INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '缓存：试卷题数，发布时刷新',
  participant_count_cached INT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '缓存：参与人次',
  created_by             BIGINT UNSIGNED NULL,
  created_at             DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at             DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at             DATETIME        NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_exam_ext (ext_id),
  UNIQUE KEY uk_exam_code (code),
  KEY idx_exam_status (status, deleted_at, end_at),
  CONSTRAINT fk_exam_course FOREIGN KEY (related_course_id) REFERENCES course (id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '考试/测评';

CREATE TABLE exam_question (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  ext_id          VARCHAR(32)     NULL COMMENT 'mock 原始 id，如 q-2',
  exam_id         BIGINT UNSIGNED NULL COMMENT '为空 = 未组卷的备用题',
  question_no     SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '题号',
  type            TINYINT         NOT NULL DEFAULT 1 COMMENT '1 单选 2 多选 3 判断 4 应用/简答',
  score           DECIMAL(5,1)    NOT NULL DEFAULT 5,
  title           VARCHAR(1000)   NOT NULL,
  code_filename   VARCHAR(128)    NULL COMMENT '题干附带的代码片段元信息',
  code_runtime    VARCHAR(128)    NULL,
  code_content    MEDIUMTEXT      NULL,
  correct_answer  VARCHAR(16)     NULL COMMENT '客观题答案：单选/判断 "A"，多选 "ABD"；主观题为空',
  reference_answer TEXT           NULL COMMENT '应用题参考答案/评分要点',
  analysis        VARCHAR(1000)   NULL COMMENT '整题解析',
  created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at      DATETIME        NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_question_ext (ext_id),
  KEY idx_question_exam (exam_id, question_no),
  KEY idx_question_type (type),
  CONSTRAINT fk_question_exam FOREIGN KEY (exam_id) REFERENCES exam (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '题目';

CREATE TABLE question_option (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  question_id  BIGINT UNSIGNED NOT NULL,
  option_key   CHAR(1)         NOT NULL COMMENT 'A/B/C/D',
  content      VARCHAR(1000)   NOT NULL,
  explanation  VARCHAR(1000)   NULL COMMENT '选项级解析（错题回顾用）',
  sort_no      INT             NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_option (question_id, option_key),
  CONSTRAINT fk_option_question FOREIGN KEY (question_id) REFERENCES exam_question (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '题目选项';

CREATE TABLE exam_assignment (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  exam_id         BIGINT UNSIGNED NOT NULL,
  user_id         BIGINT UNSIGNED NOT NULL,
  status          TINYINT         NOT NULL DEFAULT 1 COMMENT '1 待参加 2 已完成 3 已逾期',
  attempts_allowed TINYINT UNSIGNED NOT NULL DEFAULT 1,
  attempts_used   TINYINT UNSIGNED NOT NULL DEFAULT 0,
  deadline_at     DATETIME        NULL,
  created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_assignment (exam_id, user_id),
  KEY idx_assignment_user (user_id, status),
  CONSTRAINT fk_assignment_exam FOREIGN KEY (exam_id) REFERENCES exam (id) ON DELETE CASCADE,
  CONSTRAINT fk_assignment_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '考试指派（谁该考哪场）';

CREATE TABLE exam_attempt (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  ext_id           VARCHAR(32)     NULL COMMENT 'mock 原始 id，如 past-1',
  exam_id          BIGINT UNSIGNED NOT NULL,
  user_id          BIGINT UNSIGNED NOT NULL,
  attempt_no       TINYINT UNSIGNED NOT NULL DEFAULT 1,
  status           TINYINT         NOT NULL DEFAULT 3 COMMENT '1 作答中 2 已交卷待评分 3 已评分 4 系统强制交卷',
  started_at       DATETIME        NULL,
  submitted_at     DATETIME        NULL,
  remaining_seconds INT UNSIGNED   NULL COMMENT '作答中用于断点续考',
  objective_score  DECIMAL(5,1)    NULL,
  subjective_score DECIMAL(5,1)    NULL,
  total_score      DECIMAL(5,1)    NULL,
  pass_score       DECIMAL(5,1)    NOT NULL DEFAULT 60,
  is_passed        TINYINT(1)      NULL,
  wrong_count      SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  switch_count     SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '切屏次数（防作弊）',
  device_info      VARCHAR(255)    NULL,
  graded_at        DATETIME        NULL,
  created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_attempt_ext (ext_id),
  UNIQUE KEY uk_attempt_no (exam_id, user_id, attempt_no),
  KEY idx_attempt_user (user_id, status),
  CONSTRAINT fk_attempt_exam FOREIGN KEY (exam_id) REFERENCES exam (id) ON DELETE CASCADE,
  CONSTRAINT fk_attempt_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '答卷';

CREATE TABLE attempt_answer (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  attempt_id    BIGINT UNSIGNED NOT NULL,
  question_id   BIGINT UNSIGNED NOT NULL,
  selected_keys VARCHAR(16)     NULL COMMENT '作答选项，多选按字母序拼接；主观题为空',
  answer_text   TEXT            NULL COMMENT '主观题作答内容',
  is_flagged    TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '标记待复查',
  is_correct    TINYINT(1)      NULL,
  score_gained  DECIMAL(5,1)    NULL,
  answered_at   DATETIME        NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_attempt_answer (attempt_id, question_id),
  CONSTRAINT fk_aa_attempt FOREIGN KEY (attempt_id) REFERENCES exam_attempt (id) ON DELETE CASCADE,
  CONSTRAINT fk_aa_question FOREIGN KEY (question_id) REFERENCES exam_question (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '逐题作答明细';

-- -----------------------------------------------------------------------------
-- 6. 证书
-- -----------------------------------------------------------------------------

CREATE TABLE certificate_template (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  ext_id          VARCHAR(32)     NULL COMMENT 'mock 原始 id，如 cert-1',
  title           VARCHAR(200)    NOT NULL,
  level_label     VARCHAR(64)     NULL COMMENT 'LEVEL 4 · 专家级认证',
  category        VARCHAR(32)     NULL COMMENT 'architecture / compliance / devops / ai',
  issue_org       VARCHAR(128)    NULL,
  validity_months SMALLINT UNSIGNED NULL COMMENT '有效期月数，NULL = 长期有效',
  description     VARCHAR(1000)   NULL,
  badge_type      VARCHAR(32)     NULL COMMENT 'blockchain / security / cloud / ai',
  privilege_text  VARCHAR(255)    NULL COMMENT '持证权益',
  created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at      DATETIME        NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_cert_tpl_ext (ext_id),
  KEY idx_cert_tpl_category (category)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '证书模板（种类）';

CREATE TABLE certificate_issue (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  ext_id          VARCHAR(32)     NULL COMMENT 'mock 原始 id，如 aci-3',
  template_id     BIGINT UNSIGNED NOT NULL,
  user_id         BIGINT UNSIGNED NOT NULL COMMENT '持证人',
  serial_number   VARCHAR(64)     NOT NULL COMMENT '证书编号',
  issue_date      DATE            NULL COMMENT 'NULL = 尚未签发',
  expire_date     DATE            NULL COMMENT 'NULL = 长期有效',
  validity_label  VARCHAR(64)     NULL COMMENT '展示文案，如 24 个月 (2027年到期)',
  ranking_score   VARCHAR(64)     NULL COMMENT 'TOP 3% (卓越) / 98 / 100 / Platinum 徽章',
  status          TINYINT         NOT NULL DEFAULT 1 COMMENT '1 待发放 2 已发放 3 已撤销',
  blockchain_hash VARCHAR(128)    NULL,
  issued_by       BIGINT UNSIGNED NULL,
  created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at      DATETIME        NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_cert_issue_ext (ext_id),
  UNIQUE KEY uk_cert_serial (serial_number),
  KEY idx_cert_issue_user (user_id, status),
  CONSTRAINT fk_ci_template FOREIGN KEY (template_id) REFERENCES certificate_template (id),
  CONSTRAINT fk_ci_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '证书签发实例';

-- -----------------------------------------------------------------------------
-- 7. 公告与消息
-- -----------------------------------------------------------------------------

CREATE TABLE announcement (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  ext_id            VARCHAR(32)     NULL COMMENT 'mock 原始 id，如 ann-1',
  title             VARCHAR(200)    NOT NULL,
  content           TEXT            NOT NULL,
  is_top            TINYINT(1)      NOT NULL DEFAULT 0,
  status            TINYINT         NOT NULL DEFAULT 1 COMMENT '1 草稿 2 已发布',
  author_id         BIGINT UNSIGNED NULL,
  author_name       VARCHAR(64)     NULL COMMENT '冗余展示名，支持「系统自动」',
  publish_at        DATETIME        NULL,
  view_count_cached INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '缓存：阅读量（学员端展示）',
  created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at        DATETIME        NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_announcement_ext (ext_id),
  KEY idx_announcement_list (status, is_top, publish_at),
  CONSTRAINT fk_announcement_author FOREIGN KEY (author_id) REFERENCES sys_user (id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '企业培训公告';

CREATE TABLE announcement_read (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  announcement_id BIGINT UNSIGNED NOT NULL,
  user_id         BIGINT UNSIGNED NOT NULL,
  read_at         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_announcement_read (announcement_id, user_id),
  CONSTRAINT fk_ar_announcement FOREIGN KEY (announcement_id) REFERENCES announcement (id) ON DELETE CASCADE,
  CONSTRAINT fk_ar_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '公告已读回执';

CREATE TABLE notification (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  ext_id           VARCHAR(32)     NULL COMMENT 'mock 原始 id，如 msg-1',
  user_id          BIGINT UNSIGNED NOT NULL COMMENT '收件人',
  type             TINYINT         NOT NULL DEFAULT 1 COMMENT '1 考试 2 课程 3 考勤 4 公告 5 培训考勤',
  type_label       VARCHAR(32)     NULL COMMENT '展示用分类名',
  priority         TINYINT         NOT NULL DEFAULT 1 COMMENT '1 普通 2 加急',
  title            VARCHAR(200)    NOT NULL,
  summary          VARCHAR(500)    NULL COMMENT '列表预览',
  content          MEDIUMTEXT      NULL COMMENT '详情正文',
  bullet_points    JSON            NULL COMMENT '详情要点列表',
  sender_name      VARCHAR(64)     NULL,
  official_ref_no  VARCHAR(64)     NULL COMMENT '关联公文号，如 SEC-2025-Q2-0892',
  ref_type         VARCHAR(32)     NULL COMMENT 'exam / course / attendance / announcement',
  ref_id           BIGINT UNSIGNED NULL COMMENT '指向对应业务表主键',
  duration_label   VARCHAR(32)     NULL COMMENT '考试时长 60 分钟',
  passing_score_label VARCHAR(32)  NULL COMMENT '85分及格',
  is_read          TINYINT(1)      NOT NULL DEFAULT 0,
  read_at          DATETIME        NULL,
  created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at       DATETIME        NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_notification_ext (ext_id),
  KEY idx_notification_inbox (user_id, is_read, created_at),
  KEY idx_notification_type (type),
  CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '站内信/通知';

CREATE TABLE notification_attachment (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  notification_id BIGINT UNSIGNED NOT NULL,
  file_name       VARCHAR(200)    NOT NULL,
  file_size_label VARCHAR(32)     NULL COMMENT '4.8 MB；正式实现建议另存 byte 数值列',
  file_url        VARCHAR(512)    NULL,
  source_note     VARCHAR(128)    NULL COMMENT '集团法务合规部编印',
  sort_no         INT             NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_attachment_notification (notification_id),
  CONSTRAINT fk_attachment_notification FOREIGN KEY (notification_id) REFERENCES notification (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '站内信附件';

-- -----------------------------------------------------------------------------
-- 8. 考勤与请假
-- -----------------------------------------------------------------------------

CREATE TABLE leave_request (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  ext_id         VARCHAR(32)     NULL COMMENT 'mock 原始 id，如 alv-1 / lv-1',
  flow_no        VARCHAR(32)     NOT NULL COMMENT '审批流水号 LV-20250518-004',
  user_id        BIGINT UNSIGNED NOT NULL,
  leave_type     VARCHAR(64)     NOT NULL COMMENT '项目紧急发版 / 病假 / 因公外出 / 个人事务',
  training_title VARCHAR(200)    NULL COMMENT '缺席的培训/课程名称',
  course_id      BIGINT UNSIGNED NULL,
  start_date     DATE            NOT NULL,
  end_date       DATE            NULL COMMENT '单日请假可等于 start_date',
  leave_days     DECIMAL(4,1)    NOT NULL DEFAULT 0.5,
  reason         VARCHAR(1000)   NOT NULL,
  status         TINYINT         NOT NULL DEFAULT 1 COMMENT '1 待审批 2 已通过 3 已驳回',
  attachment_url VARCHAR(512)    NULL COMMENT '病假单据等',
  submitted_at   DATETIME        NOT NULL,
  approver_id    BIGINT UNSIGNED NULL,
  approved_at    DATETIME        NULL,
  approve_remark VARCHAR(500)    NULL,
  created_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at     DATETIME        NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_leave_ext (ext_id),
  UNIQUE KEY uk_leave_flow_no (flow_no),
  KEY idx_leave_status (status, submitted_at),
  KEY idx_leave_user (user_id, status),
  CONSTRAINT fk_leave_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE,
  CONSTRAINT fk_leave_approver FOREIGN KEY (approver_id) REFERENCES sys_user (id) ON DELETE SET NULL,
  CONSTRAINT fk_leave_course FOREIGN KEY (course_id) REFERENCES course (id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '培训请假申请';

CREATE TABLE checkin_record (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  ext_id         VARCHAR(32)     NULL COMMENT 'mock 原始 id，如 ck-1',
  user_id        BIGINT UNSIGNED NOT NULL,
  course_id      BIGINT UNSIGNED NULL COMMENT '线下培训场次对应的课程，日常签到为空',
  checkin_date   DATE            NOT NULL,
  check_in_time  TIME            NULL COMMENT '缺勤为 NULL',
  check_out_time TIME            NULL,
  method         TINYINT         NOT NULL DEFAULT 1 COMMENT '1 人脸识别 2 工牌刷卡 3 移动端定位',
  status         TINYINT         NOT NULL DEFAULT 1 COMMENT '1 正常 2 迟到 3 缺勤 4 早退',
  location       VARCHAR(128)    NULL,
  created_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_checkin_ext (ext_id),
  UNIQUE KEY uk_checkin_day (user_id, checkin_date),
  KEY idx_checkin_date (checkin_date, status),
  CONSTRAINT fk_checkin_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '培训签到/签退记录';

-- -----------------------------------------------------------------------------
-- 9. 操作日志（管理总览「最新动态」）
-- -----------------------------------------------------------------------------

CREATE TABLE operation_log (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  ext_id        VARCHAR(32)     NULL COMMENT 'mock 原始 id，如 act-1',
  log_type      VARCHAR(16)     NOT NULL COMMENT 'course / user / exam / notice / approve',
  content       VARCHAR(255)    NOT NULL,
  operator_id   BIGINT UNSIGNED NULL,
  operator_name VARCHAR(64)     NULL COMMENT '冗余展示名，支持「系统自动」',
  ref_type      VARCHAR(32)     NULL,
  ref_id        BIGINT UNSIGNED NULL,
  occurred_at   DATETIME        NOT NULL,
  created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_log_ext (ext_id),
  KEY idx_log_time (occurred_at),
  CONSTRAINT fk_log_operator FOREIGN KEY (operator_id) REFERENCES sys_user (id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '后台操作动态';

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- 视图：把 mock 里写死的统计数字换成可派生的口径
-- =============================================================================

-- 个人档案 / 首页欢迎区 / 管理端学员列表
-- 统计列一律用相关子查询：三张一对多的表同时 LEFT JOIN 会互相放大行数，
-- SUM/COUNT 会得到 12×4×7 这种笛卡尔结果（学时被算成 28 倍）。
CREATE OR REPLACE VIEW v_learner_profile AS
SELECT
  u.id                                AS user_id,
  u.employee_no,
  u.name,
  d.name                              AS department,
  u.job_title,
  u.grade,
  u.specialty,
  u.avatar_url,
  u.role,
  u.account_status,
  u.last_active_at,
  IFNULL((SELECT SUM(e.study_hours) FROM enrollment e
           WHERE e.user_id = u.id), 0)                    AS learning_hours,
  (SELECT COUNT(*) FROM enrollment e
    WHERE e.user_id = u.id AND e.status = 3)              AS completed_courses,
  (SELECT COUNT(*) FROM enrollment e
    WHERE e.user_id = u.id AND e.status = 2)              AS studying_courses,
  (SELECT COUNT(*) FROM enrollment e)                     AS enrolled_courses,
  (SELECT COUNT(*) FROM certificate_issue c
    WHERE c.user_id = u.id AND c.status = 2 AND c.deleted_at IS NULL) AS certificate_count,
  (SELECT COUNT(*) FROM notification n
    WHERE n.user_id = u.id AND n.is_read = 0 AND n.deleted_at IS NULL) AS unread_notifications
FROM sys_user u
LEFT JOIN department d ON d.id = u.department_id
WHERE u.deleted_at IS NULL;

-- 学员端课程卡片：进度、徽章、断点续学
CREATE OR REPLACE VIEW v_course_progress AS
SELECT
  e.user_id,
  c.id                                AS course_id,
  c.title,
  c.total_hours,
  c.total_lesson_count,
  e.status,
  e.progress_percent,
  e.completed_lesson_count,
  e.study_hours,
  e.score,
  e.last_studied_at,
  CASE
    WHEN e.status = 3 THEN CONCAT('已结课 · 成绩优秀 ', e.score, '分')
    WHEN e.status = 2 THEN CONCAT('学习中 · ', e.progress_percent, '%')
    ELSE '未开始'
  END                                 AS badge_text
FROM enrollment e
JOIN course c ON c.id = e.course_id
WHERE c.deleted_at IS NULL;

-- 管理端部门统计 / 部门管理列表
CREATE OR REPLACE VIEW v_department_stats AS
SELECT
  d.id                                AS department_id,
  d.name,
  d.manager_name,
  d.status,
  COUNT(DISTINCT u.id)                AS learner_count,
  IFNULL(SUM(e.study_hours), 0)       AS total_study_hours,
  d.learner_count_cached,
  d.study_hours_cached
FROM department d
LEFT JOIN sys_user u   ON u.department_id = d.id AND u.role = 1 AND u.deleted_at IS NULL
LEFT JOIN enrollment e ON e.user_id = u.id
WHERE d.deleted_at IS NULL
GROUP BY d.id;

-- 考试管理列表：题数与参与人次的真实口径
-- 同样用子查询：题目数与答卷数是一对多，交叉相乘后 AVG(total_score) 会被题目数加权。
CREATE OR REPLACE VIEW v_exam_overview AS
SELECT
  x.id,
  x.code,
  x.title,
  x.status,
  x.pass_score,
  (SELECT COUNT(*) FROM exam_question q
    WHERE q.exam_id = x.id AND q.deleted_at IS NULL)      AS question_count,
  (SELECT COUNT(*) FROM exam_attempt a WHERE a.exam_id = x.id) AS attempt_count,
  (SELECT COUNT(*) FROM exam_attempt a
    WHERE a.exam_id = x.id AND a.is_passed = 1)           AS passed_count,
  (SELECT ROUND(AVG(a.total_score), 1) FROM exam_attempt a
    WHERE a.exam_id = x.id AND a.total_score IS NOT NULL) AS avg_score,
  ROUND((SELECT COUNT(*) FROM exam_attempt a
    WHERE a.exam_id = x.id AND a.is_passed = 1) * 100.0
        / NULLIF((SELECT COUNT(*) FROM exam_attempt a
    WHERE a.exam_id = x.id), 0), 1)                       AS pass_rate,
  x.participant_count_cached
FROM exam x
WHERE x.deleted_at IS NULL;

-- 个人学习看板：按月学时趋势（替换 AnalyticsView 里手绘 SVG 的写死数值）
CREATE OR REPLACE VIEW v_monthly_study_hours AS
SELECT
  s.user_id,
  DATE_FORMAT(s.started_at, '%Y-%m')  AS ym,
  ROUND(SUM(s.duration_minutes) / 60, 1) AS study_hours
FROM study_session s
GROUP BY s.user_id, DATE_FORMAT(s.started_at, '%Y-%m');

-- 考评中心三个指标：待参加 / 作答中 / 往期已考
-- 「往期已考」落在 exam_attempt.status 3 已评分 与 4 系统强制交卷 两种终态上
CREATE OR REPLACE VIEW v_exam_metrics AS
SELECT
  u.id                                                            AS user_id,
  (SELECT COUNT(*) FROM exam_assignment ast
    WHERE ast.user_id = u.id AND ast.status = 1)                  AS pending_count,
  (SELECT COUNT(*) FROM exam_attempt a
    WHERE a.user_id = u.id AND a.status = 1)                      AS in_progress_count,
  (SELECT COUNT(*) FROM exam_attempt a
    WHERE a.user_id = u.id AND a.status IN (3, 4))                AS finished_count,
  (SELECT ROUND(AVG(a.total_score), 1) FROM exam_attempt a
    WHERE a.user_id = u.id AND a.status IN (3, 4)
      AND a.total_score IS NOT NULL)                              AS avg_past_score,
  ROUND((SELECT COUNT(*) FROM exam_attempt a
    WHERE a.user_id = u.id AND a.status IN (3, 4) AND a.is_passed = 1) * 100.0
        / NULLIF((SELECT COUNT(*) FROM exam_attempt a
    WHERE a.user_id = u.id AND a.status IN (3, 4)), 0), 1)        AS pass_rate
FROM sys_user u
WHERE u.deleted_at IS NULL;

-- 请假/证书/公告待办角标（管理端侧栏与仪表盘）
CREATE OR REPLACE VIEW v_admin_pending AS
SELECT
  (SELECT COUNT(*) FROM leave_request WHERE status = 1 AND deleted_at IS NULL)          AS pending_leave_count,
  (SELECT COUNT(*) FROM certificate_issue WHERE status = 1 AND deleted_at IS NULL)      AS pending_cert_count,
  (SELECT COUNT(*) FROM announcement WHERE status = 1 AND deleted_at IS NULL)           AS draft_notice_count,
  (SELECT COUNT(*) FROM exam WHERE status = 3 AND deleted_at IS NULL)                   AS active_exam_count;

-- =============================================================================
-- 迁移期说明
--   1. SCREEN_MOCKUPS 是原型截图资源清单，不属于业务数据，未建表。
--   2. mock 的 currentUserZhang / currentUserLi / currentUserAdmin 与
--      ADMIN_LEARNERS_DATA 合并进 sys_user 一张表；登录账号 employee_no 保留 'admin'。
--   3. COURSES_DATA 的 progress/completedLessons/lastStudied 属于「当前登录人」视角，
--      已拆到 enrollment（按 user_id 区分），课程表本身不再存进度。
--   4. EXAMS_DATA 与 PAST_EXAMS 合并进 exam + exam_attempt：往期记录是「某人的答卷」，
--      不是考试本身；成绩、错题数、完成时间落在 exam_attempt。
--   5. CERTIFICATES_DATA（种类）与 ADMIN_CERTIFICATE_ISSUES（签发）拆成
--      certificate_template + certificate_issue，序列号唯一。
--   6. LEAVE_REQUESTS_DATA 与 ADMIN_LEAVE_REQUESTS 是同一张表的两个视角，
--      合并为 leave_request，靠 flow_no 去重。
--   7. 学时/完成门数/证书数/阅读量等 mock 写死值，改由上面的视图派生；
--      与视图口径不一致的硬编码数字在 seed.sql 顶部逐条列出。
-- =============================================================================
