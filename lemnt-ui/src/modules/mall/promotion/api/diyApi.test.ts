import { beforeEach, expect, it, vi } from 'vitest';
import * as api from './diyApi';

const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => mockedRequest.mockReset());

const template = {
  id: '1',
  name: '默认模板',
  used: true,
  usedTime: '2026-11-01 10:00:00',
  remark: null,
  previewPicUrls: ['https://example.test/p.png'],
  property: '{"tabbar":{}}',
};
const page = {
  id: '2',
  templateId: '1',
  name: '首页',
  remark: null,
  previewPicUrls: [],
  property: '{"components":[]}',
};

it('装修模板与页面走促销路径并校验 JSON 属性', async () => {
  mockedRequest.mockResolvedValue({ rows: [template], total: 1 });
  const pageResult = await api.getDiyTemplatePage({ pageNum: 1, pageSize: 100 });
  expect(pageResult.records[0]).toMatchObject({ id: '1', used: true });
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/promotion/diy-templates',
    params: { pageNum: 1, pageSize: 100 },
  });
  mockedRequest.mockResolvedValue({ data: '3' });
  expect(await api.createDiyTemplate({ name: '模板', previewPicUrls: [] })).toBe('3');
  await expect(api.updateDiyTemplateProperty('1', 'not-json')).rejects.toThrow('JSON');
  await api.updateDiyPageProperty('2', '{"components":[]}');
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/promotion/diy-pages/2/property',
    method: 'PUT',
    data: { property: '{"components":[]}' },
  });
});

it('装修页面分页与非法状态拒绝', async () => {
  mockedRequest.mockResolvedValue({ rows: [page], total: 1 });
  expect(
    (await api.getDiyPagePage({ templateId: '1', pageNum: 1, pageSize: 100 })).records[0].name,
  ).toBe('首页');
  mockedRequest.mockResolvedValue({ rows: [{ ...template, used: 'yes' }], total: 1 });
  await expect(api.getDiyTemplatePage({ pageNum: 1, pageSize: 100 })).rejects.toThrow('启用状态');
  mockedRequest.mockResolvedValue({ rows: [{ ...page, property: 'x' }], total: 1 });
  await expect(api.getDiyPagePage({ pageNum: 1, pageSize: 100 })).rejects.toThrow('JSON');
});
