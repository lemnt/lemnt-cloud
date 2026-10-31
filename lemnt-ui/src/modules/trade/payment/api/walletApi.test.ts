import { beforeEach, expect, it, vi } from 'vitest';
import * as api from './walletApi';

const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => mockedRequest.mockReset());

const wallet = {
  id: '9223372036854775807',
  memberId: '1',
  balance: '12.00',
  freezePrice: '3.00',
  totalExpense: '5.00',
  totalRecharge: '10.00',
  createTime: '2026-10-31T12:00:00',
};
const tx = {
  transactionNo: 'WT1',
  bizType: 'RECHARGE',
  bizNo: 'R1',
  title: '充值',
  amount: '-2.00',
  balanceAfter: '12.00',
  createTime: null,
};

it('钱包分页保留大编号与精确余额', async () => {
  mockedRequest.mockResolvedValue({ rows: [wallet], total: 1 });
  const result = await api.getWallets({ memberId: '1', pageNum: 2, pageSize: 20 });
  expect(result.records[0]).toMatchObject({ id: wallet.id, balance: '12.00', freezePrice: '3.00' });
  expect(mockedRequest).toHaveBeenCalledWith({
    url: '/trade/admin/wallets',
    params: { memberId: '1', pageNum: 2, pageSize: 20 },
  });
  mockedRequest.mockResolvedValue({ rows: [{ ...wallet, balance: 12 }], total: 1 });
  await expect(api.getWallets({ pageNum: 1, pageSize: 20 })).rejects.toThrow('文本');
});

it('流水允许负金额但余额必须非负', async () => {
  mockedRequest.mockResolvedValue({ rows: [tx], total: 1 });
  const result = await api.getWalletTransactions('1', {
    bizType: 'RECHARGE',
    pageNum: 1,
    pageSize: 20,
  });
  expect(result.records[0]).toMatchObject({ amount: '-2.00', balanceAfter: '12.00' });
  mockedRequest.mockResolvedValue({ rows: [{ ...tx, balanceAfter: '-1.00' }], total: 1 });
  await expect(api.getWalletTransactions('1', { pageNum: 1, pageSize: 20 })).rejects.toThrow(
    '金额',
  );
});

it('调整余额只提交请求编号、金额与原因', async () => {
  mockedRequest.mockResolvedValue({ data: wallet });
  const result = await api.adjustWallet('1', {
    requestNo: ' A1 ',
    amount: '-2.5',
    reason: ' 扣减 ',
  });
  expect(result.balance).toBe('12.00');
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/wallets/1/adjust',
    method: 'POST',
    data: { requestNo: 'A1', amount: '-2.5', reason: '扣减' },
  });
  await expect(
    api.adjustWallet('1', { requestNo: 'A1', amount: '0', reason: 'x' }),
  ).rejects.toThrow('非零');
  await expect(
    api.adjustWallet('1', { requestNo: 'A1', amount: '1.005', reason: 'x' }),
  ).rejects.toThrow('两位小数');
  await expect(
    api.adjustWallet('1', { requestNo: '', amount: '1.00', reason: 'x' }),
  ).rejects.toThrow('请求编号');
});
