import { beforeEach, expect, it, vi } from 'vitest';
import * as api from './bannerApi';
import type { BannerSave } from '../types';

const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => mockedRequest.mockReset());

const row = {
  id: '1',
  title: '首页 Banner',
  url: '/pages/store/index',
  picUrl: 'https://example.test/banner.png',
  sort: 3,
  status: 0,
  position: 1,
  memo: null,
  browseCount: 7,
};
const value: BannerSave = {
  title: '首页 Banner',
  url: '/pages/store/index',
  picUrl: 'https://example.test/banner.png',
  sort: 3,
  status: 0,
  position: 1,
  memo: null,
};

it('Banner 分页与保存走促销路径并校验响应', async () => {
  mockedRequest.mockResolvedValue({ rows: [row], total: 1 });
  const page = await api.getBannerPage({ title: ' 首页 ', status: 0, pageNum: 1, pageSize: 20 });
  expect(page.records[0]).toMatchObject({ id: '1', position: 1, browseCount: 7 });
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/promotion/banners',
    params: { title: '首页', status: 0, pageNum: 1, pageSize: 20 },
  });
  mockedRequest.mockResolvedValue({ data: '2' });
  expect(await api.createBanner(value)).toBe('2');
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/promotion/banners',
    method: 'POST',
    data: { ...value },
  });
  await api.updateBanner('2', { ...value, position: 5, status: 1 });
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/promotion/banners/2',
    method: 'PUT',
    data: { ...value, position: 5, status: 1 },
  });
  await api.deleteBanner('2');
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/promotion/banners/2',
    method: 'DELETE',
  });
});

it('Banner 非法位置、状态、金额格式和列表响应被拒绝', async () => {
  await expect(api.createBanner({ ...value, position: 6 as 1 })).rejects.toThrow('位置');
  await expect(api.createBanner({ ...value, title: ' ' })).rejects.toThrow('标题');
  await expect(api.createBanner({ ...value, picUrl: '' })).rejects.toThrow('图片');
  mockedRequest.mockResolvedValue({ rows: [{ ...row, status: 2 }], total: 1 });
  await expect(api.getBannerPage({ pageNum: 1, pageSize: 20 })).rejects.toThrow('数据');
  mockedRequest.mockResolvedValue({ rows: [{ ...row, position: 9 }], total: 1 });
  await expect(api.getBannerPage({ pageNum: 1, pageSize: 20 })).rejects.toThrow('位置');
  mockedRequest.mockResolvedValue({ rows: [], total: -1 });
  await expect(api.getBannerPage({ pageNum: 1, pageSize: 20 })).rejects.toThrow('总数');
});
