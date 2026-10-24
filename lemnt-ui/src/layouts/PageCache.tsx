import { Activity, Suspense, useLayoutEffect, useState } from 'react';
import { App, ConfigProvider, Spin } from 'antd';
import { Route, Routes, useLocation } from 'react-router-dom';
import { pages } from '@/router/routes';
import { getCacheableComponent, updateCachedPages } from './pageCacheState';
import { usePageScroll } from './usePageScroll';
import type { ReactNode } from 'react';
import type { MenuRoute } from '@/router/menu';
import type { CachedPage } from './pageCacheState';

interface PageCacheProps {
  menus: MenuRoute[];
  children: ReactNode;
}

interface CachedPageViewProps {
  entry: CachedPage;
  isVisible: boolean;
}

interface UncachedPageViewProps {
  children: ReactNode;
}

function CachedPageView({ entry, isVisible }: CachedPageViewProps) {
  const [container, setContainer] = useState<HTMLDivElement | null>(null);
  usePageScroll(container, isVisible);
  const Page = pages[entry.component];
  return (
    <div ref={setContainer} hidden={!isVisible}>
      {container ? (
        <Activity mode={isVisible ? 'visible' : 'hidden'}>
          {/* 弹窗和下拉框随页面容器隐藏，避免保活页面的 Portal 留在 body 上。 */}
          <ConfigProvider getPopupContainer={() => container}>
            <App component={false}>
              <Suspense fallback={<Spin />}>
                {/* 使用公开的 location 覆盖，避免隐藏页读取到其他页面的 URL。 */}
                <Routes location={entry.location}>
                  <Route path="*" element={<Page />} />
                </Routes>
              </Suspense>
            </App>
          </ConfigProvider>
        </Activity>
      ) : (
        <Spin />
      )}
    </div>
  );
}

function UncachedPageView({ children }: UncachedPageViewProps) {
  const [container, setContainer] = useState<HTMLDivElement | null>(null);
  usePageScroll(container, true);
  return (
    <div ref={setContainer}>
      <Suspense fallback={<Spin />}>{children}</Suspense>
    </div>
  );
}

export function PageCache({ menus, children }: PageCacheProps) {
  const location = useLocation();
  const [cached, setCached] = useState<CachedPage[]>([]);
  const component = getCacheableComponent(menus, location.pathname);
  const current = component && pages[component] ? { component, location } : undefined;
  const entries = updateCachedPages(cached, current);
  useLayoutEffect(() => {
    // 统一由页面实例恢复位置，避免浏览器历史恢复覆盖另一页的缓存位置。
    const previous = window.history.scrollRestoration;
    window.history.scrollRestoration = 'manual';
    return () => {
      window.history.scrollRestoration = previous;
    };
  }, []);
  // 导航提交前同步登记，避免先渲染临时页面再换到缓存容器导致重复挂载。
  if (entries !== cached) setCached(entries);

  return (
    <>
      {entries.map((entry) => (
        <CachedPageView
          key={entry.location.pathname}
          entry={entry}
          isVisible={current?.location.pathname === entry.location.pathname}
        />
      ))}
      {!current && <UncachedPageView key={location.pathname}>{children}</UncachedPageView>}
    </>
  );
}
