export type TaxonomyStatus = 'ENABLED' | 'DISABLED';
export type ShelfStatus = 'ON_SHELF' | 'OFF_SHELF';
export type SpuStatus = ShelfStatus | 'RECYCLED';
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
  specValueIds?: Record<string, string> | null;
  weight: string | null;
  volume: string | null;
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
  deliveryTypes: number[];
  deliveryTemplateId: string | null;
  id: string;
  code: string;
  name: string;
  categoryId: string;
  brandId: string;
  description: string | null;
  image: string | null;
  status: SpuStatus;
  skus: Sku[];
}
export interface SpuQuery {
  name?: string;
  status?: SpuStatus;
  pageNum?: number;
  pageSize?: number;
}
export interface SkuCreateRequest {
  specValueIds?: Record<string, string>;
  weight?: string;
  volume?: string;
  code: string;
  name: string;
  price: number;
  stock: number;
  image?: string;
  properties: Record<string, string>;
}
export interface SpuCreateRequest {
  deliveryTypes: number[];
  deliveryTemplateId?: string;
  code: string;
  name: string;
  categoryId: string;
  brandId: string;
  description?: string;
  image?: string;
  skus: SkuCreateRequest[];
}
export interface SpuUpdateRequest {
  deliveryTypes: number[];
  deliveryTemplateId?: string;
  skus: (SkuCreateRequest & { id?: string })[];
  name: string;
  categoryId: string;
  brandId: string;
  description?: string;
  image?: string;
}
export interface StockAdjustment {
  requestNo: string;
  productId: string;
  delta: number;
  reason: string;
  resultingStock: number;
}
