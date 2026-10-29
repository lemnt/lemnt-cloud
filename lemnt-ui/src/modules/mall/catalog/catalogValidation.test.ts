import { expect, it } from 'vitest';
import { requireWriteSuccess } from './catalogValidation';
it('后端返回 false 时不得宣告写入成功', () => {
  expect(() => requireWriteSuccess(false)).toThrow();
  expect(() => requireWriteSuccess(true)).not.toThrow();
});

it('规格选择保持长编号并拒绝重复或未选完的项', async () => {
  const { selectedProperties } = await import('./catalogValidation');
  expect(
    selectedProperties([{ propertyId: '9223372036854775807', valueId: '9223372036854775806' }]),
  ).toEqual([{ propertyId: '9223372036854775807', valueId: '9223372036854775806' }]);
  expect(selectedProperties([])).toEqual([]);
  expect(() => selectedProperties([{ propertyId: '1' }])).toThrow();
  expect(() =>
    selectedProperties([
      { propertyId: '1', valueId: '2' },
      { propertyId: '1', valueId: '3' },
    ]),
  ).toThrow();
});
