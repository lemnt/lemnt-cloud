import { request } from '@/api/request';
import type {
  CreateUserRequest,
  DepartmentOption,
  UpdateUserRequest,
  User,
  UserDetailResponse,
  UserQuery,
  UserRolesResponse,
  UserRolesRequest,
} from '../types';

/** 独立授权接口返回可分配角色及每项的已有授权标记。 */
export async function getUserRoles(userId: number): Promise<UserRolesResponse> {
  return (await request<UserRolesResponse>({ url: `/system/users/auth-role/${userId}` })).data;
}

/** 只修改角色关系，使用数字数组而非旧 Vue 的逗号分隔字符串。 */
export function updateUserRoles(data: UserRolesRequest) {
  return request<boolean>({ url: '/system/users/auth-role', method: 'PUT', data });
}

/** 用户分页返回顶层 rows/total，不是 data 数组。 */
export async function getUserList(params: UserQuery) {
  const result = await request<User[]>({ url: '/system/users', params });
  return { data: result.rows ?? [], total: result.total ?? 0, success: true };
}
/** 无 ID 时后端返回新增表单所需的岗位、角色选项。 */
export async function getUserDetail(id?: number): Promise<UserDetailResponse> {
  const url = id === undefined ? '/system/users/options' : `/system/users/${id}`;
  return (await request<UserDetailResponse>({ url })).data;
}
export async function getUserDepartments(): Promise<DepartmentOption[]> {
  return (await request<DepartmentOption[]>({ url: '/system/users/dept-tree' })).data;
}
/** 新增用户，密码只在本次请求中传输。 */
export function createUser(data: CreateUserRequest) {
  return request<boolean>({ url: '/system/users', method: 'POST', data });
}
export function updateUser(data: UpdateUserRequest) {
  return request<boolean>({ url: '/system/users', method: 'PUT', data });
}
export function deleteUser(ids: number[]) {
  return request<boolean>({ url: `/system/users/${ids.join(',')}`, method: 'DELETE' });
}
export function updateUserStatus(userId: number, status: string) {
  return request<boolean>({
    url: '/system/users/change-status',
    method: 'PUT',
    data: { userId, status },
  });
}
export function resetUserPassword(userId: number, password: string) {
  return request<boolean>({
    url: '/system/users/reset-password',
    method: 'PUT',
    data: { userId, password },
  });
}

/** 只清除登录失败次数限制，不变更用户停用状态或历史登录日志。 */
export function unlockUser(userName: string) {
  return request<boolean>({
    url: `/system/users/unlock/${encodeURIComponent(userName)}`,
    method: 'PUT',
  });
}
