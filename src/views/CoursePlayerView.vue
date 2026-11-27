<template>
  <div class="player">

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

    <div class="layout">

      <!-- 左栏：播放器 + 视频信息（B 站结构） -->
      <div class="col-main">
        <div class="screen">
          <div class="watermark">{{ watermarkText }}</div>

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
            @loadedmetadata="handleVideoLoaded"
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
        </div>

        <!-- 视频信息区：多视频时跟在播放器下方；单视频时移到右栏「更多课程」上方 -->
        <section v-if="!isSingleLesson" class="video-info">
          <h2 class="vi-title">{{ courseTitle }}</h2>
          <p class="vi-desc">{{ courseSummary }}</p>
          <div class="vi-tags">
            <span class="vi-tag">{{ course?.categoryLabel || '视频课程' }}</span>
            <span class="vi-tag">企业培训</span>
          </div>
        </section>

        <!-- 多视频时保留原详情卡里的「无视频完成」能力 -->
        <section v-if="!isSingleLesson && currentLesson && !hasVideo && !currentLesson.completed" class="card detail-card">
          <div class="card-head">
            <h2 class="card-title"><FileText class="icon icon-blue" /> 课程详情</h2>
          </div>
          <h3 class="detail-title">{{ lessonLabel(currentLesson.name) }}</h3>
          <button class="complete-button" type="button" :disabled="savingProgress" @click="completeCurrentLesson">
            {{ savingProgress ? '正在保存...' : '完成本节并记录学时' }}
          </button>
        </section>
      </div>

      <!-- 右栏：始终显示。多视频=选集；单视频=课程信息 + 更多课程 -->
      <aside class="col-side">
        <!-- 多视频才显示选集面板；单视频时右栏只留推荐 -->
        <div v-if="!isSingleLesson" class="bili-panel">
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

        <!-- 单视频课程：课程信息放在推荐列表上方 -->
        <section v-if="isSingleLesson" class="video-info">
          <h2 class="vi-title">{{ courseTitle }}</h2>
          <p class="vi-desc">{{ courseSummary }}</p>
          <div class="vi-tags">
            <span class="vi-tag">{{ course?.categoryLabel || '视频课程' }}</span>
            <span class="vi-tag">企业培训</span>
          </div>
        </section>

        <!-- 更多推荐课程 -->
        <div class="bili-panel rec-panel">
          <div class="bili-panel-head"><h2 class="bili-head-title">更多课程</h2></div>
          <div class="rec-list">
            <div
              v-for="item in relatedCourses"
              :key="item.id"
              class="rec-item"
              @click="$emit('navigate', 'course-player', { courseId: item.id })"
            >
              <div class="rec-cover"><img :src="item.thumbnail" alt="" /><span>{{ item.lessonCount || '' }}</span></div>
              <div class="rec-text">
                <span class="rec-title">{{ item.title }}</span>
                <span class="rec-up">{{ item.instructor?.name || '云舒艺境' }}</span>
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
import { ArrowLeft, FileText } from 'lucide-vue-next';
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
const elapsedSeconds = ref(0);
const savingProgress = ref(false);
const { show: showToast } = useToast();
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

const flatLessons = computed(() => {
  const items = [];
  chapters.value.forEach((chapter, ci) => {
    (chapter.lessons || []).forEach((lesson, li) => {
      items.push({ lesson, ci, li });
    });
  });
  return items;
});

const currentGlobalIndex = computed(() => {
  if (!totalLessons.value) return 0;
  let count = 0;
  for (let ci = 0; ci < playing.value.ci; ci += 1) count += lessonsOf(ci).length;
  return count + playing.value.li + 1;
});

