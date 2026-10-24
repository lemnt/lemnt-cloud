import { describe, expect, it } from 'vitest';
import { findRoute, normalizeMenus } from './menu';
import { hasPermission, safeRedirect } from '../shared/auth/permissions';

describe('菜单与权限兼容', () => {
  it('保留隐藏页面和嵌套绝对地址，过滤排除模块', () => {
    const routes = normalizeMenus([
      {
        path: 'system',
        children: [{ path: 'post', component: 'system/post/index', hidden: true }],
      },
      { path: 'monitor', children: [{ path: 'job', component: 'monitor/job/index' }] },
      { path: 'other', component: 'tool/gen/index' },
    ]);
    expect(routes).toHaveLength(1);
    expect(findRoute(routes, '/system/post')?.hidden).toBe(true);
  });
  it('识别超级权限且拒绝未授权操作', () => {
    expect(hasPermission(['*:*:*'], 'system:user:add')).toBe(true);
    expect(hasPermission(['system:user:list'], 'system:user:add')).toBe(false);
  });
  it('只允许站内登录重定向', () => {
    expect(safeRedirect('//evil.test')).toBe('/index');
    expect(safeRedirect('/\\evil.test')).toBe('/index');
    expect(safeRedirect('/system/post?page=2')).toBe('/system/post?page=2');
  });
});
