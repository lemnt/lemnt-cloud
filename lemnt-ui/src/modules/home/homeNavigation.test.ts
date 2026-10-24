import { expect, it } from 'vitest';
import { homeShortcuts } from './homeNavigation';

it('快捷入口由可见菜单和注册表共同限定，并按菜单顺序去重', () => {
  expect(
    homeShortcuts(
      [
        {
          path: '/system',
          children: [
            { path: '/system/user', component: 'system/user/index', meta: { title: '用户管理' } },
            { path: '/system/report', component: 'system/report/index' },
            { path: '/system/hidden', hidden: true, component: 'system/user/index' },
          ],
        },
        { path: '/system/user', component: 'system/user/index', name: '重复项' },
        {
          path: '/hidden',
          hidden: true,
          children: [{ path: '/hidden/child', component: 'system/user/index' }],
        },
      ],
      ['system/user/index'],
    ),
  ).toEqual([{ path: '/system/user', title: '用户管理' }]);
});

it('外链、协议相对地址和非站内地址不作为快捷入口', () => {
  expect(
    homeShortcuts(
      [
        { path: 'https://example.com', component: 'page' },
        { path: '//example.com', component: 'page' },
        { path: '/external', component: 'page', meta: { link: 'https://example.com' } },
      ],
      ['page'],
    ),
  ).toEqual([]);
});
