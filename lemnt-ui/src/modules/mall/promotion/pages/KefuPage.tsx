import { useState } from 'react';
import { PageContainer } from '@ant-design/pro-components';
import { Alert, App, Avatar, Badge, Button, Card, Empty, Input, List, Space, Tag } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { usePermission } from '@/shared/hooks/usePermission';
import * as api from '../api/kefuApi';
import type { KefuConversation } from '../types';

/** 客服工作台：左侧会话，右侧消息与回复。 */
export function KefuPage() {
  const { can } = usePermission();
  const { message } = App.useApp();
  const cache = useQueryClient();
  const [selected, setSelected] = useState<string>();
  const [content, setContent] = useState('');
  const conversations = useQuery({
    queryKey: ['kefu', 'conversations'],
    queryFn: api.getConversations,
    enabled: can('mall:promotion:list'),
  });
  const messages = useQuery({
    queryKey: ['kefu', 'messages', selected],
    queryFn: () => api.getMessages(selected!, 50),
    enabled: !!selected && can('mall:promotion:query'),
  });
  const send = useMutation({
    mutationFn: (value: string) => api.sendMessage(selected!, value),
  });
  const read = useMutation({ mutationFn: (id: string) => api.readConversation(id) });
  const pin = useMutation({
    mutationFn: ({ id, pinned }: { id: string; pinned: boolean }) =>
      api.pinConversation(id, pinned),
  });
  const remove = useMutation({ mutationFn: (id: string) => api.deleteConversation(id) });
  const current = conversations.data?.find((row) => row.id === selected);

  async function open(row: KefuConversation) {
    setSelected(row.id);
    setContent('');
    if (can('mall:promotion:update') && row.adminUnreadMessageCount > 0) {
      try {
        await read.mutateAsync(row.id);
        await cache.invalidateQueries({ queryKey: ['kefu', 'conversations'] });
      } catch (error) {
        message.error(String(error));
      }
    }
  }
  async function submit() {
    if (!selected) return;
    if (!can('mall:promotion:create')) {
      message.error('没有客服回复权限');
      return;
    }
    try {
      await send.mutateAsync(content);
      setContent('');
      await cache.invalidateQueries({ queryKey: ['kefu', 'messages', selected] });
      await cache.invalidateQueries({ queryKey: ['kefu', 'conversations'] });
    } catch (error) {
      message.error(String(error));
    }
  }
  async function togglePin(row: KefuConversation) {
    try {
      await pin.mutateAsync({ id: row.id, pinned: !row.adminPinned });
      await cache.invalidateQueries({ queryKey: ['kefu', 'conversations'] });
    } catch (error) {
      message.error(String(error));
    }
  }
  async function destroy(row: KefuConversation) {
    try {
      await remove.mutateAsync(row.id);
      if (selected === row.id) setSelected(undefined);
      await cache.invalidateQueries({ queryKey: ['kefu', 'conversations'] });
    } catch (error) {
      message.error(String(error));
    }
  }
  if (!can('mall:promotion:list')) {
    return (
      <PageContainer title="客服管理">
        <Alert type="error" title="没有客服会话列表权限" />
      </PageContainer>
    );
  }
  return (
    <PageContainer
      title="客服管理"
      extra={[
        <Button
          key="refresh"
          onClick={() => {
            conversations.refetch();
            if (selected) messages.refetch();
          }}
          loading={conversations.isFetching || messages.isFetching}
        >
          刷新
        </Button>,
      ]}
    >
      {conversations.isError && (
        <Alert type="error" title="会话读取失败" description={String(conversations.error)} />
      )}
      <div style={{ display: 'flex', gap: 16, alignItems: 'flex-start' }}>
        <Card
          title={`会话（${conversations.data?.length ?? 0}）`}
          style={{ width: 340, flex: 'none' }}
        >
          <List
            loading={conversations.isFetching}
            dataSource={conversations.data ?? []}
            locale={{ emptyText: '暂无客服会话' }}
            renderItem={(row) => (
              <List.Item
                style={{
                  cursor: 'pointer',
                  background: row.id === selected ? '#f5f5f5' : undefined,
                }}
                onClick={() => open(row)}
                actions={[
                  can('mall:promotion:update') && (
                    <Button
                      key="pin"
                      type="link"
                      size="small"
                      onClick={(event) => {
                        event.stopPropagation();
                        togglePin(row);
                      }}
                    >
                      {row.adminPinned ? '取消置顶' : '置顶'}
                    </Button>
                  ),
                  can('mall:promotion:delete') && (
                    <Button
                      key="delete"
                      type="link"
                      size="small"
                      danger
                      onClick={(event) => {
                        event.stopPropagation();
                        destroy(row);
                      }}
                    >
                      删除
                    </Button>
                  ),
                ].filter(Boolean)}
              >
                <List.Item.Meta
                  avatar={
                    <Avatar src={row.avatar || undefined}>
                      {row.nickname?.slice(0, 1) || '客'}
                    </Avatar>
                  }
                  title={
                    <Space>
                      {row.nickname || `会员 ${row.userId}`}
                      {row.adminPinned && <Tag color="gold">置顶</Tag>}
                      {row.adminUnreadMessageCount > 0 && (
                        <Badge count={row.adminUnreadMessageCount} />
                      )}
                    </Space>
                  }
                  description={
                    <span>
                      {row.lastMessageContent || '暂无消息'}
                      <br />
                      {row.lastMessageTime || ''}
                    </span>
                  }
                />
              </List.Item>
            )}
          />
        </Card>
        <Card
          title={current ? current.nickname || `会员 ${current.userId}` : '请选择会话'}
          style={{ flex: 1, minHeight: 520 }}
        >
          {!current && <Empty description="请选择左侧会话" />}
          {current && (
            <>
              {messages.isError && (
                <Alert type="error" title="消息读取失败" description={String(messages.error)} />
              )}
              <div style={{ height: 380, overflow: 'auto', padding: 8 }}>
                {(messages.data ?? []).map((row) => (
                  <div
                    key={row.id}
                    style={{ textAlign: row.senderType === 2 ? 'right' : 'left', marginBottom: 12 }}
                  >
                    <div
                      style={{
                        display: 'inline-block',
                        maxWidth: '70%',
                        padding: '8px 12px',
                        borderRadius: 8,
                        background: row.senderType === 2 ? '#1677ff' : '#f0f0f0',
                        color: row.senderType === 2 ? '#fff' : undefined,
                        wordBreak: 'break-word',
                      }}
                    >
                      {row.contentType === 2 ? (
                        <a
                          href={row.content}
                          target="_blank"
                          rel="noreferrer"
                          style={{ color: 'inherit' }}
                        >
                          {row.content}
                        </a>
                      ) : (
                        row.content
                      )}
                    </div>
                    <div style={{ fontSize: 12, color: '#999', marginTop: 4 }}>
                      {row.senderType === 2 ? '客服' : current.nickname || '会员'} ·{' '}
                      {row.createTime}
                    </div>
                  </div>
                ))}
                {!messages.isFetching && !(messages.data ?? []).length && (
                  <Empty description="暂无消息" />
                )}
              </div>
              <Input.TextArea
                value={content}
                onChange={(event) => setContent(event.target.value)}
                rows={3}
                maxLength={2000}
                placeholder="输入回复内容"
                disabled={!can('mall:promotion:create') || send.isPending}
              />
              <Button
                type="primary"
                style={{ marginTop: 8 }}
                onClick={submit}
                loading={send.isPending}
                disabled={!content.trim() || !can('mall:promotion:create')}
              >
                回复
              </Button>
            </>
          )}
        </Card>
      </div>
    </PageContainer>
  );
}
