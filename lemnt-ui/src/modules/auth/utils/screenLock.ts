export const SCREEN_LOCK_KEY = 'lemnt-ui-screen-lock';

export interface ScreenLockRecord {
  userId: number;
  path: string;
}

export function getScreenLockPath(path: string): string {
  if (!path.startsWith('/') || path.startsWith('//') || path.includes('\\')) return '/index';
  if (Array.from(path).some((character) => character.charCodeAt(0) <= 32)) return '/index';
  const pathname = path.split(/[?#]/)[0];
  return pathname === '/login' || pathname === '/lock' ? '/index' : path;
}

export function parseScreenLock(value: string | null): ScreenLockRecord | undefined {
  if (!value) return undefined;
  try {
    const record: unknown = JSON.parse(value);
    if (
      typeof record !== 'object' ||
      record === null ||
      !('userId' in record) ||
      typeof record.userId !== 'number' ||
      !Number.isSafeInteger(record.userId) ||
      record.userId <= 0 ||
      !('path' in record) ||
      typeof record.path !== 'string'
    ) {
      return undefined;
    }
    return { userId: record.userId, path: getScreenLockPath(record.path) };
  } catch {
    return undefined;
  }
}

export function readScreenLock(): ScreenLockRecord | undefined {
  try {
    return parseScreenLock(localStorage.getItem(SCREEN_LOCK_KEY));
  } catch {
    return undefined;
  }
}
