# 第165批交付：全项目统一规范与代码重构

2026-11-03。完成全项目源码分析、批量规范重构和本地验证。依据是用户明确选择“按统一规范批量重构全部代码”，以及“注解import后用短名”。未commit、未push，未执行现有库迁移或真实环境验收。

## 结果

- Java生产及测试3001文件统一格式与导入规则；255处全限定注解、68文件改为`import ...Schema;`配合`@Schema(...)`等短名。根/后端/管理端/小程序规范形成统一入口，格式工具会阻止回退。
- 19个Mapper的68个默认方法、3个私有条件方法迁到现有GatewayImpl；15处注解SQL迁到XML；两处原子计数改用setIncrBy，数据库时间更新放XML。钱包、充值和会员行锁明确关闭缓存并刷新会话缓存。
- 修复单条角色撤销的userId/roleId反置，以及BigInteger溢出long后错误输出JSON数字的精度缺陷。两者均先用新增测试复现，再修复。
- HTTP绑定与响应对象隔离，补Schema和Serializable，两个QryExe改为QueryExe。字典分页默认1/10并拒绝非正参数；用户嵌套部门保留NULL子节点，部门树保留旧空名称/false/空children规则。
- 管理端全量lint/format积累已清理；小程序JS/JSON/WXSS统一格式，WXML只改标签行缩进。当前数据库注释目录与98表/1100列一致，历史目录不重写。
- README补齐实际工程和接口前缀，旧小程序交付状态标作历史，避免用旧记录判断当前实现。

[全项目分析](165-project-code-standards-analysis.md)、[统一规范](../../docs/code-standards.md)、[改动说明与文件清单](165-project-code-standards-files.md)。第164批促销退款业务见[单独交付](164-order-promotion-refund-delivery.md)。

## 实际验证

| 范围 | 测试 | 构建/静态检查 |
|---|---:|---|
| common | 2 | install通过；供全部服务使用 |
| trade | 311 | 全后端clean package通过 |
| mall | 197 | 同上 |
| member | 122 | 同上 |
| system | 147 | 同上，包含新增边界/授权回归 |
| auth/gateway/open | 20/35/1 | 同上；third-sdk示例也完成打包 |
| 管理端 | 320（64文件） | typecheck、全量lint、format、build通过 |
| 小程序 | 179 | JS/JSON/WXSS格式、38个WXML缩进检查通过 |
| 规范工具/SQL注释工具 | 10/7 | 源码/POM/XML规则违规0，注释缺失0 |

共1351个测试用例通过。代码扫描覆盖2797个Java生产文件、70个POM、56个Mapper XML。机器证据见[验证记录](evidence/project-code-standards-verification.json)和[架构扫描](evidence/project-code-architecture.json)。

Java首轮2996文件格式化前后JDK语法树指纹一致；后续有意的业务/结构变更由回归覆盖，不声称它们只是格式变化。WXML标签、属性、正文在忽略行首缩进后比对一致；TradeOrderMapper仅排版版本的SQL词法及动态XML结构一致。

重命名后执行了全后端clean package；检查system application JAR，旧QryExe两类为0，新QueryExe两类为2。规范工具校验固定格式化JAR的SHA256，没有加入业务运行依赖；只为公共序列化回归补了test scope的现有Spring测试依赖。

## 当前边界

上述是本地编译、测试、H2生产Mapper、MockMvc、契约与静态检查。未连接或迁移现有数据库；目标MySQL方言和实际锁、真实网关/Dubbo/Redis/MQ、支付渠道与账户线上对账、浏览器和微信原生编译/真机均未验收。规范检查违规0不等于所有业务和安全场景已穷尽。

管理端构建保留既有大于500kB分包提示，未提高阈值掩盖提示。原未提交改动及历史进度保留；完整工作区清单包括第164与165批，不能把其中所有历史行归为本批新增。
