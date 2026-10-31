# App订单中心与售后中心交付与验证

2026-10-31 第135批。按设计文档补齐会员本人订单中心与售后中心的查询、操作和小程序页面。没有新增业务表、DDL、依赖、支付渠道或网关 RPC 引用，复用既有交易/售后领域与事务。

## 后端

订单（`/trade/app/orders`，身份只取认证上下文）：
- `GET ""`：本人订单分页，按 `status`、`commentStatus`（区分待评价/已完成）筛选，携带商品明细快照。
- `GET /status-counts`：待付款/待发货/待收货/待评价/已完成/已取消/已退款数量，按 `(status, comment_status)` 分组，`RESERVING` 归入待付款。
- `GET /{orderNo}`：本人订单详情（明细、金额、物流、评价状态）。
- `GET /{orderNo}/tracks`：本人订单物流轨迹，仅按本地登记运单查询。
- 取消/收货沿用既有执行器。

售后（`/trade/app`，身份只取认证上下文）：
- `GET /orders/{orderNo}/after-sale-items`：本人订单可售后额度。
- `POST /orders/{orderNo}/after-sales`：本人申请售后，金额由服务端计算，`requestNo` 幂等。
- `GET /after-sales`：本人售后分页。
- `GET /after-sales/{no}`：本人售后详情。
- `POST /after-sales/{no}/return`：本人登记退货运单。
- 取消沿用既有执行器。

归属与安全：
- 分页在 SQL 层按 `member_id` 过滤（`FulfillmentFilter`、`AfterSaleFilter` 增加成员条件），后台传入空成员保持原行为。
- 详情、轨迹、可售后额度先比对订单/售后归属；他人资源与不存在统一返回不存在，不泄露存在性。
- 申请/登记退货走 `TradeOperator.member`，领域层再次核验归属与状态；请求体不接受 `memberId` 或金额。
- 状态数量新增 `OrderStatusCount` 投影与 `TradeOrderMapper.memberStatusCounts`。

## 小程序

- `services/trade.js` 改为显式 mall/trade 服务前缀，新增订单分页/状态数量/详情/轨迹、售后分页/详情/额度/申请/登记退货方法；分页响应统一 `{records,total}`。
- 新增页面：订单中心（状态分组与数量、取消、收货、去评价、申请售后）、订单详情（明细、物流、轨迹）、售后中心、售后申请（选择商品/数量/方式/原因，请求编号幂等）、售后详情（取消、登记退货物流）。
- 会员中心、购物车首页增加订单/售后入口；未登录跳转登录页。

## 验证

- 后端：Java 17 执行 `mvn -pl lemnt-trade-adapter,lemnt-trade-infrastructure -am test`，trade 各模块 174 项测试通过，0 失败/错误/跳过：adapter 25、application 22（含新增 `MemberOrderQueryTest` 4、`MemberAfterSaleQueryTest` 4）、domain 2、infrastructure 10、start 115（含 `CheckoutFlowIntegrationTest` 下单集成回归）。
- 小程序：`node --test tests/*.test.cjs` 136 项通过，0 失败；新增 `order-center.test.cjs`（客户端路径/参数/金额隔离）与 `order-pages.test.cjs`（状态操作、计数汇总、确认、请求编号幂等、归属校验）。
- 证据：`evidence/app-order-aftersale-center-verification.json`。

## 限制

- 未执行真实 MySQL/Redis/Nacos/Dubbo/网关部署、浏览器或真机联调；以上为代码与 H2/单测/MockMvc 证据，不等于端到端验收。
- 未接通真实支付收银台与渠道 SDK（按约定后置）；订单中心不伪造支付结果。
- 本轮未主动执行 `git commit`/`push`。工作区随后出现非本会话发起的自动提交 `ec34c46`，内容与本轮改动一致，需由用户确认该自动提交来源。
- 源 App 活动聚合、订单评价图片等不在本批范围。
