import { useRef, useState } from 'react';
import {
  PageContainer,
  ProTable,
  ProFormText,
  ProFormDigit,
  ProFormSelect,
  type ActionType,
  type ProColumns,
} from '@ant-design/pro-components';
import { Alert, App, Button, Form, Popconfirm, Result } from 'antd';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { AdminModalForm } from '@/shared/components/AdminModalForm';
import { usePermission } from '@/shared/hooks/usePermission';
import * as api from '../api/articleApi';
import { ArticleImageField } from '../components/ArticleImageField';
import type {
  ArticleCategory,
  ArticleCategoryCreateRequest,
  ArticleCategoryPageQuery,
} from '../types';

export default function ArticleCategoryPage() {
  const { can } = usePermission();
  const { message } = App.useApp();
  const client = useQueryClient();
  const action = useRef<ActionType | undefined>(undefined);
  const operation = useRef(false);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<ArticleCategory>();
  const [isOpening, setIsOpening] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [error, setError] = useState('');
  const save = useMutation<unknown, Error, ArticleCategoryCreateRequest>({
    mutationFn: (values: ArticleCategoryCreateRequest) =>
      editing ? api.updateArticleCategory(editing.id, values) : api.createArticleCategory(values),
  });
  const remove = useMutation({ mutationFn: api.deleteArticleCategory });
  const busy = isOpening || isUploading || save.isPending || remove.isPending;
  async function refresh() {
    try {
      await client.invalidateQueries({ queryKey: ['mall-article'] });
      action.current?.reload();
    } catch (reason) {
      message.warning(`操作已成功，刷新失败：${String(reason)}`);
    }
  }
  async function edit(row: ArticleCategory) {
    if (operation.current) return;
    operation.current = true;
    setIsOpening(true);
    try {
      setEditing(
        await client.fetchQuery({
          queryKey: ['mall-article', 'category', row.id],
          queryFn: () => api.getArticleCategory(row.id),
        }),
      );
      setOpen(true);
    } catch (reason) {
      message.error(String(reason));
    } finally {
      operation.current = false;
      setIsOpening(false);
    }
  }
  const columns: ProColumns<ArticleCategory>[] = [
    { title: '名称', dataIndex: 'name' },
    {
      title: '状态',
      dataIndex: 'status',
      valueType: 'select',
      valueEnum: { 0: { text: '启用' }, 1: { text: '禁用' } },
    },
    { title: '排序', dataIndex: 'sort', search: false },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      valueType: 'dateTimeRange',
      render: (_, row) => row.createTime,
    },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) => [
        can('mall:article-category:query') && can('mall:article-category:update') ? (
          <Button key="edit" type="link" disabled={busy} onClick={() => edit(row)}>
            编辑
          </Button>
        ) : null,
        can('mall:article-category:delete') ? (
          <Popconfirm
            key="delete"
            title="删除此分类？有关联文章时不能删除。"
            onConfirm={async () => {
              if (operation.current) return;
              operation.current = true;
              try {
                await remove.mutateAsync(row.id);
                message.success('删除成功');
                await refresh();
              } catch (reason) {
                message.error(String(reason));
              } finally {
                operation.current = false;
              }
            }}
          >
            <Button danger type="link" disabled={busy}>
              删除
            </Button>
          </Popconfirm>
        ) : null,
      ],
    },
  ];
  if (!can('mall:article-category:list')) return <Result status="403" title="无文章分类查询权限" />;
  return (
    <PageContainer title="文章分类">
      {error ? <Alert type="error" showIcon message={error} /> : null}
      <ProTable<ArticleCategory>
        rowKey="id"
        actionRef={action}
        columns={columns}
        pagination={{ defaultPageSize: 20, showSizeChanger: true, pageSizeOptions: [20, 50, 100] }}
        request={async (params) => {
          const range = Array.isArray(params.createTime) ? params.createTime : [];
          const query: ArticleCategoryPageQuery = {
            name: params.name || undefined,
            status:
              params.status === undefined || params.status === ''
                ? undefined
                : (Number(params.status) as 0 | 1),
            createFrom: range[0]?.replace(' ', 'T'),
            createTo: range[1]?.replace(' ', 'T'),
            pageNum: params.current ?? 1,
            pageSize: params.pageSize ?? 20,
          };
          try {
            const result = await client.fetchQuery({
              queryKey: ['mall-article', 'categories', query],
              queryFn: () => api.getArticleCategories(query),
            });
            setError('');
            return result;
          } catch (reason) {
            setError(String(reason));
            return { data: [], total: 0, success: false };
          }
        }}
        toolBarRender={() =>
          can('mall:article-category:create')
            ? [
                <Button
                  key="create"
                  type="primary"
                  disabled={busy}
                  onClick={() => {
                    setEditing(undefined);
                    setOpen(true);
                  }}
                >
                  新增分类
                </Button>,
              ]
            : []
        }
      />
      <AdminModalForm<ArticleCategoryCreateRequest>
        key={editing?.id ?? 'new'}
        title={editing ? '编辑文章分类' : '新增文章分类'}
        open={open}
        onOpenChange={setOpen}
        initialValues={editing ?? { name: '', picUrl: null, status: 0, sort: 0 }}
        loading={save.isPending}
        modalProps={{ destroyOnHidden: true, maskClosable: false }}
        submitter={{ submitButtonProps: { disabled: busy } }}
        onFinish={async (values) => {
          if (operation.current || isUploading) return false;
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
          label="分类名称"
          rules={[{ required: true, whitespace: true }, { max: 128 }]}
        />
        <Form.Item name="picUrl" label="分类图标">
          <ArticleImageField
            disabled={save.isPending || isUploading}
            onUploadingChange={setIsUploading}
          />
        </Form.Item>
        <ProFormDigit
          name="sort"
          label="排序"
          min={0}
          max={2147483647}
          fieldProps={{ precision: 0 }}
          rules={[{ required: true }]}
        />
        <ProFormSelect
          name="status"
          label="状态"
          options={[
            { label: '启用', value: 0 },
            { label: '禁用', value: 1 },
          ]}
          rules={[{ required: true }]}
        />
      </AdminModalForm>
    </PageContainer>
  );
}
