# 第160批：一致性问题修复交付

2026-11-02，v1，需求 member-domain-structure，责任 Skill：code-software-engineer。
输入：[修复设计 v1](160-consistency-fixes-design.md)，源码基线 `a457e0e7`，分支 `feat/yudao-member-mall-trade-migration`。本轮未 commit/push。

## 修复结果

1. 调价：在订单行锁内检查支付单，已生成任何渠道支付单即返回409，不写金额、不写日志；尚未生成支付单的订单继续按比例分摊。支付创建同样持有订单行锁。已有支付单需取消订单后重新下单，未增加渠道关单重建能力。
2. 赠券：增加内部 `gift_sequence`，四列联合唯一索引支持同模板多张赠券。按稳定序号派发，在原会员锁和模板锁内检查发放事实；Mapper使用 `FOR UPDATE` 当前读并包含逻辑撤回记录，重放不重复发券或占名额。补齐Domain/DO/Converter及自定义结果映射，HTTP不新增序号字段。
3. 营业时间：保存时严格解析 `HH:mm:ss`，允许跨夜和同刻全天营业，与既有建单规则一致；非法时分秒仍拒绝。

## 验证

最终命令（本机工具路径，仅为验证记录）：

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home \
/opt/homebrew/opt/maven/bin/mvn -o -f lemnt-server/pom.xml \
  -pl lemnt-trade/lemnt-trade-start,lemnt-mall/lemnt-mall-start -am test \
  -DargLine=-javaagent:/Users/wangxx/.m2/repository/org/mockito/mockito-core/5.23.0/mockito-core-5.23.0.jar
```

显式Mockito agent用于本机运行；HTTP测试使用沙箱外本地端口能力。

| 层级 | 结果 | 范围 |
| --- | --- | --- |
| mall各模块测试 | PASS，193项 | 单元、H2 MySQL模式、生产SQL、真实Mapper及事务代理 |
| trade各模块测试 | PASS，279项 | 单元、HTTP适配、H2持久化及结算支付集成 |
| 失败/错误/跳过 | 全部0 | Maven BUILD SUCCESS |
| 空白差异检查 | PASS | `git diff --check` |
| 真实MySQL升级、部署后HTTP | NOT RUN | 未修改现有库及运行服务 |
| 前端、真机、真实支付渠道 | NOT RUN | 本轮无前端改动或渠道接入 |

关键回归：

- `OrderAdjustmentServiceTest`：已有支付单拒绝调价且不写入。
- `OrderManagementHttpTest`：409 Problem Details及错误提示。
- `CheckoutFlowIntegrationTest`：先调价后支付采用新金额，再调价被拒绝后原支付仍能成功。
- `CouponSnapshotPersistenceTest`：同模板两张、并发/重复回调、撤回后重放、存量序号回填、数据库唯一键拒绝重复插入。
- `PickUpStoreHoursTest`、`PickUpOrderCreateTest`、`PickUpPersistenceTest`：跨夜/全天保存及建单、非法时间拒绝、跨夜配置落库返显；纠正旧测试将跨夜视为非法的历史预期。

证据：[consistency-fixes-verification.json](evidence/consistency-fixes-verification.json)。原始本机日志 `/private/tmp/member-fixes-final-tests.log`；模块 `target/surefire-reports` 为本次测试报告。

## 迁移与交接

- 新库在V28后继续执行 `lemnt-server/lemnt-mall/sql/V29__coupon_gift_sequence.sql`。
- 已执行V28的旧库执行 `lemnt-server/lemnt-mall/sql/upgrade/coupon_gift_sequence.sql` 一次，两者不要重复执行。先备份并迁移，再发布代码；原赠券回填序号1，普通券保持NULL。
- 已发多张赠券后无法直接恢复旧三列唯一索引，回滚前需核实数据。
- 文档：[调价接口](../../docs/order-price-adjustment-api.md)、[自提接口](../../docs/pickup-api.md)。权限和HTTP字段形状不扩展。
- 下一接收方为QA/用户：真实MySQL迁移、发布及网关验收待完成；本报告不代替人工验收。旧42.3%未重新统计。
