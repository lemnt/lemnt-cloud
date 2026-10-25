import type { SpuQuery } from '../types';
export const catalogKeys = {
  all: ['mall-catalog'] as const,
  categories: () => [...catalogKeys.all, 'categories'] as const,
  category: (id: string) => [...catalogKeys.all, 'category', id] as const,
  brands: () => [...catalogKeys.all, 'brands'] as const,
  brand: (id: string) => [...catalogKeys.all, 'brand', id] as const,
  spus: (params: SpuQuery) => [...catalogKeys.all, 'spus', params] as const,
  spu: (id: string) => [...catalogKeys.all, 'spu', id] as const,
};
