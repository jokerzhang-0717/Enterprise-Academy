<template>
  <div class="home-page">

    <!-- ============ 1. 顶部欢迎横幅 ============ -->
    <section class="welcome-banner">
      <div class="welcome-content">
        <div class="welcome-tag">
          <span class="welcome-tag-dot"></span>
          <span>企业数字化研学季进行中 · Q2加速计划</span>
        </div>
        <h1 class="welcome-title">欢迎来到云舒艺境，在这里，遇见更好的自己！</h1>
        <p class="welcome-desc">
          今天是 2025年5月14日 星期三。永远相信美好的事情即将发生！
        </p>
      </div>
    </section>

    <!-- ============ 2. 快捷入口（5 个卡片） ============ -->
    <section class="quick-actions">

      <button class="quick-card" @click="$emit('navigate', 'course-player')">
        <div class="quick-card-icon icon-blue">
          <Play class="quick-icon" />
        </div>
        <div class="quick-card-text">
          <div class="quick-card-title">继续上次学习</div>
          <div class="quick-card-sub">微服务架构 · 66%</div>
        </div>
      </button>

      <button class="quick-card" @click="$emit('navigate', 'exams')">
        <div class="quick-card-icon icon-blue">
          <FileCheck2 class="quick-icon" />
        </div>
        <div class="quick-card-text">
          <div class="quick-card-title">在线考核测评</div>
          <div class="quick-card-sub">2 场进行中考核</div>
        </div>
      </button>

      <button class="quick-card" @click="$emit('navigate', 'attendance')">
        <div class="quick-card-icon icon-green">
          <CalendarClock class="quick-icon" />
        </div>
        <div class="quick-card-text">
          <div class="quick-card-title">考勤与培训假</div>
          <div class="quick-card-sub">现场签到及申请</div>
        </div>
      </button>

      <button class="quick-card" @click="$emit('navigate', 'certificates')">
        <div class="quick-card-icon icon-purple">
          <Award class="quick-icon" />
        </div>
        <div class="quick-card-text">
          <div class="quick-card-title">荣誉资质证书</div>
          <div class="quick-card-sub">已获 4 项专业认证</div>
        </div>
      </button>

      <button class="quick-card quick-card-last" @click="$emit('navigate', 'messages')">
        <div class="quick-card-icon icon-red">
          <Bell class="quick-icon" />
        </div>
        <div class="quick-card-text">
          <div class="quick-card-title">通知与待办</div>
          <div class="quick-card-sub">3 条教务新动态</div>
        </div>
      </button>

    </section>

    <!-- ============ 3. 主体分栏 ============ -->
    <div class="main-grid">

      <!-- ======== 左栏（8 列） ======== -->
      <div class="main-left">

        <!-- 正在学习的课程 -->
        <section class="section-block">
          <div class="section-header">
            <div class="section-header-left">
              <span class="section-bar"></span>
              <h2 class="section-title">正在学习的课程</h2>
            </div>
            <button class="section-link" @click="$emit('navigate', 'courses')">全部学习档案 &gt;</button>
          </div>

          <div class="course-grid">

            <!-- 课程卡片 1 -->
            <article
              class="course-card"
              role="button" tabindex="0"
              aria-label="打开云原生微服务体系架构设计与高并发实战课程详情"
              @click="$emit('navigate', 'course-player')"
              @keydown.enter="openCourseFromKeyboard"
              @keydown.space.prevent="openCourseFromKeyboard"
            >
              <div class="course-cover">
                <img
                  src="https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=600&q=80"
                  alt="课程封面"
                  class="course-cover-img"
                />
                <div class="course-cover-shade"></div>
                <div class="course-badge badge-dark">岗位核心必修</div>
              </div>
              <div class="course-body">
                <div class="course-teacher">
                  <span class="course-avatar avatar-blue">陈</span>
                  <div class="course-teacher-info">
                    <p class="course-teacher-label">授课讲师</p>
                    <p class="course-teacher-name">陈立维 · 首席架构师</p>
                  </div>
                </div>
                <h3 class="course-name">云原生微服务体系架构设计与高并发实战</h3>
              </div>
            </article>

            <!-- 课程卡片 2 -->
            <article
              class="course-card"
              role="button" tabindex="0"
              aria-label="打开大语言模型与企业级智能体研发实战课程详情"
              @click="$emit('navigate', 'course-player')"
              @keydown.enter="openCourseFromKeyboard"
              @keydown.space.prevent="openCourseFromKeyboard"
            >
              <div class="course-cover">
                <img
                  src="https://images.unsplash.com/photo-1531482615713-2afd69097998?auto=format&fit=crop&w=600&q=80"
                  alt="课程封面"
                  class="course-cover-img"
                />
                <div class="course-cover-shade"></div>
                <div class="course-badge badge-purple">前沿专项</div>
              </div>
              <div class="course-body">
                <div class="course-teacher">
                  <span class="course-avatar avatar-purple">周</span>
                  <div class="course-teacher-info">
                    <p class="course-teacher-label">授课讲师</p>
                    <p class="course-teacher-name">周慧博士 · AI实验室</p>
                  </div>
                </div>
                <h3 class="course-name">大语言模型与企业级智能体 (Agent) 研发实战</h3>
              </div>
            </article>

          </div>
        </section>

        <!-- 企业精选研修通道 -->
        <section class="section-block">
          <div class="section-header section-header-wrap">
            <div class="section-header-left">
              <span class="section-bar"></span>
              <h2 class="section-title">企业精选研修通道</h2>
            </div>
            <div class="channel-filter">
              <button
                v-for="channel in channelOptions"
                :key="channel.id"
                class="channel-btn"
                :class="selectedChannel === channel.id ? 'channel-btn-active' : ''"
                @click="setChannel(channel.id)"
              >
                {{ channel.label }}
              </button>
            </div>
          </div>

          <div class="track-grid">
            <article
              v-for="track in visibleTracks"
              :key="track.id"
              class="track-card"
              role="button" tabindex="0"
              @click="$emit('open-track', track)"
              @keydown.enter="openTrackFromKeyboard($event, track)"
              @keydown.space.prevent="openTrackFromKeyboard($event, track)"
            >
              <div class="track-cover">
                <img :src="track.thumbnail" :alt="track.title" class="track-cover-img" />
                <span class="track-tag">{{ track.categoryLabel }}</span>
              </div>
              <h3 class="track-name">{{ track.title }}</h3>
              <p class="track-summary">{{ track.summary }}</p>
            </article>
          </div>
        </section>

      </div>

      <!-- ======== 右栏（4 列） ======== -->
      <div class="main-right">

        <!-- 待办日程 -->
        <section class="side-panel">
          <div class="side-panel-header">
            <div class="side-panel-title-row">
              <CalendarClock class="side-panel-icon" />
              <h2 class="side-panel-title">待办日程</h2>
            </div>
            <span class="side-panel-count">3 项目程</span>
          </div>

          <div class="schedule-list">

            <div class="schedule-item">
              <span class="schedule-dot dot-red"></span>
              <div class="schedule-info">
                <div class="schedule-meta">
                  <span class="schedule-time">5月17日 18:00 (本周六)</span>
                  <span class="schedule-tag tag-red">考核截止</span>
                </div>
                <div class="schedule-text">《2025数据安全与合规》全员考核系统关闭</div>
              </div>
            </div>

            <div class="schedule-item">
              <span class="schedule-dot dot-blue"></span>
              <div class="schedule-info">
                <div class="schedule-meta">
                  <span class="schedule-time">5月19日 14:30 (下周一)</span>
                  <span class="schedule-tag tag-blue">线上直播研讨</span>
                </div>
                <div class="schedule-text">云原生全链路可观测性落地工作坊</div>
              </div>
            </div>

            <div class="schedule-item">
              <span class="schedule-dot dot-purple"></span>
              <div class="schedule-info">
                <div class="schedule-meta">
                  <span class="schedule-time">5月21日 10:00 (下周三)</span>
                  <span class="schedule-tag tag-purple">证书复核</span>
                </div>
                <div class="schedule-text">PMP & 敏捷管理内训资质证书年度换发</div>
              </div>
            </div>

          </div>
        </section>

        <!-- 企业培训公告 -->
        <section class="side-panel">
          <div class="side-panel-header">
            <div class="side-panel-title-row">
              <Bell class="side-panel-icon" />
              <h2 class="side-panel-title">企业培训公告</h2>
            </div>
            <button class="section-link" @click="$emit('navigate', 'messages')">查看更多</button>
          </div>

          <div class="announcement-list">

            <div class="announcement-item" @click="$emit('navigate', 'messages')">
              <div class="announcement-meta">
                <span class="announcement-tag tag-blue-bg">全员通告</span>
                <span class="announcement-date">2025-05-13</span>
              </div>
              <div class="announcement-title">
                关于发布《2025年第二季度技术序列通用学分核算与晋升认定申报通知》...
              </div>
            </div>

            <div class="announcement-item announcement-item-last" @click="$emit('navigate', 'messages')">
              <div class="announcement-meta">
                <span class="announcement-tag tag-amber-bg">讲师招募</span>
                <span class="announcement-date">2025-05-11</span>
              </div>
              <div class="announcement-title">
                2025「智训内训师」金牌讲师团第二期招募启动，入选者将获得专属课程研发津贴...
              </div>
            </div>

          </div>
        </section>

      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue';
