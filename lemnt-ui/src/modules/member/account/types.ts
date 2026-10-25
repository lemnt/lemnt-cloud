export interface MemberProfile {
  id: string;
  memberNo: string;
  nickname: string;
  mobile: string | null;
  email: string | null;
  avatar: string | null;
  status: number;
  points: number;
  experience: number;
  levelId: string | null;
  groupId: string | null;
  sex: number | null;
  birthday: string | null;
  createTime: string | null;
}
export interface MemberProfileValues {
  nickname: string;
  email?: string | null;
  avatar?: string | null;
  sex?: number | null;
  birthday?: string | null;
  status?: number;
}
export interface MemberCreateValues extends MemberProfileValues {
  mobile: string;
}
export interface MemberPage<T> {
  records: T[];
  total: number;
}
export interface MemberPageQuery {
  mobile?: string;
  memberId?: string;
  name?: string;
  status?: number;
  pageNum: number;
  pageSize: number;
}
export type MetadataKind = 'levels' | 'groups' | 'tags';
export interface MetadataValues {
  name: string;
  remark?: string | null;
  status?: number;
  level?: number;
  experience?: number;
  discountPercent?: number;
  icon?: string | null;
  backgroundUrl?: string | null;
}
export interface MemberMetadata extends MetadataValues {
  id: string;
}
export interface MemberAddress {
  id: string;
  memberId: string;
  name: string;
  mobile: string;
  areaId: string;
  detailAddress: string;
  defaultStatus: boolean;
}
export type RecordKind =
  'point-records' | 'experience-records' | 'level-records' | 'signin-records';
export interface MemberRecord {
  id: string;
  memberId: string;
  delta?: number;
  balance?: number;
  assetType?: string;
  bizType?: string;
  bizNo?: string;
  reason?: string;
  createTime?: string;
  levelId?: string | null;
  experience?: number;
  point?: number;
  signDate?: string;
  day?: number;
}
export interface MemberPointConfig {
  pointTradeDeductEnable: boolean;
  pointTradeDeductUnitPrice: number;
  pointTradeDeductMaxPrice: number;
  pointTradeGivePoint: number;
}
export interface MemberSignConfig {
  day: number;
  point: number;
  experience: number;
  status: number;
}
export interface MemberAdjustment {
  delta: number;
  requestNo: string;
  reason: string;
}
export const memberKeys = {
  all: ['member-account'] as const,
  users: (q: MemberPageQuery) => ['member-account', 'users', q] as const,
  detail: (id?: string) => ['member-account', 'detail', id] as const,
  metadata: (kind: MetadataKind) => ['member-account', 'metadata', kind] as const,
  records: (kind: RecordKind, q: MemberPageQuery) =>
    ['member-account', 'records', kind, q] as const,
  config: ['member-account', 'config'] as const,
  signs: ['member-account', 'signs'] as const,
};
export const memberStatusOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 },
];
export const recordPermissions: Record<RecordKind, string> = {
  'point-records': 'member:point:query',
  'experience-records': 'member:experience:query',
  'level-records': 'member:level:query',
  'signin-records': 'member:signin:query',
};
export const recordLabels: Record<RecordKind, string> = {
  'point-records': '积分流水',
  'experience-records': '经验流水',
  'level-records': '等级记录',
  'signin-records': '签到记录',
};
