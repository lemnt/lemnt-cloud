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
