import { beforeEach, expect, it, vi } from 'vitest';
import * as api from './pointApi';
import type { PointActivitySave } from '../types';

const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => mockedRequest.mockReset());

const base: PointActivitySave = {
  spuId: '1',
  remark: null,
  sort: 0,
  products: [{ skuId: '2', exchangeCount: 2, point: 100, price: '0', stock: 3 }],
};

it('积分活动分页走促销路径且补差允许零', async () => {
  mockedRequest.mockResolvedValue({
    rows: [
      {
        id: '9223372036854775807',
        spuId: '1',
        spuName: '商品',
        picUrl: null,
        status: 0,
        remark: null,
        sort: 0,
        stock: 3,
        totalStock: 3,
        point: 100,
        price: '0.00',
        marketPrice: '10.00',
        products: [
          {
            id: '9',
            spuId: '1',
            skuId: '2',
            exchangeCount: 2,
            point: 100,
            price: '0.00',
            stock: 3,
          },
        ],
      },
    ],
    total: 1,
  });
  const page = await api.getPointActivityPage({ pageNum: 1, pageSize: 20 });
  expect(page.records[0].price).toBe('0.00');
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/promotion/point-activities',
    params: { status: undefined, pageNum: 1, pageSize: 20 },
  });
  mockedRequest.mockResolvedValue({
    rows: [
      {
        id: '1',
        spuId: '1',
        spuName: '商品',
        picUrl: null,
        status: 2,
        remark: null,
        sort: 0,
        stock: 3,
        totalStock: 3,
        point: 100,
        price: '0.00',
        marketPrice: '10.00',
        products: [
          {
            id: '9',
            spuId: '1',
            skuId: '2',
            exchangeCount: 2,
            point: 100,
            price: '0.00',
            stock: 3,
          },
        ],
      },
    ],
    total: 1,
  });
  await expect(api.getPointActivityPage({ pageNum: 1, pageSize: 20 })).rejects.toThrow('状态');
});

it('积分兑换次数、积分和库存校验，重复SKU拒绝', () => {
  expect(api.pointPayload(base).products[0]).toEqual({
    skuId: '2',
    exchangeCount: 2,
    point: 100,
    price: '0.00',
    stock: 3,
  });
  expect(() =>
    api.pointPayload({ ...base, products: [{ ...base.products[0], exchangeCount: 0 }] }),
  ).toThrow('兑换次数');
  expect(() =>
    api.pointPayload({ ...base, products: [{ ...base.products[0], point: -1 }] }),
  ).toThrow('积分');
  expect(() =>
    api.pointPayload({ ...base, products: [base.products[0], base.products[0]] }),
  ).toThrow('重复');
  expect(() =>
    api.pointPayload({ ...base, products: [{ ...base.products[0], stock: 0 }] }),
  ).toThrow('库存合计');
});
