import { beforeEach, expect, it, vi } from 'vitest';
import * as api from './seckillApi';
import type { SeckillActivitySave, SeckillConfigSave } from '../types';

const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => mockedRequest.mockReset());

const config: SeckillConfigSave = {
  name: '早场',
  startTime: '09:00:00',
  endTime: '10:00:00',
  sliderPicUrls: ['https://example.test/banner.png'],
  status: 0,
};
const activity: SeckillActivitySave = {
  spuId: '1',
  name: '限时秒杀',
  remark: null,
  startTime: '2026-11-01 00:00:00',
  endTime: '2026-11-02 00:00:00',
  sort: 0,
  configIds: ['2'],
  totalLimitCount: 10,
  singleLimitCount: 5,
  products: [{ skuId: '3', seckillPrice: '5', stock: 2 }],
};

it('时段与活动分页使用促销路径并保持大编号精度', async () => {
  mockedRequest.mockResolvedValue({
    rows: [{
      id: '9223372036854775807', name: '早场', startTime: '09:00:00', endTime: '10:00:00',
      sliderPicUrls: [], status: 0, activityCount: 1,
    }],
    total: 1,
  });
  expect((await api.getSeckillConfigPage({ pageNum: 1, pageSize: 20 })).records[0].id)
    .toBe('9223372036854775807');
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/promotion/seckill-configs',
    params: { name: undefined, status: undefined, pageNum: 1, pageSize: 20 },
  });
  mockedRequest.mockResolvedValue({ rows: [{ ...config, id: '1', status: 2 }], total: 1 });
  await expect(api.getSeckillConfigPage({ pageNum: 1, pageSize: 20 })).rejects.toThrow('状态');
  mockedRequest.mockResolvedValue({ rows: [{ id: Number.MAX_SAFE_INTEGER + 1 }], total: 1 });
  await expect(api.getSeckillConfigPage({ pageNum: 1, pageSize: 20 })).rejects.toThrow('精度');
});

it('活动商品价格精确校验，重复SKU和零库存合计拒绝', () => {
  const payload = api.activityPayload({
    ...activity,
    products: [{ skuId: '3', seckillPrice: '5.1', stock: 2 }],
  });
  expect(payload.products[0].seckillPrice).toBe('5.10');
  expect(() =>
    api.activityPayload({
      ...activity,
      products: [{ skuId: '3', seckillPrice: '5.126', stock: 2 }],
    }),
  ).toThrow('两位小数');
  expect(() =>
    api.activityPayload({
      ...activity,
      products: [
        { skuId: '3', seckillPrice: '5', stock: 1 },
        { skuId: '3', seckillPrice: '6', stock: 1 },
      ],
    }),
  ).toThrow('重复');
  expect(() =>
    api.activityPayload({ ...activity, products: [{ skuId: '3', seckillPrice: '5', stock: 0 }] }),
  ).toThrow('库存合计');
  expect(() => api.activityPayload({ ...activity, startTime: activity.endTime })).toThrow('结束时间');
  expect(() => api.activityPayload({ ...activity, configIds: [] })).toThrow('时段');
});

it('时段时间点格式和金额零值不进入请求', () => {
  expect(() => api.configPayload({ ...config, endTime: '09:00:00' })).toThrow('结束时间');
  expect(() => api.configPayload({ ...config, startTime: '9:00' })).toThrow('结束时间');
  expect(() =>
    api.activityPayload({ ...activity, products: [{ skuId: '3', seckillPrice: '0', stock: 1 }] }),
  ).toThrow('大于零');
  expect(api.configPayload({ ...config, name: '  早场  ', sliderPicUrls: undefined as never }).name)
    .toBe('早场');
});
