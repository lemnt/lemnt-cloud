import { useState } from 'react';
import {
  PageContainer,
  ProTable,
  ProFormText,
  ProFormSelect,
  ProFormDigit,
  ProFormDateTimePicker,
  ProFormList,
  type ProColumns,
} from '@ant-design/pro-components';
import { Alert, App, Button, Descriptions, Form, Modal, Popconfirm, Result, Table } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { AdminModalForm } from '@/shared/components/AdminModalForm';
import { usePermission } from '@/shared/hooks/usePermission';
import { getSpuDetail, getSpuList } from '../../catalog/api/catalogApi';
import * as api from '../api/combinationApi';
import type {
  CombinationActivity,
  CombinationActivityFilter,
  CombinationActivitySave,
  CombinationRecord,
} from '../types';

type CombinationForm = Omit<CombinationActivitySave, 'virtualGroup'> & {
  virtualGroup: 'true' | 'false';
};

/** 拼团活动管理。 */
export function CombinationActivityPage() {
  const { can } = usePermission();
  const { message } = App.useApp();
  const cache = useQueryClient();
  const [filters, setFilters] = useState<
    Pick<CombinationActivityFilter, 'name' | 'status' | 'spuId'>
  >({});
  const [page, setPage] = useState({ pageNum: 1, pageSize: 20 });
  const [editor, setEditor] = useState<{ id: string | null; readOnly: boolean }>();
  const rows = useQuery({
    queryKey: ['combination', 'activities', filters, page],
    queryFn: () => api.getCombinationActivityPage({ ...filters, ...page }),
    enabled: can('mall:promotion:list'),
  });
  const detail = useQuery<CombinationActivity>({
    queryKey: ['combination', 'activity', editor?.id],
    queryFn: () => api.getCombinationActivity(editor?.id ?? ''),
    enabled: !!editor?.id && can('mall:promotion:query'),
  });
  const save = useMutation({
    mutationFn: async (v: CombinationForm) => {
      const payload = api.combinationPayload({ ...v, virtualGroup: v.virtualGroup === 'true' });
      if (editor?.id) await api.updateCombinationActivity(editor.id, payload);
      else await api.createCombinationActivity(payload);
    },
  });
  const change = useMutation({
    mutationFn: ({ id, remove }: { id: string; remove: boolean }) =>
      remove ? api.deleteCombinationActivity(id) : api.closeCombinationActivity(id),
  });
  async function refresh() {
    try {
      await cache.invalidateQueries({ queryKey: ['combination'] }, { throwOnError: true });
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
  const columns: ProColumns<CombinationActivity>[] = [
    { title: '活动编号', dataIndex: 'id', search: false },
    { title: '活动名称', dataIndex: 'name' },
    { title: '商品', dataIndex: 'spuName', search: false },
    { title: '拼团价', dataIndex: 'combinationPrice', search: false },
    { title: '成团人数', dataIndex: 'userSize', search: false },
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
  if (!can('mall:promotion:list')) return <Result status="403" title="没有拼团活动列表权限" />;
  const loaded = editor?.id ? detail.data : undefined;
  const editable = !editor?.readOnly && (!loaded || loaded.status === 0);
  const initial: CombinationForm = loaded
    ? {
        name: loaded.name,
        spuId: loaded.spuId,
        totalLimitCount: loaded.totalLimitCount,
        singleLimitCount: loaded.singleLimitCount,
        startTime: loaded.startTime,
        endTime: loaded.endTime,
        userSize: loaded.userSize,
        virtualGroup: loaded.virtualGroup ? 'true' : 'false',
        limitDuration: loaded.limitDuration,
        products: loaded.products,
      }
    : {
        name: '',
        spuId: '',
        totalLimitCount: 10,
        singleLimitCount: 1,
        startTime: '',
        endTime: '',
        userSize: 2,
        virtualGroup: 'false',
        limitDuration: 24,
        products: [{ skuId: '', combinationPrice: '' }],
      };
  return (
    <PageContainer title="拼团活动">
      {rows.isError && (
        <Alert
          type="error"
          title="活动读取失败"
          description={String(rows.error)}
          action={<Button onClick={() => rows.refetch()}>重试</Button>}
        />
      )}
      <ProTable<CombinationActivity>
        options={{ reload: false }}
        rowKey="id"
        scroll={{ x: 1100 }}
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
            spuId: typeof v.spuId === 'string' ? v.spuId : undefined,
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
              新增拼团活动
            </Button>
          ),
        ]}
      />
      {editor?.id && (detail.isFetching || detail.isError) && (
        <Modal open title="拼团活动" footer={null} onCancel={() => setEditor(undefined)}>
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
        <AdminModalForm<CombinationForm>
          key={`${editor.id ?? 'new'}-${detail.dataUpdatedAt}`}
          open
          title={`${editable ? (editor.id ? '编辑' : '新增') : '查看'}拼团活动`}
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
              message.error('没有拼团活动保存权限');
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
            <CombinationDetails value={loaded} />
          ) : (
            <>
              <ProFormText
                name="name"
                label="活动名称"
                rules={[{ required: true }, { max: 100 }]}
              />
              <ProFormSelect
                name="spuId"
                label="商品"
                showSearch
                debounceTime={300}
                disabled={!can('mall:spu:list')}
                request={async ({ keyWords }) =>
                  can('mall:spu:list')
                    ? (await getSpuList({ name: keyWords, pageNum: 1, pageSize: 100 })).data.map(
                        (row) => ({
                          label: row.name,
                          value: row.id,
                        }),
                      )
                    : []
                }
                rules={[{ required: true }]}
              />
              <ProFormDateTimePicker
                name="startTime"
                label="开始时间"
                rules={[{ required: true }]}
              />
              <ProFormDateTimePicker name="endTime" label="结束时间" rules={[{ required: true }]} />
              <ProFormDigit
                name="userSize"
                label="成团人数"
                min={2}
                fieldProps={{ precision: 0 }}
                rules={[{ required: true }]}
              />
              <ProFormSelect
                name="virtualGroup"
                label="虚拟成团"
                options={[
                  { label: '是', value: 'true' },
                  { label: '否', value: 'false' },
                ]}
                rules={[{ required: true }]}
              />
              <ProFormDigit
                name="limitDuration"
                label="拼团时限（小时）"
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
              <ProFormDigit
                name="singleLimitCount"
                label="单次限购数量"
                min={1}
                fieldProps={{ precision: 0 }}
                rules={[{ required: true }]}
              />
              <ProFormList
                name="products"
                label="拼团商品"
                min={1}
                copyIconProps={false}
                creatorButtonProps={{ creatorButtonText: '添加商品SKU' }}
              >
                {(field) => <CombinationProductFields key={field.key} />}
              </ProFormList>
            </>
          )}
        </AdminModalForm>
      )}
    </PageContainer>
  );
}

