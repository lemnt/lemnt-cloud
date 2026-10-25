import { useState } from 'react';
import { PageContainer, ProTable, type ProColumns } from '@ant-design/pro-components';
import { Alert, Button, Result, Select } from 'antd';
import { useQuery } from '@tanstack/react-query';
import { usePermission } from '@/shared/hooks/usePermission';
import { getMemberRecords } from '../api/memberApi';
import { memberKeys, recordPermissions, recordLabels } from '../types';
import type { RecordKind, MemberPageQuery, MemberRecord } from '../types';
export default function MemberRecordPage() {
  const { can } = usePermission();
  const allowed = (Object.keys(recordLabels) as RecordKind[]).filter((k) =>
    can(recordPermissions[k]),
  );
  const [selected, setSelected] = useState<RecordKind>();
  const kind = selected ?? allowed[0] ?? 'point-records';
  const [query, setQuery] = useState<MemberPageQuery>({ pageNum: 1, pageSize: 20 });
  const rows = useQuery({
    queryKey: memberKeys.records(kind, query),
    queryFn: () => getMemberRecords(kind, query),
    enabled: can(recordPermissions[kind]),
  });
  const columns: ProColumns<MemberRecord>[] = [
    { title: '记录ID', dataIndex: 'id', search: false },
    { title: '会员ID', dataIndex: 'memberId' },
    ...(kind === 'signin-records'
      ? [
          { title: '日期', dataIndex: 'signDate' },
          { title: '周期天数', dataIndex: 'day' },
          { title: '奖励积分', dataIndex: 'point' },
          { title: '奖励经验', dataIndex: 'experience' },
        ]
      : kind === 'level-records'
        ? [
            { title: '等级ID', dataIndex: 'levelId' },
            { title: '经验', dataIndex: 'experience' },
            { title: '原因', dataIndex: 'reason' },
            { title: '时间', dataIndex: 'createTime' },
          ]
        : [
            { title: '变更量', dataIndex: 'delta' },
            { title: '变更后余额', dataIndex: 'balance' },
            { title: '业务类型', dataIndex: 'bizType' },
            { title: '业务编号', dataIndex: 'bizNo' },
            { title: '原因', dataIndex: 'reason' },
            { title: '时间', dataIndex: 'createTime' },
          ]
    ).map((c) => ({ ...c, search: false })),
  ];
  if (!allowed.length) return <Result status="403" title="没有会员记录查询权限" />;
  return (
    <PageContainer title="会员记录">
      <Select
        aria-label="记录类型"
        value={kind}
        options={allowed.map((value) => ({ value, label: recordLabels[value] }))}
        onChange={(value) => {
          setSelected(value);
          setQuery({ ...query, pageNum: 1 });
        }}
      />
      {rows.isError && (
        <Alert
          type="error"
          title="读取失败"
          description={String(rows.error)}
          action={<Button onClick={() => rows.refetch()}>重试</Button>}
        />
      )}
      <ProTable<MemberRecord>
        rowKey="id"
        columns={columns}
        dataSource={rows.data?.records ?? []}
        loading={rows.isFetching}
        scroll={{ x: 1100 }}
        onSubmit={(v) =>
          setQuery({
            ...query,
            pageNum: 1,
            memberId: typeof v.memberId === 'string' ? v.memberId : undefined,
          })
        }
        onReset={() => setQuery({ pageNum: 1, pageSize: 20 })}
        pagination={{
          current: query.pageNum,
          pageSize: query.pageSize,
          total: rows.data?.total ?? 0,
          onChange: (pageNum, pageSize) => setQuery({ ...query, pageNum, pageSize }),
        }}
      />
    </PageContainer>
  );
}
