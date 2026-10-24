import type { UserQuery } from '@/modules/system/user/types';
import type { RoleQuery } from '../types';
export const roleKeys = {
  all: ['systemRoles'] as const,
  list: (params: RoleQuery) => [...roleKeys.all, 'list', params] as const,
  detail: (id: number) => [...roleKeys.all, 'detail', id] as const,
  menus: (id?: number) => [...roleKeys.all, 'menus', id] as const,
  departments: (id: number) => [...roleKeys.all, 'departments', id] as const,
  users: (params: UserQuery, allocated: boolean) =>
    [...roleKeys.all, 'users', params, allocated] as const,
};
