# Mall 业务域划分

## 归属

各层统一以业务域分包。Adapter 保持 `adapter/app/{domain}`、`adapter/web/{domain}`，Controller 直接放在域包中；Application、Domain、Infrastructure 使用同名域。

| 域 | 职责 |
| --- | --- |
| `product` | 商品、分类、品牌、SPU/SKU、规格及规格值、评价、库存调整及预占 |
| `cart` | 购物车、最终数量设置及购物车版本 |
| `settlement` | 结算报价、Redis 结算单、提交幂等回执、购物车版本引用、订单远程调用及支付后的券状态编排 |
| `coupon` | 优惠券模板、签发、公开领取、会员券包、用券规则及券状态持久化 |
| `promotion` | 促销草稿与发布版本、计价引擎、适用范围、次数约束和使用记录 |
| `member` | 对会员服务的查询边界、会员状态校验、会员操作锁；不保存会员资料或认证凭证 |

分类、品牌、规格、评价都归商品域。当前库存是 SKU 库存及其预占、调整记录，不单独划分 inventory 域。原 `catalog`、`store`、`checkout`、`spec`、`comment` 和 `shared` 包不再作为业务域。各域自行维护规则，优惠券不依赖整个结算聚合维护状态。

## 编排拆分

- 商品目录与管理服务统一放在 product，保留按表 Gateway、Mapper 和 DO；库存同属 product，使用独立库存服务。
- 原混合 StorefrontService 拆为 cart 的 CartAdditionService 与 coupon 的 CouponService。
- 原混合 CheckoutService 拆为 cart 的 CartService、coupon 的 CouponIssuanceService，以及 settlement 的 SettlementRecordService。
- 请求、响应、应用 DTO 与持久化 Converter 同步按域拆分，不通过跨域通用转换器混合处理购物车、券和结算。
- 结算内部类型使用 Settlement 命名。既有 HTTP 地址、权限字符串、业务表名、Redis 键和跨服务 client 接口保持兼容；因此路径中的 `/checkout` 和公开 RPC 接口名 `CheckoutLifecycleClient` 保留，结算业务包使用 `settlement`。

## 基础锁替换

移除 `mall_checkout_account` 对应的 DO、Mapper 和数据库行锁实现。此表只有 member_id，不包含账户余额或业务结算数据，不再以业务表保存技术互斥键。

会员锁由 member 域的 MemberLockGateway 封装，Infrastructure 使用现有 `lemnt-common-lock` 提供的 Lock4j LockTemplate 和 RedissonLockExecutor。锁键是 `lemnt:mall:member:operation:{memberId}`，保持原先同一会员的加购、领券和结算互斥范围。

锁等待最多 3 秒，未获取锁则报错；expire=-1 使用 Redisson 看门狗续期。同一数据库事务只获取一次同一会员锁，锁在事务提交或回滚完成后才释放。嵌套的新事务使用其自己的同步回调，不能释放外层事务持有的那次锁。锁必须从事务内获取，基础设施失败不降级为无锁执行。

优惠券有效期和结算计价仍读取数据库时间，分别通过 CouponTemplateGateway 和 SettlementRecordGateway 获取，不因删除锁表改用应用服务器时间。

## SQL 与部署

新库初始化 V10 不再创建 mall_checkout_account，跨库购物数据迁移模板不再迁移这一技术表，当前辅助表合并、活动拆分和存量升级要求见 [表结构整理](mall-schema-consolidation.md)。旧辅助数据需要先完成缓存迁移，不能直接删除。

已有库的清理脚本是 `lemnt-server/lemnt-mall/sql/upgrade/remove_checkout_account.sql`。先确认 Redis 和基础锁配置可用，停止旧版本写入，完成所有商城实例切换后再执行；数据库行锁版本与新分布式锁版本不可混合运行。此脚本未自动执行，现有数据库没有被修改。

## 验证范围

原有 HTTP 路由、权限和字段兼容性通过契约及映射测试验证；真实 Mapper、服务和事务的 H2 MySQL 模式回归覆盖加购、领券、结算、库存预占、支付核销、异步补偿及并发幂等。

新增锁测试验证提交后释放、回滚释放、同事务重复获取、嵌套新事务、获取失败和无事务拒绝。跨域集成用真实 LockTemplate 与生产锁 Gateway，仅以本地重入锁替代 Redis 传输。未连接真实 Redis 验证网络故障或看门狗续期，未执行真实 MySQL 清理或部署端到端验收。

## MyBatis Plus 与 XML 约定

普通 CRUD 直接复用 BaseMapper，在各域 GatewayImpl 中使用 Lambda 条件，不为现有能力重新命名包装 Mapper 方法。联合主键用完整条件，更新保留业务状态和版本约束。字段增减使用 MyBatis Plus 提供的 Lambda 增减方法。

跨表查询、复杂原子约束、upsert、数据库时间和需要控制查询缓存的行锁语句集中在 `infrastructure/src/main/resources/mapper/*.xml`，Java Mapper 只声明必要的自定义语句接口。XML 平铺目录兼容既有 `classpath:/mapper/**.xml` 配置；行锁关闭查询缓存。没有使用 JPA。
