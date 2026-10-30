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

it('经验筛选完整发送，类型0有效且不污染积分查询', async () => {
  mockedRequest.mockResolvedValue({ rows: [], total: 0 });
  const filters = {
    pageNum: 2,
    pageSize: 1,
    experienceBizType: 0,
    bizNo: ' E1 ',
    title: ' 管理员调整 ',
    startTime: '2026-10-30 00:00:00',
    endTime: '2026-10-30 23:59:59',
  };
  await api.getMemberRecords('experience-records', filters);
  expect(mockedRequest.mock.calls[0][0].params).toMatchObject({
    ...filters,
    bizNo: 'E1',
    title: '管理员调整',
  });
  await api.getMemberRecords('point-records', filters);
  expect(mockedRequest.mock.calls[1][0].params).not.toHaveProperty('experienceBizType');
  expect(mockedRequest.mock.calls[1][0].params).not.toHaveProperty('startTime');
  expect(mockedRequest.mock.calls[1][0].params).not.toHaveProperty('bizNo');
});

it('经验类型拒绝非整数且保持未筛选时不默认类型0', async () => {
  await expect(
    api.getMemberRecords('experience-records', {
      pageNum: 1,
      pageSize: 20,
      experienceBizType: 0.5,
    }),
  ).rejects.toThrow('整数');
  expect(mockedRequest).not.toHaveBeenCalled();
  mockedRequest.mockResolvedValue({ rows: [], total: 0 });
  await api.getMemberRecords('experience-records', { pageNum: 1, pageSize: 20 });
  expect(mockedRequest.mock.calls[0][0].params.experienceBizType).toBeUndefined();
});

it('等级筛选保留大编号字符串与时间，隔离于经验和积分', async () => {
  mockedRequest.mockResolvedValue({ rows: [], total: 0 });
  const filters = {
    pageNum: 2,
    pageSize: 1,
    memberId: '9',
    levelId: '9007199254740993',
    startTime: '2026-10-30 10:00:00',
    endTime: '2026-10-30 11:00:00',
  };
  await api.getMemberRecords('level-records', filters);
  expect(mockedRequest.mock.calls[0][0].params).toMatchObject(filters);
  await api.getMemberRecords('experience-records', filters);
  expect(mockedRequest.mock.calls[1][0].params).not.toHaveProperty('levelId');
  await api.getMemberRecords('point-records', filters);
  expect(mockedRequest.mock.calls[2][0].params).not.toHaveProperty('levelId');
  expect(mockedRequest.mock.calls[2][0].params).not.toHaveProperty('startTime');
});

it('等级筛选拒绝非正编号，未填写时不默认一个等级', async () => {
  await expect(
    api.getMemberRecords('level-records', { pageNum: 1, pageSize: 20, levelId: '0' }),
  ).rejects.toThrow('标识');
  expect(mockedRequest).not.toHaveBeenCalled();
  mockedRequest.mockResolvedValue({ rows: [], total: 0 });
  await api.getMemberRecords('level-records', { pageNum: 1, pageSize: 20 });
  expect(mockedRequest.mock.calls[0][0].params.levelId).toBeUndefined();
});

it('积分标题按包含匹配传参，空白不筛选且等级不接收标题', async () => {
  mockedRequest.mockResolvedValue({ rows: [], total: 0 });
  await api.getMemberRecords('point-records', { pageNum: 1, pageSize: 20, title: ' 管理员 ' });
  expect(mockedRequest.mock.calls[0][0].params.title).toBe('管理员');
  await api.getMemberRecords('point-records', { pageNum: 1, pageSize: 20, title: '  ' });
  expect(mockedRequest.mock.calls[1][0].params).not.toHaveProperty('title');
  await api.getMemberRecords('level-records', { pageNum: 1, pageSize: 20, title: '管理员' });
  expect(mockedRequest.mock.calls[2][0].params).not.toHaveProperty('title');
});

it('签到天数及真实时间参数不传给积分和等级', async () => {
  mockedRequest.mockResolvedValue({ rows: [], total: 0 });
  const filters = {
    pageNum: 2,
    pageSize: 1,
    day: 3,
    nickname: ' 会员 ',
    startTime: '2026-10-29 10:00:00',
    endTime: '2026-10-30 11:00:00',
  };
  await api.getMemberRecords('signin-records', filters);
  expect(mockedRequest.mock.calls[0][0].params).toMatchObject({ ...filters, nickname: '会员' });
  await api.getMemberRecords('point-records', filters);
  expect(mockedRequest.mock.calls[1][0].params).not.toHaveProperty('day');
  expect(mockedRequest.mock.calls[1][0].params).not.toHaveProperty('startTime');
  await api.getMemberRecords('level-records', filters);
  expect(mockedRequest.mock.calls[2][0].params).not.toHaveProperty('day');
  await expect(
    api.getMemberRecords('signin-records', { pageNum: 1, pageSize: 20, day: 0 }),
  ).rejects.toThrow('正整数');
});
