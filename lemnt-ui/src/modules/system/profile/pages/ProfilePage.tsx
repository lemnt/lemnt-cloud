import { useRef, useState } from 'react';
import {
  PageContainer,
  ProForm,
  ProFormText,
  ProFormRadio,
  type ProFormInstance,
} from '@ant-design/pro-components';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { App, Avatar, Button, Card, Descriptions, Result, Space, Spin, Tabs, Upload } from 'antd';
import { useSession } from '@/stores/userStore';
import defaultAvatar from '@/shared/assets/default-avatar.png';
import { getProfile, updatePassword, updateProfile, uploadAvatar } from '../api/profileApi';
import { profileKeys } from '../hooks/profileKeys';
import { AvatarCropper } from '../components/AvatarCropper';
import type { ProfileValues } from '../types';

export default function ProfilePage() {
  const { message } = App.useApp();
  const passwordForm = useRef<ProFormInstance | undefined>(undefined);
  const client = useQueryClient();
  const profile = useQuery({ queryKey: profileKeys.detail, queryFn: getProfile });
  const save = useMutation({ mutationFn: updateProfile });
  const password = useMutation({ mutationFn: updatePassword });
  const avatar = useMutation({ mutationFn: uploadAvatar });
  const [avatarFile, setAvatarFile] = useState<File>();
  if (profile.isPending) return <Spin />;
  if (profile.isError)
    return (
      <Result
        status="error"
        title="资料加载失败"
        extra={<Button onClick={() => void profile.refetch()}>重试</Button>}
      />
    );
  const data = profile.data;
  /** 同步布局中的身份展示，不重新初始化整个登录会话。 */
  function updateSession(values: { nickName?: string; avatar?: string }) {
    useSession.setState((state) => ({
      user: state.user ? { ...state.user, user: { ...state.user.user, ...values } } : undefined,
    }));
  }
  return (
    <PageContainer title={false}>
      <Card>
        <Space orientation="vertical" style={{ width: '100%' }}>
          <Avatar size={80} src={data.user.avatar || defaultAvatar}>
            {data.user.nickName}
          </Avatar>
          <Upload
            accept="image/png,image/jpeg"
            showUploadList={false}
            disabled={avatar.isPending || Boolean(avatarFile)}
            beforeUpload={(file) => {
              if (!['image/png', 'image/jpeg'].includes(file.type)) {
                message.error('请选择 PNG 或 JPEG 图片');
                return Upload.LIST_IGNORE;
              }
              setAvatarFile(file);
              return false;
            }}
          >
            <Button loading={avatar.isPending}>更换头像</Button>
          </Upload>
          <Descriptions
            items={[
              { key: 'account', label: '账号', children: data.user.userName },
              { key: 'department', label: '部门', children: data.user.dept?.deptName || '-' },
              { key: 'roles', label: '角色', children: data.roleGroup || '-' },
              { key: 'posts', label: '岗位', children: data.postGroup || '-' },
            ]}
          />
          <Tabs
            items={[
              {
                key: 'profile',
                label: '基本资料',
                children: (
                  <ProForm<ProfileValues>
                    initialValues={data.user}
                    onFinish={async (values) => {
                      try {
                        await save.mutateAsync({
                          userId: data.user.userId,
                          nickName: values.nickName,
                          email: values.email,
                          phonenumber: values.phonenumber,
                          sex: values.sex,
                        });
                        updateSession({ nickName: values.nickName });
                        await client.invalidateQueries({ queryKey: profileKeys.detail });
                        message.success('资料更新成功');
                        return true;
                      } catch (error) {
                        message.error(String(error));
                        return false;
                      }
                    }}
                    submitter={{ submitButtonProps: { loading: save.isPending } }}
                  >
                    <ProFormText name="nickName" label="昵称" rules={[{ required: true }]} />
                    <ProFormText name="email" label="邮箱" rules={[{ type: 'email' }]} />
                    <ProFormText
                      name="phonenumber"
                      label="手机号"
                      rules={[{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' }]}
                    />
                    <ProFormRadio.Group
                      name="sex"
                      label="性别"
                      options={[
                        { label: '男', value: '0' },
                        { label: '女', value: '1' },
                        { label: '未知', value: '2' },
                      ]}
                    />
                  </ProForm>
                ),
              },
              {
                key: 'password',
                label: '修改密码',
                children: (
                  <ProForm<{ oldPassword: string; newPassword: string; confirmPassword: string }>
                    formRef={passwordForm}
                    onFinish={async (values) => {
                      try {
                        await password.mutateAsync({
                          userId: data.user.userId,
                          oldPassword: values.oldPassword,
                          newPassword: values.newPassword,
                        });
                        passwordForm.current?.resetFields();
                        password.reset();
                        message.success('密码修改成功');
                        return true;
                      } catch (error) {
                        message.error(String(error));
                        return false;
                      }
                    }}
                    autoFocusFirstInput={false}
                    submitter={{ submitButtonProps: { loading: password.isPending } }}
                    onFinishFailed={() => message.error('请检查密码输入')}
                  >
                    <ProFormText.Password
                      name="oldPassword"
                      label="旧密码"
                      rules={[{ required: true }]}
                    />
                    <ProFormText.Password
                      name="newPassword"
                      label="新密码"
                      rules={[
                        { required: true },
                        { min: 6, max: 20 },
                        { pattern: /^[^<>"'|\\]+$/, message: '密码不能包含非法字符' },
                        ({ getFieldValue }) => ({
                          validator: (_, value: string) =>
                            value && value === getFieldValue('oldPassword')
                              ? Promise.reject(new Error('新密码不能与旧密码相同'))
                              : Promise.resolve(),
                        }),
                      ]}
                    />
                    <ProFormText.Password
                      name="confirmPassword"
                      label="确认密码"
                      dependencies={['newPassword']}
                      rules={[
                        { required: true },
                        ({ getFieldValue }) => ({
                          validator: (_, value: string) =>
                            value === getFieldValue('newPassword')
                              ? Promise.resolve()
                              : Promise.reject(new Error('两次密码不一致')),
                        }),
                      ]}
                    />
                  </ProForm>
                ),
              },
            ]}
          />
        </Space>
      </Card>
      {avatarFile && (
        <AvatarCropper
          file={avatarFile}
          onCancel={() => setAvatarFile(undefined)}
          onSubmit={async (file) => {
            const result = await avatar.mutateAsync(file);
            updateSession({ avatar: result.imgUrl });
            message.success('头像更新成功');
            void client.invalidateQueries({ queryKey: profileKeys.detail });
            setAvatarFile(undefined);
          }}
        />
      )}
    </PageContainer>
  );
}
