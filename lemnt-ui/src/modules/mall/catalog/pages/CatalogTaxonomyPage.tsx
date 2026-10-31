import { useRef, useState } from 'react';
import {
  PageContainer,
  ProTable,
  ProFormText,
  ProFormDigit,
  ProFormRadio,
  ProFormSelect,
  ProFormTextArea,
  type ProColumns,
} from '@ant-design/pro-components';
import { Alert, App, Button, Popconfirm, Result } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { AdminModalForm } from '@/shared/components/AdminModalForm';
import { usePermission } from '@/shared/hooks/usePermission';
import * as api from '../api/catalogApi';
import { catalogKeys } from '../hooks/catalogKeys';
import { taxonomyStatusEnum, taxonomyStatusOptions } from '../constants';
import { requireWriteSuccess } from '../catalogValidation';
import type { Category, Brand, TaxonomyStatus } from '../types';

interface TaxonomyFormValues {
  name: string;
  parentId?: string;
  picUrl?: string;
  description?: string;
  sort: number;
  status: TaxonomyStatus;
}
export default function CatalogTaxonomyPage({ kind }: { kind: 'category' | 'brand' }) {
  const isCategory = kind === 'category';
  const label = isCategory ? '分类' : '品牌';
  const { can } = usePermission();
  const permission = (action: string) => can(`mall:${kind}:${action}`);
  const client = useQueryClient();
  const { message } = App.useApp();
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<Category | Brand>();
  const [isOpening, setIsOpening] = useState(false);
  const operation = useRef(false);
  const query = useQuery<Category[] | Brand[]>({
    queryKey: isCategory ? catalogKeys.categories() : catalogKeys.brands(),
    queryFn: isCategory ? api.getCategoryList : api.getBrandList,
    enabled: permission('list'),
  });
  const save = useMutation<unknown, Error, TaxonomyFormValues>({
    mutationFn: (values: TaxonomyFormValues) => {
      if (isCategory) {
        const payload = {
          name: values.name,
          parentId: values.parentId ?? null,
          picUrl: values.picUrl,
          sort: values.sort,
          status: values.status,
        };
        return editing ? api.updateCategory(editing.id, payload) : api.createCategory(payload);
      }
      const payload = {
        name: values.name,
        picUrl: values.picUrl,
        description: values.description,
        sort: values.sort,
        status: values.status,
      };
      return editing ? api.updateBrand(editing.id, payload) : api.createBrand(payload);
    },
  });
  const deletion = useMutation({ mutationFn: isCategory ? api.deleteCategory : api.deleteBrand });
  const isBusy = save.isPending || deletion.isPending || isOpening;
  async function refresh() {
    try {
      await client.invalidateQueries({ queryKey: catalogKeys.all });
    } catch (reason) {
      message.warning(`操作已成功，刷新失败：${String(reason)}`);
    }
  }
  async function edit(id: string) {
    if (operation.current) return;
    operation.current = true;
    setIsOpening(true);
    try {
      const detail = isCategory
        ? await client.fetchQuery({
            queryKey: catalogKeys.category(id),
            queryFn: () => api.getCategoryDetail(id),
          })
        : await client.fetchQuery({
            queryKey: catalogKeys.brand(id),
            queryFn: () => api.getBrandDetail(id),
          });
      setEditing(detail);
      setOpen(true);
    } catch (reason) {
      message.error(String(reason));
    } finally {
      setIsOpening(false);
      operation.current = false;
    }
  }
  async function remove(id: string) {
    if (operation.current) return;
    operation.current = true;
    try {
      requireWriteSuccess((await deletion.mutateAsync(id)).data);
      message.success('删除成功');
      await refresh();
    } catch (reason) {
      message.error(String(reason));
    } finally {
      operation.current = false;
    }
  }
  const columns: ProColumns<Category | Brand>[] = [
    { title: '编号', dataIndex: 'id' },
    { title: `${label}名称`, dataIndex: 'name' },
    ...(isCategory
      ? [
          {
            title: '父分类',
            render: (_: unknown, row: Category | Brand) =>
              'parentId' in row && row.parentId
                ? (query.data?.find((item) => item.id === row.parentId)?.name ?? row.parentId)
                : '根分类',
          },
        ]
      : []),
    { title: '排序', dataIndex: 'sort' },
    { title: '状态', dataIndex: 'status', valueEnum: taxonomyStatusEnum },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) => [
        permission('edit') && permission('query') && (
          <Button key="edit" type="link" disabled={isBusy} onClick={() => edit(row.id)}>
            修改
          </Button>
        ),
        permission('remove') && (
          <Popconfirm
            key="delete"
            title={`确认删除该${label}？被引用的记录无法删除。`}
            onConfirm={() => remove(row.id)}
            disabled={isBusy}
          >
            <Button type="link" danger disabled={isBusy}>
              删除
            </Button>
          </Popconfirm>
        ),
      ],
    },
  ];
  if (!permission('list')) return <Result status="403" title={`没有${label}查询权限`} />;
  return (
    <PageContainer title={`${label}管理`}>
      {query.isError && (
        <Alert
          type="error"
          title="列表加载失败"
          description={String(query.error)}
          action={<Button onClick={() => query.refetch()}>重试</Button>}
        />
      )}
      <ProTable<Category | Brand>
        rowKey="id"
        search={false}
        columns={columns}
        loading={query.isFetching}
        dataSource={query.data ?? []}
        pagination={{ defaultPageSize: 10 }}
        toolBarRender={() => [
          permission('add') && (
            <Button
              key="add"
              type="primary"
              disabled={isBusy}
              onClick={() => {
                setEditing(undefined);
                setOpen(true);
              }}
            >
              新增{label}
            </Button>
          ),
        ]}
      />
      <AdminModalForm<TaxonomyFormValues>
        key={`${kind}-${editing?.id ?? 'new'}-${open}`}
        title={`${editing ? '修改' : '新增'}${label}`}
        open={open}
        onOpenChange={setOpen}
        initialValues={{
          ...editing,
          parentId: editing && 'parentId' in editing ? (editing.parentId ?? undefined) : undefined,
          sort: editing?.sort ?? 0,
          status: editing?.status ?? 'ENABLED',
        }}
        modalProps={{ destroyOnHidden: true, maskClosable: false }}
        submitter={{ submitButtonProps: { loading: save.isPending } }}
        onFinish={async (values) => {
          if (operation.current) return false;
          operation.current = true;
          try {
            await save.mutateAsync(values);
            message.success('保存成功');
            setOpen(false);
            await refresh();
            return true;
          } catch (reason) {
            message.error(String(reason));
            return false;
          } finally {
            operation.current = false;
          }
        }}
      >
        <ProFormText
          name="name"
          label={`${label}名称`}
          rules={[{ required: true }, { max: 100 }]}
        />
        {isCategory && (
          <ProFormSelect
            name="parentId"
            label="父分类（留空为根分类）"
            disabled={Boolean(editing)}
            options={(query.data as Category[] | undefined)
              ?.filter((row) => row.parentId === null && row.id !== editing?.id)
              .map((row) => ({ label: row.name, value: row.id }))}
          />
        )}
        <ProFormText name="picUrl" label="图片 URL" rules={[{ max: 500 }]} />
        {!isCategory && <ProFormTextArea name="description" label="描述" rules={[{ max: 2000 }]} />}
        <ProFormDigit
          name="sort"
          label="排序"
          min={0}
          fieldProps={{ precision: 0 }}
          rules={[{ required: true }]}
        />
        <ProFormRadio.Group
          name="status"
          label="状态"
          options={taxonomyStatusOptions}
          rules={[{ required: true }]}
        />
      </AdminModalForm>
    </PageContainer>
  );
}
