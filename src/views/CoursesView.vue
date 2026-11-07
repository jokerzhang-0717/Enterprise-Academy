<template>
  <div class="crs-page">
    <!-- 0. 视频课程分类筛选条 -->
    <section class="crs-toolbar" aria-label="课程筛选">
      <div class="crs-toolbar__top">
        <div class="crs-toolbar__heading">
          <h1 class="crs-toolbar__title">课程中心</h1>
          <span class="crs-toolbar__result">{{ filteredCourses.length }} 门课程</span>
        </div>
        <label class="crs-search">
          <Search class="crs-search__icon" aria-hidden="true" />
          <input
            v-model.trim="keyword"
            type="text"
            placeholder="搜索课程名称、讲师或内容"
            aria-label="搜索课程名称、讲师或内容"
          />
          <button
            v-if="keyword"
            type="button"
            class="crs-search__clear"
            aria-label="清除搜索内容"
            title="清除搜索内容"
            @click="keyword = ''"
          >
            <X class="crs-search__clear-icon" aria-hidden="true" />
          </button>
        </label>
      </div>
      <div class="crs-category-rail">
        <button
          v-if="showCategoryArrows"
          type="button"
          class="crs-category-arrow"
          :disabled="!canScrollCategoryLeft"
          aria-label="向左移动分类"
          @click="scrollCategories(-1)"
        >
          <ArrowLeft class="crs-category-arrow__icon" aria-hidden="true" />
        </button>
        <nav ref="categoryNav" class="crs-category-options" aria-label="课程分类" @scroll="updateCategoryScroll">
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
        </nav>
        <button
          v-if="showCategoryArrows"
          type="button"
          class="crs-category-arrow"
          :disabled="!canScrollCategoryRight"
          aria-label="向右移动分类"
          @click="scrollCategories(1)"
        >
          <ArrowRight class="crs-category-arrow__icon" aria-hidden="true" />
        </button>
      </div>
    </section>

    <!-- 1. Course Cards Grid -->
    <div class="crs-grid">
      <article
        v-for="course in filteredCourses"
        :id="`course-card-${course.id}`"
        :key="course.id"
        class="crs-card"
        :class="{ 'crs-card--search-target': searchTargetId === `course-card-${course.id}` }"
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
              <!-- 未开始：立即学习 -->
              <button
                v-if="course.status === 'not_started'"
                type="button"
                class="crs-btn crs-btn--start"
                @click="$emit('open-course', course)"
              >
                <span>立即学习</span>
              </button>

              <!-- 进行中 / 已结课：继续学习 -->
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
        {{ keyword ? '没有找到匹配的课程' : '暂无该分类下的课程视频' }}
      </div>
    </div>

    <!-- 2. Pagination Footer -->
    <footer class="crs-footer">
      <div>
        显示 {{ filteredCourses.length }} 门课程，课程库共 {{ courses.length }} 门
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, nextTick, watch } from 'vue';
import { ArrowLeft, ArrowRight, Play, Search, X } from 'lucide-vue-next';
import { learnerStore } from '../learnerStore.js';

const props = defineProps({
  searchTargetId: {
    type: String,
    default: '',
  },
});
defineEmits(['open-course']);

const activeCategory = ref('all');
const keyword = ref('');
const categoryNav = ref(null);
const showCategoryArrows = ref(false);
const canScrollCategoryLeft = ref(false);
const canScrollCategoryRight = ref(false);
let categoryResizeObserver;

/** 学员端只看到已上架课程；管理端下架后这里跟着消失 */
const courses = computed(() => learnerStore.courses.filter(c => c.published));

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

const updateCategoryScroll = () => {
  const nav = categoryNav.value;
  if (!nav) return;
  const maxScroll = nav.scrollWidth - nav.clientWidth;
  showCategoryArrows.value = maxScroll > 1;
  canScrollCategoryLeft.value = nav.scrollLeft > 1;
  canScrollCategoryRight.value = nav.scrollLeft < maxScroll - 1;
};

const scrollCategories = direction => {
  const nav = categoryNav.value;
  if (!nav) return;
  nav.scrollBy({ left: direction * Math.max(nav.clientWidth * 0.7, 160), behavior: 'smooth' });
};

onMounted(() => {
  nextTick(() => {
    updateCategoryScroll();
    categoryResizeObserver = new ResizeObserver(updateCategoryScroll);
    if (categoryNav.value) categoryResizeObserver.observe(categoryNav.value);
  });
});

onBeforeUnmount(() => categoryResizeObserver?.disconnect());

watch(categoryTabs, () => nextTick(updateCategoryScroll), { flush: 'post' });

