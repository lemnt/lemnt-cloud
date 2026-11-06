import { renderToStaticMarkup } from 'react-dom/server';
import { App } from 'antd';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { expect, it, vi } from 'vitest';
import { ExpressPage, ExpressTemplatePage } from './DeliveryPage';
import TradeConfigPage from '../../config/pages/TradeConfigPage';
import { pages } from '@/router/routes';
import type { ReactNode } from 'react';
const { permissions } = vi.hoisted(() => ({ permissions: new Set<string>() }));
vi.mock('@/shared/hooks/usePermission', () => ({
  usePermission: () => ({ can: (key: string) => permissions.has(key) }),
}));
function render(node: ReactNode) {
  return renderToStaticMarkup(
    <QueryClientProvider client={new QueryClient()}>
      <App>{node}</App>
    </QueryClientProvider>,
  );
}
it('三个入口分别拒绝无权限用户，拥有公司权限不能进入模板和配置', () => {
  permissions.clear();
  for (const Page of [ExpressPage, ExpressTemplatePage, TradeConfigPage])
    expect(render(<Page />)).toContain('没有');
  permissions.add('trade:delivery-express:list');
  expect(render(<ExpressPage />)).toContain('快递公司');
  expect(render(<ExpressTemplatePage />)).toContain('没有运费模板');
  expect(render(<TradeConfigPage />)).toContain('没有交易配置');
});
it('仅有列表权限不出现新增操作，三个菜单component均注册', () => {
  permissions.clear();
  permissions.add('trade:delivery-template:list');
  expect(render(<ExpressTemplatePage />)).not.toContain('新增运费模板');
  permissions.add('trade:delivery-template:create');
  expect(render(<ExpressTemplatePage />)).toContain('新增运费模板');
  for (const key of [
    'trade/delivery/express/index',
    'trade/delivery/express-template/index',
    'trade/config/index',
  ])
    expect(pages[key]).toBeDefined();
});
