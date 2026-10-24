export interface CreateNoticeRequest {
  noticeTitle: string;
  noticeType: string;
  noticeContent?: string;
  status: string;
  remark?: string;
}
export interface Notice extends CreateNoticeRequest {
  noticeId: number;
  isRead?: boolean;
  createBy?: string;
  createTime?: string;
}
export interface NoticeTopResponse {
  list: Notice[];
  unreadCount: number;
}
export interface NoticeQuery {
  pageNum?: number;
  pageSize?: number;
  noticeTitle?: string;
  noticeType?: string;
  status?: string;
}
export interface NoticeReadUser {
  userId: number;
  userName: string;
  nickName: string;
  deptName?: string;
  phonenumber?: string;
  readTime: string;
}
export interface NoticeReadQuery {
  noticeId: number;
  searchValue?: string;
  pageNum?: number;
  pageSize?: number;
}
