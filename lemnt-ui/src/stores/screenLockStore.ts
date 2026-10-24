import { create } from 'zustand';
import { getToken } from '@/shared/auth/token';
import {
  getScreenLockPath,
  readScreenLock,
  SCREEN_LOCK_KEY,
} from '@/modules/auth/utils/screenLock';
import type { ScreenLockRecord } from '@/modules/auth/utils/screenLock';

interface ScreenLockState {
  record?: ScreenLockRecord;
  lock: (userId: number, path: string) => void;
  clear: () => void;
  unlock: (record: ScreenLockRecord, token: string | undefined) => boolean;
  sync: () => void;
}

export const useScreenLock = create<ScreenLockState>((set, get) => ({
  record: readScreenLock(),
  lock: (userId, path) => {
    const record = { userId, path: getScreenLockPath(path) };
    // 存储不可用时不宣称已锁定，调用入口显示失败原因。
    localStorage.setItem(SCREEN_LOCK_KEY, JSON.stringify(record));
    set({ record });
  },
  clear: () => {
    try {
      localStorage.removeItem(SCREEN_LOCK_KEY);
    } finally {
      set({ record: undefined });
    }
  },
  unlock: (record, token) => {
    // 慢响应不能解除后来创建的锁定，也不能跨登录会话解锁。
    if (!token || token !== getToken() || record !== get().record) return false;
    get().clear();
    return true;
  },
  sync: () => set({ record: readScreenLock() }),
}));
