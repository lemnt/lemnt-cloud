import { useState } from 'react';
import {
  PageContainer,
  ProTable,
  ProFormText,
  ProFormDigit,
  ProFormSelect,
  ProFormDateTimePicker,
  ProFormDependency,
  type ProColumns,
} from '@ant-design/pro-components';
import { Alert, App, Button, Popconfirm, Result } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { AdminModalForm } from '@/shared/components/AdminModalForm';
import { usePermission } from '@/shared/hooks/usePermission';
import {
  getCouponTemplates,
  createCouponTemplate,
  updateCouponTemplate,
  deleteCouponTemplate,
  enableCouponTemplate,
  issueCoupon,
  getPublicCouponTemplates,
  setCouponPublicClaim,
} from '../api/checkoutApi';
import { getCategoryList, getSpuList } from '../../catalog/api/catalogApi';
import { positiveId } from '../checkoutDecoders';
import { checkoutKeys } from '../hooks/checkoutKeys';
import { couponKindEnum, couponKindOptions, MAX_COUPON_AMOUNT } from '../constants';
import type { CouponTemplate, CouponTemplateCreateRequest } from '../types';

export default function CouponTemplatePage() {
  const { can } = usePermission();
  const { message } = App.useApp();
  const client = useQueryClient();
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<CouponTemplate>();
  const [issuing, setIssuing] = useState<CouponTemplate>();
  const templates = useQuery({
    queryKey: checkoutKeys.templates(),
    queryFn: getCouponTemplates,
    enabled: can('mall:coupon-template:list'),
  });
  const publicClaims = useQuery({
    queryKey: checkoutKeys.publicClaims(),
    queryFn: getPublicCouponTemplates,
    enabled: can('mall:coupon-template:list'),
  });
  const publish = useMutation({
    mutationFn: ({ id, enabled }: { id: string; enabled: boolean }) =>
      setCouponPublicClaim(id, enabled),
  });
  const create = useMutation({ mutationFn: createCouponTemplate });
  const enable = useMutation({
    mutationFn: ({ id, enabled }: { id: string; enabled: boolean }) =>
      enableCouponTemplate(id, enabled),
  });
  const issue = useMutation({
    mutationFn: ({ memberId, templateId }: { memberId: string; templateId: string }) =>
      issueCoupon(memberId, templateId),
  });
  async function refresh() {
    try {
      await client.invalidateQueries({ queryKey: checkoutKeys.all }, { throwOnError: true });
    } catch (reason) {
      message.warning(`已保存，但刷新失败：${String(reason)}`);
    }
  }
  const columns: ProColumns<CouponTemplate>[] = [
    { title: '编号', dataIndex: 'id' },
    { title: '编码', dataIndex: 'code' },
    {
      title: '会员自助领取',
      renderText: (_, row) =>
        publicClaims.isSuccess
          ? publicClaims.data.includes(row.id)
            ? '已开放'
            : '未开放'
          : '读取未完成',
    },
    { title: '名称', dataIndex: 'name' },
    { title: '类型', dataIndex: 'kind', valueEnum: couponKindEnum },
    { title: '门槛（元）', dataIndex: 'threshold' },
    {
      title: '优惠规则',
      renderText: (_, row) =>
        row.kind === 'FIXED'
          ? `减 ${row.fixedAmount} 元`
          : `支付 ${row.discountPercent}%${row.discountLimit ? `，最多减 ${row.discountLimit} 元` : ''}`,
    },
    {
      title: '已发 / 总量',
      renderText: (_, row) =>
        `${row.issuedCount} / ${row.totalCount === -1 ? '不限' : row.totalCount}`,
    },
    {
      title: '有效期',
      renderText: (_, row) =>
        row.validityType === 'TERM'
          ? `领取 ${row.fixedStartTerm} 天后生效，有效 ${row.fixedEndTerm} 天`
          : `${row.validFrom} 至 ${row.validUntil}`,
    },
    { title: '过期时间', dataIndex: 'validUntil' },
    { title: '启用', renderText: (_, row) => (row.enabled ? '是' : '否') },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) => [
        can('mall:coupon-template:edit') && (
          <Button
            key="edit"
            type="link"
            onClick={() => {
              setEditing(row);
              setOpen(true);
            }}
          >
            修改
          </Button>
        ),
        can('mall:coupon-template:remove') && (
          <Popconfirm
            key="delete"
            title="删除模板？已发券继续有效。"
            onConfirm={async () => {
              try {
                await deleteCouponTemplate(row.id);
                message.success('已删除');
                await refresh();
              } catch (e) {
                message.error(String(e));
              }
            }}
          >
            <Button type="link" danger>
              删除
            </Button>
          </Popconfirm>
        ),
        can('mall:coupon-template:edit') && publicClaims.isSuccess && (
          <Popconfirm
            key="public"
            title={
              publicClaims.data.includes(row.id)
                ? '关闭会员自助领取？已领券不受此开关影响。'
                : '开放会员自助领取？仍受模板启用、有效期、名额与每人限领规则约束。'
            }
            onConfirm={async () => {
              try {
                await publish.mutateAsync({
                  id: row.id,
                  enabled: !publicClaims.data.includes(row.id),
                });
                message.success('公开领取设置已保存');
                await refresh();
              } catch (e) {
                message.error(String(e));
              }
            }}
          >
            <Button type="link" disabled={publish.isPending}>
              {publicClaims.data.includes(row.id) ? '关闭自助领取' : '开放自助领取'}
            </Button>
          </Popconfirm>
        ),
        can('mall:coupon:issue') && (
          <Button
            key="issue"
            type="link"
            disabled={!row.enabled || row.takeType !== 'ADMIN' || issue.isPending}
            onClick={() => setIssuing(row)}
          >
            发放给会员
          </Button>
        ),
        can('mall:coupon-template:edit') && (
          <Popconfirm
            key="enable"
            title={row.enabled ? '停用后禁止新发放，会员已领券继续有效。确认停用？' : '确认启用？'}
            onConfirm={async () => {
              try {
                if (!(await enable.mutateAsync({ id: row.id, enabled: !row.enabled })))
                  throw new Error('模板不存在');
                message.success('状态已更新');
                await refresh();
              } catch (reason) {
                message.error(String(reason));
              }
            }}
          >
            <Button type="link" disabled={enable.isPending}>
              {row.enabled ? '停用' : '启用'}
            </Button>
          </Popconfirm>
        ),
      ],
    },
  ];
  if (!can('mall:coupon-template:list'))
    return <Result status="403" title="没有优惠券模板查询权限" />;
  return (
    <PageContainer title="优惠券模板与发放">
      <Alert
        type="info"
        title="后台授权操作；领取次数、适用范围和有效期按模板配置；已发券保留领取时的权益。"
      />
      {publicClaims.isError && (
        <Alert
          type="error"
          title="公开领取设置读取失败，操作已禁止"
          action={<Button onClick={() => publicClaims.refetch()}>重试</Button>}
        />
      )}
      {templates.isError && (
        <Alert
          type="error"
          title="模板读取失败"
          description={String(templates.error)}
          action={<Button onClick={() => templates.refetch()}>重试</Button>}
        />
      )}
      <ProTable<CouponTemplate>
        rowKey="id"
        search={false}
        columns={columns}
        loading={templates.isFetching}
        dataSource={templates.data ?? []}
        scroll={{ x: 1200 }}
        toolBarRender={() => [
          can('mall:coupon-template:add') && (
            <Button
              key="add"
              type="primary"
              onClick={() => {
                setEditing(undefined);
                setOpen(true);
              }}
            >
              新建模板
            </Button>
          ),
        ]}
      />
      <AdminModalForm<CouponTemplateCreateRequest>
        key={String(open)}
        title={editing ? '修改优惠券模板' : '新建优惠券模板'}
        open={open}
        onOpenChange={setOpen}
        initialValues={
          editing ?? {
            kind: 'FIXED',
            threshold: 0,
            totalCount: 100,
            takeType: 'ADMIN',
            takeLimitCount: 1,
            productScope: 'ALL',
            productScopeValues: [],
            validityType: 'DATE',
            fixedStartTerm: 0,
            fixedEndTerm: 7,
          }
        }
        modalProps={{ destroyOnHidden: true, maskClosable: false }}
        submitter={{ submitButtonProps: { loading: create.isPending } }}
        onFinish={async (values) => {
          if (
            create.isPending ||
            !can(editing ? 'mall:coupon-template:edit' : 'mall:coupon-template:add')
          )
            return false;
          const payload: CouponTemplateCreateRequest = {
            ...values,
            fixedAmount: values.kind === 'FIXED' ? values.fixedAmount : undefined,
            discountPercent: values.kind === 'PERCENT' ? values.discountPercent : undefined,
            discountLimit: values.kind === 'PERCENT' ? values.discountLimit : undefined,
          };
          try {
            if (editing) await updateCouponTemplate(editing.id, payload);
            else await create.mutateAsync(payload);
            message.success('模板已保存');
            setOpen(false);
            await refresh();
            return true;
          } catch (reason) {
            message.error(String(reason));
            return false;
          }
        }}
      >
        <ProFormText name="code" label="模板编码" rules={[{ required: true }, { max: 64 }]} />
        <ProFormText name="name" label="名称" rules={[{ required: true }, { max: 100 }]} />
        <ProFormSelect
          name="kind"
          label="类型"
          options={couponKindOptions}
          rules={[{ required: true }]}
        />
        <ProFormDigit
          name="threshold"
          label="商品金额门槛（元）"
          min={0}
          max={MAX_COUPON_AMOUNT}
          fieldProps={{ precision: 2 }}
          rules={[{ required: true }]}
        />
        <ProFormDependency name={['kind']}>
          {({ kind }) =>
            kind === 'FIXED' ? (
              <ProFormDigit
                name="fixedAmount"
                label="满减金额（元）"
                min={0.01}
                max={MAX_COUPON_AMOUNT}
                fieldProps={{ precision: 2 }}
                rules={[{ required: true }]}
              />
            ) : (
              <>
                <ProFormDigit
                  name="discountPercent"
                  label="支付百分比（80表示8折）"
                  min={1}
                  max={100}
                  fieldProps={{ precision: 0 }}
                  rules={[{ required: true }]}
                />
                <ProFormDigit
                  name="discountLimit"
                  label="最多优惠金额（元，可不填）"
                  min={0.01}
                  max={MAX_COUPON_AMOUNT}
                  fieldProps={{ precision: 2 }}
                />
              </>
            )
          }
        </ProFormDependency>
        <ProFormDigit
          name="totalCount"
          label="总发放数量（-1不限）"
          min={-1}
          fieldProps={{ precision: 0 }}
          rules={[{ required: true }]}
        />
        <ProFormText name="description" label="使用说明" rules={[{ max: 500 }]} />
        <ProFormSelect
          name="takeType"
          label="领取方式"
          options={[
            { label: '会员领取', value: 'USER' },
            { label: '后台发放', value: 'ADMIN' },
          ]}
          rules={[{ required: true }]}
        />
        <ProFormDigit
          name="takeLimitCount"
          label="每人限领（-1不限）"
          min={-1}
          fieldProps={{ precision: 0 }}
          rules={[
            { required: true },
            {
              validator: (_, v) =>
                v === -1 || v > 0 ? Promise.resolve() : Promise.reject(new Error('填写正整数或-1')),
            },
          ]}
        />
        <ProFormSelect
          name="productScope"
          label="适用范围"
          options={[
            { label: '全部商品', value: 'ALL' },
            { label: '指定SPU', value: 'SPU_IDS' },
            { label: '指定分类', value: 'CATEGORY_IDS' },
          ]}
          rules={[{ required: true }]}
        />
        <ProFormDependency name={['productScope']}>
          {({ productScope }) =>
            productScope !== 'ALL' && (
              <ProFormSelect
                key={productScope}
                name="productScopeValues"
                label={productScope === 'SPU_IDS' ? '适用商品' : '适用分类'}
                mode="multiple"
                showSearch
                debounceTime={300}
                request={async ({ keyWords }) =>
                  productScope === 'CATEGORY_IDS'
                    ? (await getCategoryList()).map((row) => ({ label: row.name, value: row.id }))
                    : (await getSpuList({ name: keyWords, pageNum: 1, pageSize: 100 })).data.map(
                        (row) => ({ label: row.name, value: row.id }),
                      )
                }
                rules={[{ required: true }]}
              />
            )
          }
        </ProFormDependency>
        <ProFormSelect
          name="validityType"
          label="有效期类型"
          options={[
            { label: '固定日期', value: 'DATE' },
            { label: '领取后生效', value: 'TERM' },
          ]}
          rules={[{ required: true }]}
        />
        <ProFormDependency name={['validityType']}>
          {({ validityType }) =>
            validityType === 'TERM' ? (
              <>
                <ProFormDigit
                  name="fixedStartTerm"
                  label="领取后生效天数"
                  min={0}
                  fieldProps={{ precision: 0 }}
                  rules={[{ required: true }]}
                />
                <ProFormDigit
                  name="fixedEndTerm"
                  label="生效后有效天数"
                  min={1}
                  fieldProps={{ precision: 0 }}
                  rules={[{ required: true }]}
                />
              </>
            ) : (
              <>
                <ProFormDateTimePicker
                  name="validFrom"
                  label="生效时间（数据库时区）"
                  rules={[{ required: true }]}
                />
                <ProFormDateTimePicker
                  name="validUntil"
                  label="过期时间（右开边界）"
                  rules={[{ required: true }]}
                />
              </>
            )
          }
        </ProFormDependency>
      </AdminModalForm>
      <AdminModalForm<{ memberId: string }>
        key={issuing?.id ?? 'issue'}
        title={`发放：${issuing?.name ?? ''}`}
        open={Boolean(issuing)}
        onOpenChange={(value) => {
          if (!value) setIssuing(undefined);
        }}
        modalProps={{ destroyOnHidden: true, maskClosable: false }}
        submitter={{ submitButtonProps: { loading: issue.isPending } }}
        onFinish={async ({ memberId }) => {
          if (!issuing || issue.isPending || !can('mall:coupon:issue')) return false;
          try {
            const coupon = await issue.mutateAsync({
              memberId: positiveId(memberId.trim()),
              templateId: issuing.id,
            });
            message.success(`已发放优惠券 ${coupon.id}，状态 ${coupon.status}`);
            setIssuing(undefined);
            await refresh();
            return true;
          } catch (reason) {
            message.error(String(reason));
            return false;
          }
        }}
      >
        <ProFormText
          name="memberId"
          label="目标会员编号"
          rules={[{ required: true }, { pattern: /^[1-9]\d{0,18}$/, message: '请输入正整数编号' }]}
        />
      </AdminModalForm>
    </PageContainer>
  );
}
