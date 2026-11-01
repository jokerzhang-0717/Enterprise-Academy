export const currentUserZhang = {
  id: 'zhanghr',
  name: '张浩然',
  employeeId: 'EXP-70291',
  department: '研发部',
  title: '高级工程师',
  role: 'P6+ 后端架构方向',
  learningHours: 42.5,
  completedCourses: 8,
  avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=120&q=80',
  grade: 'P6+ 架构师专班'
};

export const CURRENT_USER = currentUserZhang;


export const currentUserLi = {
  id: 'liming',
  name: '李明',
  employeeId: 'EMP-2024098',
  department: '研发部',
  title: '高级研发工程师',
  role: '云原生与微服务核心骨干',
  learningHours: 18.5,
  completedCourses: 3,
  avatarUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=120&q=80',
  grade: 'P6 高级研发'
};

export const SCREEN_MOCKUPS = {
  home: {
    title: '首页研学工作台 (Home Dashboard)',
    path: '/assets/aistudio/screen_3a2933c7.png',
    desc: '研学早报、今日签到考勤、合规红线考核倒计时、周目标进度与正在进行专项'
  },
  courses: {
    title: '课程学习中心 (Course Center)',
    path: '/assets/aistudio/screen_32dd287c.png',
    desc: '课程库分类检索、断点续学提示、个人研学档案与多门精品研修课程卡片'
  },
  coursePlayer: {
    title: '微服务课程视频与实操学习大屏 (Course Player)',
    path: '/assets/aistudio/screen_85281827.png',
    desc: '视频回放水印、大纲章节进度、Redis分布式锁代码与Lua原子解脱实操'
  },
  exams: {
    title: '考试与测评中心 (Exam Center)',
    path: '/assets/aistudio/screen_74c3fef1.png',
    desc: '待参加考评矩阵、防作弊监控提示、往期考评档案与错题深度回顾'
  },
  examTaking: {
    title: '统一在线考试作答大屏 (Exam In-Progress)',
    path: '/assets/aistudio/screen_f82e8a4b.png',
    desc: '倒计时计时器、25题答题卡、Java 21虚拟线程核心考题与双机位监考中枢'
  },
  analytics: {
    title: '个人学习看板 (Personal Study Dashboard)',
    path: '/assets/aistudio/screen_15977acc.png',
    desc: '研学效能全景、学时课时双轴趋势、六维能力雷达与晋升专班进阶通道'
  },
  attendance: {
    title: '培训考勤打卡与请假联动中心 (Attendance & Leave)',
    path: '/assets/aistudio/screen_27c7faa3.png',
    desc: '混合教学签到/签退、实时打卡通道、请假申请表单与在线审批闭环流转'
  },
  certificates: {
    title: '荣誉证书与学术问答社区 (Certificates & Honors)',
    path: '/assets/aistudio/screen_a78d2b69.png',
    desc: '官方认证防伪证书、区块链存证哈希、专家资质徽章与问答交流研学'
  },
  messages: {
    title: '消息与站内信中心 (Messages & Notifications)',
    path: '/assets/aistudio/screen_d0877850.png',
    desc: '分类信件列表、合规考核通报详情抽屉、学习资料附件下载与考试快速通道'
  },
  login: {
    title: '欢迎回到培训工作台 (Login / Auth Screen)',
    path: '/assets/aistudio/screen_9fb1de42.png',
    desc: '企业级单点登录网关、滑块安全验证、安全合规环境与核心能力矩阵'
  }
};

export const COURSE_CATEGORIES = [
  { id: 'core', label: '通识素养' },
  { id: 'tech', label: '技术研发' },
  { id: 'product', label: '产品与业务' },
  { id: 'leadership', label: '领导力与管理' },
  { id: 'compliance', label: '职场素养与安全' }
];

