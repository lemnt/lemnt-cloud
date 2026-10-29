export type ArticleStatus = 0 | 1;
export interface ArticleCategory {
  id: string;
  name: string;
  picUrl: string | null;
  status: ArticleStatus;
  sort: number;
  createTime: string;
}
export interface Article {
  id: string;
  categoryId: string;
  spuId: string;
  title: string;
  author: string | null;
  picUrl: string;
  introduction: string | null;
  sort: number;
  status: ArticleStatus;
  recommendHot: boolean;
  recommendBanner: boolean;
  content: string;
  browseCount: number;
  createTime: string;
}
export interface ArticlePageQuery {
  categoryId?: string;
  spuId?: string;
  title?: string;
  author?: string;
  status?: ArticleStatus;
  recommendHot?: boolean;
  recommendBanner?: boolean;
  createFrom?: string;
  createTo?: string;
  pageNum: number;
  pageSize: number;
}
export interface ArticleCategoryPageQuery {
  name?: string;
  status?: ArticleStatus;
  createFrom?: string;
  createTo?: string;
  pageNum: number;
  pageSize: number;
}

export interface ArticleCategoryCreateRequest {
  name: string;
  picUrl?: string | null;
  status: ArticleStatus;
  sort: number;
}

export interface ArticleCategoryUpdateRequest {
  name: string;
  picUrl?: string | null;
  status: ArticleStatus;
  sort: number;
}

export interface ArticleCreateRequest {
  categoryId: string;
  spuId: string;
  title: string;
  author?: string | null;
  picUrl: string;
  introduction?: string | null;
  sort: number;
  status: ArticleStatus;
  recommendHot: boolean;
  recommendBanner: boolean;
  content: string;
}

export interface ArticleUpdateRequest {
  categoryId: string;
  spuId: string;
  title: string;
  author?: string | null;
  picUrl: string;
  introduction?: string | null;
  sort: number;
  status: ArticleStatus;
  recommendHot: boolean;
  recommendBanner: boolean;
  content: string;
}
