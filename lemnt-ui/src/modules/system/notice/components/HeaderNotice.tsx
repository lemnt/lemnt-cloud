import { useRef, useState } from 'react';
import { BellOutlined } from '@ant-design/icons';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  Alert,
  App,
  Badge,
  Button,
  Card,
  Drawer,
  Empty,
  Listy,
  Popover,
  Space,
  Spin,
  Tag,
  Typography,
} from 'antd';
import {
  getNoticeDetail,
  getNoticeTopList,
  markNoticeRead,
  markNoticesRead,
} from '../api/noticeApi';
import { noticeKeys } from '../hooks/noticeKeys';
import { NoticeContent } from './NoticeContent';
import type { Notice } from '../types';
import styles from './headerNotice.module.css';

interface HeaderNoticeProps {
  variant?: 'header' | 'panel';
}

export default function HeaderNotice({ variant = 'header' }: HeaderNoticeProps) {
  const operation = useRef(false);
  const [pendingRead, setPendingRead] = useState<Notice>();
  const [isOpen, setIsOpen] = useState(false);
  const [noticeId, setNoticeId] = useState<number>();
  const [isOpening, setIsOpening] = useState(false);
  const { message } = App.useApp();
  const client = useQueryClient();
  const top = useQuery({ queryKey: noticeKeys.top, queryFn: getNoticeTopList });
  const detail = useQuery({
    queryKey: noticeKeys.detail(noticeId ?? 0),
    queryFn: () => {
      if (noticeId === undefined) throw new Error('未选择通知');
      return getNoticeDetail(noticeId);
    },
    enabled: noticeId !== undefined,
  });
  const read = useMutation({ mutationFn: markNoticeRead });
  const readMany = useMutation({ mutationFn: markNoticesRead });
  const isBusy = isOpening || read.isPending || readMany.isPending;
  const unreadIds = (top.data?.list ?? [])
    .filter((item) => !item.isRead)
    .map((item) => item.noticeId);

  /** 详情加载成功后才标记已读；失败时保留服务端未读状态。 */
  async function handlePreview(item: Notice) {
    if (operation.current || isBusy) return;
    operation.current = true;
    setPendingRead(item.isRead ? undefined : item);
    setIsOpening(true);
    setNoticeId(item.noticeId);
    setIsOpen(false);
    try {
      await client.fetchQuery({
        queryKey: noticeKeys.detail(item.noticeId),
        queryFn: () => getNoticeDetail(item.noticeId),
      });
      if (!item.isRead) {
        if (!(await read.mutateAsync(item.noticeId)).data) throw new Error('标记已读未成功');
        setPendingRead(undefined);
        await client.invalidateQueries({ queryKey: noticeKeys.all });
      }
    } catch (error) {
      message.error(String(error));
    } finally {
      operation.current = false;
      setIsOpening(false);
    }
  }

  async function handleReadList() {
    if (operation.current || isBusy || !unreadIds.length) return;
    operation.current = true;
    try {
      if (!(await readMany.mutateAsync(unreadIds)).data) throw new Error('批量标记已读未成功');
      await client.invalidateQueries({ queryKey: noticeKeys.all });
      message.success('当前列表已标记为已读');
    } catch (error) {
      message.error(String(error));
    } finally {
      operation.current = false;
    }
  }

  const listContent = (
    <div className={variant === 'panel' ? styles.panel : styles.list}>
      {top.isError ? (
        <Alert
          type="error"
          title="通知加载失败"
          action={
            <Button size="small" onClick={() => void top.refetch()}>
              重试
            </Button>
          }
        />
      ) : (
        <Spin spinning={top.isFetching}>
          {top.data?.list.length ? (
            <Listy
              items={top.data.list}
              rowKey="noticeId"
              classNames={{ item: styles.item }}
              itemRender={(item) => (
                <Space orientation="vertical" size={0}>
                  <Space>
                    <Tag color={item.noticeType === '1' ? 'blue' : 'green'}>
                      {item.noticeType === '1' ? '通知' : '公告'}
                    </Tag>
                    <Tag>{item.isRead ? '已读' : '未读'}</Tag>
                  </Space>
                  <Button
                    type="link"
                    className={styles.title}
                    disabled={isBusy}
                    onClick={() => void handlePreview(item)}
                  >
                    {item.noticeTitle}
                  </Button>
                  <Typography.Text type="secondary">{item.createTime}</Typography.Text>
                </Space>
              )}
            />
          ) : (
            <Empty description="暂无通知公告" />
          )}
        </Spin>
      )}
    </div>
  );

  return (
    <>
      {variant === 'panel' ? (
        <Card
          title="通知公告"
          extra={
            <Button
              disabled={!unreadIds.length || isBusy || top.isFetching || top.isError}
              loading={readMany.isPending}
              onClick={handleReadList}
            >
              当前列表全部已读
            </Button>
          }
        >
          <Typography.Paragraph type="secondary">
            {top.isError
              ? '未读数量加载失败'
              : top.isPending
                ? '通知加载中'
                : `最近五条通知 · 共 ${top.data.unreadCount} 条未读`}
          </Typography.Paragraph>
          {listContent}
          <Button loading={top.isFetching} onClick={() => void top.refetch()}>
            刷新通知
          </Button>
        </Card>
      ) : (
        <Popover
          trigger="click"
          placement="bottomRight"
          open={isOpen}
          onOpenChange={(open) => {
            setIsOpen(open);
            if (open) void top.refetch();
          }}
          title={
            <Space>
              <span>通知公告</span>
              <Button
                type="link"
                size="small"
                disabled={!unreadIds.length || isBusy || top.isFetching || top.isError}
                loading={readMany.isPending}
                onClick={handleReadList}
              >
                当前列表全部已读
              </Button>
            </Space>
          }
          content={listContent}
        >
          <Button
            type="text"
            aria-label={
              top.isError ? '通知加载失败' : `通知公告，${top.data?.unreadCount ?? 0} 条未读`
            }
            loading={isOpening}
            icon={
              <Badge count={top.data?.unreadCount ?? 0} dot={top.isError}>
                <BellOutlined />
              </Badge>
            }
          />
        </Popover>
      )}
      <Drawer
        open={noticeId !== undefined}
        title={detail.data?.noticeTitle ?? '通知详情'}
        size={720}
        onClose={() => {
          setNoticeId(undefined);
          setPendingRead(undefined);
        }}
      >
        {detail.isPending ? (
          <Spin />
        ) : detail.isError ? (
          <Alert
            type="error"
            title="详情加载失败"
            action={
              <Button
                disabled={isBusy}
                onClick={() =>
                  pendingRead ? void handlePreview(pendingRead) : void detail.refetch()
                }
              >
                重试
              </Button>
            }
          />
        ) : (
          <>
            {pendingRead && !isOpening && (
              <Alert
                type="warning"
                title="此通知尚未成功标记为已读"
                action={
                  <Button
                    loading={read.isPending}
                    disabled={isBusy}
                    onClick={() => void handlePreview(pendingRead)}
                  >
                    重试标记已读
                  </Button>
                }
              />
            )}
            <Typography.Paragraph type="secondary">
              {detail.data.createBy} · {detail.data.createTime}
            </Typography.Paragraph>
            <NoticeContent html={detail.data.noticeContent} isVisible={noticeId !== undefined} />
          </>
        )}
      </Drawer>
    </>
  );
}
