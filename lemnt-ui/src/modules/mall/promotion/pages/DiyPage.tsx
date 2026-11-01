import { useState } from 'react';
import { PageContainer, ProFormText, ProFormTextArea } from '@ant-design/pro-components';
import { Alert, App, Button, Card, Descriptions, Input, Modal, Popconfirm, Result, Space, Table, Tag } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { AdminModalForm } from '@/shared/components/AdminModalForm';
import { usePermission } from '@/shared/hooks/usePermission';
import * as api from '../api/diyApi';
import type { DiyPage, DiyTemplate } from '../types';

interface TemplateForm {
  name: string;
  remark?: string | null;
  previewPicUrls: string;
}
interface PageForm {
  name: string;
  remark?: string | null;
  previewPicUrls: string;
}
function urls(value: string) {
  return value
    .split('\n')
    .map(item => item.trim())
    .filter(Boolean);
}

/** 装修模板与页面管理，属性使用 JSON 文本编辑。 */
export function DiyPageView() {
  const { can } = usePermission();
  const { message } = App.useApp();
  const cache = useQueryClient();
  const [selected, setSelected] = useState<DiyTemplate>();
  const [templateEditor, setTemplateEditor] = useState<{ id: string | null }>();
  const [pageEditor, setPageEditor] = useState<{ id: string | null }>();
  const [propertyEditor, setPropertyEditor] = useState<{ kind: 'template' | 'page'; id: string; text: string }>();
  const templates = useQuery({
    queryKey: ['diy', 'templates'],
    queryFn: () => api.getDiyTemplatePage({ pageNum: 1, pageSize: 100 }),
    enabled: can('mall:promotion:list'),
  });
  const pages = useQuery({
    queryKey: ['diy', 'pages', selected?.id],
    queryFn: () => api.getDiyPagePage({ templateId: selected!.id, pageNum: 1, pageSize: 100 }),
    enabled: !!selected && can('mall:promotion:query'),
  });
  const invalidate = async () => {
    await cache.invalidateQueries({ queryKey: ['diy'] });
  };
  const templateSave = useMutation({
    mutationFn: async (value: TemplateForm) => {
      const data = { name: value.name, remark: value.remark, previewPicUrls: urls(value.previewPicUrls || '') };
      if (templateEditor?.id) await api.updateDiyTemplate(templateEditor.id, data);
      else await api.createDiyTemplate(data);
    },
  });
  const pageSave = useMutation({
    mutationFn: async (value: PageForm) => {
      const data = {
        templateId: selected!.id,
        name: value.name,
        remark: value.remark,
        previewPicUrls: urls(value.previewPicUrls || ''),
      };
      if (pageEditor?.id) await api.updateDiyPage(pageEditor.id, data);
      else await api.createDiyPage(data);
    },
  });
  const useTemplate = useMutation({ mutationFn: api.useDiyTemplate });
  const removeTemplate = useMutation({ mutationFn: api.deleteDiyTemplate });
  const removePage = useMutation({ mutationFn: api.deleteDiyPage });
  const saveProperty = useMutation({
    mutationFn: async () => {
      if (!propertyEditor) return;
      const parsed = JSON.parse(propertyEditor.text);
      if (!parsed || typeof parsed !== 'object') throw Error('装修属性必须是JSON对象或数组');
      if (propertyEditor.kind === 'template')
        await api.updateDiyTemplateProperty(propertyEditor.id, propertyEditor.text);
      else await api.updateDiyPageProperty(propertyEditor.id, propertyEditor.text);
    },
  });
  async function run(work: () => Promise<unknown>, success: string) {
    try {
      await work();
      message.success(success);
      await invalidate();
    } catch (error) {
      message.error(String(error));
    }
  }
  if (!can('mall:promotion:list')) {
    return (
      <PageContainer title="装修管理">
        <Result status="403" title="没有装修管理权限" />
      </PageContainer>
    );
  }
  return (
    <PageContainer title="装修管理">
      {templates.isError && (
        <Alert type="error" title="模板读取失败" description={String(templates.error)} />
      )}
      <Card
        title="装修模板"
        extra={
          can('mall:promotion:create') && (
            <Button type="primary" onClick={() => setTemplateEditor({ id: null })}>
              新增模板
            </Button>
          )
        }
      >
        <Table<DiyTemplate>
          rowKey="id"
          loading={templates.isFetching}
          dataSource={templates.data?.records ?? []}
          pagination={false}
          onRow={row => ({ onClick: () => setSelected(row) })}
          columns={[
            {
              title: '名称',
              dataIndex: 'name',
              render: (_, row) => (
                <Space>
                  {row.name}
                  {row.used && <Tag color="green">启用中</Tag>}
                </Space>
              ),
            },
            { title: '备注', dataIndex: 'remark', render: value => value || '—' },
            { title: '页面数', dataIndex: 'id', render: (_, row) => (row.id === selected?.id ? pages.data?.total : '—') },
            {
              title: '操作',
              render: (_, row) => (
                <Space onClick={event => event.stopPropagation()}>
                  {can('mall:promotion:query') && (
                    <Button type="link" onClick={() => { setSelected(row); }}>页面</Button>
                  )}
                  {can('mall:promotion:update') && (
                    <Button type="link" onClick={() => setPropertyEditor({ kind: 'template', id: row.id, text: row.property })}>
                      属性
                    </Button>
                  )}
                  {can('mall:promotion:update') && (
                    <Button type="link" onClick={() => setTemplateEditor({ id: row.id })}>编辑</Button>
                  )}
                  {can('mall:promotion:update') && !row.used && (
                    <Popconfirm title="确认启用该模板？" onConfirm={() => run(() => useTemplate.mutateAsync(row.id), '已启用')}>
                      <Button type="link">启用</Button>
                    </Popconfirm>
                  )}
                  {can('mall:promotion:delete') && !row.used && (
                    <Popconfirm title="确认删除模板？" onConfirm={() => run(() => removeTemplate.mutateAsync(row.id), '已删除')}>
                      <Button type="link" danger>删除</Button>
                    </Popconfirm>
                  )}
                </Space>
              ),
            },
          ]}
        />
      </Card>
      <Card
        title={selected ? `页面 · ${selected.name}` : '页面'}
        style={{ marginTop: 16 }}
        extra={
          selected && can('mall:promotion:create') && (
            <Button type="primary" onClick={() => setPageEditor({ id: null })}>
              新增页面
            </Button>
          )
        }
      >
        {!selected && <Descriptions column={1} items={[{ key: 'tip', label: '提示', children: '请选择模板' }]} />}
        {selected && (
          <Table<DiyPage>
            rowKey="id"
            loading={pages.isFetching}
            dataSource={pages.data?.records ?? []}
            pagination={false}
            columns={[
              { title: '名称', dataIndex: 'name' },
              { title: '备注', dataIndex: 'remark', render: value => value || '—' },
              {
                title: '操作',
                render: (_, row) => (
                  <Space>
                    {can('mall:promotion:update') && (
                      <Button type="link" onClick={() => setPropertyEditor({ kind: 'page', id: row.id, text: row.property })}>
                        属性
                      </Button>
                    )}
                    {can('mall:promotion:update') && (
                      <Button type="link" onClick={() => setPageEditor({ id: row.id })}>编辑</Button>
                    )}
                    {can('mall:promotion:delete') && (
                      <Popconfirm title="确认删除页面？" onConfirm={() => run(() => removePage.mutateAsync(row.id), '已删除')}>
                        <Button type="link" danger>删除</Button>
                      </Popconfirm>
                    )}
                  </Space>
                ),
              },
            ]}
          />
        )}
      </Card>
      {templateEditor && (
        <AdminModalForm<TemplateForm>
          open
          title={templateEditor.id ? '编辑模板' : '新增模板'}
          initialValues={
            templateEditor.id
              ? {
                  name: templates.data?.records.find(row => row.id === templateEditor.id)?.name ?? '',
                  remark: templates.data?.records.find(row => row.id === templateEditor.id)?.remark ?? null,
                  previewPicUrls: (templates.data?.records.find(row => row.id === templateEditor.id)?.previewPicUrls ?? []).join('\n'),
                }
              : { name: '', remark: null, previewPicUrls: '' }
          }
          modalProps={{ destroyOnHidden: true }}
          onOpenChange={open => { if (!open) setTemplateEditor(undefined); }}
          submitter={{ submitButtonProps: { loading: templateSave.isPending } }}
          onFinish={async value => {
            try {
              await templateSave.mutateAsync(value);
              message.success('已保存');
              setTemplateEditor(undefined);
              await invalidate();
              return true;
            } catch (error) {
              message.error(String(error));
              return false;
            }
          }}
        >
          <ProFormText name="name" label="模板名称" rules={[{ required: true }, { max: 100 }]} />
          <ProFormTextArea name="remark" label="备注" fieldProps={{ maxLength: 255 }} />
          <ProFormTextArea name="previewPicUrls" label="预览图（每行一个地址）" />
        </AdminModalForm>
      )}
      {pageEditor && selected && (
        <AdminModalForm<PageForm>
          open
          title={pageEditor.id ? '编辑页面' : '新增页面'}
          initialValues={
            pageEditor.id
              ? {
                  name: pages.data?.records.find(row => row.id === pageEditor.id)?.name ?? '',
                  remark: pages.data?.records.find(row => row.id === pageEditor.id)?.remark ?? null,
                  previewPicUrls: (pages.data?.records.find(row => row.id === pageEditor.id)?.previewPicUrls ?? []).join('\n'),
                }
              : { name: '', remark: null, previewPicUrls: '' }
          }
          modalProps={{ destroyOnHidden: true }}
          onOpenChange={open => { if (!open) setPageEditor(undefined); }}
          submitter={{ submitButtonProps: { loading: pageSave.isPending } }}
          onFinish={async value => {
            try {
              await pageSave.mutateAsync(value);
              message.success('已保存');
              setPageEditor(undefined);
              await invalidate();
              return true;
            } catch (error) {
              message.error(String(error));
              return false;
            }
          }}
        >
          <ProFormText name="name" label="页面名称" rules={[{ required: true }, { max: 100 }]} />
          <ProFormTextArea name="remark" label="备注" fieldProps={{ maxLength: 255 }} />
          <ProFormTextArea name="previewPicUrls" label="预览图（每行一个地址）" />
        </AdminModalForm>
      )}
      <Modal
        open={!!propertyEditor}
        title="编辑装修属性（JSON）"
        width={800}
        confirmLoading={saveProperty.isPending}
        onCancel={() => setPropertyEditor(undefined)}
        onOk={async () => {
          try {
            await saveProperty.mutateAsync();
            message.success('属性已保存');
            setPropertyEditor(undefined);
            await invalidate();
          } catch (error) {
            message.error(error instanceof SyntaxError ? 'JSON 格式不合法' : String(error));
          }
        }}
      >
        <Input.TextArea
          rows={18}
          value={propertyEditor?.text ?? ''}
          onChange={event => propertyEditor && setPropertyEditor({ ...propertyEditor, text: event.target.value })}
          spellCheck={false}
        />
      </Modal>
    </PageContainer>
  );
}
