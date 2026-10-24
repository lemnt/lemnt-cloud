import { httpClient, notifyUnauthorized } from './interceptor';
import { reserveSubmission, releaseSubmission } from './repeatSubmit';
import { getToken } from '@/shared/auth/token';
import type { AxiosRequestConfig } from 'axios';
import type { ApiResponse } from './types';

/** 统一附加认证头，并保留后端业务状态码和分页响应。 */
export async function request<T>(
  config: AxiosRequestConfig,
  anonymous = false,
): Promise<ApiResponse<T>> {
  const token = anonymous ? undefined : getToken();
  const ticket = await reserveSubmission(config, token);
  try {
    const response = await httpClient.request<ApiResponse<T>>({
      ...config,
      headers: { ...config.headers, isAnonymous: anonymous },
    });
    const body = response.data;
    if (body.code === 401) {
      if (!anonymous) notifyUnauthorized(token);
      throw new Error(body.msg || '登录已过期');
    }
    if (body.code !== 200) throw new Error(body.msg || '请求失败');
    return body;
  } catch (error) {
    releaseSubmission(ticket);
    throw error;
  }
}
