import { findRoute } from '@/router/menu';
import type { Location } from 'react-router-dom';
import type { MenuRoute } from '@/router/menu';

export const MAX_CACHED_PAGES = 10;

export interface CachedPage {
  component: string;
  location: Location;
}

/** 保活仅面向已授权菜单和固定入口。 */
export function getCacheableComponent(menus: MenuRoute[], pathname: string): string | undefined {
  if (pathname === '/index') return 'home/index';
  if (pathname === '/user/profile') return 'system/user/profile/index';
  const route = findRoute(menus, pathname);
  if (!route?.component || route.meta?.noCache || route.meta?.link) return undefined;
  return route.component;
}

/** 同一路径共享实例；只更新当前页位置快照，其他页面保留自己的查询参数。 */
export function updateCachedPages(cached: CachedPage[], current?: CachedPage): CachedPage[] {
  if (!current) return cached;
  const latest = cached.at(-1);
  if (latest?.component === current.component && latest.location === current.location) {
    return cached;
  }
  return [
    ...cached.filter((entry) => entry.location.pathname !== current.location.pathname),
    current,
  ].slice(-MAX_CACHED_PAGES);
}
