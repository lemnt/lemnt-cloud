import { request } from '@/api/request';

export function unlockScreen(password: string, signal: AbortSignal) {
  return request<boolean>({
    url: '/auth/sessions/screen-unlocks',
    method: 'POST',
    data: { password },
    signal,
  });
}
