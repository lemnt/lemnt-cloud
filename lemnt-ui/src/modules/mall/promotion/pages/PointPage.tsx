import { useState } from 'react';
import {
  PageContainer,
  ProTable,
  ProFormText,
  ProFormTextArea,
  ProFormSelect,
  ProFormDigit,
  ProFormList,
  type ProColumns,
} from '@ant-design/pro-components';
import { Alert, App, Button, Descriptions, Form, Modal, Popconfirm, Result, Table } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { AdminModalForm } from '@/shared/components/AdminModalForm';
import { usePermission } from '@/shared/hooks/usePermission';
import { getSpuDetail, getSpuList } from '../../catalog/api/catalogApi';
import * as api from '../api/pointApi';
import type { PointActivity, PointActivityFilter, PointActivitySave } from '../types';

/** 积分商城活动管理。 */
export function PointActivityPage() {
  const { can } = usePermission();
  const { message } = App.useApp();
  const cache = useQueryClient();
  const [filters, setFilters] = useState<Pick<PointActivityFilter, 'status'>>({});
  const [page, setPage] = useState({ pageNum: 1, pageSize: 20 });
  const [editor, setEditor] = useState<{ id: string | null; readOnly: boolean }>();
  const rows = useQuery({
    queryKey: ['point', 'activities', filters, page],
    queryFn: () => api.getPointActivityPage({ ...filters, ...page }),
    enabled: can('mall:promotion:list'),
  });
  const detail = useQuery<PointActivity>({
    queryKey: ['point', 'activity', editor?.id],
    queryFn: () => api.getPointActivity(editor?.id ?? ''),
    enabled: !!editor?.id && can('mall:promotion:query'),
  });
  const save = useMutation({
    mutationFn: async (v: PointActivitySave) => {
      const payload = api.pointPayload(v);
      if (editor?.id) await api.updatePointActivity(editor.id, payload);
      else await api.createPointActivity(payload);
    },
  });
  const change = useMutation({
    mutationFn: ({ id, remove }: { id: string; remove: boolean }) =>
      remove ? api.deletePointActivity(id) : api.closePointActivity(id),
  });
  async function refresh() {
    try {
      await cache.invalidateQueries({ queryKey: ['point'] }, { throwOnError: true });
    } catch {
      message.warning('操作成功，刷新失败，请重新加载');
    }
  }
  async function mutate(id: string, remove: boolean) {
    try {
      await change.mutateAsync({ id, remove });
      message.success(remove ? '已删除' : '已关闭');
      await refresh();
    } catch (error) {
      message.error(String(error));
    }
  }
  const columns: ProColumns<PointActivity>[] = [
    { title: '活动编号', dataIndex: 'id', search: false },
    { title: '商品', dataIndex: 'spuName', search: false },
    { title: '所需积分', dataIndex: 'point', search: false },
    { title: '补差金额', dataIndex: 'price', search: false },
    {
      title: '库存',
      dataIndex: 'stock',
      search: false,
      render: (_, row) => `${row.stock}/${row.totalStock}`,
    },
    { title: '排序', dataIndex: 'sort', search: false },
    {
      title: '状态',
      dataIndex: 'status',
      valueType: 'select',
      valueEnum: { 0: { text: '开启' }, 1: { text: '关闭' } },
    },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) => [
        can('mall:promotion:query') && (
          <Button
            key="detail"
            type="link"
            onClick={() => setEditor({ id: row.id, readOnly: true })}
          >
            详情
          </Button>
        ),
        row.status === 0 && can('mall:promotion:update') && (
          <Button key="edit" type="link" onClick={() => setEditor({ id: row.id, readOnly: false })}>
            编辑
          </Button>
        ),
        row.status === 0 && can('mall:promotion:update') && (
          <Popconfirm
            key="close"
            title="关闭后不能重新开启，确认关闭？"
            onConfirm={() => mutate(row.id, false)}
          >
            <Button type="link" disabled={change.isPending}>
              关闭
            </Button>
          </Popconfirm>
        ),
        row.status === 1 && can('mall:promotion:delete') && (
          <Popconfirm
            key="delete"
            title="确认删除已关闭活动？"
            onConfirm={() => mutate(row.id, true)}
          >
            <Button type="link" danger disabled={change.isPending}>
              删除
            </Button>
          </Popconfirm>
        ),
      ],
    },
  ];
  if (!can('mall:promotion:list')) return <Result status="403" title="没有积分商城活动列表权限" />;
  const loaded = editor?.id ? detail.data : undefined;
  const editable = !editor?.readOnly && (!loaded || loaded.status === 0);
  const initial: PointActivitySave = loaded
    ? {
        spuId: loaded.spuId,
        remark: loaded.remark,
        sort: loaded.sort,
        products: loaded.products.map((row) => ({
          skuId: row.skuId,
          exchangeCount: row.exchangeCount,
          point: row.point,
          price: row.price,
          stock: row.stock,
        })),
      }
    : {
        spuId: '',
        remark: null,
        sort: 0,
        products: [{ skuId: '', exchangeCount: 1, point: 0, price: '0', stock: 1 }],
      };
  return (
    <PageContainer title="积分商城">
      {rows.isError && (
        <Alert
          type="error"
          title="活动读取失败"
          description={String(rows.error)}
          action={<Button onClick={() => rows.refetch()}>重试</Button>}
        />
      )}
      <ProTable<PointActivity>
        options={{ reload: false }}
        rowKey="id"
        scroll={{ x: 1000 }}
        columns={columns}
        dataSource={rows.data?.records ?? []}
        loading={rows.isFetching}
        onSubmit={(v) => {
          setFilters({
            status:
              v.status === 0 || v.status === '0'
                ? 0
                : v.status === 1 || v.status === '1'
                  ? 1
                  : undefined,
          });
          setPage({ ...page, pageNum: 1 });
        }}
        onReset={() => {
          setFilters({});
          setPage({ ...page, pageNum: 1 });
        }}
        pagination={{
          current: page.pageNum,
          pageSize: page.pageSize,
          total: rows.data?.total ?? 0,
          onChange: (pageNum, pageSize) => setPage({ pageNum, pageSize }),
        }}
        toolBarRender={() => [
          <Button key="refresh" onClick={() => rows.refetch()} loading={rows.isFetching}>
            刷新
          </Button>,
          can('mall:promotion:create') && (
            <Button
              key="new"
              type="primary"
              onClick={() => setEditor({ id: null, readOnly: false })}
            >
              新增积分活动
            </Button>
          ),
        ]}
      />
      {editor?.id && (detail.isFetching || detail.isError) && (
        <Modal open title="积分商城活动" footer={null} onCancel={() => setEditor(undefined)}>
          {detail.isError ? (
            <Alert
              type="error"
              title="详情读取失败"
              description={String(detail.error)}
              action={<Button onClick={() => detail.refetch()}>重试</Button>}
            />
          ) : (
            '正在读取详情…'
          )}
        </Modal>
      )}
      {editor && (!editor.id || (loaded && !detail.isFetching && !detail.isError)) && (
        <AdminModalForm<PointActivitySave>
          key={`${editor.id ?? 'new'}-${detail.dataUpdatedAt}`}
          open
          title={`${editable ? (editor.id ? '编辑' : '新增') : '查看'}积分活动`}
          width={900}
          initialValues={initial}
          modalProps={{ destroyOnHidden: true, maskClosable: false, closable: !save.isPending }}
          onOpenChange={(open) => {
            if (!open && !save.isPending) setEditor(undefined);
          }}
          submitter={editable ? { submitButtonProps: { loading: save.isPending } } : false}
          onFinish={async (values) => {
            if (!can(editor.id ? 'mall:promotion:update' : 'mall:promotion:create')) {
              message.error('没有积分商城活动保存权限');
              return false;
            }
            try {
              await save.mutateAsync(values);
              message.success('已保存');
              setEditor(undefined);
              await refresh();
              return true;
            } catch (error) {
              message.error(String(error));
              return false;
            }
          }}
        >
          {!editable && loaded ? (
            <PointDetails value={loaded} />
          ) : (
            <>
              <PointProductFields />
              <ProFormTextArea name="remark" label="备注" />
              <ProFormDigit
                name="sort"
                label="排序"
                min={0}
                fieldProps={{ precision: 0 }}
                rules={[{ required: true }]}
              />
              <ProFormList
                name="products"
                label="积分商品"
                min={1}
                copyIconProps={false}
                creatorButtonProps={{ creatorButtonText: '添加商品SKU' }}
              >
                {(field) => <PointProductLineFields key={field.key} />}
              </ProFormList>
            </>
          )}
        </AdminModalForm>
      )}
    </PageContainer>
  );
}

