import { request } from '@/api/request';
import type {
  CreateDictDataRequest,
  CreateDictTypeRequest,
  DictData,
  DictDataQuery,
  DictType,
  DictTypeQuery,
} from '../types';

/** 字典分页使用顶层 rows/total。 */
export async function getDictTypeList(params: DictTypeQuery) {
  const response = await request<DictType[]>({ url: '/system/dictionary-types', params });
  return { data: response.rows ?? [], total: response.total ?? 0, success: true };
}
export async function getDictTypeDetail(id: number) {
  return (await request<DictType>({ url: `/system/dictionary-types/${id}` })).data;
}
export function createDictType(data: CreateDictTypeRequest) {
  return request<boolean>({ url: '/system/dictionary-types', method: 'POST', data });
}
export function updateDictType(data: CreateDictTypeRequest & { dictId: number }) {
  return request<boolean>({ url: '/system/dictionary-types', method: 'PUT', data });
}
export function deleteDictType(ids: number[]) {
  return request<boolean>({ url: `/system/dictionary-types/${ids.join(',')}`, method: 'DELETE' });
}
/** 刷新后端缓存后，页面同步失效本地查询缓存。 */
export function refreshDictCache() {
  return request<boolean>({ url: '/system/dictionary-types/refresh-cache', method: 'DELETE' });
}
export async function getDictDataList(params: DictDataQuery) {
  const response = await request<DictData[]>({ url: '/system/dictionary-data', params });
  return { data: response.rows ?? [], total: response.total ?? 0, success: true };
}
export async function getDictDataDetail(id: number) {
  return (await request<DictData>({ url: `/system/dictionary-data/${id}` })).data;
}
export function createDictData(data: CreateDictDataRequest) {
  return request<boolean>({ url: '/system/dictionary-data', method: 'POST', data });
}
export function updateDictData(data: CreateDictDataRequest & { dictCode: number }) {
  return request<boolean>({ url: '/system/dictionary-data', method: 'PUT', data });
}
export function deleteDictData(ids: number[]) {
  return request<boolean>({ url: `/system/dictionary-data/${ids.join(',')}`, method: 'DELETE' });
}

/** 获取业务字典选项，沿用旧项目按类型查询接口。 */
export async function getDictOptions(dictType: string): Promise<DictData[]> {
  return (
    (
      await request<DictData[]>({
        url: `/system/dictionary-data/type/${encodeURIComponent(dictType)}`,
      })
    ).data ?? []
  );
}
