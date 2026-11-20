import { beforeEach, expect, it, vi } from 'vitest';
import * as api from './payConfigApi';

const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => mockedRequest.mockReset());

const payApp = {
  id: '9223372036854775807',
  appKey: 'app_key',
  name: '商城',
  status: 0,
  remark: null,
  orderNotifyUrl: 'https://example.test/pay',
  refundNotifyUrl: null,
  createTime: '2026-10-31T12:00:00',
};
const channel = {
  id: '5',
  appId: '1',
  code: 'ALIPAY',
  status: 0,
  feeRate: '1.50',
  remark: null,
  configConfigured: true,
  createTime: null,
};

it('支付应用分页保留大编号，只提交本接口支持的筛选', async () => {
  mockedRequest.mockResolvedValue({ rows: [payApp], total: 1 });
  const result = await api.getPayApps({
    appKey: ' app_key ',
    name: '商城',
    status: 0,
    pageNum: 2,
    pageSize: 20,
    ...{ ignored: 'x' },
  });
  expect(result.records[0]).toMatchObject({ id: payApp.id, appKey: 'app_key', status: 0 });
  expect(mockedRequest).toHaveBeenCalledWith({
    url: '/trade/admin/pay-apps',
    params: { appKey: 'app_key', name: '商城', status: 0, pageNum: 2, pageSize: 20 },
  });
  mockedRequest.mockResolvedValue({
    rows: [{ ...payApp, id: Number.MAX_SAFE_INTEGER + 1 }],
    total: 1,
  });
  await expect(api.getPayApps({ pageNum: 1, pageSize: 20 })).rejects.toThrow('精度');
  mockedRequest.mockResolvedValue({ rows: [{ ...payApp, status: 2 }], total: 1 });
  await expect(api.getPayApps({ pageNum: 1, pageSize: 20 })).rejects.toThrow('状态');
});

it('渠道配置不返回明文，只解码是否已配置', async () => {
  mockedRequest.mockResolvedValue({ rows: [channel], total: 1 });
  const result = await api.getPayChannels({
    appId: '1',
    code: 'ALI',
    status: 0,
    pageNum: 1,
    pageSize: 20,
  });
  expect(result.records[0]).toMatchObject({
    appId: '1',
    code: 'ALIPAY',
    configConfigured: true,
    feeRate: '1.50',
  });
  mockedRequest.mockResolvedValue({ rows: [{ ...channel, configConfigured: 'yes' }], total: 1 });
  await expect(api.getPayChannels({ pageNum: 1, pageSize: 20 })).rejects.toThrow('渠道配置状态');
});

it('新增应用发送应用标识，修改不重复发送', async () => {
  mockedRequest.mockResolvedValue({ data: 9 });
  expect(
    await api.savePayApp(null, {
      appKey: ' app_key ',
      name: ' 商城 ',
      status: 0,
      remark: ' ',
      orderNotifyUrl: 'https://example.test/pay',
      refundNotifyUrl: '',
    }),
  ).toBe('9');
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/pay-apps',
    method: 'POST',
    data: {
      appKey: 'app_key',
      name: '商城',
      status: 0,
      remark: null,
      orderNotifyUrl: 'https://example.test/pay',
      refundNotifyUrl: null,
    },
  });
  mockedRequest.mockResolvedValue({ data: true });
  expect(await api.savePayApp('9', { name: '商城2', status: 1 })).toBe(true);
  const last = mockedRequest.mock.calls.at(-1)?.[0] as { data: Record<string, unknown> };
  expect(last.data).not.toHaveProperty('appKey');
  expect(last.data.status).toBe(1);
});

it('渠道配置新增需应用与编码，编辑留空保留原配置且不发送不可变字段', async () => {
  mockedRequest.mockResolvedValue({ data: 5 });
  expect(
    await api.savePayChannel(null, {
      appId: '1',
      code: 'WECHAT',
      status: 0,
      feeRate: '1.5',
      remark: ' ',
      config: '{"merchant":"m"}',
    }),
  ).toBe('5');
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/pay-channels',
    method: 'POST',
    data: {
      appId: '1',
      code: 'WECHAT',
      status: 0,
      feeRate: '1.50',
      remark: null,
      config: '{"merchant":"m"}',
    },
  });
  mockedRequest.mockResolvedValue({ data: true });
  await api.savePayChannel('5', { status: 1, feeRate: 2.25, remark: null, config: '  ' });
  const last = mockedRequest.mock.calls.at(-1)?.[0] as { data: Record<string, unknown> };
  expect(last.data).toEqual({ status: 1, feeRate: '2.25', remark: null, config: null });
  expect(last.data).not.toHaveProperty('appId');
  expect(last.data).not.toHaveProperty('code');
});

it('状态、删除要求服务端明确成功', async () => {
  mockedRequest.mockResolvedValue({ data: true });
  await api.updatePayAppStatus('9', 1);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/pay-apps/9/status',
    method: 'PUT',
    data: { status: 1 },
  });
  await api.deletePayChannel('5');
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/pay-channels/5',
    method: 'DELETE',
  });
  mockedRequest.mockResolvedValue({ data: false });
  await expect(api.deletePayApp('9')).rejects.toThrow('未确认');
});
