export type ActivityKind = 'reward' | 'discount';
export interface ActivitySummary {
  id: string;
  name: string;
  status: 0 | 1;
  startTime: string;
  endTime: string;
  remark: string | null;
}
export interface RewardValues {
  name: string;
  startTime: string;
  endTime: string;
  remark?: string | null;
  conditionType: 'PRICE' | 'COUNT';
  productScope: 'ALL' | 'SPU_IDS' | 'CATEGORY_IDS';
  productScopeValues: string[];
  rules: { limit: string; discountPrice: string }[];
}
export interface DiscountValues {
  name: string;
  startTime: string;
  endTime: string;
  remark?: string | null;
  products: {
    spuId: string;
    skuId: string;
    discountType: 'PRICE' | 'PERCENT';
    discountPercent: number | null;
    discountPrice: string | null;
  }[];
}
export type RewardActivity = ActivitySummary & RewardValues;
export type DiscountActivity = ActivitySummary & DiscountValues;
/** 表单内部状态，提交时转换为各业务独立请求，隐藏字段不发送。 */
export interface ActivityEditorValues {
  name: string;
  startTime: string;
  endTime: string;
  remark?: string | null;
  conditionType?: 'PRICE' | 'COUNT';
  productScope?: 'ALL' | 'SPU_IDS' | 'CATEGORY_IDS';
  productScopeValues?: string[];
  rules?: { limit: string; discountPrice: string }[];
  products?: {
    spuId: string;
    skuId: string;
    discountType: 'PRICE' | 'PERCENT';
    discountRate?: string;
    discountPrice?: string | null;
  }[];
}
export interface ActivityFilter {
  name?: string;
  status?: 0 | 1;
  pageNum: number;
  pageSize: number;
}
