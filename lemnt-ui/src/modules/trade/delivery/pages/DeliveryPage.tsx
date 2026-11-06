import { useState } from 'react';
import {
  PageContainer,
  ProTable,
  ProFormText,
  ProFormSelect,
  ProFormDigit,
  ProFormList,
  ProFormTreeSelect,
  ProFormDependency,
  type ProColumns,
} from '@ant-design/pro-components';
import { Alert, App, Button, Popconfirm, Result, TreeSelect } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { AdminModalForm } from '@/shared/components/AdminModalForm';
import { usePermission } from '@/shared/hooks/usePermission';
import { getAreas, areaOptions } from '@/modules/system/area/api/areaApi';
import * as api from '../api/deliveryApi';
const modes = { 1: '按件', 2: '按重量（kg）', 3: '按体积（m³）' };
export function ExpressPage() {
  return <DeliveryPage template={false} />;
}
export function ExpressTemplatePage() {
  return <DeliveryPage template />;
}
/** 公司与模板分别查询、编辑；模板规则只从详情加载。 */
function DeliveryPage({ template }: { template: boolean }) {
  const { can } = usePermission(),
    { message } = App.useApp(),
    cache = useQueryClient();
  const prefix = template ? 'trade:delivery-template' : 'trade:delivery-express';
  const title = template ? '运费模板' : '快递公司';
  const [filters, setFilters] = useState<{ name?: string; status?: number }>({});
  const [page, setPage] = useState({ pageNum: 1, pageSize: 20 });
  const [editor, setEditor] = useState<{ id: string | null; readOnly: boolean }>();
  const rows = useQuery<{ records: (api.Express | api.TemplateSummary)[]; total: number }>({
    queryKey: [prefix, filters, page],
    queryFn: () =>
      template
        ? api.getTemplates({ ...filters, ...page })
        : api.getExpresses({ ...filters, ...page }),
    enabled: can(prefix + ':list'),
  });
  const detail = useQuery<api.Express | api.Template>({
    queryKey: [prefix, 'detail', editor?.id],
    queryFn: () =>
      template ? api.getTemplate(editor?.id ?? '') : api.getExpress(editor?.id ?? ''),
    enabled: !!editor?.id && can(prefix + ':query'),
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
  });
  const areas = useQuery({
    queryKey: ['areas'],
    queryFn: getAreas,
    enabled:
      (template && !!editor && can(prefix + ':query')) ||
      (template && editor?.id === null && can(prefix + ':create')),
  });
  const save = useMutation({
    mutationFn: (values: api.ExpressValues | api.TemplateValues) =>
      template
        ? api.saveTemplate(editor?.id ?? null, values as api.TemplateValues)
        : api.saveExpress(editor?.id ?? null, values as api.ExpressValues),
  });
  const remove = useMutation({
    mutationFn: (key: string) => (template ? api.deleteTemplate(key) : api.deleteExpress(key)),
  });
  async function refresh() {
    try {
      await cache.invalidateQueries({ queryKey: [prefix] }, { throwOnError: true });
    } catch {
      message.warning('操作已成功，刷新失败请重试');
    }
  }
  const columns: ProColumns<api.Express | api.TemplateSummary>[] = [
    { title: '编号', dataIndex: 'id', search: false },
    { title: '名称', dataIndex: 'name' },
    ...(template
      ? [{ title: '计费方式', dataIndex: 'chargeMode', valueEnum: modes, search: false }]
      : [
          { title: '编码', dataIndex: 'code', search: false },
          {
            title: '状态',
            dataIndex: 'status',
            valueType: 'select' as const,
            valueEnum: { 0: '启用', 1: '停用' },
          },
        ]),
    { title: '排序', dataIndex: 'sort', search: false },
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
          {can(prefix + ':query') && can(prefix + ':update') && (
            <Button type="link" onClick={() => setEditor({ id: row.id, readOnly: false })}>
              编辑
            </Button>
          )}
          {can(prefix + ':delete') && (
            <Popconfirm
              title={`确认删除${title}？`}
              onConfirm={async () => {
                if (remove.isPending) return;
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
  const initial = editor?.id
    ? detail.data
    : template
      ? { name: '', sort: 0, chargeMode: 1, charges: [], frees: [] }
      : { code: '', name: '', sort: 0, status: 0 };
  return (
    <PageContainer title={title}>
      {rows.isError && (
        <Alert
          type="error"
          title="列表读取失败"
          description={String(rows.error)}
          action={<Button onClick={() => rows.refetch()}>重试</Button>}
        />
      )}
      <ProTable
        options={{ reload: false }}
        rowKey="id"
        columns={columns}
        dataSource={rows.data?.records ?? []}
        loading={rows.isFetching}
        scroll={{ x: 900 }}
        toolBarRender={() => [
          <Button key="refresh" onClick={() => rows.refetch()}>
            刷新
          </Button>,
          can(prefix + ':create') && (
            <Button
              key="create"
              type="primary"
              onClick={() => setEditor({ id: null, readOnly: false })}
            >
              新增{title}
            </Button>
          ),
        ]}
        onSubmit={(v) => {
          setFilters({
            name: typeof v.name === 'string' ? v.name : undefined,
            status:
              v.status === '0' || v.status === 0
                ? 0
                : v.status === '1' || v.status === 1
                  ? 1
                  : undefined,
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
      {editor && editor.id && detail.isError && (
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
        <AdminModalForm<api.ExpressValues | api.TemplateValues>
          key={`${template}-${editor.id ?? 'new'}-${editor.readOnly}`}
          title={`${editor.readOnly ? '查看' : editor.id ? '编辑' : '新增'}${title}`}
          open
          width={template ? 1000 : 640}
          initialValues={initial}
          disabled={editor.readOnly || save.isPending}
          submitter={
            editor.readOnly
              ? false
              : {
                  submitButtonProps: {
                    loading: save.isPending,
                    disabled: template && !areas.isSuccess,
                  },
                  resetButtonProps: { disabled: save.isPending },
                }
          }
          modalProps={{ maskClosable: false, closable: !save.isPending }}
          onOpenChange={(open) => {
            if (!open && !save.isPending) setEditor(undefined);
          }}
          onFinish={async (values) => {
            if (
              save.isPending ||
              editor.readOnly ||
              !can(prefix + (editor.id ? ':update' : ':create')) ||
              (template && !areas.isSuccess)
            )
              return false;
            try {
              await save.mutateAsync(values);
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
            label="名称"
            rules={[{ required: true, whitespace: true, max: 100 }]}
          />
          <ProFormDigit
            name="sort"
            label="排序"
            min={0}
            max={2147483647}
            fieldProps={{ precision: 0 }}
            rules={[{ required: true }]}
          />
          {template ? (
            <>
              <ProFormSelect
                name="chargeMode"
                label="计费方式"
                options={[
                  { value: 1, label: '按件' },
                  { value: 2, label: '按重量（kg）' },
                  { value: 3, label: '按体积（m³）' },
                ]}
                rules={[{ required: true }]}
              />
              {areas.isError && (
                <Alert
                  type="error"
                  title="配送地区读取失败，暂不能保存"
                  action={<Button onClick={() => areas.refetch()}>重试</Button>}
                />
              )}
              {areas.isFetching && <Alert type="info" title="正在读取配送地区…" />}
              <ProFormDependency name={['chargeMode']}>
                {({ chargeMode }) => (
                  <Alert
                    type="info"
                    title={`数量单位：${chargeMode === 1 ? '件（整数）' : chargeMode === 2 ? 'kg' : 'm³'}；金额单位为元。切换计费方式后请重新核对规则。`}
                  />
                )}
              </ProFormDependency>
              <ProFormList
                name="charges"
                label="计费区域"
                min={1}
                max={100}
                creatorRecord={{
                  areaIds: [],
                  startCount: '1',
                  extraCount: '1',
                  startPrice: '0.00',
                  extraPrice: '0.00',
                }}
                creatorButtonProps={editor.readOnly ? false : { creatorButtonText: '添加计费区域' }}
                copyIconProps={false}
                deleteIconProps={editor.readOnly ? false : undefined}
              >
                <ProFormText name="id" hidden />
                <ProFormTreeSelect
                  name="areaIds"
                  label="配送区域"
                  fieldProps={{
                    treeData: areaOptions(areas.data ?? []),
                    treeCheckable: true,
                    showCheckedStrategy: TreeSelect.SHOW_CHILD,
                    showSearch: true,
                    treeNodeFilterProp: 'title',
                  }}
                  rules={[{ required: true, type: 'array', min: 1 }]}
                />
                <ProFormText name="startCount" label="首量" rules={[{ required: true }]} />
                <ProFormText name="startPrice" label="首费（元）" rules={[{ required: true }]} />
                <ProFormText name="extraCount" label="续量" rules={[{ required: true }]} />
                <ProFormText name="extraPrice" label="续费（元）" rules={[{ required: true }]} />
              </ProFormList>
              <ProFormList
                name="frees"
                label="包邮区域"
                max={100}
                creatorRecord={{ areaIds: [], freeCount: 0, freePrice: '0.00' }}
                creatorButtonProps={editor.readOnly ? false : { creatorButtonText: '添加包邮区域' }}
                copyIconProps={false}
                deleteIconProps={editor.readOnly ? false : undefined}
              >
                <ProFormText name="id" hidden />
                <ProFormTreeSelect
                  name="areaIds"
                  label="包邮区域"
                  fieldProps={{
                    treeData: areaOptions(areas.data ?? []),
                    treeCheckable: true,
                    showCheckedStrategy: TreeSelect.SHOW_CHILD,
                    showSearch: true,
                    treeNodeFilterProp: 'title',
                  }}
                  rules={[{ required: true, type: 'array', min: 1 }]}
                />
                <ProFormDigit
                  name="freeCount"
                  label="包邮数量门槛"
                  min={0}
                  max={2147483647}
                  fieldProps={{ precision: 0 }}
                  rules={[{ required: true }]}
                />
                <ProFormText
                  name="freePrice"
                  label="包邮金额门槛（元）"
                  rules={[{ required: true }]}
                />
              </ProFormList>
            </>
          ) : (
            <>
              <ProFormText
                name="code"
                label="快递编码"
                rules={[{ required: true, whitespace: true, max: 40 }]}
              />
              <ProFormText name="logo" label="图标地址" rules={[{ max: 500 }]} />
              <ProFormSelect
                name="status"
                label="状态"
                options={[
                  { label: '启用', value: 0 },
                  { label: '停用', value: 1 },
                ]}
                rules={[{ required: true }]}
              />
            </>
          )}
        </AdminModalForm>
      )}
    </PageContainer>
  );
}
