import { expect, it } from 'vitest';
import { requireWriteSuccess } from './catalogValidation';
it('后端返回 false 时不得宣告写入成功', () => {
  expect(() => requireWriteSuccess(false)).toThrow();
  expect(() => requireWriteSuccess(true)).not.toThrow();
});

it('规格选择保持长编号并拒绝重复或未选完的项', async () => {
  const { selectedSpecIds } = await import('./catalogValidation');
  expect(
    selectedSpecIds([{ propertyId: '9223372036854775807', valueId: '9223372036854775806' }]),
  ).toEqual({ '9223372036854775807': '9223372036854775806' });
  expect(selectedSpecIds([])).toEqual({});
  expect(() => selectedSpecIds([{ propertyId: '1' }])).toThrow();
  expect(() =>
    selectedSpecIds([
      { propertyId: '1', valueId: '2' },
      { propertyId: '1', valueId: '3' },
    ]),
  ).toThrow();
});
