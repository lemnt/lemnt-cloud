import { Activity } from 'react';
import { Route, Routes, useLocation } from 'react-router-dom';
import { useSession } from '@/stores/userStore';
import { useScreenLock } from '@/stores/screenLockStore';
import { getToken } from '@/shared/auth/token';
import LockScreen from '@/modules/auth/pages/LockScreen';
import BasicLayout from './BasicLayout';

export default function AuthenticatedLayout() {
  const session = useSession();
  const record = useScreenLock((state) => state.record);
  const location = useLocation();
  const isLocked = Boolean(
    getToken() && !session.loading && record && record.userId === session.user?.user.userId,
  );
  return (
    <>
      <Activity mode={isLocked ? 'hidden' : 'visible'}>
        <Routes location={isLocked && record ? record.path : location}>
          <Route path="*" element={<BasicLayout />} />
        </Routes>
      </Activity>
      {isLocked && record && <LockScreen key={record.userId + record.path} record={record} />}
    </>
  );
}
