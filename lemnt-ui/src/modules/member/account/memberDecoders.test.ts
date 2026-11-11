import { decodeSignConfig } from './memberDecoders';
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

it('等级流水区分会员经验和源记录经验，未知配置不按当前等级补造', () => {
  const row = {
    id: '1',
    memberId: '9',
    levelId: '2',
    experience: 150,
    reason: '原因',
    createTime: '2026-10-30 11:00:00',
  };
  const old = decodeRecord(row, 'level-records');
  expect(old.experience).toBe(150);
  expect(old.recordExperience).toBeUndefined();
  const value = decodeRecord(
    {
      ...row,
      level: 1,
      discountPercent: 90,
      recordExperience: 100,
      description: '管理员调整为：Gold',
    },
    'level-records',
  );
  expect(value.experience).toBe(150);
  expect(value.recordExperience).toBe(100);
  expect(value.discountPercent).toBe(90);
});

it('签到真实时间仅在已知时展示，不由业务日期补造', () => {
  const row = { id: '1', memberId: '9', signDate: '2026-10-29', day: 1, point: 5, experience: 2 };
  expect(decodeRecord({ ...row, createTime: null }, 'signin-records').createTime).toBeUndefined();
  expect(
    decodeRecord({ ...row, createTime: '2026-10-29 10:00:00' }, 'signin-records').createTime,
  ).toBe('2026-10-29 10:00:00');
});

it('规则列表必须有真实ID，不能把天数当ID补齐', () => {
  const row = { day: 7, point: 10, experience: 2, status: 1 };
  expect(() => decodeSignConfig(row)).toThrow('标识');
  expect(decodeSignConfig({ ...row, id: '99' })).toMatchObject({ id: '99', day: 7 });
});

it('分组标签历史创建时间不补造，真实时间单独解析', () => {
  const group = { id: '1', name: 'VIP', remark: null, status: 0 };
  expect(decodeMetadata({ ...group, createTime: null }, 'groups').createTime).toBeUndefined();
  expect(
    decodeMetadata(
      { id: '2', name: 'active', remark: null, createTime: '2026-10-30 10:00:00' },
      'tags',
    ).createTime,
  ).toBe('2026-10-30 10:00:00');
});

it('会员关系返显保留真实编号和名称，不从ID推名称', () => {
  const row = {
    id: '1',
    nickname: 'member',
    status: 1,
    points: 0,
    experience: 0,
    levelId: '7',
    groupId: '8',
    tagIds: ['9007199254740993'],
    tagNames: ['Active'],
    levelName: 'Gold',
    groupName: 'VIP',
  };
  expect(decodeProfile(row)).toMatchObject({
    tagIds: ['9007199254740993'],
    tagNames: ['Active'],
    levelName: 'Gold',
    groupName: 'VIP',
  });
  expect(decodeProfile({ ...row, levelName: null }).levelName).toBeNull();
});

it('会员未知关系与已读取无绑定分开，不默认造空标签', () => {
  const row = { id: '1', nickname: 'member', status: 1, points: 0, experience: 0 };
  expect(decodeProfile(row).tagIds).toBeUndefined();
  expect(decodeProfile(row).tagNames).toBeUndefined();
  expect(
    decodeProfile({ ...row, tagIds: [], tagNames: [], levelName: null, groupName: null }),
  ).toMatchObject({ tagIds: [], tagNames: [], levelName: null, groupName: null });
});

it('地址地区全称来自服务端，兼容缺失和null，未知编号不转为名称', () => {
  const row = {
    id: '1',
    memberId: '2',
    name: '收件人',
    mobile: '13800000003',
    areaId: '110101',
    detailAddress: '街道门牌',
    defaultStatus: true,
  };
  expect(decodeAddress({ ...row, areaName: '北京市 北京市 东城区' }).areaName).toBe(
    '北京市 北京市 东城区',
  );
  expect(decodeAddress(row)).not.toHaveProperty('areaName');
  expect(decodeAddress({ ...row, areaId: '9223372036854775807', areaName: null })).toMatchObject({
    areaId: '9223372036854775807',
    areaName: null,
  });
  expect(() => decodeAddress({ ...row, areaName: 110101 })).toThrow();
});
