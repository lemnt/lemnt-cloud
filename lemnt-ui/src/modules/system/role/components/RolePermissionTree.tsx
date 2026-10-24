import { useState } from 'react';
import { ApartmentOutlined, SearchOutlined } from '@ant-design/icons';
import { Button, Empty, Input, Space, Tree, Typography, theme } from 'antd';
import { permissionNodeIds, permissionTreeSelection, togglePermissionNode } from '../types';
import type { CSSProperties } from 'react';
import type { DataNode } from 'antd/es/tree';
import type { PermissionNode } from '../types';
import styles from './rolePermissionTree.module.css';

interface RolePermissionTreeProps {
  nodes: PermissionNode[];
  value?: number[];
  disabled?: boolean;
  onChange?: (value: number[]) => void;
}

/** 始终父子联动，受控回显半选节点，避免历史父级授权隐式勾选所有后代。 */
export function RolePermissionTree({
  nodes,
  value = [],
  disabled = false,
  onChange,
}: RolePermissionTreeProps) {
  const [expanded, setExpanded] = useState<number[]>(() => nodes.map((node) => node.id));
  const [search, setSearch] = useState('');
  const { token } = theme.useToken();
  const allKeys = permissionNodeIds(nodes);
  const selection = permissionTreeSelection(value, nodes);
  const selectedCount = selection.checked.length;
  const keyword = search.trim().toLocaleLowerCase();
  let matches = 0;
  function toTreeData(items: PermissionNode[]): DataNode[] {
    return items.map((item) => {
      const index = keyword ? item.label.toLocaleLowerCase().indexOf(keyword) : -1;
      if (index >= 0) matches++;
      const hasChildren = Boolean(item.children?.length);
      return {
        key: item.id,
        icon: hasChildren ? <ApartmentOutlined /> : undefined,
        title: (
          <span className={styles.nodeTitle}>
            <span className={hasChildren ? styles.branchLabel : styles.leafLabel}>
              {index < 0 ? (
                item.label
              ) : (
                <>
                  {item.label.slice(0, index)}
                  <mark>{item.label.slice(index, index + keyword.length)}</mark>
                  {item.label.slice(index + keyword.length)}
                </>
              )}
            </span>
            {hasChildren && <span className={styles.childCount}>{item.children?.length}</span>}
          </span>
        ),
        children: hasChildren ? toTreeData(item.children ?? []) : undefined,
      };
    });
  }
  const treeData = toTreeData(nodes);
  function handleSearch(next: string) {
    setSearch(next);
    const text = next.trim().toLocaleLowerCase();
    if (!text) {
      setExpanded(nodes.map((node) => node.id));
      return;
    }
    const ancestors = new Set<number>();
    function visit(items: PermissionNode[], parents: number[]) {
      for (const item of items) {
        if (item.label.toLocaleLowerCase().includes(text)) {
          parents.forEach((id) => ancestors.add(id));
          if (item.children?.length) ancestors.add(item.id);
        }
        visit(item.children ?? [], [...parents, item.id]);
      }
    }
    visit(nodes, []);
    setExpanded([...ancestors]);
  }
  const panelStyle = {
    '--permission-border': token.colorBorderSecondary,
    '--permission-surface': token.colorBgContainer,
    '--permission-toolbar': token.colorFillAlter,
    '--permission-text-secondary': token.colorTextSecondary,
    '--permission-primary': token.colorPrimary,
    '--permission-highlight': token.colorPrimaryBg,
  } as CSSProperties;
  return (
    <div className={styles.panel} style={panelStyle}>
      <div className={styles.toolbar}>
        <Input
          className={styles.search}
          prefix={<SearchOutlined />}
          placeholder="搜索节点名称"
          aria-label="搜索权限节点"
          allowClear
          value={search}
          disabled={disabled}
          onChange={(event) => handleSearch(event.target.value)}
        />
        <Space size={4} wrap>
          <Button
            size="small"
            type="text"
            disabled={disabled || !allKeys.length}
            onClick={() => setExpanded(allKeys)}
          >
            展开全部
          </Button>
          <Button
            size="small"
            type="text"
            disabled={disabled || !allKeys.length}
            onClick={() => setExpanded([])}
          >
            收起全部
          </Button>
          <Button
            size="small"
            type="text"
            disabled={disabled || !allKeys.length}
            onClick={() => onChange?.(allKeys)}
          >
            全选
          </Button>
          <Button
            size="small"
            type="text"
            disabled={disabled || !value.length}
            onClick={() => onChange?.([])}
          >
            清空
          </Button>
        </Space>
      </div>
      <div className={styles.treeBody}>
        {nodes.length ? (
          <Tree
            checkable
            blockNode
            showIcon
            selectable={false}
            showLine={{ showLeafIcon: false }}
            disabled={disabled}
            checkStrictly
            expandedKeys={expanded}
            onExpand={(keys) => setExpanded(keys.map(Number))}
            treeData={treeData}
            checkedKeys={selection}
            onCheck={(_, info) =>
              onChange?.(togglePermissionNode(value, nodes, Number(info.node.key), info.checked))
            }
          />
        ) : (
          <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无权限节点" />
        )}
      </div>
      <div className={styles.footer}>
        <Typography.Text type="secondary" aria-live="polite">
          已选 <strong>{selectedCount}</strong> / {allKeys.length} 项
          {keyword && ` · 匹配 ${matches} 项`}
        </Typography.Text>
        <Typography.Text type="secondary">勾选父节点将联动子节点</Typography.Text>
      </div>
    </div>
  );
}
