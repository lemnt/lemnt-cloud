import { beforeEach, expect, it, vi } from 'vitest';
import {
  createDictData,
  deleteDictData,
  deleteDictType,
  getDictDataList,
  getDictTypeDetail,
  getDictTypeList,
  refreshDictCache,
} from './dictApi';
const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => mockedRequest.mockReset());

it('字典类型分页读取 rows/total，详情读取 data', async () => {
  mockedRequest.mockResolvedValue({ code: 200, rows: [{ dictId: 1 }], total: 5 });
  expect(await getDictTypeList({ pageNum: 2 })).toEqual({
    data: [{ dictId: 1 }],
    total: 5,
    success: true,
  });
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/system/dictionary-types',
    params: { pageNum: 2 },
  });
  mockedRequest.mockResolvedValue({ code: 200, data: { dictId: 1, dictType: 'test' } });
  expect(await getDictTypeDetail(1)).toEqual({ dictId: 1, dictType: 'test' });
});
it('字典数据始终传递所属字典类型', async () => {
  mockedRequest.mockResolvedValue({ code: 200, rows: [], total: 0 });
  await getDictDataList({ dictType: 'sys_normal_disable', pageNum: 1 });
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/system/dictionary-data',
    params: { dictType: 'sys_normal_disable', pageNum: 1 },
  });
});
it('字典零值键值、排序和默认标识不被转换', async () => {
  mockedRequest.mockResolvedValue({ code: 200, data: true });
  const data = {
    dictSort: 0,
    dictLabel: '正常',
    dictValue: '0',
    dictType: 'status',
    isDefault: '0',
    status: '1',
  };
  await createDictData(data);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/system/dictionary-data',
    method: 'POST',
    data,
  });
});
it('批量删除和刷新缓存使用后端 DELETE 路径', async () => {
  mockedRequest.mockResolvedValue({ code: 200, data: true });
  await deleteDictType([1, 2]);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/system/dictionary-types/1,2',
    method: 'DELETE',
  });
  await deleteDictData([3, 4]);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/system/dictionary-data/3,4',
    method: 'DELETE',
  });
  await refreshDictCache();
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/system/dictionary-types/refresh-cache',
    method: 'DELETE',
  });
});
