import { useRef, useState } from 'react';
import {
  PageContainer,
  ProFormDependency,
  ProFormDigit,
  ProFormSelect,
  ProFormText,
  ProTable,
} from '@ant-design/pro-components';
import { App, Button, Form, Popconfirm } from 'antd';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { SystemStatus, systemStatusEnum, systemStatusOptions } from '@/modules/system/constants';
import { usePermission } from '@/shared/hooks/usePermission';
import {
  createRole,
  deleteRole,
  getRoleDepartments,
  getRoleDetail,
  getRoleList,
  getRoleMenus,
  updateRole,
  updateRoleScope,
  updateRoleStatus,
} from '../api/roleApi';
import { RolePermissionTree } from '../components/RolePermissionTree';
import { RoleUsers } from '../components/RoleUsers';
import { roleKeys } from '../hooks/roleKeys';
import { permissionIds } from '../types';
import { FormSection } from '@/shared/components/FormSection';
import { AdminModalForm as ModalForm } from '@/shared/components/AdminModalForm';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import type { CreateRoleRequest, PermissionNode, Role, RoleScopeRequest } from '../types';
import styles from '../components/rolePermissionTree.module.css';

const statusOptions = systemStatusOptions;
const scopeOptions = [
  { label: '全部数据', value: '1' },
  { label: '自定义数据', value: '2' },
  { label: '本部门', value: '3' },
  { label: '本部门及以下', value: '4' },
  { label: '仅本人', value: '5' },
];

