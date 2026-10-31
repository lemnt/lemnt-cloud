import { renderToStaticMarkup } from 'react-dom/server';
import { MemoryRouter } from 'react-router-dom';
import { App } from 'antd';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { expect, it, vi } from 'vitest';
import SpecPage, { SpecValuePage } from './SpecPage';
import { pages } from '@/router/routes';
import type { ReactNode } from 'react';
const { permissions } = vi.hoisted(() => ({ permissions: new Set<string>() }));
vi.mock('@/shared/hooks/usePermission', () => ({
  usePermission: () => ({ can: (key: string) => permissions.has(key) }),
}));
function render(node: ReactNode, path = '/mall/specs') {
  return renderToStaticMarkup(
    <MemoryRouter initialEntries={[path]}>
      <QueryClientProvider client={new QueryClient()}>
        <App>{node}</App>
      </QueryClientProvider>
    </MemoryRouter>,
  );
}
it('规格与规格值分别检查权限，不因有一方权限开放另一方', () => {
  permissions.clear();
  expect(render(<SpecPage />)).toContain('没有商品规格');
  permissions.add('mall:spec:list');
  expect(render(<SpecPage />)).toContain('商品规格');
  expect(render(<SpecValuePage />, '/mall/spec-values?specId=1')).toContain('没有规格值');
});
it('新增和值入口按操作权限显示，非法归属参数不发请求，组件已注册', () => {
  permissions.clear();
  permissions.add('mall:spec:list');
  expect(render(<SpecPage />)).not.toContain('新增规格');
  permissions.add('mall:spec:add');
  expect(render(<SpecPage />)).toContain('新增规格');
  expect(render(<SpecValuePage />, '/mall/spec-values?specId=0')).toContain('有效规格');
  permissions.clear();
  permissions.add('mall:spec-value:list');
  const html = render(<SpecValuePage />, '/mall/spec-values?specId=9223372036854775807');
  expect(html).toContain('9223372036854775807');
  expect(html).not.toContain('新增规格值');
  expect(html).not.toContain('创建时间范围');
  expect(pages['mall/spec/index']).toBeDefined();
  expect(pages['mall/spec-value/index']).toBeDefined();
});
