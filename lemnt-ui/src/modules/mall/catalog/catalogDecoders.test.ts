import { expect, it } from 'vitest';
import { decodeCategory, decodeId, decodeList, decodeSku, decodeSpu } from './catalogDecoders';
import { editablePrice } from './catalogValidation';
it('兼容服务端的长整型序列化，并拒绝已经丢失精度的数值编号', () => {
  expect(decodeId(42)).toBe('42');
  expect(decodeId('9223372036854775807')).toBe('9223372036854775807');
  expect(() => decodeId(9007199254740992)).toThrow();
  expect(decodeCategory({ id: 1, name: 'root', sort: 0, status: 'ENABLED' }).parentId).toBeNull();
});
it('保留服务端 BigDecimal 字符串，不静默转成浮点金额', () => {
  const row = {
    id: 1,
    spuId: 2,
    code: 'S1',
    name: 'sku',
    price: '99999999999999999.99',
    stock: 0,
    properties: {},
    status: 'ON_SHELF',
  };
  expect(decodeSku(row).price).toBe(row.price);
  expect(() => editablePrice(row.price)).toThrow();
  expect(() => decodeSku({ ...row, price: 9.25 })).toThrow();
  expect(editablePrice('9.25')).toBe(9.25);
});
it('缺少列表字段时明确失败，不能伪装成空列表', () => {
  expect(() => decodeList(undefined, decodeCategory)).toThrow();
});

it('回收站只用于 SPU，SKU 和已删除商品不能作为正常商品返回', () => {
  const spu = {
    id: 1,
    code: 'P1',
    name: '商品',
    categoryId: 1,
    brandId: 1,
    status: 'RECYCLED',
    skus: [],
  };
  expect(decodeSpu(spu).status).toBe('RECYCLED');
  expect(() => decodeSpu({ ...spu, status: 'DELETED' })).toThrow();
  const sku = {
    id: 1,
    code: 'S1',
    name: '规格',
    price: '1.00',
    stock: 0,
    properties: {},
    status: 'RECYCLED',
  };
  expect(() => decodeSku(sku)).toThrow();
});

it('配送模板及重量体积保留精确字符串，拒绝未知配送方式', () => {
  const sku = {
    id: 1,
    spuId: 2,
    code: 'SKU',
    name: '规格',
    price: '10.00',
    stock: 2,
    status: 'ON_SHELF',
    properties: {},
    weight: '1.250000',
    volume: '0.125000',
  };
  const product = {
    id: 2,
    code: 'SPU',
    name: '商品',
    categoryId: 1,
    brandId: 1,
    status: 'ON_SHELF',
    skus: [sku],
    deliveryTypes: [1],
    deliveryTemplateId: '99',
  };
  const value = decodeSpu(product);
  expect(value.deliveryTemplateId).toBe('99');
  expect(value.skus[0].weight).toBe('1.250000');
  expect(value.skus[0].volume).toBe('0.125000');
  expect(() => decodeSpu({ ...product, deliveryTypes: [3] })).toThrow();
  expect(() => decodeSku({ ...sku, weight: 1.25 })).toThrow();
});

it('SKU规格列表保留编号和名称，旧名值只能还原缺号规格', () => {
  const row = {
    id: '1',
    code: 'A',
    name: '商品',
    price: '10.00',
    stock: 1,
    properties: [
      {
        propertyId: '9223372036854775807',
        propertyName: '颜色',
        valueId: '9223372036854775806',
        valueName: '红色',
      },
    ],
    status: 'ON_SHELF',
  };
  expect(decodeSku(row).properties).toEqual(row.properties);
  expect(decodeSku({ ...row, properties: [] }).properties).toEqual([]);
  expect(decodeSku({ ...row, properties: { 颜色: '红色' } }).properties).toEqual([
    { propertyId: null, propertyName: '颜色', valueId: null, valueName: '红色' },
  ]);
  expect(() =>
    decodeSku({ ...row, properties: [{ ...row.properties[0], valueId: 9007199254740992 }] }),
  ).toThrow();
  expect(() =>
    decodeSku({ ...row, properties: [{ ...row.properties[0], valueId: null }] }),
  ).toThrow();
});