export const COURSES_DATA = [
  {
    id: 'c1',
    title: 'Spring Boot 3 + MyBatis-Plus 企业级高并发架构',
    category: 'tech',
    isRequired: true,
    categoryLabel: '研发架构',
    level: '必修课程',
    hours: 32,
    students: 1280,
    progress: 66,
    totalLessons: 32,
    completedLessons: 21,
    lastStudied: '3小时前',
    instructor: {
      name: '陈建邦',
      title: '资深架构师'
    },
    tags: ['研发架构', '高并发调优'],
    status: 'studying',
    badge: '学习中 · 66%',
    badgeType: 'primary',
    thumbnail: 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=600&q=80',
    summary: '深度解析十万级 QPS 场景下的缓存穿透熔断、分布式分布式锁性能压榨与底层 SQL 执行优化秘籍。'
  },
  {
    id: 'c2',
    title: '企业网络安全防护规范与代码安全审判指南',
    category: 'compliance',
    isRequired: true,
    categoryLabel: '年度红线合规',
    level: '必修红线课程',
    hours: 12,
    students: 2450,
    progress: 0,
    totalLessons: 12,
    completedLessons: 0,
    instructor: {
      name: '集团安全部',
      title: '信息合规组'
    },
    tags: ['年度红线合规', 'OWASP Top 10'],
    status: 'not_started',
    badge: '剩余 4 天截止',
    badgeType: 'urgent',
    thumbnail: 'https://images.unsplash.com/photo-1550751827-4bd374c3f58b?auto=format&fit=crop&w=600&q=80',
    summary: '系统剖析SQL注入防御、敏感数据脱敏规约、第三方依赖漏洞扫描与全链路防基线实操规范。'
  },
  {
    id: 'c3',
    title: '高效跨部门沟通与敏捷项目管理实践',
    category: 'leadership',
    isRequired: false,
    categoryLabel: '项目管理',
    level: '管理通识',
    hours: 16,
    students: 940,
    progress: 100,
    totalLessons: 16,
    completedLessons: 16,
    score: 96,
    instructor: {
      name: '林欣怡',
      title: 'PMP 专家教练'
    },
    tags: ['项目管理', '职场协作'],
    status: 'completed',
    badge: '已结课 · 成绩优秀 96分',
    badgeType: 'success',
    thumbnail: 'https://images.unsplash.com/photo-1522071820081-009f0129c71c?auto=format&fit=crop&w=600&q=80',
    summary: '拆解 OKR 对齐困境、Scrum 迭代节奏控制、技术与业务语言翻译技巧，助力打造高效协作自驱团队。'
  },
  {
    id: 'c4',
    title: '云原生微服务观测: Prometheus + OpenTelemetry',
    category: 'tech',
    isRequired: false,
    categoryLabel: 'SRE & 运维',
    level: '进阶技术',
    hours: 24,
    students: 1120,
    progress: 35,
    totalLessons: 24,
    completedLessons: 8,
    instructor: {
      name: '赵明哲',
      title: '基础设施技术专家'
    },
    tags: ['SRE & 运维', '分布式追踪'],
    status: 'studying',
    badge: '学习中 · 35%',
    badgeType: 'primary',
    thumbnail: 'https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=600&q=80',
    summary: '从指标采集、服务拓扑关系链路跟踪到核心告警策略实战，打通可观测性体系全生命周期闭环。'
  },
  {
    id: 'c5',
    title: '职场心理学与压力管理工坊',
    category: 'core',
    isRequired: false,
    categoryLabel: '素养选修',
    level: '素养选修',
    hours: 8,
    students: 860,
    progress: 0,
    totalLessons: 8,
    completedLessons: 0,
    instructor: {
      name: '周文雅',
      title: '资深EAP特聘导师'
    },
    tags: ['身心健康', '情绪认知'],
    status: 'not_started',
    badge: '情境互动微课',
    badgeType: 'warning',
    thumbnail: 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=600&q=80',
    summary: '国家二级心理咨询师亲授认知重构模型，助你在快节奏交付中建立身心心理韧性，告别职场耗竭。'
  },
  {
    id: 'c6',
    title: '金融级数据合规与业务出海跨境传输实战',
    category: 'compliance',
    isRequired: true,
    categoryLabel: '法务合规',
    level: '出海业务必修',
    hours: 18,
    students: 620,
    progress: 15,
    totalLessons: 18,
    completedLessons: 3,
    instructor: {
      name: '韩立言',
      title: '涉外合规法务总监'
    },
    tags: ['法务合规', '全球化战略'],
    status: 'studying',
    badge: '学习中 · 15%',
    badgeType: 'primary',
    thumbnail: 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=600&q=80',
    summary: '系统解读 GDPR、新加坡 PDPA 与国内数据出境评估申报全流程，避开海外隐私保护巨额罚单。'
  },
  {
    id: 'c7',
    title: 'AI 大模型应用开发: 从 Prompt 工程到 RAG 实战',
    category: 'tech',
    isRequired: false,
    categoryLabel: '人工智能',
    level: '进阶技术',
    hours: 20,
    students: 780,
    progress: 45,
    totalLessons: 20,
    completedLessons: 9,
    instructor: {
      name: '郑亦辰',
      title: '算法平台负责人'
    },
    tags: ['人工智能', 'RAG 检索增强'],
    status: 'studying',
    badge: '学习中 · 45%',
    badgeType: 'primary',
    thumbnail: 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=600&q=80',
    summary: '从提示词设计、向量检索到知识库切分与召回评估,完整搭建企业级 RAG 应用链路。'
  },
  {
    id: 'c8',
    title: '产品需求分析与用户旅程地图实战',
    category: 'product',
    isRequired: false,
    categoryLabel: '产品与业务',
    level: '产品进阶',
    hours: 12,
    students: 520,
    progress: 0,
    totalLessons: 12,
    completedLessons: 0,
    instructor: {
      name: '许清和',
      title: '资深产品专家'
    },
    tags: ['需求分析', '用户体验'],
    status: 'not_started',
    badge: '新课上线',
    badgeType: 'warning',
    thumbnail: 'https://images.unsplash.com/photo-1522071820081-009f0129c71c?auto=format&fit=crop&w=600&q=80',
    summary: '拆解真实业务场景下的需求澄清、优先级排序与用户旅程地图绘制方法,提升需求交付质量。'
  },
  {
    id: 'c9',
    title: '高绩效团队目标管理与 OKR 落地',
    category: 'leadership',
    isRequired: false,
    categoryLabel: '领导力与管理',
    level: '管理通识',
    hours: 10,
    students: 640,
    progress: 100,
    totalLessons: 10,
    completedLessons: 10,
    score: 92,
    instructor: {
      name: '林欣怡',
      title: 'PMP 专家教练'
    },
    tags: ['目标管理', '团队协作'],
    status: 'completed',
    badge: '已结课 · 成绩优秀 92分',
    badgeType: 'success',
    thumbnail: 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=600&q=80',
    summary: '围绕 OKR 制定、对齐与复盘,结合团队激励与反馈机制,帮助管理者把目标真正落到日常执行。'
  },
  {
    id: 'c10',
    title: '数据可视化与 BI 报表体系搭建',
    category: 'tech',
    isRequired: false,
    categoryLabel: '数据分析',
    level: '进阶技术',
    hours: 16,
    students: 430,
    progress: 25,
    totalLessons: 16,
    completedLessons: 4,
    instructor: {
      name: '赵明哲',
      title: '数据平台架构师'
    },
    tags: ['数据可视化', '指标体系'],
    status: 'studying',
    badge: '学习中 · 25%',
    badgeType: 'primary',
    thumbnail: 'https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=600&q=80',
    summary: '从指标体系设计、数据建模到可视化图表选型,搭建可持续迭代的企业 BI 报表体系。'
  },
  {
    id: 'c11',
    title: '商务演讲与结构化表达训练营',
    category: 'core',
    isRequired: false,
    categoryLabel: '通识素养',
    level: '素养选修',
    hours: 8,
    students: 910,
    progress: 0,
    totalLessons: 8,
    completedLessons: 0,
    instructor: {
      name: '周文雅',
      title: '资深表达教练'
    },
    tags: ['结构化表达', '商务沟通'],
    status: 'not_started',
    badge: '情境互动微课',
    badgeType: 'warning',
    thumbnail: 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=600&q=80',
    summary: '用金字塔结构与故事化表达组织内容,在汇报、路演与跨部门沟通中清晰传递关键信息。'
  },
  {
    id: 'c12',
    title: '个人信息保护法与员工数据合规实务',
    category: 'compliance',
    isRequired: true,
    categoryLabel: '法务合规',
    level: '全员必修',
    hours: 6,
    students: 1520,
    progress: 60,
    totalLessons: 6,
    completedLessons: 4,
    instructor: {
      name: '王若琳',
      title: '数据合规法务顾问'
    },
    tags: ['个人信息保护', '数据合规'],
    status: 'studying',
    badge: '学习中 · 60%',
    badgeType: 'primary',
    thumbnail: 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=600&q=80',
    summary: '结合《个人信息保护法》与内部员工数据管理场景,讲清收集、存储、使用与删除的合规边界。'
  }
];

export const RECOMMENDED_TRACKS = [
  {
    id: 'trk-1',
    category: 'security',
    categoryLabel: '合规安全',
    title: '金融级数据隐私与跨国数据传输实战',
    thumbnail: 'https://images.unsplash.com/photo-1563986768609-322da13575f3?auto=format&fit=crop&w=800&q=80',
    summary: '深入解析 GDPR 与国内数据出境评估核心条款及研发风控策略，帮助团队建立合规的数据跨境传输流程。',
    description: '本课程结合金融业务中的数据处理场景，讲解个人信息保护、数据分类分级和跨境传输的关键合规要求。学员将学习如何识别数据出境风险、理解安全评估流程，并把合规要求落实到产品设计、研发流程和日常运营中。课程通过典型案例分析与实战检查清单，帮助团队建立可执行、可追溯的数据跨境治理机制。',
    lectureCount: 6,
    studentCount: '1,420',
    duration: '约 3 小时',
    level: '进阶',
    audience: '参与数据治理、隐私保护、跨境业务或金融业务研发的产品、研发与合规人员。',
    outline: [
      '数据隐私保护与 GDPR 核心原则',
      '国内数据出境安全评估要求',
      '跨境数据传输风险识别',
      '金融业务研发风控策略',
      '合规评估案例解析',
      '跨境传输实战与检查清单'
    ]
  },
  {
    id: 'trk-2',
    category: 'tech',
    categoryLabel: '云原生',
    title: 'Kubernetes 生产集群性能调优实战',
    thumbnail: 'https://images.unsplash.com/photo-1558494949-ef010cbdcc31?auto=format&fit=crop&w=800&q=80',
    summary: '涵盖 OOM Killer、网络抖动、etcd 性能衰退等 30 个经典生产排障案例，系统提升 Kubernetes 集群诊断与调优能力。',
    description: '本课程面向 Kubernetes 生产集群的实际运维与性能治理，带领学员从监控指标和性能基线入手，逐步定位 CPU、内存、网络及存储等常见瓶颈。课程通过 OOM、网络抖动和 etcd 延迟等生产案例，演示排查思路、调优方法与风险控制，并介绍如何通过故障演练和复盘持续提升集群稳定性。',
    lectureCount: 12,
    studentCount: '2,890',
    duration: '约 6 小时',
    level: '高级',
    audience: '负责 Kubernetes 集群运维、平台工程、云原生应用研发与生产故障排查的技术人员。',
    outline: [
      '生产集群性能基线与监控',
      'CPU、内存与 OOM 问题排查',
      '网络延迟与丢包诊断',
      'etcd 性能分析与优化',
      '调度策略与资源配额调优',
      '集群故障演练与复盘'
    ]
  },
  {
    id: 'trk-3',
    category: 'leadership',
    categoryLabel: '效能工具',
    title: '敏捷开发协同与高绩效团队实践',
    thumbnail: 'https://images.unsplash.com/photo-1522071820081-009f0129c71c?auto=format&fit=crop&w=800&q=80',
    summary: '围绕 Scrum 迭代闭环与跨部门协同，拆解敏捷团队的度量方法，帮助团队提升交付效率与协作质量。',
    description: '本课程围绕敏捷团队的日常协作，介绍 Scrum 迭代中的目标制定、需求拆解、计划执行和回顾改进。课程重点讨论跨部门协作中的信息同步与责任边界，并通过团队效能指标和真实场景案例，帮助学员发现协作瓶颈、改善交付节奏，建立持续改进的团队工作方式。',
    lectureCount: 5,
    studentCount: '980',
    duration: '约 2.5 小时',
    level: '进阶',
    audience: '需要参与敏捷项目协作、跨部门沟通或团队效能改进的项目成员与管理者。',
    outline: [
      '敏捷协作与 Scrum 基础',
      '迭代计划与需求拆解',
      '跨部门协同与沟通机制',
      '团队效能度量与持续改进',
      '高绩效团队案例复盘'
    ]
  }
];

