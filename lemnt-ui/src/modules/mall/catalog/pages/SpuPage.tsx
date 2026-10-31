import { useState } from 'react';
import { PageContainer, ProTable, type ProColumns } from '@ant-design/pro-components';
import { Alert, App, Button, Popconfirm, Result, Tabs } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { usePermission } from '@/shared/hooks/usePermission';
import {
  getSpuList,
  getSpuDetail,
  getSpuStatusCounts,
  getCategoryList,
  getBrandList,
  updateSpuShelf,
  recycleSpu,
  restoreSpu,
  deleteSpu,
} from '../api/catalogApi';
import { exportSpus } from '../api/spuExportApi';
import { catalogKeys } from '../hooks/catalogKeys';
import { spuStatusEnum } from '../constants';
import { requireWriteSuccess } from '../catalogValidation';
import { SpuEditor } from '../components/SpuEditor';
import { SpuDetailDrawer } from '../components/SpuDetailDrawer';
import type { Spu, SpuFilter, SpuStatus, SpuStockStatus } from '../types';

/** 按真实状态计数管理商品，回收、恢复及删除保留服务端状态约束。 */
export default function SpuPage() {
  const { can } = usePermission(),
    client = useQueryClient(),
    { message } = App.useApp();
  const [filters, setFilters] = useState<SpuFilter>({});
  const [status, setStatus] = useState<SpuStatus | SpuStockStatus>('ON_SHELF');
  const [page, setPage] = useState({ pageNum: 1, pageSize: 10 });
  const [editing, setEditing] = useState<Spu>();
  const [isOpening, setIsOpening] = useState(false);
  const [open, setOpen] = useState(false);
  const [detailId, setDetailId] = useState<string>();
  const stockStatus = status === 'SOLD_OUT' || status === 'LOW_STOCK' ? status : undefined;
  const query = {
    ...filters,
    status: stockStatus ? undefined : (status as SpuStatus),
    stockStatus,
    ...page,
  };
  const rows = useQuery({
    queryKey: catalogKeys.spus(query),
    queryFn: () => getSpuList(query),
    enabled: can('mall:spu:list'),
  });
  const counts = useQuery({
    queryKey: catalogKeys.counts(filters),
    queryFn: () => getSpuStatusCounts(filters),
    enabled: can('mall:spu:list'),
  });
  const categories = useQuery({
    queryKey: catalogKeys.categories(),
    queryFn: getCategoryList,
    enabled: can('mall:spu:list') && can('mall:category:list'),
  });
  const brands = useQuery({
    queryKey: catalogKeys.brands(),
    queryFn: getBrandList,
    enabled: can('mall:spu:list') && can('mall:brand:list'),
  });
  const exportFile = useMutation({ mutationFn: () => exportSpus(query) });
  const shelf = useMutation({
    mutationFn: ({ id, onShelf }: { id: string; onShelf: boolean }) => updateSpuShelf(id, onShelf),
  });
  const recycle = useMutation({ mutationFn: recycleSpu });
  const restore = useMutation({ mutationFn: restoreSpu });
  const remove = useMutation({ mutationFn: deleteSpu });
  const busy = shelf.isPending || recycle.isPending || restore.isPending || remove.isPending;
  async function refresh(afterWrite = false) {
    try {
      await client.invalidateQueries({ queryKey: catalogKeys.all }, { throwOnError: true });
    } catch (reason) {
      message.warning(`${afterWrite ? '操作成功，但' : ''}刷新失败：${String(reason)}`);
    }
  }
  /** 状态操作使记录移出当前分组时，最后一页仅剩一条则退回上一页。 */
  async function afterTransition() {
    if (rows.data?.data.length === 1 && page.pageNum > 1)
      setPage({ ...page, pageNum: page.pageNum - 1 });
    await refresh(true);
  }
  async function edit(id: string) {
    if (isOpening || !can('mall:spu:query') || !can('mall:spu:edit')) return;
    setIsOpening(true);
    try {
      const value = await client.fetchQuery({
        queryKey: catalogKeys.spu(id),
        queryFn: () => getSpuDetail(id),
        staleTime: 0,
      });
      if (value.status === 'RECYCLED') throw Error('商品已进入回收站，请先恢复');
      setEditing(value);
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
    { title: '简介', dataIndex: 'introduction', search: false, ellipsis: true },
    { title: '排序', dataIndex: 'sort', search: false },
    {
      title: '分类',
      dataIndex: 'categoryId',
      valueType: 'select',
      fieldProps: {
        options: categories.data?.map((v) => ({ value: v.id, label: v.name })),
        disabled: !can('mall:category:list') || !categories.isSuccess,
        showSearch: true,
        optionFilterProp: 'label',
      },
      render: (_, row) =>
        categories.data?.find((v) => v.id === row.categoryId)?.name ?? `ID: ${row.categoryId}`,
    },
    {
      title: '品牌',
      dataIndex: 'brandId',
      valueType: 'select',
      fieldProps: {
        options: brands.data?.map((v) => ({ value: v.id, label: v.name })),
        disabled: !can('mall:brand:list') || !brands.isSuccess,
        showSearch: true,
        optionFilterProp: 'label',
      },
      render: (_, row) =>
        brands.data?.find((v) => v.id === row.brandId)?.name ?? `ID: ${row.brandId}`,
    },
    { title: '状态', dataIndex: 'status', valueEnum: spuStatusEnum, search: false },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) => [
        can('mall:spu:query') && (
          <Button key="detail" type="link" onClick={() => setDetailId(row.id)}>
            详情与库存
          </Button>
        ),
        can('mall:spu:edit') && can('mall:spu:query') && row.status !== 'RECYCLED' && (
          <Button key="edit" type="link" disabled={isOpening || busy} onClick={() => edit(row.id)}>
            修改
          </Button>
        ),
        can('mall:spu:edit') && row.status !== 'RECYCLED' && (
          <Popconfirm
            key="shelf"
            title={row.status === 'ON_SHELF' ? '确认下架？已预占订单仍可继续处理。' : '确认上架？'}
            disabled={busy}
            onConfirm={async () => {
              if (busy || !can('mall:spu:edit')) return;
              try {
                requireWriteSuccess(
                  (await shelf.mutateAsync({ id: row.id, onShelf: row.status !== 'ON_SHELF' }))
                    .data,
                );
                message.success('状态已更新');
                await afterTransition();
              } catch (reason) {
                message.error(String(reason));
              }
            }}
          >
            <Button type="link" disabled={busy}>
              {row.status === 'ON_SHELF' ? '下架' : '上架'}
            </Button>
          </Popconfirm>
        ),
        can('mall:spu:edit') && row.status === 'OFF_SHELF' && (
          <Popconfirm
            key="recycle"
            title="确认移入回收站？恢复后仍需重新上架。"
            disabled={busy}
            onConfirm={async () => {
              if (busy || !can('mall:spu:edit')) return;
              try {
                await recycle.mutateAsync(row.id);
                message.success('已移入回收站');
                await afterTransition();
              } catch (reason) {
                message.error(String(reason));
              }
            }}
          >
            <Button type="link" disabled={busy}>
              移入回收站
            </Button>
          </Popconfirm>
        ),
        can('mall:spu:edit') && row.status === 'RECYCLED' && (
          <Popconfirm
            key="restore"
            title="确认恢复商品？恢复后为下架状态。"
            disabled={busy}
            onConfirm={async () => {
              if (busy || !can('mall:spu:edit')) return;
              try {
                await restore.mutateAsync(row.id);
                message.success('已恢复为下架');
                await afterTransition();
              } catch (reason) {
                message.error(String(reason));
              }
            }}
          >
            <Button type="link" disabled={busy}>
              恢复商品
            </Button>
          </Popconfirm>
        ),
        can('mall:spu:remove') && row.status === 'RECYCLED' && (
          <Popconfirm
            key="remove"
            title="确认删除商品？删除后不可恢复，历史订单和库存记录保留。"
            disabled={busy}
            onConfirm={async () => {
              if (busy || !can('mall:spu:remove')) return;
              try {
                await remove.mutateAsync(row.id);
                message.success('商品已删除');
                await afterTransition();
              } catch (reason) {
                message.error(String(reason));
              }
            }}
          >
            <Button type="link" danger disabled={busy}>
              删除商品
            </Button>
          </Popconfirm>
        ),
      ],
    },
  ];
  if (!can('mall:spu:list')) return <Result status="403" title="没有商品列表权限" />;
  return (
    <PageContainer title="商品与库存">
      {rows.isError && (
        <Alert
          type="error"
          title="商品列表读取失败"
          description={String(rows.error)}
          action={<Button onClick={() => rows.refetch()}>重试</Button>}
        />
      )}
      {counts.isError && (
        <Alert
          type="error"
          title="商品状态数量读取失败"
          action={<Button onClick={() => counts.refetch()}>重试</Button>}
        />
      )}
      {categories.isError && (
        <Alert
          type="warning"
          title="分类选项读取失败"
          action={<Button onClick={() => categories.refetch()}>重试</Button>}
        />
      )}
      {brands.isError && (
        <Alert
          type="warning"
          title="品牌选项读取失败"
          action={<Button onClick={() => brands.refetch()}>重试</Button>}
        />
      )}
      <Tabs
        activeKey={status}
        onChange={(value) => {
          if (
            value === 'ON_SHELF' ||
            value === 'OFF_SHELF' ||
            value === 'RECYCLED' ||
            value === 'SOLD_OUT' ||
            value === 'LOW_STOCK'
          ) {
            setStatus(value);
            setPage({ ...page, pageNum: 1 });
          }
        }}
        items={[
          { key: 'ON_SHELF', label: `出售中（${counts.data?.onShelf ?? '—'}）` },
          { key: 'OFF_SHELF', label: `仓库中（${counts.data?.offShelf ?? '—'}）` },
          { key: 'SOLD_OUT', label: `已售罄（${counts.data?.soldOut ?? '—'}）` },
          { key: 'LOW_STOCK', label: `警戒库存（${counts.data?.lowStock ?? '—'}）` },
          { key: 'RECYCLED', label: `回收站（${counts.data?.recycled ?? '—'}）` },
        ]}
      />
      <ProTable<Spu>
        options={{ reload: false }}
        rowKey="id"
        columns={columns}
        dataSource={rows.data?.data ?? []}
        loading={rows.isFetching}
        scroll={{ x: 1100 }}
        pagination={{
          current: page.pageNum,
          pageSize: page.pageSize,
          total: rows.data?.total ?? 0,
          pageSizeOptions: [10, 20, 50, 100],
          onChange: (pageNum, pageSize) => setPage({ pageNum, pageSize }),
        }}
        onSubmit={(params) => {
          setFilters({
            name: typeof params.name === 'string' ? params.name : undefined,
            categoryId: typeof params.categoryId === 'string' ? params.categoryId : undefined,
            brandId: typeof params.brandId === 'string' ? params.brandId : undefined,
          });
          setPage({ ...page, pageNum: 1 });
        }}
        onReset={() => {
          setFilters({});
          setPage({ ...page, pageNum: 1 });
        }}
        toolBarRender={() => [
          can('mall:spu:export') && (
            <Button
              key="export"
              loading={exportFile.isPending}
              title="按当前筛选导出，最多10000件商品"
              onClick={async () => {
                if (exportFile.isPending || !can('mall:spu:export')) return;
                try {
                  const blob = await exportFile.mutateAsync();
                  const url = URL.createObjectURL(blob);
                  const link = document.createElement('a');
                  link.href = url;
                  link.download = '商品.xlsx';
                  document.body.appendChild(link);
                  try {
                    link.click();
                    message.success('导出文件已生成');
                  } finally {
                    link.remove();
                    setTimeout(() => URL.revokeObjectURL(url), 1000);
                  }
                } catch (reason) {
                  message.error(String(reason));
                }
              }}
            >
              导出商品
            </Button>
          ),
          <Button
            key="refresh"
            onClick={() => refresh()}
            loading={rows.isFetching || counts.isFetching}
          >
            刷新
          </Button>,
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
        onSuccess={() => refresh(true)}
      />
      <SpuDetailDrawer
        key={detailId ?? 'detail'}
        id={detailId}
        onClose={() => setDetailId(undefined)}
      />
    </PageContainer>
  );
}
