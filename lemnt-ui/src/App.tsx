import { useEffect } from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { QueryClientProvider } from '@tanstack/react-query';
import { queryClient } from '@/api/queryClient';
import { useSession } from '@/stores/userStore';
import { setUnauthorizedHandler } from '@/api/interceptor';
import AppRouter from './router';
import { useScreenLock } from '@/stores/screenLockStore';
import { SCREEN_LOCK_KEY } from '@/modules/auth/utils/screenLock';

const router = createBrowserRouter([{ path: '*', element: <AppRouter /> }]);

export default function Application() {
  useEffect(() => {
    const session = useSession.getState();
    void session.initialize().catch(() => undefined);
    setUnauthorizedHandler(() => useSession.getState().clearSession());
    const handleStorage = (event: StorageEvent) => {
      if (event.storageArea === localStorage && (event.key === SCREEN_LOCK_KEY || !event.key)) {
        useScreenLock.getState().sync();
      }
    };
    window.addEventListener('storage', handleStorage);
    return () => {
      setUnauthorizedHandler(undefined);
      window.removeEventListener('storage', handleStorage);
    };
  }, []);
  return (
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  );
}
