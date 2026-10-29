# 订单和售后商品快照

2026-10-28，规格列表贯通修订。承接售后标准表与日志批次。以下是接口合同及构造示例，未进行部署 HTTP 或浏览器端到端验收。

商品快照在报价时读取目录，并贯通优惠分摊、运费、Redis 结算、交易订单项和售后申请。修改商品目录不修改历史订单。后台详情显示原商品信息；历史未记录字段显示“未记录”。

## 返回字段

以下字段增加到已有的独立商品行响应，不改变成功 R、分页顶层 rows/total，也不增加客户端可提交的商品元数据参数。

| 字段 | 类型 | 含义 |
|---|---|---|
| productId | long/string | 现有接口保留的 SKU 编号别名，订单表实际列为 sku_id |
| spuId | long/string/null | 下单商品所属 SPU |
| spuName | string/null | 下单时 SPU 名称 |
| properties | array/null | 完整规格快照列表，元素为下表四个字段；null 为历史未知，[] 为已知无规格 |
| properties[].propertyId | long/string/null | 保存时规格项编号，旧资料未关联时为空或省略 |
| properties[].propertyName | string | 保存时规格名称 |
| properties[].valueId | long/string/null | 保存时规格值编号，旧资料未关联时为空或省略 |
| properties[].valueName | string | 保存时规格值名称 |
| picUrl | string/null | 下单采用的图片：SKU 图片优先，空时采用已有 SPU 图片 |

售后申请、详情、分页和处理结果额外返回 skuId，来源为原订单项；可申请商品额度返回上述 productId 及快照。编号由前端按字符串保存，不转换为浮点数。properties 已调整为包含 propertyId/propertyName/valueId/valueName 的规格对象列表；来自 SKU 已保存的字典关联与名称，不回查当前字典补写历史编号。旧名值 JSON 兼容转换成列表，但两个编号保持为空。商品管理已在39/40批次统一为properties规格对象列表，写入时提供编号、服务端读取名称，详见商品规格关联文档。

相关已有接口：

| 接口 | 读取内容 | 权限 |
|---|---|---|
| GET /trade/admin/orders/{no} | items 中的原订单商品快照 | 沿用订单查询权限 |
| GET /trade/admin/after-sales/{no} | 售后创建时复制的商品快照 | trade:after-sale:query |
| GET /trade/admin/after-sales | 分页中的售后快照 | trade:after-sale:list |

Authorization 必填 Header；no 必填 Path，使用已有业务单号。查询无 Body。不增加新的写接口；报价/提单及售后请求体沿用原定义，不允许提交 SPU、规格、图片覆盖服务端快照。

订单商品行示例（放在原响应 data.items 中）：

```json
{"productId":"7","productCode":"SKU-7","productName":"黑色L码","unitPrice":"10.00","quantity":2,"lineAmount":"20.00","spuId":"3","spuName":"原商品","properties":[{"propertyId":"11","propertyName":"颜色","valueId":"21","valueName":"黑色"},{"propertyId":"12","propertyName":"尺码","valueId":"22","valueName":"L"}],"picUrl":"https://example.com/original.png"}
```

缓存预览键升至 `mall:settlement:v4:`，未提交旧预览需重新结算；提交回执及幂等恢复键 `mall:{settlement}:v2:` 保留，读取旧回执时只转换 order.items.properties 名值对象，不改写索引、订单号、原指纹或请求编号。规格编号单独改变、名称和值不变也会导致旧报价被拒绝。Redis 结算单提交时重新比较完整报价，变化沿用 ServiceException 409；旧直接购物车提单路径沿用 IllegalStateException，未将其错误码等同于 Redis 提单路径。成功合同保持原状，校验仍走统一异常处理。

## 数据库升级

初始化 SQL 已同步。存量库先完成上一批 after_sale_identity_and_log.sql，再人工核对并执行 trade/sql/upgrade/order_product_snapshot.sql；本次未执行真实库升级。脚本将原订单项 product_id 改名为 sku_id，增加 SPU/规格/图片，售后 SKU 仅从关联原订单项复制。不能与旧版写入服务同时运行。原来没有保存的 SPU/规格/图片保持 NULL，禁止从当前商品目录回填。


## 本次规格列表升级

当前复用原 trade_order_item.properties 和 trade_after_sale.properties JSON列，不增加新表或列，不需为本次列表格式执行额外DDL。先前基础字段、字典关联等升级脚本仍需按实际库版本执行，本文不代表已执行。新订单写列表，读取旧JSON对象仅保留已记录名值，读取旧NULL仍为NULL。

接口properties类型从object变为array，需要同步发布mall/trade服务及调用端；不能让不支持列表的旧写入/读取服务与新版混跑。后台解码同时识别旧名值映射，统一显示规格列表，编号按字符串保存，编号只缺一项的列表判定为格式错误。请求中仍不允许通过商品元数据覆盖服务端快照。

本次检查是本地生产DDL、事务、序列化和前端隔离测试，未执行真实Redis/MySQL/Dubbo/MQ或部署HTTP、浏览器真机端到端。完整迁移进度见38交付。

订单项评价补充：持久化后返回真实 `orderItemId` 和可空 `commentStatus`，订单含可空评价状态及完成时间。报价不生成这些数据库元数据；它们不影响购买快照或订单创建幂等比较。详见[订单项评价联动](order-item-comment-api.md)。
