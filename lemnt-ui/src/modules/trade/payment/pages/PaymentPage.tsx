import { useState } from 'react';
import { PageContainer, ProTable, type ProColumns } from '@ant-design/pro-components';
import { Alert, Button, Descriptions, Drawer, Result, Table } from 'antd';
import { useQuery } from '@tanstack/react-query';
import { usePermission } from '@/shared/hooks/usePermission';
import * as api from '../api/paymentApi';
import type { PaymentRecord, RefundRecord, PaymentFilter, RefundFilter } from '../api/paymentApi';
const paymentStates = { WAITING: '待支付', SUCCESS: '支付成功', CLOSED: '已关闭' };
const refundStates = { WAITING: '退款处理中', SUCCESS: '退款成功', FAILED: '退款失败' };
export function PaymentPage() {
  return <TransactionPage refund={false} />;
}
export function PaymentRefundPage() {
  return <TransactionPage refund />;
}
/** 支付与逆向退款分别调用各自契约，管理页不手工制造支付或退款交易。 */
function TransactionPage({ refund }: { refund: boolean }) {
  const { can } = usePermission();
  const prefix = refund ? 'trade:payment-refund' : 'trade:payment';
  const title = refund ? '支付退单' : '支付单';
  const states = refund ? refundStates : paymentStates;
  const [filters, setFilters] = useState<
    Omit<PaymentFilter & RefundFilter, 'pageNum' | 'pageSize'>
  >({});
  const [page, setPage] = useState({ pageNum: 1, pageSize: 20 });
  const [selected, setSelected] = useState<string>();
  const rows = useQuery<{ records: (PaymentRecord | RefundRecord)[]; total: number }>({
    queryKey: ['trade-payment', refund, filters, page],
    queryFn: () =>
      refund ? api.getRefunds({ ...filters, ...page }) : api.getPayments({ ...filters, ...page }),
    enabled: can(prefix + ':list'),
  });
  const detail = useQuery<PaymentRecord | RefundRecord>({
    queryKey: ['trade-payment', refund, 'detail', selected],
    queryFn: () => (refund ? api.getRefund(selected ?? '') : api.getPayment(selected ?? '')),
    enabled: !!selected && can(prefix + ':query'),
  });
  const columns: ProColumns<PaymentRecord | RefundRecord>[] = [
    ...(refund
      ? [
          { title: '退款交易号', dataIndex: 'refundNo', search: false },
          { title: '售后单号', dataIndex: 'afterSaleNo' },
        ]
      : []),
    { title: '支付单号', dataIndex: 'paymentNo' },
    { title: '订单号', dataIndex: 'orderNo', search: !refund },
    {
      title: '渠道',
      dataIndex: 'channel',
      valueType: 'select',
      search: !refund,
      valueEnum: { ALIPAY: '支付宝', WECHAT: '微信支付' },
    },
    { title: '金额（元）', dataIndex: 'amount', search: false },
    { title: '状态', dataIndex: 'status', valueType: 'select', valueEnum: states },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) =>
        can(prefix + ':query') && (
          <Button
            type="link"
            onClick={() => setSelected('refundNo' in row ? row.refundNo : row.paymentNo)}
          >
            详情
          </Button>
        ),
    },
  ];
  if (!can(prefix + ':list')) return <Result status="403" title={`没有${title}列表权限`} />;
  const value = detail.data;
  return (
    <PageContainer title={title}>
      {rows.isError && (
        <Alert
          type="error"
          title={`${title}读取失败`}
          description={String(rows.error)}
          action={<Button onClick={() => rows.refetch()}>重试</Button>}
        />
      )}
      <ProTable
        options={{ reload: false }}
        toolBarRender={() => [
          <Button key="refresh" onClick={() => rows.refetch()} loading={rows.isFetching}>
            刷新
          </Button>,
        ]}
        rowKey={(row) => ('refundNo' in row ? row.refundNo : row.paymentNo)}
        columns={columns}
        dataSource={rows.data?.records ?? []}
        loading={rows.isFetching}
        scroll={{ x: 1000 }}
        onSubmit={(v) => {
          setFilters({
            paymentNo: typeof v.paymentNo === 'string' ? v.paymentNo : undefined,
            orderNo: typeof v.orderNo === 'string' ? v.orderNo : undefined,
            afterSaleNo: typeof v.afterSaleNo === 'string' ? v.afterSaleNo : undefined,
            status: typeof v.status === 'string' ? v.status : undefined,
            channel: typeof v.channel === 'string' ? v.channel : undefined,
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
        title={`${title}详情`}
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
                ...('refundNo' in value
                  ? [
                      { key: 'refund', label: '退款交易号', children: value.refundNo },
                      { key: 'after', label: '售后单号', children: value.afterSaleNo },
                    ]
                  : [{ key: 'id', label: '支付单ID', children: value.id }]),
                { key: 'payment', label: '支付单号', children: value.paymentNo },
                { key: 'order', label: '订单号', children: value.orderNo },
                { key: 'amount', label: '金额（元）', children: value.amount },
                { key: 'channel', label: '渠道', children: value.channel },
                {
                  key: 'status',
                  label: '状态',
                  children:
                    'refundNo' in value ? refundStates[value.status] : paymentStates[value.status],
                },
                {
                  key: 'channelNo',
                  label: '渠道交易号',
                  children:
                    ('refundNo' in value ? value.channelRefundNo : value.channelTradeNo) || '—',
                },
              ]}
            />
          )
        )}
      </Drawer>
    </PageContainer>
  );
}
export function PaymentChannelPage() {
  const { can } = usePermission();
  const rows = useQuery({
    queryKey: ['trade-payment', 'channels'],
    queryFn: api.getChannels,
    enabled: can('trade:payment-channel:list'),
  });
  if (!can('trade:payment-channel:list'))
    return <Result status="403" title="没有支付渠道查询权限" />;
  return (
    <PageContainer title="支持的支付渠道">
      {rows.isError && (
        <Alert
          type="error"
          title="渠道读取失败"
          action={<Button onClick={() => rows.refetch()}>重试</Button>}
        />
      )}
      <Table
        rowKey="code"
        loading={rows.isFetching}
        dataSource={rows.data ?? []}
        pagination={false}
        columns={[
          { title: '渠道编码', dataIndex: 'code' },
          { title: '渠道名称', dataIndex: 'name' },
        ]}
      />
    </PageContainer>
  );
}
