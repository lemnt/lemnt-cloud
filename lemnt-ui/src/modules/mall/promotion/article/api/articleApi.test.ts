import { beforeEach, expect, it, vi } from 'vitest';
import * as api from './articleApi';
const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => mockedRequest.mockReset());
const row = {
  id: '9223372036854775807',
  categoryId: '2',
  spuId: '0',
  title: '标题',
  author: null,
  picUrl: 'https://example.test/a.png',
  introduction: null,
  sort: 0,
  status: 0 as const,
  recommendHot: false,
  recommendBanner: false,
  content: '正文',
  browseCount: 0,
  createTime: '2026-10-29T10:00:00',
};
it('文章分页使用rows/total且保留false和0筛选及长编号', async () => {
  mockedRequest.mockResolvedValue({ rows: [row], total: 1 });
  expect(
    (await api.getArticles({ pageNum: 1, pageSize: 20, status: 0 as const, recommendHot: false }))
      .data[0],
  ).toEqual(row);
  expect(mockedRequest).toHaveBeenCalledWith({
    url: '/mall/promotion/articles',
    params: { pageNum: 1, pageSize: 20, status: 0 as const, recommendHot: false },
  });
});
it('文章解码不把缺失浏览量或未知推荐状态当成零和false', () => {
  expect(() => api.decodeArticle({ ...row, browseCount: null })).toThrow();
  expect(() => api.decodeArticle({ ...row, recommendHot: 'false' })).toThrow();
  expect(() => api.decodeArticle({ ...row, id: Number.MAX_SAFE_INTEGER + 1 })).toThrow();
});
it('更新不使用实体替代写入结果，失败不报成功', async () => {
  mockedRequest.mockResolvedValue({ data: { success: false } });
  const { id: _, browseCount: __, createTime: ___, ...data } = row;
  void _;
  void __;
  void ___;
  await expect(api.updateArticle('7', data)).rejects.toThrow('未确认');
  mockedRequest.mockResolvedValue({ data: { id: '9' } });
  expect(await api.createArticle(data)).toBe('9');
});
it('空分类分页有效但缺失rows不能变成空数据', async () => {
  mockedRequest.mockResolvedValue({ rows: [], total: 0 });
  expect((await api.getArticleCategories({ pageNum: 1, pageSize: 20 })).data).toEqual([]);
  mockedRequest.mockResolvedValue({ total: 0 });
  await expect(api.getArticleCategories({ pageNum: 1, pageSize: 20 })).rejects.toThrow();
});
