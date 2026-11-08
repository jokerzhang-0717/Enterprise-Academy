<template>
  <div class="auth-shell relative flex min-h-screen items-stretch lg:h-dvh lg:overflow-hidden">

    <!-- ============ 全局背景层 ============ -->
    <div class="pointer-events-none absolute inset-0 z-0 hidden lg:block">
      <div class="background-gradient"></div>
      <div class="dot-pattern"></div>
      <div class="glow glow-top-left"></div>
      <div class="glow glow-bottom-right"></div>
      <div class="orbit-center-glow"></div>
      <div class="orbit-container">
        <div class="orbit orbit-outer"></div>
        <div class="orbit orbit-dashed"></div>
        <div class="orbit orbit-middle"></div>
        <div class="orbit orbit-inner"></div>
        <div class="satellite satellite-outer"><i></i></div>
        <div class="satellite satellite-middle"><i></i></div>
        <div class="satellite satellite-inner"><i></i></div>
        <div class="orbit-core"></div>
      </div>
    </div>
    <div class="background-gradient pointer-events-none absolute inset-0 z-0 lg:hidden"></div>

    <!-- ============ LEFT ============ -->
    <aside class="relative hidden flex-col justify-between overflow-hidden px-12 py-11 lg:flex lg:w-[54%] xl:w-[55%] xl:px-16">

      <div class="relative z-10 fade-in" style="--d:.05s">
        <div class="flex items-center gap-3">
          <div class="logo-icon grid h-10 w-10 place-items-center rounded-[13px]">
            <GraduationCap class="h-[19px] w-[19px]" />
          </div>
          <div>
            <div class="flex items-center gap-2">
              <span class="brand-title text-[17px] text-[#0B1B33]">云舒艺境</span>
              <span class="brand-badge">ENTERPRISE</span>
            </div>
            <div class="mono-font mt-[3px] text-[8.5px] uppercase tracking-[.28em] text-[#5B6B80]">
              Corporate L&amp;D Operating System
            </div>
          </div>
        </div>
      </div>

      <div class="relative z-10">
        <div class="fade-in inline-flex items-center gap-2 rounded-full border border-[#83C9F4]/60 bg-white/[.55] px-3 py-[5px] backdrop-blur-sm" style="--d:.12s">
          <span class="pulse-dot h-[5px] w-[5px] rounded-full bg-[#2E8BD6]"></span>
          <span class="mono-font text-[9px] uppercase tracking-[.22em] text-[#2E7AC0]">
            Next-Gen Corporate L&amp;D
          </span>
        </div>

        <h1 class="fade-in heading-font mt-5 text-[clamp(2.15rem,2.7vw,2.85rem)] leading-[1.16] tracking-[-.025em] text-[#0B1B33]" style="--d:.2s">
          让每个岗位<br />
          都有清晰的<br />
          <span class="gradient-text">成长坐标</span>
        </h1>

        <p class="fade-in mt-5 max-w-[25rem] text-[12.5px] leading-[1.85] text-[#41526A]" style="--d:.3s">
          基于角色模型与能力矩阵的自适应学习链路，构建业务敏捷驱动的千人千面成长引擎 —— 让培训投入可度量、可沉淀、可复用。
        </p>

        <ul class="fade-in mt-8 max-w-[26rem] border-t border-[#83C9F4]/35" style="--d:.4s">
          <li v-for="(f, i) in features" :key="f.title" class="feature-item group">
            <span class="mono-font w-7 shrink-0 text-[10px] tracking-[.16em] text-[#2E8BD6]">{{ f.idx }}</span>
            <div class="min-w-0 flex-1">
              <div class="text-[12.5px] font-semibold tracking-[.02em] text-[#16233A]">{{ f.title }}</div>
              <div class="mt-[2px] truncate text-[10.5px] text-[#5B6B80]">{{ f.desc }}</div>
            </div>
            <component :is="f.icon" class="h-[15px] w-[15px] shrink-0 text-[#6FA9E0] transition-colors duration-300 group-hover:text-[#9381FF]" />
          </li>
        </ul>
      </div>

      <div class="fade-in relative z-10" style="--d:.62s">
        <div class="stats-bar">
          <div v-for="(s, i) in stats" :key="s.label" class="flex-1" :class="i > 0 ? 'border-l border-[#83C9F4]/35 pl-6' : ''">
            <div class="mono-font text-[8px] uppercase tracking-[.26em] text-[#5B6B80]">{{ s.label }}</div>
            <div class="number-font mt-1 text-[19px] font-semibold leading-none text-[#0B1B33]">{{ s.value }}</div>
          </div>
        </div>
        <div class="mt-3.5 flex items-center gap-2">
          <ShieldCheck class="h-3 w-3 text-[#2E8BD6]" />
          <span class="mono-font text-[8.5px] uppercase tracking-[.22em] text-[#5B6B80]">
            ISO 27001 · 金融级数据防护 · 通过等保三级
          </span>
        </div>
      </div>
    </aside>

    <!-- ============ RIGHT ============ -->
    <main class="relative flex w-full items-center justify-center px-6 py-10 sm:px-10 lg:w-[46%] lg:px-12 xl:w-[45%] xl:px-16">
      <div class="w-full max-w-[23.5rem]">

        <div class="fade-in mb-8 flex items-center gap-2.5 lg:hidden" style="--d:.05s">
          <div class="logo-icon grid h-9 w-9 place-items-center rounded-[11px]">
            <GraduationCap class="h-[17px] w-[17px]" />
          </div>
          <div>
            <div class="brand-title text-[15px] text-[#0B1B33]">云舒艺境</div>
            <div class="mono-font text-[8px] uppercase tracking-[.22em] text-[#5B6B80]">Enterprise L&amp;D Platform</div>
          </div>
        </div>

        <div class="fade-in mb-7" style="--d:.1s">
          <div class="inline-flex rounded-[11px] border border-[#D6E7F7] bg-[#EAF3FC] p-[3px]">
            <button
              v-for="tab in tabs" :key="tab.key"
              type="button" @click="activeTab = tab.key"
              class="relative rounded-[9px] px-6 py-[7px] text-[12px] font-semibold tracking-[.04em] transition-colors duration-300"
              :class="activeTab === tab.key ? 'text-[#9381FF]' : 'text-[#4F627A] hover:text-[#2E8BD6]'"
            >
              <span v-if="activeTab === tab.key" class="tab-active-bg absolute inset-0 -z-10 rounded-[9px]"></span>
              {{ tab.label }}
            </button>
          </div>
          <div class="mt-3 flex items-center gap-[6px]">
            <span class="pulse-dot h-[5px] w-[5px] rounded-full bg-[#2FBF8F]"></span>
            <span class="mono-font text-[8.5px] uppercase tracking-[.22em] text-[#5B6B80]">
              Secure Intranet · 内网安全通道已建立
            </span>
          </div>
        </div>

        <div class="fade-in mb-6" style="--d:.16s">
          <div class="title-accent mb-3"></div>
          <h2 class="heading-font text-[23px] leading-[1.3] tracking-[-.015em] text-[#0B1B33]">
            {{ activeTab === 'login' ? '欢迎回到培训工作台' : '创建培训账号' }}
          </h2>
          <p class="mt-[6px] text-[11.5px] leading-relaxed text-[#4F627A]">
            {{ activeTab === 'login' ? '请输入企业工号与密码，继续你的学习进度。' : '填写以下信息，加入企业人才培养体系。' }}
          </p>
        </div>

        <!-- ====== 登录 ====== -->
        <form v-if="activeTab === 'login'" class="space-y-[14px]" @submit.prevent="handleSubmit" novalidate>

          <div class="fade-in" style="--d:.22s">
            <label class="form-label">Employee ID · 企业工号</label>
            <div class="input-wrap" :class="loginErrors.employeeId ? 'input-wrap-error' : ''">
              <User class="input-icon" />
              <input
                v-model.trim="employeeId"
                @input="clearLoginError('employeeId')"
                type="text"
                placeholder="EMP-2024098"
                class="input-box mono-font"
                :aria-invalid="Boolean(loginErrors.employeeId)"
                aria-describedby="login-err-employeeId"
              />
            </div>
            <p v-if="loginErrors.employeeId" id="login-err-employeeId" class="login-error">
              {{ loginErrors.employeeId }}
            </p>
          </div>

          <div class="fade-in" style="--d:.28s">
            <div class="flex items-center justify-between">
              <label class="form-label">Password · 登录密码</label>
              <a href="#forgot" class="form-link" @click.prevent="handleForgot">忘记密码？</a>
            </div>
            <div class="input-wrap mt-2" :class="loginErrors.password ? 'input-wrap-error' : ''">
              <Lock class="input-icon" />
              <input
                :type="showPassword ? 'text' : 'password'"
                v-model="password"
                @input="clearLoginError('password')"
                placeholder="请输入登录密码"
                class="input-box"
                :aria-invalid="Boolean(loginErrors.password)"
                aria-describedby="login-err-password"
              />
              <button type="button" class="password-toggle" @click="showPassword = !showPassword" aria-label="切换密码可见性">
                <EyeOff v-if="showPassword" class="h-[15px] w-[15px]" />
                <Eye v-else class="h-[15px] w-[15px]" />
              </button>
            </div>
            <p v-if="loginErrors.password" id="login-err-password" class="login-error">
              {{ loginErrors.password }}
            </p>
          </div>

          <!-- ====== 滑块行为验证 ====== -->
          <div class="fade-in" style="--d:.34s">
            <div class="flex items-center justify-between">
              <span class="form-label">Behaviour Check · 行为验证</span>
              <span class="mono-font text-[8.5px] tracking-[.18em]" :class="sliderVerified ? 'text-[#12866A]' : 'text-[#7C8FA3]'">
                {{ sliderVerified ? '✓ VERIFIED' : 'PENDING' }}
              </span>
            </div>

            <div
              ref="sliderRef"
              class="slider-track mt-2"
              :class="{ verified: sliderVerified, 'slider-track--error': loginErrors.slider && !sliderVerified }"
              @pointerdown="startDrag"
            >
              <div class="slider-progress" :style="{ width: `${sliderPosition + 44}px` }"></div>

              <span class="slider-hint" :class="sliderVerified ? 'text-[#1B7F63]' : ''">
                {{ sliderVerified ? '验证通过，安全信标就绪' : '按住滑块，拖动到最右端' }}
              </span>

              <div
                class="slider-handle"
                :class="sliderVerified ? 'slider-handle-success' : ''"
                :style="{ transform: `translateX(${sliderPosition}px)` }"
              >
                <Check v-if="sliderVerified" class="h-[15px] w-[15px]" />
                <ChevronsRight v-else class="h-[16px] w-[16px]" />
              </div>
            </div>
            <p v-if="loginErrors.slider && !sliderVerified" class="login-error">
              {{ loginErrors.slider }}
            </p>
          </div>

          <!-- 记住设备 -->
          <div class="fade-in flex items-center justify-between pt-[2px]" style="--d:.4s">
            <label class="flex cursor-pointer select-none items-center gap-2">
              <span class="custom-checkbox" :class="rememberDevice ? 'checked' : ''">
                <input type="checkbox" v-model="rememberDevice" class="sr-only" />
                <Check class="h-[11px] w-[11px] text-white" :class="rememberDevice ? 'opacity-100' : 'opacity-0'" />
              </span>
              <span class="text-[11px] text-[#4F627A]">30 天内记住当前设备</span>
            </label>
          </div>

          <div class="fade-in pt-[2px]" style="--d:.46s">
            <button type="submit" class="submit-btn">
              <span>立即登录并开启学习</span>
              <ArrowRight class="h-[15px] w-[15px] transition-transform duration-300 group-hover:translate-x-1" />
            </button>
          </div>

          <div class="fade-in mt-4 rounded-[9px] border border-dashed border-[#D3E4F3] bg-[#F6FBFF] px-3 py-2" style="--d:.52s">
            <p class="mono-font text-[9.5px] leading-[1.7] text-[#5B6B80]">
              演示账号 · 学员:EMP-2024098 / 管理员:admin · 统一密码:123456(完成滑块验证后登录)
            </p>
          </div>
        </form>

        <!-- ====== 注册 ====== -->
        <form v-else class="space-y-[12px]" @submit.prevent="handleRegister" novalidate>
          <div v-for="f in registerFields" :key="f.key" class="fade-in" :style="{ '--d': `${0.22 + f.order * 0.05}s` }">
            <label class="form-label">{{ f.label }}</label>
            <div class="input-wrap mt-2" :class="registerErrors[f.key] ? 'input-wrap-error' : ''">
              <component :is="f.icon" class="input-icon" />
              <input
                v-model.trim="f.model.value" @input="clearRegisterError(f.key)"
                :type="f.type" :placeholder="f.placeholder" :autocomplete="f.autocomplete"
                :aria-invalid="Boolean(registerErrors[f.key])" :aria-describedby="`reg-err-${f.key}`"
                class="input-box" :class="f.key === 'employeeId' ? 'mono-font' : ''"
              />
            </div>
            <p v-if="registerErrors[f.key]" :id="`reg-err-${f.key}`" class="mt-[5px] text-[10px] text-[#C2417C]">
              {{ registerErrors[f.key] }}
            </p>
          </div>

          <div class="fade-in pt-[4px]" style="--d:.5s">
            <button type="submit" class="submit-btn">
              <span>注册并进入学习</span>
              <ArrowRight class="h-[15px] w-[15px] transition-transform duration-300 group-hover:translate-x-1" />
            </button>
          </div>
        </form>

        <div class="fade-in mt-7 border-t border-[#DCE7F2] pt-4" style="--d:.56s">
          <div class="mono-font flex items-center justify-between text-[8.5px] tracking-[.16em] text-[#7C8FA3]">
            <span class="whitespace-nowrap">© 2024 云舒艺境 LMS</span>
            <span class="flex items-center gap-2.5 whitespace-nowrap">
              <a href="#privacy" class="footer-link" @click.prevent>隐私策略</a>
              <span class="h-[3px] w-[3px] rounded-full bg-[#C9D8E6]"></span>
              <a href="#iso" class="footer-link" @click.prevent>ISO 27001</a>
            </span>
          </div>
        </div>

      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onBeforeUnmount } from 'vue';
