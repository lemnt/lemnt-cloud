import axios from 'axios';
import { httpClient, notifyUnauthorized } from '@/api/interceptor';
import { getToken } from '@/shared/auth/token';
import { decodeId } from '../catalogDecoders';
import type { SpuFilter, SpuStatus, SpuStockStatus } from '../types';

/** 导出与分页是独立接口，不接受页码。 */
export interface SpuExportFilter extends SpuFilter {
  status?: SpuStatus;
  stockStatus?: SpuStockStatus;
}
const mime = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
/** 文件接口仍使用统一HTTP客户端认证；JSON业务错误不能作为Excel下载。 */
export async function exportSpus(filters: SpuExportFilter): Promise<Blob> {
  const token = getToken();
  async function fileError(blob: Blob) {
    let body: unknown;
    try {
      body = JSON.parse(await blob.text());
    } catch {
      return Error('商品导出失败，未返回Excel文件');
    }
    if (!body || typeof body !== 'object') return Error('商品导出响应不合法');
    const row = body as Record<string, unknown>;
    if (row.code === 401 || row.status === 401) notifyUnauthorized(token);
    const detail = row.detail ?? (row.code !== 200 ? row.msg : undefined);
    return Error(typeof detail === 'string' ? detail : '商品导出失败，未返回Excel文件');
  }
  function id(value?: string) {
    if (!value) return undefined;
    const key = decodeId(value);
    if (key.length > 19 || BigInt(key) > 9223372036854775807n) throw Error('商品筛选编号超出范围');
    return key;
  }
  try {
    const response = await httpClient.get<Blob>('/mall/spus/export', {
      params: {
        name: filters.name?.trim() || undefined,
        categoryId: id(filters.categoryId),
        brandId: id(filters.brandId),
        status: filters.status,
        stockStatus: filters.stockStatus,
      },
      responseType: 'blob',
      timeout: 120000,
    });
    const contentType = String(response.headers['content-type'] ?? response.data.type).split(
      ';',
    )[0];
    if (contentType !== mime) throw await fileError(response.data);
    if (response.data.size === 0) throw Error('商品导出文件为空');
    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.data instanceof Blob)
      throw await fileError(error.response.data);
    throw error;
  }
}
