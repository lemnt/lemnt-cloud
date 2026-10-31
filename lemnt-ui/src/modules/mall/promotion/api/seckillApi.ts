import { request } from '@/api/request';
import type {
  SeckillActivity,
  SeckillActivityFilter,
  SeckillActivitySave,
  SeckillConfig,
  SeckillConfigSave,
  SeckillProduct,
  SeckillProductLine,
} from '../types';

function object(v: unknown): Record<string, unknown> {
  if (!v || typeof v !== 'object' || Array.isArray(v)) throw Error('秒杀响应格式不合法');
  return v as Record<string, unknown>;
}
function text(v: unknown): string {
  if (typeof v !== 'string') throw Error('秒杀文本不合法');
  return v;
}
export function seckillId(v: unknown): string {
  const id = typeof v === 'number' && Number.isSafeInteger(v) ? String(v) : v;
  if (typeof id !== 'string' || !/^[1-9]\d{0,18}$/.test(id) || BigInt(id) > 9223372036854775807n)
    throw Error('秒杀编号不合法或已丢失精度');
  return id;
}
function integer(v: unknown): number {
  if (typeof v !== 'number' || !Number.isSafeInteger(v) || v < 0) throw Error('秒杀数值不合法');
  return v;
}
function status(v: unknown): 0 | 1 {
  if (v !== 0 && v !== 1) throw Error('秒杀状态不合法');
  return v;
}
function array(v: unknown): unknown[] {
  if (!Array.isArray(v)) throw Error('秒杀列表不合法');
  return v;
}
function strings(v: unknown): string[] {
  return array(v).map((item) => text(item));
}
/** 金额只接受正数并保留两位小数，不使用浮点换算。 */
export function money(v: unknown): string {
  const value = text(v);
  if (!/^\d{1,17}(\.\d{1,2})?$/.test(value)) throw Error('秒杀金额必须为正数，最多两位小数');
  const [whole, fraction = ''] = value.split('.');
  const result = `${BigInt(whole)}.${fraction.padEnd(2, '0')}`;
  if (BigInt(result.replace('.', '')) <= 0n) throw Error('秒杀金额必须大于零');
  return result;
}
function config(raw: unknown): SeckillConfig {
  const v = object(raw);
  return {
    id: seckillId(v.id),
    name: text(v.name),
    startTime: text(v.startTime),
    endTime: text(v.endTime),
    sliderPicUrls: strings(v.sliderPicUrls),
    status: status(v.status),
    activityCount: integer(v.activityCount),
  };
}
function product(raw: unknown): SeckillProduct {
  const v = object(raw);
  return {
    id: seckillId(v.id),
    spuId: seckillId(v.spuId),
    skuId: seckillId(v.skuId),
    seckillPrice: money(v.seckillPrice),
    stock: integer(v.stock),
    activityStartTime: text(v.activityStartTime),
    activityEndTime: text(v.activityEndTime),
  };
}
function activity(raw: unknown): SeckillActivity {
  const v = object(raw);
  return {
    id: seckillId(v.id),
    spuId: seckillId(v.spuId),
    spuName: v.spuName == null ? null : text(v.spuName),
    picUrl: v.picUrl == null ? null : text(v.picUrl),
    seckillPrice: v.seckillPrice == null ? null : money(v.seckillPrice),
    name: text(v.name),
    status: status(v.status),
    remark: v.remark == null ? null : text(v.remark),
    startTime: text(v.startTime),
    endTime: text(v.endTime),
    sort: integer(v.sort),
    configIds: array(v.configIds).map(seckillId),
    totalLimitCount: integer(v.totalLimitCount),
    singleLimitCount: integer(v.singleLimitCount),
    stock: integer(v.stock),
    totalStock: integer(v.totalStock),
    products: array(v.products).map(product),
  };
}
function page<T>(r: unknown, decode: (value: unknown) => T) {
  const body = object(r);
  if (typeof body.total !== 'number' || !Number.isSafeInteger(body.total) || body.total < 0)
    throw Error('秒杀总数不合法');
  return { records: array(body.rows).map(decode), total: body.total };
}

export async function getSeckillConfigPage(q: {
  name?: string;
  status?: 0 | 1;
  pageNum: number;
  pageSize: number;
}) {
  return page(
    await request<unknown>({
      url: '/mall/promotion/seckill-configs',
      params: {
        name: q.name?.trim() || undefined,
        status: q.status,
        pageNum: q.pageNum,
        pageSize: q.pageSize,
      },
    }),
    config,
  );
}
export async function getSeckillConfigList(): Promise<SeckillConfig[]> {
  const r = await request<unknown[]>({ url: '/mall/promotion/seckill-configs/list' });
  return array(r.data).map(config);
}
export async function createSeckillConfig(payload: SeckillConfigSave) {
  return request<number>({ url: '/mall/promotion/seckill-configs', method: 'post', data: payload });
}
export async function updateSeckillConfig(id: string, payload: SeckillConfigSave) {
  return request<boolean>({
    url: `/mall/promotion/seckill-configs/${seckillId(id)}`,
    method: 'put',
    data: payload,
  });
}
export async function updateSeckillConfigStatus(id: string, value: 0 | 1) {
  return request<boolean>({
    url: `/mall/promotion/seckill-configs/${seckillId(id)}/status`,
    method: 'put',
    data: { status: value },
  });
}
export function deleteSeckillConfig(id: string) {
  return request<boolean>({
    url: `/mall/promotion/seckill-configs/${seckillId(id)}`,
    method: 'delete',
  });
}

