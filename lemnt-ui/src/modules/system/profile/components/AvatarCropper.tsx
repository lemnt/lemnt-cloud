import { useEffect, useRef, useState } from 'react';
import { Alert, App, Button, Modal, Slider, Space, Spin } from 'antd';
import { AVATAR_SIZE, drawAvatar, exportAvatar } from '../avatarCrop';
import styles from './avatarCropper.module.css';

interface AvatarCropperProps {
  file: File;
  onCancel: () => void;
  onSubmit: (file: File) => Promise<void>;
}

/** 图片只在本地解码；取消不会上传，失败保留当前裁剪状态以便重试。 */
export function AvatarCropper({ file, onCancel, onSubmit }: AvatarCropperProps) {
  const { message } = App.useApp();
  const canvas = useRef<HTMLCanvasElement>(null);
  const [image, setImage] = useState<ImageBitmap>();
  const [hasLoadError, setHasLoadError] = useState(false);
  const [zoom, setZoom] = useState(1);
  const [rotation, setRotation] = useState(0);
  const [x, setX] = useState(0.5);
  const [y, setY] = useState(0.5);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const submission = useRef(false);

  useEffect(() => {
    let isActive = true;
    let bitmap: ImageBitmap | undefined;
    void createImageBitmap(file)
      .then((result) => {
        bitmap = result;
        if (isActive) setImage(result);
        else result.close();
      })
      .catch(() => {
        if (isActive) setHasLoadError(true);
      });
    return () => {
      isActive = false;
      bitmap?.close();
    };
  }, [file, message]);

  useEffect(() => {
    // 保活恢复时，上一轮 Effect 清理过的位图仍可能暂时保留在 state 中。
    if (image?.width && canvas.current) drawAvatar(canvas.current, image, rotation, zoom, x, y);
  }, [image, rotation, zoom, x, y]);

  async function handleSubmit() {
    if (!image?.width || !canvas.current || submission.current) return;
    submission.current = true;
    setIsSubmitting(true);
    try {
      await onSubmit(await exportAvatar(canvas.current));
    } catch (error) {
      message.error(String(error));
    } finally {
      submission.current = false;
      setIsSubmitting(false);
    }
  }

  return (
    <Modal
      open
      title="裁剪头像"
      onCancel={onCancel}
      onOk={() => void handleSubmit()}
      okText="确认并上传"
      confirmLoading={isSubmitting}
      okButtonProps={{ disabled: !image?.width }}
      cancelButtonProps={{ disabled: isSubmitting }}
      closable={!isSubmitting}
      mask={{ closable: !isSubmitting }}
      keyboard={!isSubmitting}
    >
      {hasLoadError && <Alert type="error" title="图片无法读取，请取消后重新选择" />}
      <Spin spinning={!image && !hasLoadError}>
        <canvas
          ref={canvas}
          width={AVATAR_SIZE}
          height={AVATAR_SIZE}
          className={styles.preview}
          aria-label="头像裁剪预览"
        />
      </Spin>
      <fieldset disabled={isSubmitting || !image} className={styles.controls}>
        <label>
          缩放
          <Slider
            min={1}
            max={3}
            step={0.01}
            value={zoom}
            onChange={setZoom}
            disabled={isSubmitting || !image}
          />
        </label>
        <label>
          水平位置
          <Slider
            min={0}
            max={1}
            step={0.01}
            value={x}
            onChange={setX}
            disabled={isSubmitting || !image}
          />
        </label>
        <label>
          垂直位置
          <Slider
            min={0}
            max={1}
            step={0.01}
            value={y}
            onChange={setY}
            disabled={isSubmitting || !image}
          />
        </label>
        <Space>
          <Button
            disabled={isSubmitting || !image}
            onClick={() => setRotation((value) => (value + 270) % 360)}
          >
            向左旋转
          </Button>
          <Button
            disabled={isSubmitting || !image}
            onClick={() => setRotation((value) => (value + 90) % 360)}
          >
            向右旋转
          </Button>
        </Space>
      </fieldset>
    </Modal>
  );
}
