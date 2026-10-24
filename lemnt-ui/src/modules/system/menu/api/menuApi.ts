import { request } from '@/api/request';
import type { CreateMenuRequest, Menu, MenuQuery, UpdateMenuRequest } from '../types';

/** 获取菜单平铺列表，树结构由页面按 parentId 组织。 */
export async function getMenuList(params: MenuQuery = {}): Promise<Menu[]> {
  return (await request<Menu[]>({ url: '/system/menus', params })).data;
}

/** 编辑前读取最新菜单详情。 */
export async function getMenuDetail(id: number): Promise<Menu> {
  return (await request<Menu>({ url: `/system/menus/${id}` })).data;
}

/** 新增目录、菜单或按钮。 */
export function createMenu(data: CreateMenuRequest) {
  return request<boolean>({ url: '/system/menus', method: 'POST', data });
}

/** 更新菜单及显示排序。 */
export function updateMenu(data: UpdateMenuRequest) {
  return request<boolean>({ url: '/system/menus', method: 'PUT', data });
}

/** 删除前由页面确认，子菜单和角色关联限制由后端校验。 */
export function deleteMenu(id: number) {
  return request<boolean>({ url: `/system/menus/${id}`, method: 'DELETE' });
}
