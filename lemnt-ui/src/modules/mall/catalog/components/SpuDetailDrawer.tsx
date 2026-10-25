import { useRef, useState } from 'react';
import {
  ProDescriptions,
  ProTable,
  ProFormText,
  ProFormDigit,
  type ProColumns,
} from '@ant-design/pro-components';
import { Alert, App, Button, Drawer, Empty, Space } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { AdminModalForm } from '@/shared/components/AdminModalForm';
import { usePermission } from '@/shared/hooks/usePermission';
import { adjustSkuStock, getSpuDetail, updateSku } from '../api/catalogApi';
import { catalogKeys } from '../hooks/catalogKeys';
import { MAX_STOCK, MAX_EDITABLE_PRICE, shelfStatusEnum } from '../constants';
import { requireWriteSuccess, editablePrice } from '../catalogValidation';
import type { Sku, StockAdjustRequest, SkuUpdateRequest } from '../types';

interface StockOperation {
  sku: Sku;
  requestNo: string;
}
export function SpuDetailDrawer({ id, onClose }: { id?: string; onClose: () => void }) {
  const { can } = usePermission();
  const client = useQueryClient();
  const { message } = App.useApp();
  const [stockOperation, setStockOperation] = useState<StockOperation>();
  const [editing, setEditing] = useState<Sku>();
  const uncertainRequest = useRef<StockAdjustRequest | undefined>(undefined);
  const query = useQuery({
    queryKey: catalogKeys.spu(id ?? 'none'),
    queryFn: () => getSpuDetail(id!),
    enabled: Boolean(id) && can('mall:spu:query'),
  });
  const stock = useMutation({
    mutationFn: (request: StockAdjustRequest) => adjustSkuStock(stockOperation!.sku.id, request),
  });
  const save = useMutation({
    mutationFn: (request: SkuUpdateRequest) => updateSku(editing!.id, request),
  });
  async function refresh() {
    try {
      await client.invalidateQueries({ queryKey: catalogKeys.all });
    } catch (reason) {
      message.warning(`已保存，但刷新失败：${String(reason)}`);
    }
  }
  const columns: ProColumns<Sku>[] = [
    { title: 'SKU 编码', dataIndex: 'code' },
    { title: '名称', dataIndex: 'name' },
    {
      title: '规格',
      renderText: (_, row) =>
        Object.entries(row.properties)
          .map(([key, value]) => `${key}：${value}`)
          .join(' / ') || '无规格',
    },
    { title: '价格（元）', dataIndex: 'price' },
    { title: '可用库存', dataIndex: 'stock' },
    { title: 'SKU 状态', dataIndex: 'status', valueEnum: shelfStatusEnum },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) => (
        <Space>
          {can('mall:sku:edit') && (
            <Button
              type="link"
              disabled={save.isPending || stock.isPending}
              onClick={() => {
                try {
                  editablePrice(row.price);
                  setEditing(row);
                } catch (reason) {
                  message.error(String(reason));
                }
              }}
            >
              修改资料
            </Button>
          )}
          {can('mall:sku:stock') && (
            <Button
              type="link"
              disabled={stock.isPending || save.isPending}
              onClick={() => {
                uncertainRequest.current = undefined;
                setStockOperation({ sku: row, requestNo: crypto.randomUUID() });
              }}
            >
              调整库存
            </Button>
          )}
        </Space>
      ),
    },
  ];
  return (
    <>
      <Drawer
        title="商品与 SKU 详情"
        open={Boolean(id)}
        onClose={() => {
          if (!stock.isPending && !save.isPending) onClose();
        }}
        width={1000}
      >
        {query.isError && (
          <Alert
            type="error"
            title="详情读取失败"
            description={String(query.error)}
            action={<Button onClick={() => query.refetch()}>重试</Button>}
          />
        )}
        {query.data ? (
          <>
            <ProDescriptions
              column={2}
              dataSource={query.data}
              columns={[
                { title: 'SPU 编码', dataIndex: 'code' },
                { title: '名称', dataIndex: 'name' },
                { title: '分类编号', dataIndex: 'categoryId' },
                { title: '品牌编号', dataIndex: 'brandId' },
                { title: '状态', dataIndex: 'status', valueEnum: shelfStatusEnum },
              ]}
            />
            <ProTable<Sku>
              rowKey="id"
              search={false}
              options={false}
              loading={query.isFetching}
              dataSource={query.data.skus}
              columns={columns}
              pagination={false}
              scroll={{ x: 850 }}
            />
          </>
        ) : (
          <Empty description={query.isFetching ? '加载中…' : '暂无详情'} />
        )}
      </Drawer>
      <AdminModalForm<StockAdjustRequest>
        key={stockOperation?.requestNo ?? 'stock'}
        title="库存增减"
        open={Boolean(stockOperation)}
        onOpenChange={(open) => {
          if (!open) setStockOperation(undefined);
        }}
        description="失败后重试保留同一请求编号；请求结果不明时请勿创建新的相同操作。"
        initialValues={{ requestNo: stockOperation?.requestNo }}
        modalProps={{
          maskClosable: false,
          destroyOnHidden: true,
          closable: !stock.isPending,
          keyboard: !stock.isPending,
          cancelButtonProps: { disabled: stock.isPending },
        }}
        submitter={{ submitButtonProps: { loading: stock.isPending } }}
        onFinish={async (values) => {
          if (stock.isPending || !can('mall:sku:stock')) return false;
          if (values.delta === 0) {
            message.error('增减量不能为 0');
            return false;
          }
          if (
            uncertainRequest.current &&
            JSON.stringify(uncertainRequest.current) !== JSON.stringify(values)
          ) {
            message.error('已有请求结果待核验，请按原输入重试；核验前不能更改操作内容');
            return false;
          }
          uncertainRequest.current = values;
          try {
            const result = await stock.mutateAsync(values);
            message.success(`库存调整成功，执行后库存：${result.data.resultingStock}`);
            setStockOperation(undefined);
            uncertainRequest.current = undefined;
            await refresh();
            return true;
          } catch (reason) {
            message.error(String(reason));
            return false;
          }
        }}
      >
        <ProFormText name="requestNo" label="请求编号" readonly />
        <ProFormDigit
          name="delta"
          label="库存增减量"
          min={-MAX_STOCK}
          max={MAX_STOCK}
          fieldProps={{ precision: 0 }}
          rules={[{ required: true }]}
        />
        <ProFormText name="reason" label="变更原因" rules={[{ required: true }, { max: 200 }]} />
      </AdminModalForm>
      <AdminModalForm<SkuUpdateRequest>
        key={editing?.id ?? 'sku'}
        title="修改 SKU 资料"
        open={Boolean(editing)}
        onOpenChange={(open) => {
          if (!open) setEditing(undefined);
        }}
        initialValues={
          editing
            ? {
                name: editing.name,
                price: editablePrice(editing.price),
                image: editing.image ?? undefined,
              }
            : undefined
        }
        modalProps={{ destroyOnHidden: true, maskClosable: false }}
        submitter={{ submitButtonProps: { loading: save.isPending } }}
        onFinish={async (values) => {
          if (save.isPending || !can('mall:sku:edit')) return false;
          try {
            requireWriteSuccess((await save.mutateAsync(values)).data);
            message.success('修改成功');
            setEditing(undefined);
            await refresh();
            return true;
          } catch (reason) {
            message.error(String(reason));
            return false;
          }
        }}
      >
        <ProFormText name="name" label="名称" rules={[{ required: true }, { max: 200 }]} />
        <ProFormDigit
          name="price"
          label="价格（元）"
          min={0.01}
          max={MAX_EDITABLE_PRICE}
          fieldProps={{ precision: 2 }}
          rules={[{ required: true }]}
        />
        <ProFormText name="image" label="图片 URL" rules={[{ max: 500 }]} />
      </AdminModalForm>
    </>
  );
}
