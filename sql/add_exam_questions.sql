-- =============================================================================
-- 新增考试题目数据
-- 为现有考试添加更多题目
-- =============================================================================

USE enterprise_academy;
SET FOREIGN_KEY_CHECKS = 0;

-- 为 exam-1 (exam_id=1) 添加额外题目
INSERT INTO exam_question (id, ext_id, exam_id, question_no, type, score, title, correct_answer, reference_answer, analysis) VALUES
(100, 'q-101', 1, 26, 1, 5.0, '在微服务架构中，服务网格（Service Mesh）的主要作用是什么？', 'B', NULL, '服务网格提供服务间通信的基础设施层，包括负载均衡、服务发现、故障恢复等功能。'),
(101, 'q-102', 1, 27, 2, 6.0, '以下哪些是分布式事务的解决方案？（多选）', 'ACD', NULL, '分布式事务的常见解决方案包括两阶段提交、Saga 模式、TCC 模式等。'),
(102, 'q-103', 1, 28, 1, 5.0, '在 Kubernetes 中，Pod 的探针（Probe）主要用于什么？', 'A', NULL, '探针用于检测容器的健康状态，包括存活探针、就绪探针和启动探针。'),
(103, 'q-104', 1, 29, 3, 4.0, 'Redis 的持久化机制包括 RDB 和 AOF 两种方式。', 'A', NULL, 'Redis 支持 RDB（快照）和 AOF（追加日志）两种持久化方式。'),
(104, 'q-105', 1, 30, 4, 8.0, '请设计一个高可用的微服务架构，要求支持自动扩缩容、故障自愈和灰度发布，说明关键技术选型和实现方案。', NULL, '参考答案：1. 使用 Kubernetes 进行容器编排和自动扩缩容；2. 使用 Istio 服务网格实现流量管理和灰度发布；3. 使用 Prometheus + Grafana 进行监控告警；4. 使用 ELK 栈进行日志收集和分析；5. 使用 Consul 或 etcd 进行服务发现；6. 使用 Sentinel 或 Hystrix 实现熔断降级。', '高可用架构需要结合容器化、服务网格、监控告警等多种技术。');

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
INSERT INTO exam_question (id, ext_id, exam_id, question_no, type, score, title, correct_answer, reference_answer, analysis) VALUES
(105, 'q-106', 2, 4, 1, 6.0, '在分布式数据库中，CAP 定理指的是以下哪个组合？', 'A', NULL, 'CAP 定理包括一致性（Consistency）、可用性（Availability）和分区容错性（Partition tolerance）。'),
(106, 'q-107', 2, 5, 2, 8.0, '以下哪些是高可用系统的设计原则？（多选）', 'ABD', NULL, '高可用系统设计需要考虑冗余、故障转移、负载均衡等原则。'),
(107, 'q-108', 2, 6, 1, 6.0, '在数据库主从复制中，读写分离的主要目的是？', 'B', NULL, '读写分离将读请求路由到从库，写请求路由到主库，以提高系统性能。'),
(108, 'q-109', 2, 7, 3, 5.0, '数据库索引可以完全替代查询优化。', 'B', NULL, '索引是查询优化的一种手段，但不能完全替代其他优化方法如查询重写、分区等。'),
(109, 'q-110', 2, 8, 4, 12.0, '请设计一个支持百万级并发的数据库架构，要求数据一致性、可用性和扩展性，说明分库分表策略和缓存方案。', NULL, '参考答案：1. 采用分库分表策略，按业务域或用户 ID 分片；2. 使用读写分离提高读性能；3. 使用 Redis 缓存热点数据；4. 使用消息队列异步处理写操作；5. 实现数据同步和一致性保证机制；6. 使用 CDN 加速静态资源。', '百万级并发需要综合考虑分库分表、缓存、异步处理等多种技术。');

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
INSERT INTO exam_question (id, ext_id, exam_id, question_no, type, score, title, correct_answer, reference_answer, analysis) VALUES
(110, 'q-111', 3, 4, 1, 4.0, '在自然语言处理中，"词嵌入"（Word Embedding）的主要作用是什么？', 'A', NULL, '词嵌入将单词转换为低维向量表示，捕获语义信息。'),
(111, 'q-112', 3, 5, 2, 6.0, '以下哪些是机器学习的常见任务类型？（多选）', 'ABC', NULL, '机器学习任务包括分类、回归、聚类等。'),
(112, 'q-113', 3, 6, 1, 4.0, '在深度学习中，"过拟合"（Overfitting）指的是什么？', 'B', NULL, '过拟合是指模型在训练数据上表现很好，但在新数据上表现较差的现象。'),
(113, 'q-114', 3, 7, 3, 3.0, '所有的机器学习模型都需要大量的标注数据才能训练。', 'B', NULL, '并非所有模型都需要大量标注数据，如半监督学习、自监督学习等可以减少对标注数据的依赖。'),
(114, 'q-115', 3, 8, 4, 10.0, '请说明如何评估一个机器学习模型的性能，并介绍常用的评估指标和方法。', NULL, '参考答案：1. 使用准确率、精确率、召回率、F1 分数等分类指标；2. 使用均方误差、平均绝对误差等回归指标；3. 使用交叉验证避免过拟合；4. 使用混淆矩阵分析模型性能；5. 使用 ROC 曲线和 AUC 值评估分类模型。', '模型评估需要根据具体任务选择合适的指标和方法。');

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
INSERT INTO exam_question (id, ext_id, exam_id, question_no, type, score, title, correct_answer, reference_answer, analysis) VALUES
(115, 'q-116', 4, 4, 1, 5.0, '在前端开发中，"虚拟 DOM"的主要优势是什么？', 'A', NULL, '虚拟 DOM 通过减少直接 DOM 操作来提高性能，使用 diff 算法计算最小更新。'),
(116, 'q-117', 4, 5, 2, 7.0, '以下哪些是前端性能优化的常用技术？（多选）', 'ABD', NULL, '前端性能优化包括代码分割、懒加载、缓存策略、CDN 使用等。'),
(117, 'q-118', 4, 6, 1, 5.0, '在 JavaScript 中，"闭包"（Closure）指的是什么？', 'B', NULL, '闭包是指函数能够访问其词法作用域外的变量，即使函数在该作用域外执行。'),
(118, 'q-119', 4, 7, 3, 4.0, '使用 CDN 可以提高网站的加载速度。', 'A', NULL, 'CDN（内容分发网络）通过在全球部署节点来减少用户访问延迟，提高加载速度。'),
(119, 'q-120', 4, 8, 4, 10.0, '请设计一个前端性能监控系统，包括关键指标、数据采集、分析和告警机制，并说明如何优化页面加载性能。', NULL, '参考答案：1. 关键指标：FCP、LCP、FID、CLS、TTI 等；2. 数据采集：使用 Performance API、第三方监控工具（如 Google Analytics、Sentry）；3. 分析：使用数据可视化工具（如 Grafana）展示趋势；4. 告警：设置阈值，实时监控异常；5. 优化：代码分割、懒加载、图片压缩、缓存策略、CDN、SSR/SSG。', '前端性能监控需要综合考虑多个指标和优化策略。');

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
INSERT INTO exam_question (id, ext_id, exam_id, question_no, type, score, title, correct_answer, reference_answer, analysis) VALUES
(120, 'q-121', 5, 4, 1, 3.0, '以下关于网络安全的描述，正确的是？', 'C', NULL, '网络安全涉及保护网络系统、数据和信息免受未授权访问、使用、披露、破坏、修改或阻止的各种威胁。'),
(121, 'q-122', 5, 5, 2, 5.0, '以下哪些是常见的网络攻击手段？（多选）', 'ABC', NULL, '常见的网络攻击手段包括钓鱼攻击、DDoS 攻击、SQL 注入等。'),
(122, 'q-123', 5, 6, 1, 3.0, '在密码学中，"哈希函数"的主要作用是什么？', 'A', NULL, '哈希函数将任意长度的数据映射为固定长度的哈希值，用于数据完整性验证和密码存储。'),
(123, 'q-124', 5, 7, 3, 2.0, '使用 HTTPS 可以保证数据传输的绝对安全。', 'B', NULL, 'HTTPS 提供加密传输，但不能保证绝对安全，仍可能受到中间人攻击等威胁。'),
(124, 'q-125', 5, 8, 4, 6.0, '请说明如何识别和防范常见的社交工程攻击，并介绍企业应采取的安全措施。', NULL, '参考答案：1. 识别钓鱼邮件、电话诈骗、社交媒体欺诈等；2. 防范措施：提高安全意识培训、使用双因素认证、验证请求来源、报告可疑活动；3. 企业措施：制定安全政策、实施访问控制、定期安全审计、员工培训。', '社交工程攻击利用人类心理弱点，防范需要技术和管理措施相结合。');

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
INSERT INTO exam_question (id, ext_id, exam_id, question_no, type, score, title, correct_answer, reference_answer, analysis) VALUES
(125, 'q-126', 6, 4, 1, 6.0, '在数据库性能优化中，"索引"的主要作用是什么？', 'B', NULL, '索引是一种数据结构，用于加速数据库中的数据检索操作。'),
(126, 'q-127', 6, 5, 2, 8.0, '以下哪些是常见的数据库性能问题？（多选）', 'ABC', NULL, '慢查询、全表扫描和死锁是常见的数据库性能问题。'),
(127, 'q-128', 6, 6, 1, 6.0, '在 SQL 查询中，"EXPLAIN"命令的主要作用是什么？', 'A', NULL, 'EXPLAIN 用于分析 SQL 查询的执行计划，帮助识别性能瓶颈。'),
(128, 'q-129', 6, 7, 3, 5.0, '数据库索引可以完全替代查询优化。', 'B', NULL, '索引是查询优化的一种手段，但不能完全替代其他优化方法。'),
(129, 'q-130', 6, 8, 4, 15.0, '请分析以下 SQL 查询的性能问题，并提出优化建议：SELECT * FROM orders WHERE user_id = 123 AND status = ''pending'' ORDER BY created_at DESC LIMIT 10;', NULL, '参考答案：1. 使用具体列名而非 SELECT *；2. 确保 user_id 和 status 列有索引；3. 考虑复合索引 (user_id, status, created_at)；4. 使用 EXPLAIN 分析执行计划；5. 考虑分区表或归档旧数据；6. 使用覆盖索引减少回表查询。', 'SQL 查询优化需要结合索引、查询重写、执行计划分析等多种方法。');

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

SET FOREIGN_KEY_CHECKS = 1;

-- 更新考试题目数量缓存
UPDATE exam SET question_count_cached = (
  SELECT COUNT(*) FROM exam_question WHERE exam_id = exam.id
) WHERE id IN (1, 2, 3, 4, 5, 6);
