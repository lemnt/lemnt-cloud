import styles from '../fulfillment.module.css';
import { useState } from 'react';
import AfterSaleDetailPanel from './AfterSaleDetailPanel';
import {
  PageContainer,
  ProTable,
  ProFormTextArea,
  type ProColumns,
} from '@ant-design/pro-components';
import { Alert, App, Button, Result, Space, Drawer, Popconfirm } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { AdminModalForm } from '@/shared/components/AdminModalForm';
import { usePermission } from '@/shared/hooks/usePermission';
import { fulfillmentKeys, refundStatusEnum } from '../constants';
import {
  getRefunds,
  rejectRefund,
  agreeAfterSale,
  receiveAfterSale,
  refuseAfterSale,
  refundAfterSale,
} from '../api/fulfillmentApi';
import type { PageQuery, RefundRequest } from '../types';
export default function RefundRequestPage() {
  const { can } = usePermission();
  const { message } = App.useApp();
  const client = useQueryClient();
  const [query, setQuery] = useState<PageQuery>({ pageNum: 1, pageSize: 20, status: 'REQUESTED' });
  const [detailNo, setDetailNo] = useState<string>();
  const [refusing, setRefusing] = useState<RefundRequest>();
  const [rejecting, setRejecting] = useState<RefundRequest>();
  const rows = useQuery({
    queryKey: fulfillmentKeys.refunds(query),
    queryFn: () => getRefunds(query),
    enabled: can('trade:after-sale:list'),
  });
  const reject = useMutation({
    mutationFn: ({ no, reason }: { no: string; reason: string }) => rejectRefund(no, reason),
  });
  const operate = useMutation({
    mutationFn: async ({ no, kind }: { no: string; kind: 'agree' | 'receive' | 'refund' }) => {
      if (kind === 'agree') return { kind, result: await agreeAfterSale(no) };
      if (kind === 'receive') return { kind, result: await receiveAfterSale(no) };
      return { kind, result: await refundAfterSale(no) };
    },
  });
  const refuse = useMutation({
    mutationFn: ({ no, reason }: { no: string; reason: string }) => refuseAfterSale(no, reason),
  });
  async function act(no: string, kind: 'agree' | 'receive' | 'refund') {
    try {
      const value = await operate.mutateAsync({ no, kind });
      if (value.kind === 'refund' && 'status' in value.result) {
        if (value.result.status === 'SUCCESS') message.success('渠道已确认退款成功');
        else if (value.result.status === 'FAILED') message.error('退款失败，售后尚未完成');
        else message.info('退款已受理，等待渠道结果');
      } else message.success('操作已保存');
      await client.invalidateQueries({ queryKey: fulfillmentKeys.all });
    } catch (error) {
      message.error(String(error));
    }
  }
  const columns: ProColumns<RefundRequest>[] = [
    { title: '商品售后', dataIndex: 'refundNo', search: false },
    { title: '订单编号', dataIndex: 'orderNo', fieldProps: { maxLength: 40 } },
    { title: '会员', dataIndex: 'memberId', search: false },
    { title: '申请金额（元）', dataIndex: 'amount', search: false },
    { title: '状态', dataIndex: 'status', valueEnum: refundStatusEnum, initialValue: 'REQUESTED' },
    { title: '申请原因', dataIndex: 'reason', search: false },
    { title: '登记时间', dataIndex: 'requestedAt', search: false },
    { title: '驳回原因', dataIndex: 'decisionReason', search: false },
    { title: '处理时间', dataIndex: 'decidedAt', search: false },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) => [
        can('trade:after-sale:query') && (
          <Button key="detail" type="link" onClick={() => setDetailNo(row.refundNo)}>
            详情
          </Button>
        ),
        can('trade:after-sale:agree') && row.status === 'REQUESTED' && (
          <Popconfirm
            key="agree"
            title="同意该售后申请？"
            onConfirm={() => act(row.refundNo, 'agree')}
          >
            <Button type="link" disabled={operate.isPending}>
              同意
            </Button>
          </Popconfirm>
        ),
        can('trade:after-sale:reject') && row.status === 'REQUESTED' && (
          <Button
            key="reject"
            type="link"
            disabled={reject.isPending}
            onClick={() => setRejecting(row)}
          >
            驳回申请
          </Button>
        ),
        can('trade:after-sale:receive') && row.status === 'RETURNED' && (
          <Popconfirm
            key="receive"
            title="确认已收到退货商品？"
            onConfirm={() => act(row.refundNo, 'receive')}
          >
            <Button type="link" disabled={operate.isPending}>
              确认收货
            </Button>
          </Popconfirm>
        ),
        can('trade:after-sale:refuse') && row.status === 'RETURNED' && (
          <Button key="refuse" type="link" onClick={() => setRefusing(row)}>
            拒收
          </Button>
        ),
        can('trade:after-sale:refund') &&
          (row.status === 'WAIT_REFUND' || row.status === 'REFUNDING') && (
            <Popconfirm
              key="refund"
              title="按原支付交易发起或核验退款？"
              onConfirm={() => act(row.refundNo, 'refund')}
            >
              <Button type="link" disabled={operate.isPending}>
                {row.status === 'REFUNDING' ? '核验退款' : '确认退款'}
              </Button>
            </Popconfirm>
          ),
      ],
    },
  ];
  if (!can('trade:after-sale:list')) return <Result status="403" title="无商品售后列表权限" />;
  return (
    <PageContainer>
      <Space orientation="vertical" size="middle" className={styles.stack}>
        <Alert
          title="售后申请待处理不代表已退款；发起退款后以支付退款交易结果为准。退款处理中禁止驳回和发货，驳回后订单可继续发货。"
          type="warning"
          showIcon
        />
        {rows.isError && (
          <Alert
            title="商品售后加载失败"
            description={String(rows.error)}
            type="error"
            action={<Button onClick={() => void rows.refetch()}>重试</Button>}
          />
        )}
        <ProTable<RefundRequest>
          rowKey="refundNo"
          columns={columns}
          dataSource={rows.data?.records ?? []}
          loading={rows.isFetching}
          options={false}
          toolBarRender={() => [
            <Button key="refresh" loading={rows.isFetching} onClick={() => void rows.refetch()}>
              刷新
            </Button>,
          ]}
          scroll={{ x: 1200 }}
          onSubmit={(values) =>
            setQuery({
              orderNo: typeof values.orderNo === 'string' ? values.orderNo : undefined,
              status: typeof values.status === 'string' ? values.status : undefined,
              pageNum: 1,
              pageSize: query.pageSize,
            })
          }
          onReset={() => setQuery({ pageNum: 1, pageSize: 20, status: 'REQUESTED' })}
          pagination={{
            current: query.pageNum,
            pageSize: query.pageSize,
            total: rows.data?.total ?? 0,
            showSizeChanger: true,
            pageSizeOptions: [20, 50, 100],
            onChange: (pageNum, pageSize) => setQuery({ ...query, pageNum, pageSize }),
          }}
        />
      </Space>
      <Drawer
        title="售后详情"
        open={!!detailNo}
        width={960}
        onClose={() => setDetailNo(undefined)}
        destroyOnHidden
      >
        {detailNo && can('trade:after-sale:query') && <AfterSaleDetailPanel no={detailNo} />}
      </Drawer>
      {refusing && (
        <AdminModalForm<{ reason: string }>
          title="拒收退货"
          open
          onOpenChange={(open) => {
            if (!open) setRefusing(undefined);
          }}
          modalProps={{ destroyOnHidden: true }}
          onFinish={async (values) => {
            try {
              await refuse.mutateAsync({ no: refusing.refundNo, reason: values.reason });
              message.success('已拒收');
              setRefusing(undefined);
              await client.invalidateQueries({ queryKey: fulfillmentKeys.all });
              return true;
            } catch (error) {
              message.error(String(error));
              return false;
            }
          }}
          submitter={{ submitButtonProps: { loading: refuse.isPending } }}
        >
          <ProFormTextArea
            name="reason"
            label="拒收原因"
            rules={[{ required: true, whitespace: true }]}
            fieldProps={{ maxLength: 500 }}
          />
        </AdminModalForm>
      )}
      {rejecting && (
        <AdminModalForm<{ reason: string }>
          key={rejecting.refundNo}
          title="驳回商品售后"
          description="请先核验申请。驳回会解除发货阻止；不会执行退款。相同原因重复提交返回原审核记录。"
          open
          onOpenChange={(open) => {
            if (!open) setRejecting(undefined);
          }}
          loading={reject.isPending}
          modalProps={{
            destroyOnHidden: true,
            maskClosable: !reject.isPending,
            closable: !reject.isPending,
            keyboard: !reject.isPending,
          }}
          onFinish={async (values) => {
            try {
              await reject.mutateAsync({ no: rejecting.refundNo, reason: values.reason });
              message.success('申请已驳回');
              setRejecting(undefined);
            } catch (error) {
              message.error(String(error));
              return false;
            }
            try {
              await client.invalidateQueries(
                { queryKey: fulfillmentKeys.all },
                { throwOnError: true },
              );
            } catch (error) {
              message.warning(`驳回已保存，刷新失败：${String(error)}`);
            }
            return true;
          }}
        >
          <ProFormTextArea
            name="reason"
            label="驳回原因"
            rules={[{ required: true, whitespace: true }]}
            fieldProps={{ maxLength: 500, showCount: true }}
          />
        </AdminModalForm>
      )}
    </PageContainer>
  );
}
