import { useState } from 'react';
import {
  PageContainer,
  ProTable,
  ProFormText,
  ProFormTextArea,
  type ProColumns,
} from '@ant-design/pro-components';
import { Alert, App, Button, Descriptions, Drawer, Result, Table } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { AdminModalForm } from '@/shared/components/AdminModalForm';
import { usePermission } from '@/shared/hooks/usePermission';
import * as api from '../api/walletApi';

function newRequestNo() {
  return 'ADJ-' + Date.now().toString(36) + '-' + Math.random().toString(36).slice(2, 10);
}

/** 会员钱包：查看余额与流水，人工调整以 requestNo 幂等。 */
export function WalletPage() {
  const { can } = usePermission(),
    { message } = App.useApp(),
    cache = useQueryClient();
  const prefix = 'trade:wallet';
  const [filters, setFilters] = useState<{ memberId?: string }>({});
  const [page, setPage] = useState({ pageNum: 1, pageSize: 20 });
  const [detailMember, setDetailMember] = useState<string>();
  const [adjustMember, setAdjustMember] = useState<string>();
  const rows = useQuery({
    queryKey: [prefix, filters, page],
    queryFn: () => api.getWallets({ ...filters, ...page }),
    enabled: can(prefix + ':list'),
  });
  const detail = useQuery({
    queryKey: [prefix, 'detail', detailMember],
    queryFn: () => api.getWallet(detailMember ?? ''),
    enabled: !!detailMember && can(prefix + ':query'),
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
  });
  const transactions = useQuery({
    queryKey: [prefix, 'transactions', detailMember],
    queryFn: () => api.getWalletTransactions(detailMember ?? '', { pageNum: 1, pageSize: 50 }),
    enabled: !!detailMember && can(prefix + ':query'),
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
  });
  const adjust = useMutation({
    mutationFn: (values: api.WalletAdjustValues) => api.adjustWallet(adjustMember ?? '', values),
  });
  async function refresh() {
    try {
      await cache.invalidateQueries({ queryKey: [prefix] }, { throwOnError: true });
    } catch {
      message.warning('操作已成功，刷新失败请重试');
    }
  }
  const columns: ProColumns<api.Wallet>[] = [
    { title: '钱包编号', dataIndex: 'id', search: false },
    { title: '会员编号', dataIndex: 'memberId' },
    { title: '可用余额', dataIndex: 'balance', search: false },
    { title: '冻结金额', dataIndex: 'freezePrice', search: false },
    { title: '累计支出', dataIndex: 'totalExpense', search: false },
    { title: '累计充值', dataIndex: 'totalRecharge', search: false },
    { title: '创建时间', dataIndex: 'createTime', search: false },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) => (
        <>
          {can(prefix + ':query') && (
            <Button type="link" onClick={() => setDetailMember(row.memberId)}>
              详情
            </Button>
          )}
          {can(prefix + ':adjust') && (
            <Button type="link" onClick={() => setAdjustMember(row.memberId)}>
              调整余额
            </Button>
          )}
        </>
      ),
    },
  ];
  if (!can(prefix + ':list')) return <Result status="403" title="没有钱包列表权限" />;
  const value = detail.data;
  return (
    <PageContainer title="会员钱包">
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
        ]}
        onSubmit={(v) => {
          setFilters({ memberId: typeof v.memberId === 'string' ? v.memberId : undefined });
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
        open={!!detailMember}
        title="钱包详情"
        onClose={() => setDetailMember(undefined)}
        width={760}
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
            <>
              <Descriptions
                column={2}
                items={[
                  { key: 'member', label: '会员编号', children: value.memberId },
                  { key: 'balance', label: '可用余额', children: value.balance },
                  { key: 'freeze', label: '冻结金额', children: value.freezePrice },
                  { key: 'expense', label: '累计支出', children: value.totalExpense },
                  { key: 'recharge', label: '累计充值', children: value.totalRecharge },
                ]}
              />
              <Table
                rowKey={(row) => row.transactionNo}
                size="small"
                loading={transactions.isFetching}
                dataSource={transactions.data?.records ?? []}
                pagination={false}
                scroll={{ y: 360 }}
                columns={[
                  { title: '流水号', dataIndex: 'transactionNo', ellipsis: true },
                  { title: '类型', dataIndex: 'bizType' },
                  { title: '业务单号', dataIndex: 'bizNo', ellipsis: true },
                  { title: '金额', dataIndex: 'amount' },
                  { title: '交易后余额', dataIndex: 'balanceAfter' },
                  { title: '时间', dataIndex: 'createTime' },
                ]}
              />
            </>
          )
        )}
      </Drawer>
      {adjustMember && (
        <AdminModalForm<api.WalletAdjustValues>
          key={adjustMember}
          title={`调整会员 ${adjustMember} 钱包余额`}
          open
          initialValues={{ requestNo: newRequestNo(), amount: '', reason: '' }}
          submitter={{ submitButtonProps: { loading: adjust.isPending } }}
          modalProps={{ maskClosable: false, closable: !adjust.isPending }}
          onOpenChange={(open) => {
            if (!open && !adjust.isPending) setAdjustMember(undefined);
          }}
          onFinish={async (values) => {
            if (adjust.isPending || !can(prefix + ':adjust')) return false;
            try {
              await adjust.mutateAsync(values);
              message.success('已调整余额');
              setAdjustMember(undefined);
              await refresh();
              return true;
            } catch (e) {
              message.error(String(e));
              return false;
            }
          }}
        >
          <ProFormText
            name="requestNo"
            label="请求编号"
            rules={[{ required: true, whitespace: true, max: 64 }]}
            extra="用于幂等，重复提交相同编号不会重复记账"
          />
          <ProFormText
            name="amount"
            label="调整金额（元）"
            rules={[
              { required: true, whitespace: true },
              { pattern: /^-?\d{1,9}(\.\d{1,2})?$/, message: '最多两位小数' },
            ]}
            extra="正数增加余额，负数扣减；不能为0"
          />
          <ProFormTextArea
            name="reason"
            label="调整原因"
            rules={[{ required: true, whitespace: true, max: 128 }]}
            fieldProps={{ maxLength: 128, showCount: true }}
          />
        </AdminModalForm>
      )}
    </PageContainer>
  );
}

