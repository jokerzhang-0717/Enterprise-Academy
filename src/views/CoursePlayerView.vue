<template>
  <div class="player">
    <!-- 顶部栏 -->
    <header class="top-bar">
      <button class="back-btn" type="button" @click="$emit('back')" title="返回课程列表">
        <ArrowLeft class="icon" />
      </button>
      <div class="top-info">
        <div class="top-eyebrow">
        <template v-if="!currentLesson">
          <span class="live live--pending">课程内容尚未准备好</span>
          <span class="sep">·</span>
        </template>
        <span class="eyebrow-lesson">{{ currentLessonLabel }}</span>
        </div>
        <h1 class="page-title">{{ courseTitle }}</h1>
      </div>
      <span class="chip">
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
          <h3 class="detail-title">{{ currentLesson ? lessonLabel(currentLesson.name) : '本课尚未编排小节' }}</h3>
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
        </section>
      </div>

      <!-- 视频选集（仿哔哩哔哩：所有小节平铺在一个方框里） -->
      <aside class="col-side">
        <div class="bili-panel">
          <div class="bili-panel-head">
            <h2 class="bili-head-title">视频选集</h2>
            <span v-if="totalLessons" class="bili-head-count">{{ currentGlobalIndex }}/{{ totalLessons }}</span>
          </div>

          <p v-if="!flatLessons.length" class="bili-empty">
            这门课还没有小节，请到管理端 · 课程管理里编排目录。
          </p>

          <div ref="episodesBox" class="bili-episodes">
            <div
              v-for="item in flatLessons"
              :key="item.lesson.id"
              class="bili-episode"
              :class="{ 'bili-episode-active': isCurrent(item.ci, item.li) }"
              @click="handleLessonClick(item.ci, item.li)"
            >
              <span class="bili-episode-name">{{ lessonLabel(item.lesson.name) }}</span>
              <span class="bili-episode-dur">{{ episodeDuration(item.lesson) }}</span>
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
  FileText
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
/* 仿哔哩哔哩：所有小节跨章平铺到同一个列表，不带章节分组 */
const flatLessons = computed(() => {
  const items = [];
  chapters.value.forEach((chapter, ci) => {
    (chapter.lessons || []).forEach((lesson, li) => {
      items.push({ lesson, ci, li });
    });
  });
  return items;
});
/* 仿哔哩哔哩选集头部：当前小节在整门课中的序号 */
const currentGlobalIndex = computed(() => {
  if (!totalLessons.value) return 0;
  let count = 0;
  for (let ci = 0; ci < playing.value.ci; ci += 1) count += lessonsOf(ci).length;
  return count + playing.value.li + 1;
});

/* 学习位置：进度落在哪一节，所有小节均可直接点开观看，不受进度限制 */
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

/* progress = 学到哪一节，playing = 正在播哪一节：
   回看已完成小节不会把进度倒退回滚 */
const progress = ref(positionOfProgress());
const playing = ref({ ...progress.value });
const episodesBox = ref(null);

const clampSpot = (spot) => {
  if (!chapters.value.length) return { ci: 0, li: 0 };
  const ci = Math.min(spot.ci, chapters.value.length - 1);
  return { ci, li: Math.min(spot.li, Math.max(0, lessonsOf(ci).length - 1)) };
};

/* 管理端删章/删节后，位置可能落到已经不存在的条目上 */
watch(chapters, () => {
  progress.value = clampSpot(progress.value);
  playing.value = clampSpot(playing.value);
});

