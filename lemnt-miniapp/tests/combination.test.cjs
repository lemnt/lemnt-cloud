const { test } = require('node:test');
const assert = require('node:assert/strict');
const { createCombinationClient } = require('../services/combination');

function setup(response) {
  const calls = [];
  const client = createCombinationClient({
    baseUrl: 'https://example.test', ready: true, getMemberToken: () => 'member-token',
    request: async (options) => { calls.push(options); return response; },
  });
  return { client, calls };
}

test('拼团活动列表、详情与可参团记录', async () => {
  const { client, calls } = setup({ statusCode: 200, data: { code: 200, rows: [{
    id: 1, name: '两人团', spuId: 2, spuName: '商品', picUrl: null, marketPrice: '10.00',
    combinationPrice: '5.00', status: 0, startTime: '2026-11-01 00:00:00',
    endTime: '2026-11-02 00:00:00', userSize: 2,
  }], total: 1 } });
  const page = await client.activities({ pageNum: 1 });
  assert.equal(page.records[0].combinationPrice, '5.00');
  assert.deepEqual(calls[0].data, { pageNum: 1, pageSize: 20 });
  assert.equal(calls[0].header.Authorization, undefined);

  const head = setup({ statusCode: 200, data: { code: 200, data: [{
    id: '9', activityId: '1', combinationPrice: '5.00', spuId: '2', spuName: '商品', picUrl: null,
    skuId: '3', count: 1, userId: '2', nickname: '会员', avatar: null, headId: 0, status: 0,
    orderId: null, userSize: 2, userCount: 1, virtualGroup: false, expireTime: '2026-11-01 01:00:00',
  }] } });
  assert.equal((await head.client.heads('1')).at(0).headId, '0');
});

test('我的拼团需要会员令牌，详情返回团长和成员', async () => {
  const { client, calls } = setup({ statusCode: 200, data: { code: 200, rows: [], total: 0 } });
  await client.records({ pageNum: 1 });
  assert.equal(calls[0].header.Authorization, 'Bearer member-token');
  assert.equal(calls[0].url, 'https://example.test/mall/app/combination/records');

  const detail = setup({ statusCode: 200, data: { code: 200, data: {
    headRecord: {
      id: '9', activityId: '1', combinationPrice: '5.00', spuId: '2', spuName: '商品', picUrl: null,
      skuId: '3', count: 1, userId: '2', nickname: '会员', avatar: null, headId: 0, status: 1,
      orderId: '7', userSize: 2, userCount: 2, virtualGroup: false, expireTime: '2026-11-01 01:00:00',
    },
    memberRecords: [], orderId: '7',
  } } });
  const value = await detail.client.record('9');
  assert.equal(value.head.status, 1);
  assert.equal(value.orderId, '7');
});

test('未登录不发起拼团记录请求', async () => {
  const calls = [];
  const client = createCombinationClient({
    baseUrl: 'https://example.test', ready: true, getMemberToken: () => undefined,
    request: async (options) => { calls.push(options); return {}; },
  });
  await assert.rejects(() => client.records({}), /登录/);
  assert.equal(calls.length, 0);
});
