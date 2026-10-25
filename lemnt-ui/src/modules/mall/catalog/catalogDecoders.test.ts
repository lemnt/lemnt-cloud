import { expect, it } from 'vitest';
import { decodeCategory, decodeId, decodeList, decodeSku } from './catalogDecoders';
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
