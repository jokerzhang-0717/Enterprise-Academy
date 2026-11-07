<template>
  <div class="player">
    <!-- 顶部栏 -->
    <header class="top-bar">
      <button class="back-btn" type="button" @click="$emit('back')" title="返回课程列表">
        <ArrowLeft class="icon" />
      </button>
      <div class="top-info">
        <div class="top-eyebrow">
        <span v-if="currentLesson" class="live"><span class="live-dot"></span>进度自动保存 · 完成本节后计入学时</span>
        <span v-else class="live live--pending">课程内容尚未准备好</span>
        <span class="sep">·</span>
        <span class="eyebrow-lesson">{{ currentLessonLabel }}</span>
        </div>
        <h1 class="page-title">{{ courseTitle }}</h1>
      </div>
      <span
        class="chip"
        :class="{ 'chip--playing': isPlaying, 'chip--paused': !isPlaying && elapsedSeconds > 0 }"
      >
        <span class="chip-dot"></span>{{ playerStatusLabel }}
      </span>
    </header>

    <!-- 主体：视频列 + 目录列 -->
    <div class="layout">
      <!-- 视频 + 课程详情 -->
      <div class="col-main">
        <div class="screen">
          <div class="watermark">{{ watermarkText }}</div>

          <!-- 有片源时直接播管理端为该小节绑定的视频，用原生控件 -->
          <video
            v-if="currentLesson && currentLesson.url"
            ref="videoEl"
            :key="currentLesson.url"
            class="video"
            :src="currentLesson.url"
            controls
            @play="handleVideoPlay"
            @pause="handleVideoPause"
            @timeupdate="handleVideoTimeUpdate"
            @ended="handleVideoEnded"
            @error="handleVideoError"
          />

          <template v-else>
            <img class="cover" :src="courseCover" alt="" />
            <div v-if="!currentLesson" class="empty-player" role="status">
              <FileText class="empty-player__icon" />
              <strong>课程内容正在准备中</strong>
              <span>当前课程还没有可播放的小节，请稍后再来学习。</span>
            </div>
            <p v-else class="notice">
              <span class="notice-dot"></span>本节尚未上传视频
            </p>
          </template>

          <button
            v-if="currentLesson && !hasVideo"
            class="play-btn"
            type="button"
            :disabled="currentLesson.completed"
            :aria-label="isPlaying ? '暂停本节课程' : '播放本节课程'"
            :title="isPlaying ? '暂停' : '播放'"
            @click="togglePlayback"
          >
            <Pause v-if="isPlaying" class="play-icon" fill="currentColor" />
            <Play v-else class="play-icon" fill="currentColor" />
          </button>

          <!-- 演示控制条：仅在没有真实视频时显示 -->
          <div v-if="currentLesson && !hasVideo" class="controls">
            <div class="scrub">
              <div class="scrub-fill"></div>
              <div class="scrub-thumb"></div>
            </div>
            <div class="controls-row">
              <div class="controls-group">
                <button class="control-btn" type="button" :disabled="!currentLesson || currentLesson.completed" @click="togglePlayback">
                  <Pause v-if="isPlaying" class="icon" />
                  <Play v-else class="icon" />
                </button>
                <button class="control-btn" type="button" title="后退10秒"><RotateCcw class="icon icon-small" /></button>
                <button class="control-btn" type="button" title="快进10秒"><RotateCw class="icon icon-small" /></button>
                <div class="volume">
                  <Volume2 class="icon" />
                  <div class="volume-bar"><div class="volume-fill"></div></div>
                </div>
                <span class="time">{{ elapsedLabel }} / {{ currentDuration }}</span>
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

        <section class="card detail-card">
          <div class="card-head">
            <h2 class="card-title"><FileText class="icon icon-blue" /> 课程详情</h2>
            <span class="card-badge">{{ currentLesson ? '当前小节' : '待编排' }}</span>
          </div>
          <h3 class="detail-title">{{ currentLesson ? currentLesson.name : '本课尚未编排小节' }}</h3>
          <p class="detail-text">{{ courseSummary }}</p>
          <button
            v-if="currentLesson && !hasVideo && !currentLesson.completed"
            class="complete-button"
            type="button"
            :disabled="!elapsedSeconds || savingProgress"
            @click="completeCurrentLesson"
          >
            {{ savingProgress ? '正在保存...' : '完成本节并记录学时' }}
          </button>
          <p v-else-if="currentLesson?.completed" class="completion-note">本节已完成，学习记录已保存</p>
        </section>
      </div>

      <!-- 课程目录 -->
      <aside class="col-side">
        <div class="card">
          <div class="syllabus-head">
            <h2 class="card-title"><FileText class="icon icon-blue" /> 课程目录</h2>
            <span class="syllabus-count">{{ chapters.length }} 章 · {{ totalLessons }} 节</span>
          </div>

          <div v-if="totalLessons" class="progress-block">
            <div class="progress-meta">
              <span>学习进度</span>
              <strong>{{ progressPercent }}%</strong>
            </div>
            <div class="progress-track">
              <div class="progress-fill" :style="{ width: `${progressPercent}%` }"></div>
            </div>
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
                @click="handleLessonClick(ci, li)"
              >
                <div>
                  <div class="lesson-name">
                    <CheckCircle2 v-if="lessonState(ci, li) === 'done'" class="icon icon-small icon-green" />
                    <span v-else-if="!isCurrent(ci, li)" class="ring"></span>
                    <span v-else class="lesson-current-indicator" aria-hidden="true"></span>
                    <span :class="isCurrent(ci, li) ? 'lesson-title' : 'lesson-label'">
                      {{ lessonLabel(lesson.name, ci, li) }}
                    </span>
                    <span v-if="isCurrent(ci, li) && isPlaying" class="playing-tag">播放中</span>
                    <span v-else-if="isCurrent(ci, li) && elapsedSeconds > 0" class="playing-tag playing-tag--paused">已暂停</span>
                  </div>
                  <div v-if="isCurrent(ci, li)" class="lesson-meta">
                    时长 {{ lesson.duration || '未填' }}
                    <template v-if="!lesson.url"> · 管理端尚未上传视频</template>
                  </div>
                </div>
                <button
                  v-if="isCurrent(ci, li)"
                  class="lesson-toggle"
                  type="button"
                  :aria-label="isPlaying ? '暂停本节课程' : '播放本节课程'"
                  :title="isPlaying ? '暂停' : '播放'"
                  :disabled="lesson.completed"
                  @click.stop="togglePlayback"
                >
                  <Pause v-if="isPlaying" class="icon icon-small" />
                  <Play v-else class="icon icon-small" />
                </button>
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
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue';
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
import {
  fetchCourse,
  heartbeatStudySession,
  saveLessonProgress,
  startStudySession as startStudySessionApi,
  stopStudySession as stopStudySessionApi,
} from '../api/index.js';
import { useToast } from '../composables/useToast.js';

