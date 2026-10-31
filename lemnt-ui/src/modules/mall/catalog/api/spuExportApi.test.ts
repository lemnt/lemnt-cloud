import { beforeEach, expect, it, vi } from 'vitest';
import { exportSpus } from './spuExportApi';
const { get, unauthorized } = vi.hoisted(() => ({ get: vi.fn(), unauthorized: vi.fn() }));
vi.mock('@/api/interceptor', () => ({ httpClient: { get }, notifyUnauthorized: unauthorized }));
vi.mock('@/shared/auth/token', () => ({ getToken: () => 'test-session' }));
beforeEach(() => {
  get.mockReset();
  unauthorized.mockReset();
});
const mime = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
it('导出使用当前筛选和库存分组，不将当前页参数传给导出接口', async () => {
  const blob = new Blob(['xlsx'], { type: mime });
  get.mockResolvedValueOnce({ data: blob, headers: { 'content-type': mime } });
  expect(
    await exportSpus({
      name: ' 商品 ',
      categoryId: '9223372036854775807',
      brandId: '2',
      stockStatus: 'LOW_STOCK',
      ...{ pageNum: 99, pageSize: 1 },
    }),
  ).toBe(blob);
  expect(get).toHaveBeenCalledWith('/mall/spus/export', {
    params: {
      name: '商品',
      categoryId: '9223372036854775807',
      brandId: '2',
      status: undefined,
      stockStatus: 'LOW_STOCK',
    },
    responseType: 'blob',
    timeout: 120000,
  });
});
it('JSON业务错误、ProblemDetail和过期响应不能伪装成Excel文件', async () => {
  get.mockResolvedValueOnce({
    data: new Blob([JSON.stringify({ code: 401, msg: '登录过期' })], { type: 'application/json' }),
    headers: { 'content-type': 'application/json' },
  });
  await expect(exportSpus({})).rejects.toThrow('登录过期');
  expect(unauthorized).toHaveBeenCalledWith('test-session');
  get.mockRejectedValueOnce({
    isAxiosError: true,
    response: {
      data: new Blob([JSON.stringify({ status: 400, detail: '最多导出10000件商品' })], {
        type: 'application/problem+json',
      }),
    },
  });
  await expect(exportSpus({})).rejects.toThrow('10000');
  get.mockResolvedValueOnce({
    data: new Blob(['<html>错误</html>']),
    headers: { 'content-type': 'text/html' },
  });
  await expect(exportSpus({})).rejects.toThrow('未返回Excel');
});
it('空文件与越界筛选拒绝下载', async () => {
  get.mockResolvedValueOnce({
    data: new Blob([], { type: mime }),
    headers: { 'content-type': mime },
  });
  await expect(exportSpus({})).rejects.toThrow('为空');
  get.mockClear();
  await expect(exportSpus({ brandId: '9223372036854775808' })).rejects.toThrow('范围');
  expect(get).not.toHaveBeenCalled();
});
