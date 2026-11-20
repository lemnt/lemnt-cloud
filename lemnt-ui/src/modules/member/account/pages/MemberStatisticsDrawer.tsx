import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Alert, Button, Drawer, Form, Input, Space, Table } from 'antd';
import type { AreaNode } from '@/modules/system/area/api/areaApi';
import {
  getMemberStatistics,
  type MemberDistribution,
  type MemberStatisticsRange,
} from '../api/memberApi';

interface Props {
  open: boolean;
  onClose: () => void;
  provinces?: AreaNode[];
  areasLoading: boolean;
  onRetryAreas: () => void;
}

/** 以系统地区树的真实父子关系归并到省。无法解析的档案保留在未知组。 */
function provinceDistribution(areas: MemberDistribution[], provinces: AreaNode[]) {
  const provinceByArea = new Map<number, AreaNode>();
  function visit(node: AreaNode, province: AreaNode) {
    provinceByArea.set(node.id, province);
    node.children.forEach((child) => visit(child, province));
  }
  provinces.forEach((province) => visit(province, province));

  const byProvince = new Map<number, { id: number; name: string; count: number }>();
  for (const { value, count } of areas) {
    const province = value == null ? undefined : provinceByArea.get(value);
    const id = province?.id ?? 0;
    const row = byProvince.get(id);
    if (row) row.count += count;
    else byProvince.set(id, { id, name: province?.name ?? '未知', count });
  }
  return [...byProvince.values()].sort((a, b) => b.count - a.count || a.id - b.id);
}

const sexLabels: Record<number, string> = { 0: '未知', 1: '男', 2: '女' };

export default function MemberStatisticsDrawer({
  open,
  onClose,
  provinces,
  areasLoading,
  onRetryAreas,
}: Props) {
  const [range, setRange] = useState<MemberStatisticsRange>();
  const stats = useQuery({
    queryKey: ['member', 'statistics', range],
    queryFn: () => getMemberStatistics(range!),
    enabled: open && !!range,
  });

  return (
    <Drawer title="会员统计" open={open} onClose={onClose} width={720}>
      <Space orientation="vertical" size="large" style={{ width: '100%' }}>
        <Form<MemberStatisticsRange> layout="inline" onFinish={setRange}>
          <Form.Item
            label="开始日期"
            name="from"
            rules={[{ required: true, message: '请选择开始日期' }]}
          >
            <Input type="date" />
          </Form.Item>
          <Form.Item
            label="结束日期"
            name="to"
            rules={[{ required: true, message: '请选择结束日期' }]}
          >
            <Input type="date" />
          </Form.Item>
          <Button type="primary" htmlType="submit" loading={stats.isFetching}>
            查询
          </Button>
        </Form>
        {stats.isError && (
          <Alert
            type="error"
            title="会员统计读取失败"
            description={String(stats.error)}
            action={<Button onClick={() => void stats.refetch()}>重试</Button>}
          />
        )}
        {stats.data && (
          <>
            <Table
              size="small"
              title={() => '每日新增会员'}
              rowKey="date"
              dataSource={stats.data.registrations}
              pagination={{ pageSize: 10, showSizeChanger: false }}
              columns={[
                { title: '注册日期', dataIndex: 'date' },
                { title: '新增人数', dataIndex: 'count', align: 'right' },
              ]}
            />
            <Table
              size="small"
              title={() => '当前会员性别分布'}
              rowKey={(row) => String(row.value)}
              dataSource={stats.data.sexes}
              pagination={false}
              columns={[
                { title: '性别', render: (_, row) => sexLabels[row.value ?? 0] ?? '未知' },
                { title: '人数', dataIndex: 'count', align: 'right' },
              ]}
            />
            {!provinces && !areasLoading ? (
              <Alert
                type="error"
                title="地区字典读取失败"
                action={<Button onClick={onRetryAreas}>重试</Button>}
              />
            ) : (
              <Table
                size="small"
                title={() => '当前会员地区分布'}
                rowKey="id"
                loading={areasLoading}
                dataSource={provinces ? provinceDistribution(stats.data.areas, provinces) : []}
                pagination={{ pageSize: 10, showSizeChanger: false }}
                columns={[
                  { title: '省级行政区', dataIndex: 'name' },
                  { title: '人数', dataIndex: 'count', align: 'right' },
                ]}
              />
            )}
          </>
        )}
      </Space>
    </Drawer>
  );
}
