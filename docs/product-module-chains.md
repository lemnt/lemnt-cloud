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
| 库存预占 | `ProductInventoryService.reserveStock` | 商品库本地事务扣减并登记预占 |

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
- promotion：`ProductCatalogGatewayImpl`、`ProductSkuGatewayImpl`、`ProductSpuGatewayImpl`（findOnShelf 等）

## 5. 库存预占与事件可靠性链路（核心）

活动库存由订单编排调用营销资源冻结接口处理。商品库存由订单调用商品服务在本地事务内预占，失败后按原预占号补偿。

```
order 结算/下单
  └─ Dubbo reserveStock → ProductInventoryService.reserveStock
      独立商品库短事务（REQUIRES_NEW）
      → 按 SKU 加锁 → 查询原预占行，有同量有效预占直接返回
      → 可售且库存足够时扣减（CAS）→ 登记 product_stock_reservation
      → 同事务提交，响应丢失时按原预占号重试
  └─ 订单后续失败时按原预占号释放库存及营销资源。

回确认（MQ 入向，commerce.mq.enabled 开关）：
  注意时序：stock 的物理扣减发生在预占本地事务，confirmStock 仅推进台账
  RESERVED→CONFIRMED 不再扣减；CONFIRMED 不可释放，只能经售后 returnStock 回补。
  预占独立提交、与调用方事务解耦：order 建单后续失败必须显式 cancelReservation 补偿。
  ORDER_PAID            → OrderStockEventHandler.paid    → 库存确认、预占行销量标记及 SKU/SPU 销量同事务
                          重放按预占行销量标记幂等，历史支付回执沿用原载荷核验。
                          销量按支付确认件数累计，售后回补不冲减。
                          支付确认先按SPU编号锁主体，再按SKU编号锁库存，与后台编辑保持同一锁序。
                          历史独立 SKU 仍累计本 SKU 销量，SPU 汇总使用已保存的归属。
  ORDER_CLOSED          → OrderStockEventHandler.closed  → 核验已关闭订单的会员及原 SKU 数量 → cancelReservation
                          已释放预占幂等返回，尚未预占写入 CANCELLED 以拦截迟到的预占。
                          历史关单回执继续核验原载荷。
  AFTER_SALE_COMPLETED  → OrderStockEventHandler.refunded→ 核验销量已入账或历史支付回执 → returnStock
                          售后消费回执与回补同事务保存。
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
| 业务幂等 UK | product_stock_reservation(reservation_no, product_id)；尚在使用的 Inbox 按事件编号和业务键去重 |
| Redis 锁 | MemberLockGatewayImpl（LockTemplate）：收藏创建/删除、浏览历史写入的会员维度串行化 |
| 消费开关 | `commerce.mq.enabled`（3 个商品消费者默认关闭） |
| 事务边界 | SPU 创建/更新、事件 Handler 均 `@Transactional`；预占在商品库短事务独立提交，调用方失败经补偿释放，行锁在事务内真实执行 |

## 9. 备注（整理中发现，未改动）

- `MemberValidationService`、`Settlement*`（测试夹具）等成员/测试类不属于本次链路主链。

验证：`python3 scripts/check_architecture.py`、`mvn -f lemnt-server/pom.xml -pl lemnt-product/lemnt-product-infrastructure -am test`（含 `MapperXmlLoadingTest`）、product-start 全量回归。
