import { renderToStaticMarkup } from 'react-dom/server';
import { App } from 'antd';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { expect, it, vi } from 'vitest';
import { PickUpVerifyPage } from './PickUpVerifyPage';
import { pages } from '@/router/routes';

const { permissions } = vi.hoisted(() => ({ permissions: new Set<string>() }));
vi.mock('@/shared/hooks/usePermission', () => ({
  usePermission: () => ({ can: (key: string) => permissions.has(key) }),
}));
it('没有核销查询权限时拒绝', () => {
  permissions.clear();
  const html = renderToStaticMarkup(
    <QueryClientProvider client={new QueryClient()}>
      <App>
        <PickUpVerifyPage />
      </App>
    </QueryClientProvider>,
  );
  expect(html).toContain('没有自提核销查询权限');
});
it('店员核销台已注册路由', () => {
  expect(pages['trade/pick-up-verify/index']).toBeDefined();
});
