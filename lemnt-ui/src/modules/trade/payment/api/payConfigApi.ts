import { request } from '@/api/request';

export interface PayApp {
  id: string;
  appKey: string;
  name: string;
  status: 0 | 1;
  remark: string | null;
  orderNotifyUrl: string | null;
  refundNotifyUrl: string | null;
  createTime: string | null;
}
export interface PayChannelConfig {
  id: string;
  appId: string;
  code: string;
  status: 0 | 1;
  feeRate: string | null;
  remark: string | null;
  configConfigured: boolean;
  createTime: string | null;
}
export interface PayAppFilter {
  appKey?: string;
  name?: string;
  status?: 0 | 1;
  pageNum: number;
  pageSize: number;
}
export interface PayChannelFilter {
  appId?: string;
  code?: string;
  status?: 0 | 1;
  pageNum: number;
  pageSize: number;
}
export interface PayAppValues {
  appKey?: string;
  name: string;
  status: number;
  remark?: string | null;
  orderNotifyUrl?: string | null;
  refundNotifyUrl?: string | null;
}
export interface PayChannelValues {
  appId?: string;
  code?: string;
  status: number;
  feeRate?: string | number | null;
  remark?: string | null;
  config?: string | null;
}

function record(v: unknown): Record<string, unknown> {
  if (!v || typeof v !== 'object' || Array.isArray(v)) throw Error('支付配置响应不合法');
  return v as Record<string, unknown>;
}
function text(v: unknown): string {
  if (typeof v !== 'string') throw Error('支付配置文本不合法');
  return v;
}
function optional(v: unknown): string | null {
  return v == null ? null : text(v);
}
function idOf(v: unknown): string {
  const value = typeof v === 'number' && Number.isSafeInteger(v) ? String(v) : v;
  if (
    typeof value !== 'string' ||
    !/^[1-9]\d{0,18}$/.test(value) ||
    BigInt(value) > 9223372036854775807n
  )
    throw Error('编号不合法或已丢失精度');
  return value;
}
function statusOf(v: unknown): 0 | 1 {
  if (v !== 0 && v !== 1) throw Error('状态不合法');
  return v;
}
function money(v: unknown): string | null {
  if (v == null) return null;
  const value = text(v);
  if (!/^\d+\.\d{2}$/.test(value)) throw Error('费率必须为精确两位小数字符串');
  return value;
}
function time(v: unknown): string | null {
  return v == null ? null : text(v);
}
function array(v: unknown): unknown[] {
  if (!Array.isArray(v)) throw Error('支付配置列表不合法');
  return v;
}
function total(v: unknown): number {
  if (typeof v !== 'number' || !Number.isSafeInteger(v) || v < 0) throw Error('分页总数不合法');
  return v;
}
function payApp(raw: unknown): PayApp {
  const v = record(raw);
  return {
    id: idOf(v.id),
    appKey: text(v.appKey),
    name: text(v.name),
    status: statusOf(v.status),
    remark: optional(v.remark),
    orderNotifyUrl: optional(v.orderNotifyUrl),
    refundNotifyUrl: optional(v.refundNotifyUrl),
    createTime: time(v.createTime),
  };
}
function payChannel(raw: unknown): PayChannelConfig {
  const v = record(raw);
  if (typeof v.configConfigured !== 'boolean') throw Error('渠道配置状态不合法');
  return {
    id: idOf(v.id),
    appId: idOf(v.appId),
    code: text(v.code),
    status: statusOf(v.status),
    feeRate: money(v.feeRate),
    remark: optional(v.remark),
    configConfigured: v.configConfigured,
    createTime: time(v.createTime),
  };
}
function textOrNull(v: string | null | undefined, max: number, field: string): string | null {
  const value = (v ?? '').trim();
  if (!value) return null;
  if (value.length > max) throw Error(`${field}不超过${max}字`);
  return value;
}
async function confirmed(config: Parameters<typeof request>[0]) {
  const r = await request<unknown>(config);
  if (r.data !== true) throw Error('操作未确认');
  return true;
}

