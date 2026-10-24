import { beforeEach, expect, it, vi } from 'vitest';
import {
  deleteUser,
  getUserDetail,
  getUserList,
  resetUserPassword,
  unlockUser,
  getUserRoles,
  updateUserRoles,
} from './userApi';

const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => {
  mockedRequest.mockReset();
});

it('用户详情保留真实登录和审计字段及已分配角色岗位 ID', async () => {
  const data = {
    detail: {
      userId: 2,
      userName: 'test',
      loginIp: '127.0.0.1',
      loginDate: '2026-09-30T10:00:00',
      createBy: 'admin',
      avatar: '/avatar.png',
    },
    roleIds: [3],
    postIds: [4],
    roles: [
      { roleId: 3, roleName: '已分配' },
      { roleId: 5, roleName: '未分配' },
    ],
    posts: [{ postId: 4, postName: '岗位' }],
  };
  mockedRequest.mockResolvedValue({ code: 200, data });
  expect(await getUserDetail(2)).toEqual(data);
  expect(mockedRequest).toHaveBeenCalledWith({ url: '/system/users/2' });
});

it('用户详情加载失败传递错误，不回退为列表资料', async () => {
  mockedRequest.mockRejectedValue(new Error('用户不存在'));
  await expect(getUserDetail(2)).rejects.toThrow('用户不存在');
});

it('独立授权读取嵌套用户和角色 flag，不调用用户编辑详情', async () => {
  const data = { user: { userId: 2 }, roles: [{ roleId: 3, flag: true }] };
  mockedRequest.mockResolvedValue({ code: 200, data });
  expect(await getUserRoles(2)).toEqual(data);
  expect(mockedRequest).toHaveBeenCalledWith({ url: '/system/users/auth-role/2' });
});

it('独立授权 PUT 只提交用户 ID 和数字数组，支持清空角色', async () => {
  mockedRequest.mockResolvedValue({ code: 200, data: true });
  await updateUserRoles({ userId: 2, roleIds: [3, 4] });
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/system/users/auth-role',
    method: 'PUT',
    data: { userId: 2, roleIds: [3, 4] },
  });
  await updateUserRoles({ userId: 2, roleIds: [] });
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/system/users/auth-role',
    method: 'PUT',
    data: { userId: 2, roleIds: [] },
  });
});

it('授权失败向页面传递错误，不伪装成功', async () => {
  mockedRequest.mockRejectedValue(new Error('没有授权权限'));
  await expect(updateUserRoles({ userId: 2, roleIds: [3] })).rejects.toThrow('没有授权权限');
});

it('解锁通过用户接口 PUT 提交账号，路径特殊字符正确编码', async () => {
  mockedRequest.mockResolvedValue({ code: 200, data: true });
  await unlockUser('用户+test@example.com');
  expect(mockedRequest).toHaveBeenCalledWith({
    url: `/system/users/unlock/${encodeURIComponent('用户+test@example.com')}`,
    method: 'PUT',
  });
});
it('解锁失败保持拒绝状态，不伪装成功', async () => {
  mockedRequest.mockRejectedValue(new Error('没有解锁权限'));
  await expect(unlockUser('test')).rejects.toThrow('没有解锁权限');
});

it('用户分页读取 rows/total 并保留分页参数', async () => {
  mockedRequest.mockResolvedValue({ code: 200, data: undefined, rows: [{ userId: 2 }], total: 12 });
  expect(await getUserList({ pageNum: 2, pageSize: 10 })).toEqual({
    data: [{ userId: 2 }],
    total: 12,
    success: true,
  });
  expect(mockedRequest).toHaveBeenCalledWith({
    url: '/system/users',
    params: { pageNum: 2, pageSize: 10 },
  });
});
it('新增和编辑详情分别读取不同路径下的嵌套 data', async () => {
  const data = { detail: { userId: 2 }, roles: [], posts: [], roleIds: [], postIds: [] };
  mockedRequest.mockResolvedValue({ code: 200, data });
  expect(await getUserDetail(2)).toEqual(data);
  expect(mockedRequest).toHaveBeenLastCalledWith({ url: '/system/users/2' });
  await getUserDetail();
  expect(mockedRequest).toHaveBeenLastCalledWith({ url: '/system/users/options' });
});
it('批量删除使用路径 ID，重置密码使用独立请求体', async () => {
  mockedRequest.mockResolvedValue({ code: 200, data: true });
  await deleteUser([2, 3]);
  expect(mockedRequest).toHaveBeenLastCalledWith({ url: '/system/users/2,3', method: 'DELETE' });
  await resetUserPassword(2, 'test-password');
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/system/users/reset-password',
    method: 'PUT',
    data: { userId: 2, password: 'test-password' },
  });
});