const props = defineProps({
  courseId: {
    type: String,
    default: '',
  },
  user: {
    type: Object,
    default: null,
  },
});

defineEmits(['back', 'navigate']);

const isPlaying = ref(false);
const videoEl = ref(null);
const playbackSpeed = ref('1.25x');
const elapsedSeconds = ref(0);
const savingProgress = ref(false);
const { show: showToast } = useToast();
let playbackTimer = null;
let lastProgressSavedAt = 0;
let heartbeatTimer = null;
let heartbeatInFlight = false;
let heartbeatRequest = null;
let studySessionId = '';
let studySessionStart = null;
let studySessionStop = null;
let shouldTrackStudy = false;
let studyTrackingErrorShown = false;
let completionInProgress = false;

/* 基础信息用课程中心的列表缓存，避免来回切页时目录闪烁 */
const listCourse = computed(() => {
  const list = learnerStore.courses;
  return list.find(c => c.id === props.courseId)
    || list.find(c => (c.syllabus || []).length)
    || list[0];
});

const markCourseAsStudying = () => {
  const cachedCourse = learnerStore.courses.find((item) => item.id === props.courseId);
  if (!cachedCourse) return;
  cachedCourse.status = 'studying';
  cachedCourse.statusLabel = '学习中';
  cachedCourse.lastStudiedAt = new Date().toISOString();
  cachedCourse.lastStudied = '刚刚';
};

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

