-- =============================================================================
-- 云舒艺境 ENTERPRISE ACADEMY · 企业数字化智能人才培养平台
-- 数据库结构 (MySQL 8.0+ / InnoDB / utf8mb4)
--
-- 建模口径：正规后端建模
--   1. 主键统一 BIGINT UNSIGNED AUTO_INCREMENT；业务编码单独成列并加唯一索引。
--   2. 状态类字段一律 TINYINT + 注释枚举（见下方「枚举字典」），不使用 MySQL ENUM。
--   3. 可派生的统计（学时、完成门数、证书数、题目数）不写回业务表，
--      由文件末尾的视图给出；确需缓存的计数器列名统一以 _cached 结尾，由后端维护。
--   4. 业务表带 created_at / updated_at / deleted_at（软删除，NULL = 未删除）。
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
--   leave_request.status       1 待审批  2 已通过  3 已驳回
--   checkin_record.status      1 正常  2 迟到  3 缺勤  4 早退
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
