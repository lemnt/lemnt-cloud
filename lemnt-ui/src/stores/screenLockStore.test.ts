import { beforeEach, afterEach, expect, it, vi } from 'vitest';
import { useScreenLock } from './screenLockStore';
import { parseScreenLock, SCREEN_LOCK_KEY } from '@/modules/auth/utils/screenLock';

const { token } = vi.hoisted(() => ({ token: { value: 'session-one' as string | undefined } }));
vi.mock('@/shared/auth/token', () => ({ getToken: () => token.value }));

beforeEach(() => {
  const values = new Map<string, string>();
  vi.stubGlobal('localStorage', {
    getItem: (key: string) => values.get(key) ?? null,
    setItem: (key: string, value: string) => values.set(key, value),
    removeItem: (key: string) => values.delete(key),
  });
  token.value = 'session-one';
  useScreenLock.getState().clear();
});

afterEach(() => vi.unstubAllGlobals());

it('刷新恢复用户及完整返回地址，拒绝损坏记录和非站内返回地址', () => {
  useScreenLock.getState().lock(12, '/system/user?status=0#summary');
  const saved = localStorage.getItem(SCREEN_LOCK_KEY);
  expect(parseScreenLock(saved)).toEqual({
    userId: 12,
    path: '/system/user?status=0#summary',
  });
  for (const value of ['null', '{}', 'invalid', '{"userId":0,"path":"/index"}']) {
    expect(parseScreenLock(value)).toBeUndefined();
  }
  for (const path of ['//example.com', '/\\example.com', '/login?redirect=/index', '/lock']) {
    expect(parseScreenLock(JSON.stringify({ userId: 12, path }))?.path).toBe('/index');
  }
});

it('旧解锁响应不能解除后续锁定，登录令牌变化也不能解锁', () => {
  useScreenLock.getState().lock(12, '/index');
  const first = useScreenLock.getState().record!;
  useScreenLock.getState().lock(12, '/index');
  const second = useScreenLock.getState().record!;
  expect(useScreenLock.getState().unlock(first, 'session-one')).toBe(false);
  token.value = 'session-two';
  expect(useScreenLock.getState().unlock(second, 'session-one')).toBe(false);
  expect(useScreenLock.getState().record).toBe(second);
  expect(useScreenLock.getState().unlock(second, 'session-two')).toBe(true);
  expect(useScreenLock.getState().record).toBeUndefined();
  expect(localStorage.getItem(SCREEN_LOCK_KEY)).toBeNull();
});

it('其他窗口的存储变更重新同步，旧引用不能解锁同步后的记录', () => {
  useScreenLock.getState().lock(12, '/index');
  const original = useScreenLock.getState().record!;
  localStorage.setItem(SCREEN_LOCK_KEY, JSON.stringify({ userId: 12, path: '/user/profile' }));
  useScreenLock.getState().sync();
  expect(useScreenLock.getState().unlock(original, 'session-one')).toBe(false);
  expect(useScreenLock.getState().record?.path).toBe('/user/profile');
  localStorage.removeItem(SCREEN_LOCK_KEY);
  useScreenLock.getState().sync();
  expect(useScreenLock.getState().record).toBeUndefined();
});

it('存储写入失败不进入锁屏，清理失败仍清除内存状态', () => {
  vi.spyOn(localStorage, 'setItem').mockImplementation(() => {
    throw new Error('disabled');
  });
  expect(() => useScreenLock.getState().lock(12, '/index')).toThrow('disabled');
  expect(useScreenLock.getState().record).toBeUndefined();
  useScreenLock.setState({ record: { userId: 12, path: '/index' } });
  vi.spyOn(localStorage, 'removeItem').mockImplementation(() => {
    throw new Error('disabled');
  });
  expect(() => useScreenLock.getState().clear()).toThrow('disabled');
  expect(useScreenLock.getState().record).toBeUndefined();
});
