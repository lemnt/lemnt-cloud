import type { NoticeQuery, NoticeReadQuery } from '../types';
export const noticeKeys = {
  all: ['systemNotices'] as const,
  top: ['systemNotices', 'top'] as const,
  list: (params: NoticeQuery) => [...noticeKeys.all, 'list', params] as const,
  detail: (id: number) => [...noticeKeys.all, 'detail', id] as const,
  readers: (params: NoticeReadQuery) => [...noticeKeys.all, 'readers', params] as const,
};
