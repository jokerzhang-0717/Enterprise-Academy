# 企业学堂 API 文档（Apifox 导入说明）

本目录下的 OpenAPI 文档覆盖了当前 `emp-server` 的 **147 个接口**（学员端 + 管理端 + 文件），已与 Controller 注解逐条比对，可直接导入 Apifox。

| 文件 | 用途 |
| --- | --- |
| `openapi.yaml` | 推荐。OpenAPI 3.0.3，含中文说明、请求体示例、错误响应 |
| `openapi.json` | 同上，JSON 格式；部分工具只接受 JSON 时使用 |
| `validate_openapi.py` | 校验 YAML 语法、`$ref` 是否完整 |
| `compare_routes.py` | 比对文档接口与 Java Controller 实际路由 |

## 一、导入 Apifox

1. 打开 Apifox，新建项目（例如「企业学堂」）。
2. 进入「项目设置 → 导入数据 → OpenAPI / Swagger」。
3. 选择 `emp-server/docs/openapi.yaml`，导入模式首次选「新建接口」。
4. 导入后检查「环境」：
   - 服务地址：`http://127.0.0.1:8080/api`
   - 如果 Apifox 自动读取了 `servers`，无需重复填写。
5. 鉴权设置（二选一）：
   - 在项目根目录或「学员端」目录设置 Auth：`Bearer Token`，值为 `{{token}}`；
   - 或逐个请求在 Auth 里选择 Bearer Token。
   - 登录、注册、证书验真、文件访问接口在文档中已标记为无需鉴权。

> **导入后每个接口的 Auth 都显示「继承父级 / `{{bearerToken}}`」是正常的。**
> 文档根级声明了全局 `security: bearerAuth`，Apifox 会把它抽成一个项目级「默认鉴权凭证」，所有子接口默认继承，所以看起来每个接口都一样。不要逐个接口改，改一处即可：
>
> - **最省事**：环境「本地开发」新增变量 `bearerToken`，值填登录返回的 `data.token`，所有接口自动生效；
> - **想统一用 `token`**：左侧「组件库 → 鉴权组件」→ 打开 `默认鉴权凭证`（来自文档里的 `bearerAuth`）→ 配置默认鉴权值，Bearer Token 填 `{{token}}` → 保存。所有「继承父级」的接口一起生效。
> - 注意：Apifox 的「批量管理」只支持批量删除、移动、标签、状态、负责人、导出，**不支持批量修改鉴权**；鉴权要用「组件默认值 + 目录继承」来批量生效。
> - 只有个别接口需要特殊鉴权时，才在接口自己的 Auth 里把「继承父级」改成独立配置。

> 后端上下文路径是 `/api`，所以完整地址是 `http://127.0.0.1:8080/api/auth/login`，不是 `http://127.0.0.1:8080/auth/login`。

## 二、先登录拿 token

请求：

```http
POST /auth/login
Content-Type: application/json

{
  "employeeNo": "EMP-2024098",
  "password": "123456"
}
```

