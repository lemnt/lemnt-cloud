import { request } from '@/api/request';
import {
  memberId,
  decodeProfile,
  decodePage,
  decodeMetadata,
  decodeAddress,
  decodeRecord,
  decodeConfig,
  decodeSignConfig,
  list,
  confirmed,
} from '../memberDecoders';
import type {
  MemberPageQuery,
  MemberUserPageQuery,
  MemberGroupPageQuery,
  MemberTagPageQuery,
  MemberProfileValues,
  MetadataKind,
  LevelFilterValues,
  MetadataValues,
  RecordKind,
  MemberAdjustment,
  MemberPointConfig,
  MemberSignConfigValues,
} from '../types';
const user = (id: string) => `/member/member-users/${memberId(id)}`;
export async function getMembers(q: MemberUserPageQuery) {
  return decodePage(
    await request<unknown>({
      url: '/member/member-users',
      params: {
        mobile: q.mobile?.trim() || undefined,
        email: q.email?.trim() || undefined,
        nickname: q.nickname?.trim() || undefined,
        levelId: q.levelId ? memberId(q.levelId) : undefined,
        groupId: q.groupId ? memberId(q.groupId) : undefined,
        tagIds: q.tagIds?.length ? [...new Set(q.tagIds.map(memberId))] : undefined,
        loginStartTime: q.loginStartTime || undefined,
        loginEndTime: q.loginEndTime || undefined,
        createStartTime: q.createStartTime || undefined,
        createEndTime: q.createEndTime || undefined,
        pageNum: q.pageNum,
        pageSize: q.pageSize,
      },
      paramsSerializer: { indexes: null },
    }),
    decodeProfile,
  );
}
export async function getMember(id: string) {
  const raw = (await request<unknown>({ url: user(id) })).data;
  if (raw == null) throw Error('会员不存在');
  return decodeProfile(raw);
}
function profile(v: MemberProfileValues) {
  return {
    nickname: v.nickname,
    ...(v.mobile == null ? {} : { mobile: v.mobile.trim() }),
    ...(v.mark === undefined ? {} : { mark: v.mark }),
    ...(v.name === undefined ? {} : { name: v.name }),
    ...(v.areaId === undefined ? {} : { areaId: v.areaId }),
    email: v.email || null,
    avatar: v.avatar || null,
    sex: v.sex ?? null,
    birthday: v.birthday || null,
    status: v.status,
  };
}

export async function updateMember(id: string, v: MemberProfileValues) {
  return confirmed(
    (await request<unknown>({ url: user(id), method: 'PUT', data: profile(v) })).data,
  );
}

