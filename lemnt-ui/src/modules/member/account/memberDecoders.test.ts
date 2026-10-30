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

it('后台内部备注保持原文与空值，不把未知备注补为空串', () => {
  const row = { id: '9', nickname: '会员', status: 1, points: 0, experience: 0 };
  expect(decodeProfile({ ...row, mark: '内部资料' }).mark).toBe('内部资料');
  expect(decodeProfile({ ...row, mark: '' }).mark).toBe('');
  expect(decodeProfile(row).mark).toBeUndefined();
});

it('真实姓名及地区不补造，拒绝无效地区编号', () => {
  const row = { id: '9', nickname: '会员', status: 1, points: 0, experience: 0 };
  expect(decodeProfile({ ...row, name: '姓名', areaId: 110101 })).toMatchObject({
    name: '姓名',
    areaId: 110101,
  });
  expect(decodeProfile(row).name).toBeUndefined();
  expect(() => decodeProfile({ ...row, areaId: 0 })).toThrow('地区');
  expect(() => decodeProfile({ ...row, areaId: 2147483648 })).toThrow('地区');
});

it('成功登录时间只读展示，历史未知不使用创建时间补造', () => {
  const row = {
    id: '9',
    nickname: '会员',
    status: 1,
    points: 0,
    experience: 0,
    createTime: '2020-01-01 00:00:00',
  };
  expect(decodeProfile(row).loginDate).toBeUndefined();
  expect(decodeProfile({ ...row, loginDate: '2026-10-29 12:00:00' }).loginDate).toBe(
    '2026-10-29 12:00:00',
  );
});

it('连接IP只展示服务器值，不用固定地址补齐未知历史', () => {
  const row = { id: '9', nickname: '会员', status: 1, points: 0, experience: 0 };
  expect(decodeProfile(row).loginIp).toBeUndefined();
  expect(decodeProfile({ ...row, loginIp: '203.0.113.7' }).loginIp).toBe('203.0.113.7');
});

it('积分标题描述独立于原因，历史未知不补造，经验不借用积分元数据', () => {
  const row = {
    id: '1',
    memberId: '9',
    assetType: 'POINT',
    delta: 10,
    balance: 10,
    bizType: 'ADMIN',
    bizNo: 'M1',
    reason: '人工原因',
    createTime: '2026-10-30 06:00:00',
  };
  const value = decodeRecord(
    { ...row, pointBizType: 2, title: '管理员修改', description: '管理员修改 10 积分' },
    'point-records',
  );
  expect(value.title).toBe('管理员修改');
  expect(value.reason).toBe('人工原因');
  expect(value.pointBizType).toBe(2);
  expect(decodeRecord(row, 'point-records').title).toBeUndefined();
  expect(
    decodeRecord(
      { ...row, assetType: 'EXPERIENCE', pointBizType: 2, title: '不得借用' },
      'experience-records',
    ).title,
  ).toBeUndefined();
});

it('经验编号0保持有效，标题来自经验快照且不借积分类型', () => {
  const row = {
    id: '1',
    memberId: '9',
    assetType: 'EXPERIENCE',
    delta: 5,
    balance: 5,
    bizType: 'ADMIN',
    bizNo: 'E1',
    reason: '原因',
    createTime: '2026-10-30 09:00:00',
    experienceBizType: 0,
    title: '管理员调整',
    description: '管理员调整获得 5 经验',
  };
  const value = decodeRecord(row, 'experience-records');
  expect(value.experienceBizType).toBe(0);
  expect(value.title).toBe('管理员调整');
  expect(value.pointBizType).toBeUndefined();
});
