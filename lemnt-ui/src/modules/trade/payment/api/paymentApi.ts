import { request } from '@/api/request';
export interface PaymentRecord {
  id: string;
  paymentNo: string;
  orderNo: string;
  channel: string;
  channelTradeNo: string | null;
  amount: string;
  status: 'WAITING' | 'SUCCESS' | 'CLOSED';
}
export interface RefundRecord {
  refundNo: string;
  afterSaleNo: string;
  paymentNo: string;
  orderNo: string;
  channel: string;
  channelRefundNo: string | null;
  amount: string;
  status: 'WAITING' | 'SUCCESS' | 'FAILED';
}
export interface PaymentFilter {
  paymentNo?: string;
  orderNo?: string;
  channel?: string;
  status?: string;
  pageNum: number;
  pageSize: number;
}
export interface RefundFilter {
  afterSaleNo?: string;
  paymentNo?: string;
  status?: string;
  pageNum: number;
  pageSize: number;
}
function record(v: unknown): Record<string, unknown> {
  if (!v || typeof v !== 'object' || Array.isArray(v)) throw Error('支付交易响应不合法');
  return v as Record<string, unknown>;
}
function text(v: unknown): string {
  if (typeof v !== 'string') throw Error('支付交易文本不合法');
  return v;
}
function optional(v: unknown): string | null {
  return v == null ? null : text(v);
}
function money(v: unknown): string {
  const value = text(v);
  if (!/^\d+\.\d{2}$/.test(value)) throw Error('支付金额必须为精确两位小数字符串');
  return value;
}
function payment(raw: unknown): PaymentRecord {
  const v = record(raw),
    id = typeof v.id === 'number' && Number.isSafeInteger(v.id) ? String(v.id) : v.id;
  if (typeof id !== 'string' || !/^[1-9]\d{0,18}$/.test(id) || BigInt(id) > 9223372036854775807n)
    throw Error('支付单编号不合法或已丢失精度');
  if (v.status !== 'WAITING' && v.status !== 'SUCCESS' && v.status !== 'CLOSED')
    throw Error('支付状态不合法');
  return {
    id,
    paymentNo: text(v.paymentNo),
    orderNo: text(v.orderNo),
    channel: text(v.channel),
    channelTradeNo: optional(v.channelTradeNo),
    amount: money(v.amount),
    status: v.status,
  };
}
function refund(raw: unknown): RefundRecord {
  const v = record(raw);
  if (v.status !== 'WAITING' && v.status !== 'SUCCESS' && v.status !== 'FAILED')
    throw Error('支付退单状态不合法');
  return {
    refundNo: text(v.refundNo),
    afterSaleNo: text(v.afterSaleNo),
    paymentNo: text(v.paymentNo),
    orderNo: text(v.orderNo),
    channel: text(v.channel),
    channelRefundNo: optional(v.channelRefundNo),
    amount: money(v.amount),
    status: v.status,
  };
}
function array(v: unknown): unknown[] {
  if (!Array.isArray(v)) throw Error('支付交易列表不合法');
  return v;
}
function total(v: unknown): number {
  if (typeof v !== 'number' || !Number.isSafeInteger(v) || v < 0) throw Error('分页总数不合法');
  return v;
}
export async function getPayments(q: PaymentFilter) {
  const r = await request<unknown>({
    url: '/trade/admin/payments',
    params: {
      paymentNo: q.paymentNo?.trim() || undefined,
      orderNo: q.orderNo?.trim() || undefined,
      channel: q.channel || undefined,
      status: q.status || undefined,
      pageNum: q.pageNum,
      pageSize: q.pageSize,
    },
  });
  return { records: array(r.rows).map(payment), total: total(r.total) };
}
export async function getPayment(no: string) {
  return payment(
    (await request<unknown>({ url: '/trade/admin/payments/' + encodeURIComponent(no) })).data,
  );
}
export async function getRefunds(q: RefundFilter) {
  const r = await request<unknown>({
    url: '/trade/admin/payment-refunds',
    params: {
      afterSaleNo: q.afterSaleNo?.trim() || undefined,
      paymentNo: q.paymentNo?.trim() || undefined,
      status: q.status || undefined,
      pageNum: q.pageNum,
      pageSize: q.pageSize,
    },
  });
  return { records: array(r.rows).map(refund), total: total(r.total) };
}
export async function getRefund(no: string) {
  return refund(
    (await request<unknown>({ url: '/trade/admin/payment-refunds/' + encodeURIComponent(no) }))
      .data,
  );
}
/** 支持渠道只展示实际返回的编码和名称，不制造启用或已接通状态。 */
export async function getChannels() {
  return array((await request<unknown>({ url: '/trade/admin/payment-channels' })).data).map(
    (raw) => {
      const row = record(raw);
      return { code: text(row.code), name: text(row.name) };
    },
  );
}
