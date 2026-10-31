import { ProTable } from '@ant-design/pro-components';
import { usePermission } from '@/shared/hooks/usePermission';
import { getProductFavorites, getProductBrowseHistory } from '../api/catalogApi';
import { decodeId } from '../catalogDecoders';
import type { ProductFavoriteRecord, ProductBrowseHistoryRecord } from '../types';

/** 仅挂载已有商品详情，不增加后台主菜单。 */
export default function ProductInteractionPanels({ spuId }: { spuId: string }) {
  const { can } = usePermission();
  return (
    <>
      {can('mall:product-favorite:list') && (
        <ProTable<ProductFavoriteRecord>
          headerTitle="商品收藏记录"
          rowKey="id"
          params={{ spuId }}
          options={false}
          pagination={{ defaultPageSize: 20 }}
          request={(params) =>
            getProductFavorites({
              spuId,
              userId:
                params.userId == null || params.userId === '' ? undefined : decodeId(params.userId),
              pageNum: params.current ?? 1,
              pageSize: params.pageSize ?? 20,
            })
          }
          columns={[
            { title: '会员编号', dataIndex: 'userId' },
            { title: '收藏时间', dataIndex: 'createTime', search: false },
          ]}
        />
      )}
      {can('mall:product-history:list') && (
        <ProTable<ProductBrowseHistoryRecord>
          headerTitle="商品浏览足迹"
          rowKey="id"
          params={{ spuId }}
          options={false}
          pagination={{ defaultPageSize: 20 }}
          request={(params) =>
            getProductBrowseHistory({
              spuId,
              userId:
                params.userId == null || params.userId === '' ? undefined : decodeId(params.userId),
              userDeleted:
                params.userDeleted === true || params.userDeleted === 'true'
                  ? true
                  : params.userDeleted === false || params.userDeleted === 'false'
                    ? false
                    : undefined,
              pageNum: params.current ?? 1,
              pageSize: params.pageSize ?? 20,
            })
          }
          columns={[
            { title: '会员编号', dataIndex: 'userId' },
            {
              title: '是否隐藏',
              dataIndex: 'userDeleted',
              valueEnum: { true: { text: '已隐藏' }, false: { text: '可见' } },
              renderText: (value) => (value ? '已隐藏' : '可见'),
            },
            { title: '最近浏览时间', dataIndex: 'createTime', search: false },
          ]}
        />
      )}
    </>
  );
}
