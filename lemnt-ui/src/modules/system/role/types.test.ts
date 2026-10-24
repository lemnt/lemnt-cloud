import { expect, it } from 'vitest';
import {
  permissionHalfCheckedIds,
  permissionIds,
  permissionNodeIds,
  permissionTreeSelection,
  togglePermissionNode,
} from './types';

it('授权补齐祖先并去重，但不授权同级或未选后代', () => {
  const nodes = [
    {
      id: 1,
      label: '目录',
      children: [
        { id: 2, label: '菜单', children: [{ id: 3, label: '按钮' }] },
        { id: 4, label: '其他菜单' },
      ],
    },
  ];
  expect(permissionIds([3, 3], nodes)).toEqual([3, 2, 1]);
  expect(permissionIds([1], nodes)).toEqual([1]);
  expect(permissionIds([], nodes)).toEqual([]);
});

it('关闭父子联动后只保存明确勾选的权限，不补齐祖先', () => {
  const nodes = [{ id: 1, label: '目录', children: [{ id: 2, label: '菜单' }] }];
  expect(permissionIds([2, 2], nodes, false)).toEqual([2]);
  expect(permissionIds([1], nodes, false)).toEqual([1]);
});

it('部门父子联动补齐半选祖先，不授予未选择的同级部门', () => {
  const depts = [
    {
      id: 1,
      label: '总部',
      children: [
        { id: 2, label: '部门 A' },
        { id: 3, label: '部门 B' },
      ],
    },
  ];
  expect(permissionIds([2], depts, true)).toEqual([2, 1]);
  expect(permissionIds([2], depts, false)).toEqual([2]);
});

it('全选和展开遍历所有层级，包括独立的根节点', () => {
  expect(
    permissionNodeIds([
      { id: 1, label: '目录', children: [{ id: 2, label: '菜单' }] },
      { id: 3, label: '独立节点' },
    ]),
  ).toEqual([1, 2, 3]);
});

it('独立授权回显半选祖先，不扩大保存的权限范围', () => {
  const nodes = [
    {
      id: 1,
      label: '目录',
      children: [
        {
          id: 2,
          label: '菜单',
          children: [
            { id: 3, label: '查询' },
            { id: 4, label: '修改' },
          ],
        },
        { id: 5, label: '其他菜单' },
      ],
    },
  ];
  const checked = [3];
  expect(permissionHalfCheckedIds(checked, nodes)).toEqual([2, 1]);
  expect(permissionIds(checked, nodes, false)).toEqual([3]);
  expect(checked).toEqual([3]);
  expect(permissionHalfCheckedIds([1, 3], nodes)).toEqual([2]);
  expect(permissionHalfCheckedIds([], nodes)).toEqual([]);
});

it('半选展示遍历所有分支，明确选中的父节点保持勾选', () => {
  const nodes = [
    {
      id: 1,
      label: '总部',
      children: [
        { id: 2, label: '部门 A', children: [{ id: 3, label: '小组 A' }] },
        { id: 4, label: '部门 B', children: [{ id: 5, label: '小组 B' }] },
      ],
    },
  ];
  expect(permissionHalfCheckedIds([3, 5], nodes)).toEqual([2, 4, 1]);
  expect(permissionHalfCheckedIds([1], nodes)).toEqual([]);
  expect(permissionHalfCheckedIds([1, 2, 3, 4, 5], nodes)).toEqual([]);
});

it('父级勾选联动全部后代，取消父级清空整个分支并保留其他分支', () => {
  const nodes = [
    {
      id: 1,
      label: '总部',
      children: [
        {
          id: 2,
          label: '部门 A',
          children: [
            { id: 3, label: '小组 A' },
            { id: 4, label: '小组 B' },
          ],
        },
        { id: 5, label: '部门 B' },
      ],
    },
  ];
  const selected = togglePermissionNode([], nodes, 2, true);
  expect(new Set(selected)).toEqual(new Set([1, 2, 3, 4]));
  expect(permissionTreeSelection(selected, nodes)).toEqual({
    checked: [3, 4, 2],
    halfChecked: [1],
  });
  const withOther = togglePermissionNode(selected, nodes, 5, true);
  expect(permissionTreeSelection(withOther, nodes).halfChecked).toEqual([]);
  expect(new Set(togglePermissionNode(withOther, nodes, 2, false))).toEqual(new Set([1, 5]));
  expect(togglePermissionNode(withOther, nodes, 1, false)).toEqual([]);
});

it('取消单个子节点后父级恢复半选，取消最后一个子节点清除祖先', () => {
  const nodes = [
    {
      id: 1,
      label: '菜单',
      children: [
        { id: 2, label: '查询' },
        { id: 3, label: '修改' },
      ],
    },
  ];
  const partial = togglePermissionNode([1, 2, 3], nodes, 3, false);
  expect(permissionTreeSelection(partial, nodes)).toEqual({ checked: [2], halfChecked: [1] });
  expect(togglePermissionNode(partial, nodes, 2, false)).toEqual([]);
});

it('历史父级和部分子级授权回显为半选，不自动授予兄弟节点', () => {
  const nodes = [
    {
      id: 1,
      label: '菜单',
      children: [
        { id: 2, label: '查询' },
        { id: 3, label: '修改' },
      ],
    },
  ];
  const legacy = [1, 2];
  expect(permissionTreeSelection(legacy, nodes)).toEqual({ checked: [2], halfChecked: [1] });
  expect(permissionIds(legacy, nodes)).toEqual([1, 2]);
  expect(permissionTreeSelection([1], nodes)).toEqual({ checked: [], halfChecked: [1] });
  expect(new Set(togglePermissionNode(legacy, nodes, 1, true))).toEqual(new Set([1, 2, 3]));
});
