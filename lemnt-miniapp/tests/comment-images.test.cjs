const { test } = require("node:test");
const assert = require("node:assert/strict");
const { pictureUrls, removePicture } = require("../services/images");
function page(name) {
  let def,
    token = "one";
  global.Page = (x) => (def = x);
  global.getApp = () => ({
    globalData: { memberSession: { accessToken: token } },
  });
  delete require.cache[require.resolve("../pages/" + name + "/index")];
  require("../pages/" + name + "/index");
  return {
    p: {
      ...def,
      ownerToken: "one",
      data: JSON.parse(JSON.stringify(def.data)),
      setData(x) {
        Object.assign(this.data, x);
      },
    },
    switch: () => (token = "two"),
  };
}
const a = "https://oss.test/a.png",
  b = "https://oss.test/b.png";
test("附件解析仅预览HTTPS，移除一次并保持剩余顺序", () => {
  assert.deepEqual(pictureUrls(a + "\nhttp://bad.test/a\n" + b), [a, b]);
  assert.equal(removePicture(a + "\n" + a + "\n" + b, a), a + "\n" + b);
});
test("单项图片输入、删除和提交一致，预览不能注入URL", async () => {
  const { p } = page("comment-create");
  p.orderNo = "ORD";
  p.skuId = "3";
  p.orderItemId = "301";
  p.data.eligible = true;
  p.data.content = "评价";
  p.input({
    currentTarget: { dataset: { field: "pictures" } },
    detail: { value: a + "\n" + b },
  });
  assert.deepEqual(p.data.imageUrls, [a, b]);
  let shown;
  global.wx = { previewImage: (x) => (shown = x) };
  p.previewImages({
    currentTarget: { dataset: { url: "https://evil.test/a" } },
  });
  assert.equal(shown, undefined);
  p.previewImages({ currentTarget: { dataset: { url: b } } });
  assert.deepEqual(shown, { urls: [a, b], current: b });
  p.removeImage({ currentTarget: { dataset: { url: a } } });
  let sent;
  p.client = {
    create: async (x) => {
      sent = x;
      return { id: "9" };
    },
  };
  await p.submit();
  assert.deepEqual(sent.picUrls, [b]);
  assert.deepEqual(p.data.imageUrls, [b]);
});
test("单项未知结果冻结图片，换号清理缩略图并拒绝预览", () => {
  const x = page("comment-create");
  x.p.data.pictures = a;
  x.p.data.imageUrls = [a];
  x.p.data.pending = true;
  x.p.removeImage({ currentTarget: { dataset: { url: a } } });
  assert.equal(x.p.data.pictures, a);
  x.switch();
  x.p.onShow();
  assert.deepEqual(x.p.data.imageUrls, []);
  global.wx = { previewImage: () => assert.fail("换号不能预览") };
  x.p.previewImages({ currentTarget: { dataset: { url: a } } });
});
test("同页移除只影响当前行，未知项禁止删除", () => {
  const { p } = page("order-comment");
  p.data.rows = [
    { pictures: a + "\n" + b, imageUrls: [a, b], commentStatus: false },
    { pictures: a, imageUrls: [a], commentStatus: false, pending: {} },
  ];
  p.removeImage({ currentTarget: { dataset: { index: 0, url: a } } });
  assert.equal(p.data.rows[0].pictures, b);
  assert.equal(p.data.rows[1].pictures, a);
  p.removeImage({ currentTarget: { dataset: { index: 1, url: a } } });
  assert.equal(p.data.rows[1].pictures, a);
});
