import { renderToStaticMarkup } from 'react-dom/server';
import { App } from 'antd';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { expect, it, vi } from 'vitest';
import SpuPage from './SpuPage';
import { catalogKeys } from '../hooks/catalogKeys';
import type { Spu } from '../types';
const { permissions } = vi.hoisted(() => ({ permissions: new Set<string>() }));
vi.mock('@/shared/hooks/usePermission', () => ({
  usePermission: () => ({ can: (key: string) => permissions.has(key) }),
}));
function render(status?: Spu['status'], withCounts = true) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  if (status) {
    const row: Spu = {
      id: '9',
      code: 'SP9',
      name: '测试商品',
      keyword: null,
      introduction: null,
      sliderPicUrls: [],
      sort: 0,
      categoryId: '1',
      brandId: '2',
      image: null,
      description: null,
      status,
      skus: [],
      deliveryTypes: [1],
      deliveryTemplateId: '3',
    };
    client.setQueryData(catalogKeys.spus({ status: 'ON_SHELF', pageNum: 1, pageSize: 10 }), {
      data: [row],
      total: 1,
      success: true,
    });
  }
  if (withCounts)
    client.setQueryData(catalogKeys.counts({}), {
      onShelf: 2,
      offShelf: 3,
      recycled: 4,
      soldOut: 1,
      lowStock: 2,
    });
  return renderToStaticMarkup(
    <QueryClientProvider client={client}>
      <App>
        <SpuPage />
      </App>
    </QueryClientProvider>,
  );
}
it('列表拒绝无权限访问，状态数量取服务端统计且未读取时显示未知', () => {
  permissions.clear();
  expect(render()).toContain('没有商品列表权限');
  permissions.add('mall:spu:list');
  const html = render();
  expect(html).toContain('出售中（2）');
  expect(html).toContain('仓库中（3）');
  expect(html).toContain('回收站（4）');
  expect(render(undefined, false)).toContain('出售中（—）');
  expect(html).toContain('已售罄（1）');
  expect(html).toContain('警戒库存（2）');
});
it('行操作按真实行状态限制，上架不能直接回收，回收站只能恢复或删除', () => {
  permissions.clear();
  for (const key of ['mall:spu:list', 'mall:spu:edit', 'mall:spu:query', 'mall:spu:remove'])
    permissions.add(key);
  const active = render('ON_SHELF');
  expect(active).toContain('下架');
  expect(active).not.toContain('移入回收站');
  expect(active).not.toContain('删除商品');
  const off = render('OFF_SHELF');
  expect(off).toContain('移入回收站');
  expect(off).not.toContain('恢复商品');
  const recycled = render('RECYCLED');
  expect(recycled).toContain('恢复商品');
  expect(recycled).toContain('删除商品');
  expect(recycled).not.toContain('移入回收站');
  expect(recycled).not.toContain('>修改<');
  expect(recycled).not.toContain('>上架<');
});
it('编辑权限不替代删除权限，只有列表权限不出现写入按钮', () => {
  permissions.clear();
  permissions.add('mall:spu:list');
  permissions.add('mall:spu:edit');
  expect(render('RECYCLED')).toContain('恢复商品');
  expect(render('RECYCLED')).not.toContain('删除商品');
  permissions.delete('mall:spu:edit');
  const html = render('OFF_SHELF');
  expect(html).not.toContain('移入回收站');
  expect(html).not.toContain('新增商品');
  expect(html).not.toContain('>上架<');
});
it('商品导出是独立权限，列表或编辑权限不隐式放开下载', () => {
  permissions.clear();
  permissions.add('mall:spu:list');
  permissions.add('mall:spu:edit');
  expect(render()).not.toContain('导出商品');
  permissions.add('mall:spu:export');
  expect(render()).toContain('导出商品');
});
