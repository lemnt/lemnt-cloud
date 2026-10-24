export interface CreateMenuRequest {
  menuName: string;
  parentId: number;
  orderNum: number;
  path?: string;
  component?: string;
  query?: string;
  routeName?: string;
  isFrame: string;
  isCache: string;
  menuType: 'M' | 'C' | 'F';
  visible: string;
  status: string;
  perms?: string;
  icon?: string;
}

export interface UpdateMenuRequest extends CreateMenuRequest {
  menuId: number;
}

export interface Menu extends UpdateMenuRequest {
  children?: Menu[];
}

export interface MenuQuery {
  menuName?: string;
  status?: string;
}

/** 筛选时父节点可能不在结果内，保留孤立节点，且不修改接口数据。 */
export function menuTree(items: Menu[]): Menu[] {
  const nodes = new Map(items.map((item) => [item.menuId, { ...item, children: undefined }]));
  const roots: Menu[] = [];
  for (const node of nodes.values()) {
    const parent: Menu | undefined = nodes.get(node.parentId);
    if (parent && parent !== node) (parent.children ||= []).push(node);
    else roots.push(node);
  }
  return roots;
}

/** 当前菜单及其后代不能成为上级，按钮不能承载子菜单。 */
export function menuParents(items: Menu[], editingId?: number): Menu[] {
  const excluded = new Set<number>(editingId === undefined ? [] : [editingId]);
  let changed = true;
  while (changed) {
    changed = false;
    for (const item of items) {
      if (excluded.has(item.parentId) && !excluded.has(item.menuId)) {
        excluded.add(item.menuId);
        changed = true;
      }
    }
  }
  return menuTree(items.filter((item) => item.menuType !== 'F' && !excluded.has(item.menuId)));
}

/** 只提交后端写入字段，避免将 children 等响应字段带入请求。 */
export function menuInput(menu: CreateMenuRequest): CreateMenuRequest {
  const {
    menuName,
    parentId,
    orderNum,
    path,
    component,
    query,
    routeName,
    isFrame,
    isCache,
    menuType,
    visible,
    status,
    perms,
    icon,
  } = menu;
  return {
    menuName,
    parentId,
    orderNum,
    path,
    component,
    query,
    routeName,
    isFrame,
    isCache,
    menuType,
    visible,
    status,
    perms,
    icon,
  };
}
