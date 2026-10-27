import { request } from '@/api/request';
import {
  decodeArray,
  decodeCart,
  decodeCoupon,
  decodeTemplate,
  decodeQuote,
  decodeReceipt,
  decodeBoolean,
  positiveId,
} from '../checkoutDecoders';
import type {
  CartSaveRequest,
  CouponTemplateCreateRequest,
  CheckoutQuoteRequest,
  CheckoutSubmitRequest,
} from '../types';
export async function getCartList(memberId: string) {
  return decodeArray(
    (await request<unknown>({ url: `/mall/admin/members/${memberId}/cart` })).data,
    decodeCart,
  );
}
export async function saveCartItem(memberId: string, productId: string, data: CartSaveRequest) {
  return decodeCart(
    (
      await request<unknown>({
        url: `/mall/admin/members/${memberId}/cart/${productId}`,
        method: 'PUT',
        data,
      })
    ).data,
  );
}
export async function deleteCartItem(memberId: string, productId: string) {
  return decodeBoolean(
    (
      await request<unknown>({
        url: `/mall/admin/members/${memberId}/cart/${productId}`,
        method: 'DELETE',
      })
    ).data,
  );
}
export async function getCouponTemplates() {
  return decodeArray(
    (await request<unknown>({ url: '/mall/admin/coupon-templates' })).data,
    decodeTemplate,
  );
}
export async function createCouponTemplate(data: CouponTemplateCreateRequest) {
  return decodeTemplate(
    (await request<unknown>({ url: '/mall/admin/coupon-templates', method: 'POST', data })).data,
  );
}
export async function enableCouponTemplate(id: string, enabled: boolean) {
  return decodeBoolean(
    (
      await request<unknown>({
        url: `/mall/admin/coupon-templates/${id}/enabled`,
        method: 'PUT',
        data: { enabled },
      })
    ).data,
  );
}
export async function getMemberCoupons(memberId: string) {
  return decodeArray(
    (await request<unknown>({ url: `/mall/admin/members/${memberId}/coupons` })).data,
    decodeCoupon,
  );
}
export async function issueCoupon(memberId: string, templateId: string) {
  return decodeCoupon(
    (
      await request<unknown>({
        url: `/mall/admin/members/${memberId}/coupons/${templateId}`,
        method: 'POST',
      })
    ).data,
  );
}
export async function getCheckoutQuote(memberId: string, data: CheckoutQuoteRequest) {
  return decodeQuote(
    (
      await request<unknown>({
        url: `/mall/admin/members/${memberId}/checkout/quote`,
        method: 'POST',
        data,
      })
    ).data,
  );
}
export async function submitCheckout(memberId: string, data: CheckoutSubmitRequest) {
  return decodeReceipt(
    (
      await request<unknown>({
        url: `/mall/admin/members/${memberId}/checkout`,
        method: 'POST',
        data,
      })
    ).data,
  );
}
export async function getCheckoutReceipt(orderNo: string) {
  return decodeReceipt(
    (await request<unknown>({ url: `/mall/admin/checkouts/${encodeURIComponent(orderNo)}` })).data,
  );
}

export async function getPublicCouponTemplates() {
  return decodeArray(
    (await request<unknown>({ url: '/mall/admin/coupon-templates/public-claim' })).data,
    positiveId,
  );
}
export async function setCouponPublicClaim(id: string, enabled: boolean) {
  const result = decodeBoolean(
    (
      await request<unknown>({
        url: `/mall/admin/coupon-templates/${positiveId(id)}/public-claim`,
        method: 'PUT',
        data: { enabled },
      })
    ).data,
  );
  if (!result) throw new Error('公开领取设置未确认成功');
  return result;
}
