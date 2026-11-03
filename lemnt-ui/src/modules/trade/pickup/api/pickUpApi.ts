import { request } from '@/api/request';

export interface PickUpStore {
  id: string;
  name: string;
  introduction: string | null;
  phone: string;
  areaId: string;
  detailAddress: string;
  logo: string | null;
  openingTime: string;
  closingTime: string;
  latitude: string | null;
  longitude: string | null;
  verifyUserIds: string[];
  status: 0 | 1;
}
export interface PickUpStoreSave {
  name: string;
  introduction?: string | null;
  phone: string;
  areaId: string;
  detailAddress: string;
  logo?: string | null;
  openingTime: string;
  closingTime: string;
  latitude?: string | null;
  longitude?: string | null;
  verifyUserIds: string[];
  status: 0 | 1;
}
export interface PickUpOrder {
  orderNo: string;
  status: string;
  storeId: string | null;
  storeName: string | null;
  storePhone: string | null;
  storeAddress: string | null;
  verifyCode: string | null;
}
function object(v: unknown): Record<string, unknown> {
  if (!v || typeof v !== 'object' || Array.isArray(v)) throw Error('自提响应格式不合法');
  return v as Record<string, unknown>;
}
function id(v: unknown, field = '编号'): string {
  const value = typeof v === 'number' && Number.isSafeInteger(v) ? String(v) : v;
  if (
    typeof value !== 'string' ||
    !/^[1-9]\d{0,18}$/.test(value) ||
    BigInt(value) > 9223372036854775807n
  )
    throw Error(`${field}不合法或已丢失精度`);
  return value;
}
function text(v: unknown, field: string): string {
  if (typeof v !== 'string') throw Error(`${field}不合法`);
  return v;
}
function optional(v: unknown, field: string): string | null {
  return v == null ? null : text(v, field);
}
function store(raw: unknown): PickUpStore {
  const v = object(raw);
  if ((v.status !== 0 && v.status !== 1) || !Array.isArray(v.verifyUserIds))
    throw Error('自提门店状态不合法');
  return {
    id: id(v.id),
    name: text(v.name, '名称'),
    introduction: optional(v.introduction, '简介'),
    phone: text(v.phone, '电话'),
    areaId: id(v.areaId, '地区编号'),
    detailAddress: text(v.detailAddress, '地址'),
    logo: optional(v.logo, 'logo'),
    openingTime: text(v.openingTime, '开门时间'),
    closingTime: text(v.closingTime, '关门时间'),
    latitude: optional(v.latitude, '纬度'),
    longitude: optional(v.longitude, '经度'),
    verifyUserIds: v.verifyUserIds.map((item) => id(item, '店员编号')),
    status: v.status,
  };
}
function order(raw: unknown): PickUpOrder {
  const v = object(raw);
  return {
    orderNo: text(v.orderNo, '订单编号'),
    status: text(v.status, '订单状态'),
    storeId: v.storeId == null ? null : id(v.storeId, '门店编号'),
    storeName: optional(v.storeName, '门店名称'),
    storePhone: optional(v.storePhone, '门店电话'),
    storeAddress: optional(v.storeAddress, '门店地址'),
    verifyCode: optional(v.verifyCode, '核销码'),
  };
}
export async function getStores(params: {
  name?: string;
  status?: number;
  pageNum: number;
  pageSize: number;
}) {
  const r = await request<unknown>({ url: '/trade/admin/delivery/pick-up-stores', params });
  if (typeof r.total !== 'number' || !Array.isArray(r.rows)) throw Error('门店列表不合法');
  return { records: (r.rows as unknown[]).map(store), total: r.total };
}
export async function createStore(value: PickUpStoreSave) {
  const r = await request<unknown>({
    url: '/trade/admin/delivery/pick-up-stores',
    method: 'POST',
    data: value,
  });
  return id(r.data, '门店编号');
}
export async function updateStore(storeId: string, value: PickUpStoreSave) {
  await request<unknown>({
    url: `/trade/admin/delivery/pick-up-stores/${id(storeId)}`,
    method: 'PUT',
    data: value,
  });
}
export async function bindStore(storeId: string, verifyUserIds: string[]) {
  await request<unknown>({
    url: `/trade/admin/delivery/pick-up-stores/${id(storeId)}/verify-users`,
    method: 'PUT',
    data: { verifyUserIds: verifyUserIds.map((item) => id(item, '店员编号')) },
  });
}
export async function deleteStore(storeId: string) {
  await request<unknown>({
    url: `/trade/admin/delivery/pick-up-stores/${id(storeId)}`,
    method: 'DELETE',
  });
}
export async function previewPickUp(code: string) {
  if (!/^\d{8}$/.test(code)) throw Error('核销码必须是8位数字');
  return order((await request<unknown>({ url: `/trade/admin/orders/pick-up/${code}` })).data);
}
export async function verifyPickUp(code: string) {
  if (!/^\d{8}$/.test(code)) throw Error('核销码必须是8位数字');
  return order(
    (await request<unknown>({ url: `/trade/admin/orders/pick-up/${code}/verify`, method: 'POST' }))
      .data,
  );
}
export async function assignPickUp(orderNo: string, storeId: string) {
  return order(
    (
      await request<unknown>({
        url: '/trade/admin/orders/pick-up',
        method: 'POST',
        data: { orderNo, storeId: id(storeId, '门店编号') },
      })
    ).data,
  );
}
