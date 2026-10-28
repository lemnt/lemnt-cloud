# 订单和售后商品快照

2026-10-28。承接售后标准表与日志批次。以下是接口合同及构造示例，未进行部署 HTTP 或浏览器端到端验收。

商品快照在报价时读取目录，并贯通优惠分摊、运费、Redis 结算、交易订单项和售后申请。修改商品目录不修改历史订单。后台详情显示原商品信息；历史未记录字段显示“未记录”。

## 返回字段

以下字段增加到已有的独立商品行响应，不改变成功 R、分页顶层 rows/total，也不增加客户端可提交的商品元数据参数。

| 字段 | 类型 | 含义 |
|---|---|---|
| productId | long/string | 现有接口保留的 SKU 编号别名，订单表实际列为 sku_id |
| spuId | long/string/null | 下单商品所属 SPU |
| spuName | string/null | 下单时 SPU 名称 |
| properties | object/null | 规格名称和值，例 `{"颜色":"黑色"}`；null 为历史未知，{} 为已知无规格 |
| picUrl | string/null | 下单采用的图片：SKU 图片优先，空时采用已有 SPU 图片 |

售后申请、详情、分页和处理结果额外返回 skuId，来源为原订单项；可申请商品额度返回上述 productId 及快照。编号由前端按字符串保存，不转换为浮点数。properties 当前不是源项目的 propertyId/valueId 列表，规格字典 ID 关系尚待完整迁移，不能宣称字段结构完全一致。

相关已有接口：

| 接口 | 读取内容 | 权限 |
|---|---|---|
| GET /trade/admin/orders/{no} | items 中的原订单商品快照 | 沿用订单查询权限 |
| GET /trade/admin/after-sales/{no} | 售后创建时复制的商品快照 | trade:after-sale:query |
| GET /trade/admin/after-sales | 分页中的售后快照 | trade:after-sale:list |

Authorization 必填 Header；no 必填 Path，使用已有业务单号。查询无 Body。不增加新的写接口；报价/提单及售后请求体沿用原定义，不允许提交 SPU、规格、图片覆盖服务端快照。

订单商品行示例（放在原响应 data.items 中）：

```json
{"productId":"7","productCode":"SKU-7","productName":"黑色L码","unitPrice":"10.00","quantity":2,"lineAmount":"20.00","spuId":"3","spuName":"原商品","properties":{"颜色":"黑色","尺码":"L"},"picUrl":"https://example.com/original.png"}
```

缓存预览键升至 `mall:settlement:v3:`，未提交旧预览需重新结算；提交回执及幂等恢复键 `mall:{settlement}:v2:` 保留。Redis 结算单提交时重新比较完整报价，变化沿用 ServiceException 409；旧直接购物车提单路径沿用 IllegalStateException，未将其错误码等同于 Redis 提单路径。成功合同保持原状，校验仍走统一异常处理。

## 数据库升级

初始化 SQL 已同步。存量库先完成上一批 after_sale_identity_and_log.sql，再人工核对并执行 trade/sql/upgrade/order_product_snapshot.sql；本次未执行真实库升级。脚本将原订单项 product_id 改名为 sku_id，增加 SPU/规格/图片，售后 SKU 仅从关联原订单项复制。不能与旧版写入服务同时运行。原来没有保存的 SPU/规格/图片保持 NULL，禁止从当前商品目录回填。
