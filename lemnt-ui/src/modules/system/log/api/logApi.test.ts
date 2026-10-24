import { beforeEach, expect, it, vi } from 'vitest';
import {
  cleanLoginLog,
  cleanOperationLog,
  deleteLoginLog,
  deleteOperationLog,
  getLoginLogList,
  getOperationLogList,
} from './logApi';
import { logDateRange } from '../types';
const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => mockedRequest.mockReset());
it('日志列表读取顶层分页结果', async () => {
  mockedRequest.mockResolvedValue({ rows: [{ infoId: 1 }], total: 1 });
  expect(await getLoginLogList({ status: '1' })).toEqual({
    data: [{ infoId: 1 }],
    total: 1,
    success: true,
  });
  mockedRequest.mockResolvedValue({ rows: [], total: 0 });
  expect(await getOperationLogList({ status: 1 })).toEqual({ data: [], total: 0, success: true });
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/system/operation-logs',
    params: { status: 1 },
  });
});
it('删除使用路径 ID，清空使用独立端点', async () => {
  await deleteLoginLog([1, 2]);
  await deleteOperationLog([3]);
  await cleanLoginLog();
  await cleanOperationLog();
  expect(mockedRequest.mock.calls.map(([config]) => config)).toEqual([
    { url: '/system/login-logs/1,2', method: 'DELETE' },
    { url: '/system/operation-logs/3', method: 'DELETE' },
    { url: '/system/login-logs/clean', method: 'DELETE' },
    { url: '/system/operation-logs/clean', method: 'DELETE' },
  ]);
});
it('日期范围按后端 LocalDateTime 格式提交', () => {
  expect(logDateRange(['2026-09-01', '2026-09-30'])).toEqual({
    beginTime: '2026-09-01T00:00:00',
    endTime: '2026-09-30T23:59:59',
  });
  expect(logDateRange()).toEqual({});
});
