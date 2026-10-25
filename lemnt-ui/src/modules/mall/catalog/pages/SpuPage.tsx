import { useRef, useState } from 'react';
import {
  PageContainer,
  ProTable,
  type ActionType,
  type ProColumns,
} from '@ant-design/pro-components';
import { App, Button, Popconfirm, Result } from 'antd';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { usePermission } from '@/shared/hooks/usePermission';
import { getSpuList, getSpuDetail, updateSpuShelf } from '../api/catalogApi';
import { catalogKeys } from '../hooks/catalogKeys';
import { shelfStatusEnum } from '../constants';
import { requireWriteSuccess } from '../catalogValidation';
import { SpuEditor } from '../components/SpuEditor';
import { SpuDetailDrawer } from '../components/SpuDetailDrawer';
import type { Spu, SpuQuery } from '../types';

export default function SpuPage() {
  const { can } = usePermission();
  const client = useQueryClient();
  const { message } = App.useApp();
  const table = useRef<ActionType | undefined>(undefined);
  const [editing, setEditing] = useState<Spu>();
  const [isOpening, setIsOpening] = useState(false);
  const [open, setOpen] = useState(false);
  const [detailId, setDetailId] = useState<string>();
  const shelf = useMutation({
    mutationFn: ({ id, onShelf }: { id: string; onShelf: boolean }) => updateSpuShelf(id, onShelf),
  });
  async function refresh() {
    try {
      await client.invalidateQueries({ queryKey: catalogKeys.all });
      await table.current?.reload();
    } catch (reason) {
      message.warning(`操作成功，但列表刷新失败：${String(reason)}`);
    }
  }
  async function edit(id: string) {
    if (isOpening) return;
    setIsOpening(true);
    try {
      setEditing(
        await client.fetchQuery({ queryKey: catalogKeys.spu(id), queryFn: () => getSpuDetail(id) }),
      );
      setOpen(true);
    } catch (reason) {
      message.error(String(reason));
    } finally {
      setIsOpening(false);
    }
  }
  const columns: ProColumns<Spu>[] = [
    { title: '编号', dataIndex: 'id', search: false },
    { title: '编码', dataIndex: 'code', search: false },
    { title: '商品名称', dataIndex: 'name' },
    { title: '分类编号', dataIndex: 'categoryId', search: false },
    { title: '品牌编号', dataIndex: 'brandId', search: false },
    { title: '状态', dataIndex: 'status', valueEnum: shelfStatusEnum },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) => [
        can('mall:spu:query') && (
          <Button key="detail" type="link" onClick={() => setDetailId(row.id)}>
            详情与库存
          </Button>
        ),
        can('mall:spu:edit') && can('mall:spu:query') && (
          <Button key="edit" type="link" disabled={isOpening} onClick={() => edit(row.id)}>
            修改
          </Button>
        ),
        can('mall:spu:edit') && (
          <Popconfirm
            key="shelf"
            title={row.status === 'ON_SHELF' ? '确认下架？已预占订单仍可继续处理。' : '确认上架？'}
            disabled={shelf.isPending}
            onConfirm={async () => {
              try {
                requireWriteSuccess(
                  (await shelf.mutateAsync({ id: row.id, onShelf: row.status !== 'ON_SHELF' }))
                    .data,
                );
                message.success('状态已更新');
                await refresh();
              } catch (reason) {
                message.error(String(reason));
              }
            }}
          >
            <Button type="link" disabled={shelf.isPending}>
              {row.status === 'ON_SHELF' ? '下架' : '上架'}
            </Button>
          </Popconfirm>
        ),
      ],
    },
  ];
  if (!can('mall:spu:list')) return <Result status="403" title="没有商品列表权限" />;
  return (
    <PageContainer title="商品与库存">
      <ProTable<Spu>
        rowKey="id"
        actionRef={table}
        columns={columns}
        pagination={{ defaultPageSize: 10, pageSizeOptions: [10, 20, 50, 100] }}
        request={async (params) => {
          const query: SpuQuery = {
            name: params.name,
            status: params.status,
            pageNum: params.current,
            pageSize: params.pageSize,
          };
          return client.fetchQuery({
            queryKey: catalogKeys.spus(query),
            queryFn: () => getSpuList(query),
          });
        }}
        onRequestError={(reason) => message.error(reason.message)}
        toolBarRender={() => [
          can('mall:spu:add') && (
            <Button
              key="add"
              type="primary"
              onClick={() => {
                setEditing(undefined);
                setOpen(true);
              }}
            >
              新增商品
            </Button>
          ),
        ]}
      />
      <SpuEditor
        key={`${editing?.id ?? 'new'}-${open}`}
        value={editing}
        open={open}
        onOpenChange={setOpen}
        onSuccess={refresh}
      />
      <SpuDetailDrawer
        key={detailId ?? 'detail'}
        id={detailId}
        onClose={() => setDetailId(undefined)}
      />
    </PageContainer>
  );
}
