# 商城表结构整理

本次以芋道的领域模型为参考，采用按活动类型建表：满减 `mall_promotion_reward_activity`、折扣 `mall_promotion_discount_activity`。不是每发布一次活动就创建一张新表，每个活动是一条记录。

参考源码：
- [RewardActivityDO](https://gitee.com/zhijiantianya/yudao-cloud/blob/master/yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/dal/dataobject/reward/RewardActivityDO.java)
- [DiscountActivityDO](https://gitee.com/zhijiantianya/yudao-cloud/blob/master/yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/dal/dataobject/discount/DiscountActivityDO.java)
- [CouponTemplateDO](https://gitee.com/zhijiantianya/yudao-cloud/blob/master/yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/dal/dataobject/coupon/CouponTemplateDO.java)

实际核对的是 DO 源码。该项目公开的 `ruoyi-vue-pro.sql` 不包含商城建表，不能将其作为商城 DDL 的验证证据。本项目保留已存在的不可变版本、发布状态及限次规则，不照搬未实现的促销玩法。

## 持久化边界

| 旧结构 | 当前归属 |
|---|---|
| `mall_promotion_root` | 按规则拆入满减/折扣活动表，使用业务列保存草稿 |
| `mall_promotion_counter` | 活动主表 held_count、used_count、global_limit、member_limit |
| `mall_promotion_member_counter` | 保留活动与会员的一对多计数，引用 activity_id |
| `mall_promotion_scope` | 移除重复投影，草稿范围在活动列，历史范围在不可变版本 |
| `mall_promotion_admin_request` | 基础锁包互斥 + Redis 已提交操作结果，30 天 |
| `mall_cart_add_request` | 移除，直接设置购物车最终数量，不保存请求流水 |
| `mall_coupon_public_claim` | 券模板 take_type：USER 公开领取、MANUAL 后台发放 |
| `mall_checkout`、`mall_checkout_cart` | Redis 统一提交快照及请求、购物车索引 |
| `mall_settlement_order`、`mall_settlement_abandon` | Redis 结算绑定与放弃标记，互斥写入 |

版本和使用历史是活动的一对多数据，保留 `mall_promotion_activity_version`、`mall_promotion_activity_usage`、`mall_promotion_activity_usage_event`。活动总计数与主表一起锁定更新，会员计数仍独立锁定。订单核券/释放直接查询优惠券绑定的订单号，缓存过期不会跳过有数据库绑定的券。结算预览 15 分钟，提交恢复信息 30 天；恢复窗口结束后的请求不承诺永久幂等。

商品表统一：`mall_product_spu`、`mall_product_sku`、`mall_product_category`、`mall_product_brand`、`mall_product_spec`、`mall_product_spec_value`、`mall_product_sku_spec`、`mall_product_comment`、`mall_product_stock_adjustment`、`mall_product_stock_reservation`。技术锁复用基础包，不新增账户锁表。

## 管理接口

满减 `/mall/promotion/reward-activities`，折扣 `/mall/promotion/discount-activities`。分别提供创建、草稿修改、发布、状态修改、详情和分页。根请求和响应对象分开；满减接收阶梯，折扣接收门槛、折扣比例和封顶金额。活动类型不可修改，跨类型详情或修改被拒绝。沿用现有促销权限。现有 `/mall/admin/promotions` 是兼容入口，底层同样按活动类型存储。

本次交付后台接口，不增加新的前端管理页面。

## 存量升级

新库使用商城 sql 根目录初始化脚本；V12/V13 已退役，不再包含可执行建表语句，不应交给 Spring ScriptUtils 作为初始化输入。

已有库使用 `lemnt-server/lemnt-mall/sql/upgrade/consolidate_business_tables.sql`，与新库初始化二选一。脚本未在真实 MySQL 执行。先备份并停写，禁止新旧实例混跑；DDL 有隐式提交，不能依靠事务整体回滚。先完成以下缓存数据导出、转换、导入和逐条核对，再在当前会话设置 `@mall_cache_migrated=1` 解除升级前置检查：

1. 结算回执与购物车版本引用合并为 `SettlementRecord`，配上原始 `SettlementOrder` 组成 `SettlementSubmission`。原始快照来自旧 Redis `mall:checkout:submission:{orderNo}` 或已存在的真实订单/商品明细；不得用当前商品价格重建。缺失快照必须人工处理，禁止跳过未完成订单。
2. 提交主键 `mall:{settlement}:v2:submission:{orderNo}`，内容使用项目 RedisSerializer 序列化；请求键 `mall:{settlement}:v2:request:{memberId}:{requestNo}` 值为订单号；购物车集合 `mall:{settlement}:v2:cart:{cartId}` 包含对应订单号。三者保持相同恢复时长。
3. 绑定键 `mall:{settlement}:v2:binding:{memberId}:{settlementId}` 值为订单号；放弃键 `mall:{settlement}:v2:abandon:{memberId}:{settlementId}` 值为 `1`。同一结算不能两者都有。
4. 已完成的后台操作转为 `PromotionRequest(action,fingerprint,PromotionReceipt)`，键 `mall:promotion:request:{operatorId}:{requestNo}`；未完成的旧操作先核对事务结果，不伪造成功回执。
5. 核对缓存反序列化、订单号、会员、金额、券、购物车版本、操作结果及 TTL。不能仅检查键数。缓存使用基础包序列化器和 30 天有效期，部署时保证 Redis 持久化和容量。

升级脚本保留活动 ID、版本、历史用量及计数，并将旧辅助表重命名为 `legacy_*_20261027`，不删业务数据。核对旧活动数等于两类活动总数、已发布版本引用、HELD/USED 总量及会员计数后才切换流量。旧表删除需另行批准。

跨库购物数据迁移另用 `lemnt-server/sql/migrations/mall-shopping-data-move.sql.template`，目标必须为当前空表，不能与同库升级重复执行；结算和后台请求缓存同样先迁移。缓存迁移是部署前的人工操作要求，本仓库没有自动读取生产数据库或批量迁移生产 Redis。

## 验证范围

回归使用 H2 MySQL 模式和生产 Mapper/XML，HTTP 测试检查两类落表、分页隔离和跨类型拒绝，交易测试覆盖同步锁券、支付核券、回滚及重试。Redis 传输在集成测试中由测试替身提供；缓存序列化和事务回调另有单元测试。尚未执行真实 MySQL 升级、真实 Redis Lua/Cluster 验证或线上接口验收。
