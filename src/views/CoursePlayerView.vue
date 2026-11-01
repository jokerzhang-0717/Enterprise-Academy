<template>
  <div class="player-page">
    <!-- 1. Player Top Header -->
    <header class="player-header">
      <div class="player-header__left">
        <button class="icon-btn" type="button" @click="$emit('back')" title="返回课程列表">
          <ArrowLeft class="icon icon--sm" />
        </button>

        <div class="player-header__info">
          <h1 class="player-header__title">
            Spring Boot 3 企业级微服务实战与高并发架构
          </h1>
          <div class="player-header__meta">
            <span class="live-status">
              <span class="live-status__dot"></span>
              自动学时打卡生效中
            </span>
            <span class="meta-sep">·</span>
            <span>第 3 章 · 第 2 节: Redis 集群分布式锁与缓存击穿防护</span>
          </div>
        </div>
      </div>

      <div class="player-header__right">
        <span class="badge">课程学习中</span>
      </div>
    </header>

    <!-- 2. Main Player Grid -->
    <div class="player-grid">
      <!-- Left Column: Video + Details -->
      <div class="player-col player-col--main">
        <!-- Video Player Frame -->
        <div class="video-frame">
          <!-- Watermark -->
          <div class="video-frame__watermark">
            云舒艺境 · 研发工号 094821 张浩然
          </div>

          <!-- Video Background Simulation -->
          <div class="video-screen">
            <img
              class="video-screen__bg"
              src="https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=1200&q=80"
              alt="Video backdrop"
            />

            <!-- Status Overlay -->
            <div class="video-screen__status">
              <span class="video-screen__status-dot"></span>
              <span>上次学至 18:42 · 已自动为您续播</span>
            </div>

            <!-- Big Center Play/Pause Button -->
            <button class="video-screen__play" type="button" @click="isPlaying = !isPlaying">
              <Pause v-if="isPlaying" class="video-screen__play-icon" />
              <Play v-else class="video-screen__play-icon video-screen__play-icon--play" />
            </button>
          </div>

          <!-- Player Controls Bar -->
          <div class="video-controls">
            <!-- Scrub line -->
            <div class="scrub-bar">
              <div class="scrub-bar__fill"></div>
              <div class="scrub-bar__thumb"></div>
            </div>

            <div class="video-controls__row">
              <div class="video-controls__group">
                <button class="ctrl-btn" type="button" @click="isPlaying = !isPlaying">
                  <Pause v-if="isPlaying" class="icon" />
                  <Play v-else class="icon" />
                </button>

                <button class="ctrl-btn" type="button" title="后退10秒">
                  <RotateCcw class="icon icon--sm" />
                </button>

                <button class="ctrl-btn" type="button" title="快进10秒">
                  <RotateCw class="icon icon--sm" />
                </button>

                <div class="volume">
                  <Volume2 class="icon" />
                  <div class="volume__bar">
                    <div class="volume__fill"></div>
                  </div>
                </div>

                <span class="video-controls__time">18:42 / 28:15</span>
              </div>

              <div class="video-controls__group">
                <span class="quality-tag">1080P 高清</span>

                <select v-model="playbackSpeed" class="speed-select">
                  <option value="1.0x">1.0x</option>
                  <option value="1.25x">1.25x</option>
                  <option value="1.5x">1.5x</option>
                  <option value="2.0x">2.0x</option>
                </select>

                <button class="ctrl-btn" type="button" title="全屏">
                  <Maximize2 class="icon icon--sm" />
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Course Details -->
        <section class="panel">
          <div class="panel__header">
            <h2 class="panel__title">课程详情</h2>
          </div>
          <div class="panel__body">
            <h3 class="detail-title">Redis 集群分布式锁与缓存击穿防护</h3>
            <p class="detail-text">
              本节讲解 Redis Cluster 环境下分布式锁的实现与安全边界，涵盖 Redisson 看门狗续期、Lua 脚本原子释放锁，以及缓存击穿的常见防护思路。
            </p>
          </div>
        </section>
      </div>

      <!-- Right Column: Syllabus -->
      <div class="player-col player-col--side">
        <div class="panel panel--pad">
          <div class="syllabus__header">
            <div class="syllabus__title-wrap">
              <FileText class="icon icon--blue" />
              <h3 class="panel__title">课程目录 (Syllabus)</h3>
            </div>
            <span class="syllabus__count">共 5 章</span>
          </div>

          <div class="chapter-list">
            <!-- Chapter 1 -->
            <div class="chapter">
              <div class="chapter__head">
                <div class="chapter__name">
                  <CheckCircle2 class="icon icon--done" />
                  <span class="chapter__title">第 1 章: 微服务架构选型与升级</span>
                </div>
                <span class="chapter__progress chapter__progress--done">3/3</span>
              </div>
            </div>

            <!-- Chapter 2 -->
            <div class="chapter">
              <div class="chapter__head">
                <div class="chapter__name">
                  <CheckCircle2 class="icon icon--done" />
                  <span class="chapter__title">第 2 章: 高并发削峰与消息事务实战</span>
                </div>
                <span class="chapter__progress chapter__progress--done">5/5</span>
              </div>
            </div>

            <!-- Chapter 3 (Active) -->
            <div class="chapter chapter--active">
              <div class="chapter__head chapter__head--active" @click="activeChapter = activeChapter === 3 ? 0 : 3">
                <div class="chapter__name">
                  <span class="chapter__bar"></span>
                  <span class="chapter__title chapter__title--active">第 3 章: 分布式缓存与数据一致性</span>
                </div>
                <div class="chapter__toggle">
                  <span>3/6 学习中</span>
                  <ChevronUp v-if="activeChapter === 3" class="icon icon--sm" />
                  <ChevronDown v-else class="icon icon--sm" />
                </div>
              </div>

              <div v-if="activeChapter === 3" class="lesson-list">
                <div class="lesson">
                  <div class="lesson__name">
                    <CheckCircle2 class="icon icon--done icon--sm" />
                    <span>3.1 Redis 哨兵与 Cluster 集群架构选型</span>
                  </div>
                  <span class="lesson__dur">22分</span>
                </div>

                <div class="lesson lesson--active">
                  <div>
                    <div class="lesson__name">
                      <Play class="icon icon--sm icon--fill icon--blue" />
                      <span class="lesson__title">3.2 Redis 集群分布式锁与缓存击穿防护</span>
                      <span class="lesson__tag">正在学</span>
                    </div>
                    <div class="lesson__meta">时长 28:15 · 进度 66%</div>
                  </div>
                </div>

                <div class="lesson">
                  <div class="lesson__name">
                    <span class="lesson__ring"></span>
                    <span class="lesson__label">3.3 缓存穿透布隆过滤器实战</span>
                  </div>
                  <span class="lesson__dur">20分</span>
                </div>

                <div class="lesson">
                  <div class="lesson__name">
                    <span class="lesson__ring"></span>
                    <span class="lesson__label">3.4 缓存雪崩双层防护与多级缓存体系</span>
                  </div>
                  <span class="lesson__dur">35分</span>
                </div>
              </div>
            </div>

            <!-- Chapter 4 (Locked) -->
            <div class="chapter chapter--locked">
              <div class="chapter__head">
                <div class="chapter__name chapter__name--muted">
                  <Lock class="icon icon--sm" />
                  <span>第 4 章: 全链路分布式追踪与可观测性实战</span>
                </div>
                <span class="chapter__progress">待解锁</span>
              </div>
            </div>

            <!-- Chapter 5 (Exam Final) -->
            <div class="chapter chapter--locked">
              <div class="chapter__head">
                <div class="chapter__name chapter__name--purple">
                  <Award class="icon icon--sm" />
                  <span>第 5 章: 期末综合考核与实操答辩</span>
                </div>
                <span class="chapter__progress chapter__progress--purple">实战答辩</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import {
  ArrowLeft,
  Play,
  Pause,
  RotateCcw,
  RotateCw,
  Volume2,
  Maximize2,
  FileText,
  CheckCircle2,
  ChevronDown,
  ChevronUp,
  Lock,
  Award
} from 'lucide-vue-next';

