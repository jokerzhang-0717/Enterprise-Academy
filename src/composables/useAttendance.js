import { ref } from 'vue';
import {
  checkIn as checkInApi,
  checkOut as checkOutApi,
  fetchTodayCheckin,
} from '../api/index.js';

const isCheckedIn = ref(false);
const isCheckedOut = ref(false);
const checkedInAt = ref('');
const checkedOutAt = ref('');
const attendanceDate = ref('');
const attendanceLoading = ref(false);

const formatTime = (time) => {
  if (!time) return '';
  // 如果是字符串，直接返回
  if (typeof time === 'string') return time;
  // 如果是对象（LocalTime），格式化为 HH:mm:ss
  if (typeof time === 'object' && time !== null) {
    const hours = String(time.hours || 0).padStart(2, '0');
    const minutes = String(time.minutes || 0).padStart(2, '0');
    const seconds = String(time.seconds || 0).padStart(2, '0');
    return `${hours}:${minutes}:${seconds}`;
  }
  return String(time);
};

const applyToday = (data) => {
  isCheckedIn.value = Boolean(data?.checkedIn);
  isCheckedOut.value = Boolean(data?.checkedOut);
  attendanceDate.value = data?.date || '';
  const record = data?.record || {};
  checkedInAt.value = formatTime(record.checkInTime);
  checkedOutAt.value = formatTime(record.checkOutTime);
};

export const loadAttendance = async () => {
  attendanceLoading.value = true;
  try {
    applyToday(await fetchTodayCheckin());
  } finally {
    attendanceLoading.value = false;
  }
};

export const resetAttendance = () => {
  isCheckedIn.value = false;
  isCheckedOut.value = false;
  checkedInAt.value = '';
  checkedOutAt.value = '';
  attendanceDate.value = '';
};

export const checkIn = async () => {
  if (isCheckedIn.value) return false;
  const record = await checkInApi();
  isCheckedIn.value = true;
  checkedInAt.value = formatTime(record?.checkInTime);
  isCheckedOut.value = Boolean(record?.checkOutTime);
  checkedOutAt.value = formatTime(record?.checkOutTime);
  return true;
};

export const checkOut = async () => {
  const record = await checkOutApi();
  isCheckedOut.value = true;
  checkedOutAt.value = formatTime(record?.checkOutTime);
  return true;
};

export const useAttendance = () => ({
  isCheckedIn,
  isCheckedOut,
  checkedInAt,
  checkedOutAt,
  attendanceDate,
  attendanceLoading,
  loadAttendance,
  checkIn,
  checkOut,
});
