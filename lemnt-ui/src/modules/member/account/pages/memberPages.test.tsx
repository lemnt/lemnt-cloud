import { describe, expect, it, vi } from 'vitest';
import { renderToStaticMarkup } from 'react-dom/server';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { App } from 'antd';
import MemberUserPage from './MemberUserPage';
import { MemberLevelPage, MemberGroupPage, MemberTagPage } from './MemberMetadataPage';
import MemberRecordPage from './MemberRecordPage';
import MemberConfigPage from './MemberConfigPage';
import type { ReactNode } from 'react';
import { memberKeys, type MemberPointConfig } from '../types';
const { permissions } = vi.hoisted(() => ({ permissions: new Set<string>() }));
vi.mock('@/shared/hooks/usePermission', () => ({
  usePermission: () => ({ can: (v: string) => permissions.has(v) }),
}));
function render(child: ReactNode, pointConfig?: MemberPointConfig | null) {
  const cache = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  if (pointConfig !== undefined) cache.setQueryData(memberKeys.config, pointConfig);
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
    expect(html).toContain('已接入结算');
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
  it('签到规则新增权限独立于修改权限', () => {
    permissions.clear();
    permissions.add('member:sign-in-config:query');
    permissions.add('member:sign-in-config:update');
    expect(render(<MemberConfigPage />)).not.toContain('新增规则');
    permissions.add('member:sign-in-config:create');
    expect(render(<MemberConfigPage />)).toContain('新增规则');
  });
  it('未配置时提供明确的未保存草稿，积分上限按积分展示', () => {
    permissions.clear();
    permissions.add('member:config:query');
    permissions.add('member:config:update');
    const html = render(<MemberConfigPage />, null);
    expect(html).toContain('尚未设置积分配置');
    expect(html).toContain('每单抵扣积分上限');
    expect(html).not.toContain('抵扣上限（分）');
  });
});
