import { expect, it } from 'vitest';
import { decodeIntent, intentKey, persistIntent, restoreIntent } from './checkoutIntent';
const value = {
  memberId: '1',
  requestNo: 'REQ1',
  productIds: ['5'],
  quoteToken: 'a'.repeat(64),
  couponId: '8',
};
it('按后台操作者隔离并完整恢复同一次请求，不能生成新编号替代未知结果', () => {
  const values = new Map<string, string>();
  const storage = {
    getItem: (key: string) => values.get(key) ?? null,
    setItem: (key: string, item: string) => {
      values.set(key, item);
    },
  };
  persistIntent(storage, intentKey('10'), value);
  expect(restoreIntent(storage, intentKey('10')).intent).toEqual(value);
  expect(restoreIntent(storage, intentKey('11')).intent).toBeUndefined();
});
it('存储不可写时失败，不能假装已记录请求；损坏的记录不能被静默覆盖', () => {
  expect(() =>
    persistIntent(
      {
        setItem: () => {
          throw new Error('quota');
        },
      },
      'key',
      value,
    ),
  ).toThrow();
  expect(restoreIntent({ getItem: () => 'invalid-json' }, 'key').error).toBeTruthy();
  expect(() => decodeIntent({ ...value, productIds: ['5', '5'] })).toThrow();
  expect(() => decodeIntent({ ...value, quoteToken: 'invalid' })).toThrow();
});
