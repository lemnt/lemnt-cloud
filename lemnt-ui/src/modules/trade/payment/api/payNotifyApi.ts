import { request } from '@/api/request';

export type PayNotifyType = 'PAY' | 'REFUND';
export type PayNotifyStatus = 'PENDING' | 'SUCCESS' | 'FAILED';

export interface PayNotifyTask {
  id: string;
  type: PayNotifyType;
  bizNo: string;
  orderNo: string;
  status: PayNotifyStatus;
  notifyTimes: number;
  maxNotifyTimes: number;
  nextNotifyTime: string | null;
  lastExecuteTime: string | null;
  lastResponse: string | null;
  createTime: string | null;
}
export interface PayNotifyLog {
  notifyTimes: number;
  status: 'SUCCESS' | 'FAILED';
  response: string | null;
  createTime: string | null;
}
export interface PayNotifyTaskDetail {
  task: PayNotifyTask;
  logs: PayNotifyLog[];
}
export interface PayNotifyFilter {
  type?: PayNotifyType;
  bizNo?: string;
  orderNo?: string;
  status?: PayNotifyStatus;
  pageNum: number;
  pageSize: number;
}

function record(v: unknown): Record<string, unknown> {
  if (!v || typeof v !== 'object' || Array.isArray(v)) throw Error('派发任务响应不合法');
  return v as Record<string, unknown>;
}
function text(v: unknown): string {
  if (typeof v !== 'string') throw Error('派发任务文本不合法');
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
function count(v: unknown): number {
  if (typeof v !== 'number' || !Number.isSafeInteger(v) || v < 0) throw Error('尝试次数不合法');
  return v;
}
function taskType(v: unknown): PayNotifyType {
  if (v !== 'PAY' && v !== 'REFUND') throw Error('派发类型不合法');
  return v;
}
function taskStatus(v: unknown): PayNotifyStatus {
  if (v !== 'PENDING' && v !== 'SUCCESS' && v !== 'FAILED') throw Error('派发状态不合法');
  return v;
}
function logStatus(v: unknown): 'SUCCESS' | 'FAILED' {
  if (v !== 'SUCCESS' && v !== 'FAILED') throw Error('日志状态不合法');
  return v;
}
function array(v: unknown): unknown[] {
  if (!Array.isArray(v)) throw Error('派发任务列表不合法');
  return v;
}
function total(v: unknown): number {
  if (typeof v !== 'number' || !Number.isSafeInteger(v) || v < 0) throw Error('分页总数不合法');
  return v;
}
function task(raw: unknown): PayNotifyTask {
  const v = record(raw);
  return {
    id: idOf(v.id),
    type: taskType(v.type),
    bizNo: text(v.bizNo),
    orderNo: text(v.orderNo),
    status: taskStatus(v.status),
    notifyTimes: count(v.notifyTimes),
    maxNotifyTimes: count(v.maxNotifyTimes),
    nextNotifyTime: optional(v.nextNotifyTime),
    lastExecuteTime: optional(v.lastExecuteTime),
    lastResponse: optional(v.lastResponse),
    createTime: optional(v.createTime),
  };
}
function log(raw: unknown): PayNotifyLog {
  const v = record(raw);
  return {
    notifyTimes: count(v.notifyTimes),
    status: logStatus(v.status),
    response: optional(v.response),
    createTime: optional(v.createTime),
  };
}
function detail(raw: unknown): PayNotifyTaskDetail {
  const v = record(raw);
  return { task: task(v.task), logs: array(v.logs).map(log) };
}

export async function getPayNotifyTasks(q: PayNotifyFilter) {
  const r = await request<unknown>({
    url: '/trade/admin/pay-notify-tasks',
    params: {
      type: q.type || undefined,
      bizNo: q.bizNo?.trim() || undefined,
      orderNo: q.orderNo?.trim() || undefined,
      status: q.status || undefined,
      pageNum: q.pageNum,
      pageSize: q.pageSize,
    },
  });
  return { records: array(r.rows).map(task), total: total(r.total) };
}
export async function getPayNotifyTask(key: string) {
  return detail(
    (await request<unknown>({ url: '/trade/admin/pay-notify-tasks/' + idOf(key) })).data,
  );
}
/** 重发要求服务端明确成功，否则保留失败日志并提示。 */
export async function retryPayNotifyTask(key: string) {
  const r = await request<unknown>({
    url: '/trade/admin/pay-notify-tasks/' + idOf(key) + '/retry',
    method: 'POST',
  });
  if (r.data !== true) throw Error('重发未确认成功');
  return true;
}
