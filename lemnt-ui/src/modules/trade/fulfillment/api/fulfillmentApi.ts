import { request } from '@/api/request';
import {
  decodePage,
  decodeOrder,
  decodeDetail,
  decodeShipment,
  decodeRefund,
  decodeOrderLog,
  decodeAfterSaleLog,
  decodeOrderPromotions,
  decodeAfterSalePromotions,
  decodeRefundableItems,
} from '../fulfillmentDecoders';
import type { OrderAddressRequest, PageQuery, ShipmentRequest } from '../types';
const orderPath = (no: string) => `/trade/admin/orders/${encodeURIComponent(no)}`;
export async function getOrders(params: PageQuery) {
  return decodePage(await request<unknown>({ url: '/trade/admin/orders', params }), decodeOrder);
}
export async function getOrder(no: string) {
  return decodeDetail((await request<unknown>({ url: orderPath(no) })).data);
}
export async function shipOrder(no: string, data: ShipmentRequest) {
  return decodeShipment(
    (await request<unknown>({ url: `${orderPath(no)}/shipment`, method: 'POST', data })).data,
  );
}

/** 待支付订单调价，服务端重分摊商品行实付并写日志。 */
export async function adjustOrderPrice(no: string, totalAmount: string) {
  const result = await request<boolean>({
    url: `${orderPath(no)}/price-adjustment`,
    method: 'POST',
    data: { totalAmount },
  });
  return result.data === true;
}

/** 未发货快递单改址，不重算运费。 */
export async function adjustOrderAddress(no: string, data: OrderAddressRequest) {
  const result = await request<boolean>({ url: `${orderPath(no)}/address`, method: 'PUT', data });
  return result.data === true;
}

export async function getRefunds(params: PageQuery) {
  return decodePage(
    await request<unknown>({ url: '/trade/admin/after-sales', params }),
    decodeRefund,
  );
}
export async function rejectRefund(no: string, reason: string) {
  return decodeRefund(
    (
      await request<unknown>({
        url: `/trade/admin/after-sales/${encodeURIComponent(no)}/reject`,
        method: 'POST',
        data: { reason },
      })
    ).data,
  );
}

/** 查询已提交的订单操作日志。 */
export async function getOrderLogs(no: string) {
  const result = (await request<unknown>({ url: `${orderPath(no)}/logs` })).data;
  if (!Array.isArray(result)) throw new Error('订单日志列表格式不合法');
  return result.map(decodeOrderLog);
}

const afterSalePath = (no: string) => `/trade/admin/after-sales/${encodeURIComponent(no)}`;
/** 售后详情读取持久化事实。 */
export async function getAfterSale(no: string) {
  return decodeRefund((await request<unknown>({ url: afterSalePath(no) })).data);
}
/** 同意申请由服务端决定等待退货或退款。 */
export async function agreeAfterSale(no: string) {
  return decodeRefund(
    (await request<unknown>({ url: `${afterSalePath(no)}/agree`, method: 'POST' })).data,
  );
}
/** 确认收到买家寄回商品。 */
export async function receiveAfterSale(no: string) {
  return decodeRefund(
    (await request<unknown>({ url: `${afterSalePath(no)}/receive`, method: 'POST' })).data,
  );
}
/** 拒收原因独立传递，不让前端决定状态。 */
export async function refuseAfterSale(no: string, reason: string) {
  return decodeRefund(
    (
      await request<unknown>({
        url: `${afterSalePath(no)}/refuse`,
        method: 'POST',
        data: { reason },
      })
    ).data,
  );
}
/** 原路退款状态独立于售后状态，不把已受理当作成功。 */
export async function refundAfterSale(no: string) {
  const data = (await request<unknown>({ url: `${afterSalePath(no)}/refund`, method: 'POST' }))
    .data;
  if (!data || typeof data !== 'object') throw new Error('退款响应格式不合法');
  const row = data as Record<string, unknown>;
  if (
    (typeof row.refundNo !== 'string' &&
      !(
        row.refundNo == null &&
        row.channel === 'NONE' &&
        row.amount === '0.00' &&
        row.status === 'SUCCESS' &&
        row.afterSaleNo === no
      )) ||
    !['WAITING', 'SUCCESS', 'FAILED'].includes(String(row.status))
  )
    throw new Error('退款状态不合法');
  return {
    refundNo: typeof row.refundNo === 'string' ? row.refundNo : null,
    status: row.status as 'WAITING' | 'SUCCESS' | 'FAILED',
  };
}
/** 读取后台售后操作日志。 */
export async function getAfterSaleLogs(no: string) {
  const data = (await request<unknown>({ url: `${afterSalePath(no)}/logs` })).data;
  if (!Array.isArray(data)) throw new Error('售后日志列表格式不合法');
  return data.map(decodeAfterSaleLog);
}

/** 查询订单冻结优惠，失败由详情页明确展示。 */
export async function getOrderPromotions(no: string) {
  return decodeOrderPromotions(
    (await request<unknown>({ url: `${orderPath(no)}/promotions` })).data,
  );
}
/** 查询退单生成时冻结的归因和权益处理结果。 */
export async function getAfterSalePromotions(no: string) {
  return decodeAfterSalePromotions(
    (await request<unknown>({ url: `${afterSalePath(no)}/promotions` })).data,
  );
}

/** 查询订单明细已退及可申请数量；有处理中退单时服务端返回可申请0。 */
export async function getRefundableItems(no: string) {
  return decodeRefundableItems(
    (await request<unknown>({ url: `${orderPath(no)}/after-sale-items` })).data,
  );
}