成功响应：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 43200,
    "user": {
      "id": 1,
      "employeeNo": "EMP-2024098",
      "name": "李明",
      "role": 1
    }
  }
}
```

把 `data.token` 复制到 Apifox 环境变量 `token`，后续请求携带：

```http
Authorization: Bearer {{token}}
```

演示账号：

| 角色 | 工号 | 密码 | 可访问范围 |
| --- | --- | --- | --- |
| 学员 | `EMP-2024098` | `123456` | 学员端接口 |
| 管理员 | `admin` | `123456` | 学员端 + `/admin/**` |

## 三、建议测试顺序

### 学员端

1. `POST /auth/login` → 保存 token。
2. `GET /auth/me` → 验证 token。
3. `GET /dashboard` → 首页聚合数据。
4. `GET /courses?page=1&size=12` → 课程列表。
5. `GET /courses/{id}` → 课程详情（章节、课时）。
6. `POST /courses/{id}/enroll` → 报名课程。
7. `GET /tracks?page=1&size=12` → 研修通道。
8. `GET /exams` → 我的考试。
9. `POST /exams/{examId}/start` → 开始考试，返回 `attemptId` 和题目。
10. `POST /exams/attempts/{attemptId}/answers` → 逐题保存答案。
11. `POST /exams/attempts/{attemptId}/switch` → 记录切屏。
12. `POST /exams/attempts/{attemptId}/submit` → 交卷。
13. `GET /leaves`、`POST /leaves` → 请假。
14. `GET /checkins/today`、`POST /checkins/check-in`、`POST /checkins/check-out` → 签到。
15. `GET /certificates` → 我的证书。
16. `GET /announcements`、`GET /notifications` → 公告与通知。
17. `POST /files/upload`（multipart，字段 `file`）→ 文件上传。

> 开始考试会真实创建答卷，建议用演示学员账号测试，避免污染正式数据。

### 管理端

1. 重新调用 `POST /auth/login`，使用 `admin / 123456`，把新 token 写入另一个环境变量（如 `adminToken`）。
2. `GET /admin/dashboard` → 管理看板。
3. `GET /admin/users?page=1&size=10`、`GET /admin/departments` → 用户与部门。
4. `GET /admin/courses`、`GET /admin/exams`、`GET /admin/questions?examId=1` → 课程与题库。
5. `GET /admin/exam-attempts?page=1&size=10` → 答卷列表；再调用 `POST /admin/exam-attempts/{id}/grade` 评分。
6. `GET /admin/leaves?status=1` → 待审批请假；`PUT /admin/leaves/{id}/approve` 审批。
7. `GET /admin/checkins`、`GET /admin/certificates`、`GET /admin/announcements` → 其余管理列表。
8. 用学员 token 调用任一 `/admin/**` 接口，应返回 `403`。

## 四、响应与错误约定

统一响应：

```json
{ "code": 0, "message": "ok", "data": {} }
```

- `code = 0` 表示成功；失败时 `code` 与 HTTP 状态码一致。
- 分页数据：`data = { total, page, size, records }`。
- 日期时间使用 ISO-8601：`2026-11-06T09:30:00`；日期使用 `2026-11-06`。
- 常见错误：`400` 参数/业务校验、`401` 未登录、`403` 无权限、`404` 不存在、`409` 冲突、`413` 文件过大、`415` 请求格式不支持。

## 五、文件接口

- 小文件上传：`POST /files/upload`，`multipart/form-data`，字段 `file`，可选 `folder`。
- 大文件直传：`POST /files/presign-upload` 获取 `uploadUrl`，前端直接 `PUT` 到 MinIO。
- 稳定访问：`GET /files/view/{objectName}`（公开，可直接用于 `<img>` / `<video>`）。
  - 注意：`objectName` 含 `/` 时，部分客户端会把 `/` 编码成 `%2F`，Tomcat 会返回 400。测试时直接使用上传接口返回的完整 `url`，或改用预签名下载地址即可。
- 预签名下载：`GET /files/presign?objectName=...`。
- 删除：`DELETE /files?objectName=...`。

文件接口依赖 MinIO 正常运行（默认 `http://127.0.0.1:9000`，桶 `emp-academy`）。

## 六、维护文档

修改 Controller 后，重新生成并校验：

```bash
# 1. 确认文档与 Java 路由一致
python docs/compare_routes.py

# 2. 校验 OpenAPI 语法与 $ref
python docs/validate_openapi.py

# 3. 如使用 Python，可进一步做规范校验
python -c "from openapi_spec_validator import validate; import yaml, pathlib; p=pathlib.Path('docs/openapi.yaml'); validate(yaml.safe_load(p.read_text(encoding='utf-8'))); print('ok')"
```

文档源文件已合并为 `openapi.yaml` / `openapi.json`；`.yaml` 是后续修改的主文件。
