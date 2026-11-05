# 商品模块链路总览（lemnt-product [9140]）

按当前代码整理，覆盖 HTTP / Dubbo / MQ / 定时四类入口到数据库表的完整链路。
分层模板统一为：`Controller → ReqConverter → Cmd/Qry → Executor →（Application Service）→ Gateway(域接口) → GatewayImpl → Mapper/BaseMapper → 表`；application 层不触碰 Mapper，远程调用与持久化细节全部隔离在 GatewayImpl。

## 1. 入口盘点

| 入口类型 | 数量 | 说明 |
|---|---|---|
| 管理端 HTTP（裸路径，无 `/mall` 前缀） | 8 个 Controller / 44 端点 | `adapter/web/product`，端点契约 product57 部分 |
| 商城 App HTTP `/app/**` | 5 个 Controller / 13 端点 | `adapter/app/product`；`/app` 前缀已归 product；**评价投稿 POST 已迁往 order 服务**（`/mall/app/comments`） |
| Dubbo 入向契约 | `ProductCatalogClientImpl` 13 方法、`ProductCommentClientImpl` 2 方法 | 契约在 `lemnt-product-client`，调用方为 order、promotion |
| RocketMQ 消费者 | 3 个（`OrderPaidConsumer`/`OrderClosedConsumer`/`AfterSaleCompletedConsumer`） | 均受 `commerce.mq.enabled=true` 开关控制，默认关闭 |
| RocketMQ 出站 | `StockEventGatewayImpl`（事务消息） | 预占事实经半消息+本地事务二阶段直发，无本地消息表、无中继定时任务 |

## 2. 管理端链路（裸路径，无服务前缀）

| Controller | 端点 | Executor | 落到的 Gateway → 表 |
|---|---|---|---|
| ProductBrandController `/brands` | GET、GET/{id}、POST、PUT/{id}、DELETE/{id}、GET/page、GET/simple | ProductBrand{List,Detail,Save,Delete,Page,SimpleList}Exe | ProductBrandGateway → product_brand |
| ProductCategoryController `/categories` | GET、GET/{id}、POST、PUT/{id}、DELETE/{id}、GET/tree | ProductCategory{List,Detail,Save,Delete,Tree}Exe | ProductCategoryGateway → product_category（tree内存组树） |
| ProductCommentController `/comments` | POST、GET、GET/{id}、PUT/{id}/reply、PUT/{id}/visible | ProductComment{SelfCreate,Page,Detail,Reply,Visible}Exe | ProductCommentGateway → product_comment；SelfCreate 额外校验 SKU/SPU |
| ProductSpecController `/specs` | POST、PUT/{id}、DELETE/{id}、GET、GET/{id}、GET/simple | ProductSpec{Create,Update,Delete,Page,Detail,SimpleList}Exe | ProductSpecService/ProductSpecGateway → product_spec |
| ProductSpecValueController `/spec-values` | 同上 6 个 | ProductSpecValue{...}Exe | ProductSpecService/ProductSpecValueGateway → product_spec_value |
| ProductSpuController `/spus` | GET、GET/{id}、POST、PUT/{id}、PUT/{id}/shelf、POST/{id}/recycle、POST/{id}/restore、DELETE/{id}、GET/count、GET/simple、GET/batch、GET/export | ProductSpu{Page,Detail,Create,Update,Shelf,Recycle,Restore,Delete,Count,SimpleList,BatchDetail,Export}Exe | ProductCatalogService/ProductValidationService/ProductManagementService → product_spu/product_sku；count 走 XML `countStockSpus`（售罄/低库存）；export 复用 PageQueryExe |
| ProductFavoriteController `/favorites` | GET | ProductFavoritePageQueryExe | ProductFavoriteService → product_favorite |
| ProductBrowseHistoryController `/browse-historys` | GET | ProductBrowseHistoryPageQueryExe | ProductBrowseHistoryService → product_browse_history |

## 3. 商城 App 链路（`/app/**`）

| 端点 | Executor | 链路要点 |
|---|---|---|
| GET /products（分页） | StoreProductPageQueryExe | ProductSpuGateway → XML `products`：product_spu JOIN product_category(自联父类) JOIN product_brand + EXISTS(product_sku)，口径=上架且类目/品牌启用 |
| GET /products/{id}（详情） | StoreProductDetailQueryExe | ProductCatalogService → spu + sku 聚合 |
| GET /categories | StoreCategoryListQueryExe | ProductCategoryGateway → XML `storeCatalogCategories`（仅启用，LIMIT 1001） |
| GET /comments | ProductCommentPublicPageQueryExe | 见 §6；投稿 POST 在 order（`AppOrderCommentController`） |
| POST、GET、DELETE /favorites…（5 个） | ProductFavorite*Exe → ProductFavoriteService | Redis `MemberLockGateway`(LockTemplate) 锁会员-SPU 组合后写 product_favorite |
| POST、GET、DELETE、DELETE/clear /browse-history（4 个） | ProductBrowseHistory*Exe → ProductBrowseHistoryService | 校验 SKU 后写 product_browse_history |

