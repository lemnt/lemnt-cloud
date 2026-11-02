# 业务 SQL 发布

`apply-business-sql.sh` 按版本号顺序执行 member、mall、trade 的建表脚本，再执行 `lemnt-server/sql` 下的菜单/权限脚本。

## 使用

先备份目标库，再以试运行确认顺序：

```bash
DB_HOST=10.0.0.10 DB_USER=lemnt DB_PASSWORD='***' ./lemnt-server/sql/release/apply-business-sql.sh
```

确认后执行：

```bash
DB_HOST=10.0.0.10 DB_USER=lemnt DB_PASSWORD='***' ./lemnt-server/sql/release/apply-business-sql.sh --apply
```

可通过 `MEMBER_DB`、`MALL_DB`、`TRADE_DB`、`SYSTEM_DB` 覆盖库名，默认分别为 `lemnt_member`、`lemnt_mall`、`lemnt_trade`、`lemnt_system`。

## 边界

- 第161批将积分/交易配置迁入sys_config。已有环境必须先完成[配置迁移](../../../docs/business-config-migration.md)，再执行V20261102默认种子；不能先执行种子，否则旧值复制会因目标key已存在而跳过。新库先使用更新后的system基础表结构。

- `lemnt_system` 基础表不由本脚本重建；菜单脚本只依赖已存在的 `sys_menu`、`sys_role_menu`。
- 已有环境新增列使用对应模块 `sql/upgrade/` 下的脚本，逐个人工审核后执行；本脚本不自动执行，避免重复加列。
- 建表脚本按 `V*` 文件名数字顺序执行，不依赖数据库迁移工具记录。
- 执行前必须备份；脚本不处理回滚。
- 手工用 `docker exec mysql ... < file` 导入时可能继承 `latin1` 客户端字符集，导致中文菜单双重编码；务必加 `--default-character-set=utf8mb4`，本脚本已内置该参数。已乱码数据可用 `lemnt-server/sql/upgrade/fix_menu_mojibake.sql` 修复。
