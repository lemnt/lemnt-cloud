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
    url: '/member/admin/users/9',
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
    url: '/member/admin/users/9/points',
    method: 'PUT',
    data: { delta: -2, requestNo: 'same-key', reason: '扣减' },
  });
});
it('标签和分组按实际请求契约清空', async () => {
  mockedRequest.mockResolvedValue({ data: true });
  await api.assignMemberTags('9', []);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/member/admin/users/9/tags',
    method: 'PUT',
    data: { tagIds: [] },
  });
  await api.assignMemberGroup('9', null);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/member/admin/users/9/group',
    method: 'PUT',
    data: { groupId: null },
  });
});
it('记录查询只发送有界分页和会员筛选', async () => {
  mockedRequest.mockResolvedValue({ data: { records: [], total: 0 } });
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
