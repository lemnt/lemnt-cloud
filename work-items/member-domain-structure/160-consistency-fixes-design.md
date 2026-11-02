# 第160批：一致性问题修复设计

2026-11-02，v1，需求 member-domain-structure，责任 Skill：code-software-engineer。
输入：本轮三项代码审查发现、第157–159批交付；基线 `a457e0e7`，分支 `feat/yudao-member-mall-trade-migration`。
参考工程 Skill 的 `references/development-design.md`、`references/java.md` 和 `references/api-documentation.md`，按小修复裁剪。

## 范围和规则

- 调价：沿用待支付状态、金额分摊、权限及原响应。支付模型对订单/渠道唯一且无渠道关单重建能力，因此在订单行锁内检查已有支付单，有支付单返回409“订单已生成支付单，不能调价，请取消订单后重新下单”；不改写已经发送给渠道的金额。支付创建同样持有订单行锁，两操作串行。改址不受影响。
- 赠券：每张赠券增加内部 `gift_sequence`（从1开始），唯一键改为来源订单+模板+会员+序号；来源订单保持原值。发放在原会员锁及模板行锁内按序号检查已发事实（包含逻辑撤回），再占名额并插入；重放不重复发券、不重复占名额。该序号不新增到HTTP字段。
- 营业时间：维护接口严格解析 `HH:mm:ss`，允许关门早于开门（跨夜），相同时间表示全天；建单继续使用同一语义。无新增配置或时间依赖。

## 任务及验收

1. 修改 trade `OrderAdjustmentService`，补已有支付单拒绝测试和无支付单调价回归；验证拒绝时不写金额和日志。
2. 修改 mall `Coupon`、DO、Converter、Gateway/Mapper、`CouponIssuanceService` 与 `RewardGiftService`，新增 `sql/V29__coupon_gift_sequence.sql` 及对应旧库脚本；扩展 `CouponSnapshotPersistenceTest`，以实际SQL、Mapper和事务代理验证同模板2张、重放、并发、逻辑撤回及旧记录迁移。
3. 修改 trade `PickUpStoreService`，补维护接口跨夜/全天/非法时间测试及建单跨夜回归。
4. 同步本工作项索引和交付记录，运行受影响后端模块测试；无前端字段变化。

## 迁移与风险

新库按V28→V29执行；已执行V28的旧库执行 `lemnt-mall/sql/upgrade/coupon_gift_sequence.sql` 一次。原唯一键保证每组最多1张，旧赠券回填序号1，普通券序号为空，再替换唯一键。须先迁移再发布新代码；本轮不修改正在运行的数据库。回滚前若已发多张赠券，不能直接恢复旧唯一键。

技术沿用JDK17、Spring Boot4.1.0、MyBatis-Plus及现有Maven/H2测试设施，版本来自依赖POM。权限、HTTP字段、MQ和外部支付集成不扩展。真实MySQL迁移、HTTP与渠道验收单独记录，不以H2代替。下一接收方：工程验证及用户验收。