/* 目录是异步补进来的，到位后按课程进度重算学习位置 */
const syncPosition = () => {
  const spot = positionOfProgress();
  progress.value = spot;
  playing.value = { ...spot };
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

const currentDuration = computed(() => currentLesson.value?.duration || '未定时长');
/* 顶栏状态胶囊：不展示播放/暂停标签 */
const playerStatusLabel = computed(() => {
  if (!currentLesson.value) return '待编排';
  return '课程学习中';
});
const currentLessonLabel = computed(() => {
  const lesson = currentLesson.value;
  if (!lesson) return '本课尚未编排小节';
  return lessonLabel(lesson.name);
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

const handleVideoEnded = async () => {
  isPlaying.value = false;
  await completeCurrentLesson();
  await playNextLesson();
};

/* 视频播完后自动切到下一小节继续播放；只切换播放器，不重复保存进度 */
const playNextLesson = async () => {
  const { ci, li } = playing.value;
  let nextCi = ci;
  let nextLi = li + 1;
  if (nextLi >= lessonsOf(ci).length) {
    nextCi = ci + 1;
    nextLi = 0;
  }
  if (nextCi >= chapters.value.length) return; // 已是最后一节
  playing.value = { ci: nextCi, li: nextLi };
  videoErrorLessonId = '';
  await nextTick();
  if (videoEl.value) {
    try {
      await videoEl.value.play();
    } catch {
      /* 浏览器拦截自动播放时，保留手动点击播放即可 */
    }
  }
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
    }
  } finally {
    completionInProgress = false;
  }
};

const isCurrent = (ci, li) => playing.value.ci === ci && playing.value.li === li;

/* 仿哔哩哔哩：选集标题只显示小节名，去掉数据里自带的 "3.1" 章节编号 */
const lessonLabel = (name) => String(name || '').replace(/^\d+\.\d+\s*/, '');

/* 仿哔哩哔哩：选集时长直接取 duration_seconds（已与 MP4 真实时长同步） */
const formatDuration = (seconds) => {
  const total = Number(seconds);
  if (!Number.isFinite(total) || total <= 0) return '--:--';
  const minutes = Math.floor(total / 60);
  const secs = Math.round(total % 60);
  return `${String(minutes).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
};
const episodeDuration = (lesson) => formatDuration(lesson.durationSeconds);

const openLesson = async (ci, li) => {
  if (savingProgress.value || completionInProgress) return;
  stopPlaybackTimer();
  isPlaying.value = false;
  await stopStudySession();
  if (elapsedSeconds.value > 0) {
    await saveCurrentProgress(false, elapsedSeconds.value);
  }
  playing.value = { ci, li };
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

/* 播放或切换到别的课时，把当前选集对齐到选集方框顶部（只滚方框，不影响整页） */
const revealActiveEpisode = async () => {
  await nextTick();
  const box = episodesBox.value;
  const active = box?.querySelector('.bili-episode-active');
  if (!box || !active) return;
  const boxRect = box.getBoundingClientRect();
  const activeRect = active.getBoundingClientRect();
  const top = box.scrollTop + activeRect.top - boxRect.top - 4;
  box.scrollTo({ top: Math.max(0, top), behavior: 'smooth' });
};

watch(
  () => currentLesson.value?.id,
  () => {
    elapsedSeconds.value = Number(currentLesson.value?.watchedSeconds) || 0;
    lastProgressSavedAt = 0;
    void revealActiveEpisode();
  },
  { immediate: true, flush: 'post' },
);

watch(isPlaying, (playingNow) => {
  if (playingNow) void revealActiveEpisode();
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

/* ===== 视频选集（仿哔哩哔哩：一个方框、小节平铺） ===== */
.bili-panel {
  overflow: hidden;
  border: 1px solid var(--app-border-soft);
  border-radius: 14px;
  background: var(--app-surface);
}

.bili-panel-head {
  display: flex;
  align-items: baseline;
  gap: 7px;
  padding: 14px 16px 10px;
}
.bili-head-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--app-heading);
}
.bili-head-count {
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 14px;
  font-weight: 700;
  color: var(--app-text-3);
}

.bili-empty {
  padding: 12px 16px 16px;
  font-size: 12px;
  color: var(--app-text-4);
}

/* 单个方框内的小节列表：超过 8 条在框内滚动（仿哔哩哔哩选集） */
.bili-episodes {
  max-height: 312px; /* 8 行 × 37.5px + 上下内边距 */
  overflow-y: auto;
  overscroll-behavior: contain;
  padding: 4px 0 8px;
  scrollbar-width: thin;
  scrollbar-color: var(--app-border) transparent;
}
.bili-episodes::-webkit-scrollbar { width: 6px; }
.bili-episodes::-webkit-scrollbar-track { background: transparent; }
.bili-episodes::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: var(--app-border);
}
.bili-episodes::-webkit-scrollbar-thumb:hover { background: var(--app-text-6); }
.bili-episode {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 16px;
  font-size: 12.5px;
  color: var(--app-text-3);
  cursor: pointer;
  transition: background-color 0.15s ease;
}
.bili-episode:hover { background: var(--app-surface-2); }
.bili-episode-active { background: var(--app-accent-soft); color: var(--app-accent-deep); }
.bili-episode-active:hover { background: var(--app-accent-soft); }
.bili-episode-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.bili-episode-active .bili-episode-name { font-weight: 700; color: var(--app-accent-deep); }
.bili-episode-dur {
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 13px;
  font-weight: 600;
  color: var(--app-text-3);
  flex: 0 0 auto;
}

@media (max-width: 640px) {
  .top-bar { padding: 14px; gap: 10px; }
  .eyebrow-lesson { display: none; }
  .chip { display: none; }
  .card { padding: 16px; }
  .controls-row { flex-wrap: wrap; }
}
</style>
