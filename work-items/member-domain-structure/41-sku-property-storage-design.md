# SKU规格存储合并设计

2026-10-28，承接39/40，code-software-engineer，沿用已读取设计/API示例及AGENTS。来源：本地芋道ProductSkuDO.properties、ProductSkuServiceImpl.updateSkuProperty/updateSkuPropertyValue、规格字典两个Service。源代码确认规格列表随SKU保存；字典改名同步当前SKU，删除规格值不清除SKU快照，规格项仍有子值时不可删除。本地参考仓库SQL未含product_sku建表，不能声称已核对源列长度。

目标：将额外mall_product_sku_spec关系表合并为mall_product_sku.properties内的四字段JSON列表。沿用当前JSON字段的String DO+Converter编码惯例，物理用LONGTEXT，不冒充已验证源列长度。ProductSkuGateway一次读取/写入规格及主体，移除独立规格行DO/Mapper/Gateway/XML，不新增其他表。

任务：
1. ProductSkuDO新增properties；Converter一次解析/编码；ProductSkuMapper.resultMap覆盖；BaseMapper/Lambda更新同时写主体和properties。ProductCatalogService、批量详情和结算目录直接使用SKU列表，去掉重复读写关系表组件。
2. 按源行为删除“已引用不能改名/删值”的额外限制。ProductSpecService改名后通过SKU Gateway按ID更新当前SKU相应名称，保留规格ID/值ID、价格库存及已有订单/售后；删除值只删字典，不擦SKU快照。规格项有子值时仍拒绝删除。名称同步与字典写入在同一事务，失败整体回滚。
3. 名称同步扫描非删除SKU的已有JSON，按SKU编号顺序锁定后重读，避免用扫描时旧值覆盖并发编辑；只更新实际引用目标ID的行。当前名称变体键仍随规格内容重算，冲突回滚，不改历史订单键。未知旧编号不按同名猜测同步。
4. 初始化V2改为SKU增加properties，不再建关系表；V16移除额外的自动猜测建字典逻辑。存量库提供合并脚本（需已有前序property_id/value_id列，保留NULL），先聚合复制并核对数量，再单独审核执行旧表删除脚本。不执行真实DDL，MySQL函数迁移SQL与H2业务测试分别说明，不把生成SQL当执行成功。
5. 更新生产DDL测试和所有关系表直写夹具；新增字典改名同步/失败回滚、删字典保留SKU快照、当前SKU变化不改已下单/售后、代码无旧表引用。三模块、后台、小程序回归。接口Property列表不变，无需增加HTTP入参/返回字段。

约束：新旧写者不可混跑；合并和旧表删除分步骤，删除前备份并核对每SKU的ID/名称/值，不能只看总行数。初始化表数应从46减少为45；源业务缺表仍36，不宣称缺表已补齐。真正MySQL、Redis/Dubbo/MQ、部署HTTP/网关认证/浏览器真机为独立验收。无commit/push/部署/真实升级。
