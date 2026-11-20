# 芋道与lemnt-cloud业务链路对照

日期：2026-11-18。目标代码HEAD：`ed7d9eab`；保留现有未提交环境文档和证据。

> 后续实施记录：同日B2a已完成多分类、price/salesCount排序、分类ids/排序和已有详情字段透出，自动化通过，见[验证证据](evidence/b2a-validation.json)。下表保留实施前的对比快照；创建时间、新商品配置、详情访问语义、消息收敛、内容与全量统计仍未完成。
>
> 同日后续补齐商品创建时间、商品配置、详情浏览、普通零元交易、库存预占回查、商城文章和部分统计链路。实现状态及范围见[本轮实现记录](code-summary.md)。
>
> 2026-11-19 已补推广端佣金预估、订单支付及退款入站确认、支付到账趋势和交易待办。下表保留首次静态对照，现状及验收见[实现记录](code-summary.md)。

## 结论与检查范围

**不能判定两边链路都已补齐。** 以本地`yudao-cloud`、`yudao-mall-uniapp`、`yudao-ui-admin-vue3`为参考，与当前lemnt后端、React管理端、原生小程序进行代表链静态追踪。源项目存在相应实现，不代表已运行验收；当前主交易链已有实现，但商品语义存在确定缺口，消息机制尚未满足既定收敛目标，内容和统计仍缺完整承接证据。

本轮读取代码、路由、客户端及工作项证据；没有启动服务、操作业务数据或重新执行测试。没有逐方法审计全部47能力族，不能据此给出全量迁移完成率。运行条件引用同日`local-runtime-preflight-20261118.json`，并非本轮重新探测。

## 链路对照

| 链路 | 芋道参考实现 | lemnt当前承接 | 判定 |
|---|---|---|---|
| 商品分类→列表→详情 | 分类启用排序/ids查询；多分类、价格/销量/创建时间排序；详情访问计数与浏览记录 | 单categoryId、固定sort/id倒序；详情输出裁剪，查询无同样访问副作用 | **明确未补齐** |
| 商品运营配置→前台展示 | SPU/SKU包含成本/划线价格、规格模式、虚拟销量及部分积分分佣字段 | 当前SPU/SKU未完整承载；introduction、sliderPicUrls已持久化但StoreDetailRes未输出 | **明确未补齐**；活动响应已有marketPrice，不等于商品主数据完整迁移 |
| 会员→结算→下单→库存/权益预占 | TradeOrderUpdateServiceImpl中创建前后Handler编排库存、活动、券和支付单 | checkoutFlow→结算/订单服务；OrderSubmissionService先落单，OrderRemoteService同步预占并处理补偿 | **有实现，时序不同，尚未证明全规则等价** |
| 渠道收款→订单支付→库存/积分/券 | 支付通知任务持久化、HTTP通知及重试→交易支付更新 | PaymentNotificationService→PAY_SUCCESS Outbox→order消费者/Inbox→ORDER_PAID事件→product/member/promotion | **有实现，消息恢复机制未完成既定收敛，运行未验收** |
| 用户取消/超时→关支付→资源回退 | 支付状态核对、取消Handler及提交后关支付 | OrderCloseService核对资金状态后登记ORDER_CLOSED，pay/product/member/promotion存在消费者 | **有实现，超时/迟到资金/乱序/失败恢复仍须联合验证** |
| 发货/自提→收货→完成→评价 | 交易履约、用户投稿/自动评价→商品评价 | 收货执行器及订单评价服务存在；小程序读/app/comments，写/mall/app/comments，经订单核验后保存商品评价 | **评价读写归属已对齐**；全部履约方式和后台任务未逐项验收 |
| 售后申请→审核/退货→退款→库存/权益 | AfterSaleServiceImpl→PayRefundApi→退款通知→订单项及订单更新 | AfterSaleService冻结原方案→AfterSaleRefundCoordinator→资金退单核对→售后/订单更新→AFTER_SALE_COMPLETED事件 | **有实现，跨服务效果仍待MQ与真实退款验证**，不能说退款接口缺失 |
| 内容文章→管理配置→小程序阅读 | article/article-category后台、App文章接口、uniapp文章客户端 | 本轮后端、管理端路由/API、小程序服务中未找到完整对应链 | **缺口候选，不能认定已迁移**；不以类名搜索单独证明全平台无内容能力 |
| 会员/商品/支付/交易统计→管理看板 | 四类statistics Controller及Vue API | 只核到OrderStatisticsQueryExe的订单数/应付金额/成功退单数/退款额简要统计 | **全量统计未补齐的证据充分**；四类每一指标仍需逐项映射 |
| 配置→服务注册→真实请求→微信/支付渠道 | 本轮未运行参考项目 | 专用库及模板配置已准备；同日预检业务端口和MQ未就绪；miniapp服务开关仍关闭 | **两边均不能宣称本轮E2E已通过** |

## 确定缺口的代码证据

