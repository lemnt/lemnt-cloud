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
