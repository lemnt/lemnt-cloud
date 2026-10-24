export interface Dept {
  deptId: number;
  parentId: number;
  deptName: string;
  orderNum: number;
  ancestors?: string;
  leader?: string;
  phone?: string;
  email?: string;
  status: string;
  createTime?: string;
}

export type DeptInput = Omit<Dept, 'deptId' | 'createTime'>;
export interface DeptQuery {
  deptName?: string;
  status?: string;
}
export interface DeptNode extends Dept {
  children?: DeptNode[];
}

/** 筛选结果可能缺少父节点，此时将节点作为根展示，避免查询结果丢失。 */
export function deptTree(items: Dept[]): DeptNode[] {
  // 查询结果自带 children 数组，重新构树时必须独立创建，避免污染查询缓存。
  const nodes = new Map(items.map((item) => [item.deptId, { ...item, children: [] } as DeptNode]));
  const roots: DeptNode[] = [];
  for (const node of nodes.values()) {
    const parent = nodes.get(node.parentId);
    if (parent && parent !== node) (parent.children ||= []).push(node);
    else roots.push(node);
  }
  return roots;
}
