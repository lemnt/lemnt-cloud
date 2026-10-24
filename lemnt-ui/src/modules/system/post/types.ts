export interface Post {
  postId: number;
  postCode: string;
  postName: string;
  postSort: number;
  status: string;
  remark?: string;
  createTime?: string;
}

export interface PostQuery {
  pageNum?: number;
  pageSize?: number;
  postCode?: string;
  postName?: string;
  status?: string;
}

export interface CreatePostRequest {
  postCode: string;
  postName: string;
  postSort: number;
  status: string;
  remark?: string;
}

export interface UpdatePostRequest extends CreatePostRequest {
  postId: number;
}
