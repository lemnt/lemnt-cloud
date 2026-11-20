# 会员与交易配置集中存储

2026-11-02，第161批。仅迁移 `member_config` 和 `trade_config`；签到规则、支付应用/渠道、秒杀时段、运费模板等继续独立存储。

## 配置合同

| sys_config.config_key | JSON内容 | 原维护入口 |
| --- | --- | --- |
| member.point.config | pointTradeDeductEnable、pointTradeDeductUnitPrice、pointTradeDeductMaxPrice、pointTradeGivePoint | GET/PUT /member/growths/config；member:config:query/update |
| trade.config | 原交易配置全部字段，包括id=1、配送、售后原因、分销及提现参数 | GET/PUT /trade/admin/config；trade:config:query/update |

每个key存一个完整JSON对象，单次读取/保存整个对象；未新增HTTP字段，页面和返回包装沿用既有合同。积分单位金额、包邮门槛、最低提现金额继续为分；`pointTradeDeductMaxPrice`仍是每单抵扣积分数，0不限。交易配置列表字段保存为JSON数组，不双重编码。

原业务服务继续执行开关、非负数、返佣比例之和、列表长度等校验。两个保留key只能通过原业务入口维护；system通用新增、编辑、重命名和删除返回409，不能绕过业务权限和校验。通用编辑框仍保留普通参数500字符限制，业务长配置经原业务表单维护。

内部Dubbo新增 `RemoteConfigService.saveBusinessConfig(BusinessConfigSaveBo)`，BO仅含configKey、configValue和operator；仅接受两个规范保留键及JSON对象。system按唯一key原子upsert并维护创建/修改审计，失败抛出，不使用旧表兜底。读取直接访问system权威库，不新增客户端缓存，保存清除该key旧缓存。

## 新环境

1. 使用更新后的 `lemnt-server/sql/lemnt_system.sql` 初始化system库：config_value为LONGTEXT，config_key有唯一索引。
2. 执行 `lemnt-server/sql/V20261102__business_config.sql` 初始化缺失的两个配置组。默认值与原新库初始值相同，两个开关组默认关闭；种子不覆盖已有key。
3. member/trade新库脚本不再创建旧配置表。生产业务SQL脚本仍会在末尾执行system的V*脚本。

## 已有环境

暂停配置写入并备份，确认库名后依次操作；以下仅为发布步骤，本轮没有执行现有数据库。

1. 在system库检查重复键：

```sql
SELECT config_key, COUNT(*) AS count
FROM sys_config GROUP BY config_key HAVING COUNT(*) > 1;
```

存在重复时先核实真实值，禁止自动删除。确认目标业务key若已存在，其值是否就是本次要保留的配置。

2. 在system库执行 `lemnt-server/sql/upgrade/business_config_storage.sql` 一次，扩容量并增加唯一约束。已使用新建表结构的库不重复执行。
3. 执行 `lemnt-server/sql/upgrade/migrate_business_config.sql`。脚本默认库名为lemnt_system、lemnt_member、lemnt_trade，按实际环境修改；需要发布账户具有跨库读取权限。member沿旧逻辑复制最小ID那一条，trade复制id=1；来源审计原样保留。目标key存在时不覆盖。
4. 对照源值核对目标两个JSON对象，再执行默认种子 `V20261102__business_config.sql` 补缺失项。**旧库不能先执行默认种子再复制**，否则已有默认key会使复制跳过。
5. 先发布system，再发布member/trade。新增Dubbo保存方法要求提供方先更新；部署后验证原页面保存、交易配置读取和积分报价。运行服务账户不需要跨库直连权限。

迁移不删除旧表。切换后不再双写，回滚时必须先将sys_config最新值回填旧表并核对；直接回滚代码会读到陈旧值。

## 锁与验证边界

原trade_config行锁还保护快递公司编码和运费模板名称维护，现改为独立配送维护锁，沿用LockTemplate，在事务提交或回滚后释放。原会员成长共享锁继续供等级、签到规则使用。

H2测试验证生产建表DDL（仅移除H2不支持的MySQL COLLATE声明）、复制SQL、Mapper、长JSON、唯一约束、审计、并发和失败回滚。业务集成测试中的Dubbo与Redis传输使用显式测试替身；实际MySQL执行、跨进程Dubbo和部署后网关HTTP验收另行记录，不能以这些测试替代。
