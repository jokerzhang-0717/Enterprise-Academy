<template>
  <div class="player">
    <!-- 顶部栏 -->
    <header class="top-bar">
      <button class="back-btn" type="button" @click="$emit('back')" title="返回课程列表">
        <ArrowLeft class="icon" />
      </button>
      <div class="top-info">
        <h1 class="page-title">{{ courseTitle }}</h1>
        <div class="top-meta">
          <span class="live"><span class="live-dot"></span>自动学时打卡生效中</span>
          <span class="sep">·</span>
          <span>{{ currentLessonLabel }}</span>
        </div>
      </div>
      <span class="chip">课程学习中</span>
    </header>

    <!-- 主体：视频列 + 目录列 -->
    <div class="layout">
      <!-- 视频 + 课程详情 -->
      <div class="col-main">
        <div class="screen">
          <div class="watermark">云舒艺境 · 研发工号 094821 张浩然</div>

          <!-- 有片源时直接播管理端为该小节绑定的视频，用原生控件 -->
          <video
            v-if="currentLesson && currentLesson.url"
            :key="currentLesson.url"
            class="video"
            :src="currentLesson.url"
            controls
          />

          <template v-else>
            <img class="cover" :src="courseCover" alt="" />
            <p class="notice">
              <span class="notice-dot"></span>
              {{ currentLesson ? '本节尚未上传视频' : '本课尚未编排小节' }}
            </p>
            <button class="play-btn" type="button" @click="isPlaying = !isPlaying">
              <Pause v-if="isPlaying" class="play-icon" fill="currentColor" />
              <Play v-else class="play-icon" fill="currentColor" />
            </button>
          </template>

          <!-- 演示控制条：仅在没有真实视频时显示 -->
          <div v-if="!hasVideo" class="controls">
            <div class="scrub">
              <div class="scrub-fill"></div>
              <div class="scrub-thumb"></div>
            </div>
            <div class="controls-row">
              <div class="controls-group">
                <button class="control-btn" type="button" @click="isPlaying = !isPlaying">
                  <Pause v-if="isPlaying" class="icon" />
                  <Play v-else class="icon" />
                </button>
                <button class="control-btn" type="button" title="后退10秒"><RotateCcw class="icon icon-small" /></button>
                <button class="control-btn" type="button" title="快进10秒"><RotateCw class="icon icon-small" /></button>
                <div class="volume">
                  <Volume2 class="icon" />
                  <div class="volume-bar"><div class="volume-fill"></div></div>
                </div>
                <span class="time">未开始 / {{ currentDuration }}</span>
              </div>
              <div class="controls-group">
                <span class="quality">1080P 高清</span>
                <select v-model="playbackSpeed" class="speed-select">
                  <option value="1.0x">1.0x</option>
                  <option value="1.25x">1.25x</option>
                  <option value="1.5x">1.5x</option>
                  <option value="2.0x">2.0x</option>
                </select>
                <button class="control-btn" type="button" title="全屏"><Maximize2 class="icon icon-small" /></button>
              </div>
            </div>
          </div>
        </div>

        <section class="card">
          <h2 class="card-title">课程详情</h2>
          <h3 class="detail-title">{{ currentLesson ? currentLesson.name : '本课尚未编排小节' }}</h3>
          <p class="detail-text">{{ courseSummary }}</p>
        </section>
      </div>

      <!-- 课程目录 -->
      <aside class="col-side">
        <div class="card">
          <div class="syllabus-head">
            <h2 class="card-title"><FileText class="icon icon-blue" /> 课程目录</h2>
            <span class="syllabus-count">共 {{ chapters.length }} 章 · {{ totalLessons }} 节</span>
          </div>

          <p v-if="!chapters.length" class="empty">
            这门课还没有章节，请到管理端 · 课程管理里编排目录。
          </p>

          <div
            v-for="(chapter, ci) in chapters"
            :key="chapter.id"
            class="chapter"
            :class="{
              'chapter-active': chapterState(ci) === 'active',
              'chapter-locked': chapterState(ci) === 'locked'
            }"
          >
            <div
              class="chapter-head"
              :class="{ 'chapter-head-active': chapterState(ci) === 'active' }"
              @click="toggleChapter(ci)"
            >
              <div class="chapter-name">
                <CheckCircle2 v-if="chapterState(ci) === 'done'" class="icon icon-green" />
                <span v-else-if="chapterState(ci) === 'active'" class="chapter-bar"></span>
                <Lock v-else class="icon icon-small" />
                <span
                  class="chapter-title"
                  :class="{ 'chapter-title-active': chapterState(ci) === 'active' }"
                >{{ chapter.title }}</span>
              </div>

              <div class="chapter-stat">
                <span v-if="chapterState(ci) === 'locked'">待解锁</span>
                <template v-else>
                  <span :class="{ 'chapter-stat-done': chapterState(ci) === 'done' }">
                    {{ chapterState(ci) === 'done'
                      ? `${lessonsOf(ci).length}/${lessonsOf(ci).length}`
                      : `${doneInChapter(ci)}/${lessonsOf(ci).length} 学习中` }}
                  </span>
                  <ChevronUp v-if="expandedChapter === ci" class="icon icon-small" />
                  <ChevronDown v-else class="icon icon-small" />
                </template>
              </div>
            </div>

            <div v-if="expandedChapter === ci" class="lesson-list">
              <p v-if="!lessonsOf(ci).length" class="empty empty-small">本章还没有小节</p>
              <div
                v-for="(lesson, li) in lessonsOf(ci)"
                :key="lesson.id"
                class="lesson"
                :class="{ 'lesson-active': isCurrent(ci, li) }"
                @click="openLesson(ci, li)"
              >
                <div>
                  <div class="lesson-name">
                    <CheckCircle2 v-if="lessonState(ci, li) === 'done'" class="icon icon-small icon-green" />
                    <Play v-else-if="isCurrent(ci, li)" class="icon icon-small icon-fill icon-blue" />
                    <span v-else class="ring"></span>
                    <span :class="isCurrent(ci, li) ? 'lesson-title' : 'lesson-label'">
                      {{ lessonLabel(lesson.name, ci, li) }}
                    </span>
                    <span v-if="isCurrent(ci, li)" class="playing-tag">播放中</span>
                  </div>
                  <div v-if="isCurrent(ci, li)" class="lesson-meta">
                    时长 {{ lesson.duration || '未填' }}
                    <template v-if="!lesson.url"> · 管理端尚未上传视频</template>
                  </div>
                </div>
                <span v-if="!isCurrent(ci, li)" class="lesson-dur">{{ lesson.duration }}</span>
              </div>
            </div>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
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
  Lock
} from 'lucide-vue-next';
import { learnerStore } from '../learnerStore.js';
import { fetchCourse } from '../api/index.js';

