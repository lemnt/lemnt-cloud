export const AVATAR_SIZE = 200;

/** 先按旋转后的边界等比填满裁剪框，再将位置限制在图片覆盖范围内。 */
export function avatarGeometry(
  width: number,
  height: number,
  rotation: number,
  zoom: number,
  x: number,
  y: number,
) {
  const isQuarterTurn = Math.abs(rotation % 180) === 90;
  const rotatedWidth = isQuarterTurn ? height : width;
  const rotatedHeight = isQuarterTurn ? width : height;
  const scale = (AVATAR_SIZE / Math.min(rotatedWidth, rotatedHeight)) * zoom;
  return {
    scale,
    centerX: AVATAR_SIZE / 2 + (0.5 - x) * (rotatedWidth * scale - AVATAR_SIZE),
    centerY: AVATAR_SIZE / 2 + (0.5 - y) * (rotatedHeight * scale - AVATAR_SIZE),
  };
}

/** 预览和上传使用同一画布，避免显示区域与实际裁剪结果不一致。 */
export function drawAvatar(
  canvas: HTMLCanvasElement,
  image: ImageBitmap,
  rotation: number,
  zoom: number,
  x: number,
  y: number,
) {
  const context = canvas.getContext('2d');
  if (!context) throw new Error('浏览器不支持图片裁剪');
  const geometry = avatarGeometry(image.width, image.height, rotation, zoom, x, y);
  context.clearRect(0, 0, AVATAR_SIZE, AVATAR_SIZE);
  context.save();
  context.translate(geometry.centerX, geometry.centerY);
  context.rotate((rotation * Math.PI) / 180);
  context.scale(geometry.scale, geometry.scale);
  context.drawImage(image, -image.width / 2, -image.height / 2);
  context.restore();
}

/** 固定输出 PNG，文件扩展名与实际编码一致。 */
export function exportAvatar(canvas: HTMLCanvasElement): Promise<File> {
  return new Promise((resolve, reject) => {
    canvas.toBlob((blob) => {
      if (!blob) reject(new Error('头像生成失败，请重试'));
      else resolve(new File([blob], 'avatar.png', { type: 'image/png' }));
    }, 'image/png');
  });
}
