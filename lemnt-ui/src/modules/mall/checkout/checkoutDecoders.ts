import type {
  CartItem,
  Coupon,
  CouponTemplate,
  CheckoutItem,
  CheckoutQuote,
  CheckoutReceipt,
} from './types';
function object(value: unknown): Record<string, unknown> {
  if (value === null || typeof value !== 'object' || Array.isArray(value))
    throw new Error('交易接口对象格式不合法');
  return value as Record<string, unknown>;
}
function text(value: unknown): string {
  if (typeof value !== 'string') throw new Error('交易接口文本格式不合法');
  return value;
}
function optionalText(value: unknown): string | null {
  return value == null ? null : text(value);
}
export function positiveId(value: unknown): string {
  const id = typeof value === 'number' && Number.isSafeInteger(value) ? String(value) : value;
  if (typeof id !== 'string' || !/^[1-9]\d{0,18}$/.test(id) || BigInt(id) > 9223372036854775807n)
    throw new Error('编号必须为正整数，且不能丢失精度');
  return id;
}
function integer(value: unknown): number {
  if (typeof value !== 'number' || !Number.isSafeInteger(value))
    throw new Error('交易接口整数格式不合法');
  return value;
}
function bool(value: unknown): boolean {
  if (typeof value !== 'boolean') throw new Error('交易接口布尔值格式不合法');
  return value;
}
function money(value: unknown): string {
  const amount = text(value);
  if (!/^\d+\.\d{2}$/.test(amount)) throw new Error('金额必须是两位小数字符串');
  return amount;
}
function optionalMoney(value: unknown): string | null {
  return value == null ? null : money(value);
}
export function decodeArray<T>(value: unknown, decode: (value: unknown) => T): T[] {
  if (!Array.isArray(value)) throw new Error('交易接口缺少列表');
  return value.map(decode);
}
export function decodeCart(value: unknown): CartItem {
  const row = object(value);
  const version =
    typeof row.version === 'number' && Number.isSafeInteger(row.version)
      ? String(row.version)
      : row.version;
  if (typeof version !== 'string' || !/^\d+$/.test(version)) throw new Error('购物车版本不合法');
  return {
    id: positiveId(row.id),
    productId: positiveId(row.productId),
    quantity: integer(row.quantity),
    selected: bool(row.selected),
    version,
    name: optionalText(row.name),
    productCode: optionalText(row.productCode),
    unitPrice: optionalMoney(row.unitPrice),
    availableStock: row.availableStock == null ? null : integer(row.availableStock),
    sellable: bool(row.sellable),
  };
}
export function decodeTemplate(value: unknown): CouponTemplate {
  const row = object(value);
  if (row.kind !== 'FIXED' && row.kind !== 'PERCENT') throw new Error('未知优惠券类型');
  return {
    id: positiveId(row.id),
    code: text(row.code),
    name: text(row.name),
    kind: row.kind,
    threshold: money(row.threshold),
    fixedAmount: optionalMoney(row.fixedAmount),
    discountPercent: row.discountPercent == null ? null : integer(row.discountPercent),
    discountLimit: optionalMoney(row.discountLimit),
    totalCount: integer(row.totalCount),
    issuedCount: integer(row.issuedCount),
    validFrom: text(row.validFrom),
    validUntil: text(row.validUntil),
    enabled: bool(row.enabled),
  };
}
export function decodeCoupon(value: unknown): Coupon {
  const row = object(value);
  if (row.status !== 'AVAILABLE' && row.status !== 'LOCKED' && row.status !== 'USED')
    throw new Error('未知优惠券状态');
  return {
    id: positiveId(row.id),
    templateId: positiveId(row.templateId),
    memberId: positiveId(row.memberId),
    status: row.status,
    orderNo: optionalText(row.orderNo),
  };
}
function decodeItem(value: unknown): CheckoutItem {
  const row = object(value);
  return {
    productId: positiveId(row.productId),
    productCode: text(row.productCode),
    productName: text(row.productName),
    unitPrice: money(row.unitPrice),
    quantity: integer(row.quantity),
    lineAmount: money(row.lineAmount),
  };
}
function decodePricing(value: unknown): Omit<CheckoutQuote, 'quoteToken'> {
  const row = object(value);
  return {
    items: decodeArray(row.items, decodeItem),
    originalAmount: money(row.originalAmount),
    discountAmount: money(row.discountAmount),
    payableAmount: money(row.payableAmount),
    couponId: row.couponId == null ? null : positiveId(row.couponId),
  };
}
export function decodeQuote(value: unknown): CheckoutQuote {
  const row = object(value);
  if (typeof row.quoteToken !== 'string' || !/^[a-f0-9]{64}$/.test(row.quoteToken))
    throw new Error('报价令牌不合法');
  return { ...decodePricing(value), quoteToken: row.quoteToken };
}
export function decodeReceipt(value: unknown): CheckoutReceipt {
  const row = object(value);
  if (
    row.status !== 'RESERVING' &&
    row.status !== 'PENDING_PAYMENT' &&
    row.status !== 'PAID' &&
    row.status !== 'SHIPPED' &&
    row.status !== 'COMPLETED' &&
    row.status !== 'CLOSED'
  )
    throw new Error('未知结算状态');
  return {
    ...decodePricing(value),
    orderNo: text(row.orderNo),
    memberId: positiveId(row.memberId),
    status: row.status,
  };
}
export function decodeBoolean(value: unknown): boolean {
  return bool(value);
}
