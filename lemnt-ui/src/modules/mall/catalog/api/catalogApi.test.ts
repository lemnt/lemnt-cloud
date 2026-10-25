import { beforeEach, expect, it, vi } from 'vitest';
import {
  adjustSkuStock,
  getSpuList,
  getCategoryList,
  updateSpuShelf,
  updateSku,
} from './catalogApi';
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
it('库存调整保留请求编号、负数及原因，不发送绝对库存', async () => {
  mockedRequest.mockResolvedValue({
    code: 200,
    data: { requestNo: 'R1', productId: 5, delta: -2, reason: '盘点', resultingStock: 0 },
  });
  const data = { requestNo: 'R1', delta: -2, reason: '盘点' };
  await adjustSkuStock('5', data);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/skus/5/stock',
    method: 'POST',
    data,
  });
});
it('SKU资料与SPU上下架使用各自的HTTP契约', async () => {
  mockedRequest.mockResolvedValue({ code: 200, data: true });
  await updateSpuShelf('3', false);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/spus/3/shelf',
    method: 'PUT',
    data: { onShelf: false },
  });
  await updateSku('5', { name: 'SKU', price: 9.25 });
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/skus/5',
    method: 'PUT',
    data: { name: 'SKU', price: 9.25 },
  });
});
