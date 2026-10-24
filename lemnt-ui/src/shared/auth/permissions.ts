/** 保持若依的超级权限与管理员角色语义。 */
export function hasPermission(permissions: string[], required: string) {
  return permissions.includes('*:*:*') || permissions.includes(required);
}

export function safeRedirect(value: string | null): string {
  return value?.startsWith('/') && !value.startsWith('//') && !value.includes('\\')
    ? value
    : '/index';
}
