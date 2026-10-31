import { request } from '@/api/request';

export interface Wallet {
  id: string;
  memberId: string;
  balance: string;
  freezePrice: string;
  totalExpense: string;
  totalRecharge: string;
  createTime: string | null;
}
export interface WalletTransaction {
  transactionNo: string;
  bizType: string;
  bizNo: string;
  title: string;
  amount: string;
  balanceAfter: string;
  createTime: string | null;
}
export interface WalletFilter {
  memberId?: string;
  pageNum: number;
  pageSize: number;
}
export interface WalletTransactionFilter {
  bizType?: string;
  pageNum: number;
  pageSize: number;
}
export interface WalletAdjustValues {
  requestNo: string;
  amount: string;
  reason: string;
}

function record(v: unknown): Record<string, unknown> {
  if (!v || typeof v !== 'object' || Array.isArray(v)) throw Error('钱包响应不合法');
  return v as Record<string, unknown>;
}
function text(v: unknown): string {
  if (typeof v !== 'string') throw Error('钱包文本不合法');
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
/** 流水金额可为负，余额必须非负。 */
function signed(v: unknown): string {
  const value = text(v);
  if (!/^-?\d+\.\d{2}$/.test(value)) throw Error('流水金额必须为精确两位小数字符串');
  return value;
}
function array(v: unknown): unknown[] {
  if (!Array.isArray(v)) throw Error('钱包列表不合法');
  return v;
}
function total(v: unknown): number {
  if (typeof v !== 'number' || !Number.isSafeInteger(v) || v < 0) throw Error('分页总数不合法');
  return v;
}
function wallet(raw: unknown): Wallet {
  const v = record(raw);
  return {
    id: idOf(v.id),
    memberId: idOf(v.memberId),
    balance: money(v.balance),
    freezePrice: money(v.freezePrice),
    totalExpense: money(v.totalExpense),
    totalRecharge: money(v.totalRecharge),
    createTime: optional(v.createTime),
  };
}
function transaction(raw: unknown): WalletTransaction {
  const v = record(raw);
  return {
    transactionNo: text(v.transactionNo),
    bizType: text(v.bizType),
    bizNo: text(v.bizNo),
    title: text(v.title),
    amount: signed(v.amount),
    balanceAfter: money(v.balanceAfter),
    createTime: optional(v.createTime),
  };
}

export async function getWallets(q: WalletFilter) {
  const r = await request<unknown>({
    url: '/trade/admin/wallets',
    params: { memberId: q.memberId || undefined, pageNum: q.pageNum, pageSize: q.pageSize },
  });
  return { records: array(r.rows).map(wallet), total: total(r.total) };
}
export async function getWallet(memberId: string) {
  return wallet((await request<unknown>({ url: '/trade/admin/wallets/' + idOf(memberId) })).data);
}
export async function getWalletTransactions(memberId: string, q: WalletTransactionFilter) {
  const r = await request<unknown>({
    url: '/trade/admin/wallets/' + idOf(memberId) + '/transactions',
    params: { bizType: q.bizType || undefined, pageNum: q.pageNum, pageSize: q.pageSize },
  });
  return { records: array(r.rows).map(transaction), total: total(r.total) };
}
/** 调整余额以 requestNo 幂等，金额正增负减。 */
export async function adjustWallet(memberId: string, v: WalletAdjustValues) {
  const amount = v.amount.trim();
  if (!/^-?\d{1,9}(\.\d{1,2})?$/.test(amount) || /^-?0+(\.0{1,2})?$/.test(amount))
    throw Error('调整金额须为非零、最多两位小数的金额');
  const requestNo = v.requestNo.trim();
  if (!requestNo || requestNo.length > 64) throw Error('请求编号不能为空且不超过64字');
  const reason = v.reason.trim();
  if (!reason || reason.length > 128) throw Error('调整原因不能为空且不超过128字');
  return wallet(
    (
      await request<unknown>({
        url: '/trade/admin/wallets/' + idOf(memberId) + '/adjust',
        method: 'POST',
        data: { requestNo, amount, reason },
      })
    ).data,
  );
}
