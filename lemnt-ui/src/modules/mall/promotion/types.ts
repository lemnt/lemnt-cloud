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
export interface SeckillConfig {
  id: string;
  name: string;
  startTime: string;
  endTime: string;
  sliderPicUrls: string[];
  status: 0 | 1;
  activityCount: number;
}
export interface SeckillConfigSave {
  name: string;
  startTime: string;
  endTime: string;
  sliderPicUrls: string[];
  status: 0 | 1;
}
export interface SeckillProductLine {
  skuId: string;
  seckillPrice: string;
  stock: number;
}
export interface SeckillProduct extends SeckillProductLine {
  id: string;
  spuId: string;
  activityStartTime: string;
  activityEndTime: string;
}
export interface SeckillActivity {
  id: string;
  spuId: string;
  spuName: string | null;
  picUrl: string | null;
  seckillPrice: string | null;
  name: string;
  status: 0 | 1;
  remark: string | null;
  startTime: string;
  endTime: string;
  sort: number;
  configIds: string[];
  totalLimitCount: number;
  singleLimitCount: number;
  stock: number;
  totalStock: number;
  products: SeckillProduct[];
}
export interface SeckillActivitySave {
  spuId: string;
  name: string;
  remark: string | null;
  startTime: string;
  endTime: string;
  sort: number;
  configIds: string[];
  totalLimitCount: number;
  singleLimitCount: number;
  products: SeckillProductLine[];
}
export interface SeckillActivityFilter {
  name?: string;
  status?: 0 | 1;
  configId?: string;
  pageNum: number;
  pageSize: number;
}
export interface CombinationProductLine {
  skuId: string;
  combinationPrice: string;
}
export interface CombinationActivity {
  id: string;
  name: string;
  spuId: string;
  spuName: string | null;
  picUrl: string | null;
  combinationPrice: string | null;
  totalLimitCount: number;
  singleLimitCount: number;
  startTime: string;
  endTime: string;
  userSize: number;
  virtualGroup: boolean;
  status: 0 | 1;
  limitDuration: number;
  products: CombinationProductLine[];
}
export interface CombinationActivitySave {
  name: string;
  spuId: string;
  totalLimitCount: number;
  singleLimitCount: number;
  startTime: string;
  endTime: string;
  userSize: number;
  virtualGroup: boolean;
  limitDuration: number;
  products: CombinationProductLine[];
}
export interface CombinationActivityFilter {
  name?: string;
  status?: 0 | 1;
  spuId?: string;
  pageNum: number;
  pageSize: number;
}
export interface CombinationRecord {
  id: string;
  activityId: string;
  combinationPrice: string;
  spuId: string;
  spuName: string;
  picUrl: string | null;
  skuId: string;
  count: number;
  userId: string;
  nickname: string | null;
  avatar: string | null;
  headId: string;
  status: 0 | 1 | 2;
  orderId: string | null;
  userSize: number;
  userCount: number;
  virtualGroup: boolean;
  expireTime: string;
  startTime: string;
  endTime: string | null;
}
export interface BargainActivity {
  id: string;
  name: string;
  startTime: string;
  endTime: string;
  status: 0 | 1;
  spuId: string;
  spuName: string | null;
  picUrl: string | null;
  skuPrice: string | null;
  skuId: string;
  bargainFirstPrice: string;
  bargainMinPrice: string;
  stock: number;
  totalStock: number;
  helpMaxCount: number;
  bargainCount: number;
  totalLimitCount: number;
  randomMinPrice: string;
  randomMaxPrice: string;
  successCount: number;
}
export interface BargainActivitySave {
  name: string;
  startTime: string;
  endTime: string;
  spuId: string;
  skuId: string;
  bargainFirstPrice: string;
  bargainMinPrice: string;
  stock: number;
  helpMaxCount: number;
  bargainCount: number;
  totalLimitCount: number;
  randomMinPrice: string;
  randomMaxPrice: string;
}
export interface BargainActivityFilter {
  name?: string;
  status?: 0 | 1;
  pageNum: number;
  pageSize: number;
}
export interface BargainRecord {
  id: string;
  userId: string;
  activityId: string;
  spuId: string;
  spuName: string | null;
  picUrl: string | null;
  skuId: string;
  bargainFirstPrice: string;
  bargainPrice: string;
  status: 1 | 2 | 3;
  endTime: string | null;
  orderId: string | null;
}
export interface BargainHelp {
  id: string;
  activityId: string;
  recordId: string;
  userId: string;
  reducePrice: string;
}
export interface PointProductLine {
  skuId: string;
  exchangeCount: number;
  point: number;
  price: string;
  stock: number;
}
export interface PointActivity {
  id: string;
  spuId: string;
  spuName: string | null;
  picUrl: string | null;
  status: 0 | 1;
  remark: string | null;
  sort: number;
  stock: number;
  totalStock: number;
  point: number | null;
  price: string | null;
  marketPrice: string | null;
  products: (PointProductLine & { id: string; spuId: string })[];
}
export interface PointActivitySave {
  spuId: string;
  remark: string | null;
  sort: number;
  products: PointProductLine[];
}
export interface PointActivityFilter {
  status?: 0 | 1;
  pageNum: number;
  pageSize: number;
}
