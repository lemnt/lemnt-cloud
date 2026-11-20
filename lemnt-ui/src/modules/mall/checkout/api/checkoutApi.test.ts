import { beforeEach, expect, it, vi } from 'vitest';
const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => mockedRequest.mockReset());

it('公开领券设置与模板启用开关分离，默认不推断开放', async () => {
  const api = await import('./checkoutApi');
  mockedRequest.mockResolvedValueOnce({ data: ['3', 7] });
  expect(await api.getPublicCouponTemplates()).toEqual(['3', '7']);
  mockedRequest.mockResolvedValueOnce({ data: true });
  await api.setCouponPublicClaim('3', true);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/admin/coupon-templates/3/public-claim',
    method: 'PUT',
    data: { enabled: true },
  });
  mockedRequest.mockResolvedValueOnce({ data: false });
  await expect(api.setCouponPublicClaim('3', false)).rejects.toThrow('未确认');
});

it('更新模板和删除模板都校验服务端确认，更新保留相对有效期和范围字段', async () => {
  const api = await import('./checkoutApi');
  const payload = {
    code: 'TERM',
    name: '相对券',
    kind: 'FIXED' as const,
    threshold: 0,
    fixedAmount: 3,
    totalCount: -1,
    validFrom: null,
    validUntil: null,
    takeType: 'USER' as const,
    takeLimitCount: 2,
    productScope: 'SPU_IDS' as const,
    productScopeValues: ['11'],
    validityType: 'TERM' as const,
    fixedStartTerm: 1,
    fixedEndTerm: 7,
  };
  mockedRequest.mockResolvedValueOnce({ data: true });
  expect(await api.updateCouponTemplate('3', payload)).toBe(true);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/admin/coupon-templates/3',
    method: 'PUT',
    data: payload,
  });
  mockedRequest.mockResolvedValueOnce({ data: true });
  expect(await api.deleteCouponTemplate('3')).toBe(true);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/admin/coupon-templates/3',
    method: 'DELETE',
  });
});
