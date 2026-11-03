import { beforeEach, expect, it, vi } from 'vitest';
import {
  shipOrder,
  getOrderPromotions,
  getAfterSalePromotions,
  refundAfterSale,
} from './fulfillmentApi';
const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => mockedRequest.mockReset());
const shipment = {
  orderNo: 'TR1',
  requestNo: 'S1',
  carrierCode: 'SF',
  trackingNo: 'T1',
  recipientName: '收件人',
  phone: '13800000000',
  address: '测试地址',
  shippedAt: '2026-10-25 10:00:00',
};
it('发货重试保留同一请求编号与完整载荷，不发送金额或状态', async () => {
  mockedRequest.mockResolvedValue({ data: shipment });
  const data = {
    requestNo: 'S1',
    carrierCode: 'SF',
    trackingNo: 'T1',
    recipientName: '收件人',
    phone: '13800000000',
    address: '测试地址',
  };
  await shipOrder('TR1', data);
  await shipOrder('TR1', data);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/orders/TR1/shipment',
    method: 'POST',
    data,
  });
});

it('售后审批不提交金额和状态，原路退款区分受理、成功与失败', async () => {
  const api = await import('./fulfillmentApi');
  const sale = {
    refundNo: 'RF1',
    orderNo: 'TR1',
    requestNo: 'REQ1',
    memberId: '1',
    paymentNo: 'PAY1',
    amount: '8.50',
    reason: '申请',
    status: 'WAIT_REFUND',
    requestedAt: '2026-10-28 12:00:00',
  };
  mockedRequest.mockResolvedValueOnce({ data: sale });
  await api.agreeAfterSale('RF1');
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/after-sales/RF1/agree',
    method: 'POST',
  });
  for (const status of ['WAITING', 'SUCCESS', 'FAILED']) {
    mockedRequest.mockResolvedValueOnce({ data: { refundNo: 'PR1', status } });
    expect((await api.refundAfterSale('RF1')).status).toBe(status);
    expect(mockedRequest).toHaveBeenLastCalledWith({
      url: '/trade/admin/after-sales/RF1/refund',
      method: 'POST',
    });
  }
  mockedRequest.mockResolvedValueOnce({ data: { refundNo: 'PR1', status: 'REFUNDED' } });
  await expect(api.refundAfterSale('RF1')).rejects.toThrow('退款状态');
});

it('调价与改址只发送业务字段并返回服务端确认', async () => {
  const api = await import('./fulfillmentApi');
  mockedRequest.mockResolvedValue({ data: true });
  await api.adjustOrderPrice('TR1', '88.00');
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/orders/TR1/price-adjustment',
    method: 'POST',
    data: { totalAmount: '88.00' },
  });
  const address = {
    receiverName: '李四',
    receiverMobile: '13900000000',
    receiverAreaId: '110102',
    receiverDetailAddress: '新地址',
  };
  await api.adjustOrderAddress('TR1', address);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/orders/TR1/address',
    method: 'PUT',
    data: address,
  });
});

it('优惠子资源使用原订单与售后路径，保留原包装', async () => {
  mockedRequest.mockResolvedValueOnce({
    data: {
      orderNo: 'TR1',
      snapshotStatus: 'COMPLETE',
      summary: {
        originalAmount: '100.00',
        discountAmount: '20.00',
        deliveryOriginalPrice: '0.00',
        deliveryDiscountPrice: '0.00',
        adjustmentAmount: '0.00',
        payAmount: '80.00',
      },
      promotions: [],
    },
  });
  expect((await getOrderPromotions('TR1')).summary.discountAmount).toBe('20.00');
  expect(mockedRequest).toHaveBeenLastCalledWith({ url: '/trade/admin/orders/TR1/promotions' });
  mockedRequest.mockResolvedValueOnce({
    data: {
      afterSaleNo: 'RF1',
      orderItemId: '11',
      quantity: 1,
      amount: '40.00',
      discountAmount: '10.00',
      deliveryDiscountPrice: '0.00',
      returnUsePoint: 0,
      deductGivePoint: 0,
      ruleVersion: 1,
      snapshotStatus: 'COMPLETE',
      benefitStatus: 'SUCCESS',
      promotions: [],
    },
  });
  expect((await getAfterSalePromotions('RF1')).amount).toBe('40.00');
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/after-sales/RF1/promotions',
  });
});
it('零现金售后没有支付退款编号，不能接受伪造的非零无流水成功', async () => {
  mockedRequest.mockResolvedValueOnce({
    data: {
      refundNo: null,
      afterSaleNo: 'RF1',
      channel: 'NONE',
      amount: '0.00',
      status: 'SUCCESS',
    },
  });
  expect(await refundAfterSale('RF1')).toEqual({ refundNo: null, status: 'SUCCESS' });
  mockedRequest.mockResolvedValueOnce({
    data: {
      refundNo: null,
      afterSaleNo: 'RF1',
      channel: 'NONE',
      amount: '1.00',
      status: 'SUCCESS',
    },
  });
  await expect(refundAfterSale('RF1')).rejects.toThrow();
});
