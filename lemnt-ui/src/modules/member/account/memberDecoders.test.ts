import { expect, it } from 'vitest';
import {
  decodePage,
  decodeProfile,
  memberId,
  decodeRecord,
  decodeAddress,
  decodeMetadata,
  decodeConfig,
} from './memberDecoders';
it('拒绝被JS舍入的ID和余额，保留字符串ID', () => {
  expect(memberId('9223372036854775807')).toBe('9223372036854775807');
  expect(() => memberId(Number.MAX_SAFE_INTEGER + 1)).toThrow();
  expect(() =>
    decodeProfile({
      id: 1,
      memberNo: 'M1',
      nickname: 'n',
      status: 1,
      points: 2 ** 53,
      experience: 0,
    }),
  ).toThrow('安全整数');
});
it('记录类型隔离与分页结构校验', () => {
  expect(() =>
    decodeRecord({ id: 1, memberId: 2, assetType: 'EXPERIENCE' }, 'point-records'),
  ).toThrow('类型不匹配');
  expect(() => decodePage({ rows: null, total: 1 }, decodeProfile)).toThrow();
  expect(() => decodePage({ rows: [], total: -1 }, decodeProfile)).toThrow();
});
it('地址默认值必须为boolean，配置不接受数字开关', () => {
  expect(() => decodeAddress({ defaultStatus: 'false' })).toThrow();
  expect(() => decodeConfig({ pointTradeDeductEnable: 1 })).toThrow();
});
it('等级模型保留支付百分比和经验门槛', () => {
  expect(
    decodeMetadata(
      {
        id: 1,
        name: '银卡',
        status: 1,
        level: 2,
        experience: 100,
        discountPercent: 95,
        icon: null,
        backgroundUrl: null,
      },
      'levels',
    ),
  ).toMatchObject({ id: '1', discountPercent: 95, experience: 100 });
});
