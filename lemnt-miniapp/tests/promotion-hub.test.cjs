const { test } = require('node:test');
const assert = require('node:assert/strict');
const { createPromotionHubClient } = require('../services/promotionHub');

function setup(data) {
  const calls = [];
  const client = createPromotionHubClient({
    baseUrl: 'https://example.test',
    ready: true,
    request: async (options) => {
      calls.push(options);
      return { statusCode: 200, data: { code: 200, data } };
    },
  });
  return { client, calls };
}
const section = {
  type: 'DISCOUNT',
  label: '限时折扣',
  items: [{ id: 6, name: '折扣活动', startTime: 's', endTime: 'e' }],
};

test('活动聚合与规则详情走公开接口', async () => {
  let x = setup([section]);
  const sections = await x.client.sections(5);
  assert.equal(sections[0].items[0].id, '6');
  assert.equal(x.calls[0].url, 'https://example.test/mall/app/promotion-hub/sections?limit=5');
  x = setup({
    id: 6,
    name: '折扣',
    startTime: 's',
    endTime: 'e',
    products: [
      { spuId: 1, skuId: 2, discountType: 'PERCENT', discountPercent: 8000, discountPrice: null },
    ],
  });
  assert.equal((await x.client.discount('6')).products[0].discountType, 'PERCENT');
  x = setup({
    id: 7,
    name: '满减',
    startTime: 's',
    endTime: 'e',
    conditionType: 'PRICE',
    rules: [
      {
        limit: '10.00',
        discountPrice: '1.00',
        freeDelivery: true,
        point: 5,
        giveCouponTemplateCounts: { 3: 1 },
      },
    ],
  });
  assert.equal((await x.client.reward('7')).rules[0].freeDelivery, true);
  assert.throws(() => x.client.reward('0'), /编号/);
});

let definition;
function page(name) {
  delete require.cache[require.resolve('../pages/' + name + '/index')];
  global.Page = (value) => {
    definition = value;
  };
  global.wx = {
    navigateTo: (value) => {
      global.lastNav = value.url;
    },
  };
  require('../pages/' + name + '/index');
  const value = { ...definition };
  value.data = JSON.parse(JSON.stringify(value.data));
  value.setData = function (next) {
    Object.assign(this.data, next);
  };
  return value;
}

test('活动中心按类型跳转活动或规则页', async () => {
  const p = page('promotion-hub');
  p.client = {
    sections: async () => [
      {
        type: 'SECKILL',
        label: '秒杀',
        items: [{ id: '1', name: '秒杀', startTime: 's', endTime: 'e' }],
      },
      {
        type: 'REWARD',
        label: '满减送',
        items: [{ id: '7', name: '满减', startTime: 's', endTime: 'e' }],
      },
    ],
  };
  await p.load();
  p.open({ currentTarget: { dataset: { type: 'SECKILL', id: '1' } } });
  assert.match(global.lastNav, /seckill-detail\/index\?id=1/);
  p.open({ currentTarget: { dataset: { type: 'REWARD', id: '7' } } });
  assert.match(global.lastNav, /promotion-rule\/index\?type=REWARD&id=7/);
});

test('规则页加载满减规则并拒绝非法编号', async () => {
  const p = page('promotion-rule');
  p.data.type = 'REWARD';
  p.data.id = '7';
  p.client = { reward: async () => ({ name: '满减', rules: [] }) };
  await p.load();
  assert.equal(p.data.detail.name, '满减');
  p.data.id = '0';
  await p.load();
  assert.match(p.data.error, /编号/);
});
