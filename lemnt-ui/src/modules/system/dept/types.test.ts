import { expect, it } from 'vitest';
import { deptTree, type Dept } from './types';

it('组装树时保留筛选后失去父节点的部门，并且不修改接口记录', () => {
  const input: Dept[] = [
    { deptId: 1, parentId: 0, deptName: '总公司', orderNum: 0, status: '1' },
    { deptId: 2, parentId: 1, deptName: '财务', orderNum: 0, status: '1' },
    { deptId: 4, parentId: 3, deptName: '筛选命中', orderNum: 0, status: '1' },
  ];
  const result = deptTree(input);
  expect(result.map((item) => item.deptId)).toEqual([1, 4]);
  expect(result[0].children?.[0].deptId).toBe(2);
  expect(input[0]).not.toHaveProperty('children');
});
