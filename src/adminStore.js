import { reactive } from 'vue';
import {
  COURSES_DATA,
  EXAMS_DATA,
  PAST_EXAMS,
  ADMIN_QUESTION_BANK
} from './mockData.js';

/**
 * 管理端共享响应式状态
 * 课程导入页 → 课程管理页、题库页 → 考试页 之间通过该模块同步数据
 * (演示 mock:内存态,刷新页面后还原为种子数据)
 */

const seedExams = [
  ...EXAMS_DATA.map(e => ({
    ...e,
    state: e.status === 'in_progress' ? 'running' : e.status === 'not_started' ? 'scheduled' : 'open',
    participants: e.status === 'in_progress' ? 486 : e.status === 'not_started' ? 128 : 672
  })),
  ...PAST_EXAMS.map(p => ({
    ...p,
    state: 'ended',
    durationMinutes: 60,
    timeWindow: p.completionDate,
    participants: p.type === '全员合规必修' ? 2450 : 486
  }))
];

/** 预设封面库:课程管理中可直接选用 */
export const PRESET_COVERS = [
  'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=600&q=80',
  'https://images.unsplash.com/photo-1550751827-4bd374c3f58b?auto=format&fit=crop&w=600&q=80',
  'https://images.unsplash.com/photo-1522071820081-009f0129c71c?auto=format&fit=crop&w=600&q=80',
  'https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=600&q=80',
  'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=600&q=80',
  'https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=600&q=80'
];

/** 示例课程视频(公网演示片源,供播放测试) */
export const SAMPLE_VIDEOS = {
  bunny: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
  sintel: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4',
  elephant: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4'
};

export const adminStore = reactive({
  courses: COURSES_DATA.map(c => ({
    ...c,
    published: c.status !== 'offline',
    syllabus: c.id === 'c1'
      ? [
          {
            id: 'ch-1',
            title: '第 1 章: 微服务架构选型与升级',
            lessons: [
              { id: 'ls-1', name: '1.1 微服务架构选型与升级路线', duration: '22分', url: SAMPLE_VIDEOS.bunny },
              { id: 'ls-2', name: '1.2 Spring Boot 3 核心与自动装配', duration: '28分', url: SAMPLE_VIDEOS.sintel },
              { id: 'ls-3', name: '1.3 高并发场景下的架构权衡', duration: '19分', url: SAMPLE_VIDEOS.elephant }
            ]
          },
          {
            id: 'ch-2',
            title: '第 2 章: 高并发削峰与消息事务实战',
            lessons: [
              { id: 'ls-4', name: '2.1 削峰填谷与消息队列选型', duration: '25分', url: SAMPLE_VIDEOS.bunny },
              { id: 'ls-5', name: '2.2 消息事务与最终一致性方案', duration: '31分', url: SAMPLE_VIDEOS.sintel }
            ]
          },
          {
            id: 'ch-3',
            title: '第 3 章: 分布式缓存与数据一致性',
            lessons: [
              { id: 'ls-6', name: '3.1 Redis 哨兵与 Cluster 集群架构选型', duration: '22分', url: SAMPLE_VIDEOS.bunny },
              { id: 'ls-7', name: '3.2 Redis 集群分布式锁与缓存击穿防护', duration: '28分', url: SAMPLE_VIDEOS.sintel },
              { id: 'ls-8', name: '3.3 缓存穿透布隆过滤器实战', duration: '20分', url: SAMPLE_VIDEOS.elephant }
            ]
          }
        ]
      : c.id === 'c3'
        ? [
            {
              id: 'ch-10',
              title: '第 1 章: 敏捷协作与 Scrum 基础',
              lessons: [
                { id: 'ls-10', name: '1.1 敏捷协作与 Scrum 基础', duration: '18分', url: SAMPLE_VIDEOS.elephant }
              ]
            }
          ]
        : []
  })),
  exams: seedExams,
  questions: ADMIN_QUESTION_BANK.map(q => ({
    ...q,
    options: (q.options || []).map(o => ({ ...o }))
  }))
});
/** 统计某场考试的题目数 */
export const questionCount = (examId) =>
  adminStore.questions.filter(q => q.examId === examId).length;