function PointProductFields() {
  const { can } = usePermission();
  return (
    <ProFormSelect
      name="spuId"
      label="商品"
      showSearch
      debounceTime={300}
      disabled={!can('mall:spu:list')}
      request={async ({ keyWords }) =>
        can('mall:spu:list')
          ? (await getSpuList({ name: keyWords, pageNum: 1, pageSize: 100 })).data.map((row) => ({
              label: row.name,
              value: row.id,
            }))
          : []
      }
      rules={[{ required: true }]}
    />
  );
}

function PointProductLineFields() {
  const { can } = usePermission();
  const form = Form.useFormInstance<PointActivitySave>();
  const spuId = Form.useWatch('spuId', form);
  const skus = useQuery({
    queryKey: ['point', 'spu', spuId],
    queryFn: () => getSpuDetail(String(spuId)),
    enabled: !!spuId && can('mall:spu:query'),
  });
  return (
    <>
      {!can('mall:spu:query') && <Alert type="warning" title="读取商品SKU需要商品详情权限" />}
      <ProFormSelect
        name="skuId"
        label="SKU"
        disabled={!spuId || !can('mall:spu:query') || skus.isFetching || skus.isError}
        options={
          skus.data?.skus.map((row) => ({
            label: `${row.name}（${row.code}，${row.price}元）`,
            value: row.id,
          })) ?? []
        }
        rules={[{ required: true }]}
      />
      <ProFormDigit
        name="exchangeCount"
        label="可兑换次数"
        min={1}
        fieldProps={{ precision: 0 }}
        rules={[{ required: true }]}
      />
      <ProFormDigit
        name="point"
        label="所需积分"
        min={0}
        fieldProps={{ precision: 0 }}
        rules={[{ required: true }]}
      />
      <ProFormText name="price" label="补差金额（元，可为0）" rules={[{ required: true }]} />
      <ProFormDigit
        name="stock"
        label="库存"
        min={0}
        fieldProps={{ precision: 0 }}
        rules={[{ required: true }]}
      />
    </>
  );
}

function PointDetails({ value }: { value: PointActivity }) {
  return (
    <>
      <Descriptions
        column={1}
        items={[
          { key: 'spu', label: '商品', children: value.spuName ?? value.spuId },
          { key: 'status', label: '状态', children: value.status === 0 ? '开启' : '关闭' },
          { key: 'point', label: '最低积分', children: value.point ?? '—' },
          { key: 'price', label: '最低补差', children: value.price ?? '—' },
          { key: 'stock', label: '库存', children: `${value.stock}/${value.totalStock}` },
          { key: 'sort', label: '排序', children: value.sort },
          { key: 'remark', label: '备注', children: value.remark || '—' },
        ]}
      />
      <Table
        rowKey="skuId"
        pagination={false}
        dataSource={value.products}
        columns={[
          { title: 'SKU编号', dataIndex: 'skuId' },
          { title: '可兑换次数', dataIndex: 'exchangeCount' },
          { title: '所需积分', dataIndex: 'point' },
          { title: '补差金额', dataIndex: 'price' },
          { title: '库存', dataIndex: 'stock' },
        ]}
      />
    </>
  );
}
