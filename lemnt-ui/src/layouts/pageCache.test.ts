import { describe, expect, it } from 'vitest';
import { normalizeMenus } from '@/router/menu';
import { getCacheableComponent, MAX_CACHED_PAGES, updateCachedPages } from './pageCacheState';
import type { Location } from 'react-router-dom';
import type { CachedPage } from './pageCacheState';

function page(pathname: string, search = ''): CachedPage {
  const location: Location = { pathname, search, hash: '', state: null, key: pathname + search };
  return { component: 'system/user/index', location };
}

describe('页面保活规则', () => {
  it('沿用菜单 noCache，并保留隐藏页配置', () => {
    const menus = normalizeMenus([
      {
        path: '/system',
        children: [
          { path: 'user', component: 'system/user/index', meta: { noCache: true } },
          { path: 'role', component: 'system/role/index', hidden: true },
          { path: 'post', component: 'system/post/index', meta: { noCache: false } },
          { path: 'external', component: 'system/post/index', meta: { link: 'https://a.test' } },
        ],
      },
    ]);
    expect(getCacheableComponent(menus, '/system/user')).toBeUndefined();
    expect(getCacheableComponent(menus, '/system/role')).toBe('system/role/index');
    expect(getCacheableComponent(menus, '/system/post')).toBe('system/post/index');
    expect(getCacheableComponent(menus, '/system/external')).toBeUndefined();
    expect(getCacheableComponent(menus, '/system/missing')).toBeUndefined();
  });

  it('固定入口可保活', () => {
    expect(getCacheableComponent([], '/index')).toBe('home/index');
    expect(getCacheableComponent([], '/user/profile')).toBe('system/user/profile/index');
  });

  it('同一路径查询参数变化仍共享实例，隐藏页位置快照不变', () => {
    const user = page('/system/user', '?page=2');
    const role = page('/system/role', '?name=operator');
    const cached = updateCachedPages([user], role);
    expect(cached[0]).toBe(user);
    const updatedUser = page('/system/user', '?page=3');
    const updated = updateCachedPages(cached, updatedUser);
    expect(updated).toEqual([role, updatedUser]);
    expect(updated[0]).toBe(role);
    expect(user.location.search).toBe('?page=2');
    expect(updateCachedPages(updated, updatedUser)).toBe(updated);
    expect(updateCachedPages(updated)).toBe(updated);
  });

  it('超过上限淘汰最久未访问页面，重新访问保护当前实例', () => {
    let cached: CachedPage[] = [];
    for (let index = 0; index < MAX_CACHED_PAGES; index++) {
      cached = updateCachedPages(cached, page(`/page/${index}`));
    }
    const first = cached[0];
    cached = updateCachedPages(cached, first);
    cached = updateCachedPages(cached, page('/page/new'));
    expect(cached).toHaveLength(MAX_CACHED_PAGES);
    expect(cached.some((entry) => entry.location.pathname === '/page/1')).toBe(false);
    expect(cached.find((entry) => entry.location.pathname === '/page/0')).toBe(first);
  });
});
