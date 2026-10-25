import styles from '../fulfillment.module.css';
import { useState } from 'react';
import {
  PageContainer,
  ProTable,
  ProFormTextArea,
  type ProColumns,
} from '@ant-design/pro-components';
import { Alert, App, Button, Result, Space } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { AdminModalForm } from '@/shared/components/AdminModalForm';
import { usePermission } from '@/shared/hooks/usePermission';
import { fulfillmentKeys, refundStatusEnum } from '../constants';
import { getRefunds, rejectRefund } from '../api/fulfillmentApi';
import type { PageQuery, RefundRequest } from '../types';
export default function RefundRequestPage() {
  const { can } = usePermission();
  const { message } = App.useApp();
  const client = useQueryClient();
  const [query, setQuery] = useState<PageQuery>({ pageNum: 1, pageSize: 20, status: 'REQUESTED' });
  const [rejecting, setRejecting] = useState<RefundRequest>();
  const rows = useQuery({
    queryKey: fulfillmentKeys.refunds(query),
    queryFn: () => getRefunds(query),
    enabled: can('trade:refund:list'),
  });
  const reject = useMutation({
    mutationFn: ({ no, reason }: { no: string; reason: string }) => rejectRefund(no, reason),
  });
  const columns: ProColumns<RefundRequest>[] = [
    { title: '退款申请', dataIndex: 'refundNo', search: false },
    { title: '订单编号', dataIndex: 'orderNo', fieldProps: { maxLength: 40 } },
    { title: '会员', dataIndex: 'memberId', search: false },
    { title: '申请金额（元）', dataIndex: 'amount', search: false },
    { title: '状态', dataIndex: 'status', valueEnum: refundStatusEnum, initialValue: 'REQUESTED' },
    { title: '申请原因', dataIndex: 'reason', search: false },
    { title: '登记时间', dataIndex: 'requestedAt', search: false },
    { title: '驳回原因', dataIndex: 'decisionReason', search: false },
    { title: '驳回时间', dataIndex: 'decidedAt', search: false },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) =>
        can('trade:refund:reject') && row.status === 'REQUESTED' ? (
          <Button type="link" disabled={reject.isPending} onClick={() => setRejecting(row)}>
            驳回申请
          </Button>
        ) : null,
    },
  ];
  if (!can('trade:refund:list')) return <Result status="403" title="无退款申请列表权限" />;
  return (
    <PageContainer>
      <Space orientation="vertical" size="middle" className={styles.stack}>
        <Alert
          title="退款申请队列：待处理不代表已退款。当前可登记或驳回，真实渠道退款尚未接入；驳回后订单可继续发货。"
          type="warning"
          showIcon
        />
        {rows.isError && (
          <Alert
            title="退款申请加载失败"
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
      {rejecting && (
        <AdminModalForm<{ reason: string }>
          key={rejecting.refundNo}
          title="驳回退款申请"
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
