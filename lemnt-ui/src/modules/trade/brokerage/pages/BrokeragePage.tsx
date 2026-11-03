import { useState } from 'react';
import { PageContainer, ProTable, type ProColumns } from '@ant-design/pro-components';
import { Alert, App, Button, Input, Modal, Popconfirm, Result, Space, Tabs } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { usePermission } from '@/shared/hooks/usePermission';
import * as api from '../api/brokerageApi';
import type { BrokerageRecord, BrokerageUser, BrokerageWithdraw } from '../api/brokerageApi';

const BIZ = { 1: '推广佣金', 2: '提现扣减', 3: '提现驳回' };
const STATUS = { 0: '待结算', 1: '已结算', 2: '已取消' };
const WITHDRAW_STATUS = {
  0: '审核中',
  10: '审核通过',
  11: '提现成功',
  20: '审核不通过',
  21: '提现失败',
};
const WITHDRAW_TYPE = { 1: '钱包', 2: '银行卡', 3: '微信收款码', 4: '支付宝收款码' };

/** 后台分销用户、佣金记录与提现审核。 */
export function BrokeragePage() {
  const { can } = usePermission();
  const { message } = App.useApp();
  const cache = useQueryClient();
  const [userPage, setUserPage] = useState({ pageNum: 1, pageSize: 20 });
  const [userFilter, setUserFilter] = useState<{ bindUserId?: string; brokerageEnabled?: boolean }>(
    {},
  );
  const [recordPage, setRecordPage] = useState({ pageNum: 1, pageSize: 20 });
  const [recordFilter, setRecordFilter] = useState<{
    userId?: string;
    bizType?: number;
    status?: number;
  }>({});
  const [withdrawPage, setWithdrawPage] = useState({ pageNum: 1, pageSize: 20 });
  const [withdrawFilter, setWithdrawFilter] = useState<{ userId?: string; status?: number }>({});
  const [bind, setBind] = useState<{ userId: string; value: string }>();
  const [audit, setAudit] = useState<{ id: string; approved: boolean; reason: string }>();

  const users = useQuery({
    queryKey: ['brokerage', 'users', userFilter, userPage],
    queryFn: () => api.getUsers({ ...userFilter, ...userPage }),
    enabled: can('trade:brokerage:list'),
  });
  const records = useQuery({
    queryKey: ['brokerage', 'records', recordFilter, recordPage],
    queryFn: () => api.getRecords({ ...recordFilter, ...recordPage }),
    enabled: can('trade:brokerage:list'),
  });
  const withdrawals = useQuery({
    queryKey: ['brokerage', 'withdraws', withdrawFilter, withdrawPage],
    queryFn: () => api.getWithdraws({ ...withdrawFilter, ...withdrawPage }),
    enabled: can('trade:brokerage:list'),
  });
  const enabled = useMutation({
    mutationFn: ({ id, value }: { id: string; value: boolean }) => api.setEnabled(id, value),
  });
  const bindMutation = useMutation({ mutationFn: () => api.updateBind(bind!.userId, bind!.value) });
  const auditMutation = useMutation({
    mutationFn: () => api.auditWithdraw(audit!.id, audit!.approved, audit!.reason),
  });
  const unfreeze = useMutation({ mutationFn: api.unfreeze });
  async function refreshAll() {
    await Promise.all([
      cache.invalidateQueries({ queryKey: ['brokerage', 'users'] }),
      cache.invalidateQueries({ queryKey: ['brokerage', 'records'] }),
      cache.invalidateQueries({ queryKey: ['brokerage', 'withdraws'] }),
    ]);
  }
  if (!can('trade:brokerage:list')) {
    return (
      <PageContainer title="分销管理">
        <Result status="403" title="没有分销管理权限" />
      </PageContainer>
    );
  }
  const userColumns: ProColumns<BrokerageUser>[] = [
    { title: '会员编号', dataIndex: 'id', search: false },
    {
      title: '推广员',
      dataIndex: 'bindNickname',
      search: false,
      render: (_, row) => row.bindNickname || row.bindUserId || '—',
    },
    {
      title: '分销资格',
      dataIndex: 'brokerageEnabled',
      valueType: 'select',
      valueEnum: { true: '已开通', false: '未开通' },
      search: false,
    },
    { title: '可用佣金', dataIndex: 'brokeragePrice', search: false },
    { title: '冻结佣金', dataIndex: 'frozenPrice', search: false },
    {
      title: '一级/二级',
      dataIndex: 'firstLevelCount',
      search: false,
      render: (_, row) => `${row.firstLevelCount}/${row.secondLevelCount}`,
    },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) => [
        can('trade:brokerage:update') && (
          <Popconfirm
            key="enable"
            title={row.brokerageEnabled ? '确认取消分销资格？' : '确认开通分销资格？'}
            onConfirm={async () => {
              try {
                await enabled.mutateAsync({ id: row.id, value: !row.brokerageEnabled });
                message.success('已更新');
                await refreshAll();
              } catch (error) {
                message.error(String(error));
              }
            }}
          >
            <Button type="link">{row.brokerageEnabled ? '取消资格' : '开通资格'}</Button>
          </Popconfirm>
        ),
        can('trade:brokerage:update') && (
          <Button
            key="bind"
            type="link"
            onClick={() => setBind({ userId: row.id, value: row.bindUserId || '' })}
          >
            改推广员
          </Button>
        ),
      ],
    },
  ];
  const recordColumns: ProColumns<BrokerageRecord>[] = [
    { title: '会员编号', dataIndex: 'userId' },
    { title: '业务', dataIndex: 'bizType', valueType: 'select', valueEnum: BIZ },
    { title: '业务编号', dataIndex: 'bizId', search: false },
    { title: '标题', dataIndex: 'title', search: false },
    { title: '佣金', dataIndex: 'price', search: false },
    { title: '当前总佣金', dataIndex: 'totalPrice', search: false },
    { title: '状态', dataIndex: 'status', valueType: 'select', valueEnum: STATUS, search: false },
    { title: '解冻时间', dataIndex: 'unfreezeTime', search: false },
    { title: '创建时间', dataIndex: 'createTime', search: false },
  ];
  const withdrawColumns: ProColumns<BrokerageWithdraw>[] = [
    { title: '会员编号', dataIndex: 'userId' },
    { title: '金额', dataIndex: 'price', search: false },
    { title: '手续费', dataIndex: 'feePrice', search: false },
    {
      title: '方式',
      dataIndex: 'type',
      valueType: 'select',
      valueEnum: WITHDRAW_TYPE,
      search: false,
    },
    { title: '收款人', dataIndex: 'userName', search: false },
    { title: '收款账号', dataIndex: 'userAccount', search: false },
    { title: '状态', dataIndex: 'status', valueType: 'select', valueEnum: WITHDRAW_STATUS },
    { title: '审核说明', dataIndex: 'auditReason', search: false },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) =>
        row.status === 0 && can('trade:brokerage:audit')
          ? [
              <Button
                key="pass"
                type="link"
                onClick={() => setAudit({ id: row.id, approved: true, reason: '' })}
              >
                通过
              </Button>,
              <Button
                key="reject"
                type="link"
                danger
                onClick={() => setAudit({ id: row.id, approved: false, reason: '' })}
              >
                驳回
              </Button>,
            ]
          : [],
    },
  ];
  return (
    <PageContainer
      title="分销管理"
      extra={[
        can('trade:brokerage:update') && (
          <Button
            key="unfreeze"
            onClick={async () => {
              try {
                const count = await unfreeze.mutateAsync();
                message.success(`已解冻 ${count} 条`);
                await refreshAll();
              } catch (error) {
                message.error(String(error));
              }
            }}
            loading={unfreeze.isPending}
          >
            解冻到期佣金
          </Button>
        ),
      ]}
    >
      {users.isError && (
        <Alert type="error" title="分销用户读取失败" description={String(users.error)} />
      )}
      <Tabs
        items={[
          {
            key: 'users',
            label: '分销用户',
            children: (
              <ProTable<BrokerageUser>
                rowKey="id"
                options={{ reload: false }}
                columns={userColumns}
                dataSource={users.data?.records ?? []}
                loading={users.isFetching}
                onSubmit={(value) => {
                  setUserFilter({
                    bindUserId: typeof value.id === 'string' ? value.id : undefined,
                    brokerageEnabled:
                      value.brokerageEnabled === 'true' || value.brokerageEnabled === true,
                  });
                  setUserPage({ ...userPage, pageNum: 1 });
                }}
                onReset={() => {
                  setUserFilter({});
                  setUserPage({ ...userPage, pageNum: 1 });
                }}
                pagination={{
                  current: userPage.pageNum,
                  pageSize: userPage.pageSize,
                  total: users.data?.total ?? 0,
                  onChange: (pageNum, pageSize) => setUserPage({ pageNum, pageSize }),
                }}
              />
            ),
          },
          {
            key: 'records',
            label: '佣金记录',
            children: (
              <ProTable<BrokerageRecord>
                rowKey="id"
                options={{ reload: false }}
                columns={recordColumns}
                dataSource={records.data?.records ?? []}
                loading={records.isFetching}
                onSubmit={(value) => {
                  setRecordFilter({
                    userId: typeof value.userId === 'string' ? value.userId : undefined,
                    bizType: typeof value.bizType === 'number' ? value.bizType : undefined,
                    status: typeof value.status === 'number' ? value.status : undefined,
                  });
                  setRecordPage({ ...recordPage, pageNum: 1 });
                }}
                onReset={() => {
                  setRecordFilter({});
                  setRecordPage({ ...recordPage, pageNum: 1 });
                }}
                pagination={{
                  current: recordPage.pageNum,
                  pageSize: recordPage.pageSize,
                  total: records.data?.total ?? 0,
                  onChange: (pageNum, pageSize) => setRecordPage({ pageNum, pageSize }),
                }}
              />
            ),
          },
          {
            key: 'withdraws',
            label: '提现审核',
            children: (
              <ProTable<BrokerageWithdraw>
                rowKey="id"
                options={{ reload: false }}
                columns={withdrawColumns}
                dataSource={withdrawals.data?.records ?? []}
                loading={withdrawals.isFetching}
                onSubmit={(value) => {
                  setWithdrawFilter({
                    userId: typeof value.userId === 'string' ? value.userId : undefined,
                    status: typeof value.status === 'number' ? value.status : undefined,
                  });
                  setWithdrawPage({ ...withdrawPage, pageNum: 1 });
                }}
                onReset={() => {
                  setWithdrawFilter({});
                  setWithdrawPage({ ...withdrawPage, pageNum: 1 });
                }}
                pagination={{
                  current: withdrawPage.pageNum,
                  pageSize: withdrawPage.pageSize,
                  total: withdrawals.data?.total ?? 0,
                  onChange: (pageNum, pageSize) => setWithdrawPage({ pageNum, pageSize }),
                }}
              />
            ),
          },
        ]}
      />
      <Modal
        open={!!bind}
        title="修改推广员"
        confirmLoading={bindMutation.isPending}
        onCancel={() => setBind(undefined)}
        onOk={async () => {
          try {
            await bindMutation.mutateAsync();
            message.success('已保存');
            setBind(undefined);
            await refreshAll();
          } catch (error) {
            message.error(String(error));
          }
        }}
      >
        <Space direction="vertical" style={{ width: '100%' }}>
          <div>会员 {bind?.userId}，留空表示清除推广员。</div>
          <Input
            value={bind?.value ?? ''}
            placeholder="推广员会员编号"
            onChange={(event) => bind && setBind({ ...bind, value: event.target.value })}
          />
        </Space>
      </Modal>
      <Modal
        open={!!audit}
        title={audit?.approved ? '通过提现' : '驳回提现'}
        confirmLoading={auditMutation.isPending}
        onCancel={() => setAudit(undefined)}
        onOk={async () => {
          try {
            await auditMutation.mutateAsync();
            message.success('已处理');
            setAudit(undefined);
            await refreshAll();
          } catch (error) {
            message.error(String(error));
          }
        }}
      >
        <div>提现单 {audit?.id}</div>
        <Input.TextArea
          rows={3}
          value={audit?.reason ?? ''}
          placeholder="审核说明（可选）"
          onChange={(event) => audit && setAudit({ ...audit, reason: event.target.value })}
        />
      </Modal>
    </PageContainer>
  );
}
