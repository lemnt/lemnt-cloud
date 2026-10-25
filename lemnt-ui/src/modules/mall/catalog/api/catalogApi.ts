import {
  decodeCategory,
  decodeBrand,
  decodeSpu,
  decodeList,
  decodeStockAdjustment,
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
  SkuUpdateRequest,
  StockAdjustRequest,
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
export function updateSku(id: string, data: SkuUpdateRequest) {
  return request<boolean>({ url: `/mall/skus/${id}`, method: 'PUT', data });
}
export async function adjustSkuStock(id: string, data: StockAdjustRequest) {
  const result = await request<unknown>({ url: `/mall/skus/${id}/stock`, method: 'POST', data });
  return { ...result, data: decodeStockAdjustment(result.data) };
}
