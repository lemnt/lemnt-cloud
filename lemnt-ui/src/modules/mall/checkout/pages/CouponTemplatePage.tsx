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
  enableCouponTemplate,
  issueCoupon,
  getPublicCouponTemplates,
  setCouponPublicClaim,
} from '../api/checkoutApi';
import { positiveId } from '../checkoutDecoders';
import { checkoutKeys } from '../hooks/checkoutKeys';
import { couponKindEnum, couponKindOptions, MAX_COUPON_AMOUNT } from '../constants';
import type { CouponTemplate, CouponTemplateCreateRequest } from '../types';

export default function CouponTemplatePage() {
  const { can } = usePermission();
  const { message } = App.useApp();
  const client = useQueryClient();
  const [open, setOpen] = useState(false);
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
    { title: '已发 / 总量', renderText: (_, row) => `${row.issuedCount} / ${row.totalCount}` },
    { title: '生效时间', dataIndex: 'validFrom' },
    { title: '过期时间', dataIndex: 'validUntil' },
    { title: '启用', renderText: (_, row) => (row.enabled ? '是' : '否') },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) => [
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
            disabled={!row.enabled || issue.isPending}
            onClick={() => setIssuing(row)}
          >
            发放给会员
          </Button>
        ),
        can('mall:coupon-template:edit') && (
          <Popconfirm
            key="enable"
            title={
              row.enabled
                ? '停用后禁止新发放及新兑换，已锁定订单继续有效。确认停用？'
                : '确认启用？'
            }
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
        title="后台授权操作；每会员每模板限一张。发放与兑换均核验有效期，规则创建后不能修改。"
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
            <Button key="add" type="primary" onClick={() => setOpen(true)}>
              新建模板
            </Button>
          ),
        ]}
      />
      <AdminModalForm<CouponTemplateCreateRequest>
        key={String(open)}
        title="新建优惠券模板"
        open={open}
        onOpenChange={setOpen}
        initialValues={{ kind: 'FIXED', threshold: 0, totalCount: 100 }}
        modalProps={{ destroyOnHidden: true, maskClosable: false }}
        submitter={{ submitButtonProps: { loading: create.isPending } }}
        onFinish={async (values) => {
          if (create.isPending || !can('mall:coupon-template:add')) return false;
          const payload: CouponTemplateCreateRequest = {
            ...values,
            fixedAmount: values.kind === 'FIXED' ? values.fixedAmount : undefined,
            discountPercent: values.kind === 'PERCENT' ? values.discountPercent : undefined,
            discountLimit: values.kind === 'PERCENT' ? values.discountLimit : undefined,
          };
          try {
            await create.mutateAsync(payload);
            message.success('模板已创建');
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
                  max={99}
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
          label="总发放数量"
          min={1}
          max={1000000}
          fieldProps={{ precision: 0 }}
          rules={[{ required: true }]}
        />
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
