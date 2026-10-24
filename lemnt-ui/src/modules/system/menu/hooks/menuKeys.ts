import type { MenuQuery } from '../types';

export const menuKeys = {
  all: ['systemMenus'] as const,
  list: (params: MenuQuery = {}) => [...menuKeys.all, 'list', params] as const,
  detail: (id: number) => [...menuKeys.all, 'detail', id] as const,
};
