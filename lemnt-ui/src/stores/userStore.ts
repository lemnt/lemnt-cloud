import { create } from 'zustand';

import { request } from '@/api/request';
import { queryClient } from '@/api/queryClient';
import { normalizeMenus } from '@/router/menu';
import { getToken, removeToken, setToken } from '@/shared/auth/token';
import { useScreenLock } from './screenLockStore';

import type { MenuRoute, ServerRoute } from '@/router/menu';

interface UserInfo {
  user: { userId: number; userName: string; nickName?: string; avatar?: string };
  roles: string[];
  permissions: string[];
}

interface LoginValues {
  username: string;
  password: string;
  code?: string;
  uuid?: string;
}

interface Session {
  user?: UserInfo;
  menus: MenuRoute[];
  loading: boolean;
  error?: string;
  initialize: () => Promise<void>;
  login: (values: LoginValues) => Promise<void>;
  logout: () => Promise<void>;
  clearSession: () => void;
}

export const useSession = create<Session>((set, get) => ({
  menus: [],
  loading: Boolean(getToken()),
  clearSession: () => {
    removeToken();
    queryClient.clear();
    try {
      useScreenLock.getState().clear();
    } catch {
      // 浏览器存储不可用仍必须完成会话清理。
    }
    set({ user: undefined, menus: [], error: undefined, loading: false });
  },
  initialize: async () => {
    if (!getToken()) {
      set({ loading: false });
      return;
    }
    set({ loading: true, error: undefined });
    try {
      const [info, routes] = await Promise.all([
        request<UserInfo>({ url: '/system/users/current-info' }),
        queryClient.fetchQuery({
          queryKey: ['session', 'menus'],
          queryFn: async () =>
            (await request<ServerRoute[]>({ url: '/system/menus/routers' })).data,
        }),
      ]);
      set({ user: info.data, menus: normalizeMenus(routes) });
    } catch (reason) {
      set({ error: reason instanceof Error ? reason.message : '加载用户信息失败' });
      throw reason;
    } finally {
      set({ loading: false });
    }
  },
  login: async (values) => {
    get().clearSession();
    const result = await request<{ accessToken: string }>(
      {
        url: '/auth/sessions/login',
        method: 'POST',
        data: { ...values, username: values.username.trim() },
      },
      true,
    );
    setToken(result.data.accessToken);
    await get().initialize();
  },
  logout: async () => {
    try {
      await request({ url: '/auth/sessions/logout', method: 'DELETE' });
    } finally {
      get().clearSession();
    }
  },
}));
