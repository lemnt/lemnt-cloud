const { test } = require("node:test");
const assert = require("node:assert/strict");
const { createImageClient, selectImages } = require("../services/images");
test("上传使用会员令牌及file字段，只信HTTPS返回地址", async () => {
  let sent;
  const c = createImageClient({
    baseUrl: "https://example.test",
    getMemberToken: () => "member",
    uploadFile: async (x) => {
      sent = x;
      return {
        statusCode: 200,
        data: JSON.stringify({
          code: 200,
          data: { url: "https://oss.test/a.png" },
        }),
      };
    },
  });
  assert.equal(await c.upload("/tmp/a.png"), "https://oss.test/a.png");
  assert.equal(sent.name, "file");
  assert.equal(sent.header.Authorization, "Bearer member");
  assert.equal(sent.url, "https://example.test/files/app/upload");
});
test("换号或响应不合法拒绝回填，不自动重试", async () => {
  let token = "one",
    count = 0;
  const c = createImageClient({
    baseUrl: "https://example.test",
    getMemberToken: () => token,
    uploadFile: async () => {
      count++;
      token = "two";
      return { statusCode: 200, data: "{}" };
    },
  });
  await assert.rejects(c.upload("/tmp/a"), /会员已切换/);
  assert.equal(count, 1);
});
test("多图部分成功保留结果，失败停止后续上传", async () => {
  global.wx = {
    chooseMedia: (o) =>
      o.success({
        tempFiles: [
          { size: 10, tempFilePath: "1" },
          { size: 10, tempFilePath: "2" },
          { size: 10, tempFilePath: "3" },
        ],
      }),
  };
  let n = 0;
  const urls = [];
  await assert.rejects(
    selectImages({
      count: 3,
      append: (x) => urls.push(x),
      client: {
        upload: async () => {
          if (++n === 2) throw Error("网络失败");
          return "https://oss.test/a.png";
        },
      },
    }),
    /网络/,
  );
  assert.equal(n, 2);
  assert.deepEqual(urls, ["https://oss.test/a.png"]);
});
test("取消选择无上传，超大图片不发送", async () => {
  let n = 0;
  const c = {
    upload: async () => {
      n++;
    },
  };
  global.wx = {
    chooseMedia: (o) => o.fail({ errMsg: "chooseMedia:fail cancel" }),
  };
  await selectImages({ count: 1, append: () => {}, client: c });
  assert.equal(n, 0);
  global.wx = {
    chooseMedia: (o) =>
      o.success({
        tempFiles: [{ size: 10 * 1024 * 1024 + 1, tempFilePath: "a" }],
      }),
  };
  await assert.rejects(
    selectImages({ count: 1, append: () => {}, client: c }),
    /10MiB/,
  );
  assert.equal(n, 0);
});
