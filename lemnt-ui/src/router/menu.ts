export interface ServerRoute {
  name?: string;
  path: string;
  component?: string;
  hidden?: boolean;
  meta?: { title?: string; icon?: string; link?: string; noCache?: boolean };
  children?: ServerRoute[];
}

export interface MenuRoute extends ServerRoute {
  children?: MenuRoute[];
}

/** 将相对路由转换为绝对地址，并移除未启用或已移除的模块。隐藏页面仍保留以支持直接访问。 */
export function normalizeMenus(routes: ServerRoute[], parent = ''): MenuRoute[] {
  return routes.flatMap((route) => {
    const path = route.path.startsWith('/')
      ? route.path
      : `${parent}/${route.path}`.replace(/\/{2,}/g, '/');
    if (/^\/(monitor|tool)(\/|$)/.test(path) || /^(monitor|tool)\//.test(route.component || ''))
      return [];
    const children = route.children ? normalizeMenus(route.children, path) : undefined;
    if (route.children?.length && !children?.length) return [];
    return [{ ...route, path, children }];
  });
}

export function findRoute(routes: MenuRoute[], path: string): MenuRoute | undefined {
  for (const route of routes) {
    if (route.path === path && !route.children?.length) return route;
    const child = findRoute(route.children || [], path);
    if (child) return child;
  }
}
