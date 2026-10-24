import { request } from '@/api/request';
import type { Dept, DeptInput, DeptQuery } from '../types';

export async function listDepartments(filters?: DeptQuery) {
  return (await request<Dept[]>({ url: '/system/departments', params: filters })).data;
}

export async function getDepartment(id: number) {
  return (await request<Dept>({ url: `/system/departments/${id}` })).data;
}

export async function parentDepartments(excludeId?: number) {
  return (
    await request<Dept[]>({
      url: excludeId ? `/system/departments/exclude/${excludeId}` : '/system/departments',
    })
  ).data;
}

export function saveDepartment(values: DeptInput, editing?: Dept) {
  return request({
    url: '/system/departments',
    method: editing ? 'PUT' : 'POST',
    data: {
      ...values,
      ...(editing ? { deptId: editing.deptId, ancestors: editing.ancestors } : {}),
    },
  });
}

export function deleteDepartment(id: number) {
  return request({ url: `/system/departments/${id}`, method: 'DELETE' });
}

export function saveDepartmentOrder(values: Record<string, number>) {
  // 后端接收两组一一对应的数组，不沿用旧页面的逗号字符串。
  const entries = Object.entries(values);
  return request({
    url: '/system/departments/update-sort',
    method: 'PUT',
    data: {
      deptIds: entries.map(([id]) => Number(id)),
      orderNums: entries.map(([, order]) => order),
    },
  });
}
