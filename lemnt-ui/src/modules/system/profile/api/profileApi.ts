import { request } from '@/api/request';
import type { PasswordUpdateRequest, ProfileResponse, ProfileUpdateRequest } from '../types';

/** 获取当前登录用户的个人资料。 */
export async function getProfile() {
  return (await request<ProfileResponse>({ url: '/system/users/profile' })).data;
}
/** 修改个人资料，用户 ID 使用当前登录用户的服务端结果。 */
export function updateProfile(data: ProfileUpdateRequest) {
  return request({ url: '/system/users/profile', method: 'PUT', data });
}
/** 修改密码，不向后端提交确认密码。 */
export function updatePassword(data: PasswordUpdateRequest) {
  return request({ url: '/system/users/profile/update-password', method: 'PUT', data });
}
/** 上传头像，浏览器自动设置 multipart boundary。 */
export async function uploadAvatar(file: File) {
  const data = new FormData();
  data.append('avatarfile', file);
  return (
    await request<{ imgUrl: string }>({ url: '/system/users/profile/avatar', method: 'POST', data })
  ).data;
}
