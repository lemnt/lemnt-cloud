# 4 模块拆分迁移计划

> mall + trade → product + promotion + order + pay

## 一、背景与目标

### 现状问题

| 模块 | 文件数 | 问题 |
|------|--------|------|
| lemnt-mall | 890 | 包含 product/promotion/settlement/coupon/cart/member-ACL 六个子域，职责过重 |
| lemnt-trade | 710 | 包含 order/payment/aftersale/fulfillment/delivery/brokerage 等，支付与订单耦合 |

### 目标架构

```
lemnt-product    商品 SPU/SKU/分类/规格/品牌/评论/库存/收藏/浏览     ~318 文件
lemnt-promotion  秒杀/砍价/拼团/优惠券/积分活动/折扣/DIY/客服/购物车  ~535 文件
lemnt-order      订单/结算/售后/发货/物流/履约/分佣/自提              ~459 文件
lemnt-pay        支付渠道/支付流水/退款/钱包/充值                     ~224 文件
```

### 设计原则

1. **按限界上下文拆分**，不按文件大小拆分
2. **settlement（结算）归入 order**，因为结算本质是"创建订单的前置步骤"
3. **coupon（优惠券）归入 promotion**，因为优惠券是营销工具
4. **cart（购物车）归入 promotion**，因为购物车关联营销活动（凑单、换购）
5. **member-ACL（防腐层）各模块各自维护**，拆分到需要的模块中

---

## 二、文件归属映射

### 2.1 lemnt-product（~318 文件）

| 来源 | 子域 | 文件数 | 说明 |
|------|------|--------|------|
| mall/product | domain | 38 | Product, ProductSpu, ProductSku, ProductSpec, Brand, Category, ProductComment, StockReservation 等 |
| mall/product | application | 121 | 完整 CQRS：SPU/SKU/Brand/Category/Spec/Comment/Favorite/BrowseHistory |
| mall/product | adapter | 104 | 12 Controller + req/res/converter |
| mall/product | infrastructure | 37 | 11 DO + 11 Mapper + 11 GatewayImpl + 5 Converter |
| mall/product | client | 3 | ProductCatalogClient, ProductCommentClient, DTO |
| mall/member-ACL | domain | 部分 | MemberSnapshot（会员等级折扣，商品需要） |
| mall/member-ACL | infrastructure | 部分 | MemberLookupGatewayImpl（查会员信息） |
| trade/inventory | 全部 | 2 | InventoryGateway + InventoryGatewayImpl → 归入 product |

**新建 client 接口**：
- `ProductCatalogClient` — 商品查询、库存扣减/回退、快照
- `ProductCommentClient` — 评论创建

### 2.2 lemnt-promotion（~535 文件）

| 来源 | 子域 | 文件数 | 说明 |
|------|------|--------|------|
| mall/promotion | domain | 54 | Seckill, Bargain, Combination, Discount, Point, Reward, Banner, DiyPage, Kefu 等 |
| mall/promotion | application | 207 | 每种活动的完整 CQRS |
| mall/promotion | adapter | 88 | 13 Controller + req/res/converter |
| mall/promotion | infrastructure | 75 | 22 DO + 22 Mapper + 21 GatewayImpl + 10 Converter |
| mall/coupon | 全部 | 68 | Coupon, CouponTemplate, CouponRules, CouponTerms + CQRS |
| mall/cart | 全部 | 22 | CartGateway + CartService + CartController |
| mall/member-ACL | 部分 | 部分 | MemberPointConfig（积分配置，活动需要） |
| trade/promotion-snapshot | 全部 | 21 | OrderPromotion, OrderItemPromotion, OrderPromotionRefund, OrderPricingRules |

**注意**：trade/promotion-snapshot 是订单中优惠快照数据，归属有争议：
- 方案 A：归入 promotion（优惠规则的延伸）
- 方案 B：归入 order（是订单数据的组成部分）
- **建议方案 B**：归入 order，因为快照是订单创建时的冻结数据，promotion 不应持有订单侧数据

**调整后**：promotion ~514 文件，order ~480 文件

**新建 client 接口**：
- `PromotionCalcClient` — 计算优惠（下单时调用）
- `PromotionLockClient` — 冻结/核销/回退优惠券和活动库存

### 2.3 lemnt-order（~480 文件）

