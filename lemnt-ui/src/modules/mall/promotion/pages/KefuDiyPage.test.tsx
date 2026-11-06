import { renderToStaticMarkup } from 'react-dom/server';
import { App } from 'antd';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { expect, it, vi } from 'vitest';
import { KefuPage } from './KefuPage';
import { DiyPageView } from './DiyPage';
import { pages } from '@/router/routes';

const { permissions } = vi.hoisted(() => ({ permissions: new Set<string>() }));
vi.mock('@/shared/hooks/usePermission', () => ({
  usePermission: () => ({ can: (key: string) => permissions.has(key) }),
}));
function render(node: React.ReactNode) {
  return renderToStaticMarkup(
    <QueryClientProvider client={new QueryClient()}>
      <App>{node}</App>
    </QueryClientProvider>,
  );
}
it('客服与装修在缺少列表权限时拒绝', () => {
  permissions.clear();
  expect(render(<KefuPage />)).toContain('没有客服会话列表权限');
  expect(render(<DiyPageView />)).toContain('没有装修管理权限');
});
it('客服与装修菜单已注册', () => {
  expect(pages['mall/promotion/kefu/index']).toBeDefined();
  expect(pages['mall/promotion/diy/index']).toBeDefined();
});
