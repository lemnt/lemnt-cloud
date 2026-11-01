import { renderToStaticMarkup } from 'react-dom/server';
import { App } from 'antd';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { expect, it, vi } from 'vitest';
import { PickUpStorePage } from './PickUpStorePage';
import { pages } from '@/router/routes';

const { permissions } = vi.hoisted(() => ({ permissions: new Set<string>() }));
vi.mock('@/shared/hooks/usePermission', () => ({
  usePermission: () => ({ can: (key: string) => permissions.has(key) }),
}));
it('没有门店列表权限时拒绝', () => {
  permissions.clear();
  const html = renderToStaticMarkup(
    <QueryClientProvider client={new QueryClient()}>
      <App><PickUpStorePage /></App>
    </QueryClientProvider>,
  );
  expect(html).toContain('没有自提门店权限');
});
it('自提门店页面已注册路由', () => {
  expect(pages['trade/pick-up-store/index']).toBeDefined();
});