import { RECOMMENDED_TRACKS } from '../mockData.js';
import {
  Play,
  FileCheck2,
  CalendarClock,
  Award,
  Bell,
} from 'lucide-vue-next';

const props = defineProps({
  searchTargetId: {
    type: String,
    default: '',
  },
});

defineEmits(['navigate', 'open-track']);

/* ---------- 频道筛选 ---------- */
const channelOptions = [
  { id: 'all', label: '全部' },
  { id: 'tech', label: '技术工程' },
  { id: 'security', label: '信息安全' },
  { id: 'leadership', label: '领导力与协同' }
];

const selectedChannel = ref('all');

/* 统一入口，同值短路，避免无意义重复触发 */
const setChannel = (id) => {
  if (selectedChannel.value === id) return;
  selectedChannel.value = id;
};

const visibleTracks = computed(() => {
  if (selectedChannel.value === 'all') return RECOMMENDED_TRACKS;
  return RECOMMENDED_TRACKS.filter(track => track.category === selectedChannel.value);
});

/* ---------- 键盘无障碍操作 ---------- */
const openCourseFromKeyboard = (event) => {
  if (event.target === event.currentTarget) {
    event.currentTarget.click();
  }
};

const openTrackFromKeyboard = (event, track) => {
  if (event.target === event.currentTarget) {
    event.currentTarget.click();
  }
};

