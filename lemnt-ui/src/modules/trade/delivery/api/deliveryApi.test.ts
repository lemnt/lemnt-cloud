import { beforeEach, expect, it, vi } from 'vitest';
import * as api from './deliveryApi';
import { yuanToFen, fenToYuan } from '@/shared/money';
const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => mockedRequest.mockReset());
const charge = {
  id: '9223372036854775807',
  areaIds: [110101],
  startCount: '1',
  extraCount: '2',
  startPrice: '0.29',
  extraPrice: '1.01',
};
const template = {
  id: '9',
  name: '北京',
  chargeMode: 1,
  sort: 0,
  charges: [charge],
  frees: [{ id: '20', areaIds: [110101], freeCount: 3, freePrice: '100.01' }],
};
it('元和分精确换算，拒绝负数、多余小数、指数和溢出', () => {
  expect(yuanToFen('0.29')).toBe(29);
  expect(yuanToFen('21474836.47')).toBe(2147483647);
  expect(fenToYuan(10001)).toBe('100.01');
  for (const value of ['-1', '0.001', '1e2', '21474836.48', ''])
    expect(() => yuanToFen(value)).toThrow();
});
it('模板详情保留子规则大ID，编辑以分提交并且不混入主表ID', async () => {
  mockedRequest.mockResolvedValueOnce({
    data: {
      ...template,
      charges: [{ ...charge, startCount: 1, extraCount: 2, startPrice: 29, extraPrice: 101 }],
      frees: [{ ...template.frees[0], freePrice: 10001 }],
    },
  });
  const detail = await api.getTemplate('9');
  expect(detail).toEqual(template);
  mockedRequest.mockResolvedValueOnce({ data: true });
  await api.saveTemplate('9', detail);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/delivery/templates/9',
    method: 'PUT',
    data: {
      name: '北京',
      chargeMode: 1,
      sort: 0,
      charges: [{ ...charge, startPrice: 29, extraPrice: 101 }],
      frees: [{ ...template.frees[0], freePrice: 10001 }],
    },
  });
});
it('规则校验遵循按件整数、地区必填和同类区域互斥，计费与包邮可重叠', () => {
  expect(() => api.templatePayload(template)).not.toThrow();
  expect(() => api.templatePayload({ ...template, charges: [] })).toThrow();
  expect(() =>
    api.templatePayload({ ...template, charges: [charge, { ...charge, id: undefined }] }),
  ).toThrow('重复');
  expect(() => api.templatePayload({ ...template, charges: [{ ...charge, areaIds: [] }] })).toThrow(
    '区域',
  );
  expect(() =>
    api.templatePayload({ ...template, charges: [{ ...charge, startCount: '1.1' }] }),
  ).toThrow('整数');
  expect(
    api.templatePayload({
      ...template,
      chargeMode: 2,
      charges: [{ ...charge, startCount: '0.000001' }],
    }).charges[0].startCount,
  ).toBe('0.000001');
  expect(() =>
    api.templatePayload({
      ...template,
      chargeMode: 2,
      charges: [{ ...charge, startCount: '0.0000001' }],
    }),
  ).toThrow();
});
it('公司状态0不丢失，分页与模板的筛选字段各自独立', async () => {
  mockedRequest.mockResolvedValue({ rows: [], total: 0 });
  await api.getExpresses({ name: ' 顺丰 ', status: 0, pageNum: 1, pageSize: 20 });
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/delivery/expresses',
    params: { name: '顺丰', status: 0, pageNum: 1, pageSize: 20 },
  });
  await api.getTemplates({ pageNum: 2, pageSize: 10, ...{ status: 1 } });
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/delivery/templates',
    params: { name: undefined, pageNum: 2, pageSize: 10 },
  });
});
it('新增、删除与失败响应正确处理，不把false当作成功', async () => {
  mockedRequest.mockResolvedValueOnce({ data: '10' });
  expect(
    await api.saveExpress(null, { name: '顺丰', code: 'SF', logo: '', sort: 0, status: 0 }),
  ).toBe('10');
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/delivery/expresses',
    method: 'POST',
    data: { name: '顺丰', code: 'SF', logo: null, sort: 0, status: 0 },
  });
  mockedRequest.mockResolvedValueOnce({ data: false });
  await expect(api.deleteTemplate('9')).rejects.toThrow('未成功');
  mockedRequest.mockRejectedValueOnce(Error('模板正在使用'));
  await expect(api.deleteExpress('9')).rejects.toThrow('正在使用');
  mockedRequest.mockResolvedValueOnce({ data: true });
  expect(await api.deleteExpress('9')).toBe(true);
  mockedRequest.mockResolvedValueOnce({
    data: { id: Number.MAX_SAFE_INTEGER + 1, name: '顺丰', code: 'SF', sort: 0, status: 0 },
  });
  await expect(api.getExpress('9')).rejects.toThrow('精度');
});