const props = defineProps({
  courseId: {
    type: String,
    default: '',
  },
});

defineEmits(['back', 'navigate']);

const isPlaying = ref(false);
const playbackSpeed = ref('1.25x');

/* 基础信息用课程中心的列表缓存，避免来回切页时目录闪烁 */
const listCourse = computed(() => {
  const list = learnerStore.courses;
  return list.find(c => c.id === props.courseId)
    || list.find(c => (c.syllabus || []).length)
    || list[0];
});

/* /courses 列表不带 chapters，目录只能按 id 取详情 */
const detail = ref(null);
const course = computed(() => {
  const base = listCourse.value;
  if (base && detail.value && detail.value.id === base.id) return { ...base, ...detail.value };
  return base;
});

const chapters = computed(() => course.value?.syllabus || []);
const lessonsOf = (ci) => chapters.value[ci]?.lessons || [];
const totalLessons = computed(() =>
  chapters.value.reduce((n, ch) => n + (ch.lessons || []).length, 0)
);

/* 学习位置：进度落在哪一节，决定章/节的 已完成·学习中·待解锁 三态 */
const positionOfProgress = () => {
  if (!chapters.value.length) return { ci: 0, li: 0 };
  const pct = Math.min(100, Math.max(0, Number(course.value?.progress) || 0));
  let remaining = Math.round((totalLessons.value * pct) / 100);
  for (let ci = 0; ci < chapters.value.length; ci += 1) {
    const size = lessonsOf(ci).length;
    if (remaining < size) return { ci, li: remaining };
    remaining -= size;
  }
  const lastCi = chapters.value.length - 1;
  return { ci: lastCi, li: Math.max(0, lessonsOf(lastCi).length - 1) };
};

/* progress = 学到哪一节（三态依据），playing = 正在播哪一节：
   回看已完成小节不会把进度倒退回滚 */
const progress = ref(positionOfProgress());
const playing = ref({ ...progress.value });
const expandedChapter = ref(progress.value.ci);

const clampSpot = (spot) => {
  if (!chapters.value.length) return { ci: 0, li: 0 };
  const ci = Math.min(spot.ci, chapters.value.length - 1);
  return { ci, li: Math.min(spot.li, Math.max(0, lessonsOf(ci).length - 1)) };
};

/* 管理端删章/删节后，位置可能落到已经不存在的条目上 */
watch(chapters, () => {
  progress.value = clampSpot(progress.value);
  playing.value = clampSpot(playing.value);
  if (expandedChapter.value >= chapters.value.length) expandedChapter.value = progress.value.ci;
});

