# 第162批：商品、订单与售后规格命名统一

2026-11-02，v1；责任Skill：code-software-engineer；基线a457e0e7，保留160/161批未提交变更。输入：用户要求将商品属性值、订单明细与退单属性值统一称为spec；已核实商品字典ProductSpec/ProductSpecValue与SKU类型ProductSkuSpec采用spec术语，快照字段仍为properties。

## 合同

统一商品SKU、结算、订单明细、售后快照的列表为 `specs`，元素为 `specId/specName/specValueId/specValueName`；嵌套类型Property改为Spec，独立层模型仍各自定义，不将领域模型直接暴露HTTP。评论因接收原订单快照同步元素命名，既有外层skuProperties/sku_properties不扩展改名范围。

数据库仅将mall_product_sku、trade_order_item、trade_after_sale的properties列改为specs，仍为LONGTEXT；新写JSON使用新四字段。旧列表四字段及更早的名值对象在基础设施读取时转换，不从当前字典回填编号或改写历史。null未知与[]已知无规格语义保留。

商品写入继续校验字典归属和ID，前端商品编辑、结算、订单、售后及小程序同步新合同。旧HTTP写合同不新增别名；需服务与调用端配套发布。预览缓存升v5要求未提交报价重算，提交回执v2保留且只规范order.items规格字段，不改索引、原指纹或订单编号。

## 任务与验收

1. mall/trade各层及Converter按新名传递，更新新库DDL/XML及旧库rename脚本。
2. 持久化与回执读取兼容旧名，验证历史快照、未知ID与空列表；商品改名不能修改订单或售后。
3. React/小程序调用、解码、展示和测试同步；旧名值展示兼容保留，客户端不继续输出旧合同。
4. 同步现行接口文档及需求索引，运行后端合同/链路回归、前端类型检查和测试、小程序测试。真实MySQL迁移和部署/真机单列未执行。

沿用JDK17、Spring Boot4.1、Fastjson2、MyBatis-Plus、React/TypeScript及现有构建；参考工程Skill既有开发/API示例及Java/TypeScript规范。本批不改字典数据库列、不清理无关Spring配置、不提交/部署。SQL仅改列名，旧JSON由读适配保留，回滚旧写者前必须停写并转换新JSON/列名，不能直接混跑。
