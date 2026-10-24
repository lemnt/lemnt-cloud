import { useEffect, useRef, useState } from 'react';
import { App, Button, Upload } from 'antd';
import { ProFormText } from '@ant-design/pro-components';
import Quill from 'quill';
import { uploadNoticeImage } from '../api/noticeApi';
import { sanitizeNoticeHtml } from '../utils/noticeHtml';
import { stopNoticeVideos } from '../utils/noticeVideo';
import { getNoticeVideoSource } from '../utils/noticeVideoUrl';
import './noticeVideoBlot';
import 'quill/dist/quill.snow.css';
import './noticeEditor.css';
import { AdminModalForm as ModalForm } from '@/shared/components/AdminModalForm';

interface NoticeEditorProps {
  value?: string;
  disabled?: boolean;
  onChange?: (html: string) => void;
  onUploadingChange?: (uploading: boolean) => void;
}

interface NoticeVideoFormValues {
  url: string;
}

/** Quill 由本组件管理生命周期，表单值仍以 HTML 保持原接口协议。 */
export function NoticeEditor({
  value = '',
  disabled = false,
  onChange,
  onUploadingChange,
}: NoticeEditorProps) {
  const uploadOperation = useRef(false);
  const host = useRef<HTMLDivElement>(null);
  const editor = useRef<Quill | undefined>(undefined);
  const currentValue = useRef(value);
  const change = useRef(onChange);
  const uploadImage = useRef<((file: File) => Promise<void>) | undefined>(undefined);
  const savedIndex = useRef<number | undefined>(undefined);
  const [isUploading, setIsUploading] = useState(false);
  const [isVideoOpen, setIsVideoOpen] = useState(false);
  const { message } = App.useApp();
  useEffect(() => {
    change.current = onChange;
  }, [onChange]);
  useEffect(() => {
    if (!host.current) return;
    const container = document.createElement('div');
    host.current.append(container);
    const quill = new Quill(container, {
      theme: 'snow',
      placeholder: '请输入内容',
      modules: {
        history: { userOnly: true },
        toolbar: [
          [{ header: [1, 2, 3, 4, 5, 6, false] }],
          [{ size: ['small', false, 'large', 'huge'] }],
          ['bold', 'italic', 'underline', 'strike'],
          ['blockquote', 'code-block'],
          [{ list: 'ordered' }, { list: 'bullet' }],
          [{ indent: '-1' }, { indent: '+1' }],
          [{ color: [] }, { background: [] }],
          [{ align: [] }],
          ['link', 'clean'],
        ],
      },
    });
    editor.current = quill;
    quill.clipboard.dangerouslyPasteHTML(sanitizeNoticeHtml(currentValue.current), 'silent');
    const handleChange = () => {
      currentValue.current = sanitizeNoticeHtml(quill.root.innerHTML);
      change.current?.(currentValue.current);
    };
    const handleSelection = (range: { index: number } | null) => {
      if (range) savedIndex.current = range.index;
    };
    const handlePaste = (event: ClipboardEvent) => {
      if (!quill.isEnabled()) {
        event.preventDefault();
        event.stopImmediatePropagation();
        return;
      }
      const files = Array.from(event.clipboardData?.items ?? [])
        .filter((item) => item.kind === 'file' && item.type.startsWith('image/'))
        .map((item) => item.getAsFile())
        .filter((file): file is File => file !== null);
      if (!files.length) {
        const html = event.clipboardData?.getData('text/html');
        if (!html) return;
        event.preventDefault();
        event.stopImmediatePropagation();
        const range = quill.getSelection(true);
        const index = Math.min(range?.index ?? quill.getLength() - 1, quill.getLength() - 1);
        quill.history.cutoff();
        if (range?.length) quill.deleteText(index, range.length, 'user');
        quill.clipboard.dangerouslyPasteHTML(index, sanitizeNoticeHtml(html), 'user');
        quill.history.cutoff();
        return;
      }
      event.preventDefault();
      event.stopImmediatePropagation();
      if (files.length > 1) {
        message.error('请每次粘贴一张图片');
        return;
      }
      void uploadImage.current?.(files[0]);
    };
    quill.on('text-change', handleChange);
    quill.on('selection-change', handleSelection);
    quill.root.addEventListener('paste', handlePaste, true);
    quill.history.clear();
    const root = host.current;
    return () => {
      quill.off('text-change', handleChange);
      quill.off('selection-change', handleSelection);
      quill.root.removeEventListener('paste', handlePaste, true);
      stopNoticeVideos(quill.root);
      editor.current = undefined;
      root.replaceChildren();
    };
  }, [message]);
  useEffect(() => {
    if (value !== currentValue.current && editor.current) {
      currentValue.current = value;
      editor.current.clipboard.dangerouslyPasteHTML(sanitizeNoticeHtml(value), 'silent');
      editor.current.history.clear();
      savedIndex.current = undefined;
    }
  }, [value]);
  useEffect(() => {
    editor.current?.enable(!disabled && !isUploading);
  }, [disabled, isUploading]);
  useEffect(() => {
    uploadImage.current = async (file) => {
      const quill = editor.current;
      if (!quill || disabled || uploadOperation.current) return;
      if (!['image/png', 'image/jpeg'].includes(file.type) || file.size > 5 * 1024 * 1024) {
        message.error('请上传不超过 5MB 的 PNG/JPEG 图片');
        return;
      }
      const index = Math.min(
        quill.getSelection()?.index ?? savedIndex.current ?? quill.getLength() - 1,
        quill.getLength() - 1,
      );
      uploadOperation.current = true;
      quill.enable(false);
      setIsUploading(true);
      onUploadingChange?.(true);
      try {
        const url = await uploadNoticeImage(file);
        // 保活隐藏时编辑器会清理 DOM；上传结果仍需写回当前表单，供恢复时回显。
        const activeEditor = editor.current;
        const target = activeEditor ?? quill;
        // Quill 忽略只读期间的 user 写入；上传完成后同步恢复并插入，保留撤销能力。
        target.enable(true);
        target.history.cutoff();
        target.insertEmbed(index, 'image', url, 'user');
        target.history.cutoff();
        if (!activeEditor) {
          currentValue.current = sanitizeNoticeHtml(target.root.innerHTML);
          change.current?.(currentValue.current);
        }
        savedIndex.current = index + 1;
        if (activeEditor) target.setSelection(index + 1, 0, 'silent');
      } catch (error) {
        message.error(String(error));
      } finally {
        uploadOperation.current = false;
        setIsUploading(false);
        onUploadingChange?.(false);
      }
    };
  }, [disabled, message, onUploadingChange]);
  return (
    <div className="notice-editor">
      <Upload
        accept="image/png,image/jpeg"
        showUploadList={false}
        disabled={disabled || isUploading}
        beforeUpload={async (file) => {
          await uploadImage.current?.(file);
          return Upload.LIST_IGNORE;
        }}
      >
        <Button disabled={disabled || isUploading} loading={isUploading}>
          插入图片
        </Button>
      </Upload>
      <Button
        className="notice-editor-video-button"
        disabled={disabled || isUploading}
        onClick={() => {
          const quill = editor.current;
          if (!quill || !quill.isEnabled() || uploadOperation.current) return;
          savedIndex.current = quill.getSelection()?.index ?? savedIndex.current;
          setIsVideoOpen(true);
        }}
      >
        插入视频
      </Button>
      <div ref={host} />
      {isVideoOpen && (
        <ModalForm<NoticeVideoFormValues>
          description="添加视频链接与展示标题"
          open
          title="插入视频"
          initialValues={{ url: '' }}
          disabled={disabled || isUploading}
          modalProps={{ destroyOnHidden: true, mask: { closable: false } }}
          onOpenChange={setIsVideoOpen}
          onFinish={async (values) => {
            const quill = editor.current;
            if (!quill || !quill.isEnabled() || disabled || uploadOperation.current) return false;
            const source = getNoticeVideoSource(values.url);
            if (!source) {
              message.error('请输入 MP4/WebM 视频直链或 YouTube/Vimeo 视频链接');
              return false;
            }
            const index = Math.min(
              savedIndex.current ?? quill.getLength() - 1,
              quill.getLength() - 1,
            );
            quill.history.cutoff();
            quill.insertEmbed(index, 'video', source.url, 'user');
            quill.history.cutoff();
            savedIndex.current = Math.min(index + 1, quill.getLength() - 1);
            quill.setSelection(savedIndex.current, 0, 'silent');
            setIsVideoOpen(false);
            return true;
          }}
        >
          <ProFormText
            name="url"
            label="视频地址"
            extra="支持 HTTP/HTTPS 的 MP4/WebM 直链、站内视频路径，以及 YouTube/Vimeo 视频链接。"
            rules={[{ required: true, whitespace: true, message: '请输入视频地址' }]}
          />
        </ModalForm>
      )}
    </div>
  );
}
