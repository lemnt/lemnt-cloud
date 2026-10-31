import { beforeEach, expect, it, vi } from 'vitest';
import * as api from './rechargeApi';

const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => mockedRequest.mockReset());

const pkg = {
  id: '9223372036854775807',
  name: '标准',
  payPrice: '100.00',
  bonusPrice: '20.00',
  status: 0,
  createTime: '2026-10-31T12:00:00',
};
const recharge = {
  id: '5',
  rechargeNo: 'RC1',
  requestNo: 'REQ1',
  walletId: '10',
  memberId: '1',
  packageId: '9223372036854775807',
  payPrice: '100.00',
  bonusPrice: '20.00',
  totalPrice: '120.00',
  status: 'PENDING',
  channelCode: null,
  paidTime: null,
  refundedTime: null,
  createTime: null,
};

it('套餐分页保留大编号与精确金额', async () => {
  mockedRequest.mockResolvedValue({ rows: [pkg], total: 1 });
  const result = await api.getRechargePackages({
    name: ' 标准 ',
    status: 0,
    pageNum: 2,
    pageSize: 20,
  });
  expect(result.records[0]).toMatchObject({ id: pkg.id, payPrice: '100.00', bonusPrice: '20.00' });
  expect(mockedRequest).toHaveBeenCalledWith({
    url: '/trade/admin/recharge-packages',
    params: { name: '标准', status: 0, pageNum: 2, pageSize: 20 },
  });
  mockedRequest.mockResolvedValue({ rows: [{ ...pkg, status: 2 }], total: 1 });
  await expect(api.getRechargePackages({ pageNum: 1, pageSize: 20 })).rejects.toThrow('状态');
});

it('保存套餐校验金额并分别使用创建/更新路径', async () => {
  mockedRequest.mockResolvedValue({ data: 9 });
  expect(
    await api.saveRechargePackage(null, {
      name: ' 标准 ',
      payPrice: '100',
      bonusPrice: '0',
      status: 0,
    }),
  ).toBe('9');
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/recharge-packages',
    method: 'POST',
    data: { name: '标准', payPrice: '100.00', bonusPrice: '0.00', status: 0 },
  });
  mockedRequest.mockResolvedValue({ data: true });
  expect(
    await api.saveRechargePackage('9', {
      name: '标准',
      payPrice: '50.5',
      bonusPrice: '0',
      status: 1,
    }),
  ).toBe(true);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/recharge-packages/9',
    method: 'PUT',
    data: { name: '标准', payPrice: '50.50', bonusPrice: '0.00', status: 1 },
  });
  await expect(
    api.saveRechargePackage(null, { name: 'x', payPrice: '0', bonusPrice: '0', status: 0 }),
  ).rejects.toThrow('大于0');
});

it('充值单分页与确认/退款严格解码', async () => {
  mockedRequest.mockResolvedValue({ rows: [recharge], total: 1 });
  const result = await api.getWalletRecharges({
    memberId: '1',
    status: 'PENDING',
    pageNum: 1,
    pageSize: 20,
  });
  expect(result.records[0]).toMatchObject({
    rechargeNo: 'RC1',
    totalPrice: '120.00',
    packageId: pkg.id,
  });
  mockedRequest.mockResolvedValue({ data: { ...recharge, status: 'PAID' } });
  expect((await api.confirmWalletRecharge('RC1', 'ALIPAY')).status).toBe('PAID');
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/wallet-recharges/RC1/confirm',
    method: 'POST',
    data: { channelCode: 'ALIPAY' },
  });
  mockedRequest.mockResolvedValue({ data: { ...recharge, status: 'REFUNDED' } });
  expect((await api.refundWalletRecharge('RC1')).status).toBe('REFUNDED');
  mockedRequest.mockResolvedValue({ data: { ...recharge, status: 'UNKNOWN' } });
  await expect(api.getWalletRecharge('RC1')).rejects.toThrow('充值状态');
});
