import { useRef, useState } from 'react';
import {
  PageContainer,
  ProFormDigit,
  ProFormRadio,
  ProFormText,
  ProFormTreeSelect,
  ProTable,
  type ActionType,
  type ProColumns,
} from '@ant-design/pro-components';
import { App, Button, InputNumber, Popconfirm } from 'antd';
import { useQueryClient } from '@tanstack/react-query';
import { SystemStatus, systemStatusEnum, systemStatusOptions } from '@/modules/system/constants';
import { usePermission } from '@/shared/hooks/usePermission';
import { deptKeys } from '../hooks/deptKeys';
import {
  deleteDepartment,
  getDepartment,
  listDepartments,
  parentDepartments,
  saveDepartment,
  saveDepartmentOrder,
} from '../api/deptApi';
import { deptTree, type Dept, type DeptInput, type DeptNode } from '../types';
import { AdminModalForm as ModalForm } from '@/shared/components/AdminModalForm';

export default function DeptPage() {
  const action = useRef<ActionType | undefined>(undefined);
  const [editing, setEditing] = useState<Dept>();
  const [initial, setInitial] = useState<Partial<DeptInput>>();
  const [parents, setParents] = useState<DeptNode[]>([]);
  const [open, setOpen] = useState(false);
  const [orders, setOrders] = useState<Record<string, number>>({});
  const [expanded, setExpanded] = useState<React.Key[]>([]);
  const [rows, setRows] = useState<Dept[]>([]);
  const [saving, setSaving] = useState(false);
  const [isOpening, setIsOpening] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [deletingId, setDeletingId] = useState<number>();
  const operation = useRef(false);
  const isBusy = saving || isOpening || isSubmitting || deletingId !== undefined;
  const { message } = App.useApp();
  const queryClient = useQueryClient();
  const permissions = usePermission();
  const can = (operation: string) => permissions.can(`system:dept:${operation}`);
  async function refreshDepartments() {
    try {
      await queryClient.invalidateQueries({ queryKey: deptKeys.all });
      await action.current?.reload();
    } catch (reason) {
      message.error(`操作已成功，但部门列表刷新失败：${String(reason)}`);
    }
  }

  async function showForm(record?: Dept, parentId?: number) {
    if (operation.current) return;
    operation.current = true;
    setIsOpening(true);
    try {
      const [detail, options] = await Promise.all([
        record
          ? queryClient.fetchQuery({
              queryKey: deptKeys.detail(record.deptId),
              queryFn: () => getDepartment(record.deptId),
            })
          : Promise.resolve(undefined),
        queryClient.fetchQuery({
          queryKey: deptKeys.parents(record?.deptId),
          queryFn: () => parentDepartments(record?.deptId),
        }),
      ]);
      setEditing(detail);
      setInitial(
        detail || {
          parentId: parentId ?? options[0]?.deptId ?? 0,
          orderNum: 0,
          status: SystemStatus.Normal,
        },
      );
      setParents(deptTree(options));
      setOpen(true);
    } catch (reason) {
      message.error(String(reason));
    } finally {
      operation.current = false;
      setIsOpening(false);
    }
  }

  const columns: ProColumns<DeptNode>[] = [
    { title: '部门名称', dataIndex: 'deptName' },
    {
      title: '排序',
      dataIndex: 'orderNum',
      search: false,
      render: (_, record) => (
        <InputNumber
          min={0}
          precision={0}
          disabled={!can('edit') || isBusy}
          value={orders[record.deptId] ?? record.orderNum}
          onChange={(value) => {
            if (value !== null) setOrders((current) => ({ ...current, [record.deptId]: value }));
          }}
        />
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      valueEnum: systemStatusEnum,
    },
    { title: '创建时间', dataIndex: 'createTime', valueType: 'dateTime', search: false },
    {
      title: '操作',
      valueType: 'option',
      render: (_, record) => [
        can('edit') && (
          <Button key="edit" type="link" disabled={isBusy} onClick={() => showForm(record)}>
            修改
          </Button>
        ),
        can('add') && (
          <Button
            key="add"
            type="link"
            disabled={isBusy}
            onClick={() => showForm(undefined, record.deptId)}
          >
            新增子部门
          </Button>
        ),
        record.parentId !== 0 && can('remove') && (
          <Popconfirm
            key="delete"
            title="确认删除该部门？"
            disabled={isBusy}
            onConfirm={async () => {
              if (operation.current) return;
              operation.current = true;
              setDeletingId(record.deptId);
              try {
                await deleteDepartment(record.deptId);
                message.success('删除成功');
                await refreshDepartments();
              } catch (reason) {
                message.error(String(reason));
              } finally {
                operation.current = false;
                setDeletingId(undefined);
              }
            }}
          >
            <Button danger type="link" disabled={isBusy} loading={deletingId === record.deptId}>
              删除
            </Button>
          </Popconfirm>
        ),
      ],
    },
  ];

  return (
    <PageContainer title={false}>
      <ProTable<DeptNode>
        search={{ labelWidth: 'auto' }}
        rowKey="deptId"
        actionRef={action}
        columns={columns}
        scroll={{ x: 'max-content' }}
        pagination={false}
        expandable={{
          expandedRowKeys: expanded,
          onExpandedRowsChange: (keys) => setExpanded([...keys]),
        }}
        request={async (filters) => {
          const query = { deptName: filters.deptName, status: filters.status };
          const data = await queryClient.fetchQuery({
            queryKey: deptKeys.list(query),
            queryFn: () => listDepartments(query),
          });
          setRows(data);
          setOrders({});
          return { data: deptTree(data), success: true };
        }}
        onRequestError={(reason) => message.error(reason.message)}
        toolBarRender={() => [
          can('add') && (
            <Button
              key="add"
              type="primary"
              disabled={isBusy}
              loading={isOpening}
              onClick={() => showForm()}
            >
              新增
            </Button>
          ),
          <Button
            key="expand"
            onClick={() => setExpanded(expanded.length ? [] : rows.map((row) => row.deptId))}
          >
            {expanded.length ? '折叠全部' : '展开全部'}
          </Button>,
          can('edit') && (
            <Button
              key="sort"
              loading={saving}
              disabled={isBusy || !Object.keys(orders).length}
              onClick={async () => {
                if (operation.current) return;
                operation.current = true;
                setSaving(true);
                try {
                  await saveDepartmentOrder(orders);
                  message.success('排序已保存');
                  await refreshDepartments();
                } catch (reason) {
                  message.error(String(reason));
                } finally {
                  operation.current = false;
                  setSaving(false);
                }
              }}
            >
              保存排序
            </Button>
          ),
        ]}
      />
      {open && (
        <ModalForm<DeptInput>
          description="设置部门层级、负责人和联系信息"
          title={editing ? '修改部门' : '新增部门'}
          open={open}
          onOpenChange={(nextOpen) => {
            if (!operation.current) setOpen(nextOpen);
          }}
          initialValues={initial}
          disabled={isSubmitting}
          modalProps={{
            destroyOnHidden: true,
            mask: { closable: false },
            closable: !isSubmitting,
            keyboard: !isSubmitting,
          }}
          submitter={{
            submitButtonProps: { loading: isSubmitting, disabled: isBusy },
            resetButtonProps: { disabled: isBusy },
          }}
          onFinish={async (values) => {
            if (operation.current) return false;
            operation.current = true;
            setIsSubmitting(true);
            try {
              await saveDepartment(values, editing);
              setOpen(false);
              message.success('保存成功');
              await refreshDepartments();
              return true;
            } catch (reason) {
              message.error(String(reason));
              return false;
            } finally {
              operation.current = false;
              setIsSubmitting(false);
            }
          }}
        >
          <ProFormTreeSelect
            name="parentId"
            label="上级部门"
            rules={[{ required: true }]}
            fieldProps={{
              treeData: [{ deptId: 0, deptName: '根部门', children: parents }],
              fieldNames: { label: 'deptName', value: 'deptId', children: 'children' },
              treeDefaultExpandAll: true,
              disabled: editing?.parentId === 0,
            }}
          />
          <ProFormText
            name="deptName"
            label="部门名称"
            rules={[{ required: true }]}
            fieldProps={{ maxLength: 30 }}
          />
          <ProFormDigit
            name="orderNum"
            label="显示排序"
            rules={[{ required: true }]}
            min={0}
            fieldProps={{ precision: 0 }}
          />
          <ProFormText name="leader" label="负责人" fieldProps={{ maxLength: 20 }} />
          <ProFormText
            name="phone"
            label="联系电话"
            rules={[{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码' }]}
            fieldProps={{ maxLength: 11 }}
          />
          <ProFormText
            name="email"
            label="邮箱"
            rules={[{ type: 'email' }]}
            fieldProps={{ maxLength: 50 }}
          />
          <ProFormRadio.Group name="status" label="状态" options={systemStatusOptions} />
        </ModalForm>
      )}
    </PageContainer>
  );
}