/* 目录是异步补进来的，到位后按课程进度重算学习位置 */
const syncPosition = () => {
  const spot = positionOfProgress();
  progress.value = spot;
  playing.value = { ...spot };
  expandedChapter.value = spot.ci;
};

watch(
  () => listCourse.value?.id,
  async (id) => {
    detail.value = null;
    if (!id) return;
    try {
      detail.value = await fetchCourse(id);
    } catch {
      /* 详情失败时退回列表里的课程信息 */
    }
    if (chapters.value.length) syncPosition();
  },
  { immediate: true },
);

const currentLesson = computed(() => lessonsOf(playing.value.ci)[playing.value.li] || null);
const hasVideo = computed(() => !!currentLesson.value?.url);
const courseTitle = computed(() => course.value?.title || '未命名课程');
const courseSummary = computed(() => course.value?.summary || '管理端还没有填写课程简介。');
const courseCover = computed(
  () => course.value?.thumbnail
    || 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=1200&q=80'
);
const currentDuration = computed(() => currentLesson.value?.duration || '未定时长');
const currentLessonLabel = computed(() => {
  const lesson = currentLesson.value;
  if (!lesson) return '本课尚未编排小节';
  return `第 ${playing.value.ci + 1} 章 · 第 ${playing.value.li + 1} 节: ${lesson.name}`;
});

const isCurrent = (ci, li) => playing.value.ci === ci && playing.value.li === li;

const chapterState = (ci) => {
  if (ci < progress.value.ci) return 'done';
  if (ci === progress.value.ci) return 'active';
  return 'locked';
};

const lessonState = (ci, li) => {
  if (isCurrent(ci, li)) return 'current';
  if (ci < progress.value.ci || (ci === progress.value.ci && li < progress.value.li)) return 'done';
  return 'todo';
};

const doneInChapter = (ci) =>
  ci === progress.value.ci ? progress.value.li : lessonsOf(ci).length;

/* 管理端手填的小节名一般不带编号，这里补；数据里已带 "3.1" 的不再重复 */
const lessonLabel = (name, ci, li) =>
  /^\d+\.\d+/.test(name) ? name : `${ci + 1}.${li + 1} ${name}`;

const toggleChapter = (ci) => {
  if (chapterState(ci) === 'locked') return;
  expandedChapter.value = expandedChapter.value === ci ? -1 : ci;
};

const openLesson = (ci, li) => {
  playing.value = { ci, li };
  expandedChapter.value = ci;
  isPlaying.value = false;
};
</script>

<style scoped>
/* ===== 基础 ===== */
.player {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-bottom: 48px;
  color: #1f2937;
  animation: fade-in 0.2s ease both;
}

@keyframes fade-in {
  from { opacity: 0; }
  to   { opacity: 1; }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50%      { opacity: 0.4; }
}

/* ===== 图标 ===== */
.icon { width: 18px; height: 18px; flex-shrink: 0; }
.icon-small { width: 14px; height: 14px; }
.icon-fill { fill: currentColor; }
.icon-blue { color: #2563eb; }
.icon-green { color: #16a34a; }

/* ===== 顶部栏 ===== */
.top-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
}

.back-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: none;
  color: #64748b;
  cursor: pointer;
  transition: background-color 0.2s ease;
}
.back-btn:hover { background: #f1f5f9; }

.top-info { min-width: 0; }

.page-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
}

.top-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 2px;
  font-size: 12px;
  color: #9ca3af;
}

.sep { color: #d1d5db; }

.live {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
  color: #15803d;
}

.live-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #16a34a;
  animation: pulse 1.6s ease infinite;
}

.chip {
  margin-left: auto;
  padding: 4px 10px;
  border-radius: 999px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 10px;
  font-weight: 600;
  white-space: nowrap;
}

/* ===== 布局 ===== */
.layout {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
}
@media (min-width: 1024px) {
  .layout { grid-template-columns: 2fr 1fr; }
}

.col-main, .col-side {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}

/* ===== 视频区 ===== */
.screen {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  aspect-ratio: 16 / 9;
  overflow: hidden;
  border-radius: 16px;
  background: #0f172a;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}

.watermark {
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

.video {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  background: #0f172a;
}

.cover {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0.35;
}

.notice {
  position: absolute;
  top: 16px;
  left: 16px;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0;
  padding: 4px 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(12px);
  color: #f8fafc;
  font-size: 11px;
}

.notice-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #16a34a;
}

.play-btn {
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
  background: rgba(37, 99, 235, 0.85);
  color: #ffffff;
  cursor: pointer;
  transform: translate(-50%, -50%);
  transition: background-color 0.2s ease, transform 0.2s ease;
}
.play-btn:hover {
  background: #2563eb;
  transform: translate(-50%, -50%) scale(1.1);
}

.play-icon {
  width: 30px;
  height: 30px;
}

/* ===== 演示控制条 ===== */
.controls {
  z-index: 10;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.9), rgba(0, 0, 0, 0.55) 55%, transparent);
  color: #f8fafc;
}

