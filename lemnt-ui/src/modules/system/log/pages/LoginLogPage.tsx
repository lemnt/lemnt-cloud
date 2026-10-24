import { useRef, useState } from 'react';
import { PageContainer, ProTable } from '@ant-design/pro-components';
import { App, Button, Descriptions, Drawer, Popconfirm } from 'antd';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { unlockUser } from '@/modules/system/user/api/userApi';
import { usePermission } from '@/shared/hooks/usePermission';
import { cleanLoginLog, deleteLoginLog, getLoginLogList } from '../api/logApi';
import { logKeys } from '../hooks/logKeys';
import { logDateRange } from '../types';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import type { LoginLog, LoginLogQuery } from '../types';

interface LoginLogSearch extends LoginLogQuery {
  dateRange?: string[];
}

export default function LoginLogPage() {
  const action = useRef<ActionType | undefined>(undefined);
  const [selected, setSelected] = useState<number[]>([]);
  const [detail, setDetail] = useState<LoginLog>();
  const { can } = usePermission();
  const { message } = App.useApp();
  const queryClient = useQueryClient();
  const remove = useMutation({ mutationFn: deleteLoginLog });
  const clean = useMutation({ mutationFn: cleanLoginLog });
  const unlock = useMutation({ mutationFn: unlockUser });
  const isMutating = remove.isPending || clean.isPending || unlock.isPending;
  async function refresh() {
    await queryClient.invalidateQueries({ queryKey: logKeys.login });
    setSelected([]);
    action.current?.reload();
  }
  const columns: ProColumns<LoginLog>[] = [
    { title: '用户账号', dataIndex: 'userName' },
    { title: '登录 IP', dataIndex: 'ipaddr' },
    {
      title: '状态',
      dataIndex: 'status',
      valueEnum: {
        '1': { text: '成功', status: 'Success' },
        '2': { text: '失败', status: 'Error' },
      },
    },
    { title: '提示消息', dataIndex: 'msg', search: false },
    { title: '访问时间', dataIndex: 'accessTime', valueType: 'dateTime', search: false },
    { title: '访问时间', dataIndex: 'dateRange', valueType: 'dateRange', hideInTable: true },
    {
      title: '操作',
      valueType: 'option',
      render: (_, record) => [
        can('system:logininfor:query') && (
          <Button key="detail" type="link" onClick={() => setDetail(record)}>
            详情
          </Button>
        ),
        can('system:user:edit') && (
          <Popconfirm
            key="unlock"
            title={`确认解锁账号“${record.userName}”？`}
            description="仅清除登录失败次数限制，不修改账号状态或历史日志。"
            disabled={isMutating || !record.userName}
            onConfirm={async () => {
              if (isMutating || !record.userName) return;
              try {
                const result = await unlock.mutateAsync(record.userName);
                if (!result.data) {
                  message.error('解锁未成功，请重试');
                  return;
                }
                message.success(`账号“${record.userName}”已解除登录失败限制`);
              } catch (error) {
                message.error(String(error));
              }
            }}
          >
            <Button
              type="link"
              disabled={isMutating || !record.userName}
              loading={unlock.isPending && unlock.variables === record.userName}
            >
              解锁账号
            </Button>
          </Popconfirm>
        ),
      ],
    },
  ];
  return (
    <PageContainer title={false}>
      <ProTable<LoginLog, LoginLogSearch>
        search={{ labelWidth: 'auto' }}
        rowKey="infoId"
        actionRef={action}
        columns={columns}
        scroll={{ x: 'max-content' }}
        rowSelection={{
          preserveSelectedRowKeys: true,
          selectedRowKeys: selected,
          onChange: (keys) => setSelected(keys.map(Number)),
        }}
        request={(params) => {
          const filters: LoginLogQuery = {
            pageNum: params.current,
            pageSize: params.pageSize,
            userName: params.userName,
            ipaddr: params.ipaddr,
            status: params.status,
            ...logDateRange(params.dateRange),
          };
          return queryClient.fetchQuery({
            queryKey: logKeys.loginList(filters),
            queryFn: () => getLoginLogList(filters),
          });
        }}
        onRequestError={(error) => message.error(error.message)}
        toolBarRender={() => [
          can('system:logininfor:remove') && (
            <Popconfirm
              key="delete"
              title="确认删除所选登录日志？"
              disabled={!selected.length || isMutating}
              onConfirm={async () => {
                try {
                  await remove.mutateAsync(selected);
                  message.success('删除成功');
                  await refresh();
                } catch (error) {
                  message.error(String(error));
                }
              }}
            >
              <Button danger disabled={!selected.length || isMutating} loading={remove.isPending}>
                批量删除
              </Button>
            </Popconfirm>
          ),
          can('system:logininfor:remove') && (
            <Popconfirm
              key="clean"
              title="确认清空全部登录日志？"
              description="此操作不是只删除当前筛选结果，且不可恢复。"
              disabled={isMutating}
              onConfirm={async () => {
                try {
                  await clean.mutateAsync();
                  message.success('已清空');
                  await refresh();
                } catch (error) {
                  message.error(String(error));
                }
              }}
            >
              <Button danger disabled={isMutating} loading={clean.isPending}>
                清空全部
              </Button>
            </Popconfirm>
          ),
        ]}
      />
      <Drawer open={Boolean(detail)} title="登录日志详情" onClose={() => setDetail(undefined)}>
        <Descriptions
          column={1}
          items={
            detail
              ? [
                  { key: 'id', label: '访问 ID', children: detail.infoId },
                  { key: 'user', label: '用户账号', children: detail.userName },
                  { key: 'ip', label: '登录 IP', children: detail.ipaddr },
                  {
                    key: 'status',
                    label: '状态',
                    children: detail.status === '1' ? '成功' : '失败',
                  },
                  { key: 'msg', label: '提示消息', children: detail.msg },
                  { key: 'time', label: '访问时间', children: detail.accessTime },
                ]
              : []
          }
        />
      </Drawer>
    </PageContainer>
  );
}
