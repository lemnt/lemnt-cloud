import { renderToStaticMarkup } from 'react-dom/server';
import { App } from 'antd';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { expect, it, vi } from 'vitest';
import { BannerPage } from './BannerPage';
import { pages } from '@/router/routes';
import type { ReactNode } from 'react';

const { permissions } = vi.hoisted(() => ({ permissions: new Set<string>() }));
vi.mock('@/shared/hooks/usePermission', () => ({
  usePermission: () => ({ can: (key: string) => permissions.has(key) }),
}));

function render(node: ReactNode) {
  const cache = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  cache.setQueryData(['banner', 'page', {}, { pageNum: 1, pageSize: 20 }], {
    records: [
      {
        id: '7',
        title: '测试 Banner',
        url: '/pages/store/index',
        picUrl: 'https://example.test/b.png',
        sort: 1,
        status: 0,
        position: 1,
        memo: null,
        browseCount: 3,
      },
    ],
    total: 1,
  });
  return renderToStaticMarkup(
    <QueryClientProvider client={cache}>
      <App>{node}</App>
    </QueryClientProvider>,
  );
}

it('没有列表权限时拒绝，不暴露新增入口', () => {
  permissions.clear();
  const html = render(<BannerPage />);
  expect(html).toContain('没有 Banner 列表权限');
  expect(html).not.toContain('新增 Banner');
});

it('列表、新增、编辑和删除权限相互独立', () => {
  permissions.clear();
  permissions.add('mall:promotion:list');
  let html = render(<BannerPage />);
  expect(html).toContain('测试 Banner');
  expect(html).not.toContain('新增 Banner');
  permissions.add('mall:promotion:create');
  expect(render(<BannerPage />)).toContain('新增 Banner');
  permissions.delete('mall:promotion:create');
  expect(render(<BannerPage />)).not.toContain('编辑');
  permissions.add('mall:promotion:update');
  expect(render(<BannerPage />)).toContain('编辑');
  expect(render(<BannerPage />)).not.toContain('详情');
  permissions.add('mall:promotion:query');
  expect(render(<BannerPage />)).toContain('详情');
  permissions.add('mall:promotion:delete');
  expect(render(<BannerPage />)).toContain('删除');
});

it('Banner 页面已注册菜单 component 键', () => {
  expect(pages['mall/promotion/banner/index']).toBeDefined();
});
