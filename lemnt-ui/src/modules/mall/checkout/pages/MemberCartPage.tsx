import { useRef, useState } from 'react';
import {
  PageContainer,
  ProTable,
  ProFormText,
  ProFormDigit,
  ProFormCheckbox,
  type ProColumns,
} from '@ant-design/pro-components';
import {
  Alert,
  App,
  Button,
  Descriptions,
  Input,
  Popconfirm,
  Result,
  Select,
  Space,
  Tag,
} from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { usePermission } from '@/shared/hooks/usePermission';
import { AdminModalForm } from '@/shared/components/AdminModalForm';
import { useSession } from '@/stores/userStore';
import * as api from '../api/checkoutApi';
import { positiveId } from '../checkoutDecoders';
import { intentKey, persistIntent, restoreIntent } from '../checkoutIntent';
import { checkoutKeys } from '../hooks/checkoutKeys';
import { checkoutStatusEnum } from '../constants';
import type { CartItem, CheckoutIntent, CheckoutQuote, CheckoutReceipt } from '../types';
interface CartFormValues {
  productId: string;
  quantity: number;
  selected: boolean;
}

export default function MemberCartPage() {
  const { can } = usePermission();
  const operatorId = useSession((state) => state.user?.user.userId);
  return operatorId == null ? (
    <Result status="403" title="请先登录后台" />
  ) : !can('mall:cart:query') ? (
    <Result status="403" title="没有购物车查询权限" />
  ) : (
    <CartWorkspace key={String(operatorId)} operatorId={String(operatorId)} />
  );
}
function CartWorkspace({ operatorId }: { operatorId: string }) {
  const { can } = usePermission();
  const { message } = App.useApp();
  const client = useQueryClient();
  const storageKey = intentKey(operatorId);
  const [restored] = useState(() => {
    try {
      return restoreIntent(localStorage, storageKey);
    } catch (reason) {
      return { error: `浏览器存储不可用：${String(reason)}` };
    }
  });
  const [intent, setIntent] = useState<CheckoutIntent | undefined>(restored.intent);
  const [storageError, setStorageError] = useState(restored.error);
  const [memberId, setMemberId] = useState(restored.intent?.memberId ?? '');
  const [memberInput, setMemberInput] = useState(restored.intent?.memberId ?? '');
  const [couponId, setCouponId] = useState<string>();
  const [quote, setQuote] = useState<CheckoutQuote>();
  const [receipt, setReceipt] = useState<CheckoutReceipt>();
  const [editing, setEditing] = useState<CartItem>();
  const [open, setOpen] = useState(false);
  const operation = useRef(false);
  const cart = useQuery({
    queryKey: checkoutKeys.cart(memberId),
    queryFn: () => api.getCartList(memberId),
    enabled: Boolean(memberId) && can('mall:cart:query'),
  });
  const coupons = useQuery({
    queryKey: checkoutKeys.coupons(memberId),
    queryFn: () => api.getMemberCoupons(memberId),
    enabled: Boolean(memberId) && can('mall:coupon:list'),
  });
  const templates = useQuery({
    queryKey: checkoutKeys.templates(),
    queryFn: api.getCouponTemplates,
    enabled: can('mall:coupon-template:list'),
  });
  const save = useMutation({
    mutationFn: (values: CartFormValues) =>
      api.saveCartItem(memberId, positiveId(values.productId.trim()), {
        quantity: values.quantity,
        selected: values.selected,
      }),
  });
  const deletion = useMutation({
    mutationFn: (productId: string) => api.deleteCartItem(memberId, productId),
  });
  const preview = useMutation({
    mutationFn: () =>
      api.getCheckoutQuote(memberId, {
        productIds: selectedIds,
        ...(couponId ? { couponId } : {}),
      }),
  });
  const submit = useMutation({
    mutationFn: (value: CheckoutIntent) =>
      api.submitCheckout(value.memberId, {
        requestNo: value.requestNo,
        quoteToken: value.quoteToken,
        productIds: value.productIds,
        ...(value.couponId ? { couponId: value.couponId } : {}),
      }),
  });
  const readReceipt = useMutation({ mutationFn: api.getCheckoutReceipt });
  const selected = (cart.data ?? []).filter((row) => row.selected);
  const selectedIds = selected.map((row) => row.productId);
  const isBusy =
    save.isPending ||
    deletion.isPending ||
    preview.isPending ||
    submit.isPending ||
    readReceipt.isPending;
  const isLocked = Boolean(intent || storageError || receipt?.status === 'RESERVING');
  async function refresh() {
    try {
      await client.invalidateQueries({ queryKey: checkoutKeys.all });
    } catch (reason) {
      message.warning(`操作已成功，但刷新失败：${String(reason)}`);
    }
  }
  async function placeOrder() {
    if (operation.current || !can('mall:checkout:create') || storageError) return;
    operation.current = true;
    try {
      const value: CheckoutIntent = intent ?? {
        memberId,
        quoteToken: quote!.quoteToken,
        requestNo: crypto.randomUUID(),
        productIds: selectedIds,
        ...(couponId ? { couponId } : {}),
      };
      persistIntent(localStorage, storageKey, value);
      setIntent(value);
      const result = await submit.mutateAsync(value);
      setReceipt(result);
      setQuote(undefined);
      message.success(
        `订单 ${result.orderNo} 已记录，当前状态：${checkoutStatusEnum[result.status].text}`,
      );
      try {
        localStorage.removeItem(storageKey);
        setIntent(undefined);
      } catch (reason) {
        message.warning(`订单已记录，但本地请求清理失败；重试仍会读取同一订单：${String(reason)}`);
      }
      await refresh();
    } catch (reason) {
      message.error(`未确认提交结果，请保留原请求重试：${String(reason)}`);
    } finally {
      operation.current = false;
    }
  }
  const columns: ProColumns<CartItem>[] = [
    { title: 'SKU编号', dataIndex: 'productId' },
    { title: '商品名称', renderText: (_, row) => row.name ?? '当前不可售，名称未提供' },
    { title: '数量', dataIndex: 'quantity' },
    { title: '单价（元）', renderText: (_, row) => row.unitPrice ?? '—' },
    { title: '可用库存', renderText: (_, row) => row.availableStock ?? '—' },
    {
      title: '状态',
      render: (_, row) => (
        <Tag color={row.sellable ? 'green' : 'red'}>{row.sellable ? '可售' : '不可售'}</Tag>
      ),
    },
    { title: '选中', renderText: (_, row) => (row.selected ? '是' : '否') },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) => [
        can('mall:cart:edit') && (
          <Button
            key="edit"
            type="link"
            disabled={isBusy || isLocked}
            onClick={() => {
              setEditing(row);
              setOpen(true);
            }}
          >
            修改
          </Button>
        ),
        can('mall:cart:edit') && (
          <Popconfirm
            key="delete"
            title="确认删除该购物车条目？"
            disabled={isBusy || isLocked}
            onConfirm={async () => {
              if (operation.current) return;
              operation.current = true;
              try {
                if (!(await deletion.mutateAsync(row.productId))) throw new Error('条目不存在');
                setQuote(undefined);
                message.success('已删除');
                await refresh();
              } catch (reason) {
                message.error(String(reason));
              } finally {
                operation.current = false;
              }
            }}
          >
            <Button type="link" danger disabled={isBusy || isLocked}>
              删除
            </Button>
          </Popconfirm>
        ),
      ],
    },
  ];
  return (
    <PageContainer title="后台会员购物车与结算">
      <Alert
        type="info"
        title="这是后台授权代操作；目标会员编号不代表当前后台登录身份。预览不锁库存，提交后需等待 Outbox 确认。"
      />
      <Input.Search
        aria-label="目标会员编号"
        placeholder="输入目标会员编号"
        value={memberInput}
        disabled={isBusy || isLocked}
        onChange={(event) => setMemberInput(event.target.value)}
        onSearch={() => {
          try {
            setMemberId(positiveId(memberInput.trim()));
            setCouponId(undefined);
            setQuote(undefined);
            setReceipt(undefined);
          } catch (reason) {
            message.error(String(reason));
          }
        }}
        enterButton="查询"
      />
      {(storageError || intent) && (
        <Alert
          type="warning"
          title={storageError ?? `存在未确认请求 ${intent?.requestNo}，只能按原内容重试`}
          action={
            <Space>
              {intent && can('mall:checkout:create') && (
                <Button loading={submit.isPending} onClick={placeOrder}>
                  重试原请求
                </Button>
              )}
              <Popconfirm
                title="仅清除本地请求，不会取消服务器可能已创建的订单。请先重试核验或人工确认订单状态，确认重置？"
                onConfirm={() => {
                  try {
                    localStorage.removeItem(storageKey);
                    setIntent(undefined);
                    setStorageError(undefined);
                    setQuote(undefined);
                  } catch (reason) {
                    message.error(String(reason));
                  }
                }}
              >
                <Button disabled={isBusy}>核验后重置</Button>
              </Popconfirm>
            </Space>
          }
        />
      )}
      {cart.isError && (
        <Alert
          type="error"
          title="购物车加载失败"
          description={String(cart.error)}
          action={<Button onClick={() => cart.refetch()}>重试</Button>}
        />
      )}
      <ProTable<CartItem>
        rowKey="id"
        search={false}
        columns={columns}
        dataSource={cart.data ?? []}
        loading={cart.isFetching}
        scroll={{ x: 900 }}
        toolBarRender={() => [
          can('mall:cart:edit') && (
            <Button
              key="add"
              type="primary"
              disabled={!memberId || isBusy || isLocked}
              onClick={() => {
                setEditing(undefined);
                setOpen(true);
              }}
            >
              添加 SKU
            </Button>
          ),
        ]}
      />
      {memberId && (
        <Space wrap>
          <Select<string>
            aria-label="使用优惠券"
            placeholder="不使用优惠券"
            allowClear
            value={couponId}
            disabled={!can('mall:coupon:list') || isBusy || isLocked}
            loading={coupons.isFetching}
            onChange={(value) => {
              setCouponId(value);
              setQuote(undefined);
            }}
            options={coupons.data
              ?.filter((row) => row.status === 'AVAILABLE')
              .map((row) => ({
                value: row.id,
                label: `${templates.data?.find((item) => item.id === row.templateId)?.name ?? `模板 ${row.templateId}`}（券 ${row.id}）`,
              }))}
          />
          {can('mall:checkout:quote') && (
            <Button
              disabled={
                !selectedIds.length || selected.some((row) => !row.sellable) || isBusy || isLocked
              }
              loading={preview.isPending}
              onClick={async () => {
                try {
                  setQuote(await preview.mutateAsync());
                } catch (reason) {
                  setQuote(undefined);
                  message.error(String(reason));
                }
              }}
            >
              服务器重新计价
            </Button>
          )}
        </Space>
      )}
      {(coupons.isError || templates.isError) && (
        <Alert
          type="warning"
          title="优惠券或模板数据未完整加载"
          description={String(coupons.error ?? templates.error)}
        />
      )}
      {quote && (
        <>
          <Descriptions
            title="服务器结算预览（元）"
            items={[
              { key: 'original', label: '原金额', children: quote.originalAmount },
              { key: 'discount', label: '优惠金额', children: quote.discountAmount },
              { key: 'payable', label: '应付金额', children: quote.payableAmount },
            ]}
          />
          {quote.payableAmount === '0.00' && (
            <Alert type="warning" title="0 元结算尚未实现，请更换优惠券；不会静默收取最低费用。" />
          )}
          {can('mall:checkout:create') && (
            <Popconfirm
              title="确认提交？服务器会重新计价，并先记录订单与库存预占意图。"
              onConfirm={placeOrder}
              disabled={quote.payableAmount === '0.00' || isBusy || isLocked}
            >
              <Button
                type="primary"
                disabled={quote.payableAmount === '0.00' || isBusy || isLocked}
                loading={submit.isPending}
              >
                确认结算
              </Button>
            </Popconfirm>
          )}
        </>
      )}
      {receipt && (
        <>
          <Descriptions
            title="结算订单"
            items={[
              { key: 'order', label: '订单号', children: receipt.orderNo },
              { key: 'status', label: '状态', children: checkoutStatusEnum[receipt.status].text },
              { key: 'amount', label: '应付（元）', children: receipt.payableAmount },
            ]}
          />
          {can('mall:checkout:query') && (
            <Button
              loading={readReceipt.isPending}
              onClick={async () => {
                try {
                  setReceipt(await readReceipt.mutateAsync(receipt.orderNo));
                  await refresh();
                } catch (reason) {
                  message.error(String(reason));
                }
              }}
            >
              刷新订单状态
            </Button>
          )}
          {receipt.status !== 'RESERVING' && !intent && (
            <Button
              onClick={() => {
                setReceipt(undefined);
                setQuote(undefined);
              }}
            >
              新一轮结算
            </Button>
          )}
        </>
      )}
      <AdminModalForm<CartFormValues>
        key={`${editing?.id ?? 'new'}-${open}`}
        title={editing ? '修改购物车' : '添加 SKU'}
        open={open}
        onOpenChange={setOpen}
        initialValues={
          editing
            ? {
                productId: editing.productId,
                quantity: editing.quantity,
                selected: editing.selected,
              }
            : { quantity: 1, selected: true }
        }
        modalProps={{ destroyOnHidden: true, maskClosable: false }}
        submitter={{ submitButtonProps: { loading: save.isPending } }}
        onFinish={async (values) => {
          if (operation.current || isLocked || !can('mall:cart:edit')) return false;
          operation.current = true;
          try {
            await save.mutateAsync(values);
            setQuote(undefined);
            message.success('购物车已保存');
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
          name="productId"
          label="SKU 编号"
          disabled={Boolean(editing)}
          rules={[{ required: true }, { pattern: /^[1-9]\d{0,18}$/ }]}
        />
        <ProFormDigit
          name="quantity"
          label="数量"
          min={1}
          max={999}
          fieldProps={{ precision: 0 }}
          rules={[{ required: true }]}
        />
        <ProFormCheckbox name="selected">参与结算（不可售商品只能取消选中或删除）</ProFormCheckbox>
      </AdminModalForm>
    </PageContainer>
  );
}
