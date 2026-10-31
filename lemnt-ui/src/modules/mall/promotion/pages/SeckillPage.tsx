import { useState } from 'react';
import {
  PageContainer,
  ProTable,
  ProFormText,
  ProFormTextArea,
  ProFormSelect,
  ProFormDigit,
  ProFormList,
  ProFormDateTimePicker,
  type ProColumns,
} from '@ant-design/pro-components';
import { Alert, App, Button, Descriptions, Form, Modal, Popconfirm, Result, Table } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { AdminModalForm } from '@/shared/components/AdminModalForm';
import { usePermission } from '@/shared/hooks/usePermission';
import { getSpuDetail, getSpuList } from '../../catalog/api/catalogApi';
import * as api from '../api/seckillApi';
import type {
  SeckillActivity,
  SeckillActivityFilter,
  SeckillActivitySave,
  SeckillConfig,
  SeckillConfigSave,
} from '../types';

type ConfigForm = Omit<SeckillConfigSave, 'sliderPicUrls'> & { sliderPicUrlsText: string };

/** 秒杀时段管理，时间点使用每日 HH:mm:ss。 */
export function SeckillConfigPage() {
  const { can } = usePermission();
  const { message } = App.useApp();
  const cache = useQueryClient();
  const [filters, setFilters] = useState<{ name?: string; status?: 0 | 1 }>({});
  const [page, setPage] = useState({ pageNum: 1, pageSize: 20 });
  const [editor, setEditor] = useState<{ id: string | null; readOnly: boolean }>();
  const rows = useQuery({
    queryKey: ['seckill', 'configs', filters, page],
    queryFn: () => api.getSeckillConfigPage({ ...filters, ...page }),
    enabled: can('mall:promotion:list'),
  });
  const detail = useQuery<SeckillConfig>({
    queryKey: ['seckill', 'config', editor?.id],
    queryFn: async () => {
      const list = await api.getSeckillConfigList();
      const value = list.find((item) => item.id === editor?.id);
      if (!value) throw Error('秒杀时段不存在');
      return value;
    },
    enabled: !!editor?.id && can('mall:promotion:query'),
  });
  const save = useMutation({
    mutationFn: async (v: ConfigForm) => {
      const { sliderPicUrlsText, ...rest } = v;
      const payload = api.configPayload({
        ...rest,
        sliderPicUrls: sliderPicUrlsText
          .split('\n')
          .map((url) => url.trim())
          .filter(Boolean),
      });
      if (editor?.id) await api.updateSeckillConfig(editor.id, payload);
      else await api.createSeckillConfig(payload);
    },
  });
  const change = useMutation({
    mutationFn: ({ id, remove }: { id: string; remove: boolean }) =>
      remove ? api.deleteSeckillConfig(id) : api.updateSeckillConfigStatus(id, 1),
  });
  async function refresh() {
    try {
      await cache.invalidateQueries({ queryKey: ['seckill'] }, { throwOnError: true });
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
  const columns: ProColumns<SeckillConfig>[] = [
    { title: '时段编号', dataIndex: 'id', search: false },
    { title: '名称', dataIndex: 'name' },
    {
      title: '状态',
      dataIndex: 'status',
      valueType: 'select',
      valueEnum: { 0: { text: '开启' }, 1: { text: '关闭' } },
    },
    { title: '开始', dataIndex: 'startTime', search: false },
    { title: '结束', dataIndex: 'endTime', search: false },
    { title: '关联活动', dataIndex: 'activityCount', search: false },
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
            title="关闭后该时段不再展示，确认关闭？"
            onConfirm={() => mutate(row.id, false)}
          >
            <Button type="link" disabled={change.isPending}>
              关闭
            </Button>
          </Popconfirm>
        ),
        row.status === 1 && can('mall:promotion:delete') && (
          <Popconfirm key="delete" title="确认删除该时段？" onConfirm={() => mutate(row.id, true)}>
            <Button type="link" danger disabled={change.isPending}>
              删除
            </Button>
          </Popconfirm>
        ),
      ],
    },
  ];
  if (!can('mall:promotion:list')) return <Result status="403" title="没有秒杀时段列表权限" />;
  const loaded = editor?.id ? detail.data : undefined;
  const editable = !editor?.readOnly && (!loaded || loaded.status === 0);
  const initial: ConfigForm = loaded
    ? {
        name: loaded.name,
        startTime: loaded.startTime,
        endTime: loaded.endTime,
        sliderPicUrlsText: loaded.sliderPicUrls.join('\n'),
        status: loaded.status,
      }
    : { name: '', startTime: '09:00:00', endTime: '10:00:00', sliderPicUrlsText: '', status: 0 };
  return (
    <PageContainer title="秒杀时段">
      {rows.isError && (
        <Alert
          type="error"
          title="时段读取失败"
          description={String(rows.error)}
          action={<Button onClick={() => rows.refetch()}>重试</Button>}
        />
      )}
      <ProTable<SeckillConfig>
        options={{ reload: false }}
        rowKey="id"
        scroll={{ x: 900 }}
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
              新增时段
            </Button>
          ),
        ]}
      />
      {editor?.id && (detail.isFetching || detail.isError) && (
        <Modal open title="秒杀时段" footer={null} onCancel={() => setEditor(undefined)}>
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
        <AdminModalForm<ConfigForm>
          key={`${editor.id ?? 'new'}-${detail.dataUpdatedAt}`}
          open
          title={`${editable ? (editor.id ? '编辑' : '新增') : '查看'}秒杀时段`}
          width={720}
          initialValues={initial}
          modalProps={{ destroyOnHidden: true, maskClosable: false, closable: !save.isPending }}
          onOpenChange={(open) => {
            if (!open && !save.isPending) setEditor(undefined);
          }}
          submitter={editable ? { submitButtonProps: { loading: save.isPending } } : false}
          onFinish={async (values) => {
            if (!can(editor.id ? 'mall:promotion:update' : 'mall:promotion:create')) {
              message.error('没有秒杀时段保存权限');
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
            <Descriptions
              column={1}
              items={[
                { key: 'name', label: '名称', children: loaded.name },
                { key: 'time', label: '时间', children: `${loaded.startTime} - ${loaded.endTime}` },
                { key: 'status', label: '状态', children: loaded.status === 0 ? '开启' : '关闭' },
                { key: 'count', label: '关联活动', children: loaded.activityCount },
              ]}
            />
          ) : (
            <>
              <ProFormText
                name="name"
                label="时段名称"
                rules={[{ required: true }, { max: 100 }]}
              />
              <ProFormText
                name="startTime"
                label="开始时间点"
                placeholder="HH:mm:ss"
                rules={[{ required: true, pattern: /^([01]\d|2[0-3]):[0-5]\d(:[0-5]\d)?$/ }]}
              />
              <ProFormText
                name="endTime"
                label="结束时间点"
                placeholder="HH:mm:ss"
                rules={[{ required: true, pattern: /^([01]\d|2[0-3]):[0-5]\d(:[0-5]\d)?$/ }]}
              />
              <ProFormSelect
                name="status"
                label="状态"
                options={[
                  { label: '开启', value: 0 },
                  { label: '关闭', value: 1 },
                ]}
                rules={[{ required: true }]}
              />
              <ProFormTextArea name="sliderPicUrlsText" label="轮播图地址（每行一个）" />
            </>
          )}
        </AdminModalForm>
      )}
    </PageContainer>
  );
}

