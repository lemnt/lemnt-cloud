# 规格字段统一命名

2026-11-02，第162批。商品字典原本已称ProductSpec/ProductSpecValue、SKU规格类型称ProductSkuSpec，本批将商品、结算、订单及售后快照的字段统一为spec术语。

## 字段合同

| 原字段 | 新字段 | 含义 |
| --- | --- | --- |
| properties | specs | 规格对象数组 |
| properties[].propertyId | specs[].specId | 规格项编号 |
| properties[].propertyName | specs[].specName | 规格项名称 |
| properties[].valueId | specs[].specValueId | 规格值编号 |
| properties[].valueName | specs[].specValueName | 规格值名称 |

商品SKU新增/保存/详情、SPU的SKU列表、结算项、订单明细、售后可退商品及售后详情/列表/处理结果均使用上述合同。嵌套Java类型改为Spec，各层仍独立定义。评论作为订单规格的下游同步元素四字段，原外层skuProperties和数据库sku_properties未改名。

商品写入时specId/specValueId必填正数；specName/specValueName可省略，由服务端字典读取，提供不一致名称仍拒绝。订单和售后请求不允许提交规格覆盖服务端快照。数组顺序、字典归属和重复项校验保持原规则。

完整元素示例（构造数据，非本轮真实HTTP验收）：

```json
{
  "specs": [
    {
      "specId": "3",
      "specName": "颜色",
      "specValueId": "7",
      "specValueName": "黑色"
    }
  ]
}
```

ID沿用现有HTTP长整数序列化配置，前端按字符串处理。旧未关联规格的两个编号保持null，不能按名称猜测编号。订单/售后的specs=null表示历史未知，[]=已知无规格；商品现有无规格读取保持[]。外层R、权限、金额及其他字段不变。完整请求/响应示例见本批更新的商品管理、结算、订单快照和履约接口文档。

## 存储与升级

新库三个列名为：mall_product_sku.specs、trade_order_item.specs、trade_after_sale.specs，仍为LONGTEXT。旧库在备份、停旧写者并完成前序版本升级后，各执行一次：

- mall：`lemnt-server/lemnt-mall/sql/upgrade/sku_specs_naming.sql`
- trade：`lemnt-server/lemnt-trade/sql/upgrade/order_aftersale_specs_naming.sql`

仅改列名，不批量重写历史JSON。基础设施读取同时兼容旧四字段列表和更早名值对象，返回新规格对象；新保存只写spec*字段。商品字典改名只影响当前SKU，订单和售后继续读取保存时快照。

本轮未执行现有MySQL迁移。字段/列名和Dubbo契约均有不兼容变化，mall、trade、后台、小程序和其他消费者需配套发布；旧HTTP写合同不作为双写别名继续维护。发布前执行干净构建，避免残留旧类型资源。

## 缓存与回滚

预览缓存前缀升级至mall:settlement:v5:，旧预览需重新报价；提交回执及幂等索引mall:{settlement}:v2:保持。读取历史回执仅规范order.items内的规格外层及元素字段，不改原订单号、请求号、指纹或索引，也不查询当前商品。

回滚前停写并备份；若已写入spec* JSON，需恢复旧列名并转换这些新JSON及回执后才能启用旧写者，不能直接混跑。历史原始JSON的读取兼容不等于旧服务能理解新数据。

验证分层及数量见[第162批交付](../work-items/member-domain-structure/162-spec-naming-delivery.md)。隔离H2/HTTP测试不代替真实MySQL、网关、跨进程Dubbo或浏览器/真机验收。