import {
  GraduationCap, Cpu, FileCheck2, Play, ShieldCheck,
  Eye, EyeOff, ArrowRight, Check, ChevronsRight,
  Lock, User, Gauge, KeyRound
} from 'lucide-vue-next';
import { useToast } from '../composables/useToast.js';
import { login as loginApi, register as registerApi } from '../api/index.js';

const emit = defineEmits(['login-success']);

const tabs = [
  { key: 'login', label: '登录' },
  { key: 'register', label: '注册' }
];
const activeTab = ref('login');

const features = [
  { idx: '01', title: '微服务架构', desc: '高并发弹性吞吐 · 秒级扩容', icon: Cpu },
  { idx: '02', title: '智能考试测评', desc: '自适应抽题引擎 · 多维防作弊', icon: FileCheck2 },
  { idx: '03', title: '断点续学联动', desc: '跨端进度漫游 · 无缝接续', icon: Play },
  { idx: '04', title: '合规认证闭环', desc: '证书链上防伪 · 自动核销', icon: ShieldCheck }
];

const stats = [
  { label: 'Uptime SLA', value: '99.99%' },
  { label: 'Certification', value: 'ISO 27001' },
  { label: 'Defence', value: '金融级' }
];

/* ---------- 登录 ---------- */
const employeeId = ref('EMP-2024098');
const password = ref('');
const showPassword = ref(false);
const rememberDevice = ref(true);

