import { beforeEach, expect, it, vi } from 'vitest';
import {
  deleteNotice,
  getNoticeList,
  getNoticeReadUsers,
  getNoticeTopList,
  markNoticeRead,
  markNoticesRead,
  updateNotice,
  uploadNoticeImage,
} from './noticeApi';
const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => mockedRequest.mockReset());
it('顶部通知保留后端全局未读数量与单条已读状态', async () => {
  const data = { list: [{ noticeId: 1, isRead: false }], unreadCount: 8 };
  mockedRequest.mockResolvedValue({ code: 200, data });
  expect(await getNoticeTopList()).toEqual(data);
  expect(mockedRequest).toHaveBeenCalledWith({ url: '/system/notices/top' });
});
it('单条标记使用查询参数，批量标记使用 JSON ID 数组', async () => {
  await markNoticeRead(1);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/system/notices/mark-read',
    method: 'POST',
    params: { noticeId: 1 },
  });
  await markNoticesRead([2, 3]);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/system/notices/mark-read-all',
    method: 'POST',
    data: { ids: [2, 3] },
  });
});
it('通知列表读取顶层分页结果', async () => {
  mockedRequest.mockResolvedValue({ code: 200, rows: [{ noticeId: 1 }], total: 1 });
  expect(await getNoticeList({ noticeType: '1', pageNum: 2 })).toEqual({
    data: [{ noticeId: 1 }],
    total: 1,
    success: true,
  });
});
it('富文本内容保持 HTML 字符串提交协议', async () => {
  mockedRequest.mockResolvedValue({ code: 200, data: true });
  const data = {
    noticeId: 2,
    noticeTitle: '通知',
    noticeType: '1',
    status: '1',
    noticeContent: '<p><strong>内容</strong></p>',
  };
  await updateNotice(data);
  expect(mockedRequest).toHaveBeenCalledWith({ url: '/system/notices', method: 'PUT', data });
});
it('已读用户查询包含通知 ID 和搜索词，不产生已读写入', async () => {
  mockedRequest.mockResolvedValue({ code: 200, rows: [], total: 0 });
  await getNoticeReadUsers({ noticeId: 2, searchValue: '用户', pageNum: 1 });
  expect(mockedRequest).toHaveBeenCalledTimes(1);
  expect(mockedRequest).toHaveBeenCalledWith({
    url: '/system/notices/read-users',
    params: { noticeId: 2, searchValue: '用户', pageNum: 1 },
  });
});
it('通知删除使用路径 ID 数组', async () => {
  mockedRequest.mockResolvedValue({ code: 200, data: true });
  await deleteNotice([2, 3]);
  expect(mockedRequest).toHaveBeenCalledWith({ url: '/system/notices/2,3', method: 'DELETE' });
});
it('图片上传使用 file 表单字段，不手工设置 multipart boundary', async () => {
  mockedRequest.mockResolvedValue({ code: 200, data: { url: '/image.png' } });
  const file = new File(['image'], 'image.png', { type: 'image/png' });
  expect(await uploadNoticeImage(file)).toBe('/image.png');
  const config = mockedRequest.mock.calls[0][0] as {
    url: string;
    method: string;
    data: FormData;
    headers?: unknown;
  };
  expect(config.url).toBe('/system/files/upload');
  expect(config.data.get('file')).toBe(file);
  expect(config.headers).toBeUndefined();
});
it('图片上传没有有效地址时拒绝插入', async () => {
  const file = new File(['image'], 'image.png', { type: 'image/png' });
  for (const data of [undefined, {}, { url: '' }, { url: '   ' }]) {
    mockedRequest.mockResolvedValue({ code: 200, data });
    await expect(uploadNoticeImage(file)).rejects.toThrow('图片上传未返回有效地址');
  }
});
