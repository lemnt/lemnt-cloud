import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { SCROLL_RESTORE_TIMEOUT_MS, trackPageScroll } from './pageScroll';
import type { PageScrollPosition } from './pageScroll';

class ScrollWindow extends EventTarget {
  scrollX = 0;
  scrollY = 0;
  maxLeft = 0;
  maxTop = 200;
  frames = new Map<number, FrameRequestCallback>();
  nextFrameId = 0;
  setTimeout = globalThis.setTimeout;
  clearTimeout = globalThis.clearTimeout;

  scrollTo(options: ScrollToOptions) {
    this.scrollX = Math.min(options.left ?? this.scrollX, this.maxLeft);
    this.scrollY = Math.min(options.top ?? this.scrollY, this.maxTop);
    this.dispatchEvent(new Event('scroll'));
  }

  requestAnimationFrame(callback: FrameRequestCallback): number {
    const id = ++this.nextFrameId;
    this.frames.set(id, callback);
    return id;
  }

  cancelAnimationFrame(id: number) {
    this.frames.delete(id);
  }

  flushFrame() {
    const callbacks = [...this.frames.values()];
    this.frames.clear();
    callbacks.forEach((callback) => callback(0));
  }
}

class ScrollResizeObserver implements ResizeObserver {
  static instances: ScrollResizeObserver[] = [];
  isConnected = false;
  callback: ResizeObserverCallback;

  constructor(callback: ResizeObserverCallback) {
    this.callback = callback;
    ScrollResizeObserver.instances.push(this);
  }

  observe() {
    this.isConnected = true;
  }

  unobserve() {
    this.isConnected = false;
  }

  disconnect() {
    this.isConnected = false;
  }

  notify() {
    // 即使浏览器已经投递了回调，清理后也不能重新恢复位置。
    this.callback([], this);
  }
}

describe('页面窗口滚动恢复', () => {
  let viewport: ScrollWindow;
  const container = {} as HTMLElement;
  const cleanups: (() => void)[] = [];

  beforeEach(() => {
    vi.useFakeTimers();
    viewport = new ScrollWindow();
    ScrollResizeObserver.instances = [];
    vi.stubGlobal('window', viewport);
    vi.stubGlobal('ResizeObserver', ScrollResizeObserver);
  });

  afterEach(() => {
    cleanups.splice(0).forEach((cleanup) => cleanup());
    vi.useRealTimers();
    vi.unstubAllGlobals();
  });

  function track(
    saved: PageScrollPosition,
    onPosition = vi.fn<(position: PageScrollPosition) => void>(),
  ) {
    const dispose = trackPageScroll(container, saved, onPosition);
    cleanups.push(dispose);
    return { dispose, onPosition };
  }

  it('新页面回到顶部，持续记录用户滚动，隐藏后不再读取其他页面位置', () => {
    viewport.scrollY = 150;
    const { dispose, onPosition } = track({ left: 0, top: 0 });
    expect(viewport.scrollY).toBe(0);
    viewport.scrollTo({ top: 120 });
    expect(onPosition).toHaveBeenLastCalledWith({ left: 0, top: 120 });
    const calls = onPosition.mock.calls.length;
    viewport.scrollY = 20;
    dispose();
    viewport.scrollTo({ top: 80 });
    expect(onPosition).toHaveBeenCalledTimes(calls);
    expect(vi.getTimerCount()).toBe(0);
  });

  it('等待异步内容高度增长，不将临时裁剪位置写回，随后恢复两个滚动方向', () => {
    const { onPosition } = track({ left: 100, top: 900 });
    expect(viewport.scrollY).toBe(200);
    expect(onPosition).not.toHaveBeenCalled();
    const observer = ScrollResizeObserver.instances[0];
    viewport.maxTop = 1_000;
    viewport.maxLeft = 200;
    observer.notify();
    observer.notify();
    expect(viewport.frames.size).toBe(1);
    viewport.flushFrame();
    expect(onPosition).toHaveBeenLastCalledWith({ left: 100, top: 900 });
    expect(observer.isConnected).toBe(false);
    expect(vi.getTimerCount()).toBe(0);
  });

  it.each(['wheel', 'touchstart', 'pointerdown'])('%s 中止等待，内容加载不抢回位置', (event) => {
    const { onPosition } = track({ left: 0, top: 900 });
    viewport.dispatchEvent(new Event(event));
    expect(onPosition).toHaveBeenLastCalledWith({ left: 0, top: 200 });
    viewport.maxTop = 1_000;
    ScrollResizeObserver.instances[0].notify();
    viewport.flushFrame();
    expect(viewport.scrollY).toBe(200);
    viewport.scrollTo({ top: 300 });
    expect(onPosition).toHaveBeenLastCalledWith({ left: 0, top: 300 });
  });

  it('普通按键不打断恢复，滚动按键会停止恢复', () => {
    const { onPosition } = track({ left: 0, top: 900 });
    const press = (key: string) => {
      const event = new Event('keydown');
      Object.defineProperty(event, 'key', { value: key });
      viewport.dispatchEvent(event);
    };
    press('a');
    expect(onPosition).not.toHaveBeenCalled();
    press('PageDown');
    expect(onPosition).toHaveBeenLastCalledWith({ left: 0, top: 200 });
    expect(vi.getTimerCount()).toBe(0);
  });

  it('等待超时后接受实际位置，后续加载不再跳回旧位置', () => {
    const { onPosition } = track({ left: 0, top: 900 });
    vi.advanceTimersByTime(SCROLL_RESTORE_TIMEOUT_MS);
    expect(onPosition).toHaveBeenLastCalledWith({ left: 0, top: 200 });
    viewport.maxTop = 1_000;
    ScrollResizeObserver.instances[0].notify();
    viewport.flushFrame();
    expect(viewport.scrollY).toBe(200);
  });

  it('快速切换取消旧帧、监听及超时，已投递的旧回调不影响新页面', () => {
    const previous = track({ left: 0, top: 900 });
    const observer = ScrollResizeObserver.instances[0];
    observer.notify();
    const staleFrame = [...viewport.frames.values()][0];
    previous.dispose();
    const next = track({ left: 0, top: 0 });
    viewport.scrollTo({ top: 80 });
    observer.notify();
    staleFrame(0);
    vi.advanceTimersByTime(SCROLL_RESTORE_TIMEOUT_MS);
    expect(viewport.scrollY).toBe(80);
    expect(previous.onPosition).not.toHaveBeenCalled();
    expect(next.onPosition).toHaveBeenLastCalledWith({ left: 0, top: 80 });
    expect(viewport.frames.size).toBe(0);
  });

  it('在恢复完成前离开仍保留目标，重新进入时可以继续恢复', () => {
    let saved = { left: 0, top: 900 };
    const onPosition = (position: PageScrollPosition) => {
      saved = position;
    };
    const previous = track(saved, vi.fn(onPosition));
    previous.dispose();
    expect(saved.top).toBe(900);
    viewport.maxTop = 1_000;
    track(saved, vi.fn(onPosition));
    expect(viewport.scrollY).toBe(900);
    expect(saved.top).toBe(900);
  });
});
