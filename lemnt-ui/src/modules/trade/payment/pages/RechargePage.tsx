import { useState } from 'react';
import {
  PageContainer,
  ProTable,
  ProFormText,
  ProFormSelect,
  type ProColumns,
} from '@ant-design/pro-components';
import { Alert, App, Button, Descriptions, Drawer, Popconfirm, Result } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { AdminModalForm } from '@/shared/components/AdminModalForm';
import { usePermission } from '@/shared/hooks/usePermission';
import * as api from '../api/rechargeApi';

const statusEnum = { 0: '启用', 1: '停用' } as const;
const rechargeStatusEnum = { PENDING: '待支付', PAID: '已到账', REFUNDED: '已退款' } as const;

/** 充值套餐管理。 */
export function RechargePackagePage() {
  const { can } = usePermission(),
    { message } = App.useApp(),
    cache = useQueryClient();
  const prefix = 'trade:recharge-package';
  const [filters, setFilters] = useState<{ name?: string; status?: 0 | 1 }>({});
  const [page, setPage] = useState({ pageNum: 1, pageSize: 20 });
  const [editor, setEditor] = useState<{ id: string | null }>();
  const rows = useQuery({
    queryKey: [prefix, filters, page],
    queryFn: () => api.getRechargePackages({ ...filters, ...page }),
    enabled: can(prefix + ':list'),
  });
  const detail = useQuery({
    queryKey: [prefix, 'detail', editor?.id],
    queryFn: () => api.getRechargePackage(editor?.id ?? ''),
    enabled: !!editor?.id && can(prefix + ':query'),
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
  });
  const save = useMutation({
    mutationFn: (values: api.RechargePackageValues) =>
      api.saveRechargePackage(editor?.id ?? null, values),
  });
  const remove = useMutation({ mutationFn: (key: string) => api.deleteRechargePackage(key) });
  async function refresh() {
    try {
      await cache.invalidateQueries({ queryKey: [prefix] }, { throwOnError: true });
    } catch {
      message.warning('操作已成功，刷新失败请重试');
    }
  }
  const columns: ProColumns<api.RechargePackage>[] = [
    { title: '编号', dataIndex: 'id', search: false },
    { title: '套餐名', dataIndex: 'name' },
    { title: '支付金额', dataIndex: 'payPrice', search: false },
    { title: '赠送金额', dataIndex: 'bonusPrice', search: false },
    { title: '状态', dataIndex: 'status', valueType: 'select', valueEnum: statusEnum },
    { title: '创建时间', dataIndex: 'createTime', search: false },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) => (
        <>
          {can(prefix + ':query') && can(prefix + ':update') && (
            <Button type="link" onClick={() => setEditor({ id: row.id })}>
              编辑
            </Button>
          )}
          {can(prefix + ':remove') && (
            <Popconfirm
              title="确认删除该充值套餐？"
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
  if (!can(prefix + ':list')) return <Result status="403" title="没有充值套餐列表权限" />;
  const initial = editor?.id
    ? detail.data
    : { name: '', payPrice: '100.00', bonusPrice: '0.00', status: 0 };
  return (
    <PageContainer title="充值套餐">
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
            <Button key="create" type="primary" onClick={() => setEditor({ id: null })}>
              新增套餐
            </Button>
          ),
        ]}
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
      />
      {editor && initial && (!editor.id || (detail.isSuccess && !detail.isFetching)) && (
        <AdminModalForm<api.RechargePackageValues>
          key={editor.id ?? 'new'}
          title={`${editor.id ? '编辑' : '新增'}充值套餐`}
          open
          initialValues={initial as api.RechargePackageValues}
          disabled={save.isPending}
          submitter={{ submitButtonProps: { loading: save.isPending } }}
          modalProps={{ maskClosable: false, closable: !save.isPending }}
          onOpenChange={(open) => {
            if (!open && !save.isPending) setEditor(undefined);
          }}
          onFinish={async (values) => {
            if (save.isPending || !can(prefix + (editor.id ? ':update' : ':create'))) return false;
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
            name="name"
            label="套餐名"
            rules={[{ required: true, whitespace: true, max: 64 }]}
          />
          <ProFormText
            name="payPrice"
            label="支付金额（元）"
            rules={[
              { required: true, whitespace: true },
              { pattern: /^\d{1,9}(\.\d{1,2})?$/, message: '最多两位小数' },
            ]}
          />
          <ProFormText
            name="bonusPrice"
            label="赠送金额（元）"
            rules={[
              { required: true, whitespace: true },
              { pattern: /^\d{1,9}(\.\d{1,2})?$/, message: '最多两位小数' },
            ]}
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
        </AdminModalForm>
      )}
    </PageContainer>
  );
}

/** 充值单：确认到账代替真实渠道回调，退款按到账金额出账。 */
export function WalletRechargePage() {
  const { can } = usePermission(),
    { message } = App.useApp(),
    cache = useQueryClient();
  const prefix = 'trade:wallet-recharge';
  const [filters, setFilters] = useState<{ memberId?: string; status?: api.RechargeStatus }>({});
  const [page, setPage] = useState({ pageNum: 1, pageSize: 20 });
  const [selected, setSelected] = useState<string>();
  const rows = useQuery({
    queryKey: [prefix, filters, page],
    queryFn: () => api.getWalletRecharges({ ...filters, ...page }),
    enabled: can(prefix + ':list'),
  });
  const detail = useQuery({
    queryKey: [prefix, 'detail', selected],
    queryFn: () => api.getWalletRecharge(selected ?? ''),
    enabled: !!selected && can(prefix + ':query'),
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
  });
  const confirm = useMutation({ mutationFn: (no: string) => api.confirmWalletRecharge(no) });
  const refund = useMutation({ mutationFn: (no: string) => api.refundWalletRecharge(no) });
  async function refresh() {
    try {
      await cache.invalidateQueries({ queryKey: [prefix] }, { throwOnError: true });
      if (selected) await detail.refetch();
    } catch {
      message.warning('操作已成功，刷新失败请重试');
    }
  }
  const columns: ProColumns<api.WalletRecharge>[] = [
    { title: '充值单号', dataIndex: 'rechargeNo', search: false },
    { title: '会员编号', dataIndex: 'memberId' },
    { title: '支付金额', dataIndex: 'payPrice', search: false },
    { title: '赠送金额', dataIndex: 'bonusPrice', search: false },
    { title: '到账金额', dataIndex: 'totalPrice', search: false },
    { title: '状态', dataIndex: 'status', valueType: 'select', valueEnum: rechargeStatusEnum },
    { title: '到账时间', dataIndex: 'paidTime', search: false },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) => (
        <>
          {can(prefix + ':query') && (
            <Button type="link" onClick={() => setSelected(row.rechargeNo)}>
              详情
            </Button>
          )}
          {can(prefix + ':confirm') && row.status === 'PENDING' && (
            <Popconfirm
              title="确认该充值单已到账？"
              onConfirm={async () => {
                if (confirm.isPending) return;
                try {
                  await confirm.mutateAsync(row.rechargeNo);
                  message.success('已确认到账');
                  await refresh();
                } catch (e) {
                  message.error(String(e));
                }
              }}
            >
              <Button type="link" disabled={confirm.isPending}>
                确认到账
              </Button>
            </Popconfirm>
          )}
          {can(prefix + ':refund') && row.status === 'PAID' && (
            <Popconfirm
              title="确认退款并从钱包扣回到账金额？"
              onConfirm={async () => {
                if (refund.isPending) return;
                try {
                  await refund.mutateAsync(row.rechargeNo);
                  message.success('已退款');
                  await refresh();
                } catch (e) {
                  message.error(String(e));
                }
              }}
            >
              <Button type="link" danger disabled={refund.isPending}>
                退款
              </Button>
            </Popconfirm>
          )}
        </>
      ),
    },
  ];
  if (!can(prefix + ':list')) return <Result status="403" title="没有充值单列表权限" />;
  const value = detail.data;
  return (
    <PageContainer title="充值单">
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
        scroll={{ x: 1200 }}
        toolBarRender={() => [
          <Button key="refresh" onClick={() => rows.refetch()}>
            刷新
          </Button>,
        ]}
        onSubmit={(v) => {
          setFilters({
            memberId: typeof v.memberId === 'string' ? v.memberId : undefined,
            status:
              v.status === 'PENDING' || v.status === 'PAID' || v.status === 'REFUNDED'
                ? v.status
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
      />
      <Drawer
        open={!!selected}
        title="充值单详情"
        onClose={() => setSelected(undefined)}
        width={600}
      >
        {detail.isFetching ? (
          '正在读取…'
        ) : detail.isError ? (
          <Alert
            type="error"
            title="详情读取失败"
            description={String(detail.error)}
            action={<Button onClick={() => detail.refetch()}>重试</Button>}
          />
        ) : (
          value && (
            <Descriptions
              column={1}
              items={[
                { key: 'no', label: '充值单号', children: value.rechargeNo },
                { key: 'member', label: '会员编号', children: value.memberId },
                { key: 'pay', label: '支付金额', children: value.payPrice },
                { key: 'bonus', label: '赠送金额', children: value.bonusPrice },
                { key: 'total', label: '到账金额', children: value.totalPrice },
                { key: 'status', label: '状态', children: rechargeStatusEnum[value.status] },
                { key: 'channel', label: '渠道', children: value.channelCode || '—' },
                { key: 'paid', label: '到账时间', children: value.paidTime || '—' },
                { key: 'refunded', label: '退款时间', children: value.refundedTime || '—' },
              ]}
            />
          )
        )}
      </Drawer>
    </PageContainer>
  );
}