defineEmits(['back', 'navigate']);

const isPlaying = ref(false);
const playbackSpeed = ref('1.25x');
const activeChapter = ref(3);
</script>

<style scoped>
/* ============ Design Tokens ============ */
.player-page {
  --c-border: #e2e8f0;
  --c-bg-soft: #f8fafc;
  --c-blue: #005576;
  --c-blue-bg: #d9f0ff;
  --c-sky: #83c9f4;
  --c-text: #1e293b;
  --c-text-sub: #334155;
  --c-text-soft: #475569;
  --c-muted: #64748b;
  --c-gray: #94a3b8;
  --c-purple: #6555b0;
  --c-green: #10b981;
  --c-green-text: #059669;

  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-bottom: 48px;
  animation: fadeIn 0.2s ease both;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to   { opacity: 1; }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50%      { opacity: 0.45; }
}

/* ============ Icons ============ */
.icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}
.icon--sm {
  width: 14px;
  height: 14px;
}
.icon--fill {
  fill: currentColor;
}
.icon--blue {
  color: var(--c-blue);
}
.icon--done {
  color: var(--c-green);
}

/* ============ Header ============ */
.player-header {
  display: flex;
  flex-direction: column;
  gap: 12px;
  justify-content: space-between;
  padding: 16px;
  border: 1px solid var(--c-border);
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.05);
}
@media (min-width: 768px) {
  .player-header {
    flex-direction: row;
    align-items: center;
  }
}