export const EXAMS_DATA = [
  {
    id: 'exam-1',
    code: 'ZK-20250422-09',
    title: '2025年度第二季度研发部全栈开发规范与安全编码认证考试',
    category: '技术合规部 · 2025Q2',
    status: 'in_progress',
    location: '线上隔离沙箱 (IDE内联)',
    timeWindow: '2025-04-18 14:00 ~ 15:30',
    durationMinutes: 60,
    totalScore: 100,
    passScore: 80,
    remainingTimeText: '剩余作答: 43 分钟',
    isDoubleCameraProctored: true,
    relatedCourse: '《企业级微服务与云原生架构进阶》 (已修完100%)',
    questionCount: 25
  },
  {
    id: 'exam-2',
    code: 'ZK-20250422-10',
    title: '2025Q2 分布式存储与高并发容灾等级认证实操机考',
    category: '基础架构委员会',
    status: 'not_started',
    location: '线下实训机房 A座402',
    timeWindow: '2025-04-22 09:30 ~ 11:30',
    durationMinutes: 120,
    totalScore: 100,
    passScore: 85,
    remainingTimeText: '倒计时: 3天 18小时',
    isDoubleCameraProctored: false,
    relatedCourse: '《分布式数据库与高可用单元化架构》 (进度 88%)',
    questionCount: 15
  },
  {
    id: 'exam-3',
    code: 'ZK-20250410-01',
    title: '2025年度企业生成式 AI 研发与数据脱敏应用上岗资格考核',
    category: '算法与AI合规部',
    status: 'anytime',
    location: '在线网页自适应考试端',
    timeWindow: '2025-04-10 ~ 04-30',
    durationMinutes: 45,
    totalScore: 100,
    passScore: 80,
    remainingTimeText: '剩余考评机会: 2次',
    isDoubleCameraProctored: false,
    relatedCourse: '《大模型应用安全与生成式AI提示词规范》 (已结课)',
    questionCount: 30
  },
  {
    id: 'exam-4',
    code: 'ZK-20250506-12',
    title: '2025Q3 前端工程化与页面性能优化认证',
    category: '前端技术委员会',
    status: 'not_started',
    location: '线上隔离沙箱 (IDE内联)',
    timeWindow: '2025-05-28 14:00 ~ 15:30',
    durationMinutes: 90,
    totalScore: 100,
    passScore: 80,
    remainingTimeText: '倒计时: 9天 6小时',
    isDoubleCameraProctored: false,
    relatedCourse: '《前端工程化与构建优化实战》 (进度 72%)',
    questionCount: 20
  },
  {
    id: 'exam-5',
    code: 'ZK-20250428-03',
    title: '2025年度信息安全意识全员测评',
    category: '信息安全部',
    status: 'anytime',
    location: '在线网页自适应考试端',
    timeWindow: '2025-05-01 ~ 05-31',
    durationMinutes: 30,
    totalScore: 100,
    passScore: 80,
    remainingTimeText: '剩余考评机会: 3次',
    isDoubleCameraProctored: false,
    relatedCourse: '《企业网络安全防护规范》 (必修)',
    questionCount: 18
  },
  {
    id: 'exam-6',
    code: 'ZK-20250512-07',
    title: '2025Q2 数据库性能调优与慢查询治理实战认证',
    category: '基础架构委员会',
    status: 'in_progress',
    location: '线上实训环境',
    timeWindow: '2025-05-12 09:30 ~ 11:00',
    durationMinutes: 90,
    totalScore: 100,
    passScore: 85,
    remainingTimeText: '剩余作答: 26 分钟',
    isDoubleCameraProctored: true,
    relatedCourse: '《分布式数据库与高可用单元化架构》 (进度 90%)',
    questionCount: 22
  }
];

export const PAST_EXAMS = [
  {
    id: 'past-1',
    code: 'CER-202503-ARCH-0914',
    title: '2025Q1 微服务高可用与架构容灾实战测评',
    type: '技术晋升认证',
    completionDate: '2025-03-28 15:40',
    score: 92,
    passScore: 80,
    status: 'passed_excellent',
    wrongCount: 2,
    relatedCourse: '《微服务架构模式》'
  },
  {
    id: 'past-2',
    code: 'CER-202501-SEC-3321',
    title: '企业数据安全与隐私合规 (GDPR/PIPL) 标准考评',
    type: '全员合规必修',
    completionDate: '2025-01-14 10:22',
    score: 100,
    passScore: 85,
    status: 'passed_perfect',
    wrongCount: 0,
    relatedCourse: '《法务与合规必修》'
  },
  {
    id: 'past-3',
    code: 'CER-202411-OPS-7781',
    title: 'Kubernetes 云原生容器编排与 CI/CD 流水线实践',
    type: '专业技能考级',
    completionDate: '2024-11-20 16:50',
    score: 86,
    passScore: 80,
    status: 'passed_qualified',
    wrongCount: 4,
    relatedCourse: '《云原生运维》'
  },
  {
    id: 'past-4',
    code: 'CER-202503-TRN-1206',
    title: '2025Q1 企业内训师授课能力认证',
    type: '内训师认证',
    completionDate: '2025-03-12 11:20',
    score: 88,
    passScore: 80,
    status: 'passed_excellent',
    wrongCount: 3,
    relatedCourse: '《课程设计与讲授技巧》'
  },
  {
    id: 'past-5',
    code: 'CER-202502-FIN-4417',
    title: '2025Q1 财务共享与费用报销规范考核',
    type: '全员合规必修',
    completionDate: '2025-02-21 14:10',
    score: 94,
    passScore: 85,
    status: 'passed_excellent',
    wrongCount: 1,
    relatedCourse: '《财务报销制度解读》'
  },
  {
    id: 'past-6',
    code: 'CER-202412-DEV-6602',
    title: '研发效能与代码评审规范专项测评',
    type: '专业技能考级',
    completionDate: '2024-12-18 17:30',
    score: 79,
    passScore: 80,
    status: 'failed',
    wrongCount: 7,
    relatedCourse: '《研发效能与工程规范》'
  }
];

