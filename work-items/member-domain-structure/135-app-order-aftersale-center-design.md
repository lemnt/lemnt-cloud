# App订单中心与售后中心闭环设计

2026-10-31 第135批，责任 code-software-engineer。依据芋道商城 App 订单/售后交互，补齐当前 trade 后端已具备但未对会员开放的查询与操作，并在原生小程序补齐页面。对应台账 T02/T07 的 App 检查项（原为 0 分）。

## 范围

- 订单中心：本人订单分页、按状态/评价状态筛选、状态数量、订单详情、物流轨迹、取消、收货、去评价入口。
- 售后中心：可售后商品额度、本人售后申请、售后分页、售后详情、登记退货运单、取消售后。
- 不包含：真实支付渠道 SDK、真实物流轨迹渠道、退款审批（后台职责）、库存/权益回退规则调整。

## 关键约束

1. 会员身份只取网关认证上下文 `SecurityUtils.getUserId()`，请求体不接受 `memberId`；他人订单与不存在订单对会员统一返回不存在，不泄露资源是否存在。
2. 订单/售后查询按 `member_id` 在 SQL 层过滤，不先查出再内存过滤；详情、轨迹、额度、登记退货在应用层再次核验归属。
3. 复用现有领域与事务：取消/收货沿用 `TradeOrderCloseCmdExe`、`OrderReceiveCmdExe`；售后申请/登记退货沿用 `AfterSaleService` 的 `TradeOperator.member` 分支与行锁额度校验；不新增业务表、不改状态机。
4. 分页沿用项目统一 `R.page(rows,total)`；每页上限 100，App 默认 10/20。
5. 去评价跳转现有评价页面，不新增评价写入口。

## 接口

订单（`/trade/app/orders`，会员身份）：

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `` | 本人订单分页；参数 status、commentStatus、pageNum、pageSize |
| GET | `/{orderNo}` | 本人订单详情（明细+物流+评价状态） |
| GET | `/{orderNo}/tracks` | 本人订单物流轨迹 |
| GET | `/status-counts` | 本人各状态数量（待付款/待发货/待收货/待评价/已完成/已取消/已退款） |
| POST | `/{orderNo}/cancel` | 已有 |
| POST | `/{orderNo}/receive` | 已有 |
| GET | `/{orderNo}/payment` | 已有 |

售后（`/trade/app`，会员身份）：

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/orders/{orderNo}/after-sale-items` | 本人订单可售后商品额度 |
| POST | `/orders/{orderNo}/after-sales` | 本人申请售后（幂等 requestNo） |
| GET | `/after-sales` | 本人售后分页 |
| GET | `/after-sales/{no}` | 本人售后详情 |
| POST | `/after-sales/{no}/return` | 本人登记退货运单 |
| POST | `/after-sales/{no}/cancel` | 已有 |

## 状态与筛选映射

- 订单状态沿用 `RESERVING/PENDING_PAYMENT/PAID/SHIPPED/COMPLETED/CLOSED/REFUNDED`。
- 待评价 = `COMPLETED` 且 `commentStatus=false`；已完成 = `COMPLETED` 且 `commentStatus=true`。
- 状态数量按 `(status, comment_status)` 分组，避免把待评价和已完成合并。

## 分层落点

- 领域：`FulfillmentFilter` 增加 `memberId/commentStatus`；新增 `OrderStatusCount`；`AfterSaleFilter` 增加 `memberId`。
- 基础设施：`TradeOrderMapper` 成员条件与状态计数；`TradeOrderGatewayImpl.memberStatusCounts`；`AfterSaleGatewayImpl.afterSalePage` 成员条件。
- 应用：新增 `MemberOrderPageQueryExe`、`MemberOrderDetailQueryExe`、`MemberOrderTrackQueryExe`、`MemberOrderStatusCountQueryExe`、`MemberAfterSalePageQueryExe`、`MemberAfterSaleDetailQueryExe`、`MemberAfterSaleItemListQueryExe`；售后创建/退货执行器增加会员分支。
- 适配：`AppOrderController`、`AppAfterSaleController` 增加接口，使用独立 App Req/Res 与转换器。
- 小程序：`services/trade.js` 显式区分 mall/trade 服务前缀；新增订单列表、订单详情、售后列表、售后申请、售后详情页。

## 验证计划

- 后端：会员订单分页/状态计数/详情/轨迹归属、他人订单一律不存在、售后额度与申请/登记退货身份校验、控制器不接收伪造 memberId。
- 小程序：trade 客户端仅发本端路径、分页/参数校验、归属切换保护；页面渲染与状态操作。
- 不执行真实数据库/Nacos/Redis/Dubbo/浏览器/真机联调，不 commit/push。
