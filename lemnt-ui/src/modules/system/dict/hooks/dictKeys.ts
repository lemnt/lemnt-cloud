import type { DictDataQuery, DictTypeQuery } from '../types';
export const dictKeys = {
  all: ['systemDicts'] as const,
  options: (dictType: string) => [...dictKeys.all, 'options', dictType] as const,
  types: (params: DictTypeQuery) => [...dictKeys.all, 'types', params] as const,
  type: (id: number) => [...dictKeys.all, 'type', id] as const,
  data: (params: DictDataQuery) => [...dictKeys.all, 'data', params] as const,
  detail: (id: number) => [...dictKeys.all, 'detail', id] as const,
};
