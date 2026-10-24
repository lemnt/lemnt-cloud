import { useRef, useState } from 'react';
import {
  PageContainer,
  ProFormDependency,
  ProFormDigit,
  ProFormRadio,
  ProFormText,
  ProFormTreeSelect,
  ProTable,
} from '@ant-design/pro-components';
import { App, Button, Popconfirm } from 'antd';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useSession } from '@/stores/userStore';
import { SystemStatus, systemStatusEnum, systemStatusOptions } from '@/modules/system/constants';
import { usePermission } from '@/shared/hooks/usePermission';
import { createMenu, deleteMenu, getMenuDetail, getMenuList, updateMenu } from '../api/menuApi';
import { menuKeys } from '../hooks/menuKeys';
import { menuInput, menuParents, menuTree } from '../types';
import { AdminModalForm as ModalForm } from '@/shared/components/AdminModalForm';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import type { CreateMenuRequest, Menu, MenuQuery } from '../types';

export default function MenuPage() {
  const action = useRef<ActionType | undefined>(undefined);
  const [initial, setInitial] = useState<CreateMenuRequest>();
  const [editingId, setEditingId] = useState<number>();
  const [parents, setParents] = useState<Menu[]>([]);
  const [expanded, setExpanded] = useState<React.Key[]>([]);
  const [rowIds, setRowIds] = useState<number[]>([]);
  const [isOpening, setIsOpening] = useState(false);
  const { message } = App.useApp();
  const { can } = usePermission();
  const queryClient = useQueryClient();
  const initialize = useSession((state) => state.initialize);

  async function refreshMenus() {
    await queryClient.invalidateQueries({ queryKey: menuKeys.all });
    action.current?.reload();
    await queryClient.invalidateQueries({ queryKey: ['session', 'menus'] });
    try {
      await initialize();
    } catch (error) {
      message.error(`菜单已保存，但导航刷新失败：${String(error)}`);
    }
  }

  const save = useMutation({
    mutationFn: (values: CreateMenuRequest) =>
      editingId === undefined
        ? createMenu(menuInput(values))
        : updateMenu({ ...menuInput(values), menuId: editingId }),
  });
  const remove = useMutation({ mutationFn: deleteMenu });

  async function showForm(record?: Menu, parentId = 0) {
    setIsOpening(true);
    try {
      const [items, detail] = await Promise.all([
        queryClient.fetchQuery({ queryKey: menuKeys.list(), queryFn: () => getMenuList() }),
        record
          ? queryClient.fetchQuery({
              queryKey: menuKeys.detail(record.menuId),
              queryFn: () => getMenuDetail(record.menuId),
            })
          : Promise.resolve(undefined),
      ]);
      setEditingId(detail?.menuId);
      setParents(menuParents(items, detail?.menuId));
      setInitial(
        detail
          ? menuInput(detail)
          : {
              menuName: '',
              parentId,
              orderNum: 0,
              menuType: 'M',
              isFrame: '1',
              isCache: '0',
              visible: '0',
              status: SystemStatus.Normal,
              icon: '#',
            },
      );
    } catch (error) {
      message.error(String(error));
    } finally {
      setIsOpening(false);
    }
  }

  const columns: ProColumns<Menu>[] = [
    { title: '菜单名称', dataIndex: 'menuName' },
    {
      title: '类型',
      dataIndex: 'menuType',
      search: false,
      valueEnum: { M: '目录', C: '菜单', F: '按钮' },
    },
    { title: '排序', dataIndex: 'orderNum', search: false },
    { title: '权限标识', dataIndex: 'perms', search: false },
    { title: '组件注册键', dataIndex: 'component', search: false },
    {
      title: '状态',
      dataIndex: 'status',
      valueEnum: systemStatusEnum,
    },
    {
      title: '操作',
      valueType: 'option',
      render: (_, record) => [
        can('system:menu:edit') && (
          <Button key="edit" type="link" disabled={isOpening} onClick={() => showForm(record)}>
            修改
          </Button>
        ),
        record.menuType !== 'F' && can('system:menu:add') && (
          <Button
            key="add"
            type="link"
            disabled={isOpening}
            onClick={() => showForm(undefined, record.menuId)}
          >
            新增子菜单
          </Button>
        ),
        can('system:menu:remove') && (
          <Popconfirm
            key="remove"
            title="确认删除该菜单？"
            onConfirm={async () => {
              try {
                await remove.mutateAsync(record.menuId);
                message.success('删除成功');
                await refreshMenus();
              } catch (error) {
                message.error(String(error));
              }
            }}
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
      <ProTable<Menu, MenuQuery>
        search={{ labelWidth: 'auto' }}
        rowKey="menuId"
        actionRef={action}
        columns={columns}
        scroll={{ x: 'max-content' }}
        pagination={false}
        expandable={{
          expandedRowKeys: expanded,
          onExpandedRowsChange: (keys) => setExpanded([...keys]),
        }}
        request={async (params) => {
          const filters = { menuName: params.menuName, status: params.status };
          const items = await queryClient.fetchQuery({
            queryKey: menuKeys.list(filters),
            queryFn: () => getMenuList(filters),
          });
          setRowIds(items.map((item) => item.menuId));
          return { data: menuTree(items), success: true };
        }}
        onRequestError={(error) => message.error(error.message)}
        toolBarRender={() => [
          can('system:menu:add') && (
            <Button key="add" type="primary" loading={isOpening} onClick={() => showForm()}>
              新增
            </Button>
          ),
          <Button key="expand" onClick={() => setExpanded(expanded.length ? [] : rowIds)}>
            {expanded.length ? '折叠全部' : '展开全部'}
          </Button>,
        ]}
      />
      {initial && (
        <ModalForm<CreateMenuRequest>
          description="配置菜单层级、页面入口与访问权限"
          title={editingId === undefined ? '新增菜单' : '修改菜单'}
          open
          initialValues={initial}
          // 保留显式清空的字段，避免合并 initial 时重新提交旧值。
          omitNil={false}
          onOpenChange={(open) => {
            if (!open) setInitial(undefined);
          }}
          modalProps={{ destroyOnHidden: true, mask: { closable: false } }}
          submitter={{ submitButtonProps: { loading: save.isPending } }}
          onFinish={async (values) => {
            try {
              // 条件表单未挂载的字段仍按原配置提交，避免目录或按钮丢失协议默认值。
              await save.mutateAsync({ ...initial, ...values });
              message.success('保存成功');
              setInitial(undefined);
              await refreshMenus();
              return true;
            } catch (error) {
              message.error(String(error));
              return false;
            }
          }}
        >
          <ProFormTreeSelect
            name="parentId"
            label="上级菜单"
            rules={[{ required: true }]}
            fieldProps={{
              treeData: [{ menuId: 0, menuName: '主目录', children: parents }],
              fieldNames: { label: 'menuName', value: 'menuId', children: 'children' },
              treeDefaultExpandAll: true,
            }}
          />
          <ProFormRadio.Group
            name="menuType"
            label="菜单类型"
            rules={[{ required: true }]}
            options={[
              { label: '目录', value: 'M' },
              { label: '菜单', value: 'C' },
              { label: '按钮', value: 'F' },
            ]}
          />
          <ProFormText
            name="menuName"
            label="菜单名称"
            rules={[{ required: true, whitespace: true }]}
          />
          <ProFormDigit
            name="orderNum"
            label="显示排序"
            min={0}
            rules={[{ required: true }]}
            fieldProps={{ precision: 0 }}
          />
          <ProFormDependency name={['menuType', 'isFrame']}>
            {({ menuType, isFrame }) => (
              <>
                {menuType !== 'F' && (
                  <>
                    <ProFormText name="icon" label="图标标识" />
                    <ProFormRadio.Group
                      name="isFrame"
                      label="是否外链"
                      options={[
                        { label: '是', value: '0' },
                        { label: '否', value: '1' },
                      ]}
                    />
                    <ProFormText
                      name="path"
                      label="路由地址"
                      rules={[
                        { required: true },
                        ...(isFrame === '0'
                          ? [
                              {
                                pattern: /^https?:\/\//,
                                message: '外链必须以 http:// 或 https:// 开头',
                              },
                            ]
                          : []),
                      ]}
                    />
                    <ProFormRadio.Group
                      name="visible"
                      label="显示状态"
                      options={[
                        { label: '显示', value: '0' },
                        { label: '隐藏', value: '1' },
                      ]}
                    />
                  </>
                )}
                {menuType === 'C' && (
                  <>
                    <ProFormText
                      name="component"
                      label="组件注册键"
                      placeholder="例如 system/menu/index"
                    />
                    <ProFormText name="routeName" label="路由名称" />
                    <ProFormText name="query" label="路由参数" fieldProps={{ maxLength: 255 }} />
                    <ProFormRadio.Group
                      name="isCache"
                      label="是否缓存"
                      tooltip="保留后端配置，当前 React 路由不实现页面保活。"
                      options={[
                        { label: '缓存', value: '0' },
                        { label: '不缓存', value: '1' },
                      ]}
                    />
                  </>
                )}
                {menuType !== 'M' && (
                  <ProFormText name="perms" label="权限标识" fieldProps={{ maxLength: 100 }} />
                )}
              </>
            )}
          </ProFormDependency>
          <ProFormRadio.Group name="status" label="菜单状态" options={systemStatusOptions} />
        </ModalForm>
      )}
    </PageContainer>
  );
}
