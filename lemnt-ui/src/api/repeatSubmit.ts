import type { AxiosRequestConfig } from 'axios';

const REPEAT_INTERVAL = 1000;
const submissions = new Map<string, number>();

/** 仅保存短期摘要，不把密码、Token 或请求体写入浏览器存储。 */
export async function reserveSubmission(config: AxiosRequestConfig<unknown>, token?: string) {
  const method = config.method?.toUpperCase();
  // 文件上传由页面控制提交，不将 FormData 序列化成空对象而误判重复。
  if (
    !['POST', 'PUT'].includes(method ?? '') ||
    config.data instanceof FormData ||
    !globalThis.crypto?.subtle
  )
    return undefined;
  const payload = JSON.stringify([
    method,
    config.baseURL,
    config.url,
    config.params,
    config.data,
    token,
  ]);
  const digest = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(payload));
  const key = Array.from(new Uint8Array(digest), (value) =>
    value.toString(16).padStart(2, '0'),
  ).join('');
  const now = Date.now();
  for (const [entry, time] of submissions)
    if (now - time >= REPEAT_INTERVAL) submissions.delete(entry);
  if (submissions.has(key)) throw new Error('数据正在处理，请勿重复提交');
  submissions.set(key, now);
  setTimeout(() => {
    if (submissions.get(key) === now) submissions.delete(key);
  }, REPEAT_INTERVAL);
  return { key, time: now };
}

/** 请求失败允许立即重试，不解除后续同摘要请求的保护。 */
export function releaseSubmission(ticket?: { key: string; time: number }) {
  if (ticket && submissions.get(ticket.key) === ticket.time) submissions.delete(ticket.key);
}
