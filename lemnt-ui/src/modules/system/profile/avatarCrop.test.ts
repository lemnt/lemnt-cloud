import { expect, it, vi } from 'vitest';
import { avatarGeometry, exportAvatar } from './avatarCrop';

it('横图居中填满方形，不改变纵横比', () => {
  expect(avatarGeometry(400, 200, 0, 1, 0.5, 0.5)).toEqual({
    scale: 1,
    centerX: 100,
    centerY: 100,
  });
});
it('旋转 90 度后位置范围按交换后的宽高计算', () => {
  expect(avatarGeometry(400, 200, 90, 1, 0, 0)).toEqual({ scale: 1, centerX: 100, centerY: 200 });
});
it('放大后的边缘位置仍覆盖整个裁剪框', () => {
  expect(avatarGeometry(200, 200, 0, 2, 1, 0)).toEqual({ scale: 2, centerX: 0, centerY: 200 });
});
it('导出实际 PNG 文件，与上传扩展名一致', async () => {
  const canvas = {
    toBlob: vi.fn((callback: BlobCallback) => callback(new Blob(['png'], { type: 'image/png' }))),
  };
  const file = await exportAvatar(canvas as unknown as HTMLCanvasElement);
  expect(file.name).toBe('avatar.png');
  expect(file.type).toBe('image/png');
  expect(canvas.toBlob).toHaveBeenCalledWith(expect.any(Function), 'image/png');
});
it('编码失败拒绝导出，不上传空文件', async () => {
  const canvas = { toBlob: (callback: BlobCallback) => callback(null) };
  await expect(exportAvatar(canvas as unknown as HTMLCanvasElement)).rejects.toThrow(
    '头像生成失败',
  );
});
