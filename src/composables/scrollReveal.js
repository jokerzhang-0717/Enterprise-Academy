/**
 * v-reveal：元素进入滚动可视区域时淡入上移。
 *
 * 用法：
 *   v-reveal
 *   v-reveal="120"                          // 延迟 120ms
 *   v-reveal="{ delay: 120, direction: 'left' }"
 *
 * 用 data 属性而不是 class 记录状态：卡片上的 :class 绑定在更新时会被 Vue
 * 整体重写 className，而 data 属性由指令独占，不会被覆盖。
 * 滚动容器优先使用 .app-viewport，找不到时再向上查找最近的可滚动祖先。
 */
const REVEAL_ATTR = 'data-scroll-reveal';
const DIR_ATTR = 'data-scroll-reveal-dir';
const OBSERVER_KEY = '__scrollRevealObserver';

// 按行显现用的状态键（挂在容器上，子元素之间共享同一个 observer）
const ROWS_OBSERVER_KEY = '__scrollRevealRowsObserver';
const ROWS_OPTIONS_KEY = '__scrollRevealRowsOptions';
const ROWS_SETUP_KEY = '__scrollRevealRowsSetup';
const ROWS_REVEALED_KEY = '__scrollRevealRowsRevealed';

function normalizeOptions(value) {
  if (typeof value === 'number') return { delay: value };
  return value && typeof value === 'object' ? value : {};
}

function findScrollRoot(el) {
  const viewport = el.closest('.app-viewport');
  if (viewport) return viewport;

  let node = el.parentElement;
  while (node) {
    const overflowY = getComputedStyle(node).overflowY;
    if (/(auto|scroll|overlay)/.test(overflowY) && node.scrollHeight > node.clientHeight + 1) {
      return node;
    }
    node = node.parentElement;
  }
  return null;
}

/**
 * 淡入结束后标记为 done，而不是移除属性：
 * - 不再匹配 hidden/visible 的 opacity/transform 规则，交还元素自身的 hover transform；
 * - 仍匹配 `#app [data-scroll-reveal] { animation: none }`，防止组件原有的
 *   card-reveal 等入场动画重新启动（否则淡入完成后会闪回去再播一遍）。
 */
function cleanupAfterReveal(item) {
  const cleanup = (event) => {
    if (event.target !== item || !['opacity', 'transform'].includes(event.propertyName)) return;
    item.setAttribute(REVEAL_ATTR, 'done');
    item.removeAttribute(DIR_ATTR);
    item.style.removeProperty('--reveal-delay');
    item.removeEventListener('transitionend', cleanup);
  };
  item.addEventListener('transitionend', cleanup);
}

function prefersReducedMotion() {
  return window.matchMedia?.('(prefers-reduced-motion: reduce)').matches;
}

export const revealDirective = {
  mounted(el, binding) {
    const options = normalizeOptions(binding.value);
    const direction = ['up', 'left', 'right', 'down', 'none'].includes(options.direction)
      ? options.direction
      : 'up';

    el.setAttribute(REVEAL_ATTR, 'hidden');
    if (direction !== 'up') el.setAttribute(DIR_ATTR, direction);
    if (options.delay) el.style.setProperty('--reveal-delay', `${options.delay}ms`);
    if (options.distance) el.style.setProperty('--reveal-distance', `${options.distance}px`);

    const reveal = () => {
      el.setAttribute(REVEAL_ATTR, 'visible');
      // 必须强制一次样式重算，否则浏览器不会应用 visible 的样式，
      // transition 事件一个都不触发，opacity 会一直卡在 0。
      void el.offsetHeight;
    };

    if (prefersReducedMotion() || typeof IntersectionObserver === 'undefined') {
      reveal();
      return;
    }

    const observer = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (entry.isIntersecting) {
            reveal();
            if (options.once !== false) {
              observer.unobserve(el);
              cleanupAfterReveal(el);
            }
          } else if (options.once === false) {
            el.setAttribute(REVEAL_ATTR, 'hidden');
          } else if (entry.boundingClientRect.bottom < (entry.rootBounds?.top ?? 0)) {
            // 滚动位置恢复或快速跳转把元素甩到视口上方时直接显示，
            // 避免用户往回滚动前这几块内容一直是透明的
            reveal();
            observer.unobserve(el);
            el.setAttribute(REVEAL_ATTR, 'done');
            el.removeAttribute(DIR_ATTR);
          }
        }
      },
      {
        root: findScrollRoot(el),
        rootMargin: options.rootMargin || '0px 0px -6% 0px',
        threshold: options.threshold ?? 0.06,
      },
    );

    observer.observe(el);
    el[OBSERVER_KEY] = observer;
  },

  unmounted(el) {
    el[OBSERVER_KEY]?.disconnect();
    delete el[OBSERVER_KEY];
  },
};

