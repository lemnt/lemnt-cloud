import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Alert, Button, Drawer, Form, Input, Space, Table } from 'antd';
import { getPaymentTrend, type PaymentTrendRange } from '../api/paymentApi';

/** 按原到账日查看订单支付和钱包充值毛额。 */
export default function PaymentTrendDrawer({ onClose }: { onClose: () => void }) {
  const [range, setRange] = useState<PaymentTrendRange>();
  const trend = useQuery({
    queryKey: ['trade-payment', 'trend', range],
    queryFn: () => getPaymentTrend(range!),
    enabled: !!range,
  });

  return (
    <Drawer title="到账趋势" open onClose={onClose} width={640}>
      <Space orientation="vertical" size="large" style={{ width: '100%' }}>
        <Form<PaymentTrendRange> layout="inline" onFinish={setRange}>
          <Form.Item label="开始日期" name="from" rules={[{ required: true }]}>
            <Input type="date" />
          </Form.Item>
          <Form.Item label="结束日期" name="to" rules={[{ required: true }]}>
            <Input type="date" />
          </Form.Item>
          <Button type="primary" htmlType="submit" loading={trend.isFetching}>
            查询
          </Button>
        </Form>
        {trend.isError && (
          <Alert
            type="error"
            title="到账趋势读取失败"
            description={String(trend.error)}
            action={<Button onClick={() => void trend.refetch()}>重试</Button>}
          />
        )}
        {trend.data && (
          <Table
            rowKey="date"
            dataSource={trend.data}
            pagination={{ pageSize: 15, showSizeChanger: false }}
            columns={[
              { title: '到账日期', dataIndex: 'date' },
              { title: '成功支付笔数', dataIndex: 'payments', align: 'right' },
              { title: '订单支付毛额（元）', dataIndex: 'paid', align: 'right' },
              { title: '充值实付毛额（元）', dataIndex: 'recharged', align: 'right' },
            ]}
          />
        )}
      </Space>
    </Drawer>
  );
}