export const CERTIFICATES_DATA = [
  {
    id: 'cert-1',
    serialNumber: 'ZX-2024-MS-089',
    title: '微服务高可用架构实战专家认证',
    level: 'LEVEL 4 · 专家级认证',
    category: 'architecture',
    issueOrg: '企业技术委员会',
    issueDate: '2024-03-15',
    validity: '长期有效',
    rankingScore: 'TOP 3% (卓越)',
    description: '表彰在 Spring Cloud 微服务拆分、容灾治理、Sentinel 限流降级及分布式链路追踪实战中取得卓越表现，且通过企业高并发沙箱实战考核。',
    badgeType: 'blockchain',
    privilege: '区块链存证防伪哈希已校验'
  },
  {
    id: 'cert-2',
    serialNumber: 'ZX-2024-SEC-412',
    title: '企业数据安全与隐私合规 (GDPR/PIPL) 专家证书',
    level: '法定合规 · 重点资格',
    category: 'compliance',
    issueOrg: '风控与安全合规部',
    issueDate: '2024-02-28',
    validity: '24 个月 (2026年到期)',
    description: '掌握个人信息保护法与跨境数据传输规范，通过生产库敏感字段脱敏策略实施、日志脱敏与审计防篡改专项考试。',
    badgeType: 'security',
    privilege: '具备生产环境数据脱敏审批权'
  },
  {
    id: 'cert-3',
    serialNumber: 'ZX-2023-K8S-901',
    title: 'Kubernetes 云原生容器编排认证',
    level: '运维基建 · 认证',
    category: 'devops',
    issueOrg: '企业技术委员会',
    issueDate: '2023-11-20',
    validity: '长期有效',
    rankingScore: '98 / 100',
    description: '认证合格完成生产级 K8s 集群 Helm 部署治理、服务网格 Istio 流量路由、Pod 弹性伸缩策略及 StatefulSet 灾难演练。',
    badgeType: 'cloud',
    privilege: '生产级 DevOps 权限就绪'
  },
  {
    id: 'cert-4',
    serialNumber: 'ZX-2024-AI-015',
    title: '企业生成式 AI 研发提效工程专家',
    level: 'AI 效能 · 先锋认证',
    category: 'ai',
    issueOrg: 'AI 创新实验室',
    issueDate: '2024-04-10',
    validity: '长期有效',
    rankingScore: 'Platinum 徽章',
    description: '掌握大模型企业知识库 RAG 架构搭建、Prompt 工程进阶微调、自动化单元测试生成与安全代码规审审计实践。',
    badgeType: 'ai',
    privilege: '企业 Copilot 提示词金牌贡献者'
  }
];

export const MESSAGES_DATA = [
  {
    id: 'msg-1',
    type: 'exam',
    typeLabel: '考试与测评',
    isRead: false,
    title: '企业信息安全与合规年度闭卷认证考核',
    sender: '企业信息安全委员会',
    time: '10:24',
    preview: '根据集团合规规定，2025年第一季度全员安全规范考核现已开启。请务必于5月22日 18:00前完成闭卷考试，附带最新实训手册供下载复习。',
    attachmentsCount: 2,
    priority: 'urgent',
    details: {
      fullContent: `各位研发部及业务骨干员工：\n\n依据集团《信息资产保密制度》与《2025年数字合规准则》要求，本年度第一次全员技术与安全合规在线统考将于 2025年5月22日 18:00 截止准入。\n\n请在截止前安排独立时间完成答卷，答题过程请保持摄像头正常接入，全屏防切屏机制已开启。`,
      bulletPoints: [
        '全卷共50题（单选30题、多选15题、案例分析5题），答题中途严禁切出全屏模式超过3次；',
        '支持补考机会2次，取最高成绩作为评定标准；',
        '考核结果直接纳入 Q2 员工晋升发展基线指标。'
      ],
      officialNoticeId: 'SEC-2025-Q2-0892',
      examDuration: '60 分钟',
      passingScore: '85分及格',
      attachments: [
        {
          name: '2025Q2技术实训合规手册.pdf',
          size: '4.8 MB',
          source: '集团法务合规部编印'
        },
        {
          name: '安全实操自测易错题精讲.docx',
          size: '1.2 MB',
          source: '附带重点标红解析'
        }
      ]
    }
  },
  {
    id: 'msg-2',
    type: 'course',
    typeLabel: '课程学习',
    isRead: false,
    title: '进阶必修：分布式微服务治理及高可用落地',
    sender: '集团教培中心',
    time: '昨日 14:10',
    preview: '您报名的《微服务架构与云原生运维进阶》已完成第3章内容学习。第4章「容灾高可用故障演练实战」现已开放，请保持进度。'
  },
  {
    id: 'msg-3',
    type: 'attendance',
    typeLabel: '考勤与请假',
    isRead: true,
    title: '脱产培训公假审批通过通知',
    sender: '考勤服务系统',
    time: '05-16',
    preview: '您于05月15日提交的「2025Q2新技术线下集训营调休请假」申请已通过部门负责人审批，系统已同步更新考勤打卡免除规则。'
  },
  {
    id: 'msg-4',
    type: 'announcement',
    typeLabel: '系统公告',
    isRead: true,
    title: '智慧学习平台 3.8.0 架构升级与新特性说明',
    sender: '智训云平台技术运维中心',
    time: '05-14',
    preview: '云学平台将于本周五晚23:00至次日凌晨02:00进行只读维护升级，届时将上线AI智能陪练助手与知识库精准检索功能。'
  }
];

export const LEAVE_REQUESTS_DATA = [
  {
    id: 'lv-1',
    type: '项目紧急发版 / 生产排期',
    flowNumber: 'LV-20250518-004',
    title: '《2025 架构设计委员会春季技术研讨与实操培训》',
    timeRange: '2025-05-18',
    days: '0.5',
    reason: '因核心结算集群在16:30进行双机房切流验证，需现场跟进发布 (工单: PROD-OPS-20250518-09)。申请缺席下午后半程实操。',
    status: 'pending',
    submittedTime: '2025-05-18 10:15'
  },
  {
    id: 'lv-rejected-example',
    type: '个人事务 / 家庭安排',
    flowNumber: 'LV-20250516-003',
    title: '《2025 架构设计委员会春季技术研讨与实操培训》',
    timeRange: '2025-05-16',
    days: '1',
    reason: '因个人家庭事务申请缺席全天培训。',
    status: 'rejected',
    submittedTime: '2025-05-15 09:20'
  },
  {
    id: 'lv-2',
    type: '因公外出 / 外部交流',
    flowNumber: 'LV-20250512-009',
    title: '《云原生微服务高可用容灾架构实践》',
    timeRange: '2025-05-12',
    days: '0.5',
    reason: '赴招商银行总部参加开放平台二期架构方案评审，现场进行方案推演及技术答疑。',
    status: 'approved',
    submittedTime: '2025-05-11 16:30'
  },
  {
    id: 'lv-3',
    type: '病假 / 身体不适',
    flowNumber: 'LV-20250425-015',
    title: '《全链路追踪与可观测性实战营》全天实训',
    timeRange: '2025-04-25',
    days: '1',
    reason: '急性肠胃炎就医检查及休养，附带三甲医院就诊单据及病休建议。',
    status: 'approved',
    submittedTime: '2025-04-25 08:30'
  }
];

export const EXAM_Q12 = {
  id: 12,
  questionNumber: 12,
  type: 'single',
  typeLabel: '单选题',
  score: 5,
  title: '关于 Spring Boot 3 与 JDK 21 运行环境下 Virtual Threads (虚拟线程) 的并发模型与底层载体线程 (Carrier Thread) 调度机制描述，下列哪项是完全正确的？',
  codeSnippet: {
    filename: 'application.yml & ThreadPoolConfig.java',
    runtime: 'Java 21 LTS · Virtual Thread Runtime',
    code: `# application.yml 核心运行时声明
spring:
  threads:
    virtual:
      enabled: true

// 监控切片与底层堆栈调用观察点
Thread vThread = Thread.ofVirtual().name("enterprise-worker-", 0).start(() -> {
    // 触发网络阻塞调用点: Database I/O & Microservice RPC
    var response = remoteRestClient.executeBlockingQuery();
    logger.info("Handled on carrier: {}", Thread.currentThread());
});`
  },
  options: [
    {
      key: 'A',
      text: '虚拟线程采用 1:1 内核级线程映射，当发生 I/O 阻塞时，底层 OS 会挂起宿主 Carrier Thread 并引发大量用户态与内核态切换开销。',
      explanation: '错误：这属于传统平台线程 Platform Threads 的调度与资源消耗模型'
    },
    {
      key: 'B',
      text: '虚拟线程是由 JVM 调度的 M:N 轻量级线程，在执行阻塞式网络 I/O 时会自动从 Carrier Thread 上 unmount (卸载)，Carrier Thread 可继续处理其他虚拟任务。',
      explanation: '当前已选定项 · 符合 Project Loom 与 Spring 3 标准架构',
      isCorrect: true
    },
    {
      key: 'C',
      text: '为了最大化利用吞吐量，在 Spring Boot 3 中应当始终对虚拟线程使用有界对象池 (ThreadPoolExecutor) 来复用线程实例，避免高并发频繁分配。',
      explanation: '反模式警示：虚拟线程极为轻量，池化虚拟线程会导致吞吐量劣化'
    },
    {
      key: 'D',
      text: '当使用 synchronized 关键字块或调用 JNI 发生阻塞时，虚拟线程依然能无感知 unmount，完全不会导致 Carrier Thread Pinning (固定固化)。',
      explanation: '错误：synchronized 与 JNI 会发生 Pinning 锁定，推荐改用 ReentrantLock'
    }
  ],
  selectedKey: 'B'
};

