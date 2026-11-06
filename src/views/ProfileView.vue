<template>
  <div class="profile-page" :class="user?.role === 'admin' ? 'profile-page--admin' : 'profile-page--user'">

    <!-- ============ 1. 面包屑导航 ============ -->
    <header class="profile-crumb">
      <span class="profile-crumb__item">{{ user?.role === 'admin' ? '管理控制台' : '企业赋能平台' }}</span>
      <span class="profile-crumb__sep">/</span>
      <span class="profile-crumb__item is-current">{{ user?.role === 'admin' ? '管理员个人信息' : '个人中心' }}</span>
    </header>

    <!-- ============ 2. 个人档案横幅 ============ -->
    <section class="profile-hero">
      <div class="profile-orb profile-orb-one" />
      <div class="profile-orb profile-orb-two" />

      <div class="profile-hero__inner">
        <!-- 头像:整块可点击换头像 -->
        <button
          type="button"
          class="profile-avatar"
          aria-label="更换头像"
          @click="avatarInput?.click()"
        >
          <img v-if="form.avatarUrl" :src="form.avatarUrl" alt="个人头像" class="profile-avatar__img" />
          <span v-else class="profile-avatar__letter">{{ user.name?.charAt(0) || '用' }}</span>
        </button>

        <!-- 姓名 / 部门 / 标签 -->
        <div class="profile-hero__info">
          <p class="profile-hero__label">{{ user?.role === 'admin' ? '管理员资料' : '个人档案' }}</p>
          <h2 class="profile-hero__name">{{ user.name }}</h2>
          <p class="profile-hero__dept">{{ user.department }}</p>
          <div class="profile-hero__tags">
            <span v-if="user.employeeId" class="profile-hero__tag">工号 {{ user.employeeId }}</span>
            <span v-if="user.title" class="profile-hero__tag">职位名称：{{ user.title }}</span>
          </div>
        </div>

        <!-- 学习统计 / 管理员概览 -->
        <div class="profile-hero__stats" v-if="user?.role === 'admin'">
          <div class="profile-stat">
            <p class="profile-stat__label">管理权限</p>
            <p class="profile-stat__value">超级管理</p>
          </div>
          <div class="profile-stat">
            <p class="profile-stat__label">系统权限</p>
            <p class="profile-stat__value">全量可用</p>
          </div>
        </div>
        <div class="profile-hero__stats profile-hero__stats--single" v-else>
          <div class="profile-stat">
            <p class="profile-stat__label">累计学时</p>
            <p class="profile-stat__value">{{ user.learningHours ?? 0 }}<span class="profile-stat__unit">小时</span></p>
          </div>
        </div>
      </div>
    </section>

    <!-- ============ 3. 基本资料表单 ============ -->
    <form class="profile-card" novalidate @submit.prevent="saveProfile">
      <!-- 头像隐藏上传 -->
      <input ref="avatarInput" type="file" accept="image/*" class="profile-hidden" @change="handleAvatarChange" />
      <p v-if="avatarError" role="alert" class="profile-alert profile-alert--error">{{ avatarError }}</p>

      <!-- 卡片头 -->
      <div class="profile-card__head">
        <div class="profile-card__title">
          <span class="profile-card__icon profile-card__icon--sky">
            <UserRound class="profile-icon" />
          </span>
          <div>
            <h3 class="profile-card__name">基本资料</h3>
            <p class="profile-card__desc">修改后保存，即可同步到工作台个人信息</p>
          </div>
        </div>

        <div class="profile-card__aside">
          <!-- 安全提示 -->
          <section class="profile-tip">
            <span class="profile-tip__icon">
              <ShieldCheck class="profile-icon" />
            </span>
            <div>
              <h4 class="profile-tip__title">资料安全提示</h4>
              <p class="profile-tip__text">工号由企业统一维护，请确保资料准确。</p>
            </div>
          </section>

          <span v-if="saved" role="status" class="profile-saved">
            <Check class="profile-icon profile-icon--xs" /> 已保存
          </span>
        </div>
      </div>

      <!-- 基本信息字段 -->
      <div class="profile-grid">
        <label class="profile-field">
          <span>姓名 <b>*</b></span>
          <input v-model.trim="form.name" required maxlength="30" autocomplete="name" placeholder="请输入姓名" />
        </label>
        <label class="profile-field">
          <span>工号</span>
          <input :value="user.employeeId" disabled />
          <small>工号由企业统一分配，不可修改</small>
        </label>
        <label class="profile-field">
          <span>所属部门 <b>*</b></span>
          <input v-model.trim="form.department" required maxlength="50" autocomplete="organization" placeholder="请输入所属部门" />
        </label>
        <label class="profile-field">
          <span>职位名称 <b>*</b></span>
          <input v-model.trim="form.title" required maxlength="50" autocomplete="organization-title" placeholder="请输入职位名称" />
        </label>
      </div>

      <!-- 联系与个人信息 -->
      <div class="profile-block">
        <h4 class="profile-block__title">联系与个人信息</h4>
        <p class="profile-block__desc">可选填写，用于完善个人档案</p>

        <div class="profile-grid profile-grid--spaced">
          <label class="profile-field">
            <span>性别</span>
            <select v-model="form.gender" autocomplete="sex">
              <option value="">请选择</option>
              <option value="女">女</option>
              <option value="男">男</option>
            </select>
          </label>
          <label class="profile-field">
            <span>年龄</span>
            <input v-model.trim="form.age" type="text" inputmode="numeric" maxlength="3" placeholder="请输入年龄" />
          </label>
          <label class="profile-field">
            <span>手机号</span>
            <input
              ref="phoneInput"
              v-model.trim="form.phone"
              type="tel"
              inputmode="numeric"
              autocomplete="tel"
              pattern="[0-9]{11}"
              maxlength="11"
              title="手机号必须为11位数字"
              placeholder="请输入11位手机号"
              :aria-invalid="Boolean(phoneError)"
              @blur="validatePhone"
            />
            <small v-if="phoneError" role="alert" class="profile-field__error">{{ phoneError }}</small>
          </label>
          <label class="profile-field">
            <span>邮箱</span>
            <input
              ref="emailInput"
              v-model.trim="form.email"
              type="email"
              maxlength="254"
              autocomplete="email"
              title="请输入有效的邮箱地址"
              placeholder="请输入有效邮箱地址"
              :aria-invalid="Boolean(emailError)"
              @blur="validateEmail"
            />
            <small v-if="emailError" role="alert" class="profile-field__error">{{ emailError }}</small>
          </label>
        </div>
      </div>

      <!-- 底部提交 -->
      <div class="profile-card__foot">
        <p class="profile-note">带 <b class="profile-note__star">*</b> 的项目为必填信息</p>
        <button type="submit" class="profile-btn profile-btn--primary">
          <Save class="profile-icon" />
          保存设置
        </button>
      </div>
    </form>

    <!-- ============ 4. 修改密码 ============ -->
    <section class="profile-card profile-card--password">
      <!-- 卡片头 -->
      <div class="profile-card__head">
        <div class="profile-card__title">
          <span class="profile-card__icon profile-card__icon--violet">
            <LockKeyhole class="profile-icon" />
          </span>
          <div>
            <h3 class="profile-card__name">修改密码</h3>
            <p class="profile-card__desc">定期更新密码有助于保护账户安全</p>
          </div>
        </div>
        <span class="profile-badge-amber">需验证当前密码</span>
      </div>

      <!-- 密码表单 -->
      <form class="profile-grid profile-grid--spaced profile-grid--password" @submit.prevent="submitPasswordChange">
        <label class="profile-field profile-field--full">
          <span>当前密码</span>
          <input v-model="passwordForm.current" type="password" autocomplete="current-password" required maxlength="128" placeholder="请输入当前密码" />
        </label>
        <label class="profile-field">
          <span>新密码</span>
          <input v-model="passwordForm.next" type="password" autocomplete="new-password" required minlength="8" maxlength="128" placeholder="至少 8 位" />
        </label>
        <label class="profile-field">
          <span>确认新密码</span>
          <input v-model="passwordForm.confirm" type="password" autocomplete="new-password" required minlength="8" maxlength="128" placeholder="再次输入新密码" />
        </label>

        <div class="profile-password__foot">
          <p class="profile-note profile-note--wrap">密码修改需要由身份认证服务验证并保存；此演示界面仅提供表单校验。</p>
          <button type="submit" class="profile-btn profile-btn--violet">
            <KeyRound class="profile-icon" />
            提交修改
          </button>
        </div>

        <p v-if="passwordError" role="alert" class="profile-field__error profile-field__error--block">{{ passwordError }}</p>
        <p v-if="passwordNotice" role="status" class="profile-alert profile-alert--warn">{{ passwordNotice }}</p>
      </form>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue';
