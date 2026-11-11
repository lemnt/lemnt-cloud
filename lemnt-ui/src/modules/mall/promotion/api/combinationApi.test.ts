import { beforeEach, expect, it, vi } from 'vitest';
import * as api from './combinationApi';
import type { CombinationActivitySave } from '../types';

const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => mockedRequest.mockReset());

const base: CombinationActivitySave = {
  name: '两人团',
  spuId: '1',
  totalLimitCount: 10,
  singleLimitCount: 2,
  startTime: '2026-11-01 00:00:00',
  endTime: '2026-11-02 00:00:00',
  userSize: 2,
  virtualGroup: true,
  limitDuration: 24,
  products: [{ skuId: '3', combinationPrice: '5' }],
};

it('拼团活动分页走促销路径并严格解码', async () => {
  mockedRequest.mockResolvedValue({
    rows: [
      {
        id: '9223372036854775807',
        name: '两人团',
        spuId: '1',
        spuName: '商品',
        picUrl: null,
        combinationPrice: '5.00',
        totalLimitCount: 10,
        singleLimitCount: 2,
        startTime: '2026-11-01 00:00:00',
        endTime: '2026-11-02 00:00:00',
        userSize: 2,
        virtualGroup: true,
        status: 0,
        limitDuration: 24,
        products: [{ skuId: '3', combinationPrice: '5.00' }],
      },
    ],
    total: 1,
  });
  const page = await api.getCombinationActivityPage({ pageNum: 1, pageSize: 20 });
  expect(page.records[0].id).toBe('9223372036854775807');
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/promotion/combination-activities',
    params: { name: undefined, status: undefined, spuId: undefined, pageNum: 1, pageSize: 20 },
  });
  mockedRequest.mockResolvedValue({ rows: [{ ...base, id: '1', status: 2 }], total: 1 });
  await expect(api.getCombinationActivityPage({ pageNum: 1, pageSize: 20 })).rejects.toThrow(
    '状态',
  );
});

it('拼团商品价格、人数和重复SKU校验', () => {
  expect(api.combinationPayload(base).products[0].combinationPrice).toBe('5.00');
  expect(() => api.combinationPayload({ ...base, userSize: 1 })).toThrow('两人');
  expect(() => api.combinationPayload({ ...base, limitDuration: 0 })).toThrow('限制时长');
  expect(() => api.combinationPayload({ ...base, virtualGroup: 'x' as never })).toThrow('虚拟成团');
  expect(() =>
    api.combinationPayload({
      ...base,
      products: [
        { skuId: '3', combinationPrice: '5' },
        { skuId: '3', combinationPrice: '6' },
      ],
    }),
  ).toThrow('重复');
  expect(() =>
    api.combinationPayload({ ...base, products: [{ skuId: '3', combinationPrice: '0' }] }),
  ).toThrow('大于零');
});

it('拼团记录团长编号0保留为字符串并校验状态', async () => {
  mockedRequest.mockResolvedValue({
    rows: [
      {
        id: '9',
        activityId: '1',
        combinationPrice: '5.00',
        spuId: '1',
        spuName: '商品',
        picUrl: null,
        skuId: '3',
        count: 1,
        userId: '2',
        nickname: '会员',
        avatar: null,
        headId: 0,
        status: 0,
        orderId: null,
        userSize: 2,
        userCount: 1,
        virtualGroup: false,
        expireTime: '2026-11-01 01:00:00',
        startTime: '2026-11-01 00:00:00',
        endTime: null,
      },
    ],
    total: 1,
  });
  const page = await api.getCombinationRecordPage({ pageNum: 1, pageSize: 20 });
  expect(page.records[0].headId).toBe('0');
  mockedRequest.mockResolvedValue({
    rows: [
      {
        id: '9',
        activityId: '1',
        combinationPrice: '5.00',
        spuId: '1',
        spuName: '商品',
        picUrl: null,
        skuId: '3',
        count: 1,
        userId: '2',
        nickname: '会员',
        avatar: null,
        headId: 0,
        status: 3,
        orderId: null,
        userSize: 2,
        userCount: 1,
        virtualGroup: false,
        expireTime: '2026-11-01 01:00:00',
        startTime: '2026-11-01 00:00:00',
        endTime: null,
      },
    ],
    total: 1,
  });
  await expect(api.getCombinationRecordPage({ pageNum: 1, pageSize: 20 })).rejects.toThrow('状态');
});
