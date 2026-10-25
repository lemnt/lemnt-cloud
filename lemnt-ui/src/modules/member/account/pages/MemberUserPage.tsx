import { useState } from 'react';
import {
  PageContainer,
  ProTable,
  ProFormText,
  ProFormDigit,
  ProFormSelect,
  ProFormDatePicker,
  type ProColumns,
} from '@ant-design/pro-components';
import { Alert, App, Button, Descriptions, Drawer, Result, Space, Table } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { AdminModalForm } from '@/shared/components/AdminModalForm';
import { usePermission } from '@/shared/hooks/usePermission';
import * as api from '../api/memberApi';
import { memberKeys, memberStatusOptions } from '../types';
import type { MemberProfile, MemberProfileValues, MemberPageQuery, MemberAddress } from '../types';

type ActionKind =
  'create' | 'edit' | 'password' | 'points' | 'experience' | 'level' | 'group' | 'tags';
interface Action {
  kind: ActionKind;
  row?: MemberProfile;
  requestNo: string;
}
interface Values extends MemberProfileValues {
  mobile: string;
  password: string;
  delta: number;
  reason: string;
  levelId?: string;
  groupId?: string;
  tagIds?: string[];
}
const labels: Record<ActionKind, string> = {
  create: '创建会员',
  edit: '编辑资料',
  password: '重置密码',
  points: '调整积分',
  experience: '调整经验',
  level: '调整等级',
  group: '设置分组',
  tags: '设置标签',
};
const permissions: Record<ActionKind, string> = {
  create: 'member:user:create',
  edit: 'member:user:update',
  password: 'member:user:reset-password',
  points: 'member:user:update-point',
  experience: 'member:user:update-level',
  level: 'member:user:update-level',
  group: 'member:user:update',
  tags: 'member:user:update',
};
export default function MemberUserPage() {
  const { can } = usePermission();
  const { message } = App.useApp();
  const cache = useQueryClient();
  const [query, setQuery] = useState<MemberPageQuery>({ pageNum: 1, pageSize: 20 });
  const [detail, setDetail] = useState<string>();
  const [action, setAction] = useState<Action>();
  const [frozen, setFrozen] = useState<Values>();
  const users = useQuery({
    queryKey: memberKeys.users(query),
    queryFn: () => api.getMembers(query),
    enabled: can('member:user:query'),
  });
  const profile = useQuery({
    queryKey: memberKeys.detail(detail),
    queryFn: () => api.getMember(detail!),
    enabled: !!detail && can('member:user:query'),
  });
  const addresses = useQuery({
    queryKey: [...memberKeys.detail(detail), 'addresses'],
    queryFn: () => api.getMemberAddresses(detail!),
    enabled: !!detail && can('member:address:query'),
  });
  const levels = useQuery({
    queryKey: memberKeys.metadata('levels'),
    queryFn: () => api.getMetadata('levels'),
    enabled: action?.kind === 'level' && can('member:level:query'),
  });
  const groups = useQuery({
    queryKey: memberKeys.metadata('groups'),
    queryFn: () => api.getMetadata('groups'),
    enabled: action?.kind === 'group' && can('member:group:query'),
  });
  const tags = useQuery({
    queryKey: memberKeys.metadata('tags'),
    queryFn: () => api.getMetadata('tags'),
    enabled: action?.kind === 'tags' && can('member:tag:query'),
  });
  const selectedTags = useQuery({
    queryKey: [...memberKeys.detail(action?.row?.id), 'tags'],
    queryFn: () => api.getMemberTags(action!.row!.id),
    enabled: action?.kind === 'tags' && !!action.row && can('member:user:query'),
  });
  const mutation = useMutation({
    mutationFn: async ({ operation, v }: { operation: Action; v: Values }) => {
      if (!can(permissions[operation.kind])) throw Error('没有操作权限');
      const id = operation.row?.id;
      switch (operation.kind) {
        case 'create':
          return api.createMember(v);
        case 'edit':
          return api.updateMember(id!, v);
        case 'password':
          return api.resetMemberPassword(id!, v.password);
        case 'points':
        case 'experience':
          return api.adjustMember(id!, operation.kind, {
            delta: v.delta,
            reason: v.reason,
            requestNo: operation.requestNo,
          });
        case 'level':
          return api.assignMemberLevel(id!, v.levelId || null, operation.requestNo, v.reason);
        case 'group':
          return api.assignMemberGroup(id!, v.groupId || null);
        case 'tags':
          return api.assignMemberTags(id!, v.tagIds ?? []);
      }
    },
  });
  function open(kind: ActionKind, row?: MemberProfile) {
    setFrozen(undefined);
    setAction({ kind, row, requestNo: crypto.randomUUID() });
  }
  const lookup = action?.kind === 'level' ? levels : action?.kind === 'group' ? groups : tags;
  const needsLookup = !!action && ['level', 'group', 'tags'].includes(action.kind);
  const lookupAllowed =
    action?.kind === 'level'
      ? can('member:level:query')
      : action?.kind === 'group'
        ? can('member:group:query')
        : can('member:tag:query');
  const columns: ProColumns<MemberProfile>[] = [
    { title: '会员编号', dataIndex: 'memberNo', search: false },
    { title: '昵称', dataIndex: 'nickname', search: false },
    { title: '手机号', dataIndex: 'mobile' },
    {
      title: '状态',
      dataIndex: 'status',
      search: false,
      renderText: (v) => (v === 1 ? '启用' : '停用'),
    },
    { title: '积分', dataIndex: 'points', search: false },
    { title: '经验', dataIndex: 'experience', search: false },
    { title: '等级ID', dataIndex: 'levelId', search: false },
    { title: '分组ID', dataIndex: 'groupId', search: false },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) => (
        <Space wrap>
          <Button type="link" onClick={() => setDetail(row.id)}>
            详情
          </Button>
          {(['edit', 'password', 'points', 'experience', 'level', 'group', 'tags'] as ActionKind[])
            .filter((k) => can(permissions[k]))
            .map((k) => (
              <Button key={k} type="link" onClick={() => open(k, row)}>
                {labels[k]}
              </Button>
            ))}
        </Space>
      ),
    },
  ];
  if (!can('member:user:query')) return <Result status="403" title="没有会员查询权限" />;
  return (
    <PageContainer title="会员管理">
      {users.isError && (
        <Alert
          type="error"
          title="会员读取失败"
          description={String(users.error)}
          action={<Button onClick={() => users.refetch()}>重试</Button>}
        />
      )}
      <ProTable<MemberProfile>
        rowKey="id"
        columns={columns}
        dataSource={users.data?.records ?? []}
        loading={users.isFetching}
        scroll={{ x: 1400 }}
        onSubmit={(v) =>
          setQuery({
            ...query,
            pageNum: 1,
            mobile: typeof v.mobile === 'string' ? v.mobile : undefined,
          })
        }
        onReset={() => setQuery({ pageNum: 1, pageSize: 20 })}
        pagination={{
          current: query.pageNum,
          pageSize: query.pageSize,
          total: users.data?.total ?? 0,
          onChange: (pageNum, pageSize) => setQuery({ ...query, pageNum, pageSize }),
        }}
        toolBarRender={() => [
          can(permissions.create) && (
            <Button key="create" type="primary" onClick={() => open('create')}>
              创建会员
            </Button>
          ),
        ]}
      />
      <Drawer
        title="会员详情"
        open={!!detail}
        onClose={() => setDetail(undefined)}
        width={720}
        destroyOnHidden
      >
        {profile.isFetching && <p>正在读取会员资料…</p>}
        {profile.isError && (
          <Alert
            type="error"
            title="详情读取失败"
            action={<Button onClick={() => profile.refetch()}>重试</Button>}
          />
        )}
        {profile.data && (
          <Descriptions
            column={1}
            items={[
              ['会员编号', profile.data.memberNo],
              ['昵称', profile.data.nickname],
              ['手机', profile.data.mobile],
              ['邮箱', profile.data.email],
              ['生日', profile.data.birthday],
              ['积分', profile.data.points],
              ['经验', profile.data.experience],
              ['创建时间', profile.data.createTime],
            ].map(([label, children]) => ({
              key: String(label),
              label,
              children: children ?? '未设置',
            }))}
          />
        )}
        {can('member:address:query') && (
          <>
            <h3>地址簿</h3>
            {addresses.isError && (
              <Alert
                type="error"
                title="地址读取失败"
                action={<Button onClick={() => addresses.refetch()}>重试</Button>}
              />
            )}
            <Table<MemberAddress>
              rowKey="id"
              loading={addresses.isFetching}
              dataSource={addresses.data ?? []}
              pagination={false}
              columns={[
                { title: '姓名', dataIndex: 'name' },
                { title: '手机号', dataIndex: 'mobile' },
                { title: '区划代码', dataIndex: 'areaId' },
                { title: '详细地址', dataIndex: 'detailAddress' },
                { title: '默认', render: (_, r) => (r.defaultStatus ? '是' : '否') },
              ]}
            />
          </>
        )}
      </Drawer>
      {action && (
        <AdminModalForm<Values>
          key={
            action.requestNo + (action.kind === 'tags' && selectedTags.isSuccess ? 'loaded' : '')
          }
          title={labels[action.kind]}
          open
          onOpenChange={(value) => {
            if (!value && !mutation.isPending) {
              setAction(undefined);
              setFrozen(undefined);
            }
          }}
          initialValues={{
            ...action.row,
            levelId: action.row?.levelId ?? undefined,
            groupId: action.row?.groupId ?? undefined,
            status: action.row?.status ?? 1,
            sex: action.row?.sex ?? 0,
            tagIds: selectedTags.data ?? [],
          }}
          modalProps={{ destroyOnHidden: true, maskClosable: false, closable: !mutation.isPending }}
          submitter={{
            submitButtonProps: {
              loading: mutation.isPending,
              disabled:
                (needsLookup && (!lookupAllowed || lookup.isPending || lookup.isError)) ||
                (action.kind === 'tags' && (!selectedTags.isSuccess || selectedTags.isFetching)),
            },
            resetButtonProps: { disabled: mutation.isPending },
          }}
          onFinish={async (values) => {
            if (mutation.isPending) return false;
            const v = frozen ?? values;
            if (['points', 'experience', 'level'].includes(action.kind)) setFrozen({ ...v });
            try {
              await mutation.mutateAsync({ operation: action, v });
              message.success('已保存');
              setAction(undefined);
              setFrozen(undefined);
              try {
                await cache.invalidateQueries({ queryKey: memberKeys.all }, { throwOnError: true });
              } catch {
                message.warning('保存成功，但列表刷新失败，请手动刷新');
              }
              return true;
            } catch (error) {
              message.error(String(error));
              return false;
            }
          }}
        >
          {['password', 'edit'].includes(action.kind) && (
            <Alert
              type="warning"
              title={
                action.kind === 'password'
                  ? '重置后该会员所有登录会话失效。'
                  : '停用将撤销该会员所有登录会话。'
              }
            />
          )}
          {frozen && (
            <Alert
              type="warning"
              title="结果尚未确认，重试将使用原始内容。请保持此窗口；关闭前先核验流水。"
            />
          )}
          {needsLookup && (!lookupAllowed || lookup.isError) && (
            <Alert
              type="error"
              title={
                lookupAllowed ? '选项读取失败，请关闭后重试' : '需要对应等级、分组或标签查询权限'
              }
            />
          )}
          {action.kind === 'tags' && selectedTags.isError && (
            <Alert type="error" title="原有标签读取失败，禁止覆盖" />
          )}
          {['create', 'edit'].includes(action.kind) && (
            <>
              <ProFormText name="nickname" label="昵称" rules={[{ required: true }, { max: 80 }]} />
              {action.kind === 'create' && (
                <ProFormText
                  name="mobile"
                  label="手机号"
                  rules={[{ required: true }, { pattern: /^\+?\d{7,20}$/ }]}
                />
              )}
              <ProFormText name="email" label="邮箱" rules={[{ type: 'email' }, { max: 254 }]} />
              <ProFormText
                name="avatar"
                label="头像HTTPS地址"
                rules={[{ pattern: /^https:\/\// }, { max: 500 }]}
              />
              {action.kind === 'edit' && (
                <>
                  <ProFormSelect
                    name="sex"
                    label="性别"
                    options={[
                      { label: '未知', value: 0 },
                      { label: '男', value: 1 },
                      { label: '女', value: 2 },
                    ]}
                  />
                  <ProFormDatePicker name="birthday" label="生日" />
                  <ProFormSelect
                    name="status"
                    label="状态"
                    options={memberStatusOptions}
                    rules={[{ required: true }]}
                  />
                </>
              )}
            </>
          )}
          {action.kind === 'password' && (
            <ProFormText.Password
              name="password"
              label="新密码"
              rules={[
                { required: true },
                { min: 10 },
                {
                  validator: (_, v) =>
                    !v || new TextEncoder().encode(v).length <= 72
                      ? Promise.resolve()
                      : Promise.reject(Error('密码不能超过72字节')),
                },
              ]}
            />
          )}
          {['points', 'experience'].includes(action.kind) && (
            <ProFormDigit
              name="delta"
              label="本次变更量（负数扣减）"
              min={-Number.MAX_SAFE_INTEGER}
              max={Number.MAX_SAFE_INTEGER}
              fieldProps={{ precision: 0, disabled: !!frozen }}
              rules={[{ required: true }]}
            />
          )}
          {['points', 'experience', 'level'].includes(action.kind) && (
            <ProFormText
              name="reason"
              label="调整原因"
              disabled={!!frozen}
              rules={[{ required: true }, { max: 500 }]}
            />
          )}
          {needsLookup && (
            <ProFormSelect
              name={
                action.kind === 'level' ? 'levelId' : action.kind === 'group' ? 'groupId' : 'tagIds'
              }
              label={
                action.kind === 'level'
                  ? '目标等级（清空重置经验为0）'
                  : action.kind === 'group'
                    ? '分组（可清空）'
                    : '标签（空集合清空）'
              }
              options={(lookup.data ?? [])
                .filter((v) => v.status === undefined || v.status === 1)
                .map((v) => ({ label: v.name, value: v.id }))}
              fieldProps={{
                mode: action.kind === 'tags' ? 'multiple' : undefined,
                allowClear: true,
                disabled: !!frozen || lookup.isPending,
              }}
            />
          )}
        </AdminModalForm>
      )}
    </PageContainer>
  );
}
