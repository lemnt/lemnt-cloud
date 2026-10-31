import { beforeEach, expect, it, vi } from 'vitest';
import * as api from './promotionApi';
import type { ActivityEditorValues } from '../types';
const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => mockedRequest.mockReset());
const base = { name: '双十一', startTime: '2026-11-01 00:00:00', endTime: '2026-11-12 00:00:00' };
const row = { ...base, id: '9223372036854775807', status: 0, remark: null };
const reward: ActivityEditorValues = {
  ...base,
  conditionType: 'PRICE',
  productScope: 'ALL',
  rules: [{ limit: '100', discountPrice: '10' }],
};
it('两类分页分别使用实际服务前缀，开启状态0不丢失，大ID不转浮点', async () => {
  mockedRequest.mockResolvedValue({ rows: [row], total: 1 });
  for (const kind of ['reward', 'discount'] as const) {
    expect(
      (await api.getActivityPage(kind, { name: ' 双十一 ', status: 0, pageNum: 2, pageSize: 20 }))
        .records[0].id,
    ).toBe(row.id);
    expect(mockedRequest).toHaveBeenLastCalledWith({
      url: `/mall/promotion/${kind}-activities`,
      params: { name: '双十一', status: 0, pageNum: 2, pageSize: 20 },
    });
  }
  mockedRequest.mockResolvedValue({
    rows: [{ ...row, id: Number.MAX_SAFE_INTEGER + 1 }],
    total: 1,
  });
  await expect(api.getActivityPage('reward', { pageNum: 1, pageSize: 20 })).rejects.toThrow('精度');
  mockedRequest.mockResolvedValue({ rows: [{ ...row, status: 2 }], total: 1 });
  await expect(api.getActivityPage('reward', { pageNum: 1, pageSize: 20 })).rejects.toThrow('状态');
});
it('满减阶梯精确比较超出JS安全整数的相邻金额，清除隐藏范围和额外字段', () => {
  const result = api.rewardPayload({
    ...reward,
    productScopeValues: ['7'],
    rules: [
      { limit: '9007199254740993.00', discountPrice: '0.01' },
      { limit: '9007199254740993.01', discountPrice: '0.02' },
    ],
    ...{ status: 1, memberId: '7' },
  });
  expect(result.productScopeValues).toEqual([]);
  expect(result.rules[1].limit).toBe('9007199254740993.01');
  expect(result).not.toHaveProperty('status');
  expect(result).not.toHaveProperty('memberId');
  expect(() =>
    api.rewardPayload({ ...reward, rules: [{ limit: '10', discountPrice: '11' }] }),
  ).toThrow('不能超过');
  expect(() =>
    api.rewardPayload({
      ...reward,
      rules: [
        { limit: '10', discountPrice: '1' },
        { limit: '10', discountPrice: '2' },
      ],
    }),
  ).toThrow('递增');
});
it('满件数必须整数，指定范围不能为空，金额和时间错误不进入请求', () => {
  expect(() =>
    api.rewardPayload({
      ...reward,
      conditionType: 'COUNT',
      rules: [{ limit: '1.5', discountPrice: '1' }],
    }),
  ).toThrow('整数');
  expect(() =>
    api.rewardPayload({ ...reward, productScope: 'SPU_IDS', productScopeValues: [] }),
  ).toThrow('请选择');
  expect(() => api.rewardPayload({ ...reward, endTime: base.startTime })).toThrow('结束时间');
  expect(() =>
    api.rewardPayload({ ...reward, rules: [{ limit: '100', discountPrice: '0.001' }] }),
  ).toThrow('两位小数');
  expect(
    api.rewardPayload({
      ...reward,
      conditionType: 'COUNT',
      rules: [{ limit: '2', discountPrice: '5' }],
    }).rules[0],
  ).toEqual({ limit: '2.00', discountPrice: '5.00' });
});
it('折扣按几折精确转换，减价和折扣互斥，重复SKU拒绝', () => {
  const product = {
    spuId: '9223372036854775807',
    skuId: '7',
    discountType: 'PERCENT' as const,
    discountRate: '8.888',
    discountPrice: '100',
  };
  expect(api.discountPayload({ ...base, products: [product] }).products[0]).toEqual({
    spuId: row.id,
    skuId: '7',
    discountType: 'PERCENT',
    discountPercent: 8888,
    discountPrice: null,
  });
  expect(
    api.discountPayload({ ...base, products: [{ ...product, discountRate: '0.001' }] }).products[0]
      .discountPercent,
  ).toBe(1);
  expect(
    api.discountPayload({ ...base, products: [{ ...product, discountRate: '9.999' }] }).products[0]
      .discountPercent,
  ).toBe(9999);
  expect(
    api.discountPayload({
      ...base,
      products: [{ ...product, discountType: 'PRICE', discountPrice: '0.01' }],
    }).products[0],
  ).toMatchObject({ discountPrice: '0.01', discountPercent: null });
  expect(() =>
    api.discountPayload({ ...base, products: [product, { ...product, spuId: '8' }] }),
  ).toThrow('重复');
  for (const discountRate of ['0', '10', '8.8888'])
    expect(() =>
      api.discountPayload({ ...base, products: [{ ...product, discountRate }] }),
    ).toThrow('折扣');
});
it('详情分别解码两种规则，原金额字符串和SKU基点正确保留', async () => {
  mockedRequest.mockResolvedValueOnce({
    data: {
      ...row,
      conditionType: 'COUNT',
      productScope: 'SPU_IDS',
      productScopeValues: [7],
      rules: [{ limit: '2', discountPrice: '5.00' }],
    },
  });
  expect(await api.getReward(row.id)).toMatchObject({
    productScopeValues: ['7'],
    rules: [{ limit: '2.00', discountPrice: '5.00' }],
  });
  mockedRequest.mockResolvedValueOnce({
    data: {
      ...row,
      products: [{ spuId: '7', skuId: '8', discountType: 'PERCENT', discountPercent: 8000 }],
    },
  });
  expect((await api.getDiscount(row.id)).products[0]).toMatchObject({
    discountPercent: 8000,
    discountPrice: null,
  });
});
it('增改关闭删除使用独立实际路径，false不会误报成功', async () => {
  const payload = api.rewardPayload(reward);
  mockedRequest.mockResolvedValueOnce({ data: row.id });
  expect(await api.createReward(payload)).toBe(row.id);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/promotion/reward-activities',
    method: 'POST',
    data: payload,
  });
  mockedRequest.mockResolvedValue({ data: true });
  await api.updateReward('7', payload);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/promotion/reward-activities/7',
    method: 'PUT',
    data: payload,
  });
  await api.closeActivity('discount', '7');
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/promotion/discount-activities/7/close',
    method: 'PUT',
    data: undefined,
  });
  await api.deleteActivity('reward', '7');
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/promotion/reward-activities/7',
    method: 'DELETE',
    data: undefined,
  });
  mockedRequest.mockResolvedValue({ data: false });
  await expect(api.closeActivity('reward', '7')).rejects.toThrow('未确认');
});
