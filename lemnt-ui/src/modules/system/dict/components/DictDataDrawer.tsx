import { useRef, useState } from 'react';
import {
  ProFormDigit,
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
  ProTable,
} from '@ant-design/pro-components';
import { App, Button, Drawer, Popconfirm, Tag } from 'antd';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { SystemStatus, systemStatusEnum, systemStatusOptions } from '@/modules/system/constants';
import { usePermission } from '@/shared/hooks/usePermission';
import {
  createDictData,
  deleteDictData,
  getDictDataDetail,
  getDictDataList,
  updateDictData,
} from '../api/dictApi';
import { dictKeys } from '../hooks/dictKeys';
import { AdminModalForm as ModalForm } from '@/shared/components/AdminModalForm';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import type { CreateDictDataRequest, DictData, DictType } from '../types';

interface DictDataDrawerProps {
  dictionary: DictType;
  onClose: () => void;
}
const colors: Record<string, string | undefined> = {
  default: undefined,
  primary: 'blue',
  success: 'success',
  info: 'default',
  warning: 'warning',
  danger: 'error',
};

export function DictDataDrawer({ dictionary, onClose }: DictDataDrawerProps) {
  const action = useRef<ActionType | undefined>(undefined);
  const [initial, setInitial] = useState<CreateDictDataRequest>();
  const [editingId, setEditingId] = useState<number>();
  const [selected, setSelected] = useState<number[]>([]);
  const [isOpening, setIsOpening] = useState(false);
  const { message } = App.useApp();
  const { can } = usePermission();
  const queryClient = useQueryClient();
  const save = useMutation({
    mutationFn: (values: CreateDictDataRequest) =>
      editingId === undefined
        ? createDictData(values)
        : updateDictData({ ...values, dictCode: editingId }),
  });
  const remove = useMutation({ mutationFn: deleteDictData });
  async function refresh() {
    await queryClient.invalidateQueries({ queryKey: dictKeys.all });
    setSelected([]);
    action.current?.reload();
  }
  async function showForm(record?: DictData) {
    setIsOpening(true);
    try {
      const detail = record
        ? await queryClient.fetchQuery({
            queryKey: dictKeys.detail(record.dictCode),
            queryFn: () => getDictDataDetail(record.dictCode),
          })
        : undefined;
      setEditingId(detail?.dictCode);
      setInitial(
        detail ?? {
          dictSort: 0,
          dictLabel: '',
          dictValue: '',
          dictType: dictionary.dictType,
          isDefault: '0',
          status: SystemStatus.Normal,
          listClass: 'default',
        },
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
  const columns: ProColumns<DictData>[] = [
    {
      title: '字典标签',
      dataIndex: 'dictLabel',
      render: (_, row) => <Tag color={colors[row.listClass ?? 'default']}>{row.dictLabel}</Tag>,
    },
    { title: '字典键值', dataIndex: 'dictValue', search: false },
    { title: '排序', dataIndex: 'dictSort', search: false },
    { title: '默认', dataIndex: 'isDefault', search: false, valueEnum: { '1': '是', '0': '否' } },
    { title: '状态', dataIndex: 'status', valueEnum: systemStatusEnum },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) => [
        can('system:dict:edit') && (
          <Button key="edit" type="link" disabled={isOpening} onClick={() => showForm(row)}>
            修改
          </Button>
        ),
        can('system:dict:remove') && (
          <Popconfirm
            key="remove"
            title="确认删除该字典数据？"
            onConfirm={() => handleDelete([row.dictCode])}
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
    <Drawer title={`${dictionary.dictName}：字典数据`} open size={1000} onClose={onClose}>
      <ProTable<DictData>
        rowKey="dictCode"
        actionRef={action}
        columns={columns}
        scroll={{ x: 'max-content' }}
        rowSelection={{
          selectedRowKeys: selected,
          onChange: (keys) => setSelected(keys.map(Number)),
        }}
        request={(params) => {
          const filters = {
            dictType: dictionary.dictType,
            dictLabel: params.dictLabel,
            status: params.status,
            pageNum: params.current,
            pageSize: params.pageSize,
          };
          return queryClient.fetchQuery({
            queryKey: dictKeys.data(filters),
            queryFn: () => getDictDataList(filters),
          });
        }}
        onRequestError={(error) => message.error(error.message)}
        toolBarRender={() => [
          can('system:dict:add') && (
            <Button key="add" type="primary" loading={isOpening} onClick={() => showForm()}>
              新增
            </Button>
          ),
          can('system:dict:remove') && (
            <Popconfirm
              key="delete"
              title="确认删除所选字典数据？"
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
        <ModalForm<CreateDictDataRequest>
          description="维护字典标签、键值与显示样式"
          open
          title={editingId === undefined ? '新增字典数据' : '修改字典数据'}
          initialValues={initial}
          modalProps={{ destroyOnHidden: true, mask: { closable: false } }}
          onOpenChange={(open) => {
            if (!open) setInitial(undefined);
          }}
          submitter={{ submitButtonProps: { loading: save.isPending } }}
          onFinish={async (values) => {
            try {
              await save.mutateAsync(values);
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
          <ProFormText name="dictType" label="字典类型" disabled />
          <ProFormText
            name="dictLabel"
            label="字典标签"
            rules={[{ required: true, whitespace: true }]}
          />
          <ProFormText
            name="dictValue"
            label="字典键值"
            rules={[{ required: true, whitespace: true }]}
          />
          <ProFormDigit
            name="dictSort"
            label="排序"
            min={0}
            fieldProps={{ precision: 0 }}
            rules={[{ required: true }]}
          />
          <ProFormText
            name="cssClass"
            label="样式标识"
            tooltip="保留原配置；旧 Vue CSS 类不会在 React 中直接生效。"
          />
          <ProFormSelect
            name="listClass"
            label="回显样式"
            options={Object.keys(colors).map((value) => ({ value, label: value }))}
          />
          <ProFormSelect
            name="isDefault"
            label="默认"
            options={[
              { label: '是', value: '1' },
              { label: '否', value: '0' },
            ]}
          />
          <ProFormSelect name="status" label="状态" options={systemStatusOptions} />
          <ProFormTextArea name="remark" label="备注" />
        </ModalForm>
      )}
    </Drawer>
  );
}
