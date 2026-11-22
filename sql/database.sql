-- =============================================================================
-- 云舒艺境 ENTERPRISE ACADEMY · 数据库初始化脚本（单文件）
-- 内容：建库 + 23 张业务表 + 6 个视图 + 完整演示数据
--
-- 执行：mysql -uroot -p < sql/database.sql
--   脚本内置 CREATE DATABASE 与 USE；命令行不需要再指定数据库。
--
-- 警告：本脚本会 DROP 全部业务表并重新写入演示数据，
--   仅用于新环境初始化或演示环境重建；生产库请勿直接执行。
--   如需换库名，请先复制本文件并替换其中的 enterprise_academy。
--
-- 演示账号：admin / 123456（管理员）、EMP-2024098 / 123456（学员）
-- 结构说明见仓库根目录 DATABASE_SCHEMA.md。
-- =============================================================================

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS enterprise_academy
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE enterprise_academy;

SET FOREIGN_KEY_CHECKS = 0;

-- 已下线的统计视图：旧库重建时先删除，避免残留失效视图
DROP VIEW IF EXISTS v_department_stats;

DROP TABLE IF EXISTS operation_log;
DROP TABLE IF EXISTS checkin_record;
DROP TABLE IF EXISTS leave_request;
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
DROP TABLE IF EXISTS study_session;
DROP TABLE IF EXISTS lesson_progress;
DROP TABLE IF EXISTS enrollment;
DROP TABLE IF EXISTS course_lesson;
DROP TABLE IF EXISTS course_chapter;
DROP TABLE IF EXISTS course;
DROP TABLE IF EXISTS course_category;
DROP TABLE IF EXISTS instructor;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS department;

-- -----------------------------------------------------------------------------
-- 1. 组织与账号
-- -----------------------------------------------------------------------------