| 来源 | 子域 | 文件数 | 说明 |
|------|------|--------|------|
| mall/settlement | 全部 | 56 | Settlement, SettlementItem, SettlementDelivery, SettlementPromotion, OrderPlacementService 等 |
| trade/order | 全部 | 78 | TradeOrder, TradeOrderItem, OrderLifecycleService 等 |
| trade/aftersale | 全部 | 63 | AfterSale, AfterSaleLog, AfterSaleRefundCalculator 等 |
| trade/fulfillment | 全部 | 74 | Shipment, ShipmentTrack, OrderFulfillmentService 等 |
| trade/delivery | 全部 | 87 | DeliveryExpress, DeliveryExpressTemplate, PickUpStore 等 |
| trade/brokerage | 全部 | 57 | BrokerageRecord, BrokerageUser, BrokerageWithdraw 等 |
| trade/config | 全部 | 14 | TradeConfig |
| trade/pickup | 全部 | 30 | 自提门店/自提订单 |
| trade/promotion-snapshot | 全部 | 21 | OrderPromotion, OrderPricingRules（订单优惠快照） |
| mall/member-ACL | 部分 | 部分 | MemberDeliveryAddress, MemberTradeChange（订单需要） |

**client 接口**：
- `TradeOrderClient` — 创建/查询/关闭订单（原 trade-client）
- `TradeDeliveryClient` — 运费计算（原 trade-client）
- `CheckoutLifecycleClient` — 结算生命周期（原 mall-client，迁入 order）

### 2.4 lemnt-pay（~224 文件）

| 来源 | 子域 | 文件数 | 说明 |
|------|------|--------|------|
| trade/payment | 全部 | 195 | PayApp, PayChannelConfig, PaymentRecord, Wallet, WalletRecharge 等 |
| trade/paymentrefund | 全部 | 29 | PaymentRefund, PaymentRefundResult 等 |

**新建 client 接口**：
- `PaymentClient` — 发起支付/退款
- `PaymentQueryClient` — 查询支付状态
- `WalletClient` — 钱包余额/充值/扣减

---

## 三、Dubbo Client 接口重新划分

### 3.1 接口迁移矩阵

| 原接口 | 原模块 | → 新接口 | 新模块 | 消费方 |
|--------|--------|----------|--------|--------|
| ProductCatalogClient | mall-client | ProductCatalogClient | product-client | order, promotion |
| ProductCommentClient | mall-client | ProductCommentClient | product-client | order |
| CheckoutLifecycleClient | mall-client | PromotionLockClient | promotion-client | order |
| TradeOrderClient | trade-client | TradeOrderClient | order-client | checkout(order) |
| TradeDeliveryClient | trade-client | TradeDeliveryClient | order-client | checkout(order) |
| — | — | PromotionCalcClient | promotion-client | order(checkout) |
| — | — | PaymentClient | pay-client | order |
| — | — | WalletClient | pay-client | order, member |

### 3.2 新增 Dubbo 接口

```java
// promotion-client
public interface PromotionCalcClient {
    /** 计算订单优惠（下单时调用） */
    PromotionResultDTO calculate(PromotionCalcBo bo);
}

public interface PromotionLockClient {
    /** 冻结优惠券和活动库存（下单时） */
    void lock(PromotionLockBo bo);
    /** 核销（支付成功后） */
    void confirm(String orderNo);
    /** 回退（订单关闭/退款时） */
    void rollback(String orderNo);
}

// pay-client
public interface PaymentClient {
    /** 发起支付 */
    PaymentResultDTO prepay(PrepayBo bo);
    /** 发起退款 */
    RefundResultDTO refund(RefundBo bo);
}

public interface WalletClient {
    /** 扣减余额 */
    void deduct(WalletDeductBo bo);
    /** 增加余额 */
    void add(WalletAddBo bo);
    /** 查询余额 */
    WalletDTO getBalance(Long userId);
}
```

---

## 四、MQ Topic 设计

### 4.1 Topic 清单

| Topic | 生产者 | 消费者 | 用途 | 消费模式 |
|-------|--------|--------|------|----------|
| `PAY_SUCCESS` | pay | order, promotion, member | 支付成功：更新订单状态、核销优惠券、增加积分 | 集群消费 |
| `PAY_REFUND_SUCCESS` | pay | order, promotion, product | 退款成功：更新售后状态、回退优惠券、回退库存 | 集群消费 |
| `ORDER_CLOSED` | order | pay, promotion, product | 订单关闭（超时未支付）：取消支付单、回退优惠券、回退库存 | 集群消费 |
| `ORDER_SHIPPED` | order | member | 发货通知：确认收货倒计时 | 集群消费 |
| `ORDER_COMPLETED` | order | promotion, brokerage | 订单完成：确认分佣、完成活动奖励 | 集群消费 |
| `STOCK_PRE_RESERVED` | product | promotion | 库存预占成功（下单时） | 集群消费 |

### 4.2 消息体设计示例