export default function RolePage() {
  const action = useRef<ActionType | undefined>(undefined);
  const [initial, setInitial] = useState<CreateRoleRequest>();
  const [editingId, setEditingId] = useState<number>();
  const [nodes, setNodes] = useState<PermissionNode[]>([]);
  const [scope, setScope] = useState<RoleScopeRequest>();
  const [depts, setDepts] = useState<PermissionNode[]>([]);
  const [selected, setSelected] = useState<number[]>([]);
  const [isOpening, setIsOpening] = useState(false);
  const [authRole, setAuthRole] = useState<Role>();
  const { can } = usePermission();
  const { message } = App.useApp();
  const queryClient = useQueryClient();
  const save = useMutation({
    mutationFn: (values: CreateRoleRequest) =>
      editingId === undefined ? createRole(values) : updateRole({ ...values, roleId: editingId }),
  });
  const remove = useMutation({ mutationFn: deleteRole });
  const status = useMutation({
    mutationFn: (role: Role) =>
      updateRoleStatus(
        role.roleId,
        role.status === SystemStatus.Normal ? SystemStatus.Disabled : SystemStatus.Normal,
      ),
  });
  const saveScope = useMutation({ mutationFn: updateRoleScope });

  async function refresh() {
    await queryClient.invalidateQueries({ queryKey: roleKeys.all });
    setSelected([]);
    action.current?.reload();
  }
  async function showForm(role?: Role) {
    setIsOpening(true);
    try {
      const [detail, tree] = await Promise.all([
        role
          ? queryClient.fetchQuery({
              queryKey: roleKeys.detail(role.roleId),
              queryFn: () => getRoleDetail(role.roleId),
            })
          : Promise.resolve(undefined),
        queryClient.fetchQuery({
          queryKey: roleKeys.menus(role?.roleId),
          queryFn: () => getRoleMenus(role?.roleId),
        }),
      ]);
      setEditingId(detail?.roleId);
      setNodes(tree.menus);
      setInitial({
        roleName: detail?.roleName ?? '',
        roleKey: detail?.roleKey ?? '',
        roleSort: detail?.roleSort ?? 0,
        dataScope: detail?.dataScope ?? '1',
        status: detail?.status ?? SystemStatus.Normal,
        menuIds: tree.checkedKeys,
      });
    } catch (error) {
      message.error(String(error));
    } finally {
      setIsOpening(false);
    }
  }
  async function showScope(role: Role) {
    setIsOpening(true);
    try {
      const [detail, tree] = await Promise.all([
        queryClient.fetchQuery({
          queryKey: roleKeys.detail(role.roleId),
          queryFn: () => getRoleDetail(role.roleId),
        }),
        queryClient.fetchQuery({
          queryKey: roleKeys.departments(role.roleId),
          queryFn: () => getRoleDepartments(role.roleId),
        }),
      ]);
      setDepts(tree.depts);
      setScope({
        roleId: role.roleId,
        dataScope: detail.dataScope,
        deptIds: tree.checkedKeys,
      });
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
  const columns: ProColumns<Role>[] = [
    { title: '角色编号', dataIndex: 'roleId', search: false },
    { title: '角色名称', dataIndex: 'roleName' },
    { title: '权限标识', dataIndex: 'roleKey' },
    { title: '排序', dataIndex: 'roleSort', search: false },
    {
      title: '状态',
      dataIndex: 'status',
      valueEnum: systemStatusEnum,
    },
    { title: '创建时间', dataIndex: 'dateRange', valueType: 'dateRange', hideInTable: true },
    {
      title: '操作',
      valueType: 'option',
      render: (_, role) =>
        role.roleId === 1
          ? []
          : [
              can('system:role:edit') && (
                <Button key="edit" type="link" disabled={isOpening} onClick={() => showForm(role)}>
                  修改/菜单权限
                </Button>
              ),
              can('system:role:edit') && (
                <Button
                  key="scope"
                  type="link"
                  disabled={isOpening}
                  onClick={() => showScope(role)}
                >
                  数据权限
                </Button>
              ),
              can('system:role:edit') && (
                <Button key="users" type="link" onClick={() => setAuthRole(role)}>
                  分配用户
                </Button>
              ),
              can('system:role:edit') && (
                <Popconfirm
                  key="status"
                  title={`确认${role.status === SystemStatus.Normal ? '停用' : '启用'}该角色？`}
                  onConfirm={async () => {
                    try {
                      await status.mutateAsync(role);
                      message.success('状态已更新');
                      await refresh();
                    } catch (error) {
                      message.error(String(error));
                    }
                  }}
                >
                  <Button type="link" disabled={status.isPending}>
                    {role.status === SystemStatus.Normal ? '停用' : '启用'}
                  </Button>
                </Popconfirm>
              ),
              can('system:role:remove') && (
                <Popconfirm
                  key="delete"
                  title="确认删除该角色？"
                  onConfirm={() => handleDelete([role.roleId])}
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
      <ProTable<Role>
        search={{ labelWidth: 'auto' }}
        rowKey="roleId"
        actionRef={action}
        columns={columns}
        scroll={{ x: 'max-content' }}
        rowSelection={{
          selectedRowKeys: selected,
          onChange: (keys) => setSelected(keys.map(Number)),
          getCheckboxProps: (role) => ({ disabled: role.roleId === 1 }),
        }}
        request={(params) => {
          const range = params.dateRange as string[] | undefined;
          const filters = {
            pageNum: params.current,
            pageSize: params.pageSize,
            roleName: params.roleName,
            roleKey: params.roleKey,
            status: params.status,
            beginTime: range?.[0] ? `${range[0]}T00:00:00` : undefined,
            endTime: range?.[1] ? `${range[1]}T23:59:59` : undefined,
          };
          return queryClient.fetchQuery({
            queryKey: roleKeys.list(filters),
            queryFn: () => getRoleList(filters),
          });
        }}
        onRequestError={(error) => message.error(error.message)}
        toolBarRender={() => [
          can('system:role:add') && (
            <Button key="add" type="primary" loading={isOpening} onClick={() => showForm()}>
              新增
            </Button>
          ),
          can('system:role:remove') && (
            <Popconfirm
              key="remove"
              title="确认删除所选角色？"
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
      {initial && (
        <ModalForm<CreateRoleRequest>
          description="配置角色信息与权限范围"
          open
          title={editingId === undefined ? '新增角色' : '修改角色'}
          width={900}
          initialValues={initial}
          onOpenChange={(open) => {
            if (!open && !save.isPending) setInitial(undefined);
          }}
          disabled={save.isPending}
          modalProps={{
            destroyOnHidden: true,
            mask: { closable: false },
            closable: !save.isPending,
            keyboard: !save.isPending,
          }}
          submitter={{
            submitButtonProps: { loading: save.isPending },
            resetButtonProps: { disabled: save.isPending },
          }}
          onFinish={async (values) => {
            try {
              const result = await save.mutateAsync({
                ...initial,
                ...values,
                menuIds: permissionIds(values.menuIds ?? [], nodes),
              });
              if (!result.data) throw new Error('角色保存未成功');
              setInitial(undefined);
              message.success('保存成功');
              await refresh();
              return true;
            } catch (error) {
              message.error(String(error));
              return false;
            }
          }}
        >
          <FormSection number={1} title="角色信息">
            <div className={styles.fields}>
              <ProFormText
                name="roleName"
                label="角色名称"
                rules={[{ required: true, whitespace: true }]}
                fieldProps={{ maxLength: 30 }}
              />
              <ProFormText
                name="roleKey"
                label="权限标识"
                rules={[{ required: true, whitespace: true }]}
                fieldProps={{ maxLength: 100 }}
              />
              <ProFormDigit
                name="roleSort"
                label="排序"
                min={0}
                fieldProps={{ precision: 0 }}
                rules={[{ required: true }]}
              />
              <ProFormSelect
                name="status"
                label="状态"
                options={statusOptions}
                rules={[{ required: true }]}
              />
            </div>
          </FormSection>
          <FormSection number={2} title="菜单权限" description="勾选此角色可以访问的菜单与操作">
            <Form.Item name="menuIds" label="菜单权限">
              <RolePermissionTree nodes={nodes} disabled={save.isPending} />
            </Form.Item>
          </FormSection>
        </ModalForm>
      )}
      {scope && (
        <ModalForm<RoleScopeRequest>
          description="选择角色的数据范围，并配置可访问的部门"
          open
          title="数据权限"
          initialValues={scope}
          onOpenChange={(open) => {
            if (!open && !saveScope.isPending) setScope(undefined);
          }}
          disabled={saveScope.isPending}
          modalProps={{
            destroyOnHidden: true,
            mask: { closable: false },
            closable: !saveScope.isPending,
            keyboard: !saveScope.isPending,
          }}
          submitter={{
            submitButtonProps: { loading: saveScope.isPending },
            resetButtonProps: { disabled: saveScope.isPending },
          }}
          onFinish={async (values) => {
            try {
              const result = await saveScope.mutateAsync({
                roleId: scope.roleId,
                dataScope: values.dataScope,
                deptIds: values.dataScope === '2' ? permissionIds(values.deptIds ?? [], depts) : [],
              });
              if (!result.data) throw new Error('数据权限保存未成功');
              setScope(undefined);
              message.success('数据权限已保存');
              await refresh();
              return true;
            } catch (error) {
              message.error(String(error));
              return false;
            }
          }}
        >
          <ProFormSelect
            name="dataScope"
            label="数据范围"
            options={scopeOptions}
            rules={[{ required: true }]}
          />
          <ProFormDependency name={['dataScope']}>
            {({ dataScope }) =>
              dataScope === '2' ? (
                <Form.Item name="deptIds" label="部门权限">
                  <RolePermissionTree nodes={depts} disabled={saveScope.isPending} />
                </Form.Item>
              ) : null
            }
          </ProFormDependency>
        </ModalForm>
      )}
      {authRole && <RoleUsers role={authRole} onClose={() => setAuthRole(undefined)} />}
    </PageContainer>
  );
}
