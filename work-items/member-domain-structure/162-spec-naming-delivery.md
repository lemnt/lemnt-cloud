# 第162批：spec命名统一交付

2026-11-02，v1；责任Skill：code-software-engineer。输入：[本批设计](162-spec-naming-design.md)及用户spec命名要求。基线a457e0e7，保留160/161批变更；本轮未commit/push。

## 实现

- 商品SKU、结算、订单明细、售后领域/应用/HTTP/Dubbo各层使用specs和specId/specName/specValueId/specValueName，嵌套类型统一Spec。原ProductSkuSpec类型和字典ProductSpec/ProductSpecValue术语保留。
- 三张业务表的规格列统一specs，更新DO、Converter、Mapper及新库DDL。提供两份旧库rename脚本，不修改旧历史升级文件、不实际改库。
- 持久化读取兼容原四字段JSON及更早的名值对象；不查当前商品回填历史，不混淆订单/售后的null和[]。商品新写、订单新快照、售后新快照统一输出新名。
- 评论下游元素同步新名，其skuProperties/sku_properties外层不扩展改名。后台商品编辑/显示、结算、订单和售后解码/显示、小程序商品及评论同步。
- 预览缓存升v5，提交回执v2保留；兼容旧properties字段和旧元素名，不改回执编号、原指纹和幂等索引。

## 验证

后端先clean编译，再执行：

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home \
/opt/homebrew/opt/maven/bin/mvn -o -f lemnt-server/pom.xml \
  -pl lemnt-mall/lemnt-mall-start,lemnt-trade/lemnt-trade-start -am test \
  -DargLine=-javaagent:/Users/wangxx/.m2/repository/org/mockito/mockito-core/5.23.0/mockito-core-5.23.0.jar
```

| 检查 | 结果 |
| --- | --- |
| mall全模块 | PASS，195项 |
| trade全模块 | PASS，283项 |
| React | PASS，315项 |
| TypeScript检查及后台构建 | PASS |
| 小程序Node测试 | PASS，176项 |
| git diff --check | PASS |

后端478项0失败/错误/跳过。覆盖实际序列化/HTTP新合同、字典ID校验、商品改名不影响历史订单和售后、旧列表保留编号、旧名值保留未知ID、旧回执恢复、SKU列改名后真实Mapper读取及101项结算支付售后集成。原始日志：`/private/tmp/spec-naming-full-tests.log`、`/private/tmp/spec-ui-tests.log`、`/private/tmp/spec-ui-typecheck.log`、`/private/tmp/spec-miniapp-tests.log`；前端仅对本批涉及文件执行Prettier。

验证详情：[spec-naming-verification.json](evidence/spec-naming-verification.json)。后台构建日志：`/private/tmp/spec-ui-build.log`。

## 发布和限制

[规格命名迁移说明](../../docs/spec-naming-migration.md)列出新合同、现行接口文档、SQL、缓存、发布和回滚边界。当前有效接口文档已同步；历史工作项和源对照文档不批量改写。

NOT RUN：真实MySQL升级、部署后Dubbo/网关、浏览器及真机。旧JSON只在读取时规范，不宣称已执行全库JSON重写；列名和服务/调用端需配套切换。下一接收方为QA/用户实际环境验收，不代替人工批准。