```java
// PAY_SUCCESS 消息体
public record PaySuccessMessage(
    String orderNo,           // 订单号
    String payNo,             // 支付单号
    String payChannel,        // 支付渠道
    BigDecimal payAmount,     // 支付金额
    LocalDateTime paidAt      // 支付时间
) implements Serializable {}

// ORDER_CLOSED 消息体
public record OrderClosedMessage(
    String orderNo,           // 订单号
    String closeReason,       // 关闭原因（TIMEOUT/BUYER_CANCEL/SYSTEM）
    LocalDateTime closedAt    // 关闭时间
) implements Serializable {}
```

### 4.3 幂等设计

每个消费者必须实现幂等：
- 消息携带唯一 `eventNo`（事件编号）
- 消费者维护 `consumed_events` 表，按 `eventNo` 去重
- 业务操作使用乐观锁或唯一索引保证幂等

### 4.4 可靠投递

- 生产者使用 RocketMQ 事务消息或 Outbox 模式
- 消息落库后再发送，保证至少投递一次
- 死信队列处理消费失败的消息

---

## 五、数据库拆分方案

### 5.1 当前数据库

```
lemnt_mall    — 包含 product/promotion/settlement/coupon/cart 所有表
lemnt_trade   — 包含 order/payment/aftersale/fulfillment/delivery/brokerage 所有表
```

### 5.2 拆分后数据库

```
lemnt_product    — 商品相关表
lemnt_promotion  — 营销活动相关表
lemnt_order      — 订单/售后/发货/物流/分佣相关表 + 结算表
lemnt_pay        — 支付/退款/钱包相关表
```

### 5.3 表归属映射

#### lemnt_product 数据库

```sql
-- 商品核心
product_spu, product_sku, product_spec, product_spec_value
brand, category, store_product
-- 商品互动
product_comment, product_favorite, product_browse_history
-- 库存
stock_reservation, product_inventory
```

#### lemnt_promotion 数据库

```sql
-- 营销活动
seckill_activity, seckill_item
bargain_activity, bargain_record
combination_activity, combination_record
discount_activity, discount_rule
point_activity, point_product
-- 优惠券
coupon_template, coupon, coupon_record
-- 购物车
cart_item
-- DIY/客服
diy_page, diy_template, kefu_conversation, kefu_message
-- 活动关联
promotion_banner, promotion_reward
```

#### lemnt_order 数据库

```sql
-- 订单核心
trade_order, trade_order_item, trade_order_log
-- 结算
settlement_record, settlement_order, settlement_item
-- 订单优惠快照
order_promotion, order_item_promotion, order_promotion_refund
-- 售后
after_sale, after_sale_log
-- 发货/物流
shipment, shipment_track
delivery_express, delivery_express_template, delivery_express_template_charge
-- 履约
fulfillment_order, fulfillment_shipment
-- 自提
pickup_store, pickup_order
-- 分佣
brokerage_record, brokerage_user, brokerage_withdraw
-- 配置
trade_config
```

#### lemnt_pay 数据库

```sql
-- 支付应用/渠道
pay_app, pay_channel_config
-- 支付流水
payment_record, payment_prepay
-- 支付通知
pay_notify_task, pay_notify_log
-- 退款
payment_refund
-- 钱包
wallet, wallet_recharge, wallet_transaction, wallet_transfer
-- 充值套餐
recharge_package
```

### 5.4 跨库查询处理

拆分后，原来同一个数据库的 JOIN 查询需要改为：
- 应用层组装：先查主库，再 Dubbo 查其他模块组装
- 冗余字段：订单表冗余商品名称/图片（快照），避免反查 product
- 数据同步：关键数据通过 MQ 事件同步

---

## 六、分步迁移顺序

### 原则
- **先拆低耦合的模块**，后拆高耦合的
- **每步独立可验证**，不做大爆炸式迁移
- **保持接口兼容**，消费方无感知

### Phase 1：拆分 lemnt-product（风险最低）

**理由**：product 是最底层的模块，不依赖其他业务模块，被其他模块依赖。

**步骤**：
1. 创建 `lemnt-server/lemnt-product/` 模块骨架（6 层结构）
2. 从 `lemnt-mall` 复制 product 子域的 domain/application/adapter/infrastructure 代码
3. 创建 `lemnt-product-client`，迁移 ProductCatalogClient、ProductCommentClient
4. 从 `lemnt-trade` 复制 inventory 子域到 product
5. 迁移 mall 的 member-ACL（MemberSnapshot、MemberLookupGateway）
6. 拆分 `lemnt_mall.sql` 中 product 相关表到 `lemnt_product.sql`
7. 更新 `lemnt-mall` 和 `lemnt-trade` 的 pom.xml，依赖 `lemnt-product-client`
8. 编译验证，运行测试

**验证点**：
- `mvn clean package` 全部通过
- 商品 CRUD 功能正常
- trade 模块的库存扣减通过 Dubbo 调用正常

