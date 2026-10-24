import { useRef, useState } from 'react';
import {
  PageContainer,
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
  ProTable,
} from '@ant-design/pro-components';
import { App, Button, Popconfirm } from 'antd';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { SystemStatus, systemStatusEnum, systemStatusOptions } from '@/modules/system/constants';
import { usePermission } from '@/shared/hooks/usePermission';
import {
  createDictType,
  deleteDictType,
  getDictTypeDetail,
  getDictTypeList,
  refreshDictCache,
  updateDictType,
} from '../api/dictApi';
import { DictDataDrawer } from '../components/DictDataDrawer';
import { dictKeys } from '../hooks/dictKeys';
import { AdminModalForm as ModalForm } from '@/shared/components/AdminModalForm';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import type { CreateDictTypeRequest, DictType } from '../types';

export default function DictPage() {
  const action = useRef<ActionType | undefined>(undefined);
  const queryClient = useQueryClient();
  const { message } = App.useApp();
  const { can } = usePermission();
  const [initial, setInitial] = useState<CreateDictTypeRequest>();
  const [editingId, setEditingId] = useState<number>();
  const [dictionary, setDictionary] = useState<DictType>();
  const [selected, setSelected] = useState<number[]>([]);
  const [isOpening, setIsOpening] = useState(false);
  const save = useMutation({
    mutationFn: (values: CreateDictTypeRequest) =>
      editingId === undefined
        ? createDictType(values)
        : updateDictType({ ...values, dictId: editingId }),
  });
  const remove = useMutation({ mutationFn: deleteDictType });
  const cache = useMutation({ mutationFn: refreshDictCache });
  async function refresh() {
    await queryClient.invalidateQueries({ queryKey: dictKeys.all });
    setSelected([]);
    action.current?.reload();
  }
  async function showForm(row?: DictType) {
    setIsOpening(true);
    try {
      const detail = row
        ? await queryClient.fetchQuery({
            queryKey: dictKeys.type(row.dictId),
            queryFn: () => getDictTypeDetail(row.dictId),
          })
        : undefined;
      setEditingId(detail?.dictId);
      setInitial(detail ?? { dictName: '', dictType: '', status: SystemStatus.Normal });
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
  const columns: ProColumns<DictType>[] = [
    { title: '字典名称', dataIndex: 'dictName' },
    {
      title: '字典类型',
      dataIndex: 'dictType',
      render: (_, row) => (
        <Button type="link" onClick={() => setDictionary(row)}>
          {row.dictType}
        </Button>
      ),
    },
    { title: '状态', dataIndex: 'status', valueEnum: systemStatusEnum },
    { title: '备注', dataIndex: 'remark', search: false },
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
            title="确认删除该字典？"
            onConfirm={() => handleDelete([row.dictId])}
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
      <ProTable<DictType>
        search={{ labelWidth: 'auto' }}
        rowKey="dictId"
        actionRef={action}
        columns={columns}
        scroll={{ x: 'max-content' }}
        rowSelection={{
          selectedRowKeys: selected,
          onChange: (keys) => setSelected(keys.map(Number)),
        }}
        request={(params) => {
          const filters = {
            pageNum: params.current,
            pageSize: params.pageSize,
            dictName: params.dictName,
            dictType: params.dictType,
            status: params.status,
          };
          return queryClient.fetchQuery({
            queryKey: dictKeys.types(filters),
            queryFn: () => getDictTypeList(filters),
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
              title="确认删除所选字典？"
              disabled={!selected.length}
              onConfirm={() => handleDelete(selected)}
            >
              <Button danger disabled={!selected.length} loading={remove.isPending}>
                批量删除
              </Button>
            </Popconfirm>
          ),
          can('system:dict:remove') && (
            <Button
              key="cache"
              loading={cache.isPending}
              onClick={async () => {
                try {
                  await cache.mutateAsync();
                  await refresh();
                  message.success('缓存已刷新');
                } catch (error) {
                  message.error(String(error));
                }
              }}
            >
              刷新缓存
            </Button>
          ),
        ]}
      />
      {initial && (
        <ModalForm<CreateDictTypeRequest>
          description="维护字典名称、类型与状态"
          open
          title={editingId === undefined ? '新增字典类型' : '修改字典类型'}
          initialValues={initial}
          onOpenChange={(open) => {
            if (!open) setInitial(undefined);
          }}
          modalProps={{ destroyOnHidden: true, mask: { closable: false } }}
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
          <ProFormText
            name="dictName"
            label="字典名称"
            rules={[{ required: true, whitespace: true }]}
          />
          <ProFormText
            name="dictType"
            label="字典类型"
            rules={[{ required: true, whitespace: true }]}
          />
          <ProFormSelect name="status" label="状态" options={systemStatusOptions} />
          <ProFormTextArea name="remark" label="备注" />
        </ModalForm>
      )}
      {dictionary && (
        <DictDataDrawer
          key={dictionary.dictId}
          dictionary={dictionary}
          onClose={() => setDictionary(undefined)}
        />
      )}
    </PageContainer>
  );
}
