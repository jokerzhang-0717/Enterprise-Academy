<template>
  <div class="crs-page">
    <!-- 0. 视频课程分类筛选条 -->
    <div class="crs-category-bar">
      <button
        v-for="tab in categoryTabs"
        :key="tab.key"
        type="button"
        class="crs-category-pill"
        :class="{ 'crs-category-pill--active': activeCategory === tab.key }"
        @click="activeCategory = tab.key"
      >
        <span>{{ tab.label }}</span>
        <span class="crs-category-count">{{ tab.count }}</span>
      </button>
    </div>

    <!-- 1. Course Cards Grid -->
    <div class="crs-grid">
      <article
        v-for="course in filteredCourses"
        :id="`course-card-${course.id}`"
        :key="course.id"
        class="crs-card"
      >
        <!-- Thumbnail -->
        <div class="crs-card__thumb">
          <img :src="course.thumbnail" :alt="course.title" class="crs-card__img" />
          <span v-if="course.categoryLabel" class="crs-card__category-tag">
            {{ course.categoryLabel }}
          </span>
        </div>

        <!-- Content -->
        <div class="crs-card__body">
          <div>
            <h3 class="crs-card__title">{{ course.title }}</h3>
            <p class="crs-card__summary">{{ course.summary }}</p>
          </div>

          <div>
            <!-- Instructor -->
            <div class="crs-card__instructor">
              <div class="crs-card__avatar">
                {{ course.instructor.name.charAt(0) }}
              </div>
              <span class="crs-card__instructor-name">
                {{ course.instructor.name }} · {{ course.instructor.title }}
              </span>
            </div>

            <!-- Action -->
            <div class="crs-card__actions">
              <!-- 已完成：重新学习（替代原先温故复习） -->
              <button
                v-if="course.status === 'completed'"
                type="button"
                class="crs-btn crs-btn--review"
                @click="$emit('open-course', course)"
              >
                <Play class="crs-icon crs-icon--sm crs-icon--fill" />
                <span>重新学习</span>
              </button>

              <!-- 未开始：立即学习 -->
              <button
                v-else-if="course.status === 'not_started'"
                type="button"
                class="crs-btn crs-btn--start"
                @click="$emit('open-course', course)"
              >
                <span>立即学习</span>
              </button>

              <!-- 进行中：继续学习 -->
              <button
                v-else
                type="button"
                class="crs-btn crs-btn--continue"
                @click="$emit('open-course', course)"
              >
                <Play class="crs-icon crs-icon--sm crs-icon--fill" />
                <span>继续学习</span>
              </button>
            </div>
          </div>
        </div>
      </article>
      <div v-if="!filteredCourses.length" class="crs-empty">
        暂无该分类下的课程视频
      </div>
    </div>

    <!-- 2. Pagination Footer -->
    <footer class="crs-footer">
      <div>
        当前分类 {{ filteredCourses.length }} 门课程，课程库共 {{ courses.length }} 门
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed, ref, nextTick, watch } from 'vue';
import { Play } from 'lucide-vue-next';
import { adminStore } from '../adminStore.js';

const props = defineProps({
  searchTargetId: {
    type: String,
    default: '',
  },
});
defineEmits(['open-course']);

const activeCategory = ref('all');

/** 与管理端同一份课程数据：新建档并发布后这里就出现，下架即消失 */
const courses = computed(() => adminStore.courses.filter(c => c.published));

const categoryTabs = computed(() => {
  const counts = {};
  courses.value.forEach(c => {
    const cat = c.categoryLabel || '通识课程';
    counts[cat] = (counts[cat] || 0) + 1;
  });
  const tabs = [{ key: 'all', label: '全部视频课程', count: courses.value.length }];
  Object.keys(counts).forEach(key => {
    tabs.push({ key, label: key, count: counts[key] });
  });
  return tabs;
});

const filteredCourses = computed(() => {
  let list = courses.value;
  if (props.searchTargetId) {
    const courseId = props.searchTargetId.replace('course-card-', '');
    list = list.filter(course => course.id === courseId);
  }
  if (activeCategory.value !== 'all') {
    list = list.filter(course => (course.categoryLabel || '通识课程') === activeCategory.value);
  }
  return list;
});

watch(
  () => props.searchTargetId,
  targetId => {
    if (!targetId) return;
    nextTick(() => document.getElementById(targetId)?.scrollIntoView({ behavior: 'smooth', block: 'center' }));
  },
  { immediate: true },
);
</script>

<style scoped>
/* ============ Design tokens ============ */
.crs-page {
  --c-text: #1e293b;
  --c-text-sub: #334155;
  --c-muted: #64748b;
  --c-text-soft: #475569;
  --c-border: #e2e8f0;
  --c-border-soft: #f1f5f9;
  --c-bg-soft: #f8fafc;
  --c-blue: #005576;
  --c-blue-bg: #d9f0ff;
  --c-sky: #83c9f4;
  --c-sky-hover: #6fb9e8;
  --c-purple: #6555b0;

  display: flex;
  flex-direction: column;
  gap: 20px;
  padding-bottom: 48px;
  font-family: 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', sans-serif;
}

