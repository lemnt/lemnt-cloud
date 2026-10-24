export interface Role {
  roleId: number;
  roleName: string;
  roleKey: string;
  roleSort: number;
  dataScope: string;
  status: string;
  flag?: boolean;
}
export interface RoleQuery {
  pageNum?: number;
  pageSize?: number;
  roleName?: string;
  roleKey?: string;
  status?: string;
  beginTime?: string;
  endTime?: string;
}
export interface CreateRoleRequest {
  roleName: string;
  roleKey: string;
  roleSort: number;
  dataScope: string;
  status: string;
  menuIds: number[];
}
export interface UpdateRoleRequest extends CreateRoleRequest {
  roleId: number;
}
export interface PermissionNode {
  id: number;
  label: string;
  children?: PermissionNode[];
}
export interface RoleMenuResponse {
  checkedKeys: number[];
  menus: PermissionNode[];
}
export interface RoleDepartmentResponse {
  checkedKeys: number[];
  depts: PermissionNode[];
}
export interface RoleScopeRequest {
  roleId: number;
  dataScope: string;
  deptIds: number[];
}

/** 联动时补齐半选父节点；独立选择时保留精确授权集合，避免扩大授权。 */
export function permissionIds(checked: number[], nodes: PermissionNode[], linked = true): number[] {
  const ids = new Set(checked);
  if (!linked) return [...ids];
  function visit(items: PermissionNode[]): boolean {
    let hasSelected = false;
    for (const item of items) {
      const childSelected = visit(item.children ?? []);
      if (childSelected) ids.add(item.id);
      if (ids.has(item.id)) hasSelected = true;
    }
    return hasSelected;
  }
  visit(nodes);
  return [...ids];
}

export function permissionNodeIds(nodes: PermissionNode[]): number[] {
  return nodes.flatMap((node) => [node.id, ...permissionNodeIds(node.children ?? [])]);
}

/** 独立授权时仅补充半选展示，不将祖先加入实际授权集合。 */
export function permissionHalfCheckedIds(checked: number[], nodes: PermissionNode[]): number[] {
  const selected = new Set(checked);
  const halfChecked: number[] = [];
  function visit(node: PermissionNode): boolean {
    const childrenSelected = (node.children ?? []).map(visit).some(Boolean);
    if (childrenSelected && !selected.has(node.id)) halfChecked.push(node.id);
    return selected.has(node.id) || childrenSelected;
  }
  nodes.forEach(visit);
  return halfChecked;
}

/** 历史授权中的父级 ID 可能只代表目录访问，回显时不据此授予整个子树。 */
export function permissionTreeSelection(
  value: number[],
  nodes: PermissionNode[],
): {
  checked: number[];
  halfChecked: number[];
} {
  const selected = new Set(value);
  const checked: number[] = [];
  const halfChecked: number[] = [];
  function visit(node: PermissionNode): { full: boolean; any: boolean } {
    const children = (node.children ?? []).map(visit);
    const full = children.length ? children.every((child) => child.full) : selected.has(node.id);
    const any = full || selected.has(node.id) || children.some((child) => child.any);
    if (full) checked.push(node.id);
    else if (any) halfChecked.push(node.id);
    return { full, any };
  }
  nodes.forEach(visit);
  return { checked, halfChecked };
}

/** 点击父级时勾选或取消整个子树，沿路径更新祖先，不触及其他分支。 */
export function togglePermissionNode(
  value: number[],
  nodes: PermissionNode[],
  id: number,
  isChecked: boolean,
): number[] {
  const selected = new Set(value);
  function visit(items: PermissionNode[], ancestors: PermissionNode[]): boolean {
    for (const node of items) {
      if (node.id === id) {
        for (const childId of permissionNodeIds([node])) {
          if (isChecked) selected.add(childId);
          else selected.delete(childId);
        }
        for (const parent of [...ancestors].reverse()) {
          const hasSelected = permissionNodeIds(parent.children ?? []).some((childId) =>
            selected.has(childId),
          );
          if (hasSelected) selected.add(parent.id);
          else selected.delete(parent.id);
        }
        return true;
      }
      if (visit(node.children ?? [], [...ancestors, node])) return true;
    }
    return false;
  }
  visit(nodes, []);
  return [...selected];
}
