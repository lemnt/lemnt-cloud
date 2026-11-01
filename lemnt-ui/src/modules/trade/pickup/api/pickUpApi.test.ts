import { beforeEach, expect, it, vi } from 'vitest';
import * as api from './pickUpApi';

const { mockedRequest } = vi.hoisted(() => ({ mockedRequest: vi.fn() }));
vi.mock('@/api/request', () => ({ request: mockedRequest }));
beforeEach(() => mockedRequest.mockReset());

const store = {
  id: '1', name: '中心店', introduction: null, phone: '13800000000', areaId: '1',
  detailAddress: '中心路1号', logo: null, openingTime: '09:00:00', closingTime: '21:00:00',
  latitude: null, longitude: null, verifyUserIds: ['2'], status: 0,
};
const order = {
  orderNo: 'TR1', status: 'PAID', storeId: '1', storeName: '中心店', storePhone: '13800000000',
  storeAddress: '中心路1号', verifyCode: '12345678',
};

it('门店分页、保存、绑定与核销走管理端路径', async () => {
  mockedRequest.mockResolvedValue({ rows: [store], total: 1 });
  expect((await api.getStores({ pageNum: 1, pageSize: 20 })).records[0].name).toBe('中心店');
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/delivery/pick-up-stores', params: { pageNum: 1, pageSize: 20 },
  });
  mockedRequest.mockResolvedValue({ data: '2' });
  await api.bindStore('1', ['2']);
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/delivery/pick-up-stores/1/verify-users', method: 'PUT', data: { verifyUserIds: ['2'] },
  });
  mockedRequest.mockResolvedValue({ data: order });
  expect((await api.previewPickUp('12345678')).verifyCode).toBe('12345678');
  expect(mockedRequest).toHaveBeenLastCalledWith({ url: '/trade/admin/orders/pick-up/12345678' });
  expect((await api.verifyPickUp('12345678')).status).toBe('PAID');
  expect(mockedRequest).toHaveBeenLastCalledWith({ url: '/trade/admin/orders/pick-up/12345678/verify', method: 'POST' });
  await api.assignPickUp('TR1', '1');
  expect(mockedRequest).toHaveBeenLastCalledWith({
    url: '/trade/admin/orders/pick-up', method: 'POST', data: { orderNo: 'TR1', storeId: '1' },
  });
});

it('核销码和门店响应严格校验', async () => {
  await expect(api.previewPickUp('123')).rejects.toThrow('核销码');
  await expect(api.verifyPickUp('abcdefgh')).rejects.toThrow('核销码');
  mockedRequest.mockResolvedValue({ rows: [{ ...store, status: 2 }], total: 1 });
  await expect(api.getStores({ pageNum: 1, pageSize: 20 })).rejects.toThrow('状态');
});
