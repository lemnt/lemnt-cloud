import { useState } from 'react';

import { Alert, App, Button, Card, Descriptions, Input, Result, Space } from 'antd';
import { PageContainer } from '@ant-design/pro-components';
import { useMutation } from '@tanstack/react-query';

import { usePermission } from '@/shared/hooks/usePermission';

import { previewPickUp, verifyPickUp } from '../api/pickUpApi';
import type { PickUpOrder } from '../api/pickUpApi';

/** 店员核销台：只按核销码查询与确认，不暴露门店维护能力。 */
export function PickUpVerifyPage() {
  const { can } = usePermission();
  const { message } = App.useApp();
  const [code, setCode] = useState('');
  const [preview, setPreview] = useState<PickUpOrder>();
  const previewMutation = useMutation({ mutationFn: () => previewPickUp(code) });
  const verifyMutation = useMutation({ mutationFn: () => verifyPickUp(code) });

  if (!can('trade:fulfillment:query')) {
    return (
      <PageContainer title="自提核销">
        <Result status="403" title="没有自提核销查询权限" />
      </PageContainer>
    );
  }

  async function handlePreview() {
    try {
      setPreview(await previewMutation.mutateAsync());
    } catch (error) {
      message.error(String(error));
    }
  }

  async function handleVerify() {
    try {
      const value = await verifyMutation.mutateAsync();
      setPreview(value);
      message.success('核销成功');
    } catch (error) {
      message.error(String(error));
    }
  }

  return (
    <PageContainer title="自提核销">
      <Space orientation="vertical" size="middle" style={{ width: '100%' }}>
        <Alert
          title="请核对会员出示的8位核销码；核销后订单立即完成，不可撤销。"
          type="info"
          showIcon
        />
        <Card>
          <Space>
            <Input
              value={code}
              onChange={(event) => setCode(event.target.value.replace(/\D/g, ''))}
              placeholder="8位核销码"
              maxLength={8}
              style={{ width: 220 }}
            />
            <Button onClick={handlePreview} loading={previewMutation.isPending}>
              查询
            </Button>
            <Button
              type="primary"
              danger
              disabled={!can('trade:fulfillment:update') || preview?.status === 'COMPLETED'}
              onClick={handleVerify}
              loading={verifyMutation.isPending}
            >
              确认核销
            </Button>
          </Space>
        </Card>
        {preview && (
          <Card title={`订单 ${preview.orderNo}`}>
            <Descriptions column={1}>
              <Descriptions.Item label="订单状态">{preview.status}</Descriptions.Item>
              <Descriptions.Item label="自提门店">{preview.storeName || '-'}</Descriptions.Item>
              <Descriptions.Item label="门店地址">{preview.storeAddress || '-'}</Descriptions.Item>
              <Descriptions.Item label="门店电话">{preview.storePhone || '-'}</Descriptions.Item>
              <Descriptions.Item label="核销码">{preview.verifyCode || '-'}</Descriptions.Item>
            </Descriptions>
          </Card>
        )}
      </Space>
    </PageContainer>
  );
}
