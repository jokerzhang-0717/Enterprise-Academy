# emp-server

云舒艺境后端服务。为 `D:\桌面\Emp` 下的 Vue 前端提供 REST API：业务数据全部存储于 MySQL，课件 / 头像 / 附件等文件存储于 MinIO。

## 技术栈

- Spring Boot 3.0.2（Java 17+，本机验证使用 Java 21）
- MyBatis-Plus 3.5.7 + MySQL 8
- JWT（JJWT 0.12.5）无状态鉴权
- MinIO 8.5.7 对象存储
- BCrypt（spring-security-crypto）密码散列
- Lombok

## 目录结构

```
emp-server/
├─ pom.xml
├─ .env.example                 # 环境变量样例
└─ src/main/
   ├─ java/com/joker/emp/
   │  ├─ common/                # ApiResponse / PageResult / 异常 / 数字转换
   │  ├─ config/                # MyBatis-Plus、CORS、MinIO、审计字段填充
   │  ├─ controller/            # 学员端 + 管理端 REST 接口
   │  ├─ entity/                # 30 张业务表实体
   │  ├─ mapper/                # BaseMapper + 统计/视图查询
   │  ├─ security/              # JWT、登录上下文、@AdminOnly
   │  ├─ service/               # 业务逻辑
   │  └─ storage/               # MinIO 上传 / 预签名 / 读取
   └─ resources/application.yml
```

数据库脚本位于仓库根目录：`../sql/schema.sql`（30 表 + 7 视图）与 `../sql/seed.sql`（演示数据）。

## 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.x（库名 `enterprise_academy`）
- MinIO（默认 `http://127.0.0.1:9000`）

## 初始化数据库

```bash
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS enterprise_academy DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -uroot -p enterprise_academy < ../sql/schema.sql
mysql -uroot -p enterprise_academy < ../sql/seed.sql
```

> `schema.sql` 会重建表，已有数据的环境请勿重复执行。

已有数据库升级题库知识点与难度字段时，备份数据库后执行一次：

```bash
mysql -uroot -p enterprise_academy < ../sql/migrate_exam_question_metadata.sql
```

考试创建/编辑时会将所选题目复制到该考试的试卷中；发布后或产生作答记录后，试卷题目不可再修改。

## 环境变量

复制 `.env.example` 并按需修改，或在启动前直接注入环境变量：

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `MYSQL_USERNAME` | `root` | MySQL 用户名 |
| `MYSQL_PASSWORD` | `123456` | MySQL 密码 |
| `REDIS_HOST` | `127.0.0.1` | 学习计时 Redis 地址 |
| `REDIS_PORT` | `6379` | 学习计时 Redis 端口 |
| `REDIS_PASSWORD` | 空 | Redis 密码（无密码时留空） |
| `REDIS_DATABASE` | `0` | Redis DB 编号 |
| `API_RESPONSE_CACHE_TTL` | `1s` | 登录用户 API 读取缓存时间；成功写操作会立即使旧缓存失效 |
| `EMP_JWT_SECRET` | 内置开发密钥 | JWT 签名密钥，生产必须覆盖（≥32 字节） |
| `EMP_JWT_EXPIRE_MINUTES` | `15` | 访问令牌有效期（分钟） |
| `EMP_ALLOWED_ORIGINS` | 空（仅本机 3000/5173） | 允许跨域的前端地址，逗号分隔；生产按实际域名配置 |
| `MINIO_ENDPOINT` | `http://127.0.0.1:9000` | MinIO 地址 |
| `MINIO_ACCESS_KEY` | `zhang` | MinIO Access Key |
| `MINIO_SECRET_KEY` | `12345678` | MinIO Secret Key |
| `MINIO_BUCKET` | `emp-academy` | 存储桶，启动时自动创建 |
| `MINIO_PUBLIC_READ` | `true` | 是否给桶设置公共只读；生产建议 `false` |

数据库连接、端口等其余配置见 `src/main/resources/application.yml`。服务默认端口 `8080`，上下文路径 `/api`。
课程播放期间需要 Redis 记录活跃会话，学习时长心跳会同步写入 MySQL；请先启动 Redis 服务。

## 构建与启动

```bash
cd emp-server
mvn -DskipTests package
java -jar target/emp-server-0.0.1-SNAPSHOT.jar
```

开发期也可直接运行：

```bash
mvn spring-boot:run
```

健康检查（需先登录）：`GET http://localhost:8080/api/auth/me`。

## 鉴权

1. 登录：`POST /api/auth/login`，请求体 `{ "employeeNo": "EMP-2024098", "password": "123456" }`，返回 15 分钟访问令牌 `token` 与 7 天刷新令牌 `refreshToken`。
2. 后续请求只携带 `Authorization: Bearer <token>`；不再兼容 `X-Token`、裸 header 或 `token` 查询参数。
3. 访问令牌过期后调用 `POST /api/auth/refresh`，请求体 `{ "refreshToken": "..." }` 换发新令牌；刷新令牌一次性使用，每次刷新都会轮换。
4. 退出登录调用 `POST /api/auth/logout`，同时吊销访问令牌（Redis 黑名单）与刷新令牌；账号停用、密码修改后旧令牌立即失效。
5. 服务端每次请求都会查询数据库校验账号状态、角色与凭证指纹，因此停用账号或调整角色可立即生效。
6. 管理端接口额外要求 `role = 2`，否则返回 403。
7. 无需登录的白名单：`/auth/login`、`/auth/register`、`/auth/refresh`、`/auth/logout`、`/files/view/**`、`/certificates/verify/**`。
8. 登录 / 注册带 Redis 限流：同一工号 15 分钟内连续失败 5 次，或同一 IP 注册过于频繁，会返回 429。

