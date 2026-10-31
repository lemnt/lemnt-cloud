import { request } from '@/api/request';
import type {
  ActivityKind,
  ActivitySummary,
  ActivityFilter,
  ActivityEditorValues,
  RewardValues,
  DiscountValues,
  RewardActivity,
  DiscountActivity,
} from '../types';

const path = (kind: ActivityKind) => `/mall/promotion/${kind}-activities`;
function object(v: unknown): Record<string, unknown> {
  if (!v || typeof v !== 'object' || Array.isArray(v)) throw Error('活动响应格式不合法');
  return v as Record<string, unknown>;
}
function text(v: unknown): string {
  if (typeof v !== 'string') throw Error('活动文本不合法');
  return v;
}
export function activityId(v: unknown): string {
  const id = typeof v === 'number' && Number.isSafeInteger(v) ? String(v) : v;
  if (typeof id !== 'string' || !/^[1-9]\d{0,18}$/.test(id) || BigInt(id) > 9223372036854775807n)
    throw Error('活动或商品编号不合法或已丢失精度');
  return id;
}
/** 元金额按字符串处理，不把金额或编号转换成浮点数。 */
function decimal(v: unknown): string {
  const value = text(v);
  if (!/^\d{1,17}(\.\d{1,2})?$/.test(value)) throw Error('金额或门槛必须为正数，最多两位小数');
  const [whole, fraction = ''] = value.split('.');
  const result = `${BigInt(whole)}.${fraction.padEnd(2, '0')}`;
  if (cents(result) <= 0n) throw Error('金额或门槛必须大于零');
  return result;
}
function cents(v: string) {
  return BigInt(v.replace('.', ''));
}
function array(v: unknown): unknown[] {
  if (!Array.isArray(v)) throw Error('活动列表不合法');
  return v;
}
function summary(raw: unknown): ActivitySummary {
  const v = object(raw);
  if (v.status !== 0 && v.status !== 1) throw Error('活动状态不合法');
  return {
    id: activityId(v.id),
    name: text(v.name),
    status: v.status,
    startTime: text(v.startTime),
    endTime: text(v.endTime),
    remark: v.remark == null ? null : text(v.remark),
  };
}
export async function getActivityPage(kind: ActivityKind, q: ActivityFilter) {
  const r = await request<unknown>({
    url: path(kind),
    params: {
      name: q.name?.trim() || undefined,
      status: q.status,
      pageNum: q.pageNum,
      pageSize: q.pageSize,
    },
  });
  const total = r.total;
  if (typeof total !== 'number' || !Number.isSafeInteger(total) || total < 0)
    throw Error('活动总数不合法');
  return { records: array(r.rows).map(summary), total };
}
export async function getReward(id: string): Promise<RewardActivity> {
  const v = object((await request<unknown>({ url: `${path('reward')}/${activityId(id)}` })).data);
  if (v.conditionType !== 'PRICE' && v.conditionType !== 'COUNT') throw Error('满减条件不合法');
  if (!['ALL', 'SPU_IDS', 'CATEGORY_IDS'].includes(String(v.productScope)))
    throw Error('商品范围不合法');
  return {
    ...summary(v),
    conditionType: v.conditionType,
    productScope: v.productScope as RewardValues['productScope'],
    productScopeValues: array(v.productScopeValues).map(activityId),
    rules: array(v.rules).map((raw) => {
      const row = object(raw);
      return { limit: decimal(row.limit), discountPrice: decimal(row.discountPrice) };
    }),
  };
}
export async function getDiscount(id: string): Promise<DiscountActivity> {
  const v = object((await request<unknown>({ url: `${path('discount')}/${activityId(id)}` })).data);
  return {
    ...summary(v),
    products: array(v.products).map((raw) => {
      const row = object(raw);
      if (row.discountType !== 'PRICE' && row.discountType !== 'PERCENT')
        throw Error('折扣类型不合法');
      const percent = row.discountPercent;
      if (
        row.discountType === 'PERCENT' &&
        (typeof percent !== 'number' || !Number.isInteger(percent) || percent < 1 || percent > 9999)
      )
        throw Error('折扣比例不合法');
      return {
        spuId: activityId(row.spuId),
        skuId: activityId(row.skuId),
        discountType: row.discountType,
        discountPercent: row.discountType === 'PERCENT' ? (percent as number) : null,
        discountPrice: row.discountType === 'PRICE' ? decimal(row.discountPrice) : null,
      };
    }),
  };
}
function common(v: ActivityEditorValues) {
  const name = v.name?.trim();
  if (!name || name.length > 100) throw Error('活动名称不能为空且不超过100字');
  if (
    !/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(v.startTime) ||
    !/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(v.endTime) ||
    v.endTime <= v.startTime
  )
    throw Error('结束时间必须晚于开始时间');
  return { name, startTime: v.startTime, endTime: v.endTime, remark: v.remark?.trim() || null };
}
/** 阶梯递增校验使用精确金额；切换全部商品时不携带旧范围。 */
export function rewardPayload(v: ActivityEditorValues): RewardValues {
  const base = common(v);
  if (v.conditionType !== 'PRICE' && v.conditionType !== 'COUNT') throw Error('请选择满减条件');
  if (v.productScope !== 'ALL' && v.productScope !== 'SPU_IDS' && v.productScope !== 'CATEGORY_IDS')
    throw Error('请选择商品范围');
  const ids =
    v.productScope === 'ALL' ? [] : [...new Set((v.productScopeValues ?? []).map(activityId))];
  if (v.productScope !== 'ALL' && !ids.length) throw Error('请选择适用商品或分类');
  if (!v.rules?.length) throw Error('至少设置一条满减规则');
  let previous = 0n;
  const rules = v.rules.map((row) => {
    const limit = decimal(row.limit),
      discountPrice = decimal(row.discountPrice);
    if (cents(limit) <= previous) throw Error('满减门槛必须逐级递增');
    if (v.conditionType === 'COUNT' && cents(limit) % 100n !== 0n) throw Error('满件数必须为整数');
    if (v.conditionType === 'PRICE' && cents(discountPrice) > cents(limit))
      throw Error('减免金额不能超过门槛');
    previous = cents(limit);
    return { limit, discountPrice };
  });
  return {
    ...base,
    conditionType: v.conditionType,
    productScope: v.productScope,
    productScopeValues: ids,
    rules,
  };
}
/** 页面输入几折，精确转换为后端基点，减价模式不提交残留折扣。 */
export function discountPayload(v: ActivityEditorValues): DiscountValues {
  const base = common(v);
  if (!v.products?.length) throw Error('至少选择一个SKU');
  const seen = new Set<string>();
  const products = v.products.map((row) => {
    const spuId = activityId(row.spuId),
      skuId = activityId(row.skuId);
    if (seen.has(skuId)) throw Error('不能重复选择同一SKU');
    seen.add(skuId);
    if (row.discountType === 'PRICE')
      return {
        spuId,
        skuId,
        discountType: 'PRICE' as const,
        discountPrice: decimal(row.discountPrice),
        discountPercent: null,
      };
    if (row.discountType !== 'PERCENT' || !/^\d(?:\.\d{1,3})?$/.test(row.discountRate ?? ''))
      throw Error('折扣须大于0且小于10折，最多三位小数');
    const [whole, fraction = ''] = row.discountRate!.split('.');
    const discountPercent = Number(whole + fraction.padEnd(3, '0'));
    if (discountPercent < 1 || discountPercent > 9999) throw Error('折扣须大于0且小于10折');
    return { spuId, skuId, discountType: 'PERCENT' as const, discountPrice: null, discountPercent };
  });
  return { ...base, products };
}
async function confirmed(
  url: string,
  method: 'PUT' | 'DELETE',
  data?: RewardValues | DiscountValues,
) {
  if ((await request<unknown>({ url, method, data })).data !== true)
    throw Error('操作尚未确认成功，请刷新核验');
}
export async function createReward(v: RewardValues) {
  return activityId(
    (await request<unknown>({ url: path('reward'), method: 'POST', data: v })).data,
  );
}
export async function updateReward(id: string, v: RewardValues) {
  await confirmed(`${path('reward')}/${activityId(id)}`, 'PUT', v);
}
export async function createDiscount(v: DiscountValues) {
  return activityId(
    (await request<unknown>({ url: path('discount'), method: 'POST', data: v })).data,
  );
}
export async function updateDiscount(id: string, v: DiscountValues) {
  await confirmed(`${path('discount')}/${activityId(id)}`, 'PUT', v);
}
export async function closeActivity(kind: ActivityKind, id: string) {
  await confirmed(`${path(kind)}/${activityId(id)}/close`, 'PUT');
}
export async function deleteActivity(kind: ActivityKind, id: string) {
  await confirmed(`${path(kind)}/${activityId(id)}`, 'DELETE');
}
