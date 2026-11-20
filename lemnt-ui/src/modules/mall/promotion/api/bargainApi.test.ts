import { beforeEach, expect, it, vi } from 'vitest';
import * as api from './bargainApi';
import type { BargainActivitySave } from '../types';

const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => mockedRequest.mockReset());

const base: BargainActivitySave = {
  name: '砍价',
  startTime: '2026-11-01 00:00:00',
  endTime: '2026-11-02 00:00:00',
  spuId: '1',
  skuId: '2',
  bargainFirstPrice: '10',
  bargainMinPrice: '8',
  stock: 5,
  helpMaxCount: 3,
  bargainCount: 1,
  totalLimitCount: 1,
  randomMinPrice: '1',
  randomMaxPrice: '2',
};

it('砍价活动分页走促销路径并保持金额精度', async () => {
  mockedRequest.mockResolvedValue({
    rows: [
      {
        id: '9223372036854775807',
        name: '砍价',
        startTime: '2026-11-01 00:00:00',
        endTime: '2026-11-02 00:00:00',
        status: 0,
        spuId: '1',
        spuName: '商品',
        picUrl: null,
        skuPrice: '12.00',
        skuId: '2',
        bargainFirstPrice: '10.00',
        bargainMinPrice: '8.00',
        stock: 5,
        totalStock: 5,
        helpMaxCount: 3,
        bargainCount: 1,
        totalLimitCount: 1,
        randomMinPrice: '1.00',
        randomMaxPrice: '2.00',
        successCount: 0,
      },
    ],
    total: 1,
  });
  const page = await api.getBargainActivityPage({ pageNum: 1, pageSize: 20 });
  expect(page.records[0].bargainFirstPrice).toBe('10.00');
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/promotion/bargain-activities',
    params: { name: undefined, status: undefined, pageNum: 1, pageSize: 20 },
  });
  mockedRequest.mockResolvedValue({
    rows: [
      {
        id: '1',
        name: '砍价',
        startTime: '2026-11-01 00:00:00',
        endTime: '2026-11-02 00:00:00',
        status: 2,
        spuId: '1',
        spuName: '商品',
        picUrl: null,
        skuPrice: '12.00',
        skuId: '2',
        bargainFirstPrice: '10.00',
        bargainMinPrice: '8.00',
        stock: 5,
        totalStock: 5,
        helpMaxCount: 3,
        bargainCount: 1,
        totalLimitCount: 1,
        randomMinPrice: '1.00',
        randomMaxPrice: '2.00',
        successCount: 0,
      },
    ],
    total: 1,
  });
  await expect(api.getBargainActivityPage({ pageNum: 1, pageSize: 20 })).rejects.toThrow('状态');
});

it('砍价底价、随机区间和正整数校验', () => {
  expect(api.bargainPayload(base).bargainFirstPrice).toBe('10.00');
  expect(() => api.bargainPayload({ ...base, bargainMinPrice: '10' })).toThrow('低于');
  expect(() => api.bargainPayload({ ...base, randomMaxPrice: '11' })).toThrow('起始价');
  expect(() => api.bargainPayload({ ...base, randomMinPrice: '3', randomMaxPrice: '2' })).toThrow(
    '最小',
  );
  expect(() => api.bargainPayload({ ...base, stock: 0 })).toThrow('库存');
  expect(() => api.bargainPayload({ ...base, startTime: base.endTime })).toThrow('结束时间');
});

it('砍价记录状态1至3及订单编号严格解码', async () => {
  mockedRequest.mockResolvedValue({
    rows: [
      {
        id: '9',
        userId: '1',
        activityId: '2',
        spuId: '3',
        spuName: '商品',
        picUrl: null,
        skuId: '4',
        bargainFirstPrice: '10.00',
        bargainPrice: '8.00',
        status: 3,
        endTime: null,
        orderId: null,
      },
    ],
    total: 1,
  });
  const page = await api.getBargainRecordPage({ pageNum: 1, pageSize: 20 });
  expect(page.records[0].status).toBe(3);
  mockedRequest.mockResolvedValue({
    rows: [
      {
        id: '1',
        userId: '1',
        activityId: '2',
        spuId: '3',
        spuName: '商品',
        picUrl: null,
        skuId: '4',
        bargainFirstPrice: '10.00',
        bargainPrice: '8.00',
        status: 0,
        endTime: null,
        orderId: null,
      },
    ],
    total: 1,
  });
  await expect(api.getBargainRecordPage({ pageNum: 1, pageSize: 20 })).rejects.toThrow('状态');
});
