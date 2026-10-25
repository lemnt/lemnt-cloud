import { beforeEach, expect, it, vi } from 'vitest';
import { getCheckoutQuote, submitCheckout, issueCoupon, saveCartItem } from './checkoutApi';
const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => mockedRequest.mockReset());
it('购物车更新不提交价格或身份替代字段，未选中和数量零值不被默认值覆盖', async () => {
  mockedRequest.mockResolvedValue({
    data: { id: 2, productId: 5, quantity: 1, selected: false, version: 0, sellable: false },
  });
  await saveCartItem('1', '5', { quantity: 1, selected: false });
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/members/1/cart/5',
    method: 'PUT',
    data: { quantity: 1, selected: false },
  });
});
it('同一提交保留请求编号和报价令牌，不发送客户端金额', async () => {
  mockedRequest.mockResolvedValue({
    data: {
      items: [],
      originalAmount: '20.00',
      discountAmount: '3.00',
      payableAmount: '17.00',
      orderNo: 'TR1',
      memberId: 1,
      status: 'RESERVING',
    },
  });
  const data = { requestNo: 'R1', productIds: ['5'], couponId: '8', quoteToken: 'a'.repeat(64) };
  await submitCheckout('1', data);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/members/1/checkout',
    method: 'POST',
    data,
  });
});
it('预览及发券使用后台目标会员路径，并保留明确的券实例状态', async () => {
  mockedRequest.mockResolvedValueOnce({
    data: {
      items: [],
      originalAmount: '10.00',
      discountAmount: '0.00',
      payableAmount: '10.00',
      quoteToken: 'a'.repeat(64),
    },
  });
  await getCheckoutQuote('1', { productIds: ['5'] });
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/members/1/checkout/quote',
    method: 'POST',
    data: { productIds: ['5'] },
  });
  mockedRequest.mockResolvedValueOnce({
    data: { id: 8, templateId: 2, memberId: 1, status: 'AVAILABLE' },
  });
  expect((await issueCoupon('1', '2')).id).toBe('8');
});