function payAppPayload(v: PayAppValues, create: boolean) {
  const name = (v.name ?? '').trim();
  if (!name || name.length > 64) throw Error('应用名不能为空且不超过64字');
  const body: Record<string, unknown> = {
    name,
    status: v.status,
    remark: textOrNull(v.remark, 255, '备注'),
    orderNotifyUrl: textOrNull(v.orderNotifyUrl, 255, '支付回调地址'),
    refundNotifyUrl: textOrNull(v.refundNotifyUrl, 255, '退款回调地址'),
  };
  if (create) {
    const appKey = (v.appKey ?? '').trim();
    if (!/^[A-Za-z0-9_-]{4,64}$/.test(appKey))
      throw Error('应用标识须为4-64位字母、数字、下划线或中划线');
    body.appKey = appKey;
  }
  return body;
}

function payChannelPayload(v: PayChannelValues, create: boolean) {
  const fee = v.feeRate == null || v.feeRate === '' ? null : Number(v.feeRate);
  if (fee != null && (!Number.isFinite(fee) || fee < 0 || fee > 100))
    throw Error('渠道费率须在0至100之间');
  const body: Record<string, unknown> = {
    status: v.status,
    feeRate: fee == null ? null : fee.toFixed(2),
    remark: textOrNull(v.remark, 255, '备注'),
    config: textOrNull(v.config, 4000, '渠道配置'),
  };
  if (create) {
    body.appId = idOf(v.appId);
    body.code = (v.code ?? '').trim();
    if (!body.code) throw Error('请选择渠道编码');
  }
  return body;
}

export async function getPayApps(q: PayAppFilter) {
  const r = await request<unknown>({
    url: '/trade/admin/pay-apps',
    params: {
      appKey: q.appKey?.trim() || undefined,
      name: q.name?.trim() || undefined,
      status: q.status ?? undefined,
      pageNum: q.pageNum,
      pageSize: q.pageSize,
    },
  });
  return { records: array(r.rows).map(payApp), total: total(r.total) };
}
export async function getPayApp(key: string) {
  return payApp((await request<unknown>({ url: '/trade/admin/pay-apps/' + idOf(key) })).data);
}
export async function savePayApp(key: string | null, v: PayAppValues) {
  if (key)
    return confirmed({
      url: '/trade/admin/pay-apps/' + idOf(key),
      method: 'PUT',
      data: payAppPayload(v, false),
    });
  return idOf(
    (
      await request<unknown>({
        url: '/trade/admin/pay-apps',
        method: 'POST',
        data: payAppPayload(v, true),
      })
    ).data,
  );
}
export async function updatePayAppStatus(key: string, status: 0 | 1) {
  return confirmed({
    url: '/trade/admin/pay-apps/' + idOf(key) + '/status',
    method: 'PUT',
    data: { status },
  });
}
export function deletePayApp(key: string) {
  return confirmed({ url: '/trade/admin/pay-apps/' + idOf(key), method: 'DELETE' });
}

export async function getPayChannels(q: PayChannelFilter) {
  const r = await request<unknown>({
    url: '/trade/admin/pay-channels',
    params: {
      appId: q.appId || undefined,
      code: q.code?.trim() || undefined,
      status: q.status ?? undefined,
      pageNum: q.pageNum,
      pageSize: q.pageSize,
    },
  });
  return { records: array(r.rows).map(payChannel), total: total(r.total) };
}
export async function getPayChannel(key: string) {
  return payChannel(
    (await request<unknown>({ url: '/trade/admin/pay-channels/' + idOf(key) })).data,
  );
}
export async function savePayChannel(key: string | null, v: PayChannelValues) {
  if (key)
    return confirmed({
      url: '/trade/admin/pay-channels/' + idOf(key),
      method: 'PUT',
      data: payChannelPayload(v, false),
    });
  return idOf(
    (
      await request<unknown>({
        url: '/trade/admin/pay-channels',
        method: 'POST',
        data: payChannelPayload(v, true),
      })
    ).data,
  );
}
export function deletePayChannel(key: string) {
  return confirmed({ url: '/trade/admin/pay-channels/' + idOf(key), method: 'DELETE' });
}
