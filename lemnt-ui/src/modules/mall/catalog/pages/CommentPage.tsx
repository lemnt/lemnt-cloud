import { useRef, useState } from 'react';
import {
  PageContainer,
  ProTable,
  ProFormTextArea,
  ProDescriptions,
  type ActionType,
  type ProColumns,
} from '@ant-design/pro-components';
import { Alert, App, Button, Drawer, Image, Popconfirm, Result } from 'antd';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { AdminModalForm } from '@/shared/components/AdminModalForm';
import { usePermission } from '@/shared/hooks/usePermission';
import * as api from '../api/commentApi';
import type { ProductCommentRecord, ProductCommentQuery } from '../commentTypes';
function filterFlag(value: unknown) {
  return value === true || value === 'true'
    ? true
    : value === false || value === 'false'
      ? false
      : undefined;
}
function safeImage(url: string | null) {
  return url && /^https?:\/\//.test(url) ? url : undefined;
}
export default function CommentPage() {
  const { can } = usePermission();
  const { message } = App.useApp();
  const client = useQueryClient();
  const action = useRef<ActionType | undefined>(undefined);
  const operation = useRef(false);
  const [error, setError] = useState('');
  const [detail, setDetail] = useState<ProductCommentRecord>();
  const [replying, setReplying] = useState<ProductCommentRecord>();
  const [isOpening, setIsOpening] = useState(false);
  const reply = useMutation({
    mutationFn: ({ id, content }: { id: string; content: string }) => api.replyComment(id, content),
  });
  const visible = useMutation({
    mutationFn: ({ id, value }: { id: string; value: boolean }) => api.setCommentVisible(id, value),
  });
  const busy = isOpening || reply.isPending || visible.isPending;
  async function refresh() {
    await client.invalidateQueries({ queryKey: ['mall-comment'] });
    action.current?.reload();
  }
  async function show(row: ProductCommentRecord, replyMode = false) {
    if (operation.current) return;
    operation.current = true;
    setIsOpening(true);
    try {
      const value = await client.fetchQuery({
        queryKey: ['mall-comment', 'detail', row.id],
        queryFn: () => api.getComment(row.id),
      });
      if (replyMode) setReplying(value);
      else setDetail(value);
    } catch (reason) {
      message.error(String(reason));
    } finally {
      setIsOpening(false);
      operation.current = false;
    }
  }
  const columns: ProColumns<ProductCommentRecord>[] = [
    { title: '评价编号', dataIndex: 'id', search: false },
    { title: 'SPU编号', dataIndex: 'spuId' },
    { title: '会员编号', dataIndex: 'memberId' },
    { title: '订单ID', dataIndex: 'orderId' },
    {
      title: '商品快照',
      dataIndex: 'spuName',
      renderText: (value) => value ?? '未记录',
    },
    {
      title: '会员昵称快照',
      dataIndex: 'userNickname',
      renderText: (value) => value ?? '未记录',
    },
    {
      title: '综合评分',
      dataIndex: 'score',
      valueType: 'select',
      valueEnum: {
        1: { text: '1分' },
        2: { text: '2分' },
        3: { text: '3分' },
        4: { text: '4分' },
        5: { text: '5分' },
      },
    },
    {
      title: '描述/服务',
      search: false,
      render: (_, row) => `${row.descriptionScores ?? '未记录'} / ${row.benefitScores ?? '未记录'}`,
    },
    { title: '评价内容', dataIndex: 'content', search: false, ellipsis: true },
    {
      title: '可见',
      dataIndex: 'visible',
      valueType: 'select',
      valueEnum: { true: { text: '显示' }, false: { text: '隐藏' } },
    },
    {
      title: '已回复',
      dataIndex: 'replied',
      valueType: 'select',
      valueEnum: { true: { text: '是' }, false: { text: '否' } },
      render: (_, row) => (row.replyStatus ? '是' : '否'),
    },
    { title: '评价时间', dataIndex: 'createTime', search: false },
    {
      title: '评价时间范围',
      dataIndex: 'createTimeRange',
      hideInTable: true,
      valueType: 'dateTimeRange',
      search: {
        transform: (value: string[]) => ({
          createTimeStart: value[0]?.replace(' ', 'T'),
          createTimeEnd: value[1]?.replace(' ', 'T'),
        }),
      },
    },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) => [
        can('mall:comment:query') ? (
          <Button key="detail" type="link" disabled={busy} onClick={() => show(row)}>
            详情
          </Button>
        ) : null,
        can('mall:comment:query') && can('mall:comment:reply') ? (
          <Button key="reply" type="link" disabled={busy} onClick={() => show(row, true)}>
            回复
          </Button>
        ) : null,
        can('mall:comment:edit') ? (
          <Popconfirm
            key="visible"
            title={row.visible ? '隐藏此评价？' : '显示此评价？'}
            onConfirm={async () => {
              if (operation.current) return;
              operation.current = true;
              try {
                await visible.mutateAsync({ id: row.id, value: !row.visible });
                message.success('显隐已修改');
                try {
                  await refresh();
                } catch (reason) {
                  message.warning('修改已成功，刷新失败：' + String(reason));
                }
              } catch (reason) {
                message.error(String(reason));
              } finally {
                operation.current = false;
              }
            }}
          >
            <Button type="link" disabled={busy}>
              {row.visible ? '隐藏' : '显示'}
            </Button>
          </Popconfirm>
        ) : null,
      ],
    },
  ];
  if (!can('mall:comment:list')) return <Result status="403" title="无商品评价查询权限" />;
  return (
    <PageContainer title="商品评价">
      {error ? <Alert type="error" showIcon message={error} /> : null}
      <ProTable<ProductCommentRecord>
        columns={columns}
        actionRef={action}
        rowKey="id"
        scroll={{ x: 1400 }}
        pagination={{ defaultPageSize: 20, showSizeChanger: true, pageSizeOptions: [20, 50, 100] }}
        request={async (params) => {
          const query: ProductCommentQuery = {
            userNickname: params.userNickname?.trim() || undefined,
            orderId: params.orderId || undefined,
            spuName: params.spuName?.trim() || undefined,
            createTimeStart: params.createTimeStart,
            createTimeEnd: params.createTimeEnd,
            spuId: params.spuId || undefined,
            memberId: params.memberId || undefined,
            score:
              params.score === undefined || params.score === '' ? undefined : Number(params.score),
            visible: filterFlag(params.visible),
            replied: filterFlag(params.replied),
            pageNum: params.current ?? 1,
            pageSize: params.pageSize ?? 20,
          };
          try {
            const result = await client.fetchQuery({
              queryKey: ['mall-comment', 'page', query],
              queryFn: () => api.getComments(query),
            });
            setError('');
            return result;
          } catch (reason) {
            setError(String(reason));
            return { data: [], total: 0, success: false };
          }
        }}
      />
      <Drawer title="评价详情" open={!!detail} onClose={() => setDetail(undefined)} width={760}>
        {detail ? (
          <>
            <ProDescriptions
              dataSource={detail}
              column={2}
              columns={[
                { title: '订单号', dataIndex: 'orderNo' },
                { title: '订单项编号', dataIndex: 'orderItemId' },
                { title: '会员编号', dataIndex: 'memberId' },
                { title: '昵称快照', dataIndex: 'userNickname' },
                {
                  title: '会员头像',
                  render: () =>
                    safeImage(detail.userAvatar) ? (
                      <Image src={safeImage(detail.userAvatar)} width={80} />
                    ) : (
                      '未记录'
                    ),
                },
                {
                  title: '原商品图片',
                  render: () =>
                    safeImage(detail.skuPicUrl) ? (
                      <Image src={safeImage(detail.skuPicUrl)} width={100} />
                    ) : (
                      '未记录'
                    ),
                },
                {
                  title: '是否匿名',
                  render: () =>
                    detail.anonymous == null ? '未记录' : detail.anonymous ? '是' : '否',
                },
                { title: '原商品名称', dataIndex: 'spuName' },
                { title: '综合评分', dataIndex: 'score' },
                { title: '描述评分', dataIndex: 'descriptionScores' },
                { title: '服务评分', dataIndex: 'benefitScores' },
                {
                  title: '规格快照',
                  render: () =>
                    detail.skuProperties
                      ?.map((v) => v.propertyName + '：' + v.valueName)
                      .join(' / ') ?? '未记录',
                },
                { title: '评价时间', dataIndex: 'createTime' },
                { title: '内容', dataIndex: 'content', span: 2 },
                { title: '回复内容', dataIndex: 'replyContent', span: 2 },
                { title: '回复人编号', dataIndex: 'replyUserId' },
                { title: '回复时间', dataIndex: 'replyTime' },
              ]}
            />
            <Image.PreviewGroup>
              {detail.picUrls
                ?.filter((url) => safeImage(url))
                .map((url) => (
                  <Image key={url} src={url} width={120} />
                ))}
            </Image.PreviewGroup>
          </>
        ) : null}
      </Drawer>
      <AdminModalForm<{ content: string }>
        key={replying?.id ?? 'reply'}
        title="回复商品评价"
        open={!!replying}
        onOpenChange={(open) => {
          if (!open) setReplying(undefined);
        }}
        initialValues={{ content: replying?.replyContent ?? '' }}
        loading={reply.isPending}
        modalProps={{ destroyOnHidden: true, maskClosable: false }}
        onFinish={async (values) => {
          if (operation.current || !replying) return false;
          operation.current = true;
          try {
            const saved = await reply.mutateAsync({ id: replying.id, content: values.content });
            if (!saved.replyStatus || saved.id !== replying.id) throw Error('回复结果未确认');
            setReplying(undefined);
            message.success('回复已保存');
            try {
              await refresh();
            } catch (reason) {
              message.warning('回复已成功，刷新失败：' + String(reason));
            }
            return true;
          } catch (reason) {
            message.error(String(reason));
            return false;
          } finally {
            operation.current = false;
          }
        }}
      >
        <p>{replying?.content}</p>
        <ProFormTextArea
          name="content"
          label="回复内容"
          rules={[{ required: true, whitespace: true }, { max: 1000 }]}
        />
      </AdminModalForm>
    </PageContainer>
  );
}
