import { getNoticeVideoSource } from './noticeVideoUrl';
import type { NoticeVideoSource } from './noticeVideoUrl';

type NoticeVideoElement = HTMLVideoElement | HTMLIFrameElement;

export function createNoticeVideoElement(
  source: NoticeVideoSource,
  deferLoad = false,
): NoticeVideoElement {
  const node = document.createElement(source.kind === 'file' ? 'video' : 'iframe');
  node.className = 'ql-video';
  node.setAttribute('contenteditable', 'false');
  node.setAttribute('data-notice-video-src', source.url);
  if (!deferLoad) node.setAttribute('src', source.url);
  if (source.kind === 'file') {
    node.setAttribute('controls', '');
    node.setAttribute('playsinline', '');
    node.setAttribute('preload', 'none');
    node.setAttribute('aria-label', '通知视频');
  } else {
    node.setAttribute('title', '通知视频');
    node.setAttribute('loading', 'lazy');
    node.setAttribute('referrerpolicy', 'strict-origin-when-cross-origin');
    node.setAttribute('sandbox', 'allow-scripts allow-same-origin allow-presentation');
    node.setAttribute('allow', 'fullscreen; encrypted-media; picture-in-picture');
    node.setAttribute('allowfullscreen', '');
  }
  return node;
}

function mediaNodes(root: HTMLElement): NoticeVideoElement[] {
  return Array.from(root.querySelectorAll<NoticeVideoElement>('iframe.ql-video, video.ql-video'));
}

function stopVideoNodes(nodes: NoticeVideoElement[]): void {
  for (const node of nodes) {
    if ('pause' in node) node.pause();
    node.removeAttribute('src');
    if ('load' in node) node.load();
  }
}

/** Effects 恢复时重新加载，隐藏/关闭时卸载播放器，避免 iframe 在后台继续发声。 */
export function activateNoticeVideos(root: HTMLElement): () => void {
  const nodes = mediaNodes(root);
  for (const node of nodes) {
    const source = getNoticeVideoSource(node.getAttribute('data-notice-video-src') ?? '');
    if (source && node.tagName === (source.kind === 'file' ? 'VIDEO' : 'IFRAME')) {
      node.setAttribute('src', source.url);
    } else node.removeAttribute('src');
  }
  return () => stopVideoNodes(nodes);
}

export function stopNoticeVideos(root: HTMLElement): void {
  stopVideoNodes(mediaNodes(root));
}