export async function adjustMember(id: string, kind: 'points', v: MemberAdjustment) {
  return decodeRecord(
    (
      await request<unknown>({
        url: user(id) + '/' + kind,
        method: 'PUT',
        data: { delta: v.delta, requestNo: v.requestNo, reason: v.reason },
      })
    ).data,
    kind === 'points' ? 'point-records' : 'experience-records',
  );
}
export async function assignMemberLevel(
  id: string,
  levelId: string | null,
  requestNo: string,
  reason: string,
) {
  return confirmed(
    (
      await request<unknown>({
        url: user(id) + '/level',
        method: 'PUT',
        data: { levelId: levelId ? memberId(levelId) : null, requestNo, reason },
      })
    ).data,
  );
}
export async function assignMemberGroup(id: string, groupId: string | null) {
  return confirmed(
    (
      await request<unknown>({
        url: user(id) + '/group',
        method: 'PUT',
        data: { groupId: groupId ? memberId(groupId) : null },
      })
    ).data,
  );
}
export async function getMemberTags(id: string) {
  return list((await request<unknown>({ url: user(id) + '/tags' })).data, memberId);
}
export async function assignMemberTags(id: string, tags: string[]) {
  return confirmed(
    (
      await request<unknown>({
        url: user(id) + '/tags',
        method: 'PUT',
        data: { tagIds: tags.map(memberId) },
      })
    ).data,
  );
}
export async function getMemberAddresses(id: string) {
  return list((await request<unknown>({ url: user(id) + '/addresses' })).data, decodeAddress);
}
const metadataPaths: Record<MetadataKind, string> = {
  levels: '/member/growth/levels',
  groups: '/member/groups',
  tags: '/member/tags',
};
/** 分组分页独立入参，状态0必须保留。 */
export async function getGroupPage(q: MemberGroupPageQuery) {
  return decodePage(
    await request<unknown>({
      url: '/member/groups/page',
      params: {
        name: q.name?.trim() || undefined,
        status: q.status,
        startTime: q.startTime || undefined,
        endTime: q.endTime || undefined,
        pageNum: q.pageNum,
        pageSize: q.pageSize,
      },
    }),
    (v) => decodeMetadata(v, 'groups'),
  );
}
/** 标签分页不发送分组状态或会员编号。 */
export async function getTagPage(q: MemberTagPageQuery) {
  return decodePage(
    await request<unknown>({
      url: '/member/tags/page',
      params: {
        name: q.name?.trim() || undefined,
        startTime: q.startTime || undefined,
        endTime: q.endTime || undefined,
        pageNum: q.pageNum,
        pageSize: q.pageSize,
      },
    }),
    (v) => decodeMetadata(v, 'tags'),
  );
}
export async function getMetadata(kind: MetadataKind, filters: LevelFilterValues = {}) {
  if (kind === 'levels' && filters.status !== undefined && ![0, 1].includes(filters.status))
    throw Error('等级状态不合法');
  return list(
    (
      await request<unknown>({
        url: metadataPaths[kind],
        ...(kind === 'levels'
          ? { params: { name: filters.name?.trim() || undefined, status: filters.status } }
          : {}),
      })
    ).data,
    (v) => decodeMetadata(v, kind),
  );
}
export async function saveMetadata(kind: MetadataKind, id: string | undefined, v: MetadataValues) {
  const data =
    kind === 'levels'
      ? {
          name: v.name,
          level: v.level,
          experience: v.experience,
          discountPercent: v.discountPercent,
          icon: v.icon || null,
          backgroundUrl: v.backgroundUrl || null,
          status: v.status,
        }
      : kind === 'groups'
        ? { name: v.name, remark: v.remark || null, status: v.status }
        : { name: v.name, remark: v.remark || null };
  return memberId(
    (
      await request<unknown>({
        url: metadataPaths[kind] + (id ? '/' + memberId(id) : ''),
        method: id ? 'PUT' : 'POST',
        data,
      })
    ).data,
  );
}
export async function deleteMetadata(kind: MetadataKind, id: string) {
  return confirmed(
    (
      await request<unknown>({
        url: metadataPaths[kind] + '/' + memberId(id),
        method: 'DELETE',
      })
    ).data,
  );
}
export async function getMemberRecords(kind: RecordKind, q: MemberPageQuery) {
  if (kind === 'signin-records' && q.day !== undefined && (!Number.isInteger(q.day) || q.day < 1))
    throw Error('签到天数必须为正整数');
  if (kind === 'point-records' && q.pointBizType !== undefined && !Number.isInteger(q.pointBizType))
    throw Error('积分业务类型必须为整数');
  if (
    kind === 'experience-records' &&
    q.experienceBizType !== undefined &&
    !Number.isInteger(q.experienceBizType)
  )
    throw Error('经验业务类型必须为整数');
  return decodePage(
    await request<unknown>({
      url: '/member/admin/' + kind,
      params: {
        memberId: q.memberId ? memberId(q.memberId) : undefined,
        ...(['signin-records', 'point-records'].includes(kind) && q.nickname?.trim()
          ? { nickname: q.nickname.trim() }
          : {}),
        ...(kind === 'point-records' && q.bizType?.trim() ? { bizType: q.bizType.trim() } : {}),
        ...(kind === 'point-records' && q.pointBizType !== undefined
          ? { pointBizType: q.pointBizType }
          : {}),
        ...(kind === 'point-records' && q.title?.trim() ? { title: q.title.trim() } : {}),
        ...(kind === 'experience-records'
          ? {
              experienceBizType: q.experienceBizType,
              bizNo: q.bizNo?.trim() || undefined,
              title: q.title?.trim() || undefined,
              startTime: q.startTime || undefined,
              endTime: q.endTime || undefined,
            }
          : {}),
        ...(kind === 'signin-records'
          ? { day: q.day, startTime: q.startTime || undefined, endTime: q.endTime || undefined }
          : {}),
        ...(kind === 'level-records'
          ? {
              levelId: q.levelId ? memberId(q.levelId) : undefined,
              startTime: q.startTime || undefined,
              endTime: q.endTime || undefined,
            }
          : {}),
        pageNum: q.pageNum,
        pageSize: q.pageSize,
      },
    }),
    (v) => decodeRecord(v, kind),
  );
}
export async function getPointConfig() {
  const raw = (await request<unknown>({ url: '/member/growths/config' })).data;
  return raw == null ? null : decodeConfig(raw);
}
export async function savePointConfig(values: MemberPointConfig) {
  const data = {
    pointTradeDeductEnable: values.pointTradeDeductEnable,
    pointTradeDeductUnitPrice: values.pointTradeDeductUnitPrice,
    pointTradeDeductMaxPrice: values.pointTradeDeductMaxPrice,
    pointTradeGivePoint: values.pointTradeGivePoint,
  };
  return confirmed(
    (await request<unknown>({ url: '/member/growths/config', method: 'PUT', data })).data,
  );
}
export async function getSignConfigs() {
  return list(
    (await request<unknown>({ url: '/member/growths/signin-configs' })).data,
    decodeSignConfig,
  );
}
/** 创建仅提交规则内容，不能由前端指定生成ID。 */
export async function createSignConfig(values: MemberSignConfigValues) {
  const data = {
    day: values.day,
    point: values.point,
    experience: values.experience,
    status: values.status,
  };
  return memberId(
    (await request<unknown>({ url: '/member/growths/signin-configs', method: 'POST', data })).data,
  );
}
/** 修改以路径ID定位，允许调整day。 */
export async function updateSignConfig(id: string, values: MemberSignConfigValues) {
  const data = {
    day: values.day,
    point: values.point,
    experience: values.experience,
    status: values.status,
  };
  return confirmed(
    (
      await request<unknown>({
        url: '/member/growths/signin-configs/' + memberId(id),
        method: 'PUT',
        data,
      })
    ).data,
  );
}
export async function deleteSignConfig(id: string) {
  return confirmed(
    (
      await request<unknown>({
        url: '/member/growths/signin-configs/' + memberId(id),
        method: 'DELETE',
      })
    ).data,
  );
}
