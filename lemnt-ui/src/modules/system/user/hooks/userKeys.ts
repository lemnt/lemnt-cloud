import type { UserQuery } from '../types';
export const userKeys = {
  all: ['systemUsers'] as const,
  list: (params: UserQuery) => [...userKeys.all, 'list', params] as const,
  detail: (id?: number) => [...userKeys.all, 'detail', id] as const,
  roles: (id: number) => [...userKeys.all, 'roles', id] as const,
  departments: ['systemUsers', 'departments'] as const,
};
