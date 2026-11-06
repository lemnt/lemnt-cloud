import { Alert, Button, Table } from 'antd';
import { useQuery } from '@tanstack/react-query';
import { getOrderLogs } from '../api/fulfillmentApi';
import { fulfillmentKeys } from '../constants';
const states: Record<number, string> = {
  0: '待支付',
  10: '待发货',
  20: '待收货',
  30: '已完成',
  40: '已取消',
};

/** 与订单详情共用查询权限，显示真实操作及发生顺序。 */
export default function OrderLogPanel({ orderNo }: { orderNo: string }) {
  const logs = useQuery({
    queryKey: [...fulfillmentKeys.all, 'logs', orderNo],
    queryFn: () => getOrderLogs(orderNo),
    gcTime: 0,
  });
  if (logs.isError)
    return (
      <Alert
        type="error"
        title="订单操作记录读取失败"
        description={String(logs.error)}
        action={<Button onClick={() => logs.refetch()}>重试</Button>}
      />
    );
  return (
    <Table
      rowKey="id"
      title={() => '操作记录'}
      loading={logs.isFetching}
      dataSource={logs.data ?? []}
      pagination={false}
      columns={[
        { title: '发生时间', dataIndex: 'createTime' },
        {
          title: '操作人',
          render: (_, row) =>
            row.userType === 0
              ? '系统'
              : `${row.userType === 1 ? '会员' : '后台用户'} ${row.userId}`,
        },
        { title: '操作', dataIndex: 'content' },
        {
          title: '状态变化',
          render: (_, row) =>
            `${row.beforeStatus === null ? '—' : states[row.beforeStatus]} → ${states[row.afterStatus]}`,
        },
      ]}
    />
  );
}