## 4. Dubbo 入向链路（order/promotion → product）

`ProductCatalogClientImpl`（application/dubbo）：

| 方法 | 下游链路 |
|---|---|
| catalogSku / catalogSkus / skusBySpuIds / findSpu / findSpus | ProductSkuGateway、ProductSpuGateway（只读快照） |
| findOnShelf / findOnShelfBatch | ProductCatalogGateway → Sku+Spu Gateway 组合，仅返回上架快照 |
| reserveStock(reservationNo, skuId, qty) | ProductInventoryService → 见 §5 库存链路 |
| confirmStock / releaseStock / cancelReservation | ProductInventoryService（预约状态机 RESERVED→CONFIRMED/RELEASED） |
| returnStock(orderNo, afterSaleNo, skuId, qty) | ProductInventoryService（售后返库，按业务号幂等） |
| reservationStatus(StockReservationQueryBo) | ProductStockReservationGateway 查询预占台账 |

`ProductCommentClientImpl`：saveVerified / saveSystem(ProductCommentSaveBo) → ProductCommentService → product_comment。调用方：order `OrderCommentGatewayImpl`。

其他服务调用入口（均只依赖 `lemnt-product-client` 契约）：
- order：`InventoryGatewayImpl`、`ProductCatalogGatewayImpl`、`OrderBenefitsProgressGatewayImpl`（reserveStock/returnStock）、`OrderCommentGatewayImpl`（saveVerified）
- promotion：`ProductCatalogGatewayImpl`、`ProductSkuGatewayImpl`、`ProductSpuGatewayImpl`（findOnShelf 等）、`StockPreReservedEventHandler`（消费 §5 事件）

## 5. 库存预占与事件可靠性链路（核心）

> 说明：**活动库存（秒杀/拼团/积分等）的扣减发生在建单时的同步 RPC**
> `OrderBenefitsService.promotion.lock(plan)` → `PromotionResourceService`（内部 `decreaseStock`），
> 与商品实际库存预占同属 order 结算编排、失败即补偿；`STOCK_PRE_RESERVED` 事件是
> product 向 promotion 的**事实核对**（预占数量==资源行数量、冻结已先行），不承担扣减职责。
> product 出站**不使用本地消息表**：以 RocketMQ 事务消息（半消息+回查）保证与预占原子。

```
order 结算/下单
  └─ Dubbo reserveStock ─→ ProductInventoryService.reserveStock
        台账预检(find，无锁)：已存在→按幂等语义直接返回（数量不符抛错），不重复发消息
        StockEventGateway.publishReserved ─ 事务消息二阶段：
          1. 半消息先行（destination=STOCK_PRE_RESERVED，body=CommerceEventEnvelope JSON）
          2. executeLocalTransaction 独立短事务(REQUIRES_NEW)，锁序 SKU行锁→台账：
             ProductSkuMapper.lockById(FOR UPDATE) → 查重(并发对手已插则回滚)
             → ProductSkuMapper.reserveStock CAS(stock>=qty 且可售)
             → 插台账 product_stock_reservation（UK reservation_no+product_id）
          3. 提交→COMMIT 消息对消费者可见；CAS不足/并发先行→ROLLBACK 丢弃半消息
          4. 应答丢失由 broker 回查：checkLocalTransaction 以台账行存在为唯一事实
             （存在→COMMIT / 不存在→ROLLBACK / 查询异常→UNKNOWN 等待再回查）
  └─ 下游：promotion StockPreReservedEventHandler 消费（事实核对，inbox 双 UK 幂等吸收重发）
        注意：MQ 不可用时半消息发送失败会同步阻断 reserveStock（预占入口 fail-fast，无本地暂存）

回确认（MQ 入向，commerce.mq.enabled 开关）：
  注意时序：stock 的物理扣减发生在预占本地事务，confirmStock 仅推进台账
  RESERVED→CONFIRMED 不再扣减；CONFIRMED 不可释放，只能经售后 returnStock 回补。
  预占独立提交、与调用方事务解耦：order 建单后续失败必须显式 cancelReservation 补偿。
  ORDER_PAID            → OrderStockEventHandler.paid    → begin(product.order_paid.v1  inbox幂等) → confirmStock
  ORDER_CLOSED          → OrderStockEventHandler.closed  → begin(product.order_closed.v1)          → cancelReservation
                        （预占行不存在时先插终态 RELEASED，防延迟 RPC 在关单后补扣）
  AFTER_SALE_COMPLETED  → OrderStockEventHandler.refunded→ begin(...v1) 且要求 paid 已入账        → returnStock
  三个 Handler 均先 MessageInboxGateway.begin(consumer_key+event_no / event_type+business_key 双 UK)
  做消费幂等，冲突即跳过。
```

