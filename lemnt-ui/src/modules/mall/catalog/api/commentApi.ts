import { request } from '@/api/request';
import { isAxiosError } from 'axios';
import { decodeId } from '../catalogDecoders';
import type { ProductCommentRecord, ProductCommentQuery } from '../commentTypes';
function object(value: unknown): Record<string, unknown> {
  if (!value || typeof value !== 'object' || Array.isArray(value)) throw Error('评价响应不合法');
  return value as Record<string, unknown>;
}
function text(value: unknown): string {
  if (typeof value !== 'string') throw Error('评价文本不合法');
  return value;
}
function optionalText(value: unknown) {
  return value == null ? null : text(value);
}
function flag(value: unknown): boolean {
  if (typeof value !== 'boolean') throw Error('评价标记不合法');
  return value;
}
function score(value: unknown): number {
  if (typeof value !== 'number' || !Number.isSafeInteger(value) || value < 1 || value > 5)
    throw Error('评价评分不合法');
  return value;
}
function optionalId(value: unknown) {
  return value == null ? null : decodeId(value);
}
function list<T>(value: unknown, decode: (value: unknown) => T): T[] {
  if (!Array.isArray(value)) throw Error('评价列表不合法');
  return value.map(decode);
}
export function decodeComment(value: unknown): ProductCommentRecord {
  const row = object(value);
  return {
    id: decodeId(row.id),
    spuId: decodeId(row.spuId),
    skuId: decodeId(row.skuId),
    orderNo: text(row.orderNo),
    orderId: optionalId(row.orderId),
    memberId: decodeId(row.memberId),
    score: score(row.score),
    descriptionScores: row.descriptionScores == null ? null : score(row.descriptionScores),
    benefitScores: row.benefitScores == null ? null : score(row.benefitScores),
    content: text(row.content),
    visible: flag(row.visible),
    anonymous: row.anonymous == null ? null : flag(row.anonymous),
    userNickname: optionalText(row.userNickname),
    userAvatar: optionalText(row.userAvatar),
    spuName: optionalText(row.spuName),
    skuPicUrl: optionalText(row.skuPicUrl),
    skuProperties:
      row.skuProperties == null
        ? null
        : list(row.skuProperties, (value) => {
            const p = object(value);
            return {
              propertyId: optionalId(p.propertyId),
              propertyName: text(p.propertyName),
              valueId: optionalId(p.valueId),
              valueName: text(p.valueName),
            };
          }),
    picUrls: row.picUrls == null ? null : list(row.picUrls, text),
    replyStatus: flag(row.replyStatus),
    replyContent: optionalText(row.replyContent),
    replyUserId: optionalId(row.replyUserId),
    replyTime: optionalText(row.replyTime),
    createTime: text(row.createTime),
  };
}
/** 统一异常的detail用于业务提示，成功仍使用R响应。 */
async function call<T>(task: () => Promise<T>): Promise<T> {
  try {
    return await task();
  } catch (reason) {
    if (isAxiosError(reason) && typeof reason.response?.data?.detail === 'string')
      throw Error(reason.response.data.detail);
    throw reason;
  }
}
export async function getComments(params: ProductCommentQuery) {
  return call(async () => {
    const result = await request<unknown[]>({ url: '/mall/comments', params });
    if (typeof result.total !== 'number' || !Number.isSafeInteger(result.total) || result.total < 0)
      throw Error('评价总数不合法');
    return { data: list(result.rows, decodeComment), total: result.total, success: true };
  });
}
export async function getComment(id: string) {
  return call(async () =>
    decodeComment((await request<unknown>({ url: '/mall/comments/' + decodeId(id) })).data),
  );
}
export async function replyComment(id: string, content: string) {
  return call(async () =>
    decodeComment(
      (
        await request<unknown>({
          url: '/mall/comments/' + decodeId(id) + '/reply',
          method: 'PUT',
          data: { content },
        })
      ).data,
    ),
  );
}
export async function setCommentVisible(id: string, visible: boolean) {
  return call(async () => {
    const result = await request<unknown>({
      url: '/mall/comments/' + decodeId(id) + '/visible',
      method: 'PUT',
      data: { visible },
    });
    if (object(result.data).success !== true) throw Error('显隐修改未确认，请刷新核验');
  });
}
