import { useEffect, useMemo, useRef, useState } from 'react';
import { Button } from 'antd';
import { sanitizeNoticeHtml } from '../utils/noticeHtml';
import { activateNoticeVideos } from '../utils/noticeVideo';
import 'quill/dist/quill.snow.css';
import './noticeContent.css';

interface NoticeContentProps {
  html?: string;
  isVisible?: boolean;
}

/** 阅读和管理预览共用 Quill 排版与 HTML 清洗规则。 */
export function NoticeContent({ html = '', isVisible = true }: NoticeContentProps) {
  const content = useRef<HTMLDivElement>(null);
  // 清洗涉及 HTML 解析；保持相同内容的 DOM，避免父组件刷新时重新加载播放器。
  const clean = useMemo(() => sanitizeNoticeHtml(html, true), [html]);
  const [attempt, setAttempt] = useState(0);
  const hasVideo = /<(?:iframe|video)\b/i.test(clean);
  useEffect(() => {
    if (!content.current || !isVisible) return;
    return activateNoticeVideos(content.current);
  }, [clean, attempt, isVisible]);
  return (
    <div className="notice-content ql-snow">
      <div
        ref={content}
        key={attempt}
        className="ql-editor"
        dangerouslySetInnerHTML={{
          __html: clean,
        }}
      />
      {hasVideo && (
        <Button className="notice-video-retry" onClick={() => setAttempt((value) => value + 1)}>
          重新加载视频
        </Button>
      )}
    </div>
  );
}
