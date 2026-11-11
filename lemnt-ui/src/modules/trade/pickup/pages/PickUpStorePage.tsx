import { useState } from 'react';
import {
  PageContainer,
  ProTable,
  ProFormText,
  ProFormTextArea,
  ProFormSelect,
  type ProColumns,
} from '@ant-design/pro-components';
import { Alert, App, Button, Card, Input, Popconfirm, Result, Space } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { AdminModalForm } from '@/shared/components/AdminModalForm';
import { usePermission } from '@/shared/hooks/usePermission';
import * as api from '../api/pickUpApi';
import type { PickUpOrder, PickUpStore, PickUpStoreSave } from '../api/pickUpApi';

interface StoreForm {
  name: string;
  introduction?: string | null;
  phone: string;
  areaId: string;
  detailAddress: string;
  logo?: string | null;
  openingTime: string;
  closingTime: string;
  latitude?: string | null;
  longitude?: string | null;
  verifyUserIds: string;
  status: 0 | 1;
}

/** 自提门店、店员绑定与核销工作台。 */
export function PickUpStorePage() {
  const { can } = usePermission();
  const { message } = App.useApp();
  const cache = useQueryClient();
  const [filter, setFilter] = useState<{ name?: string; status?: number }>({});
  const [page, setPage] = useState({ pageNum: 1, pageSize: 20 });
  const [editor, setEditor] = useState<PickUpStore>();
  const [creating, setCreating] = useState(false);
  const [bind, setBind] = useState<{ id: string; value: string }>();
  const [code, setCode] = useState('');
  const [preview, setPreview] = useState<PickUpOrder>();
  const [assign, setAssign] = useState({ orderNo: '', storeId: '' });
  const stores = useQuery({
    queryKey: ['pickup', 'stores', filter, page],
    queryFn: () => api.getStores({ ...filter, ...page }),
    enabled: can('trade:pick-up-store:list'),
  });
  const save = useMutation({
    mutationFn: async (value: StoreForm) => {
      const data: PickUpStoreSave = {
        ...value,
        verifyUserIds: value.verifyUserIds
          .split(',')
          .map((item) => item.trim())
          .filter(Boolean),
      };
      if (editor) await api.updateStore(editor.id, data);
      else await api.createStore(data);
    },
  });
  const bindMutation = useMutation({
    mutationFn: () =>
      api.bindStore(
        bind!.id,
        bind!.value
          .split(',')
          .map((item) => item.trim())
          .filter(Boolean),
      ),
  });
  const remove = useMutation({ mutationFn: api.deleteStore });
  const previewMutation = useMutation({ mutationFn: () => api.previewPickUp(code) });
  const verifyMutation = useMutation({ mutationFn: () => api.verifyPickUp(code) });
  const assignMutation = useMutation({
    mutationFn: () => api.assignPickUp(assign.orderNo.trim(), assign.storeId.trim()),
  });
  async function refresh() {
    await cache.invalidateQueries({ queryKey: ['pickup', 'stores'] });
  }
  if (!can('trade:pick-up-store:list')) {
    return (
      <PageContainer title="自提门店">
        <Result status="403" title="没有自提门店权限" />
      </PageContainer>
    );
  }
  const columns: ProColumns<PickUpStore>[] = [
    { title: '编号', dataIndex: 'id', search: false },
    { title: '门店名称', dataIndex: 'name' },
    { title: '电话', dataIndex: 'phone', search: false },
    { title: '地址', dataIndex: 'detailAddress', search: false },
    {
      title: '营业时间',
      dataIndex: 'openingTime',
      search: false,
      render: (_, row) => `${row.openingTime} - ${row.closingTime}`,
    },
    {
      title: '核销店员',
      dataIndex: 'verifyUserIds',
      search: false,
      render: (_, row) => row.verifyUserIds.join('、') || '未绑定',
    },
    {
      title: '状态',
      dataIndex: 'status',
      valueType: 'select',
      valueEnum: { 0: '开启', 1: '关闭' },
    },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) => [
        can('trade:pick-up-store:update') && (
          <Button key="edit" type="link" onClick={() => setEditor(row)}>
            编辑
          </Button>
        ),
        can('trade:pick-up-store:update') && (
          <Button
            key="bind"
            type="link"
            onClick={() => setBind({ id: row.id, value: row.verifyUserIds.join(',') })}
          >
            绑定店员
          </Button>
        ),
        can('trade:pick-up-store:delete') && (
          <Popconfirm
            key="delete"
            title="确认删除门店？"
            onConfirm={async () => {
              try {
                await remove.mutateAsync(row.id);
                message.success('已删除');
                await refresh();
              } catch (error) {
                message.error(String(error));
              }
            }}
          >
            <Button type="link" danger>
              删除
            </Button>
          </Popconfirm>
        ),
      ],
    },
  ];
  const initial: StoreForm = editor
    ? {
        ...editor,
        verifyUserIds: editor.verifyUserIds.join(','),
        latitude: editor.latitude,
        longitude: editor.longitude,
      }
    : {
        name: '',
        introduction: null,
        phone: '',
        areaId: '',
        detailAddress: '',
        logo: null,
        openingTime: '09:00:00',
        closingTime: '21:00:00',
        latitude: null,
        longitude: null,
        verifyUserIds: '',
        status: 0,
      };
  return (
    <PageContainer title="自提门店">
      {stores.isError && (
        <Alert type="error" title="门店读取失败" description={String(stores.error)} />
      )}
      <ProTable<PickUpStore>
        rowKey="id"
        options={{ reload: false }}
        columns={columns}
        dataSource={stores.data?.records ?? []}
        loading={stores.isFetching}
        onSubmit={(value) => {
          setFilter({
            name: typeof value.name === 'string' ? value.name : undefined,
            status: typeof value.status === 'number' ? value.status : undefined,
          });
          setPage({ ...page, pageNum: 1 });
        }}
        onReset={() => {
          setFilter({});
          setPage({ ...page, pageNum: 1 });
        }}
        pagination={{
          current: page.pageNum,
          pageSize: page.pageSize,
          total: stores.data?.total ?? 0,
          onChange: (pageNum, pageSize) => setPage({ pageNum, pageSize }),
        }}
        toolBarRender={() => [
          can('trade:pick-up-store:create') && (
            <Button
              key="new"
              type="primary"
              onClick={() => {
                setEditor(undefined);
                setCreating(true);
              }}
            >
              新增门店
            </Button>
          ),
          <Button key="refresh" onClick={() => stores.refetch()} loading={stores.isFetching}>
            刷新
          </Button>,
        ]}
      />
      {(creating || editor) && (
        <AdminModalForm<StoreForm>
          open
          title={editor ? '编辑门店' : '新增门店'}
          initialValues={initial}
          modalProps={{ destroyOnHidden: true }}
          onOpenChange={(open) => {
            if (!open) {
              setCreating(false);
              setEditor(undefined);
            }
          }}
          submitter={{ submitButtonProps: { loading: save.isPending } }}
          onFinish={async (value) => {
            try {
              await save.mutateAsync(value);
              message.success('已保存');
              setCreating(false);
              setEditor(undefined);
              await refresh();
              return true;
            } catch (error) {
              message.error(String(error));
              return false;
            }
          }}
        >
          <ProFormText name="name" label="门店名称" rules={[{ required: true }, { max: 100 }]} />
          <ProFormText name="phone" label="电话" rules={[{ required: true }, { max: 32 }]} />
          <ProFormText name="areaId" label="地区编号" rules={[{ required: true }]} />
          <ProFormText
            name="detailAddress"
            label="详细地址"
            rules={[{ required: true }, { max: 255 }]}
          />
          <ProFormText name="openingTime" label="开门时间 HH:mm:ss" rules={[{ required: true }]} />
          <ProFormText name="closingTime" label="关门时间 HH:mm:ss" rules={[{ required: true }]} />
          <ProFormTextArea name="introduction" label="简介" fieldProps={{ maxLength: 500 }} />
          <ProFormText name="logo" label="logo 地址" />
          <ProFormText name="latitude" label="纬度" />
          <ProFormText name="longitude" label="经度" />
          <ProFormText name="verifyUserIds" label="核销管理员编号（逗号分隔）" />
          <ProFormSelect
            name="status"
            label="状态"
            options={[
              { value: 0, label: '开启' },
              { value: 1, label: '关闭' },
            ]}
          />
        </AdminModalForm>
      )}
      <Card title="自提核销" style={{ marginTop: 16 }}>
        <Space direction="vertical" style={{ width: '100%' }}>
          <Space>
            <Input
              value={code}
              onChange={(event) => setCode(event.target.value)}
              placeholder="8位核销码"
              maxLength={8}
            />
            <Button
              onClick={async () => {
                try {
                  setPreview(await previewMutation.mutateAsync());
                } catch (error) {
                  message.error(String(error));
                }
              }}
              loading={previewMutation.isPending}
            >
              查询
            </Button>
            <Button
              type="primary"
              disabled={!can('trade:fulfillment:update')}
              onClick={async () => {
                try {
                  const value = await verifyMutation.mutateAsync();
                  setPreview(value);
                  message.success('核销成功');
                } catch (error) {
                  message.error(String(error));
                }
              }}
              loading={verifyMutation.isPending}
            >
              确认核销
            </Button>
          </Space>
          {preview && (
            <Alert
              type={preview.status === 'COMPLETED' ? 'success' : 'info'}
              title={`订单 ${preview.orderNo} · ${preview.status}`}
              description={`${preview.storeName || ''} ${preview.storeAddress || ''}`}
            />
          )}
        </Space>
      </Card>
      <Card title="设置自提订单" style={{ marginTop: 16 }}>
        <Space>
          <Input
            value={assign.orderNo}
            onChange={(event) => setAssign({ ...assign, orderNo: event.target.value })}
            placeholder="订单编号"
          />
          <Input
            value={assign.storeId}
            onChange={(event) => setAssign({ ...assign, storeId: event.target.value })}
            placeholder="门店编号"
          />
          <Button
            type="primary"
            disabled={!can('trade:fulfillment:update')}
            onClick={async () => {
              try {
                await assignMutation.mutateAsync();
                message.success('已设置为自提');
              } catch (error) {
                message.error(String(error));
              }
            }}
            loading={assignMutation.isPending}
          >
            设置自提
          </Button>
        </Space>
      </Card>
      <AdminModalForm<{ verifyUserIds: string }>
        open={!!bind}
        title="绑定核销店员"
        initialValues={{ verifyUserIds: bind?.value ?? '' }}
        modalProps={{ destroyOnHidden: true }}
        onOpenChange={(open) => {
          if (!open) setBind(undefined);
        }}
        submitter={{ submitButtonProps: { loading: bindMutation.isPending } }}
        onFinish={async () => {
          try {
            await bindMutation.mutateAsync();
            message.success('已绑定');
            setBind(undefined);
            await refresh();
            return true;
          } catch (error) {
            message.error(String(error));
            return false;
          }
        }}
      >
        <ProFormText name="verifyUserIds" label="管理员编号（逗号分隔）" />
      </AdminModalForm>
    </PageContainer>
  );
}