import { Check, KeyRound, LockKeyhole, Save, ShieldCheck, UserRound } from 'lucide-vue-next';

const props = defineProps({
  user: {
    type: Object,
    required: true,
  },
});

const emit = defineEmits(['update-user']);
const saved = ref(false);
const avatarError = ref('');
const avatarInput = ref(null);
const phoneInput = ref(null);
const emailInput = ref(null);
const phoneError = ref('');
const emailError = ref('');
const passwordError = ref('');
const passwordNotice = ref('');
const passwordForm = reactive({
  current: '',
  next: '',
  confirm: '',
});
const form = reactive({
  name: '',
  department: '',
  title: '',
  gender: '',
  age: '',
  phone: '',
  email: '',
  avatarUrl: '',
});

/* 单向同步：props.user -> form，不会回写 props，无闭环风险 */
watch(
  () => props.user,
  (user) => {
    form.name = user.name || '';
    form.department = user.department || '';
    form.title = user.title || '';
    form.gender = user.gender || '';
    form.age = user.age ?? '';
    form.phone = user.phone || '';
    form.email = user.email || '';
    form.avatarUrl = user.avatarUrl || '';
    saved.value = false;
  },
  { immediate: true },
);

const handleAvatarChange = (event) => {
  const file = event.target.files?.[0];
  avatarError.value = '';
  if (!file) return;

  if (!file.type.startsWith('image/')) {
    avatarError.value = '请选择图片文件';
    event.target.value = '';
    return;
  }
  if (file.size > 2 * 1024 * 1024) {
    avatarError.value = '图片大小不能超过 2 MB';
    event.target.value = '';
    return;
  }

  const reader = new FileReader();
  reader.onload = () => {
    if (typeof reader.result !== 'string') {
      avatarError.value = '无法读取该图片，请重新选择';
      return;
    }
    form.avatarUrl = reader.result;
    saved.value = false;
  };
  reader.onerror = () => {
    avatarError.value = '图片读取失败，请重试';
  };
  reader.readAsDataURL(file);
};

