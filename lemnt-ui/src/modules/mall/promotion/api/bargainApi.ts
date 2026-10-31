import { request } from '@/api/request';
import type {
  BargainActivity,
  BargainActivityFilter,
  BargainActivitySave,
  BargainHelp,
  BargainRecord,
} from '../types';

function object(v: unknown): Record<string, unknown> {
  if (!v || typeof v !== 'object' || Array.isArray(v)) throw Error('砍价响应格式不合法');
  return v as Record<string, unknown>;
}
function text(v: unknown): string {
  if (typeof v !== 'string') throw Error('砍价文本不合法');
  return v;
}
export function bargainId(v: unknown): string {
  const id = typeof v === 'number' && Number.isSafeInteger(v) ? String(v) : v;
  if (typeof id !== 'string' || !/^[1-9]\d{0,18}$/.test(id) || BigInt(id) > 9223372036854775807n)
    throw Error('砍价编号不合法或已丢失精度');
  return id;
}
function integer(v: unknown): number {
  if (typeof v !== 'number' || !Number.isSafeInteger(v) || v < 0) throw Error('砍价数值不合法');
  return v;
}
function array(v: unknown): unknown[] {
  if (!Array.isArray(v)) throw Error('砍价列表不合法');
  return v;
}
function money(v: unknown): string {
  const value = text(v);
  if (!/^\d{1,17}(\.\d{1,2})?$/.test(value)) throw Error('砍价金额必须为正数，最多两位小数');
  const [whole, fraction = ''] = value.split('.');
  const result = `${BigInt(whole)}.${fraction.padEnd(2, '0')}`;
  if (BigInt(result.replace('.', '')) < 0n) throw Error('砍价金额不能为负');
  return result;
}
function activityStatus(v: unknown): 0 | 1 {
  if (v !== 0 && v !== 1) throw Error('砍价活动状态不合法');
  return v;
}
function recordStatus(v: unknown): 1 | 2 | 3 {
  if (v !== 1 && v !== 2 && v !== 3) throw Error('砍价记录状态不合法');
  return v;
}
function activity(raw: unknown): BargainActivity {
  const v = object(raw);
  return {
    id: bargainId(v.id),
    name: text(v.name),
    startTime: text(v.startTime),
    endTime: text(v.endTime),
    status: activityStatus(v.status),
    spuId: bargainId(v.spuId),
    spuName: v.spuName == null ? null : text(v.spuName),
    picUrl: v.picUrl == null ? null : text(v.picUrl),
    skuPrice: v.skuPrice == null ? null : money(v.skuPrice),
    skuId: bargainId(v.skuId),
    bargainFirstPrice: money(v.bargainFirstPrice),
    bargainMinPrice: money(v.bargainMinPrice),
    stock: integer(v.stock),
    totalStock: integer(v.totalStock),
    helpMaxCount: integer(v.helpMaxCount),
    bargainCount: integer(v.bargainCount),
    totalLimitCount: integer(v.totalLimitCount),
    randomMinPrice: money(v.randomMinPrice),
    randomMaxPrice: money(v.randomMaxPrice),
    successCount: integer(v.successCount),
  };
}
function record(raw: unknown): BargainRecord {
  const v = object(raw);
  return {
    id: bargainId(v.id),
    userId: bargainId(v.userId),
    activityId: bargainId(v.activityId),
    spuId: bargainId(v.spuId),
    spuName: v.spuName == null ? null : text(v.spuName),
    picUrl: v.picUrl == null ? null : text(v.picUrl),
    skuId: bargainId(v.skuId),
    bargainFirstPrice: money(v.bargainFirstPrice),
    bargainPrice: money(v.bargainPrice),
    status: recordStatus(v.status),
    endTime: v.endTime == null ? null : text(v.endTime),
    orderId: v.orderId == null ? null : bargainId(v.orderId),
  };
}
function help(raw: unknown): BargainHelp {
  const v = object(raw);
  return {
    id: bargainId(v.id),
    activityId: bargainId(v.activityId),
    recordId: bargainId(v.recordId),
    userId: bargainId(v.userId),
    reducePrice: money(v.reducePrice),
  };
}
function page<T>(r: unknown, decode: (value: unknown) => T) {
  const body = object(r);
  if (typeof body.total !== 'number' || !Number.isSafeInteger(body.total) || body.total < 0)
    throw Error('砍价总数不合法');
  return { records: array(body.rows).map(decode), total: body.total };
}