/* 视频水印跟随当前登录学员，不再写死姓名和工号 */
const watermarkText = computed(() => {
  const user = props.user;
  if (!user) return '云舒艺境';
  const employeeId = user.employeeId || user.employeeNo || '';
  const name = user.name || '学员';
  return employeeId ? `云舒艺境 · ${name} · 工号 ${employeeId}` : `云舒艺境 · ${name}`;
});

const progressPercent = computed(() => {
  if (!totalLessons.value) return 0;
  const value = Number(course.value?.progress ?? course.value?.progressPercent ?? 0);
  if (!Number.isFinite(value)) return 0;
  return Math.min(100, Math.max(0, Math.round(value)));
});
const currentDuration = computed(() => currentLesson.value?.duration || '未定时长');
/* 顶栏状态胶囊与真实播放状态保持一致 */
const playerStatusLabel = computed(() => {
  if (!currentLesson.value) return '待编排';
  if (isPlaying.value) return '播放中';
  if (elapsedSeconds.value > 0) return '已暂停';
  return '课程学习中';
});
const currentLessonLabel = computed(() => {
  const lesson = currentLesson.value;
  if (!lesson) return '本课尚未编排小节';
  return `第 ${playing.value.ci + 1} 章 · 第 ${playing.value.li + 1} 节: ${lesson.name}`;
});
const elapsedLabel = computed(() => {
  const minutes = Math.floor(elapsedSeconds.value / 60);
  const seconds = elapsedSeconds.value % 60;
  return `${minutes}:${String(seconds).padStart(2, '0')}`;
});

const stopPlaybackTimer = () => {
  if (playbackTimer !== null) {
    window.clearInterval(playbackTimer);
    playbackTimer = null;
  }
};

const saveCurrentProgress = async (completed = false, seconds = elapsedSeconds.value) => {
  if (savingProgress.value) {
    if (!completed) return null;
    while (savingProgress.value) {
      await new Promise((resolve) => window.setTimeout(resolve, 25));
    }
    return saveCurrentProgress(true, seconds);
  }
  const lesson = currentLesson.value;
  if (!lesson || !props.courseId) return null;

  savingProgress.value = true;
  lastProgressSavedAt = Date.now();
  try {
    const result = await saveLessonProgress(props.courseId, lesson.id, {
      watchedSeconds: Math.max(0, Math.floor(seconds)),
      completed,
    });
    lesson.watchedSeconds = Math.max(lesson.watchedSeconds || 0, Math.floor(seconds));
    lesson.completed = Boolean(result.completed);
    if (detail.value) detail.value.progress = result.progressPercent;
    lastProgressSavedAt = Date.now();
    markCourseAsStudying();
    if (learnerStore.courses) {
      const cachedCourse = learnerStore.courses.find((item) => item.id === props.courseId);
      if (cachedCourse) {
        cachedCourse.progress = result.progressPercent;
        cachedCourse.progressPercent = result.progressPercent;
        cachedCourse.completedLessons = result.completedLessonCount;
        cachedCourse.completedLessonCount = result.completedLessonCount;
      }
    }
    return result;
  } catch (error) {
    showToast(error?.message || '学习进度保存失败，请检查网络后重试。', 'error');
    return null;
  } finally {
    savingProgress.value = false;
  }
};

const handleVideoTimeUpdate = async (event) => {
  const seconds = Math.floor(event.target.currentTime || 0);
  elapsedSeconds.value = Math.max(elapsedSeconds.value, seconds);
  if (Date.now() - lastProgressSavedAt >= 15000) {
    await saveCurrentProgress(false, elapsedSeconds.value);
  }
};

const startHeartbeatTimer = () => {
  if (heartbeatTimer !== null) return;
  heartbeatTimer = window.setInterval(async () => {
    if (!studySessionId || heartbeatInFlight) return;
    heartbeatInFlight = true;
    try {
      heartbeatRequest = heartbeatStudySession(studySessionId);
      await heartbeatRequest;
    } catch (error) {
      if (!studyTrackingErrorShown) {
        showToast(error?.message || '学习计时连接中断，请重新播放以继续记录。', 'error');
        studyTrackingErrorShown = true;
      }
      shouldTrackStudy = false;
      studySessionId = '';
      window.clearInterval(heartbeatTimer);
      heartbeatTimer = null;
    } finally {
      heartbeatInFlight = false;
      heartbeatRequest = null;
    }
  }, 15000);
};

