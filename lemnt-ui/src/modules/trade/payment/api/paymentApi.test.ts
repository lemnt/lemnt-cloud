import { beforeEach, expect, it, vi } from 'vitest';
import * as api from './paymentApi';
const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => mockedRequest.mockReset());
const payment = {
  id: '9223372036854775807',
  paymentNo: 'PAY1',
  orderNo: 'ORDER1',
  channel: 'ALIPAY',
  channelTradeNo: null,
  amount: '9007199254740993.01',
  status: 'SUCCESS',
};
const refund = {
  refundNo: 'PR1',
  afterSaleNo: 'AS1',
  paymentNo: 'PAY1',
  orderNo: 'ORDER1',
  channel: 'ALIPAY',
  channelRefundNo: null,
  amount: '0.01',
  status: 'WAITING',
};
it('支付单分页保留精确金额和大ID，仅提交本接口支持的筛选', async () => {
  mockedRequest.mockResolvedValue({ rows: [payment], total: 1 });
  expect(
    (
      await api.getPayments({
        paymentNo: ' PAY1 ',
        orderNo: 'ORDER1',
        channel: 'ALIPAY',
        status: 'SUCCESS',
        pageNum: 2,
        pageSize: 20,
        ...{ afterSaleNo: 'ignored' },
      })
    ).records[0],
  ).toMatchObject({ id: payment.id, amount: payment.amount, channelTradeNo: null });
  expect(mockedRequest).toHaveBeenCalledWith({
    url: '/trade/admin/payments',
    params: {
      paymentNo: 'PAY1',
      orderNo: 'ORDER1',
      channel: 'ALIPAY',
      status: 'SUCCESS',
      pageNum: 2,
      pageSize: 20,
    },
  });
  mockedRequest.mockResolvedValue({
    rows: [{ ...payment, id: Number.MAX_SAFE_INTEGER + 1 }],
    total: 1,
  });
  await expect(api.getPayments({ pageNum: 1, pageSize: 20 })).rejects.toThrow('精度');
  mockedRequest.mockResolvedValue({ rows: [{ ...payment, amount: 0.01 }], total: 1 });
  await expect(api.getPayments({ pageNum: 1, pageSize: 20 })).rejects.toThrow('文本');
});
it('支付退单按售后单和支付单筛选，状态不跨用支付单枚举，详情独立路径', async () => {
  mockedRequest.mockResolvedValueOnce({ rows: [refund], total: 1 });
  expect(
    (
      await api.getRefunds({
        afterSaleNo: ' AS1 ',
        paymentNo: 'PAY1',
        status: 'WAITING',
        pageNum: 1,
        pageSize: 20,
        ...{ orderNo: 'ignored', channel: 'ignored' },
      })
    ).records[0],
  ).toEqual(refund);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/payment-refunds',
    params: { afterSaleNo: 'AS1', paymentNo: 'PAY1', status: 'WAITING', pageNum: 1, pageSize: 20 },
  });
  mockedRequest.mockResolvedValueOnce({ data: refund });
  expect(await api.getRefund('PR1')).toEqual(refund);
  expect(mockedRequest).toHaveBeenLastCalledWith({ url: '/trade/admin/payment-refunds/PR1' });
  mockedRequest.mockResolvedValueOnce({ data: payment });
  expect(await api.getPayment('PAY1')).toEqual(payment);
  expect(mockedRequest).toHaveBeenLastCalledWith({ url: '/trade/admin/payments/PAY1' });
  mockedRequest.mockResolvedValueOnce({ data: { ...refund, status: 'CLOSED' } });
  await expect(api.getRefund('PR1')).rejects.toThrow('状态');
});
it('支持渠道只保留目录字段，不添加已启用或已接通状态', async () => {
  mockedRequest.mockResolvedValue({
    data: [{ code: 'ALIPAY', name: '支付宝', ...{ enabled: true, merchantKey: 'ignored' } }],
  });
  expect(await api.getChannels()).toEqual([{ code: 'ALIPAY', name: '支付宝' }]);
  expect(mockedRequest).toHaveBeenCalledWith({ url: '/trade/admin/payment-channels' });
});
