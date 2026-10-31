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
        keyword: null,
        introduction: null,
        sliderPicUrls: [],
        sort: null,
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

it('创建商品发送规格对象列表，不再发送临时编号映射', async () => {
  const { createSpu } = await import('./catalogApi');
  const data = {
    code: 'SP',
    name: '商品',
    keyword: '关键字',
    introduction: '简介',
    sliderPicUrls: [],
    sort: 0,
    categoryId: '1',
    brandId: '2',
    deliveryTypes: [],
    skus: [
      {
        code: 'SKU',
        name: '红色款',
        price: 10,
        stock: 2,
        properties: [{ propertyId: '3', valueId: '7' }],
      },
    ],
  };
  mockedRequest.mockResolvedValue({
    code: 200,
    data: {
      ...data,
      id: '10',
      status: 'OFF_SHELF',
      skus: [
        {
          ...data.skus[0],
          id: '11',
          spuId: '10',
          price: '10.00',
          status: 'ON_SHELF',
          properties: [{ propertyId: '3', propertyName: '颜色', valueId: '7', valueName: '红色' }],
        },
      ],
    },
  });
  const result = await createSpu(data);
  expect(mockedRequest).toHaveBeenCalledWith({ url: '/mall/spus', method: 'POST', data });
  expect(result.data.skus[0].properties[0].valueName).toBe('红色');
  expect('specValueIds' in result.data.skus[0]).toBe(false);
});

it('商品关联记录使用各自权限接口，隐藏状态false不能丢失', async () => {
  const { getProductFavorites, getProductBrowseHistory } = await import('./catalogApi');
  mockedRequest.mockResolvedValue({
    rows: [
      {
        id: '9223372036854775807',
        userId: '2',
        spuId: '3',
        price: '10.00',
        createTime: '2026-10-29 00:00:00',
        userDeleted: false,
      },
    ],
    total: 1,
  });
  const params = { spuId: '3', userDeleted: false, pageNum: 1, pageSize: 20 };
  expect((await getProductBrowseHistory(params)).data[0].userDeleted).toBe(false);
  expect(mockedRequest).toHaveBeenLastCalledWith({ url: '/mall/product-browse-history', params });
  expect((await getProductFavorites({ spuId: '3', pageNum: 1, pageSize: 20 })).data[0].id).toBe(
    '9223372036854775807',
  );
  expect(mockedRequest.mock.lastCall?.[0].url).toBe('/mall/product-favorites');
});

it('商品列表和状态数量采用一致的精确分类品牌条件，计数不混入状态或分页', async () => {
  const api = await import('./catalogApi');
  const filters = { name: ' 商品 ', categoryId: '9223372036854775807', brandId: '2' };
  mockedRequest.mockResolvedValueOnce({ rows: [], total: 0 });
  await api.getSpuList({ ...filters, status: 'RECYCLED', pageNum: 2, pageSize: 20 });
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/spus',
    params: {
      ...filters,
      name: '商品',
      status: 'RECYCLED',
      stockStatus: undefined,
      pageNum: 2,
      pageSize: 20,
    },
  });
  mockedRequest.mockResolvedValueOnce({
    data: { onShelf: 2, offShelf: 0, recycled: 1, soldOut: 1, lowStock: 2 },
  });
  expect(
    await api.getSpuStatusCounts({ ...filters, ...{ status: 'RECYCLED', pageNum: 2 } }),
  ).toEqual({ onShelf: 2, offShelf: 0, recycled: 1, soldOut: 1, lowStock: 2 });
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/spus/count',
    params: { ...filters, name: '商品' },
  });
  mockedRequest.mockResolvedValueOnce({
    data: { onShelf: Number.MAX_SAFE_INTEGER + 1, offShelf: 0, recycled: 1 },
  });
  await expect(api.getSpuStatusCounts({})).rejects.toThrow('整数');
});
it('回收、恢复和删除调用既有路径，false或业务拒绝不报告成功', async () => {
  const api = await import('./catalogApi');
  mockedRequest.mockResolvedValueOnce({ data: true });
  expect(await api.recycleSpu('9')).toBe(true);
  expect(mockedRequest).toHaveBeenLastCalledWith({ url: '/mall/spus/9/recycle', method: 'POST' });
  mockedRequest.mockResolvedValueOnce({ data: true });
  expect(await api.restoreSpu('9')).toBe(true);
  expect(mockedRequest).toHaveBeenLastCalledWith({ url: '/mall/spus/9/restore', method: 'POST' });
  mockedRequest.mockResolvedValueOnce({ data: false });
  await expect(api.deleteSpu('9')).rejects.toThrow('未成功');
  mockedRequest.mockRejectedValueOnce(Error('商品仍有库存预占，不能删除'));
  await expect(api.deleteSpu('9')).rejects.toThrow('库存预占');
  mockedRequest.mockResolvedValueOnce({ data: true });
  expect(await api.deleteSpu('9')).toBe(true);
  expect(mockedRequest).toHaveBeenLastCalledWith({ url: '/mall/spus/9', method: 'DELETE' });
  await expect(api.recycleSpu('../9')).rejects.toThrow('编号');
  await expect(api.restoreSpu('9223372036854775808')).rejects.toThrow('范围');
});

it('库存分组独立于上下架状态，查询与统计使用实际库存字段', async () => {
  const api = await import('./catalogApi');
  mockedRequest.mockResolvedValueOnce({ rows: [], total: 0 });
  await api.getSpuList({ stockStatus: 'SOLD_OUT', pageNum: 1, pageSize: 10 });
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/spus',
    params: {
      name: undefined,
      categoryId: undefined,
      brandId: undefined,
      status: undefined,
      stockStatus: 'SOLD_OUT',
      pageNum: 1,
      pageSize: 10,
    },
  });
  mockedRequest.mockResolvedValueOnce({
    data: { onShelf: 1, offShelf: 2, recycled: 3, soldOut: 2, lowStock: 3 },
  });
  expect(await api.getSpuStatusCounts({})).toMatchObject({ soldOut: 2, lowStock: 3 });
});
