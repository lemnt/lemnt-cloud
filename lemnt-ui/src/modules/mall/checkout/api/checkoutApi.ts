import { request } from '@/api/request';
import {
  decodeArray,
  decodeCoupon,
  decodeCouponManagementRecord,
  decodeTemplate,
  decodeBoolean,
  positiveId,
} from '../checkoutDecoders';
import type { CouponTemplateCreateRequest } from '../types';

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

/** 修改仅影响后续发券。 */
export async function updateCouponTemplate(id: string, data: CouponTemplateCreateRequest) {
  return decodeBoolean(
    (
      await request<unknown>({
        url: `/mall/admin/coupon-templates/${positiveId(id)}`,
        method: 'PUT',
        data,
      })
    ).data,
  );
}
/** 删除模板保留已发券权益。 */
export async function deleteCouponTemplate(id: string) {
  return decodeBoolean(
    (
      await request<unknown>({
        url: `/mall/admin/coupon-templates/${positiveId(id)}`,
        method: 'DELETE',
      })
    ).data,
  );
}

/** 会员持券分页，使用统一 rows/total。 */
export async function getCouponRecords(params: {
  memberId?: string;
  templateId?: string;
  status?: string;
  pageNum: number;
  pageSize: number;
}) {
  const response = await request<unknown>({ url: '/mall/admin/coupons', params });
  if (
    typeof response.total !== 'number' ||
    !Number.isSafeInteger(response.total) ||
    response.total < 0
  )
    throw new Error('持券分页总数不合法');
  return {
    data: decodeArray(response.rows, decodeCouponManagementRecord),
    total: response.total,
    success: true,
  };
}
/** 撤回未使用或已过期券。 */
export async function revokeCoupon(memberId: string, couponId: string) {
  return decodeBoolean(
    (
      await request<unknown>({
        url: `/mall/admin/members/${positiveId(memberId)}/coupons/${positiveId(couponId)}`,
        method: 'DELETE',
      })
    ).data,
  );
}
