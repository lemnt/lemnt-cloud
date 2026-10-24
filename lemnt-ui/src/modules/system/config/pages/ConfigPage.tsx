import { useRef, useState } from 'react';
import {
  PageContainer,
  ProFormRadio,
  ProFormText,
  ProFormTextArea,
  ProTable,
  type ActionType,
  type ProColumns,
} from '@ant-design/pro-components';
import { App, Button, Popconfirm } from 'antd';
import { request } from '@/api/request';
import { usePermission } from '@/shared/hooks/usePermission';
import { AdminModalForm as ModalForm } from '@/shared/components/AdminModalForm';

interface Config {
  configId?: number;
  configName: string;
  configKey: string;
  configValue: string;
  configType: string;
  remark?: string;
  createTime?: string;
}

export default function ConfigPage() {
  const action = useRef<ActionType | undefined>(undefined);
  const [editing, setEditing] = useState<Config>();
  const [open, setOpen] = useState(false);
  const [selected, setSelected] = useState<React.Key[]>([]);
  const { message } = App.useApp();
  const permissions = usePermission();
  const can = (operation: string) => permissions.can(`system:config:${operation}`);
  async function edit(record: Config) {
    try {
      setEditing(
        (await request<Config>({ url: `/system/configurations/${record.configId}` })).data,
      );
      setOpen(true);
    } catch (reason) {
      message.error(String(reason));
    }
  }
  async function remove(ids: React.Key[]) {
    try {
      await request({ url: `/system/configurations/${ids.join(',')}`, method: 'DELETE' });
      message.success('删除成功');
      setSelected([]);
      action.current?.reload();
    } catch (reason) {
      message.error(String(reason));
    }
  }
  const columns: ProColumns<Config>[] = [
    { title: '参数编号', dataIndex: 'configId', search: false },
    { title: '参数名称', dataIndex: 'configName' },
    { title: '参数键名', dataIndex: 'configKey' },
    { title: '参数键值', dataIndex: 'configValue', search: false, ellipsis: true },
    { title: '系统内置', dataIndex: 'configType', valueEnum: { '1': '是', '0': '否' } },
    { title: '创建时间', dataIndex: 'createTime', valueType: 'dateTime', search: false },
    { title: '创建时间', dataIndex: 'dateRange', valueType: 'dateRange', hideInTable: true },
    { title: '备注', dataIndex: 'remark', search: false, ellipsis: true },
    {
      title: '操作',
      valueType: 'option',
      render: (_, record) => [
        can('edit') && (
          <Button key="edit" type="link" onClick={() => edit(record)}>
            修改
          </Button>
        ),
        can('remove') && (
          <Popconfirm
            key="delete"
            title="确认删除该参数？"
            onConfirm={() => remove([record.configId!])}
          >
            <Button type="link" danger>
              删除
            </Button>
          </Popconfirm>
        ),
      ],
    },
  ];
  return (
    <PageContainer title={false}>
      <ProTable<Config>
        search={{ labelWidth: 'auto' }}
        rowKey="configId"
        actionRef={action}
        columns={columns}
        scroll={{ x: 'max-content' }}
        pagination={{ defaultPageSize: 10 }}
        request={async ({ current, pageSize, dateRange, ...filters }) => {
          const result = await request<Config[]>({
            url: '/system/configurations',
            params: {
              ...filters,
              pageNum: current,
              pageSize,
              ...(dateRange
                ? { beginTime: `${dateRange[0]}T00:00:00`, endTime: `${dateRange[1]}T23:59:59` }
                : {}),
            },
          });
          return { data: result.rows || [], total: result.total || 0, success: true };
        }}
        onRequestError={(reason) => message.error(reason.message)}
        rowSelection={{ selectedRowKeys: selected, onChange: setSelected }}
        toolBarRender={() => [
          can('add') && (
            <Button
              key="add"
              type="primary"
              onClick={() => {
                setEditing(undefined);
                setOpen(true);
              }}
            >
              新增
            </Button>
          ),
          can('remove') && (
            <Popconfirm
              key="delete"
              title="确认删除选中的参数？"
              onConfirm={() => remove(selected)}
            >
              <Button danger disabled={!selected.length}>
                批量删除
              </Button>
            </Popconfirm>
          ),
          can('remove') && (
            <Button
              key="refresh"
              onClick={async () => {
                try {
                  await request({ url: '/system/configurations/refresh-cache', method: 'DELETE' });
                  message.success('缓存已刷新');
                } catch (reason) {
                  message.error(String(reason));
                }
              }}
            >
              刷新缓存
            </Button>
          ),
        ]}
      />
      <ModalForm<Config>
        description="维护参数名称、键值与配置说明"
        title={editing ? '修改参数' : '新增参数'}
        open={open}
        onOpenChange={setOpen}
        initialValues={editing || { configType: '0' }}
        modalProps={{ destroyOnHidden: true }}
        onFinish={async (values) => {
          try {
            await request({
              url: '/system/configurations',
              method: editing ? 'PUT' : 'POST',
              data: { ...values, ...(editing ? { configId: editing.configId } : {}) },
            });
            message.success('保存成功');
            action.current?.reload();
            return true;
          } catch (reason) {
            message.error(String(reason));
            return false;
          }
        }}
      >
        <ProFormText
          name="configName"
          label="参数名称"
          rules={[{ required: true }]}
          fieldProps={{ maxLength: 100 }}
        />
        <ProFormText
          name="configKey"
          label="参数键名"
          rules={[{ required: true }]}
          fieldProps={{ maxLength: 100 }}
        />
        <ProFormText
          name="configValue"
          label="参数键值"
          rules={[{ required: true }]}
          fieldProps={{ maxLength: 500 }}
        />
        <ProFormRadio.Group
          name="configType"
          label="系统内置"
          options={[
            { label: '是', value: '1' },
            { label: '否', value: '0' },
          ]}
        />
        <ProFormTextArea name="remark" label="备注" fieldProps={{ maxLength: 500 }} />
      </ModalForm>
    </PageContainer>
  );
}