演示账号（`seed.sql`）：

| 角色 | 工号 | 密码 |
| --- | --- | --- |
| 学员（李明） | `EMP-2024098` | `123456` |
| 管理员 | `admin` | `123456` |

种子用户 `password_hash` 为空时，登录会按演示口令 `123456` 校验；新注册 / 重置密码使用 BCrypt 落库。

## 响应格式

```json
{ "code": 0, "message": "ok", "data": {} }
```

分页接口的 `data` 为：

```json
{ "total": 100, "page": 1, "size": 10, "records": [] }
```

业务异常由全局异常处理器转换：`400` 参数错误、`401` 未登录、`403` 无权限、`404` 不存在、`409` 冲突，`code` 与 HTTP 状态码保持一致。

## 主要接口分组

### 公共 / 学员端

| 模块 | 接口前缀 | 说明 |
| --- | --- | --- |
| 认证 | `/api/auth` | 登录、注册、当前用户 |
| 个人中心 | `/api/profile` | 资料、修改密码 |
| 首页看板 | `/api/dashboard` | 学习统计、待办、公告、签到 |
| 课程 | `/api/courses` | 课程目录、详情、报名、课时进度、我的学习 |
| 研修通道 | `/api/tracks` | 已上架通道列表与详情 |
| 考试 | `/api/exams` | 我的考试、试卷、开始、答题、切屏、交卷、往期 |
| 请假 | `/api/leaves` | 提交、修改、撤销、我的申请 |
| 签到 | `/api/checkins` | 今日状态、签到、签退、记录、统计 |
| 证书 | `/api/certificates` | 我的证书、证书验真 |
| 公告 | `/api/announcements` | 已发布公告、未读数、详情 |
| 通知 | `/api/notifications` | 列表、最近、未读数、已读、删除 |
| 文件 | `/api/files` | 上传、预签名上传/下载、稳定访问、删除 |

### 管理端（需管理员）

| 模块 | 接口前缀 |
| --- | --- |
| 看板 / 操作日志 | `/api/admin/dashboard`、`/api/admin/activities`、`/api/admin/logs` |
| 学员 | `/api/admin/users` |
| 部门 | `/api/admin/departments` |
| 课程 / 章节 / 课时 / 标签 / 讲师 | `/api/admin/courses`、`/api/admin/course-categories`、`/api/admin/course-tags`、`/api/admin/instructors` |
| 研修通道 | `/api/admin/tracks` |
| 考试 / 指派 / 答卷 / 评分 | `/api/admin/exams`、`/api/admin/exam-attempts` |
| 题库 | `/api/admin/questions` |
| 请假审批 | `/api/admin/leaves` |
| 签到管理 | `/api/admin/checkins` |
| 证书 / 模板 | `/api/admin/certificates`、`/api/admin/certificates/templates` |
| 公告 | `/api/admin/announcements` |

## API 文档（Apifox）

完整的 OpenAPI 3.0.3 文档与 Apifox 导入说明位于 `docs/`：

- `docs/openapi.yaml`：推荐导入 Apifox / Swagger Editor
- `docs/openapi.json`：JSON 版本
- `docs/README.md`：导入步骤、演示账号、建议测试顺序、错误约定

文档覆盖当前全部 147 个接口，并已与 Controller 路由逐条比对。修改接口后可运行：

```bash
python docs/compare_routes.py
python docs/validate_openapi.py
```

## 文件存储

- 小文件：`POST /api/files/upload`（multipart，字段 `file`，可选 `folder`）。
- 大文件：`POST /api/files/presign-upload` 获取预签名 PUT 地址，前端直传 MinIO。
- 稳定访问：`GET /api/files/view/{objectName}`（公开，可直接用于 `<img>` / `<video>` / 下载链接）。
- 预签名下载：`GET /api/files/presign?objectName=...`。
- 删除：`DELETE /api/files?objectName=...`。

启动时自动创建 `MINIO_BUCKET`；`MINIO_PUBLIC_READ=true` 时会设置公共只读策略，便于直接访问。生产环境建议关闭公共读并统一走预签名 URL。

## 常用命令

```bash
# 编译打包
mvn -DskipTests package

# 仅编译
mvn -DskipTests compile

# 前端构建（仓库根目录）
npm run build
```

## 说明

- MyBatis-Plus 已开启分页、逻辑删除、创建/更新时间自动填充；实体逻辑删除字段为 `deleted_at`。
- 所有日期时间按 `Asia/Shanghai` 序列化，日期时间格式为 ISO-8601（如 `2026-11-06T09:30:00`），日期格式为 `yyyy-MM-dd`。
- Controller 已启用 `-parameters` 编译参数，避免参数名解析告警。
- 前端 API 对接尚未切换，本服务当前以独立后端形式运行。