/** 秒杀活动管理，商品取自商品中心，价格低于原价由后端再次校验。 */
export function SeckillActivityPage() {
  const { can } = usePermission();
  const { message } = App.useApp();
  const cache = useQueryClient();
  const [filters, setFilters] = useState<
    Pick<SeckillActivityFilter, 'name' | 'status' | 'configId'>
  >({});
  const [page, setPage] = useState({ pageNum: 1, pageSize: 20 });
  const [editor, setEditor] = useState<{ id: string | null; readOnly: boolean }>();
  const rows = useQuery({
    queryKey: ['seckill', 'activities', filters, page],
    queryFn: () => api.getSeckillActivityPage({ ...filters, ...page }),
    enabled: can('mall:promotion:list'),
  });
  const configs = useQuery({
    queryKey: ['seckill', 'config', 'options'],
    queryFn: api.getSeckillConfigList,
    enabled: can('mall:promotion:query'),
  });
  const detail = useQuery<SeckillActivity>({
    queryKey: ['seckill', 'activity', editor?.id],
    queryFn: () => api.getSeckillActivity(editor?.id ?? ''),
    enabled: !!editor?.id && can('mall:promotion:query'),
  });
  const save = useMutation({
    mutationFn: async (v: SeckillActivitySave) => {
      const payload = api.activityPayload(v);
      if (editor?.id) await api.updateSeckillActivity(editor.id, payload);
      else await api.createSeckillActivity(payload);
    },
  });
  const change = useMutation({
    mutationFn: ({ id, remove }: { id: string; remove: boolean }) =>
      remove ? api.deleteSeckillActivity(id) : api.closeSeckillActivity(id),
  });
  async function refresh() {
    try {
      await cache.invalidateQueries({ queryKey: ['seckill'] }, { throwOnError: true });
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
  const columns: ProColumns<SeckillActivity>[] = [
    { title: '活动编号', dataIndex: 'id', search: false },
    { title: '活动名称', dataIndex: 'name' },
    { title: '商品', dataIndex: 'spuName', search: false },
    { title: '秒杀价', dataIndex: 'seckillPrice', search: false },
    {
      title: '库存',
      dataIndex: 'stock',
      search: false,
      render: (_, row) => `${row.stock}/${row.totalStock}`,
    },
    {
      title: '状态',
      dataIndex: 'status',
      valueType: 'select',
      valueEnum: { 0: { text: '开启' }, 1: { text: '关闭' } },
    },
    {
      title: '时段',
      dataIndex: 'configId',
      search: true,
      valueType: 'select',
      fieldProps: {
        options: configs.data?.map((row) => ({ label: row.name, value: row.id })) ?? [],
      },
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
  if (!can('mall:promotion:list')) return <Result status="403" title="没有秒杀活动列表权限" />;
  const loaded = editor?.id ? detail.data : undefined;
  const editable = !editor?.readOnly && (!loaded || loaded.status === 0);
  const initial: SeckillActivitySave = loaded
    ? {
        spuId: loaded.spuId,
        name: loaded.name,
        remark: loaded.remark,
        startTime: loaded.startTime,
        endTime: loaded.endTime,
        sort: loaded.sort,
        configIds: loaded.configIds,
        totalLimitCount: loaded.totalLimitCount,
        singleLimitCount: loaded.singleLimitCount,
        products: loaded.products.map((item) => ({
          skuId: item.skuId,
          seckillPrice: item.seckillPrice,
          stock: item.stock,
        })),
      }
    : {
        spuId: '',
        name: '',
        remark: null,
        startTime: '',
        endTime: '',
        sort: 0,
        configIds: [],
        totalLimitCount: 1,
        singleLimitCount: 1,
        products: [{ skuId: '', seckillPrice: '', stock: 1 }],
      };
  return (
    <PageContainer title="秒杀活动">
      {rows.isError && (
        <Alert
          type="error"
          title="活动读取失败"
          description={String(rows.error)}
          action={<Button onClick={() => rows.refetch()}>重试</Button>}
        />
      )}
      <ProTable<SeckillActivity>
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
            configId: typeof v.configId === 'string' ? v.configId : undefined,
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
              新增秒杀活动
            </Button>
          ),
        ]}
      />
      {editor?.id && (detail.isFetching || detail.isError) && (
        <Modal open title="秒杀活动" footer={null} onCancel={() => setEditor(undefined)}>
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
        <AdminModalForm<SeckillActivitySave>
          key={`${editor.id ?? 'new'}-${detail.dataUpdatedAt}`}
          open
          title={`${editable ? (editor.id ? '编辑' : '新增') : '查看'}秒杀活动`}
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
              message.error('没有秒杀活动保存权限');
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
            <SeckillActivityDetails value={loaded} configs={configs.data ?? []} />
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
              <ProFormSelect
                name="configIds"
                label="秒杀时段"
                mode="multiple"
                options={
                  configs.data
                    ?.filter((row) => row.status === 0)
                    .map((row) => ({
                      label: `${row.name}（${row.startTime} - ${row.endTime}）`,
                      value: row.id,
                    })) ?? []
                }
                rules={[{ required: true }]}
              />
              <ProFormDigit
                name="sort"
                label="排序"
                min={0}
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
              <ProFormTextArea name="remark" label="备注" />
              <ProFormList
                name="products"
                label="秒杀商品"
                min={1}
                copyIconProps={false}
                creatorButtonProps={{ creatorButtonText: '添加商品SKU' }}
              >
                {(field) => <SeckillProductFields key={field.key} />}
              </ProFormList>
            </>
          )}
        </AdminModalForm>
      )}
    </PageContainer>
  );
}

