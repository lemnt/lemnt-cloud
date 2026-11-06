# 业务服务数据库初始化

2026-11-03，第166批。按用户要求只整理member、mall、trade，system项目和system数据库SQL保持原样。

| 服务 | 唯一初始化文件 | 表数 | 字段数 |
|---|---|---:|---:|
| member | [lemnt_member.sql](../lemnt-server/lemnt-member/sql/lemnt_member.sql) | 17 | 149 |
| mall | [lemnt_mall.sql](../lemnt-server/lemnt-mall/sql/lemnt_mall.sql) | 35 | 379 |
| trade | [lemnt_trade.sql](../lemnt-server/lemnt-trade/sql/lemnt_trade.sql) | 29 | 403 |

三个服务合计81张表、931个字段。原V*文件的最终列、索引、唯一键、外键、检查约束均已合入CREATE TABLE；没有简单拼接ALTER语句。现有表和字段中文注释保留。

文件只允许CREATE TABLE和必要的INSERT基础数据。当前这三个服务没有必须预置的业务记录，因此没有增加会员、商品、订单、积分流水、活动或日志等演示数据。字典、菜单、权限和默认参数仍由原system脚本管理，本次不修改或复制。

## 使用

先创建对应空库并配置连接，再通过正常SQL客户端导入对应文件。文件不包含CREATE DATABASE、USE、DROP、ALTER、UPDATE、DELETE、跨库复制或历史回填。

CREATE TABLE IF NOT EXISTS允许重复建表检查；它不会把旧表升级为新结构，也不会覆盖已有记录。已有数据库应先对照实际结构另行制定和演练变更，本文件不负责迁移存量数据。本轮没有连接或修改运行中的数据库。

原member/mall/trade的V*、upgrade目录内SQL和第164批结构草案SQL已移除，不再按版本顺序执行。历史交付文档中出现的旧脚本名仅用于追溯当时方案；当前入口以本页为准。system目录及lemnt-server/sql中的系统初始化、菜单、参数和升级SQL不在此次整合范围。

## 验证

- 三文件在H2 MySQL模式下分别完整执行两次，检查建表及重跑行为。
- 对照合并前定义，核对81张表的字段及约束；全项目原98表/1100列的注释目录保持一致。
- 后端测试直接使用这三个正式文件，历史记录的NULL、规格JSON、审计和幂等读取继续回归，不再依赖已删除的升级脚本。
- schema_comments.py按新路径读取三个文件；工具测试防止重新增加分散脚本或混入迁移操作。

H2验证不代替目标MySQL实库演练。各项结果见[第166批交付](../work-items/member-domain-structure/166-database-initialization-delivery.md)。