.scrub {
  position: relative;
  width: 100%;
  height: 6px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.3);
  cursor: pointer;
}

.scrub-fill {
  width: 66%;
  height: 100%;
  border-radius: 999px;
  background: #38bdf8;
}

.scrub-thumb {
  position: absolute;
  left: 66%;
  top: 50%;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #ffffff;
  transform: translate(-50%, -50%) scale(0);
  transition: transform 0.2s ease;
}
.scrub:hover .scrub-thumb { transform: translate(-50%, -50%) scale(1); }

.controls-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-size: 12px;
}

.controls-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.control-btn {
  display: flex;
  border: 0;
  padding: 0;
  background: none;
  color: inherit;
  cursor: pointer;
  transition: color 0.2s ease;
}
.control-btn:hover { color: #38bdf8; }

.volume {
  display: flex;
  align-items: center;
  gap: 6px;
  transition: color 0.2s ease;
}
.volume:hover { color: #38bdf8; }

.volume-bar {
  width: 56px;
  height: 4px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.3);
}

.volume-fill {
  width: 80%;
  height: 100%;
  border-radius: 999px;
  background: #ffffff;
}

.time {
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.8);
}

.quality {
  padding: 2px 6px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.12);
  font-size: 10px;
  color: rgba(255, 255, 255, 0.8);
}

.speed-select {
  padding: 2px 6px;
  border: 0;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.12);
  color: #f8fafc;
  font-size: 12px;
  outline: none;
  cursor: pointer;
}
.speed-select:hover { background: rgba(255, 255, 255, 0.2); }
.speed-select option { color: #0f172a; }

/* ===== 卡片 ===== */
.card {
  padding: 20px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.05);
}

.card-title {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 14px;
  font-size: 14px;
  font-weight: 700;
}

.detail-title {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 700;
}

.detail-text {
  margin: 0;
  font-size: 12px;
  line-height: 1.65;
  color: #9ca3af;
}

/* ===== 课程目录 ===== */
.syllabus-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 16px;
}
.syllabus-head .card-title { margin: 0; }

.syllabus-count {
  font-size: 12px;
  color: #9ca3af;
  white-space: nowrap;
}

.empty {
  padding: 12px;
  border: 1px dashed #e5e7eb;
  border-radius: 10px;
  font-size: 12px;
  color: #9ca3af;
}
.empty-small {
  border: 0;
  padding: 10px 12px 12px;
}

/* ===== 章 ===== */
.chapter {
  overflow: hidden;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
}
.chapter + .chapter { margin-top: 12px; }

.chapter-active {
  border: 2px solid #38bdf8;
}
.chapter-locked {
  opacity: 0.75;
}

.chapter-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 12px;
  background: #f8fafc;
  cursor: pointer;
}
.chapter-head-active {
  background: #eff6ff;
}

.chapter-name {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.chapter-title {
  font-weight: 700;
}
.chapter-locked .chapter-title { color: #9ca3af; }
.chapter-title-active { color: #2563eb; }

.chapter-bar {
  width: 6px;
  height: 14px;
  border-radius: 999px;
  background: #38bdf8;
  flex-shrink: 0;
}

.chapter-stat {
  display: flex;
  align-items: center;
  gap: 4px;
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 10px;
  color: #9ca3af;
  flex-shrink: 0;
}
.chapter-active .chapter-stat {
  color: #2563eb;
  font-family: inherit;
  font-size: 11px;
  font-weight: 600;
}
.chapter-stat-done { color: #15803d; }

/* ===== 小节 ===== */
.lesson-list {
  background: #ffffff;
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
.lesson + .lesson { border-top: 1px solid #e5e7eb; }
.lesson:hover { background: #f8fafc; }

.lesson-active {
  border-left: 4px solid #38bdf8;
  background: #eff6ff;
}
.lesson-active:hover { background: #eff6ff; }

.lesson-name {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.lesson-title { font-weight: 700; }
.lesson-label { color: #64748b; }

.playing-tag {
  padding: 1px 6px;
  border-radius: 4px;
  background: #38bdf8;
  color: #ffffff;
  font-size: 9px;
}

.lesson-meta {
  margin-top: 4px;
  padding-left: 22px;
  font-size: 10px;
  color: #9ca3af;
}

.lesson-dur {
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 10px;
  color: #9ca3af;
  flex-shrink: 0;
}

.ring {
  width: 14px;
  height: 14px;
  border: 1px solid #cbd5e1;
  border-radius: 50%;
  flex-shrink: 0;
}
</style>