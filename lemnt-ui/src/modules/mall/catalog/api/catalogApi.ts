import {
  decodeId,
  decodeCategory,
  decodeBrand,
  decodeSpu,
  decodeList,
  decodeTotal,
} from '../catalogDecoders';
import { request } from '@/api/request';
import type {
  Category,
  Brand,
  CategorySaveRequest,
  BrandSaveRequest,
  Spu,
  SpuQuery,
  SpuFilter,
  SpuStatusCounts,
  SpuCreateRequest,
  SpuUpdateRequest,
} from '../types';
export async function getCategoryList(): Promise<Category[]> {
  return decodeList((await request<unknown>({ url: '/mall/categories' })).data, decodeCategory);
}
export async function getBrandList(): Promise<Brand[]> {
  return decodeList((await request<unknown>({ url: '/mall/brands' })).data, decodeBrand);
}
export async function getCategoryDetail(id: string): Promise<Category> {
  return decodeCategory((await request<unknown>({ url: `/mall/categories/${id}` })).data);
}
export async function getBrandDetail(id: string): Promise<Brand> {
  return decodeBrand((await request<unknown>({ url: `/mall/brands/${id}` })).data);
}
export async function createCategory(data: CategorySaveRequest) {
  const result = await request<unknown>({ url: '/mall/categories', method: 'POST', data });
  return { ...result, data: decodeCategory(result.data) };
}
export async function updateCategory(id: string, data: CategorySaveRequest) {
  const result = await request<unknown>({ url: `/mall/categories/${id}`, method: 'PUT', data });
  return { ...result, data: decodeCategory(result.data) };
}
export async function createBrand(data: BrandSaveRequest) {
  const result = await request<unknown>({ url: '/mall/brands', method: 'POST', data });
  return { ...result, data: decodeBrand(result.data) };
}
export async function updateBrand(id: string, data: BrandSaveRequest) {
  const result = await request<unknown>({ url: `/mall/brands/${id}`, method: 'PUT', data });
  return { ...result, data: decodeBrand(result.data) };
}
export function deleteCategory(id: string) {
  return request<boolean>({ url: `/mall/categories/${id}`, method: 'DELETE' });
}
export function deleteBrand(id: string) {
  return request<boolean>({ url: `/mall/brands/${id}`, method: 'DELETE' });
}
export async function getSpuList(params: SpuQuery) {
  const result = await request<unknown[]>({
    url: '/mall/spus',
    params: {
      ...spuFilters(params),
      status: params.status,
      stockStatus: params.stockStatus,
      pageNum: params.pageNum,
      pageSize: params.pageSize,
    },
  });
  return {
    data: decodeList(result.rows, decodeSpu),
    total: decodeTotal(result.total),
    success: true,
  };
}
export async function getSpuDetail(id: string): Promise<Spu> {
  return decodeSpu((await request<unknown>({ url: `/mall/spus/${id}` })).data);
}
export async function createSpu(data: SpuCreateRequest) {
  const result = await request<unknown>({ url: '/mall/spus', method: 'POST', data });
  return { ...result, data: decodeSpu(result.data) };
}
export function updateSpu(id: string, data: SpuUpdateRequest) {
  return request<boolean>({ url: `/mall/spus/${id}`, method: 'PUT', data });
}
export function updateSpuShelf(id: string, onShelf: boolean) {
  return request<boolean>({ url: `/mall/spus/${id}/shelf`, method: 'PUT', data: { onShelf } });
}

/** 读取交易服务运费模板选择项，不在商城伪造模板列表。 */
export async function getDeliveryTemplates(): Promise<{ id: string; name: string }[]> {
  const response = await request<unknown>({ url: '/trade/admin/delivery/templates/simple' });
  return decodeList(response.data, (value) => {
    if (!value || typeof value !== 'object') throw new Error('运费模板响应不合法');
    const row = value as Record<string, unknown>;
    if (typeof row.name !== 'string') throw new Error('运费模板名称不合法');
    return { id: decodeId(row.id), name: row.name };
  });
}

/** 规格选择使用原字典接口；关键字可搜索首屏之外的选项。 */
export async function getSpecOptions(name?: string): Promise<{ id: string; name: string }[]> {
  return decodeList(
    (await request<unknown>({ url: '/mall/specs/simple', params: { name, limit: 100 } })).data,
    specOption,
  );
}
export async function getSpecValueOptions(
  specId: string,
  name?: string,
): Promise<{ id: string; name: string }[]> {
  return decodeList(
    (
      await request<unknown>({
        url: '/mall/spec-values/simple',
        params: { specId, name, limit: 100 },
      })
    ).data,
    specOption,
  );
}
export async function getSpecOption(id: string) {
  return specOption(
    (await request<unknown>({ url: `/mall/specs/${encodeURIComponent(id)}` })).data,
  );
}
export async function getSpecValueOption(id: string) {
  return specOption(
    (await request<unknown>({ url: `/mall/spec-values/${encodeURIComponent(id)}` })).data,
  );
}
function specOption(value: unknown): { id: string; name: string } {
  if (!value || typeof value !== 'object' || Array.isArray(value))
    throw new Error('规格选项格式不合法');
  const row = value as Record<string, unknown>;
  if (typeof row.name !== 'string') throw new Error('规格名称不合法');
  return { id: decodeId(row.id), name: row.name };
}

