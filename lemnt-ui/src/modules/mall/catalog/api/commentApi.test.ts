import { beforeEach, expect, it, vi } from 'vitest';
import * as api from './commentApi';
const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => {
  mockedRequest.mockReset();
});
const row = {
  id: '9223372036854775807',
  spuId: 2,
  skuId: 3,
  orderNo: 'ORD1',
  memberId: 4,
  score: 4,
  content: '真实评价',
  visible: false,
  replyStatus: false,
  createTime: '2026-10-29 10:00:00',
};
it('后台评价使用分页rows/total，false和长编号保留', async () => {
  mockedRequest.mockResolvedValue({ rows: [row], total: 1 });
  const result = await api.getComments({
    pageNum: 1,
    pageSize: 20,
    visible: false,
    replied: false,
  });
  expect(result.data[0].id).toBe(row.id);
  expect(result.data[0].visible).toBe(false);
  expect(result.data[0].descriptionScores).toBeNull();
  expect(mockedRequest).toHaveBeenCalledWith({
    url: '/mall/comments',
    params: { pageNum: 1, pageSize: 20, visible: false, replied: false },
  });
});
it('历史未知快照不以当前商品资料或综合评分填充', () => {
  const value = api.decodeComment(row);
  expect(value.benefitScores).toBeNull();
  expect(value.spuName).toBeNull();
  expect(value.userNickname).toBeNull();
  expect(value.picUrls).toBeNull();
  expect(value.anonymous).toBeNull();
});
it('显隐必须得到成功对象确认，回复操作者不由请求指定', async () => {
  mockedRequest.mockResolvedValue({ data: { success: false } });
  await expect(api.setCommentVisible('9', false)).rejects.toThrow('未确认');
  mockedRequest.mockResolvedValue({ data: { ...row, replyStatus: true, replyContent: '谢谢' } });
  await api.replyComment('9', '谢谢');
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/comments/9/reply',
    method: 'PUT',
    data: { content: '谢谢' },
  });
});
it('真实业务原因通过Problem Details呈现，错误不能变成空页', async () => {
  mockedRequest.mockRejectedValue(
    Object.assign(new Error('remote'), {
      isAxiosError: true,
      response: { data: { detail: '评价不存在' } },
    }),
  );
  await expect(api.getComment('9')).rejects.toThrow('评价不存在');
  mockedRequest.mockResolvedValue({ total: 0 });
  await expect(api.getComments({ pageNum: 1, pageSize: 20 })).rejects.toThrow();
});
it('评价订单项ID保留真实长编号，旧记录不以SKU补造', () => {
  expect(api.decodeComment({ ...row, orderItemId: '9223372036854775807' }).orderItemId).toBe(
    '9223372036854775807',
  );
  expect(api.decodeComment(row).orderItemId).toBeNull();
});

it('组合评价筛选保留真实订单长ID、时间边界与未回复false', async () => {
  mockedRequest.mockResolvedValue({ rows: [], total: 0 });
  const query = {
    pageNum: 1,
    pageSize: 20,
    orderId: '9223372036854775807',
    userNickname: '会员',
    spuName: '购买商品',
    replied: false,
    createTimeStart: '2026-10-29T00:00:00',
    createTimeEnd: '2026-10-29T23:59:59',
  };
  await api.getComments(query);
  expect(mockedRequest).toHaveBeenCalledWith({ url: '/mall/comments', params: query });
});