.player-header__left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
  color: var(--c-text-soft);
  cursor: pointer;
  transition: background-color 0.2s ease;
}
.icon-btn:hover {
  background: #f4f9fd;
}

.player-header__title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--c-text);
}

.player-header__meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 2px;
  font-size: 12px;
  color: var(--c-muted);
}

.live-status {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
  color: var(--c-green-text);
}

.live-status__dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--c-green);
  animation: pulse 1.6s ease infinite;
}

.badge {
  padding: 4px 8px;
  border-radius: 999px;
  background: var(--c-blue-bg);
  color: var(--c-blue);
  font-size: 10px;
  font-weight: 600;
}

/* ============ Grid ============ */
.player-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
}
@media (min-width: 1024px) {
  .player-grid {
    grid-template-columns: repeat(12, 1fr);
  }
  .player-col--main { grid-column: span 8 / span 8; }
  .player-col--side { grid-column: span 4 / span 4; }
}

.player-col {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}

/* ============ Video Frame ============ */
.video-frame {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  aspect-ratio: 16 / 9;
  overflow: hidden;
  border: 1px solid #1e293b;
  border-radius: 16px;
  background: #0b132b;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -4px rgba(0, 0, 0, 0.1);
}

.video-frame__watermark {
  position: absolute;
  top: 12px;
  right: 16px;
  z-index: 10;
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.3);
  user-select: none;
  pointer-events: none;
}

.video-screen {
  position: relative;
  flex: 1;
  overflow: hidden;
}

.video-screen__bg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0.35;
}

.video-screen__status {
  position: absolute;
  top: 16px;
  left: 16px;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(12px);
  color: #fff;
  font-size: 11px;
}

.video-screen__status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--c-green);
}

.video-screen__play {
  position: absolute;
  left: 50%;
  top: 50%;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-radius: 50%;
  background: rgba(131, 201, 244, 0.8);
  color: var(--c-blue);
  cursor: pointer;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  transform: translate(-50%, -50%);
  transition: background-color 0.2s ease, transform 0.2s ease;
}
.video-screen__play:hover {
  background: var(--c-sky);
  transform: translate(-50%, -50%) scale(1.1);
}

.video-screen__play-icon {
  width: 32px;
  height: 32px;
  fill: currentColor;
}
.video-screen__play-icon--play {
  margin-left: 4px;
}

/* ---- Controls Bar ---- */
.video-controls {
  z-index: 10;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.9), rgba(0, 0, 0, 0.6) 50%, transparent);
}

.scrub-bar {
  position: relative;
  width: 100%;
  height: 6px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.3);
  cursor: pointer;
}

.scrub-bar__fill {
  width: 66%;
  height: 100%;
  border-radius: 999px;
  background: var(--c-sky);
}

.scrub-bar__thumb {
  position: absolute;
  left: 66%;
  top: 50%;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -2px rgba(0, 0, 0, 0.1);
  transform: translate(-50%, -50%) scale(0);
  transition: transform 0.2s ease;
}
.scrub-bar:hover .scrub-bar__thumb {
  transform: translate(-50%, -50%) scale(1);
}

.video-controls__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #fff;
  font-size: 12px;
}

