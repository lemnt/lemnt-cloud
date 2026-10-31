import { useState } from 'react';
import { PageContainer, ProTable, type ProColumns } from '@ant-design/pro-components';
import { Alert, App, Button, Descriptions, Drawer, Popconfirm, Result, Table } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { usePermission } from '@/shared/hooks/usePermission';
import * as api from '../api/payNotifyApi';

const typeEnum = { PAY: '支付', REFUND: '退款' } as const;
const statusEnum = { PENDING: '待派发', SUCCESS: '成功', FAILED: '失败' } as const;

/** 支付结果派发任务：查看尝试日志并对失败任务人工重发。 */
export function PayNotifyTaskPage() {
  const { can } = usePermission(),
    { message } = App.useApp(),
    cache = useQueryClient();
  const prefix = 'trade:pay-notify';
  const [filters, setFilters] = useState<Omit<api.PayNotifyFilter, 'pageNum' | 'pageSize'>>({});
  const [page, setPage] = useState({ pageNum: 1, pageSize: 20 });
  const [selected, setSelected] = useState<string>();
  const rows = useQuery({
    queryKey: [prefix, filters, page],
    queryFn: () => api.getPayNotifyTasks({ ...filters, ...page }),
    enabled: can(prefix + ':list'),
  });
  const detail = useQuery({
    queryKey: [prefix, 'detail', selected],
    queryFn: () => api.getPayNotifyTask(selected ?? ''),
    enabled: !!selected && can(prefix + ':query'),
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
  });
  const retry = useMutation({ mutationFn: (key: string) => api.retryPayNotifyTask(key) });
  async function refresh(key?: string) {
    try {
      await cache.invalidateQueries({ queryKey: [prefix] }, { throwOnError: true });
      if (key) await detail.refetch();
    } catch {
      message.warning('操作已成功，刷新失败请重试');
    }
  }
  const columns: ProColumns<api.PayNotifyTask>[] = [
    { title: '编号', dataIndex: 'id', search: false },
    { title: '类型', dataIndex: 'type', valueType: 'select', valueEnum: typeEnum },
    { title: '业务单号', dataIndex: 'bizNo' },
    { title: '订单号', dataIndex: 'orderNo' },
    { title: '状态', dataIndex: 'status', valueType: 'select', valueEnum: statusEnum },
    {
      title: '尝试次数',
      dataIndex: 'notifyTimes',
      search: false,
      render: (_, row) => `${row.notifyTimes}/${row.maxNotifyTimes}`,
    },
    { title: '下次派发', dataIndex: 'nextNotifyTime', search: false },
    { title: '最后响应', dataIndex: 'lastResponse', search: false, ellipsis: true },
    { title: '创建时间', dataIndex: 'createTime', search: false },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) => (
        <>
          {can(prefix + ':query') && (
            <Button type="link" onClick={() => setSelected(row.id)}>
              详情
            </Button>
          )}
          {can(prefix + ':retry') && row.status !== 'SUCCESS' && (
            <Popconfirm
              title="确认重发该任务？"
              onConfirm={async () => {
                if (retry.isPending) return;
                try {
                  await retry.mutateAsync(row.id);
                  message.success('重发已提交');
                } catch (e) {
                  message.error(String(e));
                }
                await refresh(row.id);
              }}
            >
              <Button type="link" disabled={retry.isPending}>
                重发
              </Button>
            </Popconfirm>
          )}
        </>
      ),
    },
  ];
  if (!can(prefix + ':list')) return <Result status="403" title="没有派发任务列表权限" />;
  const value = detail.data;
  return (
    <PageContainer title="支付派发任务">
      {rows.isError && (
        <Alert
          type="error"
          title="列表读取失败"
          description={String(rows.error)}
          action={<Button onClick={() => rows.refetch()}>重试</Button>}
        />
      )}
      <ProTable
        options={{ reload: false }}
        rowKey="id"
        columns={columns}
        dataSource={rows.data?.records ?? []}
        loading={rows.isFetching}
        scroll={{ x: 1200 }}
        toolBarRender={() => [
          <Button key="refresh" onClick={() => rows.refetch()}>
            刷新
          </Button>,
        ]}
        onSubmit={(v) => {
          setFilters({
            type: v.type === 'PAY' || v.type === 'REFUND' ? v.type : undefined,
            bizNo: typeof v.bizNo === 'string' ? v.bizNo : undefined,
            orderNo: typeof v.orderNo === 'string' ? v.orderNo : undefined,
            status:
              v.status === 'PENDING' || v.status === 'SUCCESS' || v.status === 'FAILED'
                ? v.status
                : undefined,
          });
          setPage({ ...page, pageNum: 1 });
        }}
        onReset={() => {
          setFilters({});
          setPage({ ...page, pageNum: 1 });
        }}
        pagination={{
          current: page.pageNum,
          pageSize: page.pageSize,
          total: rows.data?.total ?? 0,
          onChange: (pageNum, pageSize) => setPage({ pageNum, pageSize }),
        }}
      />
      <Drawer
        open={!!selected}
        title="派发任务详情"
        onClose={() => setSelected(undefined)}
        width={720}
      >
        {detail.isFetching ? (
          '正在读取…'
        ) : detail.isError ? (
          <Alert
            type="error"
            title="详情读取失败"
            description={String(detail.error)}
            action={<Button onClick={() => detail.refetch()}>重试</Button>}
          />
        ) : (
          value && (
            <>
              <Descriptions
                column={1}
                items={[
                  { key: 'id', label: '编号', children: value.task.id },
                  { key: 'type', label: '类型', children: typeEnum[value.task.type] },
                  { key: 'biz', label: '业务单号', children: value.task.bizNo },
                  { key: 'order', label: '订单号', children: value.task.orderNo },
                  { key: 'status', label: '状态', children: statusEnum[value.task.status] },
                  {
                    key: 'times',
                    label: '尝试次数',
                    children: `${value.task.notifyTimes}/${value.task.maxNotifyTimes}`,
                  },
                  { key: 'next', label: '下次派发', children: value.task.nextNotifyTime || '—' },
                  { key: 'last', label: '最后响应', children: value.task.lastResponse || '—' },
                ]}
              />
              <Table
                rowKey={(row) => row.notifyTimes}
                size="small"
                loading={detail.isFetching}
                dataSource={value.logs}
                pagination={false}
                columns={[
                  { title: '次数', dataIndex: 'notifyTimes' },
                  {
                    title: '结果',
                    dataIndex: 'status',
                    render: (status: 'SUCCESS' | 'FAILED') =>
                      status === 'SUCCESS' ? '成功' : '失败',
                  },
                  { title: '响应', dataIndex: 'response', ellipsis: true },
                  { title: '时间', dataIndex: 'createTime' },
                ]}
              />
            </>
          )
        )}
      </Drawer>
    </PageContainer>
  );
}
