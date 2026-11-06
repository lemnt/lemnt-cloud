import { renderToStaticMarkup } from 'react-dom/server';
import { App } from 'antd';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { expect, it, vi } from 'vitest';
import { PaymentPage, PaymentRefundPage, PaymentChannelPage } from './PaymentPage';
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
it('支付单、支付退单及渠道各自检查权限，均不提供手工造单入口', () => {
  permissions.clear();
  for (const Page of [PaymentPage, PaymentRefundPage, PaymentChannelPage])
    expect(render(<Page />)).toContain('没有');
  permissions.add('trade:payment:list');
  expect(render(<PaymentRefundPage />)).toContain('没有支付退单列表权限');
  expect(render(<PaymentPage />)).not.toContain('发起支付');
  permissions.clear();
  permissions.add('trade:payment-channel:list');
  const html = render(<PaymentChannelPage />);
  expect(html).toContain('支持的支付渠道');
  expect(html).not.toContain('新增');
  expect(html).not.toContain('密钥');
});
it('三个后台菜单component均已注册', () => {
  for (const key of [
    'trade/payment/index',
    'trade/payment-refund/index',
    'trade/payment-channel/index',
  ])
    expect(pages[key]).toBeDefined();
});
