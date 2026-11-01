import { useState } from 'react';
import {
  PageContainer,
  ProTable,
  ProFormText,
  ProFormTextArea,
  ProFormSelect,
  ProFormDigit,
  type ProColumns,
} from '@ant-design/pro-components';
import { Alert, App, Button, Descriptions, Image, Popconfirm, Result } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { AdminModalForm } from '@/shared/components/AdminModalForm';
import { usePermission } from '@/shared/hooks/usePermission';
import * as api from '../api/bannerApi';
import type { Banner, BannerFilter, BannerSave } from '../types';

const POSITIONS = {
  1: { text: '首页' },
  2: { text: '秒杀活动页' },
  3: { text: '拼团/砍价页' },
  4: { text: '限时折扣页' },
  5: { text: '满减送页' },
};

/** Banner 按位置管理，点击量只读展示。 */
export function BannerPage() {
  const { can } = usePermission();
  const { message } = App.useApp();
  const cache = useQueryClient();
  const [filters, setFilters] = useState<Pick<BannerFilter, 'title' | 'status'>>({});
  const [page, setPage] = useState({ pageNum: 1, pageSize: 20 });
  const [editor, setEditor] = useState<{ id: string | null; readOnly: boolean }>();
  const rows = useQuery({
    queryKey: ['banner', 'page', filters, page],
    queryFn: () => api.getBannerPage({ ...filters, ...page }),
    enabled: can('mall:promotion:list'),
  });
  const detail = useQuery<Banner>({
    queryKey: ['banner', 'detail', editor?.id],
    queryFn: () => api.getBanner(editor?.id ?? ''),
    enabled: !!editor?.id && can('mall:promotion:query'),
  });
  const save = useMutation({
    mutationFn: async (value: BannerSave) => {
      if (editor?.id) await api.updateBanner(editor.id, value);
      else await api.createBanner(value);
    },
  });
  const remove = useMutation({ mutationFn: (id: string) => api.deleteBanner(id) });
  async function refresh() {
    try {
      await cache.invalidateQueries({ queryKey: ['banner'] }, { throwOnError: true });
    } catch {
      message.warning('操作成功，刷新失败，请重新加载');
    }
  }
  async function destroy(id: string) {
    try {
      await remove.mutateAsync(id);
      message.success('已删除');
      await refresh();
    } catch (error) {
      message.error(String(error));
    }
  }
  const columns: ProColumns<Banner>[] = [
    { title: '编号', dataIndex: 'id', search: false },
    {
      title: '图片',
      dataIndex: 'picUrl',
      search: false,
      render: (_, row) => <Image src={row.picUrl} width={72} height={36} alt={row.title} />,
    },
    {
      title: '标题',
      dataIndex: 'title',
      render: (_, row) => (
        <a href={row.url} target="_blank" rel="noreferrer">
          {row.title}
        </a>
      ),
    },
    {
      title: '位置',
      dataIndex: 'position',
      valueType: 'select',
      valueEnum: POSITIONS,
      search: false,
    },
    { title: '排序', dataIndex: 'sort', search: false },
    { title: '点击量', dataIndex: 'browseCount', search: false },
    {
      title: '状态',
      dataIndex: 'status',
      valueType: 'select',
      valueEnum: { 0: { text: '开启' }, 1: { text: '关闭' } },
    },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) => [
        can('mall:promotion:query') && (
          <Button key="detail" type="link" onClick={() => setEditor({ id: row.id, readOnly: true })}>
            详情
          </Button>
        ),
        can('mall:promotion:update') && (
          <Button key="edit" type="link" onClick={() => setEditor({ id: row.id, readOnly: false })}>
            编辑
          </Button>
        ),
        can('mall:promotion:delete') && (
          <Popconfirm key="delete" title="确认删除该 Banner？" onConfirm={() => destroy(row.id)}>
            <Button type="link" danger disabled={remove.isPending}>
              删除
            </Button>
          </Popconfirm>
        ),
      ],
    },
  ];
  if (!can('mall:promotion:list')) return <Result status="403" title="没有 Banner 列表权限" />;
  const loaded = editor?.id ? detail.data : undefined;
  const initial: BannerSave = loaded
    ? {
        title: loaded.title,
        url: loaded.url,
        picUrl: loaded.picUrl,
        sort: loaded.sort,
        status: loaded.status,
        position: loaded.position,
        memo: loaded.memo,
      }
    : { title: '', url: '', picUrl: '', sort: 0, status: 0, position: 1, memo: null };
  return (
    <PageContainer title="Banner 管理">
      {rows.isError && (
        <Alert
          type="error"
          title="Banner 读取失败"
          description={String(rows.error)}
          action={<Button onClick={() => rows.refetch()}>重试</Button>}
        />
      )}
      <ProTable<Banner>
        options={{ reload: false }}
        rowKey="id"
        scroll={{ x: 1000 }}
        columns={columns}
        dataSource={rows.data?.records ?? []}
        loading={rows.isFetching}
        onSubmit={(value) => {
          setFilters({
            title: typeof value.title === 'string' ? value.title : undefined,
            status:
              value.status === 0 || value.status === '0'
                ? 0
                : value.status === 1 || value.status === '1'
                  ? 1
                  : undefined,
          });
          setPage({ ...page, pageNum: 1 });
        }}
        onReset={() => {
          setFilters({});
          setPage({ ...page, pageNum: 1 });
        }}
        pagination={{
          current: page.pageNum,
          pageSize: page.pageSize,
          total: rows.data?.total ?? 0,
          onChange: (pageNum, pageSize) => setPage({ pageNum, pageSize }),
        }}
        toolBarRender={() => [
          <Button key="refresh" onClick={() => rows.refetch()} loading={rows.isFetching}>
            刷新
          </Button>,
          can('mall:promotion:create') && (
            <Button key="new" type="primary" onClick={() => setEditor({ id: null, readOnly: false })}>
              新增 Banner
            </Button>
          ),
        ]}
      />
      {editor?.id && (detail.isFetching || detail.isError) && (
        <Descriptions
          column={1}
          items={[
            {
              key: 'loading',
              label: '详情',
              children: detail.isError ? `读取失败：${String(detail.error)}` : '正在读取…',
            },
          ]}
        />
      )}
      {editor && (!editor.id || (loaded && !detail.isFetching && !detail.isError)) && (
        <AdminModalForm<BannerSave>
          key={`${editor.id ?? 'new'}-${detail.dataUpdatedAt}`}
          open
          title={`${editor.readOnly ? '查看' : editor.id ? '编辑' : '新增'} Banner`}
          width={720}
          initialValues={initial}
          modalProps={{ destroyOnHidden: true, maskClosable: false, closable: !save.isPending }}
          onOpenChange={(open) => {
            if (!open && !save.isPending) setEditor(undefined);
          }}
          submitter={editor.readOnly ? false : { submitButtonProps: { loading: save.isPending } }}
          onFinish={async (value) => {
            if (!can(editor.id ? 'mall:promotion:update' : 'mall:promotion:create')) {
              message.error('没有 Banner 保存权限');
              return false;
            }
            try {
              await save.mutateAsync(value);
              message.success('已保存');
              setEditor(undefined);
              await refresh();
              return true;
            } catch (error) {
              message.error(String(error));
              return false;
            }
          }}
        >
          {editor.readOnly ? (
            <BannerDetails value={loaded!} />
          ) : (
            <>
              <ProFormText name="title" label="标题" rules={[{ required: true }, { max: 100 }]} />
              <ProFormText name="url" label="跳转链接" rules={[{ required: true }, { max: 500 }]} />
              <ProFormText name="picUrl" label="图片地址" rules={[{ required: true }, { max: 500 }]} />
              <ProFormSelect
                name="position"
                label="位置"
                options={Object.entries(POSITIONS).map(([value, item]) => ({
                  value: Number(value),
                  label: item.text,
                }))}
                rules={[{ required: true }]}
              />
              <ProFormSelect
                name="status"
                label="状态"
                options={[
                  { value: 0, label: '开启' },
                  { value: 1, label: '关闭' },
                ]}
                rules={[{ required: true }]}
              />
              <ProFormDigit
                name="sort"
                label="排序"
                min={0}
                fieldProps={{ precision: 0 }}
                rules={[{ required: true }]}
              />
              <ProFormTextArea name="memo" label="备注" fieldProps={{ maxLength: 255 }} />
            </>
          )}
        </AdminModalForm>
      )}
    </PageContainer>
  );
}

function BannerDetails({ value }: { value: Banner }) {
  return (
    <Descriptions
      column={1}
      items={[
        { key: 'title', label: '标题', children: value.title },
        { key: 'url', label: '跳转链接', children: value.url },
        { key: 'pic', label: '图片', children: <Image src={value.picUrl} width={240} /> },
        { key: 'position', label: '位置', children: POSITIONS[value.position].text },
        { key: 'status', label: '状态', children: value.status === 0 ? '开启' : '关闭' },
        { key: 'sort', label: '排序', children: value.sort },
        { key: 'browse', label: '点击量', children: value.browseCount },
        { key: 'memo', label: '备注', children: value.memo || '—' },
      ]}
    />
  );
}
