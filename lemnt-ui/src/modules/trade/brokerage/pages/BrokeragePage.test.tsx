import { renderToStaticMarkup } from 'react-dom/server';
import { App } from 'antd';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { expect, it, vi } from 'vitest';
import { BrokeragePage } from './BrokeragePage';
import { pages } from '@/router/routes';

const { permissions } = vi.hoisted(() => ({ permissions: new Set<string>() }));
vi.mock('@/shared/hooks/usePermission', () => ({
  usePermission: () => ({ can: (key: string) => permissions.has(key) }),
}));
it('没有列表权限时拒绝分销管理', () => {
  permissions.clear();
  const html = renderToStaticMarkup(
    <QueryClientProvider client={new QueryClient()}>
      <App>
        <BrokeragePage />
      </App>
    </QueryClientProvider>,
  );
  expect(html).toContain('没有分销管理权限');
});
it('分销页面已注册菜单 component 键', () => {
  expect(pages['trade/brokerage/index']).toBeDefined();
});
