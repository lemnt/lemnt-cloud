import { describe, expect, it } from 'vitest';
import { menuInput, menuParents, menuTree } from './types';
import type { Menu } from './types';

const root: Menu = {
  menuId: 1,
  parentId: 0,
  menuName: '系统管理',
  orderNum: 0,
  menuType: 'M',
  isFrame: '1',
  isCache: '0',
  visible: '0',
  status: '1',
};
const child: Menu = { ...root, menuId: 2, parentId: 1, menuType: 'C' };
const button: Menu = { ...root, menuId: 3, parentId: 2, menuType: 'F' };

describe('菜单树与表单参数', () => {
  it('构建菜单树且不修改原始列表', () => {
    const items = [root, child, button];
    expect(menuTree(items)[0].children?.[0].children?.[0].menuId).toBe(3);
    expect(root.children).toBeUndefined();
    expect(child.children).toBeUndefined();
  });

  it('筛选后缺失父节点的菜单仍展示', () => {
    expect(menuTree([child])[0].menuId).toBe(2);
  });

  it('排除当前节点、后代和按钮，且不依赖输入顺序', () => {
    const descendant = { ...child, menuId: 4, parentId: 2 };
    expect(menuParents([descendant, button, child, root], 2)).toEqual([
      { ...root, children: undefined },
    ]);
    expect(menuParents([root, child, button])[0].children?.[0].children).toBeUndefined();
  });

  it('请求不包含响应字段且保留合法的零值', () => {
    const input = menuInput({ ...root, children: [child] } as Menu);
    expect(input).not.toHaveProperty('menuId');
    expect(input).not.toHaveProperty('children');
    expect(input.parentId).toBe(0);
    expect(input.orderNum).toBe(0);
  });
});
