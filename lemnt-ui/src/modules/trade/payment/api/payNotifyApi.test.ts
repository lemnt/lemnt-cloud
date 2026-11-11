import { beforeEach, expect, it, vi } from 'vitest';
import * as api from './payNotifyApi';

const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => mockedRequest.mockReset());

const task = {
  id: '9223372036854775807',
  type: 'PAY',
  bizNo: 'P1',
  orderNo: 'TR1',
  status: 'FAILED',
  notifyTimes: 2,
  maxNotifyTimes: 8,
  nextNotifyTime: '2026-10-31T12:00:00',
  lastExecuteTime: '2026-10-31T11:59:45',
  lastResponse: '核券失败',
  createTime: '2026-10-31T11:00:00',
};
const log = {
  notifyTimes: 2,
  status: 'FAILED',
  response: '核券失败',
  createTime: '2026-10-31T12:00:00',
};

it('派发任务分页保留大编号并严格解码状态', async () => {
  mockedRequest.mockResolvedValue({ rows: [task], total: 1 });
  const result = await api.getPayNotifyTasks({
    type: 'PAY',
    bizNo: ' P1 ',
    orderNo: 'TR1',
    status: 'FAILED',
    pageNum: 2,
    pageSize: 20,
  });
  expect(result.records[0]).toMatchObject({ id: task.id, notifyTimes: 2, maxNotifyTimes: 8 });
  expect(mockedRequest).toHaveBeenCalledWith({
    url: '/trade/admin/pay-notify-tasks',
    params: {
      type: 'PAY',
      bizNo: 'P1',
      orderNo: 'TR1',
      status: 'FAILED',
      pageNum: 2,
      pageSize: 20,
    },
  });
  mockedRequest.mockResolvedValue({ rows: [{ ...task, status: 'UNKNOWN' }], total: 1 });
  await expect(api.getPayNotifyTasks({ pageNum: 1, pageSize: 20 })).rejects.toThrow('派发状态');
  mockedRequest.mockResolvedValue({
    rows: [{ ...task, id: Number.MAX_SAFE_INTEGER + 1 }],
    total: 1,
  });
  await expect(api.getPayNotifyTasks({ pageNum: 1, pageSize: 20 })).rejects.toThrow('精度');
});

it('任务详情解码任务与尝试日志', async () => {
  mockedRequest.mockResolvedValue({ data: { task, logs: [log] } });
  const result = await api.getPayNotifyTask('9');
  expect(result.task).toMatchObject({
    type: 'PAY',
    status: 'FAILED',
    nextNotifyTime: task.nextNotifyTime,
  });
  expect(result.logs).toEqual([log]);
  expect(mockedRequest).toHaveBeenLastCalledWith({ url: '/trade/admin/pay-notify-tasks/9' });
  mockedRequest.mockResolvedValue({ data: { task, logs: [{ ...log, status: 'NOPE' }] } });
  await expect(api.getPayNotifyTask('9')).rejects.toThrow('日志状态');
});

it('重发要求服务端明确成功', async () => {
  mockedRequest.mockResolvedValue({ data: true });
  expect(await api.retryPayNotifyTask('9')).toBe(true);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/pay-notify-tasks/9/retry',
    method: 'POST',
  });
  mockedRequest.mockResolvedValue({ data: false });
  await expect(api.retryPayNotifyTask('9')).rejects.toThrow('未确认成功');
});