const createExamQuestion = (id, type, title, score, optionTexts = []) => {
  const typeLabels = {
    single: '单选题',
    multiple: '多选题',
    judgment: '判断题',
    application: '应用题',
  };

  return {
    id,
    questionNumber: id,
    type,
    typeLabel: typeLabels[type],
    score,
    title,
    options: optionTexts.map((text, index) => ({
      key: String.fromCharCode(65 + index),
      text,
    })),
  };
};

const singleChoiceTitles = [
  '在分布式系统中，服务发现机制的主要作用是什么？',
  '使用数据库索引时，最需要关注的性能影响是什么？',
  '在 REST API 设计中，哪个 HTTP 方法通常用于读取资源？',
  '缓存穿透问题通常可以通过哪种方式缓解？',
  '消息队列中消费者确认机制的主要目的是什么？',
  '微服务之间进行身份认证时，哪种做法更合适？',
  '容器编排平台中，健康检查主要用于什么？',
  '在关系型数据库事务中，ACID 中的原子性表示什么？',
  '代码审查中，最适合优先关注哪类问题？',
  '日志中加入请求关联 ID 的主要价值是什么？',
  '负载均衡器的核心职责是什么？',
  '',
  '使用幂等接口设计可以避免哪类问题？',
  '持续集成流程中，自动化测试通常在哪个阶段运行？',
  '敏感配置项在生产环境中应如何管理？',
];
const singleChoiceOptions = [
  ['自动发现可用服务实例并提供访问入口', '统一存储所有业务数据', '替代服务间的网络通信', '自动生成业务代码'],
  ['索引会占用额外存储并增加写入维护成本', '索引一定会降低查询速度', '索引会改变字段的数据类型', '索引可以替代事务'],
  ['GET', 'POST', 'PATCH', 'DELETE'],
  ['使用布隆过滤器或缓存空结果', '关闭所有缓存', '增加数据库连接数', '延长请求超时时间'],
  ['确保消息处理结果被确认并支持可靠投递', '保证消息永远不重复', '自动修改消息内容', '替代消费者业务逻辑'],
  ['使用短期凭证并进行服务端校验', '在请求参数中传递明文密码', '所有服务共享永久令牌', '取消服务端身份校验'],
  ['识别实例是否可正常提供服务', '自动修改业务代码', '保证数据库永不故障', '替代容量规划'],
  ['事务中的操作要么全部成功，要么全部回滚', '事务执行结果对所有用户可见', '事务执行后数据永不变化', '多个事务必须同时执行'],
  ['正确性、安全性及可能导致故障的逻辑问题', '只关注变量命名风格', '只检查文件长度', '优先调整代码颜色'],
  ['串联一次请求经过的多个服务，便于追踪和排障', '自动加密所有数据库字段', '替代服务监控', '阻止所有重复请求'],
  ['将请求分发到多个服务实例', '保存所有业务数据', '管理用户密码', '编译应用程序'],
  [],
  ['重复请求不会造成重复副作用', '请求必须只允许一个用户访问', '请求永远不能失败', '接口只能使用 GET 方法'],
  ['代码提交后，在构建与测试阶段自动执行', '仅在系统下线后执行', '仅由用户手动执行', '生产故障发生后才执行'],
  ['存放在受控的密钥管理系统中并按需授权', '直接写入公开代码仓库', '发送到群聊中共享', '使用固定默认值且不轮换'],
];

export const EXAM_QUESTIONS = [
  ...singleChoiceTitles.slice(0, 11).map((title, index) =>
    createExamQuestion(index + 1, 'single', title, 5, singleChoiceOptions[index])),
  EXAM_Q12,
  ...singleChoiceTitles.slice(12).map((title, index) =>
    createExamQuestion(index + 13, 'single', title, 5, singleChoiceOptions[index + 12])),
  createExamQuestion(16, 'multiple', '以下哪些措施有助于提升微服务系统的韧性？（多选）', 4, [
    '对关键调用设置超时与合理的重试策略',
    '使用熔断机制隔离持续失败的依赖',
    '对所有请求无限次立即重试',
    '为服务设计健康检查与故障恢复机制',
  ]),
  createExamQuestion(17, 'multiple', '关于数据库索引与查询优化，以下哪些说法正确？（多选）', 4, [
    '应根据查询条件和执行计划评估索引',
    '索引过多可能增加写入和存储开销',
    '所有字段都创建索引一定能提升性能',
    '复合索引的字段顺序需要结合查询模式设计',
  ]),
  createExamQuestion(18, 'multiple', '生产环境中管理应用密钥时，哪些做法更安全？（多选）', 4, [
    '使用专门的密钥管理服务',
    '限制密钥读取权限并记录访问行为',
    '将密钥提交到公开代码仓库',
    '制定轮换和泄露后的撤销流程',
  ]),
  createExamQuestion(19, 'multiple', '为了提升异步消息处理的可靠性，哪些措施值得采用？（多选）', 4, [
    '处理成功后再确认消息',
    '设计可安全重试的幂等消费者',
    '忽略处理失败且不保留记录',
    '对失败消息配置告警或死信处理',
  ]),
  createExamQuestion(20, 'multiple', '以下哪些指标有助于评估在线服务运行状况？（多选）', 4, [
    '请求延迟分位数',
    '错误率与请求吞吐量',
    '仅统计代码文件数量',
    '资源使用率与依赖服务健康状态',
  ]),
  createExamQuestion(21, 'judgment', '虚拟线程在支持卸载的阻塞式 I/O 等待期间，可以释放 Carrier Thread 供其他虚拟线程使用。', 5, [
    '正确',
    '错误',
  ]),
  createExamQuestion(22, 'judgment', '在生产环境中把所有秘密配置直接写入代码仓库，可以避免配置管理风险。', 5, [
    '正确',
    '错误',
  ]),
  createExamQuestion(23, 'judgment', '为服务间调用设置超时，可以避免请求在依赖服务异常时无限期等待。', 5, [
    '正确',
    '错误',
  ]),
  createExamQuestion(24, 'application', '某订单服务在调用库存服务时偶发超时，随后出现重复扣减库存。请说明你会如何排查，并提出至少两项改进措施。', 5),
  createExamQuestion(25, 'application', '设计一个高可用的文件上传服务，请简述上传链路、失败重试、文件校验及监控告警的关键方案。', 5),
];

/* ============================================================
   管理员端 (Admin Console) 数据
   ============================================================ */

export const currentUserAdmin = {
  id: 'admin',
  name: '王智远',
  employeeId: 'admin',
  department: '企业大学 · 平台运营部',
  title: '平台超级管理员',
  role: 'admin',
  learningHours: 0,
  completedCourses: 0,
  avatarUrl: 'https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&w=120&q=80',
  grade: '平台管理端'
};

