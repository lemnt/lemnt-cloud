import type { MenuRoute } from '@/router/menu';

export interface HomeShortcut {
  path: string;
  title: string;
}

/** 首页快捷入口来自当前会话菜单，仅包含已迁移的可见站内页面。 */
export function homeShortcuts(menus: MenuRoute[], registeredComponents: string[]): HomeShortcut[] {
  const registered = new Set(registeredComponents);
  const seen = new Set<string>();
  const shortcuts: HomeShortcut[] = [];
  function visit(items: MenuRoute[]) {
    for (const item of items) {
      if (item.hidden) continue;
      if (item.children?.length) {
        visit(item.children);
        continue;
      }
      if (
        !item.component ||
        !registered.has(item.component) ||
        item.meta?.link ||
        !item.path.startsWith('/') ||
        item.path.startsWith('//') ||
        seen.has(item.path)
      )
        continue;
      seen.add(item.path);
      shortcuts.push({ path: item.path, title: item.meta?.title || item.name || item.path });
    }
  }
  visit(menus);
  return shortcuts;
}
