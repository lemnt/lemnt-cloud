import { useSession } from '@/stores/userStore';
import { hasPermission } from '@/shared/auth/permissions';

const EMPTY_PERMISSIONS: string[] = [];

export function usePermission(): { can: (permission: string) => boolean } {
  const permissions = useSession((state) => state.user?.permissions ?? EMPTY_PERMISSIONS);
  return { can: (permission) => hasPermission(permissions, permission) };
}
