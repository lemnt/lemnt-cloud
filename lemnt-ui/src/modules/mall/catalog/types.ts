export type TaxonomyStatus = 'ENABLED' | 'DISABLED';
export type ShelfStatus = 'ON_SHELF' | 'OFF_SHELF';
export interface Category {
  id: string;
  parentId: string | null;
  name: string;
  picUrl: string | null;
  sort: number;
  status: TaxonomyStatus;
}
export interface Brand {
  id: string;
  name: string;
  picUrl: string | null;
  description: string | null;
  sort: number;
  status: TaxonomyStatus;
}
export interface CategorySaveRequest {
  parentId: string | null;
  name: string;
  picUrl?: string;
  sort: number;
  status: TaxonomyStatus;
}
export interface BrandSaveRequest {
  name: string;
  picUrl?: string;
  description?: string;
  sort: number;
  status: TaxonomyStatus;
}
export interface Sku {
  id: string;
  spuId: string | null;
  code: string;
  name: string;
  price: string;
  stock: number;
  image: string | null;
  properties: Record<string, string>;
  status: ShelfStatus;
}
export interface Spu {
  id: string;
  code: string;
  name: string;
  categoryId: string;
  brandId: string;
  description: string | null;
  image: string | null;
  status: ShelfStatus;
  skus: Sku[];
}
export interface SpuQuery {
  name?: string;
  status?: ShelfStatus;
  pageNum?: number;
  pageSize?: number;
}
export interface SkuCreateRequest {
  code: string;
  name: string;
  price: number;
  stock: number;
  image?: string;
  properties: Record<string, string>;
}
export interface SpuCreateRequest {
  code: string;
  name: string;
  categoryId: string;
  brandId: string;
  description?: string;
  image?: string;
  skus: SkuCreateRequest[];
}
export interface SpuUpdateRequest {
  name: string;
  categoryId: string;
  brandId: string;
  description?: string;
  image?: string;
}
export interface SkuUpdateRequest {
  name: string;
  price: number;
  image?: string;
}
export interface StockAdjustRequest {
  requestNo: string;
  delta: number;
  reason: string;
}
export interface StockAdjustment {
  requestNo: string;
  productId: string;
  delta: number;
  reason: string;
  resultingStock: number;
}
