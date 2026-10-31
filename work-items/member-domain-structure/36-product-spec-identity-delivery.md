# 商品规格字典关联交付

2026-10-28，承接35设计，继续使用code-software-engineer。参考本地芋道SKU规格存在、归属、重复与组合一致性校验，沿用当前六层结构及既有接口。本批是商品写读闭环，不是完整商城迁移完成。

## 已交付

1. 既有SKU创建/保存请求新增specValueIds（规格ID→值ID），domain、application DTO及独立web/app响应贯通。已有properties保留原名值兼容；同时提交非空名值时必须与服务端字典一致。
2. ProductCatalogService先按编号锁定字典，再解析规范名称、检查归属及各SKU相同规格项，最后检查重复组合并在同一事务保存SPU、SKU和规格行。未知编号、跨规格值、伪造名称或不同规格项集合被拒绝。
3. 现有mall_product_sku_spec增加property_id/value_id及索引，按表Gateway/DO/BaseMapper保存，原复杂查询XML的resultMap覆盖新列。ProductSkuSpec将名称与编号一次读取，应用层从同一批行生成两个视图，没有为每个视图重复查表。名称和编号逐条配对，不依赖Map迭代顺序。
4. 字典删除、改名引用检查优先按ID，旧未关联行仍按原名值保护；原来已引用不可改名的限制保留。历史规格不按同名自动关联。旧名值入口存在，但升级时需停止旧写者，防止覆盖新关联。
5. 后台SpuEditor改为选择已有规格与规格值，服务端决定名称。使用已有simple接口搜索，并按详情补入已选项，不假设首屏100条就是全部字典。新增mall:spec:query/mall:spec-value:query读取门禁。旧未关联商品提示人工选择并防止保存时误清空原规格。移除本批因界面调整不再使用的自由JSON解析函数。
6. 初始化SQL与独立升级脚本、接口文档和需求索引同步；本代理没有提交/推送/部署，也没有执行实际数据库升级。

接口：[product-spec-identity-api.md](../../docs/product-spec-identity-api.md)。升级：`lemnt-server/lemnt-mall/sql/upgrade/product_sku_spec_identity.sql`，人工核对版本、停写、备份后执行一次，禁止按名称回填历史ID。

## 验证

- 后端member/mall/trade及依赖：270项测试，失败/错误/跳过均0，最终verify通过，日志`/tmp/spec-final.log`。之前clean verify基线通过，最终删除无用import后再次package编译确认，日志`/tmp/spec-package-final.log`。
- 生产DDL、MyBatis-Plus、H2 MySQL模式及实际事务代理：字典编号和名称精确配对、单SKU/完整SPU/批量详情一致；错误归属/缺失ID/不同规格项/伪造名称拒绝；引用阻止字典删除/改名；旧规格保持未知直到显式选择；第二SKU写入失败后主体、首SKU及关联行全部回滚。
- 实际Jackson配置和Bean Validation：Long最大值映射精度保留，映射键0和值负数校验失败。App独立HTTP合同新增规格编号断言，未暴露后台商品字段。
- React169项测试通过，日志`/tmp/spec-ui-tests-final.log`；类型检查及构建、ESLint和Prettier通过，日志`/tmp/spec-ui-build-final.log`、`/tmp/spec-ui-lint-final.log`、`/tmp/spec-ui-format-final.log`。
- 小程序81项测试通过，日志`/tmp/spec-miniapp-tests.log`。仅保证额外响应字段兼容，未迁移新的小程序规格组件。
- git diff --check通过。来源代码、实库模式、序列化合同、前端测试与真实端到端分别记录；本次未验证真实MySQL/Redis/Dubbo/MQ、部署HTTP或浏览器/真机交互。

## 剩余差异

当前商品接口为properties名值映射加specValueIds编号映射，与芋道properties的Property对象列表尚未完全同构。结算、订单和售后仍保存上一批名值快照，本批未为其历史资料猜测编号，完整Property列表跨服务贯通尚待迁移。其余订单金额/状态、售后资料、完整App业务及业务缺表继续按27设计推进；本批未新增表，初始化46张表、仍缺36张业务对应表（支付demo排除）。真实渠道SDK仍按用户决定后置。

交接QA：重点检查字典读取权限、首屏之外已选项展示、旧商品人工关联提示、多SKU变更/删除/重新排序，以及真实数据库升级与部署联调；这些不能由本轮静态和隔离测试代替。
