# 第161批：会员与交易配置集中存储交付

2026-11-02，v1，责任Skill：code-software-engineer，需求member-domain-structure。
输入：[本批设计v1](161-business-config-migration-design.md)，基线a457e0e7；保留第160批既有未提交改动，本轮未commit/push。

## 实现

- member_config四个积分字段迁至sys_config的member.point.config；trade_config十五个业务字段及既有id=1合同迁至trade.config。完整JSON单行读取/保存，不拆分为多个key；金额与积分数量单位、业务页面、权限、HTTP字段与包装保持原合同。
- system-client增加内部保存BO与RPC；member/trade Gateway封装RPC和JSON，删除不再使用的旧DO/Mapper/Converter/XML及本地配置保存事务。不存在回退旧表或故障降级默认值。
- system对保留key单次upsert，唯一键兜底并发；创建审计保留、修改审计更新，保存失败回滚，清除对应旧缓存。权威读取继续查数据库，不增加客户端缓存。
- 通用配置新增/修改/重命名/混合删除均保护两个保留键，不能绕过原业务校验；RPC只接受规范保留key及JSON对象。
- 配送维护原来依赖trade_config行锁，现使用独立DeliveryManagementLockGateway与项目LockTemplate/Redisson，在数据库事务完成后释放，保持编码/模板名称的并发维护保护。会员等级/签到共享锁保持。
- 新库不再创建旧配置表；sys_config值扩为LONGTEXT，新增config_key唯一索引。提供旧库结构升级、配置复制和不覆盖目标值的默认种子；旧库表不自动删除。

## 验证证据

最终执行：

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home \
/opt/homebrew/opt/maven/bin/mvn -o -f lemnt-server/pom.xml \
  -pl lemnt-system/lemnt-system-start,lemnt-member/lemnt-member-start,lemnt-trade/lemnt-trade-start \
  -am test -DargLine=-javaagent:/Users/wangxx/.m2/repository/org/mockito/mockito-core/5.23.0/mockito-core-5.23.0.jar
```

| 范围 | 结果 |
| --- | --- |
| system全模块 | PASS，142项 |
| member全模块 | PASS，119项 |
| trade全模块 | PASS，282项 |
| mall依赖模块 | PASS，45项；不含mall适配及启动模块全量 |
| 合计 | 588项，0失败、0错误、0跳过，BUILD SUCCESS |
| git diff --check | PASS |

增量编译曾残留已删除Mapper XML，通过clean清除旧构建产物；发布需使用干净构建。全量回归暴露结算夹具未提供新配送锁传输，已补齐后重跑通过。

新增重点证据：

- BusinessConfigProtectionTest：通用新增/修改/重命名/混合删除被保护；非法key或非对象JSON拒绝。
- BusinessConfigPersistenceTest：生产DDL/Mapper，长JSON、审计、并发唯一且整组替换、失败回滚；生产复制SQL验证来源首条会员配置、交易数组与金额单位、目标值保护与重复执行。
- MemberConfigStorageTest/TradeConfigStorageTest：真实业务Gateway的完整JSON读写、配置缺失及RPC故障传播。
- DeliveryManagementLockTest：真实事务提交/回滚后才解锁，拒绝非事务调用；配送、分销和101项结算集成继续通过。

原始日志：`/private/tmp/config-migration-full-tests.log`；各模块target/surefire-reports为本轮报告。机器证据：[business-config-migration-verification.json](evidence/business-config-migration-verification.json)。

## 发布、限制及交接

发布说明：[业务配置迁移](../../docs/business-config-migration.md)。已有库执行顺序：重复key核查与备份 → business_config_storage.sql → migrate_business_config.sql → 默认种子 → 干净构建并先发布system再发布member/trade。源库名按实际调整，先复制再补默认值，目标已存在key不覆盖。

NOT RUN：现有MySQL结构/数据迁移、实际Dubbo跨进程、部署后网关/浏览器、Redis集群和真实支付渠道。H2只移除了MySQL COLLATE声明，不能代替实际MySQL执行；业务集成中的RPC/Redis传输为显式替身，system存储另有真实Mapper/事务测试。无前端字段修改，本批未重跑前端测试。

切换后不双写，回滚前须先把sys_config最新值回填旧表并核对。旧表保留用于核对，不能直接回滚读取陈旧数据。下一接收方为QA/用户真实环境验收；本报告不代替人工验收，未重算整体完成率。
