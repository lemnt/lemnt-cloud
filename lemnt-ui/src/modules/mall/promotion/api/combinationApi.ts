import { request } from '@/api/request';
import type {
  CombinationActivity,
  CombinationActivityFilter,
  CombinationActivitySave,
  CombinationProductLine,
  CombinationRecord,
} from '../types';

function object(v: unknown): Record<string, unknown> {
  if (!v || typeof v !== 'object' || Array.isArray(v)) throw Error('拼团响应格式不合法');
  return v as Record<string, unknown>;
}
function text(v: unknown): string {
  if (typeof v !== 'string') throw Error('拼团文本不合法');
  return v;
}
export function combinationId(v: unknown): string {
  const id = typeof v === 'number' && Number.isSafeInteger(v) ? String(v) : v;
  if (typeof id !== 'string' || !/^[1-9]\d{0,18}$/.test(id) || BigInt(id) > 9223372036854775807n)
    throw Error('拼团编号不合法或已丢失精度');
  return id;
}
function integer(v: unknown): number {
  if (typeof v !== 'number' || !Number.isSafeInteger(v) || v < 0) throw Error('拼团数值不合法');
  return v;
}
function array(v: unknown): unknown[] {
  if (!Array.isArray(v)) throw Error('拼团列表不合法');
  return v;
}
function money(v: unknown): string {
  const value = text(v);
  if (!/^\d{1,17}(\.\d{1,2})?$/.test(value)) throw Error('拼团金额必须为正数，最多两位小数');
  const [whole, fraction = ''] = value.split('.');
  const result = `${BigInt(whole)}.${fraction.padEnd(2, '0')}`;
  if (BigInt(result.replace('.', '')) <= 0n) throw Error('拼团金额必须大于零');
  return result;
}
function activityStatus(v: unknown): 0 | 1 {
  if (v !== 0 && v !== 1) throw Error('拼团活动状态不合法');
  return v;
}
function recordStatus(v: unknown): 0 | 1 | 2 {
  if (v !== 0 && v !== 1 && v !== 2) throw Error('拼团记录状态不合法');
  return v;
}
function product(raw: unknown): CombinationProductLine {
  const v = object(raw);
  return { skuId: combinationId(v.skuId), combinationPrice: money(v.combinationPrice) };
}
function activity(raw: unknown): CombinationActivity {
  const v = object(raw);
  const virtual = v.virtualGroup;
  if (typeof virtual !== 'boolean') throw Error('虚拟成团配置不合法');
  return {
    id: combinationId(v.id),
    name: text(v.name),
    spuId: combinationId(v.spuId),
    spuName: v.spuName == null ? null : text(v.spuName),
    picUrl: v.picUrl == null ? null : text(v.picUrl),
    combinationPrice: v.combinationPrice == null ? null : money(v.combinationPrice),
    totalLimitCount: integer(v.totalLimitCount),
    singleLimitCount: integer(v.singleLimitCount),
    startTime: text(v.startTime),
    endTime: text(v.endTime),
    userSize: integer(v.userSize),
    virtualGroup: virtual,
    status: activityStatus(v.status),
    limitDuration: integer(v.limitDuration),
    products: array(v.products).map(product),
  };
}
function record(raw: unknown): CombinationRecord {
  const v = object(raw);
  const virtual = v.virtualGroup;
  if (typeof virtual !== 'boolean') throw Error('虚拟成团配置不合法');
  return {
    id: combinationId(v.id),
    activityId: combinationId(v.activityId),
    combinationPrice: money(v.combinationPrice),
    spuId: combinationId(v.spuId),
    spuName: text(v.spuName),
    picUrl: v.picUrl == null ? null : text(v.picUrl),
    skuId: combinationId(v.skuId),
    count: integer(v.count),
    userId: combinationId(v.userId),
    nickname: v.nickname == null ? null : text(v.nickname),
    avatar: v.avatar == null ? null : text(v.avatar),
    headId: v.headId === 0 || v.headId === '0' ? '0' : combinationId(v.headId),
    status: recordStatus(v.status),
    orderId: v.orderId == null ? null : combinationId(v.orderId),
    userSize: integer(v.userSize),
    userCount: integer(v.userCount),
    virtualGroup: virtual,
    expireTime: text(v.expireTime),
    startTime: text(v.startTime),
    endTime: v.endTime == null ? null : text(v.endTime),
  };
}
function page<T>(r: unknown, decode: (value: unknown) => T) {
  const body = object(r);
  if (typeof body.total !== 'number' || !Number.isSafeInteger(body.total) || body.total < 0)
    throw Error('拼团总数不合法');
  return { records: array(body.rows).map(decode), total: body.total };
}

