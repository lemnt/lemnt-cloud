import { beforeEach, expect, it, vi } from 'vitest';
import * as api from './tradeConfigApi';
const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => mockedRequest.mockReset());
const snapshot: api.TradeConfig = {
  id: '1',
  afterSaleRefundReasons: ['不需要了'],
  afterSaleReturnReasons: ['质量问题'],
  deliveryExpressFreeEnabled: true,
  deliveryExpressFreePrice: 10001,
  deliveryPickUpEnabled: true,
  brokerageEnabled: false,
  brokerageEnabledCondition: 2,
  brokerageBindMode: 3,
  brokeragePosterUrls: ['https://example.test/poster'],
  brokerageFirstPercent: 7,
  brokerageSecondPercent: 3,
  brokerageWithdrawMinPrice: 30001,
  brokerageWithdrawFeePercent: 2,
  brokerageFrozenDays: 11,
  brokerageWithdrawTypes: [1, 6],
};
it('读取全量配置，编辑售后和包邮时精确保留未开放字段且不发送id', async () => {
  mockedRequest.mockResolvedValueOnce({ data: snapshot });
  const config = await api.getTradeConfig();
  expect(api.configValues(config)).toEqual({
    refundReasons: '不需要了',
    returnReasons: '质量问题',
    freeEnabled: true,
    freePrice: '100.01',
  });
  const values = {
    refundReasons: '不喜欢\n 不需要了 ',
    returnReasons: '',
    freeEnabled: false,
    freePrice: '0.29',
  };
  mockedRequest.mockResolvedValueOnce({ data: true });
  await api.saveTradeConfig(config, values);
  const { id: _, ...remaining } = snapshot;
  void _;
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/config',
    method: 'PUT',
    data: {
      ...remaining,
      afterSaleRefundReasons: ['不喜欢', '不需要了'],
      afterSaleReturnReasons: [],
      deliveryExpressFreeEnabled: false,
      deliveryExpressFreePrice: 29,
    },
  });
});
it('配置未初始化或结构损坏时拒绝编辑，不创建默认配置覆盖数据', async () => {
  mockedRequest.mockResolvedValueOnce({ data: null });
  await expect(api.getTradeConfig()).rejects.toThrow('初始化');
  mockedRequest.mockResolvedValueOnce({ data: { ...snapshot, brokerageBindMode: undefined } });
  await expect(api.getTradeConfig()).rejects.toThrow('数值');
});
it('原因禁止重复和超限，保存失败不报告成功', async () => {
  const values = api.configValues(snapshot);
  expect(() => api.configPayload(snapshot, { ...values, refundReasons: '重复\n 重复 ' })).toThrow(
    '重复',
  );
  expect(() => api.configPayload(snapshot, { ...values, returnReasons: '字'.repeat(201) })).toThrow(
    '200',
  );
  mockedRequest.mockResolvedValueOnce({ data: false });
  await expect(api.saveTradeConfig(snapshot, values)).rejects.toThrow('保存失败');
});
