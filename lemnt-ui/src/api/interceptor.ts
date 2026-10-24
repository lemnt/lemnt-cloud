import axios from 'axios';

import { getToken } from '@/shared/auth/token';

export const httpClient = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API ?? '/dev-api',
  timeout: 10000,
});

let onUnauthorized: (() => void) | undefined;
let expiredToken: string | undefined;

export function setUnauthorizedHandler(handler?: () => void): void {
  onUnauthorized = handler;
  expiredToken = undefined;
}

/** 同一过期会话只处理一次，旧请求返回 401 不能清除新会话。 */
export function notifyUnauthorized(token = getToken()): void {
  if (!token || token !== getToken() || token === expiredToken) return;
  expiredToken = token;
  onUnauthorized?.();
}

httpClient.interceptors.request.use((config) => {
  const token = getToken();
  // AxiosHeaders 会把布尔标记规范化为字符串，两个阶段均需识别。
  const isAnonymous = config.headers.isAnonymous === true || config.headers.isAnonymous === 'true';
  if (token && !config.headers.has('Authorization') && !isAnonymous) {
    config.headers.set('Authorization', `Bearer ${token}`);
  }
  config.headers.delete('isAnonymous');
  return config;
});

httpClient.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      const authorization: unknown = error.config?.headers.get('Authorization');
      if (typeof authorization === 'string' && authorization.startsWith('Bearer '))
        notifyUnauthorized(authorization.slice(7));
    }
    return Promise.reject(error);
  },
);
