import { reactive } from 'vue';
import { fetchCourses } from './api/index.js';

export const learnerStore = reactive({
  courses: [],
  loading: false,
  loaded: false,
});

export const loadLearnerCourses = async (force = false) => {
  if (learnerStore.loading) return learnerStore.courses;
  if (learnerStore.loaded && !force) return learnerStore.courses;
  learnerStore.loading = true;
  try {
    const page = await fetchCourses();
    learnerStore.courses.splice(0, learnerStore.courses.length, ...(page?.records || []));
    learnerStore.loaded = true;
  } finally {
    learnerStore.loading = false;
  }
  return learnerStore.courses;
};

export const resetLearnerStore = () => {
  learnerStore.courses.splice(0);
  learnerStore.loading = false;
  learnerStore.loaded = false;
};
