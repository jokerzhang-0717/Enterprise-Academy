
const STATE_KEY = '__countUpState';

function parseTarget(raw) {
  if (raw && typeof raw === 'object') raw = raw.value;
  const s = String(raw ?? '');
  const m = s.match(/-?[\d,]+\.?\d*/);

  if (!m) return { num: 0, prefix: '', suffix: s, decimals: 0, raw: s, hasNumber: false };
  const numStr = m[0].replace(/,/g, '');
  const decimals = (numStr.split('.')[1] || '').length;
  const idx = s.indexOf(m[0]);
  return {
    num: parseFloat(numStr) || 0,
    prefix: s.slice(0, idx),
    suffix: s.slice(idx + m[0].length),
    decimals,
    raw: s,
    hasNumber: true,
  };
}

function formatNumber(n, decimals) {

  const fixed = Number(n).toFixed(decimals);
  const [intPart, fracPart] = fixed.split('.');
  const grouped = intPart.replace(/\B(?=(\d{3})+(?!\d))/g, ',');
  return fracPart !== undefined ? `${grouped}.${fracPart}` : grouped;
}

function prefersReducedMotion() {
  return typeof window !== 'undefined' && window.matchMedia?.('(prefers-reduced-motion: reduce)').matches;
}

function animate(el, targetRaw, duration) {
  const state = el[STATE_KEY] || (el[STATE_KEY] = {});
  if (state.raf) cancelAnimationFrame(state.raf);
  const parsed = parseTarget(targetRaw);
  const { num: target, prefix, suffix, decimals, hasNumber } = parsed;

  if (!hasNumber) {
    el.textContent = parsed.raw;
    state.current = undefined;
    state.target = undefined;
    return;
  }

  if (prefersReducedMotion() || !Number.isFinite(target)) {
    el.textContent = parsed.raw;
    state.current = target;
    state.target = target;
    return;
  }

  const startVal = Number.isFinite(state.current) ? state.current : 0;
  const delta = target - startVal;
  if (delta === 0) {
    el.textContent = `${prefix}${formatNumber(target, decimals)}${suffix}`;
    state.current = target;
    state.target = target;
    return;
  }

  const startTime = performance.now();
  const dur = Math.max(200, duration || 900);

  const step = (now) => {
    const t = Math.min(1, (now - startTime) / dur);

    const eased = 1 - Math.pow(1 - t, 3);
    const val = startVal + delta * eased;
    el.textContent = `${prefix}${formatNumber(val, decimals)}${suffix}`;
    state.current = val;
    if (t < 1) {
      state.raf = requestAnimationFrame(step);
    } else {
      state.current = target;
      state.target = target;
      el.textContent = `${prefix}${formatNumber(target, decimals)}${suffix}`;
      state.raf = null;
    }
  };
  state.target = target;
  state.raf = requestAnimationFrame(step);
}

export const countUpDirective = {
  mounted(el, binding) {
    const opts = binding.value && typeof binding.value === 'object' ? binding.value : {};
    const value = opts.value ?? binding.value;
    const duration = opts.duration;
    animate(el, value, duration);
  },
  updated(el, binding) {
    const opts = binding.value && typeof binding.value === 'object' ? binding.value : {};
    const value = opts.value ?? binding.value;
    const duration = opts.duration;
    const parsed = parseTarget(value);
    const state = el[STATE_KEY];
    if (state && parsed.hasNumber && state.target === parsed.num) return;
    animate(el, value, duration);
  },
  unmounted(el) {
    if (el[STATE_KEY]?.raf) cancelAnimationFrame(el[STATE_KEY].raf);
    delete el[STATE_KEY];
  },
};

export function useCountUp(targetRef, { duration = 900 } = {}) {

  return { animate: (el, val) => animate(el, val, duration) };
}

export default countUpDirective;