export const ADMIN_LEARNERS_DATA = [
  { id: 'u-1',  name: '李明',   employeeId: 'EMP-2024098', department: '研发部',     title: '高级研发工程师',   learningHours: 18.5, completedCourses: 3,  certificateCount: 2, status: 'active',   lastActive: '今天 09:42' },
  { id: 'u-2',  name: '张浩然', employeeId: 'EXP-70291',   department: '研发部',     title: '高级工程师',       learningHours: 42.5, completedCourses: 8,  certificateCount: 4, status: 'active',   lastActive: '今天 08:15' },
  { id: 'u-3',  name: '陈嘉怡', employeeId: 'EMP-2024156', department: '产品部',     title: '产品经理',         learningHours: 12.0, completedCourses: 2,  certificateCount: 1, status: 'active',   lastActive: '昨天 17:30' },
  { id: 'u-4',  name: '刘子墨', employeeId: 'EMP-2023877', department: '测试部',     title: '测试开发工程师',   learningHours: 26.0, completedCourses: 5,  certificateCount: 2, status: 'active',   lastActive: '今天 07:58' },
  { id: 'u-5',  name: '赵思远', employeeId: 'EMP-2023512', department: '运维部',     title: 'SRE 工程师',       learningHours: 31.5, completedCourses: 6,  certificateCount: 3, status: 'active',   lastActive: '2 天前' },
  { id: 'u-6',  name: '孙雨桐', employeeId: 'EMP-2024620', department: '市场部',     title: '品牌运营专员',     learningHours: 6.0,  completedCourses: 1,  certificateCount: 0, status: 'active',   lastActive: '今天 10:02' },
  { id: 'u-7',  name: '周凯文', employeeId: 'EMP-2023340', department: '财务部',     title: '财务分析师',       learningHours: 9.5,  completedCourses: 2,  certificateCount: 1, status: 'active',   lastActive: '3 天前' },
  { id: 'u-8',  name: '吴梦琪', employeeId: 'EMP-2024901', department: '人力资源部', title: 'HRBP',             learningHours: 15.0, completedCourses: 3,  certificateCount: 1, status: 'active',   lastActive: '昨天 11:20' },
  { id: 'u-9',  name: '郑天翔', employeeId: 'EMP-2023788', department: '研发部',     title: '前端工程师',       learningHours: 3.5,  completedCourses: 0,  certificateCount: 0, status: 'inactive', lastActive: '12 天前' },
  { id: 'u-10', name: '王若琳', employeeId: 'EMP-2024402', department: '法务部',     title: '合规专员',         learningHours: 20.0, completedCourses: 4,  certificateCount: 2, status: 'active',   lastActive: '今天 08:47' }
];

export const ADMIN_ANNOUNCEMENTS_DATA = [
  {
    id: 'ann-1',
    title: '关于发布《2025年第二季度技术序列通用学分核算与晋升认定申报通知》',
    content: '2025年Q2技术序列通用学分核算现已开启,请各部门于6月5日前完成学分录入与晋升申报材料提交。涉及职级 P5 及以上申报人员,需附上本年度技术答辩评审记录。',
    isTop: true,
    status: 'published',
    author: '王智远',
    publishTime: '2025-05-13 09:30',
    views: 1240
  },
  {
    id: 'ann-2',
    title: '2025「智训内训师」金牌讲师团第二期招募启动',
    content: '第二期金牌讲师团招募现已启动,面向全员开放报名。入选讲师将获得课程研发津贴与年度评优加分,报名截止5月31日。',
    isTop: false,
    status: 'published',
    author: '王智远',
    publishTime: '2025-05-11 14:00',
    views: 860
  },
  {
    id: 'ann-3',
    title: '平台 3.8.0 版本停机维护通知(拟发布)',
    content: '平台计划于本周五 23:00 - 次日 02:00 进行只读维护,上线 AI 智能陪练助手与知识库精准检索功能。请提前告知各部门合理安排学习时间。',
    isTop: false,
    status: 'draft',
    author: '王智远',
    publishTime: '2025-05-16 16:20',
    views: 0
  },
  {
    id: 'ann-4',
    title: '2025 全员合规红线考核即将截止的催办提醒',
    content: '《2025数据安全与合规》全员考核将于5月17日 18:00 关闭系统,当前仍有 87 名员工未完成作答,请各部门负责人督促落实。',
    isTop: false,
    status: 'published',
    author: '系统自动',
    publishTime: '2025-05-16 10:00',
    views: 1560
  },
  {
    id: 'ann-5',
    title: '2025 年第二季度「学习之星」评选结果公示',
    content: '经部门推荐与学时数据核算,本季度共评选出 12 名「学习之星」,公示期为 5 月 19 日至 5 月 23 日。如有异议请在公示期内联系企业大学运营组。',
    isTop: false,
    status: 'published',
    author: '王智远',
    publishTime: '2025-05-19 10:20',
    views: 680
  },
  {
    id: 'ann-6',
    title: '关于新增「云原生运维」系列课程上架的通知',
    content: '「云原生运维」系列 6 门课程已完成内容评审并正式上架,涵盖容器编排、可观测性、故障演练等主题,欢迎各技术团队按需选学。',
    isTop: false,
    status: 'published',
    author: '王智远',
    publishTime: '2025-05-18 15:40',
    views: 420
  },
  {
    id: 'ann-7',
    title: '端午节假期培训安排调整说明(拟发布)',
    content: '端午节假期期间线上课程正常开放,直播课与集中考试顺延至节后首个工作日,具体调整安排以各部门通知为准。',
    isTop: false,
    status: 'draft',
    author: '王智远',
    publishTime: '2025-05-20 09:10',
    views: 0
  },
  {
    id: 'ann-8',
    title: '培训考勤规则更新:迟到与缺勤判定口径说明',
    content: '自 6 月 1 日起,线上签到以课程开始后 10 分钟为界线,超过 10 分钟记为迟到;未签到且未提交请假单的记为缺勤,请各位同事留意。',
    isTop: false,
    status: 'published',
    author: '系统自动',
    publishTime: '2025-05-17 11:05',
    views: 890
  }
];

export const ADMIN_ACTIVITIES_DATA = [
  { id: 'act-1', type: 'course',  text: '上架新课程《Serverless 函数计算实战》',        operator: '王智远', time: '今天 10:20' },
  { id: 'act-2', type: 'user',    text: '新增学员账号:孙雨桐 (EMP-2024620)',           operator: '王智远', time: '今天 09:58' },
  { id: 'act-3', type: 'exam',    text: '发布考试「2025Q3 前端工程化认证」',             operator: '王智远', time: '昨天 18:05' },
  { id: 'act-4', type: 'notice',  text: '置顶公告「2025Q2 学分核算与晋升申报通知」',     operator: '王智远', time: '昨天 15:40' },
  { id: 'act-5', type: 'approve', text: '审批通过请假申请 LV-20250512-009(张浩然)',     operator: '王智远', time: '05-12 16:55' },
  { id: 'act-6', type: 'user',    text: '禁用学员账号:郑天翔 (EMP-2023788,长期未学习)',  operator: '系统自动', time: '05-10 09:00' }
];

export const ADMIN_DEPT_STATS = [
  { department: '研发部',     hours: 386, learners: 128 },
  { department: '产品部',     hours: 214, learners: 64  },
  { department: '运维部',     hours: 198, learners: 42  },
  { department: '市场部',     hours: 96,  learners: 38  },
  { department: '财务部',     hours: 72,  learners: 30  },
  { department: '人力资源部', hours: 88,  learners: 26  }
];

export const ADMIN_DEPARTMENTS = [
  { id: 'd-1', name: '研发部',     manager: '陈建邦', learners: 128, hours: 386, desc: '负责核心业务系统研发与架构治理', status: 'active' },
  { id: 'd-2', name: '产品部',     manager: '陈嘉怡', learners: 64,  hours: 214, desc: '产品规划、需求分析与用户体验设计', status: 'active' },
  { id: 'd-3', name: '测试部',     manager: '刘子墨', learners: 52,  hours: 168, desc: '质量保障、自动化测试与发布验收', status: 'active' },
  { id: 'd-4', name: '运维部',     manager: '赵思远', learners: 42,  hours: 198, desc: '基础设施、SRE 与生产稳定性保障', status: 'active' },
  { id: 'd-5', name: '市场部',     manager: '孙雨桐', learners: 38,  hours: 96,  desc: '品牌运营、市场推广与增长', status: 'active' },
  { id: 'd-6', name: '财务部',     manager: '周凯文', learners: 30,  hours: 72,  desc: '财务核算、预算与经营分析', status: 'active' },
  { id: 'd-7', name: '人力资源部', manager: '吴梦琪', learners: 26,  hours: 88,  desc: '组织发展、人才招聘与员工关系', status: 'active' },
  { id: 'd-8', name: '法务部',     manager: '王若琳', learners: 12,  hours: 54,  desc: '合规审查、合同与知识产权管理', status: 'inactive' }
];

