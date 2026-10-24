const tokenKey = 'Admin-Token';

export function getToken(): string | undefined {
  const value = document.cookie.split('; ').find((item) => item.startsWith(`${tokenKey}=`));
  const token = value ? decodeURIComponent(value.slice(tokenKey.length + 1)) : undefined;
  return token && token !== 'undefined' && token !== 'null' ? token : undefined;
}

export function setToken(token: string) {
  document.cookie = `${tokenKey}=${encodeURIComponent(token)}; Path=/; SameSite=Lax`;
}

export function removeToken() {
  document.cookie = `${tokenKey}=; Path=/; Max-Age=0; SameSite=Lax`;
}
