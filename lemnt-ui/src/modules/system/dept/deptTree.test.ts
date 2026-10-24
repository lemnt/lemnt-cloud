import { expect, it } from 'vitest';
import { deptTree, type DeptNode } from './types';

it('重复打开部门表单不修改查询缓存或累加子节点', () => {
  const rows: DeptNode[] = [
    { deptId: 100, parentId: 0, deptName: '根', orderNum: 0, status: '1', children: [] },
    { deptId: 101, parentId: 100, deptName: '子', orderNum: 1, status: '1', children: [] },
    { deptId: 102, parentId: 101, deptName: '孙', orderNum: 2, status: '1', children: [] },
  ];
  const original = structuredClone(rows);
  const first = deptTree(rows);
  const second = deptTree(rows);
  expect(rows).toEqual(original);
  expect(second).toEqual(first);
  expect(second[0].children?.map((node) => node.deptId)).toEqual([101]);
  expect(second[0].children?.[0].children?.map((node) => node.deptId)).toEqual([102]);
});

it('筛选缺少父节点时保留匹配节点', () => {
  const rows = [{ deptId: 101, parentId: 100, deptName: '子', orderNum: 1, status: '1' }];
  expect(deptTree(rows).map((node) => node.deptId)).toEqual([101]);
});