export const ADMIN_LEAVE_REQUESTS = [
  {
    id: 'alv-1',
    applicant: '张浩然',
    employeeId: 'EXP-70291',
    department: '研发部',
    type: '项目紧急发版',
    timeRange: '2025-05-18',
    days: '0.5',
    reason: '因核心结算集群在16:30进行双机房切流验证,需现场跟进发布(工单: PROD-OPS-20250518-09)。申请缺席下午后半程实操。',
    status: 'pending',
    submittedTime: '2025-05-18 10:15'
  },
  {
    id: 'alv-2',
    applicant: '刘子墨',
    employeeId: 'EMP-2023877',
    department: '测试部',
    type: '病假 / 身体不适',
    timeRange: '2025-05-19',
    days: '1',
    reason: '急性肠胃炎就医检查及休养,附三甲医院就诊单据及病休建议。',
    status: 'pending',
    submittedTime: '2025-05-18 21:02'
  },
  {
    id: 'alv-3',
    applicant: '陈嘉怡',
    employeeId: 'EMP-2024156',
    department: '产品部',
    type: '因公外出 / 外部交流',
    timeRange: '2025-05-20',
    days: '0.5',
    reason: '赴客户现场参加产品一期验收评审,现场进行功能演示及需求答疑。',
    status: 'pending',
    submittedTime: '2025-05-18 17:40'
  },
  {
    id: 'alv-4',
    applicant: '赵思远',
    employeeId: 'EMP-2023512',
    department: '运维部',
    type: '个人事务 / 家庭安排',
    timeRange: '2025-05-16',
    days: '1',
    reason: '因个人家庭事务申请缺席全天培训。',
    status: 'rejected',
    submittedTime: '2025-05-15 09:20'
  },
  {
    id: 'alv-5',
    applicant: '李明',
    employeeId: 'EMP-2024098',
    department: '研发部',
    type: '因公外出 / 外部交流',
    timeRange: '2025-05-12',
    days: '0.5',
    reason: '赴招商银行总部参加开放平台二期架构方案评审,现场进行方案推演及技术答疑。',
    status: 'approved',
    submittedTime: '2025-05-11 16:30'
  },
  {
    id: 'alv-6',
    applicant: '吴梦琪',
    employeeId: 'EMP-2024901',
    department: '人力资源部',
    type: '病假 / 身体不适',
    timeRange: '2025-04-25',
    days: '1',
    reason: '急性肠胃炎就医检查及休养,附带三甲医院就诊单据及病休建议。',
    status: 'approved',
    submittedTime: '2025-04-24 14:10'
  },
  {
    id: 'alv-7',
    applicant: '孙雨桐',
    employeeId: 'EMP-2024620',
    department: '市场部',
    type: '因公外出 / 外部交流',
    timeRange: '2025-05-21',
    days: '0.5',
    reason: '赴品牌合作方参加年度联合推广方案沟通会,需现场确认物料排期与投放节奏。',
    status: 'pending',
    submittedTime: '2025-05-19 09:35'
  },
  {
    id: 'alv-8',
    applicant: '周凯文',
    employeeId: 'EMP-2023340',
    department: '财务部',
    type: '个人事务 / 家庭安排',
    timeRange: '2025-05-22',
    days: '1',
    reason: '家中老人需要陪同到院复查,当天无法参加集中培训,相关课程将在课后通过回放补齐并提交学习笔记。',
    status: 'pending',
    submittedTime: '2025-05-20 08:50'
  },
  {
    id: 'alv-9',
    applicant: '郑天翔',
    employeeId: 'EMP-2023788',
    department: '研发部',
    type: '项目紧急发版',
    timeRange: '2025-05-22',
    days: '0.5',
    reason: '核心交易链路灰度发布需要全程值守,申请缺席下午的架构实操课程,发布完成后补看录播。',
    status: 'pending',
    submittedTime: '2025-05-20 13:12'
  },
  {
    id: 'alv-10',
    applicant: '许清和',
    employeeId: 'EMP-2023115',
    department: '产品部',
    type: '因公外出 / 外部交流',
    timeRange: '2025-05-23',
    days: '1',
    reason: '赴行业峰会参加产品创新论坛并做主题分享,行程与当天培训时间冲突,申请事后补学。',
    status: 'pending',
    submittedTime: '2025-05-21 10:05'
  },
  {
    id: 'alv-11',
    applicant: '韩立言',
    employeeId: 'EMP-2022890',
    department: '法务部',
    type: '病假 / 身体不适',
    timeRange: '2025-05-15',
    days: '1',
    reason: '感冒发热需居家休息,已提交社区医院就诊记录。',
    status: 'approved',
    submittedTime: '2025-05-14 19:20'
  },
  {
    id: 'alv-12',
    applicant: '林欣怡',
    employeeId: 'EMP-2022551',
    department: '人力资源部',
    type: '个人事务 / 家庭安排',
    timeRange: '2025-05-09',
    days: '0.5',
    reason: '临时需要处理子女入学材料提交事宜,申请缺席半天课程。',
    status: 'rejected',
    submittedTime: '2025-05-08 16:45'
  }
];

export const ADMIN_CERTIFICATE_ISSUES = [
  {
    id: 'aci-1',
    holder: '李明',
    employeeId: 'EMP-2024098',
    title: '微服务高可用架构实战专家认证',
    serialNumber: 'ZX-2024-MS-089',
    level: 'LEVEL 4 · 专家级认证',
    issueOrg: '企业技术委员会',
    issueDate: '2024-03-15',
    validity: '长期有效',
    status: 'issued'
  },
  {
    id: 'aci-2',
    holder: '张浩然',
    employeeId: 'EXP-70291',
    title: 'Kubernetes 云原生容器编排认证',
    serialNumber: 'ZX-2023-K8S-901',
    level: '运维基建 · 认证',
    issueOrg: '企业技术委员会',
    issueDate: '2023-11-20',
    validity: '长期有效',
    status: 'issued'
  },
  {
    id: 'aci-3',
    holder: '刘子墨',
    employeeId: 'EMP-2023877',
    title: '企业数据安全与隐私合规 (GDPR/PIPL) 专家证书',
    serialNumber: 'ZX-2025-SEC-128',
    level: '法定合规 · 重点资格',
    issueOrg: '风控与安全合规部',
    issueDate: '2025-04-02',
    validity: '24 个月 (2027年到期)',
    status: 'issued'
  },
  {
    id: 'aci-4',
    holder: '孙雨桐',
    employeeId: 'EMP-2024620',
    title: '企业生成式 AI 研发提效工程专家',
    serialNumber: 'ZX-2025-AI-052',
    level: 'AI 效能 · 先锋认证',
    issueOrg: 'AI 创新实验室',
    issueDate: '2025-05-06',
    validity: '长期有效',
    status: 'issued'
  },
  {
    id: 'aci-5',
    holder: '陈嘉怡',
    employeeId: 'EMP-2024156',
    title: '高效跨部门沟通与敏捷项目管理实践',
    serialNumber: 'ZX-2025-PM-077',
    level: '管理通识 · 结课认证',
    issueOrg: '企业大学教培中心',
    issueDate: '—',
    validity: '长期有效',
    status: 'pending'
  },
  {
    id: 'aci-6',
    holder: '王若琳',
    employeeId: 'EMP-2024402',
    title: '金融级数据合规与业务出海跨境传输实战',
    serialNumber: 'ZX-2025-LAW-031',
    level: '法务合规 · 专项认证',
    issueOrg: '风控与安全合规部',
    issueDate: '—',
    validity: '长期有效',
    status: 'pending'
  }
];

