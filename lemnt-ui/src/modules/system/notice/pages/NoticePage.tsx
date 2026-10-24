import { useRef, useState } from 'react';
import {
  PageContainer,
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
  ProTable,
} from '@ant-design/pro-components';
import { App, Button, Form, Modal, Popconfirm } from 'antd';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { SystemStatus } from '@/modules/system/constants';
import { usePermission } from '@/shared/hooks/usePermission';
import {
  createNotice,
  deleteNotice,
  getNoticeDetail,
  getNoticeList,
  updateNotice,
} from '../api/noticeApi';
import { NoticeEditor } from '../components/NoticeEditor';
import { NoticeContent } from '../components/NoticeContent';
import { NoticeReaders } from '../components/NoticeReaders';
import { noticeKeys } from '../hooks/noticeKeys';
import { sanitizeNoticeHtml } from '../utils/noticeHtml';
import { AdminModalForm as ModalForm } from '@/shared/components/AdminModalForm';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import type { CreateNoticeRequest, Notice } from '../types';

export default function NoticePage() {
  const writeOperation = useRef(false);
  const action = useRef<ActionType | undefined>(undefined);
  const queryClient = useQueryClient();
  const { message } = App.useApp();
  const { can } = usePermission();
  const [initial, setInitial] = useState<CreateNoticeRequest>();
  const [editingId, setEditingId] = useState<number>();
  const [selected, setSelected] = useState<number[]>([]);
  const [isOpening, setIsOpening] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [preview, setPreview] = useState<Notice>();
  const [readers, setReaders] = useState<Notice>();
  const save = useMutation({
    mutationFn: (values: CreateNoticeRequest) =>
      editingId === undefined
        ? createNotice(values)
        : updateNotice({ ...values, noticeId: editingId }),
  });
  const remove = useMutation({ mutationFn: deleteNotice });
  async function refresh() {
    await queryClient.invalidateQueries({ queryKey: noticeKeys.all });
    setSelected([]);
    action.current?.reload();
  }
  async function showNotice(row?: Notice, readOnly = false) {
    setIsOpening(true);
    try {
      const detail = row
        ? await queryClient.fetchQuery({
            queryKey: noticeKeys.detail(row.noticeId),
            queryFn: () => getNoticeDetail(row.noticeId),
          })
        : undefined;
      if (readOnly && detail) setPreview(detail);
      else {
        setEditingId(detail?.noticeId);
        setInitial(
          detail ?? {
            noticeTitle: '',
            noticeType: '1',
            noticeContent: '',
            status: SystemStatus.Normal,
          },
        );
      }
    } catch (error) {
      message.error(String(error));
    } finally {
      setIsOpening(false);
    }
  }
  async function handleDelete(ids: number[]) {
    try {
      if (!(await remove.mutateAsync(ids)).data) throw new Error('删除未成功');
      message.success('删除成功');
      await refresh();
    } catch (error) {
      message.error(String(error));
    }
  }
  const columns: ProColumns<Notice>[] = [
    {
      title: '公告标题',
      dataIndex: 'noticeTitle',
      render: (_, row) => (
        <Button type="link" disabled={isOpening} onClick={() => showNotice(row, true)}>
          {row.noticeTitle}
        </Button>
      ),
    },
    { title: '类型', dataIndex: 'noticeType', valueEnum: { '1': '通知', '2': '公告' } },
    { title: '状态', dataIndex: 'status', valueEnum: { '1': '正常', '2': '关闭' } },
    { title: '创建者', dataIndex: 'createBy', search: false },
    { title: '创建时间', dataIndex: 'createTime', valueType: 'dateTime', search: false },
    {
      title: '操作',
      valueType: 'option',
      render: (_, row) => [
        can('system:notice:edit') && (
          <Button key="edit" type="link" disabled={isOpening} onClick={() => showNotice(row)}>
            修改
          </Button>
        ),
        can('system:notice:list') && (
          <Button key="readers" type="link" onClick={() => setReaders(row)}>
            已读用户
          </Button>
        ),
        can('system:notice:remove') && (
          <Popconfirm
            key="remove"
            title="确认删除该通知？"
            onConfirm={() => handleDelete([row.noticeId])}
          >
            <Button type="link" danger disabled={remove.isPending}>
              删除
            </Button>
          </Popconfirm>
        ),
      ],
    },
  ];
  return (
    <PageContainer title={false}>
      <ProTable<Notice>
        search={{ labelWidth: 'auto' }}
        rowKey="noticeId"
        actionRef={action}
        columns={columns}
        scroll={{ x: 'max-content' }}
        rowSelection={{
          selectedRowKeys: selected,
          onChange: (keys) => setSelected(keys.map(Number)),
        }}
        request={(params) => {
          const filters = {
            pageNum: params.current,
            pageSize: params.pageSize,
            noticeTitle: params.noticeTitle,
            noticeType: params.noticeType,
            status: params.status,
          };
          return queryClient.fetchQuery({
            queryKey: noticeKeys.list(filters),
            queryFn: () => getNoticeList(filters),
          });
        }}
        onRequestError={(error) => message.error(error.message)}
        toolBarRender={() => [
          can('system:notice:add') && (
            <Button key="add" type="primary" loading={isOpening} onClick={() => showNotice()}>
              新增
            </Button>
          ),
          can('system:notice:remove') && (
            <Popconfirm
              key="remove"
              title="确认删除所选通知？"
              disabled={!selected.length}
              onConfirm={() => handleDelete(selected)}
            >
              <Button danger disabled={!selected.length} loading={remove.isPending}>
                批量删除
              </Button>
            </Popconfirm>
          ),
        ]}
      />
      {initial && (
        <ModalForm<CreateNoticeRequest>
          description="编辑通知内容，并设置发布类型与状态"
          open
          title={editingId === undefined ? '新增通知' : '修改通知'}
          initialValues={initial}
          onOpenChange={(open) => {
            if (!open && !isUploading && !save.isPending) setInitial(undefined);
          }}
          disabled={save.isPending}
          modalProps={{
            destroyOnHidden: true,
            mask: { closable: false },
            closable: !isUploading && !save.isPending,
            keyboard: !isUploading && !save.isPending,
          }}
          submitter={{
            submitButtonProps: { loading: save.isPending, disabled: isUploading },
            resetButtonProps: { disabled: isUploading || save.isPending },
          }}
          onFinish={async (values) => {
            if (isUploading || writeOperation.current) return false;
            writeOperation.current = true;
            try {
              const data = {
                ...values,
                noticeContent:
                  values.noticeContent === undefined
                    ? undefined
                    : sanitizeNoticeHtml(values.noticeContent),
              };
              if (!(await save.mutateAsync(data)).data) throw new Error('保存未成功');
              setInitial(undefined);
              message.success('保存成功');
              await refresh();
              return true;
            } catch (error) {
              message.error(String(error));
              return false;
            } finally {
              writeOperation.current = false;
            }
          }}
        >
          <ProFormText
            name="noticeTitle"
            label="标题"
            rules={[{ required: true, whitespace: true }]}
            fieldProps={{ maxLength: 50 }}
          />
          <ProFormSelect
            name="noticeType"
            label="类型"
            options={[
              { label: '通知', value: '1' },
              { label: '公告', value: '2' },
            ]}
            rules={[{ required: true }]}
          />
          <ProFormSelect
            name="status"
            label="状态"
            options={[
              { label: '正常', value: SystemStatus.Normal },
              { label: '关闭', value: SystemStatus.Disabled },
            ]}
          />
          <Form.Item name="noticeContent" label="内容">
            <NoticeEditor disabled={save.isPending} onUploadingChange={setIsUploading} />
          </Form.Item>
          <ProFormTextArea name="remark" label="备注" />
        </ModalForm>
      )}
      <Modal
        open={Boolean(preview)}
        title={preview?.noticeTitle}
        footer={null}
        onCancel={() => setPreview(undefined)}
      >
        <NoticeContent html={preview?.noticeContent} isVisible={Boolean(preview)} />
      </Modal>
      {readers && (
        <NoticeReaders
          key={readers.noticeId}
          notice={readers}
          onClose={() => setReaders(undefined)}
        />
      )}
    </PageContainer>
  );
}
