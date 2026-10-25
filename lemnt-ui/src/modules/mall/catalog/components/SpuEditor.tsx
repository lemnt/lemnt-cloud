import {
  ProFormText,
  ProFormTextArea,
  ProFormSelect,
  ProFormList,
  ProFormDigit,
} from '@ant-design/pro-components';
import { Alert, App } from 'antd';
import { useMutation, useQuery } from '@tanstack/react-query';
import { AdminModalForm } from '@/shared/components/AdminModalForm';
import { usePermission } from '@/shared/hooks/usePermission';
import { createSpu, updateSpu, getCategoryList, getBrandList } from '../api/catalogApi';
import { catalogKeys } from '../hooks/catalogKeys';
import { parseSkuProperties, requireWriteSuccess } from '../catalogValidation';
import { MAX_STOCK, MAX_EDITABLE_PRICE } from '../constants';
import type { Spu } from '../types';

interface SkuFormValues {
  code: string;
  name: string;
  price: number;
  stock: number;
  image?: string;
  propertiesJson?: string;
}
interface SpuFormValues {
  code: string;
  name: string;
  categoryId: string;
  brandId: string;
  description?: string;
  image?: string;
  skus: SkuFormValues[];
}
interface SpuEditorProps {
  value?: Spu;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSuccess: () => Promise<void>;
}
export function SpuEditor({ value, open, onOpenChange, onSuccess }: SpuEditorProps) {
  const { can } = usePermission();
  const { message } = App.useApp();
  const categories = useQuery({
    queryKey: catalogKeys.categories(),
    queryFn: getCategoryList,
    enabled: open && can('mall:category:list'),
  });
  const brands = useQuery({
    queryKey: catalogKeys.brands(),
    queryFn: getBrandList,
    enabled: open && can('mall:brand:list'),
  });
  const save = useMutation<unknown, Error, SpuFormValues>({
    mutationFn: async (values) => {
      if (value) {
        requireWriteSuccess(
          (
            await updateSpu(value.id, {
              name: values.name,
              categoryId: values.categoryId,
              brandId: values.brandId,
              description: values.description,
              image: values.image,
            })
          ).data,
        );
      } else {
        await createSpu({
          ...values,
          skus: values.skus.map(({ propertiesJson, ...sku }) => ({
            ...sku,
            properties: parseSkuProperties(propertiesJson),
          })),
        });
      }
    },
  });
  const canSave =
    can(value ? 'mall:spu:edit' : 'mall:spu:add') &&
    can('mall:category:list') &&
    can('mall:brand:list');
  const referenceError = categories.error ?? brands.error;
  return (
    <AdminModalForm<SpuFormValues>
      title={value ? '修改商品资料' : '创建 SPU 与 SKU'}
      open={open}
      onOpenChange={onOpenChange}
      width={800}
      initialValues={
        value
          ? {
              ...value,
              description: value.description ?? undefined,
              image: value.image ?? undefined,
            }
          : { skus: [{ stock: 0, propertiesJson: '{}' }] }
      }
      modalProps={{ destroyOnHidden: true, maskClosable: false }}
      submitter={{
        submitButtonProps: {
          disabled:
            !canSave || categories.isFetching || brands.isFetching || Boolean(referenceError),
          loading: save.isPending,
        },
      }}
      onFinish={async (values) => {
        if (save.isPending || !canSave) return false;
        try {
          await save.mutateAsync(values);
          message.success('保存成功');
          onOpenChange(false);
          await onSuccess();
          return true;
        } catch (reason) {
          message.error(String(reason));
          return false;
        }
      }}
    >
      {!canSave && <Alert type="warning" title="需要商品写入权限及分类、品牌列表权限" />}
      {referenceError && (
        <Alert type="error" title="分类或品牌加载失败" description={String(referenceError)} />
      )}
      <ProFormText
        name="code"
        label="SPU 编码"
        disabled={Boolean(value)}
        rules={[{ required: true }, { max: 64 }]}
      />
      <ProFormText name="name" label="商品名称" rules={[{ required: true }, { max: 200 }]} />
      <ProFormSelect
        name="categoryId"
        label="分类"
        rules={[{ required: true }]}
        options={categories.data
          ?.filter(
            (row) =>
              row.status === 'ENABLED' &&
              (row.parentId === null ||
                categories.data?.some(
                  (parent) => parent.id === row.parentId && parent.status === 'ENABLED',
                )),
          )
          .map((row) => ({ label: row.name, value: row.id }))}
      />
      <ProFormSelect
        name="brandId"
        label="品牌"
        rules={[{ required: true }]}
        options={brands.data
          ?.filter((row) => row.status === 'ENABLED')
          .map((row) => ({ label: row.name, value: row.id }))}
      />
      <ProFormText name="image" label="主图 URL" rules={[{ max: 500 }]} />
      <ProFormTextArea name="description" label="商品描述" rules={[{ max: 65535 }]} />
      {!value && (
        <ProFormList
          name="skus"
          label="SKU（规格名称须一致，每个组合唯一）"
          min={1}
          max={100}
          copyIconProps={false}
          creatorButtonProps={{ creatorButtonText: '新增 SKU' }}
        >
          <ProFormText name="code" label="SKU 编码" rules={[{ required: true }, { max: 64 }]} />
          <ProFormText name="name" label="SKU 名称" rules={[{ required: true }, { max: 200 }]} />
          <ProFormDigit
            name="price"
            label="销售价（元）"
            min={0.01}
            max={MAX_EDITABLE_PRICE}
            fieldProps={{ precision: 2 }}
            rules={[{ required: true }]}
          />
          <ProFormDigit
            name="stock"
            label="初始可用库存"
            min={0}
            max={MAX_STOCK}
            fieldProps={{ precision: 0 }}
            rules={[{ required: true }]}
          />
          <ProFormText name="image" label="SKU 图片 URL" rules={[{ max: 500 }]} />
          <ProFormTextArea
            name="propertiesJson"
            label="规格 JSON（无规格填 {}）"
            placeholder={'{"颜色":"红色","尺寸":"M"}'}
            rules={[
              {
                validator: async (_, text: string | undefined) => {
                  parseSkuProperties(text);
                },
              },
            ]}
          />
        </ProFormList>
      )}
    </AdminModalForm>
  );
}
