# Mall 域归属与基础锁调整

用户确认按业务域组织；后续明确结算叫 settlement、库存归商品、不使用 shared，锁复用基础包。

## 实现结果

- 核心业务域为 product、cart、settlement、coupon、promotion；会员远程查询、状态校验和会员操作锁归 member。
- 商品域收拢分类、品牌、SPU/SKU、规格、评价及库存；移除 catalog、store、checkout、spec、comment、shared 等原混合包。
- 拆分混合应用编排和跨域 Converter；规则放回各所属域，优惠券状态处理只接收本域 CouponOrderReference。
- 统一内部 Settlement 类型名称，保持已有 HTTP 路径、权限和跨服务公开 client 接口兼容。
- 删除 mall_checkout_account 的模型、Mapper 和行锁代码，改用 lemnt-common-lock 的 LockTemplate/RedissonLockExecutor；事务完成后解锁，保留原并发范围。
- 新库脚本及数据迁移模板移除技术锁表，旧库提供独立清理脚本，未执行真实数据库操作。

详见 [域划分与部署说明](../../docs/mall-domain-structure.md)。验证和静态归属证据见 `evidence/mall-domain-structure-verification.json`，保留此前批次证据作为历史记录。本次没有提交或推送代码。
