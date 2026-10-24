import { beforeEach, expect, it, vi } from 'vitest';
import { getRoleMenus, getRoleUsers, updateRoleScope, updateRoleUsers } from './roleApi';

const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => {
  mockedRequest.mockReset();
});
it('编辑角色从菜单授权接口读取选中 ID', async () => {
  const data = { menus: [], checkedKeys: [2, 3] };
  mockedRequest.mockResolvedValue({ code: 200, data });
  expect(await getRoleMenus(4)).toEqual(data);
  expect(mockedRequest).toHaveBeenCalledWith({ url: '/system/menus/role-menu-tree-select/4' });
});
it('角色用户列表按已分配状态选择接口', async () => {
  mockedRequest.mockResolvedValue({ code: 200, data: undefined, rows: [], total: 0 });
  await getRoleUsers({ roleId: 4, pageNum: 1 }, false);
  expect(mockedRequest).toHaveBeenCalledWith({
    url: '/system/roles/auth-user/unallocated-list',
    params: { roleId: 4, pageNum: 1 },
  });
});
it('授权和取消授权均提交数字数组', async () => {
  mockedRequest.mockResolvedValue({ code: 200, data: true });
  await updateRoleUsers(4, [2, 3], true);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/system/roles/auth-user/select-all',
    method: 'PUT',
    data: { roleId: 4, userIds: [2, 3] },
  });
  await updateRoleUsers(4, [2], false);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/system/roles/auth-user/cancel-all',
    method: 'PUT',
    data: { roleId: 4, userIds: [2] },
  });
});
it('自定义数据权限保留部门 ID 数组', async () => {
  mockedRequest.mockResolvedValue({ code: 200, data: true });
  await updateRoleScope({ roleId: 4, dataScope: '2', deptIds: [100] });
  expect(mockedRequest).toHaveBeenCalledWith({
    url: '/system/roles/data-scope',
    method: 'PUT',
    data: { roleId: 4, dataScope: '2', deptIds: [100] },
  });
});

it('部门联动固定开启后仅提交数据范围和授权 ID', async () => {
  mockedRequest.mockResolvedValue({ code: 200, data: true });
  const data = { roleId: 4, dataScope: '2', deptIds: [100, 101] };
  await updateRoleScope(data);
  expect(mockedRequest).toHaveBeenCalledWith({
    url: '/system/roles/data-scope',
    method: 'PUT',
    data,
  });
});
