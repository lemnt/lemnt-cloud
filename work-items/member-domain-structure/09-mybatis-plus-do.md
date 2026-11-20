# Mapper 与 DO 对齐 system（2026-10-27）

后续服务归属变化见 [商城与交易服务边界调整](10-mall-trade-boundary.md)；本页表名和数量记录迁移前状态。

## 范围与实现

本次调整 member、mall、trade 的 Infrastructure Mapper 与 DO。system 是风格基准；auth 仍无数据库，不新增 Mapper、DO 或数据库依赖。

- 43 个按表拆分的 Mapper 均继承对应 DO 的 `BaseMapper`。基础查询、统计、插入、删除和字段更新改为 MyBatis-Plus 默认方法，共 123 个默认方法。
- 分页使用 `Page` 与条件构造器，保留原有排序、筛选和独立的总数查询。手工创建 SqlSessionFactory 的集成测试加入分页拦截器。
- DO 补齐 `@TableName`、真实主键及 `IdType`、Serializable、Lombok 配置。数据库原有但 DO 缺少的标识及字段补齐；SKU 的 code/image 显式映射 product_code/main_image。
- 对已有表新增 9 个缺失的 DO（见下表），没有创建任何数据库表。只有拥有完整审计列的实体沿用 BaseDO，只有 create_time 的表独立声明该字段。
- 联合主键参照 system UserRoleDO，不虚构单列主键，也不使用 xxById 操作。MyBatis-Plus 因联合主键未注入 xxById 的日志与此一致。
- 会员计数器锁定查询返回 PromotionMemberCounterDO，避免将不同表映射到 PromotionCounterDO。
- 更新使用 lambdaUpdate 显式 set，保留 null 清除字段的语义，避免 updateById 默认忽略 null。
- 行锁、数据库时钟、UPSERT 幂等、CAS、库存与额度原子更新、跨表查询和带引用条件的删除保留专用 SQL。
- 查询投影如 StoreProductDO、PricingFactsDO、OrderSummaryDO、PromotionOrderDO 保持投影用途，不伪装成整表实体；统一序列化与对象格式。

## 验证

会员、商城、交易的数据库集成测试使用 H2 MySQL 模式执行实际 Mapper，覆盖事务、并发、幂等、回滚、归属、库存和额度。额外检查分页第二页及越界页、总数保持，以及资料字段可置空。

clean verify 覆盖 auth-start、member-start、mall-start、trade-start、gateway 及依赖模块，304 项测试通过，无失败、错误或跳过。补充回归断言后的最终 verify 同样为 304 项通过；日志见 evidence/mybatis-plus-do-final-verify.log。

未运行目标 MySQL，也没有执行数据库迁移。未 commit。

## 表与 Mapper/DO 对照

| 模块 | 表 | Mapper | DO | 主键策略 |
| --- | --- | --- | --- | --- |
| mall | mall_brand | BrandMapper | BrandDO | AUTO |
| mall | mall_category | CategoryMapper | CategoryDO | AUTO |
| mall | mall_product | ProductMapper | ProductDO | AUTO |
| mall | mall_sku_property | SkuPropertyMapper | SkuPropertyDO | composite |
| mall | mall_spu | SpuMapper | SpuDO | AUTO |
| mall | mall_stock_adjustment | StockAdjustmentMapper | StockAdjustmentDO | INPUT |
| mall | mall_stock_reservation | StockReservationMapper | StockReservationDO | AUTO |
| member | member_address | MemberAddressMapper | AddressDO | AUTO |
| member | member_asset_record | MemberAssetRecordMapper | AssetRecordDO | AUTO |
| member | member_config | MemberConfigMapper | ConfigDO | INPUT |
| member | member_group | MemberGroupMapper | GroupDO | AUTO |
| member | member_level | MemberLevelMapper | LevelDO | AUTO |
| member | member_level_record | MemberLevelRecordMapper | LevelRecordDO | AUTO |
| member | member_sign_config | MemberSignConfigMapper | SignConfigDO | INPUT |
| member | member_sign_record | MemberSignRecordMapper | SignRecordDO | AUTO |
| member | member_tag | MemberTagMapper | TagDO | AUTO |
| member | member_tag_link | MemberTagLinkMapper | MemberTagLinkDO | composite |
| member | member_user | MemberUserMapper | MemberDO | AUTO |
| trade | trade_cart | CartMapper | CartDO | AUTO |
| trade | trade_cart_add_request | CartAddRequestMapper | CartAdditionDO | composite |
| trade | trade_checkout | CheckoutMapper | CheckoutDO | INPUT |
| trade | trade_checkout_account | CheckoutAccountMapper | CheckoutAccountDO | INPUT |
| trade | trade_checkout_cart | CheckoutCartMapper | CheckoutCartDO | composite |
| trade | trade_coupon | CouponMapper | CouponDO | AUTO |
| trade | trade_coupon_public_claim | CouponPublicClaimMapper | CouponPublicClaimDO | INPUT |
| trade | trade_coupon_template | CouponTemplateMapper | CouponTemplateDO | AUTO |
| trade | trade_inventory_confirmation | InventoryConfirmationMapper | InventoryConfirmationDO | INPUT |
| trade | trade_order | TradeOrderMapper | TradeOrderDO | AUTO |
| trade | trade_order_item | TradeOrderItemMapper | TradeOrderItemDO | AUTO |
| trade | trade_outbox | TradeOutboxMapper | TradeOutboxDO | AUTO |
| trade | trade_payment | TradePaymentMapper | TradePaymentDO | AUTO |
| trade | trade_promotion_admin_request | PromotionRequestMapper | PromotionRequestDO | composite |
| trade | trade_promotion_counter | PromotionCounterMapper | PromotionCounterDO | INPUT |
| trade | trade_promotion_member_counter | PromotionMemberCounterMapper | PromotionMemberCounterDO | composite |
| trade | trade_promotion_root | PromotionRootMapper | PromotionRootDO | AUTO |
| trade | trade_promotion_scope | PromotionScopeMapper | PromotionScopeDO | composite |
| trade | trade_promotion_usage | PromotionUsageMapper | PromotionUsageDO | composite |
| trade | trade_promotion_usage_event | PromotionUsageEventMapper | PromotionUsageEventDO | AUTO |
| trade | trade_promotion_version | PromotionVersionMapper | PromotionVersionDO | composite |
| trade | trade_refund_request | RefundRequestMapper | RefundRequestDO | INPUT |
| trade | trade_settlement_abandon | SettlementAbandonMapper | SettlementAbandonDO | composite |
| trade | trade_settlement_order | SettlementOrderMapper | SettlementOrderDO | INPUT |
| trade | trade_shipment | ShipmentMapper | ShipmentDO | INPUT |
