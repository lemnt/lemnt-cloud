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

- `lemnt_system` 基础表不由本脚本重建；菜单脚本只依赖已存在的 `sys_menu`、`sys_role_menu`。
- 已有环境新增列使用对应模块 `sql/upgrade/` 下的脚本，逐个人工审核后执行；本脚本不自动执行，避免重复加列。
- 建表脚本按 `V*` 文件名数字顺序执行，不依赖数据库迁移工具记录。
- 执行前必须备份；脚本不处理回滚。
