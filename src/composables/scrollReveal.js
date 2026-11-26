
const REVEAL_ATTR = 'data-scroll-reveal';
const DIR_ATTR = 'data-scroll-reveal-dir';
const OBSERVER_KEY = '__scrollRevealObserver';

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

function revealBatch(hits, observer, options) {
  const el = observer.__container;
  const items = [...el.children].filter((c) => c.nodeType === 1);

  const tops = new Map(items.map((item) => [item, Math.round(item.offsetTop)]));

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

  if (added) {

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

    requestAnimationFrame(() => requestAnimationFrame(checkVisible));
    setTimeout(checkVisible, 100);
    setTimeout(checkVisible, 350);
  }
}

export const revealRowsDirective = {
  mounted(el, binding) {
    el[ROWS_OPTIONS_KEY] = normalizeOptions(binding.value);

    setupRows(el);
  },
  updated(el, binding) {
    el[ROWS_OPTIONS_KEY] = normalizeOptions(binding.value);

    setupRows(el);
  },
  unmounted(el) {
    el[ROWS_OBSERVER_KEY]?.disconnect();
    delete el[ROWS_OBSERVER_KEY];
    delete el[ROWS_OPTIONS_KEY];
  },
};

export default revealDirective;
