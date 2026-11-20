import { yuanToFen, fenToYuan } from '@/shared/money';
import { request } from '@/api/request';
/** 快递公司编辑字段。 */
export interface ExpressValues {
  code: string;
  name: string;
  logo?: string | null;
  sort: number;
  status: number;
}
/** 快递公司详情。 */
export interface Express extends ExpressValues {
  id: string;
}
/** 运费模板分页摘要，不含计费规则。 */
export interface TemplateSummary {
  id: string;
  name: string;
  sort: number;
  chargeMode: number;
}
/** 计费规则编辑字段，金额为元文本。 */
export interface ChargeValues {
  id?: string;
  areaIds: number[];
  startCount: string;
  startPrice: string;
  extraCount: string;
  extraPrice: string;
}
/** 包邮规则编辑字段，金额为元文本。 */
export interface FreeValues {
  id?: string;
  areaIds: number[];
  freeCount: number;
  freePrice: string;
}
/** 运费模板编辑字段。 */
export interface TemplateValues {
  name: string;
  sort: number;
  chargeMode: number;
  charges: ChargeValues[];
  frees: FreeValues[];
}
/** 完整运费模板详情。 */
export interface Template extends TemplateValues {
  id: string;
}
/** 快递公司分页筛选。 */
export interface ExpressFilter {
  name?: string;
  status?: number;
  pageNum: number;
  pageSize: number;
}
/** 运费模板分页筛选。 */
export interface TemplateFilter {
  name?: string;
  pageNum: number;
  pageSize: number;
}
const base = '/trade/admin/delivery';
function object(v: unknown): Record<string, unknown> {
  if (!v || typeof v !== 'object' || Array.isArray(v)) throw Error('配送响应不合法');
  return v as Record<string, unknown>;
}
function array(v: unknown): unknown[] {
  if (!Array.isArray(v)) throw Error('配送列表不合法');
  return v;
}
function text(v: unknown): string {
  if (typeof v !== 'string') throw Error('配送文本不合法');
  return v;
}
function integer(v: unknown): number {
  if (typeof v !== 'number' || !Number.isSafeInteger(v) || v < 0 || v > 2147483647)
    throw Error('配送整数不合法');
  return v;
}
function id(v: unknown): string {
  const value = typeof v === 'number' && Number.isSafeInteger(v) ? String(v) : v;
  if (
    typeof value !== 'string' ||
    !/^[1-9]\d{0,18}$/.test(value) ||
    BigInt(value) > 9223372036854775807n
  )
    throw Error('配送编号不合法或丢失精度');
  return value;
}
function quantity(v: unknown, mode: number): string {
  const value = String(v);
  if (
    !/^\d+(\.\d{1,6})?$/.test(value) ||
    Number(value) <= 0 ||
    Number(value) > 999999999999 ||
    (mode === 1 && !Number.isInteger(Number(value)))
  )
    throw Error('首量或续量不合法：按件须整数，其他最多六位小数');
  return value;
}
function areas(v: unknown): number[] {
  const values = array(v).map(integer);
  if (
    !values.length ||
    values.length > 10000 ||
    values.some((v) => !v) ||
    new Set(values).size !== values.length
  )
    throw Error('配送区域不能为空或重复');
  return values;
}
function summary(raw: unknown): TemplateSummary {
  const v = object(raw),
    mode = integer(v.chargeMode);
  if (![1, 2, 3].includes(mode)) throw Error('计费方式不合法');
  return { id: id(v.id), name: text(v.name), sort: integer(v.sort), chargeMode: mode };
}
function express(raw: unknown): Express {
  const v = object(raw),
    status = integer(v.status);
  if (status > 1) throw Error('快递公司状态不合法');
  return {
    id: id(v.id),
    name: text(v.name),
    code: text(v.code),
    logo: v.logo == null ? null : text(v.logo),
    sort: integer(v.sort),
    status,
  };
}
function template(raw: unknown): Template {
  const v = object(raw),
    main = summary(v);
  return {
    ...main,
    charges: array(v.charges).map((raw) => {
      const row = object(raw);
      return {
        id: row.id == null ? undefined : id(row.id),
        areaIds: areas(row.areaIds),
        startCount: quantity(row.startCount, main.chargeMode),
        extraCount: quantity(row.extraCount, main.chargeMode),
        startPrice: fenToYuan(row.startPrice),
        extraPrice: fenToYuan(row.extraPrice),
      };
    }),
    frees: array(v.frees).map((raw) => {
      const row = object(raw);
      return {
        id: row.id == null ? undefined : id(row.id),
        areaIds: areas(row.areaIds),
        freeCount: integer(row.freeCount),
        freePrice: fenToYuan(row.freePrice),
      };
    }),
  };
}
function total(v: unknown): number {
  if (typeof v !== 'number' || !Number.isSafeInteger(v) || v < 0) throw Error('分页总数不合法');
  return v;
}
async function confirmed(config: Parameters<typeof request>[0]) {
  if ((await request<unknown>(config)).data !== true) throw Error('操作未成功');
  return true;
}
/** 查询快递公司分页，保留状态0筛选。 */
export async function getExpresses(q: ExpressFilter) {
  const r = await request<unknown>({
    url: base + '/expresses',
    params: {
      name: q.name?.trim() || undefined,
      status: q.status,
      pageNum: q.pageNum,
      pageSize: q.pageSize,
    },
  });
  return { records: array(r.rows).map(express), total: total(r.total) };
}
/** 查询快递公司详情。 */
export async function getExpress(key: string) {
  return express((await request<unknown>({ url: base + '/expresses/' + id(key) })).data);
}
/** 新增或修改快递公司，仅提交编辑字段。 */
export async function saveExpress(key: string | null, v: ExpressValues) {
  const data = {
    code: v.code.trim(),
    name: v.name.trim(),
    logo: v.logo?.trim() || null,
    sort: integer(v.sort),
    status: integer(v.status),
  };
  if (
    !data.code ||
    data.code.length > 40 ||
    !data.name ||
    data.name.length > 100 ||
    (data.logo?.length ?? 0) > 500 ||
    data.status > 1
  )
    throw Error('快递公司字段不合法');
  const config = {
    url: base + '/expresses' + (key ? '/' + id(key) : ''),
    method: key ? 'PUT' : 'POST',
    data,
  };
  return key ? confirmed(config) : id((await request<unknown>(config)).data);
}
/** 删除快递公司，确认服务端成功后返回。 */
export function deleteExpress(key: string) {
  return confirmed({ url: base + '/expresses/' + id(key), method: 'DELETE' });
}
/** 查询运费模板摘要分页。 */
export async function getTemplates(q: TemplateFilter) {
  const r = await request<unknown>({
    url: base + '/templates',
    params: { name: q.name?.trim() || undefined, pageNum: q.pageNum, pageSize: q.pageSize },
  });
  return { records: array(r.rows).map(summary), total: total(r.total) };
}
/** 查询模板及计费、包邮规则详情。 */
export async function getTemplate(key: string) {
  return template((await request<unknown>({ url: base + '/templates/' + id(key) })).data);
}
/** 保留当前模板规则ID；省略的规则由服务端删除，不重建所有规则。 */
export function templatePayload(v: TemplateValues) {
  const name = v.name.trim(),
    mode = integer(v.chargeMode);
  if (
    !name ||
    name.length > 100 ||
    ![1, 2, 3].includes(mode) ||
    !v.charges.length ||
    v.charges.length > 100 ||
    v.frees.length > 100
  )
    throw Error('模板名称、计费方式或规则数量不合法');
  const used = [new Set<number>(), new Set<number>()];
  function region(ids: number[], kind: number) {
    const values = areas(ids);
    for (const key of values) {
      if (used[kind].has(key)) throw Error('同类规则不能重复配置地区');
      used[kind].add(key);
    }
    return values;
  }
  return {
    name,
    sort: integer(v.sort),
    chargeMode: mode,
    charges: v.charges.map((row) => ({
      id: row.id == null ? undefined : id(row.id),
      areaIds: region(row.areaIds, 0),
      startCount: quantity(row.startCount, mode),
      extraCount: quantity(row.extraCount, mode),
      startPrice: yuanToFen(row.startPrice),
      extraPrice: yuanToFen(row.extraPrice),
    })),
    frees: v.frees.map((row) => ({
      id: row.id == null ? undefined : id(row.id),
      areaIds: region(row.areaIds, 1),
      freeCount: integer(row.freeCount),
      freePrice: yuanToFen(row.freePrice),
    })),
  };
}
/** 新增或修改运费模板及规则。 */
export async function saveTemplate(key: string | null, v: TemplateValues) {
  const config = {
    url: base + '/templates' + (key ? '/' + id(key) : ''),
    method: key ? 'PUT' : 'POST',
    data: templatePayload(v),
  };
  return key ? confirmed(config) : id((await request<unknown>(config)).data);
}
/** 删除运费模板，业务引用限制由服务端校验。 */
export function deleteTemplate(key: string) {
  return confirmed({ url: base + '/templates/' + id(key), method: 'DELETE' });
}
