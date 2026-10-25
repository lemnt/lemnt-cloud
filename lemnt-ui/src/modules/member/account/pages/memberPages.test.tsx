import { describe, expect, it, vi } from 'vitest';
import { renderToStaticMarkup } from 'react-dom/server';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { App } from 'antd';
import MemberUserPage from './MemberUserPage';
import { MemberLevelPage, MemberGroupPage, MemberTagPage } from './MemberMetadataPage';
import MemberRecordPage from './MemberRecordPage';
import MemberConfigPage from './MemberConfigPage';
import type { ReactNode } from 'react';
const { permissions } = vi.hoisted(() => ({ permissions: new Set<string>() }));
vi.mock('@/shared/hooks/usePermission', () => ({
  usePermission: () => ({ can: (v: string) => permissions.has(v) }),
}));
function render(child: ReactNode) {
  const cache = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return renderToStaticMarkup(
    <QueryClientProvider client={cache}>
      <App>{child}</App>
    </QueryClientProvider>,
  );
}
describe('会员页面权限和首次加载状态', () => {
  it('所有页面在无权限时明确拒绝，不显示操作表单', () => {
    permissions.clear();
    for (const Page of [
      MemberUserPage,
      MemberLevelPage,
      MemberGroupPage,
      MemberTagPage,
      MemberRecordPage,
      MemberConfigPage,
    ]) {
      const html = render(<Page />);
      expect(html).toContain('没有');
      expect(html).toContain('权限');
      expect(html).not.toContain('新增规则');
    }
  });
  it('仅配置读取权限不会显示保存和签到规则操作', () => {
    permissions.clear();
    permissions.add('member:config:query');
    const html = render(<MemberConfigPage />);
    expect(html).toContain('积分交易配置');
    expect(html).toContain('尚未接入');
    expect(html).not.toContain('新增规则');
  });
  it('仅积分查询权限默认只提供积分记录类型', () => {
    permissions.clear();
    permissions.add('member:point:query');
    const html = render(<MemberRecordPage />);
    expect(html).toContain('积分流水');
    expect(html).not.toContain('调整积分');
    expect(html).not.toContain('经验流水');
  });
});
