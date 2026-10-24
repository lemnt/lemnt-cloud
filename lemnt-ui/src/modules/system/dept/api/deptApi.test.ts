import { beforeEach, expect, it, vi } from 'vitest';
import {
  deleteDepartment,
  getDepartment,
  parentDepartments,
  saveDepartment,
  saveDepartmentOrder,
} from './deptApi';
import type { Dept, DeptInput } from '../types';

const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => {
  mockedRequest.mockReset();
});

it('详情和排除本部门的父级选项使用独立路径', async () => {
  mockedRequest.mockResolvedValue({ data: [] });
  await getDepartment(2);
  expect(mockedRequest).toHaveBeenLastCalledWith({ url: '/system/departments/2' });
  await parentDepartments(2);
  expect(mockedRequest).toHaveBeenLastCalledWith({ url: '/system/departments/exclude/2' });
  await parentDepartments();
  expect(mockedRequest).toHaveBeenLastCalledWith({ url: '/system/departments' });
});

it('新增和修改分别使用 POST 和 PUT，修改保留 ID 和祖先路径', async () => {
  const values: DeptInput = { parentId: 1, deptName: '部门', orderNum: 2, status: '1' };
  await saveDepartment(values);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/system/departments',
    method: 'POST',
    data: values,
  });
  const editing: Dept = { ...values, deptId: 2, ancestors: '0,1' };
  await saveDepartment(values, editing);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/system/departments',
    method: 'PUT',
    data: { ...values, deptId: 2, ancestors: '0,1' },
  });
});

it('删除使用部门 ID 路径，不误删父部门', async () => {
  await deleteDepartment(2);
  expect(mockedRequest).toHaveBeenCalledWith({ url: '/system/departments/2', method: 'DELETE' });
});

it('排序以对应的数字 ID 和顺序数组提交', async () => {
  await saveDepartmentOrder({ '3': 10, '2': 20 });
  expect(mockedRequest).toHaveBeenCalledWith({
    url: '/system/departments/update-sort',
    method: 'PUT',
    data: { deptIds: [2, 3], orderNums: [20, 10] },
  });
});

it('保存失败向表单传递错误，允许页面保留输入重试', async () => {
  mockedRequest.mockRejectedValue(new Error('部门名称重复'));
  await expect(
    saveDepartment({ parentId: 1, deptName: '部门', orderNum: 0, status: '1' }),
  ).rejects.toThrow('部门名称重复');
});

it('删除失败不伪装成功', async () => {
  mockedRequest.mockRejectedValue(new Error('部门下仍有用户'));
  await expect(deleteDepartment(2)).rejects.toThrow('部门下仍有用户');
});
