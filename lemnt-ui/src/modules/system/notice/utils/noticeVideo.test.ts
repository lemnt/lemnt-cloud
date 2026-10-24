import { afterEach, describe, expect, it, vi } from 'vitest';
import { activateNoticeVideos, createNoticeVideoElement, stopNoticeVideos } from './noticeVideo';

function media(tagName: 'VIDEO' | 'IFRAME', source: string) {
  const attributes = new Map<string, string>([['data-notice-video-src', source]]);
  const node = {
    tagName,
    className: '',
    getAttribute: (name: string) => attributes.get(name) ?? null,
    setAttribute: vi.fn((name: string, value: string) => attributes.set(name, value)),
    removeAttribute: vi.fn((name: string) => attributes.delete(name)),
  };
  const pause = vi.fn();
  const load = vi.fn();
  const player = tagName === 'VIDEO' ? { ...node, pause, load } : node;
  return {
    attributes,
    player,
    pause,
    load,
    element: player as unknown as HTMLVideoElement | HTMLIFrameElement,
  };
}

function root(nodes: (HTMLVideoElement | HTMLIFrameElement)[]): HTMLElement {
  return { querySelectorAll: () => nodes } as unknown as HTMLElement;
}

afterEach(() => vi.unstubAllGlobals());

describe('通知视频播放生命周期', () => {
  it('阅读时恢复已验证地址，隐藏时暂停文件、释放资源并卸载外部播放器', () => {
    const video = media('VIDEO', '/files/clip.mp4');
    const frame = media('IFRAME', 'https://www.youtube.com/embed/M7lc1UVf-VE?autoplay=0');
    const container = root([video.element, frame.element]);
    const cleanup = activateNoticeVideos(container);
    expect(video.attributes.get('src')).toBe('/files/clip.mp4');
    expect(frame.attributes.get('src')).toBe(
      'https://www.youtube.com/embed/M7lc1UVf-VE?autoplay=0',
    );
    cleanup();
    expect(video.attributes.has('src')).toBe(false);
    expect(frame.attributes.has('src')).toBe(false);
    expect(video.pause).toHaveBeenCalledOnce();
    expect(video.load).toHaveBeenCalledOnce();
    activateNoticeVideos(container);
    expect(video.attributes.get('src')).toBe('/files/clip.mp4');
    expect(video.attributes.has('autoplay')).toBe(false);
  });

  it('重新加载或切换内容时，旧清理函数只释放原播放器', () => {
    const previous = media('IFRAME', 'https://www.youtube.com/embed/M7lc1UVf-VE');
    const next = media('IFRAME', 'https://player.vimeo.com/video/76979871');
    let nodes = [previous.element];
    const container = { querySelectorAll: () => nodes } as unknown as HTMLElement;
    const cleanup = activateNoticeVideos(container);
    nodes = [next.element];
    activateNoticeVideos(container);
    cleanup();
    expect(previous.attributes.has('src')).toBe(false);
    expect(next.attributes.get('src')).toBe('https://player.vimeo.com/video/76979871?autoplay=0');
  });

  it('不通过 iframe 加载文件，也不恢复任意网页或可执行地址', () => {
    const wrongTag = media('IFRAME', '/files/clip.mp4');
    const unknown = media('IFRAME', 'https://evil.example/player');
    const script = media('VIDEO', 'javascript:alert(1)');
    for (const item of [wrongTag, unknown, script]) item.attributes.set('src', 'stale');
    activateNoticeVideos(root([wrongTag.element, unknown.element, script.element]));
    for (const item of [wrongTag, unknown, script]) expect(item.attributes.has('src')).toBe(false);
  });

  it('编辑器清理也停止播放器，保留地址用于隐藏后的上传回写和再次初始化', () => {
    const video = media('VIDEO', '/files/clip.webm');
    video.attributes.set('src', '/files/clip.webm');
    stopNoticeVideos(root([video.element]));
    expect(video.attributes.has('src')).toBe(false);
    expect(video.attributes.get('data-notice-video-src')).toBe('/files/clip.webm');
  });

  it('创建播放器只设置固定属性，不启用自动播放和任意 iframe 权限', () => {
    const video = media('VIDEO', '');
    const frame = media('IFRAME', '');
    vi.stubGlobal('document', {
      createElement: (tag: string) => (tag === 'video' ? video.element : frame.element),
    });
    createNoticeVideoElement({ kind: 'file', url: '/files/clip.mp4' });
    expect(video.attributes.get('src')).toBe('/files/clip.mp4');
    expect(video.attributes.has('controls')).toBe(true);
    expect(video.attributes.get('preload')).toBe('none');
    expect(video.attributes.has('autoplay')).toBe(false);
    createNoticeVideoElement(
      { kind: 'embed', url: 'https://www.youtube.com/embed/M7lc1UVf-VE?autoplay=0' },
      true,
    );
    expect(frame.attributes.has('src')).toBe(false);
    expect(frame.attributes.get('sandbox')).toBe(
      'allow-scripts allow-same-origin allow-presentation',
    );
    expect(frame.attributes.get('allow')).not.toContain('autoplay');
  });
});