export async function getSeckillActivityPage(q: SeckillActivityFilter) {
  return page(
    await request<unknown>({
      url: '/mall/promotion/seckill-activities',
      params: {
        name: q.name?.trim() || undefined,
        status: q.status,
        configId: q.configId,
        pageNum: q.pageNum,
        pageSize: q.pageSize,
      },
    }),
    activity,
  );
}
export async function getSeckillActivity(id: string): Promise<SeckillActivity> {
  const r = await request<unknown>({
    url: `/mall/promotion/seckill-activities/${seckillId(id)}`,
  });
  return activity(r.data);
}
export async function createSeckillActivity(payload: SeckillActivitySave) {
  return request<number>({ url: '/mall/promotion/seckill-activities', method: 'post', data: payload });
}
export async function updateSeckillActivity(id: string, payload: SeckillActivitySave) {
  return request<boolean>({
    url: `/mall/promotion/seckill-activities/${seckillId(id)}`,
    method: 'put',
    data: payload,
  });
}
export function closeSeckillActivity(id: string) {
  return request<boolean>({
    url: `/mall/promotion/seckill-activities/${seckillId(id)}/close`,
    method: 'put',
  });
}
export function deleteSeckillActivity(id: string) {
  return request<boolean>({
    url: `/mall/promotion/seckill-activities/${seckillId(id)}`,
    method: 'delete',
  });
}

/** 时段输入必须是合法时间点，状态只允许开启或关闭。 */
export function configPayload(v: SeckillConfigSave): SeckillConfigSave {
  const name = v.name?.trim();
  if (!name || name.length > 100) throw Error('时段名称不能为空且不超过100字');
  if (!/^([01]\d|2[0-3]):[0-5]\d(:[0-5]\d)?$/.test(v.startTime) ||
    !/^([01]\d|2[0-3]):[0-5]\d(:[0-5]\d)?$/.test(v.endTime) ||
    v.endTime <= v.startTime)
    throw Error('结束时间必须晚于开始时间');
  if (v.status !== 0 && v.status !== 1) throw Error('请选择时段状态');
  return {
    name,
    startTime: v.startTime,
    endTime: v.endTime,
    sliderPicUrls: (v.sliderPicUrls ?? []).map((url) => url.trim()).filter(Boolean),
    status: v.status,
  };
}
/** 活动商品价格与编号在提交前再校验一次，避免无效行进入接口。 */
export function activityPayload(v: SeckillActivitySave): SeckillActivitySave {
  const name = v.name?.trim();
  if (!name || name.length > 100) throw Error('活动名称不能为空且不超过100字');
  if (!/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(v.startTime) ||
    !/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(v.endTime) ||
    v.endTime <= v.startTime)
    throw Error('结束时间必须晚于开始时间');
  if (!Number.isSafeInteger(v.sort) || v.sort < 0) throw Error('排序必须为非负整数');
  if (!v.configIds?.length) throw Error('至少选择一个秒杀时段');
  if (!Number.isSafeInteger(v.totalLimitCount) || v.totalLimitCount < 1)
    throw Error('总限购数量必须为正整数');
  if (!Number.isSafeInteger(v.singleLimitCount) || v.singleLimitCount < 1)
    throw Error('单次限购数量必须为正整数');
  if (!v.products?.length) throw Error('至少选择一个秒杀商品');
  const seen = new Set<string>();
  const products: SeckillProductLine[] = v.products.map((row) => {
    const skuId = seckillId(row.skuId);
    if (seen.has(skuId)) throw Error('不能重复选择同一SKU');
    seen.add(skuId);
    if (!Number.isSafeInteger(row.stock) || row.stock < 0) throw Error('秒杀库存必须为非负整数');
    return { skuId, seckillPrice: money(row.seckillPrice), stock: row.stock };
  });
  if (products.every((row) => row.stock === 0)) throw Error('秒杀库存合计必须大于零');
  return {
    spuId: seckillId(v.spuId),
    name,
    remark: v.remark?.trim() || null,
    startTime: v.startTime,
    endTime: v.endTime,
    sort: v.sort,
    configIds: [...new Set(v.configIds.map(seckillId))],
    totalLimitCount: v.totalLimitCount,
    singleLimitCount: v.singleLimitCount,
    products,
  };
}
