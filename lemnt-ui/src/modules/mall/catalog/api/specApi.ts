import { request } from '@/api/request';
import { decodeId, decodeList, decodeTotal } from '../catalogDecoders';

/** 规格新增、修改字段。 */
export interface SpecValues {
  name: string;
  remark?: string | null;
}
/** 规格字典记录，历史创建时间可能缺失。 */
export interface ProductSpec extends SpecValues {
  id: string;
  createTime: string | null;
}
/** 规格值属于一个固定规格，编辑不能迁移归属。 */
export interface ProductSpecValue extends ProductSpec {
  specId: string;
}
/** 规格分页查询条件。 */
export interface SpecFilter {
  name?: string;
  pageNum: number;
  pageSize: number;
  createTimeStart?: string;
  createTimeEnd?: string;
}
/** 规格值分页查询条件，与规格项独立。 */
export interface SpecValueFilter {
  specId: string;
  name?: string;
  pageNum: number;
  pageSize: number;
}
/** 保留Long大编号，拒绝越界或已丢失精度的值。 */
export function specId(raw: unknown): string {
  const value = decodeId(raw);
  if (value.length > 19 || BigInt(value) > 9223372036854775807n) throw Error('规格编号超出范围');
  return value;
}
function record(raw: unknown): Record<string, unknown> {
  if (!raw || typeof raw !== 'object' || Array.isArray(raw)) throw Error('规格响应不合法');
  return raw as Record<string, unknown>;
}
function spec(raw: unknown): ProductSpec {
  const v = record(raw);
  if (
    typeof v.name !== 'string' ||
    (v.remark != null && typeof v.remark !== 'string') ||
    (v.createTime != null && typeof v.createTime !== 'string')
  )
    throw Error('规格文本字段不合法');
  return {
    id: specId(v.id),
    name: v.name,
    remark: v.remark == null ? null : (v.remark as string),
    createTime: v.createTime == null ? null : (v.createTime as string),
  };
}
function specValue(raw: unknown): ProductSpecValue {
  return { ...spec(raw), specId: specId(record(raw).specId) };
}
function fields(v: SpecValues, max: number) {
  const name = v.name.trim(),
    remark = v.remark?.trim() || null;
  if (!name || name.length > max || (remark?.length ?? 0) > 500)
    throw Error(`名称必填且最多${max}字，备注最多500字`);
  return { name, remark };
}
function filters(q: SpecFilter) {
  if (q.createTimeStart && q.createTimeEnd && q.createTimeStart > q.createTimeEnd)
    throw Error('创建时间范围不合法');
  return {
    name: q.name?.trim() || undefined,
    pageNum: q.pageNum,
    pageSize: q.pageSize,
    createTimeStart: q.createTimeStart || undefined,
    createTimeEnd: q.createTimeEnd || undefined,
  };
}
/** 查询规格项分页，使用统一rows/total响应。 */
export async function getSpecs(q: SpecFilter) {
  const r = await request<unknown>({ url: '/mall/specs', params: filters(q) });
  return { records: decodeList(r.rows, spec), total: decodeTotal(r.total) };
}
/** 查询固定规格下的规格值分页。 */
export async function getSpecValues(q: SpecValueFilter) {
  const r = await request<unknown>({
    url: '/mall/spec-values',
    params: {
      name: q.name?.trim() || undefined,
      pageNum: q.pageNum,
      pageSize: q.pageSize,
      specId: specId(q.specId),
    },
  });
  const records = decodeList(r.rows, specValue);
  if (records.some((row) => row.specId !== specId(q.specId)))
    throw Error('规格值归属与查询条件不一致');
  return { records, total: decodeTotal(r.total) };
}
/** 查询规格详情，更新前必须读取真实记录。 */
export async function getSpec(id: string) {
  const row = spec((await request<unknown>({ url: '/mall/specs/' + specId(id) })).data);
  if (row.id !== specId(id)) throw Error('规格详情编号不一致');
  return row;
}
/** 查询规格值详情，核对所属规格。 */
export async function getSpecValue(id: string, parentId: string) {
  const row = specValue((await request<unknown>({ url: '/mall/spec-values/' + specId(id) })).data);
  if (row.id !== specId(id) || row.specId !== specId(parentId)) throw Error('规格值详情归属不一致');
  return row;
}
/** 保存规格，仅提交名称和备注，新增/修改均返回记录。 */
export async function saveSpec(id: string | null, values: SpecValues) {
  const row = spec(
    (
      await request<unknown>({
        url: '/mall/specs' + (id ? '/' + specId(id) : ''),
        method: id ? 'PUT' : 'POST',
        data: fields(values, 64),
      })
    ).data,
  );
  if (id && row.id !== specId(id)) throw Error('规格保存响应编号不一致');
  return row;
}
/** 新增时提交所属规格；编辑接口不允许发送specId变更归属。 */
export async function saveSpecValue(id: string | null, parentId: string, values: SpecValues) {
  const body = fields(values, 100),
    parent = specId(parentId);
  const row = specValue(
    (
      await request<unknown>({
        url: '/mall/spec-values' + (id ? '/' + specId(id) : ''),
        method: id ? 'PUT' : 'POST',
        data: id ? body : { ...body, specId: parent },
      })
    ).data,
  );
  if (row.specId !== parent || (id && row.id !== specId(id)))
    throw Error('规格值保存响应归属不一致');
  return row;
}
/** 删除规格项；服务端仍有子值时拒绝。 */
export async function deleteSpec(id: string) {
  if (
    (await request<unknown>({ url: '/mall/specs/' + specId(id), method: 'DELETE' })).data !== true
  )
    throw Error('删除规格未成功');
  return true;
}
/** 删除规格值，保留现有SKU及订单快照。 */
export async function deleteSpecValue(id: string) {
  if (
    (await request<unknown>({ url: '/mall/spec-values/' + specId(id), method: 'DELETE' })).data !==
    true
  )
    throw Error('删除规格值未成功');
  return true;
}
