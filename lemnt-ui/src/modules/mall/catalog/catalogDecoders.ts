import type {
  Brand,
  Category,
  Sku,
  Spu,
  StockAdjustment,
  TaxonomyStatus,
  ShelfStatus,
} from './types';
function record(value: unknown): Record<string, unknown> {
  if (value === null || typeof value !== 'object' || Array.isArray(value))
    throw new Error('商品接口响应格式不合法');
  return value as Record<string, unknown>;
}
function text(value: unknown): string {
  if (typeof value !== 'string') throw new Error('商品接口文本字段不合法');
  return value;
}
function optionalText(value: unknown): string | null {
  return value == null ? null : text(value);
}
export function decodeId(value: unknown): string {
  if (typeof value === 'number' && Number.isSafeInteger(value) && value > 0) return String(value);
  if (typeof value === 'string' && /^[1-9]\d*$/.test(value)) return value;
  throw new Error('商品接口编号不合法或已丢失精度');
}
function integer(value: unknown): number {
  if (typeof value !== 'number' || !Number.isSafeInteger(value))
    throw new Error('商品接口整数字段不合法');
  return value;
}
function taxonomyStatus(value: unknown): TaxonomyStatus {
  if (value !== 'ENABLED' && value !== 'DISABLED') throw new Error('未知分类或品牌状态');
  return value;
}
function shelfStatus(value: unknown): ShelfStatus {
  if (value !== 'ON_SHELF' && value !== 'OFF_SHELF') throw new Error('未知上下架状态');
  return value;
}
export function decodeCategory(value: unknown): Category {
  const row = record(value);
  return {
    id: decodeId(row.id),
    parentId: row.parentId == null ? null : decodeId(row.parentId),
    name: text(row.name),
    picUrl: optionalText(row.picUrl),
    sort: integer(row.sort),
    status: taxonomyStatus(row.status),
  };
}
export function decodeBrand(value: unknown): Brand {
  const row = record(value);
  return {
    id: decodeId(row.id),
    name: text(row.name),
    picUrl: optionalText(row.picUrl),
    description: optionalText(row.description),
    sort: integer(row.sort),
    status: taxonomyStatus(row.status),
  };
}
export function decodeSku(value: unknown): Sku {
  const row = record(value);
  const price = text(row.price);
  if (!/^\d+\.\d{2}$/.test(price)) throw new Error('商品接口金额必须为两位小数字符串');
  const properties = Object.fromEntries(
    Object.entries(record(row.properties)).map(([key, item]) => [key, text(item)]),
  );
  return {
    id: decodeId(row.id),
    spuId: row.spuId == null ? null : decodeId(row.spuId),
    code: text(row.code),
    name: text(row.name),
    price,
    stock: integer(row.stock),
    image: optionalText(row.image),
    properties,
    status: shelfStatus(row.status),
  };
}
export function decodeSpu(value: unknown): Spu {
  const row = record(value);
  return {
    id: decodeId(row.id),
    code: text(row.code),
    name: text(row.name),
    categoryId: decodeId(row.categoryId),
    brandId: decodeId(row.brandId),
    description: optionalText(row.description),
    image: optionalText(row.image),
    status: shelfStatus(row.status),
    skus: decodeList(row.skus, decodeSku),
  };
}
export function decodeList<T>(value: unknown, decode: (value: unknown) => T): T[] {
  if (!Array.isArray(value)) throw new Error('商品接口缺少列表字段');
  return value.map(decode);
}
export function decodeStockAdjustment(value: unknown): StockAdjustment {
  const row = record(value);
  return {
    requestNo: text(row.requestNo),
    productId: decodeId(row.productId),
    delta: integer(row.delta),
    reason: text(row.reason),
    resultingStock: integer(row.resultingStock),
  };
}
export function decodeTotal(value: unknown): number {
  const total = integer(value);
  if (total < 0) throw new Error('分页总数不能小于0');
  return total;
}