const startStudySession = async () => {
  if (!currentLesson.value || currentLesson.value.completed) return;
  shouldTrackStudy = true;
  if (studySessionId) return;
  if (studySessionStart) return studySessionStart;

  const sessionId = crypto.randomUUID();
  studySessionStart = (async () => {
    try {
      await startStudySessionApi(props.courseId, currentLesson.value.id, sessionId);
      studySessionId = sessionId;
      studyTrackingErrorShown = false;
      markCourseAsStudying();
      if (shouldTrackStudy) {
        startHeartbeatTimer();
      } else {
        studySessionId = '';
        await stopStudySessionApi(sessionId);
      }
    } catch (error) {
      shouldTrackStudy = false;
      showToast(error?.message || '学习计时启动失败，请稍后重试。', 'error');
    } finally {
      studySessionStart = null;
    }
  })();
  return studySessionStart;
};

const stopStudySession = async () => {
  shouldTrackStudy = false;
  if (heartbeatTimer !== null) {
    window.clearInterval(heartbeatTimer);
    heartbeatTimer = null;
  }
  if (studySessionStop) return studySessionStop;

  studySessionStop = (async () => {
    if (studySessionStart) await studySessionStart;
    if (heartbeatRequest) {
      try {
        await heartbeatRequest;
      } catch (error) {
        if (!studyTrackingErrorShown) {
          showToast(error?.message || '学习计时连接中断，请重新播放以继续记录。', 'error');
          studyTrackingErrorShown = true;
        }
      }
    }
    if (!studySessionId) return;

    const sessionId = studySessionId;
    studySessionId = '';
    try {
      await stopStudySessionApi(sessionId);
    } catch (error) {
      showToast(error?.message || '学习计时结束保存失败，请检查网络。', 'error');
    }
  })();

  try {
    await studySessionStop;
  } finally {
    studySessionStop = null;
  }
};

const handleVideoPlay = () => {
  isPlaying.value = true;
  void startStudySession();
};

const togglePlayback = async () => {
  if (!currentLesson.value || currentLesson.value.completed) return;
  if (!hasVideo.value) {
    isPlaying.value = !isPlaying.value;
    return;
  }
  const video = videoEl.value;
  if (!video) return;
  if (video.paused || video.ended) {
    try {
      await video.play();
    } catch (error) {
      reportVideoLoadError(error);
    }
  } else {
    video.pause();
  }
};

const handleVideoPause = async (event) => {
  isPlaying.value = false;
  await stopStudySession();
  const seconds = Math.floor(event.target.currentTime || 0);
  elapsedSeconds.value = Math.max(elapsedSeconds.value, seconds);
  if (elapsedSeconds.value > 0) {
    await saveCurrentProgress(false, elapsedSeconds.value);
  }
};

const handleVideoEnded = () => {
  isPlaying.value = false;
  void completeCurrentLesson();
};

let videoErrorLessonId = '';
const reportVideoLoadError = (error) => {
  const id = currentLesson.value?.id || '';
  if (id && videoErrorLessonId === id) return;
  videoErrorLessonId = id;
  showToast(error?.message || '本节视频加载失败，请检查片源或稍后重试。', 'error');
};

const handleVideoError = () => {
  isPlaying.value = false;
  void stopStudySession();
  reportVideoLoadError();
};

const completeCurrentLesson = async () => {
  if (!currentLesson.value || currentLesson.value.completed || completionInProgress) return;
  completionInProgress = true;
  stopPlaybackTimer();
  isPlaying.value = false;
  try {
    await stopStudySession();
    const result = await saveCurrentProgress(true, elapsedSeconds.value);
    if (result) {
      const lessonIndex = playing.value.li + 1;
      if (lessonIndex < lessonsOf(playing.value.ci).length) {
        progress.value = { ...playing.value, li: lessonIndex };
      } else if (playing.value.ci + 1 < chapters.value.length) {
        progress.value = { ci: playing.value.ci + 1, li: 0 };
      } else {
        progress.value = { ...playing.value };
      }
      showToast('本节已完成，学习时长已记录。', 'success');
    }
  } finally {
    completionInProgress = false;
  }
};

const isCurrent = (ci, li) => playing.value.ci === ci && playing.value.li === li;

