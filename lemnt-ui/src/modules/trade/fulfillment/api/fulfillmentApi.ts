import { request } from '@/api/request';
import {
  decodePage,
  decodeOrder,
  decodeDetail,
  decodeShipment,
  decodeRefund,
} from '../fulfillmentDecoders';
import type { PageQuery, ShipmentRequest, RefundCreateRequest } from '../types';
const orderPath = (no: string) => `/trade/admin/orders/${encodeURIComponent(no)}`;
export async function getOrders(params: PageQuery) {
  return decodePage(
    (await request<unknown>({ url: '/trade/admin/orders', params })).data,
    decodeOrder,
  );
}
export async function getOrder(no: string) {
  return decodeDetail((await request<unknown>({ url: orderPath(no) })).data);
}
export async function shipOrder(no: string, data: ShipmentRequest) {
  return decodeShipment(
    (await request<unknown>({ url: `${orderPath(no)}/shipment`, method: 'POST', data })).data,
  );
}
export async function receiveOrder(no: string) {
  return decodeShipment(
    (await request<unknown>({ url: `${orderPath(no)}/receive`, method: 'POST' })).data,
  );
}
export async function requestRefund(no: string, data: RefundCreateRequest) {
  return decodeRefund(
    (await request<unknown>({ url: `${orderPath(no)}/refund-requests`, method: 'POST', data }))
      .data,
  );
}
export async function getRefunds(params: PageQuery) {
  return decodePage(
    (await request<unknown>({ url: '/trade/admin/refund-requests', params })).data,
    decodeRefund,
  );
}
export async function rejectRefund(no: string, reason: string) {
  return decodeRefund(
    (
      await request<unknown>({
        url: `/trade/admin/refund-requests/${encodeURIComponent(no)}/reject`,
        method: 'POST',
        data: { reason },
      })
    ).data,
  );
}
