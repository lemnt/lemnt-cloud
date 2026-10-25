import { useState } from 'react';
import {
  PageContainer,
  ProForm,
  ProFormDigit,
  ProFormSwitch,
  ProFormSelect,
  ProTable,
} from '@ant-design/pro-components';
import { Alert, App, Button, Popconfirm, Result } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { AdminModalForm } from '@/shared/components/AdminModalForm';
import { usePermission } from '@/shared/hooks/usePermission';
import * as api from '../api/memberApi';
import { memberKeys, memberStatusOptions } from '../types';
import type { MemberPointConfig, MemberSignConfig } from '../types';
export default function MemberConfigPage() {
  const { can } = usePermission();
  const { message } = App.useApp();
  const cache = useQueryClient();
  const [editing, setEditing] = useState<MemberSignConfig | null>();
  const config = useQuery({
    queryKey: memberKeys.config,
    queryFn: api.getPointConfig,
    enabled: can('member:config:query'),
  });
  const signs = useQuery({
    queryKey: memberKeys.signs,
    queryFn: api.getSignConfigs,
    enabled: can('member:sign-in-config:query'),
  });
  const save = useMutation({ mutationFn: api.savePointConfig });
  const signSave = useMutation({ mutationFn: api.saveSignConfig });
  const remove = useMutation({ mutationFn: api.deleteSignConfig });
  async function refresh() {
    try {
      await cache.invalidateQueries({ queryKey: memberKeys.all }, { throwOnError: true });
    } catch {
      message.warning('已保存，刷新失败请重试读取');
    }
  }
  if (!can('member:config:query') && !can('member:sign-in-config:query'))
    return <Result status="403" title="没有会员配置查询权限" />;
  return (
    <PageContainer title="会员配置">
      {can('member:config:query') && (
        <>
          <h2>积分交易配置</h2>
          <Alert type="warning" title="此处保存会员配置；订单积分抵扣与赠送尚未接入交易流程。" />
          {config.isError && (
            <Alert
              type="error"
              title="配置读取失败"
              action={<Button onClick={() => config.refetch()}>重试</Button>}
            />
          )}
          {config.data && (
            <ProForm<MemberPointConfig>
              key={JSON.stringify(config.data)}
              initialValues={config.data}
              disabled={!can('member:config:update') || save.isPending}
              submitter={can('member:config:update') ? undefined : false}
              onFinish={async (v) => {
                if (save.isPending || !can('member:config:update')) return false;
                try {
                  await save.mutateAsync(v);
                  message.success('配置已保存');
                  await refresh();
                  return true;
                } catch (e) {
                  message.error(String(e));
                  return false;
                }
              }}
            >
              <ProFormSwitch name="pointTradeDeductEnable" label="积分抵扣开关" />
              {(
                [
                  { name: 'pointTradeDeductUnitPrice', label: '积分抵扣单位价格（分）' },
                  { name: 'pointTradeDeductMaxPrice', label: '抵扣上限（分）' },
                  { name: 'pointTradeGivePoint', label: '每元赠送积分' },
                ] as const
              ).map((v) => (
                <ProFormDigit
                  key={v.name}
                  name={v.name}
                  label={v.label}
                  min={0}
                  max={2147483647}
                  fieldProps={{ precision: 0 }}
                  rules={[{ required: true }]}
                />
              ))}
            </ProForm>
          )}
        </>
      )}
      {can('member:sign-in-config:query') && (
        <>
          <h2>签到规则</h2>
          {signs.isError && (
            <Alert
              type="error"
              title="规则读取失败"
              action={<Button onClick={() => signs.refetch()}>重试</Button>}
            />
          )}
          <ProTable<MemberSignConfig>
            rowKey="day"
            search={false}
            dataSource={signs.data ?? []}
            loading={signs.isFetching}
            pagination={false}
            columns={[
              { title: '周期天数', dataIndex: 'day' },
              { title: '积分', dataIndex: 'point' },
              { title: '经验', dataIndex: 'experience' },
              { title: '状态', render: (_, v) => (v.status === 1 ? '启用' : '停用') },
              {
                title: '操作',
                render: (_, v) => (
                  <>
                    {can('member:sign-in-config:update') && (
                      <Button type="link" onClick={() => setEditing(v)}>
                        编辑
                      </Button>
                    )}
                    {can('member:sign-in-config:delete') && (
                      <Popconfirm
                        title="确认删除规则？历史奖励保留。"
                        onConfirm={async () => {
                          try {
                            await remove.mutateAsync(v.day);
                            message.success('已删除');
                            await refresh();
                          } catch (e) {
                            message.error(String(e));
                          }
                        }}
                      >
                        <Button type="link" danger disabled={remove.isPending}>
                          删除
                        </Button>
                      </Popconfirm>
                    )}
                  </>
                ),
              },
            ]}
            toolBarRender={() => [
              can('member:sign-in-config:update') && (
                <Button key="add" type="primary" onClick={() => setEditing(null)}>
                  新增规则
                </Button>
              ),
            ]}
          />
        </>
      )}
      {editing !== undefined && (
        <AdminModalForm<MemberSignConfig>
          key={editing?.day ?? 'new'}
          title="签到规则"
          open
          onOpenChange={(v) => {
            if (!v && !signSave.isPending) setEditing(undefined);
          }}
          initialValues={editing ?? { day: 1, point: 0, experience: 0, status: 1 }}
          modalProps={{ maskClosable: false, closable: !signSave.isPending }}
          submitter={{
            submitButtonProps: { loading: signSave.isPending },
            resetButtonProps: { disabled: signSave.isPending },
          }}
          onFinish={async (v) => {
            if (signSave.isPending || !can('member:sign-in-config:update')) return false;
            try {
              await signSave.mutateAsync(v);
              message.success('规则已保存');
              setEditing(undefined);
              await refresh();
              return true;
            } catch (e) {
              message.error(String(e));
              return false;
            }
          }}
        >
          <Alert type="info" title="相同天数保存会覆盖该天规则；周期为启用规则的最大天数。" />
          <ProFormDigit
            name="day"
            label="周期天数"
            min={1}
            max={366}
            disabled={!!editing}
            fieldProps={{ precision: 0 }}
            rules={[{ required: true }]}
          />
          <ProFormDigit
            name="point"
            label="奖励积分"
            min={0}
            max={Number.MAX_SAFE_INTEGER}
            fieldProps={{ precision: 0 }}
            rules={[{ required: true }]}
          />
          <ProFormDigit
            name="experience"
            label="奖励经验"
            min={0}
            max={Number.MAX_SAFE_INTEGER}
            fieldProps={{ precision: 0 }}
            rules={[{ required: true }]}
          />
          <ProFormSelect
            name="status"
            label="状态"
            options={memberStatusOptions}
            rules={[{ required: true }]}
          />
        </AdminModalForm>
      )}
    </PageContainer>
  );
}
