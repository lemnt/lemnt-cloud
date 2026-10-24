import { describe, expect, it } from 'vitest';
import { getNoticeVideoSource } from './noticeVideoUrl';

describe('通知视频地址', () => {
  it('文件直链保留签名和定位参数，站内路径保持相对形式', () => {
    expect(getNoticeVideoSource(' https://cdn.example.com/clip.MP4?token=a%2Bb#t=10 ')).toEqual({
      kind: 'file',
      url: 'https://cdn.example.com/clip.MP4?token=a%2Bb#t=10',
    });
    expect(getNoticeVideoSource('/files/clip.webm?version=2')).toEqual({
      kind: 'file',
      url: '/files/clip.webm?version=2',
    });
    expect(getNoticeVideoSource('http://localhost:9000/bucket/clip.mp4')?.kind).toBe('file');
  });

  it.each([
    'https://www.youtube.com/watch?v=M7lc1UVf-VE&autoplay=1',
    'https://m.youtube.com/shorts/M7lc1UVf-VE',
    'http://youtu.be/M7lc1UVf-VE',
    'https://www.youtube.com/embed/M7lc1UVf-VE?enablejsapi=1&autoplay=1',
    'https://www.youtube-nocookie.com/embed/M7lc1UVf-VE',
  ])('YouTube 地址规范化且关闭自动播放：%s', (input) => {
    expect(getNoticeVideoSource(input)).toEqual({
      kind: 'embed',
      url: 'https://www.youtube.com/embed/M7lc1UVf-VE?autoplay=0',
    });
  });

  it('Vimeo 保留不公开视频哈希，移除后台自动播放等参数', () => {
    expect(getNoticeVideoSource('https://vimeo.com/76979871')).toEqual({
      kind: 'embed',
      url: 'https://player.vimeo.com/video/76979871?autoplay=0',
    });
    const input = 'https://player.vimeo.com/video/76979871?h=5e2d1c1e6d&background=1&autoplay=1';
    expect(getNoticeVideoSource(input)).toEqual({
      kind: 'embed',
      url: 'https://player.vimeo.com/video/76979871?autoplay=0&h=5e2d1c1e6d',
    });
    expect(getNoticeVideoSource('https://player.vimeo.com/video/76979871?h=bad')).toBeUndefined();
    expect(getNoticeVideoSource('https://vimeo.com/76979871/5e2d1c1e6d')).toEqual({
      kind: 'embed',
      url: 'https://player.vimeo.com/video/76979871?autoplay=0&h=5e2d1c1e6d',
    });
    expect(
      getNoticeVideoSource('https://vimeo.com/76979871/5e2d1c1e6d?h=1111111111'),
    ).toBeUndefined();
  });

  it.each([
    '',
    'javascript:alert(1)',
    'data:video/mp4;base64,AAA',
    'blob:https://example.com/video.mp4',
    'file:///tmp/video.mp4',
    '//example.com/video.mp4',
    '/\\example.com/video.mp4',
    'https://user:password@example.com/video.mp4',
    'https://www.youtube.com.evil.example/embed/M7lc1UVf-VE',
    'https://www.youtube.com@evil.example/embed/M7lc1UVf-VE',
    'https://www.youtube.com:444/embed/M7lc1UVf-VE',
    'https://www.you\ntube.com/embed/M7lc1UVf-VE',
    'https://www.you\u0000tube.com/embed/M7lc1UVf-VE',
    'https://www.youtube.com/embed/M7lc1UVf-VE/extra',
    'https://www.youtube.com/watch?v=invalid',
    'https://www.youtube.com/redirect?q=https://evil.example',
    'https://player.vimeo.com/video/0',
    'https://example.com/player.html',
    '/files/video.mp4.html',
    '/files/video.mp4%3F.html',
    'relative/video.mp4',
  ])('拒绝不支持或不安全的地址：%s', (input) => {
    expect(getNoticeVideoSource(input)).toBeUndefined();
  });

  it('看起来属于视频平台的文件地址也只交给原生 video，不创建 iframe', () => {
    expect(getNoticeVideoSource('https://www.youtube.com/anything.mp4')?.kind).toBe('file');
  });

  it('规范化结果可以反复清洗，不改变视频类型和有效地址', () => {
    for (const input of [
      '/files/clip.mp4?token=123',
      'https://youtu.be/M7lc1UVf-VE',
      'https://player.vimeo.com/video/76979871?h=5e2d1c1e6d',
    ]) {
      const source = getNoticeVideoSource(input);
      expect(source).toBeDefined();
      if (!source) throw new Error('有效视频地址未解析');
      expect(getNoticeVideoSource(source.url)).toEqual(source);
    }
  });
});
