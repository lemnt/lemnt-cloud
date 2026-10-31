import axios from 'axios';
import { beforeEach, expect, it, vi } from 'vitest';
import * as api from './memberApi';
const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => mockedRequest.mockReset());
it('后台资料允许编辑手机号，不覆盖余额', async () => {
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
      mobile: '13800000001',
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
    url: '/tags',
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

it('签到配置新增返回ID、修改按ID定位且不发送额外字段', async () => {
  const values = { day: 7, point: 10, experience: 2, status: 1, id: '999' };
  mockedRequest.mockResolvedValueOnce({ data: '9007199254740993' });
  expect(await api.createSignConfig(values)).toBe('9007199254740993');
  expect(mockedRequest.mock.calls[0][0]).toEqual({
    url: '/growths/signin-configs',
    method: 'POST',
    data: { day: 7, point: 10, experience: 2, status: 1 },
  });
  mockedRequest.mockResolvedValueOnce({ data: true });
  await api.updateSignConfig('9007199254740993', { ...values, day: 3 });
  expect(mockedRequest.mock.calls[1][0]).toEqual({
    url: '/growths/signin-configs/9007199254740993',
    method: 'PUT',
    data: { day: 3, point: 10, experience: 2, status: 1 },
  });
});

it('签到配置列表和删除使用实际growths路径及规则ID', async () => {
  mockedRequest.mockResolvedValueOnce({
    data: [{ id: '99', day: 7, point: 10, experience: 2, status: 1 }],
  });
  expect((await api.getSignConfigs())[0]).toMatchObject({ id: '99', day: 7 });
  expect(mockedRequest.mock.calls[0][0].url).toBe('/growths/signin-configs');
  mockedRequest.mockResolvedValueOnce({ data: true });
  await api.deleteSignConfig('99');
  expect(mockedRequest.mock.calls[1][0]).toEqual({
    url: '/growths/signin-configs/99',
    method: 'DELETE',
  });
});

it('积分配置真实growths路径读取，未配置不伪造服务端值', async () => {
  mockedRequest.mockResolvedValueOnce({ data: null });
  expect(await api.getPointConfig()).toBeNull();
  expect(mockedRequest.mock.calls[0][0].url).toBe('/growths/config');
  const config = {
    pointTradeDeductEnable: true,
    pointTradeDeductUnitPrice: 5,
    pointTradeDeductMaxPrice: 2,
    pointTradeGivePoint: 3,
  };
  mockedRequest.mockResolvedValueOnce({ data: config });
  expect(await api.getPointConfig()).toEqual(config);
});

it('保存积分配置只发送四项业务字段，保留false和0', async () => {
  mockedRequest.mockResolvedValue({ data: true });
  const config = {
    pointTradeDeductEnable: false,
    pointTradeDeductUnitPrice: 0,
    pointTradeDeductMaxPrice: 0,
    pointTradeGivePoint: 0,
  };
  await api.savePointConfig({ ...config, ...{ id: '77', unknown: '不得发送' } });
  expect(mockedRequest.mock.calls[0][0]).toEqual({
    url: '/growths/config',
    method: 'PUT',
    data: config,
  });
});

it('等级名称状态筛选保留0，分组标签不继承等级条件', async () => {
  mockedRequest.mockResolvedValue({ data: [] });
  await api.getMetadata('levels', { name: ' Gold ', status: 0 });
  expect(mockedRequest.mock.calls[0][0]).toEqual({
    url: '/growth/levels',
    params: { name: 'Gold', status: 0 },
  });
  await api.getMetadata('groups', { name: 'Gold', status: 0 });
  expect(mockedRequest.mock.calls[1][0]).toEqual({ url: '/groups' });
  await api.getMetadata('tags', { name: 'Gold', status: 0 });
  expect(mockedRequest.mock.calls[2][0]).toEqual({ url: '/tags' });
  await expect(api.getMetadata('levels', { status: 2 })).rejects.toThrow('状态');
});

it('等级创建修改删除使用真实路径，空图片显式发送null', async () => {
  mockedRequest.mockResolvedValueOnce({ data: '99' });
  await api.saveMetadata('levels', undefined, {
    name: 'Gold',
    level: 1,
    experience: 100,
    discountPercent: 90,
    status: 1,
  });
  expect(mockedRequest.mock.calls[0][0]).toMatchObject({
    url: '/growth/levels',
    method: 'POST',
    data: { icon: null, backgroundUrl: null },
  });
  mockedRequest.mockResolvedValueOnce({ data: '99' });
  await api.saveMetadata('levels', '99', {
    name: 'Gold',
    level: 1,
    experience: 100,
    discountPercent: 90,
    status: 0,
  });
  expect(mockedRequest.mock.calls[1][0].url).toBe('/growth/levels/99');
  mockedRequest.mockResolvedValueOnce({ data: true });
  await api.deleteMetadata('levels', '99');
  expect(mockedRequest.mock.calls[2][0]).toEqual({ url: '/growth/levels/99', method: 'DELETE' });
});

it('分组分页使用独立条件、统一分页，状态0和时间保留', async () => {
  mockedRequest.mockResolvedValue({
    rows: [{ id: '1', name: 'VIP', remark: null, status: 0, createTime: '2026-10-30 10:00:00' }],
    total: 21,
  });
  const q = {
    name: ' VIP ',
    status: 0,
    startTime: '2026-10-30 10:00:00',
    endTime: '2026-10-30 11:00:00',
    pageNum: 2,
    pageSize: 20,
  };
  const page = await api.getGroupPage({ ...q, ...{ memberId: '999' } });
  expect(page.total).toBe(21);
  expect(page.records[0].createTime).toBe('2026-10-30 10:00:00');
  expect(mockedRequest.mock.calls[0][0]).toEqual({
    url: '/groups/page',
    params: { ...q, name: 'VIP' },
  });
});

it('标签分页不借用分组状态或会员ID，保留分页total', async () => {
  mockedRequest.mockResolvedValue({
    rows: [{ id: '2', name: 'active', remark: null, createTime: null }],
    total: 1,
  });
  const q = {
    name: ' active ',
    startTime: '2026-10-30 10:00:00',
    endTime: '2026-10-30 11:00:00',
    pageNum: 1,
    pageSize: 20,
  };
  const page = await api.getTagPage({ ...q, ...{ status: 0, memberId: '999' } });
  expect(mockedRequest.mock.calls[0][0]).toEqual({
    url: '/tags/page',
    params: { ...q, name: 'active' },
  });
  expect(page.total).toBe(1);
  expect(page.records[0].createTime).toBeUndefined();
});

it('会员筛选白名单包含源条件，标签重复键序列化且大ID不丢精度', async () => {
  mockedRequest.mockResolvedValue({ rows: [], total: 0 });
  const filters = {
    mobile: ' 138 ',
    email: ' shop.test ',
    nickname: ' 会员 ',
    levelId: '9007199254740993',
    groupId: '8',
    tagIds: ['9', '10', '9'],
    loginStartTime: '2026-10-30 10:00:00',
    loginEndTime: '2026-10-30 11:00:00',
    createStartTime: '2026-10-29 00:00:00',
    createEndTime: '2026-10-30 23:59:59',
    pageNum: 2,
    pageSize: 20,
  };
  await api.getMembers({ ...filters, ...{ status: 0, pointBizType: 2 } });
  const request = mockedRequest.mock.calls[0][0];
  expect(request.params).toEqual({
    ...filters,
    mobile: '138',
    email: 'shop.test',
    nickname: '会员',
    tagIds: ['9', '10'],
  });
  const params = new URL(axios.getUri(request), 'https://test.example').searchParams;
  expect(params.getAll('tagIds')).toEqual(['9', '10']);
  expect(params.has('tagIds[]')).toBe(false);
  expect(params.get('levelId')).toBe('9007199254740993');
});

it('会员空标签不筛选，非法关联编号不发请求', async () => {
  await expect(api.getMembers({ pageNum: 1, pageSize: 20, tagIds: ['0'] })).rejects.toThrow('标识');
  expect(mockedRequest).not.toHaveBeenCalled();
  mockedRequest.mockResolvedValue({ rows: [], total: 0 });
  await api.getMembers({ pageNum: 1, pageSize: 20, tagIds: [] });
  expect(mockedRequest.mock.calls[0][0].params.tagIds).toBeUndefined();
});

it('会员详情解码关联返显，资料编辑不提交显示名称和关系编号', async () => {
  const row = {
    id: '1',
    nickname: 'member',
    status: 1,
    points: 0,
    experience: 0,
    levelId: '7',
    groupId: '8',
    tagIds: ['9'],
    tagNames: ['Active'],
    levelName: 'Gold',
    groupName: 'VIP',
  };
  mockedRequest.mockResolvedValueOnce({ data: row });
  expect(await api.getMember('1')).toMatchObject({
    tagIds: ['9'],
    tagNames: ['Active'],
    levelName: 'Gold',
    groupName: 'VIP',
  });
  mockedRequest.mockResolvedValueOnce({ data: true });
  await api.updateMember('1', row);
  const data = mockedRequest.mock.calls[1][0].data;
  expect(data).not.toHaveProperty('tagIds');
  expect(data).not.toHaveProperty('tagNames');
  expect(data).not.toHaveProperty('levelId');
  expect(data).not.toHaveProperty('groupId');
  expect(data).not.toHaveProperty('levelName');
  expect(data).not.toHaveProperty('groupName');
});

it('后台手机号按字符串发送，省略或null不提交，空串交由后端拒绝', async () => {
  mockedRequest.mockResolvedValue({ data: true });
  await api.updateMember('9', { nickname: '昵称', mobile: ' +8613800000099 ' });
  expect(mockedRequest.mock.calls[0][0].data.mobile).toBe('+8613800000099');
  await api.updateMember('9', { nickname: '昵称' });
  expect(mockedRequest.mock.calls[1][0].data).not.toHaveProperty('mobile');
  await api.updateMember('9', { nickname: '昵称', mobile: null });
  expect(mockedRequest.mock.calls[2][0].data).not.toHaveProperty('mobile');
  await api.updateMember('9', { nickname: '昵称', mobile: '' });
  expect(mockedRequest.mock.calls[3][0].data.mobile).toBe('');
});
