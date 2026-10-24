import { useLayoutEffect, useRef } from 'react';
import { trackPageScroll } from './pageScroll';
import type { PageScrollPosition } from './pageScroll';

/** 位置随页面实例保留；实例淘汰或会话重置时一并释放。 */
export function usePageScroll(container: HTMLElement | null, isVisible: boolean): void {
  const position = useRef<PageScrollPosition>({ left: 0, top: 0 });

  useLayoutEffect(() => {
    if (!container || !isVisible) return;
    return trackPageScroll(container, position.current, (next) => {
      position.current = next;
    });
  }, [container, isVisible]);
}
