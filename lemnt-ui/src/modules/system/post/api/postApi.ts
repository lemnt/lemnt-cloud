import { request } from '@/api/request';
import type { CreatePostRequest, Post, PostQuery, UpdatePostRequest } from '../types';

/** 分页沿用顶层 rows/total，不将业务 data 当作列表。 */
export async function getPostList(params: PostQuery) {
  const result = await request<Post[]>({ url: '/system/posts', params });
  return { data: result.rows ?? [], total: result.total ?? 0, success: true };
}

/** 读取编辑资料，不能直接以列表记录替代。 */
export async function getPostDetail(postId: number): Promise<Post> {
  return (await request<Post>({ url: `/system/posts/${postId}` })).data;
}

export function createPost(data: CreatePostRequest) {
  return request<boolean>({ url: '/system/posts', method: 'POST', data });
}

export function updatePost(data: UpdatePostRequest) {
  return request<boolean>({ url: '/system/posts', method: 'PUT', data });
}

export function deletePost(ids: number[]) {
  return request<boolean>({ url: `/system/posts/${ids.join(',')}`, method: 'DELETE' });
}
