# 商品规格字典关联设计

2026-10-28，承接33/34；code-software-engineer，参考已读取的references/development-design.md及api-documentation.md。来源为本地芋道ProductSkuServiceImpl.validateSkuList与现有SKU/规格字典代码，沿用六层架构与system风格。无新原型，继续现有后台编辑器。

现状：mall_product_spec/value已有主键，但SKU只存名称和值，无法验证规格值归属。目标：让后台选择真实字典编号，服务器读取规范名称并写入现有SKU规格关系表；既有订单/售后名值快照不被目录变化重写。本批先完成商品写入和读取闭环，不冒充跨服务订单的Property ID结构已完成。

方案：在现有SKU请求/领域/DTO/响应新增specValueIds（规格ID→值ID），已有properties名称映射保留旧调用兼容。新后台使用编号选择；properties由服务器读取字典生成。若同时提交非空名称映射必须与字典一致，拒绝伪造、跨规格值、同一商品不同规格编号集合。旧未关联名值保持不关联，禁止通过同名猜测历史ID。无规格使用空集合，旧未知关系为null。关系表增加property_id/value_id可空列及索引，不增加表。

ProductCatalogService在校验重复组合前解析并锁定字典，按编号排序锁规格再锁值，统一事务中写SPU/SKU/关系。saveSpecs接收含编号和名称的类型化条目，逐条写入，不按Map顺序配对，旧saveProperties入口只写原名值。读取同SPU批量返回编号映射，任一旧属性缺编号时整组返回null。删除字典按ID引用保护并保留旧未关联名值保护。现有已引用不能改名的限制暂时保留，不在这批更改字典更名规则。

任务与验收：
1. domain ProductSku、draft、独立Req/Res/DTO传递specValueIds，Gateway/DO/Mapper resultMap与初始化/独立升级SQL覆盖ID；检查全部构造复制。无应用层访问Mapper。
2. 应用层在newSpu组合校验前解析字典编号，批量商品保存任一SKU失败整体回滚；未知字典、错归属、不同编号集合、伪造名称都拒绝；原旧接口保持兼容。
3. 后台SpuEditor用规格/规格值选择替代新商品的自由JSON，旧未关联数据提示需人工选择，不能静默清空；按现有simple接口查询，权限mall:spec:query、mall:spec-value:query。
4. 实库模式集成测试覆盖ID持久化/读取/更新、失败回滚与字典删除保护；React类型/解码/请求映射测试；三模块回归、构建和格式检查。

成功返回仍R，分页rows/total；新增字段可空，Long键/值客户端使用十进制字符串。业务校验使用统一ServiceException400/409，无新异常处理器。网关认证、交易流程、缓存、MQ不变。升级只生成不执行，原历史NULL不自动回填，旧写者需停写后部署。未提交/推送/部署。完整结构化订单快照、其余源字段与36张业务缺表继续待迁移。
