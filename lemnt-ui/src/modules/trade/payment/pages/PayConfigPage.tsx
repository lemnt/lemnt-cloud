import { useState } from 'react';
import {
  PageContainer,
  ProTable,
  ProFormText,
  ProFormTextArea,
  ProFormSelect,
  ProFormDigit,
  type ProColumns,
} from '@ant-design/pro-components';
import { Alert, App, Button, Popconfirm, Result } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { AdminModalForm } from '@/shared/components/AdminModalForm';
import { usePermission } from '@/shared/hooks/usePermission';
import * as api from '../api/payConfigApi';

const statusEnum = { 0: '启用', 1: '停用' } as const;

/** 支付应用管理：应用标识创建后不可修改，仍有渠道配置时不能删除。 */
export function PayAppPage() {
  const { can } = usePermission(),
    { message } = App.useApp(),
    cache = useQueryClient();
  const prefix = 'trade:pay-app';
  const [filters, setFilters] = useState<{ appKey?: string; name?: string; status?: 0 | 1 }>({});
  const [page, setPage] = useState({ pageNum: 1, pageSize: 20 });
  const [editor, setEditor] = useState<{ id: string | null; readOnly: boolean }>();
  const rows = useQuery({
    queryKey: [prefix, filters, page],
    queryFn: () => api.getPayApps({ ...filters, ...page }),
    enabled: can(prefix + ':list'),
  });
  const detail = useQuery({
    queryKey: [prefix, 'detail', editor?.id],
    queryFn: () => api.getPayApp(editor?.id ?? ''),
    enabled: !!editor?.id && can(prefix + ':query'),
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
  });
  const save = useMutation({
    mutationFn: (values: api.PayAppValues) => api.savePayApp(editor?.id ?? null, values),
  });
  const toggle = useMutation({
    mutationFn: (row: api.PayApp) => api.updatePayAppStatus(row.id, row.status === 0 ? 1 : 0),
  });
  const remove = useMutation({ mutationFn: (key: string) => api.deletePayApp(key) });
  async function refresh() {
    try {
      await cache.invalidateQueries({ queryKey: [prefix] }, { throwOnError: true });
    } catch {
      message.warning('操作已成功，刷新失败请重试');
    }
  }
  const columns: ProColumns<api.PayApp>[] = [
    { title: '编号', dataIndex: 'id', search: false },
    { title: '应用标识', dataIndex: 'appKey' },
    { title: '应用名', dataIndex: 'name' },
    { title: '状态', dataIndex: 'status', valueType: 'select', valueEnum: statusEnum },
    { title: '创建时间', dataIndex: 'createTime', search: false },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) => (
        <>
          {can(prefix + ':query') && (
            <Button
              type="link"
              onClick={() => setEditor({ id: row.id, readOnly: !can(prefix + ':update') })}
            >
              {can(prefix + ':update') ? '编辑' : '详情'}
            </Button>
          )}
          {can(prefix + ':update') && (
            <Popconfirm
              title={`确认${row.status === 0 ? '停用' : '启用'}该应用？`}
              onConfirm={async () => {
                if (toggle.isPending) return;
                try {
                  await toggle.mutateAsync(row);
                  message.success('已更新状态');
                  await refresh();
                } catch (e) {
                  message.error(String(e));
                }
              }}
            >
              <Button type="link" disabled={toggle.isPending}>
                {row.status === 0 ? '停用' : '启用'}
              </Button>
            </Popconfirm>
          )}
          {can(prefix + ':remove') && (
            <Popconfirm
              title="确认删除该支付应用？"
              onConfirm={async () => {
                if (remove.isPending) return;
                try {
                  await remove.mutateAsync(row.id);
                  message.success('已删除');
                  await refresh();
                } catch (e) {
                  message.error(String(e));
                }
              }}
            >
              <Button type="link" danger disabled={remove.isPending}>
                删除
              </Button>
            </Popconfirm>
          )}
        </>
      ),
    },
  ];
  if (!can(prefix + ':list')) return <Result status="403" title="没有支付应用列表权限" />;
  const initial = editor?.id
    ? detail.data
    : { appKey: '', name: '', status: 0, remark: '', orderNotifyUrl: '', refundNotifyUrl: '' };
  return (
    <PageContainer title="支付应用">
      {rows.isError && (
        <Alert
          type="error"
          title="列表读取失败"
          description={String(rows.error)}
          action={<Button onClick={() => rows.refetch()}>重试</Button>}
        />
      )}
      <ProTable
        options={{ reload: false }}
        rowKey="id"
        columns={columns}
        dataSource={rows.data?.records ?? []}
        loading={rows.isFetching}
        scroll={{ x: 900 }}
        toolBarRender={() => [
          <Button key="refresh" onClick={() => rows.refetch()}>
            刷新
          </Button>,
          can(prefix + ':create') && (
            <Button
              key="create"
              type="primary"
              onClick={() => setEditor({ id: null, readOnly: false })}
            >
              新增支付应用
            </Button>
          ),
        ]}
        onSubmit={(v) => {
          const status =
            v.status === '0' || v.status === 0
              ? 0
              : v.status === '1' || v.status === 1
                ? 1
                : undefined;
          setFilters({
            appKey: typeof v.appKey === 'string' ? v.appKey : undefined,
            name: typeof v.name === 'string' ? v.name : undefined,
            status,
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
      />
      {editor && editor.id && detail.isFetching && (
        <Alert
          type="info"
          title="正在读取详情…"
          action={<Button onClick={() => setEditor(undefined)}>取消</Button>}
        />
      )}
      {editor && editor.id && detail.isError && (
        <Alert
          type="error"
          title="详情读取失败，未打开编辑表单"
          action={
            <>
              <Button onClick={() => detail.refetch()}>重试</Button>
              <Button onClick={() => setEditor(undefined)}>取消</Button>
            </>
          }
        />
      )}
      {editor && initial && (!editor.id || (detail.isSuccess && !detail.isFetching)) && (
        <AdminModalForm<api.PayAppValues>
          key={editor.id ?? 'new'}
          title={`${editor.readOnly ? '查看' : editor.id ? '编辑' : '新增'}支付应用`}
          open
          initialValues={initial as api.PayAppValues}
          disabled={editor.readOnly || save.isPending}
          submitter={editor.readOnly ? false : { submitButtonProps: { loading: save.isPending } }}
          modalProps={{ maskClosable: false, closable: !save.isPending }}
          onOpenChange={(open) => {
            if (!open && !save.isPending) setEditor(undefined);
          }}
          onFinish={async (values) => {
            if (
              save.isPending ||
              editor.readOnly ||
              !can(prefix + (editor.id ? ':update' : ':create'))
            )
              return false;
            try {
              await save.mutateAsync(values);
              message.success('已保存');
              setEditor(undefined);
              await refresh();
              return true;
            } catch (e) {
              message.error(String(e));
              return false;
            }
          }}
        >
          <ProFormText
            name="appKey"
            label="应用标识"
            disabled={!!editor.id}
            rules={[{ required: true, whitespace: true, min: 4, max: 64 }]}
            extra={editor.id ? '应用标识创建后不可修改' : '4-64位字母、数字、下划线或中划线'}
          />
          <ProFormText
            name="name"
            label="应用名"
            rules={[{ required: true, whitespace: true, max: 64 }]}
          />
          <ProFormSelect
            name="status"
            label="状态"
            options={[
              { value: 0, label: '启用' },
              { value: 1, label: '停用' },
            ]}
            rules={[{ required: true }]}
          />
          <ProFormText name="orderNotifyUrl" label="支付结果回调地址" rules={[{ max: 255 }]} />
          <ProFormText name="refundNotifyUrl" label="退款结果回调地址" rules={[{ max: 255 }]} />
          <ProFormTextArea
            name="remark"
            label="备注"
            fieldProps={{ maxLength: 255, showCount: true }}
          />
        </AdminModalForm>
      )}
    </PageContainer>
  );
}

/** 支付渠道配置：配置明文不回显，编辑时留空表示保留原值。 */
export function PayChannelConfigPage() {
  const { can } = usePermission(),
    { message } = App.useApp(),
    cache = useQueryClient();
  const prefix = 'trade:pay-channel';
  const [filters, setFilters] = useState<{ appId?: string; code?: string; status?: 0 | 1 }>({});
  const [page, setPage] = useState({ pageNum: 1, pageSize: 20 });
  const [editor, setEditor] = useState<{ id: string | null; readOnly: boolean }>();
  const rows = useQuery({
    queryKey: [prefix, filters, page],
    queryFn: () => api.getPayChannels({ ...filters, ...page }),
    enabled: can(prefix + ':list'),
  });
  const apps = useQuery({
    queryKey: ['trade-pay-app', 'options'],
    queryFn: () => api.getPayApps({ pageNum: 1, pageSize: 100 }),
    enabled: can(prefix + ':create') || can(prefix + ':list'),
  });
  const detail = useQuery({
    queryKey: [prefix, 'detail', editor?.id],
    queryFn: () => api.getPayChannel(editor?.id ?? ''),
    enabled: !!editor?.id && can(prefix + ':query'),
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
  });
  const save = useMutation({
    mutationFn: (values: api.PayChannelValues) => api.savePayChannel(editor?.id ?? null, values),
  });
  const remove = useMutation({ mutationFn: (key: string) => api.deletePayChannel(key) });
  async function refresh() {
    try {
      await cache.invalidateQueries({ queryKey: [prefix] }, { throwOnError: true });
    } catch {
      message.warning('操作已成功，刷新失败请重试');
    }
  }
  const appOptions = (apps.data?.records ?? []).map((row) => ({
    value: row.id,
    label: `${row.name}（${row.appKey}）`,
  }));
  const columns: ProColumns<api.PayChannelConfig>[] = [
    { title: '编号', dataIndex: 'id', search: false },
    { title: '应用编号', dataIndex: 'appId' },
    { title: '渠道编码', dataIndex: 'code' },
    { title: '状态', dataIndex: 'status', valueType: 'select', valueEnum: statusEnum },
    { title: '费率（%）', dataIndex: 'feeRate', search: false },
    {
      title: '渠道配置',
      dataIndex: 'configConfigured',
      search: false,
      render: (_, row) => (row.configConfigured ? '已配置' : '未配置'),
    },
    { title: '创建时间', dataIndex: 'createTime', search: false },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) => (
        <>
          {can(prefix + ':query') && (
            <Button
              type="link"
              onClick={() => setEditor({ id: row.id, readOnly: !can(prefix + ':update') })}
            >
              {can(prefix + ':update') ? '编辑' : '详情'}
            </Button>
          )}
          {can(prefix + ':remove') && (
            <Popconfirm
              title="确认删除该渠道配置？"
              onConfirm={async () => {
                if (remove.isPending) return;
                try {
                  await remove.mutateAsync(row.id);
                  message.success('已删除');
                  await refresh();
                } catch (e) {
                  message.error(String(e));
                }
              }}
            >
              <Button type="link" danger disabled={remove.isPending}>
                删除
              </Button>
            </Popconfirm>
          )}
        </>
      ),
    },
  ];
  if (!can(prefix + ':list')) return <Result status="403" title="没有支付渠道配置列表权限" />;
  const initial = editor?.id
    ? detail.data && {
        ...detail.data,
        feeRate: detail.data.feeRate == null ? null : Number(detail.data.feeRate),
        config: '',
      }
    : { appId: '', code: 'ALIPAY', status: 0, feeRate: 0, remark: '', config: '' };
  return (
    <PageContainer title="支付渠道配置">
      {rows.isError && (
        <Alert
          type="error"
          title="列表读取失败"
          description={String(rows.error)}
          action={<Button onClick={() => rows.refetch()}>重试</Button>}
        />
      )}
      <ProTable
        options={{ reload: false }}
        rowKey="id"
        columns={columns}
        dataSource={rows.data?.records ?? []}
        loading={rows.isFetching}
        scroll={{ x: 1000 }}
        toolBarRender={() => [
          <Button key="refresh" onClick={() => rows.refetch()}>
            刷新
          </Button>,
          can(prefix + ':create') && (
            <Button
              key="create"
              type="primary"
              onClick={() => setEditor({ id: null, readOnly: false })}
            >
              新增渠道配置
            </Button>
          ),
        ]}
        onSubmit={(v) => {
          const status =
            v.status === '0' || v.status === 0
              ? 0
              : v.status === '1' || v.status === 1
                ? 1
                : undefined;
          setFilters({
            appId: typeof v.appId === 'string' ? v.appId : undefined,
            code: typeof v.code === 'string' ? v.code : undefined,
            status,
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
      />
      {editor && editor.id && detail.isFetching && (
        <Alert
          type="info"
          title="正在读取详情…"
          action={<Button onClick={() => setEditor(undefined)}>取消</Button>}
        />
      )}
      {editor && editor.id && detail.isError && (
        <Alert
          type="error"
          title="详情读取失败，未打开编辑表单"
          action={
            <>
              <Button onClick={() => detail.refetch()}>重试</Button>
              <Button onClick={() => setEditor(undefined)}>取消</Button>
            </>
          }
        />
      )}
      {editor && initial && (!editor.id || (detail.isSuccess && !detail.isFetching)) && (
        <AdminModalForm<api.PayChannelValues>
          key={editor.id ?? 'new'}
          title={`${editor.readOnly ? '查看' : editor.id ? '编辑' : '新增'}渠道配置`}
          open
          initialValues={initial as api.PayChannelValues}
          disabled={editor.readOnly || save.isPending}
          submitter={editor.readOnly ? false : { submitButtonProps: { loading: save.isPending } }}
          modalProps={{ maskClosable: false, closable: !save.isPending }}
          onOpenChange={(open) => {
            if (!open && !save.isPending) setEditor(undefined);
          }}
          onFinish={async (values) => {
            if (
              save.isPending ||
              editor.readOnly ||
              !can(prefix + (editor.id ? ':update' : ':create'))
            )
              return false;
            try {
              await save.mutateAsync(values);
              message.success('已保存');
              setEditor(undefined);
              await refresh();
              return true;
            } catch (e) {
              message.error(String(e));
              return false;
            }
          }}
        >
          <ProFormSelect
            name="appId"
            label="所属应用"
            options={appOptions}
            disabled={!!editor.id}
            rules={[{ required: true }]}
            extra={editor.id ? '所属应用创建后不可修改' : undefined}
          />
          <ProFormSelect
            name="code"
            label="渠道编码"
            disabled={!!editor.id}
            options={[
              { value: 'ALIPAY', label: '支付宝' },
              { value: 'WECHAT', label: '微信支付' },
            ]}
            rules={[{ required: true }]}
            extra={editor.id ? '渠道编码创建后不可修改' : undefined}
          />
          <ProFormSelect
            name="status"
            label="状态"
            options={[
              { value: 0, label: '启用' },
              { value: 1, label: '停用' },
            ]}
            rules={[{ required: true }]}
          />
          <ProFormDigit
            name="feeRate"
            label="费率（%）"
            min={0}
            max={100}
            fieldProps={{ precision: 2 }}
          />
          <ProFormTextArea
            name="config"
            label="渠道配置（JSON）"
            fieldProps={{ maxLength: 4000, showCount: true, rows: 6 }}
            extra={editor.id ? '留空表示保留原配置；填写则整体替换' : '可选，JSON 对象文本'}
          />
          <ProFormTextArea
            name="remark"
            label="备注"
            fieldProps={{ maxLength: 255, showCount: true }}
          />
        </AdminModalForm>
      )}
    </PageContainer>
  );
}
