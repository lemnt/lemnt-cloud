import { expect, it } from 'vitest';
import { assignedRoleIds } from './types';
import type { Role } from '@/modules/system/role/types';

it('通过 flag 初始化授权，保留已选停用角色且不选未授权角色', () => {
  const base = {
    roleName: '角色',
    roleKey: 'role',
    roleSort: 0,
    dataScope: '1',
  };
  const roles: Role[] = [
    { ...base, roleId: 2, status: '1', flag: true },
    { ...base, roleId: 3, status: '2', flag: true },
    { ...base, roleId: 4, status: '1', flag: false },
  ];
  expect(assignedRoleIds(roles)).toEqual([2, 3]);
  expect(assignedRoleIds([])).toEqual([]);
});
