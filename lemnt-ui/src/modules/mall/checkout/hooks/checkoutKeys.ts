export const checkoutKeys = {
  all: ['mall-checkout'] as const,
  publicClaims: () => ['mall-checkout', 'public-claims'] as const,
  cart: (memberId: string) => [...checkoutKeys.all, 'cart', memberId] as const,
  templates: () => [...checkoutKeys.all, 'templates'] as const,
  coupons: (memberId: string) => [...checkoutKeys.all, 'coupons', memberId] as const,
  receipt: (orderNo: string) => [...checkoutKeys.all, 'receipt', orderNo] as const,
};