const positionOfProgress = () => {
  if (!chapters.value.length) return { ci: 0, li: 0 };
  const watchedLessons = [];
  for (let ci = 0; ci < chapters.value.length; ci += 1) {
    const lessons = lessonsOf(ci);
    for (let li = 0; li < lessons.length; li += 1) {
      const lesson = lessons[li];
      const watched = Number(lesson.watchedSeconds || 0);
      if (watched <= 0 && !lesson.lastStudiedAt) continue;
      const ts = lesson.lastStudiedAt ? new Date(lesson.lastStudiedAt).getTime() : 0;
      watchedLessons.push({
        ci, li,
        stamp: Number.isFinite(ts) ? ts : 0,
        watched,
        completed: Boolean(lesson.completed),
      });
    }
  }
  // 优先用更新时间排序（精确）；旧接口没有时间字段时退化为按观看秒数近似
  const useTimestamp = watchedLessons.some((item) => item.stamp > 0);
  let latest = null;
  for (const item of watchedLessons) {
    const rank = useTimestamp ? item.stamp : item.watched;
    // 排序键相同（如批量导入的时间戳一致）时，优先未完成的课时
    const better = !latest || rank > latest.rank
      || (rank === latest.rank && latest.completed && !item.completed);
    if (better) latest = { ...item, rank };
  }
  if (latest) {
    if (!latest.completed) return { ci: latest.ci, li: latest.li };
    // 刚看完最后一节，顺延到下一节（跨章时回到下一章首节）
    const size = lessonsOf(latest.ci).length;
    if (latest.li + 1 < size) return { ci: latest.ci, li: latest.li + 1 };
    if (latest.ci + 1 < chapters.value.length) return { ci: latest.ci + 1, li: 0 };
    return { ci: latest.ci, li: latest.li };
  }
  // 回退：按已完成课时数定位到下一节
  let completedCount = 0;
  for (const chapter of chapters.value) {
    for (const lesson of (chapter.lessons || [])) {
      if (lesson.completed) completedCount += 1;
    }
  }
  if (completedCount > 0 && completedCount < totalLessons.value) {
    let remaining = completedCount;
    for (let ci = 0; ci < chapters.value.length; ci += 1) {
      const size = lessonsOf(ci).length;
      if (remaining < size) return { ci, li: remaining };
      remaining -= size;
    }
  }
  // 最后回退：按课程总进度百分比
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

const progress = ref(positionOfProgress());
const playing = ref({ ...progress.value });
const episodesBox = ref(null);

const clampSpot = (spot) => {
  if (!chapters.value.length) return { ci: 0, li: 0 };
  const ci = Math.min(spot.ci, chapters.value.length - 1);
  return { ci, li: Math.min(spot.li, Math.max(0, lessonsOf(ci).length - 1)) };
};

watch(chapters, () => {
  progress.value = clampSpot(progress.value);
  playing.value = clampSpot(playing.value);
});

const syncPosition = () => {
  const spot = positionOfProgress();
  progress.value = spot;
  playing.value = { ...spot };
  // 计时基准同步为当前课时已观看秒数，避免初次进入后从 0 覆盖已有进度
  elapsedSeconds.value = Math.floor(Number(lessonsOf(spot.ci)[spot.li]?.watchedSeconds || 0));
};

watch(
  () => listCourse.value?.id,
  async (id) => {
    detail.value = null;
    if (!id) return;
    try {
      detail.value = await fetchCourse(id);
    } catch {

    }
    if (chapters.value.length) syncPosition();
  },
  { immediate: true },
);

const currentLesson = computed(() => lessonsOf(playing.value.ci)[playing.value.li] || null);
const hasVideo = computed(() => !!currentLesson.value?.url);
// 单视频课程：采用专属的宽屏沉浸布局，不再显示「1/1」的选集侧栏
const isSingleLesson = computed(() => totalLessons.value === 1);
const courseTitle = computed(() => course.value?.title || '未命名课程');
const courseSummary = computed(() => course.value?.summary || '管理端还没有填写课程简介。');
const courseCover = computed(
  () => course.value?.thumbnail
    || 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=1200&q=80'
);

// 相关课程推荐（排除当前课程）
const relatedCourses = computed(() => {
  const list = learnerStore.courses.filter(c => c.published && c.id !== props.courseId);
  return list.slice(0, 2).map(c => ({
    ...c,
    lessonCount: c.syllabus?.reduce((n, ch) => n + (ch.lessons?.length || 0), 0) || 0,
  }));
});

const watermarkText = computed(() => {
  const user = props.user;
  if (!user) return '云舒艺境';
  const employeeId = user.employeeId || user.employeeNo || '';
  const name = user.name || '学员';
  return employeeId ? `云舒艺境 · ${name} · 工号 ${employeeId}` : `云舒艺境 · ${name}`;
});

const playerStatusLabel = computed(() => {
  if (!currentLesson.value) return '待编排';
  return '课程学习中';
});
const currentLessonLabel = computed(() => {
  const lesson = currentLesson.value;
  if (!lesson) return '本课尚未编排小节';
  return lessonLabel(lesson.name);
});

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

// 切换分集时旧 <video> 仍可能派发 timeupdate / pause 等事件。
// 必须校验事件来源，否则旧视频的播放时间会被写成新分集的进度，导致新分集从旧时间续播。
const isActiveVideoEvent = (event) => {
  const video = event?.target;
  if (!video || video !== videoEl.value) return false;
  return video.getAttribute('src') === currentLesson.value?.url;
};

// 断点续学：视频元数据加载完成后，跳到上次观看位置（未看完且位置有效时）。
const handleVideoLoaded = (event) => {
  const video = event.target;
  const lesson = currentLesson.value;
  if (!video || !lesson || !isActiveVideoEvent(event)) return;
  const watched = Math.floor(Number(lesson.watchedSeconds || 0));
  const duration = Number(video.duration || lesson.durationSeconds || 0);
  // 看到接近结尾（留 5 秒容差）视为已看完，不回退
  if (lesson.completed || watched <= 0 || !Number.isFinite(duration) || duration <= 0) return;
  const resumeAt = Math.min(watched, Math.max(0, duration - 5));
  if (resumeAt <= 0) return;
  elapsedSeconds.value = resumeAt;
  try {
    video.currentTime = resumeAt;
  } catch {
    // 某些浏览器在元数据阶段设置 currentTime 会失败，忽略即可
  }
};

const handleVideoTimeUpdate = async (event) => {
  if (!isActiveVideoEvent(event)) return;
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

const handleVideoPlay = (event) => {
  if (!isActiveVideoEvent(event)) return;
  isPlaying.value = true;
  void startStudySession();
};

const togglePlayback = async () => {
  if (!currentLesson.value || currentLesson.value.completed || !hasVideo.value) return;
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
  if (!isActiveVideoEvent(event)) return;
  isPlaying.value = false;
  await stopStudySession();
  const seconds = Math.floor(event.target.currentTime || 0);
  elapsedSeconds.value = Math.max(elapsedSeconds.value, seconds);
  if (elapsedSeconds.value > 0) {
    await saveCurrentProgress(false, elapsedSeconds.value);
  }
};

const handleVideoEnded = async (event) => {
  if (!isActiveVideoEvent(event)) return;
  isPlaying.value = false;
  await completeCurrentLesson();
  await playNextLesson();
};

const playNextLesson = async () => {
  const { ci, li } = playing.value;
  let nextCi = ci;
  let nextLi = li + 1;
  if (nextLi >= lessonsOf(ci).length) {
    nextCi = ci + 1;
    nextLi = 0;
  }
  if (nextCi >= chapters.value.length) return;
  playing.value = { ci: nextCi, li: nextLi };
  elapsedSeconds.value = Math.floor(Number(lessonsOf(nextCi)[nextLi]?.watchedSeconds || 0));
  videoErrorLessonId = '';
  await nextTick();
  if (videoEl.value) {
    try {
      await videoEl.value.play();
    } catch {

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

const handleVideoError = (event) => {
  if (!isActiveVideoEvent(event)) return;
  isPlaying.value = false;
  void stopStudySession();
  reportVideoLoadError();
};

const completeCurrentLesson = async () => {
  if (!currentLesson.value || currentLesson.value.completed || completionInProgress) return;
  completionInProgress = true;
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

const lessonLabel = (name) => String(name || '').replace(/^\d+\.\d+\s*/, '');

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
  isPlaying.value = false;
  await stopStudySession();
  if (elapsedSeconds.value > 0) {
    await saveCurrentProgress(false, elapsedSeconds.value);
  }
  playing.value = { ci, li };
  videoErrorLessonId = '';
  // 切换课时后，计时基准重置为该课时已观看的时长，避免沿用上一课时的秒数
  elapsedSeconds.value = Math.floor(Number(lessonsOf(ci)[li]?.watchedSeconds || 0));

  await nextTick();
  if (videoEl.value) {
    try {
      await videoEl.value.play();
    } catch {

    }
  }
};

const handleLessonClick = (ci, li) => {
  if (isCurrent(ci, li)) {

    if (hasVideo.value) void togglePlayback();
    return;
  }
  void openLesson(ci, li);
};

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
  if (!playingNow || !currentLesson.value || currentLesson.value.completed) {
    void stopStudySession();
    return;
  }
  void startStudySession();
});

onBeforeUnmount(() => {
  void stopStudySession();
  if (elapsedSeconds.value > 0 && currentLesson.value && !currentLesson.value.completed) {
    saveCurrentProgress(false, elapsedSeconds.value);
  }
});
</script>

<style scoped>

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

.icon { width: 18px; height: 18px; flex-shrink: 0; }
.icon-small { width: 14px; height: 14px; }
.icon-fill { fill: currentColor; }
.icon-blue { color: var(--app-accent-strong); }
.icon-green { color: var(--app-emerald-fg); }

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

.layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 20px;
  align-items: start;
}
/* B 站式两栏：播放器 + 右侧信息/推荐。阈值放在内容区常见宽度，保证侧栏常驻 */
@media (min-width: 760px) {
  .layout { grid-template-columns: minmax(0, 1fr) minmax(260px, 0.42fr); }
  .col-side { position: sticky; top: 16px; align-self: start; }
}

/* 单视频课程：单列满宽，不留空列 */

.col-main, .col-side {
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-width: 0;
}

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

/* ===== B 站风格：播放器下方视频信息 ===== */
.video-info {
  display: flex; flex-direction: column; gap: 14px;
  padding: 4px 2px;
}
.vi-title {
  margin: 0; font-size: 19px; font-weight: 700; line-height: 1.5;
  color: var(--app-heading);
}
.vi-desc {
  margin: 0; font-size: 13px; line-height: 1.8; color: var(--app-text-3);
  display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2;
  -webkit-box-orient: vertical; overflow: hidden;
}
.vi-tags { display: flex; gap: 8px; flex-wrap: wrap; }
.vi-tag {
  padding: 4px 11px; border-radius: 6px;
  background: var(--app-surface-2); color: var(--app-text-3); font-size: 12px;
}

/* 侧栏：单视频课程信息卡 */

/* 侧栏：更多课程推荐（B 站右栏视频卡片） */
.rec-panel { margin-top: 16px; }
.rec-list { display: flex; flex-direction: column; gap: 10px; padding: 4px 0; }
.rec-item { display: flex; gap: 10px; cursor: pointer; padding: 4px; border-radius: 8px; transition: background .16s ease; }
.rec-item:hover { background: var(--app-surface-2); }
.rec-cover { position: relative; width: 128px; flex-shrink: 0; aspect-ratio: 16/9; border-radius: 6px; overflow: hidden; background: var(--app-surface-3); }
.rec-cover img { width: 100%; height: 100%; object-fit: cover; display: block; }
.rec-cover span { position: absolute; right: 4px; bottom: 4px; padding: 1px 5px; border-radius: 3px; background: rgba(0,0,0,.6); color: #fff; font-size: 10px; }
.rec-text { display: flex; flex-direction: column; gap: 5px; min-width: 0; }
.rec-title { font-size: 12.5px; line-height: 1.45; color: var(--app-text); display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.rec-up { font-size: 11px; color: var(--app-text-4); }

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

.bili-episodes {
  max-height: 312px;
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
}
</style>
