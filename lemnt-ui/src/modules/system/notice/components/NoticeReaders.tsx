import { ProTable } from '@ant-design/pro-components';
import { App, Drawer } from 'antd';
import { useQueryClient } from '@tanstack/react-query';
import { getNoticeReadUsers } from '../api/noticeApi';
import { noticeKeys } from '../hooks/noticeKeys';
import type { ProColumns } from '@ant-design/pro-components';
import type { Notice, NoticeReadUser } from '../types';

interface NoticeReadersProps {
  notice: Notice;
  onClose: () => void;
}
export function NoticeReaders({ notice, onClose }: NoticeReadersProps) {
  const queryClient = useQueryClient();
  const { message } = App.useApp();
  const columns: ProColumns<NoticeReadUser>[] = [
    { title: '搜索用户', dataIndex: 'searchValue', hideInTable: true },
    { title: '用户名', dataIndex: 'userName', search: false },
    { title: '昵称', dataIndex: 'nickName', search: false },
    { title: '部门', dataIndex: 'deptName', search: false },
    { title: '手机号', dataIndex: 'phonenumber', search: false },
    { title: '阅读时间', dataIndex: 'readTime', valueType: 'dateTime', search: false },
  ];
  return (
    <Drawer title={`${notice.noticeTitle}：已读用户`} open size={900} onClose={onClose}>
      <ProTable<NoticeReadUser>
        rowKey="userId"
        columns={columns}
        request={(params) => {
          const filters = {
            noticeId: notice.noticeId,
            searchValue: params.searchValue,
            pageNum: params.current,
            pageSize: params.pageSize,
          };
          return queryClient.fetchQuery({
            queryKey: noticeKeys.readers(filters),
            queryFn: () => getNoticeReadUsers(filters),
          });
        }}
        onRequestError={(error) => message.error(error.message)}
      />
    </Drawer>
  );
}
