export interface ProductCommentRecord {
  id: string;
  spuId: string;
  skuId: string;
  orderNo: string | null;
  orderId: string | null;
  orderItemId: string | null;
  memberId: string;
  score: number;
  descriptionScores: number | null;
  benefitScores: number | null;
  content: string;
  visible: boolean;
  anonymous: boolean | null;
  userNickname: string | null;
  userAvatar: string | null;
  spuName: string | null;
  skuPicUrl: string | null;
  skuProperties:
    | {
        specId: string | null;
        specName: string;
        specValueId: string | null;
        specValueName: string;
      }[]
    | null;
  picUrls: string[] | null;
  replyStatus: boolean;
  replyContent: string | null;
  replyUserId: string | null;
  replyTime: string | null;
  createTime: string;
}
export interface ProductCommentQuery {
  spuId?: string;
  memberId?: string;
  score?: number;
  visible?: boolean;
  replied?: boolean;
  userNickname?: string;
  orderId?: string;
  spuName?: string;
  createTimeStart?: string;
  createTimeEnd?: string;
  pageNum: number;
  pageSize: number;
}