/** 把一批进入视口的子元素按行排队显现 */
function revealBatch(hits, observer, options) {
  const el = observer.__container;
  const items = [...el.children].filter((c) => c.nodeType === 1);
  // 一次性读取 offsetTop：下面会写 style/attribute，若边写边读会触发强制重排。
  const tops = new Map(items.map((item) => [item, Math.round(item.offsetTop)]));
  // 同一批进入视口的行按纵向排序后逐个登场；不用绝对行号，
  // 避免慢慢往下滚时后面的元素攒出好几秒的延迟
  const rowTops = [
    ...new Set(hits.filter((i) => !i[ROWS_REVEALED_KEY]).map((i) => tops.get(i))),
  ].sort((a, b) => a - b);

  rowTops.forEach((top, rowIndex) => {
    items
      .filter((item) => tops.get(item) === top && !item[ROWS_REVEALED_KEY])
      .forEach((item, itemIndex) => {
        item[ROWS_REVEALED_KEY] = true;
        const delay =
          rowIndex * (options.rowDelay ?? 140) + itemIndex * (options.itemDelay ?? 80);
        item.style.setProperty('--reveal-delay', `${delay}ms`);
        item.setAttribute(REVEAL_ATTR, 'visible');
        observer.unobserve(item);
        cleanupAfterReveal(item);
      });
  });
  // 批量强制样式重算：让这一批 visible 的样式真正生效并启动 transition。
  void el.offsetHeight;
}

function setupRows(el) {
  const options = el[ROWS_OPTIONS_KEY] || {};
  if (prefersReducedMotion() || typeof IntersectionObserver === 'undefined') return;

  let observer = el[ROWS_OBSERVER_KEY];
  if (!observer) {
    observer = new IntersectionObserver(
      (entries) => {
        const hits = entries.filter((e) => e.isIntersecting).map((e) => e.target);
        if (hits.length) revealBatch(hits, observer, options);
      },
      {
        root: findScrollRoot(el),
        rootMargin: options.rootMargin || '0px 0px -6% 0px',
        threshold: options.threshold ?? 0.06,
      },
    );
    // 让 revealBatch 能拿到容器（回调里只需要 hits）
    observer.__container = el;
    el[ROWS_OBSERVER_KEY] = observer;
  }

  let added = false;
  [...el.children].forEach((item) => {
    if (item.nodeType !== 1 || item[ROWS_SETUP_KEY]) return;
    item[ROWS_SETUP_KEY] = true;
    item.setAttribute(REVEAL_ATTR, 'hidden');
    if (options.direction && options.direction !== 'up') item.setAttribute(DIR_ATTR, options.direction);
    observer.observe(item);
    added = true;
  });

  // IO 的首次通知在挂载瞬间常漏掉已在视口内的元素（尤其 v-for 数据后到时），
  // 主动补检：下一帧 + 两次延时兜底，确保首屏一定能出现，不必等用户滚动。
  if (added) {
    // 强制浏览器先应用 hidden 样式。否则 rAF 里紧接着改成 visible，
    // 浏览器没看到中间状态，transition 不会启动（状态变了但 opacity 一直 0）。
    void el.offsetHeight;

    const checkVisible = () => {
      if (el[ROWS_OBSERVER_KEY] !== observer) return;
      const root = findScrollRoot(el);
      const rootRect = root?.getBoundingClientRect() || {
        top: 0,
        bottom: window.innerHeight,
      };
      const visible = [...el.children].filter((item) => {
        if (item[ROWS_REVEALED_KEY]) return false;
        const rect = item.getBoundingClientRect();
        return rect.bottom > rootRect.top && rect.top < rootRect.bottom;
      });
      if (visible.length) revealBatch(visible, observer, options);
    };
    // 双重 rAF：确保 hidden 已真正 paint 一帧后再切 visible，
    // 单帧内切换会让浏览器跳过过渡，导致 opacity 卡在 0 直到很久以后才跳到 1。
    requestAnimationFrame(() => requestAnimationFrame(checkVisible));
    setTimeout(checkVisible, 100);
    setTimeout(checkVisible, 350);
  }
}

/**
 * v-reveal-rows：容器内的子元素按“行”依次显现。
 * 同一行的卡片一起淡入；同一批进入视口的多行按 rowDelay 依次登场；
 * 行内的多张卡片再按 itemDelay 错开，避免整批一起冒出来。
 *
 * 用法：
 *   v-reveal-rows
 *   v-reveal-rows="{ rowDelay: 500, itemDelay: 260 }"
 */
export const revealRowsDirective = {
  mounted(el, binding) {
    el[ROWS_OPTIONS_KEY] = normalizeOptions(binding.value);
    // Vue 父组件 mounted 时子元素已插入 DOM，可以直接扫描
    setupRows(el);
  },
  updated(el, binding) {
    el[ROWS_OPTIONS_KEY] = normalizeOptions(binding.value);
    // v-for 数据后到时补上新增的子元素
    setupRows(el);
  },
  unmounted(el) {
    el[ROWS_OBSERVER_KEY]?.disconnect();
    delete el[ROWS_OBSERVER_KEY];
    delete el[ROWS_OPTIONS_KEY];
  },
};

export default revealDirective;
