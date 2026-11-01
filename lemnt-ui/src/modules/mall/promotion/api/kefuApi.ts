import { request } from '@/api/request';
import type { KefuConversation, KefuMessage } from '../types';

const path = '/mall/promotion/kefu-conversations';
function object(v: unknown): Record<string, unknown> {
  if (!v || typeof v !== 'object' || Array.isArray(v)) throw Error('客服响应格式不合法');
  return v as Record<string, unknown>;
}
function id(v: unknown, field = '编号'): string {
  const value = typeof v === 'number' && Number.isSafeInteger(v) ? String(v) : v;
  if (typeof value !== 'string' || !/^[1-9]\d{0,18}$/.test(value) || BigInt(value) > 9223372036854775807n)
    throw Error(`${field}不合法或已丢失精度`);
  return value;
}
function text(v: unknown, field: string): string {
  if (typeof v !== 'string') throw Error(`${field}不合法`);
  return v;
}
function conversation(raw: unknown): KefuConversation {
  const v = object(raw);
  if (typeof v.adminPinned !== 'boolean' || typeof v.adminUnreadMessageCount !== 'number')
    throw Error('会话状态不合法');
  return {
    id: id(v.id, '会话编号'),
    userId: id(v.userId, '会员编号'),
    nickname: v.nickname == null ? null : text(v.nickname, '昵称'),
    avatar: v.avatar == null ? null : text(v.avatar, '头像'),
    lastMessageTime: v.lastMessageTime == null ? null : text(v.lastMessageTime, '最后消息时间'),
    lastMessageContent: v.lastMessageContent == null ? null : text(v.lastMessageContent, '最后消息'),
    lastMessageContentType: v.lastMessageContentType == null ? null : Number(v.lastMessageContentType),
    adminPinned: v.adminPinned,
    adminUnreadMessageCount: v.adminUnreadMessageCount,
  };
}
function message(raw: unknown): KefuMessage {
  const v = object(raw);
  if (v.senderType !== 1 && v.senderType !== 2) throw Error('发送人类型不合法');
  if (v.contentType !== 1 && v.contentType !== 2) throw Error('消息类型不合法');
  if (typeof v.readStatus !== 'boolean' || typeof v.createTime !== 'string')
    throw Error('消息状态不合法');
  return {
    id: id(v.id, '消息编号'),
    conversationId: id(v.conversationId, '会话编号'),
    senderId: id(v.senderId, '发送人编号'),
    senderType: v.senderType,
    contentType: v.contentType,
    content: text(v.content, '消息内容'),
    readStatus: v.readStatus,
    createTime: v.createTime,
  };
}
export async function getConversations() {
  const r = await request<unknown>({ url: path });
  if (!Array.isArray(r.data)) throw Error('客服会话列表不合法');
  return (r.data as unknown[]).map(conversation);
}
export async function getMessages(conversationId: string, limit = 50) {
  if (!Number.isInteger(limit) || limit < 1 || limit > 200) throw Error('消息条数不合法');
  const r = await request<unknown>({ url: `${path}/${id(conversationId)}/messages`, params: { limit } });
  if (!Array.isArray(r.data)) throw Error('客服消息列表不合法');
  return (r.data as unknown[]).map(message);
}
export async function sendMessage(conversationId: string, content: string, contentType = 1) {
  if (contentType !== 1 && contentType !== 2) throw Error('消息类型不合法');
  if (!content.trim() || content.length > 2000) throw Error('消息不能为空且不超过2000字');
  const r = await request<unknown>({
    url: `${path}/${id(conversationId)}/messages`,
    method: 'POST',
    data: { contentType, content: content.trim() },
  });
  return id(r.data, '消息编号');
}
export async function readConversation(conversationId: string) {
  await request<unknown>({ url: `${path}/${id(conversationId)}/read`, method: 'PUT' });
}
export async function pinConversation(conversationId: string, pinned: boolean) {
  if (typeof pinned !== 'boolean') throw Error('置顶状态不合法');
  await request<unknown>({
    url: `${path}/${id(conversationId)}/pinned`,
    method: 'PUT',
    data: { pinned },
  });
}
export async function deleteConversation(conversationId: string) {
  await request<unknown>({ url: `${path}/${id(conversationId)}`, method: 'DELETE' });
}
