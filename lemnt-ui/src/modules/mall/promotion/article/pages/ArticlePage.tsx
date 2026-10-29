import { useRef, useState } from 'react';
import {
  PageContainer,
  ProTable,
  ProFormText,
  ProFormTextArea,
  ProFormDigit,
  ProFormSelect,
  ProFormSwitch,
  type ActionType,
  type ProColumns,
} from '@ant-design/pro-components';
import { Alert, App, Button, Drawer, Form, Popconfirm, Result } from 'antd';
import DOMPurify from 'dompurify';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { AdminModalForm } from '@/shared/components/AdminModalForm';
import { usePermission } from '@/shared/hooks/usePermission';
import { getSpuList } from '@/modules/mall/catalog/api/catalogApi';
import * as api from '../api/articleApi';
import { ArticleContentEditor } from '../components/ArticleContentEditor';
import { ArticleImageField } from '../components/ArticleImageField';
import type { Article, ArticleCreateRequest, ArticlePageQuery } from '../types';
function optionalFlag(value: unknown): boolean | undefined {
  return value === true || value === 'true'
    ? true
    : value === false || value === 'false'
      ? false
      : undefined;
}
export default function ArticlePage() {
  const { can } = usePermission();
  const { message } = App.useApp();
  const client = useQueryClient();
  const action = useRef<ActionType | undefined>(undefined);
  const operation = useRef(false);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<Article>();
  const [preview, setPreview] = useState<Article>();
  const [isOpening, setIsOpening] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [error, setError] = useState('');
  const categories = useQuery({
    queryKey: ['mall-article', 'enabled-categories'],
    queryFn: api.getEnabledCategories,
    enabled: can('mall:article-category:list'),
  });
  const save = useMutation<unknown, Error, ArticleCreateRequest>({
    mutationFn: (values: ArticleCreateRequest) =>
      editing ? api.updateArticle(editing.id, values) : api.createArticle(values),
  });
  const remove = useMutation({ mutationFn: api.deleteArticle });
  const busy = isOpening || isUploading || save.isPending || remove.isPending;
  async function refresh() {
    try {
      await client.invalidateQueries({ queryKey: ['mall-article'] });
      action.current?.reload();
    } catch (reason) {
      message.warning(`操作已成功，刷新失败：${String(reason)}`);
    }
  }
  async function show(row: Article, readOnly = false) {
    if (operation.current) return;
    operation.current = true;
    setIsOpening(true);
    try {
      const detail = await client.fetchQuery({
        queryKey: ['mall-article', 'detail', row.id],
        queryFn: () => api.getArticle(row.id),
      });
      if (readOnly) setPreview(detail);
      else {
        setEditing(detail);
        setOpen(true);
      }
    } catch (reason) {
      message.error(String(reason));
    } finally {
      operation.current = false;
      setIsOpening(false);
    }
  }
  const columns: ProColumns<Article>[] = [
    { title: '标题（精确）', dataIndex: 'title' },
    { title: '作者（精确）', dataIndex: 'author' },
    {
      title: '分类',
      dataIndex: 'categoryId',
      valueType: 'select',
      fieldProps: { options: categories.data?.map((v) => ({ label: v.name, value: v.id })) },
      render: (_, row) =>
        categories.data?.find((v) => v.id === row.categoryId)?.name ?? row.categoryId,
    },
    { title: '关联商品', dataIndex: 'spuId' },
    {
      title: '状态',
      dataIndex: 'status',
      valueType: 'select',
      valueEnum: { 0: { text: '启用' }, 1: { text: '禁用' } },
    },
    {
      title: '热门',
      dataIndex: 'recommendHot',
      valueType: 'select',
      valueEnum: { true: { text: '是' }, false: { text: '否' } },
    },
    {
      title: '轮播',
      dataIndex: 'recommendBanner',
      valueType: 'select',
      valueEnum: { true: { text: '是' }, false: { text: '否' } },
    },
    { title: '浏览量', dataIndex: 'browseCount', search: false },
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
        can('mall:article:query') ? (
          <Button key="detail" type="link" disabled={busy} onClick={() => show(row, true)}>
            详情
          </Button>
        ) : null,
        can('mall:article:query') && can('mall:article:update') ? (
          <Button key="edit" type="link" disabled={busy} onClick={() => show(row)}>
            编辑
          </Button>
        ) : null,
        can('mall:article:delete') ? (
          <Popconfirm
            key="delete"
            title="删除此文章？"
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
  if (!can('mall:article:list')) return <Result status="403" title="无文章查询权限" />;
  return (
    <PageContainer title="商城文章">
      {error ? <Alert type="error" showIcon message={error} /> : null}
      {categories.isError ? (
        <Alert
          type="error"
          showIcon
          message="分类选项读取失败，请重试"
          action={<Button onClick={() => categories.refetch()}>重试</Button>}
        />
      ) : null}
      <ProTable<Article>
        rowKey="id"
        columns={columns}
        actionRef={action}
        scroll={{ x: 1300 }}
        pagination={{ defaultPageSize: 20, showSizeChanger: true, pageSizeOptions: [20, 50, 100] }}
        request={async (params) => {
          const range = Array.isArray(params.createTime) ? params.createTime : [];
          const query: ArticlePageQuery = {
            title: params.title || undefined,
            author: params.author || undefined,
            categoryId: params.categoryId || undefined,
            spuId:
              params.spuId === undefined || params.spuId === '' ? undefined : String(params.spuId),
            status:
              params.status === undefined || params.status === ''
                ? undefined
                : (Number(params.status) as 0 | 1),
            recommendHot: optionalFlag(params.recommendHot),
            recommendBanner: optionalFlag(params.recommendBanner),
            createFrom: range[0]?.replace(' ', 'T'),
            createTo: range[1]?.replace(' ', 'T'),
            pageNum: params.current ?? 1,
            pageSize: params.pageSize ?? 20,
          };
          try {
            const result = await client.fetchQuery({
              queryKey: ['mall-article', 'page', query],
              queryFn: () => api.getArticles(query),
            });
            setError('');
            return result;
          } catch (reason) {
            setError(String(reason));
            return { data: [], total: 0, success: false };
          }
        }}
        toolBarRender={() =>
          can('mall:article:create')
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
                  新增文章
                </Button>,
              ]
            : []
        }
      />
      <AdminModalForm<ArticleCreateRequest>
        key={editing?.id ?? 'new'}
        title={editing ? '编辑文章' : '新增文章'}
        open={open}
        onOpenChange={setOpen}
        initialValues={
          editing ?? {
            title: '',
            categoryId: undefined,
            spuId: '0',
            picUrl: '',
            sort: 0,
            status: 0,
            recommendHot: false,
            recommendBanner: false,
            content: '',
          }
        }
        modalProps={{ destroyOnHidden: true, maskClosable: false }}
        submitter={{ submitButtonProps: { disabled: busy } }}
        loading={save.isPending}
        onFinish={async (values) => {
          if (operation.current || isUploading) return false;
          operation.current = true;
          try {
            await save.mutateAsync({
              ...values,
              author: values.author ?? null,
              introduction: values.introduction ?? null,
              content: DOMPurify.sanitize(values.content),
            });
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
          name="title"
          label="标题"
          rules={[{ required: true, whitespace: true }, { max: 255 }]}
        />
        <ProFormSelect
          name="categoryId"
          label="分类"
          options={categories.data?.map((v) => ({ label: v.name, value: v.id }))}
          rules={[{ required: true }]}
          fieldProps={{ loading: categories.isLoading }}
        />
        <ProFormText name="author" label="作者" rules={[{ max: 128 }]} />
        <ProFormTextArea name="introduction" label="简介" rules={[{ max: 2000 }]} />
        <Form.Item name="picUrl" label="封面" rules={[{ required: true }]}>
          <ArticleImageField
            disabled={save.isPending || isUploading}
            onUploadingChange={setIsUploading}
          />
        </Form.Item>
        <ProFormSelect
          name="spuId"
          label="关联商品"
          showSearch
          debounceTime={300}
          rules={[{ required: true }]}
          request={async ({ keyWords }) => {
            const rows = can('mall:spu:list')
              ? await client.fetchQuery({
                  queryKey: ['mall-article', 'spu-options', keyWords],
                  queryFn: () => getSpuList({ name: keyWords, pageNum: 1, pageSize: 50 }),
                })
              : { data: [] };
            const options = [
              { label: '不关联商品', value: '0' },
              ...rows.data.map((row) => ({ label: row.name, value: row.id })),
            ];
            if (
              editing &&
              editing.spuId !== '0' &&
              !options.some((row) => row.value === editing.spuId)
            )
              options.push({ label: editing.spuId, value: editing.spuId });
            return options;
          }}
        />
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
        <ProFormSwitch name="recommendHot" label="热门" />
        <ProFormSwitch name="recommendBanner" label="轮播" />
        <Form.Item name="content" label="正文" rules={[{ required: true }]}>
          <ArticleContentEditor disabled={save.isPending} />
        </Form.Item>
      </AdminModalForm>
      <Drawer
        title={preview?.title}
        open={!!preview}
        onClose={() => setPreview(undefined)}
        width={700}
      >
        {preview ? (
          <>
            <p>
              {preview.author ?? ''} · {preview.createTime} · 浏览 {preview.browseCount}
            </p>
            <div dangerouslySetInnerHTML={{ __html: DOMPurify.sanitize(preview.content) }} />
          </>
        ) : null}
      </Drawer>
    </PageContainer>
  );
}
