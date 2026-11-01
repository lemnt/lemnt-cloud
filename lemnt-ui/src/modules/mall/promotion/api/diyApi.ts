import { request } from '@/api/request';
import type { DiyPage, DiyTemplate } from '../types';

const templatePath = '/mall/promotion/diy-templates';
const pagePath = '/mall/promotion/diy-pages';

function object(v: unknown): Record<string, unknown> {
  if (!v || typeof v !== 'object' || Array.isArray(v)) throw Error('装修响应格式不合法');
  return v as Record<string, unknown>;
}
function id(v: unknown, field = '编号'): string {
  const value = typeof v === 'number' && Number.isSafeInteger(v) ? String(v) : v;
  if (typeof value !== 'string' || !/^[1-9]\d{0,18}$/.test(value) || BigInt(value) > 9223372036854775807n)
    throw Error(`${field}不合法或已丢失精度`);
  return value;
}
function text(v: unknown, field: string): string {
  if (typeof v !== 'string') throw Error(`${field}不合法`);
  return v;
}
function optionalText(v: unknown, field: string): string | null {
  return v == null ? null : text(v, field);
}
function previews(v: unknown): string[] {
  if (!Array.isArray(v)) throw Error('预览图不合法');
  return v.map(item => text(item, '预览图'));
}
function property(v: unknown): string {
  const value = text(v, '装修属性');
  if (!value.trim() || (value[0] !== '{' && value[0] !== '[')) throw Error('装修属性必须是JSON对象或数组');
  return value;
}
function template(raw: unknown): DiyTemplate {
  const v = object(raw);
  if (typeof v.used !== 'boolean') throw Error('模板启用状态不合法');
  return {
    id: id(v.id, '模板编号'),
    name: text(v.name, '模板名称'),
    used: v.used,
    usedTime: optionalText(v.usedTime, '启用时间'),
    remark: optionalText(v.remark, '备注'),
    previewPicUrls: previews(v.previewPicUrls),
    property: property(v.property),
  };
}
function page(raw: unknown): DiyPage {
  const v = object(raw);
  return {
    id: id(v.id, '页面编号'),
    templateId: id(v.templateId, '模板编号'),
    name: text(v.name, '页面名称'),
    remark: optionalText(v.remark, '备注'),
    previewPicUrls: previews(v.previewPicUrls),
    property: property(v.property),
  };
}
function pageResult<T>(raw: { total?: unknown }, rows: unknown, decoder: (v: unknown) => T) {
  if (typeof raw.total !== 'number' || !Number.isSafeInteger(raw.total) || raw.total < 0)
    throw Error('装修总数不合法');
  if (!Array.isArray(rows)) throw Error('装修列表不合法');
  return { records: (rows as unknown[]).map(decoder), total: raw.total };
}
export async function getDiyTemplatePage(input: { name?: string; used?: boolean; pageNum: number; pageSize: number }) {
  const r = await request<unknown>({ url: templatePath, params: input });
  return pageResult(r, r.rows, template);
}
export async function getDiyTemplate(templateId: string) {
  return template((await request<unknown>({ url: `${templatePath}/${id(templateId)}` })).data);
}
export async function createDiyTemplate(data: { name: string; remark?: string | null; previewPicUrls: string[] }) {
  const r = await request<unknown>({ url: templatePath, method: 'POST', data });
  return id(r.data, '模板编号');
}
export async function updateDiyTemplate(templateId: string, data: { name: string; remark?: string | null; previewPicUrls: string[] }) {
  await request<unknown>({ url: `${templatePath}/${id(templateId)}`, method: 'PUT', data });
}
export async function useDiyTemplate(templateId: string) {
  await request<unknown>({ url: `${templatePath}/${id(templateId)}/use`, method: 'PUT' });
}
export async function deleteDiyTemplate(templateId: string) {
  await request<unknown>({ url: `${templatePath}/${id(templateId)}`, method: 'DELETE' });
}
export async function updateDiyTemplateProperty(templateId: string, value: string) {
  await request<unknown>({ url: `${templatePath}/${id(templateId)}/property`, method: 'PUT', data: { property: property(value) } });
}
export async function getDiyPagePage(input: { templateId?: string; name?: string; pageNum: number; pageSize: number }) {
  const r = await request<unknown>({ url: pagePath, params: input });
  return pageResult(r, r.rows, page);
}
export async function getDiyPage(pageId: string) {
  return page((await request<unknown>({ url: `${pagePath}/${id(pageId)}` })).data);
}
export async function createDiyPage(data: { templateId: string; name: string; remark?: string | null; previewPicUrls: string[] }) {
  const r = await request<unknown>({ url: pagePath, method: 'POST', data });
  return id(r.data, '页面编号');
}
export async function updateDiyPage(pageId: string, data: { templateId: string; name: string; remark?: string | null; previewPicUrls: string[] }) {
  await request<unknown>({ url: `${pagePath}/${id(pageId)}`, method: 'PUT', data });
}
export async function deleteDiyPage(pageId: string) {
  await request<unknown>({ url: `${pagePath}/${id(pageId)}`, method: 'DELETE' });
}
export async function updateDiyPageProperty(pageId: string, value: string) {
  await request<unknown>({ url: `${pagePath}/${id(pageId)}/property`, method: 'PUT', data: { property: property(value) } });
}
