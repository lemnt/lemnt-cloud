import { beforeEach, expect, it, vi } from 'vitest';
import { getProfile, updatePassword, updateProfile, uploadAvatar } from './profileApi';
const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => mockedRequest.mockReset());
it('读取当前用户资料响应', async () => {
  const data = { user: { userId: 1 }, roleGroup: '管理员', postGroup: '' };
  mockedRequest.mockResolvedValue({ data });
  expect(await getProfile()).toEqual(data);
});
it('资料和密码写入携带后端要求的用户 ID', async () => {
  const data = { userId: 1, nickName: '用户', sex: '0' };
  await updateProfile(data);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/system/users/profile',
    method: 'PUT',
    data,
  });
  const password = { userId: 1, oldPassword: 'old-password', newPassword: 'new-password' };
  await updatePassword(password);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/system/users/profile/update-password',
    method: 'PUT',
    data: password,
  });
});
it('头像使用 avatarfile 字段且不手工设置 multipart 头', async () => {
  mockedRequest.mockResolvedValue({ data: { imgUrl: '/avatar.png' } });
  const file = new File(['image'], 'avatar.png', { type: 'image/png' });
  expect(await uploadAvatar(file)).toEqual({ imgUrl: '/avatar.png' });
  const config = mockedRequest.mock.calls[0][0] as { data: FormData; headers?: unknown };
  expect(config.data.get('avatarfile')).toBe(file);
  expect(config.headers).toBeUndefined();
});
