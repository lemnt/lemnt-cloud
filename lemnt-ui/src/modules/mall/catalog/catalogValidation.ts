import { MAX_EDITABLE_PRICE } from './constants';
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

/** 编号按字符串传递，拒绝重复规格项，名称由服务端字典决定。 */
export function selectedProperties(
  rows: { propertyId?: string; valueId?: string }[] | undefined,
): { propertyId: string; valueId: string }[] {
  const result: { propertyId: string; valueId: string }[] = [];
  const selected = new Set<string>();
  if ((rows?.length ?? 0) > 10) throw new Error('每个SKU最多10项规格');
  for (const row of rows ?? []) {
    if (
      !row.propertyId ||
      !row.valueId ||
      !/^[1-9]\d*$/.test(row.propertyId) ||
      !/^[1-9]\d*$/.test(row.valueId)
    )
      throw new Error('请选择规格及规格值');
    if (selected.has(row.propertyId)) throw new Error('同一个SKU不能重复选择规格项');
    selected.add(row.propertyId);
    result.push({ propertyId: row.propertyId, valueId: row.valueId });
  }
  return result;
}
