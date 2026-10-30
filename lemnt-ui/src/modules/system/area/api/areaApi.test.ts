import { expect, it, vi } from 'vitest';
import { decodeAreas, areaName, areaOptions, getAreas } from './areaApi';
const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
const tree = [
  {
    id: 110000,
    name: '北京市',
    children: [
      { id: 110100, name: '北京市', children: [{ id: 110101, name: '东城区', children: [] }] },
    ],
  },
];
it('地区名称由真实树路径派生，未知地区不补造', () => {
  const data = decodeAreas(tree);
  expect(areaName(data, 110101)).toBe('北京市 / 北京市 / 东城区');
  expect(areaName(data, 999999)).toBeUndefined();
  expect(areaOptions(data)[0].value).toBe(110000);
});
it('拒绝重复编号、缺名和损坏子树', () => {
  expect(() => decodeAreas([tree[0], tree[0]])).toThrow();
  expect(() => decodeAreas([{ id: 1, name: '', children: [] }])).toThrow();
  expect(() => decodeAreas([{ id: 1, name: '地区', children: null }])).toThrow();
});
it('地区树通过system独立接口读取', async () => {
  mockedRequest.mockResolvedValue({ data: tree });
  expect(await getAreas()).toEqual(tree);
  expect(mockedRequest).toHaveBeenCalledWith({ url: '/areas/tree' });
});
