import { request } from '@/api/request';

export type RechargeStatus = 'PENDING' | 'PAID' | 'REFUNDED';

export interface RechargePackage {
  id: string;
  name: string;
  payPrice: string;
  bonusPrice: string;
  status: 0 | 1;
  createTime: string | null;
}
export interface WalletRecharge {
  id: string;
  rechargeNo: string;
  requestNo: string;
  walletId: string;
  memberId: string;
  packageId: string | null;
  payPrice: string;
  bonusPrice: string;
  totalPrice: string;
  status: RechargeStatus;
  channelCode: string | null;
  paidTime: string | null;
  refundedTime: string | null;
  createTime: string | null;
}
export interface RechargePackageValues {
  name: string;
  payPrice: string;
  bonusPrice: string;
  status: number;
}
export interface RechargePackageFilter {
  name?: string;
  status?: 0 | 1;
  pageNum: number;
  pageSize: number;
}
export interface WalletRechargeFilter {
  memberId?: string;
  status?: RechargeStatus;
  pageNum: number;
  pageSize: number;
}

function record(v: unknown): Record<string, unknown> {
  if (!v || typeof v !== 'object' || Array.isArray(v)) throw Error('充值响应不合法');
  return v as Record<string, unknown>;
}
function text(v: unknown): string {
  if (typeof v !== 'string') throw Error('充值文本不合法');
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
function money(v: unknown): string {
  const value = text(v);
  if (!/^\d+\.\d{2}$/.test(value)) throw Error('金额必须为精确两位小数字符串');
  return value;
}
function status(v: unknown): 0 | 1 {
  if (v !== 0 && v !== 1) throw Error('状态不合法');
  return v;
}
function rechargeStatus(v: unknown): RechargeStatus {
  if (v !== 'PENDING' && v !== 'PAID' && v !== 'REFUNDED') throw Error('充值状态不合法');
  return v;
}
function array(v: unknown): unknown[] {
  if (!Array.isArray(v)) throw Error('充值列表不合法');
  return v;
}
function total(v: unknown): number {
  if (typeof v !== 'number' || !Number.isSafeInteger(v) || v < 0) throw Error('分页总数不合法');
  return v;
}
function rechargePackage(raw: unknown): RechargePackage {
  const v = record(raw);
  return {
    id: idOf(v.id),
    name: text(v.name),
    payPrice: money(v.payPrice),
    bonusPrice: money(v.bonusPrice),
    status: status(v.status),
    createTime: optional(v.createTime),
  };
}
function walletRecharge(raw: unknown): WalletRecharge {
  const v = record(raw);
  return {
    id: idOf(v.id),
    rechargeNo: text(v.rechargeNo),
    requestNo: text(v.requestNo),
    walletId: idOf(v.walletId),
    memberId: idOf(v.memberId),
    packageId: v.packageId == null ? null : idOf(v.packageId),
    payPrice: money(v.payPrice),
    bonusPrice: money(v.bonusPrice),
    totalPrice: money(v.totalPrice),
    status: rechargeStatus(v.status),
    channelCode: optional(v.channelCode),
    paidTime: optional(v.paidTime),
    refundedTime: optional(v.refundedTime),
    createTime: optional(v.createTime),
  };
}
function requireMoney(value: string, field: string, allowZero: boolean): string {
  const text = value.trim();
  if (!/^\d{1,9}(\.\d{1,2})?$/.test(text)) throw Error(`${field}须为最多两位小数的金额`);
  const amount = Number(text);
  if (allowZero ? amount < 0 : amount <= 0)
    throw Error(`${field}${allowZero ? '不能为负' : '必须大于0'}`);
  return amount.toFixed(2);
}
async function confirmed(config: Parameters<typeof request>[0]) {
  const r = await request<unknown>(config);
  if (r.data !== true) throw Error('操作未确认');
  return true;
}

export async function getRechargePackages(q: RechargePackageFilter) {
  const r = await request<unknown>({
    url: '/trade/admin/recharge-packages',
    params: {
      name: q.name?.trim() || undefined,
      status: q.status ?? undefined,
      pageNum: q.pageNum,
      pageSize: q.pageSize,
    },
  });
  return { records: array(r.rows).map(rechargePackage), total: total(r.total) };
}
export async function getRechargePackage(key: string) {
  return rechargePackage(
    (await request<unknown>({ url: '/trade/admin/recharge-packages/' + idOf(key) })).data,
  );
}
export async function saveRechargePackage(key: string | null, v: RechargePackageValues) {
  const name = (v.name ?? '').trim();
  if (!name || name.length > 64) throw Error('套餐名不能为空且不超过64字');
  const data = {
    name,
    payPrice: requireMoney(v.payPrice, '支付金额', false),
    bonusPrice: requireMoney(v.bonusPrice, '赠送金额', true),
    status: v.status,
  };
  if (key)
    return confirmed({ url: '/trade/admin/recharge-packages/' + idOf(key), method: 'PUT', data });
  return idOf(
    (await request<unknown>({ url: '/trade/admin/recharge-packages', method: 'POST', data })).data,
  );
}
export function deleteRechargePackage(key: string) {
  return confirmed({ url: '/trade/admin/recharge-packages/' + idOf(key), method: 'DELETE' });
}

export async function getWalletRecharges(q: WalletRechargeFilter) {
  const r = await request<unknown>({
    url: '/trade/admin/wallet-recharges',
    params: {
      memberId: q.memberId || undefined,
      status: q.status || undefined,
      pageNum: q.pageNum,
      pageSize: q.pageSize,
    },
  });
  return { records: array(r.rows).map(walletRecharge), total: total(r.total) };
}
export async function getWalletRecharge(no: string) {
  return walletRecharge(
    (await request<unknown>({ url: '/trade/admin/wallet-recharges/' + encodeURIComponent(no) }))
      .data,
  );
}
export async function confirmWalletRecharge(no: string, channelCode?: string) {
  return walletRecharge(
    (
      await request<unknown>({
        url: '/trade/admin/wallet-recharges/' + encodeURIComponent(no) + '/confirm',
        method: 'POST',
        data: { channelCode: channelCode || null },
      })
    ).data,
  );
}
export async function refundWalletRecharge(no: string) {
  return walletRecharge(
    (
      await request<unknown>({
        url: '/trade/admin/wallet-recharges/' + encodeURIComponent(no) + '/refund',
        method: 'POST',
      })
    ).data,
  );
}
