export interface PageScrollPosition {
  left: number;
  top: number;
}

export const SCROLL_RESTORE_TIMEOUT_MS = 10_000;
const POSITION_TOLERANCE_PX = 1;
const SCROLL_KEYS = new Set([
  'ArrowUp',
  'ArrowDown',
  'ArrowLeft',
  'ArrowRight',
  'PageUp',
  'PageDown',
  'Home',
  'End',
  ' ',
  'Tab',
]);

function readPosition(): PageScrollPosition {
  return { left: window.scrollX, top: window.scrollY };
}

/** 跟踪可见页面的窗口滚动；返回清理函数，隐藏页不再监听或修改窗口。 */
export function trackPageScroll(
  container: HTMLElement,
  saved: PageScrollPosition,
  onPosition: (position: PageScrollPosition) => void,
): () => void {
  let isRestoring = true;
  let isDisposed = false;
  let frame: number | undefined;
  let timeout: number | undefined;

  const stopRestoring = () => {
    isRestoring = false;
    if (frame !== undefined) window.cancelAnimationFrame(frame);
    if (timeout !== undefined) window.clearTimeout(timeout);
    frame = undefined;
    timeout = undefined;
    observer?.disconnect();
    window.removeEventListener('resize', scheduleRestore);
    window.removeEventListener('wheel', handleInteraction);
    window.removeEventListener('touchstart', handleInteraction);
    window.removeEventListener('pointerdown', handleInteraction);
    window.removeEventListener('keydown', handleKeyDown);
  };

  const attemptRestore = () => {
    frame = undefined;
    if (isDisposed || !isRestoring) return;
    window.scrollTo({ ...saved, behavior: 'instant' });
    const actual = readPosition();
    const isReached =
      Math.abs(actual.left - saved.left) <= POSITION_TOLERANCE_PX &&
      Math.abs(actual.top - saved.top) <= POSITION_TOLERANCE_PX;
    if (isReached) {
      stopRestoring();
      onPosition(actual);
    }
  };

  function scheduleRestore() {
    if (!isDisposed && isRestoring && frame === undefined) {
      frame = window.requestAnimationFrame(attemptRestore);
    }
  }

  function handleInteraction() {
    if (!isRestoring || isDisposed) return;
    stopRestoring();
    onPosition(readPosition());
  }

  function handleKeyDown(event: KeyboardEvent) {
    if (SCROLL_KEYS.has(event.key)) handleInteraction();
  }

  const handleScroll = () => {
    // 内容尚未加载时，浏览器会将位置裁到当前高度，不能覆盖待恢复的记录。
    if (!isRestoring && !isDisposed) onPosition(readPosition());
  };

  window.addEventListener('scroll', handleScroll, { passive: true });
  window.addEventListener('resize', scheduleRestore);
  window.addEventListener('wheel', handleInteraction, { passive: true });
  window.addEventListener('touchstart', handleInteraction, { passive: true });
  window.addEventListener('pointerdown', handleInteraction, { passive: true });
  window.addEventListener('keydown', handleKeyDown);
  const observer = new ResizeObserver(scheduleRestore);
  observer.observe(container);
  timeout = window.setTimeout(() => {
    if (isDisposed) return;
    stopRestoring();
    onPosition(readPosition());
  }, SCROLL_RESTORE_TIMEOUT_MS);
  attemptRestore();

  return () => {
    isDisposed = true;
    stopRestoring();
    window.removeEventListener('scroll', handleScroll);
    // 隐藏/卸载时 DOM 高度可能已改变，不能再从窗口读取另一个页面的位置。
  };
}