const filteredCourses = computed(() => {
  let list = courses.value;
  if (props.searchTargetId && !keyword.value) {
    const courseId = props.searchTargetId.replace('course-card-', '');
    list = list.filter(course => course.id === courseId);
  }
  const query = keyword.value.toLocaleLowerCase();
  if (query) {
    list = list.filter(course => [
      course.title,
      course.summary,
      course.categoryLabel,
      course.instructor?.name,
      course.instructor?.title,
    ].some(value => value?.toLocaleLowerCase().includes(query)));
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
    keyword.value = '';
    activeCategory.value = 'all';
    nextTick(() => document.getElementById(targetId)?.scrollIntoView({ behavior: 'smooth', block: 'center' }));
  },
  { immediate: true },
);
</script>

<style scoped>
/* ============ Design tokens ============ */
.crs-page {
  --c-text: var(--app-text);
  --c-text-sub: var(--app-text-2);
  --c-muted: var(--app-text-4);
  --c-text-soft: var(--app-text-3);
  --c-border: var(--app-border);
  --c-border-soft: var(--app-surface-3);
  --c-bg-soft: var(--app-surface-2);
  --c-blue: var(--app-accent-deep);
  --c-blue-bg: var(--app-accent-soft);
  --c-sky: var(--app-accent);
  --c-sky-hover: var(--app-accent-hover-strong);
  --c-purple: var(--app-violet-fg);

  display: flex;
  flex-direction: column;
  gap: 20px;
  padding-bottom: 48px;
  font-family: 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', sans-serif;
}

/* ============ Course search and categories ============ */
.crs-toolbar {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 4px 0 0;
}

.crs-toolbar__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.crs-toolbar__heading {
  display: flex;
  align-items: baseline;
  gap: 10px;
  min-width: 0;
}

.crs-toolbar__title {
  margin: 0;
  color: var(--c-text);
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.crs-toolbar__result {
  flex-shrink: 0;
  color: var(--c-muted);
  font-size: 12px;
}

.crs-category-rail {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.crs-category-options {
  display: flex;
  align-items: center;
  gap: 22px;
  overflow-x: auto;
  overflow-y: hidden;
  min-width: 0;
  flex: 1;
  padding: 0 2px;
  border-bottom: 1px solid var(--c-border);
  scrollbar-width: none;
  overscroll-behavior-x: contain;
  scroll-behavior: smooth;
}

.crs-category-options::-webkit-scrollbar {
  display: none;
}

.crs-category-arrow {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  flex: 0 0 30px;
  border: 1px solid var(--c-border);
  border-radius: 50%;
  background: var(--app-surface);
  color: var(--c-text-soft);
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease, opacity 0.2s ease;
}

.crs-category-arrow:hover:not(:disabled) {
  background: var(--c-blue-bg);
  color: var(--c-blue);
}

.crs-category-arrow:disabled {
  cursor: default;
  opacity: 0.4;
}

.crs-category-arrow__icon {
  width: 16px;
  height: 16px;
}

.crs-search {
  display: flex;
  align-items: center;
  gap: 8px;
  width: min(100%, 360px);
  min-width: 240px;
  height: 42px;
  flex-shrink: 0;
  padding: 0 14px;
  border: 1px solid var(--app-blue-line-100);
  border-radius: 11px;
  background: var(--app-surface);
  color: var(--c-muted);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.crs-search:focus-within {
  border-color: var(--c-sky);
  box-shadow: 0 0 0 3px rgba(131, 201, 244, 0.18);
}

.crs-search__icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.crs-search input {
  width: 100%;
  min-width: 0;
  border: 0;
  outline: 0;
  background: transparent;
  color: var(--c-text);
  font: inherit;
  font-size: 13px;
}

.crs-search input::placeholder {
  color: var(--app-text-5);
}

.crs-search__clear {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  flex-shrink: 0;
  border: 0;
  border-radius: 50%;
  background: transparent;
  color: var(--app-text-4);
  cursor: pointer;
}

.crs-search__clear:hover {
  background: var(--app-surface-3);
  color: var(--app-text);
}

.crs-search__clear-icon {
  width: 14px;
  height: 14px;
}

.crs-category-pill {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 10px 2px 11px;
  flex-shrink: 0;
  font-size: 13px;
  font-weight: 500;
  color: var(--c-muted);
  background: transparent;
  border: 0;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  cursor: pointer;
  transition: color 0.2s ease, border-color 0.2s ease;
}

.crs-category-pill:hover {
  color: var(--c-text);
}

.crs-category-pill--active {
  border-bottom-color: var(--c-blue);
  color: var(--c-blue);
  font-weight: 700;
}

.crs-category-count {
  font-size: 11px;
  color: var(--app-text-5);
}

@media (max-width: 720px) {
  .crs-toolbar__top {
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }

  .crs-search {
    width: 100%;
    min-width: 0;
  }

  .crs-category-rail {
    width: 100%;
    gap: 18px;
  }
}

.crs-card__category-tag {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 3px 8px;
  font-size: 11px;
  font-weight: 700;
  color: var(--app-on-scrim);
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
  background: var(--app-surface);
  transition: border-color 0.25s ease, box-shadow 0.25s ease;
}
.crs-card:hover {
  border-color: var(--c-sky);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08);
}
.crs-card--search-target {
  border-color: var(--c-sky);
  box-shadow: 0 0 0 3px var(--app-accent-soft), 0 8px 20px rgba(15, 23, 42, 0.08);
}

/* ---- Thumbnail ---- */
.crs-card__thumb {
  position: relative;
  height: 176px;
  overflow: hidden;
  background: var(--app-ink-900);
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
  color: var(--app-on-fill);
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