export const ADMIN_CHECKIN_RECORDS = [
  { id: 'ck-1',  name: '李明',   employeeId: 'EMP-2024098', department: '研发部',     checkInTime: '08:52', checkOutTime: '17:36', method: '人脸识别', status: 'normal' },
  { id: 'ck-2',  name: '张浩然', employeeId: 'EXP-70291',   department: '研发部',     checkInTime: '09:14', checkOutTime: '18:02', method: '工牌刷卡', status: 'late' },
  { id: 'ck-3',  name: '陈嘉怡', employeeId: 'EMP-2024156', department: '产品部',     checkInTime: '08:47', checkOutTime: '17:28', method: '人脸识别', status: 'normal' },
  { id: 'ck-4',  name: '刘子墨', employeeId: 'EMP-2023877', department: '测试部',     checkInTime: '08:58', checkOutTime: '17:41', method: '人脸识别', status: 'normal' },
  { id: 'ck-5',  name: '赵思远', employeeId: 'EMP-2023512', department: '运维部',     checkInTime: '09:02', checkOutTime: '18:10', method: '工牌刷卡', status: 'normal' },
  { id: 'ck-6',  name: '孙雨桐', employeeId: 'EMP-2024620', department: '市场部',     checkInTime: '08:39', checkOutTime: '17:15', method: '人脸识别', status: 'normal' },
  { id: 'ck-7',  name: '周凯文', employeeId: 'EMP-2023340', department: '财务部',     checkInTime: '09:21', checkOutTime: '17:52', method: '工牌刷卡', status: 'late' },
  { id: 'ck-8',  name: '吴梦琪', employeeId: 'EMP-2024901', department: '人力资源部', checkInTime: '08:45', checkOutTime: '17:30', method: '人脸识别', status: 'normal' },
  { id: 'ck-9',  name: '王若琳', employeeId: 'EMP-2024402', department: '法务部',     checkInTime: '08:50', checkOutTime: '17:26', method: '人脸识别', status: 'normal' },
  { id: 'ck-10', name: '郑天翔', employeeId: 'EMP-2023788', department: '研发部',     checkInTime: '—',    checkOutTime: '—',    method: '—',        status: 'missing' },
  { id: 'ck-11', name: '陈建邦', employeeId: 'EMP-2023056', department: '研发部',     checkInTime: '09:06', checkOutTime: '18:20', method: '工牌刷卡', status: 'normal' },
  { id: 'ck-12', name: '林欣怡', employeeId: 'EMP-2023311', department: '培训部',     checkInTime: '08:31', checkOutTime: '17:08', method: '人脸识别', status: 'normal' }
];

export const ADMIN_QUESTION_BANK = [  {
    id: 'q-1', examId: 'exam-1', type: 'single', typeLabel: '单选题', score: 5,
    title: '在分布式系统中,服务发现机制的主要作用是什么?',
    options: [
      { key: 'A', text: '自动发现可用服务实例并提供访问入口' },
      { key: 'B', text: '统一存储所有业务数据' },
      { key: 'C', text: '替代服务间的网络通信' },
      { key: 'D', text: '自动生成业务代码' }
    ],
    answer: 'A'
  },
  {
    id: 'q-2', examId: 'exam-1', type: 'single', score: 5,
    title: '关于 Spring Boot 3 与 JDK 21 环境下的虚拟线程 (Virtual Threads) 调度机制,下列哪项描述正确?',
    options: [
      { key: 'A', text: '虚拟线程采用 1:1 内核级线程映射' },
      { key: 'B', text: '虚拟线程在阻塞 I/O 时自动从 Carrier Thread 卸载,宿主线程继续处理其他任务' },
      { key: 'C', text: '应始终对虚拟线程使用有界线程池复用实例' },
      { key: 'D', text: 'synchronized 块阻塞时虚拟线程也能无感知卸载' }
    ],
    answer: 'B'
  },
  {
    id: 'q-3', examId: 'exam-1', type: 'multiple', score: 8,
    title: '以下哪些措施有助于提升微服务系统的韧性?(多选)',
    options: [
      { key: 'A', text: '对关键调用设置超时与合理的重试策略' },
      { key: 'B', text: '使用熔断机制隔离持续失败的依赖' },
      { key: 'C', text: '对所有请求无限次立即重试' },
      { key: 'D', text: '为服务设计健康检查与故障恢复机制' }
    ],
    answer: 'ABD'
  },
  {
    id: 'q-4', examId: 'exam-1', type: 'judgment', score: 5,
    title: '虚拟线程在支持卸载的阻塞式 I/O 等待期间,可以释放 Carrier Thread 供其他虚拟线程使用。',
    options: [
      { key: 'A', text: '正确' },
      { key: 'B', text: '错误' }
    ],
    answer: 'A'
  },
  {
    id: 'q-5', examId: 'exam-1', type: 'application', score: 12,
    title: '某订单服务在调用库存服务时偶发超时,随后出现重复扣减库存。请说明你会如何排查,并提出至少两项改进措施。',
    options: [],
    answer: ''
  },
  {
    id: 'q-6', examId: 'exam-2', type: 'single', score: 5,
    title: '使用数据库索引时,最需要关注的性能影响是什么?',
    options: [
      { key: 'A', text: '索引会占用额外存储并增加写入维护成本' },
      { key: 'B', text: '索引一定会降低查询速度' },
      { key: 'C', text: '索引会改变字段的数据类型' },
      { key: 'D', text: '索引可以替代事务' }
    ],
    answer: 'A'
  },
  {
    id: 'q-7', examId: 'exam-2', type: 'single', score: 5,
    title: '缓存穿透问题通常可以通过哪种方式缓解?',
    options: [
      { key: 'A', text: '使用布隆过滤器或缓存空结果' },
      { key: 'B', text: '关闭所有缓存' },
      { key: 'C', text: '增加数据库连接数' },
      { key: 'D', text: '延长请求超时时间' }
    ],
    answer: 'A'
  },
  {
    id: 'q-8', examId: 'exam-2', type: 'judgment', score: 5,
    title: '在生产环境中把所有秘密配置直接写入代码仓库,可以避免配置管理风险。',
    options: [
      { key: 'A', text: '正确' },
      { key: 'B', text: '错误' }
    ],
    answer: 'B'
  },
  {
    id: 'q-9', examId: 'exam-2', type: 'application', score: 10,
    title: '设计一个高可用的文件上传服务,请简述上传链路、失败重试、文件校验及监控告警的关键方案。',
    options: [],
    answer: ''
  },
  {
    id: 'q-10', examId: 'exam-3', type: 'single', score: 5,
    title: '在 REST API 设计中,哪个 HTTP 方法通常用于读取资源?',
    options: [
      { key: 'A', text: 'GET' },
      { key: 'B', text: 'POST' },
      { key: 'C', text: 'PATCH' },
      { key: 'D', text: 'DELETE' }
    ],
    answer: 'A'
  },
  {
    id: 'q-11', examId: 'exam-3', type: 'multiple', score: 8,
    title: '生产环境中管理应用密钥时,哪些做法更安全?(多选)',
    options: [
      { key: 'A', text: '使用专门的密钥管理服务' },
      { key: 'B', text: '限制密钥读取权限并记录访问行为' },
      { key: 'C', text: '将密钥提交到公开代码仓库' },
      { key: 'D', text: '制定轮换和泄露后的撤销流程' }
    ],
    answer: 'ABD'
  },
  {
    id: 'q-12', examId: 'exam-3', type: 'judgment', score: 5,
    title: '为服务间调用设置超时,可以避免请求在依赖服务异常时无限期等待。',
    options: [
      { key: 'A', text: '正确' },
      { key: 'B', text: '错误' }
    ],
    answer: 'A'
  }
];
