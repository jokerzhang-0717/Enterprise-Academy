<template>
  <div class="track-page">

    <!-- ============ 1. 返回按钮 ============ -->
    <button type="button" class="track-back" @click="$emit('back')">
      <span aria-hidden="true">←</span>
      返回首页
    </button>

    <!-- ============ 2. 课程详情卡片 ============ -->
    <article class="track-card">

      <!-- 封面图 -->
      <img
        :src="track.thumbnail"
        :alt="`${track.title}课程照片`"
        class="track-cover"
      />

      <!-- 标题头部 -->
      <header class="track-header">
        <p class="track-header__meta">
          企业精选研修通道 · {{ track.categoryLabel }} · {{ track.level }}课程
        </p>
        <h1 class="track-header__title">{{ track.title }}</h1>
      </header>

      <!-- 课程详细介绍 -->
      <section class="track-section">
        <h2 class="track-section__title">课程详细介绍</h2>
        <p class="track-section__text">{{ track.description }}</p>
      </section>

      <!-- 研修内容 -->
      <section class="track-section">
        <h2 class="track-section__title">研修内容</h2>
        <ol class="track-outline">
          <li v-for="lesson in track.outline" :key="lesson" class="track-outline__item">
            {{ lesson }}
          </li>
        </ol>
      </section>

    </article>
  </div>
</template>

<script setup>
defineProps({
  track: {
    type: Object,
    required: true,
  },
});

defineEmits(['back']);
</script>

<style scoped>
/* ============================================================
   研修通道详情 · 纯手写 CSS
   ------------------------------------------------------------
   目录
     01  页面布局
     02  返回按钮
     03  详情卡片与封面
     04  标题头部
     05  内容分节
     06  研修大纲列表
     07  入场动画
   ============================================================ */

/* ==================== 01 页面布局 ==================== */
.track-page {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;             /* 20px */
  padding-bottom: 3rem;     /* 48px */
}

/* ==================== 02 返回按钮 ==================== */
.track-back {
  display: inline-flex;
  align-items: center;
  align-self: flex-start;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 0.5rem;
  background: #fff;
  font-size: 0.75rem;
  font-weight: 600;
  color: #475569;
  font-family: inherit;
  cursor: pointer;
  transition: background-color 0.2s;
}
.track-back:hover {
  background: #f8fafc;
}

/* ==================== 03 详情卡片与封面 ==================== */
.track-card {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;              /* 24px */
  padding: 1.25rem;         /* 20px */
  border: 1px solid #e2e8f0;
  border-radius: 1rem;      /* 16px */
  background: #fff;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}
@media (min-width: 768px) {
  .track-card { padding: 2rem; }  /* 32px */
}

.track-cover {
  width: 100%;
  height: 13rem;            /* 208px */
  border-radius: 0.75rem;
  object-fit: cover;
}
@media (min-width: 640px) {
  .track-cover { height: 18rem; }  /* 288px */
}

/* ==================== 04 标题头部 ==================== */
.track-header {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding-bottom: 1.25rem;
  border-bottom: 1px solid #f1f5f9;
}
.track-header__meta {
  margin: 0;
  font-size: 0.75rem;
  font-weight: 600;
  color: #005576;
}
.track-header__title {
  margin: 0;
  font-size: 1.25rem;       /* 20px */
  font-weight: 700;
  line-height: 1.625;
  color: #1e293b;
}
@media (min-width: 768px) {
  .track-header__title { font-size: 1.5rem; }  /* 24px */
}

/* ==================== 05 内容分节 ==================== */
.track-section {
  display: flex;
  flex-direction: column;
}
.track-section__title {
  margin: 0 0 0.5rem;
  font-size: 0.875rem;      /* 14px */
  font-weight: 700;
  color: #1e293b;
}
.track-section__text {
  margin: 0;
  font-size: 0.875rem;
  line-height: 1.75rem;     /* leading-7 */
  color: #475569;
}

/* ==================== 06 研修大纲列表 ==================== */
.track-outline {
  margin: 0.75rem 0 0;
  padding-left: 1.25rem;
  list-style: decimal inside;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.track-outline__item {
  font-size: 0.875rem;
  line-height: 1.75rem;
  color: #475569;
}

/* 无障碍：减少动效 */
@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>