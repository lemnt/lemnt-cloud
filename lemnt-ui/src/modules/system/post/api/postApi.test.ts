import { beforeEach, expect, it, vi } from 'vitest';
import { createPost, deletePost, getPostDetail, getPostList, updatePost } from './postApi';

const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => {
  mockedRequest.mockReset();
});

it('岗位分页读取顶层 rows/total 并保留查询参数', async () => {
  mockedRequest.mockResolvedValue({ code: 200, rows: [{ postId: 2 }], total: 12 });
  const params = { pageNum: 2, pageSize: 10, postCode: 'test', status: '1' };
  expect(await getPostList(params)).toEqual({ data: [{ postId: 2 }], total: 12, success: true });
  expect(mockedRequest).toHaveBeenCalledWith({ url: '/system/posts', params });
});

it('岗位详情读取 data，加载失败保持拒绝状态', async () => {
  const data = { postId: 2, postName: '岗位', remark: '说明' };
  mockedRequest.mockResolvedValue({ code: 200, data });
  expect(await getPostDetail(2)).toEqual(data);
  expect(mockedRequest).toHaveBeenCalledWith({ url: '/system/posts/2' });
  mockedRequest.mockRejectedValue(new Error('岗位不存在'));
  await expect(getPostDetail(2)).rejects.toThrow('岗位不存在');
});

it('新增与编辑使用各自方法，编辑携带岗位 ID', async () => {
  const data = { postCode: 'test', postName: '岗位', postSort: 0, status: '1', remark: '说明' };
  await createPost(data);
  expect(mockedRequest).toHaveBeenLastCalledWith({ url: '/system/posts', method: 'POST', data });
  await updatePost({ ...data, postId: 2 });
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/system/posts',
    method: 'PUT',
    data: { ...data, postId: 2 },
  });
});

it('单条及批量删除使用路径 ID，失败不伪装成功', async () => {
  await deletePost([2]);
  expect(mockedRequest).toHaveBeenLastCalledWith({ url: '/system/posts/2', method: 'DELETE' });
  await deletePost([2, 3]);
  expect(mockedRequest).toHaveBeenLastCalledWith({ url: '/system/posts/2,3', method: 'DELETE' });
  mockedRequest.mockRejectedValue(new Error('岗位仍被使用'));
  await expect(deletePost([2])).rejects.toThrow('岗位仍被使用');
});
