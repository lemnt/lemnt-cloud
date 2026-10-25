import { beforeEach, expect, it, vi } from 'vitest';
import { shipOrder, requestRefund, receiveOrder, rejectRefund, getOrders } from './fulfillmentApi';
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
it('退款只发送幂等编号与原因，不声明客户端金额或退款成功', async () => {
  const r = {
    refundNo: 'RF1',
    orderNo: 'TR1',
    requestNo: 'R1',
    memberId: 1,
    paymentNo: 'P1',
    amount: '17.00',
    reason: '原因',
    status: 'REQUESTED',
    requestedAt: '2026-10-25 10:00:00',
  };
  mockedRequest.mockResolvedValueOnce({ data: r });
  expect((await requestRefund('TR1', { requestNo: 'R1', reason: '原因' })).status).toBe(
    'REQUESTED',
  );
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/orders/TR1/refund-requests',
    method: 'POST',
    data: { requestNo: 'R1', reason: '原因' },
  });
  mockedRequest.mockResolvedValueOnce({
    data: { ...r, status: 'REJECTED', decisionReason: '驳回' },
  });
  await rejectRefund('RF1', '驳回');
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/refund-requests/RF1/reject',
    method: 'POST',
    data: { reason: '驳回' },
  });
});
it('收货请求没有客户端时间，分页筛选不丢失', async () => {
  mockedRequest.mockResolvedValueOnce({ data: { ...shipment, receivedAt: '2026-10-25 11:00:00' } });
  await receiveOrder('TR1');
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/orders/TR1/receive',
    method: 'POST',
  });
  mockedRequest.mockResolvedValueOnce({ data: { records: [], total: 0 } });
  await getOrders({ status: 'SHIPPED', pageNum: 2, pageSize: 20 });
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/orders',
    params: { status: 'SHIPPED', pageNum: 2, pageSize: 20 },
  });
});
