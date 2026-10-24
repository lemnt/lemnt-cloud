import { useRef, useState } from 'react';
import {
  PageContainer,
  ProFormSelect,
  ProFormText,
  ProFormTreeSelect,
  ProTable,
} from '@ant-design/pro-components';
import { ApartmentOutlined } from '@ant-design/icons';
import { App, Button, Popconfirm, Typography, theme } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { SystemStatus, systemStatusEnum, systemStatusOptions } from '@/modules/system/constants';
import { usePermission } from '@/shared/hooks/usePermission';
import {
  createUser,
  deleteUser,
  getUserDepartments,
  getUserDetail,
  getUserList,
  resetUserPassword,
  updateUser,
  updateUserStatus,
  unlockUser,
} from '../api/userApi';
import { userKeys } from '../hooks/userKeys';
import { UserRoles } from '../components/UserRoles';
import { UserDetail } from '../components/UserDetail';
import { userUpdateInput } from '../types';
import { FormSection } from '@/shared/components/FormSection';
import { AdminModalForm as ModalForm } from '@/shared/components/AdminModalForm';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import type { CSSProperties } from 'react';
import type { TreeSelectProps } from 'antd';
import type { DepartmentOption, User, UserDetailResponse, UserFormValues } from '../types';
import formStyles from '@/shared/components/formSection.module.css';
import styles from './userDepartmentSelect.module.css';

