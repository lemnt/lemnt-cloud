import { beforeEach, expect, it, vi } from 'vitest';
import * as api from './kefuApi';

const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => mockedRequest.mockReset());

const conversation = {
  id: '1', userId: '7', nickname: '会员', avatar: 'https://example.test/a.png',
  lastMessageTime: '2026-11-01 10:00:00', lastMessageContent: '你好', lastMessageContentType: 1,
  adminPinned: true, adminUnreadMessageCount: 2,
};
const message = {
  id: '2', conversationId: '1', senderId: '7', senderType: 1, contentType: 1,
  content: '你好', readStatus: false, createTime: '2026-11-01 10:00:00',
};

it('客服会话与消息走促销路径并严格解码', async () => {
  mockedRequest.mockResolvedValue({ data: [conversation] });
  const rows = await api.getConversations();
  expect(rows[0]).toMatchObject({ id: '1', userId: '7', adminUnreadMessageCount: 2 });
  expect(mockedRequest).toHaveBeenLastCalledWith({ url: '/mall/promotion/kefu-conversations' });

  mockedRequest.mockResolvedValue({ data: [message] });
  expect(await api.getMessages('1')).toEqual([message]);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/promotion/kefu-conversations/1/messages', params: { limit: 50 },
  });
});

it('客服回复、已读、置顶与删除参数受控', async () => {
  mockedRequest.mockResolvedValue({ data: '9' });
  expect(await api.sendMessage('1', ' 回复 ')).toBe('9');
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/promotion/kefu-conversations/1/messages', method: 'POST', data: { contentType: 1, content: '回复' },
  });
  await expect(api.sendMessage('1', ' ')).rejects.toThrow('消息');
  await expect(api.sendMessage('1', 'x', 3)).rejects.toThrow('类型');
  await api.readConversation('1');
  expect(mockedRequest).toHaveBeenLastCalledWith({ url: '/mall/promotion/kefu-conversations/1/read', method: 'PUT' });
  await api.pinConversation('1', true);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/promotion/kefu-conversations/1/pinned', method: 'PUT', data: { pinned: true },
  });
  await api.deleteConversation('1');
  expect(mockedRequest).toHaveBeenLastCalledWith({ url: '/mall/promotion/kefu-conversations/1', method: 'DELETE' });
});

it('非法会话或消息状态被拒绝', async () => {
  mockedRequest.mockResolvedValue({ data: [{ ...conversation, adminPinned: 'yes' }] });
  await expect(api.getConversations()).rejects.toThrow('会话');
  mockedRequest.mockResolvedValue({ data: [{ ...message, senderType: 3 }] });
  await expect(api.getMessages('1')).rejects.toThrow('发送人');
  mockedRequest.mockResolvedValue({ data: [{ ...message, contentType: 9 }] });
  await expect(api.getMessages('1')).rejects.toThrow('消息类型');
});
