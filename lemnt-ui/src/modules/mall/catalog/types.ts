export type TaxonomyStatus = 'ENABLED' | 'DISABLED';
export type ShelfStatus = 'ON_SHELF' | 'OFF_SHELF';
export type SpuStatus = ShelfStatus | 'RECYCLED';
export type SpuStockStatus = 'SOLD_OUT' | 'LOW_STOCK';
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
export interface SkuProperty {
  propertyId: string | null;
  propertyName: string;
  valueId: string | null;
  valueName: string;
}
export interface Sku {
  weight: string | null;
  volume: string | null;
  id: string;
  spuId: string | null;
  code: string;
  name: string;
  price: string;
  stock: number;
  image: string | null;
  properties: SkuProperty[];
  status: ShelfStatus;
}
export interface Spu {
  deliveryTypes: number[];
  deliveryTemplateId: string | null;
  id: string;
  code: string;
  name: string;
  keyword: string | null;
  introduction: string | null;
  sliderPicUrls: string[];
  sort: number | null;
  categoryId: string;
  brandId: string;
  description: string | null;
  image: string | null;
  status: SpuStatus;
  skus: Sku[];
}
/** 商品列表与状态统计共用的真实商品筛选字段。 */
export interface SpuFilter {
  name?: string;
  categoryId?: string;
  brandId?: string;
}
/** 商品分页包含单独的状态与分页参数。 */
export interface SpuQuery extends SpuFilter {
  stockStatus?: SpuStockStatus;
  status?: SpuStatus;
  pageNum?: number;
  pageSize?: number;
}
/** 服务端同筛选条件下的商品状态数量，不由当前页行数推算。 */
export interface SpuStatusCounts {
  onShelf: number;
  offShelf: number;
  recycled: number;
  soldOut: number;
  lowStock: number;
}
export interface SkuCreateRequest {
  weight?: string;
  volume?: string;
  code: string;
  name: string;
  price: number;
  stock: number;
  image?: string;
  properties: { propertyId: string; valueId: string }[];
}
export interface SpuCreateRequest {
  deliveryTypes: number[];
  deliveryTemplateId?: string;
  code: string;
  name: string;
  keyword: string;
  introduction: string;
  sliderPicUrls: string[];
  sort: number;
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
  keyword: string;
  introduction: string;
  sliderPicUrls: string[];
  sort: number;
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

export interface ProductFavoriteRecord {
  id: string;
  userId: string;
  spuId: string;
  spuName: string | null;
  picUrl: string | null;
  price: string | null;
  createTime: string;
}
export interface ProductBrowseHistoryRecord {
  id: string;
  userId: string;
  spuId: string;
  spuName: string | null;
  picUrl: string | null;
  price: string | null;
  createTime: string;
  userDeleted: boolean;
}
