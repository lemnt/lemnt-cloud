import { afterEach, beforeEach, expect, it, vi } from 'vitest';
import { AxiosError, AxiosHeaders } from 'axios';
import { httpClient, setUnauthorizedHandler } from './interceptor';
import { request } from './request';
import type { AxiosAdapter } from 'axios';
const { token } = vi.hoisted(() => ({ token: { value: 'session-one' as string | undefined } }));
vi.mock('@/shared/auth/token', () => ({ getToken: () => token.value }));
const originalAdapter = httpClient.defaults.adapter;
beforeEach(() => {
  token.value = 'session-one';
  setUnauthorizedHandler(undefined);
});
afterEach(() => {
  httpClient.defaults.adapter = originalAdapter;
  setUnauthorizedHandler(undefined);
  vi.useRealTimers();
});
function respond(data: unknown) {
  const adapter: AxiosAdapter = async (config) => ({
    data,
    status: 200,
    statusText: 'OK',
    headers: new AxiosHeaders(),
    config,
  });
  const mockedAdapter = vi.fn(adapter);
  httpClient.defaults.adapter = mockedAdapter;
  return mockedAdapter;
}
it('认证头默认添加、匿名跳过且保留显式授权', async () => {
  const adapter = respond({ code: 200, data: true });
  await request({ url: '/info' });
  await request({ url: '/login' }, true);
  await request({ url: '/custom', headers: { Authorization: 'Basic custom' } });
  const configs = adapter.mock.calls.map(([config]) => config);
  expect(configs[0].headers.get('Authorization')).toBe('Bearer session-one');
  expect(configs[1].headers.has('Authorization')).toBe(false);
  expect(configs[2].headers.get('Authorization')).toBe('Basic custom');
  expect(configs.every((config) => !config.headers.has('isAnonymous'))).toBe(true);
});
it('成功保留分页字段，业务失败抛出后端消息', async () => {
  respond({ code: 200, rows: [], total: 0 });
  expect(await request({ url: '/list' })).toEqual({ code: 200, rows: [], total: 0 });
  respond({ code: 500, msg: '保存失败' });
  await expect(request({ url: '/fail' })).rejects.toThrow('保存失败');
});
it('同一会话并发业务 401 只触发一次，匿名失败不清除会话', async () => {
  const handler = vi.fn();
  setUnauthorizedHandler(handler);
  respond({ code: 401 });
  await Promise.allSettled([request({ url: '/one' }), request({ url: '/two' })]);
  expect(handler).toHaveBeenCalledTimes(1);
  await expect(request({ url: '/anonymous' }, true)).rejects.toThrow('登录已过期');
  expect(handler).toHaveBeenCalledTimes(1);
});
it('HTTP 401 处理认证会话且保留原错误', async () => {
  const handler = vi.fn();
  setUnauthorizedHandler(handler);
  httpClient.defaults.adapter = async (config) => {
    throw new AxiosError('Unauthorized', 'ERR_BAD_REQUEST', config, undefined, {
      status: 401,
      statusText: 'Unauthorized',
      headers: new AxiosHeaders(),
      config,
      data: {},
    });
  };
  await expect(request({ url: '/http401' })).rejects.toThrow('Unauthorized');
  expect(handler).toHaveBeenCalledTimes(1);
});
it('网关连接故障和限流提示失败，保留登录会话', async () => {
  const handler = vi.fn();
  setUnauthorizedHandler(handler);
  for (const code of [400, 429, 500, 502, 503]) {
    respond({ code, msg: '服务暂不可用，请重试' });
    await expect(request({ url: '/system/users/current-info' })).rejects.toThrow(
      '服务暂不可用，请重试',
    );
  }
  expect(handler).not.toHaveBeenCalled();
  expect(token.value).toBe('session-one');
});
it('旧请求返回 401 不清除刚切换的新会话', async () => {
  const handler = vi.fn();
  setUnauthorizedHandler(handler);
  httpClient.defaults.adapter = async (config) => {
    token.value = 'session-two';
    return {
      status: 200,
      statusText: 'OK',
      headers: new AxiosHeaders(),
      config,
      data: { code: 401 },
    };
  };
  await expect(request({ url: '/stale' })).rejects.toThrow();
  expect(handler).not.toHaveBeenCalled();
});
it('POST/PUT 相同参数在一秒内拦截，不同参数和 GET 不拦截', async () => {
  const adapter = respond({ code: 200, data: true });
  const config = {
    url: '/repeat',
    method: 'POST',
    data: { password: 'private' },
    params: { id: 1 },
  };
  await request(config);
  await expect(request(config)).rejects.toThrow('请勿重复提交');
  await request({ ...config, params: { id: 2 } });
  await request({ ...config, method: 'PUT' });
  await request({ url: '/repeat' });
  await request({ url: '/repeat' });
  expect(adapter).toHaveBeenCalledTimes(5);
});
it('重复窗口到期允许提交，失败允许立即重试', async () => {
  vi.useFakeTimers();
  respond({ code: 200 });
  const config = { url: '/expiry', method: 'PUT', data: { id: 1 } };
  await request(config);
  await vi.advanceTimersByTimeAsync(1000);
  await request(config);
  respond({ code: 500 });
  const failed = { url: '/retry', method: 'POST' };
  await expect(request(failed)).rejects.toThrow();
  respond({ code: 200 });
  await expect(request(failed)).resolves.toMatchObject({ code: 200 });
});
it('文件上传不被误判为相同的空对象', async () => {
  const adapter = respond({ code: 200 });
  const data = new FormData();
  data.append('file', new File(['image'], 'image.png'));
  await request({ url: '/upload', method: 'POST', data });
  await request({ url: '/upload', method: 'POST', data });
  expect(adapter).toHaveBeenCalledTimes(2);
});
it('并发相同写入只允许一次发送到适配器', async () => {
  const adapter = respond({ code: 200 });
  const config = { url: '/concurrent', method: 'POST', data: { id: 1 } };
  const results = await Promise.allSettled([request(config), request(config)]);
  expect(results.map((result) => result.status).sort()).toEqual(['fulfilled', 'rejected']);
  expect(adapter).toHaveBeenCalledTimes(1);
});