export async function getCombinationActivityPage(q: CombinationActivityFilter) {
  return page(
    await request<unknown>({
      url: '/mall/promotion/combination-activities',
      params: {
        name: q.name?.trim() || undefined,
        status: q.status,
        spuId: q.spuId,
        pageNum: q.pageNum,
        pageSize: q.pageSize,
      },
    }),
    activity,
  );
}
export async function getCombinationActivity(id: string): Promise<CombinationActivity> {
  return activity(
    (await request<unknown>({ url: `/mall/promotion/combination-activities/${combinationId(id)}` }))
      .data,
  );
}
export async function createCombinationActivity(payload: CombinationActivitySave) {
  return request<number>({
    url: '/mall/promotion/combination-activities',
    method: 'post',
    data: payload,
  });
}
export async function updateCombinationActivity(id: string, payload: CombinationActivitySave) {
  return request<boolean>({
    url: `/mall/promotion/combination-activities/${combinationId(id)}`,
    method: 'put',
    data: payload,
  });
}
export function closeCombinationActivity(id: string) {
  return request<boolean>({
    url: `/mall/promotion/combination-activities/${combinationId(id)}/close`,
    method: 'put',
  });
}
export function deleteCombinationActivity(id: string) {
  return request<boolean>({
    url: `/mall/promotion/combination-activities/${combinationId(id)}`,
    method: 'delete',
  });
}
export async function getCombinationRecordPage(q: {
  userId?: string;
  activityId?: string;
  status?: 0 | 1 | 2;
  headId?: string;
  pageNum: number;
  pageSize: number;
}) {
  return page(
    await request<unknown>({
      url: '/mall/promotion/combination-records',
      params: {
        userId: q.userId,
        activityId: q.activityId,
        status: q.status,
        headId: q.headId,
        pageNum: q.pageNum,
        pageSize: q.pageSize,
      },
    }),
    record,
  );
}

/** 成团人数不少于两人，拼团时长必须为正；商品价格低于原价由后端再次校验。 */
export function combinationPayload(v: CombinationActivitySave): CombinationActivitySave {
  const name = v.name?.trim();
  if (!name || name.length > 100) throw Error('活动名称不能为空且不超过100字');
  if (
    !/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(v.startTime) ||
    !/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(v.endTime) ||
    v.endTime <= v.startTime
  )
    throw Error('结束时间必须晚于开始时间');
  for (const [label, value] of [
    ['总限购数量', v.totalLimitCount],
    ['单次限购数量', v.singleLimitCount],
    ['限制时长', v.limitDuration],
  ] as const) {
    if (!Number.isSafeInteger(value) || value < 1) throw Error(`${label}必须为正整数`);
  }
  if (!Number.isSafeInteger(v.userSize) || v.userSize < 2) throw Error('拼团人数不能少于两人');
  if (typeof v.virtualGroup !== 'boolean') throw Error('请选择是否虚拟成团');
  if (!v.products?.length) throw Error('至少选择一个拼团商品');
  const seen = new Set<string>();
  const products = v.products.map((row) => {
    const skuId = combinationId(row.skuId);
    if (seen.has(skuId)) throw Error('不能重复选择同一SKU');
    seen.add(skuId);
    return { skuId, combinationPrice: money(row.combinationPrice) };
  });
  return {
    name,
    spuId: combinationId(v.spuId),
    totalLimitCount: v.totalLimitCount,
    singleLimitCount: v.singleLimitCount,
    startTime: v.startTime,
    endTime: v.endTime,
    userSize: v.userSize,
    virtualGroup: v.virtualGroup,
    limitDuration: v.limitDuration,
    products,
  };
}
