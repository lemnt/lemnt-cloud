# 第163批：表和字段注释补齐交付

2026-11-02，v1；责任Skill：code-software-engineer。输入：[本批设计](163-schema-comments-design.md)及用户注释补齐要求；基线0f5055f0。本轮未commit/push。

## 范围与产出

| 服务 | 表数 | 字段数 | 本批补表注释 | 本批补字段注释 |
| --- | --- | --- | --- | --- |
| system | 17 | 169 | 0 | 0 |
| member | 17 | 149 | 16 | 148 |
| mall | 35 | 379 | 33 | 379 |
| trade | 26 | 343 | 11 | 320 |
| 合计 | 95 | 1040 | 60 | 847 |

38份初始化/新增列SQL就地补COMMENT。保留现有SQL布局及非空注释，没有修改业务Java、前端或历史升级脚本；不顺带删除仍在初始化中的历史短信/第三方身份表。

注释来源为既有COMMENT、DO/领域模型、服务校验和当前接口合同。金额元/分、支付及售后状态、分销佣金与提现状态、优惠支付比例、签到与等级记录经验语义已核对；未知业务编码不推测。

新增 `lemnt-server/sql/schema-comments.json` 作为完整目录；`schema_comments.py` 可检查/补齐初始化COMMENT，并根据实际SHOW CREATE TABLE生成已有库缺失注释计划，默认不写库。该工具保留实际字段定义，只有--apply才执行。`test_schema_comments.py`验证嵌套/引号、旧注释保留、ADD COLUMN、实际定义保真、未知字段跳过和目录一致性。

## 验证

- PASS：95表/1040字段覆盖检查，missing为空。
- PASS：逐文件对照HEAD，移除COMMENT属性后SQL词法序列完全相同；已有非空表/列注释保持原文。
- PASS：Python unittest 7项。
- PASS：member119、mall195、trade283，共597项后端测试，0失败/错误/跳过。
- PASS：git diff --check。

后端最终命令：

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home \
/opt/homebrew/opt/maven/bin/mvn -o -f lemnt-server/pom.xml \
  -pl lemnt-member/lemnt-member-start,lemnt-mall/lemnt-mall-start,lemnt-trade/lemnt-trade-start \
  -am test -DargLine=-javaagent:/Users/wangxx/.m2/repository/org/mockito/mockito-core/5.23.0/mockito-core-5.23.0.jar
```

原始日志 `/private/tmp/schema-comments-backend-tests.log`；机器证据：[schema-comments-verification.json](evidence/schema-comments-verification.json)。system原DDL注释已完整且本批未改，未单独重跑system业务测试；无UI变更，未重跑前端。

## 交接与限制

[已有库操作说明](../../docs/database-comments.md)包含只读计划与--apply步骤。NOT RUN：真实MySQL读取/DDL升级、实际information_schema注释验收、部署及业务数据核对。工具的列属性保真已用复杂SHOW CREATE样例验证，但未连接目标库，不宣称真实库已补齐。

下一接收方为QA/用户真实环境演练和审核。本批仅完成代码与SQL交付，不代替人工验收，不重算整体迁移完成率。

## 2026-11-02 已有库实际执行

用户明确要求“数据库执行下”后，在本地Docker mysql8（MySQL8.0.46）完成注释升级。使用容器内mysql客户端及本批upgrade_plan逻辑，凭证只在容器内使用，不输出或落盘。

| 库 | 实际表数 | 补表注释 | 补字段注释 |
| --- | --- | --- | --- |
| lemnt_member | 18 | 17 | 157 |
| lemnt_mall | 35 | 33 | 378 |
| lemnt_trade | 27 | 12 | 325 |
| lemnt_system | 17 | 0 | 0 |
| 合计 | 97 | 62 | 860 |

执行922条仅COMMENT变更，涉及80张表；system17张表注释完整，无需改写。实际库保留member_config/trade_config，已补充schema-comments-legacy.json纳入这两张已知历史表；没有执行配置迁移、赠券列升级或规格改名。

执行前建表定义、列属性快照及SQL计划位于 `/private/tmp/lemnt-schema-comments-20261102/` 的before-ddl.json、before-columns.json及plan.sql。临时驱动run.py分别执行prepare、apply、verify，结构快照不含业务行数据，不是全库业务数据备份。

实际复查PASS：97张表、1069个实际字段，空表注释和空字段注释均为0；原有非空注释保留，字段类型、精度、默认值、空值、字符集、排序规则、EXTRA和生成表达式逐项一致；索引、外键及CHECK逻辑一致。8张表的CHECK中MySQL将ASCII字面量字符集标记从_latin1重序列化为_utf8mb4，核对仅此标记不同，字段、运算符和字面值未变；原始DDL不是字节完全相同。

本节取代前文“未执行真实MySQL”的历史状态。未做服务部署/浏览器验收，未commit/push；仅执行本批注释DDL，不触碰业务行。
