<template>
  <div class="crs-page">

    <section class="crs-toolbar" v-reveal aria-label="课程筛选">
      <div class="crs-toolbar__top">
        <div class="crs-toolbar__heading">
          <h1 class="crs-toolbar__title">课程中心</h1>
          <span class="crs-toolbar__result"><span v-count-up="filteredCourses.length"></span> 门课程</span>
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
        <nav ref="categoryNav" class="crs-category-options" aria-label="课程分类">
          <button
            v-for="tab in categoryTabs"
            :key="tab.key"
            type="button"
            class="crs-category-pill"
            :class="{ 'crs-category-pill--active': activeCategory === tab.key }"
            :title="'分类：' + tab.label + '，共 ' + tab.count + ' 门课程'"
            :aria-label="'分类：' + tab.label + '，共 ' + tab.count + ' 门课程'"
            @click="selectCategory(tab.key, $event)"
          >
            <span class="crs-category-pill__label">{{ tab.label }}</span>
            <span class="crs-category-count">{{ tab.count }}</span>
          </button>
        </nav>
      </div>
    </section>

    <div class="crs-grid" v-reveal-rows>
      <article
        v-for="course in filteredCourses"
        :id="`course-card-${course.id}`"
        :key="course.id"
        class="crs-card"
        :class="{ 'crs-card--search-target': searchTargetId === `course-card-${course.id}` }"
      >

        <div class="crs-card__thumb" @click="$emit('open-course', course)">
          <img :src="course.thumbnail" :alt="course.title" class="crs-card__img" loading="lazy" />
        </div>

        <div class="crs-card__body" @click="$emit('open-course', course)">
          <h3 class="crs-card__title">{{ course.title }}</h3>

          <div class="crs-card__meta">
            <span class="crs-card__up">
              <span class="crs-card__avatar">{{ (course.instructor.name || '云').charAt(0) }}</span>
              <span class="crs-card__up-name">{{ course.instructor.name || '云舒艺境' }}</span>
            </span>

          </div>

          <div class="crs-card__tags">
            <span class="crs-card__tag">{{ course.categoryLabel || '视频课程' }}</span>
          </div>
        </div>
      </article>
      <div v-if="!filteredCourses.length" class="crs-empty">
        {{ keyword ? '没有找到匹配的课程' : '暂无该分类下的课程视频' }}
      </div>
    </div>

    <footer class="crs-footer">
      <div>
        显示 {{ filteredCourses.length }} 门课程，课程库共 {{ courses.length }} 门
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed, ref, nextTick, watch } from 'vue';
import { Search, X } from 'lucide-vue-next';
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

// 点击分类后把该标签滚到可见区域中间，像 B 站一样跟随选中项
const scrollCategoryIntoView = (button) => {
  const nav = categoryNav.value;
  if (!nav || !button) return;
  const navBox = nav.getBoundingClientRect();
  const btnBox = button.getBoundingClientRect();
  const targetLeft = nav.scrollLeft + btnBox.left - navBox.left
    - (nav.clientWidth - btnBox.width) / 2;
  nav.scrollTo({ left: Math.max(0, targetLeft), behavior: 'smooth' });
};

const selectCategory = (key, event) => {
  activeCategory.value = key;
  scrollCategoryIntoView(event?.currentTarget);
};

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
  flex: 1 0 auto;
  flex-direction: column;
  gap: 20px;
  min-height: 100%;
  font-family: 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', sans-serif;
}

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
  gap: 6px;
  min-width: 0;
}

.crs-category-options {
  display: flex;
  align-items: center;
  gap: 8px;
  overflow-x: auto;
  overflow-y: hidden;
  min-width: 0;
  flex: 1;
  padding: 2px 2px 6px;
  scrollbar-width: none;
  overscroll-behavior-x: contain;
  scroll-behavior: smooth;
}

.crs-category-options::-webkit-scrollbar {
  display: none;
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
  gap: 6px;
  padding: 8px 14px;
  flex-shrink: 0;
  border: 1px solid transparent;
  border-radius: 999px;
  background: var(--app-surface-3);
  color: var(--c-text-soft);
  font-size: 13px;
  font-weight: 500;
  line-height: 1.2;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease, border-color 0.2s ease;
}

.crs-category-pill:hover {
  background: var(--c-blue-bg);
  color: var(--c-blue);
}

.crs-category-pill--active {
  background: var(--c-blue-bg);
  border-color: var(--app-blue-line-100);
  color: var(--c-blue);
  font-weight: 600;
}

.crs-category-pill__label {
  white-space: nowrap;
}

.crs-category-count {
  color: inherit;
  font-size: 11px;
  font-weight: 600;
  opacity: 0.62;
}

.crs-category-pill--active .crs-category-count {
  opacity: 0.82;
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
    gap: 6px;
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

.crs-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 20px 16px;
}
@media (max-width: 1100px) {
  .crs-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
}
@media (max-width: 700px) {
  .crs-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
@media (max-width: 480px) {
  .crs-grid { grid-template-columns: minmax(0, 1fr); }
}

.crs-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: 6px;
  background: transparent;
  cursor: pointer;
}
.crs-card--search-target .crs-card__thumb {
  box-shadow: 0 0 0 3px var(--c-sky);
}

/* 封面：16:9，圆角，仅轻微上浮，不做整卡高亮 */
.crs-card__thumb {
  position: relative;
  width: 100%;
  /* B 站封面为 16:9，图片本身裁剪成 16:9 后完整铺满、不裁边 */
  aspect-ratio: 16 / 9;
  overflow: hidden;
  border-radius: 6px;
  background: var(--app-ink-900);
  transition: box-shadow 0.22s ease;
}
.crs-card:hover .crs-card__thumb {
  box-shadow: 0 8px 18px rgba(0, 0, 0, 0.16);
}
.crs-card__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.35s ease, filter 0.35s ease;
}
/* hover：封面轻微放大 */
.crs-card:hover .crs-card__img {
  transform: scale(1.04);
}



.crs-card__body {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 6px;
  padding: 9px 2px 0;
}

.crs-card__title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  line-height: 1.45;
  color: var(--c-text);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.2s ease, text-shadow 0.2s ease;
}
/* 只高亮文字：悬停时标题变成明亮主色并加粗发光 */
.crs-card:hover .crs-card__title {
  color: var(--app-accent-strong);
  font-weight: 700;
  text-shadow: 0 0 12px rgba(46, 139, 214, 0.35);
}

/* UP 主一行：头像 + 名称 + 时间 */
.crs-card__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-size: 12px;
  color: var(--c-muted);
}
.crs-card__up {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}
.crs-card__up-name {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.crs-card__avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  flex-shrink: 0;
  border-radius: 50%;
  background: var(--c-blue-bg);
  color: var(--c-blue);
  font-size: 10px;
  font-weight: 700;
}

.crs-card__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 2px;
}
.crs-card__tag {
  padding: 1px 6px;
  border-radius: 3px;
  background: var(--c-blue-bg);
  color: var(--c-blue);
  font-size: 11px;
  line-height: 1.5;
}
.crs-card__tag--plain {
  background: var(--app-surface-3);
  color: var(--c-text-sub);
}

.crs-footer {
  margin-top: auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  background: var(--app-page-bg);
  border-top: 1px solid var(--c-border);
  font-size: 12px;
  color: var(--c-muted);
}

</style>
