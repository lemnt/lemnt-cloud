export type CouponKind = 'FIXED' | 'PERCENT';
export type CouponStatus = 'AVAILABLE' | 'LOCKED' | 'USED' | 'EXPIRED';
export type CheckoutStatus =
  'RESERVING' | 'PENDING_PAYMENT' | 'PAID' | 'SHIPPED' | 'COMPLETED' | 'CLOSED' | 'REFUNDED';
export interface CartItem {
  id: string;
  productId: string;
  quantity: number;
  selected: boolean;
  version: string;
  name: string | null;
  productCode: string | null;
  unitPrice: string | null;
  availableStock: number | null;
  sellable: boolean;
}
export interface CartSaveRequest {
  quantity: number;
  selected: boolean;
}
export interface CouponTemplate {
  id: string;
  code: string;
  name: string;
  kind: CouponKind;
  threshold: string;
  fixedAmount: string | null;
  discountPercent: number | null;
  discountLimit: string | null;
  totalCount: number;
  issuedCount: number;
  validFrom: string | null;
  validUntil: string | null;
  enabled: boolean;
  description?: string | null;
  takeType?: 'USER' | 'ADMIN' | 'REGISTER';
  takeLimitCount?: number;
  productScope?: 'ALL' | 'SPU_IDS' | 'CATEGORY_IDS';
  productScopeValues?: string[];
  validityType?: 'DATE' | 'TERM';
  fixedStartTerm?: number | null;
  fixedEndTerm?: number | null;
  useCount?: number;
}
export interface CouponTemplateCreateRequest {
  description?: string | null;
  takeType?: 'USER' | 'ADMIN' | 'REGISTER';
  takeLimitCount?: number;
  productScope?: 'ALL' | 'SPU_IDS' | 'CATEGORY_IDS';
  productScopeValues?: string[];
  validityType?: 'DATE' | 'TERM';
  fixedStartTerm?: number | null;
  fixedEndTerm?: number | null;

  code: string;
  name: string;
  kind: CouponKind;
  threshold: number;
  fixedAmount?: number;
  discountPercent?: number;
  discountLimit?: number;
  totalCount: number;
  validFrom: string | null;
  validUntil: string | null;
}
export interface Coupon {
  id: string;
  templateId: string;
  memberId: string;
  status: CouponStatus;
  orderNo: string | null;
}
export interface CheckoutItem {
  spuId?: string | null;
  spuName?: string | null;
  properties?: Record<string, string> | null;
  picUrl?: string | null;
  productId: string;
  productCode: string;
  productName: string;
  unitPrice: string;
  quantity: number;
  lineAmount: string;
}
export interface CheckoutQuote {
  quoteToken: string;
  items: CheckoutItem[];
  originalAmount: string;
  discountAmount: string;
  payableAmount: string;
  couponId: string | null;
}
export interface CheckoutReceipt extends Omit<CheckoutQuote, 'quoteToken'> {
  orderNo: string;
  memberId: string;
  status: CheckoutStatus;
}
export interface CheckoutQuoteRequest {
  productIds: string[];
  couponId?: string;
}
export interface CheckoutSubmitRequest extends CheckoutQuoteRequest {
  quoteToken: string;
  requestNo: string;
}
export interface CheckoutIntent extends CheckoutSubmitRequest {
  memberId: string;
}

export interface CouponManagementRecord extends Coupon {
  terms: {
    name: string;
    threshold: string;
    fixedAmount: string | null;
    discountPercent: number | null;
    discountLimit: string | null;
    kind: CouponKind;
    validFrom: string;
    validUntil: string;
  } | null;
  useTime: string | null;
}