const saveProfile = () => {
  validatePhone();
  validateEmail();
  if (phoneError.value || emailError.value) {
    (phoneError.value ? phoneInput.value : emailInput.value)?.focus();
    return;
  }

  emit('update-user', { ...form });
  saved.value = true;
};

const validatePhone = () => {
  phoneError.value = form.phone && !/^\d{11}$/.test(form.phone)
    ? '手机号格式不正确，请输入11位数字'
    : '';
};

const validateEmail = () => {
  emailError.value = form.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)
    ? '邮箱格式不正确，请检查邮箱地址'
    : '';
};

const submitPasswordChange = () => {
  passwordError.value = '';
  passwordNotice.value = '';

  if (passwordForm.next.length < 8) {
    passwordError.value = '新密码至少需要 8 位';
    return;
  }
  if (passwordForm.next === passwordForm.current) {
    passwordError.value = '新密码不能与当前密码相同';
    return;
  }
  if (passwordForm.next !== passwordForm.confirm) {
    passwordError.value = '两次输入的新密码不一致';
    return;
  }

  passwordForm.current = '';
  passwordForm.next = '';
  passwordForm.confirm = '';
  passwordNotice.value = '表单校验通过；当前项目尚未接入身份认证服务，登录密码未实际修改。';
};
</script>