export async function getBargainActivityPage(q: BargainActivityFilter) {
  return page(
    await request<unknown>({
      url: '/mall/promotion/bargain-activities',
      params: {
        name: q.name?.trim() || undefined,
        status: q.status,
        pageNum: q.pageNum,
        pageSize: q.pageSize,
      },
    }),
    activity,
  );
}
export async function getBargainActivity(id: string): Promise<BargainActivity> {
  return activity(
    (await request<unknown>({ url: `/mall/promotion/bargain-activities/${bargainId(id)}` })).data,
  );
}
export async function createBargainActivity(payload: BargainActivitySave) {
  return request<number>({
    url: '/mall/promotion/bargain-activities',
    method: 'post',
    data: payload,
  });
}
export async function updateBargainActivity(id: string, payload: BargainActivitySave) {
  return request<boolean>({
    url: `/mall/promotion/bargain-activities/${bargainId(id)}`,
    method: 'put',
    data: payload,
  });
}
export function closeBargainActivity(id: string) {
  return request<boolean>({
    url: `/mall/promotion/bargain-activities/${bargainId(id)}/close`,
    method: 'put',
  });
}
export function deleteBargainActivity(id: string) {
  return request<boolean>({
    url: `/mall/promotion/bargain-activities/${bargainId(id)}`,
    method: 'delete',
  });
}
export async function getBargainRecordPage(q: {
  userId?: string;
  activityId?: string;
  status?: 1 | 2 | 3;
  pageNum: number;
  pageSize: number;
}) {
  return page(
    await request<unknown>({
      url: '/mall/promotion/bargain-records',
      params: { ...q, userId: q.userId, activityId: q.activityId },
    }),
    record,
  );
}
export async function getBargainHelpPage(q: {
  recordId?: string;
  userId?: string;
  activityId?: string;
  pageNum: number;
  pageSize: number;
}) {
  return page(await request<unknown>({ url: '/mall/promotion/bargain-helps', params: q }), help);
}

/** 底价必须低于起始价，随机区间落在起始价内，起始价不高于 SKU 原价由后端再次校验。 */
export function bargainPayload(v: BargainActivitySave): BargainActivitySave {
  const name = v.name?.trim();
  if (!name || name.length > 100) throw Error('活动名称不能为空且不超过100字');
  if (
    !/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(v.startTime) ||
    !/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(v.endTime) ||
    v.endTime <= v.startTime
  )
    throw Error('结束时间必须晚于开始时间');
  const first = money(v.bargainFirstPrice);
  const min = money(v.bargainMinPrice);
  if (BigInt(min.replace('.', '')) >= BigInt(first.replace('.', '')))
    throw Error('砍价底价必须低于起始价');
  const randomMin = money(v.randomMinPrice);
  const randomMax = money(v.randomMaxPrice);
  if (BigInt(randomMax.replace('.', '')) < BigInt(randomMin.replace('.', '')))
    throw Error('随机最大砍价不能小于随机最小砍价');
  if (BigInt(randomMax.replace('.', '')) > BigInt(first.replace('.', '')))
    throw Error('随机最大砍价不能超过起始价');
  for (const [label, value] of [
    ['库存', v.stock],
    ['砍价人数', v.helpMaxCount],
    ['帮砍次数', v.bargainCount],
    ['总限购数量', v.totalLimitCount],
  ] as const) {
    if (!Number.isSafeInteger(value) || value < 1) throw Error(`${label}必须为正整数`);
  }
  return {
    name,
    startTime: v.startTime,
    endTime: v.endTime,
    spuId: bargainId(v.spuId),
    skuId: bargainId(v.skuId),
    bargainFirstPrice: first,
    bargainMinPrice: min,
    stock: v.stock,
    helpMaxCount: v.helpMaxCount,
    bargainCount: v.bargainCount,
    totalLimitCount: v.totalLimitCount,
    randomMinPrice: randomMin,
    randomMaxPrice: randomMax,
  };
}
