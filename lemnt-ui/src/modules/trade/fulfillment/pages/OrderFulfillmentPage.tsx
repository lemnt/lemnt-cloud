import styles from '../fulfillment.module.css';
import { useState } from 'react';
import OrderLogPanel from './OrderLogPanel';
import {
  PageContainer,
  ProTable,
  ProFormText,
  ProFormTextArea,
  type ProColumns,
} from '@ant-design/pro-components';
import { Alert, App, Button, Drawer, Descriptions, Result, Space, Spin, Table, Image } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { AdminModalForm } from '@/shared/components/AdminModalForm';
import { usePermission } from '@/shared/hooks/usePermission';
import { checkoutStatusEnum } from '../../../mall/checkout/constants';
import { checkoutKeys } from '../../../mall/checkout/hooks/checkoutKeys';
import { canShip, fulfillmentKeys } from '../constants';
import { getOrders, getOrder, shipOrder } from '../api/fulfillmentApi';
import type { OrderSummary, PageQuery, ShipmentRequest } from '../types';
type Operation = { kind: 'ship'; orderNo: string; requestNo: string };
export default function OrderFulfillmentPage() {
  const { can } = usePermission();
  const { message } = App.useApp();
  const client = useQueryClient();
  const [query, setQuery] = useState<PageQuery>({ pageNum: 1, pageSize: 20 });
  const [detailNo, setDetailNo] = useState<string>();
  const [operation, setOperation] = useState<Operation>();
  const rows = useQuery({
    queryKey: fulfillmentKeys.orders(query),
    queryFn: () => getOrders(query),
    enabled: can('trade:fulfillment:list'),
  });
  const detail = useQuery({
    queryKey: fulfillmentKeys.detail(detailNo),
    queryFn: () => getOrder(detailNo!),
    enabled: !!detailNo && can('trade:fulfillment:query'),
    gcTime: 0,
  });
  const ship = useMutation({
    mutationFn: ({ no, data }: { no: string; data: ShipmentRequest }) => shipOrder(no, data),
  });
  async function refresh() {
    try {
      await Promise.all([
        client.invalidateQueries({ queryKey: fulfillmentKeys.all }, { throwOnError: true }),
        client.invalidateQueries({ queryKey: checkoutKeys.all }, { throwOnError: true }),
      ]);
    } catch (error) {
      message.warning(`操作已保存，但刷新失败，请刷新核验：${String(error)}`);
    }
  }
  const columns: ProColumns<OrderSummary>[] = [
    { title: '订单编号', dataIndex: 'orderNo', fieldProps: { maxLength: 40 } },
    { title: '会员', dataIndex: 'memberId', search: false },
    { title: '应付（元）', dataIndex: 'totalAmount', search: false },
    { title: '状态', dataIndex: 'status', valueEnum: checkoutStatusEnum },
    {
      title: '库存确认',
      search: false,
      renderText: (_, row) => (row.inventoryConfirmed ? '完成' : '未完成'),
    },
    {
      title: '待处理退款',
      search: false,
      renderText: (_, row) => (row.activeRefund ? '有（禁止发货）' : '无'),
    },
    { title: '运单号', dataIndex: 'trackingNo', search: false },
    { title: '创建时间', dataIndex: 'createTime', search: false },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) => [
        can('trade:fulfillment:query') && (
          <Button key="detail" type="link" onClick={() => setDetailNo(row.orderNo)}>
            详情
          </Button>
        ),
        can('trade:fulfillment:ship') && canShip(row) && (
          <Button
            key="ship"
            type="link"
            onClick={() =>
              setOperation({ kind: 'ship', orderNo: row.orderNo, requestNo: crypto.randomUUID() })
            }
          >
            发货
          </Button>
        ),
      ],
    },
  ];
  if (!can('trade:fulfillment:list')) return <Result status="403" title="无订单列表权限" />;
  return (
    <PageContainer>
      <Space orientation="vertical" size="middle" className={styles.stack}>
        <Alert
          title="发货前需支付及库存确认完成。待处理退款申请会阻止发货；申请不代表已退款。"
          type="info"
          showIcon
        />
        {rows.isError && (
          <Alert
            type="error"
            title="订单加载失败"
            description={String(rows.error)}
            action={<Button onClick={() => void rows.refetch()}>重试</Button>}
          />
        )}
        <ProTable<OrderSummary>
          rowKey="orderNo"
          columns={columns}
          dataSource={rows.data?.records ?? []}
          loading={rows.isFetching}
          scroll={{ x: 1200 }}
          options={false}
          toolBarRender={() => [
            <Button key="refresh" loading={rows.isFetching} onClick={() => void rows.refetch()}>
              刷新
            </Button>,
          ]}
          onSubmit={(values) =>
            setQuery({
              orderNo: typeof values.orderNo === 'string' ? values.orderNo : undefined,
              status: typeof values.status === 'string' ? values.status : undefined,
              pageNum: 1,
              pageSize: query.pageSize,
            })
          }
          onReset={() => setQuery({ pageNum: 1, pageSize: query.pageSize })}
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
        title="订单详情（收件信息仅用于履约）"
        open={!!detailNo && can('trade:fulfillment:query')}
        onClose={() => setDetailNo(undefined)}
        size="large"
        destroyOnHidden
      >
        {detail.isFetching && <Spin />}
        {detail.isError && (
          <Alert
            type="error"
            title="详情加载失败"
            description={String(detail.error)}
            action={<Button onClick={() => void detail.refetch()}>重试</Button>}
          />
        )}
        {can('trade:fulfillment:query') && detail.data && (
          <>
            <Descriptions
              bordered
              column={1}
              items={[
                { key: 'order', label: '订单编号', children: detail.data.orderNo },
                {
                  key: 'state',
                  label: '状态',
                  children: checkoutStatusEnum[detail.data.status].text,
                },
                { key: 'amount', label: '应付金额', children: `${detail.data.totalAmount} 元` },
                {
                  key: 'comment',
                  label: '全部评价',
                  children:
                    detail.data.commentStatus === true
                      ? '已完成'
                      : detail.data.commentStatus === false
                        ? '未完成'
                        : '未记录',
                },
                {
                  key: 'finish',
                  label: '评价完成时间',
                  children: detail.data.finishTime ?? '未记录',
                },
                {
                  key: 'stock',
                  label: '库存确认',
                  children: detail.data.inventoryConfirmed ? '完成' : '未完成',
                },
                {
                  key: 'refund',
                  label: '待处理退款',
                  children: detail.data.activeRefund ? '有' : '无',
                },
                {
                  key: 'shipment',
                  label: '物流信息',
                  children: detail.data.shipment
                    ? `${detail.data.shipment.carrierCode} / ${detail.data.shipment.trackingNo}`
                    : '未发货',
                },
                {
                  key: 'recipient',
                  label: '收件人',
                  children: detail.data.shipment?.recipientName ?? '—',
                },
                { key: 'phone', label: '联系电话', children: detail.data.shipment?.phone ?? '—' },
                {
                  key: 'address',
                  label: '详细地址',
                  children: detail.data.shipment?.address ?? '—',
                },
                {
                  key: 'shipped',
                  label: '发货时间',
                  children: detail.data.shipment?.shippedAt ?? '—',
                },
                {
                  key: 'received',
                  label: '收货时间',
                  children: detail.data.shipment?.receivedAt ?? '—',
                },
              ]}
            />
            <Table
              rowKey="productId"
              pagination={false}
              dataSource={detail.data.items}
              columns={[
                {
                  title: '下单图片',
                  render: (_, row) =>
                    row.picUrl && /^https?:\/\//i.test(row.picUrl) ? (
                      <Image width={48} src={row.picUrl} />
                    ) : (
                      '未记录'
                    ),
                },
                { title: '订单项编号', dataIndex: 'orderItemId' },
                {
                  title: '评价状态',
                  render: (_, row) =>
                    row.commentStatus === true
                      ? '已评价'
                      : row.commentStatus === false
                        ? '未评价'
                        : '未记录',
                },
                { title: '商品', dataIndex: 'productName' },
                { title: 'SPU', render: (_, row) => row.spuName ?? '未记录' },
                {
                  title: '下单规格',
                  render: (_, row) =>
                    row.properties == null
                      ? '未记录'
                      : row.properties
                          .map((property) => `${property.propertyName}：${property.valueName}`)
                          .join('；') || '无规格',
                },
                { title: '单价（元）', dataIndex: 'unitPrice' },
                { title: '数量', dataIndex: 'quantity' },
                { title: '原始小计（元）', dataIndex: 'lineAmount' },
              ]}
            />
            <OrderLogPanel orderNo={detail.data.orderNo} />
          </>
        )}
      </Drawer>
      {operation?.kind === 'ship' && (
        <AdminModalForm<Omit<ShipmentRequest, 'requestNo'>>
          key={operation.requestNo}
          title="登记发货"
          description="物流不可改写。请求失败可保留原表单重试；若关闭或刷新，请先核验订单结果。收件信息不保存在浏览器存储中。"
          open
          onOpenChange={(open) => {
            if (!open) setOperation(undefined);
          }}
          modalProps={{
            destroyOnHidden: true,
            maskClosable: !ship.isPending,
            closable: !ship.isPending,
            keyboard: !ship.isPending,
          }}
          loading={ship.isPending}
          onFinish={async (values) => {
            try {
              await ship.mutateAsync({
                no: operation.orderNo,
                data: { ...values, requestNo: operation.requestNo },
              });
              message.success('发货已登记');
              setOperation(undefined);
              await refresh();
              return true;
            } catch (error) {
              message.error(String(error));
              return false;
            }
          }}
        >
          <ProFormText
            name="carrierCode"
            label="承运商编码"
            rules={[{ required: true, whitespace: true }]}
            fieldProps={{ maxLength: 40 }}
          />
          <ProFormText
            name="trackingNo"
            label="运单编号"
            rules={[{ required: true, whitespace: true }]}
            fieldProps={{ maxLength: 100 }}
          />
          <ProFormText
            name="recipientName"
            label="历史订单补填收件人（新订单使用下单快照）"
            rules={[{ whitespace: true }]}
            fieldProps={{ maxLength: 100 }}
          />
          <ProFormText
            name="phone"
            label="联系电话"
            rules={[{ pattern: /^[+0-9() -]{5,32}$/, message: '请输入合法联系电话' }]}
            fieldProps={{ maxLength: 32 }}
          />
          <ProFormTextArea
            name="address"
            label="详细地址"
            rules={[{ whitespace: true }]}
            fieldProps={{ maxLength: 500, showCount: true }}
          />
        </AdminModalForm>
      )}
    </PageContainer>
  );
}