<style scoped>
/* ============================================================
   个人中心 · 纯手写 CSS
   ------------------------------------------------------------
   目录
     01  页面布局与工具类        06  表单卡片
     02  图标                    07  表单字段
     03  面包屑                  08  修改密码
     04  档案横幅                09  按钮 / 提示
     05  头像与统计              10  隐藏元素
   ============================================================ */

/* ==================== 01 页面布局 ==================== */
.profile-page {
  max-width: 72rem;         /* max-w-6xl */
  width: 100%;
  margin-left: auto;
  margin-right: auto;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;              /* 24px */
  padding-bottom: 3rem;     /* 48px */
}

@media (min-width: 1100px) {
  :fullscreen #app .profile-page--user {
    max-width: 96rem;
    gap: 1.25rem;
  }
}

/* ==================== 02 图标 (lucide) ==================== */
.profile-icon {
  width: 1rem;
  height: 1rem;
  flex-shrink: 0;
}
.profile-icon--xs {
  width: 0.875rem;
  height: 0.875rem;
}

/* ==================== 03 面包屑 ==================== */
.profile-crumb {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
  color: var(--app-text-4);
}
.profile-crumb__item.is-current {
  font-weight: 600;
  color: var(--app-accent-deep);
}
.profile-crumb__sep {
  color: var(--app-text-6);
}

/* ==================== 04 档案横幅 ==================== */
.profile-hero {
  position: relative;
  isolation: isolate;
  overflow: hidden;
  border-radius: 1rem;      /* 16px */
  padding: 1.5rem;          /* 24px */
  color: var(--app-hero-ink);
  background-image: linear-gradient(112deg, var(--app-hero-1) 0%, var(--app-hero-2) 58%, var(--app-hero-3) 100%);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}
@media (min-width: 640px) {
  .profile-hero { padding: 2rem; }
}

/* 装饰光斑 */
.profile-orb {
  position: absolute;
  z-index: -1;
  border: 1px solid var(--app-hero-orb-line);
  border-radius: 9999px;
  pointer-events: none;
}
.profile-orb-one {
  width: 340px;
  height: 340px;
  top: -210px;
  right: 12%;
  box-shadow: 0 0 0 35px var(--app-hero-orb-ring-a), 0 0 0 75px var(--app-hero-orb-ring-b);
}
.profile-orb-two {
  width: 180px;
  height: 180px;
  bottom: -130px;
  right: -25px;
  background: var(--app-hero-violet-wash);
}

.profile-hero__inner {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}
@media (min-width: 640px) {
  .profile-hero__inner {
    flex-direction: row;
    align-items: center;
  }
}