### Phase 2：拆分 lemnt-pay（风险低）

**理由**：pay 的边界清晰（payment + paymentrefund），通过 MQ 与 order 解耦。

**步骤**：
1. 创建 `lemnt-server/lemnt-pay/` 模块骨架
2. 从 `lemnt-trade` 复制 payment + paymentrefund 子域
3. 创建 `lemnt-pay-client`，定义 PaymentClient、WalletClient
4. 拆分 `lemnt_trade.sql` 中支付相关表到 `lemnt_pay.sql`
5. 引入 MQ：支付成功发送 `PAY_SUCCESS` 消息
6. order 模块改为消费 MQ 消息更新订单状态（替代同步调用）
7. 更新消费方依赖

**验证点**：
- 支付流程正常（下单→支付→回调→订单更新）
- 退款流程正常
- 钱包充值/扣减正常
- MQ 消息可靠投递

### Phase 3：拆分 lemnt-promotion（风险中）

**理由**：promotion 体量最大，但与 order 通过 Dubbo 接口解耦。

**步骤**：
1. 创建 `lemnt-server/lemnt-promotion/` 模块骨架
2. 从 `lemnt-mall` 复制 promotion + coupon + cart 子域
3. 迁移 trade/promotion-snapshot 到 order（不是 promotion）
4. 创建 `lemnt-promotion-client`，定义 PromotionCalcClient、PromotionLockClient
5. 拆分 `lemnt_mall.sql` 中 promotion/coupon/cart 相关表
6. order 模块通过 Dubbo 调用 promotion-client 计算优惠
7. 下单时 promotion 冻结资源，支付成功后 MQ 通知核销

**验证点**：
- 各种营销活动功能正常
- 下单时优惠计算正确
- 优惠券冻结/核销/回退流程正确

### Phase 4：重组 lemnt-order（风险中）

**理由**：order 吸收 settlement 后成为下单流程的入口。

**步骤**：
1. 将 `lemnt-mall/settlement` 迁入 `lemnt-order`
2. 原 `lemnt-trade` 剩余的 order/aftersale/fulfillment/delivery/brokerage/config/pickup 全部归入 order
3. 从 `lemnt-mall` 迁移 member-ACL 中订单需要的部分（MemberDeliveryAddress、MemberTradeChange）
4. `CheckoutLifecycleClient` 从 mall-client 迁移到 order-client
5. 拆分 `lemnt_mall.sql` 中 settlement 表到 `lemnt_order.sql`
6. 拆分 `lemnt_trade.sql` 中所有表到 `lemnt_order.sql` 或 `lemnt_pay.sql`
7. 前端 API 路径不变（通过网关路由兼容）

**验证点**：
- 完整下单流程正常（购物车→结算→下单→支付→发货→完成）
- 售后流程正常
- 物流跟踪正常

### Phase 5：清理

1. 删除 `lemnt-mall` 模块（所有代码已迁出）
2. 删除 `lemnt-trade` 模块（所有代码已迁出）
3. 更新 `lemnt-server/pom.xml` 的 modules 列表
4. 更新 Nacos 配置（新增 product/promotion/pay 的配置）
5. 更新网关路由配置
6. 全量回归测试

---

## 七、风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 数据库拆分后跨库 JOIN | 查询性能下降 | 冗余字段 + 应用层组装 |
| MQ 消息丢失 | 订单状态不一致 | 事务消息 + 死信队列 + 人工补偿 |
| Dubbo 调用链变长 | 响应时间增加 | 超时配置 + 熔断降级 |
| 拆分期间代码冲突 | 开发阻塞 | 每步快速合并，避免长期分支 |
| 前端 API 路径变化 | 前端改造 | 网关路由兼容，路径不变 |

---

## 八、工作量估算

| 阶段 | 预估工作量 | 说明 |
|------|-----------|------|
| Phase 1: product | 3-5 天 | 低耦合，直接迁移 |
| Phase 2: pay | 3-5 天 | 需引入 MQ |
| Phase 3: promotion | 5-7 天 | 体量最大，接口调整多 |
| Phase 4: order | 5-7 天 | 吸收 settlement，重组 |
| Phase 5: cleanup | 2-3 天 | 删除旧模块，配置更新 |
| **合计** | **18-27 天** | 1 人全职，可并行压缩 |

---

## 九、验证命令

每个 Phase 完成后执行：

```bash
# 1. 编译验证
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home \
  mvn -f lemnt-common/pom.xml clean install -DskipTests
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home \
  mvn -f lemnt-server/pom.xml clean package -DskipTests

# 2. 架构检查
python3 scripts/check_architecture.py

# 3. 测试
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home \
  mvn -f lemnt-server/pom.xml test
```
