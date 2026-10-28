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
  const result = await request<unknown[]>({ url: '/mall/spus', params });
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
