<template>
  <div class="onboarding-backdrop" role="presentation" @keydown.esc.prevent>
    <section
      class="onboarding-panel"
      role="dialog"
      aria-modal="true"
      aria-labelledby="onboarding-title"
      aria-describedby="onboarding-desc"
    >
      <header class="onboarding-head">
        <span class="onboarding-badge">
          <ShieldAlert />
          系统提醒
        </span>
        <h2 id="onboarding-title" class="onboarding-title">请先阅读《新员工入职手册》</h2>
        <p id="onboarding-desc" class="onboarding-desc">
          {{ userName ? `${userName}，` : '' }}欢迎加入云舒艺境！这是新员工首次进入平台的必读指引，
          请按顺序完成 {{ steps.length }} 个章节的阅读，完成后即可开始使用平台。
        </p>
      </header>

      <ol class="onboarding-progress" aria-label="阅读进度">
        <li
          v-for="(item, index) in steps"
          :key="item.step"
          class="onboarding-progress-item"
          :class="{
            'is-done': index < stepIndex,
            'is-active': index === stepIndex,
          }"
        >
          <button
            type="button"
            class="onboarding-progress-button"
            :disabled="index > stepIndex"
            :aria-current="index === stepIndex ? 'step' : undefined"
            @click="goToStep(index)"
          >
            <span class="onboarding-progress-dot">
              <Check v-if="index < stepIndex" />
              <template v-else>{{ index + 1 }}</template>
            </span>
            <span class="onboarding-progress-label">{{ item.title }}</span>
          </button>
        </li>
      </ol>

      <div class="onboarding-body">
        <span class="onboarding-step">{{ currentStep.step }}</span>
        <h3 class="onboarding-step-title">{{ currentStep.title }}</h3>
        <p class="onboarding-intro">{{ currentStep.intro }}</p>

        <div class="onboarding-sections">
          <article v-for="section in currentStep.sections" :key="section.title" class="onboarding-section">
            <h4>{{ section.title }}</h4>
            <p>{{ section.description }}</p>
          </article>
        </div>

        <div class="onboarding-tip">
          <Lightbulb />
          <p>{{ currentStep.tip }}</p>
        </div>
      </div>

      <footer class="onboarding-foot">
        <p class="onboarding-status" :class="{ 'is-error': error }">
          <template v-if="error">{{ error }}</template>
          <template v-else>第 {{ stepIndex + 1 }} / {{ steps.length }} 步 · 阅读完成后自动解锁平台</template>
        </p>
        <div class="onboarding-actions">
          <button
            v-if="stepIndex > 0"
            type="button"
            class="onboarding-button onboarding-button--ghost"
            :disabled="saving"
            @click="previous"
          >
            上一步
          </button>
          <button
            ref="primaryButton"
            type="button"
            class="onboarding-button onboarding-button--primary"
            :disabled="saving"
            @click="next"
          >
            <template v-if="saving">正在保存…</template>
            <template v-else-if="isLast">
              已完成阅读，进入平台
              <Check />
            </template>
            <template v-else>
              下一步
              <ArrowRight />
            </template>
          </button>
        </div>
      </footer>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue';
import { ArrowRight, Check, Lightbulb, ShieldAlert } from 'lucide-vue-next';
import { guideSteps } from '../onboardingGuide.js';
import { completeOnboarding } from '../api/onboarding.js';

const props = defineProps({
  userName: {
    type: String,
    default: '',
  },
});

const emit = defineEmits(['completed']);

const steps = guideSteps;
const stepIndex = ref(0);
const saving = ref(false);
const error = ref('');
const primaryButton = ref(null);

const currentStep = computed(() => steps[stepIndex.value]);
const isLast = computed(() => stepIndex.value === steps.length - 1);

const focusPrimary = () => {
  nextTick(() => primaryButton.value?.focus());
};

