import { useEffect, useRef } from 'react';
import Quill from 'quill';
import DOMPurify from 'dompurify';
import 'quill/dist/quill.snow.css';

interface ArticleContentEditorProps {
  value?: string;
  disabled?: boolean;
  onChange?: (value: string) => void;
}
/** 文章使用现有Quill依赖；粘贴、编辑和预览均不执行输入HTML。 */
export function ArticleContentEditor({
  value = '',
  disabled = false,
  onChange,
}: ArticleContentEditorProps) {
  const host = useRef<HTMLDivElement>(null);
  const editor = useRef<Quill | undefined>(undefined);
  const change = useRef(onChange);
  useEffect(() => {
    change.current = onChange;
  }, [onChange]);
  useEffect(() => {
    if (!host.current) return;
    const container = host.current;
    const node = document.createElement('div');
    container.append(node);
    const quill = new Quill(node, {
      theme: 'snow',
      modules: {
        toolbar: [
          [{ header: [1, 2, 3, false] }],
          ['bold', 'italic', 'underline'],
          [{ list: 'ordered' }, { list: 'bullet' }],
          ['link', 'image'],
          ['clean'],
        ],
      },
    });
    editor.current = quill;
    const listener = () => change.current?.(DOMPurify.sanitize(quill.getSemanticHTML()));
    quill.on('text-change', listener);
    return () => {
      quill.off('text-change', listener);
      editor.current = undefined;
      container.replaceChildren();
    };
  }, []);
  useEffect(() => {
    const quill = editor.current;
    if (!quill) return;
    quill.enable(!disabled);
    const html = DOMPurify.sanitize(value);
    if (DOMPurify.sanitize(quill.getSemanticHTML()) !== html) {
      quill.clipboard.dangerouslyPasteHTML(html, 'silent');
    }
  }, [value, disabled]);
  return <div ref={host} aria-label="文章富文本编辑器" />;
}
