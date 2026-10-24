import { request } from '@/api/request';
import type { UserQuery, User } from '@/modules/system/user/types';
import type {
  CreateRoleRequest,
  PermissionNode,
  Role,
  RoleDepartmentResponse,
  RoleMenuResponse,
  RoleQuery,
  RoleScopeRequest,
  UpdateRoleRequest,
} from '../types';

export async function getRoleList(params: RoleQuery) {
  const result = await request<Role[]>({ url: '/system/roles', params });
  return { data: result.rows ?? [], total: result.total ?? 0, success: true };
}
export async function getRoleDetail(id: number): Promise<Role> {
  return (await request<Role>({ url: `/system/roles/${id}` })).data;
}
/** 角色编辑从菜单授权接口独立读取选中 ID。 */
export async function getRoleMenus(id?: number): Promise<RoleMenuResponse> {
  if (id !== undefined)
    return (await request<RoleMenuResponse>({ url: `/system/menus/role-menu-tree-select/${id}` }))
      .data;
  return {
    checkedKeys: [],
    menus: (await request<PermissionNode[]>({ url: '/system/menus/tree-select' })).data,
  };
}
export async function getRoleDepartments(id: number): Promise<RoleDepartmentResponse> {
  return (await request<RoleDepartmentResponse>({ url: `/system/roles/dept-tree/${id}` })).data;
}
export function createRole(data: CreateRoleRequest) {
  return request<boolean>({ url: '/system/roles', method: 'POST', data });
}
export function updateRole(data: UpdateRoleRequest) {
  return request<boolean>({ url: '/system/roles', method: 'PUT', data });
}
export function deleteRole(ids: number[]) {
  return request<boolean>({ url: `/system/roles/${ids.join(',')}`, method: 'DELETE' });
}
export function updateRoleStatus(roleId: number, status: string) {
  return request<boolean>({
    url: '/system/roles/change-status',
    method: 'PUT',
    data: { roleId, status },
  });
}
export function updateRoleScope(data: RoleScopeRequest) {
  return request<boolean>({ url: '/system/roles/data-scope', method: 'PUT', data });
}
/** 已分配和待分配列表共享后端用户分页契约。 */
export async function getRoleUsers(params: UserQuery, allocated: boolean) {
  const result = await request<User[]>({
    url: `/system/roles/auth-user/${allocated ? 'allocated-list' : 'unallocated-list'}`,
    params,
  });
  return { data: result.rows ?? [], total: result.total ?? 0, success: true };
}
export function updateRoleUsers(roleId: number, userIds: number[], allocate: boolean) {
  return request<boolean>({
    url: `/system/roles/auth-user/${allocate ? 'select-all' : 'cancel-all'}`,
    method: 'PUT',
    data: { roleId, userIds },
  });
}