/** 商品详情下的独立只读收藏记录。 */
export async function getProductFavorites(params: {
  spuId: string;
  userId?: string;
  pageNum: number;
  pageSize: number;
}) {
  const result = await request<unknown>({ url: '/mall/product-favorites', params });
  return {
    data: decodeList(result.rows, interactionRecord),
    total: decodeTotal(result.total),
    success: true,
  };
}
/** 足迹隐藏状态必须保留false，不能当作未传。 */
export async function getProductBrowseHistory(params: {
  spuId: string;
  userId?: string;
  userDeleted?: boolean;
  pageNum: number;
  pageSize: number;
}) {
  const result = await request<unknown>({ url: '/mall/product-browse-history', params });
  return {
    data: decodeList(result.rows, (value) => {
      const row = value as Record<string, unknown>;
      if (!row || typeof row.userDeleted !== 'boolean') throw new Error('足迹隐藏状态不合法');
      return { ...interactionRecord(value), userDeleted: row.userDeleted };
    }),
    total: decodeTotal(result.total),
    success: true,
  };
}
function interactionRecord(value: unknown): import('../types').ProductFavoriteRecord {
  if (!value || typeof value !== 'object' || Array.isArray(value))
    throw new Error('商品记录格式不合法');
  const row = value as Record<string, unknown>;
  if (
    typeof row.createTime !== 'string' ||
    (row.spuName != null && typeof row.spuName !== 'string') ||
    (row.picUrl != null && typeof row.picUrl !== 'string') ||
    (row.price != null && (typeof row.price !== 'string' || !/^\d+\.\d{2}$/.test(row.price)))
  )
    throw new Error('商品记录字段不合法');
  return {
    id: decodeId(row.id),
    userId: decodeId(row.userId),
    spuId: decodeId(row.spuId),
    spuName: (row.spuName as string | null) ?? null,
    picUrl: (row.picUrl as string | null) ?? null,
    price: (row.price as string | null) ?? null,
    createTime: row.createTime,
  };
}

/** 商品列表和数量统计保持相同的名称、精确分类及品牌条件。 */
function spuFilters(params: SpuFilter) {
  return {
    name: params.name?.trim() || undefined,
    categoryId: params.categoryId ? productId(params.categoryId) : undefined,
    brandId: params.brandId ? productId(params.brandId) : undefined,
  };
}
/** 校验Long编号，防止路径注入或超过服务端编号范围。 */
function productId(value: string) {
  const id = decodeId(value);
  if (id.length > 19 || BigInt(id) > 9223372036854775807n) throw Error('商品编号超出范围');
  return id;
}
/** 查询服务端真实商品状态数量，统计条件不混入页码或当前状态。 */
export async function getSpuStatusCounts(params: SpuFilter): Promise<SpuStatusCounts> {
  const response = await request<unknown>({ url: '/mall/spus/count', params: spuFilters(params) });
  if (!response.data || typeof response.data !== 'object' || Array.isArray(response.data))
    throw Error('商品状态统计响应不合法');
  const row = response.data as Record<string, unknown>;
  return {
    onShelf: decodeTotal(row.onShelf),
    offShelf: decodeTotal(row.offShelf),
    recycled: decodeTotal(row.recycled),
    soldOut: decodeTotal(row.soldOut),
    lowStock: decodeTotal(row.lowStock),
  };
}
/** 下架商品进入回收站，由服务端校验实际状态。 */
export async function recycleSpu(id: string) {
  if (
    (await request<unknown>({ url: '/mall/spus/' + productId(id) + '/recycle', method: 'POST' }))
      .data !== true
  )
    throw Error('商品回收未成功');
  return true;
}
/** 回收站商品恢复为下架，不能直接恢复上架。 */
export async function restoreSpu(id: string) {
  if (
    (await request<unknown>({ url: '/mall/spus/' + productId(id) + '/restore', method: 'POST' }))
      .data !== true
  )
    throw Error('商品恢复未成功');
  return true;
}
/** 删除回收站商品，历史订单和库存台账保留。 */
export async function deleteSpu(id: string) {
  if (
    (await request<unknown>({ url: '/mall/spus/' + productId(id), method: 'DELETE' })).data !== true
  )
    throw Error('商品删除未成功');
  return true;
}