.video-controls__group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ctrl-btn {
  display: flex;
  align-items: center;
  border: 0;
  padding: 0;
  background: none;
  color: inherit;
  cursor: pointer;
  transition: color 0.2s ease;
}
.ctrl-btn:hover {
  color: var(--c-sky);
}

.volume {
  display: flex;
  align-items: center;
  gap: 6px;
  transition: color 0.2s ease;
}
.volume:hover {
  color: var(--c-sky);
}

.volume__bar {
  width: 56px;
  height: 4px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.3);
}

.volume__fill {
  width: 80%;
  height: 100%;
  border-radius: 999px;
  background: #fff;
}

.video-controls__time {
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.8);
}

.quality-tag {
  padding: 2px 6px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.1);
  font-size: 10px;
  color: rgba(255, 255, 255, 0.8);
}

.speed-select {
  padding: 2px 6px;
  border: 0;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  font-size: 12px;
  outline: none;
  cursor: pointer;
}
.speed-select:hover {
  background: rgba(255, 255, 255, 0.2);
}
.speed-select option {
  color: #000;
}

/* ============ Panel (通用卡片) ============ */
.panel {
  overflow: hidden;
  border: 1px solid var(--c-border);
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.05);
}
.panel--pad {
  padding: 20px;
}

.panel__header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--c-border);
}

.panel__title {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: var(--c-text);
}

.panel__body {
  padding: 20px;
}

.detail-title {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 700;
  color: var(--c-text);
}

.detail-text {
  margin: 0;
  font-size: 12px;
  line-height: 1.625;
  color: var(--c-muted);
}

/* ============ Syllabus ============ */
.syllabus__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.syllabus__title-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
}

.syllabus__count {
  font-size: 12px;
  color: var(--c-gray);
}

.chapter-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  font-size: 12px;
}

.chapter {
  overflow: hidden;
  border: 1px solid var(--c-border);
  border-radius: 12px;
}

.chapter--active {
  border: 2px solid var(--c-sky);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.05);
}

.chapter--locked {
  opacity: 0.75;
}

.chapter__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: var(--c-bg-soft);
  cursor: pointer;
}

.chapter__head--active {
  background: var(--c-blue-bg);
}

.chapter__name {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.chapter__name--muted {
  color: var(--c-gray);
}

.chapter__name--purple {
  color: var(--c-purple);
}

.chapter__title {
  font-weight: 700;
  color: var(--c-text);
}

.chapter__title--active {
  color: var(--c-blue);
}

.chapter__bar {
  width: 6px;
  height: 14px;
  border-radius: 999px;
  background: var(--c-sky);
}

.chapter__progress {
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 10px;
  color: var(--c-gray);
  flex-shrink: 0;
}

.chapter__progress--done {
  color: var(--c-green-text);
}

.chapter__progress--purple {
  color: var(--c-purple);
}

.chapter__toggle {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  font-weight: 600;
  color: var(--c-blue);
  flex-shrink: 0;
}

/* ---- Lessons ---- */
.lesson-list {
  background: #fff;
}

.lesson {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 12px;
  cursor: pointer;
  transition: background-color 0.15s ease;
}
.lesson + .lesson {
  border-top: 1px solid var(--c-border);
}
.lesson:hover {
  background: var(--c-bg-soft);
}

.lesson--active {
  border-left: 4px solid var(--c-sky);
  background: var(--c-blue-bg);
}
.lesson--active:hover {
  background: var(--c-blue-bg);
}

.lesson__name {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  margin-bottom: 4px;
}
.lesson:not(.lesson--active) .lesson__name {
  margin-bottom: 0;
}

.lesson__title {
  font-weight: 700;
  color: var(--c-text);
}

.lesson__label {
  color: var(--c-text-soft);
}

.lesson__tag {
  padding: 1px 6px;
  border-radius: 4px;
  background: var(--c-sky);
  color: var(--c-blue);
  font-size: 9px;
}

.lesson__meta {
  padding-left: 20px;
  font-size: 10px;
  color: var(--c-muted);
}

.lesson__dur {
  font-size: 10px;
  color: var(--c-gray);
  flex-shrink: 0;
}

.lesson__ring {
  width: 14px;
  height: 14px;
  border: 1px solid #cbd5e1;
  border-radius: 50%;
  flex-shrink: 0;
}
</style>