import { request } from '@/api/request';
export interface AreaNode {
  id: number;
  name: string;
  children: AreaNode[];
}
/** 只接受真实地区数据，不将缺失名称替换为编号。 */
export function decodeAreas(raw: unknown): AreaNode[] {
  const seen = new Set<number>();
  function nodes(value: unknown): AreaNode[] {
    if (!Array.isArray(value)) throw Error('地区树响应不合法');
    return value.map((item) => {
      if (!item || typeof item !== 'object') throw Error('地区节点不合法');
      const v = item as Record<string, unknown>;
      if (
        !Number.isSafeInteger(v.id) ||
        (v.id as number) <= 0 ||
        (v.id as number) > 2147483647 ||
        typeof v.name !== 'string' ||
        !v.name.trim() ||
        seen.has(v.id as number)
      )
        throw Error('地区节点编号或名称不合法');
      seen.add(v.id as number);
      return { id: v.id as number, name: v.name, children: nodes(v.children) };
    });
  }
  return nodes(raw);
}
export async function getAreas() {
  return decodeAreas((await request<unknown>({ url: '/areas/tree' })).data);
}
interface AreaOption {
  value: number;
  title: string;
  children: AreaOption[];
}
/** 用真实父子节点构建全称，未知编号返回空而不是捏造名称。 */
export function areaOptions(nodes: AreaNode[], parent = ''): AreaOption[] {
  return nodes.map((node) => {
    const title = parent ? parent + ' / ' + node.name : node.name;
    return { value: node.id, title, children: areaOptions(node.children, title) };
  });
}
export function areaName(nodes: AreaNode[], id: number): string | undefined {
  function find(options: AreaOption[]): string | undefined {
    for (const node of options) {
      if (node.value === id) return node.title;
      const result = find(node.children);
      if (result) return result;
    }
  }
  return find(areaOptions(nodes));
}
