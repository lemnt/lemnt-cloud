import { renderToStaticMarkup } from 'react-dom/server';
import { App } from 'antd';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { expect, it, vi } from 'vitest';
import { RewardActivityPage, DiscountActivityPage } from './ActivityPage';
import { pages } from '@/router/routes';
import type { ReactNode } from 'react';
const { permissions } = vi.hoisted(() => ({ permissions: new Set<string>() }));
vi.mock('@/shared/hooks/usePermission', () => ({
  usePermission: () => ({ can: (key: string) => permissions.has(key) }),
}));
function render(node: ReactNode, closed = false) {
  const cache = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  for (const kind of ['reward', 'discount'])
    cache.setQueryData(['promotion', kind, 'page', {}, { pageNum: 1, pageSize: 20 }], {
      records: closed
        ? [
            {
              id: '7',
              name: '已关闭测试活动',
              status: 1,
              startTime: '2026-11-01 00:00:00',
              endTime: '2026-11-12 00:00:00',
            },
          ]
        : [],
      total: closed ? 1 : 0,
    });
  return renderToStaticMarkup(
    <QueryClientProvider client={cache}>
      <App>{node}</App>
    </QueryClientProvider>,
  );
}
it('两类页面在没有列表权限时拒绝，不暴露活动表单', () => {
  permissions.clear();
  for (const Page of [RewardActivityPage, DiscountActivityPage]) {
    const html = render(<Page />);
    expect(html).toContain('没有活动列表权限');
    expect(html).not.toContain('添加商品SKU');
    expect(html).not.toContain('添加阶梯');
  }
});
it('新增权限独立于列表权限，关闭活动只提供允许的删除操作', () => {
  permissions.clear();
  permissions.add('mall:promotion:list');
  expect(render(<RewardActivityPage />)).not.toContain('新增满减活动');
  permissions.add('mall:promotion:create');
  expect(render(<RewardActivityPage />)).toContain('新增满减活动');
  expect(render(<DiscountActivityPage />)).toContain('新增折扣活动');
  permissions.delete('mall:promotion:create');
  permissions.add('mall:promotion:delete');
  const html = render(<RewardActivityPage />, true);
  expect(html).toContain('已关闭测试活动');
  expect(html).toContain('删除');
  expect(html).not.toContain('编辑');
  expect(html).not.toContain('详情');
});

it('菜单component键分别注册满减与折扣页面', () => {
  expect(pages['mall/promotion/reward-activity/index']).toBeDefined();
  expect(pages['mall/promotion/discount-activity/index']).toBeDefined();
});
