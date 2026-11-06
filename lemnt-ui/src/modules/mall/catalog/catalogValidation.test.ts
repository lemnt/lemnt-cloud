import { expect, it } from 'vitest';
import { requireWriteSuccess } from './catalogValidation';
it('后端返回 false 时不得宣告写入成功', () => {
  expect(() => requireWriteSuccess(false)).toThrow();
  expect(() => requireWriteSuccess(true)).not.toThrow();
});

it('规格选择保持长编号并拒绝重复或未选完的项', async () => {
  const { selectedSpecs } = await import('./catalogValidation');
  expect(
    selectedSpecs([{ specId: '9223372036854775807', specValueId: '9223372036854775806' }]),
  ).toEqual([{ specId: '9223372036854775807', specValueId: '9223372036854775806' }]);
  expect(selectedSpecs([])).toEqual([]);
  expect(() => selectedSpecs([{ specId: '1' }])).toThrow();
  expect(() =>
    selectedSpecs([
      { specId: '1', specValueId: '2' },
      { specId: '1', specValueId: '3' },
    ]),
  ).toThrow();
});