const chapterState = (ci) => {
  if (ci < progress.value.ci) return 'done';
  if (ci === progress.value.ci) return 'active';
  return 'locked';
};

const lessonState = (ci, li) => {
  if (lessonsOf(ci)[li]?.completed) return 'done';
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

const openLesson = async (ci, li) => {
  if (savingProgress.value || completionInProgress) return;
  stopPlaybackTimer();
  isPlaying.value = false;
  await stopStudySession();
  if (elapsedSeconds.value > 0) {
    await saveCurrentProgress(false, elapsedSeconds.value);
  }
  playing.value = { ci, li };
  expandedChapter.value = ci;
  videoErrorLessonId = '';
  /* 点击小节等同于按下播放键：切到该小节后直接开始播放 */
  await nextTick();
  if (videoEl.value) {
    try {
      await videoEl.value.play();
    } catch {
      /* 浏览器可能拦截自动播放，保留手动点击播放按钮即可 */
    }
  }
};

const handleLessonClick = (ci, li) => {
  if (isCurrent(ci, li)) {
    void togglePlayback();
    return;
  }
  void openLesson(ci, li);
};

watch(
  () => currentLesson.value?.id,
  () => {
    elapsedSeconds.value = Number(currentLesson.value?.watchedSeconds) || 0;
    lastProgressSavedAt = 0;
  },
  { immediate: true },
);

watch(isPlaying, (playingNow) => {
  stopPlaybackTimer();
  if (!playingNow || !currentLesson.value || currentLesson.value.completed) {
    void stopStudySession();
    return;
  }
  void startStudySession();
  /* 真实视频由 timeupdate 驱动 elapsedSeconds，只有演示模式才需要本地计时器 */
  if (hasVideo.value) return;
  playbackTimer = window.setInterval(() => {
    elapsedSeconds.value += 1;
    if (Date.now() - lastProgressSavedAt >= 15000) {
      saveCurrentProgress(false, elapsedSeconds.value);
    }
  }, 1000);
});

onBeforeUnmount(() => {
  stopPlaybackTimer();
  void stopStudySession();
  if (elapsedSeconds.value > 0 && currentLesson.value && !currentLesson.value.completed) {
    saveCurrentProgress(false, elapsedSeconds.value);
  }
});
</script>

<style scoped>
/* ===== 基础 ===== */
.player {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding-bottom: 48px;
  color: var(--app-text);
  animation: fade-in 0.2s ease both;
}

@keyframes fade-in {
  from { opacity: 0; transform: translateY(4px); }
  to   { opacity: 1; transform: none; }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50%      { opacity: 0.35; }
}

/* ===== 图标 ===== */
.icon { width: 18px; height: 18px; flex-shrink: 0; }
.icon-small { width: 14px; height: 14px; }
.icon-fill { fill: currentColor; }
.icon-blue { color: var(--app-accent-strong); }
.icon-green { color: var(--app-emerald-fg); }

/* ===== 顶部栏 ===== */
.top-bar {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 20px;
  background: linear-gradient(135deg, var(--app-surface) 0%, var(--app-accent-soft) 160%);
  border: 1px solid var(--app-border-soft);
  border-radius: 18px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}

.back-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 38px;
  height: 38px;
  padding: 0;
  border: 1px solid var(--app-border);
  border-radius: 12px;
  background: var(--app-surface);
  color: var(--app-text-3);
  cursor: pointer;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
  transition: background-color 0.18s ease, border-color 0.18s ease,
    color 0.18s ease, transform 0.18s ease;
}
.back-btn:hover {
  border-color: var(--app-accent-border);
  background: var(--app-accent-soft);
  color: var(--app-accent-deep);
  transform: translateX(-2px);
}
.back-btn:active { transform: translateX(0); }

.top-info { min-width: 0; flex: 1; }

.top-eyebrow {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  font-size: 11.5px;
  color: var(--app-text-4);
  line-height: 1.4;
}
.eyebrow-lesson {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 42vw;
}

.page-title {
  margin: 4px 0 0;
  font-size: clamp(16px, 1.35vw, 20px);
  font-weight: 800;
  letter-spacing: -0.01em;
  color: var(--app-heading);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sep { color: var(--app-text-6); }

.live {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  color: var(--app-emerald-fg);
  white-space: nowrap;
}
.live--pending { color: var(--app-text-4); }

.live-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--app-emerald-solid);
  box-shadow: 0 0 0 3px var(--app-emerald-bg);
  animation: pulse 1.8s ease infinite;
}

.chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-left: auto;
  padding: 6px 12px;
  border: 1px solid var(--app-accent-line);
  border-radius: 999px;
  background: var(--app-accent-soft);
  color: var(--app-accent-deep);
  font-size: 11px;
  font-weight: 700;
  white-space: nowrap;
  flex-shrink: 0;
}

.chip-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--app-accent-strong);
}

/* 状态胶囊跟随视频真实播放状态：播放中(绿) / 已暂停(灰) */
.chip--playing {
  border-color: var(--app-emerald-line);
  background: var(--app-emerald-soft);
  color: var(--app-emerald-fg);
}
.chip--playing .chip-dot {
  background: var(--app-emerald-solid);
  animation: pulse 1.8s ease infinite;
}

.chip--paused {
  border-color: var(--app-border);
  background: var(--app-surface-2);
  color: var(--app-text-4);
}
.chip--paused .chip-dot { background: var(--app-text-6); }

/* ===== 布局 ===== */
.layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 20px;
  align-items: start;
}
@media (min-width: 1024px) {
  .layout { grid-template-columns: minmax(0, 2.05fr) minmax(300px, 1fr); }
  .col-side { position: sticky; top: 16px; }
}

.col-main, .col-side {
  display: flex;
  flex-direction: column;
  gap: 18px;
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
  border-radius: 18px;
  background: radial-gradient(130% 130% at 12% 0%, #1b3455 0%, #0b132b 55%, #060b18 100%);
  box-shadow:
    0 26px 48px -26px rgba(8, 25, 58, 0.6),
    inset 0 1px 0 rgba(255, 255, 255, 0.06);
}

.watermark {
  position: absolute;
  top: 14px;
  right: 16px;
  z-index: 10;
  padding: 5px 11px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 999px;
  background: rgba(6, 12, 24, 0.46);
  backdrop-filter: blur(8px);
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 10.5px;
  letter-spacing: 0.02em;
  color: rgba(255, 255, 255, 0.62);
  user-select: none;
  pointer-events: none;
}

.video {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  background: #060b18;
}

.cover {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0.32;
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
  padding: 5px 12px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 999px;
  background: rgba(6, 12, 24, 0.6);
  backdrop-filter: blur(12px);
  color: #f8fafc;
  font-size: 11px;
}

.notice-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #34d399;
  box-shadow: 0 0 0 3px rgba(52, 211, 153, 0.18);
}

.empty-player {
  position: absolute;
  inset: 0;
  z-index: 5;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 24px;
  background: rgba(6, 11, 24, 0.58);
  color: #f8fafc;
  text-align: center;
}
.empty-player__icon {
  width: 30px;
  height: 30px;
  color: #7dd3fc;
}
.empty-player strong { font-size: 16px; }
.empty-player span {
  max-width: 360px;
  color: rgba(248, 250, 252, 0.76);
  font-size: 12px;
  line-height: 1.6;
}

.play-btn {
  position: absolute;
  left: 50%;
  top: 50%;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border: 1px solid rgba(255, 255, 255, 0.45);
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(56, 189, 248, 0.92), rgba(37, 99, 235, 0.92));
  color: #ffffff;
  cursor: pointer;
  transform: translate(-50%, -50%);
  box-shadow: 0 14px 26px -12px rgba(14, 116, 233, 0.85);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.play-btn:hover {
  transform: translate(-50%, -50%) scale(1.08);
  box-shadow: 0 18px 32px -12px rgba(14, 116, 233, 0.95);
}
.play-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: translate(-50%, -50%);
}

.play-icon {
  width: 22px;
  height: 22px;
}

/* ===== 演示控制条 ===== */
.controls {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 10;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 16px 18px 18px;
  background: linear-gradient(to top, rgba(3, 8, 20, 0.94), rgba(3, 8, 20, 0.56) 58%, transparent);
  color: #f8fafc;
}

.scrub {
  position: relative;
  width: 100%;
  height: 5px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.24);
  cursor: pointer;
}