const goToStep = (index) => {

  if (index < 0 || index > stepIndex.value) return;
  stepIndex.value = index;
  error.value = '';
  focusPrimary();
};

const previous = () => {
  goToStep(stepIndex.value - 1);
};

const next = async () => {
  if (saving.value) return;
  if (!isLast.value) {
    stepIndex.value += 1;
    error.value = '';
    focusPrimary();
    return;
  }

  saving.value = true;
  error.value = '';
  try {
    const user = await completeOnboarding();
    emit('completed', {
      onboardingCompleted: true,
      onboardingCompletedAt: user?.onboardingCompletedAt || new Date().toISOString(),
    });
  } catch (requestError) {
    error.value = requestError?.message || '完成状态保存失败，请稍后重试';
  } finally {
    saving.value = false;
  }
};

onMounted(() => {
  document.body.style.overflow = 'hidden';
  focusPrimary();
});

onBeforeUnmount(() => {
  document.body.style.overflow = '';
});
</script>

<style scoped>

.onboarding-backdrop {
  position: fixed;
  z-index: 80;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 1rem;
  background: rgba(15, 23, 42, 0.62);
  backdrop-filter: blur(6px);
}

.onboarding-panel {
  display: flex;
  flex-direction: column;
  width: min(100%, 44rem);
  max-height: min(94vh, 52rem);
  overflow: hidden;
  border: 1px solid var(--app-border);
  border-radius: 1.25rem;
  background: var(--app-surface);
  box-shadow: 0 32px 90px rgba(15, 23, 42, 0.35);
  animation: onboarding-reveal 220ms ease-out both;
}

.onboarding-head {
  padding: 1.6rem 1.75rem 1.1rem;
  border-bottom: 1px solid var(--app-border-soft);
  background: linear-gradient(135deg, var(--app-accent-soft), transparent 70%);
}

.onboarding-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.3rem 0.7rem;
  border-radius: 999px;
  background: var(--app-amber-bg);
  color: var(--app-amber-fg);
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.onboarding-badge svg {
  width: 0.9rem;
  height: 0.9rem;
}

.onboarding-title {
  margin-top: 0.75rem;
  color: var(--app-heading);
  font-size: 1.4rem;
  font-weight: 800;
  line-height: 1.35;
}

.onboarding-desc {
  margin-top: 0.5rem;
  color: var(--app-text-3);
  font-size: 0.86rem;
  line-height: 1.7;
}

.onboarding-progress {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0.5rem;
  margin: 0;
  padding: 1rem 1.75rem;
  border-bottom: 1px solid var(--app-border-soft);
  list-style: none;
  background: var(--app-surface-2);
}

.onboarding-progress-button {
  display: flex;
  width: 100%;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.6rem;
  border: 1px solid transparent;
  border-radius: 0.6rem;
  background: transparent;
  color: var(--app-text-4);
  font-size: 0.74rem;
  font-weight: 600;
  text-align: left;
  cursor: default;
}

.onboarding-progress-item.is-done .onboarding-progress-button {
  color: var(--app-emerald-strong);
  cursor: pointer;
}

.onboarding-progress-item.is-done .onboarding-progress-button:hover {
  background: var(--app-emerald-soft);
}

.onboarding-progress-item.is-active .onboarding-progress-button {
  border-color: var(--app-accent-soft);
  background: var(--app-surface);
  color: var(--app-text);
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.08);
}

.onboarding-progress-dot {
  display: grid;
  flex-shrink: 0;
  place-items: center;
  width: 1.45rem;
  height: 1.45rem;
  border-radius: 999px;
  background: var(--app-surface-3);
  color: var(--app-text-4);
  font-size: 0.7rem;
  font-weight: 700;
}

.onboarding-progress-dot svg {
  width: 0.85rem;
  height: 0.85rem;
}

.onboarding-progress-item.is-active .onboarding-progress-dot {
  background: var(--app-accent-deep);
  color: #fff;
}

