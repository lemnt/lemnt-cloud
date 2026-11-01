import { beforeEach, expect, it, vi } from 'vitest';
import * as api from './brokerageApi';

const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => mockedRequest.mockReset());

const user = {
  id: '1', bindUserId: '2', bindNickname: '推广员', bindAvatar: null, brokerageEnabled: true,
  brokeragePrice: '10.00', frozenPrice: '5.00', firstLevelCount: 1, secondLevelCount: 2,
};
const record = {
  id: '3', userId: '1', bizId: 'TR1', bizType: 1, title: '获得推广佣金', description: null,
  price: '10.00', totalPrice: '15.00', status: 0, frozenDays: 3,
  unfreezeTime: '2026-11-04 00:00:00', sourceUserLevel: 1, createTime: '2026-11-01 00:00:00',
};
const withdraw = {
  id: '4', userId: '1', price: '4.00', feePrice: '0.20', totalPrice: '10.00', type: 1,
  userName: '本人钱包', userAccount: '1', qrCodeUrl: null, bankName: null, bankAddress: null,
  status: 0, auditReason: null, auditTime: null, createTime: '2026-11-01 00:00:00',
};

it('分销用户、佣金与提现走管理端路径并严格解码', async () => {
  mockedRequest.mockResolvedValue({ rows: [user], total: 1 });
  expect((await api.getUsers({ pageNum: 1, pageSize: 20 })).records[0].brokeragePrice).toBe('10.00');
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/brokerage/users', params: { pageNum: 1, pageSize: 20 },
  });
  mockedRequest.mockResolvedValue({ rows: [record], total: 1 });
  expect((await api.getRecords({ pageNum: 1, pageSize: 20 })).records[0].bizType).toBe(1);
  mockedRequest.mockResolvedValue({ rows: [withdraw], total: 1 });
  expect((await api.getWithdraws({ pageNum: 1, pageSize: 20 })).records[0].status).toBe(0);
});

it('分销操作参数受控', async () => {
  await api.setEnabled('1', true);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/brokerage/users/1/enabled', method: 'PUT', data: { enabled: true },
  });
  await api.updateBind('1', ' 2 ');
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/brokerage/users/1/bind', method: 'PUT', data: { bindUserId: '2' },
  });
  await api.auditWithdraw('4', false, '驳回');
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/brokerage/withdraws/4/audit', method: 'POST', data: { approved: false, reason: '驳回' },
  });
  mockedRequest.mockResolvedValue({ rows: [{ ...user, brokeragePrice: 'abc' }], total: 1 });
  await expect(api.getUsers({ pageNum: 1, pageSize: 20 })).rejects.toThrow('金额');
  mockedRequest.mockResolvedValue({ data: 1 });
  expect(await api.unfreeze()).toBe(1);
  mockedRequest.mockResolvedValue({ data: 'x' });
  await expect(api.unfreeze()).rejects.toThrow('解冻');
});
