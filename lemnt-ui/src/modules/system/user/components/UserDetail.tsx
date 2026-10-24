import { ProDescriptions } from '@ant-design/pro-components';
import { Avatar, Button, Drawer, Result, Spin } from 'antd';
import { useQuery } from '@tanstack/react-query';
import { systemStatusEnum } from '@/modules/system/constants';
import { usePermission } from '@/shared/hooks/usePermission';
import defaultAvatar from '@/shared/assets/default-avatar.png';
import { getUserDetail } from '../api/userApi';
import { userKeys } from '../hooks/userKeys';
import type { ProDescriptionsItemProps } from '@ant-design/pro-components';
import type { UserDetailRecord } from '../types';

interface UserDetailProps {
  userId: number;
  onClose: () => void;
}

/** 查看与编辑共用详情查询，但不挂载编辑表单，也不发送任何写请求。 */
export function UserDetail({ userId, onClose }: UserDetailProps) {
  const { can } = usePermission();
  const canView = can('system:user:query');
  const query = useQuery({
    queryKey: userKeys.detail(userId),
    queryFn: () => getUserDetail(userId),
    enabled: canView,
  });
  const data = query.data;
  const detail = data?.detail;
  // 角色与岗位列表包含可选项，必须根据已分配 ID 筛选，不能全部展示为已有授权。
  const roleNames = data?.roles
    .filter((role) => data.roleIds.includes(role.roleId))
    .map((role) => role.roleName)
    .join('、');
  const postNames = data?.posts
    .filter((post) => data.postIds.includes(post.postId))
    .map((post) => post.postName)
    .join('、');
  const columns: ProDescriptionsItemProps<UserDetailRecord>[] = [
    { title: '用户编号', dataIndex: 'userId' },
    { title: '登录账号', dataIndex: 'userName' },
    { title: '昵称', dataIndex: 'nickName' },
    { title: '部门编号', dataIndex: 'deptId' },
    { title: '手机号码', dataIndex: 'phonenumber' },
    { title: '邮箱', dataIndex: 'email' },
    { title: '性别', dataIndex: 'sex', valueEnum: { '0': '男', '1': '女', '2': '未知' } },
    {
      title: '状态',
      dataIndex: 'status',
      valueEnum: systemStatusEnum,
    },
    { title: '角色', render: () => roleNames || '无角色' },
    { title: '岗位', render: () => postNames || '无岗位' },
    { title: '创建者', dataIndex: 'createBy' },
    { title: '创建时间', dataIndex: 'createTime', valueType: 'dateTime' },
    { title: '最后登录 IP', dataIndex: 'loginIp' },
    { title: '最后登录时间', dataIndex: 'loginDate', valueType: 'dateTime' },
  ];
  return (
    <Drawer open title="用户详情" size={800} onClose={onClose}>
      {!canView ? (
        <Result status="403" title="无权查看用户详情" />
      ) : query.isPending ? (
        <Spin />
      ) : query.isError ? (
        <Result
          status="error"
          title="用户详情加载失败"
          extra={
            <Button loading={query.isFetching} onClick={() => void query.refetch()}>
              重试
            </Button>
          }
        />
      ) : !detail ? (
        <Result status="404" title="用户详情不存在" />
      ) : (
        <>
          <Avatar size={64} src={detail.avatar || defaultAvatar}>
            {detail.nickName}
          </Avatar>
          <ProDescriptions<UserDetailRecord> column={2} dataSource={detail} columns={columns} />
        </>
      )}
    </Drawer>
  );
}