1. **商品筛选与排序**：源`yudao-module-mall/yudao-module-product-server/.../controller/app/spu/vo/AppProductSpuPageReqVO.java`有categoryIds/sortField/sortAsc，`dal/mysql/spu/ProductSpuMapper.java`按价格、销量、创建时间排序。目标`StorePageReq`只有name/categoryId/pageNum/pageSize；`ProductSpuMapper.xml`固定`ORDER BY s.sort DESC,s.id DESC`。目标存在父分类/子分类匹配，但不是源多分类及启用子分类规则的完整等价实现。
2. **分类排序/按ids取列表**：源`AppCategoryController`明确提供list-by-ids并按sort升序；目标`ProductCategoryGatewayImpl.listEnableCategories`只查询ENABLED，未指定排序或接收ids。
3. **详情资料与访问效果**：源`AppProductSpuController.getSpuDetail`校验状态、更新browseCount并创建浏览记录；目标`ProductCatalogService.product`为只读，隐藏商品返回null并过滤非正价SKU；`StoreDetailRes`无introduction/sliderPicUrls。目标另有显式足迹入口，不能表述为整个系统没有足迹能力。
4. **商品可见性**：源App分页SQL只明确约束SPU启用；目标`storeCatalogVisible`额外要求上架且price>0的SKU。零元可售语义不同，需购买、支付、前端一起核对，不能孤立放宽SQL。
5. **消息路径**：目标`PaymentNotificationService.execute`明确调用outbox.enqueue；`OrderEventGatewayImpl`的paid/closed/completed/afterSale均调用Outbox；`PaySuccessEventHandler`和`PayRefundSuccessEventHandler`明确使用Inbox。`OutboxRelayScheduler`受commerce.outbox.enabled控制，消费者受commerce.mq.enabled控制；模板默认false。按当前默认配置启动，不会自动形成这些跨服务异步效果。
6. **前端可用性**：`lemnt-miniapp/config.js`仍为apiBaseUrl空、memberApiReady=false、paymentAppKey空；客户端实现不能替代真实用户操作验收。

## 保留的有意差异

- 金额：源整数分与当前人民币元/BigDecimal在协议边界转换，不按同名字段判缺失，也不整体回改金额单位。
- 销量：当前支付确认累计、售后不冲减是既定口径；源虚拟销量展示是需补能力，不能用恢复源口径覆盖原决定。
- 投稿：order核对购买事实、product保存与公开查询是当前确认归属，不能因源路径不同判成路由错误。
- 架构：保留DDD及外部Client仅Infrastructure的边界；同步单体Handler不能机械替换跨服务事务与恢复流程。

## 建议补齐顺序

1. 完成B2a：商品查询参数→应用查询→Mapper→响应→管理端/小程序交互，连同分类排序/ids与已有字段透出验证。
2. 完成B2b补充设计及实施：真实创建时间、虚拟展示销量、商品配置字段、零元可售链和详情访问行为；历史未知值不补造。
3. 完成B3逐事件设计：支付/关单/退款每个事件明确业务成功事实、重复/乱序、回查与失败恢复；按既定要求收敛通用消息表，不能仅打开开关宣布完成。
4. 按47能力族继续逐方法映射，优先文章内容、四类统计，以及未完整核对的会员/营销/分佣分支。
5. 在隔离Redis及MQ条件就绪后，执行真实页面→网关→RPC→DB→MQ的正向及异常验收，微信/退款渠道单列。

## 主要可复核入口

- 目标：`lemnt-server/lemnt-product/lemnt-product-adapter/src/main/java/com/lemnt/product/adapter/app/product/{req/StorePageReq.java,res/StoreDetailRes.java}`。
- 目标：`lemnt-server/lemnt-product/lemnt-product-infrastructure/src/main/resources/mapper/ProductSpuMapper.xml`。
- 目标：`lemnt-server/lemnt-order/lemnt-order-application/src/main/java/com/lemnt/order/application/{order/service,event/service,aftersale/service}`。
- 目标：`lemnt-server/lemnt-order/lemnt-order-infrastructure/src/main/java/com/lemnt/order/infrastructure/event/gateway/OrderEventGatewayImpl.java`。
- 目标：`lemnt-server/lemnt-pay/lemnt-pay-application/src/main/java/com/lemnt/pay/application/payment/service/PaymentNotificationService.java`。
- 源：`/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/service/{order/TradeOrderUpdateServiceImpl.java,aftersale/AfterSaleServiceImpl.java}`。
- 源：`/Users/wangxx/demo/yudao-cloud/yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/service/notify/PayNotifyServiceImpl.java`。
- 源前端：`/Users/wangxx/demo/yudao-mall-uniapp/sheep/api/promotion/article.js`、`/Users/wangxx/demo/yudao-ui-admin-vue3/src/api/mall/statistics/`。

现有capability-matrix主要是首轮入口盘点；state.json的任务计数和日期落后于11月17日环境实施记录。本报告不修改旧任务完成状态，也不将历史1338/182/320测试结果称作本轮重新验证。
