import type { User } from '@/modules/system/user/types';

export interface ProfileResponse {
  user: User & { avatar?: string };
  roleGroup: string;
  postGroup: string;
}
export interface ProfileValues {
  nickName: string;
  email?: string;
  phonenumber?: string;
  sex: string;
}
export interface ProfileUpdateRequest extends ProfileValues {
  userId: number;
}
export interface PasswordUpdateRequest {
  userId: number;
  oldPassword: string;
  newPassword: string;
}