.scrub-fill {
  width: 66%;
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #38bdf8, #7dd3fc);
}

.scrub-thumb {
  position: absolute;
  left: 66%;
  top: 50%;
  width: 13px;
  height: 13px;
  border-radius: 50%;
  background: #ffffff;
  box-shadow: 0 0 0 4px rgba(56, 189, 248, 0.28);
  transform: translate(-50%, -50%) scale(0);
  transition: transform 0.18s ease;
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
  transition: color 0.18s ease, transform 0.18s ease;
}
.control-btn:hover { color: #7dd3fc; transform: translateY(-1px); }

.volume {
  display: flex;
  align-items: center;
  gap: 6px;
  transition: color 0.18s ease;
}
.volume:hover { color: #7dd3fc; }

.volume-bar {
  width: 56px;
  height: 4px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.28);
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
  color: rgba(255, 255, 255, 0.82);
}

.quality {
  padding: 2px 7px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.14);
  font-size: 10px;
  color: rgba(255, 255, 255, 0.85);
}

.speed-select {
  padding: 3px 7px;
  border: 0;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.14);
  color: #f8fafc;
  font-size: 12px;
  outline: none;
  cursor: pointer;
}
.speed-select:hover { background: rgba(255, 255, 255, 0.22); }
.speed-select option { color: #0f172a; }

/* ===== 卡片 ===== */
.card {
  padding: 20px 22px;
  background: var(--app-surface);
  border: 1px solid var(--app-border-soft);
  border-radius: 18px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: var(--app-heading);
}
.card-title .icon { width: 16px; height: 16px; }

.card-badge {
  padding: 3px 9px;
  border: 1px solid var(--app-border);
  border-radius: 999px;
  background: var(--app-surface-2);
  color: var(--app-text-4);
  font-size: 10.5px;
  font-weight: 600;
  white-space: nowrap;
}

.detail-title {
  margin: 0 0 8px;
  font-size: 15.5px;
  font-weight: 700;
  color: var(--app-text);
}

.detail-text {
  margin: 0;
  font-size: 12.5px;
  line-height: 1.75;
  color: var(--app-text-4);
}

.complete-button {
  margin-top: 16px;
  padding: 10px 16px;
  border: 0;
  border-radius: 11px;
  background: linear-gradient(135deg, var(--app-accent), var(--app-accent-strong));
  color: var(--app-on-accent-solid);
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 10px 18px -12px var(--app-accent-strong);
  transition: transform 0.16s ease, box-shadow 0.16s ease, filter 0.16s ease;
}
.complete-button:hover:not(:disabled) {
  transform: translateY(-1px);
  filter: brightness(1.04);
  box-shadow: 0 14px 22px -12px var(--app-accent-strong);
}

.complete-button:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  box-shadow: none;
}

.completion-note {
  margin: 14px 0 0;
  color: var(--app-emerald-fg);
  font-size: 12px;
  font-weight: 600;
}

/* ===== 课程目录 ===== */
.syllabus-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 14px;
}
.syllabus-head .card-title { margin: 0; }

.syllabus-count {
  padding: 3px 9px;
  border: 1px solid var(--app-border-soft);
  border-radius: 999px;
  background: var(--app-surface-2);
  font-size: 10.5px;
  color: var(--app-text-4);
  white-space: nowrap;
}

.progress-block {
  margin-bottom: 16px;
  padding: 12px 14px;
  border: 1px solid var(--app-border-soft);
  border-radius: 13px;
  background: var(--app-surface-2);
}

.progress-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 11px;
  color: var(--app-text-4);
}
.progress-meta strong {
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 13px;
  font-weight: 700;
  color: var(--app-accent-deep);
}

.progress-track {
  height: 6px;
  overflow: hidden;
  border-radius: 999px;
  background: var(--app-border);
}

.progress-fill {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, var(--app-accent), var(--app-accent-strong));
  transition: width 0.45s ease;
}

.empty {
  padding: 12px;
  border: 1px dashed var(--app-border);
  border-radius: 10px;
  font-size: 12px;
  color: var(--app-text-4);
}
.empty-small {
  border: 0;
  padding: 10px 12px 12px;
}