## 6. 评价链路

```
投稿（芋道拓扑：资格是订单事实，入口在交易域）
miniapp POST /mall/app/comments → order AppOrderCommentController
  ├─ 身份/昵称/头像快照取登录上下文（SecurityUtils，网关注入头→线程变量），请求体不可伪造
  ├─ OrderCommentService.create：订单头行锁、本人/完成态/订单项核验
  └─ Dubbo ProductCommentClient.saveVerified(bo 携带下单快照) → product ProductCommentService.saveVerified
       ├─ ProductCommentMapper.lock  XML 行锁 FOR UPDATE（重复评价检查）
       └─ 写 product_comment（score/pic_urls/anonymous/user_nickname/user_avatar…28 列）
App GET /app/comments → ProductCommentPublicPageQueryExe（公开查询留在 product）
管理端 PUT /comments/{id}/visible、/reply → 同 Gateway 更新 visible/reply_*
order 系统评价（到期自动评价调度器）→ OrderCommentService.createSystem
  → 按会员编号查 order 自有 MemberLookupGateway 取昵称快照 → Dubbo saveSystem → product 落库
legacyForLink XML：存量评价绑定检查（读取 deleted 事实但不对外返回）
```

## 7. 表 → Mapper → SQL 来源

11 张表归属 product（`check_module_split.py` 口径；`product_message_outbox` 已随事务消息改造删除），Mapper 均继承 `BaseMapper<DO>`，简单条件走 GatewayImpl Lambda，以下 XML 承载复杂/锁 SQL：

| 表 | Mapper | XML 语句（★=FOR UPDATE 行锁，useCache=false/flushCache=true） |
|---|---|---|
| product_spu | ProductSpuMapper | lockSpu★、products、storeCatalogCount、visible、selectStockPage、countStockSpus |
| product_sku | ProductSkuMapper | lockById★、findSellableByIds、findSellableById、reserveStock(CAS) |
| product_category | ProductCategoryMapper | storeCatalogCategories |
| product_brand | ProductBrandMapper | 无自定义 SQL（纯 BaseMapper+Lambda） |
| product_spec | ProductSpecMapper | lock★ |
| product_spec_value | ProductSpecValueMapper | lock★ |
| product_comment | ProductCommentMapper | lock★、legacyForLink |
| product_stock_reservation | ProductStockReservationMapper | lock★ |
| product_favorite | ProductFavoriteMapper | 无自定义 SQL |
| product_browse_history | ProductBrowseHistoryMapper | 无自定义 SQL |
| product_message_inbox | MessageInboxMapper | lockEvent★、lockBusiness★ |

所有 XML 均已使用 `<sql id="baseColumn">` 显式列清单与 `autoMapping="false"` 全列 resultMap，无 `SELECT *`。

## 8. 并发与幂等机制汇总

| 机制 | 位置 |
|---|---|
| 数据库行锁 | 上表 ★ 语句；扣减库存、锁台账、锁评价 |
| CAS 版本/条件 | product_sku.reserveStock（stock>=qty）、台账状态机（RESERVED→CONFIRMED/RELEASED） |
| 业务幂等 UK | product_stock_reservation(reservation_no, product_id)；inbox 双 UK；事务消息 eventNo 随机+businessKey 稳定，promotion inbox 吸收重复投递 |
| 事务消息 | StockEventGatewayImpl：半消息先行，本地短事务(REQUIRES_NEW)裁决，broker 回查以台账行为事实 |
| Redis 锁 | MemberLockGatewayImpl（LockTemplate）：收藏创建/删除、浏览历史写入的会员维度串行化 |
| 消费开关 | `commerce.mq.enabled`（3 消费者默认关闭）；出站无开关（MQ 不可用则预占 fail-fast） |
| 事务边界 | SPU 创建/更新、事件 Handler 均 `@Transactional`；预占在事务消息本地短事务独立提交，调用方失败经补偿释放，行锁在事务内真实执行 |

## 9. 备注（整理中发现，未改动）

- `ProductStockAdjustmentDTO`/`ProductStockAdjustmentRes`/`ProductCatalogResConverter.stockAdjustment()` 当前无调用方，疑似库存调整页迁移后的遗留断链，仅提示不删除。
- `MemberValidationService`、`Settlement*`（测试夹具）等成员/测试类不属于本次链路主链。

验证：`python3 scripts/check_architecture.py`、`mvn -f lemnt-server/pom.xml -pl lemnt-product/lemnt-product-infrastructure -am test`（含 `MapperXmlLoadingTest`）、product-start 全量回归。
