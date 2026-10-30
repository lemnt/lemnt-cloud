import { useState } from 'react';
import { PageContainer, ProTable, type ProColumns } from '@ant-design/pro-components';
import { Alert, Button, Result, Select } from 'antd';
import { useQuery } from '@tanstack/react-query';
import { usePermission } from '@/shared/hooks/usePermission';
import { getMemberRecords } from '../api/memberApi';
import {
  memberKeys,
  recordPermissions,
  recordLabels,
  pointBusinessTypeEnum,
  experienceBusinessTypeEnum,
} from '../types';
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
    ...(['signin-records', 'point-records'].includes(kind)
      ? [{ title: '会员昵称', dataIndex: 'nickname' }]
      : []),
    ...(kind === 'signin-records'
      ? [
          { title: '日期', dataIndex: 'signDate', search: false },
          {
            title: '连续第几天',
            dataIndex: 'day',
            valueType: 'digit' as const,
            fieldProps: { min: 1, precision: 0 },
          },
          { title: '签到时间', dataIndex: 'createTime', search: false },
          { title: '奖励积分', dataIndex: 'point', search: false },
          { title: '奖励经验', dataIndex: 'experience', search: false },
        ]
      : kind === 'level-records'
        ? [
            { title: '等级ID', dataIndex: 'levelId' },
            { title: '变更后会员经验', dataIndex: 'experience' },
            { title: '变更时等级', dataIndex: 'level' },
            { title: '变更时折扣', dataIndex: 'discountPercent' },
            {
              title: '记录经验值',
              dataIndex: 'recordExperience',
              tooltip: '手工调整为本次经验增减；自动升级为目标等级经验门槛',
            },
            { title: '描述快照', dataIndex: 'description' },
            { title: '原因', dataIndex: 'reason' },
            { title: '时间', dataIndex: 'createTime' },
          ]
        : [
            ...(kind === 'point-records'
              ? [
                  {
                    title: '业务类型',
                    dataIndex: 'pointBizType',
                    valueType: 'select' as const,
                    valueEnum: pointBusinessTypeEnum,
                  },
                  { title: '积分标题', dataIndex: 'title' },
                  { title: '积分描述', dataIndex: 'description' },
                ]
              : []),
            ...(kind === 'experience-records'
              ? [
                  {
                    title: '经验业务类型',
                    dataIndex: 'experienceBizType',
                    valueType: 'select' as const,
                    valueEnum: experienceBusinessTypeEnum,
                  },
                  { title: '经验标题', dataIndex: 'title' },
                  { title: '经验描述', dataIndex: 'description' },
                ]
              : []),
            { title: '变更量', dataIndex: 'delta' },
            { title: '变更后余额', dataIndex: 'balance' },
            { title: '原业务码', dataIndex: 'bizType' },
            { title: '业务编号', dataIndex: 'bizNo' },
            { title: '原因', dataIndex: 'reason' },
            { title: '时间', dataIndex: 'createTime' },
          ]
    ).map((c) => ({
      ...c,
      search:
        (kind === 'signin-records' && c.dataIndex === 'day') ||
        (kind === 'level-records' && c.dataIndex === 'levelId') ||
        (kind === 'point-records' && ['bizType', 'pointBizType', 'title'].includes(c.dataIndex)) ||
        (kind === 'experience-records' &&
          ['experienceBizType', 'bizNo', 'title'].includes(c.dataIndex)),
    })),
    ...(['experience-records', 'level-records', 'signin-records'].includes(kind)
      ? [
          {
            title: '创建时间范围',
            dataIndex: 'createTimeRange',
            valueType: 'dateTimeRange' as const,
            hideInTable: true,
          },
        ]
      : []),
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
          setQuery({ pageNum: 1, pageSize: query.pageSize });
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
        key={kind}
        dateFormatter="string"
        rowKey="id"
        columns={columns}
        dataSource={rows.data?.records ?? []}
        loading={rows.isFetching}
        scroll={{ x: 1100 }}
        onSubmit={(v) =>
          setQuery({
            ...query,
            pageNum: 1,
            nickname:
              ['signin-records', 'point-records'].includes(kind) && typeof v.nickname === 'string'
                ? v.nickname
                : undefined,
            pointBizType:
              kind === 'point-records' && v.pointBizType != null && v.pointBizType !== ''
                ? Number(v.pointBizType)
                : undefined,
            bizType:
              kind === 'point-records' && typeof v.bizType === 'string' ? v.bizType : undefined,
            experienceBizType:
              kind === 'experience-records' &&
              v.experienceBizType != null &&
              v.experienceBizType !== ''
                ? Number(v.experienceBizType)
                : undefined,
            bizNo:
              kind === 'experience-records' && typeof v.bizNo === 'string' ? v.bizNo : undefined,
            title:
              ['point-records', 'experience-records'].includes(kind) && typeof v.title === 'string'
                ? v.title
                : undefined,
            startTime:
              ['experience-records', 'level-records', 'signin-records'].includes(kind) &&
              Array.isArray(v.createTimeRange)
                ? v.createTimeRange[0]
                : undefined,
            endTime:
              ['experience-records', 'level-records', 'signin-records'].includes(kind) &&
              Array.isArray(v.createTimeRange)
                ? v.createTimeRange[1]
                : undefined,
            day:
              kind === 'signin-records' && v.day != null && v.day !== ''
                ? Number(v.day)
                : undefined,
            levelId:
              kind === 'level-records' && typeof v.levelId === 'string' ? v.levelId : undefined,
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
