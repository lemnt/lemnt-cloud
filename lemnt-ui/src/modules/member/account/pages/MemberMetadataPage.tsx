import { useState } from 'react';
import {
  PageContainer,
  ProTable,
  ProFormText,
  ProFormDigit,
  ProFormSelect,
  type ProColumns,
} from '@ant-design/pro-components';
import { Alert, App, Button, Popconfirm, Result } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { AdminModalForm } from '@/shared/components/AdminModalForm';
import { usePermission } from '@/shared/hooks/usePermission';
import * as api from '../api/memberApi';
import { memberKeys, memberStatusOptions } from '../types';
import type { MemberMetadata, MetadataKind, MetadataValues } from '../types';
export function MemberMetadataPage({ kind }: { kind: MetadataKind }) {
  const { can } = usePermission();
  const { message } = App.useApp();
  const cache = useQueryClient();
  const label = kind === 'levels' ? '等级' : kind === 'groups' ? '分组' : '标签';
  const prefix = 'member:' + (kind === 'levels' ? 'level' : kind === 'groups' ? 'group' : 'tag');
  const [editing, setEditing] = useState<MemberMetadata | null>();
  const rows = useQuery({
    queryKey: memberKeys.metadata(kind),
    queryFn: () => api.getMetadata(kind),
    enabled: can(prefix + ':query'),
  });
  const save = useMutation({
    mutationFn: (v: MetadataValues) => api.saveMetadata(kind, editing?.id, v),
  });
  const remove = useMutation({ mutationFn: (id: string) => api.deleteMetadata(kind, id) });
  async function refresh() {
    try {
      await cache.invalidateQueries({ queryKey: memberKeys.all }, { throwOnError: true });
    } catch {
      message.warning('保存成功，但刷新失败，请重新加载');
    }
  }
  const columns: ProColumns<MemberMetadata>[] = [
    { title: 'ID', dataIndex: 'id' },
    { title: '名称', dataIndex: 'name' },
    ...(kind === 'levels'
      ? [
          { title: '等级序号', dataIndex: 'level' },
          { title: '经验门槛', dataIndex: 'experience' },
          { title: '支付比例（%）', dataIndex: 'discountPercent' },
        ]
      : [{ title: '备注', dataIndex: 'remark' }]),
    ...(kind !== 'tags'
      ? [
          {
            title: '状态',
            dataIndex: 'status',
            renderText: (v: unknown) => (v === 1 ? '启用' : '停用'),
          },
        ]
      : []),
    {
      title: '操作',
      valueType: 'option',
      render: (_, r) => [
        can(prefix + ':update') && (
          <Button key="edit" type="link" onClick={() => setEditing(r)}>
            编辑
          </Button>
        ),
        can(prefix + ':delete') && (
          <Popconfirm
            key="delete"
            title={`确认删除${label}？正在使用的记录不可删除。`}
            onConfirm={async () => {
              try {
                await remove.mutateAsync(r.id);
                message.success('已删除');
                await refresh();
              } catch (e) {
                message.error(String(e));
              }
            }}
          >
            <Button danger type="link" disabled={remove.isPending}>
              删除
            </Button>
          </Popconfirm>
        ),
      ],
    },
  ];
  if (!can(prefix + ':query')) return <Result status="403" title={`没有${label}查询权限`} />;
  return (
    <PageContainer title={`会员${label}`}>
      {rows.isError && (
        <Alert
          type="error"
          title="读取失败"
          description={String(rows.error)}
          action={<Button onClick={() => rows.refetch()}>重试</Button>}
        />
      )}
      <ProTable<MemberMetadata>
        rowKey="id"
        search={false}
        columns={columns}
        dataSource={rows.data ?? []}
        loading={rows.isFetching}
        scroll={{ x: 800 }}
        toolBarRender={() => [
          can(prefix + ':create') && (
            <Button key="add" type="primary" onClick={() => setEditing(null)}>
              新增{label}
            </Button>
          ),
        ]}
      />
      {editing !== undefined && (
        <AdminModalForm<MetadataValues>
          key={editing?.id ?? 'new'}
          title={(editing ? '编辑' : '新增') + label}
          open
          onOpenChange={(v) => {
            if (!v && !save.isPending) setEditing(undefined);
          }}
          initialValues={editing ?? { status: 1, discountPercent: 100, experience: 1, level: 1 }}
          modalProps={{ destroyOnHidden: true, maskClosable: false, closable: !save.isPending }}
          submitter={{
            submitButtonProps: { loading: save.isPending },
            resetButtonProps: { disabled: save.isPending },
          }}
          onFinish={async (v) => {
            if (save.isPending || !can(prefix + (editing ? ':update' : ':create'))) return false;
            try {
              await save.mutateAsync(v);
              message.success('已保存');
              setEditing(undefined);
              await refresh();
              return true;
            } catch (e) {
              message.error(String(e));
              return false;
            }
          }}
        >
          <ProFormText name="name" label="名称" rules={[{ required: true }, { max: 80 }]} />
          {kind === 'levels' ? (
            <>
              <Alert type="info" title="等级与经验门槛须严格递增；支付比例95代表支付原价95%。" />
              <ProFormDigit
                name="level"
                label="等级序号"
                min={1}
                max={1000}
                fieldProps={{ precision: 0 }}
                rules={[{ required: true }]}
              />
              <ProFormDigit
                name="experience"
                label="经验门槛"
                min={1}
                max={Number.MAX_SAFE_INTEGER}
                fieldProps={{ precision: 0 }}
                rules={[{ required: true }]}
              />
              <ProFormDigit
                name="discountPercent"
                label="支付比例（%）"
                min={0}
                max={100}
                fieldProps={{ precision: 0 }}
                rules={[{ required: true }]}
              />
              <ProFormText name="icon" label="图标URL" rules={[{ max: 500 }]} />
              <ProFormText name="backgroundUrl" label="背景URL" rules={[{ max: 500 }]} />
            </>
          ) : (
            <ProFormText name="remark" label="备注" rules={[{ max: 500 }]} />
          )}
          {kind !== 'tags' && (
            <ProFormSelect
              name="status"
              label="状态"
              options={memberStatusOptions}
              rules={[{ required: true }]}
            />
          )}
        </AdminModalForm>
      )}
    </PageContainer>
  );
}
export function MemberLevelPage() {
  return <MemberMetadataPage kind="levels" />;
}
export function MemberGroupPage() {
  return <MemberMetadataPage kind="groups" />;
}
export function MemberTagPage() {
  return <MemberMetadataPage kind="tags" />;
}
