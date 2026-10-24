import { useRef, useState } from 'react';
import { ProTable } from '@ant-design/pro-components';
import { App, Alert, Button, Descriptions, Drawer, Result, Space, Spin } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { roleKeys } from '@/modules/system/role/hooks/roleKeys';
import { SystemStatus, systemStatusEnum } from '@/modules/system/constants';
import { usePermission } from '@/shared/hooks/usePermission';
import { getUserRoles, updateUserRoles } from '../api/userApi';
import { userKeys } from '../hooks/userKeys';
import { assignedRoleIds } from '../types';
import type { ProColumns } from '@ant-design/pro-components';
import type { Role } from '@/modules/system/role/types';
import type { User, UserRolesResponse } from '../types';

interface UserRolesProps {
  user: User;
  onClose: () => void;
  onSuccess: () => Promise<void>;
}

/** 只有授权数据成功加载后才初始化选择，避免空列表覆盖已有权限。 */
export function UserRoles(props: UserRolesProps) {
  const { can } = usePermission();
  const canAssign = props.user.userId !== 1 && can('system:user:query') && can('system:user:edit');
  const query = useQuery({
    queryKey: userKeys.roles(props.user.userId),
    queryFn: () => getUserRoles(props.user.userId),
    enabled: canAssign,
    staleTime: 0,
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
  });
  if (canAssign && query.isSuccess && !query.isFetching)
    return <UserRolesForm {...props} key={props.user.userId} data={query.data} />;
  return (
    <Drawer open title="分配角色" onClose={props.onClose}>
      {!canAssign ? (
        <Result status="403" title="无权分配角色" />
      ) : query.isError ? (
        <Result
          status="error"
          title="角色加载失败"
          extra={<Button onClick={() => void query.refetch()}>重试</Button>}
        />
      ) : (
        <Spin />
      )}
    </Drawer>
  );
}

function UserRolesForm({ data, onClose, onSuccess }: UserRolesProps & { data: UserRolesResponse }) {
  const [selected, setSelected] = useState<number[]>(() => assignedRoleIds(data.roles));
  const submission = useRef(false);
  const client = useQueryClient();
  const { can } = usePermission();
  const { message } = App.useApp();
  const save = useMutation({ mutationFn: updateUserRoles });
  const canSave = data.user.userId !== 1 && can('system:user:query') && can('system:user:edit');
  const columns: ProColumns<Role>[] = [
    { title: '角色编号', dataIndex: 'roleId' },
    { title: '角色名称', dataIndex: 'roleName' },
    { title: '权限标识', dataIndex: 'roleKey' },
    {
      title: '状态',
      dataIndex: 'status',
      valueEnum: systemStatusEnum,
    },
  ];

  async function handleSave() {
    if (!canSave || submission.current) return;
    submission.current = true;
    try {
      const result = await save.mutateAsync({ userId: data.user.userId, roleIds: selected });
      if (!result.data) throw new Error('角色授权未成功，请重试');
      message.success('角色授权已更新');
      onClose();
      void client.invalidateQueries({ queryKey: roleKeys.all });
      void onSuccess().catch((error: unknown) =>
        message.error(`授权已保存，列表刷新失败：${String(error)}`),
      );
    } catch (error) {
      message.error(String(error));
    } finally {
      submission.current = false;
    }
  }

  return (
    <Drawer
      open
      title="分配角色"
      size={800}
      closable={!save.isPending}
      mask={{ closable: !save.isPending }}
      keyboard={!save.isPending}
      onClose={() => {
        if (!submission.current) onClose();
      }}
      footer={
        <Space>
          <Button onClick={onClose} disabled={save.isPending}>
            取消
          </Button>
          <Button
            type="primary"
            loading={save.isPending}
            disabled={!canSave}
            onClick={() => void handleSave()}
          >
            保存授权
          </Button>
        </Space>
      }
    >
      <Descriptions
        items={[
          { key: 'name', label: '账号', children: data.user.userName },
          { key: 'nickname', label: '昵称', children: data.user.nickName },
        ]}
      />
      <Alert
        type="info"
        title="停用角色不可调整，已有授权保持不变；清空可选角色后保存将取消相应授权。"
      />
      <ProTable<Role>
        rowKey="roleId"
        columns={columns}
        dataSource={data.roles}
        search={false}
        options={false}
        toolBarRender={false}
        pagination={{ defaultPageSize: 10 }}
        rowSelection={{
          selectedRowKeys: selected,
          preserveSelectedRowKeys: true,
          onChange: (keys) => setSelected(keys.map(Number)),
          getCheckboxProps: (role) => ({
            disabled: role.status !== SystemStatus.Normal || save.isPending || !canSave,
          }),
        }}
      />
    </Drawer>
  );
}
