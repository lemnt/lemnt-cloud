export interface NoticeVideoSource {
  kind: 'file' | 'embed';
  url: string;
}

const YOUTUBE_HOSTS = new Set(['youtube.com', 'www.youtube.com', 'm.youtube.com']);
const YOUTUBE_EMBED_HOSTS = new Set(['youtube-nocookie.com', 'www.youtube-nocookie.com']);
const VIMEO_HOSTS = new Set(['vimeo.com', 'www.vimeo.com']);
const YOUTUBE_ID = /^[\w-]{11}$/;
const VIMEO_ID = /^[1-9]\d{0,19}$/;
const FILE_EXTENSION = /\.(mp4|webm)$/i;
const ASCII_SPACE_CODE = 0x20;
const ASCII_DELETE_CODE = 0x7f;

/** 文件只通过 video 播放；iframe 仅使用已确认的视频平台播放地址。 */
export function getNoticeVideoSource(input: string): NoticeVideoSource | undefined {
  const value = input.trim();
  const hasUnsafeCharacters = Array.from(value).some((character) => {
    const code = character.charCodeAt(0);
    return code <= ASCII_SPACE_CODE || code === ASCII_DELETE_CODE || character === '\\';
  });
  if (!value || hasUnsafeCharacters || value.startsWith('//')) {
    return undefined;
  }
  const isRelative = value.startsWith('/');
  let url: URL;
  try {
    url = isRelative ? new URL(value, 'https://notice.invalid') : new URL(value);
  } catch {
    return undefined;
  }
  if (!['http:', 'https:'].includes(url.protocol) || url.username || url.password) return undefined;

  // 保留文件签名参数和片段，不将文件 URL 作为可执行的 iframe 页面。
  if (FILE_EXTENSION.test(url.pathname)) {
    return { kind: 'file', url: isRelative ? url.pathname + url.search + url.hash : url.href };
  }
  if (isRelative || url.port) return undefined;

  let youtubeId: string | undefined;
  if (YOUTUBE_HOSTS.has(url.hostname)) {
    youtubeId =
      url.pathname === '/watch'
        ? (url.searchParams.get('v') ?? undefined)
        : /^\/(?:embed|shorts)\/([\w-]{11})\/?$/.exec(url.pathname)?.[1];
  } else if (YOUTUBE_EMBED_HOSTS.has(url.hostname)) {
    youtubeId = /^\/embed\/([\w-]{11})\/?$/.exec(url.pathname)?.[1];
  } else if (url.hostname === 'youtu.be') {
    youtubeId = /^\/([\w-]{11})\/?$/.exec(url.pathname)?.[1];
  }
  if (youtubeId && YOUTUBE_ID.test(youtubeId)) {
    return { kind: 'embed', url: `https://www.youtube.com/embed/${youtubeId}?autoplay=0` };
  }

  const vimeoShare = VIMEO_HOSTS.has(url.hostname)
    ? /^\/([1-9]\d{0,19})(?:\/([a-f0-9]{10}))?\/?$/i.exec(url.pathname)
    : undefined;
  const vimeoId =
    vimeoShare?.[1] ??
    (url.hostname === 'player.vimeo.com'
      ? /^\/video\/([1-9]\d{0,19})\/?$/.exec(url.pathname)?.[1]
      : undefined);
  if (vimeoId && VIMEO_ID.test(vimeoId)) {
    const embed = new URL(`https://player.vimeo.com/video/${vimeoId}`);
    embed.searchParams.set('autoplay', '0');
    // 不公开视频的 h 参数属于访问凭证，保留合法值，其余播放参数不沿用。
    const queryHash = url.searchParams.get('h');
    if (queryHash && vimeoShare?.[2] && queryHash !== vimeoShare[2]) return undefined;
    const hash = queryHash ?? vimeoShare?.[2];
    if (hash !== undefined) {
      if (!/^[a-f0-9]{10}$/i.test(hash)) return undefined;
      embed.searchParams.set('h', hash);
    }
    return { kind: 'embed', url: embed.href };
  }
  return undefined;
}