const loginErrors = ref({});
const { show: showToast } = useToast();

const clearLoginError = (field) => {
  if (!loginErrors.value[field]) return;
  const { [field]: _drop, ...rest } = loginErrors.value;
  loginErrors.value = rest;
};

/* ---------- 滑块行为验证 ---------- */
const sliderVerified = ref(false);
const sliderPosition = ref(0);
const sliderRef = ref(null);
const dragging = ref(false);

const maxOffset = () => {
  const el = sliderRef.value;
  return el ? el.clientWidth - 44 : 260;
};

const startDrag = (e) => {
  if (sliderVerified.value) return;
  dragging.value = true;
  window.addEventListener('pointermove', onDrag);
  window.addEventListener('pointerup', stopDrag);
  window.addEventListener('pointercancel', stopDrag);
  onDrag(e);
};

const onDrag = (e) => {
  if (!dragging.value || sliderVerified.value) return;
  const el = sliderRef.value;
  if (!el) return;
  const rect = el.getBoundingClientRect();
  const max = maxOffset();
  const pos = Math.min(Math.max(e.clientX - rect.left - 22, 0), max);
  sliderPosition.value = pos;
  if (pos >= max - 6) {
    sliderVerified.value = true;
    sliderPosition.value = max;
    clearLoginError('slider');
    stopDrag();
  }
};

