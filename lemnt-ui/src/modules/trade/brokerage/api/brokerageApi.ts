import { request } from '@/api/request';

export interface BrokerageUser {
  id: string; bindUserId: string | null; bindNickname: string | null; bindAvatar: string | null;
  brokerageEnabled: boolean; brokeragePrice: string; frozenPrice: string;
  firstLevelCount: number; secondLevelCount: number;
}
export interface BrokerageRecord {
  id: string; userId: string; bizId: string; bizType: number; title: string; description: string | null;
  price: string; totalPrice: string; status: number; frozenDays: number | null;
  unfreezeTime: string | null; sourceUserLevel: number | null; createTime: string | null;
}
export interface BrokerageWithdraw {
  id: string; userId: string; price: string; feePrice: string; totalPrice: string; type: number;
  userName: string; userAccount: string; qrCodeUrl: string | null; bankName: string | null;
  bankAddress: string | null; status: number; auditReason: string | null; auditTime: string | null;
  createTime: string | null;
}
function object(v: unknown): Record<string, unknown> {
  if (!v || typeof v !== 'object' || Array.isArray(v)) throw Error('分销响应格式不合法');
  return v as Record<string, unknown>;
}
function id(v: unknown, field = '编号'): string {
  const value = typeof v === 'number' && Number.isSafeInteger(v) ? String(v) : v;
  if (typeof value !== 'string' || !/^[1-9]\d{0,18}$/.test(value) || BigInt(value) > 9223372036854775807n)
    throw Error(`${field}不合法或已丢失精度`);
  return value;
}
function money(v: unknown): string {
  const value = typeof v === 'number' ? v.toFixed(2) : v;
  if (typeof value !== 'string' || !/^\d{1,17}\.\d{2}$/.test(value)) throw Error('佣金金额不合法');
  return value;
}
function text(v: unknown, field: string): string {
  if (typeof v !== 'string') throw Error(`${field}不合法`);
  return v;
}
function optional(v: unknown, field: string): string | null {
  return v == null ? null : text(v, field);
}
function user(raw: unknown): BrokerageUser {
  const v = object(raw);
  if (typeof v.brokerageEnabled !== 'boolean' || !Number.isSafeInteger(v.firstLevelCount)
    || !Number.isSafeInteger(v.secondLevelCount)) throw Error('分销用户不合法');
  return {
    id: id(v.id, '会员编号'), bindUserId: v.bindUserId == null ? null : id(v.bindUserId, '推广员编号'),
    bindNickname: optional(v.bindNickname, '推广员昵称'), bindAvatar: optional(v.bindAvatar, '推广员头像'),
    brokerageEnabled: v.brokerageEnabled, brokeragePrice: money(v.brokeragePrice),
    frozenPrice: money(v.frozenPrice), firstLevelCount: v.firstLevelCount as number,
    secondLevelCount: v.secondLevelCount as number,
  };
}
function record(raw: unknown): BrokerageRecord {
  const v = object(raw);
  if (!Number.isInteger(v.bizType) || !Number.isInteger(v.status)) throw Error('佣金记录不合法');
  return {
    id: id(v.id), userId: id(v.userId, '会员编号'), bizId: text(v.bizId, '业务编号'),
    bizType: v.bizType as number, title: text(v.title, '标题'),
    description: optional(v.description, '说明'), price: money(v.price), totalPrice: money(v.totalPrice),
    status: v.status as number, frozenDays: v.frozenDays == null ? null : Number(v.frozenDays),
    unfreezeTime: optional(v.unfreezeTime, '解冻时间'),
    sourceUserLevel: v.sourceUserLevel == null ? null : Number(v.sourceUserLevel),
    createTime: optional(v.createTime, '创建时间'),
  };
}
function withdraw(raw: unknown): BrokerageWithdraw {
  const v = object(raw);
  if (!Number.isInteger(v.type) || !Number.isInteger(v.status)) throw Error('提现单不合法');
  return {
    id: id(v.id), userId: id(v.userId, '会员编号'), price: money(v.price), feePrice: money(v.feePrice),
    totalPrice: money(v.totalPrice), type: v.type as number, userName: text(v.userName, '收款人'),
    userAccount: text(v.userAccount, '收款账号'), qrCodeUrl: optional(v.qrCodeUrl, '收款码'),
    bankName: optional(v.bankName, '银行名称'), bankAddress: optional(v.bankAddress, '开户地址'),
    status: v.status as number, auditReason: optional(v.auditReason, '审核说明'),
    auditTime: optional(v.auditTime, '审核时间'), createTime: optional(v.createTime, '创建时间'),
  };
}
function pageResult<T>(r: { total?: unknown; rows?: unknown }, decoder: (v: unknown) => T) {
  if (typeof r.total !== 'number' || !Number.isSafeInteger(r.total) || r.total < 0)
    throw Error('分销总数不合法');
  if (!Array.isArray(r.rows)) throw Error('分销列表不合法');
  return { records: (r.rows as unknown[]).map(decoder), total: r.total };
}
export async function getUsers(params: { bindUserId?: string; brokerageEnabled?: boolean; pageNum: number; pageSize: number }) {
  return pageResult(await request<unknown>({ url: '/trade/admin/brokerage/users', params }), user);
}
export async function setEnabled(memberId: string, enabled: boolean) {
  await request<unknown>({ url: `/trade/admin/brokerage/users/${id(memberId)}/enabled`, method: 'PUT', data: { enabled } });
}
export async function updateBind(memberId: string, bindUserId: string | null) {
  await request<unknown>({
    url: `/trade/admin/brokerage/users/${id(memberId)}/bind`, method: 'PUT',
    data: { bindUserId: bindUserId == null || !bindUserId.trim() ? null : id(bindUserId.trim(), '推广员编号') },
  });
}
export async function getRecords(params: { userId?: string; bizType?: number; status?: number; pageNum: number; pageSize: number }) {
  return pageResult(await request<unknown>({ url: '/trade/admin/brokerage/records', params }), record);
}
export async function getWithdraws(params: { userId?: string; status?: number; pageNum: number; pageSize: number }) {
  return pageResult(await request<unknown>({ url: '/trade/admin/brokerage/withdraws', params }), withdraw);
}
export async function auditWithdraw(withdrawId: string, approved: boolean, reason?: string) {
  await request<unknown>({
    url: `/trade/admin/brokerage/withdraws/${id(withdrawId)}/audit`, method: 'POST',
    data: { approved, reason: reason || null },
  });
}
export async function unfreeze() {
  const r = await request<unknown>({ url: '/trade/admin/brokerage/unfreeze', method: 'POST', params: { limit: 100 } });
  if (typeof r.data !== 'number') throw Error('解冻结果不合法');
  return r.data;
}
