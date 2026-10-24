import DOMPurify from 'dompurify';
import { createNoticeVideoElement } from './noticeVideo';
import { getNoticeVideoSource } from './noticeVideoUrl';

/** 编辑、HTML 粘贴和所有阅读入口共用同一规则，不使用影响全站的清洗钩子。 */
export function sanitizeNoticeHtml(html: string, deferVideoLoad = false): string {
  const fragment = DOMPurify.sanitize(html, {
    USE_PROFILES: { html: true },
    ADD_TAGS: ['iframe'],
    FORBID_TAGS: ['audio', 'object', 'embed'],
    FORBID_ATTR: ['srcdoc'],
    RETURN_DOM_FRAGMENT: true,
  });
  for (const node of fragment.querySelectorAll('iframe, video')) {
    const candidates = [
      node.getAttribute('src'),
      node.getAttribute('data-notice-video-src'),
      ...Array.from(node.querySelectorAll('source')).map((source) => source.getAttribute('src')),
    ];
    const source = candidates
      .filter((value): value is string => Boolean(value))
      .map(getNoticeVideoSource)
      .find((value) => value !== undefined);
    if (!source) {
      const placeholder = document.createElement('p');
      placeholder.textContent = '[视频地址暂不支持，请使用 MP4/WebM 或 YouTube/Vimeo 链接]';
      node.replaceWith(placeholder);
      continue;
    }
    // 重建媒体节点，不沿用输入 HTML 的事件、srcdoc、自动播放或页面样式属性。
    const safe = createNoticeVideoElement(source, deferVideoLoad);
    for (const className of ['ql-align-center', 'ql-align-right', 'ql-align-justify']) {
      if (node.classList.contains(className)) safe.classList.add(className);
    }
    node.replaceWith(safe);
  }
  const container = document.createElement('div');
  container.append(fragment);
  return container.innerHTML;
}