function CombinationProductFields() {
  const { can } = usePermission();
  const form = Form.useFormInstance<CombinationActivitySave>();
  const spuId = Form.useWatch('spuId', form);
  const skus = useQuery({
    queryKey: ['combination', 'spu', spuId],
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
      <ProFormText name="combinationPrice" label="拼团价（元）" rules={[{ required: true }]} />
    </>
  );
}

function CombinationDetails({ value }: { value: CombinationActivity }) {
  return (
    <>
      <Descriptions
        column={1}
        items={[
          { key: 'name', label: '名称', children: value.name },
          { key: 'spu', label: '商品', children: value.spuName ?? value.spuId },
          { key: 'status', label: '状态', children: value.status === 0 ? '开启' : '关闭' },
          { key: 'time', label: '活动时间', children: `${value.startTime} 至 ${value.endTime}` },
          { key: 'size', label: '成团人数', children: value.userSize },
          { key: 'virtual', label: '虚拟成团', children: value.virtualGroup ? '是' : '否' },
          { key: 'duration', label: '拼团时限', children: `${value.limitDuration} 小时` },
          {
            key: 'limit',
            label: '限购',
            children: `总 ${value.totalLimitCount}，单次 ${value.singleLimitCount}`,
          },
        ]}
      />
      <Table
        rowKey="skuId"
        pagination={false}
        dataSource={value.products}
        columns={[
          { title: 'SKU编号', dataIndex: 'skuId' },
          { title: '拼团价（元）', dataIndex: 'combinationPrice' },
        ]}
      />
    </>
  );
}

/** 拼团记录只读查询。 */
export function CombinationRecordPage() {
  const { can } = usePermission();
  const [filters, setFilters] = useState<{
    userId?: string;
    activityId?: string;
    status?: 0 | 1 | 2;
  }>({});
  const [page, setPage] = useState({ pageNum: 1, pageSize: 20 });
  const rows = useQuery({
    queryKey: ['combination', 'records', filters, page],
    queryFn: () => api.getCombinationRecordPage({ ...filters, ...page }),
    enabled: can('mall:promotion:list'),
  });
  const columns: ProColumns<CombinationRecord>[] = [
    { title: '记录编号', dataIndex: 'id', search: false },
    { title: '活动编号', dataIndex: 'activityId', search: true },
    { title: '商品', dataIndex: 'spuName', search: false },
    { title: '会员', dataIndex: 'nickname', search: false },
    {
      title: '状态',
      dataIndex: 'status',
      valueType: 'select',
      valueEnum: { 0: { text: '进行中' }, 1: { text: '成功' }, 2: { text: '失败' } },
    },
    { title: '团长记录', dataIndex: 'headId', search: false },
    {
      title: '人数',
      dataIndex: 'userCount',
      search: false,
      render: (_, row) => `${row.userCount}/${row.userSize}`,
    },
    { title: '拼团价', dataIndex: 'combinationPrice', search: false },
    { title: '订单编号', dataIndex: 'orderId', search: false },
    { title: '过期时间', dataIndex: 'expireTime', search: false },
    { title: '结束时间', dataIndex: 'endTime', search: false },
  ];
  if (!can('mall:promotion:list')) return <Result status="403" title="没有拼团记录列表权限" />;
  return (
    <PageContainer title="拼团记录">
      {rows.isError && (
        <Alert
          type="error"
          title="记录读取失败"
          description={String(rows.error)}
          action={<Button onClick={() => rows.refetch()}>重试</Button>}
        />
      )}
      <ProTable<CombinationRecord>
        options={{ reload: false }}
        rowKey="id"
        scroll={{ x: 1200 }}
        columns={columns}
        dataSource={rows.data?.records ?? []}
        loading={rows.isFetching}
        onSubmit={(v) => {
          setFilters({
            userId: typeof v.userId === 'string' ? v.userId : undefined,
            activityId: typeof v.activityId === 'string' ? v.activityId : undefined,
            status:
              v.status === 0 || v.status === '0'
                ? 0
                : v.status === 1 || v.status === '1'
                  ? 1
                  : v.status === 2 || v.status === '2'
                    ? 2
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
    </PageContainer>
  );
}
