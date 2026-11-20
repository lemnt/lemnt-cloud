import { useRef, useState } from 'react';

import {
  PageContainer,
  ProFormDigit,
  ProFormSelect,
  ProFormSwitch,
  ProFormText,
  ProFormTextArea,
  ProTable,
} from '@ant-design/pro-components';
import { App, Button, Popconfirm, Tabs } from 'antd';
import { useQuery } from '@tanstack/react-query';

import { usePermission } from '@/shared/hooks/usePermission';
import { AdminModalForm } from '@/shared/components/AdminModalForm';
import {
  deleteArticle,
  deleteArticleCategory,
  getArticle,
  getArticleCategories,
  getArticlePage,
  getEnabledArticleCategories,
  saveArticle,
  saveArticleCategory,
} from '../api/articleApi';

import type { ActionType, ProColumns } from '@ant-design/pro-components';
import type {
  Article,
  ArticleCategory,
  ArticleCategoryInput,
  ArticleInput,
} from '../api/articleApi';

const statusOptions = [
  { label: '启用', value: 0 },
  { label: '停用', value: 1 },
];
const enabledCategoriesKey = ['mall', 'promotion', 'articles', 'enabledCategories'];
const required = [{ required: true }];

function Articles() {
  const { can } = usePermission();
  const { message } = App.useApp();
  const table = useRef<ActionType>(null);
  const [editor, setEditor] = useState<Article | 'new' | null>(null);
  const [loadingArticle, setLoadingArticle] = useState(false);
  const categoryQuery = useQuery({
    queryKey: enabledCategoriesKey,
    queryFn: getEnabledArticleCategories,
    enabled: editor !== null,
  });
  const columns: ProColumns<Article>[] = [
    { title: '标题', dataIndex: 'title' },
    { title: '作者', dataIndex: 'author', search: false },
    { title: '状态', dataIndex: 'status', search: false, valueEnum: { 0: '启用', 1: '停用' } },
    { title: '浏览次数', dataIndex: 'browseCount', search: false },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) => [
        can('mall:promotion:update') && can('mall:promotion:query') ? (
          <Button
            type="link"
            key="edit"
            loading={loadingArticle}
            onClick={async () => {
              setLoadingArticle(true);
              try {
                setEditor(await getArticle(row.id));
              } finally {
                setLoadingArticle(false);
              }
            }}
          >
            编辑
          </Button>
        ) : null,
        can('mall:promotion:delete') ? (
          <Popconfirm
            key="delete"
            title="删除这篇文章？"
            onConfirm={async () => {
              const result = await deleteArticle(row.id);
              if (!result.data) message.warning('文章已不存在');
              await table.current?.reload();
            }}
          >
            <Button type="link" danger>
              删除
            </Button>
          </Popconfirm>
        ) : null,
      ],
    },
  ];
  return (
    <>
      <ProTable<Article>
        rowKey="id"
        columns={columns}
        actionRef={table}
        request={({ current, pageSize, title }) =>
          getArticlePage({ pageNum: current ?? 1, pageSize: pageSize ?? 20, title })
        }
        toolBarRender={() =>
          can('mall:promotion:create') && can('mall:promotion:query')
            ? [
                <Button key="new" type="primary" onClick={() => setEditor('new')}>
                  新增文章
                </Button>,
              ]
            : []
        }
      />
      {editor && (
        <AdminModalForm<ArticleInput>
          title={editor === 'new' ? '新增文章' : '编辑文章'}
          open
          onOpenChange={(open) => {
            if (!open) setEditor(null);
          }}
          initialValues={
            editor === 'new'
              ? { status: 1, sort: 0, recommendHot: false, recommendBanner: false }
              : editor
          }
          onFinish={async (data) => {
            await saveArticle(
              { ...data, spuId: data.spuId?.trim() || undefined },
              editor === 'new' ? undefined : editor.id,
            );
            await table.current?.reload();
            return true;
          }}
        >
          <ProFormText name="title" label="标题" rules={[{ required: true, max: 200 }]} />
          <ProFormSelect
            name="categoryId"
            label="分类"
            rules={required}
            options={categoryQuery.data?.map((value) => ({ label: value.name, value: value.id }))}
          />
          <ProFormText name="spuId" label="关联商品编号" rules={[{ pattern: /^[1-9]\d{0,18}$/ }]} />
          <ProFormText name="author" label="作者" rules={[{ max: 100 }]} />
          <ProFormText name="picUrl" label="封面地址" rules={[{ max: 500 }]} />
          <ProFormText name="introduction" label="摘要" rules={[{ max: 500 }]} />
          <ProFormTextArea
            name="content"
            label="正文（支持小程序富文本标签）"
            rules={[{ required: true, max: 60000 }]}
          />
          <ProFormDigit
            name="sort"
            label="排序"
            min={0}
            fieldProps={{ precision: 0 }}
            rules={required}
          />
          <ProFormSelect name="status" label="状态" options={statusOptions} rules={required} />
          <ProFormSwitch name="recommendHot" label="热门推荐" />
          <ProFormSwitch name="recommendBanner" label="轮播推荐" />
        </AdminModalForm>
      )}
    </>
  );
}

