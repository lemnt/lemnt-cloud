import { expect, it } from 'vitest';
import { userUpdateInput } from './types';

it('用户更新仅提交后端允许的字段，不包含密码和账号', () => {
  const result = userUpdateInput(2, {
    userName: 'user',
    password: 'secret',
    nickName: '用户',
    sex: '2',
    status: '1',
    roleIds: [3],
    postIds: [],
    deptId: 0,
  });
  expect(result).toEqual({
    userId: 2,
    nickName: '用户',
    sex: '2',
    status: '1',
    roleIds: [3],
    postIds: [],
    deptId: 0,
    email: undefined,
    phonenumber: undefined,
  });
  expect(result).not.toHaveProperty('password');
  expect(result).not.toHaveProperty('userName');
});
