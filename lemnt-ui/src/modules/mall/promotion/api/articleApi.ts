import { request } from '@/api/request';
import { decodeId } from '../../catalog/catalogDecoders';

export interface ArticleCategoryInput {
  name: string;
  picUrl?: string;
  sort: number;
  status: 0 | 1;
}
export interface ArticleInput {
  categoryId: string;
  spuId?: string;
  title: string;
  author?: string;
  picUrl?: string;
  introduction?: string;
  sort: number;
  status: 0 | 1;
  recommendHot: boolean;
  recommendBanner: boolean;
  content: string;
}
export type ArticleCategory = ArticleCategoryInput & { id: string };
export type Article = ArticleInput & { id: string; browseCount: number };
type PageQuery = { pageNum: number; pageSize: number };

function category(row: ArticleCategory): ArticleCategory {
  return { ...row, id: decodeId(row.id) };
}
function article(row: Article): Article {
  return {
    ...row,
    id: decodeId(row.id),
    categoryId: decodeId(row.categoryId),
    spuId: row.spuId == null ? undefined : decodeId(row.spuId),
  };
}

const articles = '/mall/promotion/articles';
const categories = '/mall/promotion/article-categories';
export async function getArticlePage(params: PageQuery & { title?: string }) {
  const result = await request<Article[]>({ url: articles, params });
  return {
    data: ((result.rows ?? []) as Article[]).map(article),
    total: result.total ?? 0,
    success: true,
  };
}
export async function getArticleCategories(params: PageQuery & { name?: string }) {
  const result = await request<ArticleCategory[]>({ url: categories, params });
  return {
    data: ((result.rows ?? []) as ArticleCategory[]).map(category),
    total: result.total ?? 0,
    success: true,
  };
}
export async function getEnabledArticleCategories() {
  const result = await request<ArticleCategory[]>({ url: categories + '/enabled' });
  return result.data.map(category);
}
export async function getArticle(id: string) {
  return article((await request<Article>({ url: articles + '/' + decodeId(id) })).data);
}
export function saveArticle(data: ArticleInput, id?: string) {
  return request({
    url: id ? articles + '/' + decodeId(id) : articles,
    method: id ? 'PUT' : 'POST',
    data,
  });
}
export function saveArticleCategory(data: ArticleCategoryInput, id?: string) {
  return request({
    url: id ? categories + '/' + decodeId(id) : categories,
    method: id ? 'PUT' : 'POST',
    data,
  });
}
export function deleteArticle(id: string) {
  return request<boolean>({ url: articles + '/' + decodeId(id), method: 'DELETE' });
}
export function deleteArticleCategory(id: string) {
  return request<boolean>({ url: categories + '/' + decodeId(id), method: 'DELETE' });
}
