import { App, Button, Input, Upload } from 'antd';
import { uploadArticleImage } from '../api/articleApi';
interface ArticleImageFieldProps {
  value?: string | null;
  onChange?: (value: string) => void;
  onUploadingChange: (value: boolean) => void;
  disabled?: boolean;
}
/** 分类图标与文章封面使用同一系统上传接口。 */
export function ArticleImageField({
  value,
  onChange,
  onUploadingChange,
  disabled,
}: ArticleImageFieldProps) {
  const { message } = App.useApp();
  return (
    <div>
      <Input
        aria-label="图片地址"
        value={value ?? ''}
        disabled={disabled}
        maxLength={1024}
        onChange={(event) => onChange?.(event.target.value)}
      />
      <Upload
        accept="image/*"
        showUploadList={false}
        disabled={disabled}
        beforeUpload={async (file) => {
          onUploadingChange(true);
          try {
            onChange?.(await uploadArticleImage(file));
          } catch (reason) {
            message.error(String(reason));
          } finally {
            onUploadingChange(false);
          }
          return false;
        }}
      >
        <Button disabled={disabled}>上传图片</Button>
      </Upload>
    </div>
  );
}
