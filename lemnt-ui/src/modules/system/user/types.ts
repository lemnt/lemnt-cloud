import type { Role } from '@/modules/system/role/types';

export interface User {
  userId: number;
  deptId?: number;
  userName: string;
  nickName: string;
  email?: string;
  phonenumber?: string;
  sex: string;
  status: string;
  createTime?: string;
  dept?: { deptName: string };
}
export interface UserQuery {
  pageNum?: number;
  pageSize?: number;
  userName?: string;
  phonenumber?: string;
  status?: string;
  deptId?: number;
  roleId?: number;
  beginTime?: string;
  endTime?: string;
}
export interface CreateUserRequest {
  deptId?: number;
  userName: string;
  nickName: string;
  email?: string;
  phonenumber?: string;
  sex: string;
  password: string;
  status: string;
  roleIds: number[];
  postIds: number[];
}
export interface UpdateUserRequest extends Omit<CreateUserRequest, 'userName' | 'password'> {
  userId: number;
}
export interface UserFormValues extends Omit<CreateUserRequest, 'password'> {
  password?: string;
}
/** 用户详情端点不返回部门对象，不能沿用列表中的部门名称字段。 */
export interface UserDetailRecord extends Omit<User, 'dept'> {
  avatar?: string;
  loginIp?: string;
  loginDate?: string;
  createBy?: string;
}
export interface UserDetailResponse {
  detail?: UserDetailRecord;
  roleIds: number[];
  postIds: number[];
  roles: Role[];
  posts: { postId: number; postName: string; status: string }[];
}
export interface DepartmentOption {
  id: number;
  label: string;
  children?: DepartmentOption[];
}

export interface UserRolesResponse {
  user: User;
  roles: Role[];
}

export interface UserRolesRequest {
  userId: number;
  roleIds: number[];
}

/** 后端以 flag 表示已有授权，停用角色的已有授权也必须保留。 */
export function assignedRoleIds(roles: Role[]): number[] {
  return roles.filter((role) => role.flag).map((role) => role.roleId);
}

/** 编辑接口不接受账号和密码，密码只能通过独立重置接口修改。 */
export function userUpdateInput(id: number, values: UserFormValues): UpdateUserRequest {
  const { deptId, nickName, email, phonenumber, sex, status, roleIds, postIds } = values;
  return { userId: id, deptId, nickName, email, phonenumber, sex, status, roleIds, postIds };
}
