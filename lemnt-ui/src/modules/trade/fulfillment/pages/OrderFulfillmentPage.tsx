import styles from '../fulfillment.module.css';
import { useState } from 'react';
import {
  PageContainer,
  ProTable,
  ProFormText,
  ProFormTextArea,
  type ProColumns,
} from '@ant-design/pro-components';
import {
  Alert,
  App,
  Button,
  Drawer,
  Descriptions,
  Popconfirm,
  Result,
  Space,
  Spin,
  Table,
} from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { AdminModalForm } from '@/shared/components/AdminModalForm';
import { usePermission } from '@/shared/hooks/usePermission';
import { checkoutStatusEnum } from '../../checkout/constants';
import { checkoutKeys } from '../../checkout/hooks/checkoutKeys';
import { canShip, canRequestRefund, fulfillmentKeys } from '../constants';
import { getOrders, getOrder, shipOrder, receiveOrder, requestRefund } from '../api/fulfillmentApi';
import type { OrderSummary, PageQuery, ShipmentRequest, RefundCreateRequest } from '../types';
type Operation = { kind: 'ship' | 'refund'; orderNo: string; requestNo: string };
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
  const refund = useMutation({
    mutationFn: ({ no, data }: { no: string; data: RefundCreateRequest }) =>
      requestRefund(no, data),
  });
  const receive = useMutation({ mutationFn: receiveOrder });
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
        can('trade:refund:request') && canRequestRefund(row) && (
          <Button
            key="refund"
            type="link"
            onClick={() =>
              setOperation({ kind: 'refund', orderNo: row.orderNo, requestNo: crypto.randomUUID() })
            }
          >
            申请退款
          </Button>
        ),
        can('trade:fulfillment:receive') && row.status === 'SHIPPED' && (
          <Popconfirm
            key="receive"
            title="确认已核实收件人收货？此操作将完成订单。"
            onConfirm={async () => {
              try {
                await receive.mutateAsync(row.orderNo);
                message.success('已登记收货');
                await refresh();
              } catch (error) {
                message.error(String(error));
              }
            }}
          >
            <Button type="link" loading={receive.isPending} disabled={receive.isPending}>
              登记收货
            </Button>
          </Popconfirm>
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
                { title: '商品', dataIndex: 'productName' },
                { title: '单价（元）', dataIndex: 'unitPrice' },
                { title: '数量', dataIndex: 'quantity' },
                { title: '原始小计（元）', dataIndex: 'lineAmount' },
              ]}
            />
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
            label="收件人姓名"
            rules={[{ required: true, whitespace: true }]}
            fieldProps={{ maxLength: 100 }}
          />
          <ProFormText
            name="phone"
            label="联系电话"
            rules={[
              { required: true },
              { pattern: /^[+0-9() -]{5,32}$/, message: '请输入合法联系电话' },
            ]}
            fieldProps={{ maxLength: 32 }}
          />
          <ProFormTextArea
            name="address"
            label="详细地址"
            rules={[{ required: true, whitespace: true }]}
            fieldProps={{ maxLength: 500, showCount: true }}
          />
        </AdminModalForm>
      )}
      {operation?.kind === 'refund' && (
        <AdminModalForm<{ reason: string }>
          key={operation.requestNo}
          title="登记整单退款申请"
          description="仅限发货前整单申请，金额由服务端取实际支付金额。登记后禁止发货。"
          open
          onOpenChange={(open) => {
            if (!open) setOperation(undefined);
          }}
          modalProps={{
            destroyOnHidden: true,
            maskClosable: !refund.isPending,
            closable: !refund.isPending,
            keyboard: !refund.isPending,
          }}
          loading={refund.isPending}
          onFinish={async (values) => {
            try {
              await refund.mutateAsync({
                no: operation.orderNo,
                data: { requestNo: operation.requestNo, reason: values.reason },
              });
              message.success('申请已登记，尚未执行退款');
              setOperation(undefined);
              await refresh();
              return true;
            } catch (error) {
              message.error(String(error));
              return false;
            }
          }}
        >
          <Alert
            title="本功能不会执行渠道退款，也不会回补库存或返还优惠券。请在退款申请队列跟进。"
            type="warning"
            showIcon
          />
          <ProFormTextArea
            name="reason"
            label="退款申请原因"
            rules={[{ required: true, whitespace: true }]}
            fieldProps={{ maxLength: 500, showCount: true }}
          />
        </AdminModalForm>
      )}
    </PageContainer>
  );
}