/* ---- 05 头像 ---- */
.profile-avatar {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 5rem;              /* 80px */
  height: 5rem;
  flex-shrink: 0;
  overflow: hidden;
  padding: 0;
  border: 1px solid var(--app-hero-glass-line);
  border-radius: 1rem;
  background: var(--app-hero-glass-strong);
  font: inherit;
  font-size: 1.875rem;
  font-weight: 700;
  color: inherit;
  cursor: pointer;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.06);
  backdrop-filter: blur(4px);
}
.profile-avatar__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* ---- 姓名信息 ---- */
.profile-hero__info {
  min-width: 0;
  flex: 1;
}
.profile-hero__label {
  font-size: 0.75rem;
  font-weight: 500;
  letter-spacing: 0.025em;
  color: var(--app-hero-ink-75);
}
.profile-hero__name {
  margin: 0.25rem 0 0;
  font-size: 1.5rem;        /* 24px */
  font-weight: 700;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.profile-hero__dept {
  margin: 0.25rem 0 0;
  font-size: 0.875rem;
  color: var(--app-hero-ink-85);
}
.profile-hero__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.75rem;
}
.profile-hero__tag {
  padding: 0.25rem 0.625rem;
  border: 1px solid var(--app-hero-glass-line);
  border-radius: 0.375rem;
  background: var(--app-hero-glass);
  font-size: 0.75rem;
  color: var(--app-hero-ink);
}

/* ---- 学习统计 ---- */
.profile-hero__stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  padding: 0.25rem 0;
  border: 1px solid var(--app-hero-glass-line);
  border-radius: 0.75rem;
  background: var(--app-hero-glass);
  backdrop-filter: blur(4px);
}
@media (min-width: 640px) {
  .profile-hero__stats { min-width: 250px; }
}
/* 学员端只剩一项统计:收成单列,否则右半格是空白 */
.profile-hero__stats.profile-hero__stats--single {
  grid-template-columns: 1fr;
  min-width: 0;
}
.profile-stat {
  padding: 0 1rem;
}
.profile-stat + .profile-stat {
  border-left: 1px solid var(--app-hero-stat-div);
}
.profile-stat__label {
  font-size: 0.75rem;
  color: var(--app-hero-ink-75);
}
.profile-stat__value {
  margin-top: 0.25rem;
  font-size: 1.25rem;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}
.profile-stat__unit {
  margin-left: 0.25rem;
  font-size: 0.75rem;
  font-weight: 500;
  color: var(--app-hero-ink-80);
}

/* ==================== 06 表单卡片 ==================== */
.profile-card {
  padding: 1.25rem;         /* 20px */
  border: 1px solid var(--app-border-soft);
  border-radius: 1rem;
  background: var(--app-surface);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}
@media (min-width: 640px) {
  .profile-card { padding: 1.75rem; }
}
.profile-card--password {
  margin-top: 1.25rem;
}

.profile-card__foot {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid var(--app-border-soft);
}
.profile-card__foot .profile-note {
  margin: 0;
}
.profile-card__foot .profile-btn {
  margin-left: auto;
}

.profile-card__head {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  padding-bottom: 1.25rem;
  border-bottom: 1px solid var(--app-border-soft);
}
.profile-card__title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  min-width: 220px;
}
.profile-card__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2rem;
  height: 2rem;
  border-radius: 0.5rem;
}
.profile-card__icon--sky {
  background: var(--app-blue-50);
  color: var(--app-accent-ink);
}
.profile-card__icon--violet {
  background: var(--app-violet-50);
  color: var(--app-violet-deep);
}
.profile-card__name {
  margin: 0;
  font-size: 0.9375rem;
  font-weight: 700;
  color: var(--app-ink-800);
}
.profile-card__desc {
  margin: 0.5rem 0 0;
  font-size: 0.75rem;
  color: var(--app-slate-400);
}

.profile-card__aside {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 1rem;
}

/* ---- 安全提示小卡 ---- */
.profile-tip {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  max-width: 360px;
  padding: 0.75rem 1rem;
  border: 1px solid var(--app-blue-line-200);
  border-radius: 0.75rem;
  background: var(--app-note-wash);
}
.profile-tip__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2rem;
  height: 2rem;
  flex-shrink: 0;
  border-radius: 0.5rem;
  background: var(--app-surface);
  color: var(--app-accent-ink);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}
.profile-tip__title {
  margin: 0;
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--app-ink-700);
}
.profile-tip__text {
  margin: 0.25rem 0 0;
  font-size: 0.6875rem;
  line-height: 1rem;
  color: var(--app-slate-500);
}

