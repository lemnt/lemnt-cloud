import { useRef, useState } from 'react';
import { ProTable } from '@ant-design/pro-components';
import { App, Button, Drawer, Popconfirm, Segmented } from 'antd';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { userKeys } from '@/modules/system/user/hooks/userKeys';
import { getRoleUsers, updateRoleUsers } from '../api/roleApi';
import { roleKeys } from '../hooks/roleKeys';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import type { User } from '@/modules/system/user/types';
import type { Role } from '../types';

interface RoleUsersProps {
  role: Role;
  onClose: () => void;
}

export function RoleUsers({ role, onClose }: RoleUsersProps) {
  const [allocated, setAllocated] = useState(true);
  const [selected, setSelected] = useState<number[]>([]);
  const action = useRef<ActionType | undefined>(undefined);
  const queryClient = useQueryClient();
  const { message } = App.useApp();
  const mutation = useMutation({
    mutationFn: () => updateRoleUsers(role.roleId, selected, !allocated),
  });
  const columns: ProColumns<User>[] = [
    { title: '账号', dataIndex: 'userName' },
    { title: '昵称', dataIndex: 'nickName', search: false },
    { title: '手机号码', dataIndex: 'phonenumber' },
  ];
  return (
    <Drawer title={`${role.roleName}：分配用户`} open size={900} onClose={onClose}>
      <Segmented
        disabled={mutation.isPending}
        value={allocated ? 'allocated' : 'unallocated'}
        options={[
          { label: '已分配', value: 'allocated' },
          { label: '待分配', value: 'unallocated' },
        ]}
        onChange={(value) => {
          setAllocated(value === 'allocated');
          setSelected([]);
        }}
      />
      <ProTable<User>
        key={String(allocated)}
        rowKey="userId"
        actionRef={action}
        columns={columns}
        rowSelection={{
          selectedRowKeys: selected,
          onChange: (keys) => setSelected(keys.map(Number)),
        }}
        request={(params) => {
          const filters = {
            roleId: role.roleId,
            pageNum: params.current,
            pageSize: params.pageSize,
            userName: params.userName,
            phonenumber: params.phonenumber,
          };
          return queryClient.fetchQuery({
            queryKey: roleKeys.users(filters, allocated),
            queryFn: () => getRoleUsers(filters, allocated),
          });
        }}
        onRequestError={(error) => message.error(error.message)}
        toolBarRender={() => [
          <Popconfirm
            key="save"
            title={allocated ? '确认取消所选用户的授权？' : '确认授权所选用户？'}
            disabled={!selected.length}
            onConfirm={async () => {
              try {
                await mutation.mutateAsync();
                message.success('授权已更新');
                await queryClient.invalidateQueries({ queryKey: roleKeys.all });
                await queryClient.invalidateQueries({ queryKey: userKeys.all });
                setSelected([]);
                action.current?.reload();
              } catch (error) {
                message.error(String(error));
              }
            }}
          >
            <Button type="primary" disabled={!selected.length} loading={mutation.isPending}>
              {allocated ? '取消授权' : '授权用户'}
            </Button>
          </Popconfirm>,
        ]}
      />
    </Drawer>
  );
}
