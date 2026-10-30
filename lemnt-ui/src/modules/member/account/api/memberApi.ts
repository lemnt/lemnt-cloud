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
  MemberProfileValues,
  MetadataKind,
  MetadataValues,
  RecordKind,
  MemberAdjustment,
  MemberPointConfig,
  MemberSignConfig,
} from '../types';
const user = (id: string) => `/member-users/${memberId(id)}`;
export async function getMembers(q: MemberPageQuery) {
  return decodePage(
    await request<unknown>({
      url: '/member-users',
      params: { mobile: q.mobile, pageNum: q.pageNum, pageSize: q.pageSize },
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
export async function getMetadata(kind: MetadataKind) {
  return list((await request<unknown>({ url: '/member/admin/' + kind })).data, (v) =>
    decodeMetadata(v, kind),
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
        url: '/member/admin/' + kind + (id ? '/' + memberId(id) : ''),
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
        url: '/member/admin/' + kind + '/' + memberId(id),
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
  return decodeConfig((await request<unknown>({ url: '/member/admin/config' })).data);
}
export async function savePointConfig(data: MemberPointConfig) {
  return confirmed(
    (await request<unknown>({ url: '/member/admin/config', method: 'PUT', data })).data,
  );
}
export async function getSignConfigs() {
  return list(
    (await request<unknown>({ url: '/member/admin/signin-configs' })).data,
    decodeSignConfig,
  );
}
export async function saveSignConfig(data: MemberSignConfig) {
  return confirmed(
    (await request<unknown>({ url: '/member/admin/signin-configs', method: 'PUT', data })).data,
  );
}
export async function deleteSignConfig(day: number) {
  return confirmed(
    (await request<unknown>({ url: '/member/admin/signin-configs/' + day, method: 'DELETE' })).data,
  );
}