const stopDrag = () => {
  if (!sliderVerified.value) sliderPosition.value = 0;
  dragging.value = false;
  window.removeEventListener('pointermove', onDrag);
  window.removeEventListener('pointerup', stopDrag);
  window.removeEventListener('pointercancel', stopDrag);
};

onBeforeUnmount(stopDrag);

/* ---------- 注册 ---------- */
const registerName = ref('');
const registerEmployeeId = ref('');
const registerPassword = ref('');
const confirmPassword = ref('');
const registerErrors = ref({});

const registerFields = computed(() => [
  { key: 'name', order: 0, label: 'Name · 姓名', type: 'text', placeholder: '请输入姓名', autocomplete: 'name', icon: User, model: registerName },
  { key: 'employeeId', order: 1, label: 'Employee ID · 企业工号', type: 'text', placeholder: '请输入企业工号', autocomplete: 'off', icon: Gauge, model: registerEmployeeId },
  { key: 'password', order: 2, label: 'Password · 设置密码', type: 'password', placeholder: '至少 8 位', autocomplete: 'new-password', icon: KeyRound, model: registerPassword },
  { key: 'confirmPassword', order: 3, label: 'Confirm · 确认密码', type: 'password', placeholder: '请再次输入密码', autocomplete: 'new-password', icon: ShieldCheck, model: confirmPassword }
]);