/** 钱包转账记录：只读查询转出/转入与金额。 */
export function WalletTransferPage() {
  const { can } = usePermission();
  const prefix = 'trade:wallet-transfer';
  const [filters, setFilters] = useState<{ fromMemberId?: string; toMemberId?: string }>({});
  const [page, setPage] = useState({ pageNum: 1, pageSize: 20 });
  const [selected, setSelected] = useState<string>();
  const rows = useQuery({
    queryKey: [prefix, filters, page],
    queryFn: () => api.getWalletTransfers({ ...filters, ...page }),
    enabled: can(prefix + ':list'),
  });
  const detail = useQuery({
    queryKey: [prefix, 'detail', selected],
    queryFn: () => api.getWalletTransfer(selected ?? ''),
    enabled: !!selected && can(prefix + ':query'),
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
  });
  const columns: ProColumns<api.WalletTransfer>[] = [
    { title: '转账单号', dataIndex: 'transferNo', search: false },
    { title: '转出会员', dataIndex: 'fromMemberId' },
    { title: '转入会员', dataIndex: 'toMemberId' },
    { title: '金额（元）', dataIndex: 'amount', search: false },
    { title: '备注', dataIndex: 'remark', search: false, ellipsis: true },
    { title: '状态', dataIndex: 'status', search: false },
    { title: '创建时间', dataIndex: 'createTime', search: false },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) =>
        can(prefix + ':query') && (
          <Button type="link" onClick={() => setSelected(row.transferNo)}>
            详情
          </Button>
        ),
    },
  ];
  if (!can(prefix + ':list')) return <Result status="403" title="没有钱包转账列表权限" />;
  const value = detail.data;
  return (
    <PageContainer title="钱包转账">
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
        ]}
        onSubmit={(v) => {
          setFilters({
            fromMemberId: typeof v.fromMemberId === 'string' ? v.fromMemberId : undefined,
            toMemberId: typeof v.toMemberId === 'string' ? v.toMemberId : undefined,
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
      <Drawer open={!!selected} title="转账详情" onClose={() => setSelected(undefined)} width={600}>
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
                { key: 'no', label: '转账单号', children: value.transferNo },
                { key: 'from', label: '转出会员', children: value.fromMemberId },
                { key: 'to', label: '转入会员', children: value.toMemberId },
                { key: 'amount', label: '金额（元）', children: value.amount },
                { key: 'remark', label: '备注', children: value.remark || '—' },
                { key: 'status', label: '状态', children: '成功' },
                { key: 'time', label: '创建时间', children: value.createTime || '—' },
              ]}
            />
          )
        )}
      </Drawer>
    </PageContainer>
  );
}