/* ---- 已保存状态 ---- */
.profile-saved {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.75rem;
  font-weight: 500;
  color: var(--app-emerald-fg);
}

/* ---- 字段栅格 ---- */
.profile-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1rem 1.25rem;
  padding-top: 1.25rem;
}
@media (min-width: 640px) {
  .profile-grid { grid-template-columns: 1fr 1fr; }
}
.profile-grid--spaced {
  padding-top: 0;
  margin-top: 1rem;
}
.profile-grid--password {
  padding-top: 1.25rem;
}

/* ---- 分组块 ---- */
.profile-block {
  margin-top: 1.25rem;
  padding-top: 1.25rem;
  border-top: 1px solid var(--app-border-soft);
}
.profile-block__title {
  margin: 0;
  font-size: 0.875rem;
  font-weight: 700;
  color: var(--app-text-2);
}
.profile-block__desc {
  margin: 0.25rem 0 0;
  font-size: 0.75rem;
  color: var(--app-slate-400);
}

/* ==================== 07 表单字段 ==================== */
.profile-field {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 0.4rem;
}
.profile-field--full {
  grid-column: 1 / -1;
}
.profile-field > span {
  color: var(--app-text-3);
  font-size: 0.75rem;
  font-weight: 600;
}
.profile-field > span b {
  color: var(--app-rose-500);
}
.profile-field small {
  font-size: 0.6875rem;
  color: var(--app-text-5);
}
.profile-field__error {
  color: var(--app-rose-600) !important;
  font-size: 0.75rem;
  font-weight: 500;
}
.profile-field__error--block {
  grid-column: 1 / -1;
  margin: 0;
}

.profile-field input,
.profile-field select {
  width: 100%;
  min-height: 2.625rem;
  border: 1px solid var(--app-border);
  border-radius: 0.5rem;
  background: var(--app-surface);
  padding: 0.625rem 0.75rem;
  color: var(--app-ink-800);
  font-size: 0.8125rem;
  font-family: inherit;
  outline: none;
  transition: border-color 150ms, box-shadow 150ms, background-color 150ms;
  box-sizing: border-box;
}
.profile-field input::placeholder {
  color: var(--app-slate-300);
}
.profile-field input:focus,
.profile-field select:focus {
  border-color: var(--app-accent-focus);
  box-shadow: 0 0 0 3px rgb(56 166 212 / 13%);
}
.profile-field input:disabled {
  cursor: not-allowed;
  background: var(--app-surface-2);
  color: var(--app-text-5);
}

/* ==================== 08 修改密码 ==================== */
.profile-badge-amber {
  padding: 0.25rem 0.625rem;
  border-radius: 999px;
  background: var(--app-amber-50);
  font-size: 0.6875rem;
  font-weight: 500;
  color: var(--app-amber-fg);
}
.profile-password__foot {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
@media (min-width: 640px) {
  .profile-password__foot {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }
}

/* ==================== 09 按钮 / 提示 ==================== */
.profile-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  border: 0;
  border-radius: 0.5rem;
  padding: 0.625rem 1.25rem;
  font-size: 0.875rem;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: background-color 0.2s, box-shadow 0.2s;
}
.profile-btn--primary {
  background: var(--app-accent);
  color: var(--app-on-accent-solid);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}
.profile-btn--primary:hover {
  background: var(--app-btn-primary-hover);
}
.profile-btn--primary:focus-visible {
  outline: none;
  box-shadow: 0 0 0 4px rgb(131 201 244 / 30%);
}
.profile-btn--violet {
  flex-shrink: 0;
  padding: 0.625rem 1rem;
  border: 1px solid var(--app-violet-line-100);
  background: var(--app-surface);
  color: var(--app-violet-fg);
}
.profile-btn--violet:hover {
  background: var(--app-violet-50);
}
.profile-btn--violet:focus-visible {
  outline: none;
  box-shadow: 0 0 0 4px var(--app-violet-bg);
}