function SeckillProductFields() {
  const { can } = usePermission();
  const form = Form.useFormInstance<SeckillActivitySave>();
  const spuId = Form.useWatch('spuId', form);
  const skus = useQuery({
    queryKey: ['seckill', 'spu', spuId],
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
      <ProFormText name="seckillPrice" label="秒杀价（元）" rules={[{ required: true }]} />
      <ProFormDigit
        name="stock"
        label="秒杀库存"
        min={0}
        fieldProps={{ precision: 0 }}
        rules={[{ required: true }]}
      />
    </>
  );
}

function SeckillActivityDetails({
  value,
  configs,
}: {
  value: SeckillActivity;
  configs: SeckillConfig[];
}) {
  const names = value.configIds
    .map((id) => configs.find((row) => row.id === id)?.name ?? id)
    .join('、');
  return (
    <>
      <Descriptions
        column={1}
        items={[
          { key: 'name', label: '名称', children: value.name },
          { key: 'spu', label: '商品', children: value.spuName ?? value.spuId },
          { key: 'status', label: '状态', children: value.status === 0 ? '开启' : '关闭' },
          { key: 'time', label: '活动时间', children: `${value.startTime} 至 ${value.endTime}` },
          { key: 'config', label: '秒杀时段', children: names || '—' },
          {
            key: 'limit',
            label: '限购',
            children: `总 ${value.totalLimitCount}，单次 ${value.singleLimitCount}`,
          },
          { key: 'stock', label: '库存', children: `${value.stock}/${value.totalStock}` },
          { key: 'remark', label: '备注', children: value.remark || '—' },
        ]}
      />
      <Table
        rowKey="skuId"
        pagination={false}
        dataSource={value.products}
        columns={[
          { title: 'SKU编号', dataIndex: 'skuId' },
          { title: '秒杀价（元）', dataIndex: 'seckillPrice' },
          { title: '剩余库存', dataIndex: 'stock' },
        ]}
      />
    </>
  );
}
