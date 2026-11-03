# 第165批改动说明与文件清单

全部路径相对于项目根目录。完整新增/修改/删除清单见[evidence/project-code-standards-files.json](evidence/project-code-standards-files.json)，包含第164与165批合并工作区；两个QryExe内部类为重命名。

| 改动类别 | 主要位置 | 原因 |
|---|---|---|
| 全项目Java格式、注解与导入 | lemnt-common及lemnt-server的src/main、src/test | 固定AOSP、项目导入分组、注解短名；3001文件覆盖 |
| Mapper职责 | system/member/trade的19组Mapper与GatewayImpl | 条件构造归Gateway，Mapper保留BaseMapper/XML |
| 数据库特定操作 | trade resources/mapper、member MemberUserMapper.xml | 15处注解SQL及复杂更新移到XML，行锁关闭缓存 |
| 真实缺陷 | UserRoleGatewayImpl、BigNumberSerializer及回归测试 | 参数绑定和大整数精度 |
| HTTP边界 | system Adapter Controller/Req/Res/Converter | DTO隔离、原JSON保留、分页绑定校验 |
| 命名 | MenuTreeQueryExe、MenuUserRouterQueryExe及引用 | 统一QueryExe，clean后无旧class |
| 管理端 | lemnt-ui/src | 规范化19个既有未格式化文件、保留第164批联动、修prefer-const |
| 小程序 | services、pages、tests、config与prettier.config.mjs | 统一代码格式，WXML保留正文与指令 |
| 规范与工具 | 根AGENTS/.editorconfig、各端AGENTS、docs/code-standards.md、scripts | 持续检查同一规则，10项正反例测试 |
| 数据库注释目录 | schema-comments.json、docs/database-comments.md | 与新交易结构一致；原legacy目录及163交付证据保留 |
| 进度文档 | README、work-items本批交付及第164批记录 | 区分历史事实、当前代码、本地验证和真实验收 |

新增的正式运行表结构和升级SQL属于第164批；本批统一规范没有执行DDL。文件清单不包含build输出、临时格式化JAR、日志、IDE文件、凭据或Python缓存。
