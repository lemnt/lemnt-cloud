import { request } from '@/api/request';
import type { Banner, BannerFilter, BannerSave } from '../types';

const path = '/mall/promotion/banners';
function object(v: unknown): Record<string, unknown> {
  if (!v || typeof v !== 'object' || Array.isArray(v)) throw Error('Banner 响应格式不合法');
  return v as Record<string, unknown>;
}
function text(v: unknown, field: string): string {
  if (typeof v !== 'string' || !v.trim()) throw Error(`${field}不合法`);
  return v;
}
function id(v: unknown): string {
  const value = typeof v === 'number' && Number.isSafeInteger(v) ? String(v) : v;
  if (
    typeof value !== 'string' ||
    !/^[1-9]\d{0,18}$/.test(value) ||
    BigInt(value) > 9223372036854775807n
  )
    throw Error('Banner 编号不合法或已丢失精度');
  return value;
}
function position(v: unknown): 1 | 2 | 3 | 4 | 5 {
  const value = typeof v === 'string' ? Number(v) : v;
  if (![1, 2, 3, 4, 5].includes(value as number)) throw Error('Banner 位置不合法');
  return value as 1 | 2 | 3 | 4 | 5;
}
function badge(v: unknown): Banner {
  const row = object(v);
  const sort = row.sort;
  const browseCount = row.browseCount;
  if (
    (row.status !== 0 && row.status !== 1) ||
    typeof sort !== 'number' ||
    !Number.isSafeInteger(sort) ||
    typeof browseCount !== 'number' ||
    !Number.isSafeInteger(browseCount) ||
    browseCount < 0
  )
    throw Error('Banner 数据不合法');
  return {
    id: id(row.id),
    title: text(row.title, '标题'),
    url: text(row.url, '跳转链接'),
    picUrl: text(row.picUrl, '图片地址'),
    sort,
    status: row.status,
    position: position(row.position),
    memo: row.memo == null ? null : String(row.memo),
    browseCount,
  };
}
function payload(value: BannerSave) {
  return {
    title: text(value.title, '标题').trim(),
    url: text(value.url, '跳转链接').trim(),
    picUrl: text(value.picUrl, '图片地址').trim(),
    sort: value.sort,
    status: value.status,
    position: position(value.position),
    memo: value.memo?.trim() || null,
  };
}
export async function getBannerPage(q: BannerFilter) {
  const r = await request<unknown>({
    url: path,
    params: {
      title: q.title?.trim() || undefined,
      status: q.status,
      pageNum: q.pageNum,
      pageSize: q.pageSize,
    },
  });
  const total = r.total;
  if (typeof total !== 'number' || !Number.isSafeInteger(total) || total < 0)
    throw Error('Banner 总数不合法');
  if (!Array.isArray(r.rows)) throw Error('Banner 列表不合法');
  return { records: (r.rows as unknown[]).map(badge), total };
}
export async function getBanner(bannerId: string): Promise<Banner> {
  return badge((await request<unknown>({ url: `${path}/${id(bannerId)}` })).data);
}
export async function createBanner(value: BannerSave) {
  return id((await request<unknown>({ url: path, method: 'POST', data: payload(value) })).data);
}
export async function updateBanner(bannerId: string, value: BannerSave) {
  await request<unknown>({ url: `${path}/${id(bannerId)}`, method: 'PUT', data: payload(value) });
}
export async function deleteBanner(bannerId: string) {
  await request<unknown>({ url: `${path}/${id(bannerId)}`, method: 'DELETE' });
}