.onboarding-progress-item.is-done .onboarding-progress-dot {
  background: var(--app-emerald-bg);
  color: var(--app-emerald-strong);
}

.onboarding-progress-label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.onboarding-body {
  flex: 1;
  overflow-y: auto;
  padding: 1.4rem 1.75rem;
}

.onboarding-step {
  color: var(--app-accent-deep);
  font-size: 0.7rem;
  font-weight: 700;
  letter-spacing: 0.03em;
}

.onboarding-step-title {
  margin-top: 0.4rem;
  color: var(--app-heading);
  font-size: 1.15rem;
  font-weight: 800;
}

.onboarding-intro {
  margin-top: 0.45rem;
  color: var(--app-text-3);
  font-size: 0.85rem;
  line-height: 1.7;
}

.onboarding-sections {
  display: grid;
  gap: 0.65rem;
  margin-top: 1rem;
}

.onboarding-section {
  padding: 0.85rem 1rem;
  border: 1px solid var(--app-border);
  border-radius: 0.75rem;
  background: var(--app-surface-2);
}

.onboarding-section h4 {
  color: var(--app-text);
  font-size: 0.84rem;
  font-weight: 700;
}

.onboarding-section p {
  margin-top: 0.28rem;
  color: var(--app-text-4);
  font-size: 0.78rem;
  line-height: 1.65;
}

.onboarding-tip {
  display: flex;
  align-items: flex-start;
  gap: 0.55rem;
  margin-top: 0.9rem;
  padding: 0.75rem;
  border-radius: 0.65rem;
  background: var(--app-accent-soft);
  color: var(--app-accent-deep);
}

.onboarding-tip svg {
  flex-shrink: 0;
  width: 1rem;
  height: 1rem;
  margin-top: 0.1rem;
}

.onboarding-tip p {
  font-size: 0.76rem;
  line-height: 1.6;
}

.onboarding-foot {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  padding: 1rem 1.75rem 1.2rem;
  border-top: 1px solid var(--app-border-soft);
  background: var(--app-surface);
}

.onboarding-status {
  margin: 0;
  color: var(--app-text-5);
  font-size: 0.72rem;
}

.onboarding-status.is-error {
  color: var(--app-rose-fg);
  font-weight: 600;
}

.onboarding-actions {
  display: flex;
  gap: 0.6rem;
  margin-left: auto;
}

.onboarding-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  min-width: 6.5rem;
  padding: 0.62rem 1.1rem;
  border-radius: 0.6rem;
  font-size: 0.82rem;
  font-weight: 700;
  cursor: pointer;
  transition: filter 0.15s ease, background 0.15s ease;
}

.onboarding-button svg {
  width: 1rem;
  height: 1rem;
}

.onboarding-button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.onboarding-button--ghost {
  border: 1px solid var(--app-border);
  background: var(--app-surface);
  color: var(--app-text-3);
}

.onboarding-button--ghost:hover:not(:disabled) {
  background: var(--app-surface-3);
}

.onboarding-button--primary {
  border: 0;
  background: var(--app-accent-deep);
  color: var(--app-on-fill);
}

.onboarding-button--primary:hover:not(:disabled) {
  filter: brightness(1.08);
}

@keyframes onboarding-reveal {
  from {
    opacity: 0;
    transform: translateY(12px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@media (max-width: 640px) {
  .onboarding-head,
  .onboarding-body {
    padding-right: 1.2rem;
    padding-left: 1.2rem;
  }

  .onboarding-progress {
    padding: 0.85rem 1.2rem;
  }

  .onboarding-progress-label {
    display: none;
  }

  .onboarding-foot {
    padding: 0.9rem 1.2rem 1rem;
  }

  .onboarding-status {
    width: 100%;
  }

  .onboarding-actions {
    width: 100%;
  }

  .onboarding-button {
    flex: 1;
  }
}
</style>
