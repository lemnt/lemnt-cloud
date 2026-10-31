import { beforeEach, expect, it, vi } from 'vitest';
import { shipOrder } from './fulfillmentApi';
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
