import { useRef } from 'react';
import { ProTable, type ActionType, type ProColumns } from '@ant-design/pro-components';
import { App, Button, Popconfirm } from 'antd';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { usePermission } from '@/shared/hooks/usePermission';
import { getCouponRecords, revokeCoupon } from '../api/checkoutApi';
import { couponStatusEnum } from '../constants';
import { positiveId } from '../checkoutDecoders';
import { checkoutKeys } from '../hooks/checkoutKeys';
import type { CouponManagementRecord } from '../types';

/** 发券记录与撤回使用服务器分页和真实持券快照。 */
export default function CouponRecordsPanel() {
  const { can } = usePermission();
  const { message } = App.useApp();
  const client = useQueryClient();
  const action = useRef<ActionType>(null);
  const revoke = useMutation({
    mutationFn: ({ memberId, id }: { memberId: string; id: string }) => revokeCoupon(memberId, id),
  });
  if (!can('mall:coupon:list')) return null;
  const columns: ProColumns<CouponManagementRecord>[] = [
    { title: '券编号', dataIndex: 'id', search: false },
    { title: '会员编号', dataIndex: 'memberId' },
    { title: '模板编号', dataIndex: 'templateId' },
    { title: '券名称', dataIndex: ['terms', 'name'], search: false },
    { title: '状态', dataIndex: 'status', valueType: 'select', valueEnum: couponStatusEnum },
    { title: '生效时间', dataIndex: ['terms', 'validFrom'], search: false },
    { title: '到期时间', dataIndex: ['terms', 'validUntil'], search: false },
    { title: '使用订单', dataIndex: 'orderNo', search: false },
    { title: '核销时间', dataIndex: 'useTime', search: false },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) =>
        can('mall:coupon:remove') && (row.status === 'AVAILABLE' || row.status === 'EXPIRED')
          ? [
              <Popconfirm
                key="revoke"
                title="撤回该优惠券并归还领取名额？"
                onConfirm={async () => {
                  try {
                    await revoke.mutateAsync({ memberId: row.memberId, id: row.id });
                    message.success('已撤回');
                    action.current?.reload();
                    await client.invalidateQueries({ queryKey: checkoutKeys.templates() });
                  } catch (reason) {
                    message.error(String(reason));
                  }
                }}
              >
                <Button type="link" danger disabled={revoke.isPending}>
                  撤回
                </Button>
              </Popconfirm>,
            ]
          : [],
    },
  ];
  return (
    <ProTable<CouponManagementRecord>
      headerTitle="发券记录"
      rowKey="id"
      actionRef={action}
      columns={columns}
      scroll={{ x: 1200 }}
      pagination={{ defaultPageSize: 20, showSizeChanger: true, pageSizeOptions: [20, 50, 100] }}
      onRequestError={(reason) => message.error(String(reason))}
      request={async ({ current = 1, pageSize = 20, memberId, templateId, status }) =>
        getCouponRecords({
          pageNum: current,
          pageSize,
          memberId: memberId ? positiveId(String(memberId).trim()) : undefined,
          templateId: templateId ? positiveId(String(templateId).trim()) : undefined,
          status,
        })
      }
    />
  );
}
