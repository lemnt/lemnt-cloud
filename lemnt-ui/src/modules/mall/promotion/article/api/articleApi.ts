import { request } from '@/api/request';
import type {
  Article,
  ArticleCategory,
  ArticleStatus,
  ArticlePageQuery,
  ArticleCategoryPageQuery,
  ArticleCreateRequest,
  ArticleUpdateRequest,
  ArticleCategoryCreateRequest,
  ArticleCategoryUpdateRequest,
} from '../types';
function object(value: unknown): Record<string, unknown> {
  if (!value || typeof value !== 'object' || Array.isArray(value))
    throw Error('文章响应格式不合法');
  return value as Record<string, unknown>;
}
function id(value: unknown): string {
  if (typeof value === 'string' && /^[1-9]\d*$/.test(value)) return value;
  if (typeof value === 'number' && Number.isSafeInteger(value) && value > 0) return String(value);
  throw Error('文章编号不合法或已丢失精度');
}
function text(value: unknown): string {
  if (typeof value !== 'string') throw Error('文章文本格式不合法');
  return value;
}
function integer(value: unknown): number {
  if (typeof value !== 'number' || !Number.isSafeInteger(value) || value < 0)
    throw Error('文章整数不合法');
  return value;
}
function status(value: unknown): ArticleStatus {
  if (value !== 0 && value !== 1) throw Error('文章状态不合法');
  return value;
}
function flag(value: unknown): boolean {
  if (typeof value !== 'boolean') throw Error('文章推荐标记不合法');
  return value;
}
export function decodeArticleCategory(value: unknown): ArticleCategory {
  const row = object(value);
  return {
    id: id(row.id),
    name: text(row.name),
    picUrl: row.picUrl == null ? null : text(row.picUrl),
    status: status(row.status),
    sort: integer(row.sort),
    createTime: text(row.createTime),
  };
}
export function decodeArticle(value: unknown): Article {
  const row = object(value);
  return {
    id: id(row.id),
    categoryId: id(row.categoryId),
    spuId: row.spuId === 0 || row.spuId === '0' ? '0' : id(row.spuId),
    title: text(row.title),
    author: row.author == null ? null : text(row.author),
    picUrl: text(row.picUrl),
    introduction: row.introduction == null ? null : text(row.introduction),
    sort: integer(row.sort),
    status: status(row.status),
    recommendHot: flag(row.recommendHot),
    recommendBanner: flag(row.recommendBanner),
    content: text(row.content),
    browseCount: integer(row.browseCount),
    createTime: text(row.createTime),
  };
}
async function page<T>(
  path: string,
  params: ArticlePageQuery | ArticleCategoryPageQuery,
  decode: (value: unknown) => T,
) {
  const result = await request<unknown[]>({ url: '/mall/promotion/' + path, params });
  if (!Array.isArray(result.rows)) throw Error('文章分页响应不合法');
  return { data: result.rows.map(decode), total: integer(result.total), success: true };
}
export const getArticles = (params: ArticlePageQuery) => page('articles', params, decodeArticle);
export const getArticleCategories = (params: ArticleCategoryPageQuery) =>
  page('article-categories', params, decodeArticleCategory);
export async function getArticle(idValue: string) {
  return decodeArticle(
    (await request<unknown>({ url: '/mall/promotion/articles/' + id(idValue) })).data,
  );
}
export async function getArticleCategory(idValue: string) {
  return decodeArticleCategory(
    (await request<unknown>({ url: '/mall/promotion/article-categories/' + id(idValue) })).data,
  );
}
export async function getEnabledCategories(): Promise<{ id: string; name: string }[]> {
  const result = await request<unknown>({ url: '/mall/promotion/article-categories/enabled' });
  if (!Array.isArray(result.data)) throw Error('分类选项响应不合法');
  return result.data.map((value) => {
    const row = object(value);
    return { id: id(row.id), name: text(row.name) };
  });
}
async function create(path: string, data: ArticleCreateRequest | ArticleCategoryCreateRequest) {
  return id(
    object((await request<unknown>({ url: '/mall/promotion/' + path, method: 'POST', data })).data)
      .id,
  );
}
async function change(
  path: string,
  idValue: string,
  method: 'PUT' | 'DELETE',
  data?: ArticleUpdateRequest | ArticleCategoryUpdateRequest,
) {
  const result = await request<unknown>({
    url: '/mall/promotion/' + path + '/' + id(idValue),
    method,
    data,
  });
  if (object(result.data).success !== true) throw Error('文章操作结果未确认，请刷新核验');
}
export const createArticle = (data: ArticleCreateRequest) => create('articles', data);
export const updateArticle = (key: string, data: ArticleUpdateRequest) =>
  change('articles', key, 'PUT', data);
export const deleteArticle = (key: string) => change('articles', key, 'DELETE');
export const createArticleCategory = (data: ArticleCategoryCreateRequest) =>
  create('article-categories', data);
export const updateArticleCategory = (key: string, data: ArticleCategoryUpdateRequest) =>
  change('article-categories', key, 'PUT', data);
export const deleteArticleCategory = (key: string) => change('article-categories', key, 'DELETE');
/** 复用系统已认证文件上传能力，不依赖公告业务权限。 */
export async function uploadArticleImage(file: File): Promise<string> {
  if (!file.type.startsWith('image/')) throw Error('请选择图片文件');
  const data = new FormData();
  data.append('file', file);
  const result = await request<{ url: string }>({
    url: '/system/files/upload',
    method: 'POST',
    data,
  });
  if (!/^https?:\/\//.test(result.data?.url ?? '')) throw Error('图片上传未返回有效地址');
  return result.data.url;
}