/* ===== 章 ===== */
.chapter {
  overflow: hidden;
  border: 1px solid var(--app-border-soft);
  border-radius: 14px;
  background: var(--app-surface);
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}
.chapter + .chapter { margin-top: 10px; }

.chapter-active {
  border-color: var(--app-accent-border);
  box-shadow: 0 0 0 3px var(--app-accent-a10);
}
.chapter-locked { opacity: 0.72; }

.chapter-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 12px 14px;
  background: var(--app-surface-2);
  cursor: pointer;
  transition: background-color 0.15s ease;
}
.chapter-head:hover { background: var(--app-surface-3); }

.chapter-head-active { background: var(--app-accent-soft); }
.chapter-head-active:hover { background: var(--app-accent-hover); }

.chapter-name {
  display: flex;
  align-items: center;
  gap: 9px;
  min-width: 0;
}

.chapter-title {
  font-size: 13px;
  font-weight: 650;
  color: var(--app-text-2);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.chapter-locked .chapter-title { color: var(--app-text-4); }
.chapter-title-active { color: var(--app-accent-deep); }

.chapter-bar {
  width: 4px;
  height: 16px;
  border-radius: 999px;
  background: var(--app-accent-strong);
  flex-shrink: 0;
}

.chapter-stat {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 3px 8px;
  border: 1px solid var(--app-border-soft);
  border-radius: 999px;
  background: var(--app-surface);
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 10px;
  color: var(--app-text-4);
  white-space: nowrap;
  flex-shrink: 0;
}
.chapter-active .chapter-stat {
  border-color: var(--app-accent-line);
  background: var(--app-surface);
  color: var(--app-accent-deep);
  font-family: inherit;
  font-size: 10.5px;
  font-weight: 600;
}
.chapter-stat-done { color: var(--app-emerald-fg); }

/* ===== 小节 ===== */
.lesson-list {
  background: var(--app-surface);
}

.lesson {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 11px 14px 11px 17px;
  font-size: 12.5px;
  cursor: pointer;
  transition: background-color 0.15s ease;
}
.lesson + .lesson { border-top: 1px solid var(--app-border-soft); }
.lesson:hover { background: var(--app-surface-2); }

.lesson-active { background: var(--app-accent-soft); }
.lesson-active:hover { background: var(--app-accent-soft); }
.lesson-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 9px;
  bottom: 9px;
  width: 3px;
  border-radius: 0 999px 999px 0;
  background: var(--app-accent-strong);
}

.lesson-name {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}
.lesson-current-indicator {
  width: 14px;
  height: 14px;
  flex: 0 0 14px;
}
.lesson > div:first-child { min-width: 0; flex: 1; }
.lesson-toggle {
  display: inline-flex;
  width: 30px;
  height: 30px;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--app-accent-border);
  border-radius: 50%;
  background: var(--app-accent-soft);
  color: var(--app-accent-strong);
  cursor: pointer;
}
.lesson-toggle:hover { background: var(--app-accent); color: var(--app-on-accent-solid); }
.lesson-toggle:disabled { opacity: 0.5; cursor: not-allowed; }

.lesson-title {
  font-weight: 700;
  color: var(--app-text);
}
.lesson-label { color: var(--app-text-3); }

.playing-tag {
  padding: 2px 7px;
  border-radius: 999px;
  background: var(--app-accent-strong);
  color: var(--app-on-accent-solid);
  font-size: 9px;
  font-weight: 700;
  white-space: nowrap;
}

.playing-tag--paused {
  background: var(--app-surface-3);
  color: var(--app-text-4);
  font-weight: 600;
}

.lesson-meta {
  margin-top: 4px;
  padding-left: 22px;
  font-size: 10.5px;
  color: var(--app-text-4);
}

.lesson-dur {
  padding: 2px 7px;
  border-radius: 6px;
  background: var(--app-surface-2);
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 10px;
  color: var(--app-text-5);
  flex-shrink: 0;
}

.ring {
  width: 14px;
  height: 14px;
  border: 1.5px solid var(--app-text-6);
  border-radius: 50%;
  flex-shrink: 0;
}

@media (max-width: 640px) {
  .top-bar { padding: 14px; gap: 10px; }
  .eyebrow-lesson { display: none; }
  .chip { display: none; }
  .card { padding: 16px; }
  .controls-row { flex-wrap: wrap; }
}
</style>
