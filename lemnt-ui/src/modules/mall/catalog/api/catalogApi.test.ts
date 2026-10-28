import { beforeEach, expect, it, vi } from 'vitest';
import { getSpuList, getCategoryList } from './catalogApi';
const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => mockedRequest.mockReset());
it('商品分页使用 rows/total，分类使用 data', async () => {
  mockedRequest.mockResolvedValue({
    code: 200,
    rows: [
      {
        id: 3,
        code: 'SP1',
        name: '商品',
        categoryId: 1,
        brandId: 2,
        status: 'OFF_SHELF',
        skus: [],
      },
    ],
    total: 8,
  });
  expect(await getSpuList({ pageNum: 2, pageSize: 10 })).toEqual({
    data: [
      {
        id: '3',
        code: 'SP1',
        name: '商品',
        deliveryTypes: [],
        deliveryTemplateId: null,
        categoryId: '1',
        brandId: '2',
        status: 'OFF_SHELF',
        skus: [],
        description: null,
        image: null,
      },
    ],
    total: 8,
    success: true,
  });
  mockedRequest.mockResolvedValue({ code: 200, data: [] });
  expect(await getCategoryList()).toEqual([]);
});

it('规格选项使用独立字典接口，值查询必须携带规格编号', async () => {
  const api = await import('./catalogApi');
  mockedRequest.mockResolvedValue({
    code: 200,
    data: [{ id: '9223372036854775807', name: '红色' }],
  });
  expect(await api.getSpecOptions('颜色')).toEqual([{ id: '9223372036854775807', name: '红色' }]);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/specs/simple',
    params: { name: '颜色', limit: 100 },
  });
  await api.getSpecValueOptions('9223372036854775807', '红');
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/spec-values/simple',
    params: { specId: '9223372036854775807', name: '红', limit: 100 },
  });
  mockedRequest.mockResolvedValue({ code: 200, data: { id: '2', name: '已选择的值' } });
  expect((await api.getSpecValueOption('2')).name).toBe('已选择的值');
});
