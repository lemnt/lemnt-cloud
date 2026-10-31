import { useState } from 'react';
import {
  PageContainer,
  ProTable,
  ProFormText,
  ProFormSelect,
  ProFormDigit,
  ProFormDateTimePicker,
  type ProColumns,
} from '@ant-design/pro-components';
import { Alert, App, Button, Descriptions, Form, Modal, Popconfirm, Result, Table } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { AdminModalForm } from '@/shared/components/AdminModalForm';
import { usePermission } from '@/shared/hooks/usePermission';
import { getSpuDetail, getSpuList } from '../../catalog/api/catalogApi';
import * as api from '../api/bargainApi';
import type {
  BargainActivity,
  BargainActivityFilter,
  BargainActivitySave,
  BargainHelp,
  BargainRecord,
} from '../types';

/** 砍价活动管理。 */
export function BargainActivityPage() {
  const { can } = usePermission();
  const { message } = App.useApp();
  const cache = useQueryClient();
  const [filters, setFilters] = useState<Pick<BargainActivityFilter, 'name' | 'status'>>({});
  const [page, setPage] = useState({ pageNum: 1, pageSize: 20 });
  const [editor, setEditor] = useState<{ id: string | null; readOnly: boolean }>();
  const rows = useQuery({
    queryKey: ['bargain', 'activities', filters, page],
    queryFn: () => api.getBargainActivityPage({ ...filters, ...page }),
    enabled: can('mall:promotion:list'),
  });
  const detail = useQuery<BargainActivity>({
    queryKey: ['bargain', 'activity', editor?.id],
    queryFn: () => api.getBargainActivity(editor?.id ?? ''),
    enabled: !!editor?.id && can('mall:promotion:query'),
  });
  const save = useMutation({
    mutationFn: async (v: BargainActivitySave) => {
      const payload = api.bargainPayload(v);
      if (editor?.id) await api.updateBargainActivity(editor.id, payload);
      else await api.createBargainActivity(payload);
    },
  });
  const change = useMutation({
    mutationFn: ({ id, remove }: { id: string; remove: boolean }) =>
      remove ? api.deleteBargainActivity(id) : api.closeBargainActivity(id),
  });
  async function refresh() {
    try {
      await cache.invalidateQueries({ queryKey: ['bargain'] }, { throwOnError: true });
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
  const columns: ProColumns<BargainActivity>[] = [
    { title: '活动编号', dataIndex: 'id', search: false },
    { title: '活动名称', dataIndex: 'name' },
    { title: '商品', dataIndex: 'spuName', search: false },
    { title: '起始价', dataIndex: 'bargainFirstPrice', search: false },
    { title: '底价', dataIndex: 'bargainMinPrice', search: false },
    {
      title: '库存',
      dataIndex: 'stock',
      search: false,
      render: (_, row) => `${row.stock}/${row.totalStock}`,
    },
    { title: '成功人数', dataIndex: 'successCount', search: false },
    {
      title: '状态',
      dataIndex: 'status',
      valueType: 'select',
      valueEnum: { 0: { text: '开启' }, 1: { text: '关闭' } },
    },
    {
      title: '活动时间',
      dataIndex: 'startTime',
      search: false,
      render: (_, row) => `${row.startTime} 至 ${row.endTime}`,
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
  if (!can('mall:promotion:list')) return <Result status="403" title="没有砍价活动列表权限" />;
  const loaded = editor?.id ? detail.data : undefined;
  const editable = !editor?.readOnly && (!loaded || loaded.status === 0);
  const initial: BargainActivitySave = loaded
    ? {
        name: loaded.name,
        startTime: loaded.startTime,
        endTime: loaded.endTime,
        spuId: loaded.spuId,
        skuId: loaded.skuId,
        bargainFirstPrice: loaded.bargainFirstPrice,
        bargainMinPrice: loaded.bargainMinPrice,
        stock: loaded.stock,
        helpMaxCount: loaded.helpMaxCount,
        bargainCount: loaded.bargainCount,
        totalLimitCount: loaded.totalLimitCount,
        randomMinPrice: loaded.randomMinPrice,
        randomMaxPrice: loaded.randomMaxPrice,
      }
    : {
        name: '',
        startTime: '',
        endTime: '',
        spuId: '',
        skuId: '',
        bargainFirstPrice: '',
        bargainMinPrice: '',
        stock: 1,
        helpMaxCount: 3,
        bargainCount: 1,
        totalLimitCount: 1,
        randomMinPrice: '',
        randomMaxPrice: '',
      };
  return (
    <PageContainer title="砍价活动">
      {rows.isError && (
        <Alert
          type="error"
          title="活动读取失败"
          description={String(rows.error)}
          action={<Button onClick={() => rows.refetch()}>重试</Button>}
        />
      )}
      <ProTable<BargainActivity>
        options={{ reload: false }}
        rowKey="id"
        scroll={{ x: 1200 }}
        columns={columns}
        dataSource={rows.data?.records ?? []}
        loading={rows.isFetching}
        onSubmit={(v) => {
          setFilters({
            name: typeof v.name === 'string' ? v.name : undefined,
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
              新增砍价活动
            </Button>
          ),
        ]}
      />
      {editor?.id && (detail.isFetching || detail.isError) && (
        <Modal open title="砍价活动" footer={null} onCancel={() => setEditor(undefined)}>
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
        <AdminModalForm<BargainActivitySave>
          key={`${editor.id ?? 'new'}-${detail.dataUpdatedAt}`}
          open
          title={`${editable ? (editor.id ? '编辑' : '新增') : '查看'}砍价活动`}
          width={900}
          dateFormatter="string"
          initialValues={initial}
          modalProps={{ destroyOnHidden: true, maskClosable: false, closable: !save.isPending }}
          onOpenChange={(open) => {
            if (!open && !save.isPending) setEditor(undefined);
          }}
          submitter={editable ? { submitButtonProps: { loading: save.isPending } } : false}
          onFinish={async (values) => {
            if (!can(editor.id ? 'mall:promotion:update' : 'mall:promotion:create')) {
              message.error('没有砍价活动保存权限');
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
            <BargainDetails value={loaded} />
          ) : (
            <>
              <ProFormText
                name="name"
                label="活动名称"
                rules={[{ required: true }, { max: 100 }]}
              />
              <BargainProductFields />
              <ProFormDateTimePicker
                name="startTime"
                label="开始时间"
                rules={[{ required: true }]}
              />
              <ProFormDateTimePicker name="endTime" label="结束时间" rules={[{ required: true }]} />
              <ProFormText
                name="bargainFirstPrice"
                label="砍价起始价（元）"
                rules={[{ required: true }]}
              />
              <ProFormText
                name="bargainMinPrice"
                label="砍价底价（元）"
                rules={[{ required: true }]}
              />
              <ProFormDigit
                name="stock"
                label="砍价库存"
                min={1}
                fieldProps={{ precision: 0 }}
                rules={[{ required: true }]}
              />
              <ProFormDigit
                name="helpMaxCount"
                label="成功所需助力人数"
                min={1}
                fieldProps={{ precision: 0 }}
                rules={[{ required: true }]}
              />
              <ProFormDigit
                name="bargainCount"
                label="每人可帮砍次数"
                min={1}
                fieldProps={{ precision: 0 }}
                rules={[{ required: true }]}
              />
              <ProFormDigit
                name="totalLimitCount"
                label="总限购数量"
                min={1}
                fieldProps={{ precision: 0 }}
                rules={[{ required: true }]}
              />
              <ProFormText
                name="randomMinPrice"
                label="单次最小砍价（元）"
                rules={[{ required: true }]}
              />
              <ProFormText
                name="randomMaxPrice"
                label="单次最大砍价（元）"
                rules={[{ required: true }]}
              />
            </>
          )}
        </AdminModalForm>
      )}
    </PageContainer>
  );
}

function BargainProductFields() {
  const { can } = usePermission();
  const form = Form.useFormInstance<BargainActivitySave>();
  const spuId = Form.useWatch('spuId', form);
  const skus = useQuery({
    queryKey: ['bargain', 'spu', spuId],
    queryFn: () => getSpuDetail(String(spuId)),
    enabled: !!spuId && can('mall:spu:query'),
  });
  return (
    <>
      {!can('mall:spu:query') && <Alert type="warning" title="读取商品SKU需要商品详情权限" />}
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
        fieldProps={{ onChange: () => form.setFieldValue('skuId', undefined) }}
        rules={[{ required: true }]}
      />
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
    </>
  );
}

function BargainDetails({ value }: { value: BargainActivity }) {
  return (
    <Descriptions
      column={1}
      items={[
        { key: 'name', label: '名称', children: value.name },
        { key: 'spu', label: '商品', children: value.spuName ?? value.spuId },
        { key: 'status', label: '状态', children: value.status === 0 ? '开启' : '关闭' },
        { key: 'time', label: '活动时间', children: `${value.startTime} 至 ${value.endTime}` },
        {
          key: 'price',
          label: '价格',
          children: `${value.bargainFirstPrice} 元起，底价 ${value.bargainMinPrice} 元`,
        },
        {
          key: 'random',
          label: '单次砍价',
          children: `${value.randomMinPrice} - ${value.randomMaxPrice} 元`,
        },
        { key: 'stock', label: '库存', children: `${value.stock}/${value.totalStock}` },
        {
          key: 'help',
          label: '助力规则',
          children: `${value.helpMaxCount} 人成功，每人可帮砍 ${value.bargainCount} 次`,
        },
        { key: 'success', label: '成功人数', children: value.successCount },
      ]}
    />
  );
}

/** 砍价记录与助力只读查询。 */
export function BargainRecordPage() {
  const { can } = usePermission();
  const [filters, setFilters] = useState<{
    userId?: string;
    activityId?: string;
    status?: 1 | 2 | 3;
  }>({});
  const [page, setPage] = useState({ pageNum: 1, pageSize: 20 });
  const [helpFor, setHelpFor] = useState<BargainRecord>();
  const rows = useQuery({
    queryKey: ['bargain', 'records', filters, page],
    queryFn: () => api.getBargainRecordPage({ ...filters, ...page }),
    enabled: can('mall:promotion:list'),
  });
  const helps = useQuery<{ records: BargainHelp[]; total: number }>({
    queryKey: ['bargain', 'helps', helpFor?.id],
    queryFn: () => api.getBargainHelpPage({ recordId: helpFor?.id, pageNum: 1, pageSize: 50 }),
    enabled: !!helpFor?.id && can('mall:promotion:query'),
  });
  const columns: ProColumns<BargainRecord>[] = [
    { title: '记录编号', dataIndex: 'id', search: false },
    { title: '活动编号', dataIndex: 'activityId', search: true },
    { title: '商品', dataIndex: 'spuName', search: false },
    { title: '会员编号', dataIndex: 'userId', search: true },
    { title: '起始价', dataIndex: 'bargainFirstPrice', search: false },
    { title: '当前价', dataIndex: 'bargainPrice', search: false },
    {
      title: '状态',
      dataIndex: 'status',
      valueType: 'select',
      valueEnum: { 1: { text: '砍价中' }, 2: { text: '成功' }, 3: { text: '失败' } },
    },
    { title: '订单编号', dataIndex: 'orderId', search: false },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) => [
        can('mall:promotion:query') && (
          <Button key="helps" type="link" onClick={() => setHelpFor(row)}>
            助力记录
          </Button>
        ),
      ],
    },
  ];
  if (!can('mall:promotion:list')) return <Result status="403" title="没有砍价记录列表权限" />;
  return (
    <PageContainer title="砍价记录">
      {rows.isError && (
        <Alert
          type="error"
          title="记录读取失败"
          description={String(rows.error)}
          action={<Button onClick={() => rows.refetch()}>重试</Button>}
        />
      )}
      <ProTable<BargainRecord>
        options={{ reload: false }}
        rowKey="id"
        scroll={{ x: 1100 }}
        columns={columns}
        dataSource={rows.data?.records ?? []}
        loading={rows.isFetching}
        onSubmit={(v) => {
          setFilters({
            userId: typeof v.userId === 'string' ? v.userId : undefined,
            activityId: typeof v.activityId === 'string' ? v.activityId : undefined,
            status:
              v.status === 1 || v.status === '1'
                ? 1
                : v.status === 2 || v.status === '2'
                  ? 2
                  : v.status === 3 || v.status === '3'
                    ? 3
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
        ]}
      />
      <Modal
        open={!!helpFor}
        title="砍价助力记录"
        footer={null}
        onCancel={() => setHelpFor(undefined)}
      >
        {helps.isError ? (
          <Alert
            type="error"
            title="助力读取失败"
            description={String(helps.error)}
            action={<Button onClick={() => helps.refetch()}>重试</Button>}
          />
        ) : (
          <Table
            rowKey="id"
            loading={helps.isFetching}
            pagination={false}
            dataSource={helps.data?.records ?? []}
            columns={[
              { title: '助力编号', dataIndex: 'id' },
              { title: '会员编号', dataIndex: 'userId' },
              { title: '砍掉金额（元）', dataIndex: 'reducePrice' },
            ]}
          />
        )}
      </Modal>
    </PageContainer>
  );
}