const clearRegisterError = (field) => {
  if (!registerErrors.value[field]) return;
  const { [field]: _drop, ...rest } = registerErrors.value;
  registerErrors.value = rest;
};

const submitting = ref(false);

const handleSubmit = async () => {
  const errors = {};
  if (!employeeId.value) errors.employeeId = '请输入企业工号。';
  if (!password.value) errors.password = '请输入登录密码。';
  if (!sliderVerified.value) errors.slider = '请先完成滑块安全验证。';

  loginErrors.value = errors;
  if (Object.keys(errors).length) return;
  if (submitting.value) return;

  submitting.value = true;
  try {
    const result = await loginApi(employeeId.value, password.value);
    emit('login-success', {
      token: result.token,
      refreshToken: result.refreshToken,
      user: result.user,
      employeeId: result.user?.employeeId || employeeId.value,
      rememberDevice: rememberDevice.value,
    });
  } catch (error) {
    loginErrors.value = { password: error?.message || '登录失败，请稍后重试。' };
    showToast(error?.message || '登录失败，请稍后重试。', 'error');
  } finally {
    submitting.value = false;
  }
};

const handleForgot = () => {
  showToast('请联系企业 IT 运维部工单重置密码', 'info');
};

const handleRegister = async () => {
  const errors = {};
  if (!registerName.value) errors.name = '请输入姓名。';
  if (!registerEmployeeId.value) errors.employeeId = '请输入企业工号。';
  if (!registerPassword.value) {
    errors.password = '请设置密码。';
  } else if (registerPassword.value.length < 8) {
    errors.password = '密码至少需要 8 位。';
  }
  if (!confirmPassword.value) {
    errors.confirmPassword = '请再次输入密码。';
  } else if (registerPassword.value !== confirmPassword.value) {
    errors.confirmPassword = '两次输入的密码不一致。';
  }

  registerErrors.value = errors;
  if (Object.keys(errors).length) return;
  if (submitting.value) return;

  submitting.value = true;
  try {
    await registerApi({
      name: registerName.value,
      employeeNo: registerEmployeeId.value,
      password: registerPassword.value,
    });
    const result = await loginApi(registerEmployeeId.value, registerPassword.value);
    showToast('注册成功，已自动登录', 'success');
    emit('login-success', {
      token: result.token,
      refreshToken: result.refreshToken,
      user: result.user,
      employeeId: result.user?.employeeId || registerEmployeeId.value,
      rememberDevice: rememberDevice.value,
    });
  } catch (error) {
    registerErrors.value = { employeeId: error?.message || '注册失败，请稍后重试。' };
    showToast(error?.message || '注册失败，请稍后重试。', 'error');
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
/* ============================================================
   云舒艺境 · 登录 / 注册页样式
   ------------------------------------------------------------
   目录
     01  字体排版            08  表单标签 / 链接
     02  渐变高亮文字        09  表单输入框
     03  全页渐变背景        10  滑块行为验证
     04  氛围光斑            11  复选框
     05  轨道动效图形        12  Tab 切换
     06  品牌标识            13  提交按钮
     07  特性列表 + 指标条   14  入场动效 / 15 无障碍
   ------------------------------------------------------------
   色板速查
     主蓝   #83C9F4 · #2E8BD6 · #A3D5FF · #6FA9E0
     品牌紫 #9381FF · #6B55E0
     深字   #0B1B33 · #16233A · #41526A
     灰蓝   #5B6B80 · #4F627A · #7C8FA3 · #8497AC
     成功绿 #2FBF8F · #12866A · #1B7F63 · #7BD9B8
     错误粉 #C2417C · #F0B4CC
     描边   #D3E4F3 · #D6E7F7 · #DCE7F2 · #C9D8E6
   ============================================================ */

/* ==================== 01 字体排版 ==================== */
:global(.auth-shell) {
  font-family: 'Sora', 'Noto Sans SC', ui-sans-serif, system-ui, sans-serif;
  -webkit-font-smoothing: antialiased;
  text-rendering: optimizeLegibility;
}
.heading-font { font-family: 'Sora', 'Noto Sans SC', sans-serif; font-weight: 800; }
.brand-title  { font-family: 'Sora', 'Noto Sans SC', sans-serif; font-weight: 700; letter-spacing: .06em; }
.mono-font    { font-family: 'JetBrains Mono', ui-monospace, SFMono-Regular, monospace; font-weight: 500; }
.number-font  { font-family: 'Sora', 'Noto Sans SC', sans-serif; letter-spacing: -.01em; font-variant-numeric: tabular-nums; }

/* ==================== 02 渐变高亮文字 ==================== */
.gradient-text {
  background: linear-gradient(96deg, #A3D5FF 0%, #83C9F4 34%, #9381FF 92%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

/* ==================== 03 全页渐变背景 ==================== */
.background-gradient {
  position: absolute; inset: 0;
  background:
    radial-gradient(58% 52% at 18% 28%, rgba(131, 201, 244, .4) 0%, transparent 66%),
    radial-gradient(54% 50% at 88% 90%, rgba(147, 129, 255, .32) 0%, transparent 66%),
    radial-gradient(46% 44% at 60% 10%, rgba(163, 213, 255, .34) 0%, transparent 70%),
    linear-gradient(112deg, #E4F1FD 0%, #E9F2FC 34%, #EBEEFB 62%, #EDEAFB 100%);
}

/* ==================== 04 氛围层（点阵 + 光斑漂移） ==================== */
.dot-pattern {
  position: absolute; inset: 0;
  background-image: radial-gradient(rgba(46, 139, 214, .14) 1px, transparent 1px);
  background-size: 24px 24px;
  mask-image: radial-gradient(46% 58% at 16% 42%, #000 18%, transparent 88%);
  -webkit-mask-image: radial-gradient(46% 58% at 16% 42%, #000 18%, transparent 88%);
}
.glow { position: absolute; border-radius: 50%; filter: blur(88px); will-change: transform; }
.glow-top-left {
  top: -110px; left: -90px; width: 420px; height: 420px;
  background: rgba(83, 156, 232, .3);
  animation: glow-drift 19s ease-in-out infinite alternate;
}
.glow-bottom-right {
  bottom: -130px; left: 30%; width: 400px; height: 400px;
  background: rgba(147, 129, 255, .22);
  animation: glow-drift 23s ease-in-out infinite alternate-reverse;
}
@keyframes glow-drift {
  from { transform: translate3d(0,0,0) scale(1); }
  to   { transform: translate3d(30px,-24px,0) scale(1.07); }
}

/* ==================== 05 轨道动效图形（同心圆 + 卫星） ==================== */
.orbit-center-glow {
  position: absolute; left: 44%; top: 50%; width: 36vw; height: 36vw;
  transform: translate(-50%, -50%); border-radius: 50%;
  background: radial-gradient(circle, rgba(163, 213, 255, .5) 0%, rgba(131, 201, 244, .2) 42%, transparent 70%);
}
.orbit-container {
  position: absolute; left: 44%; top: 50%; width: 36vw; height: 36vw;
  transform: translate(-50%, -50%);
}
.orbit { position: absolute; border-radius: 50%; }
.orbit-outer  { inset: 0;   border: 1px solid rgba(131, 201, 244, .5); }
.orbit-dashed { inset: 11%; border: 1px dashed rgba(147, 129, 255, .55); }
.orbit-middle { inset: 23%; border: 1px solid rgba(131, 201, 244, .45); }
.orbit-inner  { inset: 35%; border: 1px solid rgba(131, 201, 244, .6); }

.satellite { position: absolute; border-radius: 50%; animation: satellite-spin linear infinite; }
.satellite i { position: absolute; top: -4px; left: 50%; margin-left: -4px; width: 8px; height: 8px; border-radius: 50%; }
.satellite-outer  { inset: 0;   animation-duration: 28s; }
.satellite-outer i  { background: #83C9F4; box-shadow: 0 0 16px rgba(131, 201, 244, 1); }
.satellite-middle { inset: 11%; animation-duration: 19s; animation-direction: reverse; }
.satellite-middle i { background: #9381FF; box-shadow: 0 0 18px rgba(147, 129, 255, 1); }
.satellite-inner  { inset: 23%; animation-duration: 36s; }
.satellite-inner i  { background: #A3D5FF; box-shadow: 0 0 14px rgba(163, 213, 255, 1); }

.orbit-core {
  position: absolute; inset: 42%; border-radius: 50%;
  background: radial-gradient(circle, rgba(163, 213, 255, .65) 0%, rgba(131, 201, 244, .18) 46%, transparent 72%);
}
@keyframes satellite-spin { to { transform: rotate(360deg); } }

/* ==================== 06 品牌标识 ==================== */
.logo-icon {
  background: linear-gradient(138deg, #A3D5FF 0%, #83C9F4 42%, #9381FF 100%);
  color: #fff;
  box-shadow: 0 10px 22px -12px rgba(131, 201, 244, .9);
}
.brand-badge {
  border-radius: 999px; padding: 2px 7px;
  background: rgba(147, 129, 255, .2); border: 1px solid rgba(147, 129, 255, .38);
  color: #6B55E0;
  font-family: 'JetBrains Mono', monospace; font-size: 7.5px; letter-spacing: .22em;
}

/* ==================== 07 特性列表 + 指标条 ==================== */
.feature-item {
  display: flex; align-items: center; gap: .95rem;
  padding: .82rem 0;
  border-bottom: 1px solid rgba(46, 139, 214, .18);
  transition: padding-left .3s ease;
}
.feature-item:hover { padding-left: .35rem; }
.stats-bar {
  display: flex; align-items: stretch; gap: 1.5rem;
  padding-top: 1.15rem;
  border-top: 1px solid rgba(46, 139, 214, .22);
}

/* ==================== 08 标题装饰 / 表单标签 / 链接 ==================== */
.title-accent {
  width: 34px; height: 3px; border-radius: 999px;
  background: linear-gradient(90deg, #83C9F4 0%, #9381FF 100%);
}
.form-label {
  display: block;
  font-family: 'JetBrains Mono', monospace; font-size: 8.5px; font-weight: 500;
  letter-spacing: .22em; text-transform: uppercase; color: #2E8BD6;
}
.form-link {
  font-family: 'JetBrains Mono', monospace; font-size: 8.5px; letter-spacing: .16em; color: #2E8BD6;
}
.form-link:hover { text-decoration: underline; color: #1F6FB5; }
.footer-link { transition: color .18s ease; }
.footer-link:hover { color: #2E8BD6; }

/* ==================== 09 表单输入框 ==================== */
.input-wrap {
  display: flex; align-items: center; gap: .6rem;
  height: 44px; padding: 0 .85rem;
  border: 1.5px solid #D3E4F3; border-radius: 11px;
  background: rgba(255, 255, 255, .78);
  transition: border-color .2s, background .2s, box-shadow .2s;
}
.input-wrap:focus-within {
  border-color: #83C9F4; background: #fff;
  box-shadow: 0 0 0 3.5px rgba(131, 201, 244, .3);
}
.input-wrap-error {
  border-color: #F0B4CC !important;
  background: #FDF4F8 !important;
}

/* 登录字段错误提示（与注册一致的内联提示风格） */
.login-error {
  margin-top: 5px;
  font-size: 10px;
  line-height: 1.5;
  color: #C2417C;
}
.input-icon {
  height: 15px; width: 15px; flex: none;
  color: #6FA9E0; transition: color .18s;
}
.input-wrap:focus-within .input-icon { color: #2E8BD6; }
.input-box {
  flex: 1; min-width: 0; height: 100%;
  background: transparent; outline: none; border: none; padding: 0;
  font-size: 12px; color: #0B1B33;
}
.input-box::placeholder { color: #8497AC; }
.password-toggle {
  flex: none; padding: 3px; color: #8296AC;
  border-radius: 6px; transition: color .18s;
}
.password-toggle:hover { color: #2E8BD6; }

/* ==================== 10 滑块行为验证 ==================== */
/* 轨道 = 一条 44px 表单框：同圆角、同描边、同底色 */
.slider-track {
  position: relative;
  height: 44px; border-radius: 11px;
  overflow: hidden;
  border: 1.5px solid #D3E4F3;
  background: rgba(255, 255, 255, .78);
  user-select: none; touch-action: none;
  transition: border-color .25s, background .25s;
}
.slider-track:hover { border-color: #B9D7F0; }
.slider-track--error {
  border-color: #F0B4CC;
  background: #FDF4F8;
}
.slider-track.verified {
  border-color: rgba(47, 191, 143, .5);
  background: rgba(233, 248, 242, .9);
}

/* 进度填充 */
.slider-progress {
  position: absolute; inset: 0 auto 0 0;
  border-radius: 10px;
  background: linear-gradient(90deg, #83C9F4 0%, #A3D5FF 100%);
  transition: background .3s;
}
.slider-track.verified .slider-progress {
  background: linear-gradient(90deg, #2FBF8F 0%, #7BD9B8 100%);
}

/* 居中提示文字 */
.slider-hint {
  position: absolute; inset: 0; z-index: 1;
  display: grid; place-items: center;
  font-size: 11.5px; font-weight: 500; letter-spacing: .05em;
  color: #2C4A63; pointer-events: none;
}

/* 拖动把手 */
.slider-handle {
  position: absolute; top: 4px;
  width: 36px; height: 36px; border-radius: 9px;
  z-index: 2;
  display: grid; place-items: center;
  background: #FFFFFF;
  border: 1px solid rgba(131, 201, 244, .5);
  color: #2E8BD6;
  box-shadow: 0 2px 8px rgba(46, 100, 180, .18);
  cursor: grab;
  transition: box-shadow .2s, border-color .2s, color .2s, background .25s;
}
.slider-handle:hover {
  border-color: #83C9F4; color: #1F6FB5;
  box-shadow: 0 4px 12px rgba(46, 100, 180, .26);
}
.slider-handle:active {
  cursor: grabbing;
  box-shadow: 0 1px 5px rgba(46, 100, 180, .18);
}
.slider-handle-success {
  background: #2FBF8F; border-color: #2FBF8F; color: #fff;
  cursor: default;
  box-shadow: 0 2px 8px rgba(47, 191, 143, .32);
}
.slider-handle-success:hover,
.slider-handle-success:active {
  background: #2FBF8F; border-color: #2FBF8F; color: #fff;
  cursor: default;
  box-shadow: 0 2px 8px rgba(47, 191, 143, .32);
}

/* ==================== 11 复选框 ==================== */
.custom-checkbox {
  display: grid; place-items: center;
  width: 15px; height: 15px; border-radius: 5px;
  border: 1.5px solid #A9CFEC; background: rgba(255, 255, 255, .85);
  transition: all .18s;
}
.custom-checkbox.checked {
  border-color: transparent;
  background: linear-gradient(135deg, #83C9F4, #9381FF);
}

/* ==================== 12 Tab 切换指示器 ==================== */
.tab-active-bg {
  background: #FFFFFF;
  border: 1px solid rgba(147, 129, 255, .35);
  box-shadow: 0 4px 12px -6px rgba(147, 129, 255, .55);
  animation: tab-slide-in .28s cubic-bezier(.2, .9, .25, 1);
}
@keyframes tab-slide-in {
  from { opacity: 0; transform: scale(.9) translateY(3px); }
  to   { opacity: 1; transform: scale(1) translateY(0); }
}

/* ==================== 13 提交按钮 ==================== */
.submit-btn {
  position: relative;
  display: flex; width: 100%;
  align-items: center; justify-content: center; gap: .55rem;
  height: 46px; border-radius: 11px; overflow: hidden;
  background: linear-gradient(108deg, #83C9F4 0%, #7BA9F3 46%, #9381FF 100%);
  color: #fff;
  font-family: 'Sora', 'Noto Sans SC', sans-serif;
  font-size: 12.5px; font-weight: 700; letter-spacing: .08em;
  box-shadow: 0 14px 28px -16px rgba(110, 110, 225, 1);
  transition: transform .22s, box-shadow .22s, filter .22s;
}
.submit-btn:hover {
  transform: translateY(-1px); filter: brightness(1.05);
  box-shadow: 0 18px 32px -16px rgba(122, 108, 235, .9);
}
.submit-btn:active { transform: translateY(0); }

/* ==================== 14 入场动效 ==================== */
@keyframes fade-up {
  from { opacity: 0; transform: translateY(14px); }
  to   { opacity: 1; transform: translateY(0); }
}
.fade-in {
  opacity: 0;
  animation: fade-up .8s cubic-bezier(.19, 1, .22, 1) forwards;
  animation-delay: var(--d, 0s);
}

@keyframes pulse-heartbeat {
  0%, 100% { opacity: 1; transform: scale(1); }
  50%      { opacity: .3; transform: scale(.72); }
}
.pulse-dot { animation: pulse-heartbeat 2.1s ease-in-out infinite; }

/* ==================== 15 无障碍：减少动效 ==================== */
@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: .01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: .01ms !important;
  }
}
</style>