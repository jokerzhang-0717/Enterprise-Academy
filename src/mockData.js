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
