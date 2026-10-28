# 完整规格快照列表贯通设计

2026-10-28，承接35/36；code-software-engineer，沿用已读取的development-design/api-documentation示例和仓库AGENTS。对照本地芋道TradeOrderItemDO/AfterSaleDO中的properties列表，商品目录实际关联来自ProductSkuSpec及已保存的字典编号，不查询当前字典改写旧快照。

目标：结算、订单、售后的properties使用独立HTTP/DTO/领域嵌套Property列表，字段propertyId/propertyName/valueId/valueName。不同层不直接传领域对象。目录SKU管理的properties映射/specValueIds选择合同保持本批原状，目录读取时将同一行编号与名称构造完整列表。旧未关联行保留名值、两个编号均为空，未知整组null与已知无规格[]区分。

任务：
1. 变更ProductSnapshot、SettlementItem及trade客户端/订单/售后各层独立模型与Converter，逐一检查生产构造/拷贝，不依赖兼容构造器丢字段；计价、积分、运费必须完整转发。
2. 订单项及售后的现有properties JSON列保存列表，不增加表或列；基础设施Converter兼容旧JSON对象，只还原已保存名值，不查商品字典猜测ID。读取原订单项创建售后，编号和名称一起固化。
3. 报价摘要按规格编号/名称稳定排序并包含四字段，变编号同名也拒绝旧报价。Redis预览键v3→v4，提交恢复v2保留，读取旧submission时只转换其order.items.properties名值对象，不能改写幂等索引或任意业务JSON。
4. RPC校验编号成对且为正数、名称和值符合长度，拒绝重复规格。React结算/订单/售后解码和显示列表，编号按字符串处理；支持读取旧名值映射以便滚动展示，缺失值不伪造。
5. 验证真实双库模式下字典关联SKU→结算→订单→售后，换商品当前名称/值不改历史，变规格编号同名使旧报价失效，存量JSON及提交回执可恢复；生产序列化、计价及运费、HTTP合同、三个模块/后台/小程序回归。

API输出properties由object调整为array，客户端需同步部署；请求不新增可覆盖商品快照的参数。成功R、分页rows/total和统一异常保持原合同，旧订单未知规格保持null。构造示例与隔离测试分开记录，真实库/Redis/MQ/Dubbo/部署HTTP/浏览器真机未验证时不可声称闭环验收。无commit/push/实际DDL。其余字段及36张业务缺表仍待迁移。