/* ============ Category Filter Bar ============ */
.crs-category-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  padding: 12px 16px;
  background: #fff;
  border: 1px solid var(--c-border);
  border-radius: 12px;
}

.crs-category-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  font-size: 13px;
  font-weight: 600;
  color: var(--c-text-soft);
  background: var(--c-border-soft);
  border: 1px solid transparent;
  border-radius: 9999px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.crs-category-pill:hover {
  background: #e2e8f0;
  color: var(--c-text);
}

.crs-category-pill--active {
  background: linear-gradient(135deg, #005576, #003B52) !important;
  color: #fff !important;
  box-shadow: 0 2px 8px rgba(0, 85, 118, 0.25);
}

.crs-category-count {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 9999px;
  background: rgba(0, 0, 0, 0.08);
}
.crs-category-pill--active .crs-category-count {
  background: rgba(255, 255, 255, 0.25);
  color: #fff;
}

.crs-card__category-tag {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 3px 8px;
  font-size: 11px;
  font-weight: 700;
  color: #fff;
  background: rgba(15, 23, 42, 0.7);
  backdrop-filter: blur(4px);
  border-radius: 6px;
}

.crs-empty {
  grid-column: 1 / -1;
  text-align: center;
  padding: 48px 0;
  color: var(--c-muted);
  font-size: 14px;
}

/* ============ Icons (lucide) ============ */
.crs-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}
.crs-icon--sm {
  width: 14px;
  height: 14px;
}
.crs-icon--fill {
  fill: currentColor;
}

/* ============ Grid ============ */
.crs-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
}
@media (min-width: 768px) {
  .crs-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (min-width: 1024px) {
  .crs-grid { grid-template-columns: repeat(3, 1fr); }
}

/* ============ Card ============ */
.crs-card {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  overflow: hidden;
  border: 1px solid var(--c-border);
  border-radius: 12px;
  background: #fff;
  transition: border-color 0.25s ease, box-shadow 0.25s ease;
}
.crs-card:hover {
  border-color: var(--c-sky);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08);
}

/* ---- Thumbnail ---- */
.crs-card__thumb {
  position: relative;
  height: 176px;
  overflow: hidden;
  background: #0f172a;
}
.crs-card__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0.9;
  transition: transform 0.3s ease;
}
.crs-card:hover .crs-card__img {
  transform: scale(1.05);
}

/* ---- Body ---- */
.crs-card__body {
  display: flex;
  flex: 1;
  flex-direction: column;
  justify-content: space-between;
  gap: 16px;
  padding: 16px;
}

.crs-card__title {
  margin: 0 0 8px;
  font-size: 14px;
  font-weight: 700;
  line-height: 1.4;
  color: var(--c-text);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: color 0.2s ease;
}
.crs-card:hover .crs-card__title {
  color: var(--c-blue);
}

.crs-card__summary {
  margin: 0 0 16px;
  font-size: 12px;
  line-height: 1.625;
  color: var(--c-muted);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* ---- Instructor ---- */
.crs-card__instructor {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid var(--c-border-soft);
  font-size: 12px;
}
.crs-card__avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  flex-shrink: 0;
  border-radius: 50%;
  background: var(--c-blue-bg);
  color: var(--c-blue);
  font-size: 10px;
  font-weight: 700;
}
.crs-card__instructor-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  font-size: 12px;
  font-weight: 500;
  color: var(--c-text-sub);
}
.crs-card__cert {
  flex-shrink: 0;
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 10px;
  color: var(--c-purple);
}

/* ---- Action buttons ---- */
.crs-card__actions {
  margin-top: 12px;
}
.crs-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 100%;
  padding: 8px 0;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s ease, border-color 0.2s ease;
}

/* 已完成：温故复习 */
.crs-btn--review {
  border: 1px solid var(--c-border);
  background: var(--c-bg-soft);
  color: var(--c-text-soft);
}
.crs-btn--review:hover {
  background: var(--c-border-soft);
}

/* 未开始：立即学习 */
.crs-btn--start {
  border: 1px solid rgba(131, 201, 244, 0.4);
  background: var(--c-blue-bg);
  color: var(--c-blue);
}
.crs-btn--start:hover {
  border-color: var(--c-sky);
}

/* 进行中：继续学习 */
.crs-btn--continue {
  border: 1px solid transparent;
  background: var(--c-sky);
  color: #fff;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.05);
}
.crs-btn--continue:hover {
  background: var(--c-sky-hover);
}

/* ============ Footer ============ */
.crs-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid var(--c-border);
  font-size: 12px;
  color: var(--c-muted);
}
</style>