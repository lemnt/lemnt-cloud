import { expect, it } from 'vitest';
import { parseSkuProperties, requireWriteSuccess } from './catalogValidation';
it('规范化规格文本并拒绝非对象或非字符串值', () => {
  expect(parseSkuProperties(' {" 颜色 ":" 红色 "} ')).toEqual({ 颜色: '红色' });
  expect(parseSkuProperties('')).toEqual({});
  for (const invalid of ['[]', 'null', '1', '{"颜色":1}', '{" ":"红"}', '{"a":"1"," a ":"2"}']) {
    expect(() => parseSkuProperties(invalid)).toThrow();
  }
});
it('后端返回 false 时不得宣告写入成功', () => {
  expect(() => requireWriteSuccess(false)).toThrow();
  expect(() => requireWriteSuccess(true)).not.toThrow();
});
