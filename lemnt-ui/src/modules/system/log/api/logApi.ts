import { request } from '@/api/request';
import type { LoginLog, LoginLogQuery, OperationLog, OperationLogQuery } from '../types';

/** 登录日志分页返回顶层 rows/total。 */
export async function getLoginLogList(params: LoginLogQuery) {
  const response = await request<LoginLog[]>({ url: '/system/login-logs', params });
  return { data: response.rows ?? [], total: response.total ?? 0, success: true };
}
/** 操作详情已包含在分页记录中，后端没有单独详情端点。 */
export async function getOperationLogList(params: OperationLogQuery) {
  const response = await request<OperationLog[]>({ url: '/system/operation-logs', params });
  return { data: response.rows ?? [], total: response.total ?? 0, success: true };
}
/** 删除日志前由页面确认；ID 以逗号分隔路径提交。 */
export function deleteLoginLog(ids: number[]) {
  return request<boolean>({ url: `/system/login-logs/${ids.join(',')}`, method: 'DELETE' });
}
export function deleteOperationLog(ids: number[]) {
  return request<boolean>({ url: `/system/operation-logs/${ids.join(',')}`, method: 'DELETE' });
}
/** 清空针对全库日志，而非当前筛选结果。 */
export function cleanLoginLog() {
  return request<boolean>({ url: '/system/login-logs/clean', method: 'DELETE' });
}
export function cleanOperationLog() {
  return request<boolean>({ url: '/system/operation-logs/clean', method: 'DELETE' });
}
