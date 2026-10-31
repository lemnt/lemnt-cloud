import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  PageContainer,
  ProTable,
  ProFormText,
  ProFormTextArea,
  type ProColumns,
} from '@ant-design/pro-components';
import { Alert, App, Button, Popconfirm, Result } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { AdminModalForm } from '@/shared/components/AdminModalForm';
import { usePermission } from '@/shared/hooks/usePermission';
import { catalogKeys } from '../hooks/catalogKeys';
import * as api from '../api/specApi';

/** 商品规格项维护入口。 */
export default function SpecPage() {
  return <SpecManager />;
}
/** 规格值从规格项进入，固定归属，不允许编辑时换到其他规格。 */
export function SpecValuePage() {
  const [params] = useSearchParams();
  let parentId: string;
  try {
    parentId = api.specId(params.get('specId'));
  } catch {
    return <Result status="warning" title="请从商品规格管理选择一个有效规格" />;
  }
  return <SpecManager key={parentId} parentId={parentId} />;
}
/** 两种字典各用独立接口及权限，只共享表格与编辑交互。 */
function SpecManager({ parentId }: { parentId?: string }) {
  const valueMode = !!parentId,
    prefix = valueMode ? 'mall:spec-value' : 'mall:spec',
    title = valueMode ? '规格值' : '商品规格';
  const { can } = usePermission(),
    { message } = App.useApp(),
    cache = useQueryClient(),
    navigate = useNavigate();
  const [filters, setFilters] = useState<
    Pick<api.SpecFilter, 'name' | 'createTimeStart' | 'createTimeEnd'>
  >({});
  const [page, setPage] = useState({ pageNum: 1, pageSize: 20 });
  const [editor, setEditor] = useState<{ id: string | null; readOnly: boolean }>();
  const rows = useQuery({
    queryKey: ['product-specs', prefix, parentId, filters, page],
    queryFn: () =>
      parentId
        ? api.getSpecValues({ ...filters, ...page, specId: parentId })
        : api.getSpecs({ ...filters, ...page }),
    enabled: can(prefix + ':list'),
  });
  const detail = useQuery({
    queryKey: ['product-specs', prefix, 'detail', editor?.id],
    queryFn: () =>
      parentId ? api.getSpecValue(editor?.id ?? '', parentId) : api.getSpec(editor?.id ?? ''),
    enabled: !!editor?.id && can(prefix + ':query'),
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
  });
  const parent = useQuery({
    queryKey: ['product-specs', 'parent', parentId],
    queryFn: () => api.getSpec(parentId ?? ''),
    enabled: !!parentId && can('mall:spec:query'),
  });
  const save = useMutation({
    mutationFn: (v: api.SpecValues) =>
      parentId
        ? api.saveSpecValue(editor?.id ?? null, parentId, v)
        : api.saveSpec(editor?.id ?? null, v),
  });
  const remove = useMutation({
    mutationFn: (id: string) => (parentId ? api.deleteSpecValue(id) : api.deleteSpec(id)),
  });
  async function refresh() {
    try {
      await Promise.all([
        cache.invalidateQueries({ queryKey: ['product-specs'] }, { throwOnError: true }),
        cache.invalidateQueries({ queryKey: catalogKeys.all }, { throwOnError: true }),
      ]);
    } catch {
      message.warning('操作已成功，刷新失败请重试');
    }
  }
  const columns: ProColumns<api.ProductSpec>[] = [
    { title: '编号', dataIndex: 'id', search: false },
    { title: '名称', dataIndex: 'name' },
    { title: '备注', dataIndex: 'remark', search: false, render: (_, row) => row.remark || '—' },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      search: false,
      render: (_, row) => row.createTime || '—',
    },
    {
      title: '创建时间范围',
      dataIndex: 'createTimeRange',
      valueType: 'dateTimeRange',
      hideInTable: true,
      search: valueMode
        ? false
        : {
            transform: (range: string[]) => ({
              createTimeStart: range[0]?.replace(' ', 'T'),
              createTimeEnd: range[1]?.replace(' ', 'T'),
            }),
          },
    },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) => (
        <>
          {can(prefix + ':query') && (
            <Button type="link" onClick={() => setEditor({ id: row.id, readOnly: true })}>
              详情
            </Button>
          )}
          {can(prefix + ':edit') && can(prefix + ':query') && (
            <Button type="link" onClick={() => setEditor({ id: row.id, readOnly: false })}>
              编辑
            </Button>
          )}
          {!valueMode && can('mall:spec-value:list') && (
            <Button
              type="link"
              onClick={() => navigate('/mall/spec-values?specId=' + encodeURIComponent(row.id))}
            >
              规格值
            </Button>
          )}
          {can(prefix + ':remove') && (
            <Popconfirm
              title={
                valueMode
                  ? '确认删除规格值？现有商品和订单快照保留。'
                  : '确认删除规格？仍包含规格值时不能删除。'
              }
              onConfirm={async () => {
                if (remove.isPending || !can(prefix + ':remove')) return;
                try {
                  await remove.mutateAsync(row.id);
                  message.success('已删除');
                  await refresh();
                } catch (e) {
                  message.error(String(e));
                }
              }}
            >
              <Button type="link" danger disabled={remove.isPending}>
                删除
              </Button>
            </Popconfirm>
          )}
        </>
      ),
    },
  ];
  if (!can(prefix + ':list')) return <Result status="403" title={`没有${title}列表权限`} />;
  const initial = editor?.id ? detail.data : { name: '', remark: '' };
  return (
    <PageContainer title={title}>
      {parentId && (
        <Alert
          type="info"
          title={`所属规格：${parent.data?.name ? parent.data.name + '（' + parentId + '）' : parentId}`}
          action={<Button onClick={() => navigate('/mall/specs')}>返回规格管理</Button>}
        />
      )}
      {parent.isError && (
        <Alert
          type="warning"
          title="规格名称读取失败"
          action={<Button onClick={() => parent.refetch()}>重试</Button>}
        />
      )}
      {rows.isError && (
        <Alert
          type="error"
          title="列表读取失败"
          description={String(rows.error)}
          action={<Button onClick={() => rows.refetch()}>重试</Button>}
        />
      )}
      <ProTable<api.ProductSpec>
        dateFormatter="string"
        options={{ reload: false }}
        rowKey="id"
        columns={columns}
        dataSource={rows.data?.records ?? []}
        loading={rows.isFetching}
        scroll={{ x: 1000 }}
        toolBarRender={() => [
          <Button key="refresh" onClick={() => rows.refetch()}>
            刷新
          </Button>,
          can(prefix + ':add') && (
            <Button
              key="add"
              type="primary"
              onClick={() => setEditor({ id: null, readOnly: false })}
            >
              新增{valueMode ? '规格值' : '规格'}
            </Button>
          ),
        ]}
        onSubmit={(v) => {
          setFilters({
            name: typeof v.name === 'string' ? v.name : undefined,
            createTimeStart: typeof v.createTimeStart === 'string' ? v.createTimeStart : undefined,
            createTimeEnd: typeof v.createTimeEnd === 'string' ? v.createTimeEnd : undefined,
          });
          setPage({ ...page, pageNum: 1 });
        }}
        onReset={() => {
          setFilters({});
          setPage({ ...page, pageNum: 1 });
        }}
        pagination={{
          current: page.pageNum,
          pageSize: page.pageSize,
          total: rows.data?.total ?? 0,
          onChange: (pageNum, pageSize) => setPage({ pageNum, pageSize }),
        }}
      />
      {editor?.id && detail.isFetching && (
        <Alert
          type="info"
          title="正在读取详情…"
          action={<Button onClick={() => setEditor(undefined)}>取消</Button>}
        />
      )}
      {editor?.id && detail.isError && (
        <Alert
          type="error"
          title="详情读取失败，未打开编辑表单"
          action={
            <>
              <Button onClick={() => detail.refetch()}>重试</Button>
              <Button onClick={() => setEditor(undefined)}>取消</Button>
            </>
          }
        />
      )}
      {editor && initial && (!editor.id || (detail.isSuccess && !detail.isFetching)) && (
        <AdminModalForm<api.SpecValues>
          key={`${editor.id ?? 'new'}-${editor.readOnly}`}
          title={`${editor.readOnly ? '查看' : editor.id ? '编辑' : '新增'}${valueMode ? '规格值' : '规格'}`}
          open
          initialValues={initial}
          disabled={editor.readOnly || save.isPending}
          submitter={
            editor.readOnly
              ? false
              : {
                  submitButtonProps: { loading: save.isPending },
                  resetButtonProps: { disabled: save.isPending },
                }
          }
          modalProps={{ maskClosable: false, closable: !save.isPending }}
          onOpenChange={(open) => {
            if (!open && !save.isPending) setEditor(undefined);
          }}
          onFinish={async (v) => {
            if (editor.readOnly || save.isPending || !can(prefix + (editor.id ? ':edit' : ':add')))
              return false;
            try {
              await save.mutateAsync(v);
              message.success('已保存');
              setEditor(undefined);
              await refresh();
              return true;
            } catch (e) {
              message.error(String(e));
              return false;
            }
          }}
        >
          <ProFormText
            name="name"
            label={valueMode ? '规格值名称' : '规格名称'}
            rules={[{ required: true, whitespace: true, max: valueMode ? 100 : 64 }]}
          />
          <ProFormTextArea name="remark" label="备注" rules={[{ max: 500 }]} />
        </AdminModalForm>
      )}
    </PageContainer>
  );
}