/* ---------- 搜索定位滚动（修复死循环 / 爆栈风险） ---------- */
let lastScrolledId = '';

const scrollToTarget = (targetId) => {
  if (!targetId) return;
  // 同一目标不重复滚动，打断 watch <-> 状态 闭环
  if (lastScrolledId === targetId) return;
  lastScrolledId = targetId;

  nextTick(() => {
    requestAnimationFrame(() => {
      const el = document.getElementById(targetId);
      if (el && typeof el.scrollIntoView === 'function') {
        el.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
    });
  });
};

watch(
  () => props.searchTargetId,
  targetId => {
    if (!targetId) return;
    scrollToTarget(targetId);
  },
  { immediate: true },
);
</script>

<style scoped>
/* ============================================================
   智训云学 · 首页仪表盘样式
   ------------------------------------------------------------
   目录
     01  页面基础布局          08  研修通道卡片
     02  顶部欢迎横幅          09  右栏通用面板
     03  快捷入口卡片          10  待办日程
     04  主体分栏网格          11  企业公告
     05  分栏标题              12  标签配色变体
     06  课程卡片              13  入场动画
     07  课程封面与角标        14  无障碍
   ------------------------------------------------------------
   色板速查
     品牌蓝   #005576 · #83C9F4 · #D9F0FF
     品牌紫   #6555B0 · #9381FF
     标题深色 #1E293B · #334155 · #475569
     正文灰   #64748B · #94A3B8
     成功绿   #16A34A · #F0FDF4
     警告红   #DC2626 · #E11D48 · #FEE2E2 · #FFF1F2
     警示琥珀 #D97706 · #FEF3C7
     边框浅灰 #E2E8F0 · #F1F5F9 · #F8FAFC · #FFFFFF
   ============================================================ */

/* ==================== 01 页面基础布局 ==================== */
.home-page {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;              /* 24px */
  padding-bottom: 3rem;     /* 48px */
}

/* ==================== 02 顶部欢迎横幅 ==================== */
.welcome-banner {
  border-radius: 1rem;      /* 16px */
  border: 1px solid #D9F0FF;
  background: linear-gradient(135deg, #D9F0FF 0%, #F4F9FD 50%, #FFFFFF 100%);
  padding: 2rem 1.25rem;    /* 32px 20px */
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}
@media (min-width: 768px) {
  .welcome-banner { padding: 2.5rem 2.5rem; }
}
.welcome-content {
  max-width: 48rem;         /* 768px */
  margin-left: auto;
  margin-right: auto;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;             /* 12px */
  text-align: center;
}

/* 标题上方小标签 */
.welcome-tag {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;            /* 6px */
  padding: 0.25rem 0.75rem;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(131, 201, 244, 0.4);
  color: #005576;
  font-size: 0.75rem;       /* 12px */
  font-weight: 600;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  align-self: center;
  animation: welcome-text-in 360ms ease-out both;
}
.welcome-tag-dot {
  width: 0.5rem;            /* 8px */
  height: 0.5rem;
  border-radius: 50%;
  background: #83C9F4;
  flex-shrink: 0;
}
.welcome-title {
  font-size: 1.5rem;        /* 24px */
  font-weight: 800;
  color: #1E293B;
  letter-spacing: -0.025em;
  line-height: 1.25;
  animation: welcome-text-in 380ms cubic-bezier(0.2, 0.7, 0.2, 1) both;
}
@media (min-width: 640px) {
  .welcome-title { font-size: 1.875rem; }
}
@media (min-width: 768px) {
  .welcome-title { font-size: 2.25rem; }
}
.welcome-desc {
  max-width: 42rem;
  margin-left: auto;
  margin-right: auto;
  font-size: 0.75rem;
  color: #64748B;
  line-height: 1.625;
  animation: welcome-text-in 380ms ease-out both;
}
@media (min-width: 640px) {
  .welcome-desc { font-size: 0.875rem; }
}

/* ==================== 03 快捷入口卡片 ==================== */
.quick-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;             /* 12px */
}
@media (min-width: 640px) {
  .quick-actions { grid-template-columns: repeat(5, 1fr); }
}
.quick-card {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.875rem;        /* 14px */
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 0.75rem;   /* 12px */
  cursor: pointer;
  text-align: left;
  transition: transform 0.2s, border-color 0.2s, box-shadow 0.2s;
  animation: card-reveal 360ms ease-out both;
}
.quick-card:hover {
  transform: translateY(-2px);
  border-color: #83C9F4;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}
.quick-card-last {
  grid-column: span 2;
}
@media (min-width: 640px) {
  .quick-card-last { grid-column: span 1; }
}

/* 图标容器 + 变体 */
.quick-card-icon {
  width: 2.5rem;            /* 40px */
  height: 2.5rem;
  border-radius: 0.75rem;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: transform 0.2s;
}
.quick-card:hover .quick-card-icon {
  transform: scale(1.05);
}
.quick-icon {
  width: 1.25rem;
  height: 1.25rem;
}
.icon-blue   { background: #D9F0FF; color: #005576; }
.icon-green  { background: #F0FDF4; color: #16A34A; }
.icon-purple { background: #D9F0FF; color: #6555B0; }
.icon-red    { background: #FFF1F2; color: #E11D48; }

/* 卡片文字 */
.quick-card-text { min-width: 0; }
.quick-card-title {
  font-size: 0.75rem;
  font-weight: 700;
  color: #1E293B;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: color 0.2s;
}
.quick-card:hover .quick-card-title {
  color: #005576;
}
.quick-card-sub {
  font-size: 0.625rem;      /* 10px */
  color: #64748B;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ==================== 04 主体分栏网格 ==================== */
.main-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
}
@media (min-width: 1024px) {
  .main-grid {
    grid-template-columns: repeat(12, 1fr);
  }
  .main-left  { grid-column: span 8; }
  .main-right { grid-column: span 4; }
}
.main-left {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}
.main-right {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* ==================== 05 分栏标题 ==================== */
.section-block {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.section-header-wrap {
  flex-wrap: wrap;
  gap: 0.5rem;
}
@media (min-width: 640px) {
  .section-header-wrap { flex-wrap: nowrap; }
}
.section-header-left {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.section-bar {
  width: 0.25rem;           /* 4px */
  height: 1rem;             /* 16px */
  border-radius: 999px;
  background: #83C9F4;
  flex-shrink: 0;
}
.section-title {
  font-size: 1rem;
  font-weight: 700;
  color: #1E293B;
}
.section-link {
  font-size: 0.75rem;
  color: #005576;
  font-weight: 500;
  cursor: pointer;
  background: none;
  border: none;
}
.section-link:hover {
  text-decoration: underline;
}

/* ==================== 06 课程卡片 ==================== */
.course-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1rem;
}
@media (min-width: 768px) {
  .course-grid { grid-template-columns: 1fr 1fr; }
}
.course-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: 0.75rem;
  border: 1px solid #E2E8F0;
  background: #FFFFFF;
  cursor: pointer;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s, border-color 0.2s, box-shadow 0.2s;
  animation: card-reveal 360ms ease-out both;
}
.course-card:hover {
  transform: translateY(-2px);
  border-color: #83C9F4;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
.course-card:focus-visible {
  outline: 2px solid #83C9F4;
  outline-offset: 2px;
}

/* ==================== 07 课程封面与角标 ==================== */
.course-cover {
  position: relative;
  height: 10rem;            /* 160px */
  overflow: hidden;
  background: #1E293B;
  flex-shrink: 0;
}
.course-cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0.9;
  transition: transform 0.5s;
}
.course-card:hover .course-cover-img {
  transform: scale(1.05);
}
.course-cover-shade {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.25), transparent 60%);
}
.course-badge {
  position: absolute;
  left: 0.75rem;
  top: 0.75rem;
  padding: 0.25rem 0.625rem;
  border-radius: 0.375rem;
  font-size: 0.625rem;
  font-weight: 600;
  color: #FFFFFF;
}
.badge-dark {
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
}
.badge-purple {
  background: #9381FF;
}

/* 课程正文 */
.course-body {
  display: flex;
  flex-direction: column;
  flex: 1;
  padding: 1rem;
}
.course-teacher {
  display: flex;
  align-items: center;
  gap: 0.625rem;
  margin-bottom: 0.75rem;
}
.course-avatar {
  width: 2rem;              /* 32px */
  height: 2rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 700;
  flex-shrink: 0;
}
.avatar-blue   { background: #D9F0FF; color: #005576; }
.avatar-purple { background: #D9F0FF; color: #6555B0; }
.course-teacher-info { min-width: 0; }
.course-teacher-label {
  font-size: 0.625rem;
  line-height: 1;
  color: #94A3B8;
}
.course-teacher-name {
  font-size: 0.75rem;
  font-weight: 500;
  color: #475569;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.course-name {
  font-size: 0.875rem;
  font-weight: 700;
  line-height: 1.25;
  color: #1E293B;
  min-height: 2.75rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* ==================== 08 研修通道卡片 ==================== */
.channel-filter {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  background: rgba(226, 232, 240, 0.4);
  padding: 0.25rem;
  border-radius: 0.5rem;
  font-size: 0.75rem;
}
.channel-btn {
  padding: 0.25rem 0.75rem;
  border-radius: 0.375rem;
  font-weight: 500;
  border: none;
  background: transparent;
  color: #64748B;
  cursor: pointer;
  white-space: nowrap;
  transition: color 0.2s, background 0.2s, box-shadow 0.2s;
}
.channel-btn:hover {
  color: #1E293B;
}
.channel-btn-active {
  background: #FFFFFF;
  color: #005576;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.track-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 0.75rem;
}
@media (min-width: 768px) {
  .track-grid { grid-template-columns: repeat(3, 1fr); }
}
.track-card {
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 0.75rem;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;
  animation: card-reveal 360ms ease-out both;
}
.track-card:hover {
  border-color: #83C9F4;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
.track-card:focus-visible {
  outline: 2px solid #83C9F4;
  outline-offset: 2px;
}
.track-cover {
  position: relative;
  height: 7rem;             /* 112px */
  border-radius: 0.5rem;
  overflow: hidden;
  margin-bottom: 0.75rem;
  background: #0F172A;
}
.track-cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0.85;
}
.track-tag {
  position: absolute;
  top: 0.5rem;
  left: 0.5rem;
  padding: 0.125rem 0.5rem;
  border-radius: 0.25rem;
  background: rgba(0, 0, 0, 0.6);
  color: #FFFFFF;
  font-size: 0.625rem;
}
.track-name {
  font-size: 0.75rem;
  font-weight: 700;
  color: #1E293B;
  margin-bottom: 0.25rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.track-summary {
  font-size: 0.6875rem;     /* 11px */
  color: #64748B;
  line-height: 1.625;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 0.75rem;
}

/* ==================== 09 右栏通用面板 ==================== */
.side-panel {
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 0.75rem;
  padding: 1.25rem;         /* 20px */
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  animation: card-reveal 360ms ease-out both;
}
.side-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}
.side-panel-title-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.side-panel-icon {
  width: 1rem;
  height: 1rem;
  color: #005576;
}
.side-panel-title {
  font-size: 0.875rem;
  font-weight: 700;
  color: #1E293B;
}
.side-panel-count {
  font-size: 0.625rem;
  padding: 0.125rem 0.5rem;
  border-radius: 999px;
  background: #F1F5F9;
  color: #64748B;
  font-family: 'JetBrains Mono', monospace;
}

/* ==================== 10 待办日程 ==================== */
.schedule-list {
  display: flex;
  flex-direction: column;
  gap: 0.875rem;
}
.schedule-item {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  font-size: 0.75rem;
  animation: card-reveal 340ms ease-out both;
}
.schedule-dot {
  width: 0.5rem;
  height: 0.5rem;
  border-radius: 50%;
  margin-top: 0.375rem;
  flex-shrink: 0;
}
.dot-red    { background: #DC2626; box-shadow: 0 0 6px rgba(220, 38, 38, 0.5); }
.dot-blue   { background: #83C9F4; box-shadow: 0 0 6px rgba(131, 201, 244, 0.6); }
.dot-purple { background: #9381FF; box-shadow: 0 0 6px rgba(147, 129, 255, 0.5); }

.schedule-info {
  display: flex;
  flex-direction: column;
  gap: 0.125rem;
}
.schedule-meta {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.schedule-time {
  font-weight: 600;
  color: #1E293B;
}
.schedule-text {
  color: #334155;
  font-weight: 500;
}

/* ==================== 11 企业公告 ==================== */
.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.announcement-item {
  padding: 0.625rem;
  border-radius: 0.5rem;
  cursor: pointer;
  border-bottom: 1px solid #F1F5F9;
  transition: background 0.2s;
  animation: card-reveal 340ms ease-out both;
}
.announcement-item:hover {
  background: #F8FAFC;
}
.announcement-item-last {
  border-bottom: none;
}
.announcement-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 0.625rem;
  color: #94A3B8;
  margin-bottom: 0.25rem;
}
.announcement-title {
  font-size: 0.75rem;
  font-weight: 600;
  color: #1E293B;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ==================== 12 标签配色变体 ==================== */
.schedule-tag {
  padding: 0.05rem 0.375rem;
  border-radius: 0.25rem;
  font-size: 0.625rem;
  font-weight: 700;
  white-space: nowrap;
  line-height: 1.4;
}
.tag-red    { background: #FEE2E2; color: #DC2626; }
.tag-blue   { background: #D9F0FF; color: #005576; }
.tag-purple { background: #D9F0FF; color: #6555B0; }

.announcement-tag {
  padding: 0.05rem 0.375rem;
  border-radius: 0.25rem;
  font-size: 0.625rem;
  font-weight: 500;
  white-space: nowrap;
  line-height: 1.4;
}
.tag-blue-bg  { background: #D9F0FF; color: #005576; }
.tag-amber-bg { background: #FEF3C7; color: #D97706; }

.announcement-date {
  font-size: 0.625rem;
  color: #94A3B8;
}

/* ==================== 13 入场动画 ==================== */
@keyframes welcome-text-in {
  from {
    opacity: 0.65;
    transform: translateY(3px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
@keyframes card-reveal {
  from {
    opacity: 0.65;
    transform: translateY(3px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
/* ==================== 14 无障碍：减少动效 ==================== */
@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>