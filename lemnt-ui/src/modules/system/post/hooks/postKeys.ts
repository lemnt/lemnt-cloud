import type { PostQuery } from '../types';

export const postKeys = {
  all: ['systemPosts'] as const,
  list: (params: PostQuery) => [...postKeys.all, 'list', params] as const,
  detail: (id: number) => [...postKeys.all, 'detail', id] as const,
};
