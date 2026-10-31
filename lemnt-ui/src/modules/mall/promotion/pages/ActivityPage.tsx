import { useState } from 'react';
import {
  PageContainer,
  ProTable,
  ProFormText,
  ProFormTextArea,
  ProFormSelect,
  ProFormDateTimePicker,
  ProFormList,
  ProFormDependency,
  type ProColumns,
} from '@ant-design/pro-components';
import { Alert, App, Button, Descriptions, Form, Modal, Popconfirm, Result, Table } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { AdminModalForm } from '@/shared/components/AdminModalForm';
import { usePermission } from '@/shared/hooks/usePermission';
import { getCategoryList, getSpuList, getSpuDetail } from '../../catalog/api/catalogApi';
import * as api from '../api/promotionApi';
import type {
  ActivityKind,
  ActivitySummary,
  ActivityEditorValues,
  ActivityFilter,
  RewardActivity,
  DiscountActivity,
} from '../types';

/** 两类活动共享列表和状态操作，各自保留独立规则与请求。 */
export function ActivityPage({ kind }: { kind: ActivityKind }) {
  const { can } = usePermission();
  const { message } = App.useApp();
  const cache = useQueryClient();
  const label = kind === 'reward' ? '满减活动' : '折扣活动';
  const [filters, setFilters] = useState<Pick<ActivityFilter, 'name' | 'status'>>({});
  const [page, setPage] = useState({ pageNum: 1, pageSize: 20 });
  const [editor, setEditor] = useState<{ id: string | null; readOnly: boolean }>();
  const rows = useQuery({
    queryKey: ['promotion', kind, 'page', filters, page],
    queryFn: () => api.getActivityPage(kind, { ...filters, ...page }),
    enabled: can('mall:promotion:list'),
  });
  const detail = useQuery<RewardActivity | DiscountActivity>({
    queryKey: ['promotion', kind, 'detail', editor?.id],
    queryFn: () =>
      kind === 'reward' ? api.getReward(editor?.id ?? '') : api.getDiscount(editor?.id ?? ''),
    enabled: !!editor?.id && can('mall:promotion:query'),
  });
  const save = useMutation({
    mutationFn: async (v: ActivityEditorValues) => {
      if (kind === 'reward') {
        const payload = api.rewardPayload(v);
        if (editor?.id) await api.updateReward(editor.id, payload);
        else await api.createReward(payload);
      } else {
        const payload = api.discountPayload(v);
        if (editor?.id) await api.updateDiscount(editor.id, payload);
        else await api.createDiscount(payload);
      }
    },
  });
  const change = useMutation({
    mutationFn: ({ id, remove }: { id: string; remove: boolean }) =>
      remove ? api.deleteActivity(kind, id) : api.closeActivity(kind, id),
  });
  async function refresh() {
    try {
      await cache.invalidateQueries({ queryKey: ['promotion', kind] }, { throwOnError: true });
    } catch {
      message.warning('操作成功，刷新失败，请重新加载');
    }
  }
  async function mutate(id: string, remove: boolean) {
    try {
      await change.mutateAsync({ id, remove });
      message.success(remove ? '已删除' : '已关闭');
      await refresh();
    } catch (error) {
      message.error(String(error));
    }
  }
  const columns: ProColumns<ActivitySummary>[] = [
    { title: '活动编号', dataIndex: 'id', search: false },
    { title: '活动名称', dataIndex: 'name' },
    {
      title: '状态',
      dataIndex: 'status',
      valueType: 'select',
      valueEnum: { 0: { text: '开启' }, 1: { text: '关闭' } },
    },
    { title: '开始时间', dataIndex: 'startTime', search: false },
    { title: '结束时间', dataIndex: 'endTime', search: false },
    { title: '备注', dataIndex: 'remark', search: false },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) => [
        can('mall:promotion:query') && (
          <Button
            key="detail"
            type="link"
            onClick={() => setEditor({ id: row.id, readOnly: true })}
          >
            详情
          </Button>
        ),
        row.status === 0 && can('mall:promotion:update') && can('mall:promotion:query') && (
          <Button key="edit" type="link" onClick={() => setEditor({ id: row.id, readOnly: false })}>
            编辑
          </Button>
        ),
        row.status === 0 && can('mall:promotion:update') && (
          <Popconfirm
            key="close"
            title="关闭后不能重新开启，确认关闭？"
            onConfirm={() => mutate(row.id, false)}
          >
            <Button type="link" disabled={change.isPending}>
              关闭
            </Button>
          </Popconfirm>
        ),
        row.status === 1 && can('mall:promotion:delete') && (
          <Popconfirm
            key="delete"
            title="确认删除已关闭活动？"
            onConfirm={() => mutate(row.id, true)}
          >
            <Button type="link" danger disabled={change.isPending}>
              删除
            </Button>
          </Popconfirm>
        ),
      ],
    },
  ];
  if (!can('mall:promotion:list')) return <Result status="403" title="没有活动列表权限" />;
  const loaded = editor?.id ? detail.data : undefined;
  const editable = !editor?.readOnly && (!loaded || loaded.status === 0);
  const initial: ActivityEditorValues = loaded
    ? 'rules' in loaded
      ? loaded
      : {
          ...loaded,
          products: loaded.products.map((p) => ({
            ...p,
            discountRate: p.discountPercent == null ? undefined : String(p.discountPercent / 1000),
          })),
        }
    : {
        name: '',
        startTime: '',
        endTime: '',
        conditionType: 'PRICE',
        productScope: 'ALL',
        productScopeValues: [],
        rules: [{ limit: '', discountPrice: '' }],
        products: [{ spuId: '', skuId: '', discountType: 'PERCENT', discountRate: '8' }],
      };
  return (
    <PageContainer title={label}>
      {rows.isError && (
        <Alert
          type="error"
          title="活动读取失败"
          description={String(rows.error)}
          action={<Button onClick={() => rows.refetch()}>重试</Button>}
        />
      )}
      <ProTable<ActivitySummary>
        options={{ reload: false }}
        rowKey="id"
        scroll={{ x: 1000 }}
        columns={columns}
        dataSource={rows.data?.records ?? []}
        loading={rows.isFetching}
        onSubmit={(v) => {
          setFilters({
            name: typeof v.name === 'string' ? v.name : undefined,
            status:
              v.status === 0 || v.status === '0'
                ? 0
                : v.status === 1 || v.status === '1'
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
        toolBarRender={() => [
          <Button key="refresh" onClick={() => rows.refetch()} loading={rows.isFetching}>
            刷新
          </Button>,
          can('mall:promotion:create') && (
            <Button
              key="new"
              type="primary"
              onClick={() => setEditor({ id: null, readOnly: false })}
            >
              新增{label}
            </Button>
          ),
        ]}
      />
      {editor?.id && (detail.isFetching || detail.isError) && (
        <Modal open title={label} footer={null} onCancel={() => setEditor(undefined)}>
          {detail.isError ? (
            <Alert
              type="error"
              title="详情读取失败，未打开编辑表单"
              description={String(detail.error)}
              action={<Button onClick={() => detail.refetch()}>重试</Button>}
            />
          ) : (
            '正在读取详情…'
          )}
        </Modal>
      )}
      {editor && (!editor.id || (loaded && !detail.isFetching && !detail.isError)) && (
        <AdminModalForm<ActivityEditorValues>
          key={`${kind}-${editor.id ?? 'new'}-${detail.dataUpdatedAt}`}
          open
          title={`${editable ? (editor.id ? '编辑' : '新增') : '查看'}${label}`}
          width={900}
          dateFormatter="string"
          initialValues={initial}
          modalProps={{ destroyOnHidden: true, maskClosable: false, closable: !save.isPending }}
          onOpenChange={(open) => {
            if (!open && !save.isPending) setEditor(undefined);
          }}
          submitter={editable ? { submitButtonProps: { loading: save.isPending } } : false}
          onFinish={async (values) => {
            if (!can(editor.id ? 'mall:promotion:update' : 'mall:promotion:create')) {
              message.error('没有活动保存权限');
              return false;
            }
            try {
              await save.mutateAsync(values);
              message.success('已保存');
              setEditor(undefined);
              await refresh();
              return true;
            } catch (error) {
              message.error(String(error));
              return false;
            }
          }}
        >
          {!editable && loaded ? (
            <ActivityDetails value={loaded} />
          ) : (
            <>
              <ProFormText
                name="name"
                label="活动名称"
                rules={[{ required: true }, { max: 100 }]}
              />
              <ProFormDateTimePicker
                name="startTime"
                label="开始时间"
                rules={[{ required: true }]}
              />
              <ProFormDateTimePicker name="endTime" label="结束时间" rules={[{ required: true }]} />
              <ProFormTextArea name="remark" label="备注" />
              {kind === 'reward' ? <RewardFields /> : <DiscountFields />}
            </>
          )}
        </AdminModalForm>
      )}
    </PageContainer>
  );
}
function ActivityDetails({ value }: { value: RewardActivity | DiscountActivity }) {
  return (
    <>
      <Descriptions
        column={1}
        items={[
          { key: 'name', label: '名称', children: value.name },
          { key: 'status', label: '状态', children: value.status === 0 ? '开启' : '关闭' },
          { key: 'time', label: '活动时间', children: `${value.startTime} 至 ${value.endTime}` },
          { key: 'remark', label: '备注', children: value.remark || '—' },
        ]}
      />
      {'rules' in value ? (
        <>
          <p>
            适用范围：
            {value.productScope === 'ALL'
              ? '全部商品'
              : `${value.productScope === 'SPU_IDS' ? '商品' : '分类'} ${value.productScopeValues.join('、')}`}
          </p>
          {value.rules.map((rule, i) => (
            <p key={i}>
              满 {rule.limit} {value.conditionType === 'PRICE' ? '元' : '件'}，减{' '}
              {rule.discountPrice} 元
            </p>
          ))}
        </>
      ) : (
        <Table
          rowKey="skuId"
          pagination={false}
          dataSource={value.products}
          columns={[
            { title: '商品编号', dataIndex: 'spuId' },
            { title: 'SKU编号', dataIndex: 'skuId' },
            {
              title: '优惠',
              render: (_, row) =>
                row.discountType === 'PRICE'
                  ? `每件减 ${row.discountPrice} 元`
                  : row.discountPercent == null
                    ? '折扣未记录'
                    : `${row.discountPercent / 1000} 折`,
            },
          ]}
        />
      )}
    </>
  );
}
function RewardFields() {
  const { can } = usePermission();
  const form = Form.useFormInstance<ActivityEditorValues>();
  const condition = Form.useWatch('conditionType', form);
  return (
    <>
      <ProFormSelect
        name="conditionType"
        label="优惠条件"
        options={[
          { label: '满金额', value: 'PRICE' },
          { label: '满件数', value: 'COUNT' },
        ]}
        rules={[{ required: true }]}
      />
      <ProFormSelect
        name="productScope"
        label="适用范围"
        options={[
          { label: '全部商品', value: 'ALL' },
          { label: '指定商品', value: 'SPU_IDS' },
          { label: '指定分类', value: 'CATEGORY_IDS' },
        ]}
        fieldProps={{ onChange: () => form.setFieldValue('productScopeValues', []) }}
        rules={[{ required: true }]}
      />
      <ProFormDependency name={['productScope']}>
        {({ productScope }) =>
          productScope !== 'ALL' && (
            <>
              {!(productScope === 'CATEGORY_IDS'
                ? can('mall:category:list')
                : can('mall:spu:list')) && (
                <Alert type="warning" title="需要对应商品或分类查询权限后才能选择范围" />
              )}
              <ProFormSelect
                key={productScope}
                name="productScopeValues"
                label="适用商品或分类"
                mode="multiple"
                showSearch
                debounceTime={300}
                disabled={
                  !(productScope === 'CATEGORY_IDS'
                    ? can('mall:category:list')
                    : can('mall:spu:list'))
                }
                request={async ({ keyWords }) => {
                  if (productScope === 'CATEGORY_IDS')
                    return can('mall:category:list')
                      ? (await getCategoryList()).map((r) => ({ label: r.name, value: r.id }))
                      : [];
                  return can('mall:spu:list')
                    ? (await getSpuList({ name: keyWords, pageNum: 1, pageSize: 100 })).data.map(
                        (r) => ({ label: r.name, value: r.id }),
                      )
                    : [];
                }}
                rules={[{ required: true }]}
              />
            </>
          )
        }
      </ProFormDependency>
      <ProFormList
        name="rules"
        label="满减阶梯（门槛递增）"
        min={1}
        copyIconProps={false}
        creatorButtonProps={{ creatorButtonText: '添加阶梯' }}
      >
        <ProFormText
          name="limit"
          label={condition === 'COUNT' ? '优惠门槛（件）' : '优惠门槛（元）'}
          rules={[{ required: true }]}
        />
        <ProFormText name="discountPrice" label="减免金额（元）" rules={[{ required: true }]} />
      </ProFormList>
    </>
  );
}
function DiscountFields() {
  const { can } = usePermission();
  return (
    <>
      {(!can('mall:spu:list') || !can('mall:spu:query')) && (
        <Alert type="warning" title="选择活动SKU需要商品列表及详情查询权限" />
      )}
      <ProFormList
        name="products"
        label="折扣商品"
        min={1}
        copyIconProps={false}
        creatorButtonProps={{ creatorButtonText: '添加商品SKU' }}
      >
        {(field) => <DiscountProductFields index={field.name} />}
      </ProFormList>
    </>
  );
}
function DiscountProductFields({ index }: { index: number }) {
  const { can } = usePermission();
  const form = Form.useFormInstance<ActivityEditorValues>();
  const raw = Form.useWatch(['products', index, 'spuId'], form);
  const mode = Form.useWatch(['products', index, 'discountType'], form);
  const spuId = typeof raw === 'string' ? raw : undefined;
  const skus = useQuery({
    queryKey: ['promotion', 'spu', spuId],
    queryFn: () => getSpuDetail(spuId ?? ''),
    enabled: !!spuId && can('mall:spu:query'),
  });
  return (
    <>
      <ProFormSelect
        name="spuId"
        label="商品"
        showSearch
        debounceTime={300}
        disabled={!can('mall:spu:list')}
        request={async ({ keyWords }) =>
          can('mall:spu:list')
            ? (await getSpuList({ name: keyWords, pageNum: 1, pageSize: 100 })).data.map((r) => ({
                label: r.name,
                value: r.id,
              }))
            : []
        }
        fieldProps={{ onChange: () => form.setFieldValue(['products', index, 'skuId'], undefined) }}
        rules={[{ required: true }]}
      />
      {skus.isError && (
        <Alert
          type="error"
          title="SKU读取失败"
          action={<Button onClick={() => skus.refetch()}>重试</Button>}
        />
      )}
      <ProFormSelect
        name="skuId"
        label="SKU"
        disabled={!spuId || !can('mall:spu:query') || skus.isFetching || skus.isError}
        options={skus.data?.skus.map((r) => ({
          label: `${r.name}（${r.code}，${r.price}元）`,
          value: r.id,
        }))}
        rules={[{ required: true }]}
      />
      <ProFormSelect
        name="discountType"
        label="优惠方式"
        options={[
          { label: '减价', value: 'PRICE' },
          { label: '打折', value: 'PERCENT' },
        ]}
        rules={[{ required: true }]}
      />
      {mode === 'PRICE' ? (
        <ProFormText name="discountPrice" label="每件减价金额（元）" rules={[{ required: true }]} />
      ) : (
        <ProFormText name="discountRate" label="折扣（折，例如8）" rules={[{ required: true }]} />
      )}
    </>
  );
}
export function RewardActivityPage() {
  return <ActivityPage kind="reward" />;
}
export function DiscountActivityPage() {
  return <ActivityPage kind="discount" />;
}
