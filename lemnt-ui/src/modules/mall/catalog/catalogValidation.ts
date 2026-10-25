import { MAX_EDITABLE_PRICE } from './constants';
/** Never silently coerce JSON values or overwrite available stock from stale page data. */
export function parseSkuProperties(input: string | undefined): Record<string, string> {
  const value: unknown = JSON.parse(input?.trim() || '{}');
  if (!value || typeof value !== 'object' || Array.isArray(value)) {
    throw new Error('规格必须是 JSON 对象');
  }
  const entries = Object.entries(value);
  if (entries.length > 10) throw new Error('规格最多 10 项');
  const names = new Set<string>();
  for (const [key, property] of entries) {
    if (
      !key.trim() ||
      key.trim().length > 64 ||
      typeof property !== 'string' ||
      !property.trim() ||
      property.trim().length > 100 ||
      names.has(key.trim())
    ) {
      throw new Error('规格名称或值不合法，不能重复或为空');
    }
    names.add(key.trim());
  }
  return Object.fromEntries(
    entries.map(([key, property]) => [key.trim(), String(property).trim()]),
  );
}
export function requireWriteSuccess(result: boolean) {
  if (!result) throw new Error('记录不存在或状态已变化，请刷新后重试');
}

export function editablePrice(price: string): number {
  const value = Number(price);
  if (!Number.isFinite(value) || value <= 0 || value > MAX_EDITABLE_PRICE) {
    throw new Error('该价格超出管理表单的安全编辑范围，请使用精确金额的导入接口处理');
  }
  return value;
}
