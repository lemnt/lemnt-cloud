import { beforeEach, expect, it, vi } from 'vitest';
import * as api from './memberApi';
const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => mockedRequest.mockReset());
it('后台资料只发送编辑字段，不覆盖手机号和余额', async () => {
  mockedRequest.mockResolvedValue({ data: true });
  await api.updateMember('9', {
    nickname: '昵称',
    email: '',
    avatar: '',
    birthday: '2020-01-01',
    sex: 0,
    status: 1,
    ...{ mobile: '13800000001', points: 100 },
  });
  expect(mockedRequest).toHaveBeenCalledWith({
    url: '/member-users/9',
    method: 'PUT',
    data: {
      nickname: '昵称',
      email: null,
      avatar: null,
      birthday: '2020-01-01',
      sex: 0,
      status: 1,
    },
  });
});
it('积分重试保持同一业务编号，不能发送绝对余额', async () => {
  mockedRequest.mockResolvedValue({
    data: {
      id: 1,
      memberId: 9,
      assetType: 'POINT',
      delta: -2,
      balance: 8,
      bizType: 'ADMIN',
      bizNo: 'same-key',
      reason: '扣减',
      createTime: '2026-10-25 12:00:00',
    },
  });
  const v = { delta: -2, requestNo: 'same-key', reason: '扣减', balance: 999 };
  await api.adjustMember('9', 'points', v);
  await api.adjustMember('9', 'points', v);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/member-users/9/points',
    method: 'PUT',
    data: { delta: -2, requestNo: 'same-key', reason: '扣减' },
  });
});
it('标签和分组按实际请求契约清空', async () => {
  mockedRequest.mockResolvedValue({ data: true });
  await api.assignMemberTags('9', []);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/member-users/9/tags',
    method: 'PUT',
    data: { tagIds: [] },
  });
  await api.assignMemberGroup('9', null);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/member-users/9/group',
    method: 'PUT',
    data: { groupId: null },
  });
});
it('记录查询只发送有界分页和会员筛选', async () => {
  mockedRequest.mockResolvedValue({ code: 200, rows: [], total: 0 });
  await api.getMemberRecords('signin-records', {
    memberId: '9',
    pageNum: 2,
    pageSize: 20,
    mobile: 'ignored',
  });
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/member/admin/signin-records',
    params: { memberId: '9', pageNum: 2, pageSize: 20 },
  });
});
it('元数据分类型白名单，标签不提交等级或状态字段', async () => {
  mockedRequest.mockResolvedValue({ data: 15 });
  await api.saveMetadata('tags', undefined, { name: 'VIP', remark: '备注', level: 9, status: 1 });
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/member/admin/tags',
    method: 'POST',
    data: { name: 'VIP', remark: '备注' },
  });
});
it('删除false和不安全ID均拒绝，不能误报成功', async () => {
  mockedRequest.mockResolvedValue({ data: false });
  await expect(api.deleteMetadata('levels', '1')).rejects.toThrow('未确认');
  mockedRequest.mockClear();
  await expect(api.getMember('1/../../other')).rejects.toThrow();
  expect(mockedRequest).not.toHaveBeenCalled();
});

it('后台备注保留空串清除语义，省略时不构造默认备注', async () => {
  mockedRequest.mockResolvedValue({ data: true });
  await api.updateMember('9', { nickname: '昵称', mark: '' });
  expect(mockedRequest.mock.calls[0][0].data.mark).toBe('');
  await api.updateMember('9', { nickname: '昵称' });
  expect(mockedRequest.mock.calls[1][0].data).not.toHaveProperty('mark');
});

it('姓名地区只随后台资料写入，姓名空串不转为null', async () => {
  mockedRequest.mockResolvedValue({ data: true });
  await api.updateMember('9', { nickname: '会员', name: '', areaId: 110101 });
  expect(mockedRequest.mock.calls[0][0].data).toMatchObject({ name: '', areaId: 110101 });
});

it('签到昵称按查询提交，经验流水不继承昵称条件', async () => {
  mockedRequest.mockResolvedValue({ rows: [], total: 0 });
  await api.getMemberRecords('signin-records', {
    pageNum: 1,
    pageSize: 20,
    memberId: '9',
    nickname: '  昵称  ',
  });
  expect(mockedRequest.mock.calls[0][0].params).toMatchObject({ memberId: '9', nickname: '昵称' });
  await api.getMemberRecords('experience-records', { pageNum: 1, pageSize: 20, nickname: '昵称' });
  expect(mockedRequest.mock.calls[1][0].params).not.toHaveProperty('nickname');
});

it('积分昵称和业务类型组合发送，经验流水不复用积分筛选', async () => {
  mockedRequest.mockResolvedValue({ rows: [], total: 0 });
  await api.getMemberRecords('point-records', {
    pageNum: 1,
    pageSize: 20,
    memberId: '9',
    nickname: '  会员 ',
    bizType: ' ADMIN ',
  });
  expect(mockedRequest.mock.calls[0][0].params).toMatchObject({
    memberId: '9',
    nickname: '会员',
    bizType: 'ADMIN',
  });
  await api.getMemberRecords('experience-records', {
    pageNum: 1,
    pageSize: 20,
    nickname: '会员',
    bizType: 'ADMIN',
  });
  expect(mockedRequest.mock.calls[1][0].params).not.toHaveProperty('bizType');
  expect(mockedRequest.mock.calls[1][0].params).not.toHaveProperty('nickname');
});

it('积分数值类型按数字发送，经验查询不会继承', async () => {
  mockedRequest.mockResolvedValue({ rows: [], total: 0 });
  await api.getMemberRecords('point-records', { pageNum: 1, pageSize: 20, pointBizType: 2 });
  expect(mockedRequest.mock.calls[0][0].params.pointBizType).toBe(2);
  await api.getMemberRecords('experience-records', { pageNum: 1, pageSize: 20, pointBizType: 2 });
  expect(mockedRequest.mock.calls[1][0].params).not.toHaveProperty('pointBizType');
  await expect(
    api.getMemberRecords('point-records', { pageNum: 1, pageSize: 20, pointBizType: 1.5 }),
  ).rejects.toThrow('整数');
});
