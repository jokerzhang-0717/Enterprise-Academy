import { onBeforeUnmount, watch } from 'vue';

const MIN_PAD = 4;
const LIST_SELECTOR = '.table-wrap, .question-list, .notice-list';

/* 卡片列表的行内边距基础值实测后缓存:压缩后读到的已是压缩值 */
const basePads = new WeakMap();

/**
 * 管理端分页列表:把当前页的行数完整放进一屏。
 * 列表区吃掉页面剩余高度,行(表格行 / 题目卡 / 公告卡)的上下内边距按剩余空间等比分配,
 * 数据少(或屏幕高)时保持原始行高,数据多(或屏幕矮)时收紧行高。
 */
export function useAdminTableFit(rootRef, viewRef) {
  let mo = null;
  let ro = null;
  let timer = null;

  const itemsOf = (wrap) => {
    const table = wrap.querySelector('.data-table');
    if (table) return { rows: table.tBodies[0] ? [...table.tBodies[0].rows] : [], table };
    return {
      rows: [...wrap.children].filter((el) => el.classList.contains('question-item') || el.classList.contains('notice-card')),
      table: null
    };
  };

  const fit = () => {
    const root = rootRef.value;
    const page = root && root.querySelector('.admin-fit-page');
    const wrap = page && page.querySelector(LIST_SELECTOR);
    if (!wrap) return;

    const { rows, table } = itemsOf(wrap);
    if (!rows.length) {
      wrap.style.removeProperty('--row-pad');
      return;
    }

    /* 可用高度 = 页面高度 - 列表区以外的兄弟节点(含 flex 间距)。
       不能取列表区自身高度:压缩后它等于内容高度,会锁定在压缩态。
       弹窗类节点是 position: fixed,不占文档流,不能算进已用高度。 */
    const gap = parseFloat(getComputedStyle(page).rowGap) || 0;
    const others = [...page.children].filter(
      (el) => el !== wrap && getComputedStyle(el).position !== 'fixed'
    );
    const used =
      others.reduce((sum, el) => sum + el.getBoundingClientRect().height, 0) +
      gap * others.length;
    /* 列表区的边框和横向滚动条也占高度 */
    const chrome = wrap.getBoundingClientRect().height - wrap.clientHeight;
    const avail = page.getBoundingClientRect().height - used - chrome;

    const head = table && table.tHead ? table.tHead.getBoundingClientRect().height : 0;
    const first = table ? rows[0].cells[0] : rows[0];
    const pad = parseFloat(getComputedStyle(first).paddingTop) || 0;
    if (!wrap.style.getPropertyValue('--row-pad')) basePads.set(wrap, pad);
    const base = basePads.get(wrap) || pad;
    const contentTotal = rows.reduce(
      (sum, row) => sum + Math.max(0, row.getBoundingClientRect().height - pad * 2),
      0
    );
    /* 卡片列表自身还有行间距,表格没有 */
    const innerGap = (parseFloat(getComputedStyle(wrap).rowGap) || 0) * Math.max(0, rows.length - 1);
    const next = Math.min(
      base,
      Math.max(MIN_PAD, (avail - head - contentTotal - innerGap) / (rows.length * 2))
    );
    const current = parseFloat(wrap.style.getPropertyValue('--row-pad'));
    /* 收敛保护:两次结果基本一致时不再写回,避免与 ResizeObserver 互相触发 */
    if (Number.isFinite(current) && Math.abs(current - next) < 0.4) return;
    wrap.style.setProperty('--row-pad', `${next.toFixed(1)}px`);
  };

  const schedule = () => {
    if (timer) clearTimeout(timer);
    timer = setTimeout(fit, 20);
  };

  const teardown = () => {
    mo && mo.disconnect();
    ro && ro.disconnect();
    mo = ro = null;
  };

  /* 登录页时画布节点还没挂上来,等它出现再观察 */
  watch(
    rootRef,
    (el) => {
      teardown();
      if (!el) return;
      mo = new MutationObserver(schedule);
      mo.observe(el, { childList: true, subtree: true });
      ro = new ResizeObserver(schedule);
      ro.observe(el);
      schedule();
    },
    { immediate: true }
  );

  watch(viewRef, () => {
    const root = rootRef.value;
    const wrap = root && root.querySelector(`.admin-fit-page ${LIST_SELECTOR}`);
    if (wrap) wrap.style.removeProperty('--row-pad');
    schedule();
  });

  onBeforeUnmount(() => {
    if (timer) clearTimeout(timer);
    teardown();
  });
}
