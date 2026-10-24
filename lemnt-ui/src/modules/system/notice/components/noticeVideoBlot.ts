import Quill from 'quill';
import { BlockEmbed } from 'quill/blots/block';
import { createNoticeVideoElement } from '../utils/noticeVideo';
import { getNoticeVideoSource } from '../utils/noticeVideoUrl';

class NoticeVideoBlot extends BlockEmbed {
  static blotName = 'video';
  static tagName = ['IFRAME', 'VIDEO'];
  static className = 'ql-video';

  static create(value: unknown): HTMLElement {
    const source = typeof value === 'string' ? getNoticeVideoSource(value) : undefined;
    if (source) return createNoticeVideoElement(source);
    const placeholder = document.createElement('p');
    placeholder.className = 'ql-video';
    placeholder.textContent = '[不支持的视频地址]';
    return placeholder;
  }

  static value(node: Element): string {
    return node.getAttribute('src') ?? node.getAttribute('data-notice-video-src') ?? '';
  }
}

// Quill 只用于通知编辑；替换默认视频格式，避免通过粘贴加载任意 iframe。
Quill.register('formats/video', NoticeVideoBlot, true);
