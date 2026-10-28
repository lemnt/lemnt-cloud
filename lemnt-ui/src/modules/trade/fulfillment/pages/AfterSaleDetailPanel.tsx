import { Alert, Button, Descriptions, Image, Spin, Table } from 'antd';
import { useQuery } from '@tanstack/react-query';
import { getAfterSale, getAfterSaleLogs } from '../api/fulfillmentApi';
import { fulfillmentKeys, refundStatusEnum } from '../constants';
const states: Record<number, string> = {
  10: '申请中',
  20: '同意退货',
  30: '买家已寄回',
  40: '等待退款',
  50: '已退款',
  61: '会员取消',
  62: '拒绝申请',
  63: '拒收退货',
};
/** 详情与日志独立加载，失败不展示虚构的成功状态。 */
export default function AfterSaleDetailPanel({ no }: { no: string }) {
  const detail = useQuery({
    queryKey: [...fulfillmentKeys.all, 'after-sale', no],
    queryFn: () => getAfterSale(no),
    gcTime: 0,
  });
  const logs = useQuery({
    queryKey: [...fulfillmentKeys.all, 'after-sale-logs', no],
    queryFn: () => getAfterSaleLogs(no),
    gcTime: 0,
  });
  return (
    <>
      {detail.isLoading && <Spin />}
      {detail.isError && (
        <Alert
          type="error"
          title="售后详情读取失败"
          description={String(detail.error)}
          action={<Button onClick={() => detail.refetch()}>重试</Button>}
        />
      )}
      {detail.data && (
        <Descriptions
          column={1}
          bordered
          items={[
            { key: 'no', label: '售后单号', children: detail.data.refundNo },
            { key: 'order', label: '订单编号', children: detail.data.orderNo },
            { key: 'member', label: '会员编号', children: detail.data.memberId },
            { key: 'amount', label: '退款金额', children: `${detail.data.amount} 元` },
            {
              key: 'state',
              label: '当前状态',
              children: refundStatusEnum[detail.data.status].text,
            },
            {
              key: 'way',
              label: '售后方式',
              children:
                detail.data.type === 'RETURN_REFUND'
                  ? '退货退款'
                  : detail.data.type === 'REFUND_ONLY'
                    ? '仅退款'
                    : '—',
            },
            { key: 'spu', label: '下单商品', children: detail.data.spuName ?? '未记录' },
            { key: 'sku', label: 'SKU 编号', children: detail.data.skuId ?? '未记录' },
            {
              key: 'spec',
              label: '下单规格',
              children:
                detail.data.properties == null
                  ? '未记录'
                  : Object.entries(detail.data.properties)
                      .map(([name, value]) => `${name}：${value}`)
                      .join('；') || '无规格',
            },
            {
              key: 'pic',
              label: '下单图片',
              children:
                detail.data.picUrl && /^https?:\/\//i.test(detail.data.picUrl) ? (
                  <Image width={64} src={detail.data.picUrl} />
                ) : (
                  '未记录'
                ),
            },
            { key: 'quantity', label: '商品数量', children: detail.data.quantity ?? '—' },
            { key: 'reason', label: '申请原因', children: detail.data.reason },
            { key: 'decision', label: '处理原因', children: detail.data.decisionReason ?? '—' },
            { key: 'carrier', label: '退货承运商', children: detail.data.returnCarrierCode ?? '—' },
            { key: 'tracking', label: '退货运单', children: detail.data.returnTrackingNo ?? '—' },
            { key: 'shipped', label: '寄回时间', children: detail.data.returnShippedAt ?? '—' },
            {
              key: 'received',
              label: '收到退货时间',
              children: detail.data.returnReceivedAt ?? '—',
            },
            { key: 'refund', label: '退款成功时间', children: detail.data.refundTime ?? '—' },
          ]}
        />
      )}
      {logs.isError ? (
        <Alert
          type="error"
          title="售后日志读取失败"
          action={<Button onClick={() => logs.refetch()}>重试</Button>}
        />
      ) : (
        <Table
          rowKey="id"
          title={() => '操作记录'}
          loading={logs.isFetching}
          dataSource={logs.data ?? []}
          pagination={false}
          columns={[
            { title: '时间', dataIndex: 'createTime' },
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
                `${row.beforeStatus == null ? '—' : states[row.beforeStatus]} → ${states[row.afterStatus]}`,
            },
          ]}
        />
      )}
    </>
  );
}
