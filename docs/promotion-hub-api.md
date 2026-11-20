# App 活动聚合与营销规则接口

2026-11-01。聚合接口与折扣/满减详情均为公开读接口；活动只返回已开启且当前有效的记录。

## 接口

| 方法与路径 | 功能 |
| --- | --- |
| GET /mall/app/promotion-hub/sections?limit=10 | 返回七个分区中的六个：秒杀、拼团、砍价、积分商城、限时折扣、满减送 |
| GET /mall/app/promotion-hub/discount/{id} | 限时折扣详情，含 SPU/SKU、折扣类型、基点或减价金额 |
| GET /mall/app/promotion-hub/reward/{id} | 满减送详情，含条件类型、商品范围和规则 |

`sections` 每项：`type`、`label`、`items[{id,name,startTime,endTime}]`；积分商城无起止时间，`startTime/endTime` 为空。`limit` 范围 1–50。

## 满减规则

规则字段：

```json
{
  "limit": "5.00",
  "discountPrice": "1.00",
  "freeDelivery": true,
  "point": 10,
  "giveCouponTemplateCounts": { "1": 1 }
}
```

- `limit` 在 `PRICE` 条件为金额、`COUNT` 条件为件数。
- `freeDelivery`、`point`、赠券目前完成配置、后台页面和小程序展示。
- 结算计价当前应用金额减免；包邮、赠积分、赠券的结算与支付后派发尚未接入，见交付文档的未覆盖项。
