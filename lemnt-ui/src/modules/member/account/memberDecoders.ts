import type {
  MemberProfile,
  MemberPage,
  MemberMetadata,
  MetadataKind,
  MemberAddress,
  MemberRecord,
  RecordKind,
  MemberPointConfig,
  MemberSignConfig,
} from './types';
export function object(v: unknown): Record<string, unknown> {
  if (!v || typeof v !== 'object' || Array.isArray(v)) throw Error('会员响应格式不合法');
  return v as Record<string, unknown>;
}
export function memberId(v: unknown): string {
  if (typeof v === 'number' && Number.isSafeInteger(v)) v = String(v);
  if (typeof v !== 'string' || !/^[1-9]\d{0,18}$/.test(v)) throw Error('会员标识不合法');
  return v;
}
function text(v: unknown): string {
  if (typeof v !== 'string') throw Error('会员文本响应不合法');
  return v;
}
function nullableText(v: unknown): string | null {
  return v == null ? null : text(v);
}
export function integer(v: unknown): number {
  if (typeof v !== 'number' || !Number.isSafeInteger(v)) throw Error('会员数值超出安全整数范围');
  return v;
}
function status(v: unknown): number {
  const n = integer(v);
  if (n !== 0 && n !== 1) throw Error('会员状态不合法');
  return n;
}
function nullableId(v: unknown): string | null {
  return v == null ? null : memberId(v);
}
/** 地区编号沿用源表有符号INT范围，不作为名称展示。 */
function areaId(value: unknown) {
  const result = integer(value);
  if (result <= 0 || result > 2147483647) throw Error('地区编号不合法');
  return result;
}
export function decodeProfile(raw: unknown): MemberProfile {
  const v = object(raw);
  return {
    id: memberId(v.id),
    nickname: text(v.nickname),
    ...(v.loginDate == null ? {} : { loginDate: text(v.loginDate) }),
    ...(v.loginIp == null ? {} : { loginIp: text(v.loginIp) }),
    ...(v.mark == null ? {} : { mark: text(v.mark) }),
    ...(v.name == null ? {} : { name: text(v.name) }),
    ...(v.areaId == null ? {} : { areaId: areaId(v.areaId) }),
    mobile: nullableText(v.mobile),
    email: nullableText(v.email),
    avatar: nullableText(v.avatar),
    status: status(v.status),
    points: integer(v.points),
    experience: integer(v.experience),
    levelId: nullableId(v.levelId),
    groupId: nullableId(v.groupId),
    sex: v.sex == null ? null : integer(v.sex),
    birthday: nullableText(v.birthday),
    createTime: nullableText(v.createTime),
  };
}
export function decodePage<T>(raw: unknown, decode: (v: unknown) => T): MemberPage<T> {
  const v = object(raw);
  if (!Array.isArray(v.rows)) throw Error('会员分页响应不合法');
  const total = integer(v.total);
  if (total < 0) throw Error('会员分页总数不合法');
  return { records: v.rows.map(decode), total };
}
export function decodeMetadata(raw: unknown, kind: MetadataKind): MemberMetadata {
  const v = object(raw);
  const base = { id: memberId(v.id), name: text(v.name) };
  if (kind === 'tags') return { ...base, remark: nullableText(v.remark) };
  if (kind === 'groups')
    return { ...base, remark: nullableText(v.remark), status: status(v.status) };
  return {
    ...base,
    status: status(v.status),
    level: integer(v.level),
    experience: integer(v.experience),
    discountPercent: integer(v.discountPercent),
    icon: nullableText(v.icon),
    backgroundUrl: nullableText(v.backgroundUrl),
  };
}
export function decodeAddress(raw: unknown): MemberAddress {
  const v = object(raw);
  if (typeof v.defaultStatus !== 'boolean') throw Error('默认地址响应不合法');
  return {
    id: memberId(v.id),
    memberId: memberId(v.memberId),
    name: text(v.name),
    mobile: text(v.mobile),
    areaId: memberId(v.areaId),
    detailAddress: text(v.detailAddress),
    defaultStatus: v.defaultStatus,
  };
}
export function decodeRecord(raw: unknown, kind: RecordKind): MemberRecord {
  const v = object(raw);
  const base = { id: memberId(v.id), memberId: memberId(v.memberId) };
  if (kind === 'signin-records')
    return {
      ...base,
      ...(v.nickname == null ? {} : { nickname: text(v.nickname) }),
      signDate: text(v.signDate),
      day: integer(v.day),
      point: integer(v.point),
      experience: integer(v.experience),
    };
  if (kind === 'level-records')
    return {
      ...base,
      levelId: nullableId(v.levelId),
      experience: integer(v.experience),
      reason: text(v.reason),
      createTime: text(v.createTime),
    };
  if (v.assetType !== (kind === 'point-records' ? 'POINT' : 'EXPERIENCE'))
    throw Error('会员资产类型不匹配');
  return {
    ...base,
    ...(kind === 'point-records' && v.pointBizType != null
      ? { pointBizType: integer(v.pointBizType) }
      : {}),
    ...((kind === 'point-records' ||
      (kind === 'experience-records' && v.experienceBizType != null)) &&
    v.title != null
      ? { title: text(v.title) }
      : {}),
    ...((kind === 'point-records' ||
      (kind === 'experience-records' && v.experienceBizType != null)) &&
    v.description != null
      ? { description: text(v.description) }
      : {}),
    ...(kind === 'point-records' && v.nickname != null ? { nickname: text(v.nickname) } : {}),
    ...(kind === 'experience-records' && v.experienceBizType != null
      ? { experienceBizType: integer(v.experienceBizType) }
      : {}),
    delta: integer(v.delta),
    balance: integer(v.balance),
    assetType: text(v.assetType),
    bizType: text(v.bizType),
    bizNo: text(v.bizNo),
    reason: text(v.reason),
    createTime: text(v.createTime),
  };
}
export function decodeConfig(raw: unknown): MemberPointConfig {
  const v = object(raw);
  if (typeof v.pointTradeDeductEnable !== 'boolean') throw Error('积分开关不合法');
  return {
    pointTradeDeductEnable: v.pointTradeDeductEnable,
    pointTradeDeductUnitPrice: integer(v.pointTradeDeductUnitPrice),
    pointTradeDeductMaxPrice: integer(v.pointTradeDeductMaxPrice),
    pointTradeGivePoint: integer(v.pointTradeGivePoint),
  };
}
export function decodeSignConfig(raw: unknown): MemberSignConfig {
  const v = object(raw);
  return {
    day: integer(v.day),
    point: integer(v.point),
    experience: integer(v.experience),
    status: status(v.status),
  };
}
export function list<T>(raw: unknown, decode: (v: unknown) => T): T[] {
  if (!Array.isArray(raw)) throw Error('会员列表响应不合法');
  return raw.map(decode);
}
export function confirmed(raw: unknown): boolean {
  if (raw !== true) throw Error('操作未确认成功，请刷新核验');
  return true;
}
