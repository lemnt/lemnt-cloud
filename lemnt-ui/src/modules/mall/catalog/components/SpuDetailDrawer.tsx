import { ProDescriptions, ProTable, type ProColumns } from '@ant-design/pro-components';
import { Alert, Button, Drawer, Empty } from 'antd';
import { useQuery } from '@tanstack/react-query';
import { usePermission } from '@/shared/hooks/usePermission';
import { getSpuDetail } from '../api/catalogApi';
import { catalogKeys } from '../hooks/catalogKeys';
import { shelfStatusEnum } from '../constants';
import ProductInteractionPanels from './ProductInteractionPanels';
import type { Sku } from '../types';

export function SpuDetailDrawer({ id, onClose }: { id?: string; onClose: () => void }) {
  const { can } = usePermission();
  const query = useQuery({
    queryKey: catalogKeys.spu(id ?? 'none'),
    queryFn: () => getSpuDetail(id!),
    enabled: Boolean(id) && can('mall:spu:query'),
  });
  const columns: ProColumns<Sku>[] = [
    { title: 'SKU 编码', dataIndex: 'code' },
    { title: '名称', dataIndex: 'name' },
    {
      title: '规格',
      renderText: (_, row) =>
        row.properties
          .map((property) => `${property.propertyName}：${property.valueName}`)
          .join(' / ') || '无规格',
    },
    { title: '价格（元）', dataIndex: 'price' },
    { title: '可用库存', dataIndex: 'stock' },
    { title: '重量（千克）', dataIndex: 'weight' },
    { title: '体积（立方米）', dataIndex: 'volume' },
    { title: '状态', dataIndex: 'status', valueEnum: shelfStatusEnum },
  ];
  return (
    <Drawer title="商品与 SKU 详情" open={Boolean(id)} onClose={onClose} width={1000}>
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
              { title: '运费模板', dataIndex: 'deliveryTemplateId' },
              { title: '状态', dataIndex: 'status', valueEnum: shelfStatusEnum },
            ]}
          />
          <ProductInteractionPanels spuId={query.data.id} />
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
  );
}
