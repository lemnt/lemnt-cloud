import { request } from '@/api/request';
import { yuanToFen, fenToYuan } from '@/shared/money';
/** 完整交易配置快照，包含暂未开放编辑的原始配置。 */
export interface TradeConfig {
  id: string | number;
  afterSaleRefundReasons: string[];
  afterSaleReturnReasons: string[];
  deliveryExpressFreeEnabled: boolean;
  deliveryExpressFreePrice: number;
  deliveryPickUpEnabled: boolean;
  brokerageEnabled: boolean;
  brokerageEnabledCondition: number;
  brokerageBindMode: number;
  brokeragePosterUrls: string[];
  brokerageFirstPercent: number;
  brokerageSecondPercent: number;
  brokerageWithdrawMinPrice: number;
  brokerageWithdrawFeePercent: number;
  brokerageFrozenDays: number;
  brokerageWithdrawTypes: number[];
}
/** 后台售后原因和包邮配置表单。 */
export interface ConfigValues {
  refundReasons: string;
  returnReasons: string;
  freeEnabled: boolean;
  freePrice: string;
}
const numeric = [
  'deliveryExpressFreePrice',
  'brokerageEnabledCondition',
  'brokerageBindMode',
  'brokerageFirstPercent',
  'brokerageSecondPercent',
  'brokerageWithdrawMinPrice',
  'brokerageWithdrawFeePercent',
  'brokerageFrozenDays',
] as const;
const booleans = [
  'deliveryExpressFreeEnabled',
  'deliveryPickUpEnabled',
  'brokerageEnabled',
] as const;
const lists = ['afterSaleRefundReasons', 'afterSaleReturnReasons', 'brokeragePosterUrls'] as const;
/** 读取现有完整配置；缺失配置时禁止创建默认值。 */
export async function getTradeConfig(): Promise<TradeConfig> {
  const raw = (await request<unknown>({ url: '/trade/admin/config' })).data;
  if (!raw || typeof raw !== 'object' || Array.isArray(raw))
    throw Error('交易配置缺失，请先初始化配置');
  const v = raw as Record<string, unknown>;
  if (v.id !== 1 && v.id !== '1') throw Error('交易配置编号不合法');
  for (const key of numeric)
    if (
      typeof v[key] !== 'number' ||
      !Number.isInteger(v[key]) ||
      v[key] < 0 ||
      v[key] > 2147483647
    )
      throw Error('交易配置数值不合法');
  for (const key of booleans) if (typeof v[key] !== 'boolean') throw Error('交易配置开关不合法');
  for (const key of lists)
    if (!Array.isArray(v[key]) || !(v[key] as unknown[]).every((row) => typeof row === 'string'))
      throw Error('交易配置列表不合法');
  if (
    !Array.isArray(v.brokerageWithdrawTypes) ||
    !v.brokerageWithdrawTypes.every((row) => typeof row === 'number' && Number.isInteger(row))
  )
    throw Error('提现方式配置不合法');
  return {
    id: v.id,
    ...Object.fromEntries(
      [...numeric, ...booleans, ...lists, 'brokerageWithdrawTypes'].map((key) => [key, v[key]]),
    ),
  } as unknown as TradeConfig;
}
/** 从配置快照构建可编辑表单。 */
export function configValues(v: TradeConfig): ConfigValues {
  return {
    refundReasons: v.afterSaleRefundReasons.join('\n'),
    returnReasons: v.afterSaleReturnReasons.join('\n'),
    freeEnabled: v.deliveryExpressFreeEnabled,
    freePrice: fenToYuan(v.deliveryExpressFreePrice),
  };
}
function reasons(value: string): string[] {
  const rows = value
    .split(/\r?\n/)
    .map((row) => row.trim())
    .filter(Boolean);
  if (
    rows.length > 100 ||
    rows.some((row) => row.length > 200) ||
    new Set(rows).size !== rows.length
  )
    throw Error('原因最多100条、每条200字，不能重复');
  return rows;
}
/** 全量更新接口保留未开放编辑的配置，禁止用默认值覆盖现有配置。 */
export function configPayload(snapshot: TradeConfig, values: ConfigValues) {
  if (typeof values.freeEnabled !== 'boolean') throw Error('包邮开关不合法');
  return {
    afterSaleRefundReasons: reasons(values.refundReasons),
    afterSaleReturnReasons: reasons(values.returnReasons),
    deliveryExpressFreeEnabled: values.freeEnabled,
    deliveryExpressFreePrice: yuanToFen(values.freePrice),
    deliveryPickUpEnabled: snapshot.deliveryPickUpEnabled,
    brokerageEnabled: snapshot.brokerageEnabled,
    brokerageEnabledCondition: snapshot.brokerageEnabledCondition,
    brokerageBindMode: snapshot.brokerageBindMode,
    brokeragePosterUrls: snapshot.brokeragePosterUrls,
    brokerageFirstPercent: snapshot.brokerageFirstPercent,
    brokerageSecondPercent: snapshot.brokerageSecondPercent,
    brokerageWithdrawMinPrice: snapshot.brokerageWithdrawMinPrice,
    brokerageWithdrawFeePercent: snapshot.brokerageWithdrawFeePercent,
    brokerageFrozenDays: snapshot.brokerageFrozenDays,
    brokerageWithdrawTypes: snapshot.brokerageWithdrawTypes,
  };
}
/** 提交配置，仅在明确成功时返回。 */
export async function saveTradeConfig(snapshot: TradeConfig, values: ConfigValues) {
  if (
    (
      await request<unknown>({
        url: '/trade/admin/config',
        method: 'PUT',
        data: configPayload(snapshot, values),
      })
    ).data !== true
  )
    throw Error('交易配置保存失败');
  return true;
}
