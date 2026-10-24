import { useRef, useState } from 'react';
import { FieldCode, PageContainer, ProTable } from '@ant-design/pro-components';
import { App, Button, Descriptions, Drawer, Popconfirm, Typography } from 'antd';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { usePermission } from '@/shared/hooks/usePermission';
import { cleanOperationLog, deleteOperationLog, getOperationLogList } from '../api/logApi';
import { logKeys } from '../hooks/logKeys';
import { logDateRange } from '../types';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import type { OperationLog, OperationLogQuery } from '../types';
import styles from './operationLogPage.module.css';

interface OperationLogSearch extends Omit<OperationLogQuery, 'status' | 'businessType'> {
  status?: string;
  businessType?: string;
  dateRange?: string[];
}
const businessTypes = {
  0: '其他',
  1: '新增',
  2: '修改',
  3: '删除',
  4: '授权',
  5: '导出',
  6: '导入',
  7: '强退',
  8: '生成代码',
  9: '清空数据',
};

export default function OperationLogPage() {
  const action = useRef<ActionType | undefined>(undefined);
  const queryClient = useQueryClient();
  const [selected, setSelected] = useState<number[]>([]);
  const [detail, setDetail] = useState<OperationLog>();
  const { message } = App.useApp();
  const { can } = usePermission();
  const remove = useMutation({ mutationFn: deleteOperationLog });
  const clean = useMutation({ mutationFn: cleanOperationLog });
  const isMutating = remove.isPending || clean.isPending;
  async function refresh() {
    await queryClient.invalidateQueries({ queryKey: logKeys.operations });
    setSelected([]);
    action.current?.reload();
  }
  const columns: ProColumns<OperationLog>[] = [
    { title: '模块标题', dataIndex: 'title' },
    { title: '业务类型', dataIndex: 'businessType', valueEnum: businessTypes },
    { title: '操作人员', dataIndex: 'operName' },
    { title: '请求方式', dataIndex: 'requestMethod', search: false },
    { title: '主机地址', dataIndex: 'operIp', search: false },
    {
      title: '操作状态',
      dataIndex: 'status',
      valueEnum: { 1: { text: '成功', status: 'Success' }, 2: { text: '失败', status: 'Error' } },
    },
    { title: '操作时间', dataIndex: 'operTime', valueType: 'dateTime', search: false },
    { title: '操作时间', dataIndex: 'dateRange', valueType: 'dateRange', hideInTable: true },
    {
      title: '操作',
      valueType: 'option',
      render: (_, record) => [
        can('system:operlog:query') && (
          <Button key="detail" type="link" onClick={() => setDetail(record)}>
            详情
          </Button>
        ),
      ],
    },
  ];
  return (
    <PageContainer title={false}>
      <ProTable<OperationLog, OperationLogSearch>
        search={{ labelWidth: 'auto' }}
        rowKey="operId"
        actionRef={action}
        columns={columns}
        scroll={{ x: 'max-content' }}
        rowSelection={{
          preserveSelectedRowKeys: true,
          selectedRowKeys: selected,
          onChange: (keys) => setSelected(keys.map(Number)),
        }}
        request={(params) => {
          const filters: OperationLogQuery = {
            pageNum: params.current,
            pageSize: params.pageSize,
            title: params.title,
            operName: params.operName,
            status: params.status === undefined ? undefined : Number(params.status),
            businessType:
              params.businessType === undefined ? undefined : Number(params.businessType),
            ...logDateRange(params.dateRange),
          };
          return queryClient.fetchQuery({
            queryKey: logKeys.operationList(filters),
            queryFn: () => getOperationLogList(filters),
          });
        }}
        onRequestError={(error) => message.error(error.message)}
        toolBarRender={() => [
          can('system:operlog:remove') && (
            <Popconfirm
              key="delete"
              title="确认删除所选操作日志？"
              disabled={!selected.length || isMutating}
              onConfirm={async () => {
                try {
                  await remove.mutateAsync(selected);
                  message.success('删除成功');
                  await refresh();
                } catch (error) {
                  message.error(String(error));
                }
              }}
            >
              <Button danger disabled={!selected.length || isMutating} loading={remove.isPending}>
                批量删除
              </Button>
            </Popconfirm>
          ),
          can('system:operlog:remove') && (
            <Popconfirm
              key="clean"
              title="确认清空全部操作日志？"
              description="此操作不是只删除当前筛选结果，且不可恢复。"
              disabled={isMutating}
              onConfirm={async () => {
                try {
                  await clean.mutateAsync();
                  message.success('已清空');
                  await refresh();
                } catch (error) {
                  message.error(String(error));
                }
              }}
            >
              <Button danger disabled={isMutating} loading={clean.isPending}>
                清空全部
              </Button>
            </Popconfirm>
          ),
        ]}
      />
      <Drawer
        open={Boolean(detail)}
        title="操作日志详情"
        size={800}
        onClose={() => setDetail(undefined)}
      >
        <Descriptions
          column={1}
          items={
            detail
              ? [
                  { key: 'title', label: '模块标题', children: detail.title },
                  {
                    key: 'type',
                    label: '业务类型',
                    children:
                      businessTypes[detail.businessType as keyof typeof businessTypes] ??
                      detail.businessType,
                  },
                  { key: 'user', label: '操作人员', children: detail.operName },
                  { key: 'dept', label: '部门', children: detail.deptName },
                  { key: 'url', label: '请求地址', children: detail.operUrl },
                  { key: 'method', label: '方法名称', children: detail.method },
                  { key: 'request', label: '请求方式', children: detail.requestMethod },
                  { key: 'ip', label: '主机地址', children: detail.operIp },
                  { key: 'location', label: '操作地点', children: detail.operLocation },
                  { key: 'time', label: '操作时间', children: detail.operTime },
                  { key: 'status', label: '状态', children: detail.status === 1 ? '成功' : '失败' },
                  {
                    key: 'params',
                    label: '请求参数',
                    children: detail.operParam ? (
                      <FieldCode
                        text={detail.operParam}
                        mode="read"
                        language="json"
                        fieldProps={{ className: styles.jsonCode, style: { width: '100%' } }}
                      />
                    ) : (
                      '-'
                    ),
                  },
                  {
                    key: 'result',
                    label: '返回参数',
                    children: detail.jsonResult ? (
                      <FieldCode
                        text={detail.jsonResult}
                        mode="read"
                        language="json"
                        fieldProps={{ className: styles.jsonCode, style: { width: '100%' } }}
                      />
                    ) : (
                      '-'
                    ),
                  },
                  {
                    key: 'error',
                    label: '错误消息',
                    children: <Typography.Paragraph>{detail.errorMsg}</Typography.Paragraph>,
                  },
                ]
              : []
          }
        />
      </Drawer>
    </PageContainer>
  );
}
