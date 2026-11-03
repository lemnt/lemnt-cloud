# 数据库表与字段注释

2026-11-02，第163批。当时覆盖system、member、mall、trade初始化SQL中的95张表、1040个字段。本批补齐60处表注释、847处字段注释；已有非空注释保留。只调整COMMENT，不改字段类型、默认值、空值、索引、约束或业务数据。

2026-11-03续更：第164批新增交易优惠结构后，当前初始化盘点为98张表、1100个字段，COMMENT缺失0；当前schema-comments.json已与DDL同步。第163批的95表/1040字段为当时交付基线，schema-comments-legacy.json保留历史描述，不随当前结构重写。

## 新库及检查

第166批起member/mall/trade分别读取`sql/lemnt_member.sql`、`sql/lemnt_mall.sql`、`sql/lemnt_trade.sql`；system仍读取`lemnt-server/sql/lemnt_system.sql`，本次不调整system。注释目录为 `lemnt-server/sql/schema-comments.json`，按服务、表、字段登记中文含义；包含已合并到最终建表中的specs快照、来源订单、赠券序号等。三个业务服务不再保留历史升级SQL，详见[当前初始化入口](database-initialization.md)。

从项目根目录执行只读覆盖检查：

```bash
python3 -B lemnt-server/sql/schema_comments.py
python3 -B -m unittest discover -s lemnt-server/sql -p test_schema_comments.py -v
```

第一条输出当前表/字段数量及missing数组，有缺失时退出码为1。新增表/字段时同步维护目录和原DDL，可用 `--fill` 仅补缺失注释；工具会检查剔除COMMENT后的DDL词法序列保持一致，拒绝非注释结构变化。

金额按实际存储及业务合同标明元/分，状态按实际枚举说明；会员状态的1启用/0停用，与促销、快递等0启用/1停用保持各自原业务口径。等级记录的经验门槛/手工差值和变更后余额分别说明。旧快照不以当前商品回填；specs的字段名和历史空值含义保持第162批合同。

## 已有数据库

不要重跑初始化SQL，也不要直接将仓库里的字段类型复制到已有库。

工具使用本机mysql客户端；连接信息读取DB_HOST、DB_PORT、DB_USER、DB_PASSWORD环境变量，默认主机127.0.0.1、端口3306。密码只传给mysql子进程环境，不放入命令行、生成SQL或目录。请通过既有安全方式注入连接环境变量，不把真实凭证保存到脚本或文档。

先按服务生成计划，例如：

```bash
python3 -B lemnt-server/sql/schema_comments.py --domain member --database lemnt_member > /private/tmp/member-comments-plan.sql
python3 -B lemnt-server/sql/schema_comments.py --domain mall --database lemnt_mall > /private/tmp/mall-comments-plan.sql
python3 -B lemnt-server/sql/schema_comments.py --domain trade --database lemnt_trade > /private/tmp/trade-comments-plan.sql
python3 -B lemnt-server/sql/schema_comments.py --domain system --database lemnt_system > /private/tmp/system-comments-plan.sql
```

省略database时使用lemnt_<domain>。生成计划仅执行SHOW TABLES/SHOW CREATE TABLE；只处理目录中存在且实际库也存在的表/字段，未知扩展字段不猜测。实际库已有非空注释不替换；不存在的表会在输出中标记跳过。

计划中的字段MODIFY COLUMN直接保留实际SHOW CREATE TABLE列定义，包括真实精度、字符集、默认值、ON UPDATE、AUTO_INCREMENT和生成列表达式；只增加COMMENT。表说明使用ALTER TABLE COMMENT，不重建表或索引。

核对备份、计划及维护窗口后，显式执行，例如：

```bash
python3 -B lemnt-server/sql/schema_comments.py --domain member --database lemnt_member --apply
```

其余服务按对应domain执行。执行时重新读取实际定义，已补齐的注释会跳过，可重复运行。DDL由MySQL提交，工具不提供事务回滚；执行期间应避免并行结构修改。目录只覆盖当前版本表/列，不代表未知旧表或自定义字段也已补齐。

本轮没有运行上述已有库连接/升级操作。Python测试验证生成逻辑；H2回归验证初始化SQL与原业务链路，不能替代目标MySQL的DDL演练和实际注释验收。

## 本地库实际执行记录（2026-11-02）

已按用户授权在Docker mysql8的四个lemnt库执行，补62处表注释、860处字段注释。实际97表/1069字段的注释缺失已归零。库中保留的member_config/trade_config已通过schema-comments-legacy.json登记，已有库模式会一并处理；初始化覆盖检查仍只统计当前95张表。

建表定义快照、计划及复查结果保存在 `/private/tmp/lemnt-schema-comments-20261102/`。逐列属性保持，已有非空注释保留；MySQL对8张表CHECK中的ASCII字面量做了字符集标记规范化，已单独核对约束逻辑相同。未执行其他结构迁移或部署。