const statusOptions = systemStatusOptions;
const passwordRules = [
  { required: true },
  { min: 6, max: 20, message: '密码长度为 6–20 位' },
  { pattern: /^[^<>"'|\\]+$/, message: '密码不能包含 < > " \u0027 | 或反斜杠' },
];

function departmentOptions(nodes: DepartmentOption[]): TreeSelectProps<number>['treeData'] {
  return nodes.map((node) => ({
    key: node.id,
    value: node.id,
    label: node.label,
    title: (
      <span className={styles.node}>
        {Boolean(node.children?.length) && <ApartmentOutlined />}
        <span className={node.children?.length ? styles.branch : undefined}>{node.label}</span>
        {Boolean(node.children?.length) && (
          <span className={styles.count}>{node.children?.length}</span>
        )}
      </span>
    ),
    children: node.children?.length ? departmentOptions(node.children) : undefined,
  }));
}

export default function UserPage() {
  const unlockOperation = useRef(false);
  const action = useRef<ActionType | undefined>(undefined);
  const [formData, setFormData] = useState<UserDetailResponse>();
  const [isOpening, setIsOpening] = useState(false);
  const [selected, setSelected] = useState<number[]>([]);
  const [passwordUser, setPasswordUser] = useState<User>();
  const [roleUser, setRoleUser] = useState<User>();
  const [detailUserId, setDetailUserId] = useState<number>();
  const { can } = usePermission();
  const { message } = App.useApp();
  const queryClient = useQueryClient();
  const departments = useQuery({ queryKey: userKeys.departments, queryFn: getUserDepartments });
  const { token } = theme.useToken();
  const departmentFieldProps: TreeSelectProps<number> = {
    treeData: departmentOptions(departments.data ?? []),
    treeNodeLabelProp: 'label',
    treeDefaultExpandedKeys: departments.data?.map((node) => node.id),
    treeLine: { showLeafIcon: false },
    loading: departments.isLoading,
    allowClear: true,
    showSearch: { treeNodeFilterProp: 'label' },
    placeholder: '搜索或选择部门',
    popupMatchSelectWidth: 340,
    listHeight: 320,
    classNames: { popup: { root: styles.popup } },
    styles: {
      popup: {
        root: {
          '--department-border': token.colorBorderSecondary,
          '--department-secondary': token.colorTextSecondary,
          '--department-primary': token.colorPrimary,
          '--department-fill': token.colorFillAlter,
        } as CSSProperties,
      },
    },
    popupRender: (menu) => (
      <div>
        <div className={styles.heading}>
          <ApartmentOutlined />
          <Typography.Text strong>组织架构</Typography.Text>
        </div>
        {menu}
        <div className={styles.hint}>支持名称搜索，可清空选择</div>
      </div>
    ),
    notFoundContent: departments.isLoading ? '部门加载中…' : '暂无匹配部门',
  };
  const editId = formData?.detail?.userId;
  const save = useMutation({
    mutationFn: (values: UserFormValues) =>
      editId === undefined
        ? createUser({ ...values, password: values.password ?? '' })
        : updateUser(userUpdateInput(editId, values)),
  });
  const remove = useMutation({ mutationFn: deleteUser });
  const status = useMutation({
    mutationFn: (user: User) =>
      updateUserStatus(
        user.userId,
        user.status === SystemStatus.Normal ? SystemStatus.Disabled : SystemStatus.Normal,
      ),
  });
  const unlock = useMutation({ mutationFn: unlockUser });
  const password = useMutation({
    mutationFn: (values: { userId: number; password: string }) =>
      resetUserPassword(values.userId, values.password),
  });

  async function refresh() {
    await queryClient.invalidateQueries({ queryKey: userKeys.all });
    setSelected([]);
    action.current?.reload();
  }
  async function showForm(user?: User) {
    setIsOpening(true);
    try {
      setFormData(
        await queryClient.fetchQuery({
          queryKey: userKeys.detail(user?.userId),
          queryFn: () => getUserDetail(user?.userId),
        }),
      );
    } catch (error) {
      message.error(String(error));
    } finally {
      setIsOpening(false);
    }
  }
  async function handleDelete(ids: number[]) {
    try {
      await remove.mutateAsync(ids);
      message.success('删除成功');
      await refresh();
    } catch (error) {
      message.error(String(error));
    }
  }
  const columns: ProColumns<User>[] = [
    { title: '用户编号', dataIndex: 'userId', search: false },
    {
      title: '用户账号',
      dataIndex: 'userName',
      render: (_, user) =>
        can('system:user:query') ? (
          <Button type="link" onClick={() => setDetailUserId(user.userId)}>
            {user.userName}
          </Button>
        ) : (
          user.userName
        ),
    },
    { title: '用户昵称', dataIndex: 'nickName', search: false },
    { title: '部门', dataIndex: ['dept', 'deptName'], search: false },
    {
      title: '部门筛选',
      dataIndex: 'deptId',
      hideInTable: true,
      valueType: 'treeSelect',
      fieldProps: departmentFieldProps,
    },
    { title: '手机号码', dataIndex: 'phonenumber' },
    {
      title: '状态',
      dataIndex: 'status',
      valueEnum: systemStatusEnum,
    },
    { title: '创建时间', dataIndex: 'createTime', valueType: 'dateTime', search: false },
    { title: '创建时间', dataIndex: 'dateRange', valueType: 'dateRange', hideInTable: true },
    {
      title: '操作',
      valueType: 'option',
      render: (_, user) =>
        user.userId === 1
          ? []
          : [
              can('system:user:edit') && (
                <Button key="edit" type="link" disabled={isOpening} onClick={() => showForm(user)}>
                  修改
                </Button>
              ),
              can('system:user:query') && can('system:user:edit') && (
                <Button key="roles" type="link" onClick={() => setRoleUser(user)}>
                  分配角色
                </Button>
              ),
              can('system:user:edit') && (
                <Popconfirm
                  key="status"
                  title={`确认${user.status === SystemStatus.Normal ? '停用' : '启用'}该用户？`}
                  onConfirm={async () => {
                    try {
                      await status.mutateAsync(user);
                      message.success('状态已更新');
                      await refresh();
                    } catch (error) {
                      message.error(String(error));
                    }
                  }}
                >
                  <Button type="link" disabled={status.isPending}>
                    {user.status === SystemStatus.Normal ? '停用' : '启用'}
                  </Button>
                </Popconfirm>
              ),
              can('system:user:edit') && (
                <Popconfirm
                  key="unlock"
                  title="确认清除该用户的登录失败次数限制？"
                  onConfirm={async () => {
                    if (unlockOperation.current) return;
                    unlockOperation.current = true;
                    try {
                      if (!(await unlock.mutateAsync(user.userName)).data)
                        throw new Error('解锁未成功');
                      message.success('用户已解锁');
                    } catch (error) {
                      message.error(String(error));
                    } finally {
                      unlockOperation.current = false;
                    }
                  }}
                >
                  <Button type="link" disabled={unlock.isPending}>
                    解锁
                  </Button>
                </Popconfirm>
              ),
              can('system:user:resetPwd') && (
                <Button key="password" type="link" onClick={() => setPasswordUser(user)}>
                  重置密码
                </Button>
              ),
              can('system:user:remove') && (
                <Popconfirm
                  key="delete"
                  title="确认删除该用户？"
                  onConfirm={() => handleDelete([user.userId])}
                >
                  <Button type="link" danger disabled={remove.isPending}>
                    删除
                  </Button>
                </Popconfirm>
              ),
            ],
    },
  ];

  return (
    <PageContainer title={false}>
      {departments.error && (
        <Button onClick={() => departments.refetch()}>部门加载失败，点击重试</Button>
      )}
      <ProTable<User>
        search={{ labelWidth: 'auto' }}
        scroll={{ x: 'max-content' }}
        rowKey="userId"
        actionRef={action}
        columns={columns}
        rowSelection={{
          selectedRowKeys: selected,
          onChange: (keys) => setSelected(keys.map(Number)),
          getCheckboxProps: (user) => ({ disabled: user.userId === 1 }),
        }}
        request={async (params) => {
          const range = params.dateRange as string[] | undefined;
          const filters = {
            pageNum: params.current,
            pageSize: params.pageSize,
            userName: params.userName,
            phonenumber: params.phonenumber,
            status: params.status,
            deptId: params.deptId,
            beginTime: range?.[0] ? `${range[0]}T00:00:00` : undefined,
            endTime: range?.[1] ? `${range[1]}T23:59:59` : undefined,
          };
          return queryClient.fetchQuery({
            queryKey: userKeys.list(filters),
            queryFn: () => getUserList(filters),
            // 查询、重置和手动刷新都应读取最新用户资料。
            staleTime: 0,
          });
        }}
        onRequestError={(error) => message.error(error.message)}
        toolBarRender={() => [
          can('system:user:add') && (
            <Button key="add" type="primary" loading={isOpening} onClick={() => showForm()}>
              新增
            </Button>
          ),
          can('system:user:remove') && (
            <Popconfirm
              key="remove"
              title="确认删除所选用户？"
              disabled={!selected.length}
              onConfirm={() => handleDelete(selected)}
            >
              <Button danger disabled={!selected.length} loading={remove.isPending}>
                批量删除
              </Button>
            </Popconfirm>
          ),
        ]}
      />
      {formData && (
        <ModalForm<UserFormValues>
          width={760}
          description="管理账号资料、部门与角色配置"
          title={editId === undefined ? '新增用户' : '修改用户'}
          open
          initialValues={{
            sex: '2',
            status: SystemStatus.Normal,
            ...formData.detail,
            roleIds: formData.roleIds ?? [],
            postIds: formData.postIds ?? [],
          }}
          onOpenChange={(open) => {
            if (!open) setFormData(undefined);
          }}
          modalProps={{ destroyOnHidden: true, mask: { closable: false } }}
          submitter={{ submitButtonProps: { loading: save.isPending } }}
          onFinish={async (values) => {
            try {
              await save.mutateAsync(values);
              setFormData(undefined);
              message.success('保存成功');
              await refresh();
              return true;
            } catch (error) {
              message.error(String(error));
              return false;
            }
          }}
        >
          <FormSection number={1} title="账号资料">
            <div className={formStyles.grid}>
              <ProFormText
                name="userName"
                label="用户账号"
                disabled={editId !== undefined}
                rules={[{ required: true, min: 2, max: 30 }]}
              />
              <ProFormText
                name="nickName"
                label="用户昵称"
                rules={[{ required: true, whitespace: true }]}
                fieldProps={{ maxLength: 30 }}
              />
              {editId === undefined && (
                <ProFormText.Password name="password" label="用户密码" rules={passwordRules} />
              )}
              <ProFormTreeSelect name="deptId" label="所属部门" fieldProps={departmentFieldProps} />
            </div>
          </FormSection>
          <FormSection number={2} title="联系信息">
            <div className={formStyles.grid}>
              <ProFormText
                name="phonenumber"
                label="手机号码"
                rules={[{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码' }]}
                fieldProps={{ maxLength: 11 }}
              />
              <ProFormText
                name="email"
                label="邮箱"
                rules={[{ type: 'email' }]}
                fieldProps={{ maxLength: 50 }}
              />
              <ProFormSelect
                name="sex"
                label="性别"
                options={[
                  { label: '男', value: '0' },
                  { label: '女', value: '1' },
                  { label: '未知', value: '2' },
                ]}
              />
            </div>
          </FormSection>
          <FormSection number={3} title="组织与角色">
            <div className={formStyles.grid}>
              <ProFormSelect
                name="postIds"
                label="岗位"
                mode="multiple"
                options={formData.posts.map((post) => ({
                  label: post.postName,
                  value: post.postId,
                  disabled: post.status !== SystemStatus.Normal,
                }))}
              />
              <ProFormSelect
                name="roleIds"
                label="角色"
                mode="multiple"
                options={formData.roles.map((role) => ({
                  label: role.roleName,
                  value: role.roleId,
                  disabled: role.status !== SystemStatus.Normal,
                }))}
              />
              <ProFormSelect
                name="status"
                label="状态"
                options={statusOptions}
                rules={[{ required: true }]}
              />
            </div>
          </FormSection>
        </ModalForm>
      )}
      {passwordUser && (
        <ModalForm<{ password: string }>
          width={520}
          description="设置新的登录密码，请妥善保存并通知账号使用者"
          open
          title={`重置 ${passwordUser.userName} 的密码`}
          modalProps={{ destroyOnHidden: true, mask: { closable: false } }}
          onOpenChange={(open) => {
            if (!open) setPasswordUser(undefined);
          }}
          submitter={{ submitButtonProps: { loading: password.isPending } }}
          onFinish={async (values) => {
            try {
              await password.mutateAsync({
                userId: passwordUser.userId,
                password: values.password,
              });
              message.success('密码已重置');
              setPasswordUser(undefined);
              return true;
            } catch (error) {
              message.error(String(error));
              return false;
            }
          }}
        >
          <ProFormText.Password name="password" label="新密码" rules={passwordRules} />
        </ModalForm>
      )}
      {roleUser && (
        <UserRoles user={roleUser} onClose={() => setRoleUser(undefined)} onSuccess={refresh} />
      )}
      {detailUserId !== undefined && (
        <UserDetail
          key={detailUserId}
          userId={detailUserId}
          onClose={() => setDetailUserId(undefined)}
        />
      )}
    </PageContainer>
  );
}