function Categories() {
  const { can } = usePermission();
  const { message } = App.useApp();
  const table = useRef<ActionType>(null);
  const [editor, setEditor] = useState<ArticleCategory | 'new' | null>(null);
  const columns: ProColumns<ArticleCategory>[] = [
    { title: '名称', dataIndex: 'name' },
    { title: '排序', dataIndex: 'sort', search: false },
    { title: '状态', dataIndex: 'status', search: false, valueEnum: { 0: '启用', 1: '停用' } },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) => [
        can('mall:promotion:update') ? (
          <Button type="link" key="edit" onClick={() => setEditor(row)}>
            编辑
          </Button>
        ) : null,
        can('mall:promotion:delete') ? (
          <Popconfirm
            key="delete"
            title="删除这个分类？"
            onConfirm={async () => {
              const result = await deleteArticleCategory(row.id);
              if (!result.data) message.warning('分类已不存在');
              await table.current?.reload();
            }}
          >
            <Button type="link" danger>
              删除
            </Button>
          </Popconfirm>
        ) : null,
      ],
    },
  ];
  return (
    <>
      <ProTable<ArticleCategory>
        rowKey="id"
        columns={columns}
        actionRef={table}
        request={({ current, pageSize, name }) =>
          getArticleCategories({ pageNum: current ?? 1, pageSize: pageSize ?? 20, name })
        }
        toolBarRender={() =>
          can('mall:promotion:create')
            ? [
                <Button key="new" type="primary" onClick={() => setEditor('new')}>
                  新增分类
                </Button>,
              ]
            : []
        }
      />
      {editor && (
        <AdminModalForm<ArticleCategoryInput>
          title={editor === 'new' ? '新增分类' : '编辑分类'}
          open
          onOpenChange={(open) => {
            if (!open) setEditor(null);
          }}
          initialValues={editor === 'new' ? { status: 0, sort: 0 } : editor}
          onFinish={async (data) => {
            await saveArticleCategory(data, editor === 'new' ? undefined : editor.id);
            await table.current?.reload();
            return true;
          }}
        >
          <ProFormText name="name" label="名称" rules={[{ required: true, max: 100 }]} />
          <ProFormText name="picUrl" label="图片地址" rules={[{ max: 500 }]} />
          <ProFormDigit
            name="sort"
            label="排序"
            min={0}
            fieldProps={{ precision: 0 }}
            rules={required}
          />
          <ProFormSelect name="status" label="状态" options={statusOptions} rules={required} />
        </AdminModalForm>
      )}
    </>
  );
}

export function ArticlePage() {
  return (
    <PageContainer title="商城文章">
      <Tabs
        destroyOnHidden
        items={[
          { key: 'articles', label: '文章', children: <Articles /> },
          { key: 'categories', label: '文章分类', children: <Categories /> },
        ]}
      />
    </PageContainer>
  );
}
