import { request } from '@/api/request';
import type {
  CreateNoticeRequest,
  Notice,
  NoticeQuery,
  NoticeReadQuery,
  NoticeReadUser,
  NoticeTopResponse,
} from '../types';

/** 顶部接口由后端限定最近五条，并返回全局未读数量。 */
export async function getNoticeTopList() {
  return (await request<NoticeTopResponse>({ url: '/system/notices/top' })).data;
}
/** 单条已读接口使用查询参数，不使用 JSON 请求体。 */
export function markNoticeRead(noticeId: number) {
  return request<boolean>({
    url: '/system/notices/mark-read',
    method: 'POST',
    params: { noticeId },
  });
}
/** 批量接口只标记指定 ID，不代表全站通知全部已读。 */
export function markNoticesRead(ids: number[]) {
  return request<boolean>({ url: '/system/notices/mark-read-all', method: 'POST', data: { ids } });
}

/** 通知列表返回顶层分页字段。 */
export async function getNoticeList(params: NoticeQuery) {
  const response = await request<Notice[]>({ url: '/system/notices', params });
  return { data: response.rows ?? [], total: response.total ?? 0, success: true };
}
export async function getNoticeDetail(id: number) {
  return (await request<Notice>({ url: `/system/notices/${id}` })).data;
}
export function createNotice(data: CreateNoticeRequest) {
  return request<boolean>({ url: '/system/notices', method: 'POST', data });
}
export function updateNotice(data: CreateNoticeRequest & { noticeId: number }) {
  return request<boolean>({ url: '/system/notices', method: 'PUT', data });
}
export function deleteNotice(ids: number[]) {
  return request<boolean>({ url: `/system/notices/${ids.join(',')}`, method: 'DELETE' });
}
/** 只查询已有阅读记录，不在管理预览中自动标记已读。 */
export async function getNoticeReadUsers(params: NoticeReadQuery) {
  const response = await request<NoticeReadUser[]>({
    url: '/system/notices/read-users',
    params,
  });
  return { data: response.rows ?? [], total: response.total ?? 0, success: true };
}
export async function uploadNoticeImage(file: File): Promise<string> {
  const data = new FormData();
  data.append('file', file);
  const response = await request<{ url: string }>({
    url: '/system/files/upload',
    method: 'POST',
    data,
  });
  if (!response.data?.url?.trim()) throw new Error('图片上传未返回有效地址');
  return response.data.url;
}
