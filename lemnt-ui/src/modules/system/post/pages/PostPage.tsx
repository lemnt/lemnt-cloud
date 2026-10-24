import { useRef, useState } from 'react';
import {
  PageContainer,
  ProFormDigit,
  ProFormRadio,
  ProFormText,
  ProFormTextArea,
  ProTable,
  type ActionType,
  type ProColumns,
} from '@ant-design/pro-components';
import { App, Button, Popconfirm } from 'antd';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { userKeys } from '@/modules/system/user/hooks/userKeys';
import { SystemStatus, systemStatusEnum, systemStatusOptions } from '@/modules/system/constants';
import { usePermission } from '@/shared/hooks/usePermission';
import { createPost, deletePost, getPostDetail, getPostList, updatePost } from '../api/postApi';
import { postKeys } from '../hooks/postKeys';
import { AdminModalForm as ModalForm } from '@/shared/components/AdminModalForm';
import type { CreatePostRequest, Post } from '../types';

export default function PostPage() {
  const action = useRef<ActionType | undefined>(undefined);
  const [editing, setEditing] = useState<Post>();
  const [open, setOpen] = useState(false);
  const [selected, setSelected] = useState<number[]>([]);
  const [isOpening, setIsOpening] = useState(false);
  const operation = useRef(false);
  const client = useQueryClient();
  const save = useMutation({
    mutationFn: (values: CreatePostRequest) =>
      editing ? updatePost({ ...values, postId: editing.postId }) : createPost(values),
  });
  const deletion = useMutation({ mutationFn: deletePost });
  const isBusy = isOpening || save.isPending || deletion.isPending;
  const { message } = App.useApp();
  const permissions = usePermission();
  const can = (operation: string) => permissions.can(`system:post:${operation}`);
  /** 写入已成功时，刷新失败不应使用户再次提交同一操作。 */
  async function refresh() {
    try {
      await client.invalidateQueries({ queryKey: postKeys.all });
      await client.invalidateQueries({ queryKey: userKeys.all });
      await action.current?.reload();
    } catch (reason) {
      message.error(`操作已成功，但列表刷新失败：${String(reason)}`);
    }
  }
  async function edit(record: Post) {
    if (operation.current) return;
    operation.current = true;
    setIsOpening(true);
    try {
      setEditing(
        await client.fetchQuery({
          queryKey: postKeys.detail(record.postId),
          queryFn: () => getPostDetail(record.postId),
        }),
      );
      setOpen(true);
    } catch (reason) {
      message.error(String(reason));
    } finally {
      operation.current = false;
      setIsOpening(false);
    }
  }
  async function remove(ids: number[]) {
    if (operation.current || !ids.length) return;
    operation.current = true;
    try {
      const result = await deletion.mutateAsync(ids);
      if (!result.data) throw new Error('删除未成功，请重试');
      message.success('删除成功');
      setSelected([]);
      await refresh();
    } catch (reason) {
      message.error(String(reason));
    } finally {
      operation.current = false;
    }
  }
  const columns: ProColumns<Post>[] = [
    { title: '岗位编号', dataIndex: 'postId', search: false },
    { title: '岗位编码', dataIndex: 'postCode' },
    { title: '岗位名称', dataIndex: 'postName' },
    { title: '岗位排序', dataIndex: 'postSort', search: false },
    {
      title: '状态',
      dataIndex: 'status',
      valueEnum: systemStatusEnum,
    },
    { title: '创建时间', dataIndex: 'createTime', valueType: 'dateTime', search: false },
    {
      title: '操作',
      valueType: 'option',
      render: (_, record) => [
        can('edit') && (
          <Button key="edit" type="link" disabled={isBusy} onClick={() => edit(record)}>
            修改
          </Button>
        ),
        can('remove') && (
          <Popconfirm
            key="delete"
            title="确认删除该岗位？"
            disabled={isBusy}
            onConfirm={() => remove([record.postId])}
          >
            <Button
              type="link"
              danger
              disabled={isBusy}
              loading={deletion.isPending && deletion.variables?.includes(record.postId)}
            >
              删除
            </Button>
          </Popconfirm>
        ),
      ],
    },
  ];
  return (
    <PageContainer title={false}>
      <ProTable<Post>
        search={{ labelWidth: 'auto' }}
        rowKey="postId"
        actionRef={action}
        columns={columns}
        pagination={{ defaultPageSize: 10 }}
        request={async (params) => {
          const filters = {
            pageNum: params.current,
            pageSize: params.pageSize,
            postCode: params.postCode,
            postName: params.postName,
            status: params.status,
          };
          return client.fetchQuery({
            queryKey: postKeys.list(filters),
            queryFn: () => getPostList(filters),
          });
        }}
        onRequestError={(reason) => message.error(reason.message)}
        rowSelection={{
          selectedRowKeys: selected,
          onChange: (keys) => setSelected(keys.map(Number)),
          getCheckboxProps: () => ({ disabled: isBusy }),
        }}
        toolBarRender={() => [
          can('add') && (
            <Button
              key="add"
              type="primary"
              disabled={isBusy}
              loading={isOpening}
              onClick={() => {
                if (operation.current) return;
                setEditing(undefined);
                setOpen(true);
              }}
            >
              新增
            </Button>
          ),
          can('remove') && (
            <Popconfirm
              key="delete"
              title="确认删除选中的岗位？"
              disabled={isBusy || !selected.length}
              onConfirm={() => remove(selected)}
            >
              <Button danger disabled={isBusy || !selected.length} loading={deletion.isPending}>
                批量删除
              </Button>
            </Popconfirm>
          ),
        ]}
      />
      {open && (
        <ModalForm<CreatePostRequest>
          description="维护岗位名称、编码与显示顺序"
          title={editing ? '修改岗位' : '新增岗位'}
          open={open}
          onOpenChange={(nextOpen) => {
            if (!operation.current) setOpen(nextOpen);
          }}
          initialValues={editing || { postSort: 0, status: SystemStatus.Normal }}
          disabled={save.isPending}
          modalProps={{
            destroyOnHidden: true,
            mask: { closable: false },
            closable: !save.isPending,
            keyboard: !save.isPending,
          }}
          submitter={{
            submitButtonProps: { loading: save.isPending, disabled: isBusy },
            resetButtonProps: { disabled: isBusy },
          }}
          onFinish={async (values) => {
            if (operation.current) return false;
            operation.current = true;
            try {
              // 仅提交后端 DTO 字段，避免将表格展示字段写回接口。
              const { postCode, postName, postSort, status, remark } = values;
              const result = await save.mutateAsync({
                postCode,
                postName,
                postSort,
                status,
                remark,
              });
              if (!result.data) throw new Error('保存未成功，请重试');
              message.success('保存成功');
              setOpen(false);
              await refresh();
              return true;
            } catch (reason) {
              message.error(String(reason));
              return false;
            } finally {
              operation.current = false;
            }
          }}
        >
          <ProFormText
            name="postName"
            label="岗位名称"
            rules={[{ required: true }]}
            fieldProps={{ maxLength: 50 }}
          />
          <ProFormText
            name="postCode"
            label="岗位编码"
            rules={[{ required: true }]}
            fieldProps={{ maxLength: 64 }}
          />
          <ProFormDigit name="postSort" label="岗位排序" min={0} fieldProps={{ precision: 0 }} />
          <ProFormRadio.Group name="status" label="状态" options={systemStatusOptions} />
          <ProFormTextArea name="remark" label="备注" fieldProps={{ maxLength: 500 }} />
        </ModalForm>
      )}
    </PageContainer>
  );
}
