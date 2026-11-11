import { request } from '@/api/request';
import type {
  PointActivity,
  PointActivityFilter,
  PointActivitySave,
  PointProductLine,
} from '../types';

function object(v: unknown): Record<string, unknown> {
  if (!v || typeof v !== 'object' || Array.isArray(v)) throw Error('积分商城响应格式不合法');
  return v as Record<string, unknown>;
}
function text(v: unknown): string {
  if (typeof v !== 'string') throw Error('积分商城文本不合法');
  return v;
}
export function pointId(v: unknown): string {
  const id = typeof v === 'number' && Number.isSafeInteger(v) ? String(v) : v;
  if (typeof id !== 'string' || !/^[1-9]\d{0,18}$/.test(id) || BigInt(id) > 9223372036854775807n)
    throw Error('积分商城编号不合法或已丢失精度');
  return id;
}
function integer(v: unknown): number {
  if (typeof v !== 'number' || !Number.isSafeInteger(v) || v < 0) throw Error('积分商城数值不合法');
  return v;
}
function array(v: unknown): unknown[] {
  if (!Array.isArray(v)) throw Error('积分商城列表不合法');
  return v;
}
/** 补差金额允许为零，积分商品积分不允许为负。 */
function amount(v: unknown): string {
  const value = text(v);
  if (!/^\d{1,17}(\.\d{1,2})?$/.test(value)) throw Error('补差金额必须为非负数，最多两位小数');
  const [whole, fraction = ''] = value.split('.');
  return `${BigInt(whole)}.${fraction.padEnd(2, '0')}`;
}
function status(v: unknown): 0 | 1 {
  if (v !== 0 && v !== 1) throw Error('积分商城活动状态不合法');
  return v;
}
function product(raw: unknown): PointProductLine & { id: string; spuId: string } {
  const v = object(raw);
  return {
    id: pointId(v.id),
    spuId: pointId(v.spuId),
    skuId: pointId(v.skuId),
    exchangeCount: integer(v.exchangeCount),
    point: integer(v.point),
    price: amount(v.price),
    stock: integer(v.stock),
  };
}
function activity(raw: unknown): PointActivity {
  const v = object(raw);
  return {
    id: pointId(v.id),
    spuId: pointId(v.spuId),
    spuName: v.spuName == null ? null : text(v.spuName),
    picUrl: v.picUrl == null ? null : text(v.picUrl),
    status: status(v.status),
    remark: v.remark == null ? null : text(v.remark),
    sort: integer(v.sort),
    stock: integer(v.stock),
    totalStock: integer(v.totalStock),
    point: v.point == null ? null : integer(v.point),
    price: v.price == null ? null : amount(v.price),
    marketPrice: v.marketPrice == null ? null : amount(v.marketPrice),
    products: array(v.products).map(product),
  };
}

export async function getPointActivityPage(q: PointActivityFilter) {
  const r = await request<unknown>({
    url: '/mall/promotion/point-activities',
    params: { status: q.status, pageNum: q.pageNum, pageSize: q.pageSize },
  });
  const body = object(r);
  if (typeof body.total !== 'number' || !Number.isSafeInteger(body.total) || body.total < 0)
    throw Error('积分商城总数不合法');
  return { records: array(body.rows).map(activity), total: body.total };
}
export async function getPointActivity(id: string): Promise<PointActivity> {
  return activity(
    (await request<unknown>({ url: `/mall/promotion/point-activities/${pointId(id)}` })).data,
  );
}
export async function createPointActivity(payload: PointActivitySave) {
  return request<number>({
    url: '/mall/promotion/point-activities',
    method: 'post',
    data: payload,
  });
}
export async function updatePointActivity(id: string, payload: PointActivitySave) {
  return request<boolean>({
    url: `/mall/promotion/point-activities/${pointId(id)}`,
    method: 'put',
    data: payload,
  });
}
export function closePointActivity(id: string) {
  return request<boolean>({
    url: `/mall/promotion/point-activities/${pointId(id)}/close`,
    method: 'put',
  });
}
export function deletePointActivity(id: string) {
  return request<boolean>({
    url: `/mall/promotion/point-activities/${pointId(id)}`,
    method: 'delete',
  });
}

/** 兑换次数、积分、库存必须为正整数或非负整数，补差金额允许零。 */
export function pointPayload(v: PointActivitySave): PointActivitySave {
  if (!v.products?.length) throw Error('至少选择一个积分商品');
  if (!Number.isSafeInteger(v.sort) || v.sort < 0) throw Error('排序必须为非负整数');
  const seen = new Set<string>();
  const products: PointProductLine[] = v.products.map((row) => {
    const skuId = pointId(row.skuId);
    if (seen.has(skuId)) throw Error('不能重复选择同一SKU');
    seen.add(skuId);
    if (!Number.isSafeInteger(row.exchangeCount) || row.exchangeCount < 1)
      throw Error('可兑换次数必须为正整数');
    if (!Number.isSafeInteger(row.point) || row.point < 0) throw Error('兑换积分不能为负');
    if (!Number.isSafeInteger(row.stock) || row.stock < 0) throw Error('库存不能为负');
    return {
      skuId,
      exchangeCount: row.exchangeCount,
      point: row.point,
      price: amount(row.price),
      stock: row.stock,
    };
  });
  if (products.every((row) => row.stock === 0)) throw Error('积分活动库存合计必须大于零');
  return {
    spuId: pointId(v.spuId),
    remark: v.remark?.trim() || null,
    sort: v.sort,
    products,
  };
}
