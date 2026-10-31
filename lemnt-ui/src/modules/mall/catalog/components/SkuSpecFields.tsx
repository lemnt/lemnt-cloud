import { ProFormGroup, ProFormList, ProFormSelect } from '@ant-design/pro-components';
import { App, Button, Form } from 'antd';
import { useState } from 'react';
import {
  getSpecOptions,
  getSpecValueOptions,
  getSpecOption,
  getSpecValueOption,
} from '../api/catalogApi';

/** 每个SKU选取已有字典编号，不允许在商品编辑时临时创建同名规格。 */
export function SkuSpecFields({ skuIndex }: { skuIndex: number }) {
  const [revision, setRevision] = useState(0);
  return (
    <>
      <ProFormList
        name="specSelections"
        label="商品规格（无规格时不添加）"
        max={10}
        copyIconProps={false}
        creatorButtonProps={{ creatorButtonText: '添加规格' }}
      >
        {(field) => <SpecChoice skuIndex={skuIndex} specIndex={field.name} revision={revision} />}
      </ProFormList>
      <Button onClick={() => setRevision((value) => value + 1)}>刷新规格字典</Button>
    </>
  );
}
function SpecChoice({
  skuIndex,
  specIndex,
  revision,
}: {
  skuIndex: number;
  specIndex: number;
  revision: number;
}) {
  const form = Form.useFormInstance();
  const { message } = App.useApp();
  const path = ['skus', skuIndex, 'specSelections', specIndex];
  const propertyId = Form.useWatch([...path, 'propertyId'], form) as string | undefined;
  const valueId = Form.useWatch([...path, 'valueId'], form) as string | undefined;
  async function options(kind: 'spec' | 'value', keyword?: string) {
    try {
      const rows =
        kind === 'spec'
          ? await getSpecOptions(keyword)
          : propertyId
            ? await getSpecValueOptions(propertyId, keyword)
            : [];
      const selected = kind === 'spec' ? propertyId : valueId;
      if (selected && !rows.some((row) => row.id === selected)) {
        // 已保存名称是编辑快照；字典值删除后仍能展示它并选择替代值，不伪造当前字典状态。
        const savedName = form.getFieldValue([
          ...path,
          kind === 'spec' ? 'propertyName' : 'valueName',
        ]);
        rows.unshift(
          typeof savedName === 'string' && savedName
            ? { id: selected, name: savedName }
            : kind === 'spec'
              ? await getSpecOption(selected)
              : await getSpecValueOption(selected),
        );
      }
      return rows.map((row) => ({ label: row.name, value: row.id }));
    } catch (error) {
      message.error(`规格读取失败：${String(error)}`);
      throw error;
    }
  }
  return (
    <ProFormGroup>
      <ProFormSelect
        name="propertyId"
        label="规格"
        showSearch
        debounceTime={300}
        params={{ selected: propertyId, revision }}
        request={({ keyWords }) => options('spec', keyWords)}
        fieldProps={{
          filterOption: false,
          onChange: () => {
            form.setFieldValue([...path, 'valueId'], undefined);
            form.setFieldValue([...path, 'propertyName'], undefined);
            form.setFieldValue([...path, 'valueName'], undefined);
          },
        }}
        rules={[{ required: true }]}
      />
      <ProFormSelect
        name="valueId"
        label="规格值"
        showSearch
        disabled={!propertyId}
        debounceTime={300}
        params={{ propertyId, selected: valueId, revision }}
        request={({ keyWords }) => options('value', keyWords)}
        fieldProps={{ filterOption: false }}
        rules={[{ required: true }]}
      />
    </ProFormGroup>
  );
}
