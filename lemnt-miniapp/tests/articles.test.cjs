const { test } = require('node:test');
const assert = require('node:assert/strict');
const { createArticleClient } = require('../services/articles');

test('文章使用公开入口保留推荐条件和长ID，详情支持标题查询', async () => {
  const calls = [];
  const long = '9223372036854775807';
  const row = { id: long, title: '购物须知', content: '<p>正文</p>', spuId: '2' };
  const client = createArticleClient({
    baseUrl: 'https://shop.example',
    ready: true,
    request: async (options) => {
      calls.push(options);
      return {
        statusCode: 200,
        data: { code: 200, rows: [row], total: 1, data: options.method === 'PUT' ? true : row },
      };
    },
  });
  assert.equal((await client.detail({ title: row.title })).id, long);
  assert.equal((await client.page(2, { categoryId: '3', recommendHot: true })).total, 1);
  assert.equal(await client.browse(long), true);
  assert.deepEqual(
    calls.map((call) => call.url),
    [
      'https://shop.example/mall/app/articles/detail',
      'https://shop.example/mall/app/articles',
      'https://shop.example/mall/app/articles/' + long + '/browse',
    ],
  );
  assert.equal(calls[1].data.recommendHot, true);
  assert.equal(calls[1].data.pageNum, 2);
  assert.equal(calls[2].method, 'PUT');
  assert.ok(calls.every((call) => call.header === undefined));
});
