import type { DeptQuery } from '../types';

export const deptKeys = {
  all: ['departments'] as const,
  list: (filters?: DeptQuery) => [...deptKeys.all, 'list', filters] as const,
  detail: (id: number) => [...deptKeys.all, 'detail', id] as const,
  parents: (excludeId?: number) => [...deptKeys.all, 'parents', excludeId] as const,
};