CREATE TABLE department (
  id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  name                VARCHAR(64)     NOT NULL COMMENT '部门名称',
  manager_name        VARCHAR(64)     NULL COMMENT '部门负责人姓名',
  description         VARCHAR(255)    NULL COMMENT '部门职责说明',
  status              TINYINT         NOT NULL DEFAULT 1 COMMENT '1 启用 2 停用',
  sort_no             INT             NOT NULL DEFAULT 0 COMMENT '排序序号，越小越靠前',
  created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_at          DATETIME        NULL COMMENT '软删除时间，NULL = 未删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_department_name (name),
  KEY idx_department_status (status, deleted_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '部门';

CREATE TABLE sys_user (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  employee_no     VARCHAR(32)     NOT NULL COMMENT '工号，登录账号',
  password_hash   VARCHAR(128)    NOT NULL DEFAULT '' COMMENT '口令散列；演示种子为空串',
  name            VARCHAR(64)     NOT NULL COMMENT '姓名',
  department_id   BIGINT UNSIGNED NULL COMMENT '所属部门 ID',
  job_title       VARCHAR(64)     NULL COMMENT '职位，如 高级研发工程师',
  avatar_url      VARCHAR(512)    NULL COMMENT '头像地址',
  gender          VARCHAR(8)      NULL COMMENT '性别：男 / 女',
  age             INT             NULL COMMENT '年龄',
  phone           VARCHAR(32)     NULL COMMENT '手机号',
  email           VARCHAR(128)    NULL COMMENT '邮箱',
  role            TINYINT         NOT NULL DEFAULT 1 COMMENT '1 学员 2 管理员',
  account_status  TINYINT         NOT NULL DEFAULT 1 COMMENT '1 正常 2 禁用',
  last_active_at  DATETIME        NULL COMMENT '最近活跃时间',
  onboarding_completed_at DATETIME NULL COMMENT '入职手册阅读完成时间，NULL = 未完成',
  created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_at      DATETIME        NULL COMMENT '软删除时间，NULL = 未删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_employee_no (employee_no),
  KEY idx_user_department (department_id),
  KEY idx_user_list (role, account_status, deleted_at),
  CONSTRAINT fk_user_department FOREIGN KEY (department_id) REFERENCES department (id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '平台用户（学员 + 管理员）';

CREATE TABLE instructor (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  name        VARCHAR(64)     NOT NULL COMMENT '讲师姓名或机构名',
  title       VARCHAR(64)     NULL COMMENT '讲师头衔，如 资深架构师',
  created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_instructor_name_title (name, title)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '讲师档案';

-- -----------------------------------------------------------------------------
-- 2. 课程目录
-- -----------------------------------------------------------------------------

CREATE TABLE course_category (
  id       BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  code     VARCHAR(32)     NOT NULL COMMENT 'core / tech / product / leadership / compliance',
  label    VARCHAR(64)     NOT NULL COMMENT '通识素养 / 技术研发 ...',
  sort_no  INT             NOT NULL DEFAULT 0 COMMENT '排序序号，越小越靠前',
  PRIMARY KEY (id),
  UNIQUE KEY uk_course_category_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '课程一级分类';

CREATE TABLE course (
  id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  title               VARCHAR(200)    NOT NULL COMMENT '课程名称',
  category_id         BIGINT UNSIGNED NOT NULL COMMENT '一级分类 ID',
  subcategory_label   VARCHAR(64)     NULL COMMENT '卡片二级标签，如 研发架构',
  instructor_id       BIGINT UNSIGNED NULL COMMENT '讲师 ID',
  thumbnail_url       VARCHAR(512)    NULL COMMENT '封面图地址',
  summary             VARCHAR(500)    NULL COMMENT '一句话简介',
  status              TINYINT         NOT NULL DEFAULT 1 COMMENT '1 草稿 2 已上架 3 已下架',
  created_by          BIGINT UNSIGNED NULL COMMENT '创建人用户 ID',
  created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_at          DATETIME        NULL COMMENT '软删除时间，NULL = 未删除',
  PRIMARY KEY (id),
  KEY idx_course_category (category_id, status),
  KEY idx_course_list (status, deleted_at, updated_at),
  CONSTRAINT fk_course_category FOREIGN KEY (category_id) REFERENCES course_category (id),
  CONSTRAINT fk_course_instructor FOREIGN KEY (instructor_id) REFERENCES instructor (id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '课程';

CREATE TABLE course_chapter (
  id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  course_id  BIGINT UNSIGNED NOT NULL COMMENT '所属课程 ID',
  title      VARCHAR(200)    NOT NULL COMMENT '章节标题',
  sort_no    INT             NOT NULL DEFAULT 0 COMMENT '排序序号，越小越靠前',
  created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_chapter_course (course_id, sort_no),
  CONSTRAINT fk_chapter_course FOREIGN KEY (course_id) REFERENCES course (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '课程章节';

CREATE TABLE course_lesson (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  chapter_id        BIGINT UNSIGNED NOT NULL COMMENT '所属章节 ID',
  title             VARCHAR(200)    NOT NULL COMMENT '小节标题',
  duration_seconds  INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '课时时长（秒），与 video_url 指向的 MP4 真实时长一致（取整秒）',
  video_url         VARCHAR(512)    NULL COMMENT '为空时前端走占位控制条',
  sort_no           INT             NOT NULL DEFAULT 0 COMMENT '排序序号，越小越靠前',
  created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_lesson_chapter (chapter_id, sort_no),
  CONSTRAINT fk_lesson_chapter FOREIGN KEY (chapter_id) REFERENCES course_chapter (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '课时（章节下的小节）';

-- -----------------------------------------------------------------------------
-- 3. 学习过程
-- -----------------------------------------------------------------------------

CREATE TABLE enrollment (
  id                     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  user_id                BIGINT UNSIGNED NOT NULL COMMENT '学员用户 ID',
  course_id              BIGINT UNSIGNED NOT NULL COMMENT '课程 ID',
  status                 TINYINT         NOT NULL DEFAULT 1 COMMENT '1 未开始 2 学习中 3 已完成',
  progress_percent       TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '缓存：0-100，由后端按完成课时推进',
  completed_lesson_count INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '已完成小节数',
  study_hours            DECIMAL(6,2)    NOT NULL DEFAULT 0 COMMENT '缓存：该门课已投入学时',
  started_at             DATETIME        NULL COMMENT '开始学习时间',
  last_studied_at        DATETIME        NULL COMMENT '断点续学时间',
  created_at             DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at             DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_enrollment (user_id, course_id),
  KEY idx_enrollment_course (course_id, status),
  KEY idx_enrollment_recent (user_id, last_studied_at),
  CONSTRAINT fk_enrollment_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE,
  CONSTRAINT fk_enrollment_course FOREIGN KEY (course_id) REFERENCES course (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '选课与学习进度';

CREATE TABLE lesson_progress (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  user_id          BIGINT UNSIGNED NOT NULL COMMENT '学员用户 ID',
  lesson_id        BIGINT UNSIGNED NOT NULL COMMENT '小节 ID',
  is_completed     TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '1 已完成 0 未完成',
  watched_seconds  INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '断点续播位置',
  updated_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_lesson_progress (user_id, lesson_id),
  CONSTRAINT fk_lp_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE,
  CONSTRAINT fk_lp_lesson FOREIGN KEY (lesson_id) REFERENCES course_lesson (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '课时级完成明细';

CREATE TABLE study_session (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  user_id          BIGINT UNSIGNED NOT NULL COMMENT '学员用户 ID',
  course_id        BIGINT UNSIGNED NULL COMMENT '课程 ID',
  lesson_id        BIGINT UNSIGNED NULL COMMENT '小节 ID',
  started_at       DATETIME        NOT NULL COMMENT '学习开始时间',
  duration_minutes DECIMAL(6,1)    NOT NULL DEFAULT 0 COMMENT '本次学习时长（分钟）',
  created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_session_user_time (user_id, started_at),
  KEY idx_session_user_lesson (user_id, lesson_id),
  CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE,
  CONSTRAINT fk_session_course FOREIGN KEY (course_id) REFERENCES course (id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '学时流水（个人看板月度趋势的数据源）';

-- -----------------------------------------------------------------------------
-- 4. 考试与题库
-- -----------------------------------------------------------------------------

CREATE TABLE exam (
  id                     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  code                   VARCHAR(64)     NOT NULL COMMENT '考试编号 ZK-... / CER-...',
  title                  VARCHAR(200)    NOT NULL COMMENT '考试名称',
  organizer              VARCHAR(128)    NULL COMMENT '主办方，如 技术合规部 · 2025Q2',
  status                 TINYINT         NOT NULL DEFAULT 1 COMMENT '1 草稿 2 未开始 3 进行中 4 已结束',
  start_at               DATETIME        NULL COMMENT 'mock timeWindow 前半段',
  end_at                 DATETIME        NULL COMMENT 'mock timeWindow 后半段',
  duration_minutes       SMALLINT UNSIGNED NOT NULL DEFAULT 60 COMMENT '考试时长（分钟）',
  total_score            DECIMAL(5,1)    NOT NULL DEFAULT 100 COMMENT '卷面总分',
  pass_score             DECIMAL(5,1)    NOT NULL DEFAULT 60 COMMENT '及格分',
  created_by             BIGINT UNSIGNED NULL COMMENT '创建人用户 ID',
  created_at             DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at             DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_at             DATETIME        NULL COMMENT '软删除时间，NULL = 未删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_exam_code (code),
  KEY idx_exam_status (status, deleted_at, end_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '考试/测评';

CREATE TABLE exam_question (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  exam_id         BIGINT UNSIGNED NULL COMMENT '为空 = 未组卷的备用题',
  question_no     SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '题号',
  type            TINYINT         NOT NULL DEFAULT 1 COMMENT '1 单选 2 多选 3 判断 4 应用/简答',
  score           DECIMAL(5,1)    NOT NULL DEFAULT 2 COMMENT '题目分值：单选 2 / 多选 4 / 判断 2 / 应用题 8（应用层兜底，此为单选默认值）',
  title           VARCHAR(1000)   NOT NULL COMMENT '题干',
  correct_answer  VARCHAR(16)     NULL COMMENT '客观题答案：单选/判断 "A"，多选 "ABD"；主观题为空',
  reference_answer TEXT           NULL COMMENT '应用题参考答案/评分要点',
  analysis        VARCHAR(1000)   NULL COMMENT '整题解析',
  created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_at      DATETIME        NULL COMMENT '软删除时间，NULL = 未删除',
  PRIMARY KEY (id),
  KEY idx_question_exam (exam_id, question_no),
  KEY idx_question_type (type),
  CONSTRAINT fk_question_exam FOREIGN KEY (exam_id) REFERENCES exam (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '题目';

CREATE TABLE question_option (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  question_id  BIGINT UNSIGNED NOT NULL COMMENT '所属题目 ID',
  option_key   CHAR(1)         NOT NULL COMMENT 'A/B/C/D',
  content      VARCHAR(1000)   NOT NULL COMMENT '选项内容',
  explanation  VARCHAR(1000)   NULL COMMENT '选项级解析（错题回顾用）',
  sort_no      INT             NOT NULL DEFAULT 0 COMMENT '排序序号，越小越靠前',
  PRIMARY KEY (id),
  UNIQUE KEY uk_option (question_id, option_key),
  CONSTRAINT fk_option_question FOREIGN KEY (question_id) REFERENCES exam_question (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '题目选项';

CREATE TABLE exam_assignment (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  exam_id         BIGINT UNSIGNED NOT NULL COMMENT '考试 ID',
  user_id         BIGINT UNSIGNED NOT NULL COMMENT '被指派学员用户 ID',
  status          TINYINT         NOT NULL DEFAULT 1 COMMENT '1 待参加 2 已完成 3 已逾期',
  attempts_allowed TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '允许作答次数',
  attempts_used   TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '已用作答次数',
  deadline_at     DATETIME        NULL COMMENT '指派截止时间',
  created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_assignment (exam_id, user_id),
  KEY idx_assignment_user (user_id, status),
  CONSTRAINT fk_assignment_exam FOREIGN KEY (exam_id) REFERENCES exam (id) ON DELETE CASCADE,
  CONSTRAINT fk_assignment_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '考试指派（谁该考哪场）';

CREATE TABLE exam_attempt (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  exam_id          BIGINT UNSIGNED NOT NULL COMMENT '考试 ID',
  user_id          BIGINT UNSIGNED NOT NULL COMMENT '考生用户 ID',
  attempt_no       TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '第几次作答',
  status           TINYINT         NOT NULL DEFAULT 3 COMMENT '1 作答中 2 已交卷待评分 3 已评分 4 系统强制交卷',
  started_at       DATETIME        NULL COMMENT '开始作答时间',
  submitted_at     DATETIME        NULL COMMENT '交卷时间，未交卷为 NULL',
  remaining_seconds INT UNSIGNED   NULL COMMENT '作答中用于断点续考',
  objective_score  DECIMAL(5,1)    NULL COMMENT '客观题得分',
  subjective_score DECIMAL(5,1)    NULL COMMENT '主观题得分',
  total_score      DECIMAL(5,1)    NULL COMMENT '总得分',
  pass_score       DECIMAL(5,1)    NOT NULL DEFAULT 60 COMMENT '本场及格分',
  is_passed        TINYINT(1)      NULL COMMENT '1 通过 0 未通过',
  wrong_count      SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '答错题数',
  graded_at        DATETIME        NULL COMMENT '判分完成时间',
  created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_attempt_no (exam_id, user_id, attempt_no),
  KEY idx_attempt_user (user_id, status),
  CONSTRAINT fk_attempt_exam FOREIGN KEY (exam_id) REFERENCES exam (id) ON DELETE CASCADE,
  CONSTRAINT fk_attempt_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '答卷';

CREATE TABLE attempt_answer (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  attempt_id    BIGINT UNSIGNED NOT NULL COMMENT '答卷 ID',
  question_id   BIGINT UNSIGNED NOT NULL COMMENT '题目 ID',
  selected_keys VARCHAR(16)     NULL COMMENT '作答选项，多选按字母序拼接；主观题为空',
  answer_text   TEXT            NULL COMMENT '主观题作答内容',
  is_flagged    TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '标记待复查',
  score_gained  DECIMAL(5,1)    NULL COMMENT '本题得分',
  answered_at   DATETIME        NULL COMMENT '作答时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_attempt_answer (attempt_id, question_id),
  CONSTRAINT fk_aa_attempt FOREIGN KEY (attempt_id) REFERENCES exam_attempt (id) ON DELETE CASCADE,
  CONSTRAINT fk_aa_question FOREIGN KEY (question_id) REFERENCES exam_question (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '逐题作答明细';

-- -----------------------------------------------------------------------------
-- 5. 证书
-- -----------------------------------------------------------------------------

CREATE TABLE certificate_template (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  title           VARCHAR(200)    NOT NULL COMMENT '证书名称',
  level_label     VARCHAR(64)     NULL COMMENT 'LEVEL 4 · 专家级认证',
  category        VARCHAR(32)     NULL COMMENT 'architecture / compliance / devops / ai',
  issue_org       VARCHAR(128)    NULL COMMENT '颁发机构',
  validity_months SMALLINT UNSIGNED NULL COMMENT '有效期月数，NULL = 长期有效',
  description     VARCHAR(1000)   NULL COMMENT '证书说明',
  privilege_text  VARCHAR(255)    NULL COMMENT '持证权益',
  created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_at      DATETIME        NULL COMMENT '软删除时间，NULL = 未删除',
  PRIMARY KEY (id),
  KEY idx_cert_tpl_category (category)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '证书模板（种类）';

CREATE TABLE certificate_issue (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  template_id     BIGINT UNSIGNED NOT NULL COMMENT '证书模板 ID',
  user_id         BIGINT UNSIGNED NOT NULL COMMENT '持证人',
  serial_number   VARCHAR(64)     NOT NULL COMMENT '证书编号',
  issue_date      DATE            NULL COMMENT 'NULL = 尚未签发',
  expire_date     DATE            NULL COMMENT 'NULL = 长期有效',
  validity_label  VARCHAR(64)     NULL COMMENT '展示文案，如 24 个月 (2027年到期)',
  status          TINYINT         NOT NULL DEFAULT 1 COMMENT '1 待发放 2 已发放 3 已撤销',
  blockchain_hash VARCHAR(128)    NULL COMMENT '区块链存证哈希',
  issued_by       BIGINT UNSIGNED NULL COMMENT '签发人用户 ID',
  created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_at      DATETIME        NULL COMMENT '软删除时间，NULL = 未删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_cert_serial (serial_number),
  KEY idx_cert_issue_user (user_id, status),
  CONSTRAINT fk_ci_template FOREIGN KEY (template_id) REFERENCES certificate_template (id),
  CONSTRAINT fk_ci_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '证书签发实例';

-- -----------------------------------------------------------------------------
-- 6. 公告与消息
-- -----------------------------------------------------------------------------

CREATE TABLE announcement (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  title             VARCHAR(200)    NOT NULL COMMENT '公告标题',
  content           TEXT            NOT NULL COMMENT '公告正文',
  is_top            TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '1 置顶 0 普通',
  status            TINYINT         NOT NULL DEFAULT 1 COMMENT '1 草稿 2 已发布',
  author_id         BIGINT UNSIGNED NULL COMMENT '发布人用户 ID',
  author_name       VARCHAR(64)     NULL COMMENT '冗余展示名，支持「系统自动」',
  publish_at        DATETIME        NULL COMMENT '发布时间，草稿为 NULL',
  created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_at        DATETIME        NULL COMMENT '软删除时间，NULL = 未删除',
  PRIMARY KEY (id),
  KEY idx_announcement_list (status, is_top, publish_at),
  CONSTRAINT fk_announcement_author FOREIGN KEY (author_id) REFERENCES sys_user (id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '企业培训公告';

CREATE TABLE announcement_read (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  announcement_id BIGINT UNSIGNED NOT NULL COMMENT '公告 ID',
  user_id         BIGINT UNSIGNED NOT NULL COMMENT '阅读人用户 ID',
  read_at         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_announcement_read (announcement_id, user_id),
  CONSTRAINT fk_ar_announcement FOREIGN KEY (announcement_id) REFERENCES announcement (id) ON DELETE CASCADE,
  CONSTRAINT fk_ar_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '公告已读回执';

-- -----------------------------------------------------------------------------
-- 7. 考勤与请假
-- -----------------------------------------------------------------------------

CREATE TABLE leave_request (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  flow_no        VARCHAR(32)     NOT NULL COMMENT '审批流水号 LV-20250518-004',
  user_id        BIGINT UNSIGNED NOT NULL COMMENT '申请人用户 ID',
  leave_type     VARCHAR(64)     NOT NULL COMMENT '项目紧急发版 / 病假 / 因公外出 / 个人事务',
  start_date     DATE            NOT NULL COMMENT '请假开始日期',
  end_date       DATE            NULL COMMENT '单日请假可等于 start_date',
  leave_days     DECIMAL(4,1)    NOT NULL DEFAULT 0.5 COMMENT '请假天数',
  reason         VARCHAR(1000)   NOT NULL COMMENT '请假事由',
  status         TINYINT         NOT NULL DEFAULT 1 COMMENT '1 待审批 2 已通过 3 已驳回',
  submitted_at   DATETIME        NOT NULL COMMENT '提交时间',
  approver_id    BIGINT UNSIGNED NULL COMMENT '审批人用户 ID',
  approved_at    DATETIME        NULL COMMENT '审批时间',
  approve_remark VARCHAR(500)    NULL COMMENT '审批意见',
  created_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_at     DATETIME        NULL COMMENT '软删除时间，NULL = 未删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_leave_flow_no (flow_no),
  KEY idx_leave_status (status, submitted_at),
  KEY idx_leave_user (user_id, status),
  CONSTRAINT fk_leave_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE,
  CONSTRAINT fk_leave_approver FOREIGN KEY (approver_id) REFERENCES sys_user (id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '培训请假申请';

CREATE TABLE checkin_record (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  user_id        BIGINT UNSIGNED NOT NULL COMMENT '签到人用户 ID',
  checkin_date   DATE            NOT NULL COMMENT '签到日期',
  check_in_time  TIME            NULL COMMENT '缺勤为 NULL',
  check_out_time TIME            NULL COMMENT '签退时间，未签退为 NULL',
  status         TINYINT         NOT NULL DEFAULT 1 COMMENT '1 正常 2 迟到 3 缺勤 4 早退',
  created_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_checkin_day (user_id, checkin_date),
  KEY idx_checkin_date (checkin_date, status),
  CONSTRAINT fk_checkin_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '培训签到/签退记录';

-- -----------------------------------------------------------------------------
-- 8. 操作日志（管理总览「最新动态」）
-- -----------------------------------------------------------------------------

CREATE TABLE operation_log (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  log_type      VARCHAR(16)     NOT NULL COMMENT 'course / user / exam / notice / approve',
  content       VARCHAR(255)    NOT NULL COMMENT '操作内容描述',
  operator_id   BIGINT UNSIGNED NULL COMMENT '操作人用户 ID',
  operator_name VARCHAR(64)     NULL COMMENT '冗余展示名，支持「系统自动」',
  ref_type      VARCHAR(32)     NULL COMMENT '关联业务类型，如 course / exam',
  ref_id        BIGINT UNSIGNED NULL COMMENT '关联业务 ID',
  occurred_at   DATETIME        NOT NULL COMMENT '操作发生时间',
  created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
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
  u.avatar_url,
  u.role,
  u.account_status,
  u.last_active_at,
  IFNULL((SELECT SUM(e.study_hours) FROM enrollment e
           WHERE e.user_id = u.id), 0)                    AS learning_hours,
  (SELECT COUNT(*) FROM enrollment e
    WHERE e.user_id = u.id AND e.status = 3)              AS completed_courses,
  (SELECT COUNT(*) FROM certificate_issue c
    WHERE c.user_id = u.id AND c.status = 2 AND c.deleted_at IS NULL) AS certificate_count
FROM sys_user u
LEFT JOIN department d ON d.id = u.department_id
WHERE u.deleted_at IS NULL;

-- 学员端课程卡片：进度、徽章、断点续学
CREATE OR REPLACE VIEW v_course_progress AS
SELECT
  e.user_id,
  c.id                                AS course_id,
  c.title,
  e.status,
  e.progress_percent,
  e.completed_lesson_count,
  e.study_hours,
  e.last_studied_at,
  CASE
    WHEN e.status = 3 THEN '已结课'
    WHEN e.status = 2 THEN CONCAT('学习中 · ', e.progress_percent, '%')
    ELSE '未开始'
  END                                 AS badge_text
FROM enrollment e
JOIN course c ON c.id = e.course_id
WHERE c.deleted_at IS NULL;

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
  (SELECT COUNT(*) FROM exam_attempt a WHERE a.exam_id = x.id) AS attempt_count
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
-- 演示数据（重新写入全部业务表）
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE operation_log;
TRUNCATE TABLE checkin_record;
TRUNCATE TABLE leave_request;
TRUNCATE TABLE announcement_read;
TRUNCATE TABLE announcement;
TRUNCATE TABLE certificate_issue;
TRUNCATE TABLE certificate_template;
TRUNCATE TABLE attempt_answer;
TRUNCATE TABLE exam_attempt;
TRUNCATE TABLE exam_assignment;
TRUNCATE TABLE question_option;
TRUNCATE TABLE exam_question;
TRUNCATE TABLE exam;
TRUNCATE TABLE study_session;
TRUNCATE TABLE lesson_progress;
TRUNCATE TABLE enrollment;
TRUNCATE TABLE course_lesson;
TRUNCATE TABLE course_chapter;
TRUNCATE TABLE course;
TRUNCATE TABLE course_category;
TRUNCATE TABLE sys_user;
TRUNCATE TABLE instructor;
TRUNCATE TABLE department;

-- -----------------------------------------------------------------------------
-- 部门（ADMIN_DEPARTMENTS + 签到/请假数据里出现但目录缺失的培训部、平台运营部）
-- -----------------------------------------------------------------------------
INSERT INTO department (id, name, manager_name, description, status, sort_no) VALUES
(1, '研发部', '陈建邦', '负责核心业务系统研发与架构治理', 1, 1),
(2, '产品部', '陈嘉怡', '产品规划、需求分析与用户体验设计', 1, 2),
(3, '测试部', '刘子墨', '质量保障、自动化测试与发布验收', 1, 3),
(4, '运维部', '赵思远', '基础设施、SRE 与生产稳定性保障', 1, 4),
(5, '市场部', '孙雨桐', '品牌运营、市场推广与增长', 1, 5),
(6, '财务部', '周凯文', '财务核算、预算与经营分析', 1, 6),
(7, '人力资源部', '吴梦琪', '组织发展、人才招聘与员工关系', 1, 7),
(8, '法务部', '王若琳', '合规审查、合同与知识产权管理', 2, 8),
(9, '培训部', '林欣怡', '企业大学课程运营与内训师管理', 1, 9),
(10, '平台运营部', '王智远', '学习平台运营、账号与权限管理', 1, 10);

-- -----------------------------------------------------------------------------
-- 讲师（课程 instructor 字段去重；同名不同头衔按机构/岗位分开）
-- -----------------------------------------------------------------------------
INSERT INTO instructor (id, name, title) VALUES
(1, '陈建邦', '资深架构师'),
(2, '集团安全部', '信息合规组'),
(3, '林欣怡', 'PMP 专家教练'),
(4, '赵明哲', '基础设施技术专家'),
(5, '周文雅', '资深EAP特聘导师'),
(6, '韩立言', '涉外合规法务总监'),
(7, '郑亦辰', '算法平台负责人'),
(8, '许清和', '资深产品专家'),
(9, '王若琳', '数据合规法务顾问'),
(10, '赵明哲', '数据平台架构师'),
(11, '周文雅', '资深表达教练');

-- -----------------------------------------------------------------------------
-- 用户（ADMIN_LEARNERS_DATA 10 人 + 管理员 + 请假/签到引用到的 4 人）
-- 注：employee_no 保留 mock 的 'admin'，与前端登录分支 (employeeId === 'admin') 对齐
-- -----------------------------------------------------------------------------
INSERT INTO sys_user (id, employee_no, name, department_id, job_title, avatar_url, role, account_status, last_active_at) VALUES
(1, 'EMP-2024098', '李明', 1, '高级研发工程师', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=120&q=80', 1, 1, '2025-05-14 09:42:00'),
(2, 'EXP-70291', '张浩然', 1, '高级工程师', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=120&q=80', 1, 1, '2025-05-14 08:15:00'),
(3, 'EMP-2024156', '陈嘉怡', 2, '产品经理', 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=120&q=80', 1, 1, '2025-05-13 17:30:00'),
(4, 'EMP-2023877', '刘子墨', 3, '测试开发工程师', 'https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&w=120&q=80', 1, 1, '2025-05-14 07:58:00'),
(5, 'EMP-2023512', '赵思远', 4, 'SRE 工程师', 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=120&q=80', 1, 1, '2025-05-12 18:20:00'),
(6, 'EMP-2024620', '孙雨桐', 5, '品牌运营专员', 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=120&q=80', 1, 1, '2025-05-14 10:02:00'),
(7, 'EMP-2023340', '周凯文', 6, '财务分析师', 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=120&q=80', 1, 1, '2025-05-11 15:40:00'),
(8, 'EMP-2024901', '吴梦琪', 7, 'HRBP', 'https://images.unsplash.com/photo-1580489944761-15a19d654956?auto=format&fit=crop&w=120&q=80', 1, 1, '2025-05-13 11:20:00'),
(9, 'EMP-2023788', '郑天翔', 1, '前端工程师', 'https://images.unsplash.com/photo-1506794778695-cb8523da5808?auto=format&fit=crop&w=120&q=80', 1, 2, '2025-05-02 10:00:00'),
(10, 'EMP-2024402', '王若琳', 8, '合规专员', 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=120&q=80', 1, 1, '2025-05-14 08:47:00'),
(11, 'admin', '王智远', 10, '超级管理员', 'https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&w=120&q=80', 2, 1, '2025-05-14 10:24:00'),
(12, 'EMP-2023115', '许清和', 2, '资深产品专家', NULL, 1, 1, '2025-05-13 09:10:00'),
(13, 'EMP-2022890', '韩立言', 8, '涉外合规法务总监', NULL, 1, 1, '2025-05-14 09:05:00'),
(14, 'EMP-2022551', '林欣怡', 9, 'PMP 专家教练', NULL, 1, 1, '2025-05-14 08:31:00'),
(15, 'EMP-2023056', '陈建邦', 1, '资深架构师', NULL, 1, 1, '2025-05-14 09:06:00');

-- -----------------------------------------------------------------------------
-- 课程分类
-- -----------------------------------------------------------------------------
INSERT INTO course_category (id, code, label, sort_no) VALUES
(1, 'core',        '通识素养',       1),
(2, 'tech',        '技术研发',       2),
(3, 'product',     '产品与业务',     3),
(4, 'leadership',  '领导力与管理',   4),
(5, 'compliance',  '职场素养与安全', 5);

-- -----------------------------------------------------------------------------
-- 课程（COURSES_DATA c1-c12；进度类字段已移到 enrollment）
-- -----------------------------------------------------------------------------
INSERT INTO course (id, title, category_id, subcategory_label, instructor_id, thumbnail_url, summary, status, created_by) VALUES
(1, 'Spring Boot 3 + MyBatis-Plus 企业级高并发架构', 2, '研发架构', 1, 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=600&q=80', '深度解析十万级 QPS 场景下的缓存穿透熔断、分布式分布式锁性能压榨与底层 SQL 执行优化秘籍。', 2, 11),
(2, '企业网络安全防护规范与代码安全审判指南', 5, '年度红线合规', 2, 'https://images.unsplash.com/photo-1550751827-4bd374c3f58b?auto=format&fit=crop&w=600&q=80', '系统剖析SQL注入防御、敏感数据脱敏规约、第三方依赖漏洞扫描与全链路防基线实操规范。', 2, 11),
(3, '高效跨部门沟通与敏捷项目管理实践', 4, '项目管理', 3, 'https://images.unsplash.com/photo-1522071820081-009f0129c71c?auto=format&fit=crop&w=600&q=80', '拆解 OKR 对齐困境、Scrum 迭代节奏控制、技术与业务语言翻译技巧，助力打造高效协作自驱团队。', 2, 11),
(4, '云原生微服务观测: Prometheus + OpenTelemetry', 2, 'SRE & 运维', 4, 'https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=600&q=80', '从指标采集、服务拓扑关系链路跟踪到核心告警策略实战，打通可观测性体系全生命周期闭环。', 2, 11),
(5, '职场心理学与压力管理工坊', 1, '素养选修', 5, 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=600&q=80', '国家二级心理咨询师亲授认知重构模型，助你在快节奏交付中建立身心心理韧性，告别职场耗竭。', 2, 11),
(6, '金融级数据合规与业务出海跨境传输实战', 5, '法务合规', 6, 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=600&q=80', '系统解读 GDPR、新加坡 PDPA 与国内数据出境评估申报全流程，避开海外隐私保护巨额罚单。', 2, 11),
(7, 'AI 大模型应用开发: 从 Prompt 工程到 RAG 实战', 2, '人工智能', 7, 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=600&q=80', '从提示词设计、向量检索到知识库切分与召回评估,完整搭建企业级 RAG 应用链路。', 2, 11),
(8, '产品需求分析与用户旅程地图实战', 3, '产品与业务', 8, 'https://images.unsplash.com/photo-1522071820081-009f0129c71c?auto=format&fit=crop&w=600&q=80', '拆解真实业务场景下的需求澄清、优先级排序与用户旅程地图绘制方法,提升需求交付质量。', 2, 11),
(9, '高绩效团队目标管理与 OKR 落地', 4, '领导力与管理', 3, 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=600&q=80', '围绕 OKR 制定、对齐与复盘,结合团队激励与反馈机制,帮助管理者把目标真正落到日常执行。', 2, 11),
(10, '数据可视化与 BI 报表体系搭建', 2, '数据分析', 10, 'https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=600&q=80', '从指标体系设计、数据建模到可视化图表选型,搭建可持续迭代的企业 BI 报表体系。', 2, 11),
(11, '商务演讲与结构化表达训练营', 1, '通识素养', 11, 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=600&q=80', '用金字塔结构与故事化表达组织内容,在汇报、路演与跨部门沟通中清晰传递关键信息。', 2, 11),
(12, '个人信息保护法与员工数据合规实务', 5, '法务合规', 9, 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=600&q=80', '结合《个人信息保护法》与内部员工数据管理场景,讲清收集、存储、使用与删除的合规边界。', 2, 11);

-- -----------------------------------------------------------------------------
-- 章节目录（c1 三章 13 课时，c3 一章 1 课时）
-- duration：按 video_url 指向的 MP4 真实时长取整（与播放器选集显示一致）
-- video_url 必须是可直接播放的媒体文件（.mp4 等）：
--   <video src> 不能播放 B 站 / YouTube 的「观看页」链接，否则前端报「视频加载失败」。
--   生产环境请先把视频上传到 MinIO，再填 /api/files/view/{objectName} 的稳定地址。
-- -----------------------------------------------------------------------------
INSERT INTO course_chapter (id, course_id, title, sort_no) VALUES
(1, 1, '第 1 章: 微服务架构选型与升级', 1),
(2, 1, '第 2 章: 高并发削峰与消息事务实战', 2),
(3, 1, '第 3 章: 分布式缓存与数据一致性', 3),
(4, 3, '第 1 章: 敏捷协作与 Scrum 基础', 1);

INSERT INTO course_lesson (id, chapter_id, title, duration_seconds, video_url, sort_no) VALUES
(1, 1, '1.1 微服务架构选型与升级路线', 10, 'https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/360/Big_Buck_Bunny_360_10s_1MB.mp4', 1),
(2, 1, '1.2 Spring Boot 3 核心与自动装配', 52, 'https://media.w3.org/2010/05/sintel/trailer.mp4', 2),
(3, 1, '1.3 高并发场景下的架构权衡', 47, 'https://vjs.zencdn.net/v/oceans.mp4', 3),
(4, 2, '2.1 削峰填谷与消息队列选型', 10, 'https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/360/Big_Buck_Bunny_360_10s_1MB.mp4', 1),
(5, 2, '2.2 消息事务与最终一致性方案', 52, 'https://media.w3.org/2010/05/sintel/trailer.mp4', 2),
(6, 3, '3.1 Redis 哨兵与 Cluster 集群架构选型', 10, 'https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/360/Big_Buck_Bunny_360_10s_1MB.mp4', 1),
(7, 3, '3.2 Redis 集群分布式锁与缓存击穿防护', 52, 'https://media.w3.org/2010/05/sintel/trailer.mp4', 2),
(8, 3, '3.3 缓存穿透布隆过滤器实战', 47, 'https://vjs.zencdn.net/v/oceans.mp4', 3),
(9, 4, '1.1 敏捷协作与 Scrum 基础', 47, 'https://vjs.zencdn.net/v/oceans.mp4', 1),
-- 以下为补充课时：各自使用独立的 MP4，选集里不再重复同一段视频
(10, 1, '1.4 服务网格与流量治理实践', 10, 'https://test-videos.co.uk/vids/jellyfish/mp4/h264/360/Jellyfish_360_10s_1MB.mp4', 4),
(11, 2, '2.3 消息幂等与重复消费治理', 10, 'https://test-videos.co.uk/vids/sintel/mp4/h264/360/Sintel_360_10s_1MB.mp4', 3),
(12, 2, '2.4 限流降级与熔断策略落地', 10, 'https://www.w3schools.com/html/mov_bbb.mp4', 4),
(13, 3, '3.4 双写一致性与延迟双删方案', 13, 'https://www.w3schools.com/html/movie.mp4', 4),
(14, 3, '3.5 热点 Key 探测与多级缓存实战', 10, 'https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/720/Big_Buck_Bunny_720_10s_2MB.mp4', 5);

-- -----------------------------------------------------------------------------
-- 学习进度（COURSES_DATA 的 progress / completedLessons / lastStudied 拆到人头上）
-- 李明(u1) 的 enrollment 精确拆成 18.5 学时、3 门已结课，与 mock 档案一致
-- -----------------------------------------------------------------------------
INSERT INTO enrollment (user_id, course_id, status, progress_percent, completed_lesson_count, study_hours, started_at, last_studied_at) VALUES
-- 李明：3 完成 + 6 学习中 + 3 未开始 = 18.5h
(1, 3, 3, 100, 16, 4.00, '2025-02-10 09:00:00', '2025-03-02 17:20:00'),
(1, 9, 3, 100, 10, 1.00, '2025-03-05 09:00:00', '2025-03-20 16:10:00'),
(1, 5, 3, 100, 8, 1.00, '2025-01-08 09:00:00', '2025-01-22 11:30:00'),
(1, 1, 2, 66, 21, 5.50, '2025-04-02 09:00:00', '2025-05-14 09:00:00'),
(1, 4, 2, 35, 8, 2.50, '2025-04-18 14:00:00', '2025-05-12 10:20:00'),
(1, 6, 2, 15, 3, 1.50, '2025-05-06 09:30:00', '2025-05-11 15:40:00'),
(1, 7, 2, 45, 9, 2.00, '2025-04-20 19:00:00', '2025-05-13 20:10:00'),
(1, 10, 2, 25, 4, 0.50, '2025-05-08 09:00:00', '2025-05-09 18:00:00'),
(1, 12, 2, 60, 4, 0.50, '2025-05-02 14:00:00', '2025-05-13 09:30:00'),
(1, 2, 1, 0, 0, 0.00, NULL, NULL),
(1, 8, 1, 0, 0, 0.00, NULL, NULL),
(1, 11, 1, 0, 0, 0.00, NULL, NULL),
-- 张浩然 42.5h / 8 完成
(2, 1, 3, 100, 32, 12.00, '2024-11-01 09:00:00', '2025-02-14 18:00:00'),
(2, 3, 3, 100, 16, 4.00, '2025-01-06 09:00:00', '2025-02-01 12:00:00'),
(2, 4, 3, 100, 24, 6.00, '2025-01-10 09:00:00', '2025-03-08 17:00:00'),
(2, 6, 3, 100, 18, 3.50, '2025-02-12 09:00:00', '2025-03-25 16:00:00'),
(2, 7, 3, 100, 20, 4.00, '2025-03-01 09:00:00', '2025-04-10 15:00:00'),
(2, 10, 3, 100, 16, 3.00, '2025-03-14 09:00:00', '2025-04-20 11:00:00'),
(2, 12, 3, 100, 6, 2.00, '2025-04-01 09:00:00', '2025-04-24 10:00:00'),
(2, 5, 3, 100, 8, 1.00, '2025-04-08 09:00:00', '2025-04-30 14:00:00'),
(2, 2, 2, 70, 8, 5.00, '2025-05-02 09:00:00', '2025-05-14 08:15:00'),
(2, 8, 2, 40, 5, 2.00, '2025-05-06 09:00:00', '2025-05-12 09:40:00'),
-- 陈嘉怡 12.0h / 2
(3, 3, 3, 100, 16, 4.00, '2025-03-02 09:00:00', '2025-04-06 16:00:00'),
(3, 8, 3, 100, 12, 3.00, '2025-03-20 09:00:00', '2025-04-28 15:00:00'),
(3, 1, 2, 30, 10, 3.00, '2025-05-02 09:00:00', '2025-05-13 17:30:00'),
(3, 11, 2, 25, 2, 2.00, '2025-05-08 09:00:00', '2025-05-11 10:00:00'),
-- 刘子墨 26.0h / 5
(4, 2, 3, 100, 12, 5.00, '2025-01-09 09:00:00', '2025-02-18 17:00:00'),
(4, 12, 3, 100, 6, 4.00, '2025-02-20 09:00:00', '2025-03-15 16:00:00'),
(4, 5, 3, 100, 8, 2.00, '2025-03-01 09:00:00', '2025-03-22 14:00:00'),
(4, 3, 3, 100, 16, 4.00, '2025-03-10 09:00:00', '2025-04-12 11:00:00'),
(4, 10, 3, 100, 16, 3.00, '2025-04-02 09:00:00', '2025-04-30 15:00:00'),
(4, 1, 2, 55, 18, 5.00, '2025-04-20 09:00:00', '2025-05-14 07:58:00'),
(4, 6, 2, 20, 4, 3.00, '2025-05-05 09:00:00', '2025-05-12 16:20:00'),
-- 赵思远 31.5h / 6
(5, 4, 3, 100, 24, 8.00, '2024-12-02 09:00:00', '2025-01-25 18:00:00'),
(5, 1, 3, 100, 32, 6.00, '2025-01-28 09:00:00', '2025-03-04 17:00:00'),
(5, 2, 3, 100, 12, 5.00, '2025-02-06 09:00:00', '2025-03-11 16:00:00'),
(5, 10, 3, 100, 16, 3.50, '2025-03-12 09:00:00', '2025-04-02 15:00:00'),
(5, 12, 3, 100, 6, 2.00, '2025-04-05 09:00:00', '2025-04-18 14:00:00'),
(5, 5, 3, 100, 8, 1.00, '2025-04-20 09:00:00', '2025-05-02 11:00:00'),
(5, 7, 2, 45, 9, 4.00, '2025-05-03 09:00:00', '2025-05-12 18:20:00'),
(5, 3, 2, 12, 2, 2.00, '2025-05-09 09:00:00', '2025-05-11 09:30:00'),
-- 孙雨桐 6.0h / 1
(6, 11, 3, 100, 8, 2.00, '2025-04-10 09:00:00', '2025-05-06 15:00:00'),
(6, 5, 2, 50, 4, 2.00, '2025-05-08 09:00:00', '2025-05-14 10:02:00'),
(6, 1, 2, 10, 3, 2.00, '2025-05-10 09:00:00', '2025-05-13 14:20:00'),
-- 周凯文 9.5h / 2
(7, 12, 3, 100, 6, 3.00, '2025-03-04 09:00:00', '2025-04-08 16:00:00'),
(7, 6, 3, 100, 18, 2.00, '2025-04-10 09:00:00', '2025-05-04 15:00:00'),
(7, 2, 2, 40, 5, 3.00, '2025-05-06 09:00:00', '2025-05-11 15:40:00'),
(7, 8, 2, 15, 2, 1.50, '2025-05-09 09:00:00', '2025-05-10 11:00:00'),
-- 吴梦琪 15.0h / 3
(8, 3, 3, 100, 16, 4.00, '2025-02-14 09:00:00', '2025-03-28 17:00:00'),
(8, 5, 3, 100, 8, 2.00, '2025-04-01 09:00:00', '2025-04-22 16:00:00'),
(8, 11, 3, 100, 8, 2.00, '2025-04-24 09:00:00', '2025-05-08 15:00:00'),
(8, 9, 2, 60, 6, 4.00, '2025-05-02 09:00:00', '2025-05-13 11:20:00'),
(8, 1, 2, 18, 6, 3.00, '2025-05-07 09:00:00', '2025-05-12 10:10:00'),
-- 郑天翔 3.5h / 0（账号已禁用）
(9, 1, 2, 12, 4, 2.00, '2025-04-26 09:00:00', '2025-05-02 10:00:00'),
(9, 4, 2, 8, 2, 1.50, '2025-04-30 09:00:00', '2025-05-01 17:30:00'),
-- 王若琳 20.0h / 4
(10, 2, 3, 100, 12, 6.00, '2024-12-10 09:00:00', '2025-02-02 16:00:00'),
(10, 12, 3, 100, 6, 4.00, '2025-02-05 09:00:00', '2025-03-01 15:00:00'),
(10, 6, 3, 100, 18, 3.00, '2025-03-03 09:00:00', '2025-04-11 17:00:00'),
(10, 5, 3, 100, 8, 1.00, '2025-04-14 09:00:00', '2025-04-29 14:00:00'),
(10, 1, 2, 35, 11, 4.00, '2025-05-02 09:00:00', '2025-05-14 08:47:00'),
(10, 7, 2, 20, 4, 2.00, '2025-05-08 09:00:00', '2025-05-11 16:30:00');

-- 课时级明细（示例：李明在 c1 已完成的 5 个课时，其余由后端按播放事件补写）
INSERT INTO lesson_progress (user_id, lesson_id, is_completed, watched_seconds) VALUES
(1, 1, 1, 1320),
(1, 2, 1, 1680),
(1, 3, 1, 1140),
(1, 4, 1, 1500),
(1, 5, 1, 1860),
(1, 6, 0, 420);

-- 学时流水（个人看板月度趋势；李明合计 1110 分钟 = 18.5 小时）
INSERT INTO study_session (user_id, course_id, started_at, duration_minutes) VALUES
(1, 1, '2025-01-10 19:00:00', 240.0),
(1, 4, '2025-02-05 20:00:00', 300.0),
(1, 7, '2025-02-18 09:00:00', 180.0),
(1, 3, '2025-03-06 14:00:00', 150.0),
(1, 6, '2025-04-02 10:00:00', 120.0),
(1, 1, '2025-05-06 15:00:00', 120.0);

-- -----------------------------------------------------------------------------
-- 考试（EXAMS_DATA exam-1~6 + PAST_EXAMS past-1~6 转为已结束考试）
-- -----------------------------------------------------------------------------
INSERT INTO exam (id, code, title, organizer, status, start_at, end_at, duration_minutes, total_score, pass_score) VALUES
-- 开放窗口统一顺延到 2026-11 ~ 2026-12，保证「剩余考试机会」可实际开始作答
-- 分值规则：单选 2 / 多选 4 / 判断 2 / 应用题 8；总分 = 题目分值之和，及格分统一固定 60
(1, 'ZK-20250422-09', '2025年度第二季度研发部全栈开发规范与安全编码认证考试', '技术合规部 · 2025Q2', 3, '2026-11-01 14:00:00', '2026-12-31 23:59:59', 60, 100, 60),
(2, 'ZK-20250422-10', '2025Q2 分布式存储与高并发容灾等级认证实操机考', '基础架构委员会', 3, '2026-11-01 09:30:00', '2026-12-31 23:59:59', 120, 100, 60),
(3, 'ZK-20250410-01', '2025年度企业生成式 AI 研发与数据脱敏应用上岗资格考核', '算法与AI合规部', 3, '2026-11-01 00:00:00', '2026-12-31 23:59:59', 45, 100, 60),
(4, 'ZK-20250506-12', '2025Q3 前端工程化与页面性能优化认证', '前端技术委员会', 3, '2026-11-01 14:00:00', '2026-12-31 23:59:59', 90, 100, 60),
(5, 'ZK-20250428-03', '2025年度信息安全意识全员测评', '信息安全部', 3, '2026-11-01 00:00:00', '2026-12-31 23:59:59', 30, 100, 60),
(6, 'ZK-20250512-07', '2025Q2 数据库性能调优与慢查询治理实战认证', '基础架构委员会', 3, '2026-11-01 09:30:00', '2026-12-31 23:59:59', 90, 100, 60),
(7, 'CER-202503-ARCH-0914', '2025Q1 微服务高可用与架构容灾实战测评', '基础架构委员会', 4, NULL, NULL, 60, 100, 60),
(8, 'CER-202501-SEC-3321', '企业数据安全与隐私合规 (GDPR/PIPL) 标准考评', '信息安全部', 4, NULL, NULL, 60, 100, 60),
(9, 'CER-202411-OPS-7781', 'Kubernetes 云原生容器编排与 CI/CD 流水线实践', NULL, 4, NULL, NULL, 60, 100, 60),
(10, 'CER-202503-TRN-1206', '2025Q1 企业内训师授课能力认证', NULL, 4, NULL, NULL, 60, 100, 60),
(11, 'CER-202502-FIN-4417', '2025Q1 财务共享与费用报销规范考核', NULL, 4, NULL, NULL, 60, 100, 60),
(12, 'CER-202412-DEV-6602', '研发效能与代码评审规范专项测评', NULL, 4, NULL, NULL, 60, 100, 60);

-- -----------------------------------------------------------------------------
-- 题库
--   exam-1 试卷 = EXAM_QUESTIONS 25 题（学员端答题大屏用的固定卷）
--   exam-2/3   = ADMIN_QUESTION_BANK 里 q-6~q-12（q-1~q-5 与试卷内容重复，已去重）
--   correct_answer：单选/判断存 'A'，多选按字母序存 'ABD'；主观题为 NULL
-- -----------------------------------------------------------------------------
INSERT INTO exam_question (id, exam_id, question_no, type, score, title, correct_answer, reference_answer, analysis) VALUES
(1, 1, 1, 1, 2, '在分布式系统中，服务发现机制的主要作用是什么？', 'A', NULL, NULL),
(2, 1, 2, 1, 2, '使用数据库索引时，最需要关注的性能影响是什么？', 'A', NULL, NULL),
(3, 1, 3, 1, 2, '在 REST API 设计中，哪个 HTTP 方法通常用于读取资源？', 'A', NULL, NULL),
(4, 1, 4, 1, 2, '缓存穿透问题通常可以通过哪种方式缓解？', 'A', NULL, NULL),
(5, 1, 5, 1, 2, '消息队列中消费者确认机制的主要目的是什么？', 'A', NULL, NULL),
(6, 1, 6, 1, 2, '微服务之间进行身份认证时，哪种做法更合适？', 'A', NULL, NULL),
(7, 1, 7, 1, 2, '容器编排平台中，健康检查主要用于什么？', 'A', NULL, NULL),
(8, 1, 8, 1, 2, '在关系型数据库事务中，ACID 中的原子性表示什么？', 'A', NULL, NULL),
(9, 1, 9, 1, 2, '代码审查中，最适合优先关注哪类问题？', 'A', NULL, NULL),
(10, 1, 10, 1, 2, '日志中加入请求关联 ID 的主要价值是什么？', 'A', NULL, NULL),
(11, 1, 11, 1, 2, '负载均衡器的核心职责是什么？', 'A', NULL, NULL),
(12, 1, 12, 1, 2, '关于 Spring Boot 3 与 JDK 21 运行环境下 Virtual Threads (虚拟线程) 的并发模型与底层载体线程 (Carrier Thread) 调度机制描述，下列哪项是完全正确的？', 'B', NULL, '符合 Project Loom 与 Spring 3 标准架构'),
(13, 1, 13, 1, 2, '使用幂等接口设计可以避免哪类问题？', 'A', NULL, NULL),
(14, 1, 14, 1, 2, '持续集成流程中，自动化测试通常在哪个阶段运行？', 'A', NULL, NULL),
(15, 1, 15, 1, 2, '敏感配置项在生产环境中应如何管理？', 'A', NULL, NULL),
(16, 1, 16, 2, 4, '以下哪些措施有助于提升微服务系统的韧性？（多选）', 'ABD', NULL, NULL),
(17, 1, 17, 2, 4, '关于数据库索引与查询优化，以下哪些说法正确？（多选）', 'ABD', NULL, NULL),
(18, 1, 18, 2, 4, '生产环境中管理应用密钥时，哪些做法更安全？（多选）', 'ABD', NULL, NULL),
(19, 1, 19, 2, 4, '为了提升异步消息处理的可靠性，哪些措施值得采用？（多选）', 'ABD', NULL, NULL),
(20, 1, 20, 2, 4, '以下哪些指标有助于评估在线服务运行状况？（多选）', 'ABD', NULL, NULL),
(21, 1, 21, 3, 2, '虚拟线程在支持卸载的阻塞式 I/O 等待期间，可以释放 Carrier Thread 供其他虚拟线程使用。', 'A', NULL, NULL),
(22, 1, 22, 3, 2, '在生产环境中把所有秘密配置直接写入代码仓库，可以避免配置管理风险。', 'B', NULL, NULL),
(23, 1, 23, 3, 2, '为服务间调用设置超时，可以避免请求在依赖服务异常时无限期等待。', 'A', NULL, NULL),
(24, 1, 24, 4, 8, '某订单服务在调用库存服务时偶发超时，随后出现重复扣减库存。请说明你会如何排查，并提出至少两项改进措施。', NULL, '排查：全链路追踪定位超时节点、核对库存服务 P99 与线程池水位、检查重试与幂等键缺失。改进：调用侧设置超时+熔断重试退避；库存扣减改为幂等令牌或数据库唯一约束 + 条件更新。', NULL),
(25, 1, 25, 4, 8, '设计一个高可用的文件上传服务，请简述上传链路、失败重试、文件校验及监控告警的关键方案。', NULL, '链路：客户端直连对象存储预签名 URL，服务端只签发凭证与回调校验；失败重试：分片上传 + 断点续传 + 幂等 uploadId；校验：类型/大小白名单、病毒扫描、哈希一致性；监控：上传成功率、分片失败率、存储水位与回调延迟告警。', NULL),
(26, 2, 1, 1, 2, '使用数据库索引时,最需要关注的性能影响是什么?', 'A', NULL, NULL),
(27, 2, 2, 1, 2, '缓存穿透问题通常可以通过哪种方式缓解?', 'A', NULL, NULL),
(28, 2, 3, 3, 2, '在生产环境中把所有秘密配置直接写入代码仓库,可以避免配置管理风险。', 'B', NULL, NULL),
(29, 2, 4, 4, 8, '设计一个高可用的文件上传服务,请简述上传链路、失败重试、文件校验及监控告警的关键方案。', NULL, '同 exam-1 第 25 题评分要点。', NULL),
(30, 3, 1, 1, 2, '在 REST API 设计中,哪个 HTTP 方法通常用于读取资源?', 'A', NULL, NULL),
(31, 3, 2, 2, 4, '生产环境中管理应用密钥时,哪些做法更安全?(多选)', 'ABD', NULL, NULL),
(32, 3, 3, 3, 2, '为服务间调用设置超时,可以避免请求在依赖服务异常时无限期等待。', 'A', NULL, NULL);

INSERT INTO question_option (question_id, option_key, content, explanation, sort_no) VALUES
(1,'A','自动发现可用服务实例并提供访问入口',NULL,1),(1,'B','统一存储所有业务数据',NULL,2),(1,'C','替代服务间的网络通信',NULL,3),(1,'D','自动生成业务代码',NULL,4),
(2,'A','索引会占用额外存储并增加写入维护成本',NULL,1),(2,'B','索引一定会降低查询速度',NULL,2),(2,'C','索引会改变字段的数据类型',NULL,3),(2,'D','索引可以替代事务',NULL,4),
(3,'A','GET',NULL,1),(3,'B','POST',NULL,2),(3,'C','PATCH',NULL,3),(3,'D','DELETE',NULL,4),
(4,'A','使用布隆过滤器或缓存空结果',NULL,1),(4,'B','关闭所有缓存',NULL,2),(4,'C','增加数据库连接数',NULL,3),(4,'D','延长请求超时时间',NULL,4),
(5,'A','确保消息处理结果被确认并支持可靠投递',NULL,1),(5,'B','保证消息永远不重复',NULL,2),(5,'C','自动修改消息内容',NULL,3),(5,'D','替代消费者业务逻辑',NULL,4),
(6,'A','使用短期凭证并进行服务端校验',NULL,1),(6,'B','在请求参数中传递明文密码',NULL,2),(6,'C','所有服务共享永久令牌',NULL,3),(6,'D','取消服务端身份校验',NULL,4),
(7,'A','识别实例是否可正常提供服务',NULL,1),(7,'B','自动修改业务代码',NULL,2),(7,'C','保证数据库永不故障',NULL,3),(7,'D','替代容量规划',NULL,4),
(8,'A','事务中的操作要么全部成功，要么全部回滚',NULL,1),(8,'B','事务执行结果对所有用户可见',NULL,2),(8,'C','事务执行后数据永不变化',NULL,3),(8,'D','多个事务必须同时执行',NULL,4),
(9,'A','正确性、安全性及可能导致故障的逻辑问题',NULL,1),(9,'B','只关注变量命名风格',NULL,2),(9,'C','只检查文件长度',NULL,3),(9,'D','优先调整代码颜色',NULL,4),
(10,'A','串联一次请求经过的多个服务，便于追踪和排障',NULL,1),(10,'B','自动加密所有数据库字段',NULL,2),(10,'C','替代服务监控',NULL,3),(10,'D','阻止所有重复请求',NULL,4),
(11,'A','将请求分发到多个服务实例',NULL,1),(11,'B','保存所有业务数据',NULL,2),(11,'C','管理用户密码',NULL,3),(11,'D','编译应用程序',NULL,4),
(12,'A','虚拟线程采用 1:1 内核级线程映射，当发生 I/O 阻塞时，底层 OS 会挂起宿主 Carrier Thread 并引发大量用户态与内核态切换开销。','错误：这属于传统平台线程 Platform Threads 的调度与资源消耗模型',1),
(12,'B','虚拟线程是由 JVM 调度的 M:N 轻量级线程，在执行阻塞式网络 I/O 时会自动从 Carrier Thread 上 unmount (卸载)，Carrier Thread 可继续处理其他虚拟任务。','当前已选定项 · 符合 Project Loom 与 Spring 3 标准架构',2),
(12,'C','为了最大化利用吞吐量，在 Spring Boot 3 中应当始终对虚拟线程使用有界对象池 (ThreadPoolExecutor) 来复用线程实例，避免高并发频繁分配。','反模式警示：虚拟线程极为轻量，池化虚拟线程会导致吞吐量劣化',3),
(12,'D','当使用 synchronized 关键字块或调用 JNI 发生阻塞时，虚拟线程依然能无感知 unmount，完全不会导致 Carrier Thread Pinning (固定固化)。','错误：synchronized 与 JNI 会发生 Pinning 锁定，推荐改用 ReentrantLock',4),
(13,'A','重复请求不会造成重复副作用',NULL,1),(13,'B','请求必须只允许一个用户访问',NULL,2),(13,'C','请求永远不能失败',NULL,3),(13,'D','接口只能使用 GET 方法',NULL,4),
(14,'A','代码提交后，在构建与测试阶段自动执行',NULL,1),(14,'B','仅在系统下线后执行',NULL,2),(14,'C','仅由用户手动执行',NULL,3),(14,'D','生产故障发生后才执行',NULL,4),
(15,'A','存放在受控的密钥管理系统中并按需授权',NULL,1),(15,'B','直接写入公开代码仓库',NULL,2),(15,'C','发送到群聊中共享',NULL,3),(15,'D','使用固定默认值且不轮换',NULL,4),
(16,'A','对关键调用设置超时与合理的重试策略',NULL,1),(16,'B','使用熔断机制隔离持续失败的依赖',NULL,2),(16,'C','对所有请求无限次立即重试',NULL,3),(16,'D','为服务设计健康检查与故障恢复机制',NULL,4),
(17,'A','应根据查询条件和执行计划评估索引',NULL,1),(17,'B','索引过多可能增加写入和存储开销',NULL,2),(17,'C','所有字段都创建索引一定能提升性能',NULL,3),(17,'D','复合索引的字段顺序需要结合查询模式设计',NULL,4),
(18,'A','使用专门的密钥管理服务',NULL,1),(18,'B','限制密钥读取权限并记录访问行为',NULL,2),(18,'C','将密钥提交到公开代码仓库',NULL,3),(18,'D','制定轮换和泄露后的撤销流程',NULL,4),
(19,'A','处理成功后再确认消息',NULL,1),(19,'B','设计可安全重试的幂等消费者',NULL,2),(19,'C','忽略处理失败且不保留记录',NULL,3),(19,'D','对失败消息配置告警或死信处理',NULL,4),
(20,'A','请求延迟分位数',NULL,1),(20,'B','错误率与请求吞吐量',NULL,2),(20,'C','仅统计代码文件数量',NULL,3),(20,'D','资源使用率与依赖服务健康状态',NULL,4),
(21,'A','正确',NULL,1),(21,'B','错误',NULL,2),
(22,'A','正确',NULL,1),(22,'B','错误',NULL,2),
(23,'A','正确',NULL,1),(23,'B','错误',NULL,2),
(26,'A','索引会占用额外存储并增加写入维护成本',NULL,1),(26,'B','索引一定会降低查询速度',NULL,2),(26,'C','索引会改变字段的数据类型',NULL,3),(26,'D','索引可以替代事务',NULL,4),
(27,'A','使用布隆过滤器或缓存空结果',NULL,1),(27,'B','关闭所有缓存',NULL,2),(27,'C','增加数据库连接数',NULL,3),(27,'D','延长请求超时时间',NULL,4),
(28,'A','正确',NULL,1),(28,'B','错误',NULL,2),
(30,'A','GET',NULL,1),(30,'B','POST',NULL,2),(30,'C','PATCH',NULL,3),(30,'D','DELETE',NULL,4),
(31,'A','使用专门的密钥管理服务',NULL,1),(31,'B','限制密钥读取权限并记录访问行为',NULL,2),(31,'C','将密钥提交到公开代码仓库',NULL,3),(31,'D','制定轮换和泄露后的撤销流程',NULL,4),
(32,'A','正确',NULL,1),(32,'B','错误',NULL,2);

-- 补充题目：保证每场开放考试都有可考题目
-- 为 exam-1 (exam_id=1) 添加额外题目
INSERT INTO exam_question (id, exam_id, question_no, type, score, title, correct_answer, reference_answer, analysis) VALUES
(100, 1, 26, 1, 2, '在微服务架构中，服务网格（Service Mesh）的主要作用是什么？', 'B', NULL, '服务网格提供服务间通信的基础设施层，包括负载均衡、服务发现、故障恢复等功能。'),
(101, 1, 27, 2, 4, '以下哪些是分布式事务的解决方案？（多选）', 'ACD', NULL, '分布式事务的常见解决方案包括两阶段提交、Saga 模式、TCC 模式等。'),
(102, 1, 28, 1, 2, '在 Kubernetes 中，Pod 的探针（Probe）主要用于什么？', 'A', NULL, '探针用于检测容器的健康状态，包括存活探针、就绪探针和启动探针。'),
(103, 1, 29, 3, 2, 'Redis 的持久化机制包括 RDB 和 AOF 两种方式。', 'A', NULL, 'Redis 支持 RDB（快照）和 AOF（追加日志）两种持久化方式。'),
(104, 1, 30, 4, 8, '请设计一个高可用的微服务架构，要求支持自动扩缩容、故障自愈和灰度发布，说明关键技术选型和实现方案。', NULL, '参考答案：1. 使用 Kubernetes 进行容器编排和自动扩缩容；2. 使用 Istio 服务网格实现流量管理和灰度发布；3. 使用 Prometheus + Grafana 进行监控告警；4. 使用 ELK 栈进行日志收集和分析；5. 使用 Consul 或 etcd 进行服务发现；6. 使用 Sentinel 或 Hystrix 实现熔断降级。', '高可用架构需要结合容器化、服务网格、监控告警等多种技术。');

-- 为 question_option 添加选项
INSERT INTO question_option (question_id, option_key, content, explanation, sort_no) VALUES
(100,'A','提供业务逻辑实现',NULL,1),
(100,'B','管理服务间通信基础设施',NULL,2),
(100,'C','替代应用层安全机制',NULL,3),
(100,'D','自动生成服务接口文档',NULL,4),
(101,'A','两阶段提交（2PC）',NULL,1),
(101,'B','本地事务',NULL,2),
(101,'C','Saga 模式',NULL,3),
(101,'D','TCC 模式',NULL,4),
(102,'A','检测容器健康状态',NULL,1),
(102,'B','自动重启容器',NULL,2),
(102,'C','管理容器网络',NULL,3),
(102,'D','存储容器数据',NULL,4),
(103,'A','正确',NULL,1),
(103,'B','错误',NULL,2);

-- 为 exam-2 (exam_id=2) 添加额外题目
INSERT INTO exam_question (id, exam_id, question_no, type, score, title, correct_answer, reference_answer, analysis) VALUES
(105, 2, 5, 1, 2, '在分布式数据库中，CAP 定理指的是以下哪个组合？', 'A', NULL, 'CAP 定理包括一致性（Consistency）、可用性（Availability）和分区容错性（Partition tolerance）。'),
(106, 2, 6, 2, 4, '以下哪些是高可用系统的设计原则？（多选）', 'BCD', NULL, '高可用系统设计需要考虑冗余、故障转移、负载均衡等原则。'),
(107, 2, 7, 1, 2, '在数据库主从复制中，读写分离的主要目的是？', 'B', NULL, '读写分离将读请求路由到从库，写请求路由到主库，以提高系统性能。'),
(108, 2, 8, 3, 2, '数据库索引可以完全替代查询优化。', 'B', NULL, '索引是查询优化的一种手段，但不能完全替代其他优化方法如查询重写、分区等。'),
(109, 2, 9, 4, 8, '请设计一个支持百万级并发的数据库架构，要求数据一致性、可用性和扩展性，说明分库分表策略和缓存方案。', NULL, '参考答案：1. 采用分库分表策略，按业务域或用户 ID 分片；2. 使用读写分离提高读性能；3. 使用 Redis 缓存热点数据；4. 使用消息队列异步处理写操作；5. 实现数据同步和一致性保证机制；6. 使用 CDN 加速静态资源。', '百万级并发需要综合考虑分库分表、缓存、异步处理等多种技术。');

-- 为 question_option 添加选项
INSERT INTO question_option (question_id, option_key, content, explanation, sort_no) VALUES
(105,'A','一致性、可用性、分区容错性',NULL,1),
(105,'B','计算机、算法、程序',NULL,2),
(105,'C','缓存、API、性能',NULL,3),
(105,'D','安全、审计、权限',NULL,4),
(106,'A','单点部署',NULL,1),
(106,'B','数据冗余',NULL,2),
(106,'C','故障转移',NULL,3),
(106,'D','负载均衡',NULL,4),
(107,'A','提高写入性能',NULL,1),
(107,'B','提高读取性能',NULL,2),
(107,'C','简化数据模型',NULL,3),
(107,'D','减少存储空间',NULL,4),
(108,'A','正确',NULL,1),
(108,'B','错误',NULL,2);

-- 为 exam-3 (exam_id=3) 添加额外题目
INSERT INTO exam_question (id, exam_id, question_no, type, score, title, correct_answer, reference_answer, analysis) VALUES
(110, 3, 4, 1, 2, '在自然语言处理中，"词嵌入"（Word Embedding）的主要作用是什么？', 'A', NULL, '词嵌入将单词转换为低维向量表示，捕获语义信息。'),
(111, 3, 5, 2, 4, '以下哪些是机器学习的常见任务类型？（多选）', 'ABC', NULL, '机器学习任务包括分类、回归、聚类等。'),
(112, 3, 6, 1, 2, '在深度学习中，"过拟合"（Overfitting）指的是什么？', 'B', NULL, '过拟合是指模型在训练数据上表现很好，但在新数据上表现较差的现象。'),
(113, 3, 7, 3, 2, '所有的机器学习模型都需要大量的标注数据才能训练。', 'B', NULL, '并非所有模型都需要大量标注数据，如半监督学习、自监督学习等可以减少对标注数据的依赖。'),
(114, 3, 8, 4, 8, '请说明如何评估一个机器学习模型的性能，并介绍常用的评估指标和方法。', NULL, '参考答案：1. 使用准确率、精确率、召回率、F1 分数等分类指标；2. 使用均方误差、平均绝对误差等回归指标；3. 使用交叉验证避免过拟合；4. 使用混淆矩阵分析模型性能；5. 使用 ROC 曲线和 AUC 值评估分类模型。', '模型评估需要根据具体任务选择合适的指标和方法。');

-- 为 question_option 添加选项
INSERT INTO question_option (question_id, option_key, content, explanation, sort_no) VALUES
(110,'A','将单词转换为向量表示',NULL,1),
(110,'B','存储单词的拼写',NULL,2),
(110,'C','统计单词频率',NULL,3),
(110,'D','翻译单词',NULL,4),
(111,'A','分类',NULL,1),
(111,'B','回归',NULL,2),
(111,'C','聚类',NULL,3),
(111,'D','编程',NULL,4),
(112,'A','模型性能优秀',NULL,1),
(112,'B','模型在训练数据上表现好但泛化能力差',NULL,2),
(112,'C','模型训练速度快',NULL,3),
(112,'D','模型参数数量少',NULL,4),
(113,'A','正确',NULL,1),
(113,'B','错误',NULL,2);

-- 为 exam-4 (exam_id=4) 添加额外题目
INSERT INTO exam_question (id, exam_id, question_no, type, score, title, correct_answer, reference_answer, analysis) VALUES
(115, 4, 1, 1, 2, '在前端开发中，"虚拟 DOM"的主要优势是什么？', 'A', NULL, '虚拟 DOM 通过减少直接 DOM 操作来提高性能，使用 diff 算法计算最小更新。'),
(116, 4, 2, 2, 4, '以下哪些是前端性能优化的常用技术？（多选）', 'BD', NULL, '前端性能优化包括代码分割、懒加载、缓存策略、CDN 使用等。'),
(117, 4, 3, 1, 2, '在 JavaScript 中，"闭包"（Closure）指的是什么？', 'B', NULL, '闭包是指函数能够访问其词法作用域外的变量，即使函数在该作用域外执行。'),
(118, 4, 4, 3, 2, '使用 CDN 可以提高网站的加载速度。', 'A', NULL, 'CDN（内容分发网络）通过在全球部署节点来减少用户访问延迟，提高加载速度。'),
(119, 4, 5, 4, 8, '请设计一个前端性能监控系统，包括关键指标、数据采集、分析和告警机制，并说明如何优化页面加载性能。', NULL, '参考答案：1. 关键指标：FCP、LCP、FID、CLS、TTI 等；2. 数据采集：使用 Performance API、第三方监控工具（如 Google Analytics、Sentry）；3. 分析：使用数据可视化工具（如 Grafana）展示趋势；4. 告警：设置阈值，实时监控异常；5. 优化：代码分割、懒加载、图片压缩、缓存策略、CDN、SSR/SSG。', '前端性能监控需要综合考虑多个指标和优化策略。');

-- 为 question_option 添加选项
INSERT INTO question_option (question_id, option_key, content, explanation, sort_no) VALUES
(115,'A','减少直接 DOM 操作',NULL,1),
(115,'B','增加内存使用',NULL,2),
(115,'C','简化代码结构',NULL,3),
(115,'D','提高安全性',NULL,4),
(116,'A','增加代码复杂度',NULL,1),
(116,'B','代码分割',NULL,2),
(116,'C','增加服务器负载',NULL,3),
(116,'D','懒加载',NULL,4),
(117,'A','函数嵌套',NULL,1),
(117,'B','函数能够访问外部作用域变量',NULL,2),
(117,'C','循环引用',NULL,3),
(117,'D','变量提升',NULL,4),
(118,'A','正确',NULL,1),
(118,'B','错误',NULL,2);

-- 为 exam-5 (exam_id=5) 添加额外题目
INSERT INTO exam_question (id, exam_id, question_no, type, score, title, correct_answer, reference_answer, analysis) VALUES
(120, 5, 1, 1, 2, '以下关于网络安全的描述，正确的是？', 'C', NULL, '网络安全涉及保护网络系统、数据和信息免受未授权访问、使用、披露、破坏、修改或阻止的各种威胁。'),
(121, 5, 2, 2, 4, '以下哪些是常见的网络攻击手段？（多选）', 'ABC', NULL, '常见的网络攻击手段包括钓鱼攻击、DDoS 攻击、SQL 注入等。'),
(122, 5, 3, 1, 2, '在密码学中，"哈希函数"的主要作用是什么？', 'B', NULL, '哈希函数将任意长度的数据映射为固定长度的哈希值，用于数据完整性验证和密码存储。'),
(123, 5, 4, 3, 2, '使用 HTTPS 可以保证数据传输的绝对安全。', 'B', NULL, 'HTTPS 提供加密传输，但不能保证绝对安全，仍可能受到中间人攻击等威胁。'),
(124, 5, 5, 4, 8, '请说明如何识别和防范常见的社交工程攻击，并介绍企业应采取的安全措施。', NULL, '参考答案：1. 识别钓鱼邮件、电话诈骗、社交媒体欺诈等；2. 防范措施：提高安全意识培训、使用双因素认证、验证请求来源、报告可疑活动；3. 企业措施：制定安全政策、实施访问控制、定期安全审计、员工培训。', '社交工程攻击利用人类心理弱点，防范需要技术和管理措施相结合。');

-- 为 question_option 添加选项
INSERT INTO question_option (question_id, option_key, content, explanation, sort_no) VALUES
(120,'A','只保护计算机硬件',NULL,1),
(120,'B','只保护软件程序',NULL,2),
(120,'C','保护网络系统、数据和信息',NULL,3),
(120,'D','只保护个人隐私',NULL,4),
(121,'A','钓鱼攻击',NULL,1),
(121,'B','DDoS 攻击',NULL,2),
(121,'C','SQL 注入',NULL,3),
(121,'D','正常访问',NULL,4),
(122,'A','将数据加密',NULL,1),
(122,'B','验证数据完整性',NULL,2),
(122,'C','压缩数据大小',NULL,3),
(122,'D','存储数据',NULL,4),
(123,'A','正确',NULL,1),
(123,'B','错误',NULL,2);

-- 为 exam-6 (exam_id=6) 添加额外题目
INSERT INTO exam_question (id, exam_id, question_no, type, score, title, correct_answer, reference_answer, analysis) VALUES
(125, 6, 1, 1, 2, '在数据库性能优化中，"索引"的主要作用是什么？', 'B', NULL, '索引是一种数据结构，用于加速数据库中的数据检索操作。'),
(126, 6, 2, 2, 4, '以下哪些是常见的数据库性能问题？（多选）', 'ABC', NULL, '慢查询、全表扫描和死锁是常见的数据库性能问题。'),
(127, 6, 3, 1, 2, '在 SQL 查询中，"EXPLAIN"命令的主要作用是什么？', 'A', NULL, 'EXPLAIN 用于分析 SQL 查询的执行计划，帮助识别性能瓶颈。'),
(128, 6, 4, 3, 2, '数据库索引可以完全替代查询优化。', 'B', NULL, '索引是查询优化的一种手段，但不能完全替代其他优化方法。'),
(129, 6, 5, 4, 8, '请分析以下 SQL 查询的性能问题，并提出优化建议：SELECT * FROM orders WHERE user_id = 123 AND status = ''pending'' ORDER BY created_at DESC LIMIT 10;', NULL, '参考答案：1. 使用具体列名而非 SELECT *；2. 确保 user_id 和 status 列有索引；3. 考虑复合索引 (user_id, status, created_at)；4. 使用 EXPLAIN 分析执行计划；5. 考虑分区表或归档旧数据；6. 使用覆盖索引减少回表查询。', 'SQL 查询优化需要结合索引、查询重写、执行计划分析等多种方法。');

-- 为 question_option 添加选项
INSERT INTO question_option (question_id, option_key, content, explanation, sort_no) VALUES
(125,'A','存储数据',NULL,1),
(125,'B','加速查询',NULL,2),
(125,'C','备份数据',NULL,3),
(125,'D','压缩数据',NULL,4),
(126,'A','慢查询',NULL,1),
(126,'B','全表扫描',NULL,2),
(126,'C','死锁',NULL,3),
(126,'D','数据冗余',NULL,4),
(127,'A','显示查询执行计划',NULL,1),
(127,'B','优化查询语句',NULL,2),
(127,'C','执行查询',NULL,3),
(127,'D','备份数据',NULL,4),
(128,'A','正确',NULL,1),
(128,'B','错误',NULL,2);


-- 考试指派（学员端「待参加考核」列表的数据源）
INSERT INTO exam_assignment (exam_id, user_id, status, attempts_allowed, attempts_used, deadline_at) VALUES
(1, 1, 1, 2, 1, '2026-12-31 23:59:59'),
(2, 1, 1, 2, 0, '2026-12-31 23:59:59'),
(3, 1, 1, 3, 1, '2026-12-31 23:59:59'),
(4, 1, 1, 2, 0, '2026-12-31 23:59:59'),
(5, 1, 1, 3, 0, '2026-12-31 23:59:59'),
(6, 1, 1, 2, 0, '2026-12-31 23:59:59'),
(5, 2, 1, 3, 0, '2026-12-31 23:59:59'),
(5, 3, 1, 3, 0, '2026-12-31 23:59:59'),
(5, 4, 1, 3, 0, '2026-12-31 23:59:59'),
(5, 10, 2, 3, 1, '2026-12-31 23:59:59');

-- 答卷：PAST_EXAMS 是李明的往期成绩；另补少量他人答卷让统计可派生
INSERT INTO exam_attempt (id, exam_id, user_id, attempt_no, status, started_at, submitted_at, remaining_seconds, objective_score, subjective_score, total_score, pass_score, is_passed, wrong_count, graded_at) VALUES
(1, 7, 1, 1, 3, '2025-03-28 14:40:00', '2025-03-28 15:40:00', NULL, 92.0, NULL, 92.0, 80, 1, 2, '2025-03-28 15:41:00'),
(2, 8, 1, 1, 3, '2025-01-14 09:22:00', '2025-01-14 10:22:00', NULL, 100.0, NULL, 100.0, 85, 1, 0, '2025-01-14 10:23:00'),
(3, 9, 1, 1, 3, '2024-11-20 15:50:00', '2024-11-20 16:50:00', NULL, 86.0, NULL, 86.0, 80, 1, 4, '2024-11-20 16:51:00'),
(4, 10, 1, 1, 3, '2025-03-12 10:20:00', '2025-03-12 11:20:00', NULL, 88.0, NULL, 88.0, 80, 1, 3, '2025-03-12 11:21:00'),
(5, 11, 1, 1, 3, '2025-02-21 13:10:00', '2025-02-21 14:10:00', NULL, 94.0, NULL, 94.0, 85, 1, 1, '2025-02-21 14:11:00'),
(6, 12, 1, 1, 3, '2024-12-18 16:30:00', '2024-12-18 17:30:00', NULL, 79.0, NULL, 79.0, 80, 0, 7, '2024-12-18 17:31:00'),
(7, 1, 1, 1, 2, '2025-05-14 11:17:00', '2025-05-14 12:17:00', NULL, 24.0, NULL, 24.0, 77, NULL, 0, NULL),
(8, 8, 2, 1, 3, '2025-01-14 09:30:00', '2025-01-14 10:35:00', NULL, 88.0, NULL, 88.0, 85, 1, 3, '2025-01-14 10:36:00'),
(9, 8, 4, 1, 3, '2025-01-15 14:00:00', '2025-01-15 14:52:00', NULL, 76.0, NULL, 76.0, 85, 0, 6, '2025-01-15 14:53:00'),
(10, 8, 10, 1, 3, '2025-01-16 10:00:00', '2025-01-16 10:48:00', NULL, 96.0, NULL, 96.0, 85, 1, 1, '2025-01-16 10:49:00'),
(11, 7, 2, 1, 3, '2025-03-28 14:35:00', '2025-03-28 15:38:00', NULL, 90.0, NULL, 90.0, 80, 1, 2, '2025-03-28 15:39:00');

-- 逐题作答（示例：李明 exam-1 进行中的那份卷子，前 12 题已答、第 22 题标记待复查）
INSERT INTO attempt_answer (attempt_id, question_id, selected_keys, is_flagged, score_gained, answered_at) VALUES
(7, 1, 'A', 0, 2.0, '2025-05-14 11:19:00'),
(7, 2, 'A', 0, 2.0, '2025-05-14 11:21:00'),
(7, 3, 'A', 0, 2.0, '2025-05-14 11:22:00'),
(7, 4, 'A', 0, 2.0, '2025-05-14 11:24:00'),
(7, 5, 'A', 0, 2.0, '2025-05-14 11:26:00'),
(7, 6, 'A', 0, 2.0, '2025-05-14 11:28:00'),
(7, 7, 'A', 0, 2.0, '2025-05-14 11:30:00'),
(7, 8, 'A', 0, 2.0, '2025-05-14 11:32:00'),
(7, 9, 'A', 0, 2.0, '2025-05-14 11:34:00'),
(7, 10, 'A', 0, 2.0, '2025-05-14 11:36:00'),
(7, 11, 'A', 0, 2.0, '2025-05-14 11:38:00'),
(7, 12, 'B', 0, 2.0, '2025-05-14 11:42:00'),
(7, 22, NULL, 1, NULL, NULL);

-- 主观题作答：待人工评卷（status=2），管理端「阅卷」需要能看到学员原文并逐题给分
INSERT INTO attempt_answer (attempt_id, question_id, answer_text, is_flagged, score_gained, answered_at) VALUES
(7, 24, '排查：先用链路追踪定位超时发生在库存服务，查看 P99 与线程池队列水位；再核对调用侧重试配置，确认缺少幂等键导致重复扣减。改进：调用侧设置超时 + 熔断 + 指数退避重试；库存扣减改为幂等令牌 + 数据库唯一约束 + 条件更新，保证同一订单只扣一次。', 0, NULL, '2025-05-14 12:05:00'),
(7, 25, '上传链路：客户端通过服务端签发的预签名 URL 直连对象存储，上传完成回调服务端校验并落库。失败重试：分片上传 + 断点续传，使用同一 uploadId 保证幂等。文件校验：类型/大小白名单、哈希一致性校验与病毒扫描。监控告警：上传成功率、分片失败率、存储水位和回调延迟超过阈值即告警。', 0, NULL, '2025-05-14 12:15:00');

-- -----------------------------------------------------------------------------
-- 证书：模板（种类）+ 签发实例
-- -----------------------------------------------------------------------------
INSERT INTO certificate_template (id, title, level_label, category, issue_org, validity_months, description, privilege_text) VALUES
(1, '微服务高可用架构实战专家认证', 'LEVEL 4 · 专家级认证', 'architecture', '企业技术委员会', NULL, '表彰在 Spring Cloud 微服务拆分、容灾治理、Sentinel 限流降级及分布式链路追踪实战中取得卓越表现，且通过企业高并发沙箱实战考核。', '区块链存证防伪哈希已校验'),
(2, '企业数据安全与隐私合规 (GDPR/PIPL) 专家证书', '法定合规 · 重点资格', 'compliance', '风控与安全合规部', 24, '掌握个人信息保护法与跨境数据传输规范，通过生产库敏感字段脱敏策略实施、日志脱敏与审计防篡改专项考试。', '具备生产环境数据脱敏审批权'),
(3, 'Kubernetes 云原生容器编排认证', '运维基建 · 认证', 'devops', '企业技术委员会', NULL, '认证合格完成生产级 K8s 集群 Helm 部署治理、服务网格 Istio 流量路由、Pod 弹性伸缩策略及 StatefulSet 灾难演练。', '生产级 DevOps 权限就绪'),
(4, '企业生成式 AI 研发提效工程专家', 'AI 效能 · 先锋认证', 'ai', 'AI 创新实验室', NULL, '掌握大模型企业知识库 RAG 架构搭建、Prompt 工程进阶微调、自动化单元测试生成与安全代码规审审计实践。', '企业 Copilot 提示词金牌贡献者'),
(5, '高效跨部门沟通与敏捷项目管理实践', '管理通识 · 结课认证', 'management', '企业大学教培中心', NULL, '完成敏捷协作、迭代计划与跨部门沟通机制全部课程并通过结课测评。', '具备内部敏捷教练候选资格'),
(6, '金融级数据合规与业务出海跨境传输实战', '法务合规 · 专项认证', 'compliance', '风控与安全合规部', NULL, '通过 GDPR/PDPA 与国内数据出境评估专项考核，掌握跨境传输合规落地流程。', '具备跨境业务数据合规评审权');

INSERT INTO certificate_issue (id, template_id, user_id, serial_number, issue_date, expire_date, validity_label, status, blockchain_hash, issued_by) VALUES
(1, 1, 1, 'ZX-2024-MS-089', '2024-03-15', NULL, '长期有效', 2, '0x8f31c9e2a4d7b60519ce4f7a2d8b1e63c05a9f47d28b61e3', 11),
(2, 2, 1, 'ZX-2024-SEC-412', '2024-02-28', '2026-02-28', '24 个月 (2026年到期)', 2, '0x2b71e04a9dc8635f17a2b4e0d9c658317f4a02be', 11),
(3, 3, 1, 'ZX-2023-K8S-901', '2023-11-20', NULL, '长期有效', 2, '0x5d02a7f19c3b684e0a17d5c2b9e836410af7d2c5', 11),
(4, 4, 1, 'ZX-2024-AI-015', '2024-04-10', NULL, '长期有效', 2, '0x91ce4b70a2d5f8613e0b7c4a9d25e608317bf94c', 11),
(5, 3, 2, 'ZX-2023-K8S-902', '2023-11-20', NULL, '长期有效', 2, '0x7a3d1c95e0b4826f7a0c1d5e93b2846af07c1d52', 11),
(6, 2, 4, 'ZX-2025-SEC-128', '2025-04-02', '2027-04-02', '24 个月 (2027年到期)', 2, '0x1e5b83a7d0c2946f1b5a8e0c73d92645018af3b7', 11),
(7, 4, 6, 'ZX-2025-AI-052', '2025-05-06', NULL, '长期有效', 2, NULL, 11),
(8, 5, 3, 'ZX-2025-PM-077', NULL, NULL, '长期有效', 1, NULL, 11),
(9, 6, 10, 'ZX-2025-LAW-031', NULL, NULL, '长期有效', 1, NULL, 11);

-- -----------------------------------------------------------------------------
-- 公告（ADMIN_ANNOUNCEMENTS_DATA ann-1~8；draft 状态保留）
-- -----------------------------------------------------------------------------
INSERT INTO announcement (id, title, content, is_top, status, author_id, author_name, publish_at) VALUES
(1, '关于发布《2025年第二季度技术序列通用学分核算与晋升认定申报通知》', '2025年Q2技术序列通用学分核算现已开启,请各部门于6月5日前完成学分录入与晋升申报材料提交。涉及职级 P5 及以上申报人员,需附上本年度技术答辩评审记录。', 1, 2, 11, '王智远', '2025-05-13 09:30:00'),
(2, '2025「云舒内训师」金牌讲师团第二期招募启动', '第二期金牌讲师团招募现已启动,面向全员开放报名。入选讲师将获得课程研发津贴与年度评优加分,报名截止5月31日。', 0, 2, 11, '王智远', '2025-05-11 14:00:00'),
(3, '平台 3.8.0 版本停机维护通知(拟发布)', '平台计划于本周五 23:00 - 次日 02:00 进行只读维护,上线 AI 智能陪练助手与知识库精准检索功能。请提前告知各部门合理安排学习时间。', 0, 1, 11, '王智远', '2025-05-16 16:20:00'),
(4, '2025 全员合规红线考核即将截止的催办提醒', '《2025数据安全与合规》全员考核将于5月17日 18:00 关闭系统,当前仍有 87 名员工未完成作答,请各部门负责人督促落实。', 0, 2, NULL, '系统自动', '2025-05-16 10:00:00'),
(5, '2025 年第二季度「学习之星」评选结果公示', '经部门推荐与学时数据核算,本季度共评选出 12 名「学习之星」,公示期为 5 月 19 日至 5 月 23 日。如有异议请在公示期内联系企业大学运营组。', 0, 2, 11, '王智远', '2025-05-19 10:20:00'),
(6, '关于新增「云原生运维」系列课程上架的通知', '「云原生运维」系列 6 门课程已完成内容评审并正式上架,涵盖容器编排、可观测性、故障演练等主题,欢迎各技术团队按需选学。', 0, 2, 11, '王智远', '2025-05-18 15:40:00'),
(7, '端午节假期培训安排调整说明(拟发布)', '端午节假期期间线上课程正常开放,直播课与集中考试顺延至节后首个工作日,具体调整安排以各部门通知为准。', 0, 1, 11, '王智远', '2025-05-20 09:10:00'),
(8, '培训考勤规则更新:迟到与缺勤判定口径说明', '自 6 月 1 日起,线上签到以课程开始后 10 分钟为界线,超过 10 分钟记为迟到;未签到且未提交请假单的记为缺勤,请各位同事留意。', 0, 2, NULL, '系统自动', '2025-05-17 11:05:00');

INSERT INTO announcement_read (announcement_id, user_id, read_at) VALUES
(1, 1, '2025-05-13 10:05:00'), (2, 1, '2025-05-11 15:20:00'), (4, 1, '2025-05-16 10:30:00');

-- -----------------------------------------------------------------------------
-- 请假（ADMIN_LEAVE_REQUESTS 11 条；学员视角 LEAVE_REQUESTS_DATA 的 4 条与之重合，
--       已按 flow_no 合并）
-- -----------------------------------------------------------------------------
INSERT INTO leave_request (id, flow_no, user_id, leave_type, start_date, end_date, leave_days, reason, status, submitted_at, approver_id, approved_at, approve_remark) VALUES
(1, 'LV-20250518-004', 2, '项目紧急发版', '2025-05-18', '2025-05-18', 0.5, '因核心结算集群在16:30进行双机房切流验证,需现场跟进发布(工单: PROD-OPS-20250518-09)。申请缺席下午后半程实操。', 1, '2025-05-18 10:15:00', NULL, NULL, NULL),
(2, 'LV-20250519-001', 4, '病假 / 身体不适', '2025-05-19', '2025-05-19', 1.0, '急性肠胃炎就医检查及休养,附三甲医院就诊单据及病休建议。', 1, '2025-05-18 21:02:00', NULL, NULL, NULL),
(3, 'LV-20250520-002', 3, '因公外出 / 外部交流', '2025-05-20', '2025-05-20', 0.5, '赴客户现场参加产品一期验收评审,现场进行功能演示及需求答疑。', 1, '2025-05-18 17:40:00', NULL, NULL, NULL),
(4, 'LV-20250516-003', 5, '个人事务 / 家庭安排', '2025-05-16', '2025-05-16', 1.0, '因个人家庭事务申请缺席全天培训。', 3, '2025-05-15 09:20:00', 11, '2025-05-15 14:10:00', '当日为必修实操环节,建议改为调整学习批次'),
(6, 'LV-20250425-015', 8, '病假 / 身体不适', '2025-04-25', '2025-04-25', 1.0, '急性肠胃炎就医检查及休养,附带三甲医院就诊单据及病休建议。', 2, '2025-04-24 14:10:00', 11, '2025-04-24 17:30:00', '已附就诊单据,同意'),
(7, 'LV-20250521-005', 6, '因公外出 / 外部交流', '2025-05-21', '2025-05-21', 0.5, '赴品牌合作方参加年度联合推广方案沟通会,需现场确认物料排期与投放节奏。', 1, '2025-05-19 09:35:00', NULL, NULL, NULL),
(8, 'LV-20250522-006', 7, '个人事务 / 家庭安排', '2025-05-22', '2025-05-22', 1.0, '家中老人需要陪同到院复查,当天无法参加集中培训,相关课程将在课后通过回放补齐并提交学习笔记。', 1, '2025-05-20 08:50:00', NULL, NULL, NULL),
(9, 'LV-20250522-007', 9, '项目紧急发版', '2025-05-22', '2025-05-22', 0.5, '核心交易链路灰度发布需要全程值守,申请缺席下午的架构实操课程,发布完成后补看录播。', 1, '2025-05-20 13:12:00', NULL, NULL, NULL),
(10, 'LV-20250523-008', 12, '因公外出 / 外部交流', '2025-05-23', '2025-05-23', 1.0, '赴行业峰会参加产品创新论坛并做主题分享,行程与当天培训时间冲突,申请事后补学。', 1, '2025-05-21 10:05:00', NULL, NULL, NULL),
(11, 'LV-20250515-010', 13, '病假 / 身体不适', '2025-05-15', '2025-05-15', 1.0, '感冒发热需居家休息,已提交社区医院就诊记录。', 2, '2025-05-14 19:20:00', 11, '2025-05-15 08:40:00', '同意,注意留存就诊记录'),
(12, 'LV-20250509-011', 14, '个人事务 / 家庭安排', '2025-05-09', '2025-05-09', 0.5, '临时需要处理子女入学材料提交事宜,申请缺席半天课程。', 3, '2025-05-08 16:45:00', 11, '2025-05-08 18:20:00', '距开课不足 12 小时提交,按规则不予批准');

-- -----------------------------------------------------------------------------
-- 签到（ADMIN_CHECKIN_RECORDS，日期 = 锚点当天 2025-05-14）
-- -----------------------------------------------------------------------------
INSERT INTO checkin_record (id, user_id, checkin_date, check_in_time, check_out_time, status) VALUES
(1, 1, '2025-05-14', '08:52:00', '17:36:00', 1),
(2, 2, '2025-05-14', '08:56:00', '18:02:00', 1),
(3, 3, '2025-05-14', '08:47:00', '17:28:00', 1),
(4, 4, '2025-05-14', '08:58:00', '17:41:00', 1),
(5, 5, '2025-05-14', '09:02:00', '18:10:00', 2),
(6, 6, '2025-05-14', '08:39:00', '17:15:00', 1),
(7, 7, '2025-05-14', '08:51:00', '17:52:00', 1),
(8, 8, '2025-05-14', '08:45:00', '17:30:00', 1),
(9, 10, '2025-05-14', '08:50:00', '17:26:00', 1),
(10, 9, '2025-05-14', NULL, NULL, 3),
(11, 15, '2025-05-14', '09:06:00', '18:20:00', 2),
(12, 14, '2025-05-14', '08:31:00', '17:08:00', 1);

-- -----------------------------------------------------------------------------
-- 操作动态（ADMIN_ACTIVITIES_DATA）
-- -----------------------------------------------------------------------------
INSERT INTO operation_log (id, log_type, content, operator_id, operator_name, ref_type, ref_id, occurred_at) VALUES
(1, 'course', '上架新课程《Serverless 函数计算实战》', 11, '王智远', 'course', NULL, '2025-05-14 10:20:00'),
(2, 'user', '新增学员账号:孙雨桐 (EMP-2024620)', 11, '王智远', 'user', 6, '2025-05-14 09:58:00'),
(3, 'exam', '发布考试「2025Q3 前端工程化认证」', 11, '王智远', 'exam', 4, '2025-05-13 18:05:00'),
(4, 'notice', '置顶公告「2025Q2 学分核算与晋升申报通知」', 11, '王智远', 'announcement', 1, '2025-05-13 15:40:00'),
(5, 'approve', '审批通过请假申请 LV-20250512-009(张浩然)', 11, '王智远', 'leave', 5, '2025-05-12 16:55:00'),
(6, 'user', '禁用学员账号:郑天翔 (EMP-2023788,长期未学习)', NULL, '系统自动', 'user', 9, '2025-05-10 09:00:00');

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- 自检：种子跑完后这几条应当都能出数
-- =============================================================================
-- SELECT employee_no, name, learning_hours, completed_courses, certificate_count
--   FROM v_learner_profile WHERE user_id = 1;              -- 期望 18.5 / 3 / 4
-- SELECT ym, study_hours FROM v_monthly_study_hours WHERE user_id = 1 ORDER BY ym;
-- SELECT id, code, question_count, attempt_count, pass_rate FROM v_exam_overview ORDER BY id;
-- SELECT * FROM v_admin_pending;

-- -----------------------------------------------------------------------------
-- 试卷补足到 100 分：新增题目副本（保持单选 2 / 多选 4 / 判断 2 / 应用题 8 分值规则）
-- -----------------------------------------------------------------------------
INSERT INTO exam_question (id, exam_id, question_no, type, score, title, correct_answer, reference_answer, analysis) VALUES
(200, 1, 31, 4, 8, '某订单服务在调用库存服务时偶发超时，随后出现重复扣减库存。请说明你会如何排查，并提出至少两项改进措施。', NULL, '排查：全链路追踪定位超时节点、核对库存服务 P99 与线程池水位、检查重试与幂等键缺失。改进：调用侧设置超时+熔断重试退避；库存扣减改为幂等令牌或数据库唯一约束 + 条件更新。', NULL),
(201, 1, 32, 1, 2, '在分布式系统中，服务发现机制的主要作用是什么？', 'A', NULL, NULL),
(202, 2, 10, 4, 8, '某订单服务在调用库存服务时偶发超时，随后出现重复扣减库存。请说明你会如何排查，并提出至少两项改进措施。', NULL, '排查：全链路追踪定位超时节点、核对库存服务 P99 与线程池水位、检查重试与幂等键缺失。改进：调用侧设置超时+熔断重试退避；库存扣减改为幂等令牌或数据库唯一约束 + 条件更新。', NULL),
(203, 2, 11, 4, 8, '设计一个高可用的文件上传服务，请简述上传链路、失败重试、文件校验及监控告警的关键方案。', NULL, '链路：客户端直连对象存储预签名 URL，服务端只签发凭证与回调校验；失败重试：分片上传 + 断点续传 + 幂等 uploadId；校验：类型/大小白名单、病毒扫描、哈希一致性；监控：上传成功率、分片失败率、存储水位与回调延迟告警。', NULL),
(204, 2, 12, 4, 8, '设计一个高可用的文件上传服务,请简述上传链路、失败重试、文件校验及监控告警的关键方案。', NULL, '同 exam-1 第 25 题评分要点。', NULL),
(205, 2, 13, 4, 8, '请设计一个高可用的微服务架构，要求支持自动扩缩容、故障自愈和灰度发布，说明关键技术选型和实现方案。', NULL, '参考答案：1. 使用 Kubernetes 进行容器编排和自动扩缩容；2. 使用 Istio 服务网格实现流量管理和灰度发布；3. 使用 Prometheus + Grafana 进行监控告警；4. 使用 ELK 栈进行日志收集和分析；5. 使用 Consul 或 etcd 进行服务发现；6. 使用 Sentinel 或 Hystrix 实现熔断降级。', NULL),
(206, 2, 14, 4, 8, '请设计一个支持百万级并发的数据库架构，要求数据一致性、可用性和扩展性，说明分库分表策略和缓存方案。', NULL, '参考答案：1. 采用分库分表策略，按业务域或用户 ID 分片；2. 使用读写分离提高读性能；3. 使用 Redis 缓存热点数据；4. 使用消息队列异步处理写操作；5. 实现数据同步和一致性保证机制；6. 使用 CDN 加速静态资源。', NULL),
(207, 2, 15, 4, 8, '请说明如何评估一个机器学习模型的性能，并介绍常用的评估指标和方法。', NULL, '参考答案：1. 使用准确率、精确率、召回率、F1 分数等分类指标；2. 使用均方误差、平均绝对误差等回归指标；3. 使用交叉验证避免过拟合；4. 使用混淆矩阵分析模型性能；5. 使用 ROC 曲线和 AUC 值评估分类模型。', NULL),
(208, 2, 16, 4, 8, '请设计一个前端性能监控系统，包括关键指标、数据采集、分析和告警机制，并说明如何优化页面加载性能。', NULL, '参考答案：1. 关键指标：FCP、LCP、FID、CLS、TTI 等；2. 数据采集：使用 Performance API、第三方监控工具（如 Google Analytics、Sentry）；3. 分析：使用数据可视化工具（如 Grafana）展示趋势；4. 告警：设置阈值，实时监控异常；5. 优化：代码分割、懒加载、图片压缩、缓存策略、CDN、SSR/SSG。', NULL),
(209, 2, 17, 4, 8, '请说明如何识别和防范常见的社交工程攻击，并介绍企业应采取的安全措施。', NULL, '参考答案：1. 识别钓鱼邮件、电话诈骗、社交媒体欺诈等；2. 防范措施：提高安全意识培训、使用双因素认证、验证请求来源、报告可疑活动；3. 企业措施：制定安全政策、实施访问控制、定期安全审计、员工培训。', NULL),
(210, 2, 18, 1, 2, '在分布式系统中，服务发现机制的主要作用是什么？', 'A', NULL, NULL),
(211, 2, 19, 3, 2, '虚拟线程在支持卸载的阻塞式 I/O 等待期间，可以释放 Carrier Thread 供其他虚拟线程使用。', 'A', NULL, NULL),
(212, 3, 9, 4, 8, '某订单服务在调用库存服务时偶发超时，随后出现重复扣减库存。请说明你会如何排查，并提出至少两项改进措施。', NULL, '排查：全链路追踪定位超时节点、核对库存服务 P99 与线程池水位、检查重试与幂等键缺失。改进：调用侧设置超时+熔断重试退避；库存扣减改为幂等令牌或数据库唯一约束 + 条件更新。', NULL),
(213, 3, 10, 4, 8, '设计一个高可用的文件上传服务，请简述上传链路、失败重试、文件校验及监控告警的关键方案。', NULL, '链路：客户端直连对象存储预签名 URL，服务端只签发凭证与回调校验；失败重试：分片上传 + 断点续传 + 幂等 uploadId；校验：类型/大小白名单、病毒扫描、哈希一致性；监控：上传成功率、分片失败率、存储水位与回调延迟告警。', NULL),
(214, 3, 11, 4, 8, '设计一个高可用的文件上传服务,请简述上传链路、失败重试、文件校验及监控告警的关键方案。', NULL, '同 exam-1 第 25 题评分要点。', NULL),
(215, 3, 12, 4, 8, '请设计一个高可用的微服务架构，要求支持自动扩缩容、故障自愈和灰度发布，说明关键技术选型和实现方案。', NULL, '参考答案：1. 使用 Kubernetes 进行容器编排和自动扩缩容；2. 使用 Istio 服务网格实现流量管理和灰度发布；3. 使用 Prometheus + Grafana 进行监控告警；4. 使用 ELK 栈进行日志收集和分析；5. 使用 Consul 或 etcd 进行服务发现；6. 使用 Sentinel 或 Hystrix 实现熔断降级。', NULL),
(216, 3, 13, 4, 8, '请设计一个支持百万级并发的数据库架构，要求数据一致性、可用性和扩展性，说明分库分表策略和缓存方案。', NULL, '参考答案：1. 采用分库分表策略，按业务域或用户 ID 分片；2. 使用读写分离提高读性能；3. 使用 Redis 缓存热点数据；4. 使用消息队列异步处理写操作；5. 实现数据同步和一致性保证机制；6. 使用 CDN 加速静态资源。', NULL),
(217, 3, 14, 4, 8, '请说明如何评估一个机器学习模型的性能，并介绍常用的评估指标和方法。', NULL, '参考答案：1. 使用准确率、精确率、召回率、F1 分数等分类指标；2. 使用均方误差、平均绝对误差等回归指标；3. 使用交叉验证避免过拟合；4. 使用混淆矩阵分析模型性能；5. 使用 ROC 曲线和 AUC 值评估分类模型。', NULL),
(218, 3, 15, 4, 8, '请设计一个前端性能监控系统，包括关键指标、数据采集、分析和告警机制，并说明如何优化页面加载性能。', NULL, '参考答案：1. 关键指标：FCP、LCP、FID、CLS、TTI 等；2. 数据采集：使用 Performance API、第三方监控工具（如 Google Analytics、Sentry）；3. 分析：使用数据可视化工具（如 Grafana）展示趋势；4. 告警：设置阈值，实时监控异常；5. 优化：代码分割、懒加载、图片压缩、缓存策略、CDN、SSR/SSG。', NULL),
(219, 3, 16, 4, 8, '请说明如何识别和防范常见的社交工程攻击，并介绍企业应采取的安全措施。', NULL, '参考答案：1. 识别钓鱼邮件、电话诈骗、社交媒体欺诈等；2. 防范措施：提高安全意识培训、使用双因素认证、验证请求来源、报告可疑活动；3. 企业措施：制定安全政策、实施访问控制、定期安全审计、员工培训。', NULL),
(220, 3, 17, 4, 8, '请分析以下 SQL 查询的性能问题，并提出优化建议：SELECT * FROM orders WHERE user_id = 123 AND status = ''pending'' ORDER BY created_at DESC LIMIT 10;', NULL, '参考答案：1. 使用具体列名而非 SELECT *；2. 确保 user_id 和 status 列有索引；3. 考虑复合索引 (user_id, status, created_at)；4. 使用 EXPLAIN 分析执行计划；5. 考虑分区表或归档旧数据；6. 使用覆盖索引减少回表查询。', NULL),
(221, 3, 18, 1, 2, '在分布式系统中，服务发现机制的主要作用是什么？', 'A', NULL, NULL),
(222, 4, 6, 4, 8, '设计一个高可用的文件上传服务，请简述上传链路、失败重试、文件校验及监控告警的关键方案。', NULL, '链路：客户端直连对象存储预签名 URL，服务端只签发凭证与回调校验；失败重试：分片上传 + 断点续传 + 幂等 uploadId；校验：类型/大小白名单、病毒扫描、哈希一致性；监控：上传成功率、分片失败率、存储水位与回调延迟告警。', NULL),
(223, 4, 7, 4, 8, '设计一个高可用的文件上传服务,请简述上传链路、失败重试、文件校验及监控告警的关键方案。', NULL, '同 exam-1 第 25 题评分要点。', NULL),
(224, 4, 8, 4, 8, '请设计一个高可用的微服务架构，要求支持自动扩缩容、故障自愈和灰度发布，说明关键技术选型和实现方案。', NULL, '参考答案：1. 使用 Kubernetes 进行容器编排和自动扩缩容；2. 使用 Istio 服务网格实现流量管理和灰度发布；3. 使用 Prometheus + Grafana 进行监控告警；4. 使用 ELK 栈进行日志收集和分析；5. 使用 Consul 或 etcd 进行服务发现；6. 使用 Sentinel 或 Hystrix 实现熔断降级。', NULL),
(225, 4, 9, 4, 8, '请设计一个支持百万级并发的数据库架构，要求数据一致性、可用性和扩展性，说明分库分表策略和缓存方案。', NULL, '参考答案：1. 采用分库分表策略，按业务域或用户 ID 分片；2. 使用读写分离提高读性能；3. 使用 Redis 缓存热点数据；4. 使用消息队列异步处理写操作；5. 实现数据同步和一致性保证机制；6. 使用 CDN 加速静态资源。', NULL),
(226, 4, 10, 4, 8, '请说明如何评估一个机器学习模型的性能，并介绍常用的评估指标和方法。', NULL, '参考答案：1. 使用准确率、精确率、召回率、F1 分数等分类指标；2. 使用均方误差、平均绝对误差等回归指标；3. 使用交叉验证避免过拟合；4. 使用混淆矩阵分析模型性能；5. 使用 ROC 曲线和 AUC 值评估分类模型。', NULL),
(227, 4, 11, 4, 8, '请设计一个前端性能监控系统，包括关键指标、数据采集、分析和告警机制，并说明如何优化页面加载性能。', NULL, '参考答案：1. 关键指标：FCP、LCP、FID、CLS、TTI 等；2. 数据采集：使用 Performance API、第三方监控工具（如 Google Analytics、Sentry）；3. 分析：使用数据可视化工具（如 Grafana）展示趋势；4. 告警：设置阈值，实时监控异常；5. 优化：代码分割、懒加载、图片压缩、缓存策略、CDN、SSR/SSG。', NULL),
(228, 4, 12, 4, 8, '请说明如何识别和防范常见的社交工程攻击，并介绍企业应采取的安全措施。', NULL, '参考答案：1. 识别钓鱼邮件、电话诈骗、社交媒体欺诈等；2. 防范措施：提高安全意识培训、使用双因素认证、验证请求来源、报告可疑活动；3. 企业措施：制定安全政策、实施访问控制、定期安全审计、员工培训。', NULL),
(229, 4, 13, 4, 8, '请分析以下 SQL 查询的性能问题，并提出优化建议：SELECT * FROM orders WHERE user_id = 123 AND status = ''pending'' ORDER BY created_at DESC LIMIT 10;', NULL, '参考答案：1. 使用具体列名而非 SELECT *；2. 确保 user_id 和 status 列有索引；3. 考虑复合索引 (user_id, status, created_at)；4. 使用 EXPLAIN 分析执行计划；5. 考虑分区表或归档旧数据；6. 使用覆盖索引减少回表查询。', NULL),
(230, 4, 14, 4, 8, '某订单服务在调用库存服务时偶发超时，随后出现重复扣减库存。请说明你会如何排查，并提出至少两项改进措施。', NULL, '排查：全链路追踪定位超时节点、核对库存服务 P99 与线程池水位、检查重试与幂等键缺失。改进：调用侧设置超时+熔断重试退避；库存扣减改为幂等令牌或数据库唯一约束 + 条件更新。', NULL),
(231, 4, 15, 4, 8, '设计一个高可用的文件上传服务，请简述上传链路、失败重试、文件校验及监控告警的关键方案。', NULL, '链路：客户端直连对象存储预签名 URL，服务端只签发凭证与回调校验；失败重试：分片上传 + 断点续传 + 幂等 uploadId；校验：类型/大小白名单、病毒扫描、哈希一致性；监控：上传成功率、分片失败率、存储水位与回调延迟告警。', NULL),
(232, 4, 16, 1, 2, '在分布式系统中，服务发现机制的主要作用是什么？', 'A', NULL, NULL),
(233, 5, 6, 4, 8, '设计一个高可用的文件上传服务,请简述上传链路、失败重试、文件校验及监控告警的关键方案。', NULL, '同 exam-1 第 25 题评分要点。', NULL),
(234, 5, 7, 4, 8, '请设计一个高可用的微服务架构，要求支持自动扩缩容、故障自愈和灰度发布，说明关键技术选型和实现方案。', NULL, '参考答案：1. 使用 Kubernetes 进行容器编排和自动扩缩容；2. 使用 Istio 服务网格实现流量管理和灰度发布；3. 使用 Prometheus + Grafana 进行监控告警；4. 使用 ELK 栈进行日志收集和分析；5. 使用 Consul 或 etcd 进行服务发现；6. 使用 Sentinel 或 Hystrix 实现熔断降级。', NULL),
(235, 5, 8, 4, 8, '请设计一个支持百万级并发的数据库架构，要求数据一致性、可用性和扩展性，说明分库分表策略和缓存方案。', NULL, '参考答案：1. 采用分库分表策略，按业务域或用户 ID 分片；2. 使用读写分离提高读性能；3. 使用 Redis 缓存热点数据；4. 使用消息队列异步处理写操作；5. 实现数据同步和一致性保证机制；6. 使用 CDN 加速静态资源。', NULL),
(236, 5, 9, 4, 8, '请说明如何评估一个机器学习模型的性能，并介绍常用的评估指标和方法。', NULL, '参考答案：1. 使用准确率、精确率、召回率、F1 分数等分类指标；2. 使用均方误差、平均绝对误差等回归指标；3. 使用交叉验证避免过拟合；4. 使用混淆矩阵分析模型性能；5. 使用 ROC 曲线和 AUC 值评估分类模型。', NULL),
(237, 5, 10, 4, 8, '请设计一个前端性能监控系统，包括关键指标、数据采集、分析和告警机制，并说明如何优化页面加载性能。', NULL, '参考答案：1. 关键指标：FCP、LCP、FID、CLS、TTI 等；2. 数据采集：使用 Performance API、第三方监控工具（如 Google Analytics、Sentry）；3. 分析：使用数据可视化工具（如 Grafana）展示趋势；4. 告警：设置阈值，实时监控异常；5. 优化：代码分割、懒加载、图片压缩、缓存策略、CDN、SSR/SSG。', NULL),
(238, 5, 11, 4, 8, '请说明如何识别和防范常见的社交工程攻击，并介绍企业应采取的安全措施。', NULL, '参考答案：1. 识别钓鱼邮件、电话诈骗、社交媒体欺诈等；2. 防范措施：提高安全意识培训、使用双因素认证、验证请求来源、报告可疑活动；3. 企业措施：制定安全政策、实施访问控制、定期安全审计、员工培训。', NULL),
(239, 5, 12, 4, 8, '请分析以下 SQL 查询的性能问题，并提出优化建议：SELECT * FROM orders WHERE user_id = 123 AND status = ''pending'' ORDER BY created_at DESC LIMIT 10;', NULL, '参考答案：1. 使用具体列名而非 SELECT *；2. 确保 user_id 和 status 列有索引；3. 考虑复合索引 (user_id, status, created_at)；4. 使用 EXPLAIN 分析执行计划；5. 考虑分区表或归档旧数据；6. 使用覆盖索引减少回表查询。', NULL),
(240, 5, 13, 4, 8, '某订单服务在调用库存服务时偶发超时，随后出现重复扣减库存。请说明你会如何排查，并提出至少两项改进措施。', NULL, '排查：全链路追踪定位超时节点、核对库存服务 P99 与线程池水位、检查重试与幂等键缺失。改进：调用侧设置超时+熔断重试退避；库存扣减改为幂等令牌或数据库唯一约束 + 条件更新。', NULL),
(241, 5, 14, 4, 8, '设计一个高可用的文件上传服务，请简述上传链路、失败重试、文件校验及监控告警的关键方案。', NULL, '链路：客户端直连对象存储预签名 URL，服务端只签发凭证与回调校验；失败重试：分片上传 + 断点续传 + 幂等 uploadId；校验：类型/大小白名单、病毒扫描、哈希一致性；监控：上传成功率、分片失败率、存储水位与回调延迟告警。', NULL),
(242, 5, 15, 4, 8, '设计一个高可用的文件上传服务,请简述上传链路、失败重试、文件校验及监控告警的关键方案。', NULL, '同 exam-1 第 25 题评分要点。', NULL),
(243, 5, 16, 1, 2, '在分布式系统中，服务发现机制的主要作用是什么？', 'A', NULL, NULL),
(244, 6, 6, 4, 8, '请设计一个高可用的微服务架构，要求支持自动扩缩容、故障自愈和灰度发布，说明关键技术选型和实现方案。', NULL, '参考答案：1. 使用 Kubernetes 进行容器编排和自动扩缩容；2. 使用 Istio 服务网格实现流量管理和灰度发布；3. 使用 Prometheus + Grafana 进行监控告警；4. 使用 ELK 栈进行日志收集和分析；5. 使用 Consul 或 etcd 进行服务发现；6. 使用 Sentinel 或 Hystrix 实现熔断降级。', NULL),
(245, 6, 7, 4, 8, '请设计一个支持百万级并发的数据库架构，要求数据一致性、可用性和扩展性，说明分库分表策略和缓存方案。', NULL, '参考答案：1. 采用分库分表策略，按业务域或用户 ID 分片；2. 使用读写分离提高读性能；3. 使用 Redis 缓存热点数据；4. 使用消息队列异步处理写操作；5. 实现数据同步和一致性保证机制；6. 使用 CDN 加速静态资源。', NULL),
(246, 6, 8, 4, 8, '请说明如何评估一个机器学习模型的性能，并介绍常用的评估指标和方法。', NULL, '参考答案：1. 使用准确率、精确率、召回率、F1 分数等分类指标；2. 使用均方误差、平均绝对误差等回归指标；3. 使用交叉验证避免过拟合；4. 使用混淆矩阵分析模型性能；5. 使用 ROC 曲线和 AUC 值评估分类模型。', NULL),
(247, 6, 9, 4, 8, '请设计一个前端性能监控系统，包括关键指标、数据采集、分析和告警机制，并说明如何优化页面加载性能。', NULL, '参考答案：1. 关键指标：FCP、LCP、FID、CLS、TTI 等；2. 数据采集：使用 Performance API、第三方监控工具（如 Google Analytics、Sentry）；3. 分析：使用数据可视化工具（如 Grafana）展示趋势；4. 告警：设置阈值，实时监控异常；5. 优化：代码分割、懒加载、图片压缩、缓存策略、CDN、SSR/SSG。', NULL),
(248, 6, 10, 4, 8, '请说明如何识别和防范常见的社交工程攻击，并介绍企业应采取的安全措施。', NULL, '参考答案：1. 识别钓鱼邮件、电话诈骗、社交媒体欺诈等；2. 防范措施：提高安全意识培训、使用双因素认证、验证请求来源、报告可疑活动；3. 企业措施：制定安全政策、实施访问控制、定期安全审计、员工培训。', NULL),
(249, 6, 11, 4, 8, '请分析以下 SQL 查询的性能问题，并提出优化建议：SELECT * FROM orders WHERE user_id = 123 AND status = ''pending'' ORDER BY created_at DESC LIMIT 10;', NULL, '参考答案：1. 使用具体列名而非 SELECT *；2. 确保 user_id 和 status 列有索引；3. 考虑复合索引 (user_id, status, created_at)；4. 使用 EXPLAIN 分析执行计划；5. 考虑分区表或归档旧数据；6. 使用覆盖索引减少回表查询。', NULL),
(250, 6, 12, 4, 8, '某订单服务在调用库存服务时偶发超时，随后出现重复扣减库存。请说明你会如何排查，并提出至少两项改进措施。', NULL, '排查：全链路追踪定位超时节点、核对库存服务 P99 与线程池水位、检查重试与幂等键缺失。改进：调用侧设置超时+熔断重试退避；库存扣减改为幂等令牌或数据库唯一约束 + 条件更新。', NULL),
(251, 6, 13, 4, 8, '设计一个高可用的文件上传服务，请简述上传链路、失败重试、文件校验及监控告警的关键方案。', NULL, '链路：客户端直连对象存储预签名 URL，服务端只签发凭证与回调校验；失败重试：分片上传 + 断点续传 + 幂等 uploadId；校验：类型/大小白名单、病毒扫描、哈希一致性；监控：上传成功率、分片失败率、存储水位与回调延迟告警。', NULL),
(252, 6, 14, 4, 8, '设计一个高可用的文件上传服务,请简述上传链路、失败重试、文件校验及监控告警的关键方案。', NULL, '同 exam-1 第 25 题评分要点。', NULL),
(253, 6, 15, 4, 8, '请设计一个高可用的微服务架构，要求支持自动扩缩容、故障自愈和灰度发布，说明关键技术选型和实现方案。', NULL, '参考答案：1. 使用 Kubernetes 进行容器编排和自动扩缩容；2. 使用 Istio 服务网格实现流量管理和灰度发布；3. 使用 Prometheus + Grafana 进行监控告警；4. 使用 ELK 栈进行日志收集和分析；5. 使用 Consul 或 etcd 进行服务发现；6. 使用 Sentinel 或 Hystrix 实现熔断降级。', NULL),
(254, 6, 16, 1, 2, '在分布式系统中，服务发现机制的主要作用是什么？', 'A', NULL, NULL);

INSERT INTO question_option (question_id, option_key, content, explanation, sort_no) VALUES
(201, 'A', '自动发现可用服务实例并提供访问入口', NULL, 1),
(201, 'B', '统一存储所有业务数据', NULL, 2),
(201, 'C', '替代服务间的网络通信', NULL, 3),
(201, 'D', '自动生成业务代码', NULL, 4),
(210, 'A', '自动发现可用服务实例并提供访问入口', NULL, 1),
(210, 'B', '统一存储所有业务数据', NULL, 2),
(210, 'C', '替代服务间的网络通信', NULL, 3),
(210, 'D', '自动生成业务代码', NULL, 4),
(211, 'A', '正确', NULL, 1),
(211, 'B', '错误', NULL, 2),
(221, 'A', '自动发现可用服务实例并提供访问入口', NULL, 1),
(221, 'B', '统一存储所有业务数据', NULL, 2),
(221, 'C', '替代服务间的网络通信', NULL, 3),
(221, 'D', '自动生成业务代码', NULL, 4),
(232, 'A', '自动发现可用服务实例并提供访问入口', NULL, 1),
(232, 'B', '统一存储所有业务数据', NULL, 2),
(232, 'C', '替代服务间的网络通信', NULL, 3),
(232, 'D', '自动生成业务代码', NULL, 4),
(243, 'A', '自动发现可用服务实例并提供访问入口', NULL, 1),
(243, 'B', '统一存储所有业务数据', NULL, 2),
(243, 'C', '替代服务间的网络通信', NULL, 3),
(243, 'D', '自动生成业务代码', NULL, 4),
(254, 'A', '自动发现可用服务实例并提供访问入口', NULL, 1),
(254, 'B', '统一存储所有业务数据', NULL, 2),
(254, 'C', '替代服务间的网络通信', NULL, 3),
(254, 'D', '自动生成业务代码', NULL, 4);

SET FOREIGN_KEY_CHECKS = 1;
