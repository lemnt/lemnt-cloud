import { positiveId } from './checkoutDecoders';
import type { CheckoutIntent } from './types';
export function intentKey(operatorId: string): string {
  return `trade-checkout-intent:${operatorId}`;
}
export function decodeIntent(value: unknown): CheckoutIntent {
  if (!value || typeof value !== 'object' || Array.isArray(value))
    throw new Error('结算请求记录不合法');
  const row = value as Record<string, unknown>;
  if (
    typeof row.quoteToken !== 'string' ||
    !/^[a-f0-9]{64}$/.test(row.quoteToken) ||
    typeof row.requestNo !== 'string' ||
    !row.requestNo.trim() ||
    row.requestNo.length > 64 ||
    !Array.isArray(row.productIds) ||
    !row.productIds.length ||
    row.productIds.length > 100
  )
    throw new Error('结算请求内容不合法');
  const ids = row.productIds.map(positiveId);
  if (new Set(ids).size !== ids.length) throw new Error('结算请求中SKU重复');
  return {
    quoteToken: row.quoteToken,
    memberId: positiveId(row.memberId),
    requestNo: row.requestNo,
    productIds: ids,
    ...(row.couponId == null ? {} : { couponId: positiveId(row.couponId) }),
  };
}
export function restoreIntent(
  storage: Pick<Storage, 'getItem'>,
  key: string,
): { intent?: CheckoutIntent; error?: string } {
  try {
    const raw = storage.getItem(key);
    return raw ? { intent: decodeIntent(JSON.parse(raw)) } : {};
  } catch (reason) {
    return { error: `无法恢复未决结算请求：${String(reason)}` };
  }
}
export function persistIntent(
  storage: Pick<Storage, 'setItem'>,
  key: string,
  value: CheckoutIntent,
): void {
  storage.setItem(key, JSON.stringify(decodeIntent(value)));
}
