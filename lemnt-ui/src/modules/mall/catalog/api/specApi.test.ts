import { beforeEach, expect, it, vi } from 'vitest';
import * as api from './specApi';
const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => mockedRequest.mockReset());
const spec = {
  id: '9223372036854775807',
  name: '颜色',
  remark: null,
  createTime: '2026-10-15 12:00:00',
};
const value = { id: '3', specId: spec.id, name: '红', remark: null, createTime: null };
it('规格分页保留大ID与创建时间，筛选只发送本接口字段', async () => {
  mockedRequest.mockResolvedValueOnce({ rows: [spec], total: 1 });
  expect(
    (
      await api.getSpecs({
        name: ' 颜色 ',
        pageNum: 2,
        pageSize: 20,
        createTimeStart: '2026-10-15T00:00:00',
        createTimeEnd: '2026-10-16T00:00:00',
        ...{ specId: '999' },
      })
    ).records,
  ).toEqual([spec]);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/specs',
    params: {
      name: '颜色',
      pageNum: 2,
      pageSize: 20,
      createTimeStart: '2026-10-15T00:00:00',
      createTimeEnd: '2026-10-16T00:00:00',
    },
  });
  await expect(
    api.getSpecs({
      pageNum: 1,
      pageSize: 20,
      createTimeStart: '2026-10-16T00:00:00',
      createTimeEnd: '2026-10-15T00:00:00',
    }),
  ).rejects.toThrow('时间范围');
});
it('规格值固定所属规格查询，跨规格响应拒绝显示', async () => {
  mockedRequest.mockResolvedValueOnce({ rows: [value], total: 1 });
  expect((await api.getSpecValues({ specId: spec.id, pageNum: 1, pageSize: 20 })).records).toEqual([
    value,
  ]);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/spec-values',
    params: {
      name: undefined,
      pageNum: 1,
      pageSize: 20,
      specId: spec.id,
    },
  });
  mockedRequest.mockResolvedValueOnce({ rows: [{ ...value, specId: '2' }], total: 1 });
  await expect(api.getSpecValues({ specId: spec.id, pageNum: 1, pageSize: 20 })).rejects.toThrow(
    '归属',
  );
});
it('规格新增及修改返回记录而非Boolean，不提交页面、时间或ID字段', async () => {
  mockedRequest.mockResolvedValueOnce({ data: spec });
  expect(
    await api.saveSpec(null, { name: ' 颜色 ', remark: '', ...{ id: '9', createTime: 'forged' } }),
  ).toMatchObject(spec);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/specs',
    method: 'POST',
    data: { name: '颜色', remark: null },
  });
  mockedRequest.mockResolvedValueOnce({ data: { ...spec, name: '色彩' } });
  await api.saveSpec(spec.id, { name: '色彩' });
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/specs/' + spec.id,
    method: 'PUT',
    data: { name: '色彩', remark: null },
  });
  mockedRequest.mockResolvedValueOnce({ data: true });
  await expect(api.saveSpec(spec.id, { name: '颜色' })).rejects.toThrow('响应');
});
it('新增值携带父规格，编辑值不发送specId，服务端返回归属必须一致', async () => {
  mockedRequest.mockResolvedValueOnce({ data: value });
  await api.saveSpecValue(null, spec.id, { name: '红', remark: null });
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/spec-values',
    method: 'POST',
    data: { name: '红', remark: null, specId: spec.id },
  });
  mockedRequest.mockResolvedValueOnce({ data: value });
  await api.saveSpecValue('3', spec.id, { name: '红', ...{ specId: '999', id: '999' } });
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/mall/spec-values/3',
    method: 'PUT',
    data: { name: '红', remark: null },
  });
  mockedRequest.mockResolvedValueOnce({ data: { ...value, specId: '999' } });
  await expect(api.saveSpecValue('3', spec.id, { name: '红' })).rejects.toThrow('归属');
});
it('详情验证编号与归属，缺失创建时间保持空值，不造历史时间', async () => {
  mockedRequest.mockResolvedValueOnce({ data: { id: '9', name: '尺寸' } });
  expect(await api.getSpec('9')).toEqual({ id: '9', name: '尺寸', remark: null, createTime: null });
  mockedRequest.mockResolvedValueOnce({ data: value });
  expect(await api.getSpecValue('3', spec.id)).toEqual(value);
  mockedRequest.mockResolvedValueOnce({ data: value });
  await expect(api.getSpecValue('3', '9')).rejects.toThrow('归属');
  mockedRequest.mockResolvedValueOnce({ data: spec });
  await expect(api.getSpec('9')).rejects.toThrow('编号');
  mockedRequest.mockResolvedValueOnce({ data: { ...spec, id: Number.MAX_SAFE_INTEGER + 1 } });
  await expect(api.getSpec('9')).rejects.toThrow('精度');
  expect(() => api.specId('9223372036854775808')).toThrow('范围');
});
it('必填和长度校验在提交前执行，删除失败不报告成功', async () => {
  await expect(api.saveSpec(null, { name: ' '.repeat(10) })).rejects.toThrow('必填');
  await expect(api.saveSpec(null, { name: '规'.repeat(65) })).rejects.toThrow('64');
  await expect(api.saveSpecValue(null, '1', { name: '值'.repeat(101) })).rejects.toThrow('100');
  expect(mockedRequest).not.toHaveBeenCalled();
  mockedRequest.mockResolvedValueOnce({ data: false });
  await expect(api.deleteSpec('1')).rejects.toThrow('未成功');
  mockedRequest.mockRejectedValueOnce(Error('规格仍包含规格值'));
  await expect(api.deleteSpec('1')).rejects.toThrow('仍包含');
  mockedRequest.mockResolvedValueOnce({ data: true });
  expect(await api.deleteSpecValue('3')).toBe(true);
  expect(mockedRequest).toHaveBeenLastCalledWith({ url: '/mall/spec-values/3', method: 'DELETE' });
});
