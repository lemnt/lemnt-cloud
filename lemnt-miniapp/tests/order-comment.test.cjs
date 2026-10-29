const { test } = require("node:test");
const assert = require("node:assert/strict");
function setup() {
  let def,
    token = "one";
  global.Page = (x) => (def = x);
  global.getApp = () => ({
    globalData: { memberSession: { accessToken: token } },
  });
  delete require.cache[require.resolve("../pages/order-comment/index")];
  require("../pages/order-comment/index");
  const p = {
    ...def,
    data: JSON.parse(JSON.stringify(def.data)),
    setData(x) {
      Object.assign(this.data, x);
    },
  };
  p.ownerToken = "one";
  p.orderNo = "ORD";
  p.trade = {
    order: async () => ({
      orderNo: "ORD",
      status: "COMPLETED",
      items: [
        { productId: "3", orderItemId: "301", commentStatus: false },
        { productId: "3", orderItemId: "302", commentStatus: false },
      ],
    }),
  };
  return { p, switch: () => (token = "two") };
}
test("同SKU真实行逐项提交，部分成功后未知项原内容重试", async () => {
  const { p } = setup();
  await p.load();
  p.data.rows.forEach((v, i) => (v.content = "原内容" + i));
  const sent = [];
  p.client = {
    create: async (x) => {
      sent.push(structuredClone(x));
      if (sent.length === 2)
        throw Object.assign(Error("timeout"), { unconfirmed: true });
      return { id: "9" };
    },
  };
  await p.submit();
  assert.equal(p.data.rows[0].submitted, true);
  assert.ok(p.data.rows[1].pending);
  p.input({
    currentTarget: { dataset: { index: 1, field: "content" } },
    detail: { value: "不可修改" },
  });
  assert.equal(p.data.rows[1].content, "原内容1");
  await p.submit();
  assert.equal(sent.length, 3);
  assert.equal(sent[0].orderItemId, "301");
  assert.equal(sent[1].orderItemId, "302");
  assert.deepEqual(sent[1], sent[2]);
  assert.match(p.data.success, /均已确认/);
});
test("明确拒绝可修改，已成功项不重发", async () => {
  const { p } = setup();
  await p.load();
  let n = 0;
  p.client = {
    create: async () => {
      if (++n === 2) throw Error("内容不合法");
      return { id: "9" };
    },
  };
  await p.submit();
  assert.equal(p.data.rows[1].pending, null);
  p.input({
    currentTarget: { dataset: { index: 1, field: "content" } },
    detail: { value: "修正" },
  });
  await p.submit();
  assert.equal(n, 3);
  assert.equal(p.data.rows[1].content, "修正");
});
test("换号清除私人表单并拒绝在途响应", async () => {
  const x = setup();
  await x.p.load();
  x.p.data.rows[0].content = "私人";
  x.p.client = {
    create: async () => {
      x.switch();
      return { id: "9" };
    },
  };
  await x.p.submit();
  assert.deepEqual(x.p.data.rows, []);
  assert.equal(x.p.data.eligible, false);
  assert.equal(x.p.data.success, "");
});
test("缺失或重复真实行ID、未完成订单不可提交", async () => {
  const { p } = setup();
  p.trade = {
    order: async () => ({ orderNo: "ORD", status: "PAID", items: [] }),
  };
  await p.load();
  assert.equal(p.data.eligible, false);
  p.trade = {
    order: async () => ({
      orderNo: "ORD",
      status: "COMPLETED",
      items: [{ productId: "3" }],
    }),
  };
  await p.load();
  assert.equal(p.data.eligible, false);
  p.trade = {
    order: async () => ({
      orderNo: "ORD",
      status: "COMPLETED",
      items: [
        { productId: "3", orderItemId: "301" },
        { productId: "4", orderItemId: "301" },
      ],
    }),
  };
  await p.load();
  assert.equal(p.data.eligible, false);
});
test("已评不重发，未知状态不猜未评", async () => {
  const { p } = setup();
  p.trade = {
    order: async () => ({
      orderNo: "ORD",
      status: "COMPLETED",
      items: [
        { productId: "3", orderItemId: "301", commentStatus: true },
        { productId: "4", orderItemId: "302", commentStatus: null },
      ],
    }),
  };
  await p.load();
  let n = 0;
  p.client = {
    create: async () => {
      n++;
      return { id: "9" };
    },
  };
  await p.submit();
  assert.equal(n, 0);
  assert.match(p.data.error, /未知/);
});