.profile-note {
  font-size: 0.6875rem;
  color: var(--app-text-5);
}
.profile-note--wrap {
  line-height: 1.25rem;
  margin: 0;
}
.profile-note__star {
  color: var(--app-rose-500);
}

.profile-alert {
  margin-bottom: 1rem;
  padding: 0.5rem 0.75rem;
  border-radius: 0.5rem;
  font-size: 0.75rem;
  line-height: 1.25rem;
}
.profile-alert--error {
  background: var(--app-rose-100);
  color: var(--app-rose-fg);
}
.profile-alert--warn {
  grid-column: 1 / -1;
  margin: 0;
  background: var(--app-amber-50);
  color: var(--app-amber-deep);
}

/* ==================== 10 隐藏元素 ==================== */
.profile-hidden {
  display: none;
}

/* ==================== 11 管理端主题覆盖 ====================
   本页同时给学员端和管理端用。学员端永远走上面的原色,
   .profile-page--admin 只在 role==='admin' 时挂上,
   让横幅、卡片、按钮按当前主题 token 重画,深色主题下不出现浅底深字。 */
.profile-page--admin .profile-hero__label,
.profile-page--admin .profile-stat__label {
  color: var(--app-on-hero-soft);
}
.profile-page--admin .profile-hero__dept,
.profile-page--admin .profile-stat__unit {
  color: var(--app-on-hero);
}
.profile-page--admin .profile-avatar {
  border-color: var(--app-glass-line);
  background: var(--app-glass-bg-strong);
}
.profile-page--admin .profile-hero__tag,
.profile-page--admin .profile-hero__stats {
  border-color: var(--app-glass-line);
  background: var(--app-glass-bg);
}
.profile-page--admin .profile-stat + .profile-stat {
  border-left-color: var(--app-hero-divide);
}
.profile-page--admin .profile-card__name {
  color: var(--app-heading);
}
.profile-page--admin .profile-card__desc,
.profile-page--admin .profile-block__desc {
  color: var(--app-text-4);
}
.profile-page--admin .profile-card__icon--sky {
  background: var(--app-blue-bg);
  color: var(--app-blue-fg);
}
.profile-page--admin .profile-card__icon--violet {
  background: var(--app-violet-bg);
  color: var(--app-violet-fg);
}
.profile-page--admin .profile-tip {
  border-color: var(--app-blue-line);
  background: var(--app-blue-bg);
}
.profile-page--admin .profile-tip__icon {
  color: var(--app-blue-fg);
}
.profile-page--admin .profile-tip__title {
  color: var(--app-blue-fg);
}
.profile-page--admin .profile-tip__text {
  color: var(--app-text-3);
}
.profile-page--admin .profile-saved {
  color: var(--app-emerald-fg);
}
.profile-page--admin .profile-field input,
.profile-page--admin .profile-field select {
  color: var(--app-text);
}
.profile-page--admin .profile-badge-amber {
  background: var(--app-amber-bg);
  color: var(--app-amber-fg);
}
.profile-page--admin .profile-btn--primary {
  color: var(--app-on-accent-fill);
}
.profile-page--admin .profile-btn--primary:hover {
  background: var(--app-accent-fill-hi);
}
.profile-page--admin .profile-btn--violet {
  border-color: var(--app-violet-line);
  color: var(--app-violet-fg);
}
.profile-page--admin .profile-btn--violet:hover {
  background: var(--app-violet-bg);
}
.profile-page--admin .profile-btn--violet:focus-visible {
  box-shadow: 0 0 0 4px var(--app-violet-bg);
}
.profile-page--admin .profile-alert--error {
  background: var(--app-rose-bg);
  color: var(--app-rose-fg);
}
.profile-page--admin .profile-alert--warn {
  background: var(--app-amber-bg);
  color: var(--app-amber-fg);
}
</style>
